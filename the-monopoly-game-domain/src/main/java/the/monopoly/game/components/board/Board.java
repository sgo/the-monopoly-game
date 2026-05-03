package the.monopoly.game.components.board;

import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;

import java.util.List;
import java.util.stream.Stream;

public class Board {
  private final List<Street.Type> layout;
  private Rule.Set ruleSet;

  public Board(List<Street.Type> layout) {
    this.layout = layout;
  }

  public Stream<Street> streets() {
    return layout.stream().map(ruleSet::create);
  }

  public void ruleSet(Rule.Set ruleSet) {
    this.ruleSet = ruleSet;
  }
}
