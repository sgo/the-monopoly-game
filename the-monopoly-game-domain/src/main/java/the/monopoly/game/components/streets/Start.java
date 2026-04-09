package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

public class Start implements Street.Factory {
  @Override
  public Street create() {
    return new Street(new Money(-200));
  }
}
