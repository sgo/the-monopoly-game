package the.monopoly.game.specs.cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.nl.Dan;
import io.cucumber.java.nl.Gegeven;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.test.fixtures.services.RuleSetService;
import the.monopoly.game.test.fixtures.validators.RuleSetValidator;

import java.util.List;

import static the.monopoly.game.rules.Rule.Type.official;

public class RuleSetSteps {
  private final RuleSetService service;
  private final RuleSetValidator validator;

  public RuleSetSteps(RuleSetService service, RuleSetValidator validator) {
    this.service = service;
    this.validator = validator;
  }

  @Given("the {ruleset} rule set")
  @Gegeven("de {ruleset} regels")
  public void theRuleSet(Rule.Type type) {
    service.selectRuleSet(type);
  }

  @Then("we play with the following dice")
  @Dan("spelen we met de volgende dobbelstenen")
  public void wePlayWithTheFollowingDice(List<Dice.Type> expectations) {
    validator.assertPlayWithDice(expectations);
  }

  @ParameterType(".*")
  public Rule.Type ruleset(String type) {
    return switch (type) {
      case "officiële" -> official;
      default -> Rule.Type.valueOf(type);
    };
  }
}
