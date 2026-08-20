package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;

/**
 * The core war-profits-tax computation: which tax rate a player's current
 * ownership share attracts, and how much they owe on the rent they collected
 * that year.
 *
 * <p>Ownership share is the player's land's *current* rent value as a fraction
 * of the whole board's value at full development (the same figure
 * {@link Stalemate#threshold} uses). The rate is fixed by bands, each band's
 * lower bound inclusive:</p>
 *
 * <table>
 *   <tr><th>Ownership share</th><th>Rate</th></tr>
 *   <tr><td>below 25%</td><td>0%</td></tr>
 *   <tr><td>25% – 40%</td><td>100%</td></tr>
 *   <tr><td>40% – 60%</td><td>150%</td></tr>
 *   <tr><td>60% – 80%</td><td>200%</td></tr>
 *   <tr><td>80% – 100%</td><td>300%</td></tr>
 *   <tr><td>100%</td><td>400%</td></tr>
 * </table>
 */
public final class WarProfitsTax {
  private WarProfitsTax() {
  }

  /**
   * The board's rent value at full development, which everything else is
   * measured against. Kept identical to the stalemate threshold by reusing
   * the same figure.
   */
  public static Money boardValue(Rule.Set rules) {
    return Stalemate.threshold(rules);
  }

  /**
   * The tax rate, in hundredths of a percent's worth of basis points phrased
   * as a plain percentage (0, 100, 150, 200, 300, or 400), for a player whose
   * land is currently worth {@code landValue} in rent.
   */
  public static int rate(Money boardValue, Money landValue) {
    long board = boardValue.cents();
    long land = landValue.cents();
    if (land >= board) return 400;
    if (land * 100 < board * 25) return 0;
    if (land * 100 < board * 40) return 100;
    if (land * 100 < board * 60) return 150;
    if (land * 100 < board * 80) return 200;
    return 300;
  }

  /**
   * The tax owed on {@code collected} rent at a player's current ownership
   * share. Below 25% ownership the result is zero; the bands above it climb
   * past 100%, so a high share owes more than the rent was worth.
   */
  public static Money tax(Money boardValue, Money landValue, Money collected) {
    return collected.percentage(rate(boardValue, landValue));
  }
}