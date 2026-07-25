package the.monopoly.game.specs.acceptance;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.Street;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static the.monopoly.game.specs.acceptance.StepHandler.step;

/**
 * The step vocabulary of the component features. Steps that differ only in
 * their example values share one handler.
 */
public final class MonopolyStepHandlers {
  /** Either a literal amount or a {@code <placeholder>} naming an example column. */
  private static final String VALUE = "(<[^<>]+>|-?[0-9,]+)";
  private static final String NAME = "(<[^<>]+>|[^\"]+)";

  private MonopolyStepHandlers() {
  }

  public static List<StepHandler> handlers() {
    return List.of(
        step("^the (?:street|station|utility|tax space) \"" + NAME + "\"$",
            (world, arguments) -> world.select(SpaceNames.of(arguments.text(1)))),

        step("^your salary is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space().toll())
                .isEqualTo(new Money(-arguments.number(1)))),

        step("^the (?:street|station|utility) value is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space().toll())
                .isEqualTo(money(arguments.number(1)))),

        step("^the tax is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space().tax())
                .isEqualTo(money(arguments.number(1)))),

        step("^vacant rent is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space().vacantRent())
                .isEqualTo(money(arguments.number(1)))),

        step("^rent for ([0-9]+) houses? is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(rentForHouses(world.space(), arguments.number(1)))
                .isEqualTo(money(arguments.number(2)))),

        step("^rent for ([0-9]+) hotel is \\$" + VALUE + "$",
            (world, arguments) -> {
              assertThat(arguments.number(1)).isEqualTo(1);
              assertThat(world.space().rentForOneHotel()).isEqualTo(money(arguments.number(2)));
            }),

        step("^house construction cost is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space().houseConstructionCost())
                .isEqualTo(money(arguments.number(1)))),

        step("^hotel construction cost is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space().hotelConstructionCost())
                .isEqualTo(money(arguments.number(1)))),

        step("^hotel construction requires ([0-9]+) existing houses$",
            (world, arguments) -> assertThat(world.space().hotelConstructionRequiresNumberOfHouses())
                .isEqualTo(arguments.number(1))),

        step("^mortgage value of the land is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space().landMortgageValue())
                .isEqualTo(money(arguments.number(1)))),

        step("^rent for owning ([0-9]+) stations? is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space().rentForOwning(arguments.number(1)))
                .isEqualTo(money(arguments.number(2)))),

        step("^rent for owning ([0-9]+) utilit(?:y|ies) is ([0-9]+) times the dice roll$",
            (world, arguments) -> assertThat(world.space().rentDiceMultiplierForOwning(arguments.number(1)))
                .isEqualTo(arguments.number(2)))
    );
  }

  private static Money rentForHouses(Street space, int houses) {
    return switch (houses) {
      case 0 -> space.vacantRent();
      case 1 -> space.rentForOneHouse();
      case 2 -> space.rentForTwoHouses();
      case 3 -> space.rentForThreeHouses();
      case 4 -> space.rentForFourHouses();
      default -> throw new AssertionError("A street never holds " + houses + " houses.");
    };
  }

  private static Money money(int amount) {
    return new Money(amount);
  }
}
