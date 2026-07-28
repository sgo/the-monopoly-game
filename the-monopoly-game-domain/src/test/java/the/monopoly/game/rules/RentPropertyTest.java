package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

/**
 * Rent.resolve only ever moves money between the tenant's and owner's
 * accounts, by an amount fixed by the board and by whether the owner holds
 * the whole colour group. RentTest samples two streets with the owner always
 * claiming; this sweeps every colour street, monopoly and non-monopoly
 * ownership, an owner who claims and one who does not, and a wide range of
 * starting balances, to pin down conservation of total money and the exact
 * amount owed together rather than at a few hand-picked points.
 */
@Tag("property-test")
class RentPropertyTest {
  private static final Generator<Integer> BALANCES = Generator.integers(0, 5_000);

  @Test
  void resolvingRentConservesTotalMoneyAndMovesExactlyTheAmountOwed() {
    PropertyChecker.forAll(cases(), c -> {
      // A fresh rule set and bank per iteration: Bank.Simple accumulates
      // accounts in a Set keyed by owner name and balance, so reusing one
      // bank across iterations can hand back a stale account left over from
      // an earlier case with the same player name.
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player owner = playerWith(rules, "owner");
      Player tenant = playerWith(rules, "tenant");
      if (c.monopoly()) sellWholeGroup(rules, deeds, owner, c.land().colourGroup());
      else deeds.sell(c.land(), owner, Money.ZERO);
      owner.account().deposit(new Money(c.ownerBalance()));
      tenant.account().deposit(new Money(c.tenantBalance()));

      Money totalBefore = totalOf(owner, tenant);
      rent(rules, deeds, List.of(owner, tenant), c.claims()).resolve(tenant, c.land());
      Money totalAfter = totalOf(owner, tenant);

      Money owed = c.claims() ? owedRent(c) : Money.ZERO;
      return totalBefore.equals(totalAfter)
          && owner.account().balance().amount().equals(new Money(c.ownerBalance()).plus(owed))
          && tenant.account().balance().amount().equals(new Money(c.tenantBalance()).minus(owed));
    });
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

  private Generator<ColourStreet> colourStreetTypes() {
    return Generator.sampledFrom(
        Rule.Set.Type.official.create().streets()
            .filter(ColourStreet.class::isInstance)
            .map(ColourStreet.class::cast)
            .toList()
    );
  }

  private record Case(ColourStreet land, boolean monopoly, boolean claims, int ownerBalance, int tenantBalance) {
  }

  private record FixedClaim(boolean claims) implements Strategy {
    @Override
    public boolean claims(Rent.Claim claim) {
      return claims;
    }
  }
}
