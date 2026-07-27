package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.StartSpace;
import the.monopoly.game.components.streets.Street;

/**
 * One player's turn: roll, move, resolve where the pawn landed, and roll again
 * while the dice keep coming up the same.
 */
public class Turn {
  /** Rolling this many doubles in a row is taken as cheating, and is jailed. */
  private static final int DOUBLES_ALLOWED = 2;

  private final Rule.Set rules;
  private final Cup cup;

  public Turn(Rule.Set rules, Cup cup) {
    this.rules = rules;
    this.cup = cup;
  }

  public void take(Player player) {
    int doubles = 0;
    while (true) {
      Roll roll = cup.roll();

      if (roll.isDouble() && ++doubles > DOUBLES_ALLOWED) {
        sendToJail(player);
        return;
      }

      move(player, roll.total());

      if (!roll.isDouble()) return;
    }
  }

  /**
   * Moves the pawn on around the board, paying the salary for reaching Start.
   * Landing exactly on Start is not the same as going past it, because the
   * optional rule pays double for one and not the other.
   */
  private void move(Player player, int steps) {
    int spaces = rules.gameboard().size();
    int from = player.position().index();
    int to = (from + steps) % spaces;

    if (to == 0) player.land(start());
    else if (from + steps >= spaces) player.pass(start());

    player.position().moveTo(to);
  }

  /**
   * Going to jail is not a move around the board, so the pawn passes nothing on
   * the way and is paid for nothing.
   */
  private void sendToJail(Player player) {
    player.position().moveTo(positionOf(Street.Type.OpBezoek));
  }

  private int positionOf(Street.Type space) {
    int at = rules.gameboard().layout().indexOf(space);
    if (at < 0) throw new IllegalStateException("This board has no " + space + " space.");
    return at;
  }

  private StartSpace start() {
    return (StartSpace) rules.create(Street.Type.start);
  }
}
