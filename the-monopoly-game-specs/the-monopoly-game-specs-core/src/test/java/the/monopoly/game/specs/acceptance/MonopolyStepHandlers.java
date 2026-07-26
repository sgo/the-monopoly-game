package the.monopoly.game.specs.acceptance;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.StartSpace;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.TaxSpace;
import the.monopoly.game.components.streets.Utility;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static the.monopoly.game.specs.acceptance.StepHandler.step;

/**
 * The step vocabulary of the component features. Steps that differ only in
 * their example values share one handler.
 * <p>
 * Each step asks the world for the kind of space it needs, so a step written
 * against the wrong kind of space fails saying so.
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
            (world, arguments) -> assertThat(world.space(StartSpace.class).salary())
                .isEqualTo(money(arguments.number(1)))),

        step("^the (?:street|station|utility) value is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(Ownable.class).price())
                .isEqualTo(money(arguments.number(1)))),

        step("^the tax is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(TaxSpace.class).tax())
                .isEqualTo(money(arguments.number(1)))),

        step("^vacant rent is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).vacantRent())
                .isEqualTo(money(arguments.number(1)))),

        step("^rent for ([0-9]+) houses? is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).rentForHouses(arguments.number(1)))
                .isEqualTo(money(arguments.number(2)))),

        step("^rent for ([0-9]+) hotel is \\$" + VALUE + "$",
            (world, arguments) -> {
              assertThat(arguments.number(1)).isEqualTo(1);
              assertThat(world.space(ColourStreet.class).rentForOneHotel())
                  .isEqualTo(money(arguments.number(2)));
            }),

        step("^house construction cost is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).houseConstructionCost())
                .isEqualTo(money(arguments.number(1)))),

        step("^hotel construction cost is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).hotelConstructionCost())
                .isEqualTo(money(arguments.number(1)))),

        step("^hotel construction requires ([0-9]+) existing houses$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).hotelConstructionRequiresNumberOfHouses())
                .isEqualTo(arguments.number(1))),

        step("^mortgage value of the land is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(Ownable.class).landMortgageValue())
                .isEqualTo(money(arguments.number(1)))),

        step("^rent for owning ([0-9]+) stations? is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(Station.class).rentForOwning(arguments.number(1)))
                .isEqualTo(money(arguments.number(2)))),

        step("^rent for owning ([0-9]+) utilit(?:y|ies) is ([0-9]+) times the dice roll$",
            (world, arguments) -> assertThat(world.space(Utility.class).rentDiceMultiplierForOwning(arguments.number(1)))
                .isEqualTo(arguments.number(2)))
    );
  }

  private static Money money(int amount) {
    return new Money(amount);
  }
}
