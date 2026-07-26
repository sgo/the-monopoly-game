package the.monopoly.game.components.board;

import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.stream.Stream;

/**
 * The order of the spaces on the board. A board records which space sits where
 * and nothing else; turning a space into a playable {@link Street} depends on
 * the rules in force, so that is the rule set's job.
 */
public record Board(List<Street.Type> layout) {
  public Board(List<Street.Type> layout) {
    this.layout = List.copyOf(layout);
  }

  public Stream<Street.Type> spaces() {
    return layout.stream();
  }

  public int size() {
    return layout.size();
  }
}
