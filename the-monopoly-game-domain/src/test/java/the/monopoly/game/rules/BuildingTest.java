package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Greedo;

import static org.assertj.core.api.Assertions.assertThat;

class BuildingTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();

  /**
   * Once every street in an owned colour group already has a hotel, develop()
   * has nothing left to offer for that group. Reproduces a defect found while
   * verifying the journal-logging follow-up: Simulator.start's real, unseeded
   * dice let a background game run long enough to reach this state, and it
   * threw IllegalStateException("... already has a hotel.") from inside the
   * daemon thread instead of leaving the group alone, intermittently failing
   * SimulatorTest.keepsPlayingUntilToldToStop (about 40% of runs locally).
   */
  @Test
  void developDoesNotOfferAFurtherBuildOnAColourGroupAlreadyFullyHoteled() {
    Deeds deeds = new Deeds();
    ColourStreet first = street(Street.Type.RueGrandeDinant);
    ColourStreet second = street(Street.Type.DiestsestraatLeuven);
    Player owner = ownerWith(deeds, first, second, 10_000);
    deeds.arrangeHotel(first);
    deeds.arrangeHotel(second);
    Money before = owner.account().balance().amount();

    Building building = new Building(deeds, rules, player -> new Greedo(), new Building.Events() {
    });

    building.develop(owner);

    assertThat(owner.account().balance().amount()).isEqualTo(before);
    assertThat(deeds.hasHotelOn(first)).isTrue();
    assertThat(deeds.hasHotelOn(second)).isTrue();
  }

  private Player ownerWith(Deeds deeds, ColourStreet first, ColourStreet second, int balance) {
    Player.ID id = new Player.ID("owner");
    rules.bank().createAccountFor(id);
    Player owner = new Player(id, rules.bank().accountOf(id));
    deeds.sell(first, owner, Money.ZERO);
    deeds.sell(second, owner, Money.ZERO);
    owner.account().deposit(new Money(balance));
    return owner;
  }

  private ColourStreet street(Street.Type type) {
    return (ColourStreet) rules.create(type);
  }
}
