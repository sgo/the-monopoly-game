package the.monopoly.game;

import org.slf4j.Logger;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Building;
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
    Landings rent = new Rent(deeds, rules, turnOrder, strategies, journalling);
    Landings landSale = new LandSale(deeds, turnOrder, strategies, journalling);
    Building building = new Building(deeds, rules, strategies, journalling);
    Landings resolvingLandings = (player, space, roll) -> {
      rent.resolve(player, space, roll);
      landSale.resolve(player, space, roll);
    };
    Player builder = turnOrder.getFirst();
    turnOrder.forEach(player -> {
      takeTurn(player, journal, journalling, resolvingLandings);
      if (player.id().equals(builder.id())) building.develop(player);
    });

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
  private record Journalling(Journal journal) implements Turn.Events, LandSale.Events, Rent.Events, Building.Events {
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
    public void paid(Player tenant, Player owner, Ownable land, Money rent) {
      journal.log(new Journal.Entry.RentPaid(tenant.id(), owner.id(), land.type(), rent));
    }

    @Override
    public void builtHouse(Player player, ColourStreet street, Money price) {
      journal.log(new Journal.Entry.HouseBuilt(player.id(), street.type(), price));
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

      record HouseBuilt(Player.ID player, Street.Type land, Money price) implements Entry {
      }

      record HouseSold(Player.ID player, Street.Type land, Money price) implements Entry {
      }

      record Mortgaged(Player.ID player, Street.Type land, Money value) implements Entry {
      }

      record MortgageLifted(Player.ID player, Street.Type land, Money total, Money interest) implements Entry {
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=c06874a95665df4e341e51309cde0bc20567ce1576671c6ece2288d76f58a2b8
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjM1
scope.0.kind=class
scope.0.startLine=35
scope.0.endLine=229
scope.0.semanticHash=fce8f447715e06c2b2f4a8cbc4a404e7a0a65142a1bac55f7f4e025dbbb1e2c1
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6MTQ2
scope.1.kind=class
scope.1.startLine=146
scope.1.endLine=149
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6MTYz
scope.2.kind=class
scope.2.startLine=163
scope.2.endLine=228
scope.2.semanticHash=c57140a1c9ab91ef25700ac11649e63f4680c6b39062474f54ae193c974f2990
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjE4Mg
scope.3.kind=class
scope.3.startLine=182
scope.3.endLine=227
scope.3.semanticHash=5d6ef4c86e32005e119800eeb7392404f4efe1652980ea14324d9e25aed8128d
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jQXVjdGlvbldvbjoyMTA
scope.4.kind=class
scope.4.startLine=210
scope.4.endLine=211
scope.4.semanticHash=71ee77afd5451e73d54900eb221aa9640cfe5a2b42680f570ad5efdf9e473cb9
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNCb3VnaHQ6MjA2
scope.5.kind=class
scope.5.startLine=206
scope.5.endLine=207
scope.5.semanticHash=27025028a74b83acd66fd8557558fb235025cf183bcc4e3623183b26da003aec
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjSG91c2VCdWlsdDoyMTY
scope.6.kind=class
scope.6.startLine=216
scope.6.endLine=217
scope.6.semanticHash=27575972b2787c07a6fa98a725c5bed4487591e647678d5880869a7c637aee97
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNIb3VzZVNvbGQ6MjE5
scope.7.kind=class
scope.7.startLine=219
scope.7.endLine=220
scope.7.semanticHash=7bfd22802262e4e36e5ffa57b44a79dc928c71eaa883b7f110cd1b9e7f7230b2
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjE4Nw
scope.8.kind=class
scope.8.startLine=187
scope.8.endLine=188
scope.8.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjoxOTA
scope.9.kind=class
scope.9.startLine=190
scope.9.endLine=191
scope.9.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI01vcnRnYWdlTGlmdGVkOjIyNQ
scope.10.kind=class
scope.10.startLine=225
scope.10.endLine=226
scope.10.semanticHash=876fc18a90cbc579ad9618bc95f03fa33b7cdeae3c7f18f0ead53f7795f64237
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNNb3J0Z2FnZWQ6MjIy
scope.11.kind=class
scope.11.startLine=222
scope.11.endLine=223
scope.11.semanticHash=2536842d77794ad82293557093f6b95391662318c0cf866281f0bfeb54b47212
scope.12.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjE5OQ
scope.12.kind=class
scope.12.startLine=199
scope.12.endLine=200
scope.12.semanticHash=4e6b3e5a3aadbf584012feeb3f79ac0d9d7c37bd772a52948f0535fcc8469248
scope.13.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI1JlbnRQYWlkOjIxMw
scope.13.kind=class
scope.13.startLine=213
scope.13.endLine=214
scope.13.semanticHash=47c40c5b19c0d8df73eda2478d761c7a7e8604713b0719926b1f46a2f2b6f104
scope.14.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6MTk2
scope.14.kind=class
scope.14.startLine=196
scope.14.endLine=197
scope.14.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.15.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6MjAy
scope.15.kind=class
scope.15.startLine=202
scope.15.endLine=203
scope.15.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.16.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjE4Mw
scope.16.kind=class
scope.16.startLine=183
scope.16.endLine=184
scope.16.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.17.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjE5Mw
scope.17.kind=class
scope.17.startLine=193
scope.17.endLine=194
scope.17.semanticHash=dfc91baefb26739577f196db32ba23dd37b692bf0ff3e65056a0e06ff7cdbcc9
scope.18.id=Y2xhc3M6R2FtZS5Kb3VybmFsbGluZyNKb3VybmFsbGluZzoxMDQ
scope.18.kind=class
scope.18.startLine=104
scope.18.endLine=139
scope.18.semanticHash=79e4134888c8b799ab5e1d9cefbadd8a710cb3dbf6fd1405a21658f208db079b
scope.19.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjE2MA
scope.19.kind=class
scope.19.startLine=160
scope.19.endLine=161
scope.19.semanticHash=2b233921741f05b2f0ebe500bff5fc2081ac190bf5a13ec394205f90fc8dcd28
scope.20.id=ZmllbGQ6R2FtZSNjdXBzOjM4
scope.20.kind=field
scope.20.startLine=38
scope.20.endLine=38
scope.20.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.21.id=ZmllbGQ6R2FtZSNkZWVkczo0MA
scope.21.kind=field
scope.21.startLine=40
scope.21.endLine=40
scope.21.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.22.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjM3
scope.22.kind=field
scope.22.startLine=37
scope.22.endLine=37
scope.22.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.23.id=ZmllbGQ6R2FtZSNydWxlczozNg
scope.23.kind=field
scope.23.startLine=36
scope.23.endLine=36
scope.23.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.24.id=ZmllbGQ6R2FtZSNzdHJhdGVnaWVzOjM5
scope.24.kind=field
scope.24.startLine=39
scope.24.endLine=39
scope.24.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.25.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6MTY2
scope.25.kind=field
scope.25.startLine=166
scope.25.endLine=166
scope.25.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.26.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjoxNjQ
scope.26.kind=field
scope.26.startLine=164
scope.26.endLine=164
scope.26.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.27.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jbGFuZDoyMTA
scope.27.kind=field
scope.27.startLine=210
scope.27.endLine=210
scope.27.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.28.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcGxheWVyOjIxMA
scope.28.kind=field
scope.28.startLine=210
scope.28.endLine=210
scope.28.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.29.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcHJpY2U6MjEw
scope.29.kind=field
scope.29.startLine=210
scope.29.endLine=210
scope.29.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.30.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNsYW5kOjIwNg
scope.30.kind=field
scope.30.startLine=206
scope.30.endLine=206
scope.30.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.31.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwbGF5ZXI6MjA2
scope.31.kind=field
scope.31.startLine=206
scope.31.endLine=206
scope.31.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.32.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwcmljZToyMDY
scope.32.kind=field
scope.32.startLine=206
scope.32.endLine=206
scope.32.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.33.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjbGFuZDoyMTY
scope.33.kind=field
scope.33.startLine=216
scope.33.endLine=216
scope.33.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.34.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcGxheWVyOjIxNg
scope.34.kind=field
scope.34.startLine=216
scope.34.endLine=216
scope.34.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.35.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcHJpY2U6MjE2
scope.35.kind=field
scope.35.startLine=216
scope.35.endLine=216
scope.35.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.36.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNsYW5kOjIxOQ
scope.36.kind=field
scope.36.startLine=219
scope.36.endLine=219
scope.36.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.37.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwbGF5ZXI6MjE5
scope.37.kind=field
scope.37.startLine=219
scope.37.endLine=219
scope.37.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.38.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwcmljZToyMTk
scope.38.kind=field
scope.38.startLine=219
scope.38.endLine=219
scope.38.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.39.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjoxODc
scope.39.kind=field
scope.39.startLine=187
scope.39.endLine=187
scope.39.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.40.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjE4Nw
scope.40.kind=field
scope.40.startLine=187
scope.40.endLine=187
scope.40.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.41.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjE5MA
scope.41.kind=field
scope.41.startLine=190
scope.41.endLine=190
scope.41.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.42.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2ludGVyZXN0OjIyNQ
scope.42.kind=field
scope.42.startLine=225
scope.42.endLine=225
scope.42.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.43.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2xhbmQ6MjI1
scope.43.kind=field
scope.43.startLine=225
scope.43.endLine=225
scope.43.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.44.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3BsYXllcjoyMjU
scope.44.kind=field
scope.44.startLine=225
scope.44.endLine=225
scope.44.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.45.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3RvdGFsOjIyNQ
scope.45.kind=field
scope.45.startLine=225
scope.45.endLine=225
scope.45.semanticHash=c19b9ea10da3846e1c37942dc47f59b13649fda020125cea6df5d3a470490575
scope.46.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNsYW5kOjIyMg
scope.46.kind=field
scope.46.startLine=222
scope.46.endLine=222
scope.46.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.47.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNwbGF5ZXI6MjIy
scope.47.kind=field
scope.47.startLine=222
scope.47.endLine=222
scope.47.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.48.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCN2YWx1ZToyMjI
scope.48.kind=field
scope.48.startLine=222
scope.48.endLine=222
scope.48.semanticHash=0d022e0e6113de33cad355012469518c3a21832e276a9c3f3a6893880214fa0a
scope.49.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206MTk5
scope.49.kind=field
scope.49.startLine=199
scope.49.endLine=199
scope.49.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.50.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjoxOTk
scope.50.kind=field
scope.50.startLine=199
scope.50.endLine=199
scope.50.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.51.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjE5OQ
scope.51.kind=field
scope.51.startLine=199
scope.51.endLine=199
scope.51.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.52.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI2xhbmQ6MjEz
scope.52.kind=field
scope.52.startLine=213
scope.52.endLine=213
scope.52.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.53.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI293bmVyOjIxMw
scope.53.kind=field
scope.53.startLine=213
scope.53.endLine=213
scope.53.semanticHash=4dc1b9a409163bdb6ca915d1f2c9f0426b20999866c7edce9557fe1f7cfb7c14
scope.54.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3JlbnQ6MjEz
scope.54.kind=field
scope.54.startLine=213
scope.54.endLine=213
scope.54.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.55.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3RlbmFudDoyMTM
scope.55.kind=field
scope.55.startLine=213
scope.55.endLine=213
scope.55.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.56.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6MTk2
scope.56.kind=field
scope.56.startLine=196
scope.56.endLine=196
scope.56.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.57.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDoxOTY
scope.57.kind=field
scope.57.startLine=196
scope.57.endLine=196
scope.57.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.58.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6MjAy
scope.58.kind=field
scope.58.startLine=202
scope.58.endLine=202
scope.58.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.59.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6MjAy
scope.59.kind=field
scope.59.startLine=202
scope.59.endLine=202
scope.59.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.60.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6MTgz
scope.60.kind=field
scope.60.startLine=183
scope.60.endLine=183
scope.60.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.61.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjoxOTM
scope.61.kind=field
scope.61.startLine=193
scope.61.endLine=193
scope.61.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.62.id=ZmllbGQ6R2FtZS5Kb3VybmFsbGluZyNqb3VybmFsOjEwNA
scope.62.kind=field
scope.62.startLine=104
scope.62.endLine=104
scope.62.semanticHash=85c5d503112071935eec23a4e6e40fde3bfadba06348aca287c46765762c68bf
scope.63.id=ZmllbGQ6R2FtZS5SZXN1bHQjZGVlZHM6MTYw
scope.63.kind=field
scope.63.startLine=160
scope.63.endLine=160
scope.63.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.64.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDoxNjA
scope.64.kind=field
scope.64.startLine=160
scope.64.endLine=160
scope.64.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.65.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjE2MA
scope.65.kind=field
scope.65.startLine=160
scope.65.endLine=160
scope.65.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.66.id=bWV0aG9kOkdhbWUjY3RvcigyKTo2MA
scope.66.kind=method
scope.66.startLine=60
scope.66.endLine=62
scope.66.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.67.id=bWV0aG9kOkdhbWUjY3RvcigzKTo1NQ
scope.67.kind=method
scope.67.startLine=55
scope.67.endLine=57
scope.67.semanticHash=6776d3f993630076b24a0ed0b3bc39a8d8cf1fb2c29184dba3c568e657a2a980
scope.68.id=bWV0aG9kOkdhbWUjY3RvcigzKTo2NQ
scope.68.kind=method
scope.68.startLine=65
scope.68.endLine=67
scope.68.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.69.id=bWV0aG9kOkdhbWUjY3Rvcig0KTo0Mg
scope.69.kind=method
scope.69.startLine=42
scope.69.endLine=44
scope.69.semanticHash=99fd77fe526ead513b310d1ea7e21b391ba47c0e2d324dba3f6b10e3483dbad5
scope.70.id=bWV0aG9kOkdhbWUjY3Rvcig1KTo0Ng
scope.70.kind=method
scope.70.startLine=46
scope.70.endLine=52
scope.70.semanticHash=ddf0d7ab77bbf8f8b627725456196a5de53809911b6d3c7ba76f085278eadd1a
scope.71.id=bWV0aG9kOkdhbWUjaWRzKDEpOjE1MQ
scope.71.kind=method
scope.71.startLine=151
scope.71.endLine=153
scope.71.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.72.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6OTI
scope.72.kind=method
scope.72.startLine=92
scope.72.endLine=96
scope.72.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.73.id=bWV0aG9kOkdhbWUjcGxheSgwKTo2OQ
scope.73.kind=method
scope.73.startLine=69
scope.73.endLine=90
scope.73.semanticHash=d9fa8c289c8593ecb068e15e6c2e7740fbb6a240e6ad5de4f456fc6324a871a2
scope.74.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oNCk6OTg
scope.74.kind=method
scope.74.startLine=98
scope.74.endLine=101
scope.74.semanticHash=4debec5e860dda014b6143b6d72e27d9e98fe9b718222e4b73b5f78064fedea9
scope.75.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6MTQ4
scope.75.kind=method
scope.75.startLine=148
scope.75.endLine=148
scope.75.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.76.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjE2Mw
scope.76.kind=method
scope.76.startLine=1
scope.76.endLine=229
scope.76.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.77.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjE3Mw
scope.77.kind=method
scope.77.startLine=173
scope.77.endLine=175
scope.77.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.78.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6MTY4
scope.78.kind=method
scope.78.startLine=168
scope.78.endLine=171
scope.78.semanticHash=b5421e8cb3d5bc8c502ce4d321cdeecd90548b5faa054fb99b324edef550d319
scope.79.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5BdWN0aW9uV29uI2N0b3IoMyk6MjEw
scope.79.kind=method
scope.79.startLine=1
scope.79.endLine=229
scope.79.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.80.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Cb3VnaHQjY3RvcigzKToyMDY
scope.80.kind=method
scope.80.startLine=1
scope.80.endLine=229
scope.80.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.81.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZUJ1aWx0I2N0b3IoMyk6MjE2
scope.81.kind=method
scope.81.startLine=1
scope.81.endLine=229
scope.81.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.82.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZVNvbGQjY3RvcigzKToyMTk
scope.82.kind=method
scope.82.startLine=1
scope.82.endLine=229
scope.82.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.83.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjE4Nw
scope.83.kind=method
scope.83.startLine=1
scope.83.endLine=229
scope.83.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.84.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6MTkw
scope.84.kind=method
scope.84.startLine=1
scope.84.endLine=229
scope.84.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.85.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUxpZnRlZCNjdG9yKDQpOjIyNQ
scope.85.kind=method
scope.85.startLine=1
scope.85.endLine=229
scope.85.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.86.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZWQjY3RvcigzKToyMjI
scope.86.kind=method
scope.86.startLine=1
scope.86.endLine=229
scope.86.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.87.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjE5OQ
scope.87.kind=method
scope.87.startLine=1
scope.87.endLine=229
scope.87.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.88.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5SZW50UGFpZCNjdG9yKDQpOjIxMw
scope.88.kind=method
scope.88.startLine=1
scope.88.endLine=229
scope.88.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.89.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKToxOTY
scope.89.kind=method
scope.89.startLine=1
scope.89.endLine=229
scope.89.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.90.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKToyMDI
scope.90.kind=method
scope.90.startLine=1
scope.90.endLine=229
scope.90.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.91.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjE4Mw
scope.91.kind=method
scope.91.startLine=1
scope.91.endLine=229
scope.91.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.92.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDEpOjE5Mw
scope.92.kind=method
scope.92.startLine=1
scope.92.endLine=229
scope.92.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.93.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYm91Z2h0KDMpOjEyMA
scope.93.kind=method
scope.93.startLine=120
scope.93.endLine=123
scope.93.semanticHash=79e1d988b41c146a0d4c76c5bf1404dcfb5f43bf5ed7caac3fd2b5b0fbd2c437
scope.94.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYnVpbHRIb3VzZSgzKToxMzU
scope.94.kind=method
scope.94.startLine=135
scope.94.endLine=138
scope.94.semanticHash=e51ffaaf9fc64c2ff825668ffee31babc9a49fd98e53b320a973887332b1074d
scope.95.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY29sbGVjdGVkU2FsYXJ5KDIpOjExNQ
scope.95.kind=method
scope.95.startLine=115
scope.95.endLine=118
scope.95.semanticHash=9d31c851d99e8df553fdaf39330dc1ae11e0fe903b61f6b97b858c59389d5411
scope.96.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY3RvcigxKToxMDQ
scope.96.kind=method
scope.96.startLine=1
scope.96.endLine=229
scope.96.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
scope.97.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW92ZWQoMyk6MTEw
scope.97.kind=method
scope.97.startLine=110
scope.97.endLine=113
scope.97.semanticHash=624e9fcd1bcf33ec8f097872d5cf6e59f4e27c7ca7a686a5b60d818f937efc7b
scope.98.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZCg0KToxMzA
scope.98.kind=method
scope.98.startLine=130
scope.98.endLine=133
scope.98.semanticHash=66317d89046f5bdcdf22cb407d9a450e9f7221f4020da0e087ac3b105a7beaa8
scope.99.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcm9sbGVkKDIpOjEwNQ
scope.99.kind=method
scope.99.startLine=105
scope.99.endLine=108
scope.99.semanticHash=41570b54acd7a2a0ee9f75bcefcdd79248dd7db4784b77cea66150ac4e5a790f
scope.100.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uQXRBdWN0aW9uKDMpOjEyNQ
scope.100.kind=method
scope.100.startLine=125
scope.100.endLine=128
scope.100.semanticHash=db10ed18596729bcd7577e23594ec8451323564e17dc73c245ab874c151f6569
scope.101.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoMyk6MTYw
scope.101.kind=method
scope.101.startLine=1
scope.101.endLine=229
scope.101.semanticHash=01a976e16ec1b31c32b4ca4eec89e371973776fb33830b62fd96b32b016c024c
*/
