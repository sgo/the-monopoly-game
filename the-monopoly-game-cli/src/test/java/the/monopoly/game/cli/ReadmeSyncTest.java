package the.monopoly.game.cli;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps README.md's "Simulated game characteristics" section in sync with the
 * checked-in baseline fixtures. Reads README.md and the JSON fixtures directly,
 * so no packaging or CLI invocation is required — the ground truth is already on
 * disk. Only factual data points are checked, not the hand-written analytical
 * asides, which reflect human judgment rather than something derived mechanically
 * from a fixture.
 */
@Tag("characterization-test")
class ReadmeSyncTest {

  private static final Path README = Path.of("..", "README.md");
  private static final String BASELINE_CLASSPATH_PREFIX = "/characterization/";

  @Test
  void readmeSummariesAndDetailedBreakdownsMatchTheBaselineFixtures() throws IOException {
    String readme = Files.readString(README);
    List<String> blocks = parseDetailBlocks(readme);
    CharacterizationConfig[] configs = CharacterizationConfig.values();
    // Per CHARACTERIZATION-TESTS.md, every config must have a matching <details> block in
    // the README. A config added without its block (or a renamed one the README hasn't
    // caught up with) has to fail this test loudly — that's exactly the drift the
    // sync-check exists to catch. Permitting min(blocks, configs) would let a missing
    // block stay green forever.
    assertThat(blocks).as("one detail block per characterization config").hasSize(configs.length);

    for (int i = 0; i < configs.length; i++) {
      validate(configs[i], blocks.get(i), readme);
    }
  }

  private void validate(CharacterizationConfig config, String block, String readme) throws IOException {
    String fixture = readBaseline(config.name() + ".json");
    GameBreakdown baseline = GameBreakdown.fromJson(fixture);
    String configLabel = "config " + config.name();

    assertField(block, "Outcome", outcomesFromBaseline(baseline.outcomes()), configLabel);
    assertNameMultiset(block, "Winners", baseline.winners(), configLabel);

    // Age at end: min / median / mean / max (both Summary and Detailed forms).
    String ageSummary = ageFromBaseline(baseline.ageAtEnd());
    assertThat(readme).as(configLabel + " Summary age column").contains(ageSummary);
    assertField(block, "Age at end", detailedAgeFromBaseline(baseline.ageAtEnd()), configLabel);

    assertNameMultiset(block, "Bankruptcies", baseline.core().bankruptcies(), configLabel);

    GameBreakdown.Core core = baseline.core();
    int landAcquisitions = core.auctions() + core.directPurchases();
    String auctionPercent = percent(core.auctions(), landAcquisitions);
    String auctionText = core.auctions() + " (" + auctionPercent + " of "
        + group(core.auctions() + core.directPurchases()) + " land acquisitions)";
    assertField(block, "Auctions", auctionText, configLabel);

    assertField(block, "Mortgages", group(core.mortgages()), configLabel);

    GameBreakdown.Income income = core.income();
    String incomeText = "salary \\$" + dollars(income.salary()) + ", rent \\$" + dollars(income.rent())
        + ", bank payments \\$" + dollars(income.bankPayments());
    assertField(block, "Income", incomeText, configLabel);
    assertField(block, "Income by player", incomeByPlayer(income), configLabel);

    baseline.loans().ifPresent(loans -> validateLoans(block, loans, configLabel));
    baseline.entities().ifPresent(entities ->
        assertField(block, "Entities", entities.formed() + " formed, " + entities.dissolved() + " dissolved", configLabel));
    baseline.trades().ifPresent(trades ->
        assertField(block, "Peer trades", String.valueOf(trades.peerTrades()), configLabel));
    baseline.warProfitsTax().ifPresent(tax -> validateWarProfitsTax(block, tax, configLabel));
    baseline.rentRelief().ifPresent(relief -> validateRentRelief(block, relief, configLabel));
    if (baseline.warProfitsTax().isPresent() || baseline.rentRelief().isPresent())
      assertField(block, "Effective tax burden", effectiveTaxBurden(baseline), configLabel);
    if (baseline.rentRelief().isPresent())
      assertField(block, "Net fiscal position", netFiscalPosition(baseline), configLabel);
  }

