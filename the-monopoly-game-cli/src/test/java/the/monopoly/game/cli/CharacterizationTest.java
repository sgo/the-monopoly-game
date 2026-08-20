package the.monopoly.game.cli;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("characterization-test")
class CharacterizationTest {

  private static final long[] SEEDS = java.util.stream.LongStream.rangeClosed(1, 50).toArray();
  private static final int MAX_YEARS = 2500;
  private static final Path LOG_DIR = Path.of("target", "characterization-logs");
  private static final Path BASELINE_DIR = Path.of("src", "test", "resources", "characterization");

  @Test
  void aggregatesWarProfitsTaxAcrossSeedsIncludingZeroBalances() {
    GameBreakdown.GameResult taxed = new GameBreakdown.GameResult(
        "dog pays a war profits tax of $100\n"
            + "The government's account holds $100\n"
            + "dog's final age is 4 years\n",
        false, false, false, true);
    GameBreakdown.GameResult untaxed = new GameBreakdown.GameResult(
        "The government's account holds $0\n"
            + "dog's final age is 5 years\n",
        false, false, false, true);

    GameBreakdown.WarProfitsTaxExtras tax = GameBreakdown.aggregate(List.of(taxed, untaxed))
        .warProfitsTax().orElseThrow();

    assertThat(tax.payments()).isEqualTo(1);
    assertThat(tax.totalDollars()).isEqualTo(100);
    assertThat(tax.payers()).containsExactly(Map.entry("dog", 1));
    assertThat(tax.governmentBalance().min().orElseThrow()).isZero();
    assertThat(tax.governmentBalance().max().orElseThrow()).isEqualTo(100);
    assertThat(tax.governmentBalance().mean().orElseThrow()).isEqualTo(50.0);
    assertThat(tax.governmentBalance().median().orElseThrow()).isEqualTo(50);

    GameBreakdown.WarProfitsTaxExtras roundTripped = GameBreakdown
        .fromJson(new GameBreakdown(Map.of(), Map.of(), GameBreakdown.Stats.of(List.of()),
            GameBreakdown.Core.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.of(tax)).toJson())
        .warProfitsTax().orElseThrow();
    assertThat(roundTripped.payments()).isEqualTo(tax.payments());
    assertThat(roundTripped.totalDollars()).isEqualTo(tax.totalDollars());
    assertThat(roundTripped.payers()).containsExactlyInAnyOrderEntriesOf(tax.payers());
    assertThat(roundTripped.governmentBalance()).isEqualTo(tax.governmentBalance());
  }

  @ParameterizedTest
  @EnumSource(CharacterizationConfig.class)
  void characterization(CharacterizationConfig config) throws IOException {
    List<GameBreakdown.GameResult> results = new ArrayList<>();
    int workers = Math.max(1, Runtime.getRuntime().availableProcessors());
    try (ExecutorService pool = Executors.newFixedThreadPool(workers)) {
      List<Future<GameBreakdown.GameResult>> futures = Arrays.stream(SEEDS)
          .mapToObj(seed -> pool.submit(() -> runSeed(config, seed)))
          .toList();
      for (Future<GameBreakdown.GameResult> future : futures) {
        try {
          results.add(future.get());
        } catch (InterruptedException cause) {
          Thread.currentThread().interrupt();
          throw new AssertionError("Interrupted while running characterization seed for " + config.name(), cause);
        } catch (ExecutionException cause) {
          Throwable root = cause.getCause();
          if (root instanceof IOException io)
            throw io;
          throw new AssertionError("Characterization seed failed for " + config.name(), root);
        }
      }
    }

    GameBreakdown actual = GameBreakdown.aggregate(results);
    GameBreakdown expected = loadBaseline(config, actual);
    assertThat(actual.toJson())
        .as("config %s breakdown does not match baseline", config.name())
        .isEqualTo(expected.toJson());
  }

  private static GameBreakdown.GameResult runSeed(CharacterizationConfig config, long seed) throws IOException {
    String[] args = config.arguments(seed, MAX_YEARS);
    Simulator.Result result = Simulator.execute(args);
    if (!result.succeeded())
      throw new AssertionError("config " + config.name() + " seed " + seed
          + " should succeed: " + result.output());
    Path logFile = LOG_DIR.resolve(config.name()).resolve("seed-" + seed + ".log");
    Files.createDirectories(logFile.getParent());
    Files.writeString(logFile, result.output());
    return new GameBreakdown.GameResult(result.output(), config.developmentLoans(),
        config.legalEntityTrading(), config.stalemateTrading(), config.warProfitsTax());
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
