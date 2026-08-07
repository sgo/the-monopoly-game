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
    List<ColourStreet> group = splitGroup(first, second, rules, deeds);
    if (group.isEmpty()) return Optional.empty();
    return selectWinner(first, second, rules, deeds, group)
        .flatMap(winner -> settle(winner, winner.id().equals(first.id()) ? second : first,
            rules, deeds, group));
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
      Player winner, Player loser, Rule.Set rules, Deeds deeds, List<ColourStreet> group) {
    ColourStreet winnerStreet = streetOwnedBy(group, deeds, winner);
    ColourStreet loserStreet = streetOwnedBy(group, deeds, loser);
    List<ColourStreet> spare = spareStreetsOf(winner, loser, winnerStreet.colourGroup(), rules, deeds);
    Optional<Money> cash = settlementCash(winner, loser, price(winner, winnerStreet, spare, loserStreet), spare);
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

  private static List<ColourStreet> spareStreetsOf(Player winner, Player loser,
                                                   Street.Colour excludedGroup, Rule.Set rules, Deeds deeds) {
    List<ColourStreet> streets = rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).toList();
    return streets.stream()
        .filter(it -> it.colourGroup() != excludedGroup)
        .filter(it -> deeds.ownerOf(it.type()).filter(winner.id()::equals).isPresent())
        .filter(it -> !isSplitGroup(groupFor(it, streets), winner, loser, deeds)).toList();
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
          .filter(it -> deeds.ownerOf(it.type()).filter(candidate.id()::equals).isPresent())
          .filter(it -> !isSplitGroup(groupFor(it, rules.streets().filter(ColourStreet.class::isInstance)
              .map(ColourStreet.class::cast).toList()), candidate,
              candidate.id().equals(first.id()) ? second : first, deeds))
          .findAny().isPresent()) return candidate;
    }
    return null;
  }

  private static Money price(Player winner, ColourStreet winnerStreet, List<ColourStreet> spare,
                             ColourStreet loserStreet) {
    if (spare.isEmpty()) return new Money(Math.max(0,
        Math.abs(loserStreet.price().amount() - winnerStreet.price().amount()) - 10));
    if (winner.account().balance().amount().amount() > 1500) {
      ColourStreet mostValuable = spare.stream().max(Comparator.comparingInt(it -> it.rentForOneHotel().amount())).orElseThrow();
      return new Money(mostValuable.rentForOneHotel().amount() * 2);
    }
    return new Money(winnerStreet.vacantRent().amount() * 3);
  }

  private static boolean isSplitGroup(List<ColourStreet> group, Player first, Player second, Deeds deeds) {
    return group.stream().allMatch(it -> deeds.ownerOf(it.type()).isPresent())
        && group.stream().map(it -> deeds.ownerOf(it.type()).orElseThrow()).distinct().count() == 2
        && group.stream().anyMatch(it -> deeds.ownerOf(it.type()).filter(first.id()::equals).isPresent())
        && group.stream().anyMatch(it -> deeds.ownerOf(it.type()).filter(second.id()::equals).isPresent());
  }

  public record Outcome(Player winner, Player loser, Money payment) {
  }
}

