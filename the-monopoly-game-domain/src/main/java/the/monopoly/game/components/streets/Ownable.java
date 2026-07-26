package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

/**
 * A space a player can buy from the bank and later mortgage. What its owner
 * charges for a visit differs per kind, so rent lives on the subtypes.
 */
public sealed interface Ownable extends Street permits ColourStreet, Station, Utility {
  Money price();

  Money landMortgageValue();
}
