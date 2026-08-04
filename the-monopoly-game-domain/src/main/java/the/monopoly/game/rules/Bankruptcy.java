package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Comparator;

/** Resolves an unpaid debt after the ordinary landing rule has charged it. */
public final class Bankruptcy {
  private final Deeds deeds;
  private final Rule.Set rules;
  private final List<Player> players;
  private final Strategy.OfPlayers strategies;
  private final Events events;

  public Bankruptcy(Deeds deeds, Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies, Events events) {
    this.deeds = deeds;
    this.rules = rules;
    this.players = players;
    this.strategies = strategies;
    this.events = events;
  }

  public void resolve(Player debtor, Player creditor) {
    if (!Money.ZERO.exceeds(debtor.account().balance().amount()) || deeds.isBankrupt(debtor)) return;
    if (creditor != null) creditor.account().withdraw(new Money(-debtor.account().balance().amount().amount()));
    if (resolveDistressedSales(debtor)) return;
    sellHousesUntilSolvent(debtor);
    mortgageUntilSolvent(debtor);
    if (debtor.account().balance().amount().amount() >= 0) return;

    Money remaining = debtor.account().balance().amount();
    debtor.account().deposit(new Money(-remaining.amount()));
    deeds.bankrupt(debtor);
    if (creditor == null) bankruptToBank(debtor);
    else bankruptToPlayer(debtor, creditor);
    events.bankrupt(debtor, creditor);
    announceWinnerIfOnlyOneRemains();
  }

  private void announceWinnerIfOnlyOneRemains() {
    List<Player> playersStillInGame = players.stream().filter(it -> !deeds.isBankrupt(it)).toList();
    if (playersStillInGame.size() == 1) events.won(playersStillInGame.getFirst());
  }

  private void sellHousesUntilSolvent(Player debtor) {
    for (Street.Type type : ownedLandInBoardOrder(debtor)) {
      if (!Money.ZERO.exceeds(debtor.account().balance().amount())) return;
      Street street = rules.create(type);
      sellImprovementsIfNeeded(street, debtor);
    }
  }

  private void sellImprovementsIfNeeded(Street street, Player debtor) {
    if (!(street instanceof ColourStreet colour)) return;
    if (deeds.hasHotelOn(colour)) deeds.exchangeHotelForHouses(colour, debtor);
    while (debtor.account().balance().amount().amount() < 0 && deeds.housesBuiltOn(colour) > 0) {
      Money price = deeds.sellHouse(colour, debtor);
      events.soldHouse(debtor, colour, price);
    }
  }

  private void mortgageUntilSolvent(Player debtor) {
    for (Street.Type type : liquidationOrder(debtor)) {
      if (!Money.ZERO.exceeds(debtor.account().balance().amount())) return;
      Ownable land = (Ownable) rules.create(type);
      if (!deeds.isMortgaged(land)) {
        Money value = deeds.mortgage(land, debtor);
        events.mortgaged(debtor, land, value);
      }
    }
  }