/* mutate4java-manifest
version=1
moduleHash=4dff41d6d5d98f442c8ca2055ecc13b4bf32200ef19deef8d6a1a0c054b0ee02
scope.0.id=Y2xhc3M6TW9ub3BvbHlCdXlvdXQjTW9ub3BvbHlCdXlvdXQ6MTM
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=148
scope.0.semanticHash=9f4c2b7531d1fe0330d8ef45e11e4445beb99318c088e779d5d94a3f15106417
scope.1.id=Y2xhc3M6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNPdXRjb21lOjE0Ng
scope.1.kind=class
scope.1.startLine=146
scope.1.endLine=147
scope.1.semanticHash=144d70dc47fb3d590fe148c934cce2134d44a0193705654c874b999590133429
scope.2.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNsb3NlcjoxNDY
scope.2.kind=field
scope.2.startLine=146
scope.2.endLine=146
scope.2.semanticHash=9a9d28dd1b1ff9f3a22a06427daf939e490cce4068e62054f6db6fb640566588
scope.3.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNwYXltZW50OjE0Ng
scope.3.kind=field
scope.3.startLine=146
scope.3.endLine=146
scope.3.semanticHash=aff660ee290181d1288e5334e90f91e17f3c6ca243e12946e16df96c1de72e2e
scope.4.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSN3aW5uZXI6MTQ2
scope.4.kind=field
scope.4.startLine=146
scope.4.endLine=146
scope.4.semanticHash=d76827961eca0eb4c351ae8de7a2ab43cef35e44ddbeacc98fe0869ab9a2a5cf
scope.5.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2Nhc2hPbmx5UHJpY2UoMik6MTQx
scope.5.kind=method
scope.5.startLine=141
scope.5.endLine=144
scope.5.semanticHash=cac83728b89ad526d7460e2182a95ce6284ce3a3e6a5682bcc948a81d30933e8
scope.6.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2N0b3IoMCk6MTQ
scope.6.kind=method
scope.6.startLine=14
scope.6.endLine=15
scope.6.semanticHash=63ac2206a9ffc42cdfad955bd5f627aa364526c58316028457dafce68df5c9b6
scope.7.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2dyb3VwRm9yKDIpOjEwOA
scope.7.kind=method
scope.7.startLine=108
scope.7.endLine=111
scope.7.semanticHash=fb74a4f477abc6f191c77c773f2778e2c5d04c45105ee453e64c67c08d105f66
scope.8.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2luY2x1ZGVzU3BhcmVTd2VldGVuZXIoMyk6OTM
scope.8.kind=method
scope.8.startLine=93
scope.8.endLine=95
scope.8.semanticHash=943e2bdf3f9f3571c71fddab3fdf6888b55ce66e3c242f066c217bb02c3f6769
scope.9.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3ByaWNlKDUpOjEyOQ
scope.9.kind=method
scope.9.startLine=129
scope.9.endLine=139
scope.9.semanticHash=ea8b79b3a40ef914aede7b2985d5d881d9a649cbdf2293b7a934314ce28b85df
scope.10.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3Jlc29sdmUoNCk6MTc
scope.10.kind=method
scope.10.startLine=17
scope.10.endLine=19
scope.10.semanticHash=bcc2f12aa042b93821574b76e87f38865df95355c553c3aab81c18115ee65445
scope.11.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3Jlc29sdmUoNSk6MjU
scope.11.kind=method
scope.11.startLine=25
scope.11.endLine=32
scope.11.semanticHash=7bf5adf345bf2ba1bc5f4833b5c9b1d5096099e713c1ff4a94b986ee0ceaa8a9
scope.12.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3Jlc29sdmVBdFR1cm5TdGFydCg0KToyMQ
scope.12.kind=method
scope.12.startLine=21
scope.12.endLine=23
scope.12.semanticHash=6b2d0042d665861a32c6070c9b0d0f298ae53e15af7a45b20911cd8013f39626
scope.13.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3JpY2hlcigyKToxMTM
scope.13.kind=method
scope.13.startLine=113
scope.13.endLine=117
scope.13.semanticHash=91c14c712483c9f9d20e68dbd44d5f68d2a4c191dba337d66bd001b77187f502
scope.14.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NlbGVjdFdpbm5lcig1KTo1Mg
scope.14.kind=method
scope.14.startLine=52
scope.14.endLine=57
scope.14.semanticHash=bd29d26e6cf8f8cf6441a006cbba69200d3dce5de490e4f5e3cc1ebda776f76a
scope.15.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NldHRsZSg2KTo1OQ
scope.15.kind=method
scope.15.startLine=59
scope.15.endLine=72
scope.15.semanticHash=c1f914d0a63f474c91b3a22997b77b30083ec4d89e4f1b71913062f91907e1c3
scope.16.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NldHRsZW1lbnRDYXNoKDQpOjc1
scope.16.kind=method
scope.16.startLine=75
scope.16.endLine=78
scope.16.semanticHash=aff5bfe5b5013ec92e292e4156d0bd5cefd5fb034f1ac3a1c8c333567d56ac01
scope.17.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwYXJlT3duZXIoNSk6MTE5
scope.17.kind=method
scope.17.startLine=119
scope.17.endLine=127
scope.17.semanticHash=53b0d3f443992019c568d07064731545c9be83a16116ecc21237ff927ac29d1a
scope.18.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwYXJlU3RyZWV0c09mKDQpOjEwMg
scope.18.kind=method
scope.18.startLine=102
scope.18.endLine=106
scope.18.semanticHash=2591ec9f4c7c6c6fd56b5d24979f3942f6548437b7554412042d9446f803f40b
scope.19.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwbGl0R3JvdXAoNCk6MzU
scope.19.kind=method
scope.19.startLine=35
scope.19.endLine=50
scope.19.semanticHash=5f7a787e49945444e1d8d4b60bb76ad398cae7ac51553b7c155605a1a544e229
scope.20.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3N0cmVldE93bmVkQnkoMyk6OTc
scope.20.kind=method
scope.20.startLine=97
scope.20.endLine=100
scope.20.semanticHash=83240e715406f9201397d346f12a43949b1fbb4fa02fd7fbf95fd6a8d67b7988
scope.21.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3dhaXZlSWZVbmFmZm9yZGFibGUoMyk6ODc
scope.21.kind=method
scope.21.startLine=87
scope.21.endLine=91
scope.21.semanticHash=95c436cfdeb9e0b2d98c833ea92e2cf55fd74f3d63c92e445d22e70642d55292
scope.22.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0Lk91dGNvbWUjY3RvcigzKToxNDY
scope.22.kind=method
scope.22.startLine=1
scope.22.endLine=148
scope.22.semanticHash=cf73b7c3724fb33721968d169063df418ec508b0ab1ef44f944652db07fae602
*/
