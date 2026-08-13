package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;

import java.util.Optional;

/** Greedo's decisions with a billionaire's opening balance. */
public final class Billionaire extends Greedo {
  private static final Money OPENING_CAPITAL = new Money(57_700_000);
  private final boolean appliesOpeningCapital;

  public Billionaire() {
    this(Money.ZERO, false, false, true);
  }

  public Billionaire(Money reserve, boolean stalemateTrading, boolean legalEntityTrading) {
    this(reserve, stalemateTrading, legalEntityTrading, true);
  }

  public Billionaire(Money reserve, boolean stalemateTrading, boolean legalEntityTrading,
                     boolean appliesOpeningCapital) {
    super(reserve, stalemateTrading, legalEntityTrading);
    this.appliesOpeningCapital = appliesOpeningCapital;
  }

  @Override
  public Optional<Money> openingCapital() {
    return appliesOpeningCapital ? Optional.of(OPENING_CAPITAL) : Optional.empty();
  }
}
