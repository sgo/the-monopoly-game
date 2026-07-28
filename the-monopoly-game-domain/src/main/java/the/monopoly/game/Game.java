package the.monopoly.game;

import org.slf4j.Logger;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.LandSale;
import the.monopoly.game.rules.Landings;
import the.monopoly.game.rules.Rent;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Turn;
import the.monopoly.game.strategies.Strategy;

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
  private final Strategy.OfPlayers strategies;
  private final Deeds deeds;

  public Game(Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies) {
    this(rules, players, cups, strategies, new Deeds());
  }

  public Game(Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds) {
    this.rules = rules;
    this.players = players;
    this.cups = cups;
    this.strategies = strategies;
    this.deeds = deeds;
  }

  /** A game whose players leave every choice they are offered alone. */
  public Game(Rule.Set rules, List<Player> players, Cups cups) {
    this(rules, players, cups, Strategy.OfPlayers.NOBODY_DECIDES);
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

    Journalling journalling = new Journalling(journal);
    Landings landings = new LandSale(deeds, turnOrder, strategies, journalling);
    Landings rent = new Rent(deeds, rules, turnOrder, strategies, journalling);
    Landings resolvingLandings = (player, space) -> {
      landings.resolve(player, space);
      rent.resolve(player, space);
    };
    turnOrder.forEach(player -> takeTurn(player, journal, journalling, resolvingLandings));

    return new Result(turnOrder, journal.entries(), deeds);
  }

  private int initiativeRollFor(Player player, Journal journal) {
    int total = cups.forPlayer(player).roll().total();
    journal.log(new Journal.Entry.InitiativeRoll(player.id(), total));
    return total;
  }

  private void takeTurn(Player player, Journal journal, Turn.Events events, Landings landings) {
    journal.log(new Journal.Entry.TurnStarted(player.id()));
    new Turn(rules, cups.forPlayer(player), events, landings).take(player);
  }

  /** Writes down what a turn and a sale say they did, as the game's account of it. */
  private record Journalling(Journal journal) implements Turn.Events, LandSale.Events, Rent.Events {
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

    @Override
    public void bought(Player buyer, Ownable land, Money price) {
      journal.log(new Journal.Entry.Bought(buyer.id(), land.type(), price));
    }

    @Override
    public void wonAtAuction(Player winner, Ownable land, Money price) {
      journal.log(new Journal.Entry.AuctionWon(winner.id(), land.type(), price));
    }

    @Override
    public void paid(Player tenant, Player owner, ColourStreet land, Money rent) {
      journal.log(new Journal.Entry.RentPaid(tenant.id(), owner.id(), land.type(), rent));
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
   * How the game went: the players in the order they take their turns, the
   * account of what happened, and who ended up owning what. The account is data
   * rather than written text, so that rendering it stays somebody else's job.
   */
  public record Result(List<Player> turnOrder, List<Journal.Entry> journal, Deeds deeds) {
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

      /** Land bought from the bank at the price on the board. */
      record Bought(Player.ID player, Street.Type land, Money price) implements Entry {
      }

      /** Land the table bid for, and what the winner paid for it. */
      record AuctionWon(Player.ID player, Street.Type land, Money price) implements Entry {
      }

      record RentPaid(Player.ID tenant, Player.ID owner, Street.Type land, Money rent) implements Entry {
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=35940ef5c69804663818be8998f89a2d6c0f8e05a22687415acc8a566c970d68
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjMy
scope.0.kind=class
scope.0.startLine=32
scope.0.endLine=186
scope.0.semanticHash=20bd4e2159924d2850a8fa388b78b19d4a102b72cc7252627df3870b5836cea5
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6MTE4
scope.1.kind=class
scope.1.startLine=118
scope.1.endLine=121
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6MTM1
scope.2.kind=class
scope.2.startLine=135
scope.2.endLine=185
scope.2.semanticHash=3e0e53c90343d3226f45c69c69f2b82f838f82d47b316e8fb3b51050df4e6e69
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjE1NA
scope.3.kind=class
scope.3.startLine=154
scope.3.endLine=184
scope.3.semanticHash=2bf8898ecdc4951148d987f379c41e1bcadce4e79eab502591d062a94c748aeb
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jQXVjdGlvbldvbjoxODI
scope.4.kind=class
scope.4.startLine=182
scope.4.endLine=183
scope.4.semanticHash=71ee77afd5451e73d54900eb221aa9640cfe5a2b42680f570ad5efdf9e473cb9
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNCb3VnaHQ6MTc4
scope.5.kind=class
scope.5.startLine=178
scope.5.endLine=179
scope.5.semanticHash=27025028a74b83acd66fd8557558fb235025cf183bcc4e3623183b26da003aec
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjE1OQ
scope.6.kind=class
scope.6.startLine=159
scope.6.endLine=160
scope.6.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjoxNjI
scope.7.kind=class
scope.7.startLine=162
scope.7.endLine=163
scope.7.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjE3MQ
scope.8.kind=class
scope.8.startLine=171
scope.8.endLine=172
scope.8.semanticHash=4e6b3e5a3aadbf584012feeb3f79ac0d9d7c37bd772a52948f0535fcc8469248
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6MTY4
scope.9.kind=class
scope.9.startLine=168
scope.9.endLine=169
scope.9.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6MTc0
scope.10.kind=class
scope.10.startLine=174
scope.10.endLine=175
scope.10.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjE1NQ
scope.11.kind=class
scope.11.startLine=155
scope.11.endLine=156
scope.11.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.12.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjE2NQ
scope.12.kind=class
scope.12.startLine=165
scope.12.endLine=166
scope.12.semanticHash=dfc91baefb26739577f196db32ba23dd37b692bf0ff3e65056a0e06ff7cdbcc9
scope.13.id=Y2xhc3M6R2FtZS5Kb3VybmFsbGluZyNKb3VybmFsbGluZzo4Ng
scope.13.kind=class
scope.13.startLine=86
scope.13.endLine=111
scope.13.semanticHash=bd554458f28b29490fc9d8fbbb203145fe1afdac5eefe9f69ab79f0b57af1299
scope.14.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjEzMg
scope.14.kind=class
scope.14.startLine=132
scope.14.endLine=133
scope.14.semanticHash=2b233921741f05b2f0ebe500bff5fc2081ac190bf5a13ec394205f90fc8dcd28
scope.15.id=ZmllbGQ6R2FtZSNjdXBzOjM1
scope.15.kind=field
scope.15.startLine=35
scope.15.endLine=35
scope.15.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.16.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjM0
scope.16.kind=field
scope.16.startLine=34
scope.16.endLine=34
scope.16.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.17.id=ZmllbGQ6R2FtZSNydWxlczozMw
scope.17.kind=field
scope.17.startLine=33
scope.17.endLine=33
scope.17.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.18.id=ZmllbGQ6R2FtZSNzdHJhdGVnaWVzOjM2
scope.18.kind=field
scope.18.startLine=36
scope.18.endLine=36
scope.18.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.19.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6MTM4
scope.19.kind=field
scope.19.startLine=138
scope.19.endLine=138
scope.19.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.20.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjoxMzY
scope.20.kind=field
scope.20.startLine=136
scope.20.endLine=136
scope.20.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.21.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jbGFuZDoxODI
scope.21.kind=field
scope.21.startLine=182
scope.21.endLine=182
scope.21.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.22.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcGxheWVyOjE4Mg
scope.22.kind=field
scope.22.startLine=182
scope.22.endLine=182
scope.22.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.23.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcHJpY2U6MTgy
scope.23.kind=field
scope.23.startLine=182
scope.23.endLine=182
scope.23.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.24.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNsYW5kOjE3OA
scope.24.kind=field
scope.24.startLine=178
scope.24.endLine=178
scope.24.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.25.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwbGF5ZXI6MTc4
scope.25.kind=field
scope.25.startLine=178
scope.25.endLine=178
scope.25.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.26.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwcmljZToxNzg
scope.26.kind=field
scope.26.startLine=178
scope.26.endLine=178
scope.26.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.27.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjoxNTk
scope.27.kind=field
scope.27.startLine=159
scope.27.endLine=159
scope.27.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.28.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjE1OQ
scope.28.kind=field
scope.28.startLine=159
scope.28.endLine=159
scope.28.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.29.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjE2Mg
scope.29.kind=field
scope.29.startLine=162
scope.29.endLine=162
scope.29.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.30.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206MTcx
scope.30.kind=field
scope.30.startLine=171
scope.30.endLine=171
scope.30.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.31.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjoxNzE
scope.31.kind=field
scope.31.startLine=171
scope.31.endLine=171
scope.31.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.32.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjE3MQ
scope.32.kind=field
scope.32.startLine=171
scope.32.endLine=171
scope.32.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.33.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6MTY4
scope.33.kind=field
scope.33.startLine=168
scope.33.endLine=168
scope.33.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.34.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDoxNjg
scope.34.kind=field
scope.34.startLine=168
scope.34.endLine=168
scope.34.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.35.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6MTc0
scope.35.kind=field
scope.35.startLine=174
scope.35.endLine=174
scope.35.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.36.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6MTc0
scope.36.kind=field
scope.36.startLine=174
scope.36.endLine=174
scope.36.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.37.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6MTU1
scope.37.kind=field
scope.37.startLine=155
scope.37.endLine=155
scope.37.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.38.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjoxNjU
scope.38.kind=field
scope.38.startLine=165
scope.38.endLine=165
scope.38.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.39.id=ZmllbGQ6R2FtZS5Kb3VybmFsbGluZyNqb3VybmFsOjg2
scope.39.kind=field
scope.39.startLine=86
scope.39.endLine=86
scope.39.semanticHash=85c5d503112071935eec23a4e6e40fde3bfadba06348aca287c46765762c68bf
scope.40.id=ZmllbGQ6R2FtZS5SZXN1bHQjZGVlZHM6MTMy
scope.40.kind=field
scope.40.startLine=132
scope.40.endLine=132
scope.40.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.41.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDoxMzI
scope.41.kind=field
scope.41.startLine=132
scope.41.endLine=132
scope.41.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.42.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjEzMg
scope.42.kind=field
scope.42.startLine=132
scope.42.endLine=132
scope.42.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.43.id=bWV0aG9kOkdhbWUjY3RvcigyKTo1MQ
scope.43.kind=method
scope.43.startLine=51
scope.43.endLine=53
scope.43.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.44.id=bWV0aG9kOkdhbWUjY3RvcigzKTo0Ng
scope.44.kind=method
scope.44.startLine=46
scope.44.endLine=48
scope.44.semanticHash=6776d3f993630076b24a0ed0b3bc39a8d8cf1fb2c29184dba3c568e657a2a980
scope.45.id=bWV0aG9kOkdhbWUjY3RvcigzKTo1Ng
scope.45.kind=method
scope.45.startLine=56
scope.45.endLine=58
scope.45.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.46.id=bWV0aG9kOkdhbWUjY3Rvcig0KTozOA
scope.46.kind=method
scope.46.startLine=38
scope.46.endLine=43
scope.46.semanticHash=543f77dfece4b843cc6ee16635ebb899e3f63337d55e3bb12de09fff18a7bc47
scope.47.id=bWV0aG9kOkdhbWUjaWRzKDEpOjEyMw
scope.47.kind=method
scope.47.startLine=123
scope.47.endLine=125
scope.47.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.48.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6NzQ
scope.48.kind=method
scope.48.startLine=74
scope.48.endLine=78
scope.48.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.49.id=bWV0aG9kOkdhbWUjcGxheSgwKTo2MA
scope.49.kind=method
scope.49.startLine=60
scope.49.endLine=72
scope.49.semanticHash=15da5588c4819b9c15850f15eb05c7bdaaa84497f2f97ea683871f6e5a418707
scope.50.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oNCk6ODA
scope.50.kind=method
scope.50.startLine=80
scope.50.endLine=83
scope.50.semanticHash=4debec5e860dda014b6143b6d72e27d9e98fe9b718222e4b73b5f78064fedea9
scope.51.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6MTIw
scope.51.kind=method
scope.51.startLine=120
scope.51.endLine=120
scope.51.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.52.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjEzNQ
scope.52.kind=method
scope.52.startLine=1
scope.52.endLine=186
scope.52.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.53.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjE0NQ
scope.53.kind=method
scope.53.startLine=145
scope.53.endLine=147
scope.53.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.54.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6MTQw
scope.54.kind=method
scope.54.startLine=140
scope.54.endLine=143
scope.54.semanticHash=b5421e8cb3d5bc8c502ce4d321cdeecd90548b5faa054fb99b324edef550d319
scope.55.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5BdWN0aW9uV29uI2N0b3IoMyk6MTgy
scope.55.kind=method
scope.55.startLine=1
scope.55.endLine=186
scope.55.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.56.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Cb3VnaHQjY3RvcigzKToxNzg
scope.56.kind=method
scope.56.startLine=1
scope.56.endLine=186
scope.56.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.57.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjE1OQ
scope.57.kind=method
scope.57.startLine=1
scope.57.endLine=186
scope.57.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.58.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6MTYy
scope.58.kind=method
scope.58.startLine=1
scope.58.endLine=186
scope.58.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.59.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjE3MQ
scope.59.kind=method
scope.59.startLine=1
scope.59.endLine=186
scope.59.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.60.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKToxNjg
scope.60.kind=method
scope.60.startLine=1
scope.60.endLine=186
scope.60.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.61.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKToxNzQ
scope.61.kind=method
scope.61.startLine=1
scope.61.endLine=186
scope.61.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.62.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjE1NQ
scope.62.kind=method
scope.62.startLine=1
scope.62.endLine=186
scope.62.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.63.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDEpOjE2NQ
scope.63.kind=method
scope.63.startLine=1
scope.63.endLine=186
scope.63.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.64.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYm91Z2h0KDMpOjEwMg
scope.64.kind=method
scope.64.startLine=102
scope.64.endLine=105
scope.64.semanticHash=79e1d988b41c146a0d4c76c5bf1404dcfb5f43bf5ed7caac3fd2b5b0fbd2c437
scope.65.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY29sbGVjdGVkU2FsYXJ5KDIpOjk3
scope.65.kind=method
scope.65.startLine=97
scope.65.endLine=100
scope.65.semanticHash=9d31c851d99e8df553fdaf39330dc1ae11e0fe903b61f6b97b858c59389d5411
scope.66.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY3RvcigxKTo4Ng
scope.66.kind=method
scope.66.startLine=1
scope.66.endLine=186
scope.66.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
scope.67.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW92ZWQoMyk6OTI
scope.67.kind=method
scope.67.startLine=92
scope.67.endLine=95
scope.67.semanticHash=624e9fcd1bcf33ec8f097872d5cf6e59f4e27c7ca7a686a5b60d818f937efc7b
scope.68.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcm9sbGVkKDIpOjg3
scope.68.kind=method
scope.68.startLine=87
scope.68.endLine=90
scope.68.semanticHash=41570b54acd7a2a0ee9f75bcefcdd79248dd7db4784b77cea66150ac4e5a790f
scope.69.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uQXRBdWN0aW9uKDMpOjEwNw
scope.69.kind=method
scope.69.startLine=107
scope.69.endLine=110
scope.69.semanticHash=db10ed18596729bcd7577e23594ec8451323564e17dc73c245ab874c151f6569
scope.70.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoMyk6MTMy
scope.70.kind=method
scope.70.startLine=1
scope.70.endLine=186
scope.70.semanticHash=35e73468891b5635a6b3b637bed8a6a3d965a131547b677b8c239991798a356b
*/
