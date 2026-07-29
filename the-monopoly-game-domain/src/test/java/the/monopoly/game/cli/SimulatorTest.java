package the.monopoly.game.cli;

import org.junit.jupiter.api.Test;
import the.monopoly.game.strategies.Strategy;

import static org.assertj.core.api.Assertions.assertThat;

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
}
