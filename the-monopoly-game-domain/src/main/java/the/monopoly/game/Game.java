package the.monopoly.game;

import org.slf4j.Logger;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Turn;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A game being played: the rules in force, the players sitting round the board,
 * and where their rolls come from. The rules decide what happens; the game only
 * asks them, in order, and narrates the answers.
 * <p>
 * Playing moves the players it was given, the way a real game moves the pawns
 * on a real board. A game is therefore something being done rather than a value
 * to be compared or copied.
 */
public class Game {
  private final Rule.Set rules;
  private final List<Player> players;
  private final Cups cups;

  public Game(Rule.Set rules, List<Player> players, Cups cups) {
    this.rules = rules;
    this.players = players;
    this.cups = cups;
  }

  /** A game thrown with the dice the rules call for. */
  public Game(Rule.Set rules, List<Player> players) {
    this(rules, players, Cup.of(rules.dice().toList()));
  }

  /** A game played with one cup, passed round the table as turns come and go. */
  public Game(Rule.Set rules, List<Player> players, Cup cup) {
    this(rules, players, player -> cup);
  }

  public Result play() {
    var journal = new Journal();
    journal.log(new Journal.Entry.Start(ids(players)));
    List<Player> turnOrder = new Initiative(player -> initiativeRollFor(player, journal)).order(players);
    journal.log(new Journal.Entry.InitiativeWon(turnOrder.getFirst().id()));
    turnOrder.forEach(player -> takeTurn(player, journal));
    return new Result(turnOrder, journal.entries());
  }

  private int initiativeRollFor(Player player, Journal journal) {
    int total = cups.forPlayer(player).roll().total();
    journal.log(new Journal.Entry.InitiativeRoll(player.id(), total));
    return total;
  }

  private void takeTurn(Player player, Journal journal) {
    journal.log(new Journal.Entry.TurnStarted(player.id()));
    new Turn(rules, cups.forPlayer(player), new Journalling(journal)).take(player);
  }

  /** Writes down what a turn says it did, as the game's own account of it. */
  private record Journalling(Journal journal) implements Turn.Events {
    @Override
    public void rolled(Player player, Roll roll) {
      journal.log(new Journal.Entry.Rolled(player.id(), roll.total()));
    }

    @Override
    public void moved(Player player, int from, int to) {
      journal.log(new Journal.Entry.Moved(player.id(), from, to));
    }

    @Override
    public void collectedSalary(Player player, Money salary) {
      journal.log(new Journal.Entry.SalaryCollected(player.id(), salary));
    }
  }

  /**
   * Where a player's rolls come from. Everyone shares one cup at a real table,
   * but a game being replayed hands each player the rolls they are known to
   * have thrown.
   */
  @FunctionalInterface
  public interface Cups {
    Cup forPlayer(Player player);
  }

  private static List<Player.ID> ids(List<Player> players) {
    return players.stream().map(Player::id).toList();
  }

  /**
   * How the game went: the players in the order they take their turns, and the
   * account of what happened. The account is data rather than written text, so
   * that rendering it stays somebody else's job.
   */
  public record Result(List<Player> turnOrder, List<Journal.Entry> journal) {
  }

  public static class Journal {
    private static final Logger logger = getLogger(Journal.class);

    private final List<Entry> entries = new ArrayList<>();

    public void log(Entry evt) {
      entries.add(evt);
      logger.info(evt.toString());
    }

    public List<Entry> entries() {
      return List.copyOf(entries);
    }

    /**
     * What happened, as data. No entry words itself: {@link Report} decides how
     * a game reads, and sealing this is what makes it answer for every entry
     * there is.
     */
    public sealed interface Entry {
      record Start(List<Player.ID> players) implements Entry {
      }

      /** What a player threw when the table rolled to see who starts. */
      record InitiativeRoll(Player.ID player, int total) implements Entry {
      }

      record InitiativeWon(Player.ID player) implements Entry {
      }

      record TurnStarted(Player.ID player) implements Entry {
      }

      record Rolled(Player.ID player, int total) implements Entry {
      }

      record Moved(Player.ID player, int from, int to) implements Entry {
      }