  private static String incomeByPlayer(GameBreakdown.Income income) {
    return income.byPlayer().entrySet().stream()
        .map(entry -> entry.getKey() + " salary \\$" + dollars(entry.getValue().salary())
            + ", rent \\$" + dollars(entry.getValue().rent()))
        .collect(java.util.stream.Collectors.joining(", "));
  }

  private void validateRentRelief(String block, GameBreakdown.RentReliefExtras relief, String configLabel) {
    assertField(block, "Rent relief", group(relief.reliefPayments()) + " payments, \\$"
        + dollars(relief.reliefDollars()) + " total, " + relief.gamesWithRelief() + " games", configLabel);
    assertField(block, "MegaCorp salary tax", group(relief.megacorpTaxPayments()) + " payments, \\$"
        + dollars(relief.megacorpTaxDollars()) + " total", configLabel);
    assertNameLongMultiset(block, "MegaCorp tax payers", relief.megacorpTaxByPlayer(), configLabel);
    assertNameLongMultiset(block, "Relief received", relief.reliefByPlayer(), configLabel);
    assertField(block, "Relief starved", group(relief.starvedPayments()) + " payments, \\$"
        + dollars(relief.starvedDollars()) + " shortfall, " + relief.gamesWithStarvation() + " games", configLabel);
    assertNameLongMultiset(block, "Starved by pawn", relief.starvedByPlayer(), configLabel);
    assertField(block, "Relief age at event", detailedAgeFromBaseline(relief.reliefAgeAtEvent()), configLabel);
    assertField(block, "Starved age at event", detailedAgeFromBaseline(relief.starvedAgeAtEvent()), configLabel);
  }

  private static String effectiveTaxBurden(GameBreakdown baseline) {
    Map<String, Long> taxes = new java.util.LinkedHashMap<>();
    baseline.warProfitsTax().ifPresent(tax -> taxes.putAll(tax.payerDollars()));
    baseline.rentRelief().ifPresent(relief -> relief.megacorpTaxByPlayer().forEach(
        (name, amount) -> taxes.merge(name, amount, Long::sum)));
    return baseline.core().income().byPlayer().entrySet().stream()
        .filter(entry -> entry.getValue().salary() + entry.getValue().rent() > 0)
        .map(entry -> {
          long tax = taxes.getOrDefault(entry.getKey(), 0L);
          long grossSalary = entry.getValue().salary();
          if (baseline.rentRelief().isPresent())
            grossSalary += baseline.rentRelief().get().megacorpTaxByPlayer().getOrDefault(entry.getKey(), 0L);
          double burden = tax * 100.0 / (grossSalary + entry.getValue().rent());
          return entry.getKey() + " " + String.format(java.util.Locale.ROOT, "%.2f%%", burden);
        }).collect(java.util.stream.Collectors.joining(", "));
  }

  private static String netFiscalPosition(GameBreakdown baseline) {
    GameBreakdown.RentReliefExtras relief = baseline.rentRelief().orElseThrow();
    Map<String, Long> taxes = new java.util.LinkedHashMap<>();
    baseline.warProfitsTax().ifPresent(tax -> taxes.putAll(tax.payerDollars()));
    relief.megacorpTaxByPlayer().forEach((name, amount) -> taxes.merge(name, amount, Long::sum));
    return baseline.core().income().byPlayer().entrySet().stream()
        .filter(entry -> entry.getValue().salary() + entry.getValue().rent() > 0)
        .map(entry -> {
          long grossSalary = entry.getValue().salary()
              + relief.megacorpTaxByPlayer().getOrDefault(entry.getKey(), 0L);
          double denominator = grossSalary + entry.getValue().rent();
          double burden = taxes.getOrDefault(entry.getKey(), 0L) * 100.0 / denominator;
          double reliefRate = relief.reliefByPlayer().getOrDefault(entry.getKey(), 0L) * 100.0 / denominator;
          return entry.getKey() + " " + String.format(java.util.Locale.ROOT, "%.2f%%", reliefRate - burden);
        }).collect(java.util.stream.Collectors.joining(", "));
  }

