package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.Utility;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

/**
 * Rent.resolve only ever moves money between the tenant's and owner's
 * accounts, by an amount fixed by the board and by how many spaces of that
 * kind its owner holds (and, for a utility, by the roll that landed there).
 * RentTest samples a handful of hand-picked streets, stations, and utilities
 * with the owner always claiming; this sweeps every colour street, station,
 * and utility, every valid owned count, an owner who claims and one who does
 * not, and a wide range of starting balances and rolls, to pin down
 * conservation of total money and the exact amount owed together rather than
 * at a few hand-picked points.
 */
@Tag("property-test")
class RentPropertyTest {
  private static final Generator<Integer> BALANCES = Generator.integers(0, 5_000);
  private static final Generator<Roll> ROLLS =
      Generator.zipWith(Generator.integers(1, 6), Generator.integers(1, 6), Roll::new);

  @Test
  void resolvingRentConservesTotalMoneyAndMovesExactlyTheAmountOwed() {
    PropertyChecker.forAll(cases(), c -> {
      // A fresh rule set and bank keep generated cases independent: balances
      // and ownership from one case must not become another case's setup.
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player owner = playerWith(rules, "owner");
      Player tenant = playerWith(rules, "tenant");
      if (c.monopoly()) sellWholeGroup(rules, deeds, owner, c.land().colourGroup());
      else deeds.sell(c.land(), owner, Money.ZERO);

      return conservesAndCharges(owner, tenant, c.ownerBalance(), c.tenantBalance(), c.claims(), owedRent(c),
          () -> rent(rules, deeds, List.of(owner, tenant), c.claims()).resolve(tenant, c.land()));
    });
  }

  @Test
  void resolvingStationRentConservesTotalMoneyAndMovesExactlyTheAmountOwed() {
    PropertyChecker.forAll(stationCases(), c -> {
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player owner = playerWith(rules, "owner");
      Player tenant = playerWith(rules, "tenant");
      ownCount(deeds, owner, ownableList(rules, Station.class), c.land().type(), c.ownedCount());

      Money owed = c.land().rentForOwning(c.ownedCount());
      return conservesAndCharges(owner, tenant, c.ownerBalance(), c.tenantBalance(), c.claims(), owed,
          () -> rent(rules, deeds, List.of(owner, tenant), c.claims()).resolve(tenant, c.land()));
    });
  }

  @Test
  void resolvingUtilityRentConservesTotalMoneyAndMovesExactlyTheAmountOwed() {
    PropertyChecker.forAll(utilityCases(), c -> {
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player owner = playerWith(rules, "owner");
      Player tenant = playerWith(rules, "tenant");
      ownCount(deeds, owner, ownableList(rules, Utility.class), c.land().type(), c.ownedCount());

      Money owed = new Money(c.land().rentDiceMultiplierForOwning(c.ownedCount()) * c.roll().total());
      return conservesAndCharges(owner, tenant, c.ownerBalance(), c.tenantBalance(), c.claims(), owed,
          () -> rent(rules, deeds, List.of(owner, tenant), c.claims()).resolve(tenant, c.land(), c.roll()));
    });
  }

  /**
   * Deposits the given starting balances, runs {@code resolveRent}, and checks
   * that the two accounts together hold what they held before, and that only
   * {@code owedIfClaimed} moved from tenant to owner if {@code claims} is set.
   */
  private boolean conservesAndCharges(
      Player owner, Player tenant, int ownerBalance, int tenantBalance,
      boolean claims, Money owedIfClaimed, Runnable resolveRent
  ) {
    owner.account().deposit(new Money(ownerBalance));
    tenant.account().deposit(new Money(tenantBalance));
    Money totalBefore = totalOf(owner, tenant);

    resolveRent.run();

    Money totalAfter = totalOf(owner, tenant);
    Money owed = claims ? owedIfClaimed : Money.ZERO;
    return totalBefore.equals(totalAfter)
        && owner.account().balance().amount().equals(new Money(ownerBalance).plus(owed))
        && tenant.account().balance().amount().equals(new Money(tenantBalance).minus(owed));
  }

