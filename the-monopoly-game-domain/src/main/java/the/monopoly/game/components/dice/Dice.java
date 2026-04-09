package the.monopoly.game.components.dice;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Dice {
  private final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
  private final Face[] faces;
  private final Type type;

  public Dice(Type type, Face... faces) {
    this.type = type;
    this.faces = faces;
  }

  public Stream<Face> faces() {
    return Stream.of(faces);
  }

  public Face roll() {
    return faces[random.get().nextInt(6)];
  }

  public Type type() {
    return type;
  }

  public record Face(String symbol) {
  }

  public enum Type {
    six(IntStream.range(1, 7).mapToObj(it -> Integer.toString(it)).map(Face::new).toArray(Face[]::new));

    private final Face[] faces;

    Type(Face... faces) {
      this.faces = faces;
    }

    public Dice create() {
      return new Dice(this, faces);
    }
  }
}
