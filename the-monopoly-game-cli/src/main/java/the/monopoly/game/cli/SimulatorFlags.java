package the.monopoly.game.cli;

import java.util.List;

/**
 * Argument-flag interpretation for {@link Simulator}: which optional rules a
 * command line requests and which tokens are recognised flags (as opposed to
 * player names). Kept separate from {@code Simulator} so the flag vocabulary
 * lives in one place instead of being re-derived across {@code main} and
 * {@code runSelected}.
 */
final class SimulatorFlags {
  static final String MAX_YEARS_FLAG = "--max-years=";
  static final String SEED_FLAG = "--seed=";

  private SimulatorFlags() {
  }

  static boolean stalemateTrading(String... arguments) {
    return present(arguments, "--optional-greedo-stalemate-trading");
  }

  static boolean legalEntityTrading(String... arguments) {
    return present(arguments, "--optional-greedo-legal-entity");
  }

  static boolean assetRichOpening(String... arguments) {
    return present(arguments, "--optional-asset-rich-billionaire");
  }

  static boolean developmentLoans(String... arguments) {
    return present(arguments, "--optional-development-loans");
  }

  static boolean fullDrawDevelopmentLoans(String... arguments) {
    return present(arguments, "--optional-development-loans-full-draw");
  }

  static boolean warProfitsTax(String... arguments) {
    return present(arguments, "--optional-war-profits-tax");
  }

  /** Whether {@code token} is a recognised flag rather than a strategy name. */
  static boolean recognized(String argument) {
    return argument.equals("--optional-greedo-stalemate-trading")
        || argument.equals("--optional-greedo-legal-entity")
        || argument.equals("--optional-asset-rich-billionaire")
        || argument.equals("--optional-development-loans")
        || argument.equals("--optional-development-loans-full-draw")
        || argument.equals("--optional-war-profits-tax")
        || argument.startsWith(MAX_YEARS_FLAG)
        || argument.startsWith(SEED_FLAG);
  }

  /** Year limit from a {@code --max-years=N} argument, or -1 when absent. */
  static int maxYears(String... arguments) {
    for (String argument : arguments) {
      if (argument.startsWith(MAX_YEARS_FLAG)) {
        return Integer.parseInt(argument.substring(MAX_YEARS_FLAG.length()));
      }
    }
    return -1;
  }

  /** Seed from a {@code --seed=N} argument, or null when absent. */
  static Long seed(String... arguments) {
    for (String argument : arguments) {
      if (argument.startsWith(SEED_FLAG)) {
        return Long.parseLong(argument.substring(SEED_FLAG.length()));
      }
    }
    return null;
  }

  private static boolean present(String[] arguments, String flag) {
    return List.of(arguments).contains(flag);
  }
}