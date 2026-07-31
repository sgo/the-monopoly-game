package the.monopoly.game.specs.acceptance;

import java.util.List;

import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.NAME;
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
