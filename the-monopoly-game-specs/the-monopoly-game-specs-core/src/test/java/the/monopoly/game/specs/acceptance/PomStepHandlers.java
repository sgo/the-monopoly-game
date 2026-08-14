package the.monopoly.game.specs.acceptance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.NAME;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.VALUE;
import static the.monopoly.game.specs.acceptance.StepHandler.given;
import static the.monopoly.game.specs.acceptance.StepHandler.step;
import static the.monopoly.game.specs.acceptance.StepHandler.then;

/** Step vocabulary for scenarios that inspect a module's pom.xml. */
final class PomStepHandlers {
  private PomStepHandlers() {
  }

  static List<StepHandler> handlers() {
    return List.of(
        given("^the CLI module's pom\\.xml in \"" + NAME + "\"$",
            (world, arguments) -> world.selectPomModule(arguments.text(1))),

        step("^I inspect the declared dependencies$",
            (world, arguments) -> world.inspectPomDependencies()),

        step("^I inspect the declared build plugins$",
            (world, arguments) -> world.inspectPomPlugins()),

        then("^the project packages an executable jar with main class \"" + NAME + "\"$",
            (world, arguments) -> world.assertExecutableJar(arguments.text(1))),

        given("^the CLI module has been packaged$",
            (world, arguments) -> world.packageCli()),

        step("^I run the packaged simulator jar with \"" + NAME + "\"$",
            (world, arguments) -> world.runPackagedCli(arguments.text(1))),

        step("^I start the packaged simulator jar with the arguments \"" + NAME + "\"$",
            (world, arguments) -> world.startPackagedCli(arguments.text(1))),

        then("^the packaged jar's output confirms that stalemate trading is " + NAME + "$",
            (world, arguments) -> world.assertPackagedCliStalemateTrading(arguments.text(1))),

        then("^the packaged jar's output confirms that legal entity is " + NAME + "$",
            (world, arguments) -> world.assertPackagedCliLegalEntity(arguments.text(1))),

        then("^the packaged jar's output confirms that the year limit is " + VALUE + " years$",
            (world, arguments) -> world.assertPackagedCliYearLimit(arguments.number(1))),

        step("^I stop the packaged jar$", (world, arguments) -> world.stopPackagedCli()),

        then("^the packaged jar process ends$",
            (world, arguments) -> assertThat(world.packagedCliProcessEnded()).isTrue()),

        then("^the packaged jar exits successfully$",
            (world, arguments) -> world.assertPackagedCliSucceeded()),

        then("^the packaged jar's output explains how to use the simulator$",
            (world, arguments) -> world.assertPackagedCliUsage()),

        then("^the README usage report includes the optional flag \"" + NAME + "\"$",
            (world, arguments) -> world.assertReadmeUsageFlag(arguments.text(1))),

        then("^the project includes dependency \"" + NAME + "\"$",
            (world, arguments) -> world.assertPomDeclaresDependency(arguments.text(1))),

        then("^the dependency version is at least \"" + NAME + "\"$",
            (world, arguments) -> world.assertLastCheckedPomDependencyVersionAtLeast(arguments.text(1)))
    );
  }

  /** Numeric dot-separated version comparison, e.g. "2.0.17" against "1.7.0". */
  static boolean atLeast(String actual, String minimum) {
    String[] actualParts = actual.split("\\.");
    String[] minimumParts = minimum.split("\\.");
    int length = Math.max(actualParts.length, minimumParts.length);
    for (int i = 0; i < length; i++) {
      int actualPart = i < actualParts.length ? Integer.parseInt(actualParts[i]) : 0;
      int minimumPart = i < minimumParts.length ? Integer.parseInt(minimumParts[i]) : 0;
      if (actualPart != minimumPart) return actualPart > minimumPart;
    }
    return true;
  }
}
