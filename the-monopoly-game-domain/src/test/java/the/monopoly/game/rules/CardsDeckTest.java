package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CardsDeckTest {
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
}
