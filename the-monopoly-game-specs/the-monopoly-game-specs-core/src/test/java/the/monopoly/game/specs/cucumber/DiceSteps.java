package the.monopoly.game.specs.cucumber;

import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.nl.Als;
import io.cucumber.java.nl.Dan;
import io.cucumber.java.nl.Gegeven;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.test.fixtures.model.DiceRollReport.FaceResult;
import the.monopoly.game.test.fixtures.services.DiceService;
import the.monopoly.game.test.fixtures.validators.DiceValidator;

import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static the.monopoly.game.components.dice.Dice.Type.six;
import static the.monopoly.game.specs.cucumber.ConversionUtils.value;

public class DiceSteps {
  private final DiceService service;
  private final DiceValidator validator;

  public DiceSteps(DiceService service, DiceValidator validator) {
    this.service = service;
    this.validator = validator;
  }

  @Given("a {diceType} faced dice")
  @Gegeven("een dobbelsteen met {diceType} zijdes")
  public void aFacedDice(Dice.Type type) {
    service.createDice(type);
  }

  @When("I roll the dice {int} times")
  @Als("ik de dobbelsteen {int} keer rol")
  public void iRollTheDiceTimes(int numberOfTimes) {
    service.rollDiceEqualTo(numberOfTimes);
  }

  @Then("each face was rolled an equal amount of times")
  @Dan("werd elke zijde een gelijk aantal keren gerold")
  public void eachFaceWasRolledAnEqualAmountOfTimes(List<FaceResult.Expectation> expectations) {
    validator.assertDiceRollReportMatches(expectations);
  }

  @ParameterType(".*")
  public Dice.Type diceType(String type) {
    return switch (type) {
      case "6" -> six;
      default -> throw new IllegalArgumentException("Unknown dice type! [" + type + "]");
    };
  }

  @DataTableType
  public Dice.Type diceTypeExpectation(Map<String, String> record) {
    String type = value(record, "type");
    return switch (type) {
      case "6 faced" -> six;
      case "6 zijdig" -> six;
      default -> throw new IllegalArgumentException("Unknown dice type! [" + type + "]");
    };
  }

  @DataTableType
  public FaceResult.Expectation faceResultExpectation(Map<String, String> record) {
    return new FaceResult.Expectation(new FaceResult(
        symbol(record),
        timesSeen(record)
    ), errorMargin(record));
  }

  private static int errorMargin(Map<String, String> record) {
    return parseInt(value(record, "error margin %", "foutmarge in %"));
  }

  private static int timesSeen(Map<String, String> record) {
    return parseInt(value(record, "times seen", "aantal keer gezien"));
  }

  private static String symbol(Map<String, String> record) {
    return value(record, "symbol", "symbool");
  }
}
