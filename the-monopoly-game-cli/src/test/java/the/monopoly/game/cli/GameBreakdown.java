package the.monopoly.game.cli;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The aggregated result of running one characterization config across its seeds.
 */
record GameBreakdown(
    Map<String, Integer> outcomes,
    Map<String, Integer> winners,
    Stats ageAtEnd,
    Optional<LoanExtras> loans,
    Optional<EntityExtras> entities,
    Optional<TradeExtras> trades
) {

  static GameBreakdown aggregate(List<GameResult> results) {
    Map<String, Integer> outcomes = new LinkedHashMap<>();
    Map<String, Integer> winners = new LinkedHashMap<>();
    List<Integer> ages = results.stream().map(GameResult::finalAge)
        .filter(OptionalInt::isPresent).mapToInt(OptionalInt::getAsInt).boxed().toList();

    for (GameResult result : results) {
      String outcome = result.outcome();
      outcomes.merge(outcome, 1, Integer::sum);
      result.winner().ifPresent(w -> winners.merge(w, 1, Integer::sum));
    }

    LoanExtras loans = results.stream().map(GameResult::loans).filter(Optional::isPresent).map(Optional::get)
        .reduce(LoanExtras::merge).orElse(null);
    EntityExtras entities = results.stream().map(GameResult::entities).filter(Optional::isPresent).map(Optional::get)
        .reduce(EntityExtras::merge).orElse(null);
    TradeExtras trades = results.stream().map(GameResult::trades).filter(Optional::isPresent).map(Optional::get)
        .reduce(TradeExtras::merge).orElse(null);

    return new GameBreakdown(outcomes, winners, Stats.of(ages),
        Optional.ofNullable(loans), Optional.ofNullable(entities), Optional.ofNullable(trades));
  }

  String toJson() {
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    json.append("  \"outcomes\": ").append(mapToJson(outcomes)).append(",\n");
    json.append("  \"winners\": ").append(mapToJson(winners)).append(",\n");
    json.append("  \"ageAtEnd\": ").append(ageAtEnd.toJson());
    loans.ifPresent(it -> json.append(",\n  \"loans\": ").append(it.toJson()));
    entities.ifPresent(it -> json.append(",\n  \"entities\": ").append(it.toJson()));
    trades.ifPresent(it -> json.append(",\n  \"trades\": ").append(it.toJson()));
    json.append("\n}");
    return json.toString();
  }

  static GameBreakdown fromJson(String json) {
    Map<String, String> fields = parseObject(json.trim());
    return new GameBreakdown(
        parseStringIntMap(fields.get("outcomes")),
        parseStringIntMap(fields.get("winners")),
        Stats.fromJson(fields.get("ageAtEnd")),
        Optional.ofNullable(fields.get("loans")).map(LoanExtras::fromJson),
        Optional.ofNullable(fields.get("entities")).map(EntityExtras::fromJson),
        Optional.ofNullable(fields.get("trades")).map(TradeExtras::fromJson));
  }

  private static String mapToJson(Map<String, Integer> map) {
    return "{" + map.entrySet().stream()
        .map(e -> "\"" + escape(e.getKey()) + "\": " + e.getValue())
        .collect(Collectors.joining(", ")) + "}";
  }

  private static Map<String, Integer> parseStringIntMap(String json) {
    Map<String, Integer> map = new LinkedHashMap<>();
    String content = json.trim();
    if (!content.startsWith("{") || !content.endsWith("}")) return map;
    content = content.substring(1, content.length() - 1).trim();
    if (content.isEmpty()) return map;
    for (String entry : splitEntries(content)) {
      int colon = entry.indexOf(':');
      String key = unquote(entry.substring(0, colon).trim());
      int value = Integer.parseInt(entry.substring(colon + 1).trim());
      map.put(key, value);
    }
    return map;
  }

  private static Map<String, String> parseObject(String json) {
    Map<String, String> fields = new LinkedHashMap<>();
    String content = json.trim();
    if (!content.startsWith("{") || !content.endsWith("}")) return fields;
    content = content.substring(1, content.length() - 1).trim();
    if (content.isEmpty()) return fields;
    for (String entry : splitTopLevelEntries(content)) {
      int colon = entry.indexOf(':');
      String key = unquote(entry.substring(0, colon).trim());
      String value = entry.substring(colon + 1).trim();
      fields.put(key, value);
    }
    return fields;
  }

  private static List<String> splitTopLevelEntries(String content) {
    return splitEntries(content, true);
  }

  private static List<String> splitEntries(String content) {
    return splitEntries(content, false);
  }

  private static List<String> splitEntries(String content, boolean nestedObjects) {
    List<String> entries = new java.util.ArrayList<>();
    int depth = 0;
    StringBuilder current = new StringBuilder();
    boolean inString = false;
    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) inString = !inString;
      if (!inString) {
        if (nestedObjects && (c == '{' || c == '[')) depth++;
        if (nestedObjects && (c == '}' || c == ']')) depth--;
        if (c == ',' && depth == 0) {
          entries.add(current.toString().trim());
          current.setLength(0);
          continue;
        }
      }
      current.append(c);
    }
    String last = current.toString().trim();
    if (!last.isEmpty()) entries.add(last);
    return entries;
  }

  private static String unquote(String s) {
    s = s.trim();
    if (s.startsWith("\"") && s.endsWith("\""))
      return s.substring(1, s.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
    return s;
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  record Stats(OptionalInt min, OptionalInt max, OptionalDouble mean, OptionalInt median) {

    static Stats of(List<Integer> values) {
      if (values.isEmpty())
        return new Stats(OptionalInt.empty(), OptionalInt.empty(), OptionalDouble.empty(), OptionalInt.empty());
      List<Integer> sorted = values.stream().sorted().toList();
      int min = sorted.get(0);
      int max = sorted.get(sorted.size() - 1);
      double mean = values.stream().mapToInt(Integer::intValue).average().orElseThrow();
      int median = sorted.size() % 2 == 1
          ? sorted.get(sorted.size() / 2)
          : (sorted.get(sorted.size() / 2 - 1) + sorted.get(sorted.size() / 2)) / 2;
      return new Stats(OptionalInt.of(min), OptionalInt.of(max), OptionalDouble.of(mean), OptionalInt.of(median));
    }

    String toJson() {
      return "{\"min\": " + optInt(min) + ", \"max\": " + optInt(max) + ", \"mean\": " + mean.orElse(0)
          + ", \"median\": " + optInt(median) + "}";
    }

    static Stats fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      return new Stats(
          parseOptionalInt(fields.get("min")),
          parseOptionalInt(fields.get("max")),
          OptionalDouble.of(Double.parseDouble(fields.get("mean"))),
          parseOptionalInt(fields.get("median")));
    }

    private static String optInt(OptionalInt value) {
      return value.isPresent() ? String.valueOf(value.getAsInt()) : "null";
    }

    private static OptionalInt parseOptionalInt(String value) {
      String trimmed = value.trim();
      return "null".equals(trimmed) ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(trimmed));
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Stats other)) return false;
      return min.equals(other.min) && max.equals(other.max)
          && doubleEquals(mean.orElse(0), other.mean.orElse(0)) && median.equals(other.median);
    }

    private static boolean doubleEquals(double a, double b) {
      return Math.abs(a - b) < 0.0001;
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(min, max, mean, median);
    }
  }

  record LoanExtras(int loansRaised, int totalDollars, Map<String, Integer> borrowers,
                    Map<String, Integer> bondholders, int defaults) {

    LoanExtras merge(LoanExtras other) {
      return new LoanExtras(
          loansRaised + other.loansRaised,
          totalDollars + other.totalDollars,
          mergeMaps(borrowers, other.borrowers),
          mergeMaps(bondholders, other.bondholders),
          defaults + other.defaults);
    }

    String toJson() {
      return "{\"loansRaised\": " + loansRaised + ", \"totalDollars\": " + totalDollars
          + ", \"borrowers\": " + mapToJson(borrowers) + ", \"bondholders\": " + mapToJson(bondholders)
          + ", \"defaults\": " + defaults + "}";
    }

    static LoanExtras fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      return new LoanExtras(
          Integer.parseInt(fields.get("loansRaised")),
          Integer.parseInt(fields.get("totalDollars")),
          parseStringIntMap(fields.get("borrowers")),
          parseStringIntMap(fields.get("bondholders")),
          Integer.parseInt(fields.get("defaults")));
    }
  }

  record EntityExtras(int formed, int dissolved) {

    EntityExtras merge(EntityExtras other) {
      return new EntityExtras(formed + other.formed, dissolved + other.dissolved);
    }

    String toJson() {
      return "{\"formed\": " + formed + ", \"dissolved\": " + dissolved + "}";
    }

    static EntityExtras fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      return new EntityExtras(Integer.parseInt(fields.get("formed")), Integer.parseInt(fields.get("dissolved")));
    }
  }

  record TradeExtras(int peerTrades) {

    TradeExtras merge(TradeExtras other) {
      return new TradeExtras(peerTrades + other.peerTrades);
    }

    String toJson() {
      return "{\"peerTrades\": " + peerTrades + "}";
    }

    static TradeExtras fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      return new TradeExtras(Integer.parseInt(fields.get("peerTrades")));
    }
  }

  private static Map<String, Integer> mergeMaps(Map<String, Integer> a, Map<String, Integer> b) {
    Map<String, Integer> merged = new LinkedHashMap<>(a);
    b.forEach((k, v) -> merged.merge(k, v, Integer::sum));
    return merged;
  }

  /**
   * A single game's parsed report.
   */
  static final class GameResult {
    private final String outcome;
    private final Optional<String> winner;
    private final OptionalInt finalAge;
    private final Optional<LoanExtras> loans;
    private final Optional<EntityExtras> entities;
    private final Optional<TradeExtras> trades;

    GameResult(String report, boolean developmentLoans, boolean legalEntityTrading, boolean stalemateTrading) {
      List<String> lines = report.lines().toList();
      Optional<String> won = lines.stream().filter(line -> line.contains(" wins the game")).findFirst()
          .map(line -> line.substring(0, line.indexOf(" wins the game")));
      Optional<String> stalemate = lines.stream().filter(line -> line.equals("The game ends in a stalemate")).findFirst();
      Optional<String> yearLimit = lines.stream().filter(line -> line.equals("The year limit was reached")).findFirst();
      if (won.isPresent()) this.outcome = "ordinary_win";
      else if (stalemate.isPresent()) this.outcome = "stalemate";
      else if (yearLimit.isPresent()) this.outcome = "year_limit";
      else this.outcome = "unknown";
      this.winner = won;

      OptionalInt lastAge = OptionalInt.empty();
      for (String line : lines) {
        if (line.endsWith(" years")) {
          int start = line.lastIndexOf(" is ");
          if (start >= 0) {
            String number = line.substring(start + 4, line.length() - 6);
            try {
              lastAge = OptionalInt.of(Integer.parseInt(number));
            } catch (NumberFormatException ignored) {
            }
          }
        }
      }
      this.finalAge = lastAge;

      Map<String, Integer> borrowers = new LinkedHashMap<>();
      Map<String, Integer> bondholders = new LinkedHashMap<>();
      int loansRaised = 0;
      int totalDollars = 0;
      int defaults = 0;
      int formed = 0;
      int dissolved = 0;
      int peerTrades = 0;

      for (String line : lines) {
        if (developmentLoans) {
          int raisesAt = line.indexOf(" raises a development loan of $");
          if (raisesAt >= 0) {
            String borrower = line.substring(0, raisesAt);
            int amountStart = raisesAt + " raises a development loan of $".length();
            int amountEnd = line.indexOf(' ', amountStart);
            if (amountEnd < 0) amountEnd = line.length();
            int amount = Integer.parseInt(line.substring(amountStart, amountEnd));
            borrowers.merge(borrower, 1, Integer::sum);
            loansRaised++;
            totalDollars += amount;
            int fundedBy = line.indexOf(", funded by ");
            if (fundedBy >= 0) {
              String bondholder = line.substring(fundedBy + ", funded by ".length(), line.indexOf("'s bond purchase", fundedBy));
              bondholders.merge(bondholder, 1, Integer::sum);
            }
          }
          if (line.contains(" defaults on the development loan")) {
            defaults++;
          }
        }
        if (legalEntityTrading) {
          if (line.contains(" is formed, held in equal thirds by ")) formed++;
          if (line.contains(" liquidates ")) dissolved++;
        }
        if (stalemateTrading) {
          if (line.contains(" trades ") && line.contains(" to ") && line.contains(" for ")) peerTrades++;
        }
      }

      this.loans = developmentLoans
          ? Optional.of(new LoanExtras(loansRaised, totalDollars, borrowers, bondholders, defaults))
          : Optional.empty();
      this.entities = legalEntityTrading
          ? Optional.of(new EntityExtras(formed, dissolved))
          : Optional.empty();
      this.trades = stalemateTrading
          ? Optional.of(new TradeExtras(peerTrades))
          : Optional.empty();
    }

    String outcome() { return outcome; }
    Optional<String> winner() { return winner; }
    OptionalInt finalAge() { return finalAge; }
    Optional<LoanExtras> loans() { return loans; }
    Optional<EntityExtras> entities() { return entities; }
    Optional<TradeExtras> trades() { return trades; }
  }
}
