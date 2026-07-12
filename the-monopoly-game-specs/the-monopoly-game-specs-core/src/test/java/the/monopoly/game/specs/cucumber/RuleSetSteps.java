package the.monopoly.game.specs.cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.nl.Dan;
import io.cucumber.java.nl.En;
import io.cucumber.java.nl.Gegeven;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.test.fixtures.services.RuleSetService;
import the.monopoly.game.test.fixtures.validators.RuleSetValidator;
import the.monopoly.game.test.fixtures.validators.RuleSetValidator.BoardLayoutExpectation;

import java.util.List;

import static the.monopoly.game.rules.Rule.Set.Type.official;
import static the.monopoly.game.rules.Rule.Type.double_salary_when_landing_on_start;

public class RuleSetSteps {
  private final RuleSetService service;
  private final RuleSetValidator validator;

  public RuleSetSteps(RuleSetService service, RuleSetValidator validator) {
    this.service = service;
    this.validator = validator;
  }

  @Given("the {ruleset} rule set")
  @Gegeven("de {ruleset} regels")
  public void theRuleSet(Rule.Set.Type type) {
    service.selectRuleSet(type);
  }

  @Then("we play with the following dice")
  @Dan("spelen we met de volgende dobbelstenen")
  public void wePlayWithTheFollowingDice(List<Dice.Type> expectations) {
    validator.assertPlayWithDice(expectations);
  }

  @ParameterType(".*")
  public Rule.Set.Type ruleset(String type) {
    return switch (type) {
      case "officiële" -> official;
      default -> Rule.Set.Type.valueOf(type);
    };
  }

  @Then("we play with {int} to {int} players")
  @Dan("spelen we met {int} tot {int} spelers")
  public void wePlayWithToPlayers(int min, int max) {
    validator.assertPlayWithMinMaxPlayers(min, max);
  }

  @And("with optional double salary when landing on Start rule")
  @En("met optionele dubbel loon bij het landen op Start regel")
  public void withOptionalDoubleSalaryWhenLandingOnStartRule() {
    service.withOptionalRule(double_salary_when_landing_on_start);
  }

  @Then("the gameboard layout is")
  @Dan("is de layout van het spelbord")
  public void theGameboardLayoutIs(List<BoardLayoutExpectation> expectation) {
    validator.assertGameboardEquals(expectation);
  }
}
