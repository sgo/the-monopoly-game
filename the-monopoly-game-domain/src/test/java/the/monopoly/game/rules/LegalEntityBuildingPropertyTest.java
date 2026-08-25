package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.stream.IntStream;

/**
 * sharesOf funds every entity build commitment and every entity loan, so an
 * unfair or lossy split would either strand a cent in no one's account or let
 * one shareholder carry more of the cost than another. This sweeps shareholder
 * counts and amounts broadly rather than pinning down a few hand-picked splits.
 *
 * ownedStreets must never let a surviving entity plan around a street it has
 * lost to individual foreclosure — the defect behind
 * entity-dev-loan-dissolution-desync. The example-based {@link LegalEntityTest}
 * pins one hand-picked loss (the first street); this sweeps every possible
 * subset of a formed entity's streets being individually foreclosed away.
 */
@Tag("property-test")
class LegalEntityBuildingPropertyTest {
  private static final Generator<Integer> AMOUNTS = Generator.integers(0, 10_000);
  private static final Generator<Integer> SHAREHOLDER_COUNTS = Generator.integers(1, 8);
  private static final Rule.Set RULES = Rule.Set.Type.official.create();
  private static final List<ColourStreet> PINK_STREETS = LegalEntity.streetsOf(Street.Colour.pink, RULES);

  @Test
  void ownedStreetsNeverIncludesAStreetLostToIndividualForeclosure() {
    for (int keptMask = 0; keptMask < (1 << PINK_STREETS.size()); keptMask++) {
      int mask = keptMask;
      Deeds deeds = new Deeds();
      LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink, players(3), RULES);
      deeds.form(entity);
      List<ColourStreet> expectedOwned = PINK_STREETS.stream()
          .filter(street -> (mask & (1 << PINK_STREETS.indexOf(street))) != 0)
          .toList();
      PINK_STREETS.stream().filter(street -> !expectedOwned.contains(street))
          .forEach(street -> deeds.returnToBank(street, entity));

      List<ColourStreet> owned = LegalEntityBuilding.ownedStreets(entity, deeds);

      org.assertj.core.api.Assertions.assertThat(owned).isEqualTo(expectedOwned);
      org.assertj.core.api.Assertions.assertThat(owned).allMatch(street ->
          deeds.entityOwnerOf(street.type()).filter(entity::equals).isPresent());
    }
  }

  @Test
  void splitsAnAmountExactlyAndFairlyAcrossEveryShareholder() {
    PropertyChecker.forAll(cases(), c -> {
      List<Player> shareholders = players(c.count());

      List<Money> shares = LegalEntityBuilding.sharesOf(shareholders, new Money(c.amount()));

      Money total = shares.stream().reduce(Money.ZERO, Money::plus);
      int max = shares.stream().mapToInt(Money::amount).max().orElseThrow();
      int min = shares.stream().mapToInt(Money::amount).min().orElseThrow();
      boolean earlierNeverPoorerThanLater = IntStream.range(0, shares.size() - 1)
          .allMatch(i -> shares.get(i).amount() >= shares.get(i + 1).amount());
      return shares.size() == c.count() && total.equals(new Money(c.amount()))
          && max - min <= 1 && earlierNeverPoorerThanLater;
    });
  }

  private Generator<Case> cases() {
    return SHAREHOLDER_COUNTS.flatMap(count -> AMOUNTS.map(amount -> new Case(count, amount)));
  }

  private List<Player> players(int count) {
    Bank bank = new Bank.Simple();
    return IntStream.range(0, count).mapToObj(index -> {
      Player.ID id = new Player.ID("shareholder" + index);
      bank.createAccountFor(id);
      return new Player(id, bank.accountOf(id));
    }).toList();
  }

  private record Case(int count, int amount) {
  }
}
