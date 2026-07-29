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
    assertThat(result.output()).contains("number of players must be between 2 and 8");
  }

  @Test
  void producesAReadableReportAndWinnerForAnOfficialGame() {
    Simulator.Result result = Simulator.run(2, Strategy.OfPlayers.NOBODY_DECIDES);

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("The game starts").contains("wins the game");
  }

  @Test
  void completesAnEightPlayerSimulationPromptly() {
    Simulator.Result result = assertTimeout(
        Duration.ofSeconds(1), () -> Simulator.run(8, player -> new the.monopoly.game.strategies.AgreeIfAffordable())
    );

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("goes bankrupt").contains("wins the game");
  }
}