  private void validateWarProfitsTax(String block, GameBreakdown.WarProfitsTaxExtras tax, String configLabel) {
    assertField(block, "War-profits tax", tax.payments() + " payments, \\$" + dollars(tax.totalDollars()) + " total", configLabel);
    assertNameMultiset(block, "Tax payers", tax.payers(), configLabel);
    assertField(block, "Government balance", detailedAgeFromBaseline(tax.governmentBalance()), configLabel);
    tax.survivorsAtFirstTax().ifPresent(stats ->
        assertField(block, "Survivors at first tax", detailedAgeFromBaseline(stats), configLabel));
  }

  private void validateLoans(String block, GameBreakdown.LoanExtras loans, String configLabel) {
    assertField(block, "Loans", loans.loansRaised() + " raised, \\$" + dollars(loans.totalDollars())
        + " total, " + loans.defaults() + " defaults", configLabel);
    assertNameMultiset(block, "Borrowers", loans.borrowers(), configLabel);
    assertNameMultiset(block, "Bondholders", loans.bondholders(), configLabel);
    String servicing = "borrowers paid \\$" + dollars(loans.interestPaid()) + " interest + \\$"
        + dollars(loans.principalPaid()) + " principal; bondholders received \\$"
        + dollars(loans.bondInterestReceived()) + " interest + \\$"
        + dollars(loans.bondPrincipalReceived()) + " principal";
    assertContains(block, "Servicing", servicing, configLabel);
  }

  private void assertField(String block, String label, String expected, String configLabel) {
    String actual = field(block, label);
    assertThat(actual)
        .as(configLabel + " '" + label + "' should show the fixture value. Expected: '" + expected + "'")
        .isEqualTo(expected);
  }

  /** Assert the labelled bullet's value contains a substring (used for lines like Servicing that
   *  carry hand-written asides after the data segment). */
  private void assertContains(String block, String label, String expectedSubstring, String configLabel) {
    String actual = field(block, label);
    assertThat(actual)
        .as(configLabel + " '" + label + "' should contain '" + expectedSubstring + "'")
        .contains(expectedSubstring);
  }

  /** Bankruptcies and Winners/Borrowers/Bondholders are presentational lists with inconsistent
   *  ordering across configs, so compare them as multisets of name→count pairs. */
  private void assertNameMultiset(String block, String label, Map<String, Integer> expected, String configLabel) {
    String raw = field(block, label);
    assertThat(raw).as(configLabel + " " + label + " line").isNotNull();
    Map<String, Integer> actual = parseNameCounts(raw);
    assertThat(actual)
        .as(configLabel + " " + label + " (parsed from README line '" + raw + "')")
        .containsExactlyInAnyOrderEntriesOf(expected);
  }

  private void assertNameLongMultiset(String block, String label, Map<String, Long> expected, String configLabel) {
    String raw = field(block, label);
    assertThat(raw).as(configLabel + " " + label + " line").isNotNull();
    Map<String, Long> actual = new java.util.LinkedHashMap<>();
    for (String part : raw.split(",")) {
      String trimmed = part.trim();
      int lastSpace = trimmed.lastIndexOf(' ');
      actual.put(trimmed.substring(0, lastSpace), Long.parseLong(trimmed.substring(lastSpace + 1)
          .replace("$", "").replace("\\", "")));
    }
    assertThat(actual).as(configLabel + " " + label).containsExactlyInAnyOrderEntriesOf(expected);
  }

  private static Map<String, Integer> parseNameCounts(String value) {
    Map<String, Integer> out = new java.util.LinkedHashMap<>();
    for (String part : value.split(",")) {
      String trimmed = part.trim();
      if (trimmed.isEmpty()) continue;
      int lastSpace = trimmed.lastIndexOf(' ');
      if (lastSpace < 0) throw new AssertionError("Cannot parse name-count from '" + trimmed + "'");
      String name = trimmed.substring(0, lastSpace);
      int count = Integer.parseInt(trimmed.substring(lastSpace + 1));
      out.put(name, count);
    }
    return out;
  }

  private static String readBaseline(String fileName) throws IOException {
    var url = ReadmeSyncTest.class.getResource(BASELINE_CLASSPATH_PREFIX + fileName);
    if (url == null) throw new IOException("baseline not on classpath: " + fileName);
    try (var in = url.openStream()) {
      return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }
  }