      record SalaryCollected(Player.ID player, Money salary) implements Entry {
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=4e6f3a49fe9b9d1331a7d2bc48d87aae061e13c46677dc577193be4bfda316aa
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjI2
scope.0.kind=class
scope.0.startLine=26
scope.0.endLine=150
scope.0.semanticHash=dab37b57a0d2bab0cbb676d937e6f3182f7a5b1921f4c8b3d1b70af3ce486b58
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6OTA
scope.1.kind=class
scope.1.startLine=90
scope.1.endLine=93
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6MTA3
scope.2.kind=class
scope.2.startLine=107
scope.2.endLine=149
scope.2.semanticHash=73072f768f6ca5d82c0bbfb11446071173e4896c24e503d7b2c0c4f18af5e736
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjEyNg
scope.3.kind=class
scope.3.startLine=126
scope.3.endLine=148
scope.3.semanticHash=5f6368ece7db84415b1d51a31466f6668299eca8d870752072b1e87aedc20920
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjEzMQ
scope.4.kind=class
scope.4.startLine=131
scope.4.endLine=132
scope.4.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjoxMzQ
scope.5.kind=class
scope.5.startLine=134
scope.5.endLine=135
scope.5.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjE0Mw
scope.6.kind=class
scope.6.startLine=143
scope.6.endLine=144
scope.6.semanticHash=4e6b3e5a3aadbf584012feeb3f79ac0d9d7c37bd772a52948f0535fcc8469248
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6MTQw
scope.7.kind=class
scope.7.startLine=140
scope.7.endLine=141
scope.7.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6MTQ2
scope.8.kind=class
scope.8.startLine=146
scope.8.endLine=147
scope.8.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjEyNw
scope.9.kind=class
scope.9.startLine=127
scope.9.endLine=128
scope.9.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjEzNw
scope.10.kind=class
scope.10.startLine=137
scope.10.endLine=138
scope.10.semanticHash=dfc91baefb26739577f196db32ba23dd37b692bf0ff3e65056a0e06ff7cdbcc9
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsbGluZyNKb3VybmFsbGluZzo2OA
scope.11.kind=class
scope.11.startLine=68
scope.11.endLine=83
scope.11.semanticHash=86e80cfd7e554748c0670f60e588ed13ce214287c5f7f894c59c013bf419be13
scope.12.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjEwNA
scope.12.kind=class
scope.12.startLine=104
scope.12.endLine=105
scope.12.semanticHash=d74adb3f09be3933695d415b72fbb92063d0367a07806aca40dde9a6beeaa3b7
scope.13.id=ZmllbGQ6R2FtZSNjdXBzOjI5
scope.13.kind=field
scope.13.startLine=29
scope.13.endLine=29
scope.13.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.14.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjI4
scope.14.kind=field
scope.14.startLine=28
scope.14.endLine=28
scope.14.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.15.id=ZmllbGQ6R2FtZSNydWxlczoyNw
scope.15.kind=field
scope.15.startLine=27
scope.15.endLine=27
scope.15.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.16.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6MTEw
scope.16.kind=field
scope.16.startLine=110
scope.16.endLine=110
scope.16.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.17.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjoxMDg
scope.17.kind=field
scope.17.startLine=108
scope.17.endLine=108
scope.17.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.18.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjoxMzE
scope.18.kind=field
scope.18.startLine=131
scope.18.endLine=131
scope.18.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.19.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjEzMQ
scope.19.kind=field
scope.19.startLine=131
scope.19.endLine=131
scope.19.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.20.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjEzNA
scope.20.kind=field
scope.20.startLine=134
scope.20.endLine=134
scope.20.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.21.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206MTQz
scope.21.kind=field
scope.21.startLine=143
scope.21.endLine=143
scope.21.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.22.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjoxNDM
scope.22.kind=field
scope.22.startLine=143
scope.22.endLine=143
scope.22.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.23.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjE0Mw
scope.23.kind=field
scope.23.startLine=143
scope.23.endLine=143
scope.23.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.24.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6MTQw
scope.24.kind=field
scope.24.startLine=140
scope.24.endLine=140
scope.24.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.25.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDoxNDA
scope.25.kind=field
scope.25.startLine=140
scope.25.endLine=140
scope.25.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.26.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6MTQ2
scope.26.kind=field
scope.26.startLine=146
scope.26.endLine=146
scope.26.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.27.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6MTQ2
scope.27.kind=field
scope.27.startLine=146
scope.27.endLine=146
scope.27.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.28.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6MTI3
scope.28.kind=field
scope.28.startLine=127
scope.28.endLine=127
scope.28.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.29.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjoxMzc
scope.29.kind=field
scope.29.startLine=137
scope.29.endLine=137
scope.29.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.30.id=ZmllbGQ6R2FtZS5Kb3VybmFsbGluZyNqb3VybmFsOjY4
scope.30.kind=field
scope.30.startLine=68
scope.30.endLine=68
scope.30.semanticHash=85c5d503112071935eec23a4e6e40fde3bfadba06348aca287c46765762c68bf
scope.31.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDoxMDQ
scope.31.kind=field
scope.31.startLine=104
scope.31.endLine=104
scope.31.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.32.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjEwNA
scope.32.kind=field
scope.32.startLine=104
scope.32.endLine=104
scope.32.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.33.id=bWV0aG9kOkdhbWUjY3RvcigyKTozOA
scope.33.kind=method
scope.33.startLine=38
scope.33.endLine=40
scope.33.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.34.id=bWV0aG9kOkdhbWUjY3RvcigzKTozMQ
scope.34.kind=method
scope.34.startLine=31
scope.34.endLine=35
scope.34.semanticHash=b939900b9cb3d79701b68ca7feb968f1f9e79b1b06e1ceaac62356d04694c432
scope.35.id=bWV0aG9kOkdhbWUjY3RvcigzKTo0Mw
scope.35.kind=method
scope.35.startLine=43
scope.35.endLine=45
scope.35.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.36.id=bWV0aG9kOkdhbWUjaWRzKDEpOjk1
scope.36.kind=method
scope.36.startLine=95
scope.36.endLine=97
scope.36.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.37.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6NTY
scope.37.kind=method
scope.37.startLine=56
scope.37.endLine=60
scope.37.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.38.id=bWV0aG9kOkdhbWUjcGxheSgwKTo0Nw
scope.38.kind=method
scope.38.startLine=47
scope.38.endLine=54
scope.38.semanticHash=5db0d31de87648684f2d31f2cf977f394aec9ef61933639ed1639fb78252bafe
scope.39.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oMik6NjI
scope.39.kind=method
scope.39.startLine=62
scope.39.endLine=65
scope.39.semanticHash=715a42c4dc121765793f7566d72ca2852933dc5c281d0a75be4849c28d198c66
scope.40.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6OTI
scope.40.kind=method
scope.40.startLine=92
scope.40.endLine=92
scope.40.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.41.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjEwNw
scope.41.kind=method
scope.41.startLine=1
scope.41.endLine=150
scope.41.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.42.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjExNw
scope.42.kind=method
scope.42.startLine=117
scope.42.endLine=119
scope.42.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.43.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6MTEy
scope.43.kind=method
scope.43.startLine=112
scope.43.endLine=115
scope.43.semanticHash=b5421e8cb3d5bc8c502ce4d321cdeecd90548b5faa054fb99b324edef550d319
scope.44.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjEzMQ
scope.44.kind=method
scope.44.startLine=1
scope.44.endLine=150
scope.44.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.45.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6MTM0
scope.45.kind=method
scope.45.startLine=1
scope.45.endLine=150
scope.45.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.46.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjE0Mw
scope.46.kind=method
scope.46.startLine=1
scope.46.endLine=150
scope.46.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.47.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKToxNDA
scope.47.kind=method
scope.47.startLine=1
scope.47.endLine=150
scope.47.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.48.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKToxNDY
scope.48.kind=method
scope.48.startLine=1
scope.48.endLine=150
scope.48.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.49.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjEyNw
scope.49.kind=method
scope.49.startLine=1
scope.49.endLine=150
scope.49.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.50.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDEpOjEzNw
scope.50.kind=method
scope.50.startLine=1
scope.50.endLine=150
scope.50.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.51.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY29sbGVjdGVkU2FsYXJ5KDIpOjc5
scope.51.kind=method
scope.51.startLine=79
scope.51.endLine=82
scope.51.semanticHash=9d31c851d99e8df553fdaf39330dc1ae11e0fe903b61f6b97b858c59389d5411
scope.52.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY3RvcigxKTo2OA
scope.52.kind=method
scope.52.startLine=1
scope.52.endLine=150
scope.52.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
scope.53.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW92ZWQoMyk6NzQ
scope.53.kind=method
scope.53.startLine=74
scope.53.endLine=77
scope.53.semanticHash=624e9fcd1bcf33ec8f097872d5cf6e59f4e27c7ca7a686a5b60d818f937efc7b
scope.54.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcm9sbGVkKDIpOjY5
scope.54.kind=method
scope.54.startLine=69
scope.54.endLine=72
scope.54.semanticHash=41570b54acd7a2a0ee9f75bcefcdd79248dd7db4784b77cea66150ac4e5a790f
scope.55.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoMik6MTA0
scope.55.kind=method
scope.55.startLine=1
scope.55.endLine=150
scope.55.semanticHash=e29e62f1bf859a50d8e8a823aaf63847158e462e3b8ea1cf9dc4cb850d763fad
*/
