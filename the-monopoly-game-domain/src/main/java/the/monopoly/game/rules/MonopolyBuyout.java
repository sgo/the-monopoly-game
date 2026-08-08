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

  /** Whether these players currently split a complete colour group. */
  public static boolean hasSplit(Player first, Player second, Rule.Set rules, Deeds deeds) {
    return !splitGroup(first, second, rules, deeds).isEmpty();
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
        .filter(candidate -> isSplitGroup(candidate, first, second, deeds))
        .max(Comparator.comparingInt(candidate -> candidate.stream()
            .mapToInt(it -> it.price().amount()).max().orElse(0)
            - candidate.stream().mapToInt(it -> it.price().amount()).min().orElse(0)))
        .orElse(List.of());
    return group;
  }

  private static Optional<Player> selectWinner(
      Player first, Player second, Rule.Set rules, Deeds deeds, List<ColourStreet> group) {
    Player winner = majorityOwner(first, second, group, deeds);
    if (winner == null) winner = richer(first, second);
    if (winner == null) winner = spareOwner(first, second, rules, deeds, group);
    return Optional.ofNullable(winner);
  }

  private static Player majorityOwner(Player first, Player second,
                                      List<ColourStreet> group, Deeds deeds) {
    long firstOwned = group.stream().filter(it -> deeds.ownerOf(it.type())
        .filter(first.id()::equals).isPresent()).count();
    long secondOwned = group.stream().filter(it -> deeds.ownerOf(it.type())
        .filter(second.id()::equals).isPresent()).count();
    return firstOwned == secondOwned ? null : firstOwned > secondOwned ? first : second;
  }

  private static Optional<Outcome> settle(
      Player winner, Player loser, Rule.Set rules, Deeds deeds, List<ColourStreet> group) {
    ColourStreet winnerStreet = streetOwnedBy(group, deeds, winner);
    ColourStreet loserStreet = streetOwnedBy(group, deeds, loser);
    List<ColourStreet> spare = spareStreetsOf(winner, loser, winnerStreet.colourGroup(), rules, deeds);
    Optional<Money> cash = settlementCash(winner, price(winner, winnerStreet, spare, loserStreet), spare);
    if (cash.isEmpty()) return Optional.empty();
    deeds.transfer(loserStreet, loser, winner, cash.get());
    if (includesSpareSweetener(spare, cash.get(), winner)) {
      deeds.transfer(spare.getFirst(), winner, loser, Money.ZERO);
    }
    return Optional.of(new Outcome(winner, loser, cash.get()));
  }

  /** The sticker price when the winner's 35% offer ceiling covers it. */
  private static Optional<Money> settlementCash(Player winner, Money stickerPrice,
                                                 List<ColourStreet> spare) {
    if (stickerPrice.equals(Money.ZERO)) return Optional.of(stickerPrice);
    int maximumOffer = winner.account().balance().amount().amount() * 35 / 100;
    if (stickerPrice.amount() <= maximumOffer) return Optional.of(stickerPrice);
    return spare.isEmpty() ? Optional.empty() : Optional.of(Money.ZERO);
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
        .filter(it -> !ownsCompleteGroup(winner, groupFor(it, streets), deeds))
        .filter(it -> !isSplitGroup(groupFor(it, streets), deeds)).toList();
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
    List<ColourStreet> streets = rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).toList();
    for (Player candidate : List.of(first, second)) {
      Player other = candidate.id().equals(first.id()) ? second : first;
      if (streets.stream()
          .filter(it -> !group.contains(it))
          .filter(it -> deeds.ownerOf(it.type()).filter(candidate.id()::equals).isPresent())
          .filter(it -> !isSplitGroup(groupFor(it, streets), candidate, other, deeds))
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

  private static boolean isSplitGroup(List<ColourStreet> group, Deeds deeds) {
    return group.stream().allMatch(it -> deeds.ownerOf(it.type()).isPresent())
        && group.stream().map(it -> deeds.ownerOf(it.type()).orElseThrow()).distinct().count() > 1;
  }

  private static boolean ownsCompleteGroup(Player owner, List<ColourStreet> group, Deeds deeds) {
    return group.stream().allMatch(it -> deeds.ownerOf(it.type()).filter(owner.id()::equals).isPresent());
  }

  public record Outcome(Player winner, Player loser, Money payment) {
  }
}

/* mutate4java-manifest
version=1
moduleHash=1504c256ca075e5df47f6965f7f5ffe2b19a601404d1161515624ceb1b49751f
scope.0.id=Y2xhc3M6TW9ub3BvbHlCdXlvdXQjTW9ub3BvbHlCdXlvdXQ6MTM
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=152
scope.0.semanticHash=05e35842d22ba90e1cc85f0a8bebaca6a0cf1b9df7d1932b0aa114ba7b899112
scope.1.id=Y2xhc3M6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNPdXRjb21lOjE1MA
scope.1.kind=class
scope.1.startLine=150
scope.1.endLine=151
scope.1.semanticHash=144d70dc47fb3d590fe148c934cce2134d44a0193705654c874b999590133429
scope.2.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNsb3NlcjoxNTA
scope.2.kind=field
scope.2.startLine=150
scope.2.endLine=150
scope.2.semanticHash=9a9d28dd1b1ff9f3a22a06427daf939e490cce4068e62054f6db6fb640566588
scope.3.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSNwYXltZW50OjE1MA
scope.3.kind=field
scope.3.startLine=150
scope.3.endLine=150
scope.3.semanticHash=aff660ee290181d1288e5334e90f91e17f3c6ca243e12946e16df96c1de72e2e
scope.4.id=ZmllbGQ6TW9ub3BvbHlCdXlvdXQuT3V0Y29tZSN3aW5uZXI6MTUw
scope.4.kind=field
scope.4.startLine=150
scope.4.endLine=150
scope.4.semanticHash=d76827961eca0eb4c351ae8de7a2ab43cef35e44ddbeacc98fe0869ab9a2a5cf
scope.5.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2N0b3IoMCk6MTQ
scope.5.kind=method
scope.5.startLine=14
scope.5.endLine=15
scope.5.semanticHash=63ac2206a9ffc42cdfad955bd5f627aa364526c58316028457dafce68df5c9b6
scope.6.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2dyb3VwRm9yKDIpOjk3
scope.6.kind=method
scope.6.startLine=97
scope.6.endLine=100
scope.6.semanticHash=fb74a4f477abc6f191c77c773f2778e2c5d04c45105ee453e64c67c08d105f66
scope.7.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2hhc1NwbGl0KDQpOjI2
scope.7.kind=method
scope.7.startLine=26
scope.7.endLine=28
scope.7.semanticHash=844ed595180203c113f180b8d864a6247eeeec790abc533f605db975d5333f8d
scope.8.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2luY2x1ZGVzU3BhcmVTd2VldGVuZXIoMyk6Nzc
scope.8.kind=method
scope.8.startLine=77
scope.8.endLine=79
scope.8.semanticHash=943e2bdf3f9f3571c71fddab3fdf6888b55ce66e3c242f066c217bb02c3f6769
scope.9.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2lzU3BsaXRHcm91cCgyKToxNDE
scope.9.kind=method
scope.9.startLine=141
scope.9.endLine=144
scope.9.semanticHash=de9e8c249f6b173d225c8d5f3902bdbffd957f21b1d9785dbcf4813fb2519203
scope.10.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I2lzU3BsaXRHcm91cCg0KToxMzQ
scope.10.kind=method
scope.10.startLine=134
scope.10.endLine=139
scope.10.semanticHash=ec01f21de7ded937d74efd6070d836c6f0e100932094549f3381aa9ed4ac9d50
scope.11.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I293bnNDb21wbGV0ZUdyb3VwKDMpOjE0Ng
scope.11.kind=method
scope.11.startLine=146
scope.11.endLine=148
scope.11.semanticHash=e39b5bec221af777a86288a16a497b7edd1fe626aaf9194ee2837b8adc06b7d1
scope.12.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3ByaWNlKDQpOjEyMw
scope.12.kind=method
scope.12.startLine=123
scope.12.endLine=132
scope.12.semanticHash=109bc28476dd604fed00f9f0c4a9f46ac2c7e29b5bfe849fc1a73de28bd998a0
scope.13.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3Jlc29sdmUoNCk6MTc
scope.13.kind=method
scope.13.startLine=17
scope.13.endLine=23
scope.13.semanticHash=4f8b8de1468b011c18fc17ae62717a739a7573468a02497bb262b1bc1dace763
scope.14.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3JpY2hlcigyKToxMDI
scope.14.kind=method
scope.14.startLine=102
scope.14.endLine=106
scope.14.semanticHash=91c14c712483c9f9d20e68dbd44d5f68d2a4c191dba337d66bd001b77187f502
scope.15.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NlbGVjdFdpbm5lcig1KTo0Nw
scope.15.kind=method
scope.15.startLine=47
scope.15.endLine=52
scope.15.semanticHash=bd29d26e6cf8f8cf6441a006cbba69200d3dce5de490e4f5e3cc1ebda776f76a
scope.16.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NldHRsZSg1KTo1NA
scope.16.kind=method
scope.16.startLine=54
scope.16.endLine=66
scope.16.semanticHash=97ee9e00e8f594537dd9f5c4887c0600a732f191e3e6b030ac0a44f66e48a59f
scope.17.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NldHRsZW1lbnRDYXNoKDMpOjY5
scope.17.kind=method
scope.17.startLine=69
scope.17.endLine=75
scope.17.semanticHash=28b5744daf5a9f406e0eb969728625c3f8dc0d8d0a5bf59fb131065927b0c436
scope.18.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwYXJlT3duZXIoNSk6MTA4
scope.18.kind=method
scope.18.startLine=108
scope.18.endLine=121
scope.18.semanticHash=2f291c1a14787fea64250d2d5eae534dea834e2d39acf296354774bb04046177
scope.19.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwYXJlU3RyZWV0c09mKDUpOjg2
scope.19.kind=method
scope.19.startLine=86
scope.19.endLine=95
scope.19.semanticHash=7bfbec07867b359bce41b90980c7b3a73aa1a42f2dd1ec4c1a32366c3f24c5e6
scope.20.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3NwbGl0R3JvdXAoNCk6MzE
scope.20.kind=method
scope.20.startLine=31
scope.20.endLine=45
scope.20.semanticHash=3a9943ebdd407f525be92d140a10338af2fff2ec99b46f026478d0151c265185
scope.21.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0I3N0cmVldE93bmVkQnkoMyk6ODE
scope.21.kind=method
scope.21.startLine=81
scope.21.endLine=84
scope.21.semanticHash=83240e715406f9201397d346f12a43949b1fbb4fa02fd7fbf95fd6a8d67b7988
scope.22.id=bWV0aG9kOk1vbm9wb2x5QnV5b3V0Lk91dGNvbWUjY3RvcigzKToxNTA
scope.22.kind=method
scope.22.startLine=1
scope.22.endLine=152
scope.22.semanticHash=cc21043fa1534a03903f97889cc7bd2090d2e797e810a00376df51baa4517df0
*/