  private boolean resolveDistressedSales(Player debtor) {
    List<Street.Type> candidates = liquidationOrder(debtor).stream()
        .filter(type -> !(rules.create(type) instanceof ColourStreet street)
            || rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
            .filter(it -> it.colourGroup() == street.colourGroup())
            .noneMatch(it -> deeds.housesBuiltOn(it) > 0 || deeds.hasHotelOn(it)))
        .toList();
    List<Street.Type> deferredToHouseSales = new java.util.ArrayList<>();
    for (Street.Type type : candidates) {
      if (debtor.account().balance().amount().amount() >= 0) return true;
      Ownable land = (Ownable) rules.create(type);
      events.distressedSaleStarted(debtor, land);
      int shortfall = -debtor.account().balance().amount().amount();
      AuctionResult result = auctionDistressed(land, debtor);
      Player winner = result.winner();
      Money bid = result.bid();
      if (winner != null && bid.amount() > 0 && hasSellableHouse(debtor)) {
        deferredToHouseSales.add(type);
        continue;
      }
      if (winner == null || bid.amount() <= 0 || !coversDebtWithOtherLand(debtor, land, bid)) continue;
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
    if (debtor.account().balance().amount().amount() < 0) {
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
    return debtor.account().balance().amount().amount() >= 0;
  }

  private AuctionResult auctionDistressed(Ownable land, Player debtor) {
    List<Player> bidders = players.stream()
        .filter(player -> !player.id().equals(debtor.id()) && !deeds.isBankrupt(player))
        .filter(player -> maximumDistressedBid(player, debtor, land).amount() > 0)
        .toList();
    if (bidders.isEmpty()) return new AuctionResult(null, Money.ZERO);

    List<Money> maximums = bidders.stream().map(player -> maximumDistressedBid(player, debtor, land)).toList();
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

  private Money maximumDistressedBid(Player bidder, Player debtor, Ownable land) {
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

  private boolean lowerNetWorth(Player candidate, Player incumbent) {
    if (incumbent == null) return true;
    int candidateWorth = candidate.account().balance().amount().amount()
        + deeds.landOwnedBy(candidate).stream().map(type -> ((Ownable) rules.create(type)).price().amount()).reduce(0, Integer::sum);
    int incumbentWorth = incumbent.account().balance().amount().amount()
        + deeds.landOwnedBy(incumbent).stream().map(type -> ((Ownable) rules.create(type)).price().amount()).reduce(0, Integer::sum);
    return candidateWorth < incumbentWorth;
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

  private void bankruptToBank(Player debtor) {
    deeds.returnRetainedCardsToDeck(debtor);
    for (Street.Type type : ownedLandInBoardOrder(debtor)) {
      Ownable land = (Ownable) rules.create(type);
      deeds.returnToBank(land, debtor);
      auction(land);
    }
  }

  private void bankruptToPlayer(Player debtor, Player creditor) {
    Money cash = debtor.account().balance().amount();
    if (cash.amount() > 0) {
      debtor.account().withdraw(cash);
      creditor.account().deposit(cash);
    }
    for (Street.Type type : ownedLandInBoardOrder(debtor)) inherit((Ownable) rules.create(type), debtor, creditor);
    deeds.transferRetainedCards(debtor, creditor);
  }

  private List<Street.Type> ownedLandInBoardOrder(Player owner) {
    return rules.gameboard().layout().stream().filter(deeds.landOwnedBy(owner)::contains).toList();
  }

  private void auction(Ownable land) {
    Player winner = null;
    Money bid = Money.ZERO;
    for (Player player : players) {
      if (deeds.isBankrupt(player)) continue;
      Money offered = strategies.forPlayer(player).bidFor(new Strategy.Offer(land, player.account().balance().amount()));
      if (offered.exceeds(bid)) { winner = player; bid = offered; }
    }
    if (winner == null) return;
    deeds.sell(land, winner, bid);
    events.wonAtAuction(winner, land, bid);
    settleInheritedMortgage(land, winner);
  }

  private void inherit(Ownable land, Player debtor, Player creditor) {
    deeds.transferWithoutPayment(land, debtor, creditor);
    events.inherited(creditor, land, debtor);
    if (deeds.isMortgaged(land)) creditor.account().deposit(land.landMortgageValue());
    settleInheritedMortgage(land, creditor);
  }

  private void settleInheritedMortgage(Ownable land, Player owner) {
    if (!deeds.isMortgaged(land)) return;
    if (strategies.forPlayer(owner) instanceof Greedo
        && owner.account().balance().amount().covers(land.landMortgageValue().plus(new Money((land.landMortgageValue().amount() + 9) / 10))))
      events.liftedMortgage(owner, land, deeds.liftMortgage(land, owner));
    else events.keptMortgage(owner, land, deeds.keepMortgaged(land, owner));
  }

  public interface Events {
    void bankrupt(Player debtor, Player creditor);
    void won(Player player);

    default void soldHouse(Player player, ColourStreet street, Money price) {
    }

    default void mortgaged(Player player, Ownable land, Money value) {
    }

    default void wonAtAuction(Player winner, Ownable land, Money price) {
    }

    default void inherited(Player creditor, Ownable land, Player debtor) {
    }

    default void keptMortgage(Player player, Ownable land, Money interest) {
    }

    default void liftedMortgage(Player player, Ownable land, Deeds.MortgageCost cost) {
    }

    default void soldToPeer(Player seller, Ownable land, Player buyer, Money price) {
    }

    default void distressedSaleStarted(Player seller, Ownable land) {
    }

    default void distressedOffer(Player bidder, Ownable land, Money price) {
    }

    default void distressedSaleWon(Player bidder, Ownable land, Money price) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=f8f8d496487c829a087bb328d4991ab3b118a21c534481eb0a7c0270ff38300a
scope.0.id=Y2xhc3M6QmFua3J1cHRjeSNCYW5rcnVwdGN5OjE1
scope.0.kind=class
scope.0.startLine=15
scope.0.endLine=265
scope.0.semanticHash=b1aea2e15bf969b8023e8ee32377e125cde1844bf28166b0141b45acbb5b0ccc
scope.1.id=Y2xhc3M6QmFua3J1cHRjeS5FdmVudHMjRXZlbnRzOjIzMQ
scope.1.kind=class
scope.1.startLine=231
scope.1.endLine=264
scope.1.semanticHash=f731a09e7545abce5a9443f8e97d67f7d1dbb90ed5c414e085f891ad28c85de6
scope.2.id=ZmllbGQ6QmFua3J1cHRjeSNkZWVkczoxNg
scope.2.kind=field
scope.2.startLine=16
scope.2.endLine=16
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6QmFua3J1cHRjeSNldmVudHM6MjA
scope.3.kind=field
scope.3.startLine=20
scope.3.endLine=20
scope.3.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.4.id=ZmllbGQ6QmFua3J1cHRjeSNwbGF5ZXJzOjE4
scope.4.kind=field
scope.4.startLine=18
scope.4.endLine=18
scope.4.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.5.id=ZmllbGQ6QmFua3J1cHRjeSNydWxlczoxNw
scope.5.kind=field
scope.5.startLine=17
scope.5.endLine=17
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6QmFua3J1cHRjeSNzdHJhdGVnaWVzOjE5
scope.6.kind=field
scope.6.startLine=19
scope.6.endLine=19
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=bWV0aG9kOkJhbmtydXB0Y3kjYW5ub3VuY2VXaW5uZXJJZk9ubHlPbmVSZW1haW5zKDApOjQ3
scope.7.kind=method
scope.7.startLine=47
scope.7.endLine=50
scope.7.semanticHash=bc5e8420c1eba72b608fff2b5e327ea9eb5f533c2870085599d3ebe1e064c4e0
scope.8.id=bWV0aG9kOkJhbmtydXB0Y3kjYXVjdGlvbigxKToyMDI
scope.8.kind=method
scope.8.startLine=202
scope.8.endLine=214
scope.8.semanticHash=3e93ee3fc6fc659b753cd5beaaea3733e4b4a54e0fe4899b6385836960354588
scope.9.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb0JhbmsoMSk6MTc5
scope.9.kind=method
scope.9.startLine=179
scope.9.endLine=186
scope.9.semanticHash=090205af28921fa3019b8e76d87fb7389a379a4d4b535c916574a0488e5164ea
scope.10.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb1BsYXllcigyKToxODg
scope.10.kind=method
scope.10.startLine=188
scope.10.endLine=196
scope.10.semanticHash=84653e21ff8b0e80973d19a61046c956c4cf0ade646d096a7d42861b097ef92b
scope.11.id=bWV0aG9kOkJhbmtydXB0Y3kjY292ZXJzRGVidFdpdGhPdGhlckxhbmQoMyk6MTUy
scope.11.kind=method
scope.11.startLine=152
scope.11.endLine=158
scope.11.semanticHash=0de48a55441cb5c50fa61e88b74b9593c3a5bc62bf0b72967c56457fe98eaedb
scope.12.id=bWV0aG9kOkJhbmtydXB0Y3kjY3Rvcig1KToyMg
scope.12.kind=method
scope.12.startLine=22
scope.12.endLine=28
scope.12.semanticHash=82adf465e4d113095ad1bb580939041a479a7fa85a6434b75704fd88c5e28cb5
scope.13.id=bWV0aG9kOkJhbmtydXB0Y3kjaGFzU2VsbGFibGVIb3VzZSgxKToxNDY
scope.13.kind=method
scope.13.startLine=146
scope.13.endLine=150
scope.13.semanticHash=9de3d17c4bb4b0a7a53f5aa15557aeb376b770e0602203a569b83a226ad7f9bc
scope.14.id=bWV0aG9kOkJhbmtydXB0Y3kjaW5oZXJpdCgzKToyMTY
scope.14.kind=method
scope.14.startLine=216
scope.14.endLine=221
scope.14.semanticHash=1793205acbaeb54b9bc841e1476fda05364b8ab581f865828adad57e9123e319
scope.15.id=bWV0aG9kOkJhbmtydXB0Y3kjbGlxdWlkYXRpb25PcmRlcigxKToxNjk
scope.15.kind=method
scope.15.startLine=169
scope.15.endLine=177
scope.15.semanticHash=26cf8b0f5a34fb9d484f9cedfbf3c58cdf7abc183f5e2383682ec48516e4d154
scope.16.id=bWV0aG9kOkJhbmtydXB0Y3kjbG93ZXJOZXRXb3J0aCgyKToxNjA
scope.16.kind=method
scope.16.startLine=160
scope.16.endLine=167
scope.16.semanticHash=2c00c0c4cdef891fb06c6ce0bee7459d65273971c13ebae58de06bb6d5216d5d
scope.17.id=bWV0aG9kOkJhbmtydXB0Y3kjbW9ydGdhZ2VVbnRpbFNvbHZlbnQoMSk6Njk
scope.17.kind=method
scope.17.startLine=69
scope.17.endLine=78
scope.17.semanticHash=be71f97bc47507c1ee12587dbe8ca72363ce1a441e2e4dca0872700754120799
scope.18.id=bWV0aG9kOkJhbmtydXB0Y3kjb3duZWRMYW5kSW5Cb2FyZE9yZGVyKDEpOjE5OA
scope.18.kind=method
scope.18.startLine=198
scope.18.endLine=200
scope.18.semanticHash=ff053001f78ad4941b56eff8f82f0aebeb50aee2c89d74509e5443d6c82df11d
scope.19.id=bWV0aG9kOkJhbmtydXB0Y3kjcmVzb2x2ZSgyKTozMA
scope.19.kind=method
scope.19.startLine=30
scope.19.endLine=45
scope.19.semanticHash=fdcd5bf40deb97d1004d175199cfd465d80f9585b8d3769cf753edec65a28ca8
scope.20.id=bWV0aG9kOkJhbmtydXB0Y3kjcmVzb2x2ZURpc3RyZXNzZWRTYWxlcygxKTo4MA
scope.20.kind=method
scope.20.startLine=80
scope.20.endLine=144
scope.20.semanticHash=252408724cfa97bd7b307bea13731382651e5c4f621107cb8b4a891eedb07753
scope.21.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEhvdXNlc1VudGlsU29sdmVudCgxKTo1Mg
scope.21.kind=method
scope.21.startLine=52
scope.21.endLine=58
scope.21.semanticHash=998ca257ca19deb67b829d474566a243d2f60a666130211e7f5855e4c33e0ed9
scope.22.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEltcHJvdmVtZW50c0lmTmVlZGVkKDIpOjYw
scope.22.kind=method
scope.22.startLine=60
scope.22.endLine=67
scope.22.semanticHash=7d0166a3bd740e4bfe6e026292d0aadf641a7bf05d1917e3900a0dad2d3682df
scope.23.id=bWV0aG9kOkJhbmtydXB0Y3kjc2V0dGxlSW5oZXJpdGVkTW9ydGdhZ2UoMik6MjIz
scope.23.kind=method
scope.23.startLine=223
scope.23.endLine=229
scope.23.semanticHash=6cf12a1d47550fc0be0245613176b9d8119a4f0e5daf9b80dafe0be03d190abf
scope.24.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2JhbmtydXB0KDIpOjIzMg
scope.24.kind=method
scope.24.startLine=232
scope.24.endLine=232
scope.24.semanticHash=7f4245795eb5364550035e63391a6b0f0cbe4d6960405be388bcba13e1b7fe20
scope.25.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRPZmZlcigzKToyNTk
scope.25.kind=method
scope.25.startLine=259
scope.25.endLine=260
scope.25.semanticHash=5aa7159f27bd9240fbbdfb13b1427861c7064b9e7c4d9237006826493e108bac
scope.26.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRTYWxlU3RhcnRlZCgyKToyNTY
scope.26.kind=method
scope.26.startLine=256
scope.26.endLine=257
scope.26.semanticHash=e055e92e186bf4a57b7b85cbc73cdbf43ea72b9ab22bf0e76047f6b5d0aa98a2
scope.27.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRTYWxlV29uKDMpOjI2Mg
scope.27.kind=method
scope.27.startLine=262
scope.27.endLine=263
scope.27.semanticHash=e5e3462c96b4520a02805f9871ae8b8c4b81b8e0f3f59b3a9a2360aa97e67f4d
scope.28.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2luaGVyaXRlZCgzKToyNDQ
scope.28.kind=method
scope.28.startLine=244
scope.28.endLine=245
scope.28.semanticHash=6736013fa0fb98388e66ca94037bf024de1b9fd9d07f745bff07ad77a3a3c5d6
scope.29.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2tlcHRNb3J0Z2FnZSgzKToyNDc
scope.29.kind=method
scope.29.startLine=247
scope.29.endLine=248
scope.29.semanticHash=165fe7febc18e8c2d65fd3763f99d8afe3c3a2516f75d6679bca9d110b042722
scope.30.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2xpZnRlZE1vcnRnYWdlKDMpOjI1MA
scope.30.kind=method
scope.30.startLine=250
scope.30.endLine=251
scope.30.semanticHash=ea8c019db19059d4704a89148d99d006e5f96069ef098a2bc3396717a63bbf26
scope.31.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI21vcnRnYWdlZCgzKToyMzg
scope.31.kind=method
scope.31.startLine=238
scope.31.endLine=239
scope.31.semanticHash=5abce788c801f27ad6ee7f179d2636b9732b563ebc994dab3436fc500067ec04
scope.32.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3NvbGRIb3VzZSgzKToyMzU
scope.32.kind=method
scope.32.startLine=235
scope.32.endLine=236
scope.32.semanticHash=152b94650137ea5d4adf43d125bc9566e5841576ff487d806b0fe842f2f8974e
scope.33.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3NvbGRUb1BlZXIoNCk6MjUz
scope.33.kind=method
scope.33.startLine=253
scope.33.endLine=254
scope.33.semanticHash=84c012cbd6eb17175d00e593ebc380bc29b348a961821eb131e7119cf4dbe018
scope.34.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbigxKToyMzM
scope.34.kind=method
scope.34.startLine=233
scope.34.endLine=233
scope.34.semanticHash=ce12067a8a202d3129808f17240e0aa178642ab35e8daa5d55fc685008b5a7b3
scope.35.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbkF0QXVjdGlvbigzKToyNDE
scope.35.kind=method
scope.35.startLine=241
scope.35.endLine=242
scope.35.semanticHash=6f108f1caf6087bc1f1df1b92cebd84d0f234d46d35fc9e32cb6429b0450009b
*/
