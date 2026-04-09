package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Dice;

import java.util.stream.Stream;

import static the.monopoly.game.components.dice.Dice.Type.six;

public class Official implements Rule.Set.Factory {
  @Override
  public Rule.Set create() {
    return new Rule.Set.Simple(
        Stream.of(six, six).map(Dice.Type::create).toList()
    );
  }
}
