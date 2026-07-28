package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;

/**
 * Agrees to whatever it has the means to pay for: it buys land it can afford,
 * and at auction it bids the most it can afford rather than lose the land.
 */
public final class AgreeIfAffordable implements Strategy {
  @Override
  public boolean accepts(Offer offer) {
    return offer.isAffordable();
  }

  @Override
  public Money bidFor(Offer offer) {
    return offer.available();
  }
}
