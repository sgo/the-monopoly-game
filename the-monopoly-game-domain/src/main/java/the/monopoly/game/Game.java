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
    Strategy strategy = strategies.forPlayer(player);
    journal.log(new Journal.Entry.TurnStarted(
        player.id(), player.account().balance().amount(), strategy.cashReserve(player, rules, deeds)));
    new Turn(rules, cups.forPlayer(player), events, landings, jail, strategy, deeds).take(player);
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
    public void soldHouse(Player player, ColourStreet street, Money price) {
      journal.log(new Journal.Entry.HouseSold(player.id(), street.type(), price));
    }

    @Override
    public void soldToPeer(Player seller, Ownable land, Player buyer, Money price) {
      journal.log(new Journal.Entry.LandSold(seller.id(), land.type(), buyer.id(), price));
    }

    @Override
    public void distressedSaleStarted(Player seller, Ownable land) {
      journal.log(new Journal.Entry.DistressedSaleStarted(seller.id(), land.type()));
    }

    @Override
    public void distressedOffer(Player bidder, Ownable land, Money price) {
      journal.log(new Journal.Entry.DistressedOffer(bidder.id(), land.type(), price));
    }

    @Override
    public void distressedSaleWon(Player bidder, Ownable land, Money price) {
      journal.log(new Journal.Entry.DistressedSaleWon(bidder.id(), land.type(), price));
    }

    @Override
    public void mortgaged(Player player, Ownable land, Money value) {
      journal.log(new Journal.Entry.Mortgaged(player.id(), land.type(), value));
    }

    @Override
    public void inherited(Player creditor, Ownable land, Player debtor) {
      journal.log(new Journal.Entry.Inherited(creditor.id(), land.type(), debtor.id()));
    }

    @Override
    public void keptMortgage(Player player, Ownable land, Money interest) {
      journal.log(new Journal.Entry.MortgageKept(player.id(), land.type(), interest));
    }

    @Override
    public void liftedMortgage(Player player, Ownable land, Deeds.MortgageCost cost) {
      journal.log(new Journal.Entry.MortgageLifted(player.id(), land.type(), cost.total(), cost.interest()));
    }

    @Override
    public void declinedToBuy(Player player, Ownable land, Money price,
                              Strategy.DeclineReason reason, Money reserve) {
      journal.log(new Journal.Entry.PurchaseDeclined(player.id(), land.type(), price, reason, reserve));
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
    public void receivedBank(Player player, Money amount) {
      journal.log(new Journal.Entry.BankReceived(player.id(), amount));
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
    public void leftJailWithCard(Player player) {
      journal.log(new Journal.Entry.JailCardUsed(player.id()));
    }

    @Override
    public void leftJailByRollingDoubles(Player player) {
      journal.log(new Journal.Entry.JailDoublesRolled(player.id()));
    }

    @Override
    public void stayedInJail(Player player) {
      journal.log(new Journal.Entry.JailStayed(player.id()));
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
      record TurnStarted(Player.ID player, Money balance, Money reserve) implements Entry {
        public TurnStarted(Player.ID player, Money balance) {
          this(player, balance, Money.ZERO);
        }
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

      record PurchaseDeclined(Player.ID player, Street.Type land, Money price,
                              Strategy.DeclineReason reason, Money reserve) implements Entry {
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

      record Inherited(Player.ID player, Street.Type land, Player.ID debtor) implements Entry {
      }

      record MortgageKept(Player.ID player, Street.Type land, Money interest) implements Entry {
      }

      record LandSold(Player.ID seller, Street.Type land, Player.ID buyer, Money price) implements Entry {
      }

      record LandSaleRefused(Player.ID seller, Street.Type land, Player.ID buyer, Money price) implements Entry {
      }

      record DistressedSaleStarted(Player.ID seller, Street.Type land) implements Entry {
      }

      record DistressedOffer(Player.ID bidder, Street.Type land, Money price) implements Entry {
      }

      record DistressedSaleWon(Player.ID bidder, Street.Type land, Money price) implements Entry {
      }

      record BuildingRefused(Player.ID player, Street.Type land, Money price) implements Entry {
      }

      record ChanceCardDrawn(Player.ID player, String card) implements Entry {
      }

      record CommunityChestCardDrawn(Player.ID player, String card) implements Entry {
      }

      record BankPaid(Player.ID player, Money amount) implements Entry {
      }

      record BankReceived(Player.ID player, Money amount) implements Entry {
      }

      record JailEntered(Player.ID player, Street.Type cause) implements Entry {
      }

      record JailFinePaid(Player.ID player, Money fine) implements Entry {
      }

      record JailCardUsed(Player.ID player) implements Entry {
      }

      record JailDoublesRolled(Player.ID player) implements Entry {
      }

      record JailStayed(Player.ID player) implements Entry {
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
moduleHash=f0fc3a02b3aad69c26310d437b42bd82c3dde406a3dbc4665199d87c9580493f
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjQx
scope.0.kind=class
scope.0.startLine=41
scope.0.endLine=514
scope.0.semanticHash=343042cd4ef7187683b3e52bd1e69c0543ac8fda0eaf2888e9dcad4491bd0a65
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6MzU2
scope.1.kind=class
scope.1.startLine=356
scope.1.endLine=359
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6Mzcz
scope.2.kind=class
scope.2.startLine=373
scope.2.endLine=513
scope.2.semanticHash=485bf7aaff95b1d1ce72eba094ab5c2641b45f5c48981572b6e8e9534e5e69b4
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjM5Mg
scope.3.kind=class
scope.3.startLine=392
scope.3.endLine=512
scope.3.semanticHash=c156a9ae8b7e81d7a3991b829cec4cb96d950ec3320b818683ef78d025e3b99e
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jQXVjdGlvbldvbjo0MzE
scope.4.kind=class
scope.4.startLine=431
scope.4.endLine=432
scope.4.semanticHash=71ee77afd5451e73d54900eb221aa9640cfe5a2b42680f570ad5efdf9e473cb9
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI0JhbmtQYWlkOjQ4Ng
scope.5.kind=class
scope.5.startLine=486
scope.5.endLine=487
scope.5.semanticHash=cce5d5e05e58aed8d75d4ca2deb97d6d8388449cd41d7b4bc422482a269ff5b4
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNCYW5rUmVjZWl2ZWQ6NDg5
scope.6.kind=class
scope.6.startLine=489
scope.6.endLine=490
scope.6.semanticHash=02d04a8dd004416ac824aee0a5687eb08034ac9dcbe0bae2355581bd183f3790
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I0JhbmtydXB0OjUwNw
scope.7.kind=class
scope.7.startLine=507
scope.7.endLine=508
scope.7.semanticHash=16825b9c28c79a36f8a880d0adc21014ea4b665f40f0fb2eb70ef7ece3155e0b
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNCb3VnaHQ6NDI3
scope.8.kind=class
scope.8.startLine=427
scope.8.endLine=428
scope.8.semanticHash=27025028a74b83acd66fd8557558fb235025cf183bcc4e3623183b26da003aec
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNCdWlsZGluZ1JlZnVzZWQ6NDc3
scope.9.kind=class
scope.9.startLine=477
scope.9.endLine=478
scope.9.semanticHash=c46235e296f36ff19ba4b74246e14db6236dced240c6de450bc4186fef89d59b
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNDaGFuY2VDYXJkRHJhd246NDgw
scope.10.kind=class
scope.10.startLine=480
scope.10.endLine=481
scope.10.semanticHash=a926e22751d95373a632fb1725c91e97b535d7cf2eb62c29d7d515570e3e3344
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI0NvbW11bml0eUNoZXN0Q2FyZERyYXduOjQ4Mw
scope.11.kind=class
scope.11.startLine=483
scope.11.endLine=484
scope.11.semanticHash=3a1c04eb7d87bf7ea391ae3f28c5fecb5d887626ebce02e8f7ab581a4ce85290
scope.12.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNEaXN0cmVzc2VkT2ZmZXI6NDcx
scope.12.kind=class
scope.12.startLine=471
scope.12.endLine=472
scope.12.semanticHash=a6b26851b984f848f04bdd88b35a8e6173605e1d87739ac1f81895b2f786a8cf
scope.13.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNEaXN0cmVzc2VkU2FsZVN0YXJ0ZWQ6NDY4
scope.13.kind=class
scope.13.startLine=468
scope.13.endLine=469
scope.13.semanticHash=5b2163fb1a971085705c59755fe2387b0bdd1a91016841925222a61184d97e11
scope.14.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI0Rpc3RyZXNzZWRTYWxlV29uOjQ3NA
scope.14.kind=class
scope.14.startLine=474
scope.14.endLine=475
scope.14.semanticHash=7665fa2235db4f1f740916093d7b1cb0f3a1bcdd186c0b53f9ff6e2d5652f1f6
scope.15.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjSG91c2VCdWlsdDo0NDQ
scope.15.kind=class
scope.15.startLine=444
scope.15.endLine=445
scope.15.semanticHash=27575972b2787c07a6fa98a725c5bed4487591e647678d5880869a7c637aee97
scope.16.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNIb3VzZVNvbGQ6NDQ3
scope.16.kind=class
scope.16.startLine=447
scope.16.endLine=448
scope.16.semanticHash=7bfd22802262e4e36e5ffa57b44a79dc928c71eaa883b7f110cd1b9e7f7230b2
scope.17.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNJbmhlcml0ZWQ6NDU2
scope.17.kind=class
scope.17.startLine=456
scope.17.endLine=457
scope.17.semanticHash=4e87cf40a11022ccf4933f9a448697b3a8224c48633ac93bd888d686f9632d19
scope.18.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjM5Nw
scope.18.kind=class
scope.18.startLine=397
scope.18.endLine=398
scope.18.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.19.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjo0MDA
scope.19.kind=class
scope.19.startLine=400
scope.19.endLine=401
scope.19.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.20.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxDYXJkVXNlZCNKYWlsQ2FyZFVzZWQ6NDk4
scope.20.kind=class
scope.20.startLine=498
scope.20.endLine=499
scope.20.semanticHash=78d932232a0f5e673d3dc6c6d78e5ba0e266df879e171af6995e7c6686e39ff5
scope.21.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxEb3VibGVzUm9sbGVkI0phaWxEb3VibGVzUm9sbGVkOjUwMQ
scope.21.kind=class
scope.21.startLine=501
scope.21.endLine=502
scope.21.semanticHash=7103e2c440de0b5645f3f7249799dd79a41fc35d18ec6f0287ae995d1d07be51
scope.22.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI0phaWxFbnRlcmVkOjQ5Mg
scope.22.kind=class
scope.22.startLine=492
scope.22.endLine=493
scope.22.semanticHash=72be50c2af861ce8f500dde65a8bfa767ceb307161772739134a6968fd43b907
scope.23.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNKYWlsRmluZVBhaWQ6NDk1
scope.23.kind=class
scope.23.startLine=495
scope.23.endLine=496
scope.23.semanticHash=88ed119890c309e1480e4400ae91bdb79250c1dd70940253050a847641cb4283
scope.24.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxTdGF5ZWQjSmFpbFN0YXllZDo1MDQ
scope.24.kind=class
scope.24.startLine=504
scope.24.endLine=505
scope.24.semanticHash=15c417a86539b6369b8adabdfdc67525574d0262be87768fc04e199c4b2daa60
scope.25.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNMYW5kU2FsZVJlZnVzZWQ6NDY1
scope.25.kind=class
scope.25.startLine=465
scope.25.endLine=466
scope.25.semanticHash=071a81b29122c94c28ba6cee1630b23eeb7b6acc5cf69c07a622eb04893d79ef
scope.26.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI0xhbmRTb2xkOjQ2Mg
scope.26.kind=class
scope.26.startLine=462
scope.26.endLine=463
scope.26.semanticHash=fd9ac419a61f440251c0473ce96aa04eb9d1e3761c450d6e7d55b0f1d92a105e
scope.27.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNNb3J0Z2FnZUtlcHQ6NDU5
scope.27.kind=class
scope.27.startLine=459
scope.27.endLine=460
scope.27.semanticHash=bf247aef5b7c272b93350d039dbbc80307604012fab76265fc78befb32c6355d
scope.28.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI01vcnRnYWdlTGlmdGVkOjQ1Mw
scope.28.kind=class
scope.28.startLine=453
scope.28.endLine=454
scope.28.semanticHash=876fc18a90cbc579ad9618bc95f03fa33b7cdeae3c7f18f0ead53f7795f64237
scope.29.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNNb3J0Z2FnZWQ6NDUw
scope.29.kind=class
scope.29.startLine=450
scope.29.endLine=451
scope.29.semanticHash=2536842d77794ad82293557093f6b95391662318c0cf866281f0bfeb54b47212
scope.30.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjQxMw
scope.30.kind=class
scope.30.startLine=413
scope.30.endLine=421
scope.30.semanticHash=ed37919856542e0d29f91d0622487a42cbe6023a70d3c23b3950fc66a5e8f1ab
scope.31.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjUGxheWVyUGFpZDo0NDE
scope.31.kind=class
scope.31.startLine=441
scope.31.endLine=442
scope.31.semanticHash=ecda18178391ece7e75c3e72ec3f854adff15a3950fc135b48bdf7e6cb119a23
scope.32.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjUHVyY2hhc2VEZWNsaW5lZDo0MzQ
scope.32.kind=class
scope.32.startLine=434
scope.32.endLine=436
scope.32.semanticHash=72af27050d45a9fbfac729c104126892ecd90a709dfdce45deca6935b40546a4
scope.33.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI1JlbnRQYWlkOjQzOA
scope.33.kind=class
scope.33.startLine=438
scope.33.endLine=439
scope.33.semanticHash=47c40c5b19c0d8df73eda2478d761c7a7e8604713b0719926b1f46a2f2b6f104
scope.34.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6NDEw
scope.34.kind=class
scope.34.startLine=410
scope.34.endLine=411
scope.34.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.35.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6NDIz
scope.35.kind=class
scope.35.startLine=423
scope.35.endLine=424
scope.35.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.36.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjM5Mw
scope.36.kind=class
scope.36.startLine=393
scope.36.endLine=394
scope.36.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.37.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjQwNA
scope.37.kind=class
scope.37.startLine=404
scope.37.endLine=408
scope.37.semanticHash=7640e6e1a3ca0ec35df8a37362691e232188108c807020c2aa9552d2cf964ee5
scope.38.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNXb246NTEw
scope.38.kind=class
scope.38.startLine=510
scope.38.endLine=511
scope.38.semanticHash=1018a3f41b5571c335e5fbf1476a6a3112c2284616837f2e0c7fbd00dd3d8b76
scope.39.id=Y2xhc3M6R2FtZS5Kb3VybmFsbGluZyNKb3VybmFsbGluZzoxODc
scope.39.kind=class
scope.39.startLine=187
scope.39.endLine=349
scope.39.semanticHash=780be4425653087ec2676678d88c0bffd58016611c492b14d5a200238f24662a
scope.40.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjM3MA
scope.40.kind=class
scope.40.startLine=370
scope.40.endLine=371
scope.40.semanticHash=024a5de82b58c6e09d33d689b003f51dcd43a63a5e94cb88b5d8b96d1706df96
scope.41.id=ZmllbGQ6R2FtZSNjdXBzOjQ0
scope.41.kind=field
scope.41.startLine=44
scope.41.endLine=44
scope.41.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.42.id=ZmllbGQ6R2FtZSNkZWNrczo0Nw
scope.42.kind=field
scope.42.startLine=47
scope.42.endLine=47
scope.42.semanticHash=130541f31392b2fc32d3c8343ebd76de366010b8930395e22ba244946f508252
scope.43.id=ZmllbGQ6R2FtZSNkZWVkczo0Ng
scope.43.kind=field
scope.43.startLine=46
scope.43.endLine=46
scope.43.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.44.id=ZmllbGQ6R2FtZSNqYWlsOjQ4
scope.44.kind=field
scope.44.startLine=48
scope.44.endLine=48
scope.44.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.45.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjQz
scope.45.kind=field
scope.45.startLine=43
scope.45.endLine=43
scope.45.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.46.id=ZmllbGQ6R2FtZSNydWxlczo0Mg
scope.46.kind=field
scope.46.startLine=42
scope.46.endLine=42
scope.46.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.47.id=ZmllbGQ6R2FtZSNzdHJhdGVnaWVzOjQ1
scope.47.kind=field
scope.47.startLine=45
scope.47.endLine=45
scope.47.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.48.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6Mzc2
scope.48.kind=field
scope.48.startLine=376
scope.48.endLine=376
scope.48.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.49.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjozNzQ
scope.49.kind=field
scope.49.startLine=374
scope.49.endLine=374
scope.49.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.50.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jbGFuZDo0MzE
scope.50.kind=field
scope.50.startLine=431
scope.50.endLine=431
scope.50.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.51.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcGxheWVyOjQzMQ
scope.51.kind=field
scope.51.startLine=431
scope.51.endLine=431
scope.51.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.52.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcHJpY2U6NDMx
scope.52.kind=field
scope.52.startLine=431
scope.52.endLine=431
scope.52.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.53.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI2Ftb3VudDo0ODY
scope.53.kind=field
scope.53.startLine=486
scope.53.endLine=486
scope.53.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.54.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI3BsYXllcjo0ODY
scope.54.kind=field
scope.54.startLine=486
scope.54.endLine=486
scope.54.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.55.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNhbW91bnQ6NDg5
scope.55.kind=field
scope.55.startLine=489
scope.55.endLine=489
scope.55.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.56.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNwbGF5ZXI6NDg5
scope.56.kind=field
scope.56.startLine=489
scope.56.endLine=489
scope.56.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.57.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I2NyZWRpdG9yOjUwNw
scope.57.kind=field
scope.57.startLine=507
scope.57.endLine=507
scope.57.semanticHash=04806e2a3ca47061887c26b1a6e5df08f09b4b4e10f22dac41fe60a342b7338b
scope.58.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I3BsYXllcjo1MDc
scope.58.kind=field
scope.58.startLine=507
scope.58.endLine=507
scope.58.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.59.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNsYW5kOjQyNw
scope.59.kind=field
scope.59.startLine=427
scope.59.endLine=427
scope.59.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.60.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwbGF5ZXI6NDI3
scope.60.kind=field
scope.60.startLine=427
scope.60.endLine=427
scope.60.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.61.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwcmljZTo0Mjc
scope.61.kind=field
scope.61.startLine=427
scope.61.endLine=427
scope.61.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.62.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNsYW5kOjQ3Nw
scope.62.kind=field
scope.62.startLine=477
scope.62.endLine=477
scope.62.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.63.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwbGF5ZXI6NDc3
scope.63.kind=field
scope.63.startLine=477
scope.63.endLine=477
scope.63.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.64.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwcmljZTo0Nzc
scope.64.kind=field
scope.64.startLine=477
scope.64.endLine=477
scope.64.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.65.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNjYXJkOjQ4MA
scope.65.kind=field
scope.65.startLine=480
scope.65.endLine=480
scope.65.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.66.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNwbGF5ZXI6NDgw
scope.66.kind=field
scope.66.startLine=480
scope.66.endLine=480
scope.66.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.67.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI2NhcmQ6NDgz
scope.67.kind=field
scope.67.startLine=483
scope.67.endLine=483
scope.67.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.68.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI3BsYXllcjo0ODM
scope.68.kind=field
scope.68.startLine=483
scope.68.endLine=483
scope.68.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.69.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNiaWRkZXI6NDcx
scope.69.kind=field
scope.69.startLine=471
scope.69.endLine=471
scope.69.semanticHash=b8ec71792a9a472362073b97719425ae8b5e956ba271d8efedb75371e8312526
scope.70.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNsYW5kOjQ3MQ
scope.70.kind=field
scope.70.startLine=471
scope.70.endLine=471
scope.70.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.71.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNwcmljZTo0NzE
scope.71.kind=field
scope.71.startLine=471
scope.71.endLine=471
scope.71.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.72.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNsYW5kOjQ2OA
scope.72.kind=field
scope.72.startLine=468
scope.72.endLine=468
scope.72.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.73.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNzZWxsZXI6NDY4
scope.73.kind=field
scope.73.startLine=468
scope.73.endLine=468
scope.73.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.74.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI2JpZGRlcjo0NzQ
scope.74.kind=field
scope.74.startLine=474
scope.74.endLine=474
scope.74.semanticHash=b8ec71792a9a472362073b97719425ae8b5e956ba271d8efedb75371e8312526
scope.75.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI2xhbmQ6NDc0
scope.75.kind=field
scope.75.startLine=474
scope.75.endLine=474
scope.75.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.76.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI3ByaWNlOjQ3NA
scope.76.kind=field
scope.76.startLine=474
scope.76.endLine=474
scope.76.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.77.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjbGFuZDo0NDQ
scope.77.kind=field
scope.77.startLine=444
scope.77.endLine=444
scope.77.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.78.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcGxheWVyOjQ0NA
scope.78.kind=field
scope.78.startLine=444
scope.78.endLine=444
scope.78.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.79.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcHJpY2U6NDQ0
scope.79.kind=field
scope.79.startLine=444
scope.79.endLine=444
scope.79.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.80.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNsYW5kOjQ0Nw
scope.80.kind=field
scope.80.startLine=447
scope.80.endLine=447
scope.80.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.81.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwbGF5ZXI6NDQ3
scope.81.kind=field
scope.81.startLine=447
scope.81.endLine=447
scope.81.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.82.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwcmljZTo0NDc
scope.82.kind=field
scope.82.startLine=447
scope.82.endLine=447
scope.82.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.83.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNkZWJ0b3I6NDU2
scope.83.kind=field
scope.83.startLine=456
scope.83.endLine=456
scope.83.semanticHash=7187277bc5d3a4f7eb1846526a3403b2a46995f8b6f5195af4e3989efac8c17f
scope.84.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNsYW5kOjQ1Ng
scope.84.kind=field
scope.84.startLine=456
scope.84.endLine=456
scope.84.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.85.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNwbGF5ZXI6NDU2
scope.85.kind=field
scope.85.startLine=456
scope.85.endLine=456
scope.85.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.86.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjozOTc
scope.86.kind=field
scope.86.startLine=397
scope.86.endLine=397
scope.86.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.87.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjM5Nw
scope.87.kind=field
scope.87.startLine=397
scope.87.endLine=397
scope.87.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.88.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjQwMA
scope.88.kind=field
scope.88.startLine=400
scope.88.endLine=400
scope.88.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.89.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxDYXJkVXNlZCNwbGF5ZXI6NDk4
scope.89.kind=field
scope.89.startLine=498
scope.89.endLine=498
scope.89.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.90.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxEb3VibGVzUm9sbGVkI3BsYXllcjo1MDE
scope.90.kind=field
scope.90.startLine=501
scope.90.endLine=501
scope.90.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.91.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI2NhdXNlOjQ5Mg
scope.91.kind=field
scope.91.startLine=492
scope.91.endLine=492
scope.91.semanticHash=f0527b6e66e3f950052646384f5a7c874593c4a39f0fb849428625529221f17b
scope.92.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI3BsYXllcjo0OTI
scope.92.kind=field
scope.92.startLine=492
scope.92.endLine=492
scope.92.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.93.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNmaW5lOjQ5NQ
scope.93.kind=field
scope.93.startLine=495
scope.93.endLine=495
scope.93.semanticHash=e365883bc779c1a28df50988a532d020ef388a587ebb4e7379f58733cfb94b21
scope.94.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNwbGF5ZXI6NDk1
scope.94.kind=field
scope.94.startLine=495
scope.94.endLine=495
scope.94.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.95.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxTdGF5ZWQjcGxheWVyOjUwNA
scope.95.kind=field
scope.95.startLine=504
scope.95.endLine=504
scope.95.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.96.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNidXllcjo0NjU
scope.96.kind=field
scope.96.startLine=465
scope.96.endLine=465
scope.96.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.97.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNsYW5kOjQ2NQ
scope.97.kind=field
scope.97.startLine=465
scope.97.endLine=465
scope.97.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.98.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNwcmljZTo0NjU
scope.98.kind=field
scope.98.startLine=465
scope.98.endLine=465
scope.98.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.99.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNzZWxsZXI6NDY1
scope.99.kind=field
scope.99.startLine=465
scope.99.endLine=465
scope.99.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.100.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2J1eWVyOjQ2Mg
scope.100.kind=field
scope.100.startLine=462
scope.100.endLine=462
scope.100.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.101.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2xhbmQ6NDYy
scope.101.kind=field
scope.101.startLine=462
scope.101.endLine=462
scope.101.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.102.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3ByaWNlOjQ2Mg
scope.102.kind=field
scope.102.startLine=462
scope.102.endLine=462
scope.102.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.103.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3NlbGxlcjo0NjI
scope.103.kind=field
scope.103.startLine=462
scope.103.endLine=462
scope.103.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.104.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNpbnRlcmVzdDo0NTk
scope.104.kind=field
scope.104.startLine=459
scope.104.endLine=459
scope.104.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.105.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNsYW5kOjQ1OQ
scope.105.kind=field
scope.105.startLine=459
scope.105.endLine=459
scope.105.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.106.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNwbGF5ZXI6NDU5
scope.106.kind=field
scope.106.startLine=459
scope.106.endLine=459
scope.106.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.107.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2ludGVyZXN0OjQ1Mw
scope.107.kind=field
scope.107.startLine=453
scope.107.endLine=453
scope.107.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.108.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2xhbmQ6NDUz
scope.108.kind=field
scope.108.startLine=453
scope.108.endLine=453
scope.108.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.109.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3BsYXllcjo0NTM
scope.109.kind=field
scope.109.startLine=453
scope.109.endLine=453
scope.109.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.110.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3RvdGFsOjQ1Mw
scope.110.kind=field
scope.110.startLine=453
scope.110.endLine=453
scope.110.semanticHash=c19b9ea10da3846e1c37942dc47f59b13649fda020125cea6df5d3a470490575
scope.111.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNsYW5kOjQ1MA
scope.111.kind=field
scope.111.startLine=450
scope.111.endLine=450
scope.111.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.112.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNwbGF5ZXI6NDUw
scope.112.kind=field
scope.112.startLine=450
scope.112.endLine=450
scope.112.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.113.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCN2YWx1ZTo0NTA
scope.113.kind=field
scope.113.startLine=450
scope.113.endLine=450
scope.113.semanticHash=0d022e0e6113de33cad355012469518c3a21832e276a9c3f3a6893880214fa0a
scope.114.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206NDEz
scope.114.kind=field
scope.114.startLine=413
scope.114.endLine=413
scope.114.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.115.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb21TcGFjZTo0MTM
scope.115.kind=field
scope.115.startLine=413
scope.115.endLine=413
scope.115.semanticHash=fdcd833bf3c0613749af9aa35feb23fbe7068c7d720cdb3a09bbbebeefbe4e7c
scope.116.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjo0MTM
scope.116.kind=field
scope.116.startLine=413
scope.116.endLine=413
scope.116.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.117.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjQxMw
scope.117.kind=field
scope.117.startLine=413
scope.117.endLine=413
scope.117.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.118.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvU3BhY2U6NDEz
scope.118.kind=field
scope.118.startLine=413
scope.118.endLine=413
scope.118.semanticHash=061c4ba46bf16ef78d0e00d27fbe750d73f969cccf700678171eb04b70eab629
scope.119.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjYW1vdW50OjQ0MQ
scope.119.kind=field
scope.119.startLine=441
scope.119.endLine=441
scope.119.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.120.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZWU6NDQx
scope.120.kind=field
scope.120.startLine=441
scope.120.endLine=441
scope.120.semanticHash=289e0e3b44081936aa54790d8a0cebe895c1f5ad30a87142f45cb4ec4a20f8d3
scope.121.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZXI6NDQx
scope.121.kind=field
scope.121.startLine=441
scope.121.endLine=441
scope.121.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.122.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjbGFuZDo0MzQ
scope.122.kind=field
scope.122.startLine=434
scope.122.endLine=434
scope.122.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.123.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcGxheWVyOjQzNA
scope.123.kind=field
scope.123.startLine=434
scope.123.endLine=434
scope.123.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.124.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcHJpY2U6NDM0
scope.124.kind=field
scope.124.startLine=434
scope.124.endLine=434
scope.124.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.125.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcmVhc29uOjQzNQ
scope.125.kind=field
scope.125.startLine=435
scope.125.endLine=435
scope.125.semanticHash=9925e2b957cf3e5ae356bb085657ef3bece891d34dc0ab901046c1292ffc60fd
scope.126.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcmVzZXJ2ZTo0MzU
scope.126.kind=field
scope.126.startLine=435
scope.126.endLine=435
scope.126.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.127.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI2xhbmQ6NDM4
scope.127.kind=field
scope.127.startLine=438
scope.127.endLine=438
scope.127.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.128.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI293bmVyOjQzOA
scope.128.kind=field
scope.128.startLine=438
scope.128.endLine=438
scope.128.semanticHash=4dc1b9a409163bdb6ca915d1f2c9f0426b20999866c7edce9557fe1f7cfb7c14
scope.129.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3JlbnQ6NDM4
scope.129.kind=field
scope.129.startLine=438
scope.129.endLine=438
scope.129.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.130.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3RlbmFudDo0Mzg
scope.130.kind=field
scope.130.startLine=438
scope.130.endLine=438
scope.130.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.131.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6NDEw
scope.131.kind=field
scope.131.startLine=410
scope.131.endLine=410
scope.131.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.132.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDo0MTA
scope.132.kind=field
scope.132.startLine=410
scope.132.endLine=410
scope.132.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.133.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6NDIz
scope.133.kind=field
scope.133.startLine=423
scope.133.endLine=423
scope.133.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.134.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6NDIz
scope.134.kind=field
scope.134.startLine=423
scope.134.endLine=423
scope.134.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.135.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6Mzkz
scope.135.kind=field
scope.135.startLine=393
scope.135.endLine=393
scope.135.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.136.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI2JhbGFuY2U6NDA0
scope.136.kind=field
scope.136.startLine=404
scope.136.endLine=404
scope.136.semanticHash=9ba2008e7dc4127b70833455dbf24f667e36899897c47cbf225bbc8bd4f5575a
scope.137.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjo0MDQ
scope.137.kind=field
scope.137.startLine=404
scope.137.endLine=404
scope.137.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.138.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3Jlc2VydmU6NDA0
scope.138.kind=field
scope.138.startLine=404
scope.138.endLine=404
scope.138.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.139.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNwbGF5ZXI6NTEw
scope.139.kind=field
scope.139.startLine=510
scope.139.endLine=510
scope.139.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.140.id=ZmllbGQ6R2FtZS5Kb3VybmFsbGluZyNqb3VybmFsOjE4Nw
scope.140.kind=field
scope.140.startLine=187
scope.140.endLine=187
scope.140.semanticHash=85c5d503112071935eec23a4e6e40fde3bfadba06348aca287c46765762c68bf
scope.141.id=ZmllbGQ6R2FtZS5SZXN1bHQjZGVlZHM6Mzcw
scope.141.kind=field
scope.141.startLine=370
scope.141.endLine=370
scope.141.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.142.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDozNzA
scope.142.kind=field
scope.142.startLine=370
scope.142.endLine=370
scope.142.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.143.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjM3MA
scope.143.kind=field
scope.143.startLine=370
scope.143.endLine=370
scope.143.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.144.id=ZmllbGQ6R2FtZS5SZXN1bHQjd2lubmVyOjM3MA
scope.144.kind=field
scope.144.startLine=370
scope.144.endLine=370
scope.144.semanticHash=9e05c00db702321e24ecb1c4429dea5328a65101957c7f0b7699f23ee7c539a9
scope.145.id=bWV0aG9kOkdhbWUjY3RvcigyKTo4NA
scope.145.kind=method
scope.145.startLine=84
scope.145.endLine=86
scope.145.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.146.id=bWV0aG9kOkdhbWUjY3RvcigzKTo3OQ
scope.146.kind=method
scope.146.startLine=79
scope.146.endLine=81
scope.146.semanticHash=6776d3f993630076b24a0ed0b3bc39a8d8cf1fb2c29184dba3c568e657a2a980
scope.147.id=bWV0aG9kOkdhbWUjY3RvcigzKTo4OQ
scope.147.kind=method
scope.147.startLine=89
scope.147.endLine=91
scope.147.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.148.id=bWV0aG9kOkdhbWUjY3Rvcig0KTo1MA
scope.148.kind=method
scope.148.startLine=50
scope.148.endLine=52
scope.148.semanticHash=d4615ba990b44348e21394831d757cef04354db1b8751fb1a298772f84bb2d76
scope.149.id=bWV0aG9kOkdhbWUjY3Rvcig1KTo1NA
scope.149.kind=method
scope.149.startLine=54
scope.149.endLine=56
scope.149.semanticHash=8f72f5dd6632da91ac15bbd4118e10ec925d3f7f35e6559ed82d3cfe56b10db1
scope.150.id=bWV0aG9kOkdhbWUjY3Rvcig2KTo1OA
scope.150.kind=method
scope.150.startLine=58
scope.150.endLine=63
scope.150.semanticHash=201613e9dfbe05f1b87a4d5e480877d354f121084a686ec5d292531839832ee1
scope.151.id=bWV0aG9kOkdhbWUjY3Rvcig3KTo2NQ
scope.151.kind=method
scope.151.startLine=65
scope.151.endLine=76
scope.151.semanticHash=f52081f9941f6fd288e8813ed6efe9e1b2983823b59016b4e27f63a7aebbc788
scope.152.id=bWV0aG9kOkdhbWUjaWRzKDEpOjM2MQ
scope.152.kind=method
scope.152.startLine=361
scope.152.endLine=363
scope.152.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.153.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6MTUz
scope.153.kind=method
scope.153.startLine=153
scope.153.endLine=157
scope.153.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.154.id=bWV0aG9kOkdhbWUjbGFuZGluZ3NGb3IoMyk6MTY2
scope.154.kind=method
scope.154.startLine=166
scope.154.endLine=184
scope.154.semanticHash=288a76391f035403c868df024ced5feb15dc22a1a78a4cb2b8c51752854acc39
scope.155.id=bWV0aG9kOkdhbWUjcGxheSgwKTo5Mw
scope.155.kind=method
scope.155.startLine=93
scope.155.endLine=95
scope.155.semanticHash=3bcadbbb1f6b598fdb83fbc0fdd237a7656cc24edc1054185a280a4b7b46cb3b
scope.156.id=bWV0aG9kOkdhbWUjcGxheSgyKToxMTE
scope.156.kind=method
scope.156.startLine=111
scope.156.endLine=124
scope.156.semanticHash=b33495d9e1f85d2ecfdbf5a12eb1917000669bde817741180a76ea216bbb34db
scope.157.id=bWV0aG9kOkdhbWUjcGxheVRvQ29tcGxldGlvbigwKTo5OA
scope.157.kind=method
scope.157.startLine=98
scope.157.endLine=100
scope.157.semanticHash=a60fc108488c55d28cf9d6828599290071eeae99381682b526b1392f2b106627
scope.158.id=bWV0aG9kOkdhbWUjcGxheVR1cm4oNik6MTM2
scope.158.kind=method
scope.158.startLine=136
scope.158.endLine=142
scope.158.semanticHash=14d1c67e1253523473d93ac0bdd8afc80e7a9f1495504c30d21e9fd7c6af7912
scope.159.id=bWV0aG9kOkdhbWUjcGxheVR1cm5zKDcpOjEyNg
scope.159.kind=method
scope.159.startLine=126
scope.159.endLine=134
scope.159.semanticHash=88e73da11f64b7e7bdad3de9a8c45fd8e0fb36043acde7e08013b8294dc8e898
scope.160.id=bWV0aG9kOkdhbWUjcGxheVVudGlsU3RvcHBlZCgxKToxMDc
scope.160.kind=method
scope.160.startLine=107
scope.160.endLine=109
scope.160.semanticHash=2159cc9b2267372bf24f16472c20269d3d5376d0624e178122a5a131ef094b22
scope.161.id=bWV0aG9kOkdhbWUjcmVtYWluaW5nUGxheWVycygwKToxNDQ
scope.161.kind=method
scope.161.startLine=144
scope.161.endLine=146
scope.161.semanticHash=a0e051c1b866b1352982334442d470d1567187f7e091423c51fc78cf3a6f2874
scope.162.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oNCk6MTU5
scope.162.kind=method
scope.162.startLine=159
scope.162.endLine=164
scope.162.semanticHash=aef43811750ff9610633f3399f31c54f5f9466e0754e7e88f8e11700fcf94967
scope.163.id=bWV0aG9kOkdhbWUjd2lubmVyKDApOjE0OA
scope.163.kind=method
scope.163.startLine=148
scope.163.endLine=151
scope.163.semanticHash=702f44695db994b2e4908c5393ffd81fcd816cff000bc8cb31c6d97c66191345
scope.164.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6MzU4
scope.164.kind=method
scope.164.startLine=358
scope.164.endLine=358
scope.164.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.165.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjM3Mw
scope.165.kind=method
scope.165.startLine=1
scope.165.endLine=514
scope.165.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.166.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjM4Mw
scope.166.kind=method
scope.166.startLine=383
scope.166.endLine=385
scope.166.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.167.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6Mzc4
scope.167.kind=method
scope.167.startLine=378
scope.167.endLine=381
scope.167.semanticHash=f2f4e1f3c7bd7244a0e0a2e125110a27d8516e8cb7036d71c5cb73f65468d33f
scope.168.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5BdWN0aW9uV29uI2N0b3IoMyk6NDMx
scope.168.kind=method
scope.168.startLine=1
scope.168.endLine=514
scope.168.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.169.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUGFpZCNjdG9yKDIpOjQ4Ng
scope.169.kind=method
scope.169.startLine=1
scope.169.endLine=514
scope.169.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.170.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUmVjZWl2ZWQjY3RvcigyKTo0ODk
scope.170.kind=method
scope.170.startLine=1
scope.170.endLine=514
scope.170.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.171.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rcnVwdCNjdG9yKDIpOjUwNw
scope.171.kind=method
scope.171.startLine=1
scope.171.endLine=514
scope.171.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.172.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Cb3VnaHQjY3RvcigzKTo0Mjc
scope.172.kind=method
scope.172.startLine=1
scope.172.endLine=514
scope.172.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.173.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CdWlsZGluZ1JlZnVzZWQjY3RvcigzKTo0Nzc
scope.173.kind=method
scope.173.startLine=1
scope.173.endLine=514
scope.173.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.174.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5DaGFuY2VDYXJkRHJhd24jY3RvcigyKTo0ODA
scope.174.kind=method
scope.174.startLine=1
scope.174.endLine=514
scope.174.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.175.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Db21tdW5pdHlDaGVzdENhcmREcmF3biNjdG9yKDIpOjQ4Mw
scope.175.kind=method
scope.175.startLine=1
scope.175.endLine=514
scope.175.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.176.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkT2ZmZXIjY3RvcigzKTo0NzE
scope.176.kind=method
scope.176.startLine=1
scope.176.endLine=514
scope.176.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.177.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZVN0YXJ0ZWQjY3RvcigyKTo0Njg
scope.177.kind=method
scope.177.startLine=1
scope.177.endLine=514
scope.177.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.178.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZVdvbiNjdG9yKDMpOjQ3NA
scope.178.kind=method
scope.178.startLine=1
scope.178.endLine=514
scope.178.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.179.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZUJ1aWx0I2N0b3IoMyk6NDQ0
scope.179.kind=method
scope.179.startLine=1
scope.179.endLine=514
scope.179.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.180.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZVNvbGQjY3RvcigzKTo0NDc
scope.180.kind=method
scope.180.startLine=1
scope.180.endLine=514
scope.180.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.181.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbmhlcml0ZWQjY3RvcigzKTo0NTY
scope.181.kind=method
scope.181.startLine=1
scope.181.endLine=514
scope.181.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.182.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjM5Nw
scope.182.kind=method
scope.182.startLine=1
scope.182.endLine=514
scope.182.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.183.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6NDAw
scope.183.kind=method
scope.183.startLine=1
scope.183.endLine=514
scope.183.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.184.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsQ2FyZFVzZWQjY3RvcigxKTo0OTg
scope.184.kind=method
scope.184.startLine=1
scope.184.endLine=514
scope.184.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.185.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRG91Ymxlc1JvbGxlZCNjdG9yKDEpOjUwMQ
scope.185.kind=method
scope.185.startLine=1
scope.185.endLine=514
scope.185.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.186.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRW50ZXJlZCNjdG9yKDIpOjQ5Mg
scope.186.kind=method
scope.186.startLine=1
scope.186.endLine=514
scope.186.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.187.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRmluZVBhaWQjY3RvcigyKTo0OTU
scope.187.kind=method
scope.187.startLine=1
scope.187.endLine=514
scope.187.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.188.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsU3RheWVkI2N0b3IoMSk6NTA0
scope.188.kind=method
scope.188.startLine=1
scope.188.endLine=514
scope.188.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.189.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU2FsZVJlZnVzZWQjY3Rvcig0KTo0NjU
scope.189.kind=method
scope.189.startLine=1
scope.189.endLine=514
scope.189.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.190.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU29sZCNjdG9yKDQpOjQ2Mg
scope.190.kind=method
scope.190.startLine=1
scope.190.endLine=514
scope.190.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.191.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUtlcHQjY3RvcigzKTo0NTk
scope.191.kind=method
scope.191.startLine=1
scope.191.endLine=514
scope.191.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.192.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUxpZnRlZCNjdG9yKDQpOjQ1Mw
scope.192.kind=method
scope.192.startLine=1
scope.192.endLine=514
scope.192.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.193.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZWQjY3RvcigzKTo0NTA
scope.193.kind=method
scope.193.startLine=1
scope.193.endLine=514
scope.193.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.194.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjQxNA
scope.194.kind=method
scope.194.startLine=414
scope.194.endLine=416
scope.194.semanticHash=a25dcf65a363730c6f293f8a1f1404f79f6c1932a440cc31c1262695a9baa056
scope.195.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDUpOjQxMw
scope.195.kind=method
scope.195.startLine=1
scope.195.endLine=514
scope.195.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.196.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNvZmZpY2lhbFNwYWNlQXQoMSk6NDE4
scope.196.kind=method
scope.196.startLine=418
scope.196.endLine=420
scope.196.semanticHash=d857123e25d1bd7ad9e99a5f83a2cc20dc70a077e141b0d2f4b1de0cd88b32ac
scope.197.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QbGF5ZXJQYWlkI2N0b3IoMyk6NDQx
scope.197.kind=method
scope.197.startLine=1
scope.197.endLine=514
scope.197.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.198.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QdXJjaGFzZURlY2xpbmVkI2N0b3IoNSk6NDM0
scope.198.kind=method
scope.198.startLine=1
scope.198.endLine=514
scope.198.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.199.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5SZW50UGFpZCNjdG9yKDQpOjQzOA
scope.199.kind=method
scope.199.startLine=1
scope.199.endLine=514
scope.199.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.200.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKTo0MTA
scope.200.kind=method
scope.200.startLine=1
scope.200.endLine=514
scope.200.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.201.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKTo0MjM
scope.201.kind=method
scope.201.startLine=1
scope.201.endLine=514
scope.201.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.202.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjM5Mw
scope.202.kind=method
scope.202.startLine=1
scope.202.endLine=514
scope.202.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.203.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDIpOjQwNQ
scope.203.kind=method
scope.203.startLine=405
scope.203.endLine=407
scope.203.semanticHash=50bc458bc5572020d0072616b76bafb6d64ddbc09e71a9cb1c781d7712954d31
scope.204.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDMpOjQwNA
scope.204.kind=method
scope.204.startLine=1
scope.204.endLine=514
scope.204.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.205.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Xb24jY3RvcigxKTo1MTA
scope.205.kind=method
scope.205.startLine=1
scope.205.endLine=514
scope.205.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.206.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYmFua3J1cHQoMik6MzQw
scope.206.kind=method
scope.206.startLine=340
scope.206.endLine=343
scope.206.semanticHash=84c8992a880ac6e758541bcb41f72cb9a252c2de254e8f3a8ae03888beb87a3d
scope.207.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYm91Z2h0KDMpOjIwNA
scope.207.kind=method
scope.207.startLine=204
scope.207.endLine=207
scope.207.semanticHash=79e1d988b41c146a0d4c76c5bf1404dcfb5f43bf5ed7caac3fd2b5b0fbd2c437
scope.208.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYnVpbHRIb3VzZSgzKToyNzU
scope.208.kind=method
scope.208.startLine=275
scope.208.endLine=278
scope.208.semanticHash=e51ffaaf9fc64c2ff825668ffee31babc9a49fd98e53b320a973887332b1074d
scope.209.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY29sbGVjdGVkU2FsYXJ5KDIpOjE5OQ
scope.209.kind=method
scope.209.startLine=199
scope.209.endLine=202
scope.209.semanticHash=9d31c851d99e8df553fdaf39330dc1ae11e0fe903b61f6b97b858c59389d5411
scope.210.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY3RvcigxKToxODc
scope.210.kind=method
scope.210.startLine=1
scope.210.endLine=514
scope.210.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
scope.211.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGVjbGluZWRUb0J1eSg1KToyNTk
scope.211.kind=method
scope.211.startLine=259
scope.211.endLine=263
scope.211.semanticHash=7dad8584ee95edba7ca11ce127d37d287aaf4042dd6742ea3aefd97724f21418
scope.212.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGlzdHJlc3NlZE9mZmVyKDMpOjIyOQ
scope.212.kind=method
scope.212.startLine=229
scope.212.endLine=232
scope.212.semanticHash=2de1aefcb9d72a8e0bb36fc5d8e816aac1fbe6fd90d9c4145d845c338a798d42
scope.213.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGlzdHJlc3NlZFNhbGVTdGFydGVkKDIpOjIyNA
scope.213.kind=method
scope.213.startLine=224
scope.213.endLine=227
scope.213.semanticHash=f22257cc08d2d61fb18392696915ad141cd503c29d6399bc08ebc3d14e57019a
scope.214.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGlzdHJlc3NlZFNhbGVXb24oMyk6MjM0
scope.214.kind=method
scope.214.startLine=234
scope.214.endLine=237
scope.214.semanticHash=bc40742d43eb9dc85baa428eeca7270774e1c81940b65489373b80dfe62946ce
scope.215.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZHJld0NoYW5jZUNhcmQoMik6Mjk1
scope.215.kind=method
scope.215.startLine=295
scope.215.endLine=298
scope.215.semanticHash=c2d3dd8c5dd528d5bf8090da5f0547757d08ffc07fd3f699588877b9ab2cc644
scope.216.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZHJld0NvbW11bml0eUNoZXN0Q2FyZCgyKTozMDA
scope.216.kind=method
scope.216.startLine=300
scope.216.endLine=303
scope.216.semanticHash=11d7ba10463c79d04b3ea80df07002fc939392f73649bbcb263b0c8ef1bc1e6a
scope.217.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjaW5oZXJpdGVkKDMpOjI0NA
scope.217.kind=method
scope.217.startLine=244
scope.217.endLine=247
scope.217.semanticHash=e658917c0bba26af6652047ba4f32060a8892696ec360ac4153d5f122d64fd02
scope.218.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcja2VwdE1vcnRnYWdlKDMpOjI0OQ
scope.218.kind=method
scope.218.startLine=249
scope.218.endLine=252
scope.218.semanticHash=af7c50b8b4adf43eed5c4914e9693ba3978e4f6aee623806da31fb725e53d74a
scope.219.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGVmdEphaWxCeVBheWluZygyKTozMjA
scope.219.kind=method
scope.219.startLine=320
scope.219.endLine=323
scope.219.semanticHash=993f52acd6ec0eceb0d216453eba1ca97476032ea358a9746d3f1225533220ce
scope.220.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGVmdEphaWxCeVJvbGxpbmdEb3VibGVzKDEpOjMzMA
scope.220.kind=method
scope.220.startLine=330
scope.220.endLine=333
scope.220.semanticHash=7e3073f77b3c33d40e026561c595d8c975d4088635e159d6d48b196be1f41fcf
scope.221.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGVmdEphaWxXaXRoQ2FyZCgxKTozMjU
scope.221.kind=method
scope.221.startLine=325
scope.221.endLine=328
scope.221.semanticHash=0eca1027b49296bf1ffec48857f24051dc8ed65d5c3170ab1e113873da565fb9
scope.222.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGlmdGVkTW9ydGdhZ2UoMyk6MjU0
scope.222.kind=method
scope.222.startLine=254
scope.222.endLine=257
scope.222.semanticHash=197a6e5001b712cbd7977cfde5e0bb401f45ad7fc8c5914f720c256c76f56656
scope.223.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW9ydGdhZ2VkKDMpOjIzOQ
scope.223.kind=method
scope.223.startLine=239
scope.223.endLine=242
scope.223.semanticHash=4603f42f43f0d481d6e7fc4c95d250fddba4fdd3ff983e723313f111250d8d11
scope.224.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW92ZWQoNSk6MTk0
scope.224.kind=method
scope.224.startLine=194
scope.224.endLine=197
scope.224.semanticHash=57ba893b31d09539341b88a45dec4b8648b167b19f0a9b4afacd5710a34d446b
scope.225.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZCgzKToyNzA
scope.225.kind=method
scope.225.startLine=270
scope.225.endLine=273
scope.225.semanticHash=bbfe5de1f707f21da4dcef71f01afff91482740647e970d3c03072a7836b2269
scope.226.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZCg0KToyNjU
scope.226.kind=method
scope.226.startLine=265
scope.226.endLine=268
scope.226.semanticHash=66317d89046f5bdcdf22cb407d9a450e9f7221f4020da0e087ac3b105a7beaa8
scope.227.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZEJhbmsoMik6MzA1
scope.227.kind=method
scope.227.startLine=305
scope.227.endLine=308
scope.227.semanticHash=68b8289c6b9caa436a850d29ac9f703de981f579f49fc4af396225097d422309
scope.228.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcmVjZWl2ZWRCYW5rKDIpOjMxMA
scope.228.kind=method
scope.228.startLine=310
scope.228.endLine=313
scope.228.semanticHash=a6258b0d1573f0be24eee767d38df0589a0ae88b68ecc2de87ace7150011325f
scope.229.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcmVmdXNlZEJ1aWxkaW5nKDMpOjI5MA
scope.229.kind=method
scope.229.startLine=290
scope.229.endLine=293
scope.229.semanticHash=bc9150e16e6d26cf9949ae96894cd793c0d87ac4b6c0fb087c080025dd60a3a8
scope.230.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcm9sbGVkKDIpOjE4OQ
scope.230.kind=method
scope.230.startLine=189
scope.230.endLine=192
scope.230.semanticHash=41570b54acd7a2a0ee9f75bcefcdd79248dd7db4784b77cea66150ac4e5a790f
scope.231.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc2FsZVJlZnVzZWQoNCk6Mjg1
scope.231.kind=method
scope.231.startLine=285
scope.231.endLine=288
scope.231.semanticHash=902eb0534ab31b9b916eb8f3fd7fb549f669e096152fce279c949a5029c28717
scope.232.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc2VudFRvSmFpbCgyKTozMTU
scope.232.kind=method
scope.232.startLine=315
scope.232.endLine=318
scope.232.semanticHash=f9903884ef9a43d743af735bb6cd1fd5841112ad61c1a8732a427e9b3a86fb7b
scope.233.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc29sZCg0KToyODA
scope.233.kind=method
scope.233.startLine=280
scope.233.endLine=283
scope.233.semanticHash=36ceffd86df9fb98c3fdd440c3cda480841b4012d082ae4a65009180a250f049
scope.234.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc29sZEhvdXNlKDMpOjIxNA
scope.234.kind=method
scope.234.startLine=214
scope.234.endLine=217
scope.234.semanticHash=0df05f4707d5821c12844685ad057fc45a6be91a84fabf16d7cb0bbcbc606d1a
scope.235.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc29sZFRvUGVlcig0KToyMTk
scope.235.kind=method
scope.235.startLine=219
scope.235.endLine=222
scope.235.semanticHash=07c2b312bb64bd273c79a4dde59a6df7e8d168e4e699a41e1e3fae8fc03119b7
scope.236.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc3RheWVkSW5KYWlsKDEpOjMzNQ
scope.236.kind=method
scope.236.startLine=335
scope.236.endLine=338
scope.236.semanticHash=295e9a5bc9cfede2c3707fd6f1cff98334d5b12e55a97f490737eb8088eadf4e
scope.237.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uKDEpOjM0NQ
scope.237.kind=method
scope.237.startLine=345
scope.237.endLine=348
scope.237.semanticHash=9b6835462492c397435304e562eaac3dcf2c94fed0f481fd6852fe731129d58f
scope.238.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uQXRBdWN0aW9uKDMpOjIwOQ
scope.238.kind=method
scope.238.startLine=209
scope.238.endLine=212
scope.238.semanticHash=db10ed18596729bcd7577e23594ec8451323564e17dc73c245ab874c151f6569
scope.239.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoNCk6Mzcw
scope.239.kind=method
scope.239.startLine=1
scope.239.endLine=514
scope.239.semanticHash=c126473bf95271f22ca4b29691d9ba82cc7ba876fdd4f19c3c0360f2a2cb37f8
*/
