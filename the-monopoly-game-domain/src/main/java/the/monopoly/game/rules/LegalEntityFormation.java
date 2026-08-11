package the.monopoly.game.rules;

import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Eligibility checks for consolidating a colour group's split ownership into a {@link LegalEntity}. */
final class LegalEntityFormation {
  private LegalEntityFormation() {
  }

  static Optional<List<ColourStreet>> eligibleStreets(List<Player> shareholders, Street.Colour colour,
                                                       Rule.Set rules, Deeds deeds,
                                                       Predicate<ColourStreet> highestPriority) {
    if (!hasThreeDistinctShareholders(shareholders)) return Optional.empty();
    if (!boardFullyOwned(rules, deeds)) return Optional.empty();
    List<ColourStreet> streets = LegalEntity.streetsOf(colour, rules);
    if (colourGroupIneligible(streets, highestPriority)) return Optional.empty();
    if (!splitAcrossThreeDistinctOwners(streets, deeds)) return Optional.empty();
    if (!everyShareholderOwnsAStreet(shareholders, streets, deeds)) return Optional.empty();
    return Optional.of(streets);
  }

  private static boolean hasThreeDistinctShareholders(List<Player> shareholders) {
    return shareholders.size() == 3 && shareholders.stream().distinct().count() == 3;
  }

  private static boolean boardFullyOwned(Rule.Set rules, Deeds deeds) {
    return rules.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .noneMatch(it -> deeds.isUnowned(it.type()));
  }

  /** Empty, or led by a street the Greedo priority always trades toward rather than split. */
  private static boolean colourGroupIneligible(List<ColourStreet> streets,
                                               Predicate<ColourStreet> highestPriority) {
    return streets.isEmpty() || streets.stream().anyMatch(highestPriority);
  }

  private static boolean splitAcrossThreeDistinctOwners(List<ColourStreet> streets, Deeds deeds) {
    return streets.stream().map(it -> deeds.ownerOf(it.type()).orElse(null)).distinct().count() == 3;
  }

  private static boolean everyShareholderOwnsAStreet(List<Player> shareholders, List<ColourStreet> streets, Deeds deeds) {
    return shareholders.stream().allMatch(player -> streets.stream()
        .anyMatch(street -> deeds.ownerOf(street.type()).filter(player.id()::equals).isPresent()));
  }
}

/* mutate4java-manifest
version=1
moduleHash=aeb9d4f022b7b13d103264cc19dcc8cba8f8989454868905302d45c9055f0195
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHlGb3JtYXRpb24jTGVnYWxFbnRpdHlGb3JtYXRpb246MTM
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=52
scope.0.semanticHash=59b15e6800d4ed330a7fe34f8c25d2026a6a78a2a3150c9d8bacada8ebf08a71
scope.1.id=bWV0aG9kOkxlZ2FsRW50aXR5Rm9ybWF0aW9uI2JvYXJkRnVsbHlPd25lZCgyKTozMw
scope.1.kind=method
scope.1.startLine=33
scope.1.endLine=36
scope.1.semanticHash=f1356c828dc58032afd29882bdbf3bcedd40c0b501507da684fe18f9a2db8250
scope.2.id=bWV0aG9kOkxlZ2FsRW50aXR5Rm9ybWF0aW9uI2NvbG91ckdyb3VwSW5lbGlnaWJsZSgyKTozOQ
scope.2.kind=method
scope.2.startLine=39
scope.2.endLine=42
scope.2.semanticHash=43daeaa19e539a65cb621c6584fc7509343a73cdf3f0767f07219b7c8cd10d79
scope.3.id=bWV0aG9kOkxlZ2FsRW50aXR5Rm9ybWF0aW9uI2N0b3IoMCk6MTQ
scope.3.kind=method
scope.3.startLine=14
scope.3.endLine=15
scope.3.semanticHash=91117a78e3531a83bf4769255d1297cf36f8c68989f4f5d2f80cb4092bdb889c
scope.4.id=bWV0aG9kOkxlZ2FsRW50aXR5Rm9ybWF0aW9uI2VsaWdpYmxlU3RyZWV0cyg1KToxNw
scope.4.kind=method
scope.4.startLine=17
scope.4.endLine=27
scope.4.semanticHash=99fe47a2adfdf2feb154dec44a8ea5fe3ed8c0eb9d16ffa265a4b8f761fa9b34
scope.5.id=bWV0aG9kOkxlZ2FsRW50aXR5Rm9ybWF0aW9uI2V2ZXJ5U2hhcmVob2xkZXJPd25zQVN0cmVldCgzKTo0OA
scope.5.kind=method
scope.5.startLine=48
scope.5.endLine=51
scope.5.semanticHash=ccafcb1c2b7075297ef35f00d1b8d817de2f5ba594606694c2e32468ba2e9abd
scope.6.id=bWV0aG9kOkxlZ2FsRW50aXR5Rm9ybWF0aW9uI2hhc1RocmVlRGlzdGluY3RTaGFyZWhvbGRlcnMoMSk6Mjk
scope.6.kind=method
scope.6.startLine=29
scope.6.endLine=31
scope.6.semanticHash=a9e99322b63c32a1e02399009013cd9d9a74c94ce67d42a5b62cef7639db9b8d
scope.7.id=bWV0aG9kOkxlZ2FsRW50aXR5Rm9ybWF0aW9uI3NwbGl0QWNyb3NzVGhyZWVEaXN0aW5jdE93bmVycygyKTo0NA
scope.7.kind=method
scope.7.startLine=44
scope.7.endLine=46
scope.7.semanticHash=e1dc43db1d652d4ca15adb46375d2e18b50cd99ada2d53edb65d468a6ee090b2
*/
