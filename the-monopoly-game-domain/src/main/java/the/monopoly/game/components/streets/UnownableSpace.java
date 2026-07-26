package the.monopoly.game.components.streets;

/**
 * A space nobody can buy: chance, community chest, jail, free parking and go to
 * jail. What landing on one does is decided by the turn resolution, not by the
 * space itself.
 */
public record UnownableSpace(Street.Type type, Street.Kind kind) implements Street {
  static Street.Factory of(Street.Kind kind) {
    return (type, activatedRules) -> new UnownableSpace(type, kind);
  }
}
