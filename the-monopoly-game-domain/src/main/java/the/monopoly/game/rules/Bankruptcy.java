package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.AgreeIfAffordable;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

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
    sellHousesUntilSolvent(debtor);
    mortgageUntilSolvent(debtor);
    if (debtor.account().balance().amount().amount() >= 0) return;

    Money remaining = debtor.account().balance().amount();
    debtor.account().deposit(new Money(-remaining.amount()));
    deeds.bankrupt(debtor);
    if (creditor == null) bankruptToBank(debtor);
    else bankruptToPlayer(debtor, creditor);
    events.bankrupt(debtor, creditor);
    players.stream().filter(it -> !deeds.isBankrupt(it)).findFirst().ifPresent(events::won);
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
    while (debtor.account().balance().amount().amount() < 0 && deeds.housesBuiltOn(colour) > 0)
      deeds.sellHouse(colour, debtor);
  }

  private void mortgageUntilSolvent(Player debtor) {
    for (Street.Type type : ownedLandInBoardOrder(debtor)) {
      if (!Money.ZERO.exceeds(debtor.account().balance().amount())) return;
      Ownable land = (Ownable) rules.create(type);
      if (!deeds.isMortgaged(land)) deeds.mortgage(land, debtor);
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
    settleInheritedMortgage(land, winner);
  }

  private void inherit(Ownable land, Player debtor, Player creditor) {
    deeds.transferWithoutPayment(land, debtor, creditor);
    if (deeds.isMortgaged(land)) creditor.account().deposit(land.landMortgageValue());
    settleInheritedMortgage(land, creditor);
  }

  private void settleInheritedMortgage(Ownable land, Player owner) {
    if (!deeds.isMortgaged(land)) return;
    if (strategies.forPlayer(owner) instanceof AgreeIfAffordable
        && owner.account().balance().amount().covers(land.landMortgageValue().plus(new Money((land.landMortgageValue().amount() + 9) / 10))))
      deeds.liftMortgage(land, owner);
    else deeds.keepMortgaged(land, owner);
  }

  public interface Events {
    void bankrupt(Player debtor, Player creditor);
    void won(Player player);
  }
}

/* mutate4java-manifest
version=1
moduleHash=39538a8c66dd960b0ca3cca033c07a582810ba7fbf8fbb84d36bf079e8a9becd
scope.0.id=Y2xhc3M6QmFua3J1cHRjeSNCYW5rcnVwdGN5OjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=119
scope.0.semanticHash=ae028378fdfdcdf7152ca3296d12fefeb952f1f39572f325f44ee63db993faad
scope.1.id=Y2xhc3M6QmFua3J1cHRjeS5FdmVudHMjRXZlbnRzOjExNQ
scope.1.kind=class
scope.1.startLine=115
scope.1.endLine=118
scope.1.semanticHash=6122c4c3fab2bcaf5a14be40cb45ea7c1b06e0119453eefe9f42b56809f3b570
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
scope.7.id=bWV0aG9kOkJhbmtydXB0Y3kjYXVjdGlvbigxKTo4OA
scope.7.kind=method
scope.7.startLine=88
scope.7.endLine=99
scope.7.semanticHash=dbdb6fef56cdca619384b517d6c8599089dadc71446b2982670fc6b6f61c3487
scope.8.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb0JhbmsoMSk6NjU
scope.8.kind=method
scope.8.startLine=65
scope.8.endLine=72
scope.8.semanticHash=090205af28921fa3019b8e76d87fb7389a379a4d4b535c916574a0488e5164ea
scope.9.id=bWV0aG9kOkJhbmtydXB0Y3kjYmFua3J1cHRUb1BsYXllcigyKTo3NA
scope.9.kind=method
scope.9.startLine=74
scope.9.endLine=82
scope.9.semanticHash=84653e21ff8b0e80973d19a61046c956c4cf0ade646d096a7d42861b097ef92b
scope.10.id=bWV0aG9kOkJhbmtydXB0Y3kjY3Rvcig1KToyMQ
scope.10.kind=method
scope.10.startLine=21
scope.10.endLine=27
scope.10.semanticHash=82adf465e4d113095ad1bb580939041a479a7fa85a6434b75704fd88c5e28cb5
scope.11.id=bWV0aG9kOkJhbmtydXB0Y3kjaW5oZXJpdCgzKToxMDE
scope.11.kind=method
scope.11.startLine=101
scope.11.endLine=105
scope.11.semanticHash=34a5448170dd42a76b48148a1599059d5342470667b8e2dabb75dea11130aba9
scope.12.id=bWV0aG9kOkJhbmtydXB0Y3kjbW9ydGdhZ2VVbnRpbFNvbHZlbnQoMSk6NTc
scope.12.kind=method
scope.12.startLine=57
scope.12.endLine=63
scope.12.semanticHash=4f97dcc77255e32267200616878eae5664e0a6af2d0e496440605040ca551cd2
scope.13.id=bWV0aG9kOkJhbmtydXB0Y3kjb3duZWRMYW5kSW5Cb2FyZE9yZGVyKDEpOjg0
scope.13.kind=method
scope.13.startLine=84
scope.13.endLine=86
scope.13.semanticHash=ff053001f78ad4941b56eff8f82f0aebeb50aee2c89d74509e5443d6c82df11d
scope.14.id=bWV0aG9kOkJhbmtydXB0Y3kjcmVzb2x2ZSgyKToyOQ
scope.14.kind=method
scope.14.startLine=29
scope.14.endLine=43
scope.14.semanticHash=fd420addcec6ba30f1ef9c856407c383d87e669897adb8a435ed569eb856238c
scope.15.id=bWV0aG9kOkJhbmtydXB0Y3kjc2VsbEhvdXNlc1VudGlsU29sdmVudCgxKTo0NQ
scope.15.kind=method
scope.15.startLine=45
scope.15.endLine=55
scope.15.semanticHash=2c78c00828d338e6797c3b9ecec577507f12199810c4ae004c5a37dd1ed38226
scope.16.id=bWV0aG9kOkJhbmtydXB0Y3kjc2V0dGxlSW5oZXJpdGVkTW9ydGdhZ2UoMik6MTA3
scope.16.kind=method
scope.16.startLine=107
scope.16.endLine=113
scope.16.semanticHash=fed7409d5884342381d498a8f32733902198c2a12bc258825c7d50428634fc5b
scope.17.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI2JhbmtydXB0KDIpOjExNg
scope.17.kind=method
scope.17.startLine=116
scope.17.endLine=116
scope.17.semanticHash=7f4245795eb5364550035e63391a6b0f0cbe4d6960405be388bcba13e1b7fe20
scope.18.id=bWV0aG9kOkJhbmtydXB0Y3kuRXZlbnRzI3dvbigxKToxMTc
scope.18.kind=method
scope.18.startLine=117
scope.18.endLine=117
scope.18.semanticHash=ce12067a8a202d3129808f17240e0aa178642ab35e8daa5d55fc685008b5a7b3
*/
