package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.StartSpace;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

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
  private final Jail jail;
  private final Strategy strategy;
  private final Deeds deeds;

  public Turn(Rule.Set rules, Cup cup, Events events, Landings landings) {
    this(rules, cup, events, landings, new Jail(rules), Strategy.UNDECIDED, new Deeds());
  }

  public Turn(
      Rule.Set rules, Cup cup, Events events, Landings landings,
      Jail jail, Strategy strategy, Deeds deeds
  ) {
    this.rules = rules;
    this.cup = cup;
    this.events = events;
    this.landings = landings;
    this.jail = jail;
    this.strategy = strategy;
    this.deeds = deeds;
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
    if (!jail.mayTakeTurn(player, strategy, deeds)) {
      takeFromJail(player);
      return;
    }

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

  private void takeFromJail(Player player) {
    Roll roll = cup.roll();
    events.rolled(player, roll);
    if (jail.leavesOn(roll, player)) move(player, roll);
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
    events.moved(player, from, to,
        rules.gameboard().layout().get(Math.floorMod(from, spaces)),
        rules.gameboard().layout().get(Math.floorMod(to, spaces)));

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
    jail.imprison(player);
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

    default void moved(Player player, int from, int to, Street.Type fromSpace, Street.Type toSpace) {
      moved(player, from, to);
    }

    default void collectedSalary(Player player, Money salary) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=4abc2117a96fbc416bd22d4bfd89ec97bf025cb768461393fc92bb8622931c99
scope.0.id=Y2xhc3M6VHVybiNUdXJuOjE1
scope.0.kind=class
scope.0.startLine=15
scope.0.endLine=135
scope.0.semanticHash=4f384b2e1a136700e0870153bf7b3f2f323163d221c6db1043a3e2ca9ef6e23c
scope.1.id=Y2xhc3M6VHVybi4jOjUx
scope.1.kind=class
scope.1.startLine=51
scope.1.endLine=52
scope.1.semanticHash=9265619237dc049c9efd5c01a5c74a7e99db1a67aa7ef3e2e396e54563e1bc57
scope.2.id=Y2xhc3M6VHVybi5FdmVudHMjRXZlbnRzOjEyNQ
scope.2.kind=class
scope.2.startLine=125
scope.2.endLine=134
scope.2.semanticHash=3b9b76331994f1b8000bd5ae3d4d1cd0c79771b1941d566b820f6a14cb8d24ec
scope.3.id=ZmllbGQ6VHVybiNET1VCTEVTX0FMTE9XRUQ6MTc
scope.3.kind=field
scope.3.startLine=17
scope.3.endLine=17
scope.3.semanticHash=e3ce574bfa8ae0053e0c5d980ade579177d6285215e63733feff0234bdffb9fb
scope.4.id=ZmllbGQ6VHVybiNjdXA6MjA
scope.4.kind=field
scope.4.startLine=20
scope.4.endLine=20
scope.4.semanticHash=4ae53f57002dea57cceac893a4facafbe5d9e0989268accba8cc0b9b1b70e4ae
scope.5.id=ZmllbGQ6VHVybiNkZWVkczoyNQ
scope.5.kind=field
scope.5.startLine=25
scope.5.endLine=25
scope.5.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.6.id=ZmllbGQ6VHVybiNldmVudHM6MjE
scope.6.kind=field
scope.6.startLine=21
scope.6.endLine=21
scope.6.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.7.id=ZmllbGQ6VHVybiNqYWlsOjIz
scope.7.kind=field
scope.7.startLine=23
scope.7.endLine=23
scope.7.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.8.id=ZmllbGQ6VHVybiNsYW5kaW5nczoyMg
scope.8.kind=field
scope.8.startLine=22
scope.8.endLine=22
scope.8.semanticHash=b986582c960b20ef11a30c1c2fc57e17a003904395f7bb7ae983443aa233bc11
scope.9.id=ZmllbGQ6VHVybiNydWxlczoxOQ
scope.9.kind=field
scope.9.startLine=19
scope.9.endLine=19
scope.9.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.10.id=ZmllbGQ6VHVybiNzdHJhdGVneToyNA
scope.10.kind=field
scope.10.startLine=24
scope.10.endLine=24
scope.10.semanticHash=e9ff2bf271af3ec24d609e826126fb222442539a79ad67f1f38822bc5090a537
scope.11.id=bWV0aG9kOlR1cm4jY3RvcigyKTo1MA
scope.11.kind=method
scope.11.startLine=50
scope.11.endLine=53
scope.11.semanticHash=fb33919b9e0d7878741443837d1a24ee4f14cc481b2f74ec76ad36f4986ca8e0
scope.12.id=bWV0aG9kOlR1cm4jY3RvcigzKTo0NQ
scope.12.kind=method
scope.12.startLine=45
scope.12.endLine=47
scope.12.semanticHash=1fcfda89e51fb15c84048c1bfa369e442003a6cf7d6b1c029710606a80681df6
scope.13.id=bWV0aG9kOlR1cm4jY3Rvcig0KToyNw
scope.13.kind=method
scope.13.startLine=27
scope.13.endLine=29
scope.13.semanticHash=c4e6c3c8f6ad069af916d067c239194778877bfb1d04ab8faafdd799617f29b1
scope.14.id=bWV0aG9kOlR1cm4jY3Rvcig3KTozMQ
scope.14.kind=method
scope.14.startLine=31
scope.14.endLine=42
scope.14.semanticHash=bde66b0c3d8e0798cc57d2080d30bc695bc63bff72ea3f40e0b07780628cefb3
scope.15.id=bWV0aG9kOlR1cm4jbW92ZSgyKTo4OA
scope.15.kind=method
scope.15.startLine=88
scope.15.endLine=103
scope.15.semanticHash=fb6c30f76d5af9542659edb22eacb827f65595f27de06448008da20febddbcf3
scope.16.id=bWV0aG9kOlR1cm4jc2VuZFRvSmFpbCgxKToxMTM
scope.16.kind=method
scope.16.startLine=113
scope.16.endLine=115
scope.16.semanticHash=39a3e841bb1f3cd10a3592cb66a28f9fa9ad07c5bef4936520812727aaa7f7a0
scope.17.id=bWV0aG9kOlR1cm4jc3BhY2VBdCgxKToxMDU
scope.17.kind=method
scope.17.startLine=105
scope.17.endLine=107
scope.17.semanticHash=3cf1682b5d12e12710c2220fe7a9b80855f69e8cb475fb7e7b6715a1407f7a5b
scope.18.id=bWV0aG9kOlR1cm4jc3RhcnQoMCk6MTE3
scope.18.kind=method
scope.18.startLine=117
scope.18.endLine=119
scope.18.semanticHash=afb3b0a57fbbaa1251328756f79d64399df7945610a5fc68fb8115e48ef2233e
scope.19.id=bWV0aG9kOlR1cm4jdGFrZSgxKTo1NQ
scope.19.kind=method
scope.19.startLine=55
scope.19.endLine=75
scope.19.semanticHash=bf25db68ca6199d2907937a883caddfccd19a2e7aa4dddd90117afbb47275503
scope.20.id=bWV0aG9kOlR1cm4jdGFrZUZyb21KYWlsKDEpOjc3
scope.20.kind=method
scope.20.startLine=77
scope.20.endLine=81
scope.20.semanticHash=3151fff8cefa5ccdec58e2031c542ff15679994e904903aa0e02fabef4c5e487
scope.21.id=bWV0aG9kOlR1cm4uI2N0b3IoMCk6NTE
scope.21.kind=method
scope.21.startLine=1
scope.21.endLine=135
scope.21.semanticHash=3d14705bb5dd787b0b2f712223bae49ac545a42ab02fa7d5eeefcc95d40806c7
scope.22.id=bWV0aG9kOlR1cm4uRXZlbnRzI2NvbGxlY3RlZFNhbGFyeSgyKToxMzI
scope.22.kind=method
scope.22.startLine=132
scope.22.endLine=133
scope.22.semanticHash=6cd1f36caef5236a7b98bb77c90da39864e729d6d252fdc58b623f587a32a1b6
scope.23.id=bWV0aG9kOlR1cm4uRXZlbnRzI21vdmVkKDMpOjEyOQ
scope.23.kind=method
scope.23.startLine=129
scope.23.endLine=130
scope.23.semanticHash=bf8ac6aae9c2fb39caa0da86e8d10d9a39d0fd199deb60d2ed3d2dd4326881f4
scope.24.id=bWV0aG9kOlR1cm4uRXZlbnRzI3JvbGxlZCgyKToxMjY
scope.24.kind=method
scope.24.startLine=126
scope.24.endLine=127
scope.24.semanticHash=d337b62683e09a112c3bba9b808a8165b26be5a0102e64fcddc9210746e7975c
*/
