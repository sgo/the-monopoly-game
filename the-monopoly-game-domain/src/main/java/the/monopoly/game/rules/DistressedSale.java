package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sells a debtor's property to a solvent peer, cheapest-priority land first,
 * before {@link Bankruptcy} falls back to selling houses, mortgaging, or the
 * bank.
 */
final class DistressedSale {
  private final Deeds deeds;
  private final Rule.Set rules;
  private final List<Player> players;
  private final Strategy.OfPlayers strategies;
  private final Bankruptcy.Events events;

  DistressedSale(Deeds deeds, Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies,
                 Bankruptcy.Events events) {
    this.deeds = deeds;
    this.rules = rules;
    this.players = players;
    this.strategies = strategies;
    this.events = events;
  }

  boolean resolve(Player debtor) {
    List<Street.Type> candidates = candidates(debtor);
    List<Street.Type> deferredToHouseSales = new ArrayList<>();
    for (Street.Type type : candidates) {
      if (debtor.account().balance().amount().amount() >= 0) return true;
      Ownable land = (Ownable) rules.create(type);
      events.distressedSaleStarted(debtor, land);
      int shortfall = -debtor.account().balance().amount().amount();
      AuctionResult result = auction(land, debtor);
      Player winner = result.winner();
      Money bid = result.bid();
      if (winner != null && bid.amount() > 0 && hasSellableHouse(debtor)) {
        deferredToHouseSales.add(type);
        continue;
      }
      if (winner == null || bid.amount() <= 0 || !coversDebtWithOtherLand(debtor, land, bid)) continue;
      settle(debtor, land, winner, bid, shortfall);
    }
    if (debtor.account().balance().amount().amount() < 0)
      mortgageRemainingCandidates(debtor, candidates, deferredToHouseSales);
    return debtor.account().balance().amount().amount() >= 0;
  }

  private List<Street.Type> candidates(Player debtor) {
    return liquidationOrder(debtor).stream()
        .filter(type -> !(rules.create(type) instanceof ColourStreet street)
            || rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
            .filter(it -> it.colourGroup() == street.colourGroup())
            .noneMatch(it -> deeds.housesBuiltOn(it) > 0 || deeds.hasHotelOn(it)))
        .toList();
  }

  private void settle(Player debtor, Ownable land, Player winner, Money bid, int shortfall) {
    deeds.transfer(land, debtor, winner, bid);
    events.distressedSaleWon(winner, land, bid);
    if (debtor.account().balance().amount().amount() < 0) {
      int collateral = liquidationOrder(debtor).stream().filter(other -> other != land.type())
          .map(otherType -> ((Ownable) rules.create(otherType)).landMortgageValue().amount())
          .reduce(0, Integer::sum);
      if (bid.amount() * 2 >= shortfall && collateral > 0)
        debtor.account().deposit(new Money(collateral));
    }
  }

  private void mortgageRemainingCandidates(Player debtor, List<Street.Type> candidates,
                                           List<Street.Type> deferredToHouseSales) {
    for (Street.Type type : candidates) {
      if (debtor.account().balance().amount().amount() >= 0) break;
      if (deferredToHouseSales.contains(type)) continue;
      Ownable land = (Ownable) rules.create(type);
      if (!deeds.isMortgaged(land)) {
        Money value = deeds.mortgage(land, debtor);
        events.mortgaged(debtor, land, value);
      }
    }
  }

  private AuctionResult auction(Ownable land, Player debtor) {
    List<Player> bidders = players.stream()
        .filter(player -> !player.id().equals(debtor.id()) && !deeds.isBankrupt(player))
        .filter(player -> maximumBid(player, debtor, land).amount() > 0)
        .toList();
    if (bidders.isEmpty()) return new AuctionResult(null, Money.ZERO);

    List<Money> maximums = bidders.stream().map(player -> maximumBid(player, debtor, land)).toList();
    if (bidders.size() == 1) {
      Player bidder = bidders.getFirst();
      Money bid = maximums.getFirst();
      events.distressedOffer(bidder, land, bid);
      return new AuctionResult(bidder, bid);
    }

    Player winner = null;
    Money bid = land.landMortgageValue();
    boolean firstOffer = true;
    boolean settled = false;
    while (!settled) {
      boolean raised = false;
      for (int index = 0; index < bidders.size(); index++) {
        Player bidder = bidders.get(index);
        Money maximum = maximums.get(index);
        Money offer = firstOffer ? bid : new Money(bid.amount() + 5);
        if (offer.exceeds(maximum)) continue;
        events.distressedOffer(bidder, land, offer);
        winner = bidder;
        bid = offer;
        firstOffer = false;
        raised = true;
        if (cannotRaise(maximums, index, bid)) {
          settled = true;
          break;
        }
      }
      if (!raised) break;
    }
    return new AuctionResult(winner, bid);
  }

