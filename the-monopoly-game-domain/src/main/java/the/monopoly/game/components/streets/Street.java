package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

public record Street(Money value) {
  public enum Type implements Factory {
    start(new Start());

    private final Factory factory;

    Type(Factory factory) {
      this.factory = factory;
    }

    @Override
    public Street create() {
      return factory.create();
    }
  }

  public interface Factory {
    Street create();
  }
}
