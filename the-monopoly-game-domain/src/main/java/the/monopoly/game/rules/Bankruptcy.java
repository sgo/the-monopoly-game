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
    if (debtor.account().balance().amount().amount() >= 0 || deeds.isBankrupt(debtor)) return;
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
      if (debtor.account().balance().amount().amount() >= 0) return;
      Street street = rules.create(type);
      if (street instanceof ColourStreet colour && deeds.housesBuiltOn(colour) > 0) deeds.sellHouse(colour, debtor);
    }
  }

  private void mortgageUntilSolvent(Player debtor) {
    for (Street.Type type : ownedLandInBoardOrder(debtor)) {
      if (debtor.account().balance().amount().amount() >= 0) return;
      Ownable land = (Ownable) rules.create(type);
      if (!deeds.isMortgaged(land)) deeds.mortgage(land, debtor);
    }
  }

  private void bankruptToBank(Player debtor) {
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
