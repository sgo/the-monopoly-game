package the.monopoly.game.components.players;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PawnTest {
  @Test
  void theOfficialSetHasEightPawns() {
    assertThat(Pawn.values()).hasSize(8);
  }

  @Test
  void everyPawnIsNamedAfterThePieceItIs() {
    assertThat(Pawn.values())
        .extracting(Pawn::pawnName)
        .containsExactly(
            "dog",
            "high hat",
            "iron box",
            "racecar",
            "ship",
            "shoe",
            "thimble",
            "wheelbarrow"
        );
  }

  @Test
  void aPawnCanBeFoundByTheNameASpecificationUses() {
    assertThat(Pawn.named("high hat")).isEqualTo(Pawn.high_hat);
    assertThat(Pawn.named("dog")).isEqualTo(Pawn.dog);
  }

  @Test
  void anUnknownPawnNameIsRejected() {
    assertThat(Pawn.named("battleship")).isNull();
  }

  @Test
  void aPawnIdentifiesItsPlayer() {
    assertThat(Pawn.dog.id()).isEqualTo(new Player.ID("dog"));
  }
}
