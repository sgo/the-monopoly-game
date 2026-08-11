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
    boolean shareSold = sellEntitySharesUntilSolvent(debtor);
    if (debtor.account().balance().amount().amount() >= 0) return;
    finalizeBankruptcy(debtor, creditor, shareSold);
  }

  private void finalizeBankruptcy(Player debtor, Player creditor, boolean preserveNegativeBalance) {
    Money remaining = debtor.account().balance().amount();
    if (!preserveNegativeBalance) debtor.account().deposit(new Money(-remaining.amount()));
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

  private boolean sellEntitySharesUntilSolvent(Player debtor) {
    for (LegalEntity entity : deeds.legalEntities()) {
      if (!Money.ZERO.exceeds(debtor.account().balance().amount())) return false;
      if (entity.shareOf(debtor) == 0.0) continue;
      if (entity.shareholders().size() == 1) {
        liquidateEntity(debtor, entity);
        distressedSale.resolve(debtor);
        continue;
      }
      if (sellShareToHighestBidder(debtor, entity)) return true;
    }
    return false;
  }

  private void liquidateEntity(Player debtor, LegalEntity entity) {
    Money transferred = entity.liquidateTo(debtor);
    deeds.dissolve(entity, debtor);
    events.entityLiquidated(debtor, entity, transferred);
  }

  private boolean sellShareToHighestBidder(Player debtor, LegalEntity entity) {
    List<Bid> bids = bidsFor(debtor, entity);
    if (bids.isEmpty()) return false;
    Bid winner = highestBid(bids);
    Money price = shareSalePrice(winner, bids);
    entity.sellShare(debtor, winner.bidder(), price);
    events.soldEntityShare(debtor, entity, winner.bidder(), price);
    return true;
  }

  private List<Bid> bidsFor(Player debtor, LegalEntity entity) {
    List<Bid> bids = new java.util.ArrayList<>();
    for (Player candidate : players) {
      if (!isEligibleBidder(debtor, entity, candidate)) continue;
      int available = candidate.account().balance().amount().amount();
      Money offered = new Money(Math.min(available, available * 35 / 100));
      if (offered.amount() > 0) bids.add(new Bid(candidate, offered));
    }
    return bids;
  }

  private boolean isEligibleBidder(Player debtor, LegalEntity entity, Player candidate) {
    return !candidate.id().equals(debtor.id()) && !deeds.isBankrupt(candidate)
        && entity.shareOf(candidate) != 0.0
        && strategies.forPlayer(candidate) instanceof Greedo greedo
        && greedo.legalEntityTradingEnabled();
  }

  private Bid highestBid(List<Bid> bids) {
    Bid winner = bids.getFirst();
    for (Bid bid : bids) if (bid.maximum().exceeds(winner.maximum())) winner = bid;
    return winner;
  }

  private Money shareSalePrice(Bid winner, List<Bid> bids) {
    int second = bids.stream().filter(bid -> bid != winner)
        .mapToInt(bid -> bid.maximum().amount()).max().orElse(0);
    return new Money(Math.min(winner.maximum().amount(), second + 5));
  }

  private record Bid(Player bidder, Money maximum) {
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

    default void entityLiquidated(Player recipient, LegalEntity entity, Money amount) {
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
moduleHash=0fe89e329cf3856df70ecdc27b88b3101957dbb53fa2c3afb02fc553535591e7
scope.0.id=Y2xhc3M6QmFua3J1cHRjeSNCYW5rcnVwdGN5OjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=248
scope.0.semanticHash=f6b7b216f423de81d6cb85abf7919f104b48d53a7c37fdc4af33513d5843b856
scope.1.id=Y2xhc3M6QmFua3J1cHRjeS5CaWQjQmlkOjE1MA
scope.1.kind=class
scope.1.startLine=150
scope.1.endLine=151
scope.1.semanticHash=97b314026df1eab366d688333aef7df8f49ff6e2e5a587fcdb8be53fb04761a7
scope.2.id=Y2xhc3M6QmFua3J1cHRjeS5FdmVudHMjRXZlbnRzOjIwNQ
scope.2.kind=class
scope.2.startLine=205
scope.2.endLine=247
scope.2.semanticHash=49c866e91e761ba4697e4175b25ccbf96302fcbe417609c148011cf59b5dffce
scope.3.id=ZmllbGQ6QmFua3J1cHRjeSNkZWVkczoxNQ
scope.3.kind=field
scope.3.startLine=15
scope.3.endLine=15
scope.3.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.4.id=ZmllbGQ6QmFua3J1cHRjeSNkaXN0cmVzc2VkU2FsZToyMA
scope.4.kind=field
scope.4.startLine=20
scope.4.endLine=20
scope.4.semanticHash=995abbbe28ea58edb93f9606941ffc595b47284363f3d9a5e0df2b8c07daf873
scope.5.id=ZmllbGQ6QmFua3J1cHRjeSNldmVudHM6MTk
scope.5.kind=field
scope.5.startLine=19
scope.5.endLine=19
scope.5.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.6.id=ZmllbGQ6QmFua3J1cHRjeSNwbGF5ZXJzOjE3
scope.6.kind=field
scope.6.startLine=17
scope.6.endLine=17
scope.6.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.7.id=ZmllbGQ6QmFua3J1cHRjeSNydWxlczoxNg
scope.7.kind=field
scope.7.startLine=16
scope.7.endLine=16
scope.7.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.8.id=ZmllbGQ6QmFua3J1cHRjeSNzdHJhdGVnaWVzOjE4
scope.8.kind=field
scope.8.startLine=18
scope.8.endLine=18
scope.8.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.9.id=ZmllbGQ6QmFua3J1cHRjeS5CaWQjYmlkZGVyOjE1MA
scope.9.kind=field
scope.9.startLine=150
scope.9.endLine=150
scope.9.semanticHash=15124b96dbece005b8c8c7de9124d20899e2e3f600b6ba015c98543b3281553c
scope.10.id=ZmllbGQ6QmFua3J1cHRjeS5CaWQjbWF4aW11bToxNTA
scope.10.kind=field
scope.10.startLine=150
scope.10.endLine=150
scope.10.semanticHash=1a25c765871611156bf8758a3acf6c7bee322b41a33639bf6b6a8a0f724f25bb
scope.11.id=bWV0aG9kOkJhbmtydXB0Y3kjYW5ub3VuY2VXaW5uZXJJZk9ubHlPbmVSZW1haW5zKDApOjUz
scope.11.kind=method
scope.11.startLine=53
scope.11.endLine=56
scope.11.semanticHash=bc5e8420c1eba72b608fff2b5e327ea9eb5f533c2870085599d3ebe1e064c4e0
scope.12.id=bWV0aG9kOkJhbmtydXB0Y3kjYXVjdGlvbigxKToxNzY
scope.12.kind=method
scope.12.startLine=176
scope.12.endLine=188
scope.12.semanticHash=3e93ee3fc6fc659b753cd5beaaea3733e4b4a54e0fe4899b6385836960354588
scope.13.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb0JhbmsoMSk6MTUz
scope.13.kind=method
scope.13.startLine=153
scope.13.endLine=160
scope.13.semanticHash=090205af28921fa3019b8e76d87fb7389a379a4d4b535c916574a0488e5164ea
scope.14.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb1BsYXllcigyKToxNjI
scope.14.kind=method
scope.14.startLine=162
scope.14.endLine=170
scope.14.semanticHash=84653e21ff8b0e80973d19a61046c956c4cf0ade646d096a7d42861b097ef92b
scope.15.id=bWV0aG9kOkJhbmtydXB0Y3kjYmlkc0ZvcigzKToxMjA
scope.15.kind=method
scope.15.startLine=120
scope.15.endLine=129
scope.15.semanticHash=e62248e640169f9722d8ef94bad66c90e233f819d8bbcac534f741056cdf758a
scope.16.id=bWV0aG9kOkJhbmtydXB0Y3kjY3Rvcig1KToyMg
scope.16.kind=method
scope.16.startLine=22
scope.16.endLine=29
scope.16.semanticHash=1597590f8efc55dc62be40781fa34de976b8e6aaa6db55ef84693be5bcf033c3
scope.17.id=bWV0aG9kOkJhbmtydXB0Y3kjZmluYWxpemVCYW5rcnVwdGN5KDIpOjQy
scope.17.kind=method
scope.17.startLine=42
scope.17.endLine=51
scope.17.semanticHash=bf9a6c656c842b574617110e9a08dc8b0fdeab1730de2c14273248690a122c51
scope.18.id=bWV0aG9kOkJhbmtydXB0Y3kjaGlnaGVzdEJpZCgxKToxMzg
scope.18.kind=method
scope.18.startLine=138
scope.18.endLine=142
scope.18.semanticHash=12a7374c305de1a9ee49d04a37daad1f4760468f56850bdd389dba73cb83ee00
scope.19.id=bWV0aG9kOkJhbmtydXB0Y3kjaW5oZXJpdCgzKToxOTA
scope.19.kind=method
scope.19.startLine=190
scope.19.endLine=195
scope.19.semanticHash=1793205acbaeb54b9bc841e1476fda05364b8ab581f865828adad57e9123e319
scope.20.id=bWV0aG9kOkJhbmtydXB0Y3kjaXNFbGlnaWJsZUJpZGRlcigzKToxMzE
scope.20.kind=method
scope.20.startLine=131
scope.20.endLine=136
scope.20.semanticHash=e4e03ea4bec009f858195831c4feefd14ee41b0c8e3bea3b8aaef28156a610ec
scope.21.id=bWV0aG9kOkJhbmtydXB0Y3kjbGlxdWlkYXRlRW50aXR5KDIpOjk5
scope.21.kind=method
scope.21.startLine=99
scope.21.endLine=103
scope.21.semanticHash=fe736ff449499cf51d3299639b19b8c506ee4e6002ddf1a361601479680588fb
scope.22.id=bWV0aG9kOkJhbmtydXB0Y3kjbWluaW11bVNoYXJlQmlkKDIpOjExNQ
scope.22.kind=method
scope.22.startLine=115
scope.22.endLine=118
scope.22.semanticHash=ce64725426ee7da529673f150722f7889b8940d3043a86c044a4e56136e0a8c9
scope.23.id=bWV0aG9kOkJhbmtydXB0Y3kjbW9ydGdhZ2VVbnRpbFNvbHZlbnQoMSk6NzU
scope.23.kind=method
scope.23.startLine=75
scope.23.endLine=84
scope.23.semanticHash=8f8f6f2b7835e43d6e2f9a5014712a7cb6163810c6aa744635f1407dee5da9a0
scope.24.id=bWV0aG9kOkJhbmtydXB0Y3kjb3duZWRMYW5kSW5Cb2FyZE9yZGVyKDEpOjE3Mg
scope.24.kind=method
scope.24.startLine=172
scope.24.endLine=174
scope.24.semanticHash=ff053001f78ad4941b56eff8f82f0aebeb50aee2c89d74509e5443d6c82df11d
scope.25.id=bWV0aG9kOkJhbmtydXB0Y3kjcmVzb2x2ZSgyKTozMQ
scope.25.kind=method
scope.25.startLine=31
scope.25.endLine=40
scope.25.semanticHash=7f17a3bb48a88ab9239dba55b98b2c576ba735e838e5ab64debcdea839a1e1ea
scope.26.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEVudGl0eVNoYXJlc1VudGlsU29sdmVudCgxKTo4Ng
scope.26.kind=method
scope.26.startLine=86
scope.26.endLine=97
scope.26.semanticHash=a382311af0cb834f2db79407212d29342f32d935eb98c61734e467855362f32e
scope.27.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEhvdXNlc1VudGlsU29sdmVudCgxKTo1OA
scope.27.kind=method
scope.27.startLine=58
scope.27.endLine=64
scope.27.semanticHash=998ca257ca19deb67b829d474566a243d2f60a666130211e7f5855e4c33e0ed9
scope.28.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEltcHJvdmVtZW50c0lmTmVlZGVkKDIpOjY2
scope.28.kind=method
scope.28.startLine=66
scope.28.endLine=73
scope.28.semanticHash=7d0166a3bd740e4bfe6e026292d0aadf641a7bf05d1917e3900a0dad2d3682df
scope.29.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbFNoYXJlVG9IaWdoZXN0QmlkZGVyKDIpOjEwNQ
scope.29.kind=method
scope.29.startLine=105
scope.29.endLine=113
scope.29.semanticHash=1e5c5dd040eac773456b5c486d23bfdd7473486ed6505d3ce1754a0779b26baf
scope.30.id=bWV0aG9kOkJhbmtydXB0Y3kjc2V0dGxlSW5oZXJpdGVkTW9ydGdhZ2UoMik6MTk3
scope.30.kind=method
scope.30.startLine=197
scope.30.endLine=203
scope.30.semanticHash=6cf12a1d47550fc0be0245613176b9d8119a4f0e5daf9b80dafe0be03d190abf
scope.31.id=bWV0aG9kOkJhbmtydXB0Y3kjc2hhcmVTYWxlUHJpY2UoMyk6MTQ0
scope.31.kind=method
scope.31.startLine=144
scope.31.endLine=148
scope.31.semanticHash=2630ca8130d4cf86c28fc244090e14fcad51b264f1680aaa981bbc638574c550
scope.32.id=bWV0aG9kOkJhbmtydXB0Y3kuQmlkI2N0b3IoMik6MTUw
scope.32.kind=method
scope.32.startLine=1
scope.32.endLine=248
scope.32.semanticHash=098ddb6232880d325325826fa3fa6ac781da6685b1e55ec844e8855a035a27bd
scope.33.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2JhbmtydXB0KDIpOjIwNg
scope.33.kind=method
scope.33.startLine=206
scope.33.endLine=206
scope.33.semanticHash=7f4245795eb5364550035e63391a6b0f0cbe4d6960405be388bcba13e1b7fe20
scope.34.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRPZmZlcigzKToyNDI
scope.34.kind=method
scope.34.startLine=242
scope.34.endLine=243
scope.34.semanticHash=5aa7159f27bd9240fbbdfb13b1427861c7064b9e7c4d9237006826493e108bac
scope.35.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRTYWxlTm9CaWRkZXIoMik6MjM5
scope.35.kind=method
scope.35.startLine=239
scope.35.endLine=240
scope.35.semanticHash=6800451a787d04b3dc4e19d9eda3cbaea9c9810bbe6d24a117185b32885091fd
scope.36.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRTYWxlU3RhcnRlZCgyKToyMzY
scope.36.kind=method
scope.36.startLine=236
scope.36.endLine=237
scope.36.semanticHash=e055e92e186bf4a57b7b85cbc73cdbf43ea72b9ab22bf0e76047f6b5d0aa98a2
scope.37.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2Rpc3RyZXNzZWRTYWxlV29uKDMpOjI0NQ
scope.37.kind=method
scope.37.startLine=245
scope.37.endLine=246
scope.37.semanticHash=e5e3462c96b4520a02805f9871ae8b8c4b81b8e0f3f59b3a9a2360aa97e67f4d
scope.38.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2VudGl0eUxpcXVpZGF0ZWQoMyk6MjMz
scope.38.kind=method
scope.38.startLine=233
scope.38.endLine=234
scope.38.semanticHash=76940c868639e131e319b4a989bf24a9750006bb9ad4980532b469a9056eecad
scope.39.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2luaGVyaXRlZCgzKToyMTg
scope.39.kind=method
scope.39.startLine=218
scope.39.endLine=219
scope.39.semanticHash=6736013fa0fb98388e66ca94037bf024de1b9fd9d07f745bff07ad77a3a3c5d6
scope.40.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2tlcHRNb3J0Z2FnZSgzKToyMjE
scope.40.kind=method
scope.40.startLine=221
scope.40.endLine=222
scope.40.semanticHash=165fe7febc18e8c2d65fd3763f99d8afe3c3a2516f75d6679bca9d110b042722
scope.41.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2xpZnRlZE1vcnRnYWdlKDMpOjIyNA
scope.41.kind=method
scope.41.startLine=224
scope.41.endLine=225
scope.41.semanticHash=ea8c019db19059d4704a89148d99d006e5f96069ef098a2bc3396717a63bbf26
scope.42.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI21vcnRnYWdlZCgzKToyMTI
scope.42.kind=method
scope.42.startLine=212
scope.42.endLine=213
scope.42.semanticHash=5abce788c801f27ad6ee7f179d2636b9732b563ebc994dab3436fc500067ec04
scope.43.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3NvbGRFbnRpdHlTaGFyZSg0KToyMzA
scope.43.kind=method
scope.43.startLine=230
scope.43.endLine=231
scope.43.semanticHash=f72ab6542f387c272a05fcf755f72e06a80eb82f0f9460c4acbf1a16934f908a
scope.44.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3NvbGRIb3VzZSgzKToyMDk
scope.44.kind=method
scope.44.startLine=209
scope.44.endLine=210
scope.44.semanticHash=152b94650137ea5d4adf43d125bc9566e5841576ff487d806b0fe842f2f8974e
scope.45.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3NvbGRUb1BlZXIoNCk6MjI3
scope.45.kind=method
scope.45.startLine=227
scope.45.endLine=228
scope.45.semanticHash=84c012cbd6eb17175d00e593ebc380bc29b348a961821eb131e7119cf4dbe018
scope.46.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbigxKToyMDc
scope.46.kind=method
scope.46.startLine=207
scope.46.endLine=207
scope.46.semanticHash=ce12067a8a202d3129808f17240e0aa178642ab35e8daa5d55fc685008b5a7b3
scope.47.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbkF0QXVjdGlvbigzKToyMTU
scope.47.kind=method
scope.47.startLine=215
scope.47.endLine=216
scope.47.semanticHash=6f108f1caf6087bc1f1df1b92cebd84d0f234d46d35fc9e32cb6429b0450009b
*/
