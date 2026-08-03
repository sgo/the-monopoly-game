package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.strategies.Greedo;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BuildingTest pins one reported case (the two-street brown group) of a
 * defect where a fully-hoteled colour group still offered a further build.
 * This sweeps every colour group on the board, of every size the board has,
 * to confirm develop() has nothing left to offer once any of them is fully
 * hoteled, rather than only the group that happened to be reported.
 */
@Tag("property-test")
class BuildingPropertyTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();

  @Test
  void developOffersNoFurtherBuildOnAnyColourGroupAlreadyFullyHoteled() {
    // The board has only eight colour groups; jetCheck's default iteration
    // count expects far more distinct values than that from a sampledFrom
    // generator. One iteration per group exhaustively covers the domain.
    PropertyChecker.customized().withIterationCount(8).forAll(colourGroups(), group -> {
      Deeds deeds = new Deeds();
      Player owner = ownerWith(deeds, group, 100_000);
      group.forEach(deeds::arrangeHotel);
      Money before = owner.account().balance().amount();

      Building building = new Building(deeds, rules, player -> new Greedo(), new Building.Events() {
      });
      building.develop(owner);

      return owner.account().balance().amount().equals(before)
          && group.stream().allMatch(deeds::hasHotelOn);
    });
  }

  private Generator<List<ColourStreet>> colourGroups() {
    return Generator.sampledFrom(
        rules.streets()
            .filter(ColourStreet.class::isInstance)
            .map(ColourStreet.class::cast)
            .collect(Collectors.groupingBy(ColourStreet::colourGroup))
            .values().stream()
            .map(List::copyOf)
            .toList()
    );
  }

  private Player ownerWith(Deeds deeds, List<ColourStreet> group, int balance) {
    Player.ID id = new Player.ID("owner");
    rules.bank().createAccountFor(id);
    Player owner = new Player(id, rules.bank().accountOf(id));
    group.forEach(street -> deeds.sell(street, owner, Money.ZERO));
    owner.account().deposit(new Money(balance));
    return owner;
  }
}
