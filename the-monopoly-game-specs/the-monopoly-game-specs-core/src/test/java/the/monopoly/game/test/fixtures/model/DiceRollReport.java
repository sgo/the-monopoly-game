package the.monopoly.game.test.fixtures.model;

import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.dice.Dice.Face;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public record DiceRollReport(FaceResult... results) {
  public static Collector collectorFor(Dice dice) {
    return new Collector(dice);
  }

  public record FaceResult(String symbol, int timesSeen) {
    public record Expectation(FaceResult result, int errorMarginPercentage) {
      public int lowerbound() {
        return result.timesSeen - getErrorMargin();
      }

      private int getErrorMargin() {
        return result.timesSeen / 100 * errorMarginPercentage;
      }

      public int upperbound() {
        return result.timesSeen + getErrorMargin();
      }
    }
  }

  public static class Collector implements java.util.stream.Collector<Face, Map<Face, Counter>, DiceRollReport> {
    private final Dice dice;

    public Collector(Dice dice) {
      this.dice = dice;
    }

    @Override
    public Supplier<Map<Face, Counter>> supplier() {
      return this::createBuffer;
    }

    private Map<Face, Counter> createBuffer() {
      Map<Face, Counter> buffer = new HashMap<>();
      dice.faces().forEach(face -> buffer.put(face, new Counter()));
      return buffer;
    }

    @Override
    public BiConsumer<Map<Face, Counter>, Face> accumulator() {
      return Collector::process;
    }

    private static void process(Map<Face, Counter> buffer, Face face) {
      buffer.get(face).increment();
    }

    @Override
    public BinaryOperator<Map<Face, Counter>> combiner() {
      return Collector::combine;
    }

    private static Map<Face, Counter> combine(Map<Face, Counter> x, Map<Face, Counter> y) {
      Map<Face, Counter> buffer = new HashMap<>();
      x.keySet().forEach(key -> buffer.put(key, x.get(key).plus(y.get(key))));
      return buffer;
    }

    @Override
    public Function<Map<Face, Counter>, DiceRollReport> finisher() {
      return Collector::finish;
    }

    private static DiceRollReport finish(Map<Face, Counter> buffer) {
      return new DiceRollReport(buffer
          .entrySet()
          .stream()
          .map(it -> new FaceResult(it.getKey().symbol(), it.getValue().number()))
          .toArray(FaceResult[]::new)
      );
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Set.of();
    }
  }
}
