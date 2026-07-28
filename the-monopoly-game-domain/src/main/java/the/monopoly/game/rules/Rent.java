package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Optional;

/** Collects rent when a visitor stops on an owned, unimproved colour street. */
public class Rent implements Landings {
  private final Deeds deeds;
  private final Rule.Set rules;
  private final List<Player> players;
  private final Strategy.OfPlayers strategies;
  private final Events events;

  public Rent(Deeds deeds, Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies, Events events) {
    this.deeds = deeds;
    this.rules = rules;
    this.players = players;
    this.strategies = strategies;
    this.events = events;
  }

  @Override
  public void resolve(Player tenant, Street space) {
    if (!(space instanceof ColourStreet land)) return;
    deeds.ownerOf(land.type()).flatMap(this::playerNamed).ifPresent(owner -> collect(owner, tenant, land));
  }

  private void collect(Player owner, Player tenant, ColourStreet land) {
    if (owner.id().equals(tenant.id())) return;
    Money rent = rentFor(owner, land);
    if (!strategies.forPlayer(owner).claims(new Claim(tenant, land, rent))) return;
    tenant.account().withdraw(rent);
    owner.account().deposit(rent);
    events.paid(tenant, owner, land, rent);
  }

  private Money rentFor(Player owner, ColourStreet land) {
    boolean monopoly = rules.streets()
        .filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast)
        .filter(it -> it.colourGroup() == land.colourGroup())
        .allMatch(it -> deeds.ownerOf(it.type()).filter(owner.id()::equals).isPresent());
    return monopoly ? land.vacantRent().plus(land.vacantRent()) : land.vacantRent();
  }

  private Optional<Player> playerNamed(Player.ID id) {
    return players.stream().filter(it -> it.id().equals(id)).findFirst();
  }

  /** The choice an owner makes when a tenant owes them rent. */
  public record Claim(Player tenant, ColourStreet land, Money amount) {
  }

  /** What the payment did, for whoever records the game. */
  public interface Events {
    void paid(Player tenant, Player owner, ColourStreet land, Money rent);
  }
}
