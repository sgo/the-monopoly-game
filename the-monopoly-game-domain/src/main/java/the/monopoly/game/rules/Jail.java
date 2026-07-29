package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.HashSet;
import java.util.Set;

/** Keeps track of prisoners and resolves the ways a prisoner may leave jail. */
public final class Jail implements Landings {
  public static final Money FINE = new Money(50);

  private final Rule.Set rules;
  private Events events;
  private final Set<Player.ID> prisoners = new HashSet<>();
  private final Set<Player.ID> cardUsers = new HashSet<>();
  private final java.util.Map<Player.ID, Integer> failedDoubleAttempts = new java.util.HashMap<>();

  public Jail(Rule.Set rules) {
    this(rules, new Events() {
    });
  }

  public Jail(Rule.Set rules, Events events) {
    this.rules = rules;
    this.events = events;
  }

  @Override
  public void resolve(Player player, Street space, Roll roll) {
    if (space.kind() == Street.Kind.go_to_jail) {
      imprison(player);
      events.sentToJail(player, space.type());
    }
  }

  public void imprison(Player player) {
    prisoners.add(player.id());
    player.position().moveTo(rules.gameboard().positionOf(Street.Type.OpBezoek));
  }

  public boolean holds(Player player) {
    return prisoners.contains(player.id());
  }

  public void useCard(Player player) {
    cardUsers.add(player.id());
  }

  /** Whether the normal turn may proceed without a doubles-only attempt. */
  public boolean mayTakeTurn(Player player, Strategy strategy, Deeds deeds) {
    if (!holds(player)) return true;
    if (cardUsers.remove(player.id()) && deeds.releaseGetOutOfJailFreeCard(player)) {
      release(player);
      events.leftJailWithCard(player);
      return true;
    }
    Strategy.JailFine fine = new Strategy.JailFine(FINE, player.account().balance().amount());
    if (!strategy.pays(fine)) return false;

    player.account().withdraw(FINE);
    release(player);
    events.leftJailByPaying(player, FINE);
    return true;
  }

  /** A doubles roll frees a prisoner and supplies their movement for this turn. */
  public boolean leavesOn(Roll roll, Player player) {
    if (roll.isDouble()) {
      release(player);
      events.leftJailByRollingDoubles(player);
      return true;
    }
    int failedAttempts = failedDoubleAttempts.merge(player.id(), 1, Integer::sum);
    if (failedAttempts < 3) return false;

    player.account().withdraw(FINE);
    release(player);
    events.leftJailByPaying(player, FINE);
    return true;
  }

  public void observe(Events events) {
    this.events = events;
  }

  private void release(Player player) {
    prisoners.remove(player.id());
    failedDoubleAttempts.remove(player.id());
  }

  public interface Events {
    default void sentToJail(Player player, Street.Type cause) {
    }

    default void leftJailByPaying(Player player, Money fine) {
    }

    default void leftJailWithCard(Player player) {
    }

    default void leftJailByRollingDoubles(Player player) {
    }
  }
}
