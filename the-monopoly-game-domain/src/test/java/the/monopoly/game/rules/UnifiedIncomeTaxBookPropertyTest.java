package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

/**
 * The example-based {@link UnifiedIncomeTaxBookTest} pins two hand-picked
 * salary/rent combinations. These properties sweep a continuous range of net
 * salaries and accumulated rent to pin the invariants those samples only
 * spot-check: the tax is always 43% of the combined gross-salary-plus-rent
 * base (mirroring {@link MegacorpSalaryTaxPropertyTest}'s own gross-basis
 * property), {@link UnifiedIncomeTaxBook#assess} always resets the rent
 * accumulator to zero regardless of what was collected, and repeated
 * assessments conserve money exactly into the government account.
 */
@Tag("property-test")
class UnifiedIncomeTaxBookPropertyTest {
  private static final int RATE = 43;
  private static final Generator<Integer> NET_SALARY_CENTS = Generator.integers(0, 10_000_000);
  private static final Generator<Integer> RENT_CENTS = Generator.integers(0, 10_000_000);

  @Test
  void assessIsFortyThreePercentOfCombinedGrossBaseWithinHalfCentRounding() {
    PropertyChecker.forAll(Generator.zipWith(NET_SALARY_CENTS, RENT_CENTS, Pair::new), pair -> {
      Bank.Simple bank = new Bank.Simple();
      Player dog = player(bank, "dog");
      UnifiedIncomeTaxBook book = new UnifiedIncomeTaxBook(bank);
      Money net = Money.fromCents(pair.a());
      Money rent = Money.fromCents(pair.b());

      book.accumulate(dog, rent);
      Money tax = book.assess(dog, net);

      double grossSalaryCents = pair.a() / 0.57;
      double exact = (grossSalaryCents + pair.b()) * (RATE / 100.0);
      return Math.abs(tax.cents() - exact) <= 0.5;
    });
  }

  @Test
  void assessAlwaysResetsCollectedRentToZero() {
    PropertyChecker.forAll(Generator.zipWith(NET_SALARY_CENTS, RENT_CENTS, Pair::new), pair -> {
      Bank.Simple bank = new Bank.Simple();
      Player dog = player(bank, "dog");
      UnifiedIncomeTaxBook book = new UnifiedIncomeTaxBook(bank);

      book.accumulate(dog, Money.fromCents(pair.b()));
      book.assess(dog, Money.fromCents(pair.a()));

      return book.collected(dog).equals(Money.ZERO);
    });
  }

  @Test
  void repeatedAssessmentsConserveMoneyExactlyIntoTheGovernmentAccount() {
    PropertyChecker.forAll(Generator.zipWith(NET_SALARY_CENTS, RENT_CENTS, Pair::new), pair -> {
      Bank.Simple bank = new Bank.Simple();
      Player dog = player(bank, "dog");
      UnifiedIncomeTaxBook book = new UnifiedIncomeTaxBook(bank);

      book.accumulate(dog, Money.fromCents(pair.b()));
      Money firstTax = book.assess(dog, Money.fromCents(pair.a()));
      Money secondTax = book.assess(dog, Money.fromCents(pair.a()));

      return book.governmentBalance().equals(firstTax.plus(secondTax));
    });
  }

  private Player player(Bank bank, String name) {
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }

  private record Pair(int a, int b) {
  }
}
