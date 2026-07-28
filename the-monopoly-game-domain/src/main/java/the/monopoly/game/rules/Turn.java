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
    for (;;) {
      Roll roll = cup.roll();
      events.rolled(player, roll);

      if (roll.isDouble() && ++doubles > DOUBLES_ALLOWED) {
        sendToJail(player);
        return;
      }

      move(player, roll);

      if (!roll.isDouble()) return;
    }
  }

  /**
   * Moves the pawn on around the board, paying the salary for reaching Start.
   * Landing exactly on Start is not the same as going past it, because the
   * optional rule pays double for one and not the other.
   */
  private void move(Player player, Roll roll) {
    int steps = roll.total();
    int spaces = rules.gameboard().size();
    int from = player.position().index();
    int to = (from + steps) % spaces;

    player.position().moveTo(to);
    events.moved(player, from, to);

    if (from + steps >= spaces) {
      if (to == 0) events.collectedSalary(player, player.land(start()));
      else events.collectedSalary(player, player.pass(start()));
    }

    landings.resolve(player, spaceAt(to), roll);
  }

  private Street spaceAt(int position) {
    return rules.create(rules.gameboard().layout().get(position));
  }

  /**
   * Going to jail is not a move around the board, so the pawn passes nothing on
   * the way and is paid for nothing.
   */
  private void sendToJail(Player player) {
    player.position().moveTo(rules.gameboard().positionOf(Street.Type.OpBezoek));
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
moduleHash=198b9093d280987552d99f5bf418d155c7a853a5eb9d3a3e94bc71d19fa8e187
scope.0.id=Y2xhc3M6VHVybiNUdXJuOjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=110
scope.0.semanticHash=0e97a3ceb02377f4b4ef66cb6eac169647ea1e10a2b04662891e51a59828c137
scope.1.id=Y2xhc3M6VHVybi4jOjM3
scope.1.kind=class
scope.1.startLine=37
scope.1.endLine=38
scope.1.semanticHash=9265619237dc049c9efd5c01a5c74a7e99db1a67aa7ef3e2e396e54563e1bc57
scope.2.id=Y2xhc3M6VHVybi5FdmVudHMjRXZlbnRzOjEwMA
scope.2.kind=class
scope.2.startLine=100
scope.2.endLine=109
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
scope.6.id=ZmllbGQ6VHVybiNsYW5kaW5nczoyMQ
scope.6.kind=field
scope.6.startLine=21
scope.6.endLine=21
scope.6.semanticHash=b986582c960b20ef11a30c1c2fc57e17a003904395f7bb7ae983443aa233bc11
scope.7.id=ZmllbGQ6VHVybiNydWxlczoxOA
scope.7.kind=field
scope.7.startLine=18
scope.7.endLine=18
scope.7.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.8.id=bWV0aG9kOlR1cm4jY3RvcigyKTozNg
scope.8.kind=method
scope.8.startLine=36
scope.8.endLine=39
scope.8.semanticHash=fb33919b9e0d7878741443837d1a24ee4f14cc481b2f74ec76ad36f4986ca8e0
scope.9.id=bWV0aG9kOlR1cm4jY3RvcigzKTozMQ
scope.9.kind=method
scope.9.startLine=31
scope.9.endLine=33
scope.9.semanticHash=1fcfda89e51fb15c84048c1bfa369e442003a6cf7d6b1c029710606a80681df6
scope.10.id=bWV0aG9kOlR1cm4jY3Rvcig0KToyMw
scope.10.kind=method
scope.10.startLine=23
scope.10.endLine=28
scope.10.semanticHash=022bb488dd3bd0a584b990ac8e4a5998372c0380cf834c395134a88ce6d36b0b
scope.11.id=bWV0aG9kOlR1cm4jbW92ZSgyKTo2Mw
scope.11.kind=method
scope.11.startLine=63
scope.11.endLine=78
scope.11.semanticHash=fb6c30f76d5af9542659edb22eacb827f65595f27de06448008da20febddbcf3
scope.12.id=bWV0aG9kOlR1cm4jc2VuZFRvSmFpbCgxKTo4OA
scope.12.kind=method
scope.12.startLine=88
scope.12.endLine=90
scope.12.semanticHash=4ba38fd0a7d71766911e04202b79cc0fcc7f22ec217e679b11c6eb5a71f96598
scope.13.id=bWV0aG9kOlR1cm4jc3BhY2VBdCgxKTo4MA
scope.13.kind=method
scope.13.startLine=80
scope.13.endLine=82
scope.13.semanticHash=3cf1682b5d12e12710c2220fe7a9b80855f69e8cb475fb7e7b6715a1407f7a5b
scope.14.id=bWV0aG9kOlR1cm4jc3RhcnQoMCk6OTI
scope.14.kind=method
scope.14.startLine=92
scope.14.endLine=94
scope.14.semanticHash=afb3b0a57fbbaa1251328756f79d64399df7945610a5fc68fb8115e48ef2233e
scope.15.id=bWV0aG9kOlR1cm4jdGFrZSgxKTo0MQ
scope.15.kind=method
scope.15.startLine=41
scope.15.endLine=56
scope.15.semanticHash=cb3bd42acc798ee40c4f220101122cca8b557377115d43cec0728a0c85b07076
scope.16.id=bWV0aG9kOlR1cm4uI2N0b3IoMCk6Mzc
scope.16.kind=method
scope.16.startLine=1
scope.16.endLine=110
scope.16.semanticHash=b6a539b45ed6f7758257233ef2208a099232f6b9e89f1d9fe402c7df93b9d350
scope.17.id=bWV0aG9kOlR1cm4uRXZlbnRzI2NvbGxlY3RlZFNhbGFyeSgyKToxMDc
scope.17.kind=method
scope.17.startLine=107
scope.17.endLine=108
scope.17.semanticHash=6cd1f36caef5236a7b98bb77c90da39864e729d6d252fdc58b623f587a32a1b6
scope.18.id=bWV0aG9kOlR1cm4uRXZlbnRzI21vdmVkKDMpOjEwNA
scope.18.kind=method
scope.18.startLine=104
scope.18.endLine=105
scope.18.semanticHash=bf8ac6aae9c2fb39caa0da86e8d10d9a39d0fd199deb60d2ed3d2dd4326881f4
scope.19.id=bWV0aG9kOlR1cm4uRXZlbnRzI3JvbGxlZCgyKToxMDE
scope.19.kind=method
scope.19.startLine=101
scope.19.endLine=102
scope.19.semanticHash=d337b62683e09a112c3bba9b808a8165b26be5a0102e64fcddc9210746e7975c
*/