  /** The content of a top-level bullet line whose label is {@code label}. Outcome/Auctions/
   *  Loans/Servicing fields keep their parenthetical data; other labels have their trailing
   *  parenthetical and em-dash asides stripped because those are human-written commentary. */
  private static String field(String block, String label) {
    for (String line : block.lines().map(String::trim).toList()) {
      if (line.startsWith("- ") && line.length() > 2 && !line.startsWith("-  ")) {
        String body = line.substring(2);
        int colon = body.indexOf(':');
        if (colon < 0) continue;
        String key = body.substring(0, colon).trim();
        if (key.equalsIgnoreCase(label)) {
          String value = body.substring(colon + 1).trim();
          int emdash = value.indexOf(" — ");
          if (emdash >= 0) value = value.substring(0, emdash).trim();
          if (!KEEP_PARENS.contains(key.toLowerCase())) {
            int paren = value.indexOf(" (");
            if (paren >= 0) value = value.substring(0, paren).trim();
          }
          return value;
        }
      }
    }
    return null;
  }

  private static final java.util.Set<String> KEEP_PARENS = java.util.Set.of(
      "outcome", "auctions", "loans", "servicing"
  );

  /** All {@code <details>...</details>} blocks under "### Detailed Breakdown". */
  private static List<String> parseDetailBlocks(String readme) {
    String sectionStart = "### Detailed Breakdown";
    String sectionEnd = "## Running the simulator";
    int start = readme.indexOf(sectionStart);
    int end = readme.indexOf(sectionEnd, start);
    String section = readme.substring(start, end);

    List<String> blocks = new ArrayList<>();
    int from = 0;
    while (true) {
      int open = section.indexOf("<details>", from);
      if (open < 0) break;
      int close = section.indexOf("</details>", open);
      if (close < 0) break;
      blocks.add(section.substring(open + "<details>".length(), close));
      from = close + "</details>".length();
    }
    return blocks;
  }

  /** Outcome line, re-derived from the fixture, e.g. "98% win (49), 2% stalemate (1)". */
  private static String outcomesFromBaseline(Map<String, Integer> outcomes) {
    List<String> parts = new ArrayList<>();
    int total = outcomes.values().stream().mapToInt(Integer::intValue).sum();
    for (String label : List.of("ordinary_win", "stalemate", "year_limit")) {
      Integer count = outcomes.get(label);
      if (count == null) continue;
      String word = switch (label) {
        case "ordinary_win" -> "win";
        case "stalemate" -> "stalemate";
        default -> "year limit";
      };
      long rounded = total == 0 ? 0 : Math.round(count * 100.0 / total);
      parts.add(rounded + "% " + word + " (" + count + ")");
    }
    return String.join(", ", parts);
  }

  private static String ageFromBaseline(GameBreakdown.Stats stats) {
    if (!stats.min().isPresent()) return "";
    int median = stats.median().orElse(0);
    double mean = stats.mean().orElse(0);
    return stats.min().getAsInt() + " / " + median + " / " + formatMean(mean) + " / " + stats.max().getAsInt();
  }

  private static String detailedAgeFromBaseline(GameBreakdown.Stats stats) {
    if (!stats.min().isPresent()) return "";
    int median = stats.median().orElse(0);
    double mean = stats.mean().orElse(0);
    return "min " + stats.min().getAsInt() + ", median " + median + ", mean " + formatMean(mean) + ", max " + stats.max().getAsInt();
  }

  /** Render the mean the way both JSON and README do (up to two decimals, trailing .0 kept). */
  private static String formatMean(double mean) {
    return Double.toString(mean);
  }

  /** The auction-vs-land-acquisitions ratio rendered with one decimal, the way the README does. */
  private static String percent(int part, int whole) {
    if (whole == 0) return "0.0%";
    double pct = part * 100.0 / whole;
    long tenths = Math.round(pct * 10.0);
    return (tenths / 10) + "." + (tenths % 10) + "%";
  }

  /** A thousand-grouped dollar figure as it appears in the README, e.g. "$653,600" → amount. */
  private static String dollars(long amount) {
    return group(amount);
  }

  private static String group(long value) {
    String s = Long.toString(value);
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      if (i > 0 && (s.length() - i) % 3 == 0) out.append(',');
      out.append(s.charAt(i));
    }
    return out.toString();
  }
}
