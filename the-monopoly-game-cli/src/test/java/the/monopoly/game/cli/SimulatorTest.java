package the.monopoly.game.cli;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.AgreeIfAffordable;
import the.monopoly.game.strategies.Strategy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
  void explainsHowToUseTheExecutable() {
    Simulator.Result result = Simulator.execute("--h");

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("Usage: simulator");
  }

  @Test
  void rejectsANonNumericPlayerCountWithoutTerminatingTheTestProcess() {
    Simulator.Result result = Simulator.execute("many");

    assertThat(result.succeeded()).isFalse();
    assertThat(result.output()).contains("received many players");
  }

  @Test
  void acceptsOneNamedStrategyForEachPlayer() {
    Simulator.Result result = Simulator.execute("2", "agree-if-affordable", "agree-if-affordable");

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("wins the game");
  }

  @Test
  void rejectsUnknownStrategies() {
    Simulator.Result result = Simulator.execute("2", "careful", "careful");

    assertThat(result.succeeded()).isFalse();
    assertThat(result.output()).contains("Unknown strategy: careful.");
  }

  @Test
  void defaultsEveryPlayerToAgreeIfAffordable() {
    List<Player> players = Rule.Set.Type.official.create().players().select(2).toList();
    Strategy.OfPlayers strategies = Simulator.strategiesFor(2, List.of());

    assertThat(strategies.forPlayer(players.get(0))).isInstanceOf(AgreeIfAffordable.class);
    assertThat(strategies.forPlayer(players.get(1))).isInstanceOf(AgreeIfAffordable.class);
  }

  @Test
  void startsPlayersWithTheOfficialCapital() {
    Simulator.Result result = Simulator.run(2, Simulator.strategiesFor(2, List.of()));

    assertThat(result.startingBalances()).extracting(balance -> balance.amount()).containsOnly(1500);
  }

  @Test
  void rejectsPartialStrategySelections() {
    Simulator.Result result = Simulator.execute("2", "agree-if-affordable");

    assertThat(result.exitCode()).isEqualTo(1);
    assertThat(result.output()).contains("Supply one strategy for each player.");
  }

  @Test
  void runsTheExecutableProcessAndReturnsItsInvalidInputStatus() throws Exception {
    Process process = new ProcessBuilder(
        System.getProperty("java.home") + "/bin/java",
        "-cp", System.getProperty("java.class.path"), Simulator.class.getName(), "many"
    ).redirectErrorStream(true).start();

    assertThat(process.waitFor(5, TimeUnit.SECONDS)).isTrue();
    assertThat(process.exitValue()).isEqualTo(1);
    assertThat(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8))
        .contains("received many players");
  }

  @Test
  void producesAReadableReportAndWinnerForAnOfficialGame() {
    Simulator.Result result = Simulator.run(2, Simulator.strategiesFor(2, List.of()));

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("The game starts").contains("dog wins the game");
  }

  @Test
  void completesAnEightPlayerSimulationPromptly() {
    Simulator.Result result = assertTimeout(
        Duration.ofSeconds(1), () -> Simulator.run(8, player -> new the.monopoly.game.strategies.AgreeIfAffordable())
    );

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("wheelbarrow rolls 9 for initiative")
        .contains("goes bankrupt")
        .contains("wins the game");
  }
}
