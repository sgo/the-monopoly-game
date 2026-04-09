package the.monopoly.game.specs.cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.nl.Dan;
import io.cucumber.java.nl.Gegeven;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.test.fixtures.services.StreetService;
import the.monopoly.game.test.fixtures.validators.StreetValidator;

import static the.monopoly.game.components.streets.Street.Type.start;

public class StreetSteps {
  private final StreetService service;
  private final StreetValidator validator;

  public StreetSteps(StreetService service, StreetValidator validator) {
    this.service = service;
    this.validator = validator;
  }

  @Given("the street {streetType}")
  @Gegeven("de straat {streetType}")
  public void theStreet(Street.Type type) {
    service.select(type);
  }

  @Then("the street value is {money}")
  @Dan("is de waarde van de straat {money}")
  public void theStreetValueIs(Money amount) {
    validator.assertValueEquals(amount);
  }

  @ParameterType("\"(.*)\"")
  public Street.Type streetType(String type) {
    return switch (type) {
      case "Start" -> start;
      default -> throw new IllegalArgumentException("Unknown dice type! [" + type + "]");
    };
  }

  // TODO - move to MoneySteps or FinanceSteps
  @ParameterType(".*")
  public Money money(String amount) {
    int modifier = 1;
    if (amount.startsWith("-"))
      modifier = -1;
    return new Money(Integer.parseInt(amount.substring(modifier > 0 ? 1 : 2)) * modifier);
  }
}
