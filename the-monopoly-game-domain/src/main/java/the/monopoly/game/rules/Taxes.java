package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;

/** Collects the fixed tax charged when a pawn stops on a tax space. */
public final class Taxes implements Landings {
  private final Events events;

  public Taxes(Events events) {
    this.events = events;
  }

  @Override
  public void resolve(Player player, Street space, Roll roll) {
    if (!(space instanceof TaxSpace tax)) return;

    Money amount = tax.tax();
    player.account().withdraw(amount);
    events.paidBank(player, amount);
  }

  /** What a tax payment says happened, for whoever keeps the game journal. */
  public interface Events {
    void paidBank(Player player, Money amount);
  }
}
