package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;

import java.util.Optional;

/** Greedo's decisions with a billionaire's opening balance. */
public final class Billionaire extends Greedo {
  private static final Money OPENING_CAPITAL = new Money(57_700_000);
  private final boolean appliesOpeningCapital;
  private final boolean assetRichOpening;

  public Billionaire() {
    this(Money.ZERO, false, false, true, false);
  }

  public Billionaire(Money reserve, boolean stalemateTrading, boolean legalEntityTrading) {
    this(reserve, stalemateTrading, legalEntityTrading, true, false);
  }

  public Billionaire(Money reserve, boolean stalemateTrading, boolean legalEntityTrading,
                     boolean appliesOpeningCapital) {
    this(reserve, stalemateTrading, legalEntityTrading, appliesOpeningCapital, false);
  }

  public Billionaire(Money reserve, boolean stalemateTrading, boolean legalEntityTrading,
                     boolean appliesOpeningCapital, boolean assetRichOpening) {
    this(reserve, stalemateTrading, legalEntityTrading, appliesOpeningCapital, assetRichOpening, false, false);
  }

  public Billionaire(Money reserve, boolean stalemateTrading, boolean legalEntityTrading,
                     boolean appliesOpeningCapital, boolean assetRichOpening,
                     boolean developmentLoans, boolean fullDrawDevelopmentLoans) {
    super(reserve, stalemateTrading, legalEntityTrading, developmentLoans, fullDrawDevelopmentLoans);
    this.appliesOpeningCapital = appliesOpeningCapital;
    this.assetRichOpening = assetRichOpening;
  }

  @Override
  public Optional<Money> openingCapital() {
    return appliesOpeningCapital && !assetRichOpening ? Optional.of(OPENING_CAPITAL) : Optional.empty();
  }

  @Override
  public boolean assetRichOpening() {
    return assetRichOpening;
  }
}
