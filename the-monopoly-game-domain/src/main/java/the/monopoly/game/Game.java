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
import the.monopoly.game.rules.Cards;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.LandSale;
import the.monopoly.game.rules.Landings;
import the.monopoly.game.rules.Rent;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Taxes;
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
  private final Cards.Decks decks;

  public Game(Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies) {
    this(rules, players, cups, strategies, new Deeds(), Cards.Decks.EMPTY);
  }

  public Game(Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds) {
    this(rules, players, cups, strategies, deeds, Cards.Decks.EMPTY);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds, Cards.Decks decks
  ) {
    this.rules = rules;
    this.players = players;
    this.cups = cups;
    this.strategies = strategies;
    this.deeds = deeds;
    this.decks = decks;
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
    Building building = new Building(deeds, rules, strategies, journalling);
    Player builder = turnOrder.getFirst();
    turnOrder.forEach(player -> {
      takeTurn(player, journal, journalling, landingsFor(player, turnOrder, journalling));
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

  private Landings landingsFor(Player player, List<Player> turnOrder, Journalling journalling) {
    Landings rent = new Rent(deeds, rules, turnOrder, strategies, journalling);
    Landings landSale = new LandSale(deeds, rules, turnOrder, strategies, journalling);
    Landings cards = new Cards(deeds, rules, turnOrder, strategies, decks, journalling, cups.forPlayer(player));
    Landings taxes = new Taxes(journalling);
    return (who, space, roll) -> {
      rent.resolve(who, space, roll);
      landSale.resolve(who, space, roll);
      cards.resolve(who, space, roll);
      taxes.resolve(who, space, roll);
    };
  }

  /** Writes down what a turn and a sale say they did, as the game's account of it. */
  private record Journalling(Journal journal)
      implements Turn.Events, LandSale.Events, Rent.Events, Building.Events, Cards.Events, Taxes.Events {
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

    @Override
    public void sold(Player seller, Ownable land, Player buyer, Money price) {
      journal.log(new Journal.Entry.LandSold(seller.id(), land.type(), buyer.id(), price));
    }

    @Override
    public void saleRefused(Player seller, Ownable land, Player buyer, Money price) {
      journal.log(new Journal.Entry.LandSaleRefused(seller.id(), land.type(), buyer.id(), price));
    }

    @Override
    public void refusedBuilding(Player player, ColourStreet street, Money price) {
      journal.log(new Journal.Entry.BuildingRefused(player.id(), street.type(), price));
    }

    @Override
    public void drewChanceCard(Player player, String card) {
      journal.log(new Journal.Entry.ChanceCardDrawn(player.id(), card));
    }

    @Override
    public void drewCommunityChestCard(Player player, String card) {
      journal.log(new Journal.Entry.CommunityChestCardDrawn(player.id(), card));
    }

    @Override
    public void paidBank(Player player, Money amount) {
      journal.log(new Journal.Entry.BankPaid(player.id(), amount));
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

      record LandSold(Player.ID seller, Street.Type land, Player.ID buyer, Money price) implements Entry {
      }

      record LandSaleRefused(Player.ID seller, Street.Type land, Player.ID buyer, Money price) implements Entry {
      }

      record BuildingRefused(Player.ID player, Street.Type land, Money price) implements Entry {
      }

      record ChanceCardDrawn(Player.ID player, String card) implements Entry {
      }

      record CommunityChestCardDrawn(Player.ID player, String card) implements Entry {
      }

      record BankPaid(Player.ID player, Money amount) implements Entry {
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=1393116c781404ce1b44a12739e4b68f90b87068ce1a5dd34699bcd33a2992c5
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjM2
scope.0.kind=class
scope.0.startLine=36
scope.0.endLine=292
scope.0.semanticHash=682d6a78355968de26786519754779a9c9de1f8952345c055d472177770c7c9e
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6MTkx
scope.1.kind=class
scope.1.startLine=191
scope.1.endLine=194
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6MjA4
scope.2.kind=class
scope.2.startLine=208
scope.2.endLine=291
scope.2.semanticHash=3d70708c9dd167eee8040515f86868053f74ce5217b37b930740d46607d64db3
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjIyNw
scope.3.kind=class
scope.3.startLine=227
scope.3.endLine=290
scope.3.semanticHash=09a87309e7c75192ca5f508e6dbe9b3cb7bc09f1d0686197411b9818dab8150d
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jQXVjdGlvbldvbjoyNTU
scope.4.kind=class
scope.4.startLine=255
scope.4.endLine=256
scope.4.semanticHash=71ee77afd5451e73d54900eb221aa9640cfe5a2b42680f570ad5efdf9e473cb9
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI0JhbmtQYWlkOjI4OA
scope.5.kind=class
scope.5.startLine=288
scope.5.endLine=289
scope.5.semanticHash=cce5d5e05e58aed8d75d4ca2deb97d6d8388449cd41d7b4bc422482a269ff5b4
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNCb3VnaHQ6MjUx
scope.6.kind=class
scope.6.startLine=251
scope.6.endLine=252
scope.6.semanticHash=27025028a74b83acd66fd8557558fb235025cf183bcc4e3623183b26da003aec
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNCdWlsZGluZ1JlZnVzZWQ6Mjc5
scope.7.kind=class
scope.7.startLine=279
scope.7.endLine=280
scope.7.semanticHash=c46235e296f36ff19ba4b74246e14db6236dced240c6de450bc4186fef89d59b
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNDaGFuY2VDYXJkRHJhd246Mjgy
scope.8.kind=class
scope.8.startLine=282
scope.8.endLine=283
scope.8.semanticHash=a926e22751d95373a632fb1725c91e97b535d7cf2eb62c29d7d515570e3e3344
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI0NvbW11bml0eUNoZXN0Q2FyZERyYXduOjI4NQ
scope.9.kind=class
scope.9.startLine=285
scope.9.endLine=286
scope.9.semanticHash=3a1c04eb7d87bf7ea391ae3f28c5fecb5d887626ebce02e8f7ab581a4ce85290
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjSG91c2VCdWlsdDoyNjE
scope.10.kind=class
scope.10.startLine=261
scope.10.endLine=262
scope.10.semanticHash=27575972b2787c07a6fa98a725c5bed4487591e647678d5880869a7c637aee97
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNIb3VzZVNvbGQ6MjY0
scope.11.kind=class
scope.11.startLine=264
scope.11.endLine=265
scope.11.semanticHash=7bfd22802262e4e36e5ffa57b44a79dc928c71eaa883b7f110cd1b9e7f7230b2
scope.12.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjIzMg
scope.12.kind=class
scope.12.startLine=232
scope.12.endLine=233
scope.12.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.13.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjoyMzU
scope.13.kind=class
scope.13.startLine=235
scope.13.endLine=236
scope.13.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.14.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNMYW5kU2FsZVJlZnVzZWQ6Mjc2
scope.14.kind=class
scope.14.startLine=276
scope.14.endLine=277
scope.14.semanticHash=071a81b29122c94c28ba6cee1630b23eeb7b6acc5cf69c07a622eb04893d79ef
scope.15.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI0xhbmRTb2xkOjI3Mw
scope.15.kind=class
scope.15.startLine=273
scope.15.endLine=274
scope.15.semanticHash=fd9ac419a61f440251c0473ce96aa04eb9d1e3761c450d6e7d55b0f1d92a105e
scope.16.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI01vcnRnYWdlTGlmdGVkOjI3MA
scope.16.kind=class
scope.16.startLine=270
scope.16.endLine=271
scope.16.semanticHash=876fc18a90cbc579ad9618bc95f03fa33b7cdeae3c7f18f0ead53f7795f64237
scope.17.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNNb3J0Z2FnZWQ6MjY3
scope.17.kind=class
scope.17.startLine=267
scope.17.endLine=268
scope.17.semanticHash=2536842d77794ad82293557093f6b95391662318c0cf866281f0bfeb54b47212
scope.18.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjI0NA
scope.18.kind=class
scope.18.startLine=244
scope.18.endLine=245
scope.18.semanticHash=4e6b3e5a3aadbf584012feeb3f79ac0d9d7c37bd772a52948f0535fcc8469248
scope.19.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI1JlbnRQYWlkOjI1OA
scope.19.kind=class
scope.19.startLine=258
scope.19.endLine=259
scope.19.semanticHash=47c40c5b19c0d8df73eda2478d761c7a7e8604713b0719926b1f46a2f2b6f104
scope.20.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6MjQx
scope.20.kind=class
scope.20.startLine=241
scope.20.endLine=242
scope.20.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.21.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6MjQ3
scope.21.kind=class
scope.21.startLine=247
scope.21.endLine=248
scope.21.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.22.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjIyOA
scope.22.kind=class
scope.22.startLine=228
scope.22.endLine=229
scope.22.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.23.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjIzOA
scope.23.kind=class
scope.23.startLine=238
scope.23.endLine=239
scope.23.semanticHash=dfc91baefb26739577f196db32ba23dd37b692bf0ff3e65056a0e06ff7cdbcc9
scope.24.id=Y2xhc3M6R2FtZS5Kb3VybmFsbGluZyNKb3VybmFsbGluZzoxMTg
scope.24.kind=class
scope.24.startLine=118
scope.24.endLine=184
scope.24.semanticHash=f971fa3db2cb31f89799f59ac658675eeadb202a75dce613ceee451c741f3ef9
scope.25.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjIwNQ
scope.25.kind=class
scope.25.startLine=205
scope.25.endLine=206
scope.25.semanticHash=2b233921741f05b2f0ebe500bff5fc2081ac190bf5a13ec394205f90fc8dcd28
scope.26.id=ZmllbGQ6R2FtZSNjdXBzOjM5
scope.26.kind=field
scope.26.startLine=39
scope.26.endLine=39
scope.26.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.27.id=ZmllbGQ6R2FtZSNkZWNrczo0Mg
scope.27.kind=field
scope.27.startLine=42
scope.27.endLine=42
scope.27.semanticHash=130541f31392b2fc32d3c8343ebd76de366010b8930395e22ba244946f508252
scope.28.id=ZmllbGQ6R2FtZSNkZWVkczo0MQ
scope.28.kind=field
scope.28.startLine=41
scope.28.endLine=41
scope.28.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.29.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjM4
scope.29.kind=field
scope.29.startLine=38
scope.29.endLine=38
scope.29.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.30.id=ZmllbGQ6R2FtZSNydWxlczozNw
scope.30.kind=field
scope.30.startLine=37
scope.30.endLine=37
scope.30.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.31.id=ZmllbGQ6R2FtZSNzdHJhdGVnaWVzOjQw
scope.31.kind=field
scope.31.startLine=40
scope.31.endLine=40
scope.31.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.32.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6MjEx
scope.32.kind=field
scope.32.startLine=211
scope.32.endLine=211
scope.32.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.33.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjoyMDk
scope.33.kind=field
scope.33.startLine=209
scope.33.endLine=209
scope.33.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.34.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jbGFuZDoyNTU
scope.34.kind=field
scope.34.startLine=255
scope.34.endLine=255
scope.34.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.35.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcGxheWVyOjI1NQ
scope.35.kind=field
scope.35.startLine=255
scope.35.endLine=255
scope.35.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.36.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcHJpY2U6MjU1
scope.36.kind=field
scope.36.startLine=255
scope.36.endLine=255
scope.36.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.37.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI2Ftb3VudDoyODg
scope.37.kind=field
scope.37.startLine=288
scope.37.endLine=288
scope.37.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.38.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI3BsYXllcjoyODg
scope.38.kind=field
scope.38.startLine=288
scope.38.endLine=288
scope.38.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.39.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNsYW5kOjI1MQ
scope.39.kind=field
scope.39.startLine=251
scope.39.endLine=251
scope.39.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.40.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwbGF5ZXI6MjUx
scope.40.kind=field
scope.40.startLine=251
scope.40.endLine=251
scope.40.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.41.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwcmljZToyNTE
scope.41.kind=field
scope.41.startLine=251
scope.41.endLine=251
scope.41.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.42.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNsYW5kOjI3OQ
scope.42.kind=field
scope.42.startLine=279
scope.42.endLine=279
scope.42.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.43.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwbGF5ZXI6Mjc5
scope.43.kind=field
scope.43.startLine=279
scope.43.endLine=279
scope.43.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.44.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwcmljZToyNzk
scope.44.kind=field
scope.44.startLine=279
scope.44.endLine=279
scope.44.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.45.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNjYXJkOjI4Mg
scope.45.kind=field
scope.45.startLine=282
scope.45.endLine=282
scope.45.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.46.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNwbGF5ZXI6Mjgy
scope.46.kind=field
scope.46.startLine=282
scope.46.endLine=282
scope.46.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.47.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI2NhcmQ6Mjg1
scope.47.kind=field
scope.47.startLine=285
scope.47.endLine=285
scope.47.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.48.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI3BsYXllcjoyODU
scope.48.kind=field
scope.48.startLine=285
scope.48.endLine=285
scope.48.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.49.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjbGFuZDoyNjE
scope.49.kind=field
scope.49.startLine=261
scope.49.endLine=261
scope.49.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.50.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcGxheWVyOjI2MQ
scope.50.kind=field
scope.50.startLine=261
scope.50.endLine=261
scope.50.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.51.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcHJpY2U6MjYx
scope.51.kind=field
scope.51.startLine=261
scope.51.endLine=261
scope.51.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.52.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNsYW5kOjI2NA
scope.52.kind=field
scope.52.startLine=264
scope.52.endLine=264
scope.52.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.53.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwbGF5ZXI6MjY0
scope.53.kind=field
scope.53.startLine=264
scope.53.endLine=264
scope.53.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.54.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwcmljZToyNjQ
scope.54.kind=field
scope.54.startLine=264
scope.54.endLine=264
scope.54.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.55.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjoyMzI
scope.55.kind=field
scope.55.startLine=232
scope.55.endLine=232
scope.55.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.56.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjIzMg
scope.56.kind=field
scope.56.startLine=232
scope.56.endLine=232
scope.56.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.57.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjIzNQ
scope.57.kind=field
scope.57.startLine=235
scope.57.endLine=235
scope.57.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.58.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNidXllcjoyNzY
scope.58.kind=field
scope.58.startLine=276
scope.58.endLine=276
scope.58.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.59.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNsYW5kOjI3Ng
scope.59.kind=field
scope.59.startLine=276
scope.59.endLine=276
scope.59.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.60.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNwcmljZToyNzY
scope.60.kind=field
scope.60.startLine=276
scope.60.endLine=276
scope.60.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.61.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNzZWxsZXI6Mjc2
scope.61.kind=field
scope.61.startLine=276
scope.61.endLine=276
scope.61.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.62.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2J1eWVyOjI3Mw
scope.62.kind=field
scope.62.startLine=273
scope.62.endLine=273
scope.62.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.63.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2xhbmQ6Mjcz
scope.63.kind=field
scope.63.startLine=273
scope.63.endLine=273
scope.63.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.64.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3ByaWNlOjI3Mw
scope.64.kind=field
scope.64.startLine=273
scope.64.endLine=273
scope.64.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.65.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3NlbGxlcjoyNzM
scope.65.kind=field
scope.65.startLine=273
scope.65.endLine=273
scope.65.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.66.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2ludGVyZXN0OjI3MA
scope.66.kind=field
scope.66.startLine=270
scope.66.endLine=270
scope.66.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.67.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2xhbmQ6Mjcw
scope.67.kind=field
scope.67.startLine=270
scope.67.endLine=270
scope.67.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.68.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3BsYXllcjoyNzA
scope.68.kind=field
scope.68.startLine=270
scope.68.endLine=270
scope.68.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.69.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3RvdGFsOjI3MA
scope.69.kind=field
scope.69.startLine=270
scope.69.endLine=270
scope.69.semanticHash=c19b9ea10da3846e1c37942dc47f59b13649fda020125cea6df5d3a470490575
scope.70.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNsYW5kOjI2Nw
scope.70.kind=field
scope.70.startLine=267
scope.70.endLine=267
scope.70.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.71.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNwbGF5ZXI6MjY3
scope.71.kind=field
scope.71.startLine=267
scope.71.endLine=267
scope.71.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.72.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCN2YWx1ZToyNjc
scope.72.kind=field
scope.72.startLine=267
scope.72.endLine=267
scope.72.semanticHash=0d022e0e6113de33cad355012469518c3a21832e276a9c3f3a6893880214fa0a
scope.73.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206MjQ0
scope.73.kind=field
scope.73.startLine=244
scope.73.endLine=244
scope.73.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.74.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjoyNDQ
scope.74.kind=field
scope.74.startLine=244
scope.74.endLine=244
scope.74.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.75.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjI0NA
scope.75.kind=field
scope.75.startLine=244
scope.75.endLine=244
scope.75.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.76.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI2xhbmQ6MjU4
scope.76.kind=field
scope.76.startLine=258
scope.76.endLine=258
scope.76.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.77.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI293bmVyOjI1OA
scope.77.kind=field
scope.77.startLine=258
scope.77.endLine=258
scope.77.semanticHash=4dc1b9a409163bdb6ca915d1f2c9f0426b20999866c7edce9557fe1f7cfb7c14
scope.78.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3JlbnQ6MjU4
scope.78.kind=field
scope.78.startLine=258
scope.78.endLine=258
scope.78.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.79.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3RlbmFudDoyNTg
scope.79.kind=field
scope.79.startLine=258
scope.79.endLine=258
scope.79.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.80.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6MjQx
scope.80.kind=field
scope.80.startLine=241
scope.80.endLine=241
scope.80.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.81.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDoyNDE
scope.81.kind=field
scope.81.startLine=241
scope.81.endLine=241
scope.81.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.82.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6MjQ3
scope.82.kind=field
scope.82.startLine=247
scope.82.endLine=247
scope.82.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.83.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6MjQ3
scope.83.kind=field
scope.83.startLine=247
scope.83.endLine=247
scope.83.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.84.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6MjI4
scope.84.kind=field
scope.84.startLine=228
scope.84.endLine=228
scope.84.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.85.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjoyMzg
scope.85.kind=field
scope.85.startLine=238
scope.85.endLine=238
scope.85.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.86.id=ZmllbGQ6R2FtZS5Kb3VybmFsbGluZyNqb3VybmFsOjExOA
scope.86.kind=field
scope.86.startLine=118
scope.86.endLine=118
scope.86.semanticHash=85c5d503112071935eec23a4e6e40fde3bfadba06348aca287c46765762c68bf
scope.87.id=ZmllbGQ6R2FtZS5SZXN1bHQjZGVlZHM6MjA1
scope.87.kind=field
scope.87.startLine=205
scope.87.endLine=205
scope.87.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.88.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDoyMDU
scope.88.kind=field
scope.88.startLine=205
scope.88.endLine=205
scope.88.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.89.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjIwNQ
scope.89.kind=field
scope.89.startLine=205
scope.89.endLine=205
scope.89.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.90.id=bWV0aG9kOkdhbWUjY3RvcigyKTo2OQ
scope.90.kind=method
scope.90.startLine=69
scope.90.endLine=71
scope.90.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.91.id=bWV0aG9kOkdhbWUjY3RvcigzKTo2NA
scope.91.kind=method
scope.91.startLine=64
scope.91.endLine=66
scope.91.semanticHash=6776d3f993630076b24a0ed0b3bc39a8d8cf1fb2c29184dba3c568e657a2a980
scope.92.id=bWV0aG9kOkdhbWUjY3RvcigzKTo3NA
scope.92.kind=method
scope.92.startLine=74
scope.92.endLine=76
scope.92.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.93.id=bWV0aG9kOkdhbWUjY3Rvcig0KTo0NA
scope.93.kind=method
scope.93.startLine=44
scope.93.endLine=46
scope.93.semanticHash=70ea6ffce64adc633a1387d925355af5df30f677a7752d6107e2cac43a44a6c6
scope.94.id=bWV0aG9kOkdhbWUjY3Rvcig1KTo0OA
scope.94.kind=method
scope.94.startLine=48
scope.94.endLine=50
scope.94.semanticHash=11747e7df725e800061a8402d26c10ab242af8ec49ec7a226b2770f70f66d615
scope.95.id=bWV0aG9kOkdhbWUjY3Rvcig2KTo1Mg
scope.95.kind=method
scope.95.startLine=52
scope.95.endLine=61
scope.95.semanticHash=a4f75123ff728341e8dbec0db30a63204bd892251f4c5b1b38f8520475e9cdcb
scope.96.id=bWV0aG9kOkdhbWUjaWRzKDEpOjE5Ng
scope.96.kind=method
scope.96.startLine=196
scope.96.endLine=198
scope.96.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.97.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6OTU
scope.97.kind=method
scope.97.startLine=95
scope.97.endLine=99
scope.97.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.98.id=bWV0aG9kOkdhbWUjbGFuZGluZ3NGb3IoMyk6MTA2
scope.98.kind=method
scope.98.startLine=106
scope.98.endLine=115
scope.98.semanticHash=ed6c8d54ecf501776083de06cd088cc905d117860ce8bef8f816ab590c9dcf95
scope.99.id=bWV0aG9kOkdhbWUjcGxheSgwKTo3OA
scope.99.kind=method
scope.99.startLine=78
scope.99.endLine=93
scope.99.semanticHash=85c8462534242fd3abf29ef4e54f6b6b8090366a3cb1e4dc8ceb0749989a78dd
scope.100.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oNCk6MTAx
scope.100.kind=method
scope.100.startLine=101
scope.100.endLine=104
scope.100.semanticHash=4debec5e860dda014b6143b6d72e27d9e98fe9b718222e4b73b5f78064fedea9
scope.101.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6MTkz
scope.101.kind=method
scope.101.startLine=193
scope.101.endLine=193
scope.101.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.102.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjIwOA
scope.102.kind=method
scope.102.startLine=1
scope.102.endLine=292
scope.102.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.103.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjIxOA
scope.103.kind=method
scope.103.startLine=218
scope.103.endLine=220
scope.103.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.104.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6MjEz
scope.104.kind=method
scope.104.startLine=213
scope.104.endLine=216
scope.104.semanticHash=b5421e8cb3d5bc8c502ce4d321cdeecd90548b5faa054fb99b324edef550d319
scope.105.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5BdWN0aW9uV29uI2N0b3IoMyk6MjU1
scope.105.kind=method
scope.105.startLine=1
scope.105.endLine=292
scope.105.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.106.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUGFpZCNjdG9yKDIpOjI4OA
scope.106.kind=method
scope.106.startLine=1
scope.106.endLine=292
scope.106.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.107.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Cb3VnaHQjY3RvcigzKToyNTE
scope.107.kind=method
scope.107.startLine=1
scope.107.endLine=292
scope.107.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.108.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CdWlsZGluZ1JlZnVzZWQjY3RvcigzKToyNzk
scope.108.kind=method
scope.108.startLine=1
scope.108.endLine=292
scope.108.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.109.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5DaGFuY2VDYXJkRHJhd24jY3RvcigyKToyODI
scope.109.kind=method
scope.109.startLine=1
scope.109.endLine=292
scope.109.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.110.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Db21tdW5pdHlDaGVzdENhcmREcmF3biNjdG9yKDIpOjI4NQ
scope.110.kind=method
scope.110.startLine=1
scope.110.endLine=292
scope.110.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.111.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZUJ1aWx0I2N0b3IoMyk6MjYx
scope.111.kind=method
scope.111.startLine=1
scope.111.endLine=292
scope.111.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.112.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZVNvbGQjY3RvcigzKToyNjQ
scope.112.kind=method
scope.112.startLine=1
scope.112.endLine=292
scope.112.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.113.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjIzMg
scope.113.kind=method
scope.113.startLine=1
scope.113.endLine=292
scope.113.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.114.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6MjM1
scope.114.kind=method
scope.114.startLine=1
scope.114.endLine=292
scope.114.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.115.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU2FsZVJlZnVzZWQjY3Rvcig0KToyNzY
scope.115.kind=method
scope.115.startLine=1
scope.115.endLine=292
scope.115.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.116.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU29sZCNjdG9yKDQpOjI3Mw
scope.116.kind=method
scope.116.startLine=1
scope.116.endLine=292
scope.116.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.117.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUxpZnRlZCNjdG9yKDQpOjI3MA
scope.117.kind=method
scope.117.startLine=1
scope.117.endLine=292
scope.117.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.118.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZWQjY3RvcigzKToyNjc
scope.118.kind=method
scope.118.startLine=1
scope.118.endLine=292
scope.118.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.119.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjI0NA
scope.119.kind=method
scope.119.startLine=1
scope.119.endLine=292
scope.119.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.120.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5SZW50UGFpZCNjdG9yKDQpOjI1OA
scope.120.kind=method
scope.120.startLine=1
scope.120.endLine=292
scope.120.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.121.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKToyNDE
scope.121.kind=method
scope.121.startLine=1
scope.121.endLine=292
scope.121.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.122.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKToyNDc
scope.122.kind=method
scope.122.startLine=1
scope.122.endLine=292
scope.122.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.123.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjIyOA
scope.123.kind=method
scope.123.startLine=1
scope.123.endLine=292
scope.123.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.124.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDEpOjIzOA
scope.124.kind=method
scope.124.startLine=1
scope.124.endLine=292
scope.124.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.125.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYm91Z2h0KDMpOjEzNQ
scope.125.kind=method
scope.125.startLine=135
scope.125.endLine=138
scope.125.semanticHash=79e1d988b41c146a0d4c76c5bf1404dcfb5f43bf5ed7caac3fd2b5b0fbd2c437
scope.126.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYnVpbHRIb3VzZSgzKToxNTA
scope.126.kind=method
scope.126.startLine=150
scope.126.endLine=153
scope.126.semanticHash=e51ffaaf9fc64c2ff825668ffee31babc9a49fd98e53b320a973887332b1074d
scope.127.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY29sbGVjdGVkU2FsYXJ5KDIpOjEzMA
scope.127.kind=method
scope.127.startLine=130
scope.127.endLine=133
scope.127.semanticHash=9d31c851d99e8df553fdaf39330dc1ae11e0fe903b61f6b97b858c59389d5411
scope.128.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY3RvcigxKToxMTg
scope.128.kind=method
scope.128.startLine=1
scope.128.endLine=292
scope.128.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
scope.129.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZHJld0NoYW5jZUNhcmQoMik6MTcw
scope.129.kind=method
scope.129.startLine=170
scope.129.endLine=173
scope.129.semanticHash=c2d3dd8c5dd528d5bf8090da5f0547757d08ffc07fd3f699588877b9ab2cc644
scope.130.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZHJld0NvbW11bml0eUNoZXN0Q2FyZCgyKToxNzU
scope.130.kind=method
scope.130.startLine=175
scope.130.endLine=178
scope.130.semanticHash=11d7ba10463c79d04b3ea80df07002fc939392f73649bbcb263b0c8ef1bc1e6a
scope.131.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW92ZWQoMyk6MTI1
scope.131.kind=method
scope.131.startLine=125
scope.131.endLine=128
scope.131.semanticHash=624e9fcd1bcf33ec8f097872d5cf6e59f4e27c7ca7a686a5b60d818f937efc7b
scope.132.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZCg0KToxNDU
scope.132.kind=method
scope.132.startLine=145
scope.132.endLine=148
scope.132.semanticHash=66317d89046f5bdcdf22cb407d9a450e9f7221f4020da0e087ac3b105a7beaa8
scope.133.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZEJhbmsoMik6MTgw
scope.133.kind=method
scope.133.startLine=180
scope.133.endLine=183
scope.133.semanticHash=68b8289c6b9caa436a850d29ac9f703de981f579f49fc4af396225097d422309
scope.134.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcmVmdXNlZEJ1aWxkaW5nKDMpOjE2NQ
scope.134.kind=method
scope.134.startLine=165
scope.134.endLine=168
scope.134.semanticHash=bc9150e16e6d26cf9949ae96894cd793c0d87ac4b6c0fb087c080025dd60a3a8
scope.135.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcm9sbGVkKDIpOjEyMA
scope.135.kind=method
scope.135.startLine=120
scope.135.endLine=123
scope.135.semanticHash=41570b54acd7a2a0ee9f75bcefcdd79248dd7db4784b77cea66150ac4e5a790f
scope.136.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc2FsZVJlZnVzZWQoNCk6MTYw
scope.136.kind=method
scope.136.startLine=160
scope.136.endLine=163
scope.136.semanticHash=902eb0534ab31b9b916eb8f3fd7fb549f669e096152fce279c949a5029c28717
scope.137.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc29sZCg0KToxNTU
scope.137.kind=method
scope.137.startLine=155
scope.137.endLine=158
scope.137.semanticHash=36ceffd86df9fb98c3fdd440c3cda480841b4012d082ae4a65009180a250f049
scope.138.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uQXRBdWN0aW9uKDMpOjE0MA
scope.138.kind=method
scope.138.startLine=140
scope.138.endLine=143
scope.138.semanticHash=db10ed18596729bcd7577e23594ec8451323564e17dc73c245ab874c151f6569
scope.139.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoMyk6MjA1
scope.139.kind=method
scope.139.startLine=1
scope.139.endLine=292
scope.139.semanticHash=feda50f59669cd87d6cf51d012123d8c40e3db5400a81830e4f05b5e5d42420c
*/
