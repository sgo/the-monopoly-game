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
import the.monopoly.game.rules.Bankruptcy;
import the.monopoly.game.rules.Cards;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.Jail;
import the.monopoly.game.rules.LandSale;
import the.monopoly.game.rules.Landings;
import the.monopoly.game.rules.Rent;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Taxes;
import the.monopoly.game.rules.Turn;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

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
  private final Jail jail;

  public Game(Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies) {
    this(rules, players, cups, strategies, new Deeds(), null);
  }

  public Game(Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds) {
    this(rules, players, cups, strategies, deeds, null);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds, Cards.Decks decks
  ) {
    this(rules, players, cups, strategies, deeds,
        decks == null ? Cards.Decks.official(deeds) : decks, new Jail(rules));
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds,
      Cards.Decks decks, Jail jail
  ) {
    this.rules = rules;
    this.players = players;
    this.cups = cups;
    this.strategies = strategies;
    this.deeds = deeds;
    this.decks = decks;
    this.jail = jail;
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
    return play(false, () -> true);
  }

  /** Plays successive turns until only one player remains at the table. */
  public Result playToCompletion() {
    return play(true, () -> true);
  }

  /**
   * Plays successive turns until the game is told to stop or only one player
   * remains at the table. Stopping is cooperative: the game finishes the round
   * it is on and then stops, however long the game would still have gone on.
   */
  public Result playUntilStopped(BooleanSupplier keepPlaying) {
    return play(true, keepPlaying);
  }

  private Result play(boolean untilComplete, BooleanSupplier keepPlaying) {
    var journal = new Journal();
    journal.log(new Journal.Entry.Start(ids(players)));
    List<Player> turnOrder = new Initiative(player -> initiativeRollFor(player, journal)).order(players);
    journal.log(new Journal.Entry.InitiativeWon(turnOrder.getFirst().id()));

    Journalling journalling = new Journalling(journal);
    jail.observe(journalling);
    Building building = new Building(deeds, rules, strategies, journalling);
    Player builder = turnOrder.getFirst();
    playTurns(turnOrder, builder, journal, journalling, building, untilComplete, keepPlaying);

    return new Result(turnOrder, journal.entries(), deeds, winner());
  }

  private void playTurns(List<Player> turnOrder, Player builder, Journal journal,
                         Journalling journalling, Building building, boolean untilComplete,
                         BooleanSupplier keepPlaying) {
    do {
      for (Player player : turnOrder) {
        if (playTurn(player, builder, turnOrder, journal, journalling, building)) break;
      }
    } while (untilComplete && keepPlaying.getAsBoolean() && remainingPlayers().size() > 1);
  }

  private boolean playTurn(Player player, Player builder, List<Player> turnOrder, Journal journal,
                           Journalling journalling, Building building) {
    if (deeds.isBankrupt(player)) return false;
    takeTurn(player, journal, journalling, landingsFor(player, turnOrder, journalling));
    if (player.id().equals(builder.id()) && !deeds.isBankrupt(player)) building.develop(player);
    return remainingPlayers().size() <= 1;
  }

  private List<Player> remainingPlayers() {
    return players.stream().filter(player -> !deeds.isBankrupt(player)).toList();
  }

  private Optional<Player> winner() {
    List<Player> remaining = remainingPlayers();
    return remaining.size() == 1 ? Optional.of(remaining.getFirst()) : Optional.empty();
  }

  private int initiativeRollFor(Player player, Journal journal) {
    int total = cups.forPlayer(player).roll().total();
    journal.log(new Journal.Entry.InitiativeRoll(player.id(), total));
    return total;
  }

  private void takeTurn(Player player, Journal journal, Turn.Events events, Landings landings) {
    journal.log(new Journal.Entry.TurnStarted(player.id(), player.account().balance().amount()));
    new Turn(rules, cups.forPlayer(player), events, landings, jail, strategies.forPlayer(player), deeds).take(player);
  }

  private Landings landingsFor(Player player, List<Player> turnOrder, Journalling journalling) {
    Landings rent = new Rent(deeds, rules, turnOrder, strategies, journalling);
    Landings landSale = new LandSale(deeds, rules, turnOrder, strategies, journalling);
    Landings cards = new Cards(deeds, rules, turnOrder, strategies, decks, journalling, cups.forPlayer(player), jail);
    Landings taxes = new Taxes(journalling);
    Bankruptcy bankruptcy = new Bankruptcy(deeds, rules, turnOrder, strategies, journalling);
    return (who, space, roll) -> {
      rent.resolve(who, space, roll);
      landSale.resolve(who, space, roll);
      cards.resolve(who, space, roll);
      taxes.resolve(who, space, roll);
      jail.resolve(who, space, roll);
      Player creditor = space instanceof Ownable land
          ? deeds.ownerOf(land.type()).filter(id -> !id.equals(who.id()))
              .flatMap(id -> turnOrder.stream().filter(it -> it.id().equals(id)).findFirst()).orElse(null)
          : null;
      bankruptcy.resolve(who, creditor);
    };
  }

  /** Writes down what a turn and a sale say they did, as the game's account of it. */
  private record Journalling(Journal journal)
      implements Turn.Events, LandSale.Events, Rent.Events, Building.Events, Cards.Events, Taxes.Events, Jail.Events, Bankruptcy.Events {
    @Override
    public void rolled(Player player, Roll roll) {
      journal.log(new Journal.Entry.Rolled(player.id(), roll.total()));
    }

    @Override
    public void moved(Player player, int from, int to, Street.Type fromSpace, Street.Type toSpace) {
      journal.log(new Journal.Entry.Moved(player.id(), from, to, fromSpace, toSpace));
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
    public void paid(Player payer, Player payee, Money amount) {
      journal.log(new Journal.Entry.PlayerPaid(payer.id(), payee.id(), amount));
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

    @Override
    public void sentToJail(Player player, Street.Type cause) {
      journal.log(new Journal.Entry.JailEntered(player.id(), cause));
    }

    @Override
    public void leftJailByPaying(Player player, Money fine) {
      journal.log(new Journal.Entry.JailFinePaid(player.id(), fine));
    }

    @Override
    public void bankrupt(Player debtor, Player creditor) {
      journal.log(new Journal.Entry.Bankrupt(debtor.id(), creditor == null ? null : creditor.id()));
    }

    @Override
    public void won(Player player) {
      journal.log(new Journal.Entry.Won(player.id()));
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
  public record Result(List<Player> turnOrder, List<Journal.Entry> journal, Deeds deeds, Optional<Player> winner) {
  }

  public static class Journal {
    private static final Logger logger = getLogger(Journal.class);

    private final List<Entry> entries = new ArrayList<>();

    public void log(Entry evt) {
      entries.add(evt);
      logger.info(Report.of(List.of(evt)), evt);
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

      /** The pawn's account balance is carried at this point so the turn's starting money is on record. */
      record TurnStarted(Player.ID player, Money balance) implements Entry {
      }

      record Rolled(Player.ID player, int total) implements Entry {
      }

      record Moved(Player.ID player, int from, int to, Street.Type fromSpace, Street.Type toSpace) implements Entry {
        public Moved(Player.ID player, int from, int to) {
          this(player, from, to, officialSpaceAt(from), officialSpaceAt(to));
        }

        private static Street.Type officialSpaceAt(int position) {
          return Rule.Set.Type.official.create().gameboard().layout().get(position);
        }
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

      record PlayerPaid(Player.ID payer, Player.ID payee, Money amount) implements Entry {
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

      record JailEntered(Player.ID player, Street.Type cause) implements Entry {
      }

      record JailFinePaid(Player.ID player, Money fine) implements Entry {
      }

      record Bankrupt(Player.ID player, Player.ID creditor) implements Entry {
      }

      record Won(Player.ID player) implements Entry {
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=445d07da3491fde92daa86c9f046b96df843cd2f88043747b14b4cb3bed81f3d
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjQx
scope.0.kind=class
scope.0.startLine=41
scope.0.endLine=406
scope.0.semanticHash=dc7074e2245ae9b2f4c5cc9c60329ac6be0cf8142ac757929ca0097f94e8f3b0
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6Mjgy
scope.1.kind=class
scope.1.startLine=282
scope.1.endLine=285
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6Mjk5
scope.2.kind=class
scope.2.startLine=299
scope.2.endLine=405
scope.2.semanticHash=8cd91c041c48c93dd0f61f1f7153f6d5b2eea55109b113da93d5cbafb8e385c5
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjMxOA
scope.3.kind=class
scope.3.startLine=318
scope.3.endLine=404
scope.3.semanticHash=a5bd2ce6da805ec5ec59042feefba750bda306c453db8510d20994a8771ba2ad
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jQXVjdGlvbldvbjozNTQ
scope.4.kind=class
scope.4.startLine=354
scope.4.endLine=355
scope.4.semanticHash=71ee77afd5451e73d54900eb221aa9640cfe5a2b42680f570ad5efdf9e473cb9
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI0JhbmtQYWlkOjM5MA
scope.5.kind=class
scope.5.startLine=390
scope.5.endLine=391
scope.5.semanticHash=cce5d5e05e58aed8d75d4ca2deb97d6d8388449cd41d7b4bc422482a269ff5b4
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I0JhbmtydXB0OjM5OQ
scope.6.kind=class
scope.6.startLine=399
scope.6.endLine=400
scope.6.semanticHash=16825b9c28c79a36f8a880d0adc21014ea4b665f40f0fb2eb70ef7ece3155e0b
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNCb3VnaHQ6MzUw
scope.7.kind=class
scope.7.startLine=350
scope.7.endLine=351
scope.7.semanticHash=27025028a74b83acd66fd8557558fb235025cf183bcc4e3623183b26da003aec
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNCdWlsZGluZ1JlZnVzZWQ6Mzgx
scope.8.kind=class
scope.8.startLine=381
scope.8.endLine=382
scope.8.semanticHash=c46235e296f36ff19ba4b74246e14db6236dced240c6de450bc4186fef89d59b
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNDaGFuY2VDYXJkRHJhd246Mzg0
scope.9.kind=class
scope.9.startLine=384
scope.9.endLine=385
scope.9.semanticHash=a926e22751d95373a632fb1725c91e97b535d7cf2eb62c29d7d515570e3e3344
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI0NvbW11bml0eUNoZXN0Q2FyZERyYXduOjM4Nw
scope.10.kind=class
scope.10.startLine=387
scope.10.endLine=388
scope.10.semanticHash=3a1c04eb7d87bf7ea391ae3f28c5fecb5d887626ebce02e8f7ab581a4ce85290
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjSG91c2VCdWlsdDozNjM
scope.11.kind=class
scope.11.startLine=363
scope.11.endLine=364
scope.11.semanticHash=27575972b2787c07a6fa98a725c5bed4487591e647678d5880869a7c637aee97
scope.12.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNIb3VzZVNvbGQ6MzY2
scope.12.kind=class
scope.12.startLine=366
scope.12.endLine=367
scope.12.semanticHash=7bfd22802262e4e36e5ffa57b44a79dc928c71eaa883b7f110cd1b9e7f7230b2
scope.13.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjMyMw
scope.13.kind=class
scope.13.startLine=323
scope.13.endLine=324
scope.13.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.14.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjozMjY
scope.14.kind=class
scope.14.startLine=326
scope.14.endLine=327
scope.14.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.15.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI0phaWxFbnRlcmVkOjM5Mw
scope.15.kind=class
scope.15.startLine=393
scope.15.endLine=394
scope.15.semanticHash=72be50c2af861ce8f500dde65a8bfa767ceb307161772739134a6968fd43b907
scope.16.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNKYWlsRmluZVBhaWQ6Mzk2
scope.16.kind=class
scope.16.startLine=396
scope.16.endLine=397
scope.16.semanticHash=88ed119890c309e1480e4400ae91bdb79250c1dd70940253050a847641cb4283
scope.17.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNMYW5kU2FsZVJlZnVzZWQ6Mzc4
scope.17.kind=class
scope.17.startLine=378
scope.17.endLine=379
scope.17.semanticHash=071a81b29122c94c28ba6cee1630b23eeb7b6acc5cf69c07a622eb04893d79ef
scope.18.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI0xhbmRTb2xkOjM3NQ
scope.18.kind=class
scope.18.startLine=375
scope.18.endLine=376
scope.18.semanticHash=fd9ac419a61f440251c0473ce96aa04eb9d1e3761c450d6e7d55b0f1d92a105e
scope.19.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI01vcnRnYWdlTGlmdGVkOjM3Mg
scope.19.kind=class
scope.19.startLine=372
scope.19.endLine=373
scope.19.semanticHash=876fc18a90cbc579ad9618bc95f03fa33b7cdeae3c7f18f0ead53f7795f64237
scope.20.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNNb3J0Z2FnZWQ6MzY5
scope.20.kind=class
scope.20.startLine=369
scope.20.endLine=370
scope.20.semanticHash=2536842d77794ad82293557093f6b95391662318c0cf866281f0bfeb54b47212
scope.21.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjMzNg
scope.21.kind=class
scope.21.startLine=336
scope.21.endLine=344
scope.21.semanticHash=ed37919856542e0d29f91d0622487a42cbe6023a70d3c23b3950fc66a5e8f1ab
scope.22.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjUGxheWVyUGFpZDozNjA
scope.22.kind=class
scope.22.startLine=360
scope.22.endLine=361
scope.22.semanticHash=ecda18178391ece7e75c3e72ec3f854adff15a3950fc135b48bdf7e6cb119a23
scope.23.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI1JlbnRQYWlkOjM1Nw
scope.23.kind=class
scope.23.startLine=357
scope.23.endLine=358
scope.23.semanticHash=47c40c5b19c0d8df73eda2478d761c7a7e8604713b0719926b1f46a2f2b6f104
scope.24.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6MzMz
scope.24.kind=class
scope.24.startLine=333
scope.24.endLine=334
scope.24.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.25.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6MzQ2
scope.25.kind=class
scope.25.startLine=346
scope.25.endLine=347
scope.25.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.26.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjMxOQ
scope.26.kind=class
scope.26.startLine=319
scope.26.endLine=320
scope.26.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.27.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjMzMA
scope.27.kind=class
scope.27.startLine=330
scope.27.endLine=331
scope.27.semanticHash=41e90d0f67674d5f95b45c4e275f7e8bb77e718742dd61c81f3d9281a6811e44
scope.28.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNXb246NDAy
scope.28.kind=class
scope.28.startLine=402
scope.28.endLine=403
scope.28.semanticHash=1018a3f41b5571c335e5fbf1476a6a3112c2284616837f2e0c7fbd00dd3d8b76
scope.29.id=Y2xhc3M6R2FtZS5Kb3VybmFsbGluZyNKb3VybmFsbGluZzoxODQ
scope.29.kind=class
scope.29.startLine=184
scope.29.endLine=275
scope.29.semanticHash=b8c65dadc895d31d04fad01f2cafaf1f89cc02c5aa6203c865dc194ce20d309c
scope.30.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjI5Ng
scope.30.kind=class
scope.30.startLine=296
scope.30.endLine=297
scope.30.semanticHash=024a5de82b58c6e09d33d689b003f51dcd43a63a5e94cb88b5d8b96d1706df96
scope.31.id=ZmllbGQ6R2FtZSNjdXBzOjQ0
scope.31.kind=field
scope.31.startLine=44
scope.31.endLine=44
scope.31.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.32.id=ZmllbGQ6R2FtZSNkZWNrczo0Nw
scope.32.kind=field
scope.32.startLine=47
scope.32.endLine=47
scope.32.semanticHash=130541f31392b2fc32d3c8343ebd76de366010b8930395e22ba244946f508252
scope.33.id=ZmllbGQ6R2FtZSNkZWVkczo0Ng
scope.33.kind=field
scope.33.startLine=46
scope.33.endLine=46
scope.33.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.34.id=ZmllbGQ6R2FtZSNqYWlsOjQ4
scope.34.kind=field
scope.34.startLine=48
scope.34.endLine=48
scope.34.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.35.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjQz
scope.35.kind=field
scope.35.startLine=43
scope.35.endLine=43
scope.35.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.36.id=ZmllbGQ6R2FtZSNydWxlczo0Mg
scope.36.kind=field
scope.36.startLine=42
scope.36.endLine=42
scope.36.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.37.id=ZmllbGQ6R2FtZSNzdHJhdGVnaWVzOjQ1
scope.37.kind=field
scope.37.startLine=45
scope.37.endLine=45
scope.37.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.38.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6MzAy
scope.38.kind=field
scope.38.startLine=302
scope.38.endLine=302
scope.38.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.39.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjozMDA
scope.39.kind=field
scope.39.startLine=300
scope.39.endLine=300
scope.39.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.40.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jbGFuZDozNTQ
scope.40.kind=field
scope.40.startLine=354
scope.40.endLine=354
scope.40.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.41.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcGxheWVyOjM1NA
scope.41.kind=field
scope.41.startLine=354
scope.41.endLine=354
scope.41.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.42.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcHJpY2U6MzU0
scope.42.kind=field
scope.42.startLine=354
scope.42.endLine=354
scope.42.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.43.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI2Ftb3VudDozOTA
scope.43.kind=field
scope.43.startLine=390
scope.43.endLine=390
scope.43.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.44.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI3BsYXllcjozOTA
scope.44.kind=field
scope.44.startLine=390
scope.44.endLine=390
scope.44.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.45.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I2NyZWRpdG9yOjM5OQ
scope.45.kind=field
scope.45.startLine=399
scope.45.endLine=399
scope.45.semanticHash=04806e2a3ca47061887c26b1a6e5df08f09b4b4e10f22dac41fe60a342b7338b
scope.46.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I3BsYXllcjozOTk
scope.46.kind=field
scope.46.startLine=399
scope.46.endLine=399
scope.46.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.47.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNsYW5kOjM1MA
scope.47.kind=field
scope.47.startLine=350
scope.47.endLine=350
scope.47.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.48.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwbGF5ZXI6MzUw
scope.48.kind=field
scope.48.startLine=350
scope.48.endLine=350
scope.48.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.49.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwcmljZTozNTA
scope.49.kind=field
scope.49.startLine=350
scope.49.endLine=350
scope.49.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.50.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNsYW5kOjM4MQ
scope.50.kind=field
scope.50.startLine=381
scope.50.endLine=381
scope.50.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.51.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwbGF5ZXI6Mzgx
scope.51.kind=field
scope.51.startLine=381
scope.51.endLine=381
scope.51.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.52.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwcmljZTozODE
scope.52.kind=field
scope.52.startLine=381
scope.52.endLine=381
scope.52.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.53.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNjYXJkOjM4NA
scope.53.kind=field
scope.53.startLine=384
scope.53.endLine=384
scope.53.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.54.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNwbGF5ZXI6Mzg0
scope.54.kind=field
scope.54.startLine=384
scope.54.endLine=384
scope.54.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.55.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI2NhcmQ6Mzg3
scope.55.kind=field
scope.55.startLine=387
scope.55.endLine=387
scope.55.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.56.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI3BsYXllcjozODc
scope.56.kind=field
scope.56.startLine=387
scope.56.endLine=387
scope.56.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.57.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjbGFuZDozNjM
scope.57.kind=field
scope.57.startLine=363
scope.57.endLine=363
scope.57.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.58.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcGxheWVyOjM2Mw
scope.58.kind=field
scope.58.startLine=363
scope.58.endLine=363
scope.58.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.59.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcHJpY2U6MzYz
scope.59.kind=field
scope.59.startLine=363
scope.59.endLine=363
scope.59.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.60.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNsYW5kOjM2Ng
scope.60.kind=field
scope.60.startLine=366
scope.60.endLine=366
scope.60.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.61.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwbGF5ZXI6MzY2
scope.61.kind=field
scope.61.startLine=366
scope.61.endLine=366
scope.61.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.62.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwcmljZTozNjY
scope.62.kind=field
scope.62.startLine=366
scope.62.endLine=366
scope.62.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.63.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjozMjM
scope.63.kind=field
scope.63.startLine=323
scope.63.endLine=323
scope.63.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.64.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjMyMw
scope.64.kind=field
scope.64.startLine=323
scope.64.endLine=323
scope.64.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.65.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjMyNg
scope.65.kind=field
scope.65.startLine=326
scope.65.endLine=326
scope.65.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.66.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI2NhdXNlOjM5Mw
scope.66.kind=field
scope.66.startLine=393
scope.66.endLine=393
scope.66.semanticHash=f0527b6e66e3f950052646384f5a7c874593c4a39f0fb849428625529221f17b
scope.67.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI3BsYXllcjozOTM
scope.67.kind=field
scope.67.startLine=393
scope.67.endLine=393
scope.67.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.68.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNmaW5lOjM5Ng
scope.68.kind=field
scope.68.startLine=396
scope.68.endLine=396
scope.68.semanticHash=e365883bc779c1a28df50988a532d020ef388a587ebb4e7379f58733cfb94b21
scope.69.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNwbGF5ZXI6Mzk2
scope.69.kind=field
scope.69.startLine=396
scope.69.endLine=396
scope.69.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.70.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNidXllcjozNzg
scope.70.kind=field
scope.70.startLine=378
scope.70.endLine=378
scope.70.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.71.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNsYW5kOjM3OA
scope.71.kind=field
scope.71.startLine=378
scope.71.endLine=378
scope.71.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.72.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNwcmljZTozNzg
scope.72.kind=field
scope.72.startLine=378
scope.72.endLine=378
scope.72.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.73.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNzZWxsZXI6Mzc4
scope.73.kind=field
scope.73.startLine=378
scope.73.endLine=378
scope.73.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.74.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2J1eWVyOjM3NQ
scope.74.kind=field
scope.74.startLine=375
scope.74.endLine=375
scope.74.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.75.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2xhbmQ6Mzc1
scope.75.kind=field
scope.75.startLine=375
scope.75.endLine=375
scope.75.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.76.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3ByaWNlOjM3NQ
scope.76.kind=field
scope.76.startLine=375
scope.76.endLine=375
scope.76.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.77.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3NlbGxlcjozNzU
scope.77.kind=field
scope.77.startLine=375
scope.77.endLine=375
scope.77.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.78.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2ludGVyZXN0OjM3Mg
scope.78.kind=field
scope.78.startLine=372
scope.78.endLine=372
scope.78.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.79.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2xhbmQ6Mzcy
scope.79.kind=field
scope.79.startLine=372
scope.79.endLine=372
scope.79.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.80.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3BsYXllcjozNzI
scope.80.kind=field
scope.80.startLine=372
scope.80.endLine=372
scope.80.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.81.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3RvdGFsOjM3Mg
scope.81.kind=field
scope.81.startLine=372
scope.81.endLine=372
scope.81.semanticHash=c19b9ea10da3846e1c37942dc47f59b13649fda020125cea6df5d3a470490575
scope.82.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNsYW5kOjM2OQ
scope.82.kind=field
scope.82.startLine=369
scope.82.endLine=369
scope.82.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.83.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNwbGF5ZXI6MzY5
scope.83.kind=field
scope.83.startLine=369
scope.83.endLine=369
scope.83.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.84.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCN2YWx1ZTozNjk
scope.84.kind=field
scope.84.startLine=369
scope.84.endLine=369
scope.84.semanticHash=0d022e0e6113de33cad355012469518c3a21832e276a9c3f3a6893880214fa0a
scope.85.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206MzM2
scope.85.kind=field
scope.85.startLine=336
scope.85.endLine=336
scope.85.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.86.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb21TcGFjZTozMzY
scope.86.kind=field
scope.86.startLine=336
scope.86.endLine=336
scope.86.semanticHash=fdcd833bf3c0613749af9aa35feb23fbe7068c7d720cdb3a09bbbebeefbe4e7c
scope.87.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjozMzY
scope.87.kind=field
scope.87.startLine=336
scope.87.endLine=336
scope.87.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.88.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjMzNg
scope.88.kind=field
scope.88.startLine=336
scope.88.endLine=336
scope.88.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.89.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvU3BhY2U6MzM2
scope.89.kind=field
scope.89.startLine=336
scope.89.endLine=336
scope.89.semanticHash=061c4ba46bf16ef78d0e00d27fbe750d73f969cccf700678171eb04b70eab629
scope.90.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjYW1vdW50OjM2MA
scope.90.kind=field
scope.90.startLine=360
scope.90.endLine=360
scope.90.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.91.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZWU6MzYw
scope.91.kind=field
scope.91.startLine=360
scope.91.endLine=360
scope.91.semanticHash=289e0e3b44081936aa54790d8a0cebe895c1f5ad30a87142f45cb4ec4a20f8d3
scope.92.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZXI6MzYw
scope.92.kind=field
scope.92.startLine=360
scope.92.endLine=360
scope.92.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.93.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI2xhbmQ6MzU3
scope.93.kind=field
scope.93.startLine=357
scope.93.endLine=357
scope.93.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.94.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI293bmVyOjM1Nw
scope.94.kind=field
scope.94.startLine=357
scope.94.endLine=357
scope.94.semanticHash=4dc1b9a409163bdb6ca915d1f2c9f0426b20999866c7edce9557fe1f7cfb7c14
scope.95.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3JlbnQ6MzU3
scope.95.kind=field
scope.95.startLine=357
scope.95.endLine=357
scope.95.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.96.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3RlbmFudDozNTc
scope.96.kind=field
scope.96.startLine=357
scope.96.endLine=357
scope.96.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.97.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6MzMz
scope.97.kind=field
scope.97.startLine=333
scope.97.endLine=333
scope.97.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.98.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDozMzM
scope.98.kind=field
scope.98.startLine=333
scope.98.endLine=333
scope.98.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.99.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6MzQ2
scope.99.kind=field
scope.99.startLine=346
scope.99.endLine=346
scope.99.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.100.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6MzQ2
scope.100.kind=field
scope.100.startLine=346
scope.100.endLine=346
scope.100.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.101.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6MzE5
scope.101.kind=field
scope.101.startLine=319
scope.101.endLine=319
scope.101.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.102.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI2JhbGFuY2U6MzMw
scope.102.kind=field
scope.102.startLine=330
scope.102.endLine=330
scope.102.semanticHash=9ba2008e7dc4127b70833455dbf24f667e36899897c47cbf225bbc8bd4f5575a
scope.103.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjozMzA
scope.103.kind=field
scope.103.startLine=330
scope.103.endLine=330
scope.103.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.104.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNwbGF5ZXI6NDAy
scope.104.kind=field
scope.104.startLine=402
scope.104.endLine=402
scope.104.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.105.id=ZmllbGQ6R2FtZS5Kb3VybmFsbGluZyNqb3VybmFsOjE4NA
scope.105.kind=field
scope.105.startLine=184
scope.105.endLine=184
scope.105.semanticHash=85c5d503112071935eec23a4e6e40fde3bfadba06348aca287c46765762c68bf
scope.106.id=ZmllbGQ6R2FtZS5SZXN1bHQjZGVlZHM6Mjk2
scope.106.kind=field
scope.106.startLine=296
scope.106.endLine=296
scope.106.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.107.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDoyOTY
scope.107.kind=field
scope.107.startLine=296
scope.107.endLine=296
scope.107.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.108.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjI5Ng
scope.108.kind=field
scope.108.startLine=296
scope.108.endLine=296
scope.108.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.109.id=ZmllbGQ6R2FtZS5SZXN1bHQjd2lubmVyOjI5Ng
scope.109.kind=field
scope.109.startLine=296
scope.109.endLine=296
scope.109.semanticHash=9e05c00db702321e24ecb1c4429dea5328a65101957c7f0b7699f23ee7c539a9
scope.110.id=bWV0aG9kOkdhbWUjY3RvcigyKTo4Mw
scope.110.kind=method
scope.110.startLine=83
scope.110.endLine=85
scope.110.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.111.id=bWV0aG9kOkdhbWUjY3RvcigzKTo3OA
scope.111.kind=method
scope.111.startLine=78
scope.111.endLine=80
scope.111.semanticHash=6776d3f993630076b24a0ed0b3bc39a8d8cf1fb2c29184dba3c568e657a2a980
scope.112.id=bWV0aG9kOkdhbWUjY3RvcigzKTo4OA
scope.112.kind=method
scope.112.startLine=88
scope.112.endLine=90
scope.112.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.113.id=bWV0aG9kOkdhbWUjY3Rvcig0KTo1MA
scope.113.kind=method
scope.113.startLine=50
scope.113.endLine=52
scope.113.semanticHash=b071d0079eba2cc3a153e8433fb4919b47f465dfed5082bc52cd70af15066368
scope.114.id=bWV0aG9kOkdhbWUjY3Rvcig1KTo1NA
scope.114.kind=method
scope.114.startLine=54
scope.114.endLine=56
scope.114.semanticHash=c856955cc9871f7ab36e125edadacf9a5d055a0928af489d4e9897a5d9b89196
scope.115.id=bWV0aG9kOkdhbWUjY3Rvcig2KTo1OA
scope.115.kind=method
scope.115.startLine=58
scope.115.endLine=62
scope.115.semanticHash=c41bf9cfb6d4d7ea360ce1ac58cda7af9b7fb7e3fe143cc6add5a15c734e19af
scope.116.id=bWV0aG9kOkdhbWUjY3Rvcig3KTo2NA
scope.116.kind=method
scope.116.startLine=64
scope.116.endLine=75
scope.116.semanticHash=f52081f9941f6fd288e8813ed6efe9e1b2983823b59016b4e27f63a7aebbc788
scope.117.id=bWV0aG9kOkdhbWUjaWRzKDEpOjI4Nw
scope.117.kind=method
scope.117.startLine=287
scope.117.endLine=289
scope.117.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.118.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6MTUy
scope.118.kind=method
scope.118.startLine=152
scope.118.endLine=156
scope.118.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.119.id=bWV0aG9kOkdhbWUjbGFuZGluZ3NGb3IoMyk6MTYz
scope.119.kind=method
scope.119.startLine=163
scope.119.endLine=181
scope.119.semanticHash=288a76391f035403c868df024ced5feb15dc22a1a78a4cb2b8c51752854acc39
scope.120.id=bWV0aG9kOkdhbWUjcGxheSgwKTo5Mg
scope.120.kind=method
scope.120.startLine=92
scope.120.endLine=94
scope.120.semanticHash=3bcadbbb1f6b598fdb83fbc0fdd237a7656cc24edc1054185a280a4b7b46cb3b
scope.121.id=bWV0aG9kOkdhbWUjcGxheSgyKToxMTA
scope.121.kind=method
scope.121.startLine=110
scope.121.endLine=123
scope.121.semanticHash=b33495d9e1f85d2ecfdbf5a12eb1917000669bde817741180a76ea216bbb34db
scope.122.id=bWV0aG9kOkdhbWUjcGxheVRvQ29tcGxldGlvbigwKTo5Nw
scope.122.kind=method
scope.122.startLine=97
scope.122.endLine=99
scope.122.semanticHash=a60fc108488c55d28cf9d6828599290071eeae99381682b526b1392f2b106627
scope.123.id=bWV0aG9kOkdhbWUjcGxheVR1cm4oNik6MTM1
scope.123.kind=method
scope.123.startLine=135
scope.123.endLine=141
scope.123.semanticHash=14d1c67e1253523473d93ac0bdd8afc80e7a9f1495504c30d21e9fd7c6af7912
scope.124.id=bWV0aG9kOkdhbWUjcGxheVR1cm5zKDcpOjEyNQ
scope.124.kind=method
scope.124.startLine=125
scope.124.endLine=133
scope.124.semanticHash=88e73da11f64b7e7bdad3de9a8c45fd8e0fb36043acde7e08013b8294dc8e898
scope.125.id=bWV0aG9kOkdhbWUjcGxheVVudGlsU3RvcHBlZCgxKToxMDY
scope.125.kind=method
scope.125.startLine=106
scope.125.endLine=108
scope.125.semanticHash=2159cc9b2267372bf24f16472c20269d3d5376d0624e178122a5a131ef094b22
scope.126.id=bWV0aG9kOkdhbWUjcmVtYWluaW5nUGxheWVycygwKToxNDM
scope.126.kind=method
scope.126.startLine=143
scope.126.endLine=145
scope.126.semanticHash=a0e051c1b866b1352982334442d470d1567187f7e091423c51fc78cf3a6f2874
scope.127.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oNCk6MTU4
scope.127.kind=method
scope.127.startLine=158
scope.127.endLine=161
scope.127.semanticHash=f3ac8eb7431b23632198840bb1f5c7b7fd23c485c6d5c9cb181d517d711af0f7
scope.128.id=bWV0aG9kOkdhbWUjd2lubmVyKDApOjE0Nw
scope.128.kind=method
scope.128.startLine=147
scope.128.endLine=150
scope.128.semanticHash=702f44695db994b2e4908c5393ffd81fcd816cff000bc8cb31c6d97c66191345
scope.129.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6Mjg0
scope.129.kind=method
scope.129.startLine=284
scope.129.endLine=284
scope.129.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.130.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjI5OQ
scope.130.kind=method
scope.130.startLine=1
scope.130.endLine=406
scope.130.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.131.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjMwOQ
scope.131.kind=method
scope.131.startLine=309
scope.131.endLine=311
scope.131.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.132.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6MzA0
scope.132.kind=method
scope.132.startLine=304
scope.132.endLine=307
scope.132.semanticHash=f2f4e1f3c7bd7244a0e0a2e125110a27d8516e8cb7036d71c5cb73f65468d33f
scope.133.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5BdWN0aW9uV29uI2N0b3IoMyk6MzU0
scope.133.kind=method
scope.133.startLine=1
scope.133.endLine=406
scope.133.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.134.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUGFpZCNjdG9yKDIpOjM5MA
scope.134.kind=method
scope.134.startLine=1
scope.134.endLine=406
scope.134.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.135.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rcnVwdCNjdG9yKDIpOjM5OQ
scope.135.kind=method
scope.135.startLine=1
scope.135.endLine=406
scope.135.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.136.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Cb3VnaHQjY3RvcigzKTozNTA
scope.136.kind=method
scope.136.startLine=1
scope.136.endLine=406
scope.136.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.137.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CdWlsZGluZ1JlZnVzZWQjY3RvcigzKTozODE
scope.137.kind=method
scope.137.startLine=1
scope.137.endLine=406
scope.137.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.138.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5DaGFuY2VDYXJkRHJhd24jY3RvcigyKTozODQ
scope.138.kind=method
scope.138.startLine=1
scope.138.endLine=406
scope.138.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.139.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Db21tdW5pdHlDaGVzdENhcmREcmF3biNjdG9yKDIpOjM4Nw
scope.139.kind=method
scope.139.startLine=1
scope.139.endLine=406
scope.139.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.140.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZUJ1aWx0I2N0b3IoMyk6MzYz
scope.140.kind=method
scope.140.startLine=1
scope.140.endLine=406
scope.140.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.141.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZVNvbGQjY3RvcigzKTozNjY
scope.141.kind=method
scope.141.startLine=1
scope.141.endLine=406
scope.141.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.142.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjMyMw
scope.142.kind=method
scope.142.startLine=1
scope.142.endLine=406
scope.142.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.143.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6MzI2
scope.143.kind=method
scope.143.startLine=1
scope.143.endLine=406
scope.143.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.144.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRW50ZXJlZCNjdG9yKDIpOjM5Mw
scope.144.kind=method
scope.144.startLine=1
scope.144.endLine=406
scope.144.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.145.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRmluZVBhaWQjY3RvcigyKTozOTY
scope.145.kind=method
scope.145.startLine=1
scope.145.endLine=406
scope.145.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.146.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU2FsZVJlZnVzZWQjY3Rvcig0KTozNzg
scope.146.kind=method
scope.146.startLine=1
scope.146.endLine=406
scope.146.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.147.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU29sZCNjdG9yKDQpOjM3NQ
scope.147.kind=method
scope.147.startLine=1
scope.147.endLine=406
scope.147.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.148.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUxpZnRlZCNjdG9yKDQpOjM3Mg
scope.148.kind=method
scope.148.startLine=1
scope.148.endLine=406
scope.148.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.149.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZWQjY3RvcigzKTozNjk
scope.149.kind=method
scope.149.startLine=1
scope.149.endLine=406
scope.149.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.150.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjMzNw
scope.150.kind=method
scope.150.startLine=337
scope.150.endLine=339
scope.150.semanticHash=a25dcf65a363730c6f293f8a1f1404f79f6c1932a440cc31c1262695a9baa056
scope.151.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDUpOjMzNg
scope.151.kind=method
scope.151.startLine=1
scope.151.endLine=406
scope.151.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.152.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNvZmZpY2lhbFNwYWNlQXQoMSk6MzQx
scope.152.kind=method
scope.152.startLine=341
scope.152.endLine=343
scope.152.semanticHash=d857123e25d1bd7ad9e99a5f83a2cc20dc70a077e141b0d2f4b1de0cd88b32ac
scope.153.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QbGF5ZXJQYWlkI2N0b3IoMyk6MzYw
scope.153.kind=method
scope.153.startLine=1
scope.153.endLine=406
scope.153.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.154.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5SZW50UGFpZCNjdG9yKDQpOjM1Nw
scope.154.kind=method
scope.154.startLine=1
scope.154.endLine=406
scope.154.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.155.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKTozMzM
scope.155.kind=method
scope.155.startLine=1
scope.155.endLine=406
scope.155.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.156.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKTozNDY
scope.156.kind=method
scope.156.startLine=1
scope.156.endLine=406
scope.156.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.157.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjMxOQ
scope.157.kind=method
scope.157.startLine=1
scope.157.endLine=406
scope.157.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.158.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDIpOjMzMA
scope.158.kind=method
scope.158.startLine=1
scope.158.endLine=406
scope.158.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.159.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Xb24jY3RvcigxKTo0MDI
scope.159.kind=method
scope.159.startLine=1
scope.159.endLine=406
scope.159.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.160.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYmFua3J1cHQoMik6MjY2
scope.160.kind=method
scope.160.startLine=266
scope.160.endLine=269
scope.160.semanticHash=84c8992a880ac6e758541bcb41f72cb9a252c2de254e8f3a8ae03888beb87a3d
scope.161.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYm91Z2h0KDMpOjIwMQ
scope.161.kind=method
scope.161.startLine=201
scope.161.endLine=204
scope.161.semanticHash=79e1d988b41c146a0d4c76c5bf1404dcfb5f43bf5ed7caac3fd2b5b0fbd2c437
scope.162.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYnVpbHRIb3VzZSgzKToyMjE
scope.162.kind=method
scope.162.startLine=221
scope.162.endLine=224
scope.162.semanticHash=e51ffaaf9fc64c2ff825668ffee31babc9a49fd98e53b320a973887332b1074d
scope.163.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY29sbGVjdGVkU2FsYXJ5KDIpOjE5Ng
scope.163.kind=method
scope.163.startLine=196
scope.163.endLine=199
scope.163.semanticHash=9d31c851d99e8df553fdaf39330dc1ae11e0fe903b61f6b97b858c59389d5411
scope.164.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY3RvcigxKToxODQ
scope.164.kind=method
scope.164.startLine=1
scope.164.endLine=406
scope.164.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
scope.165.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZHJld0NoYW5jZUNhcmQoMik6MjQx
scope.165.kind=method
scope.165.startLine=241
scope.165.endLine=244
scope.165.semanticHash=c2d3dd8c5dd528d5bf8090da5f0547757d08ffc07fd3f699588877b9ab2cc644
scope.166.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZHJld0NvbW11bml0eUNoZXN0Q2FyZCgyKToyNDY
scope.166.kind=method
scope.166.startLine=246
scope.166.endLine=249
scope.166.semanticHash=11d7ba10463c79d04b3ea80df07002fc939392f73649bbcb263b0c8ef1bc1e6a
scope.167.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGVmdEphaWxCeVBheWluZygyKToyNjE
scope.167.kind=method
scope.167.startLine=261
scope.167.endLine=264
scope.167.semanticHash=993f52acd6ec0eceb0d216453eba1ca97476032ea358a9746d3f1225533220ce
scope.168.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW92ZWQoNSk6MTkx
scope.168.kind=method
scope.168.startLine=191
scope.168.endLine=194
scope.168.semanticHash=57ba893b31d09539341b88a45dec4b8648b167b19f0a9b4afacd5710a34d446b
scope.169.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZCgzKToyMTY
scope.169.kind=method
scope.169.startLine=216
scope.169.endLine=219
scope.169.semanticHash=bbfe5de1f707f21da4dcef71f01afff91482740647e970d3c03072a7836b2269
scope.170.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZCg0KToyMTE
scope.170.kind=method
scope.170.startLine=211
scope.170.endLine=214
scope.170.semanticHash=66317d89046f5bdcdf22cb407d9a450e9f7221f4020da0e087ac3b105a7beaa8
scope.171.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZEJhbmsoMik6MjUx
scope.171.kind=method
scope.171.startLine=251
scope.171.endLine=254
scope.171.semanticHash=68b8289c6b9caa436a850d29ac9f703de981f579f49fc4af396225097d422309
scope.172.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcmVmdXNlZEJ1aWxkaW5nKDMpOjIzNg
scope.172.kind=method
scope.172.startLine=236
scope.172.endLine=239
scope.172.semanticHash=bc9150e16e6d26cf9949ae96894cd793c0d87ac4b6c0fb087c080025dd60a3a8
scope.173.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcm9sbGVkKDIpOjE4Ng
scope.173.kind=method
scope.173.startLine=186
scope.173.endLine=189
scope.173.semanticHash=41570b54acd7a2a0ee9f75bcefcdd79248dd7db4784b77cea66150ac4e5a790f
scope.174.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc2FsZVJlZnVzZWQoNCk6MjMx
scope.174.kind=method
scope.174.startLine=231
scope.174.endLine=234
scope.174.semanticHash=902eb0534ab31b9b916eb8f3fd7fb549f669e096152fce279c949a5029c28717
scope.175.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc2VudFRvSmFpbCgyKToyNTY
scope.175.kind=method
scope.175.startLine=256
scope.175.endLine=259
scope.175.semanticHash=f9903884ef9a43d743af735bb6cd1fd5841112ad61c1a8732a427e9b3a86fb7b
scope.176.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc29sZCg0KToyMjY
scope.176.kind=method
scope.176.startLine=226
scope.176.endLine=229
scope.176.semanticHash=36ceffd86df9fb98c3fdd440c3cda480841b4012d082ae4a65009180a250f049
scope.177.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uKDEpOjI3MQ
scope.177.kind=method
scope.177.startLine=271
scope.177.endLine=274
scope.177.semanticHash=9b6835462492c397435304e562eaac3dcf2c94fed0f481fd6852fe731129d58f
scope.178.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uQXRBdWN0aW9uKDMpOjIwNg
scope.178.kind=method
scope.178.startLine=206
scope.178.endLine=209
scope.178.semanticHash=db10ed18596729bcd7577e23594ec8451323564e17dc73c245ab874c151f6569
scope.179.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoNCk6Mjk2
scope.179.kind=method
scope.179.startLine=1
scope.179.endLine=406
scope.179.semanticHash=e387b67bd0babe4cf61bd235da0305a0e9f3667a3bd7f32ea9e085c3e3964d0f
*/
