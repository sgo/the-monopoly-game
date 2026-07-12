package the.monopoly.game.specs.cucumber;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import the.monopoly.game.test.fixtures.services.GameService;
import the.monopoly.game.test.fixtures.validators.GameValidator;

public class GameSteps {
  private final GameService service;
  private final GameValidator validator;

  public GameSteps(GameService service, GameValidator validator) {
    this.service = service;
    this.validator = validator;
  }

  @When("we play {int} times")
  public void wePlayTimes(int numberOfTimes) {
    service.play(numberOfTimes);
  }

  @Then("the game ends every time with a monopoly")
  public void theGameEndsEveryTimeWithAMonopoly() {
    validator.assertGameEndedInMonopolyEveryTime();
  }
}
