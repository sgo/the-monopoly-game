package the.monopoly.game.specs.acceptance;

import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.StartSpace;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;
import the.monopoly.game.components.streets.Utility;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static the.monopoly.game.rules.Rule.Type.double_salary_when_landing_on_start;
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
                .isEqualTo(arguments.number(2))),

        step("^the " + NAME + " rule set$",
            (world, arguments) -> world.selectRuleSet(Vocabulary.ruleSet(arguments.text(1)))),

        step("^dice ([0-9]+) is ([0-9]+) faced$",
            (world, arguments) -> assertThat(diceFaceCount(world, arguments.number(1)))
                .isEqualTo(arguments.number(2))),

        step("^we play with ([0-9]+) to ([0-9]+) players$",
            (world, arguments) -> {
              assertThat(world.ruleSet().players().min()).isEqualTo(arguments.number(1));
              assertThat(world.ruleSet().players().max()).isEqualTo(arguments.number(2));
            }),

        step("^space " + VALUE + " is \"" + NAME + "\" of type " + NAME
                + " and colour group " + NAME + "$",
            (world, arguments) -> {
              Street space = world.spaceAt(arguments.number(1));
              assertThat(space.type()).isEqualTo(SpaceNames.of(arguments.text(2)));
              assertThat(space.kind()).isEqualTo(Vocabulary.kind(arguments.text(3)));
              assertThat(colourGroupOf(space)).isEqualTo(Vocabulary.colour(arguments.text(4)));
            }),

        step("^we select ([0-9]+) players$",
            (world, arguments) -> world.selectPlayers(arguments.number(1))),

        step("^pawn \"" + NAME + "\" is at play$",
            (world, arguments) -> assertThat(world.pawn(arguments.text(1))).isNotNull()),

        step("^pawn \"" + NAME + "\"'s account balance is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.pawn(arguments.text(1)).account().balance())
                .isEqualTo(Balance.of(arguments.number(2)))),

        step("^a player$",
            (world, arguments) -> world.startPlayer()),

        step("^with \\$" + VALUE + " in his account$",
            (world, arguments) -> world.fundPlayer(money(arguments.number(1)))),

        step("^with optional double salary when landing on Start rule$",
            (world, arguments) -> world.ruleSet().activate(double_salary_when_landing_on_start)),

        step("^the player passes the street \"" + NAME + "\"$",
            (world, arguments) -> {
              world.select(SpaceNames.of(arguments.text(1)));
              world.player().pass(world.space(StartSpace.class));
            }),

        step("^the player lands on the street \"" + NAME + "\"$",
            (world, arguments) -> {
              world.select(SpaceNames.of(arguments.text(1)));
              world.player().land(world.space(StartSpace.class));
            }),

        step("^the player's account balance is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.player().account().balance())
                .isEqualTo(Balance.of(arguments.number(1)))),

        step("^a ([0-9]+) faced dice$",
            (world, arguments) -> world.selectDice(arguments.number(1))),

        step("^I roll the dice " + VALUE + " times$",
            (world, arguments) -> world.rollDice(arguments.number(1))),

        step("^each face was rolled about " + VALUE + " times within a " + VALUE + "% margin$",
            (world, arguments) -> {
              int expected = arguments.number(1);
              double margin = expected * (arguments.number(2) / 100.0);
              assertThat(world.rolls().values())
                  .allSatisfy(seen -> assertThat(seen).isCloseTo(expected, within((int) margin)));
            })
    );
  }

  private static long diceFaceCount(World world, int position) {
    List<Dice> dice = world.ruleSet().dice().toList();
    if (position < 1 || position > dice.size())
      throw new AssertionError("The rules use " + dice.size() + " dice, so there is no dice " + position + ".");
    return dice.get(position - 1).faces().count();
  }

  /** Only a colour street belongs to a colour group; every other space has none. */
  private static Street.Colour colourGroupOf(Street space) {
    return space instanceof ColourStreet street ? street.colourGroup() : null;
  }

  private static Money money(int amount) {
    return new Money(amount);
  }
}
