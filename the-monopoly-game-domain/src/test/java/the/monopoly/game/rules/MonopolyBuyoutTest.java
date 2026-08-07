package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;

import static org.assertj.core.api.Assertions.assertThat;

class MonopolyBuyoutTest {
  @Test
  void richerSecondPlayerWins() {
    Rule.Set rules = Rule.Set.Type.official.create();
    Deeds deeds = new Deeds();
    Player dog = player(rules, "dog", 100);
    Player highHat = player(rules, "high hat", 1000);
    deeds.sell(ownable(rules, Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(rules, Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);
    assertThat(deeds.ownerOf(Street.Type.MeirAntwerpen)).contains(dog.id());
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(highHat.id());
    assertThat(rules.streets().filter(it -> it.type() == Street.Type.MeirAntwerpen
        || it.type() == Street.Type.NieuwstraatBrussel).count()).isEqualTo(2);
    ColourStreet meir = (ColourStreet) ownable(rules, Street.Type.MeirAntwerpen);
    ColourStreet nieuw = (ColourStreet) ownable(rules, Street.Type.NieuwstraatBrussel);
    assertThat(meir.colourGroup()).isEqualTo(nieuw.colourGroup());
    assertThat(rules.streets().filter(it -> it instanceof ColourStreet street
        && street.colourGroup() == meir.colourGroup()).count()).isEqualTo(2);
    assertThat(MonopolyBuyout.resolve(dog, highHat, rules, deeds)).isPresent()
        .get().extracting("winner").isEqualTo(highHat);
  }

  private static Ownable ownable(Rule.Set rules, Street.Type type) {
    return (Ownable) rules.create(type);
  }

  private static Player player(Rule.Set rules, String name, int balance) {
    Player.ID id = new Player.ID(name);
    Bank bank = rules.bank();
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }
}
