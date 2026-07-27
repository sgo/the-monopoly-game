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

import static java.lang.String.join;
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

    public sealed interface Entry {
      record Start(List<Player.ID> players) implements Entry {
        @Override
        public String toString() {
          return """
              Start game with players:
              - %s
              """.formatted(namesOf(players));
        }
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

      private static String namesOf(List<Player.ID> players) {
        return join("\n- ", players.stream().map(Player.ID::value).toList());
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=80da6f2d976799685f01819aa9425c78ffbf436aa9fb10487a9d4724c001f3ab
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjI1
scope.0.kind=class
scope.0.startLine=25
scope.0.endLine=121
scope.0.semanticHash=e75f241e06bddc28f8cf7bca3fa0bb82fc7b93e978d862dd2cacaed9faebfaa2
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6NjQ
scope.1.kind=class
scope.1.startLine=64
scope.1.endLine=67
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6ODE
scope.2.kind=class
scope.2.startLine=81
scope.2.endLine=120
scope.2.semanticHash=9ffb81d3d5504ba2c02b022642a90169b6bbd820e9b20e53c917cff9a3fb1b46
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5Ojk1
scope.3.kind=class
scope.3.startLine=95
scope.3.endLine=119
scope.3.semanticHash=f0b74b50c13f95b5d0aeabf1019810d0f65d3369363ae4e19d2366b1a1e5fdcf
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0Ojk2
scope.4.kind=class
scope.4.startLine=96
scope.4.endLine=104
scope.4.semanticHash=57dfadc1955e0f5a50a56923f5cf693ecf9b55c4d1642a985cf365a2023ca9fd
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5PcmRlciNUdXJuT3JkZXI6MTA2
scope.5.kind=class
scope.5.startLine=106
scope.5.endLine=114
scope.5.semanticHash=97a939c3dc9fb4f26b89beed0583f7ca8bb1fd1098c1a49e50d35768125ba629
scope.6.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0Ojc4
scope.6.kind=class
scope.6.startLine=78
scope.6.endLine=79
scope.6.semanticHash=d74adb3f09be3933695d415b72fbb92063d0367a07806aca40dde9a6beeaa3b7
scope.7.id=ZmllbGQ6R2FtZSNjdXBzOjI4
scope.7.kind=field
scope.7.startLine=28
scope.7.endLine=28
scope.7.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.8.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjI3
scope.8.kind=field
scope.8.startLine=27
scope.8.endLine=27
scope.8.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.9.id=ZmllbGQ6R2FtZSNydWxlczoyNg
scope.9.kind=field
scope.9.startLine=26
scope.9.endLine=26
scope.9.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.10.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6ODQ
scope.10.kind=field
scope.10.startLine=84
scope.10.endLine=84
scope.10.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.11.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjo4Mg
scope.11.kind=field
scope.11.startLine=82
scope.11.endLine=82
scope.11.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.12.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6OTY
scope.12.kind=field
scope.12.startLine=96
scope.12.endLine=96
scope.12.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.13.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5PcmRlciNwbGF5ZXJzOjEwNg
scope.13.kind=field
scope.13.startLine=106
scope.13.endLine=106
scope.13.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.14.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDo3OA
scope.14.kind=field
scope.14.startLine=78
scope.14.endLine=78
scope.14.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.15.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjc4
scope.15.kind=field
scope.15.startLine=78
scope.15.endLine=78
scope.15.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.16.id=bWV0aG9kOkdhbWUjY3RvcigyKTozNw
scope.16.kind=method
scope.16.startLine=37
scope.16.endLine=39
scope.16.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.17.id=bWV0aG9kOkdhbWUjY3RvcigzKTozMA
scope.17.kind=method
scope.17.startLine=30
scope.17.endLine=34
scope.17.semanticHash=b939900b9cb3d79701b68ca7feb968f1f9e79b1b06e1ceaac62356d04694c432
scope.18.id=bWV0aG9kOkdhbWUjY3RvcigzKTo0Mg
scope.18.kind=method
scope.18.startLine=42
scope.18.endLine=44
scope.18.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.19.id=bWV0aG9kOkdhbWUjaWRzKDEpOjY5
scope.19.kind=method
scope.19.startLine=69
scope.19.endLine=71
scope.19.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.20.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMSk6NTU
scope.20.kind=method
scope.20.startLine=55
scope.20.endLine=57
scope.20.semanticHash=2d5e7ab2ef37ea5fc8f4904962cf49af9934b0ea0282d61ba57ccd892169df3c
scope.21.id=bWV0aG9kOkdhbWUjcGxheSgwKTo0Ng
scope.21.kind=method
scope.21.startLine=46
scope.21.endLine=53
scope.21.semanticHash=67aa603ec551fcf8269a994daa37d9f4dcd7265882c16cb5ef3ac859a597e993
scope.22.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6NjY
scope.22.kind=method
scope.22.startLine=66
scope.22.endLine=66
scope.22.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.23.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjgx
scope.23.kind=method
scope.23.startLine=1
scope.23.endLine=121
scope.23.semanticHash=e7c067cf4d4cbdf8aedcec5ebe50becc09e0634c488479976178d13211ccfacf
scope.24.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjkx
scope.24.kind=method
scope.24.startLine=91
scope.24.endLine=93
scope.24.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.25.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6ODY
scope.25.kind=method
scope.25.startLine=86
scope.25.endLine=89
scope.25.semanticHash=b5421e8cb3d5bc8c502ce4d321cdeecd90548b5faa054fb99b324edef550d319
scope.26.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeSNuYW1lc09mKDEpOjExNg
scope.26.kind=method
scope.26.startLine=116
scope.26.endLine=118
scope.26.semanticHash=2360bc51922a515df816193f3319ed6c9e8d1f7d378c7dfd626c482198f39beb
scope.27.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjk2
scope.27.kind=method
scope.27.startLine=1
scope.27.endLine=121
scope.27.semanticHash=e7c067cf4d4cbdf8aedcec5ebe50becc09e0634c488479976178d13211ccfacf
scope.28.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCN0b1N0cmluZygwKTo5Nw
scope.28.kind=method
scope.28.startLine=97
scope.28.endLine=103
scope.28.semanticHash=79cb8d5544397a6467d440acaaf7f5727fe0aef69149421b27d3bb5515a14fe6
scope.29.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuT3JkZXIjY3RvcigxKToxMDY
scope.29.kind=method
scope.29.startLine=1
scope.29.endLine=121
scope.29.semanticHash=e7c067cf4d4cbdf8aedcec5ebe50becc09e0634c488479976178d13211ccfacf
scope.30.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuT3JkZXIjdG9TdHJpbmcoMCk6MTA3
scope.30.kind=method
scope.30.startLine=107
scope.30.endLine=113
scope.30.semanticHash=cd43f11a76bad588897c7e4158de7961e992b255054259e2fbd12f0849e62172
scope.31.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoMik6Nzg
scope.31.kind=method
scope.31.startLine=1
scope.31.endLine=121
scope.31.semanticHash=e7c067cf4d4cbdf8aedcec5ebe50becc09e0634c488479976178d13211ccfacf
*/
