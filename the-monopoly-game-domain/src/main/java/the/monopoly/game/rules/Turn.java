package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
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
  private final Events events;

  public Turn(Rule.Set rules, Cup cup, Events events) {
    this.rules = rules;
    this.cup = cup;
    this.events = events;
  }

  /** A turn nobody is keeping an account of. */
  public Turn(Rule.Set rules, Cup cup) {
    this(rules, cup, new Events() {
    });
  }

  public void take(Player player) {
    int doubles = 0;
    while (true) {
      Roll roll = cup.roll();
      events.rolled(player, roll);

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

    player.position().moveTo(to);
    events.moved(player, from, to);

    if (from + steps >= spaces) {
      if (to == 0) events.collectedSalary(player, player.land(start()));
      else events.collectedSalary(player, player.pass(start()));
    }
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

  /**
   * What a turn did, told as it happens, for whoever is keeping an account of
   * the game. A turn that is not being watched does nothing differently.
   */
  public interface Events {
    default void rolled(Player player, Roll roll) {
    }

    default void moved(Player player, int from, int to) {
    }

    default void collectedSalary(Player player, Money salary) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=af09d3ccb1e7f7e38e80be00ed413e90498f1ac11386c0c716e9c3bcf9c8622a
scope.0.id=Y2xhc3M6VHVybiNUdXJuOjEz
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=76
scope.0.semanticHash=7fb55620bde77509f74d45b03c901c30019854e968e2d3f20b26d495009fcee8
scope.1.id=ZmllbGQ6VHVybiNET1VCTEVTX0FMTE9XRUQ6MTU
scope.1.kind=field
scope.1.startLine=15
scope.1.endLine=15
scope.1.semanticHash=e3ce574bfa8ae0053e0c5d980ade579177d6285215e63733feff0234bdffb9fb
scope.2.id=ZmllbGQ6VHVybiNjdXA6MTg
scope.2.kind=field
scope.2.startLine=18
scope.2.endLine=18
scope.2.semanticHash=4ae53f57002dea57cceac893a4facafbe5d9e0989268accba8cc0b9b1b70e4ae
scope.3.id=ZmllbGQ6VHVybiNydWxlczoxNw
scope.3.kind=field
scope.3.startLine=17
scope.3.endLine=17
scope.3.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.4.id=bWV0aG9kOlR1cm4jY3RvcigyKToyMA
scope.4.kind=method
scope.4.startLine=20
scope.4.endLine=23
scope.4.semanticHash=de7531d5f7f7550bc99964e0e188d9b2986c658039d328be87b76c0ed46147d2
scope.5.id=bWV0aG9kOlR1cm4jbW92ZSgyKTo0Ng
scope.5.kind=method
scope.5.startLine=46
scope.5.endLine=57
scope.5.semanticHash=6a750a9d564920b0840c221db1172a769ee69eb6b4c89ce75c60a84261e6cc43
scope.6.id=bWV0aG9kOlR1cm4jcG9zaXRpb25PZigxKTo2Nw
scope.6.kind=method
scope.6.startLine=67
scope.6.endLine=71
scope.6.semanticHash=e67ea0972577b6aa3d47992ef7e228c62e1c045599156e70c82f08a9fd391a9f
scope.7.id=bWV0aG9kOlR1cm4jc2VuZFRvSmFpbCgxKTo2Mw
scope.7.kind=method
scope.7.startLine=63
scope.7.endLine=65
scope.7.semanticHash=49c0402499402ae5e3a78b3bc2c30e48d02c80270b8ce1cca831b90878c3c3fe
scope.8.id=bWV0aG9kOlR1cm4jc3RhcnQoMCk6NzM
scope.8.kind=method
scope.8.startLine=73
scope.8.endLine=75
scope.8.semanticHash=afb3b0a57fbbaa1251328756f79d64399df7945610a5fc68fb8115e48ef2233e
scope.9.id=bWV0aG9kOlR1cm4jdGFrZSgxKToyNQ
scope.9.kind=method
scope.9.startLine=25
scope.9.endLine=39
scope.9.semanticHash=5dd0880f5af3b1690cda88dea45b7bef54ceec22c639a28de2f2feea07cacab2
*/
