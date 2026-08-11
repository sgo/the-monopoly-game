package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

/** Resolves an unpaid debt after the ordinary landing rule has charged it. */
public final class Bankruptcy {
  private final Deeds deeds;
  private final Rule.Set rules;
  private final List<Player> players;
  private final Strategy.OfPlayers strategies;
  private final Events events;
  private final DistressedSale distressedSale;

  public Bankruptcy(Deeds deeds, Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies, Events events) {
    this.deeds = deeds;
    this.rules = rules;
    this.players = players;
    this.strategies = strategies;
    this.events = events;
    this.distressedSale = new DistressedSale(deeds, rules, players, strategies, events);
  }

  public void resolve(Player debtor, Player creditor) {
    if (!Money.ZERO.exceeds(debtor.account().balance().amount()) || deeds.isBankrupt(debtor)) return;
    if (creditor != null) creditor.account().withdraw(new Money(-debtor.account().balance().amount().amount()));
    if (distressedSale.resolve(debtor)) return;
    sellHousesUntilSolvent(debtor);
    mortgageUntilSolvent(debtor);
    sellEntitySharesUntilSolvent(debtor);
    if (debtor.account().balance().amount().amount() >= 0) return;

    Money remaining = debtor.account().balance().amount();
    debtor.account().deposit(new Money(-remaining.amount()));
    deeds.legalEntities().forEach(entity -> entity.removeShares(debtor));
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
    for (Street.Type type : Liquidation.order(deeds, rules, strategies, debtor)) {
      if (!Money.ZERO.exceeds(debtor.account().balance().amount())) return;
      Ownable land = (Ownable) rules.create(type);
      if (!deeds.isMortgaged(land)) {
        Money value = deeds.mortgage(land, debtor);
        events.mortgaged(debtor, land, value);
      }
    }
  }

