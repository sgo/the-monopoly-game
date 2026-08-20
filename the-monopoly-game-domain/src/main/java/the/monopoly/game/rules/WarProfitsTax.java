package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Utility;

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

  /**
   * The current rent value of a player's land, the numerator of their
   * ownership share. Mirrors {@link Rent}'s valuation: vacant rent for an
   * undeveloped street (doubled when the player holds the whole colour
   * group), the matching house-tier rent as houses go up, hotel rent once
   * complete, and the owned-count rent for stations. Mortgaged land and
   * entity-owned land contribute nothing — only the player's own, unencumbered
   * holdings count.
   */
  public static Money landValue(Rule.Set rules, Deeds deeds, Player player) {
    Money total = Money.ZERO;
    for (the.monopoly.game.components.streets.Street street : rules.streets().toList()) {
      if (!(street instanceof Ownable land)) continue;
      if (deeds.ownerOf(land.type()).filter(player.id()::equals).isEmpty()) continue;
      if (deeds.isMortgaged(land)) continue;
      total = total.plus(switch (land) {
        case ColourStreet colour -> colourStreetValue(rules, deeds, player, colour);
        case Station ignored -> stationValue(rules, deeds, player);
        case Utility ignored -> Money.ZERO;
        default -> Money.ZERO;
      });
    }
    return total;
  }

  private static Money colourStreetValue(Rule.Set rules, Deeds deeds, Player player, ColourStreet street) {
    if (deeds.hasHotelOn(street)) return street.rentForOneHotel();
    int houses = deeds.housesBuiltOn(street);
    if (houses > 0) return street.rentForHouses(houses);
    boolean monopoly = rules.streets()
        .filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast)
        .filter(it -> it.colourGroup() == street.colourGroup())
        .allMatch(it -> deeds.ownerOf(it.type()).filter(player.id()::equals).isPresent() && !deeds.isMortgaged(it));
    return monopoly ? street.vacantRent().plus(street.vacantRent()) : street.vacantRent();
  }

  private static Money stationValue(Rule.Set rules, Deeds deeds, Player player) {
    int owned = (int) rules.streets().filter(Station.class::isInstance)
        .filter(it -> deeds.ownerOf(it.type()).filter(player.id()::equals).isPresent()).count();
    return rules.streets().filter(Station.class::isInstance).findFirst()
        .map(Station.class::cast)
        .map(station -> station.rentForOwning(owned))
        .orElse(Money.ZERO);
  }
}

/* mutate4java-manifest
version=1
moduleHash=da0253cdbf4ab6888bc7ff7d71f4ab3109b9d141b2e590237e2ccd99d0584879
scope.0.id=Y2xhc3M6V2FyUHJvZml0c1RheCNXYXJQcm9maXRzVGF4OjMw
scope.0.kind=class
scope.0.startLine=30
scope.0.endLine=113
scope.0.semanticHash=2dd61e1af515dea72d51727389fdda551335969d027af060ed6d7d2526d49215
scope.1.id=bWV0aG9kOldhclByb2ZpdHNUYXgjYm9hcmRWYWx1ZSgxKTozOQ
scope.1.kind=method
scope.1.startLine=39
scope.1.endLine=41
scope.1.semanticHash=e82a4dbfc4b3093a67ee8a96258438c8937742d6913ae04587e0cb1c22aa921b
scope.2.id=bWV0aG9kOldhclByb2ZpdHNUYXgjY29sb3VyU3RyZWV0VmFsdWUoNCk6OTM
scope.2.kind=method
scope.2.startLine=93
scope.2.endLine=103
scope.2.semanticHash=dad22e8e322515db447c1c1d2d1ffaefe2db181bb419ae9deb1f177a5c00d3ed
scope.3.id=bWV0aG9kOldhclByb2ZpdHNUYXgjY3RvcigwKTozMQ
scope.3.kind=method
scope.3.startLine=31
scope.3.endLine=32
scope.3.semanticHash=26dc92d8751c09470b948b3ae5b3e9cc168cbe544c4d80d7097a2797589aacee
scope.4.id=bWV0aG9kOldhclByb2ZpdHNUYXgjbGFuZFZhbHVlKDMpOjc3
scope.4.kind=method
scope.4.startLine=77
scope.4.endLine=91
scope.4.semanticHash=ee6fa7d413f359c1c34c14c219955068179fab5418aa887e5654b031179dccee
scope.5.id=bWV0aG9kOldhclByb2ZpdHNUYXgjcmF0ZSgyKTo0OA
scope.5.kind=method
scope.5.startLine=48
scope.5.endLine=57
scope.5.semanticHash=0fe8a417ee108116c0220088e671d1b588f09ac44f0091c57633ac70652a6639
scope.6.id=bWV0aG9kOldhclByb2ZpdHNUYXgjc3RhdGlvblZhbHVlKDMpOjEwNQ
scope.6.kind=method
scope.6.startLine=105
scope.6.endLine=112
scope.6.semanticHash=98faab78b8f2a4b5ffe54c2158daa51c7ff87039c4510acbc62bc6ef41dd0249
scope.7.id=bWV0aG9kOldhclByb2ZpdHNUYXgjdGF4KDMpOjY0
scope.7.kind=method
scope.7.startLine=64
scope.7.endLine=66
scope.7.semanticHash=afd7c7636243aa59ca7e2c9b592c4a22a5d98f0a710a3a39ce8c15795e101a7c
*/
