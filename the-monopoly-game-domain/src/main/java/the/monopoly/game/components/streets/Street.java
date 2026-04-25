package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.Set;

public class Street {
  private final Type type;
  private final Set<Rule> activatedRules;
  private final Money.Factory.Rent rent;
  private final Money.Factory.Toll toll;

  public Street(Type type, Set<Rule> activatedRules, Money.Factory.Rent rent, Money.Factory.Toll toll) {
    this.type = type;
    this.activatedRules = activatedRules;
    this.rent = rent;
    this.toll = toll;
  }

  public Money toll() {
    return toll.create(activatedRules);
  }

  public Money rent() {
    return rent.create(activatedRules);
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Street street)) return false;

    return type == street.type;
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  public enum Type implements Factory {
    start(new Start());

    private final Factory factory;

    Type(Factory factory) {
      this.factory = factory;
    }

    @Override
    public Street create(Set<Rule> activatedRules) {
      return factory.create(activatedRules);
    }
  }

  public interface Factory {
    Street create(Set<Rule> activatedRules);
  }
}
