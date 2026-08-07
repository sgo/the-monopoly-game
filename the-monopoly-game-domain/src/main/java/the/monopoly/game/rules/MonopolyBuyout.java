package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Resolves two players who jointly own every street in one colour group. */
public final class MonopolyBuyout {
  private MonopolyBuyout() {
  }

  public static Optional<Outcome> resolve(Player first, Player second, Rule.Set rules, Deeds deeds) {
    return resolve(first, second, rules, deeds, false);
  }

  public static Optional<Outcome> resolveAtTurnStart(Player first, Player second, Rule.Set rules, Deeds deeds) {
    return resolve(first, second, rules, deeds, true);
  }

  private static Optional<Outcome> resolve(Player first, Player second, Rule.Set rules, Deeds deeds,
                                           boolean turnStart) {
    List<ColourStreet> group = splitGroup(first, second, rules, deeds);
    if (group.isEmpty()) return Optional.empty();
    return selectWinner(first, second, rules, deeds, group)
        .flatMap(winner -> settle(winner, winner.id().equals(first.id()) ? second : first,
            rules, deeds, group, turnStart));
  }

  /** The colour group split between exactly these two players, or empty if none qualifies. */
  private static List<ColourStreet> splitGroup(Player first, Player second, Rule.Set rules, Deeds deeds) {
    List<ColourStreet> streets = rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).toList();
    List<ColourStreet> group = streets.stream()
        .filter(street -> deeds.ownerOf(street.type()).filter(first.id()::equals).isPresent()
            || deeds.ownerOf(street.type()).filter(second.id()::equals).isPresent())
        .map(street -> groupFor(street, streets))
        .filter(candidate -> candidate.stream().allMatch(it -> deeds.ownerOf(it.type()).isPresent()))
        .max(Comparator.comparingInt(candidate -> candidate.stream()
            .mapToInt(it -> it.price().amount()).max().orElse(0)
            - candidate.stream().mapToInt(it -> it.price().amount()).min().orElse(0)))
        .orElse(List.of());
    boolean splitBetweenBoth = !group.isEmpty()
        && group.stream().map(it -> deeds.ownerOf(it.type()).orElseThrow()).distinct().count() == 2;
    return splitBetweenBoth ? group : List.of();
  }

  private static Optional<Player> selectWinner(
      Player first, Player second, Rule.Set rules, Deeds deeds, List<ColourStreet> group) {
    Player winner = richer(first, second);
    if (winner == null) winner = spareOwner(first, second, rules, deeds, group);
    return Optional.ofNullable(winner);
  }

  private static Optional<Outcome> settle(
      Player winner, Player loser, Rule.Set rules, Deeds deeds, List<ColourStreet> group, boolean turnStart) {
    ColourStreet winnerStreet = streetOwnedBy(group, deeds, winner);
    ColourStreet loserStreet = streetOwnedBy(group, deeds, loser);
    List<ColourStreet> spare = spareStreetsOf(winner, winnerStreet.colourGroup(), rules, deeds);
    Optional<Money> cash = settlementCash(winner, loser,
        price(winner, winnerStreet, spare, loserStreet, turnStart), spare);
    if (cash.isEmpty()) return Optional.empty();
    deeds.transfer(loserStreet, loser, winner, cash.get());
    if (includesSpareSweetener(spare, cash.get(), winner)) {
      deeds.transfer(spare.getFirst(), winner, loser, Money.ZERO);
    }
    return Optional.of(new Outcome(winner, loser, cash.get()));
  }

  /** The sticker price adjusted for what the winner can actually afford, or empty if no settlement is possible. */
  private static Optional<Money> settlementCash(Player winner, Player loser, Money stickerPrice, List<ColourStreet> spare) {
    Money cash = waiveIfUnaffordable(winner, loser, stickerPrice);
    return cash.equals(Money.ZERO) && spare.isEmpty() ? Optional.empty() : Optional.of(cash);
  }

  /**
   * The winner pays the sticker price outright when they can afford double it (a cushion
   * against undershooting on a close-run settlement); a richer winner who can't gets it for
   * nothing instead. A winner tied on cash always pays it as-is, since {@link #selectWinner}
   * only picks a tied winner when they hold a spare street to sweeten the deal with, so this
   * branch never needs to waive anything.
   */
  private static Money waiveIfUnaffordable(Player winner, Player loser, Money stickerPrice) {
    if (stickerPrice.equals(Money.ZERO)) return stickerPrice;
    if (winner.account().balance().amount().covers(new Money(stickerPrice.amount() * 2))) return stickerPrice;
    return winner.account().balance().amount().exceeds(loser.account().balance().amount()) ? Money.ZERO : stickerPrice;
  }

  private static boolean includesSpareSweetener(List<ColourStreet> spare, Money cash, Player winner) {
    return !spare.isEmpty() && (cash.equals(Money.ZERO) || winner.account().balance().amount().amount() < 2000);
  }

  private static ColourStreet streetOwnedBy(List<ColourStreet> group, Deeds deeds, Player player) {
    return group.stream().filter(it -> deeds.ownerOf(it.type()).filter(player.id()::equals).isPresent())
        .findFirst().orElseThrow();
  }

  private static List<ColourStreet> spareStreetsOf(Player winner, Street.Colour excludedGroup, Rule.Set rules, Deeds deeds) {
    return rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .filter(it -> it.colourGroup() != excludedGroup)
        .filter(it -> deeds.ownerOf(it.type()).filter(winner.id()::equals).isPresent()).toList();
  }

  private static List<ColourStreet> groupFor(ColourStreet street, List<ColourStreet> streets) {
    return streets.stream()
        .filter(it -> it.colourGroup() == street.colourGroup()).toList();
  }

  private static Player richer(Player first, Player second) {
    int a = first.account().balance().amount().amount();
    int b = second.account().balance().amount().amount();
    return a == b ? null : a > b ? first : second;
  }

  private static Player spareOwner(Player first, Player second, Rule.Set rules, Deeds deeds,
                                   List<ColourStreet> group) {
    for (Player candidate : List.of(first, second)) {
      if (rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
          .filter(it -> !group.contains(it))
          .anyMatch(it -> deeds.ownerOf(it.type()).filter(candidate.id()::equals).isPresent())) return candidate;
    }
    return null;
  }

  private static Money price(Player winner, ColourStreet winnerStreet, List<ColourStreet> spare,
                             ColourStreet loserStreet, boolean turnStart) {
    if (turnStart) return cashOnlyPrice(winnerStreet, loserStreet);
    if (spare.isEmpty()) return new Money(Math.max(0,
        Math.abs(loserStreet.price().amount() - winnerStreet.price().amount()) - 10));
    if (winner.account().balance().amount().amount() > 1500) {
      ColourStreet mostValuable = spare.stream().max(Comparator.comparingInt(it -> it.rentForOneHotel().amount())).orElseThrow();
      return new Money(mostValuable.rentForOneHotel().amount() * 2);
    }
    return new Money(winnerStreet.vacantRent().amount() * 3);
  }

  private static Money cashOnlyPrice(ColourStreet winnerStreet, ColourStreet loserStreet) {
    return new Money(Math.max(0,
        Math.abs(loserStreet.price().amount() - winnerStreet.price().amount()) - 10));
  }

  public record Outcome(Player winner, Player loser, Money payment) {
  }
}