  private Money owedRent(Case c) {
    Money vacant = c.land().vacantRent();
    return c.monopoly() ? vacant.plus(vacant) : vacant;
  }

  private Money totalOf(Player owner, Player tenant) {
    return owner.account().balance().amount().plus(tenant.account().balance().amount());
  }

  private Rent rent(Rule.Set rules, Deeds deeds, List<Player> players, boolean claims) {
    return new Rent(deeds, rules, players, player -> new FixedClaim(claims), (t, o, land, rent) -> {
    });
  }

  private void sellWholeGroup(Rule.Set rules, Deeds deeds, Player owner, Street.Colour colour) {
    rules.streets()
        .filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast)
        .filter(street -> street.colourGroup() == colour)
        .forEach(street -> deeds.sell(street, owner, Money.ZERO));
  }

  /** Sells the owner {@code count} spaces from {@code allOfKind}, starting at {@code mustInclude}. */
  private <T extends Ownable> void ownCount(Deeds deeds, Player owner, List<T> allOfKind, Street.Type mustInclude, int count) {
    int start = 0;
    for (int i = 0; i < allOfKind.size(); i++) if (allOfKind.get(i).type() == mustInclude) start = i;
    for (int i = 0; i < count; i++) deeds.sell(allOfKind.get((start + i) % allOfKind.size()), owner, Money.ZERO);
  }

  private Player playerWith(Rule.Set rules, String name) {
    Player.ID id = new Player.ID(name);
    rules.bank().createAccountFor(id);
    return new Player(id, rules.bank().accountOf(id));
  }

  private Generator<Case> cases() {
    return colourStreetTypes().flatMap(land ->
        Generator.booleans().flatMap(monopoly ->
            Generator.booleans().flatMap(claims ->
                BALANCES.flatMap(ownerBalance ->
                    BALANCES.map(tenantBalance ->
                        new Case(land, monopoly, claims, ownerBalance, tenantBalance))))));
  }

  private Generator<StationCase> stationCases() {
    return stationTypes().flatMap(land ->
        Generator.integers(1, 4).flatMap(ownedCount ->
            Generator.booleans().flatMap(claims ->
                BALANCES.flatMap(ownerBalance ->
                    BALANCES.map(tenantBalance ->
                        new StationCase(land, ownedCount, claims, ownerBalance, tenantBalance))))));
  }

  private Generator<UtilityCase> utilityCases() {
    return utilityTypes().flatMap(land ->
        Generator.integers(1, 2).flatMap(ownedCount ->
            ROLLS.flatMap(roll ->
                Generator.booleans().flatMap(claims ->
                    BALANCES.flatMap(ownerBalance ->
                        BALANCES.map(tenantBalance ->
                            new UtilityCase(land, ownedCount, roll, claims, ownerBalance, tenantBalance)))))));
  }

  private Generator<ColourStreet> colourStreetTypes() {
    return Generator.sampledFrom(ownableList(Rule.Set.Type.official.create(), ColourStreet.class));
  }

  private Generator<Station> stationTypes() {
    return Generator.sampledFrom(ownableList(Rule.Set.Type.official.create(), Station.class));
  }

  private Generator<Utility> utilityTypes() {
    return Generator.sampledFrom(ownableList(Rule.Set.Type.official.create(), Utility.class));
  }

  private <T extends Ownable> List<T> ownableList(Rule.Set rules, Class<T> kind) {
    return rules.streets().filter(kind::isInstance).map(kind::cast).toList();
  }

  private record Case(ColourStreet land, boolean monopoly, boolean claims, int ownerBalance, int tenantBalance) {
  }

  private record StationCase(Station land, int ownedCount, boolean claims, int ownerBalance, int tenantBalance) {
  }

  private record UtilityCase(
      Utility land, int ownedCount, Roll roll, boolean claims, int ownerBalance, int tenantBalance
  ) {
  }

  private record FixedClaim(boolean claims) implements Strategy {
    @Override
    public boolean claims(RentClaim claim) {
      return claims;
    }
  }
}
