package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

import java.util.HashSet;
import java.util.stream.Stream;

import static the.monopoly.game.components.dice.Dice.Type.six;

public class Official implements Rule.Set.Factory {
  @Override
  public Rule.Set create() {
    Bank.Simple bank = new Bank.Simple(new HashSet<>());
    return new Rule.Set.Simple(
        Stream.of(six, six).map(Dice.Type::create).toList(),
        new Player.Pool(2, 8, bank, new Money(1500)),
        bank
    );
  }
}