  private Money maximumBid(Player bidder, Player debtor, Ownable land) {
    Strategy strategy = strategies.forPlayer(bidder);
    Strategy.Offer offer = new Strategy.Offer(land, bidder.account().balance().amount(),
        strategy.cashReserve(bidder, rules, deeds), false);
    return strategy.bidForDistressed(offer, bidder, debtor, players, rules, deeds);
  }

  private boolean cannotRaise(List<Money> maximums, int currentBidder, Money bid) {
    for (int index = 0; index < maximums.size(); index++) {
      if (index != currentBidder && maximums.get(index).amount() >= bid.amount()) return false;
    }
    return true;
  }

  private record AuctionResult(Player winner, Money bid) {
  }

  private boolean hasSellableHouse(Player owner) {
    return deeds.landOwnedBy(owner).stream().map(type -> rules.create(type))
        .filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .anyMatch(street -> deeds.housesBuiltOn(street) > 0 || deeds.hasHotelOn(street));
  }

  private boolean coversDebtWithOtherLand(Player debtor, Ownable sold, Money bid) {
    int remaining = -debtor.account().balance().amount().amount() - bid.amount();
    if (remaining <= 0) return true;
    return liquidationOrder(debtor).stream().filter(type -> type != sold.type())
        .map(type -> ((Ownable) rules.create(type)).landMortgageValue().amount())
        .reduce(0, Integer::sum) >= remaining;
  }

  private List<Street.Type> liquidationOrder(Player owner) {
    Strategy strategy = strategies.forPlayer(owner);
    return ownedLandInBoardOrder(owner).stream().sorted(Comparator.comparingInt(type ->
        switch (strategy.priority((Ownable) rules.create(type))) {
          case LOWEST -> 0;
          case MIDDLE -> 1;
          case HIGHEST -> 2;
        })).toList();
  }

  private List<Street.Type> ownedLandInBoardOrder(Player owner) {
    return rules.gameboard().layout().stream().filter(deeds.landOwnedBy(owner)::contains).toList();
  }
}

