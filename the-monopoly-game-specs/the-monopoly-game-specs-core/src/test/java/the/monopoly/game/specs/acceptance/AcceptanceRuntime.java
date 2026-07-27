package the.monopoly.game.specs.acceptance;

import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Executes the scenarios of a feature IR. Generated acceptance entry points
 * delegate everything to this runtime and to the project step handlers.
 */
public final class AcceptanceRuntime {
  private final List<StepHandler> handlers;

  public AcceptanceRuntime(List<StepHandler> handlers) {
    this.handlers = handlers;
  }

  public Stream<DynamicTest> execute(Ir feature) {
    List<DynamicTest> tests = new ArrayList<>();
    for (Ir.Scenario scenario : feature.scenarios()) {
      List<Map<String, String>> examples = scenario.examples().isEmpty()
          ? List.of(Map.of())
          : scenario.examples();
      for (int i = 0; i < examples.size(); i++) {
        Map<String, String> example = examples.get(i);
        tests.add(DynamicTest.dynamicTest(
            scenario.name() + "/example_" + (i + 1),
            () -> run(feature, scenario, example)
        ));
      }
    }
    return tests.stream();
  }

  private void run(Ir feature, Ir.Scenario scenario, Map<String, String> example) {
    World world = new World();
    List<Ir.Step> steps = new ArrayList<>(feature.background());
    steps.addAll(scenario.steps());

    String keyword = null;
    for (Ir.Step step : steps) {
      keyword = effectiveKeywordOf(step, keyword);
      try {
        handlerFor(step, keyword).execute(world, step.text(), example);
      } catch (AssertionError cause) {
        throw new AssertionError(step.keyword() + " " + step.text() + "\n  " + cause.getMessage(), cause);
      }
    }
  }

  /**
   * {@code And} continues whatever came before it, so a step introduced by it
   * arranges or asserts according to the last real keyword. A scenario may open
   * with {@code And} when a background preceded it, in which case the keyword
   * carries across.
   */
  private String effectiveKeywordOf(Ir.Step step, String preceding) {
    if (!step.keyword().equals("And")) return step.keyword();
    if (preceding == null)
      throw new AssertionError("Step continues nothing: " + step.keyword() + " " + step.text());
    return preceding;
  }

  private StepHandler handlerFor(Ir.Step step, String keyword) {
    List<StepHandler> matching = handlers.stream().filter(it -> it.matches(keyword, step.text())).toList();

    if (matching.isEmpty())
      throw new AssertionError("Unsupported step: " + step.keyword() + " " + step.text());
    if (matching.size() > 1)
      throw new AssertionError(
          "Ambiguous step: " + step.keyword() + " " + step.text() + " matches "
              + matching.stream().map(it -> it.pattern().pattern()).collect(joining(", "))
      );

    return matching.getFirst();
  }
}
