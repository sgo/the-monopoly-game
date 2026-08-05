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

/* mutate4java-manifest
version=1
moduleHash=d3ee5b4c636cb686b494256d057cd7d6cb11f4d45a6f5543e0e01573a972a6e3
scope.0.id=Y2xhc3M6U3RhbGVtYXRlI1N0YWxlbWF0ZToxMg
scope.0.kind=class
scope.0.startLine=12
scope.0.endLine=37
scope.0.semanticHash=02c33d469c1c09646c47bc7472aa7a2fa68377f30f03cea2e2ee39e4e668efc4
scope.1.id=ZmllbGQ6U3RhbGVtYXRlI01BWElNVU1fRElDRV9UT1RBTDoxMw
scope.1.kind=field
scope.1.startLine=13
scope.1.endLine=13
scope.1.semanticHash=ee703b323ab0f17f9e16b09b1e571494e07d8b9dd031a4e5200eedd4a1c26334
scope.2.id=bWV0aG9kOlN0YWxlbWF0ZSNjdG9yKDApOjE1
scope.2.kind=method
scope.2.startLine=15
scope.2.endLine=16
scope.2.semanticHash=a2e77e94c130fd7f97ba387c39b81a604b9f79b11fb24b41388524fee2a0ef50
scope.3.id=bWV0aG9kOlN0YWxlbWF0ZSNtYXhpbXVtUmVudCgxKToyOQ
scope.3.kind=method
scope.3.startLine=29
scope.3.endLine=36
scope.3.semanticHash=839358ddf4ca1e64b3ba7ca699c8888e94af25b4ed9e4a6dbaee2e1defdcdfbf
scope.4.id=bWV0aG9kOlN0YWxlbWF0ZSNyZWFjaGVkKDMpOjIy
scope.4.kind=method
scope.4.startLine=22
scope.4.endLine=27
scope.4.semanticHash=eec328d0949be1b40be256e195fd8e84f7d3f6702682d25a200e6a4364083ae0
scope.5.id=bWV0aG9kOlN0YWxlbWF0ZSN0aHJlc2hvbGQoMSk6MTg
scope.5.kind=method
scope.5.startLine=18
scope.5.endLine=20
scope.5.semanticHash=671c4924c87a5a54b5a4289fd504e8433caabc554cbc0f8edb5dd56d0de6cbae
*/
