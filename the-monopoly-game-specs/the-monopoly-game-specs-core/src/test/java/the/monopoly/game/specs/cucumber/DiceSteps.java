package the.monopoly.game.specs.cucumber;

import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.nl.Gegeven;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.test.fixtures.DiceValidator;
import the.monopoly.game.test.fixtures.model.DiceRollReport.FaceResult;
import the.monopoly.game.test.fixtures.services.DiceService;

import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static the.monopoly.game.components.dice.Dice.Type.six;

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
  public void iRollTheDiceTimes(int numberOfTimes) {
    service.rollDiceEqualTo(numberOfTimes);
  }

  @Then("each face was rolled an equal amount of times")
  public void eachFaceWasRolledAnEqualAmountOfTimes(List<FaceResult.Expectation> expectations) {
    validator.assertDiceRollReportMatches(expectations);
  }

  @ParameterType(".*")
  public Dice.Type diceType(String type) {
    switch (type) {
      case "6":
        return six;
      default:
        throw new IllegalArgumentException("Unknown dice type! [" + type + "]");
    }
  }

  @DataTableType
  public FaceResult.Expectation faceResultExpectation(Map<String, String> record) {
    return new FaceResult.Expectation(
        new FaceResult(record.get("symbol"), parseInt(record.get("times seen"))),
        parseInt(record.get("error margin %"))
    );
  }
}
