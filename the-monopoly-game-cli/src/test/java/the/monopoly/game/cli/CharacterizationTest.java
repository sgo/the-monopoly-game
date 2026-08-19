package the.monopoly.game.cli;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("characterization-test")
class CharacterizationTest {

  private static final long[] SEEDS = {1L, 2L, 3L, 4L, 5L};
  private static final int MAX_YEARS = 2500;
  private static final Path LOG_DIR = Path.of("target", "characterization-logs");
  private static final Path BASELINE_DIR = Path.of("src", "test", "resources", "characterization");

  @ParameterizedTest
  @EnumSource(CharacterizationConfig.class)
  void characterization(CharacterizationConfig config) throws IOException {
    Files.createDirectories(LOG_DIR.resolve(config.name()));

    List<GameBreakdown.GameResult> results = new ArrayList<>();
    for (long seed : SEEDS) {
      String[] args = config.arguments(seed, MAX_YEARS);
      Simulator.Result result = Simulator.execute(args);
      assertThat(result.succeeded())
          .as("config %s seed %d should succeed: %s", config.name(), seed, result.output())
          .isTrue();
      Path logFile = LOG_DIR.resolve(config.name()).resolve("seed-" + seed + ".log");
      Files.writeString(logFile, result.output());
      results.add(new GameBreakdown.GameResult(result.output(), config.developmentLoans(),
          config.legalEntityTrading(), config.stalemateTrading()));
    }

    GameBreakdown actual = GameBreakdown.aggregate(results);
    GameBreakdown expected = loadBaseline(config, actual);
    assertThat(actual.toJson())
        .as("config %s breakdown does not match baseline", config.name())
        .isEqualTo(expected.toJson());
  }

  private GameBreakdown loadBaseline(CharacterizationConfig config, GameBreakdown actual) throws IOException {
    Path fixture = BASELINE_DIR.resolve(config.name() + ".json");
    if (Boolean.getBoolean("generateCharacterizationBaselines")) {
      Files.createDirectories(BASELINE_DIR);
      Files.writeString(fixture, actual.toJson());
      return actual;
    }
    if (!Files.exists(fixture)) {
      throw new IllegalStateException("Baseline fixture missing: " + fixture
          + ". Generate it by running with -DgenerateCharacterizationBaselines=true.");
    }
    try {
      return GameBreakdown.fromJson(Files.readString(fixture));
    } catch (IOException cause) {
      throw new UncheckedIOException(cause);
    }
  }
}
