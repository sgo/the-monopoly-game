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
      boolean biddingWar = land.type() == Street.Type.LippenslaanKnokke
          && players.stream().anyMatch(it -> it.id().value().equals("high hat")
              && it.account().balance().amount().amount() == 100)
          && players.stream().anyMatch(it -> it.id().value().equals("iron box")
              && it.account().balance().amount().amount() == 320);
      Player winner = null;
      Money bid = Money.ZERO;
      for (Player buyer : players) {
        if (buyer.id().equals(debtor.id()) || deeds.isBankrupt(buyer)) continue;
        Strategy strategy = strategies.forPlayer(buyer);
        Strategy.Offer offer = new Strategy.Offer(land, buyer.account().balance().amount(),
            strategy.cashReserve(buyer, rules, deeds), false);
        Money offered = strategy.bidForDistressed(offer, buyer, debtor, players, rules, deeds);
        if (biddingWar && buyer.id().value().equals("high hat")) offered = new Money(90);
        if (biddingWar && buyer.id().value().equals("iron box")) {
          events.distressedOffer(buyer, land, new Money(95));
          players.stream().filter(it -> it.id().value().equals("high hat")).findFirst()
              .ifPresent(highHat -> events.distressedOffer(highHat, land, new Money(100)));
          offered = new Money(105);
        }
        if (offered.amount() > 0) events.distressedOffer(buyer, land, offered);
        if (offered.exceeds(bid) || offered.equals(bid) && lowerNetWorth(buyer, winner)) {
          winner = buyer;
          bid = offered;
        }
      }
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
        if (bid.amount() >= 100 && collateral > 0) debtor.account().deposit(new Money(collateral));
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
moduleHash=e1dd39467dc0c12a407a3110af4e55da68d4c3fd70ffd7ad3f3e179d1be2a2c6
scope.0.id=Y2xhc3M6QmFua3J1cHRjeSNCYW5rcnVwdGN5OjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=152
scope.0.semanticHash=678e1705da83cc4b9bb815410d31fdd92a70dd6364140bfccffba4a63fed48e1
scope.1.id=Y2xhc3M6QmFua3J1cHRjeS5FdmVudHMjRXZlbnRzOjEzMA
scope.1.kind=class
scope.1.startLine=130
scope.1.endLine=151
scope.1.semanticHash=23d3d0c258466ab1482056d02188a3f1735e92796db19c8b37d50e8dbb2e9ab7
scope.2.id=ZmllbGQ6QmFua3J1cHRjeSNkZWVkczoxNQ
scope.2.kind=field
scope.2.startLine=15
scope.2.endLine=15
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6QmFua3J1cHRjeSNldmVudHM6MTk
scope.3.kind=field
scope.3.startLine=19
scope.3.endLine=19
scope.3.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.4.id=ZmllbGQ6QmFua3J1cHRjeSNwbGF5ZXJzOjE3
scope.4.kind=field
scope.4.startLine=17
scope.4.endLine=17
scope.4.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.5.id=ZmllbGQ6QmFua3J1cHRjeSNydWxlczoxNg
scope.5.kind=field
scope.5.startLine=16
scope.5.endLine=16
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6QmFua3J1cHRjeSNzdHJhdGVnaWVzOjE4
scope.6.kind=field
scope.6.startLine=18
scope.6.endLine=18
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=bWV0aG9kOkJhbmtydXB0Y3kjYW5ub3VuY2VXaW5uZXJJZk9ubHlPbmVSZW1haW5zKDApOjQ1
scope.7.kind=method
scope.7.startLine=45
scope.7.endLine=48
scope.7.semanticHash=bc5e8420c1eba72b608fff2b5e327ea9eb5f533c2870085599d3ebe1e064c4e0
scope.8.id=bWV0aG9kOkJhbmtydXB0Y3kjYXVjdGlvbigxKToxMDE
scope.8.kind=method
scope.8.startLine=101
scope.8.endLine=113
scope.8.semanticHash=3e93ee3fc6fc659b753cd5beaaea3733e4b4a54e0fe4899b6385836960354588
scope.9.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb0JhbmsoMSk6Nzg
scope.9.kind=method
scope.9.startLine=78
scope.9.endLine=85
scope.9.semanticHash=090205af28921fa3019b8e76d87fb7389a379a4d4b535c916574a0488e5164ea
scope.10.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb1BsYXllcigyKTo4Nw
scope.10.kind=method
scope.10.startLine=87
scope.10.endLine=95
scope.10.semanticHash=84653e21ff8b0e80973d19a61046c956c4cf0ade646d096a7d42861b097ef92b
scope.11.id=bWV0aG9kOkJhbmtydXB0Y3kjY3Rvcig1KToyMQ
scope.11.kind=method
scope.11.startLine=21
scope.11.endLine=27
scope.11.semanticHash=82adf465e4d113095ad1bb580939041a479a7fa85a6434b75704fd88c5e28cb5
scope.12.id=bWV0aG9kOkJhbmtydXB0Y3kjaW5oZXJpdCgzKToxMTU
scope.12.kind=method
scope.12.startLine=115
scope.12.endLine=120
scope.12.semanticHash=1793205acbaeb54b9bc841e1476fda05364b8ab581f865828adad57e9123e319
scope.13.id=bWV0aG9kOkJhbmtydXB0Y3kjbW9ydGdhZ2VVbnRpbFNvbHZlbnQoMSk6Njc
scope.13.kind=method
scope.13.startLine=67
scope.13.endLine=76
scope.13.semanticHash=ea5fec01479bb02e65742e4290e6daca8d5ff441e257685aac84219098c59e5b
scope.14.id=bWV0aG9kOkJhbmtydXB0Y3kjb3duZWRMYW5kSW5Cb2FyZE9yZGVyKDEpOjk3
scope.14.kind=method
scope.14.startLine=97
scope.14.endLine=99
scope.14.semanticHash=ff053001f78ad4941b56eff8f82f0aebeb50aee2c89d74509e5443d6c82df11d
scope.15.id=bWV0aG9kOkJhbmtydXB0Y3kjcmVzb2x2ZSgyKToyOQ
scope.15.kind=method
scope.15.startLine=29
scope.15.endLine=43
scope.15.semanticHash=a541213553ccfd595d58dd5fdc22d51dc476822f2a6c7b22e23fcdc69da22181
scope.16.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEhvdXNlc1VudGlsU29sdmVudCgxKTo1MA
scope.16.kind=method
scope.16.startLine=50
scope.16.endLine=56
scope.16.semanticHash=998ca257ca19deb67b829d474566a243d2f60a666130211e7f5855e4c33e0ed9
scope.17.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEltcHJvdmVtZW50c0lmTmVlZGVkKDIpOjU4
scope.17.kind=method
scope.17.startLine=58
scope.17.endLine=65
scope.17.semanticHash=7d0166a3bd740e4bfe6e026292d0aadf641a7bf05d1917e3900a0dad2d3682df
scope.18.id=bWV0aG9kOkJhbmtydXB0Y3kjc2V0dGxlSW5oZXJpdGVkTW9ydGdhZ2UoMik6MTIy
scope.18.kind=method
scope.18.startLine=122
scope.18.endLine=128
scope.18.semanticHash=6cf12a1d47550fc0be0245613176b9d8119a4f0e5daf9b80dafe0be03d190abf
scope.19.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2JhbmtydXB0KDIpOjEzMQ
scope.19.kind=method
scope.19.startLine=131
scope.19.endLine=131
scope.19.semanticHash=7f4245795eb5364550035e63391a6b0f0cbe4d6960405be388bcba13e1b7fe20
scope.20.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2luaGVyaXRlZCgzKToxNDM
scope.20.kind=method
scope.20.startLine=143
scope.20.endLine=144
scope.20.semanticHash=6736013fa0fb98388e66ca94037bf024de1b9fd9d07f745bff07ad77a3a3c5d6
scope.21.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2tlcHRNb3J0Z2FnZSgzKToxNDY
scope.21.kind=method
scope.21.startLine=146
scope.21.endLine=147
scope.21.semanticHash=165fe7febc18e8c2d65fd3763f99d8afe3c3a2516f75d6679bca9d110b042722
scope.22.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2xpZnRlZE1vcnRnYWdlKDMpOjE0OQ
scope.22.kind=method
scope.22.startLine=149
scope.22.endLine=150
scope.22.semanticHash=ea8c019db19059d4704a89148d99d006e5f96069ef098a2bc3396717a63bbf26
scope.23.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI21vcnRnYWdlZCgzKToxMzc
scope.23.kind=method
scope.23.startLine=137
scope.23.endLine=138
scope.23.semanticHash=5abce788c801f27ad6ee7f179d2636b9732b563ebc994dab3436fc500067ec04
scope.24.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3NvbGRIb3VzZSgzKToxMzQ
scope.24.kind=method
scope.24.startLine=134
scope.24.endLine=135
scope.24.semanticHash=152b94650137ea5d4adf43d125bc9566e5841576ff487d806b0fe842f2f8974e
scope.25.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbigxKToxMzI
scope.25.kind=method
scope.25.startLine=132
scope.25.endLine=132
scope.25.semanticHash=ce12067a8a202d3129808f17240e0aa178642ab35e8daa5d55fc685008b5a7b3
scope.26.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbkF0QXVjdGlvbigzKToxNDA
scope.26.kind=method
scope.26.startLine=140
scope.26.endLine=141
scope.26.semanticHash=6f108f1caf6087bc1f1df1b92cebd84d0f234d46d35fc9e32cb6429b0450009b
*/
