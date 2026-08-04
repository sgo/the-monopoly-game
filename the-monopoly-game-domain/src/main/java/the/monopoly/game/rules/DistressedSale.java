package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
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
      int minimumBid = minimumBid(debtor, land, candidates, shortfall);
      AuctionResult result = auction(land, debtor, minimumBid);
      Player winner = result.winner();
      Money bid = result.bid();
      boolean completesBuyersGroup = winner != null && deeds.completesColourGroup(rules, land, winner);
      if (shouldDeferToHouseSale(debtor, winner, bid, completesBuyersGroup)) {
        deferredToHouseSales.add(type);
        continue;
      }
      if (winner == null || bid.amount() <= 0 || !coversDebtWithOtherLand(debtor, land, bid, candidates)) continue;
      settle(debtor, land, winner, bid);
    }
    if (debtor.account().balance().amount().amount() < 0)
      mortgageRemainingCandidates(debtor, candidates, deferredToHouseSales);
    return debtor.account().balance().amount().amount() >= 0;
  }

  private List<Street.Type> candidates(Player debtor) {
    return Liquidation.order(deeds, rules, strategies, debtor).stream()
        .filter(type -> !(rules.create(type) instanceof ColourStreet street)
            || rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
            .filter(it -> it.colourGroup() == street.colourGroup())
            .noneMatch(it -> deeds.housesBuiltOn(it) > 0 || deeds.hasHotelOn(it)))
        .toList();
  }

  private void settle(Player debtor, Ownable land, Player winner, Money bid) {
    deeds.transfer(land, debtor, winner, bid);
    events.distressedSaleWon(winner, land, bid);
  }

  private void mortgageRemainingCandidates(Player debtor, List<Street.Type> candidates,
                                           List<Street.Type> deferredToHouseSales) {
    for (Street.Type type : candidates) {
      if (debtor.account().balance().amount().amount() >= 0) break;
      if (deferredToHouseSales.contains(type)) continue;
      Ownable land = (Ownable) rules.create(type);
      if (deeds.ownerOf(type).filter(debtor.id()::equals).isEmpty()) continue;
      if (!deeds.isMortgaged(land)) {
        Money value = deeds.mortgage(land, debtor);
        events.mortgaged(debtor, land, value);
      }
    }
  }

  private AuctionResult auction(Ownable land, Player debtor, int minimumBid) {
    List<Player> bidders = players.stream()
        .filter(player -> !player.id().equals(debtor.id()) && !deeds.isBankrupt(player))
        .filter(player -> maximumBid(player, debtor, land).amount() >= minimumBid)
        .toList();
    if (bidders.isEmpty()) return new AuctionResult(null, Money.ZERO);

    List<Money> maximums = bidders.stream().map(player -> maximumBid(player, debtor, land)).toList();
    if (bidders.size() == 1) {
      Player bidder = bidders.getFirst();
      Money bid = new Money(minimumBid);
      events.distressedOffer(bidder, land, bid);
      return new AuctionResult(bidder, bid);
    }

    Player winner = null;
    Money bid = new Money(minimumBid);
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

  private int minimumBid(Player debtor, Ownable land, List<Street.Type> candidates, int shortfall) {
    int otherCollateral = candidates.stream().filter(type -> type != land.type())
        .map(type -> ((Ownable) rules.create(type)).landMortgageValue().amount())
        .reduce(0, Integer::sum);
    return Math.max(land.landMortgageValue().amount(), shortfall - otherCollateral);
  }

  private boolean shouldDeferToHouseSale(Player debtor, Player winner, Money bid, boolean completesBuyersGroup) {
    return winner != null && bid.amount() > 0 && hasSellableHouse(debtor) && completesBuyersGroup;
  }

  private boolean coversDebtWithOtherLand(Player debtor, Ownable sold, Money bid, List<Street.Type> candidates) {
    int remaining = -debtor.account().balance().amount().amount() - bid.amount();
    if (remaining <= 0) return true;
    return candidates.stream().filter(type -> type != sold.type())
        .map(type -> ((Ownable) rules.create(type)).landMortgageValue().amount())
        .reduce(0, Integer::sum) >= remaining;
  }
}

