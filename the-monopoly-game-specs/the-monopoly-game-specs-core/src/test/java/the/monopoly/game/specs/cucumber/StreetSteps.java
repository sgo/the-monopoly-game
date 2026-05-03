package the.monopoly.game.specs.cucumber;

import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.nl.Dan;
import io.cucumber.java.nl.En;
import io.cucumber.java.nl.Gegeven;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.test.fixtures.services.StreetService;
import the.monopoly.game.test.fixtures.validators.StreetValidator;

import java.util.Map;

import static the.monopoly.game.components.streets.Street.Type.RueGrandeDinant;
import static the.monopoly.game.components.streets.Street.Type.start;
import static the.monopoly.game.specs.cucumber.ConversionUtils.value;

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

  @Then("your salary is ${money}")
  public void yourSalaryIs$(Money amount) {
    validator.assertSalaryEquals(amount);
  }

  @ParameterType("\"(.*)\"")
  public Street.Type streetType(String type) {
    return switch (type) {
      case "Start" -> start;
      case "Rue Grande Dinant" -> RueGrandeDinant;
      default -> throw new IllegalArgumentException("Unknown street type! [" + type + "]");
    };
  }

  @DataTableType
  public Street.Type streetType(Map<String, String> row) {
    return streetType(value(row, "street names", "straat namen"));
  }

  @ParameterType("\"(.*)\"")
  public Street street(String type) {
    return service.create(streetType(type));
  }

  @And("vacant rent is ${money}")
  @En("onbebouwde huur is €{money}")
  public void vacantRentIs$(Money expectation) {
    validator.assertVacantRentEquals(expectation);
  }

  @And("rent for 1 house is ${money}")
  @En("huur voor 1 huis is €{money}")
  public void rentForHouseIs$(Money expectation) {
    validator.assertRentForOneHouseEquals(expectation);
  }

  @And("rent for 2 houses is ${money}")
  @En("huur voor 2 huizen is €{money}")
  public void rentForTwoHousesIs$(Money expectation) {
    validator.assertRentForTwoHousesEquals(expectation);
  }

  @And("rent for 3 houses is ${money}")
  @En("huur voor 3 huizen is €{money}")
  public void rentForThreeHousesIs$(Money expectation) {
    validator.assertRentForThreeHousesEquals(expectation);
  }

  @And("rent for 4 houses is ${money}")
  @En("huur voor 4 huizen is €{money}")
  public void rentForFourHousesIs$(Money expectation) {
    validator.assertRentForFourHousesEquals(expectation);
  }

  @And("rent for 1 hotel is ${money}")
  @En("huur voor 1 hotel is €{money}")
  public void rentForHotelIs$(Money expectation) {
    validator.assertRentForOneHotelEquals(expectation);
  }

  @And("house construction cost is ${money}")
  @En("bouw kost voor een huis is €{money}")
  public void houseConstructionCostIs$(Money expectation) {
    validator.assertHouseConstructionCostEquals(expectation);
  }

  @And("hotel construction cost is ${money}")
  @En("bouw kost voor een hotel is €{money}")
  public void hotelConstructionCostIs$(Money expectation) {
    validator.assertHotelConstructionCostEquals(expectation);
  }

  @And("hotel construction requires {int} existing houses")
  @En("bouw van een hotel vereist {int} bestaande huizen")
  public void hotelConstructionRequiresExistingHouses(int expectation) {
    validator.hotelConstructionRequiresExistingHouses(expectation);
  }

  @And("mortgage value of the land is ${money}")
  @En("hypotheekwaarde van het land is €{money}")
  public void mortgageValueOfTheLandIs$(Money expectation) {
    validator.assertLandMortgageValueEquals(expectation);
  }
}
