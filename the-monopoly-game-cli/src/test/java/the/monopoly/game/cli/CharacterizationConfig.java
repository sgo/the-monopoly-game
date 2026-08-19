package the.monopoly.game.cli;

import java.util.ArrayList;
import java.util.List;

enum CharacterizationConfig {
  two_greedo(2, List.of("greedo", "greedo"), List.of()),
  three_greedo(3, List.of("greedo", "greedo", "greedo"), List.of()),
  three_greedo_stalemate(3, List.of("greedo", "greedo", "greedo"), List.of("--optional-greedo-stalemate-trading")),
  eight_greedo(8, allGreedo(8), List.of()),
  eight_greedo_stalemate(8, allGreedo(8), List.of("--optional-greedo-stalemate-trading")),
  eight_greedo_stalemate_entity(8, allGreedo(8), List.of("--optional-greedo-stalemate-trading", "--optional-greedo-legal-entity")),
  eight_billionaire_greedo(8, billionaireThenGreedo(8), List.of("--optional-greedo-stalemate-trading", "--optional-greedo-legal-entity")),
  eight_billionaire_greedo_asset_rich(8, billionaireThenGreedo(8), List.of("--optional-greedo-stalemate-trading", "--optional-greedo-legal-entity", "--optional-asset-rich-billionaire")),
  eight_billionaire_greedo_asset_rich_loans(8, billionaireThenGreedo(8), List.of("--optional-greedo-stalemate-trading", "--optional-greedo-legal-entity", "--optional-asset-rich-billionaire", "--optional-development-loans"));

  private final int playerCount;
  private final List<String> strategies;
  private final List<String> flags;

  CharacterizationConfig(int playerCount, List<String> strategies, List<String> flags) {
    this.playerCount = playerCount;
    this.strategies = strategies;
    this.flags = flags;
  }

  String[] arguments(long seed, int maxYears) {
    List<String> args = new ArrayList<>();
    args.add(Integer.toString(playerCount));
    args.addAll(strategies);
    args.addAll(flags);
    args.add("--max-years=" + maxYears);
    args.add("--seed=" + seed);
    return args.toArray(new String[0]);
  }

  boolean developmentLoans() {
    return flags.contains("--optional-development-loans");
  }

  boolean legalEntityTrading() {
    return flags.contains("--optional-greedo-legal-entity");
  }

  boolean stalemateTrading() {
    return flags.contains("--optional-greedo-stalemate-trading");
  }

  private static List<String> allGreedo(int count) {
    return java.util.Collections.nCopies(count, "greedo");
  }

  private static List<String> billionaireThenGreedo(int count) {
    List<String> names = new ArrayList<>();
    names.add("billionaire");
    for (int i = 1; i < count; i++) names.add("greedo");
    return names;
  }
}
