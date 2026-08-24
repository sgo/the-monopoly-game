package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

/**
 * The example-based {@link MegacorpSalaryTaxTest} pins a handful of net
 * salaries by hand. These properties sweep a continuous range of net salaries
 * to pin the invariants those samples only spot-check: the tax is always
 * 43% of the resulting gross (not the net) figure, it never decreases as the
 * net salary grows, and {@link MegacorpSalaryTax#collect} conserves money
 * exactly — the player receives the full net salary and the government
 * receives exactly the computed tax, no more and no less.
 */
@Tag("property-test")
class MegacorpSalaryTaxPropertyTest {
  private static final int RATE = 43;
  private static final Generator<Integer> NET_SALARY_CENTS = Generator.integers(0, 10_000_000);

  @Test
  void taxIsFortyThreePercentOfGrossWithinHalfCentRounding() {
    PropertyChecker.forAll(NET_SALARY_CENTS, netCents -> {
      Money net = Money.fromCents(netCents);
      Money tax = new MegacorpSalaryTax(new Bank.Simple()).payTax(net);
      long gross = netCents + tax.cents();
      double exact = gross * (RATE / 100.0);
      return Math.abs(tax.cents() - exact) <= 0.5;
    });
  }

  @Test
  void taxNonDecreasingAsNetSalaryGrows() {
    PropertyChecker.forAll(Generator.zipWith(NET_SALARY_CENTS, NET_SALARY_CENTS, Pair::new), pair -> {
      Money smaller = Money.fromCents(Math.min(pair.a(), pair.b()));
      Money larger = Money.fromCents(Math.max(pair.a(), pair.b()));
      long taxOnSmaller = new MegacorpSalaryTax(new Bank.Simple()).payTax(smaller).cents();
      long taxOnLarger = new MegacorpSalaryTax(new Bank.Simple()).payTax(larger).cents();
      return taxOnSmaller <= taxOnLarger;
    });
  }

  @Test
  void collectConservesMoneyExactlyBetweenPlayerAndGovernment() {
    PropertyChecker.forAll(NET_SALARY_CENTS, netCents -> {
      Bank.Simple bank = new Bank.Simple();
      Player.ID id = new Player.ID("dog");
      bank.createAccountFor(id);
      Player player = new Player(id, bank.accountOf(id));
      Money net = Money.fromCents(netCents);
      MegacorpSalaryTax tax = new MegacorpSalaryTax(bank);

      Money collectedTax = tax.collect(player, net);

      return player.account().balance().amount().equals(net)
          && tax.governmentBalance().equals(collectedTax);
    });
  }

  private record Pair(int a, int b) {
  }
}
