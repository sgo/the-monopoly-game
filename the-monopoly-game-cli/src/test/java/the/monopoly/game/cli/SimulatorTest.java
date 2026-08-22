package the.monopoly.game.cli;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

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
  void rejectsUnknownStrategies() {
    Simulator.Result result = Simulator.execute("2", "careful", "careful");

    assertThat(result.succeeded()).isFalse();
    assertThat(result.output()).contains("Unknown strategy: careful.");
  }

  @Test
  void defaultsEveryPlayerToGreedo() {
    List<Player> players = Rule.Set.Type.official.create().players().select(2).toList();
    Strategy.OfPlayers strategies = Simulator.strategiesFor(2, List.of());

    assertThat(strategies.forPlayer(players.get(0))).isInstanceOf(Greedo.class);
    assertThat(strategies.forPlayer(players.get(1))).isInstanceOf(Greedo.class);
  }

  @Test
  void wiresOptionalTradingFlagsIntoEveryGreedoStrategy() {
    List<Player> players = Rule.Set.Type.official.create().players().select(2).toList();
    Strategy.OfPlayers strategies = Simulator.strategiesFor(2, List.of(), true, true);

    assertThat(((Greedo) strategies.forPlayer(players.get(0))).stalemateTradingEnabled()).isTrue();
    assertThat(((Greedo) strategies.forPlayer(players.get(0))).legalEntityTradingEnabled()).isTrue();
    assertThat(((Greedo) strategies.forPlayer(players.get(1))).stalemateTradingEnabled()).isTrue();
    assertThat(((Greedo) strategies.forPlayer(players.get(1))).legalEntityTradingEnabled()).isTrue();
  }

  @Test
  void rejectsPartialStrategySelections() {
    Simulator.Result result = Simulator.execute("2", "greedo");

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
  void startsPlayingInTheBackgroundUntilStopped() {
    Path report = Path.of(Simulator.reportPath());
    try { Files.deleteIfExists(report); } catch (Exception cause) { throw new AssertionError(cause); }
    Simulator.Running running = Simulator.start(2, Simulator.strategiesFor(2, List.of()));

    assertThat(running.isPlaying()).isTrue();

    running.stop();
    Simulator.Result result = running.awaitEnd();

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("The game starts");
    try {
      assertThat(Files.readString(report)).isEqualTo(result.output());
    } catch (Exception cause) {
      throw new AssertionError("The simulator did not write its report file.", cause);
    }
    assertThat(running.isPlaying()).isFalse();
  }

  @Test
  void rejectsAPlayerCountOutsideTheOfficialRangeWhenStarted() {
    Simulator.Running running = Simulator.start(9, Strategy.OfPlayers.NOBODY_DECIDES);

    assertThat(running.isPlaying()).isFalse();
    assertThat(running.awaitEnd().succeeded()).isFalse();
    assertThat(running.awaitEnd().output()).contains("received 9 players");
  }

  @Test
  void acceptsThePlayerCountAtTheUpperBoundaryWhenStarted() {
    Simulator.Running running = Simulator.start(8, Simulator.strategiesFor(8, List.of()));

    assertThat(running.isPlaying()).isTrue();

    running.stop();

    assertThat(running.awaitEnd().succeeded()).isTrue();
  }

  @Test
  void keepsPlayingUntilToldToStop() throws Exception {
    // Eight players, not two: with real, unseeded dice a two-player game can
    // legitimately finish inside the sleep below (observed in about 60% of
    // runs), which would fail this assertion despite the simulator behaving
    // correctly. Eight players reliably outlasts it (0/30 in the same
    // sampling) without changing what the assertion proves.
    Simulator.Running running = Simulator.start(8, Simulator.strategiesFor(8, List.of()));

    // A single round finishes far faster than this even under coverage
    // instrumentation; still playing here shows the simulation keeps going on
    // its own rather than stopping after one round regardless of whether
    // stop() was ever called.
    Thread.sleep(1000);
    assertThat(running.isPlaying()).isTrue();

    running.stop();

    assertThat(running.awaitEnd().succeeded()).isTrue();
    assertThat(running.isPlaying()).isFalse();
  }

  @Test
  void recognizesTheWarProfitsTaxFlagAndThreadsItIntoTheGame() {
    Simulator.Result result = Simulator.execute("2", "greedo", "greedo",
        "--optional-war-profits-tax", "--seed=1");

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("war profits tax is enabled");
  }

  @Test
  void threadsTheWarProfitsTaxFlagIntoTheGame() {
    Simulator.Result result = Simulator.run(2, Simulator.strategiesFor(2, List.of()),
        false, false, false, false, -1, 1L, true);

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("war profits tax is enabled");
  }

  @Test
  void leavesWarProfitsTaxDisabledByDefault() {
    Simulator.Result result = Simulator.run(2, Simulator.strategiesFor(2, List.of()),
        false, false, false, false, -1, 1L, false);

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).doesNotContain("war profits tax is enabled");
  }

  @Test
  void recognizesTheRentReliefFlagAndThreadsItIntoTheGame() {
    Simulator.Result result = Simulator.execute("2", "greedo", "greedo",
        "--optional-rent-relief", "--seed=1");

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("rent relief is enabled");
  }

  @Test
  void threadsTheRentReliefFlagIntoTheGame() {
    Simulator.Result result = Simulator.run(2, Simulator.strategiesFor(2, List.of()),
        false, false, false, false, -1, 1L, false, true);

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).contains("rent relief is enabled");
  }

  @Test
  void leavesRentReliefDisabledByDefault() {
    Simulator.Result result = Simulator.run(2, Simulator.strategiesFor(2, List.of()),
        false, false, false, false, -1, 1L, false, false);

    assertThat(result.succeeded()).isTrue();
    assertThat(result.output()).doesNotContain("rent relief is enabled");
  }

}