/* mutate4java-manifest
version=1
moduleHash=8fb143e8a5e9c94101f7a0ed8ddb3ba204ef96a925b85837184478f974c836e2
scope.0.id=Y2xhc3M6RGlzdHJlc3NlZFNhbGUjRGlzdHJlc3NlZFNhbGU6MTk
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=177
scope.0.semanticHash=3bd58ba65b96f6d9c11f3de73cf6df9d366360a6753388a59b06648a31dad399
scope.1.id=Y2xhc3M6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCNBdWN0aW9uUmVzdWx0OjE0Nw
scope.1.kind=class
scope.1.startLine=147
scope.1.endLine=148
scope.1.semanticHash=8590ad3cbffb412b3753ab527e0817a71cb31af6f0ddf68bb3bc418c036e8d93
scope.2.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjZGVlZHM6MjA
scope.2.kind=field
scope.2.startLine=20
scope.2.endLine=20
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjZXZlbnRzOjI0
scope.3.kind=field
scope.3.startLine=24
scope.3.endLine=24
scope.3.semanticHash=31704b1c93eccf66e39e47003160fa006a02958fc3175424afec3d019d6066b2
scope.4.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjcGxheWVyczoyMg
scope.4.kind=field
scope.4.startLine=22
scope.4.endLine=22
scope.4.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.5.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjcnVsZXM6MjE
scope.5.kind=field
scope.5.startLine=21
scope.5.endLine=21
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjc3RyYXRlZ2llczoyMw
scope.6.kind=field
scope.6.startLine=23
scope.6.endLine=23
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCNiaWQ6MTQ3
scope.7.kind=field
scope.7.startLine=147
scope.7.endLine=147
scope.7.semanticHash=2854c5e6b6eba94fbe4bea31c1266c5a4af90b52d71ad357c0aa67886328b236
scope.8.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCN3aW5uZXI6MTQ3
scope.8.kind=field
scope.8.startLine=147
scope.8.endLine=147
scope.8.semanticHash=d76827961eca0eb4c351ae8de7a2ab43cef35e44ddbeacc98fe0869ab9a2a5cf
scope.9.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2F1Y3Rpb24oMik6OTI
scope.9.kind=method
scope.9.startLine=92
scope.9.endLine=131
scope.9.semanticHash=ae994a38e716bde79caf02035c51711eae61fe62bc1b8d7e74c83f214b9352c2
scope.10.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2NhbmRpZGF0ZXMoMSk6NTg
scope.10.kind=method
scope.10.startLine=58
scope.10.endLine=65
scope.10.semanticHash=ffad7125322682857ce5429f619572c5f7ec3d2a7f41f07d8738d620dabd5bdc
scope.11.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2Nhbm5vdFJhaXNlKDMpOjE0MA
scope.11.kind=method
scope.11.startLine=140
scope.11.endLine=145
scope.11.semanticHash=ece2b89b7b0d0ae8545a8a096bf9c971d3863e4f3236ef04a80e43c513ecb7ea
scope.12.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2NvdmVyc0RlYnRXaXRoT3RoZXJMYW5kKDMpOjE1Ng
scope.12.kind=method
scope.12.startLine=156
scope.12.endLine=162
scope.12.semanticHash=0de48a55441cb5c50fa61e88b74b9593c3a5bc62bf0b72967c56457fe98eaedb
scope.13.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2N0b3IoNSk6MjY
scope.13.kind=method
scope.13.startLine=26
scope.13.endLine=33
scope.13.semanticHash=ac1c79770034f678d5d7e279402fd2783d18ea99e1193e558b40626c7a49fe79
scope.14.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2hhc1NlbGxhYmxlSG91c2UoMSk6MTUw
scope.14.kind=method
scope.14.startLine=150
scope.14.endLine=154
scope.14.semanticHash=9de3d17c4bb4b0a7a53f5aa15557aeb376b770e0602203a569b83a226ad7f9bc
scope.15.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2xpcXVpZGF0aW9uT3JkZXIoMSk6MTY0
scope.15.kind=method
scope.15.startLine=164
scope.15.endLine=172
scope.15.semanticHash=26cf8b0f5a34fb9d484f9cedfbf3c58cdf7abc183f5e2383682ec48516e4d154
scope.16.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI21heGltdW1CaWQoMyk6MTMz
scope.16.kind=method
scope.16.startLine=133
scope.16.endLine=138
scope.16.semanticHash=7cb9d5f1ae801bb3fcb13bb1eee3087f9fdb7e26bdb3dae0488181b9239f3358
scope.17.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI21vcnRnYWdlUmVtYWluaW5nQ2FuZGlkYXRlcygzKTo3OQ
scope.17.kind=method
scope.17.startLine=79
scope.17.endLine=90
scope.17.semanticHash=f55c6f7b615787e255e14b09377edc33f84d583b96aa70d4f4ef62e40d9813e5
scope.18.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI293bmVkTGFuZEluQm9hcmRPcmRlcigxKToxNzQ
scope.18.kind=method
scope.18.startLine=174
scope.18.endLine=176
scope.18.semanticHash=ff053001f78ad4941b56eff8f82f0aebeb50aee2c89d74509e5443d6c82df11d
scope.19.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI3Jlc29sdmUoMSk6MzU
scope.19.kind=method
scope.19.startLine=35
scope.19.endLine=56
scope.19.semanticHash=397e1720b530c06fa814ac633065099cd6ba3c017fb6c1e66eabc7535997c050
scope.20.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI3NldHRsZSg1KTo2Nw
scope.20.kind=method
scope.20.startLine=67
scope.20.endLine=77
scope.20.semanticHash=64d1222cd8f17129d206adb00c7ac9b545553aa1233259d3638fbf6aa2dc657d
scope.21.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlLkF1Y3Rpb25SZXN1bHQjY3RvcigyKToxNDc
scope.21.kind=method
scope.21.startLine=1
scope.21.endLine=177
scope.21.semanticHash=b104c1bdd04ee852b58bed879a14574897f8a25f196d6f3ba74a2edc06621674
*/
