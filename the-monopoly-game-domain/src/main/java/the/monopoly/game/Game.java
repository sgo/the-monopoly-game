package the.monopoly.game;

import org.slf4j.Logger;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Turn;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.join;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A game being played: the rules in force, the players sitting round the board,
 * and where their rolls come from. The rules decide what happens; the game only
 * asks them, in order, and narrates the answers.
 */
public record Game(Rule.Set rules, List<Player> players, Cups cups) {
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
    List<Player> turnOrder = new Initiative(this::initiativeRollFor).order(players);
    journal.log(new Journal.Entry.TurnOrder(ids(turnOrder)));
    turnOrder.forEach(player -> new Turn(rules, cups.forPlayer(player)).take(player));
    return new Result(turnOrder, journal.entries());
  }

  private int initiativeRollFor(Player player) {
    return cups.forPlayer(player).roll().total();
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

    public interface Entry {
      record Start(List<Player.ID> players) implements Entry {
        @Override
        public String toString() {
          return """
              Start game with players:
              - %s
              """.formatted(namesOf(players));
        }
      }

      record TurnOrder(List<Player.ID> players) implements Entry {
        @Override
        public String toString() {
          return """
              Turn order:
              - %s
              """.formatted(namesOf(players));
        }
      }

      private static String namesOf(List<Player.ID> players) {
        return join("\n- ", players.stream().map(Player.ID::value).toList());
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=a20df7cc025989985e3b48eec31bb1cd43f9d5e1dea45bad22572e31f269f4fb
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjIw
scope.0.kind=class
scope.0.startLine=20
scope.0.endLine=86
scope.0.semanticHash=506523cda606bb66d8ec0a812a24461a66c1e53d5a8118e6bfe3ef5253605474
scope.1.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6NDY
scope.1.kind=class
scope.1.startLine=46
scope.1.endLine=85
scope.1.semanticHash=9ffb81d3d5504ba2c02b022642a90169b6bbd820e9b20e53c917cff9a3fb1b46
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjYw
scope.2.kind=class
scope.2.startLine=60
scope.2.endLine=84
scope.2.semanticHash=f0b74b50c13f95b5d0aeabf1019810d0f65d3369363ae4e19d2366b1a1e5fdcf
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjYx
scope.3.kind=class
scope.3.startLine=61
scope.3.endLine=69
scope.3.semanticHash=57dfadc1955e0f5a50a56923f5cf693ecf9b55c4d1642a985cf365a2023ca9fd
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5PcmRlciNUdXJuT3JkZXI6NzE
scope.4.kind=class
scope.4.startLine=71
scope.4.endLine=79
scope.4.semanticHash=97a939c3dc9fb4f26b89beed0583f7ca8bb1fd1098c1a49e50d35768125ba629
scope.5.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjQz
scope.5.kind=class
scope.5.startLine=43
scope.5.endLine=44
scope.5.semanticHash=d74adb3f09be3933695d415b72fbb92063d0367a07806aca40dde9a6beeaa3b7
scope.6.id=ZmllbGQ6R2FtZSNjdXA6MjA
scope.6.kind=field
scope.6.startLine=20
scope.6.endLine=20
scope.6.semanticHash=fc950a24ba0c2bbc3f59a6f71aa82f70bb33c4d98f50da1b8066e98556c0051f
scope.7.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjIw
scope.7.kind=field
scope.7.startLine=20
scope.7.endLine=20
scope.7.semanticHash=ae5d2e8c41de41fca338532c751a7a679eafcb486860ea5a70036947cc37c4e0
scope.8.id=ZmllbGQ6R2FtZSNydWxlczoyMA
scope.8.kind=field
scope.8.startLine=20
scope.8.endLine=20
scope.8.semanticHash=ed497e01a36cc45680984842a5fb0537b4670b1599b4b4bccf1b2a2f0105dcbb
scope.9.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6NDk
scope.9.kind=field
scope.9.startLine=49
scope.9.endLine=49
scope.9.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.10.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjo0Nw
scope.10.kind=field
scope.10.startLine=47
scope.10.endLine=47
scope.10.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.11.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6NjE
scope.11.kind=field
scope.11.startLine=61
scope.11.endLine=61
scope.11.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.12.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5PcmRlciNwbGF5ZXJzOjcx
scope.12.kind=field
scope.12.startLine=71
scope.12.endLine=71
scope.12.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.13.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDo0Mw
scope.13.kind=field
scope.13.startLine=43
scope.13.endLine=43
scope.13.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.14.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjQz
scope.14.kind=field
scope.14.startLine=43
scope.14.endLine=43
scope.14.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.15.id=bWV0aG9kOkdhbWUjY3RvcigyKToyMg
scope.15.kind=method
scope.15.startLine=22
scope.15.endLine=24
scope.15.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.16.id=bWV0aG9kOkdhbWUjY3RvcigzKToyMA
scope.16.kind=method
scope.16.startLine=1
scope.16.endLine=86
scope.16.semanticHash=b9ea899a500b9abf2b1f9fbb8d00bf3e3bba661d535e57b79110b5ed62256d11
scope.17.id=bWV0aG9kOkdhbWUjaWRzKDEpOjM0
scope.17.kind=method
scope.17.startLine=34
scope.17.endLine=36
scope.17.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.18.id=bWV0aG9kOkdhbWUjcGxheSgwKToyNg
scope.18.kind=method
scope.18.startLine=26
scope.18.endLine=32
scope.18.semanticHash=7b7bc5d33f8c4dea269ee0d12520254785842bc2bde982d808aa2998234129db
scope.19.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjQ2
scope.19.kind=method
scope.19.startLine=1
scope.19.endLine=86
scope.19.semanticHash=b9ea899a500b9abf2b1f9fbb8d00bf3e3bba661d535e57b79110b5ed62256d11
scope.20.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjU2
scope.20.kind=method
scope.20.startLine=56
scope.20.endLine=58
scope.20.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.21.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6NTE
scope.21.kind=method
scope.21.startLine=51
scope.21.endLine=54
scope.21.semanticHash=b5421e8cb3d5bc8c502ce4d321cdeecd90548b5faa054fb99b324edef550d319
scope.22.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeSNuYW1lc09mKDEpOjgx
scope.22.kind=method
scope.22.startLine=81
scope.22.endLine=83
scope.22.semanticHash=2360bc51922a515df816193f3319ed6c9e8d1f7d378c7dfd626c482198f39beb
scope.23.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjYx
scope.23.kind=method
scope.23.startLine=1
scope.23.endLine=86
scope.23.semanticHash=b9ea899a500b9abf2b1f9fbb8d00bf3e3bba661d535e57b79110b5ed62256d11
scope.24.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCN0b1N0cmluZygwKTo2Mg
scope.24.kind=method
scope.24.startLine=62
scope.24.endLine=68
scope.24.semanticHash=79cb8d5544397a6467d440acaaf7f5727fe0aef69149421b27d3bb5515a14fe6
scope.25.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuT3JkZXIjY3RvcigxKTo3MQ
scope.25.kind=method
scope.25.startLine=1
scope.25.endLine=86
scope.25.semanticHash=b9ea899a500b9abf2b1f9fbb8d00bf3e3bba661d535e57b79110b5ed62256d11
scope.26.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuT3JkZXIjdG9TdHJpbmcoMCk6NzI
scope.26.kind=method
scope.26.startLine=72
scope.26.endLine=78
scope.26.semanticHash=cd43f11a76bad588897c7e4158de7961e992b255054259e2fbd12f0849e62172
scope.27.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoMik6NDM
scope.27.kind=method
scope.27.startLine=1
scope.27.endLine=86
scope.27.semanticHash=b9ea899a500b9abf2b1f9fbb8d00bf3e3bba661d535e57b79110b5ed62256d11
*/
