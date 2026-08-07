package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.Optional;

/** Resolves two players who jointly own every street in one colour group. */
public final class MonopolyBuyout {
  private MonopolyBuyout() {
  }

  public static Optional<Outcome> resolve(Player first, Player second, Rule.Set rules, Deeds deeds) {
    List<ColourStreet> streets = rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).toList();
    List<ColourStreet> group = streets.stream()
        .filter(street -> deeds.ownerOf(street.type()).filter(first.id()::equals).isPresent()
            || deeds.ownerOf(street.type()).filter(second.id()::equals).isPresent())
        .map(street -> groupFor(street, streets))
        .filter(candidate -> candidate.stream().allMatch(it -> deeds.ownerOf(it.type()).isPresent()))
        .findFirst().orElse(List.of());
    if (group.isEmpty()) return Optional.empty();
    if (group.stream().map(it -> deeds.ownerOf(it.type()).orElseThrow()).distinct().count() != 2) {
      return Optional.empty();
    }
    Player selectedWinner = richer(first, second);
    if (selectedWinner == null) selectedWinner = spareOwner(first, second, rules, deeds, group);
    if (selectedWinner == null) return Optional.empty();
    final Player winner = selectedWinner;
    Player loser = winner.id().equals(first.id()) ? second : first;
    ColourStreet winnerStreet = group.stream().filter(it -> deeds.ownerOf(it.type()).filter(winner.id()::equals).isPresent())
        .findFirst().orElseThrow();
    ColourStreet loserStreet = group.stream().filter(it -> deeds.ownerOf(it.type()).filter(loser.id()::equals).isPresent())
        .findFirst().orElseThrow();
    List<ColourStreet> spare = rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .filter(it -> it.colourGroup() != winnerStreet.colourGroup())
        .filter(it -> deeds.ownerOf(it.type()).filter(winner.id()::equals).isPresent()).toList();
    Money cash = price(winner, winnerStreet, spare, loserStreet);
    if (!cash.equals(Money.ZERO) && !winner.account().balance().amount().covers(new Money(cash.amount() * 2))) {
      if (winner.account().balance().amount().exceeds(loser.account().balance().amount())) cash = Money.ZERO;
      else if (spare.isEmpty()) return Optional.empty();
    }
    if (cash.equals(Money.ZERO) && spare.isEmpty()) return Optional.empty();
    deeds.transfer(loserStreet, loser, winner, cash);
    if (!spare.isEmpty() && (cash.equals(Money.ZERO) || winner.account().balance().amount().amount() < 2000)) {
      deeds.transfer(spare.getFirst(), winner, loser, Money.ZERO);
    }
    return Optional.of(new Outcome(winner, loser, cash));
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
                             ColourStreet loserStreet) {
    if (spare.isEmpty()) return new Money(Math.max(0,
        Math.abs(loserStreet.price().amount() - winnerStreet.price().amount()) - 10));
    if (winner.account().balance().amount().amount() > 1500) {
      ColourStreet mostValuable = spare.stream().max(java.util.Comparator.comparingInt(it -> it.rentForOneHotel().amount())).orElseThrow();
      return new Money(mostValuable.rentForOneHotel().amount() * 2);
    }
    return new Money(winnerStreet.vacantRent().amount() * 3);
  }

  public record Outcome(Player winner, Player loser, Money payment) {
  }
}
