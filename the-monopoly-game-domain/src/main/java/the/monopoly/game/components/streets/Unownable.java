package the.monopoly.game.components.streets;

import the.monopoly.game.rules.Rule;

import java.util.Set;

/**
 * A space nobody can buy: chance, community chest, jail, free parking and go to
 * jail. What landing on one does is decided by the turn resolution, not by the
 * space itself.
 */
class Unownable implements Street.Factory {
  private final Street.Kind kind;

  Unownable(Street.Kind kind) {
    this.kind = kind;
  }

  @Override
  public Street create(Street.Type type, Set<Rule> activatedRules) {
    return Street.unownable(type, kind, activatedRules);
  }
}
