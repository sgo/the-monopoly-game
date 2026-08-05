package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Utility;

import java.util.List;

/** Detects the point at which no remaining player can realistically be ruined. */
public final class Stalemate {
  private static final int MAXIMUM_DICE_TOTAL = 7;

  private Stalemate() {
  }

  public static Money threshold(Rule.Set rules) {
    return rules.streets().map(Stalemate::maximumRent).reduce(Money.ZERO, Money::plus);
  }

  public static boolean reached(Rule.Set rules, List<Player> players, Deeds deeds) {
    Money threshold = threshold(rules);
    return players.stream()
        .filter(player -> !deeds.isBankrupt(player))
        .allMatch(player -> player.account().balance().amount().covers(threshold));
  }

  private static Money maximumRent(the.monopoly.game.components.streets.Street street) {
    return switch (street) {
      case ColourStreet colourStreet -> colourStreet.rentForOneHotel();
      case Station station -> station.rentForOwning(4);
      case Utility utility -> new Money(MAXIMUM_DICE_TOTAL * utility.rentDiceMultiplierForOwning(2));
      default -> Money.ZERO;
    };
  }
}
