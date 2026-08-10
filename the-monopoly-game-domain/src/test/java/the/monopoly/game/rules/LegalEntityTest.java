package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LegalEntityTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Deeds deeds = new Deeds();
  private final Player dog = player("dog");
  private final Player highHat = player("high hat");
  private final Player ironBox = player("iron box");

  @Test
  void threeDistinctGreedoOwnersCanFormAnEligibleEntityOnlyWhenTheBoardIsOwned() {
    own(Street.Type.RueDeDiekirchArlon, dog);
    own(Street.Type.BruulMechelen, highHat);
    own(Street.Type.PlaceVerteVerviers, ironBox);
    rules.streets().filter(it -> it instanceof the.monopoly.game.components.streets.Ownable)
        .map(it -> (the.monopoly.game.components.streets.Ownable) it)
        .filter(it -> deeds.isUnowned(it.type()))
        .forEach(it -> deeds.sell(it, highHat, Money.ZERO));

    LegalEntity entity = LegalEntity.form("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules, deeds).orElseThrow();

    assertThat(entity.shareholders()).containsExactly(dog, highHat, ironBox);
    assertThat(entity.shareOf(dog)).isEqualTo(1.0 / 3.0);
    assertThat(entity.streets()).hasSize(3);
  }

  @Test
  void anUnownedStreetPreventsFormation() {
    own(Street.Type.RueDeDiekirchArlon, dog);
    own(Street.Type.BruulMechelen, highHat);
    own(Street.Type.PlaceVerteVerviers, ironBox);

    assertThat(LegalEntity.form("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules, deeds)).isEmpty();
  }

  private void own(Street.Type type, Player owner) {
    deeds.sell((the.monopoly.game.components.streets.Ownable) rules.create(type), owner, Money.ZERO);
  }

  private static Player player(String name) {
    Player.ID id = new Player.ID(name);
    Bank bank = new Bank.Simple();
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}
