package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.players.Player;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CardsDeckTest {
  private static final String CHANCE_GET_OUT_OF_JAIL_FREE = "Verlaat de gevangenis zonder te betalen.";

  @Test
  void anOfficialChanceDeckRotatesAllSixteenCards() {
    Cards.Decks decks = Cards.Decks.official();

    Set<String> drawn = new HashSet<>();
    String first = decks.drawChance();
    drawn.add(first);
    for (int draw = 1; draw < 16; draw++) drawn.add(decks.drawChance());

    assertThat(first).isNotNull();
    assertThat(drawn).hasSize(15);
    assertThat(decks.drawChance()).isEqualTo(first);
  }

  @Test
  void anOfficialCommunityChestDeckRotatesAllSixteenCards() {
    Cards.Decks decks = Cards.Decks.official();

    String first = decks.drawCommunityChest();
    for (int draw = 1; draw < 16; draw++) assertThat(decks.drawCommunityChest()).isNotNull();

    assertThat(first).isNotNull();
    assertThat(decks.drawCommunityChest()).isEqualTo(first);
  }

  @Test
  void anOfficialChanceGetOutOfJailFreeCardStaysOutUntilReleased() {
    Deeds deeds = new Deeds();
    Cards.Decks decks = Cards.Decks.official(deeds);
    String card;
    do card = decks.drawChance(); while (!card.equals(CHANCE_GET_OUT_OF_JAIL_FREE));

    Player dog = Rule.Set.Type.official.create().players().select(1).findFirst().orElseThrow();
    deeds.hold(Deeds.RetainedCard.CHANCE_GET_OUT_OF_JAIL_FREE, dog);
    for (int draw = 0; draw < 15; draw++) assertThat(decks.drawChance()).isNotEqualTo(card);

    assertThat(deeds.releaseGetOutOfJailFreeCard(dog)).isTrue();
    boolean returned = false;
    for (int draw = 0; draw < 16; draw++) returned |= decks.drawChance().equals(card);
    assertThat(returned).isTrue();
  }
}