  private void sellEntitySharesUntilSolvent(Player debtor) {
    for (LegalEntity entity : deeds.legalEntities()) {
      if (!Money.ZERO.exceeds(debtor.account().balance().amount())) return;
      if (entity.shareOf(debtor) == 0.0) continue;
      Money value = entity.shareValue();
      int shortfall = -debtor.account().balance().amount().amount();
      int minimumBid = Math.min(value.amount(), shortfall);
      List<Player> bidders = new java.util.ArrayList<>();
      List<Money> maximums = new java.util.ArrayList<>();
      for (Player candidate : players) {
        if (candidate.id().equals(debtor.id()) || deeds.isBankrupt(candidate)
            || entity.shareOf(candidate) == 0.0
            || !(strategies.forPlayer(candidate) instanceof Greedo greedo)
            || !greedo.legalEntityTradingEnabled()) continue;
        int available = candidate.account().balance().amount().amount();
        int ceiling = Math.max(value.amount(), available * 35 / 100);
        Money offered = new Money(Math.min(available, ceiling));
        if (offered.amount() >= minimumBid) {
          bidders.add(candidate);
          maximums.add(offered);
        }
      }
      if (bidders.isEmpty()) continue;
      int winnerIndex = 0;
      for (int index = 1; index < bidders.size(); index++) {
        if (maximums.get(index).exceeds(maximums.get(winnerIndex))) winnerIndex = index;
      }
      Player buyer = bidders.get(winnerIndex);
      Money maximumBid = maximums.get(winnerIndex);
      Money price;
      if (maximumBid.amount() < value.amount()) {
        price = maximumBid;
      } else {
        int second = 0;
        for (int index = 0; index < maximums.size(); index++) {
          if (index != winnerIndex) second = Math.max(second, maximums.get(index).amount());
        }
        price = new Money(Math.min(maximumBid.amount(), Math.max(minimumBid, second + 5)));
      }
      entity.sellShare(debtor, buyer, price);
      events.soldEntityShare(debtor, entity, buyer, price);
    }
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

    default void soldEntityShare(Player seller, LegalEntity entity, Player buyer, Money price) {
    }

    default void distressedSaleStarted(Player seller, Ownable land) {
    }

    default void distressedSaleNoBidder(Player seller, Ownable land) {
    }

    default void distressedOffer(Player bidder, Ownable land, Money price) {
    }

    default void distressedSaleWon(Player bidder, Ownable land, Money price) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=1b5cc5a83cf8552334483ce2d1596b30999bf317ba4d6a1fc958d289f591cf82
scope.0.id=Y2xhc3M6QmFua3J1cHRjeSNCYW5rcnVwdGN5OjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=170
scope.0.semanticHash=9630e7ef3e92211931e1f2d4b91b0b70482c954fc36c0185aacb56c9f3895df5
scope.1.id=Y2xhc3M6QmFua3J1cHRjeS5FdmVudHMjRXZlbnRzOjEzMw
scope.1.kind=class
scope.1.startLine=133
scope.1.endLine=169
scope.1.semanticHash=f60cd7cd8e870c1a6dc4dd771890bdc0a0ae881bade8c6dc1699ac4c496c8a82
scope.2.id=ZmllbGQ6QmFua3J1cHRjeSNkZWVkczoxNQ
scope.2.kind=field
scope.2.startLine=15
scope.2.endLine=15
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6QmFua3J1cHRjeSNkaXN0cmVzc2VkU2FsZToyMA
scope.3.kind=field
scope.3.startLine=20
scope.3.endLine=20
scope.3.semanticHash=995abbbe28ea58edb93f9606941ffc595b47284363f3d9a5e0df2b8c07daf873
scope.4.id=ZmllbGQ6QmFua3J1cHRjeSNldmVudHM6MTk
scope.4.kind=field
scope.4.startLine=19
scope.4.endLine=19
scope.4.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.5.id=ZmllbGQ6QmFua3J1cHRjeSNwbGF5ZXJzOjE3
scope.5.kind=field
scope.5.startLine=17
scope.5.endLine=17
scope.5.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.6.id=ZmllbGQ6QmFua3J1cHRjeSNydWxlczoxNg
scope.6.kind=field
scope.6.startLine=16
scope.6.endLine=16
scope.6.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.7.id=ZmllbGQ6QmFua3J1cHRjeSNzdHJhdGVnaWVzOjE4
scope.7.kind=field
scope.7.startLine=18
scope.7.endLine=18
scope.7.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.8.id=bWV0aG9kOkJhbmtydXB0Y3kjYW5ub3VuY2VXaW5uZXJJZk9ubHlPbmVSZW1haW5zKDApOjQ4
scope.8.kind=method
scope.8.startLine=48
scope.8.endLine=51
scope.8.semanticHash=bc5e8420c1eba72b608fff2b5e327ea9eb5f533c2870085599d3ebe1e064c4e0
scope.9.id=bWV0aG9kOkJhbmtydXB0Y3kjYXVjdGlvbigxKToxMDQ
scope.9.kind=method
scope.9.startLine=104
scope.9.endLine=116
scope.9.semanticHash=3e93ee3fc6fc659b753cd5beaaea3733e4b4a54e0fe4899b6385836960354588
scope.10.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb0JhbmsoMSk6ODE
scope.10.kind=method
scope.10.startLine=81
scope.10.endLine=88
scope.10.semanticHash=090205af28921fa3019b8e76d87fb7389a379a4d4b535c916574a0488e5164ea
scope.11.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb1BsYXllcigyKTo5MA
scope.11.kind=method
scope.11.startLine=90
scope.11.endLine=98
scope.11.semanticHash=84653e21ff8b0e80973d19a61046c956c4cf0ade646d096a7d42861b097ef92b
scope.12.id=bWV0aG9kOkJhbmtydXB0Y3kjY3Rvcig1KToyMg
scope.12.kind=method
scope.12.startLine=22
scope.12.endLine=29
scope.12.semanticHash=1597590f8efc55dc62be40781fa34de976b8e6aaa6db55ef84693be5bcf033c3
scope.13.id=bWV0aG9kOkJhbmtydXB0Y3kjaW5oZXJpdCgzKToxMTg
scope.13.kind=method
scope.13.startLine=118
scope.13.endLine=123
scope.13.semanticHash=1793205acbaeb54b9bc841e1476fda05364b8ab581f865828adad57e9123e319
scope.14.id=bWV0aG9kOkJhbmtydXB0Y3kjbW9ydGdhZ2VVbnRpbFNvbHZlbnQoMSk6NzA
scope.14.kind=method
scope.14.startLine=70
scope.14.endLine=79
scope.14.semanticHash=8f8f6f2b7835e43d6e2f9a5014712a7cb6163810c6aa744635f1407dee5da9a0
scope.15.id=bWV0aG9kOkJhbmtydXB0Y3kjb3duZWRMYW5kSW5Cb2FyZE9yZGVyKDEpOjEwMA
scope.15.kind=method
scope.15.startLine=100
scope.15.endLine=102
scope.15.semanticHash=ff053001f78ad4941b56eff8f82f0aebeb50aee2c89d74509e5443d6c82df11d
scope.16.id=bWV0aG9kOkJhbmtydXB0Y3kjcmVzb2x2ZSgyKTozMQ
scope.16.kind=method
scope.16.startLine=31
scope.16.endLine=46
scope.16.semanticHash=18f158ff598e2655199bb90d6f419a6b0c72fbda888a9ee8fae9be4f2a85f257
scope.17.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEhvdXNlc1VudGlsU29sdmVudCgxKTo1Mw
scope.17.kind=method
scope.17.startLine=53
scope.17.endLine=59
scope.17.semanticHash=998ca257ca19deb67b829d474566a243d2f60a666130211e7f5855e4c33e0ed9
scope.18.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEltcHJvdmVtZW50c0lmTmVlZGVkKDIpOjYx
scope.18.kind=method
scope.18.startLine=61
scope.18.endLine=68
scope.18.semanticHash=7d0166a3bd740e4bfe6e026292d0aadf641a7bf05d1917e3900a0dad2d3682df
scope.19.id=bWV0aG9kOkJhbmtydXB0Y3kjc2V0dGxlSW5oZXJpdGVkTW9ydGdhZ2UoMik6MTI1
scope.19.kind=method
scope.19.startLine=125
scope.19.endLine=131
scope.19.semanticHash=6cf12a1d47550fc0be0245613176b9d8119a4f0e5daf9b80dafe0be03d190abf
scope.20.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2JhbmtydXB0KDIpOjEzNA
scope.20.kind=method
scope.20.startLine=134
scope.20.endLine=134
scope.20.semanticHash=7f4245795eb5364550035e63391a6b0f0cbe4d6960405be388bcba13e1b7fe20
scope.21.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRPZmZlcigzKToxNjQ
scope.21.kind=method
scope.21.startLine=164
scope.21.endLine=165
scope.21.semanticHash=5aa7159f27bd9240fbbdfb13b1427861c7064b9e7c4d9237006826493e108bac
scope.22.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRTYWxlTm9CaWRkZXIoMik6MTYx
scope.22.kind=method
scope.22.startLine=161
scope.22.endLine=162
scope.22.semanticHash=6800451a787d04b3dc4e19d9eda3cbaea9c9810bbe6d24a117185b32885091fd
scope.23.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRTYWxlU3RhcnRlZCgyKToxNTg
scope.23.kind=method
scope.23.startLine=158
scope.23.endLine=159
scope.23.semanticHash=e055e92e186bf4a57b7b85cbc73cdbf43ea72b9ab22bf0e76047f6b5d0aa98a2
scope.24.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRTYWxlV29uKDMpOjE2Nw
scope.24.kind=method
scope.24.startLine=167
scope.24.endLine=168
scope.24.semanticHash=e5e3462c96b4520a02805f9871ae8b8c4b81b8e0f3f59b3a9a2360aa97e67f4d
scope.25.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2luaGVyaXRlZCgzKToxNDY
scope.25.kind=method
scope.25.startLine=146
scope.25.endLine=147
scope.25.semanticHash=6736013fa0fb98388e66ca94037bf024de1b9fd9d07f745bff07ad77a3a3c5d6
scope.26.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2tlcHRNb3J0Z2FnZSgzKToxNDk
scope.26.kind=method
scope.26.startLine=149
scope.26.endLine=150
scope.26.semanticHash=165fe7febc18e8c2d65fd3763f99d8afe3c3a2516f75d6679bca9d110b042722
scope.27.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2xpZnRlZE1vcnRnYWdlKDMpOjE1Mg
scope.27.kind=method
scope.27.startLine=152
scope.27.endLine=153
scope.27.semanticHash=ea8c019db19059d4704a89148d99d006e5f96069ef098a2bc3396717a63bbf26
scope.28.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI21vcnRnYWdlZCgzKToxNDA
scope.28.kind=method
scope.28.startLine=140
scope.28.endLine=141
scope.28.semanticHash=5abce788c801f27ad6ee7f179d2636b9732b563ebc994dab3436fc500067ec04
scope.29.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3NvbGRIb3VzZSgzKToxMzc
scope.29.kind=method
scope.29.startLine=137
scope.29.endLine=138
scope.29.semanticHash=152b94650137ea5d4adf43d125bc9566e5841576ff487d806b0fe842f2f8974e
scope.30.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3NvbGRUb1BlZXIoNCk6MTU1
scope.30.kind=method
scope.30.startLine=155
scope.30.endLine=156
scope.30.semanticHash=84c012cbd6eb17175d00e593ebc380bc29b348a961821eb131e7119cf4dbe018
scope.31.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbigxKToxMzU
scope.31.kind=method
scope.31.startLine=135
scope.31.endLine=135
scope.31.semanticHash=ce12067a8a202d3129808f17240e0aa178642ab35e8daa5d55fc685008b5a7b3
scope.32.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbkF0QXVjdGlvbigzKToxNDM
scope.32.kind=method
scope.32.startLine=143
scope.32.endLine=144
scope.32.semanticHash=6f108f1caf6087bc1f1df1b92cebd84d0f234d46d35fc9e32cb6429b0450009b
*/
