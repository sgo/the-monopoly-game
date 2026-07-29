package the.monopoly.game.cli;

import org.junit.jupiter.api.Test;
import the.monopoly.game.strategies.Strategy;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class SimulatorTest {
  @Test
  void rejectsPlayerCountsOutsideTheOfficialRange() {
    Simulator.Result result = Simulator.run(1, Strategy.OfPlayers.NOBODY_DECIDES);

    assertThat(result.exitCode()).isEqualTo(1);
    assertThat(result.output()).contains("number of players must be between 2 and 8").contains("received 1 players");
  }

  @Test
  void producesAReadableReportAndWinnerForAnOfficialGame() {
    Simulator.Result result = Simulator.run(2, Strategy.OfPlayers.NOBODY_DECIDES);

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("The game starts").contains("dog wins the game");
  }

  @Test
  void completesAnEightPlayerSimulationPromptly() {
    Simulator.Result result = assertTimeout(
        Duration.ofSeconds(1), () -> Simulator.run(8, player -> new the.monopoly.game.strategies.AgreeIfAffordable())
    );

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("dog rolls 2 for initiative")
        .contains("high hat rolls 3 for initiative")
        .contains("iron box rolls 4 for initiative")
        .contains("racecar rolls 5 for initiative")
        .contains("ship rolls 6 for initiative")
        .contains("shoe rolls 7 for initiative")
        .contains("thimble rolls 8 for initiative")
        .contains("wheelbarrow rolls 9 for initiative")
        .contains("wheelbarrow goes bankrupt")
        .contains("dog goes bankrupt")
        .contains("high hat goes bankrupt")
        .contains("iron box goes bankrupt")
        .contains("racecar goes bankrupt")
        .contains("ship goes bankrupt")
        .contains("shoe goes bankrupt")
        .contains("thimble wins the game");
  }
}
