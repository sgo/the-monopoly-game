package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.Set;

/**
 * The space every player starts on and is paid for passing.
 * <p>
 * The activated rules are held live rather than copied, so a rule switched on
 * after the board was laid out still applies.
 */
public record StartSpace(Street.Type type, Set<Rule> activatedRules) implements Street {
  private static final Money SALARY = new Money(200);

  /** The only optional rule that applies here pays the salary twice over. */
  private static final Rule.Processor<Boolean> DOUBLES_SALARY = rule -> true;

  @Override
  public Street.Kind kind() {
    return Street.Kind.start;
  }

  /** What the bank pays a player for passing or landing on Start. */
  public Money salary() {
    return doublesSalary() ? SALARY.plus(SALARY) : SALARY;
  }

  private boolean doublesSalary() {
    return activatedRules.stream().anyMatch(rule -> rule.process(DOUBLES_SALARY));
  }

  static Street.Factory factory() {
    return StartSpace::new;
  }
}
