package the.monopoly.game.specs.cucumber;

import io.cucumber.java.DataTableType;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.nl.Als;
import io.cucumber.java.nl.Dan;
import io.cucumber.java.nl.En;
import io.cucumber.java.nl.Wanneer;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.test.fixtures.services.PlayerService;
import the.monopoly.game.test.fixtures.validators.PlayerValidator;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static the.monopoly.game.specs.cucumber.ConversionUtils.value;

public class PlayerSteps {
  private final PlayerService service;
  private final PlayerValidator validator;

  public PlayerSteps(PlayerService service, PlayerValidator validator) {
    this.service = service;
    this.validator = validator;
  }

  @And("a player")
  @En("een speler")
  public void aPlayer() {
    service.createAtLeastOnePLayer();
  }

  @When("we select {int} players")
  @Als("we {int} spelers kiezen")
  public void weSelectPlayers(int numberOfPlayers) {
    service.create(numberOfPlayers);
  }

  @Then("the following pawns are at play")
  public void theFollowingPawnsAreAtPlay(List<Player.ID> expectations) {
    validator.assertPawnsAtPlay(expectations, Locale.forLanguageTag("en"));
  }

  @Dan("staan de volgende pionnen in het spel")
  public void staanDeVolgendePionnenInHetSpel(List<Player.ID> expectations) {
    validator.assertPawnsAtPlay(expectations, Locale.forLanguageTag("nl"));
  }

  @When("the player passes the street {street}")
  @Wanneer("de speler langs de straat {street} passeert")
  public void thePlayerPassesTheStreet(Street street) {
    service.pass(street);
  }

  @DataTableType
  public Player.ID playerID(Map<String, String> record) {
    return new Player.ID(value(record, "name", "naam"));
  }

  @When("the player lands on the street {street}")
  @Wanneer("de speler op de straat {street} land")
  public void thePlayerLandsOnTheStreet(Street street) {
    service.visit(street);
  }
}
