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
      attemptSale(debtor, type, candidates, deferredToHouseSales);
    }
    if (debtor.account().balance().amount().amount() < 0)
      mortgageRemainingCandidates(debtor, candidates, deferredToHouseSales);
    return debtor.account().balance().amount().amount() >= 0;
  }

  private void attemptSale(Player debtor, Street.Type type, List<Street.Type> candidates,
                           List<Street.Type> deferredToHouseSales) {
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
      return;
    }
    if (winner == null || bid.amount() <= 0 || !coversDebtWithOtherLand(debtor, land, bid, candidates)) return;
    settle(debtor, land, winner, bid);
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
    if (bidders.isEmpty()) {
      events.distressedSaleNoBidder(debtor, land);
      return new AuctionResult(null, Money.ZERO);
    }

    List<Money> maximums = bidders.stream().map(player -> maximumBid(player, debtor, land)).toList();
    if (bidders.size() == 1) {
      Player bidder = bidders.getFirst();
      Money bid = new Money(minimumBid);
      events.distressedOffer(bidder, land, bid);
      return new AuctionResult(bidder, bid);
    }

    return ascend(bidders, maximums, land, minimumBid);
  }

  private AuctionResult ascend(List<Player> bidders, List<Money> maximums, Ownable land, int minimumBid) {
    Player winner = null;
    Money bid = new Money(minimumBid);
    boolean firstOffer = true;
    Round round;
    do {
      round = bidRound(bidders, maximums, land, winner, bid, firstOffer);
      winner = round.winner();
      bid = round.bid();
      firstOffer = round.firstOffer();
    } while (round.raised() && !round.settled());
    return new AuctionResult(winner, bid);
  }

  private Round bidRound(List<Player> bidders, List<Money> maximums, Ownable land, Player winner, Money bid,
                         boolean firstOffer) {
    boolean raised = false;
    boolean settled = false;
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
      if (Auction.cannotRaise(maximums, index, bid)) {
        settled = true;
        break;
      }
    }
    return new Round(winner, bid, firstOffer, raised, settled);
  }

  private record Round(Player winner, Money bid, boolean firstOffer, boolean raised, boolean settled) {
  }

  private Money maximumBid(Player bidder, Player debtor, Ownable land) {
    Strategy strategy = strategies.forPlayer(bidder);
    Strategy.Offer offer = new Strategy.Offer(land, bidder.account().balance().amount(),
        strategy.cashReserve(bidder, rules, deeds), false);
    return strategy.bidForDistressed(offer, bidder, debtor, players, rules, deeds);
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
moduleHash=5c0be3bddc44d7739c8454bfcdba7ccac71f8b43bcc55920efa48ca01b86036e
scope.0.id=Y2xhc3M6RGlzdHJlc3NlZFNhbGUjRGlzdHJlc3NlZFNhbGU6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=186
scope.0.semanticHash=6f337f029e1e671361bb52e561ff2ddcbc9b9e226ea6e4dca01f950de88bb9e2
scope.1.id=Y2xhc3M6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCNBdWN0aW9uUmVzdWx0OjE1OQ
scope.1.kind=class
scope.1.startLine=159
scope.1.endLine=160
scope.1.semanticHash=8590ad3cbffb412b3753ab527e0817a71cb31af6f0ddf68bb3bc418c036e8d93
scope.2.id=Y2xhc3M6RGlzdHJlc3NlZFNhbGUuUm91bmQjUm91bmQ6MTQ5
scope.2.kind=class
scope.2.startLine=149
scope.2.endLine=150
scope.2.semanticHash=d61dfc5f4eb9004418f8d537e43413e06f29aece3c455ab190b96bbf615461bf
scope.3.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjZGVlZHM6MTk
scope.3.kind=field
scope.3.startLine=19
scope.3.endLine=19
scope.3.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.4.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjZXZlbnRzOjIz
scope.4.kind=field
scope.4.startLine=23
scope.4.endLine=23
scope.4.semanticHash=31704b1c93eccf66e39e47003160fa006a02958fc3175424afec3d019d6066b2
scope.5.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjcGxheWVyczoyMQ
scope.5.kind=field
scope.5.startLine=21
scope.5.endLine=21
scope.5.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.6.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjcnVsZXM6MjA
scope.6.kind=field
scope.6.startLine=20
scope.6.endLine=20
scope.6.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.7.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUjc3RyYXRlZ2llczoyMg
scope.7.kind=field
scope.7.startLine=22
scope.7.endLine=22
scope.7.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.8.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCNiaWQ6MTU5
scope.8.kind=field
scope.8.startLine=159
scope.8.endLine=159
scope.8.semanticHash=2854c5e6b6eba94fbe4bea31c1266c5a4af90b52d71ad357c0aa67886328b236
scope.9.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuQXVjdGlvblJlc3VsdCN3aW5uZXI6MTU5
scope.9.kind=field
scope.9.startLine=159
scope.9.endLine=159
scope.9.semanticHash=d76827961eca0eb4c351ae8de7a2ab43cef35e44ddbeacc98fe0869ab9a2a5cf
scope.10.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuUm91bmQjYmlkOjE0OQ
scope.10.kind=field
scope.10.startLine=149
scope.10.endLine=149
scope.10.semanticHash=2854c5e6b6eba94fbe4bea31c1266c5a4af90b52d71ad357c0aa67886328b236
scope.11.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuUm91bmQjZmlyc3RPZmZlcjoxNDk
scope.11.kind=field
scope.11.startLine=149
scope.11.endLine=149
scope.11.semanticHash=23d37a2927c843a6b0e4a8db4646ec38687fd0fac96fcd140692494476257ae9
scope.12.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuUm91bmQjcmFpc2VkOjE0OQ
scope.12.kind=field
scope.12.startLine=149
scope.12.endLine=149
scope.12.semanticHash=675c17924dd694661b4cb62b8a2f61825211b77b491034218f20f51ab8427103
scope.13.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuUm91bmQjc2V0dGxlZDoxNDk
scope.13.kind=field
scope.13.startLine=149
scope.13.endLine=149
scope.13.semanticHash=871f5b3f6bb5a5f3b1063f2a6a8977198bbad44d7deddb94c75e8ac9166c7934
scope.14.id=ZmllbGQ6RGlzdHJlc3NlZFNhbGUuUm91bmQjd2lubmVyOjE0OQ
scope.14.kind=field
scope.14.startLine=149
scope.14.endLine=149
scope.14.semanticHash=d76827961eca0eb4c351ae8de7a2ab43cef35e44ddbeacc98fe0869ab9a2a5cf
scope.15.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2FzY2VuZCg0KToxMTM
scope.15.kind=method
scope.15.startLine=113
scope.15.endLine=125
scope.15.semanticHash=c5f8c637eb821f79de79e03ed5ed95ac034bfe41cb680b6701e0f27432a4baa4
scope.16.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2F0dGVtcHRTYWxlKDQpOjQ2
scope.16.kind=method
scope.16.startLine=46
scope.16.endLine=62
scope.16.semanticHash=8961a9ccb4502aee09778f942cdbdda033d24c0dfd8b9f0a899f261c19805756
scope.17.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2F1Y3Rpb24oMyk6OTI
scope.17.kind=method
scope.17.startLine=92
scope.17.endLine=111
scope.17.semanticHash=d7d97a5d871576bca4a873b04fac25e1375b4515a84d96ec9f6441b99b2c8fae
scope.18.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2JpZFJvdW5kKDYpOjEyNw
scope.18.kind=method
scope.18.startLine=127
scope.18.endLine=147
scope.18.semanticHash=e48748521aa21fc8e874c3b49012b21388f9237b492379493463b091e46c78e5
scope.19.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2NhbmRpZGF0ZXMoMSk6NjQ
scope.19.kind=method
scope.19.startLine=64
scope.19.endLine=71
scope.19.semanticHash=bc45803cdf8d75c4a09158a18fea8d6c4ff6326aa8b867c8a7ec5d9af418eff5
scope.20.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2NvdmVyc0RlYnRXaXRoT3RoZXJMYW5kKDQpOjE3OQ
scope.20.kind=method
scope.20.startLine=179
scope.20.endLine=185
scope.20.semanticHash=9858483d8aa354c1928988017fab82b8027c36eaa130c899ae49cca51adf43dc
scope.21.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2N0b3IoNSk6MjU
scope.21.kind=method
scope.21.startLine=25
scope.21.endLine=32
scope.21.semanticHash=ac1c79770034f678d5d7e279402fd2783d18ea99e1193e558b40626c7a49fe79
scope.22.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI2hhc1NlbGxhYmxlSG91c2UoMSk6MTYy
scope.22.kind=method
scope.22.startLine=162
scope.22.endLine=166
scope.22.semanticHash=9de3d17c4bb4b0a7a53f5aa15557aeb376b770e0602203a569b83a226ad7f9bc
scope.23.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI21heGltdW1CaWQoMyk6MTUy
scope.23.kind=method
scope.23.startLine=152
scope.23.endLine=157
scope.23.semanticHash=7cb9d5f1ae801bb3fcb13bb1eee3087f9fdb7e26bdb3dae0488181b9239f3358
scope.24.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI21pbmltdW1CaWQoNCk6MTY4
scope.24.kind=method
scope.24.startLine=168
scope.24.endLine=173
scope.24.semanticHash=7b4287e0806fd8b292817e584d9a3a0565d43fc0661261ebf75062bdb13463c9
scope.25.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI21vcnRnYWdlUmVtYWluaW5nQ2FuZGlkYXRlcygzKTo3OA
scope.25.kind=method
scope.25.startLine=78
scope.25.endLine=90
scope.25.semanticHash=cc495fa0d8d9cb35a8921a44327da4d46170d3c7fcd3837d83f16f9a40924553
scope.26.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI3Jlc29sdmUoMSk6MzQ
scope.26.kind=method
scope.26.startLine=34
scope.26.endLine=44
scope.26.semanticHash=2f2a19959c5e011064a8b6e899af998e2cd2058ac93479e69377fac4756df803
scope.27.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI3NldHRsZSg0KTo3Mw
scope.27.kind=method
scope.27.startLine=73
scope.27.endLine=76
scope.27.semanticHash=a207d447aafe1a5c4d2c2e3810e2e0dbec2c5fc2301d6aa718384b80787aea43
scope.28.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlI3Nob3VsZERlZmVyVG9Ib3VzZVNhbGUoNCk6MTc1
scope.28.kind=method
scope.28.startLine=175
scope.28.endLine=177
scope.28.semanticHash=739ec4215d2cd39c5f8a4a7e897bac5df93d01962a3acb6a98547f23992a948b
scope.29.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlLkF1Y3Rpb25SZXN1bHQjY3RvcigyKToxNTk
scope.29.kind=method
scope.29.startLine=1
scope.29.endLine=186
scope.29.semanticHash=7059d950e3ca05a370b2300e6ec53d8e97041d20b51d352c4771a1bc53c86486
scope.30.id=bWV0aG9kOkRpc3RyZXNzZWRTYWxlLlJvdW5kI2N0b3IoNSk6MTQ5
scope.30.kind=method
scope.30.startLine=1
scope.30.endLine=186
scope.30.semanticHash=7059d950e3ca05a370b2300e6ec53d8e97041d20b51d352c4771a1bc53c86486
*/
