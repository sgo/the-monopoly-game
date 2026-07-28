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
  private final Landings landings;

  public Turn(Rule.Set rules, Cup cup, Events events, Landings landings) {
    this.rules = rules;
    this.cup = cup;
    this.events = events;
    this.landings = landings;
  }

  /** A turn played where stopping on a space is worth nothing. */
  public Turn(Rule.Set rules, Cup cup, Events events) {
    this(rules, cup, events, Landings.UNEVENTFUL);
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

    landings.resolve(player, spaceAt(to));
  }

  private Street spaceAt(int position) {
    return rules.create(rules.gameboard().layout().get(position));
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
moduleHash=53dc1a7d50f53c2a2262047e909cc07b99205653c9ba0e7ada0b34d2b3273719
scope.0.id=Y2xhc3M6VHVybiNUdXJuOjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=102
scope.0.semanticHash=ad53fb506583cc47b8f6ed25ea6e17b2926f7200b9aab48d9486dcf2c89a21d4
scope.1.id=Y2xhc3M6VHVybi4jOjMw
scope.1.kind=class
scope.1.startLine=30
scope.1.endLine=31
scope.1.semanticHash=9265619237dc049c9efd5c01a5c74a7e99db1a67aa7ef3e2e396e54563e1bc57
scope.2.id=Y2xhc3M6VHVybi5FdmVudHMjRXZlbnRzOjky
scope.2.kind=class
scope.2.startLine=92
scope.2.endLine=101
scope.2.semanticHash=3b9b76331994f1b8000bd5ae3d4d1cd0c79771b1941d566b820f6a14cb8d24ec
scope.3.id=ZmllbGQ6VHVybiNET1VCTEVTX0FMTE9XRUQ6MTY
scope.3.kind=field
scope.3.startLine=16
scope.3.endLine=16
scope.3.semanticHash=e3ce574bfa8ae0053e0c5d980ade579177d6285215e63733feff0234bdffb9fb
scope.4.id=ZmllbGQ6VHVybiNjdXA6MTk
scope.4.kind=field
scope.4.startLine=19
scope.4.endLine=19
scope.4.semanticHash=4ae53f57002dea57cceac893a4facafbe5d9e0989268accba8cc0b9b1b70e4ae
scope.5.id=ZmllbGQ6VHVybiNldmVudHM6MjA
scope.5.kind=field
scope.5.startLine=20
scope.5.endLine=20
scope.5.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.6.id=ZmllbGQ6VHVybiNydWxlczoxOA
scope.6.kind=field
scope.6.startLine=18
scope.6.endLine=18
scope.6.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.7.id=bWV0aG9kOlR1cm4jY3RvcigyKToyOQ
scope.7.kind=method
scope.7.startLine=29
scope.7.endLine=32
scope.7.semanticHash=fb33919b9e0d7878741443837d1a24ee4f14cc481b2f74ec76ad36f4986ca8e0
scope.8.id=bWV0aG9kOlR1cm4jY3RvcigzKToyMg
scope.8.kind=method
scope.8.startLine=22
scope.8.endLine=26
scope.8.semanticHash=fefa3ef6a3d66abba1a9fbafe4a0e6b426f0d73904a07acb5b21f4a4b7c2ccdd
scope.9.id=bWV0aG9kOlR1cm4jbW92ZSgyKTo1Ng
scope.9.kind=method
scope.9.startLine=56
scope.9.endLine=68
scope.9.semanticHash=1988892e271edd5224b4319e5d239df15dc2a277c0b27eeefeb3f75621b32d5d
scope.10.id=bWV0aG9kOlR1cm4jcG9zaXRpb25PZigxKTo3OA
scope.10.kind=method
scope.10.startLine=78
scope.10.endLine=82
scope.10.semanticHash=e67ea0972577b6aa3d47992ef7e228c62e1c045599156e70c82f08a9fd391a9f
scope.11.id=bWV0aG9kOlR1cm4jc2VuZFRvSmFpbCgxKTo3NA
scope.11.kind=method
scope.11.startLine=74
scope.11.endLine=76
scope.11.semanticHash=49c0402499402ae5e3a78b3bc2c30e48d02c80270b8ce1cca831b90878c3c3fe
scope.12.id=bWV0aG9kOlR1cm4jc3RhcnQoMCk6ODQ
scope.12.kind=method
scope.12.startLine=84
scope.12.endLine=86
scope.12.semanticHash=afb3b0a57fbbaa1251328756f79d64399df7945610a5fc68fb8115e48ef2233e
scope.13.id=bWV0aG9kOlR1cm4jdGFrZSgxKTozNA
scope.13.kind=method
scope.13.startLine=34
scope.13.endLine=49
scope.13.semanticHash=e95168b705564850a28e8ae9c083bef5ea82d7662874bd560bd5b9bf04742d39
scope.14.id=bWV0aG9kOlR1cm4uI2N0b3IoMCk6MzA
scope.14.kind=method
scope.14.startLine=1
scope.14.endLine=102
scope.14.semanticHash=404e6eb86fbb0892a553332edf10442430e1875b4352241ac5a2b9bd042a7b0b
scope.15.id=bWV0aG9kOlR1cm4uRXZlbnRzI2NvbGxlY3RlZFNhbGFyeSgyKTo5OQ
scope.15.kind=method
scope.15.startLine=99
scope.15.endLine=100
scope.15.semanticHash=6cd1f36caef5236a7b98bb77c90da39864e729d6d252fdc58b623f587a32a1b6
scope.16.id=bWV0aG9kOlR1cm4uRXZlbnRzI21vdmVkKDMpOjk2
scope.16.kind=method
scope.16.startLine=96
scope.16.endLine=97
scope.16.semanticHash=bf8ac6aae9c2fb39caa0da86e8d10d9a39d0fd199deb60d2ed3d2dd4326881f4
scope.17.id=bWV0aG9kOlR1cm4uRXZlbnRzI3JvbGxlZCgyKTo5Mw
scope.17.kind=method
scope.17.startLine=93
scope.17.endLine=94
scope.17.semanticHash=d337b62683e09a112c3bba9b808a8165b26be5a0102e64fcddc9210746e7975c
*/
