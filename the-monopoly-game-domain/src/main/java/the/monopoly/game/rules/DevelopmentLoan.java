package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;

/** A development loan amortized in equal principal instalments over twenty years. */
public final class DevelopmentLoan {
  public static final int TERM_YEARS = 20;

  private final Money originalPrincipal;
  private Money outstanding;
  private int yearsServiced;

  public DevelopmentLoan(Money principal) {
    this(principal, 0);
  }

  public DevelopmentLoan(Money principal, int yearsServiced) {
    if (principal.cents() < 0 || yearsServiced < 0 || yearsServiced > TERM_YEARS)
      throw new IllegalArgumentException("Development loan principal and age must be valid.");
    this.originalPrincipal = principal;
    this.outstanding = principal;
    this.yearsServiced = 0;
    for (int year = 0; year < yearsServiced; year++) serviceNextYear();
  }

  public Money originalPrincipal() {
    return originalPrincipal;
  }

  public Money outstanding() {
    return outstanding;
  }

  public int yearsServiced() {
    return yearsServiced;
  }

  public boolean isRepaid() {
    return outstanding.equals(Money.ZERO);
  }

  public void serviceToZero() {
    outstanding = Money.ZERO;
  }

  public Payment serviceNextYear() {
    if (isRepaid()) return new Payment(Money.ZERO, Money.ZERO, Money.ZERO, Money.ZERO);
    Money interest = outstanding.percentage(5);
    Money scheduledPrincipal = originalPrincipal.percentage(5);
    if (scheduledPrincipal.cents() < 100) scheduledPrincipal = Money.fromCents(100);
    Money principal = outstanding.covers(scheduledPrincipal) ? scheduledPrincipal : outstanding;
    outstanding = outstanding.minus(principal);
    yearsServiced++;
    Money bondInterest = outstanding.plus(principal).percentage(3);
    return new Payment(interest, principal, bondInterest, interest.minus(bondInterest));
  }

  public record Payment(Money interest, Money principal, Money bondInterest, Money bankSpread) {
    public Money borrowerTotal() {
      return interest.plus(principal);
    }
  }
}
