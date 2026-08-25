package the.monopoly.game.cli;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The aggregated result of running one characterization config across its seeds.
 */
record GameBreakdown(
    Map<String, Integer> outcomes,
    Map<String, Integer> winners,
    Stats ageAtEnd,
    Core core,
    Optional<LoanExtras> loans,
    Optional<EntityExtras> entities,
    Optional<TradeExtras> trades,
    Optional<WarProfitsTaxExtras> warProfitsTax,
    Optional<RentReliefExtras> rentRelief
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

    Core core = results.stream().map(GameResult::core).reduce(Core::merge).orElse(Core.empty());
    LoanExtras loans = results.stream().map(GameResult::loans).filter(Optional::isPresent).map(Optional::get)
        .reduce(LoanExtras::merge).orElse(null);
    EntityExtras entities = results.stream().map(GameResult::entities).filter(Optional::isPresent).map(Optional::get)
        .reduce(EntityExtras::merge).orElse(null);
    TradeExtras trades = results.stream().map(GameResult::trades).filter(Optional::isPresent).map(Optional::get)
        .reduce(TradeExtras::merge).orElse(null);
    WarProfitsTaxExtras warProfitsTax = results.stream().map(GameResult::warProfitsTax)
        .filter(Optional::isPresent).map(Optional::get).reduce(WarProfitsTaxExtras::merge).orElse(null);
    RentReliefExtras rentRelief = results.stream().map(GameResult::rentRelief)
        .filter(Optional::isPresent).map(Optional::get).reduce(RentReliefExtras::merge).orElse(null);

    return new GameBreakdown(outcomes, winners, Stats.of(ages), core,
        Optional.ofNullable(loans), Optional.ofNullable(entities), Optional.ofNullable(trades),
        Optional.ofNullable(warProfitsTax), Optional.ofNullable(rentRelief));
  }

  String toJson() {
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    json.append("  \"outcomes\": ").append(mapToJson(outcomes)).append(",\n");
    json.append("  \"winners\": ").append(mapToJson(winners)).append(",\n");
    json.append("  \"ageAtEnd\": ").append(ageAtEnd.toJson()).append(",\n");
    json.append("  \"core\": ").append(core.toJson());
    loans.ifPresent(it -> json.append(",\n  \"loans\": ").append(it.toJson()));
    entities.ifPresent(it -> json.append(",\n  \"entities\": ").append(it.toJson()));
    trades.ifPresent(it -> json.append(",\n  \"trades\": ").append(it.toJson()));
    warProfitsTax.ifPresent(it -> json.append(",\n  \"warProfitsTax\": ").append(it.toJson()));
    rentRelief.ifPresent(it -> json.append(",\n  \"rentRelief\": ").append(it.toJson()));
    json.append("\n}");
    return json.toString();
  }

  static GameBreakdown fromJson(String json) {
    Map<String, String> fields = parseObject(json.trim());
    return new GameBreakdown(
        parseStringIntMap(fields.get("outcomes")),
        parseStringIntMap(fields.get("winners")),
        Stats.fromJson(fields.get("ageAtEnd")),
        Core.fromJson(fields.get("core")),
        Optional.ofNullable(fields.get("loans")).map(LoanExtras::fromJson),
        Optional.ofNullable(fields.get("entities")).map(EntityExtras::fromJson),
        Optional.ofNullable(fields.get("trades")).map(TradeExtras::fromJson),
        Optional.ofNullable(fields.get("warProfitsTax")).map(WarProfitsTaxExtras::fromJson),
        Optional.ofNullable(fields.get("rentRelief")).map(RentReliefExtras::fromJson));
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

  private static Map<String, Long> parseStringLongMap(String json) {
    Map<String, Long> map = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> entry : parseStringIntMap(json).entrySet())
      map.put(entry.getKey(), entry.getValue().longValue());
    if (json == null || json.isBlank()) return map;
    Map<String, String> fields = parseObject(json);
    map.clear();
    fields.forEach((key, value) -> map.put(key, parseLong(value)));
    return map;
  }

  private static Map<String, String> parseObject(String json) {
    Map<String, String> fields = new LinkedHashMap<>();
    if (json == null) return fields;
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

  private static long parseLong(String value) {
    return Long.parseLong(value.trim());
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

  /**
   * Generic core statistics, always present for every config regardless of flags.
   */
  record Core(Map<String, Integer> bankruptcies, int auctions, int directPurchases,
             int mortgages, Income income) {

    static Core empty() {
      return new Core(new LinkedHashMap<>(), 0, 0, 0, Income.empty());
    }

    Core merge(Core other) {
      return new Core(
          mergeMaps(bankruptcies, other.bankruptcies),
          auctions + other.auctions,
          directPurchases + other.directPurchases,
          mortgages + other.mortgages,
          income.merge(other.income));
    }

    String toJson() {
      return "{\"bankruptcies\": " + mapToJson(bankruptcies)
          + ", \"auctions\": " + auctions
          + ", \"directPurchases\": " + directPurchases
          + ", \"mortgages\": " + mortgages
          + ", \"income\": " + income.toJson() + "}";
    }

    static Core fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      return new Core(
          parseStringIntMap(fields.get("bankruptcies")),
          Integer.parseInt(fields.get("auctions")),
          Integer.parseInt(fields.get("directPurchases")),
          Integer.parseInt(fields.get("mortgages")),
          Income.fromJson(fields.get("income")));
    }
  }

  /**
   * Income composition, summed across all players.
   */
  record Income(long salary, long rent, long bankPayments, Map<String, PlayerIncome> byPlayer) {

    record PlayerIncome(long salary, long rent) {
      PlayerIncome merge(PlayerIncome other) {
        return new PlayerIncome(salary + other.salary, rent + other.rent);
      }

      String toJson() {
        return "{\"salary\": " + salary + ", \"rent\": " + rent + "}";
      }

      static PlayerIncome fromJson(String json) {
        Map<String, String> fields = parseObject(json);
        return new PlayerIncome(parseLong(fields.get("salary")), parseLong(fields.get("rent")));
      }
    }

    static Income empty() {
      return new Income(0, 0, 0, new LinkedHashMap<>());
    }

    Income merge(Income other) {
      Map<String, PlayerIncome> players = new LinkedHashMap<>(byPlayer);
      other.byPlayer.forEach((name, income) -> players.merge(name, income, PlayerIncome::merge));
      return new Income(salary + other.salary, rent + other.rent, bankPayments + other.bankPayments, players);
    }

    String toJson() {
      return "{\"salary\": " + salary + ", \"rent\": " + rent + ", \"bankPayments\": " + bankPayments
          + ", \"byPlayer\": " + byPlayer.entrySet().stream()
          .map(entry -> "\"" + escape(entry.getKey()) + "\": " + entry.getValue().toJson())
          .collect(Collectors.joining(", ", "{", "}")) + "}";
    }

    static Income fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      return new Income(
          parseLong(fields.get("salary")),
          parseLong(fields.get("rent")),
          parseLong(fields.get("bankPayments")),
          parseObject(fields.get("byPlayer")).entrySet().stream()
              .collect(Collectors.toMap(entry -> entry.getKey(), entry -> PlayerIncome.fromJson(entry.getValue()),
                  (a, b) -> b, LinkedHashMap::new)));
    }
  }

  record LoanExtras(int loansRaised, int totalDollars, Map<String, Integer> borrowers,
                    Map<String, Integer> bondholders, int defaults,
                    long interestPaid, long principalPaid, long bondInterestReceived,
                    long bondPrincipalReceived) {

    LoanExtras merge(LoanExtras other) {
      return new LoanExtras(
          loansRaised + other.loansRaised,
          totalDollars + other.totalDollars,
          mergeMaps(borrowers, other.borrowers),
          mergeMaps(bondholders, other.bondholders),
          defaults + other.defaults,
          interestPaid + other.interestPaid,
          principalPaid + other.principalPaid,
          bondInterestReceived + other.bondInterestReceived,
          bondPrincipalReceived + other.bondPrincipalReceived);
    }

    String toJson() {
      return "{\"loansRaised\": " + loansRaised + ", \"totalDollars\": " + totalDollars
          + ", \"borrowers\": " + mapToJson(borrowers) + ", \"bondholders\": " + mapToJson(bondholders)
          + ", \"defaults\": " + defaults
          + ", \"interestPaid\": " + interestPaid + ", \"principalPaid\": " + principalPaid
          + ", \"bondInterestReceived\": " + bondInterestReceived
          + ", \"bondPrincipalReceived\": " + bondPrincipalReceived + "}";
    }

    static LoanExtras fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      return new LoanExtras(
          Integer.parseInt(fields.get("loansRaised")),
          Integer.parseInt(fields.get("totalDollars")),
          parseStringIntMap(fields.get("borrowers")),
          parseStringIntMap(fields.get("bondholders")),
          Integer.parseInt(fields.get("defaults")),
          parseLong(fields.get("interestPaid")),
          parseLong(fields.get("principalPaid")),
          parseLong(fields.get("bondInterestReceived")),
          parseLong(fields.get("bondPrincipalReceived")));
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

  record RentReliefExtras(int reliefPayments, long reliefDollars, int gamesWithRelief,
                          int megacorpTaxPayments, long megacorpTaxDollars,
                          Map<String, Integer> megacorpTaxPayers,
                          Map<String, Long> megacorpTaxByPlayer,
                          Map<String, Long> reliefByPlayer,
                          int starvedPayments, long starvedDollars, int gamesWithStarvation,
                          Map<String, Long> starvedByPlayer) {
    RentReliefExtras merge(RentReliefExtras other) {
      return new RentReliefExtras(reliefPayments + other.reliefPayments,
          reliefDollars + other.reliefDollars, gamesWithRelief + other.gamesWithRelief,
          megacorpTaxPayments + other.megacorpTaxPayments,
          megacorpTaxDollars + other.megacorpTaxDollars,
          mergeMaps(megacorpTaxPayers, other.megacorpTaxPayers),
          mergeLongMaps(megacorpTaxByPlayer, other.megacorpTaxByPlayer),
          mergeLongMaps(reliefByPlayer, other.reliefByPlayer),
          starvedPayments + other.starvedPayments, starvedDollars + other.starvedDollars,
          gamesWithStarvation + other.gamesWithStarvation,
          mergeLongMaps(starvedByPlayer, other.starvedByPlayer));
    }

    String toJson() {
      return "{\"reliefPayments\": " + reliefPayments + ", \"reliefDollars\": " + reliefDollars
          + ", \"gamesWithRelief\": " + gamesWithRelief + ", \"megacorpTaxPayments\": "
          + megacorpTaxPayments + ", \"megacorpTaxDollars\": " + megacorpTaxDollars
          + ", \"megacorpTaxPayers\": " + mapToJson(megacorpTaxPayers)
          + ", \"megacorpTaxByPlayer\": " + longMapToJson(megacorpTaxByPlayer)
          + ", \"reliefByPlayer\": " + longMapToJson(reliefByPlayer)
          + ", \"starvedPayments\": " + starvedPayments + ", \"starvedDollars\": " + starvedDollars
          + ", \"gamesWithStarvation\": " + gamesWithStarvation
          + ", \"starvedByPlayer\": " + longMapToJson(starvedByPlayer) + "}";
    }

    static RentReliefExtras fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      return new RentReliefExtras(Integer.parseInt(fields.get("reliefPayments")),
          parseLong(fields.get("reliefDollars")), Integer.parseInt(fields.get("gamesWithRelief")),
          Integer.parseInt(fields.get("megacorpTaxPayments")), parseLong(fields.get("megacorpTaxDollars")),
          parseStringIntMap(fields.get("megacorpTaxPayers")), parseStringLongMap(fields.get("megacorpTaxByPlayer")),
          parseStringLongMap(fields.get("reliefByPlayer")), Integer.parseInt(fields.get("starvedPayments")),
          parseLong(fields.get("starvedDollars")), Integer.parseInt(fields.get("gamesWithStarvation")),
          parseStringLongMap(fields.get("starvedByPlayer")));
    }
  }

  record WarProfitsTaxExtras(int payments, long totalDollars, Map<String, Integer> payers,
                             Map<String, Long> payerDollars,
                             Stats governmentBalance, List<Integer> governmentBalances,
                             Optional<Stats> survivorsAtFirstTax, List<Integer> survivorCounts) {

    WarProfitsTaxExtras merge(WarProfitsTaxExtras other) {
      List<Integer> balances = new java.util.ArrayList<>(governmentBalances);
      balances.addAll(other.governmentBalances);
      List<Integer> survivors = new java.util.ArrayList<>(survivorCounts);
      survivors.addAll(other.survivorCounts);
      return new WarProfitsTaxExtras(
          payments + other.payments,
          totalDollars + other.totalDollars,
          mergeMaps(payers, other.payers),
          mergeLongMaps(payerDollars, other.payerDollars),
          Stats.of(balances), balances,
          survivors.isEmpty() ? Optional.empty() : Optional.of(Stats.of(survivors)), survivors);
    }

    String toJson() {
      StringBuilder json = new StringBuilder("{\"payments\": ").append(payments)
          .append(", \"totalDollars\": ").append(totalDollars)
          .append(", \"payers\": ").append(mapToJson(payers))
          .append(", \"payerDollars\": ").append(longMapToJson(payerDollars))
          .append(", \"governmentBalance\": ").append(governmentBalance.toJson());
      survivorsAtFirstTax.ifPresent(stats -> json.append(", \"survivorsAtFirstTax\": ").append(stats.toJson()));
      return json.append("}").toString();
    }

    static WarProfitsTaxExtras fromJson(String json) {
      Map<String, String> fields = parseObject(json);
      Optional<Stats> survivors = Optional.ofNullable(fields.get("survivorsAtFirstTax")).map(Stats::fromJson);
      return new WarProfitsTaxExtras(
          Integer.parseInt(fields.get("payments")),
          parseLong(fields.get("totalDollars")),
          parseStringIntMap(fields.get("payers")),
          parseStringLongMap(fields.get("payerDollars")),
          Stats.fromJson(fields.get("governmentBalance")),
          List.of(), survivors, List.of());
    }
  }

  private static Map<String, Integer> mergeMaps(Map<String, Integer> a, Map<String, Integer> b) {
    Map<String, Integer> merged = new LinkedHashMap<>(a);
    b.forEach((k, v) -> merged.merge(k, v, Integer::sum));
    return merged;
  }

  private static Map<String, Long> mergeLongMaps(Map<String, Long> a, Map<String, Long> b) {
    Map<String, Long> merged = new LinkedHashMap<>(a);
    b.forEach((k, v) -> merged.merge(k, v, Long::sum));
    return merged;
  }

  private static String longMapToJson(Map<String, Long> map) {
    return "{" + map.entrySet().stream()
        .map(e -> "\"" + escape(e.getKey()) + "\": " + e.getValue())
        .collect(Collectors.joining(", ")) + "}";
  }

  /** The number of dollars at the start of {@code rest}, i.e. after {@code marker}. */
  private static long dollarsAfter(String line, String marker) {
    int start = line.indexOf(marker);
    if (start < 0) return 0;
    String rest = line.substring(start + marker.length());
    int end = 0;
    while (end < rest.length() && Character.isDigit(rest.charAt(end))) end++;
    if (end == 0) return 0;
    return Long.parseLong(rest.substring(0, end));
  }

  /** The dollar amount appearing just before " rent " in a rent-payment line. */
  private static long rentIn(String line) {
    int rent = line.indexOf(" rent ");
    if (rent < 0) return 0;
    int dollar = line.lastIndexOf('$', rent);
    if (dollar < 0) return 0;
    int end = dollar + 1;
    while (end < line.length() && Character.isDigit(line.charAt(end))) end++;
    if (end == dollar + 1) return 0;
    return Long.parseLong(line.substring(dollar + 1, end));
  }

  /**
   * A single game's parsed report.
   */
  static final class GameResult {
    private final String outcome;
    private final Optional<String> winner;
    private final OptionalInt finalAge;
    private final Core core;
    private final Optional<LoanExtras> loans;
    private final Optional<EntityExtras> entities;
    private final Optional<TradeExtras> trades;
    private final Optional<WarProfitsTaxExtras> warProfitsTax;
    private final Optional<RentReliefExtras> rentRelief;

    GameResult(String report, boolean developmentLoans, boolean legalEntityTrading, boolean stalemateTrading,
               boolean warProfitsTax, boolean rentRelief) {
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
      Map<String, Integer> bankruptcies = new LinkedHashMap<>();
      int loansRaised = 0;
      int totalDollars = 0;
      int defaults = 0;
      int auctions = 0;
      int directPurchases = 0;
      int mortgages = 0;
      long salary = 0;
      long rent = 0;
      long bankPayments = 0;
      Map<String, Income.PlayerIncome> incomeByPlayer = new LinkedHashMap<>();
      long interestPaid = 0;
      long principalPaid = 0;
      long bondInterestReceived = 0;
      long bondPrincipalReceived = 0;
      int formed = 0;
      int dissolved = 0;
      int peerTrades = 0;
      int taxPayments = 0;
      long taxDollars = 0;
      Map<String, Integer> taxPayers = new LinkedHashMap<>();
      Map<String, Long> taxByPlayer = new LinkedHashMap<>();
      int reliefPayments = 0;
      long reliefDollars = 0;
      Map<String, Long> reliefByPlayer = new LinkedHashMap<>();
      int starvedPayments = 0;
      long starvedDollars = 0;
      boolean gameHadStarvation = false;
      Map<String, Long> starvedByPlayer = new LinkedHashMap<>();
      String pendingTenant = null;
      long pendingShortfall = 0;
      boolean gameHadRelief = false;
      int megacorpTaxPayments = 0;
      long megacorpTaxDollars = 0;
      Map<String, Integer> megacorpTaxPayers = new LinkedHashMap<>();
      Map<String, Long> megacorpTaxByPlayer = new LinkedHashMap<>();
      List<Integer> governmentBalances = new java.util.ArrayList<>();
      Set<String> survivingPlayers = new java.util.LinkedHashSet<>();
      String billionaire = null;
      boolean survivorsCaptured = false;
      List<Integer> survivorCounts = new java.util.ArrayList<>();

      for (String line : lines) {
        if (rentRelief && pendingTenant != null && pendingShortfall > 0 && !line.startsWith("The government pays ")) {
          starvedPayments++;
          starvedDollars += pendingShortfall;
          gameHadStarvation = true;
          starvedByPlayer.merge(pendingTenant, pendingShortfall, Long::sum);
          pendingTenant = null;
        }
        // Loan handling only counts when the config enables development loans,
        // because a plain land game never emits these lines and counting absent
        // mechanics would be misleading under a loans extra.
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
          if (line.contains(" pays the bank $") && line.contains(" interest and $")
              && line.contains(" principal on the development loan")) {
            interestPaid += dollarsAfter(line, " pays the bank $");
            principalPaid += dollarsAfter(line, " interest and $");
          }
          if (line.contains(" receives $") && line.contains(" interest and $")
              && line.contains(" principal on the development loan bond")) {
            bondInterestReceived += dollarsAfter(line, " receives $");
            bondPrincipalReceived += dollarsAfter(line, " interest and $");
          }
        }
        if (legalEntityTrading) {
          if (line.contains(" is formed, held in equal thirds by ")) formed++;
          if (line.contains(" liquidates ")) dissolved++;
        }
        if (stalemateTrading) {
          if (line.contains(" trades ") && line.contains(" to ") && line.contains(" for ")) peerTrades++;
        }
        if (warProfitsTax) {
          if (line.startsWith("The game starts with ")) {
            String names = line.substring("The game starts with ".length());
            survivingPlayers.addAll(List.of(names.split(", ")));
          }
          int billionaireAt = line.indexOf(" uses Billionaire (");
          if (billionaireAt >= 0) billionaire = line.substring(0, billionaireAt);
          int bankruptAt = line.indexOf(" goes bankrupt to ");
          if (bankruptAt >= 0) survivingPlayers.remove(line.substring(0, bankruptAt));
          int taxAt = line.indexOf(" pays a war profits tax of $");
          if (taxAt >= 0) {
            String payer = line.substring(0, taxAt);
            long amount = dollarsAfter(line, " pays a war profits tax of $");
            taxPayments++;
            taxDollars += amount;
            taxPayers.merge(payer, 1, Integer::sum);
            taxByPlayer.merge(payer, amount, Long::sum);
            if (!survivorsCaptured && payer.equals(billionaire)) {
              survivorCounts.add(survivingPlayers.size());
              survivorsCaptured = true;
            }
          }
          if (line.contains("The government's account holds $"))
            governmentBalances.add((int) dollarsAfter(line, "The government's account holds $"));
        }
        if (rentRelief) {
          int megacorpTaxAt = line.indexOf("MegaCorp pays the government an individual income tax of $");
          if (megacorpTaxAt >= 0) {
            int payerStart = line.indexOf('(', megacorpTaxAt);
            int payerEnd = line.indexOf(')', payerStart);
            String payer = payerStart >= 0 && payerEnd > payerStart
                ? line.substring(payerStart + 1, payerEnd) : "unknown";
            long amount = dollarsAfter(line, "MegaCorp pays the government an individual income tax of $");
            megacorpTaxPayments++;
            megacorpTaxDollars += amount;
            megacorpTaxPayers.merge(payer, 1, Integer::sum);
            megacorpTaxByPlayer.merge(payer, amount, Long::sum);
          }
        }

        // Generic core accounting happens unconditionally: these lines are exactly
        // the mechanics the spec wants visible for every config.
        int bankruptAt = line.indexOf(" goes bankrupt to ");
        if (bankruptAt >= 0) {
          String recipient = line.substring(bankruptAt + " goes bankrupt to ".length());
          bankruptcies.merge(recipient, 1, Integer::sum);
        }
        if (line.contains(" wins the auction for ")) auctions++;
        if (line.contains(" buys ")) directPurchases++;
        if (line.contains(" mortgages ")) mortgages++;

        if (line.contains("collects a salary of $")) {
          long amount = dollarsAfter(line, "collects a salary of $");
          salary += amount;
          incomeByPlayer.merge(line.substring(0, line.indexOf(" collects a salary of $")),
              new Income.PlayerIncome(amount, 0), Income.PlayerIncome::merge);
        }
        if (line.contains(" rent ") && !line.startsWith("The government pays ")) {
          long amount = rentIn(line);
          rent += amount;
          int pays = line.indexOf(" pays ");
          int dollars = pays < 0 ? -1 : line.indexOf(" $", pays);
          if (pays >= 0 && dollars > pays + 6) {
            String landlord = line.substring(pays + 6, dollars);
            incomeByPlayer.merge(landlord, new Income.PlayerIncome(0, amount), Income.PlayerIncome::merge);
            if (rentRelief) {
              pendingTenant = line.substring(0, pays);
              pendingShortfall = Math.max(0, amount - 200);
            }
          }
        }
        if (rentRelief && line.startsWith("The government pays ")) {
          reliefPayments++;
          reliefDollars += dollarsAfter(line, " $");
          gameHadRelief = true;
          int nameStart = "The government pays ".length();
          int nameEnd = line.indexOf(" $", nameStart);
          if (nameEnd > nameStart) {
            String landlord = line.substring(nameStart, nameEnd);
            long amount = dollarsAfter(line, " $");
            rent += amount;
            incomeByPlayer.merge(landlord, new Income.PlayerIncome(0, amount), Income.PlayerIncome::merge);
            if (pendingTenant != null) reliefByPlayer.merge(pendingTenant, amount, Long::sum);
            pendingTenant = null;
          }
        }
        if (line.contains("receives $") && line.contains(" from the bank"))
          bankPayments += dollarsAfter(line, "receives $");
      }

      this.core = new Core(bankruptcies, auctions, directPurchases, mortgages,
          new Income(salary, rent, bankPayments, incomeByPlayer));
      this.loans = developmentLoans
          ? Optional.of(new LoanExtras(loansRaised, totalDollars, borrowers, bondholders, defaults,
              interestPaid, principalPaid, bondInterestReceived, bondPrincipalReceived))
          : Optional.empty();
      this.entities = legalEntityTrading
          ? Optional.of(new EntityExtras(formed, dissolved))
          : Optional.empty();
      this.trades = stalemateTrading
          ? Optional.of(new TradeExtras(peerTrades))
          : Optional.empty();
      this.warProfitsTax = warProfitsTax
          ? Optional.of(new WarProfitsTaxExtras(taxPayments, taxDollars, taxPayers, taxByPlayer,
              Stats.of(governmentBalances), governmentBalances,
              survivorCounts.isEmpty() ? Optional.empty() : Optional.of(Stats.of(survivorCounts)), survivorCounts))
          : Optional.empty();
      if (rentRelief && pendingTenant != null && pendingShortfall > 0) {
        starvedPayments++;
        starvedDollars += pendingShortfall;
        gameHadStarvation = true;
        starvedByPlayer.merge(pendingTenant, pendingShortfall, Long::sum);
      }
      this.rentRelief = rentRelief
          ? Optional.of(new RentReliefExtras(reliefPayments, reliefDollars, gameHadRelief ? 1 : 0,
              megacorpTaxPayments, megacorpTaxDollars, megacorpTaxPayers, megacorpTaxByPlayer, reliefByPlayer,
              starvedPayments, starvedDollars, gameHadStarvation ? 1 : 0, starvedByPlayer))
          : Optional.empty();
    }

    String outcome() { return outcome; }
    Optional<String> winner() { return winner; }
    OptionalInt finalAge() { return finalAge; }
    Core core() { return core; }
    Optional<LoanExtras> loans() { return loans; }
    Optional<EntityExtras> entities() { return entities; }
    Optional<TradeExtras> trades() { return trades; }
    Optional<WarProfitsTaxExtras> warProfitsTax() { return warProfitsTax; }
    Optional<RentReliefExtras> rentRelief() { return rentRelief; }
  }
}