/* mutate4java-manifest
version=1
moduleHash=1479ec4a28aba0eb468d36464996423d80185e926ad75880bc31dc7399e82759
scope.0.id=Y2xhc3M6RGlzdHJlc3NlZFNhbGUjRGlzdHJlc3NlZFNhbGU6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=174
scope.0.semanticHash=9efbc63d031e50d9bc31ee64fe683fdbaf3792f4995ecf309f267820b2d7a785
scope.1.id=Y2xhc3M6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCNBdWN0aW9uUmVzdWx0OjE0OQ
scope.1.kind=class
scope.1.startLine=149
scope.1.endLine=150
scope.1.semanticHash=8590ad3cbffb412b3753ab527e0817a71cb31af6f0ddf68bb3bc418c036e8d93
scope.2.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjZGVlZHM6MTk
scope.2.kind=field
scope.2.startLine=19
scope.2.endLine=19
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjZXZlbnRzOjIz
scope.3.kind=field
scope.3.startLine=23
scope.3.endLine=23
scope.3.semanticHash=31704b1c93eccf66e39e47003160fa006a02958fc3175424afec3d019d6066b2
scope.4.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjcGxheWVyczoyMQ
scope.4.kind=field
scope.4.startLine=21
scope.4.endLine=21
scope.4.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.5.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjcnVsZXM6MjA
scope.5.kind=field
scope.5.startLine=20
scope.5.endLine=20
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjc3RyYXRlZ2llczoyMg
scope.6.kind=field
scope.6.startLine=22
scope.6.endLine=22
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCNiaWQ6MTQ5
scope.7.kind=field
scope.7.startLine=149
scope.7.endLine=149
scope.7.semanticHash=2854c5e6b6eba94fbe4bea31c1266c5a4af90b52d71ad357c0aa67886328b236
scope.8.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCN3aW5uZXI6MTQ5
scope.8.kind=field
scope.8.startLine=149
scope.8.endLine=149
scope.8.semanticHash=d76827961eca0eb4c351ae8de7a2ab43cef35e44ddbeacc98fe0869ab9a2a5cf
scope.9.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2F1Y3Rpb24oMik6OTQ
scope.9.kind=method
scope.9.startLine=94
scope.9.endLine=133
scope.9.semanticHash=ae994a38e716bde79caf02035c51711eae61fe62bc1b8d7e74c83f214b9352c2
scope.10.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2JlbG93TW9ydGdhZ2VGbG9vcig0KToxNTk
scope.10.kind=method
scope.10.startLine=159
scope.10.endLine=161
scope.10.semanticHash=8a34a3c0373058b09a350357775b36739a96e845d140c7ee78ad55d03dd9759e
scope.11.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2NhbmRpZGF0ZXMoMSk6NTk
scope.11.kind=method
scope.11.startLine=59
scope.11.endLine=66
scope.11.semanticHash=bc45803cdf8d75c4a09158a18fea8d6c4ff6326aa8b867c8a7ec5d9af418eff5
scope.12.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2Nhbm5vdFJhaXNlKDMpOjE0Mg
scope.12.kind=method
scope.12.startLine=142
scope.12.endLine=147
scope.12.semanticHash=ece2b89b7b0d0ae8545a8a096bf9c971d3863e4f3236ef04a80e43c513ecb7ea
scope.13.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2NvdmVyc0RlYnRXaXRoT3RoZXJMYW5kKDMpOjE2Nw
scope.13.kind=method
scope.13.startLine=167
scope.13.endLine=173
scope.13.semanticHash=2b88e73123633bef6c589dafb7d4b7ecb6897f6b24c9914e2ee0ccef510e9093
scope.14.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2N0b3IoNSk6MjU
scope.14.kind=method
scope.14.startLine=25
scope.14.endLine=32
scope.14.semanticHash=ac1c79770034f678d5d7e279402fd2783d18ea99e1193e558b40626c7a49fe79
scope.15.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2hhc1NlbGxhYmxlSG91c2UoMSk6MTUy
scope.15.kind=method
scope.15.startLine=152
scope.15.endLine=156
scope.15.semanticHash=9de3d17c4bb4b0a7a53f5aa15557aeb376b770e0602203a569b83a226ad7f9bc
scope.16.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI21heGltdW1CaWQoMyk6MTM1
scope.16.kind=method
scope.16.startLine=135
scope.16.endLine=140
scope.16.semanticHash=7cb9d5f1ae801bb3fcb13bb1eee3087f9fdb7e26bdb3dae0488181b9239f3358
scope.17.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI21vcnRnYWdlUmVtYWluaW5nQ2FuZGlkYXRlcygzKTo4MA
scope.17.kind=method
scope.17.startLine=80
scope.17.endLine=92
scope.17.semanticHash=cc495fa0d8d9cb35a8921a44327da4d46170d3c7fcd3837d83f16f9a40924553
scope.18.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI3Jlc29sdmUoMSk6MzQ
scope.18.kind=method
scope.18.startLine=34
scope.18.endLine=57
scope.18.semanticHash=0e952ced03ae61c13572c50a5ef873da77ae4e447b661fcf02f1770ac771ebd7
scope.19.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI3NldHRsZSg1KTo2OA
scope.19.kind=method
scope.19.startLine=68
scope.19.endLine=78
scope.19.semanticHash=b3167d5041208e838b96cb58f0c38974bfc8c942a5f41e766cddd93fa9a6628c
scope.20.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI3Nob3VsZERlZmVyVG9Ib3VzZVNhbGUoNCk6MTYz
scope.20.kind=method
scope.20.startLine=163
scope.20.endLine=165
scope.20.semanticHash=739ec4215d2cd39c5f8a4a7e897bac5df93d01962a3acb6a98547f23992a948b
scope.21.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlLkF1Y3Rpb25SZXN1bHQjY3RvcigyKToxNDk
scope.21.kind=method
scope.21.startLine=1
scope.21.endLine=174
scope.21.semanticHash=b3d41ac47774c47ec82782cc43adc37644a8af4733ff8804b4ca8e821163c2d8
*/