/* mutate4java-manifest
version=1
moduleHash=49142c50c62074c4f9779ea3aed61b023586902e28fc535beb7a6ac6e04b6740
scope.0.id=Y2xhc3M6TW9ub3BvbHlCdXlvdXQjTW9ub3BvbHlCdXlvdXQ6MTM
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=128
scope.0.semanticHash=9fb1f846f96f66e2e4dfd1eb8c4d0f9fea7b9bb7f4a21645cc9cebb01dc0a982
scope.1.id=Y2xhc3M6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNPdXRjb21lOjEyNg
scope.1.kind=class
scope.1.startLine=126
scope.1.endLine=127
scope.1.semanticHash=144d70dc47fb3d590fe148c934cce2134d44a0193705654c874b999590133429
scope.2.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNsb3NlcjoxMjY
scope.2.kind=field
scope.2.startLine=126
scope.2.endLine=126
scope.2.semanticHash=9a9d28dd1b1ff9f3a22a06427daf939e490cce4068e62054f6db6fb640566588
scope.3.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNwYXltZW50OjEyNg
scope.3.kind=field
scope.3.startLine=126
scope.3.endLine=126
scope.3.semanticHash=aff660ee290181d1288e5334e90f91e17f3c6ca243e12946e16df96c1de72e2e
scope.4.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSN3aW5uZXI6MTI2
scope.4.kind=field
scope.4.startLine=126
scope.4.endLine=126
scope.4.semanticHash=d76827961eca0eb4c351ae8de7a2ab43cef35e44ddbeacc98fe0869ab9a2a5cf
scope.5.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2N0b3IoMCk6MTQ
scope.5.kind=method
scope.5.startLine=14
scope.5.endLine=15
scope.5.semanticHash=63ac2206a9ffc42cdfad955bd5f627aa364526c58316028457dafce68df5c9b6
scope.6.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2dyb3VwRm9yKDIpOjk0
scope.6.kind=method
scope.6.startLine=94
scope.6.endLine=97
scope.6.semanticHash=fb74a4f477abc6f191c77c773f2778e2c5d04c45105ee453e64c67c08d105f66
scope.7.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2luY2x1ZGVzU3BhcmVTd2VldGVuZXIoMyk6Nzk
scope.7.kind=method
scope.7.startLine=79
scope.7.endLine=81
scope.7.semanticHash=943e2bdf3f9f3571c71fddab3fdf6888b55ce66e3c242f066c217bb02c3f6769
scope.8.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3ByaWNlKDQpOjExNQ
scope.8.kind=method
scope.8.startLine=115
scope.8.endLine=124
scope.8.semanticHash=109bc28476dd604fed00f9f0c4a9f46ac2c7e29b5bfe849fc1a73de28bd998a0
scope.9.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3Jlc29sdmUoNCk6MTc
scope.9.kind=method
scope.9.startLine=17
scope.9.endLine=22
scope.9.semanticHash=282267c183c2d23280ebd35b807c97b45a45857bba4c20a51daedc0d99002670
scope.10.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3JpY2hlcigyKTo5OQ
scope.10.kind=method
scope.10.startLine=99
scope.10.endLine=103
scope.10.semanticHash=91c14c712483c9f9d20e68dbd44d5f68d2a4c191dba337d66bd001b77187f502
scope.11.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NlbGVjdFdpbm5lcig1KTozOQ
scope.11.kind=method
scope.11.startLine=39
scope.11.endLine=44
scope.11.semanticHash=bd29d26e6cf8f8cf6441a006cbba69200d3dce5de490e4f5e3cc1ebda776f76a
scope.12.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NldHRsZSg1KTo0Ng
scope.12.kind=method
scope.12.startLine=46
scope.12.endLine=58
scope.12.semanticHash=e4cb9850538a21294fba547960f34e908183af444c1b16fbb43fa2bd4cf73a18
scope.13.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NldHRsZW1lbnRDYXNoKDQpOjYx
scope.13.kind=method
scope.13.startLine=61
scope.13.endLine=64
scope.13.semanticHash=aff5bfe5b5013ec92e292e4156d0bd5cefd5fb034f1ac3a1c8c333567d56ac01
scope.14.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwYXJlT3duZXIoNSk6MTA1
scope.14.kind=method
scope.14.startLine=105
scope.14.endLine=113
scope.14.semanticHash=53b0d3f443992019c568d07064731545c9be83a16116ecc21237ff927ac29d1a
scope.15.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwYXJlU3RyZWV0c09mKDQpOjg4
scope.15.kind=method
scope.15.startLine=88
scope.15.endLine=92
scope.15.semanticHash=2591ec9f4c7c6c6fd56b5d24979f3942f6548437b7554412042d9446f803f40b
scope.16.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwbGl0R3JvdXAoNCk6MjU
scope.16.kind=method
scope.16.startLine=25
scope.16.endLine=37
scope.16.semanticHash=26c018a290316e73adaef6ed3ffa86b3ca8dc0fe6a2191bbb5077f4b4f15a76c
scope.17.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3N0cmVldE93bmVkQnkoMyk6ODM
scope.17.kind=method
scope.17.startLine=83
scope.17.endLine=86
scope.17.semanticHash=83240e715406f9201397d346f12a43949b1fbb4fa02fd7fbf95fd6a8d67b7988
scope.18.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3dhaXZlSWZVbmFmZm9yZGFibGUoMyk6NzM
scope.18.kind=method
scope.18.startLine=73
scope.18.endLine=77
scope.18.semanticHash=95c436cfdeb9e0b2d98c833ea92e2cf55fd74f3d63c92e445d22e70642d55292
scope.19.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0Lk91dGNvbWUjY3RvcigzKToxMjY
scope.19.kind=method
scope.19.startLine=1
scope.19.endLine=128
scope.19.semanticHash=a4a9167aa392767d90de581aadde75bcdff38e6e58e53d054cb18928133e9e19
*/
