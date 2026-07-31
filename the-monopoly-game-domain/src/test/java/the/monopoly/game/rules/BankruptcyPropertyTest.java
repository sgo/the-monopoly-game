package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BankruptcyTest samples hand-picked deficits, holdings, and creditors. This
 * sweeps the whole board, a wide range of deficits, houses, hotels, and
 * mortgages, and both the bank and a creditor, to pin down the invariants of
 * resolve: a debtor is never left with a negative balance, a bankrupt debtor
 * has nothing left, and once the debtor is solvent or bankrupt, resolving
 * again changes nothing.
 */
@Tag("property-test")
class BankruptcyPropertyTest {
  private static final int STARTING_BALANCE = 1500;

  @Test
  void resolvingNeverLeavesTheDebtorWithANegativeBalance() {
    PropertyChecker.forAll(cases(), c -> {
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player debtor = player(rules, "debtor");
      Player creditor = player(rules, "creditor");
      hold(rules, deeds, debtor, c.holdings());
      debtor.account().withdraw(new Money(c.deficit()));

      new Bankruptcy(deeds, rules, List.of(debtor, creditor), Strategy.OfPlayers.NOBODY_DECIDES, noEvents())
          .resolve(debtor, c.toCreditor() ? creditor : null);

      int balance = debtor.account().balance().amount().amount();
      return balance >= 0 && (!deeds.isBankrupt(debtor) || balance == 0);
    });
  }

  @Test
  void resolvingAgainChangesNothingOnceTheDebtorIsSolventOrBankrupt() {
    PropertyChecker.forAll(cases(), c -> {
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player debtor = player(rules, "debtor");
      Player creditor = player(rules, "creditor");
      hold(rules, deeds, debtor, c.holdings());
      debtor.account().withdraw(new Money(c.deficit()));
      Bankruptcy bankruptcy =
          new Bankruptcy(deeds, rules, List.of(debtor, creditor), Strategy.OfPlayers.NOBODY_DECIDES, noEvents());

      bankruptcy.resolve(debtor, c.toCreditor() ? creditor : null);
      List<String> settled = stateOf(rules, deeds, debtor, creditor);
      bankruptcy.resolve(debtor, c.toCreditor() ? creditor : null);

      return stateOf(rules, deeds, debtor, creditor).equals(settled);
    });
  }

  private void hold(Rule.Set rules, Deeds deeds, Player owner, List<Holding> holdings) {
    Set<Street.Type> held = new HashSet<>();
    for (Holding holding : holdings) {
      if (!held.add(holding.type())) continue;
      Ownable land = (Ownable) rules.create(holding.type());
      deeds.sell(land, owner, Money.ZERO);
      if (holding.improvement() >= 1 && land instanceof ColourStreet colour) {
        if (holding.improvement() == 4) deeds.arrangeHotel(colour);
        else deeds.arrangeHouses(colour, holding.improvement());
      }
      if (holding.mortgaged()) deeds.arrangeMortgaged(land);
    }
  }

  private List<String> stateOf(Rule.Set rules, Deeds deeds, Player debtor, Player creditor) {
    List<String> state = new ArrayList<>();
    state.add(debtor.account().balance().amount().amount() + ":" + creditor.account().balance().amount().amount());
    state.add(String.valueOf(deeds.isBankrupt(debtor)));
    for (Street.Type type : rules.gameboard().layout()) {
      Street street = rules.create(type);
      if (!(street instanceof Ownable land)) continue;
      deeds.ownerOf(type).ifPresent(owner -> state.add(type + "->" + owner));
      if (street instanceof ColourStreet colour) {
        if (deeds.housesBuiltOn(colour) > 0) state.add(type + "houses" + deeds.housesBuiltOn(colour));
        if (deeds.hasHotelOn(colour)) state.add(type + "hotel");
      }
      if (deeds.isMortgaged(land)) state.add(type + "mortgaged");
    }
    return state;
  }

  private Player player(Rule.Set rules, String name) {
    Player.ID id = new Player.ID(name);
    rules.bank().createAccountFor(id);
    Player player = new Player(id, rules.bank().accountOf(id));
    player.account().deposit(new Money(STARTING_BALANCE));
    return player;
  }

  private Bankruptcy.Events noEvents() {
    return new Bankruptcy.Events() {
      @Override
      public void bankrupt(Player debtor, Player creditor) {
      }

      @Override
      public void won(Player player) {
      }
    };
  }

  private Generator<Case> cases() {
    return Generator.integers(0, 2_500).flatMap(deficit ->
        Generator.booleans().flatMap(toCreditor ->
            Generator.listsOf(holding()).map(holdings -> new Case(deficit, toCreditor, holdings))));
  }

  private Generator<Holding> holding() {
    return Generator.sampledFrom(streetTypes()).flatMap(type ->
        Generator.integers(0, 4).flatMap(improvement ->
            Generator.booleans().map(mortgaged -> new Holding(type, improvement, mortgaged))));
  }

  private List<Street.Type> streetTypes() {
    Rule.Set rules = Rule.Set.Type.official.create();
    return rules.gameboard().layout().stream().filter(type -> rules.create(type) instanceof Ownable).toList();
  }

  private record Case(int deficit, boolean toCreditor, List<Holding> holdings) {
  }

  private record Holding(Street.Type type, int improvement, boolean mortgaged) {
  }
}
