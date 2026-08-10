package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.Optional;

/** A company that consolidates a three-player split of an eligible colour group. */
public final class LegalEntity {
  private final String name;
  private final Street.Colour colour;
  private final List<Player> shareholders;
  private final List<ColourStreet> streets;
  private Money loan = Money.ZERO;
  private boolean operated;

  private LegalEntity(String name, Street.Colour colour, List<Player> shareholders,
                      List<ColourStreet> streets) {
    this.name = name;
    this.colour = colour;
    this.shareholders = List.copyOf(shareholders);
    this.streets = List.copyOf(streets);
  }

  public static Optional<LegalEntity> form(String name, Street.Colour colour,
                                           List<Player> shareholders, Rule.Set rules, Deeds deeds) {
    if (shareholders.size() != 3 || shareholders.stream().distinct().count() != 3) return Optional.empty();
    if (rules.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .anyMatch(it -> deeds.isUnowned(it.type()))) return Optional.empty();
    List<ColourStreet> streets = rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).filter(it -> it.colourGroup() == colour).toList();
    if (streets.isEmpty() || streets.stream().anyMatch(it -> deeds.isUnowned(it.type()))) return Optional.empty();
    if (streets.stream().anyMatch(it -> new GreedoPriority().isHighest(it))) return Optional.empty();
    if (streets.stream().map(it -> deeds.ownerOf(it.type()).orElse(null)).distinct().count() != 3) return Optional.empty();
    if (shareholders.stream().anyMatch(player -> streets.stream()
        .noneMatch(street -> deeds.ownerOf(street.type()).filter(player.id()::equals).isPresent()))) return Optional.empty();
    return Optional.of(new LegalEntity(name, colour, shareholders, streets));
  }

  /** Creates an entity from already-set-up scenario state. */
  public static LegalEntity formed(String name, Street.Colour colour, List<Player> shareholders, Rule.Set rules) {
    List<ColourStreet> streets = rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).filter(it -> it.colourGroup() == colour).toList();
    return new LegalEntity(name, colour, shareholders, streets);
  }

  public String name() { return name; }
  public Street.Colour colour() { return colour; }
  public List<Player> shareholders() { return shareholders; }
  public List<ColourStreet> streets() { return streets; }
  public double shareOf(Player shareholder) { return shareholders.contains(shareholder) ? 1.0 / shareholders.size() : 0.0; }

  public Money loan() { return loan; }
  public boolean operated() { return operated; }
  public void markOperated() { operated = true; }
  public void raiseLoan(Money amount) { loan = loan.plus(amount); }
  public Money repayLoan(Money principal) {
    Money repayment = new Money(principal.amount() + principal.amount() * 5 / 100);
    loan = loan.minus(principal);
    return repayment;
  }

  private static final class GreedoPriority {
    private final the.monopoly.game.strategies.Greedo greedo = new the.monopoly.game.strategies.Greedo();
    boolean isHighest(ColourStreet street) {
      return greedo.priority(street) == the.monopoly.game.strategies.Strategy.Priority.HIGHEST;
    }
  }
}
