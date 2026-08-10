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
import the.monopoly.game.rules.LegalEntity;
import the.monopoly.game.rules.MonopolyBuyout;
import the.monopoly.game.rules.PeerTrading;
import the.monopoly.game.rules.Rent;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Stalemate;
import the.monopoly.game.rules.Taxes;
import the.monopoly.game.rules.Turn;
import the.monopoly.game.strategies.Strategy;
import the.monopoly.game.strategies.Greedo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final boolean stalemateTrading;
  private final boolean legalEntityTrading;

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
    this(rules, players, cups, strategies, deeds, decks, jail, false);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds,
      Cards.Decks decks, Jail jail, boolean stalemateTrading
  ) {
    this(rules, players, cups, strategies, deeds, decks, jail, stalemateTrading, false);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds,
      Cards.Decks decks, Jail jail, boolean stalemateTrading, boolean legalEntityTrading
  ) {
    this.rules = rules;
    this.players = players;
    this.cups = cups;
    this.strategies = strategies;
    this.deeds = deeds;
    this.decks = decks;
    this.jail = jail;
    this.stalemateTrading = stalemateTrading;
    this.legalEntityTrading = legalEntityTrading;
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

  /** Plays no more than the requested number of rounds, even if nobody wins. */
  public Result playUpToRounds(int rounds) {
    if (rounds <= 0) throw new IllegalArgumentException("A game needs at least one round.");
    java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(rounds);
    return playUntilStopped(() -> remaining.getAndDecrement() > 1);
  }

  private Result play(boolean untilComplete, BooleanSupplier keepPlaying) {
    var journal = new Journal();
    Map<Player.ID, Integer> ages = new HashMap<>();
    Journalling journalling = new Journalling(journal, ages);
    journal.log(new Journal.Entry.Start(ids(players)));
    deeds.legalEntities().forEach(journalling::entityFormed);
    journalling.stalemateTrading(stalemateTrading);
    List<Player> turnOrder = new Initiative(player -> initiativeRollFor(player, journal)).order(players);
    journal.log(new Journal.Entry.InitiativeWon(turnOrder.getFirst().id()));

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
        if (playTurn(player, builder, turnOrder, journal, journalling, building)) return;
      }
    } while (untilComplete && keepPlaying.getAsBoolean() && remainingPlayers().size() > 1);
  }

  private boolean playTurn(Player player, Player builder, List<Player> turnOrder, Journal journal,
                           Journalling journalling, Building building) {
    if (deeds.isBankrupt(player)) return false;
    resolveLegalEntityAtStart(player, journalling);
    resolveSplitOwnershipAtStart(player, turnOrder, journalling);
    takeTurn(player, journal, journalling, landingsFor(player, turnOrder, journalling));
    operateLegalEntities(journalling);
    if (isBuilderStillSolvent(player, builder)) building.develop(player);
    if (remainingPlayers().size() <= 1) return true;
    if (!Stalemate.reached(rules, players, deeds)) return false;
    journal.log(new Journal.Entry.Stalemate());
    remainingPlayers().forEach(it -> {
      journal.log(new Journal.Entry.FinalBalance(it.id(), it.account().balance().amount()));
      journal.log(new Journal.Entry.FinalAge(it.id(), journalling.age(it)));
    });
    return true;
  }

  private boolean operateLegalEntities(Journalling journalling) {
    boolean operated = deeds.legalEntities().stream().anyMatch(entity -> !entity.operated());
    deeds.legalEntities().forEach(entity -> {
      if (doneOperatingForNow(entity)) return;
      switch (entity.operate(deeds)) {
        case LegalEntity.Operation.LoanRepaid it ->
            journalling.entityLoanRepaid(entity, it.shareholder(), it.principal(), it.repayment());
        case LegalEntity.Operation.LoanRaisedWithDividend it -> {
          journalling.entityLoanRaised(entity, it.loan());
          journalling.entityDividendPaid(entity, it.dividend());
        }
        case LegalEntity.Operation.HouseBuilt ignored -> { }
        case LegalEntity.Operation.LoanRaisedAndHouseBuilt it -> {
          journalling.entityLoanRaised(entity, it.loan());
        }
        case LegalEntity.Operation.LoanRaised it -> journalling.entityLoanRaised(entity, it.amount());
        case LegalEntity.Operation.DividendPaid it -> journalling.entityDividendPaid(entity, it.amount());
        case LegalEntity.Operation.NoAction ignored -> { }
      }
    });
    return operated;
  }

  private boolean doneOperatingForNow(LegalEntity entity) {
    return entity.operated() && !entity.receivedRent();
  }

  private boolean isBuilderStillSolvent(Player player, Player builder) {
    return player.id().equals(builder.id()) && !deeds.isBankrupt(player);
  }

  private void resolveLegalEntityAtStart(Player trader, Journalling journalling) {
    if (!legalEntityTrading) return;
    rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .map(ColourStreet::colourGroup).distinct()
        .map(colour -> {
          List<ColourStreet> streets = LegalEntity.streetsOf(colour, rules);
          List<Player> shareholders = players.stream()
              .filter(player -> streets.stream().anyMatch(street -> deeds.ownerOf(street.type())
                  .filter(player.id()::equals).isPresent()))
              .toList();
          return LegalEntity.form(entityName(colour), colour, shareholders, rules, deeds,
              street -> Strategy.priorityOf(street) == Strategy.Priority.HIGHEST);
        })
        .filter(Optional::isPresent).map(Optional::orElseThrow).findFirst()
        .ifPresent(entity -> {
          deeds.form(entity);
          journalling.entityFormed(entity);
        });
  }

  private String entityName(Street.Colour colour) {
    String name = colour.name().replace('_', ' ');
    return Character.toUpperCase(name.charAt(0)) + name.substring(1) + " Realty";
  }

  private void resolveSplitOwnershipAtStart(Player trader, List<Player> turnOrder, Journalling journalling) {
    if (!tradeAtStart(trader, turnOrder, journalling)) resolveBuyoutAtStart(trader, turnOrder, journalling);
  }

  private boolean tradeAtStart(Player trader, List<Player> turnOrder, Journalling journalling) {
    if (!stalemateTrading || !allOwnableSpacesOwned()) return false;
    return PeerTrading.select(trader, strategies.forPlayer(trader), turnOrder, rules, deeds)
        .map(offer -> {
          completeTrade(trader, offer, journalling);
          return true;
        }).orElse(false);
  }

  private boolean resolveBuyoutAtStart(Player trader, List<Player> turnOrder, Journalling journalling) {
    if (!stalemateTrading || !allOwnableSpacesOwned()) return false;
    if (!(strategies.forPlayer(trader) instanceof Greedo)) return false;
    if (isTiedWithItsPartner(trader, turnOrder)) return false;
    List<Player> partners = turnOrder.stream().filter(partner -> partner != trader).toList();
    return resolvableBuyout(trader, partners)
        .map(outcome -> applyBuyout(outcome, journalling))
        .orElseGet(() -> anySplitExists(trader, partners));
  }

  private Optional<MonopolyBuyout.Outcome> resolvableBuyout(Player trader, List<Player> partners) {
    return partners.stream()
        .map(partner -> MonopolyBuyout.resolve(trader, partner, rules, deeds))
        .filter(Optional::isPresent).map(Optional::orElseThrow).findFirst();
  }

  private boolean anySplitExists(Player trader, List<Player> partners) {
    return partners.stream().anyMatch(partner -> MonopolyBuyout.hasSplit(trader, partner, rules, deeds));
  }

  /**
   * Turn-start resolution leaves an equal-cash tie to the established peer-trade
   * behavior — but only when every other player is tied; a lower-balance partner
   * elsewhere in turn order still leaves a real buyout to resolve against.
   */
  private boolean isTiedWithItsPartner(Player trader, List<Player> turnOrder) {
    return turnOrder.stream().filter(partner -> partner != trader)
        .allMatch(partner -> partner.account().balance().amount()
            .equals(trader.account().balance().amount()));
  }

  private boolean applyBuyout(MonopolyBuyout.Outcome outcome, Journalling journalling) {
    journalling.splitMonopolyWon(outcome.winner(), outcome.loser());
    if (!outcome.payment().equals(Money.ZERO)) journalling.splitMonopolyPaid(
        outcome.winner(), outcome.loser(), outcome.payment());
    return true;
  }

  private void completeTrade(Player trader, Strategy.TradeOffer offer, Journalling journalling) {
    deeds.transferWithoutPayment(offer.offered(), trader, offer.partner());
    deeds.transferWithoutPayment(offer.wanted(), offer.partner(), trader);
    journalling.peerTrade(trader, offer.offered(), offer.partner(), offer.wanted());
  }

  private boolean allOwnableSpacesOwned() {
    return rules.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .allMatch(it -> !deeds.isUnowned(it.type()));
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

  private void takeTurn(Player player, Journal journal, Journalling journalling, Landings landings) {
    Strategy strategy = strategies.forPlayer(player);
    journal.log(new Journal.Entry.TurnStarted(
        player.id(), player.account().balance().amount(), strategy.cashReserve(player, rules, deeds),
        journalling.age(player)));
    new Turn(rules, cups.forPlayer(player), journalling, landings, jail, strategy, deeds).take(player);
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
  private record Journalling(Journal journal, Map<Player.ID, Integer> ages)
      implements Turn.Events, LandSale.Events, Rent.Events, Building.Events, Cards.Events, Taxes.Events, Jail.Events, Bankruptcy.Events {
    private int age(Player player) {
      return ages.getOrDefault(player.id(), 0);
    }

    private void ageAfter(Player player) {
      ages.merge(player.id(), 1, Integer::sum);
    }

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
      ageAfter(player);
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

    public void peerTrade(Player trader, Ownable offered, Player partner, Ownable wanted) {
      journal.log(new Journal.Entry.PeerTrade(trader.id(), offered.type(), partner.id(), wanted.type()));
    }

    public void stalemateTrading(boolean enabled) {
      journal.log(new Journal.Entry.StalemateTrading(enabled));
    }

    public void splitMonopolyWon(Player winner, Player loser) {
      journal.log(new Journal.Entry.SplitMonopolyWon(winner.id(), loser.id()));
    }

    public void splitMonopolyPaid(Player payer, Player payee, Money amount) {
      journal.log(new Journal.Entry.SplitMonopolyPaid(payer.id(), payee.id(), amount));
    }

    public void entityFormed(LegalEntity entity) {
      journal.log(new Journal.Entry.LegalEntityFormed(entity.name(),
          entity.shareholders().stream().map(Player::id).toList()));
    }

    public void entityLoanRaised(LegalEntity entity, Money amount) {
      journal.log(new Journal.Entry.LegalEntityLoanRaised(entity.name(), amount,
          entity.shareholders().stream().map(Player::id).toList()));
    }

    public void entityLoanRepaid(LegalEntity entity, Player shareholder, Money principal, Money repayment) {
      journal.log(new Journal.Entry.LegalEntityLoanRepaid(entity.name(), shareholder.id(), principal, repayment));
    }

    public void entityDividendPaid(LegalEntity entity, Money amount) {
      journal.log(new Journal.Entry.LegalEntityDividendPaid(entity.name(),
          entity.shareholders().stream().map(Player::id).toList(), amount));
    }

    @Override
    public void distressedSaleStarted(Player seller, Ownable land) {
      journal.log(new Journal.Entry.DistressedSaleStarted(seller.id(), land.type()));
    }

    @Override
    public void distressedSaleNoBidder(Player seller, Ownable land) {
      journal.log(new Journal.Entry.DistressedSaleNoBidder(seller.id(), land.type()));
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
      ageAfter(player);
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
      journal.log(new Journal.Entry.FinalAge(player.id(), age(player)));
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
      record TurnStarted(Player.ID player, Money balance, Money reserve, int age) implements Entry {
        public TurnStarted(Player.ID player, Money balance) {
          this(player, balance, Money.ZERO, 0);
        }

        public TurnStarted(Player.ID player, Money balance, Money reserve) {
          this(player, balance, reserve, 0);
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

      record PeerTrade(Player.ID trader, Street.Type offered, Player.ID partner, Street.Type wanted) implements Entry {
      }

      record StalemateTrading(boolean enabled) implements Entry {
      }

      record SplitMonopolyWon(Player.ID winner, Player.ID loser) implements Entry {
      }

      record SplitMonopolyPaid(Player.ID payer, Player.ID payee, Money amount) implements Entry {
      }

      record LegalEntityFormed(String name, List<Player.ID> shareholders) implements Entry {
      }

      record LegalEntityLoanRaised(String name, Money amount, List<Player.ID> shareholders) implements Entry {
      }

      record LegalEntityLoanRepaid(String name, Player.ID shareholder, Money principal, Money repayment) implements Entry {
      }

      record LegalEntityDividendPaid(String name, List<Player.ID> shareholders, Money amount) implements Entry {
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

      record DistressedSaleNoBidder(Player.ID seller, Street.Type land) implements Entry {
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

      record Stalemate() implements Entry {
      }

      record FinalBalance(Player.ID player, Money balance) implements Entry {
      }

      record FinalAge(Player.ID player, int age) implements Entry {
      }

      record Won(Player.ID player) implements Entry {
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=bc3e342636eeb5c72850a68a91f851d1148f51803f033df29a941240612b8f15
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjQ4
scope.0.kind=class
scope.0.startLine=48
scope.0.endLine=764
scope.0.semanticHash=e201bd4a6fdd5b8f08b0efb1e3139a250bc7ed09db9e2a7799340e090be36f71
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6NTY2
scope.1.kind=class
scope.1.startLine=566
scope.1.endLine=569
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6NTgz
scope.2.kind=class
scope.2.startLine=583
scope.2.endLine=763
scope.2.semanticHash=a568f47dee13378f8820dbc1e89f9a9ca82490259f84f57555660b14aa6a5921
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjYwMg
scope.3.kind=class
scope.3.startLine=602
scope.3.endLine=762
scope.3.semanticHash=04684556e6d23c47d49a94d5260a8e6214afef7f5d4a1d59d8160283433c35b6
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jQXVjdGlvbldvbjo2NDU
scope.4.kind=class
scope.4.startLine=645
scope.4.endLine=646
scope.4.semanticHash=71ee77afd5451e73d54900eb221aa9640cfe5a2b42680f570ad5efdf9e473cb9
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI0JhbmtQYWlkOjcyNw
scope.5.kind=class
scope.5.startLine=727
scope.5.endLine=728
scope.5.semanticHash=cce5d5e05e58aed8d75d4ca2deb97d6d8388449cd41d7b4bc422482a269ff5b4
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNCYW5rUmVjZWl2ZWQ6NzMw
scope.6.kind=class
scope.6.startLine=730
scope.6.endLine=731
scope.6.semanticHash=02d04a8dd004416ac824aee0a5687eb08034ac9dcbe0bae2355581bd183f3790
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I0JhbmtydXB0Ojc0OA
scope.7.kind=class
scope.7.startLine=748
scope.7.endLine=749
scope.7.semanticHash=16825b9c28c79a36f8a880d0adc21014ea4b665f40f0fb2eb70ef7ece3155e0b
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNCb3VnaHQ6NjQx
scope.8.kind=class
scope.8.startLine=641
scope.8.endLine=642
scope.8.semanticHash=27025028a74b83acd66fd8557558fb235025cf183bcc4e3623183b26da003aec
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNCdWlsZGluZ1JlZnVzZWQ6NzE4
scope.9.kind=class
scope.9.startLine=718
scope.9.endLine=719
scope.9.semanticHash=c46235e296f36ff19ba4b74246e14db6236dced240c6de450bc4186fef89d59b
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNDaGFuY2VDYXJkRHJhd246NzIx
scope.10.kind=class
scope.10.startLine=721
scope.10.endLine=722
scope.10.semanticHash=a926e22751d95373a632fb1725c91e97b535d7cf2eb62c29d7d515570e3e3344
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI0NvbW11bml0eUNoZXN0Q2FyZERyYXduOjcyNA
scope.11.kind=class
scope.11.startLine=724
scope.11.endLine=725
scope.11.semanticHash=3a1c04eb7d87bf7ea391ae3f28c5fecb5d887626ebce02e8f7ab581a4ce85290
scope.12.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNEaXN0cmVzc2VkT2ZmZXI6NzEy
scope.12.kind=class
scope.12.startLine=712
scope.12.endLine=713
scope.12.semanticHash=a6b26851b984f848f04bdd88b35a8e6173605e1d87739ac1f81895b2f786a8cf
scope.13.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjRGlzdHJlc3NlZFNhbGVOb0JpZGRlcjo3MDk
scope.13.kind=class
scope.13.startLine=709
scope.13.endLine=710
scope.13.semanticHash=75e912e170f8d8fa05b68bb0cf8b559956b819929182eb968e2b53d51012c9b7
scope.14.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNEaXN0cmVzc2VkU2FsZVN0YXJ0ZWQ6NzA2
scope.14.kind=class
scope.14.startLine=706
scope.14.endLine=707
scope.14.semanticHash=5b2163fb1a971085705c59755fe2387b0bdd1a91016841925222a61184d97e11
scope.15.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI0Rpc3RyZXNzZWRTYWxlV29uOjcxNQ
scope.15.kind=class
scope.15.startLine=715
scope.15.endLine=716
scope.15.semanticHash=7665fa2235db4f1f740916093d7b1cb0f3a1bcdd186c0b53f9ff6e2d5652f1f6
scope.16.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI0ZpbmFsQWdlOjc1Nw
scope.16.kind=class
scope.16.startLine=757
scope.16.endLine=758
scope.16.semanticHash=174faa5146bf4e6b710a1dc3a9e2a96bc71d0c264c37895b044997623e4c691d
scope.17.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNGaW5hbEJhbGFuY2U6NzU0
scope.17.kind=class
scope.17.startLine=754
scope.17.endLine=755
scope.17.semanticHash=f991eb829ddb2423403d242bcdbdd98ba3199698ebd5c3ebd2dcb0d5cfe0a627
scope.18.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjSG91c2VCdWlsdDo2ODI
scope.18.kind=class
scope.18.startLine=682
scope.18.endLine=683
scope.18.semanticHash=27575972b2787c07a6fa98a725c5bed4487591e647678d5880869a7c637aee97
scope.19.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNIb3VzZVNvbGQ6Njg1
scope.19.kind=class
scope.19.startLine=685
scope.19.endLine=686
scope.19.semanticHash=7bfd22802262e4e36e5ffa57b44a79dc928c71eaa883b7f110cd1b9e7f7230b2
scope.20.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNJbmhlcml0ZWQ6Njk0
scope.20.kind=class
scope.20.startLine=694
scope.20.endLine=695
scope.20.semanticHash=4e87cf40a11022ccf4933f9a448697b3a8224c48633ac93bd888d686f9632d19
scope.21.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjYwNw
scope.21.kind=class
scope.21.startLine=607
scope.21.endLine=608
scope.21.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.22.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjo2MTA
scope.22.kind=class
scope.22.startLine=610
scope.22.endLine=611
scope.22.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.23.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxDYXJkVXNlZCNKYWlsQ2FyZFVzZWQ6NzM5
scope.23.kind=class
scope.23.startLine=739
scope.23.endLine=740
scope.23.semanticHash=78d932232a0f5e673d3dc6c6d78e5ba0e266df879e171af6995e7c6686e39ff5
scope.24.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxEb3VibGVzUm9sbGVkI0phaWxEb3VibGVzUm9sbGVkOjc0Mg
scope.24.kind=class
scope.24.startLine=742
scope.24.endLine=743
scope.24.semanticHash=7103e2c440de0b5645f3f7249799dd79a41fc35d18ec6f0287ae995d1d07be51
scope.25.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI0phaWxFbnRlcmVkOjczMw
scope.25.kind=class
scope.25.startLine=733
scope.25.endLine=734
scope.25.semanticHash=72be50c2af861ce8f500dde65a8bfa767ceb307161772739134a6968fd43b907
scope.26.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNKYWlsRmluZVBhaWQ6NzM2
scope.26.kind=class
scope.26.startLine=736
scope.26.endLine=737
scope.26.semanticHash=88ed119890c309e1480e4400ae91bdb79250c1dd70940253050a847641cb4283
scope.27.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxTdGF5ZWQjSmFpbFN0YXllZDo3NDU
scope.27.kind=class
scope.27.startLine=745
scope.27.endLine=746
scope.27.semanticHash=15c417a86539b6369b8adabdfdc67525574d0262be87768fc04e199c4b2daa60
scope.28.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNMYW5kU2FsZVJlZnVzZWQ6NzAz
scope.28.kind=class
scope.28.startLine=703
scope.28.endLine=704
scope.28.semanticHash=071a81b29122c94c28ba6cee1630b23eeb7b6acc5cf69c07a622eb04893d79ef
scope.29.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI0xhbmRTb2xkOjcwMA
scope.29.kind=class
scope.29.startLine=700
scope.29.endLine=701
scope.29.semanticHash=fd9ac419a61f440251c0473ce96aa04eb9d1e3761c450d6e7d55b0f1d92a105e
scope.30.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI0xlZ2FsRW50aXR5RGl2aWRlbmRQYWlkOjY2OQ
scope.30.kind=class
scope.30.startLine=669
scope.30.endLine=670
scope.30.semanticHash=a6b973b482e59b7949c8ceb0e26d5ba94a15e1a58524a46c28931c27273d0ba3
scope.31.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI0xlZ2FsRW50aXR5Rm9ybWVkOjY2MA
scope.31.kind=class
scope.31.startLine=660
scope.31.endLine=661
scope.31.semanticHash=631fdf7745dde5d4380f5cdef077abbf6488d087a9fdfc52335a91183bbda172
scope.32.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNMZWdhbEVudGl0eUxvYW5SYWlzZWQ6NjYz
scope.32.kind=class
scope.32.startLine=663
scope.32.endLine=664
scope.32.semanticHash=da73a08e8b2cd04078088cbd60125d7682d83240f4184b8a74a41930af31edf1
scope.33.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNMZWdhbEVudGl0eUxvYW5SZXBhaWQ6NjY2
scope.33.kind=class
scope.33.startLine=666
scope.33.endLine=667
scope.33.semanticHash=136375fc94d9d917f2bed703b3d18023767213fe5b45a803857ec6e27243300d
scope.34.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNNb3J0Z2FnZUtlcHQ6Njk3
scope.34.kind=class
scope.34.startLine=697
scope.34.endLine=698
scope.34.semanticHash=bf247aef5b7c272b93350d039dbbc80307604012fab76265fc78befb32c6355d
scope.35.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI01vcnRnYWdlTGlmdGVkOjY5MQ
scope.35.kind=class
scope.35.startLine=691
scope.35.endLine=692
scope.35.semanticHash=876fc18a90cbc579ad9618bc95f03fa33b7cdeae3c7f18f0ead53f7795f64237
scope.36.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNNb3J0Z2FnZWQ6Njg4
scope.36.kind=class
scope.36.startLine=688
scope.36.endLine=689
scope.36.semanticHash=2536842d77794ad82293557093f6b95391662318c0cf866281f0bfeb54b47212
scope.37.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjYyNw
scope.37.kind=class
scope.37.startLine=627
scope.37.endLine=635
scope.37.semanticHash=ed37919856542e0d29f91d0622487a42cbe6023a70d3c23b3950fc66a5e8f1ab
scope.38.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNQZWVyVHJhZGU6NjQ4
scope.38.kind=class
scope.38.startLine=648
scope.38.endLine=649
scope.38.semanticHash=0b4feb9a22fe8ab12f1803c2626a29366e17eca02670c18aa1efec0c2512fb6d
scope.39.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjUGxheWVyUGFpZDo2Nzk
scope.39.kind=class
scope.39.startLine=679
scope.39.endLine=680
scope.39.semanticHash=ecda18178391ece7e75c3e72ec3f854adff15a3950fc135b48bdf7e6cb119a23
scope.40.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjUHVyY2hhc2VEZWNsaW5lZDo2NzI
scope.40.kind=class
scope.40.startLine=672
scope.40.endLine=674
scope.40.semanticHash=72af27050d45a9fbfac729c104126892ecd90a709dfdce45deca6935b40546a4
scope.41.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI1JlbnRQYWlkOjY3Ng
scope.41.kind=class
scope.41.startLine=676
scope.41.endLine=677
scope.41.semanticHash=47c40c5b19c0d8df73eda2478d761c7a7e8604713b0719926b1f46a2f2b6f104
scope.42.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6NjI0
scope.42.kind=class
scope.42.startLine=624
scope.42.endLine=625
scope.42.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.43.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6NjM3
scope.43.kind=class
scope.43.startLine=637
scope.43.endLine=638
scope.43.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.44.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI1NwbGl0TW9ub3BvbHlQYWlkOjY1Nw
scope.44.kind=class
scope.44.startLine=657
scope.44.endLine=658
scope.44.semanticHash=088ba655dca222db0149859641809b25dcc4469fc60702c2129eec19d7cf93ee
scope.45.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jU3BsaXRNb25vcG9seVdvbjo2NTQ
scope.45.kind=class
scope.45.startLine=654
scope.45.endLine=655
scope.45.semanticHash=7eab67f65f325bc29dc9eed6c1b7f342f135afe2dadc8b640001776424af9ad8
scope.46.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZSNTdGFsZW1hdGU6NzUx
scope.46.kind=class
scope.46.startLine=751
scope.46.endLine=752
scope.46.semanticHash=d706ac5ec3788f780b9dced589058470dac53a78cd99870750604517e057e2b4
scope.47.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZVRyYWRpbmcjU3RhbGVtYXRlVHJhZGluZzo2NTE
scope.47.kind=class
scope.47.startLine=651
scope.47.endLine=652
scope.47.semanticHash=b5eb812ee87ce82f7b000eb8f037883ca2ca2e04a1679052bb93a2d2020c4eca
scope.48.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjYwMw
scope.48.kind=class
scope.48.startLine=603
scope.48.endLine=604
scope.48.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.49.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjYxNA
scope.49.kind=class
scope.49.startLine=614
scope.49.endLine=622
scope.49.semanticHash=611be8f7912e6193ac83d3badf59a472e4cb21571d4be0e07854eaec325c9099
scope.50.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNXb246NzYw
scope.50.kind=class
scope.50.startLine=760
scope.50.endLine=761
scope.50.semanticHash=1018a3f41b5571c335e5fbf1476a6a3112c2284616837f2e0c7fbd00dd3d8b76
scope.51.id=Y2xhc3M6R2FtZS5Kb3VybmFsbGluZyNKb3VybmFsbGluZzozNDY
scope.51.kind=class
scope.51.startLine=346
scope.51.endLine=559
scope.51.semanticHash=1daaf5e2a46d42b594c1902eeb0d52874efcf95aa3f8b39d5cfa717e440a4a08
scope.52.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjU4MA
scope.52.kind=class
scope.52.startLine=580
scope.52.endLine=581
scope.52.semanticHash=024a5de82b58c6e09d33d689b003f51dcd43a63a5e94cb88b5d8b96d1706df96
scope.53.id=ZmllbGQ6R2FtZSNjdXBzOjUx
scope.53.kind=field
scope.53.startLine=51
scope.53.endLine=51
scope.53.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.54.id=ZmllbGQ6R2FtZSNkZWNrczo1NA
scope.54.kind=field
scope.54.startLine=54
scope.54.endLine=54
scope.54.semanticHash=130541f31392b2fc32d3c8343ebd76de366010b8930395e22ba244946f508252
scope.55.id=ZmllbGQ6R2FtZSNkZWVkczo1Mw
scope.55.kind=field
scope.55.startLine=53
scope.55.endLine=53
scope.55.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.56.id=ZmllbGQ6R2FtZSNqYWlsOjU1
scope.56.kind=field
scope.56.startLine=55
scope.56.endLine=55
scope.56.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.57.id=ZmllbGQ6R2FtZSNsZWdhbEVudGl0eVRyYWRpbmc6NTc
scope.57.kind=field
scope.57.startLine=57
scope.57.endLine=57
scope.57.semanticHash=79e35a24ea51a961b285f1431176277ba37f40aebb7dc85ea35c6c3e9ef9567e
scope.58.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjUw
scope.58.kind=field
scope.58.startLine=50
scope.58.endLine=50
scope.58.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.59.id=ZmllbGQ6R2FtZSNydWxlczo0OQ
scope.59.kind=field
scope.59.startLine=49
scope.59.endLine=49
scope.59.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.60.id=ZmllbGQ6R2FtZSNzdGFsZW1hdGVUcmFkaW5nOjU2
scope.60.kind=field
scope.60.startLine=56
scope.60.endLine=56
scope.60.semanticHash=3fb0db6ec778e457ec4b9262d01f922604291d8cbeefa5df7e177c0d5beea6b1
scope.61.id=ZmllbGQ6R2FtZSNzdHJhdGVnaWVzOjUy
scope.61.kind=field
scope.61.startLine=52
scope.61.endLine=52
scope.61.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.62.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6NTg2
scope.62.kind=field
scope.62.startLine=586
scope.62.endLine=586
scope.62.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.63.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjo1ODQ
scope.63.kind=field
scope.63.startLine=584
scope.63.endLine=584
scope.63.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.64.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jbGFuZDo2NDU
scope.64.kind=field
scope.64.startLine=645
scope.64.endLine=645
scope.64.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.65.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcGxheWVyOjY0NQ
scope.65.kind=field
scope.65.startLine=645
scope.65.endLine=645
scope.65.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.66.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcHJpY2U6NjQ1
scope.66.kind=field
scope.66.startLine=645
scope.66.endLine=645
scope.66.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.67.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI2Ftb3VudDo3Mjc
scope.67.kind=field
scope.67.startLine=727
scope.67.endLine=727
scope.67.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.68.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI3BsYXllcjo3Mjc
scope.68.kind=field
scope.68.startLine=727
scope.68.endLine=727
scope.68.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.69.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNhbW91bnQ6NzMw
scope.69.kind=field
scope.69.startLine=730
scope.69.endLine=730
scope.69.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.70.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNwbGF5ZXI6NzMw
scope.70.kind=field
scope.70.startLine=730
scope.70.endLine=730
scope.70.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.71.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I2NyZWRpdG9yOjc0OA
scope.71.kind=field
scope.71.startLine=748
scope.71.endLine=748
scope.71.semanticHash=04806e2a3ca47061887c26b1a6e5df08f09b4b4e10f22dac41fe60a342b7338b
scope.72.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I3BsYXllcjo3NDg
scope.72.kind=field
scope.72.startLine=748
scope.72.endLine=748
scope.72.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.73.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNsYW5kOjY0MQ
scope.73.kind=field
scope.73.startLine=641
scope.73.endLine=641
scope.73.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.74.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwbGF5ZXI6NjQx
scope.74.kind=field
scope.74.startLine=641
scope.74.endLine=641
scope.74.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.75.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwcmljZTo2NDE
scope.75.kind=field
scope.75.startLine=641
scope.75.endLine=641
scope.75.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.76.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNsYW5kOjcxOA
scope.76.kind=field
scope.76.startLine=718
scope.76.endLine=718
scope.76.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.77.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwbGF5ZXI6NzE4
scope.77.kind=field
scope.77.startLine=718
scope.77.endLine=718
scope.77.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.78.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwcmljZTo3MTg
scope.78.kind=field
scope.78.startLine=718
scope.78.endLine=718
scope.78.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.79.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNjYXJkOjcyMQ
scope.79.kind=field
scope.79.startLine=721
scope.79.endLine=721
scope.79.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.80.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNwbGF5ZXI6NzIx
scope.80.kind=field
scope.80.startLine=721
scope.80.endLine=721
scope.80.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.81.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI2NhcmQ6NzI0
scope.81.kind=field
scope.81.startLine=724
scope.81.endLine=724
scope.81.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.82.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI3BsYXllcjo3MjQ
scope.82.kind=field
scope.82.startLine=724
scope.82.endLine=724
scope.82.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.83.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNiaWRkZXI6NzEy
scope.83.kind=field
scope.83.startLine=712
scope.83.endLine=712
scope.83.semanticHash=b8ec71792a9a472362073b97719425ae8b5e956ba271d8efedb75371e8312526
scope.84.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNsYW5kOjcxMg
scope.84.kind=field
scope.84.startLine=712
scope.84.endLine=712
scope.84.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.85.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNwcmljZTo3MTI
scope.85.kind=field
scope.85.startLine=712
scope.85.endLine=712
scope.85.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.86.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjbGFuZDo3MDk
scope.86.kind=field
scope.86.startLine=709
scope.86.endLine=709
scope.86.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.87.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjc2VsbGVyOjcwOQ
scope.87.kind=field
scope.87.startLine=709
scope.87.endLine=709
scope.87.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.88.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNsYW5kOjcwNg
scope.88.kind=field
scope.88.startLine=706
scope.88.endLine=706
scope.88.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.89.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNzZWxsZXI6NzA2
scope.89.kind=field
scope.89.startLine=706
scope.89.endLine=706
scope.89.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.90.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI2JpZGRlcjo3MTU
scope.90.kind=field
scope.90.startLine=715
scope.90.endLine=715
scope.90.semanticHash=b8ec71792a9a472362073b97719425ae8b5e956ba271d8efedb75371e8312526
scope.91.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI2xhbmQ6NzE1
scope.91.kind=field
scope.91.startLine=715
scope.91.endLine=715
scope.91.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.92.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI3ByaWNlOjcxNQ
scope.92.kind=field
scope.92.startLine=715
scope.92.endLine=715
scope.92.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.93.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI2FnZTo3NTc
scope.93.kind=field
scope.93.startLine=757
scope.93.endLine=757
scope.93.semanticHash=73cf07319348f7df4be5ec1725d9e768231a03a1f8a9a8230686f74c2a9e64d3
scope.94.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI3BsYXllcjo3NTc
scope.94.kind=field
scope.94.startLine=757
scope.94.endLine=757
scope.94.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.95.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNiYWxhbmNlOjc1NA
scope.95.kind=field
scope.95.startLine=754
scope.95.endLine=754
scope.95.semanticHash=9ba2008e7dc4127b70833455dbf24f667e36899897c47cbf225bbc8bd4f5575a
scope.96.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNwbGF5ZXI6NzU0
scope.96.kind=field
scope.96.startLine=754
scope.96.endLine=754
scope.96.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.97.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjbGFuZDo2ODI
scope.97.kind=field
scope.97.startLine=682
scope.97.endLine=682
scope.97.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.98.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcGxheWVyOjY4Mg
scope.98.kind=field
scope.98.startLine=682
scope.98.endLine=682
scope.98.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.99.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcHJpY2U6Njgy
scope.99.kind=field
scope.99.startLine=682
scope.99.endLine=682
scope.99.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.100.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNsYW5kOjY4NQ
scope.100.kind=field
scope.100.startLine=685
scope.100.endLine=685
scope.100.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.101.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwbGF5ZXI6Njg1
scope.101.kind=field
scope.101.startLine=685
scope.101.endLine=685
scope.101.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.102.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwcmljZTo2ODU
scope.102.kind=field
scope.102.startLine=685
scope.102.endLine=685
scope.102.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.103.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNkZWJ0b3I6Njk0
scope.103.kind=field
scope.103.startLine=694
scope.103.endLine=694
scope.103.semanticHash=7187277bc5d3a4f7eb1846526a3403b2a46995f8b6f5195af4e3989efac8c17f
scope.104.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNsYW5kOjY5NA
scope.104.kind=field
scope.104.startLine=694
scope.104.endLine=694
scope.104.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.105.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNwbGF5ZXI6Njk0
scope.105.kind=field
scope.105.startLine=694
scope.105.endLine=694
scope.105.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.106.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjo2MDc
scope.106.kind=field
scope.106.startLine=607
scope.106.endLine=607
scope.106.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.107.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjYwNw
scope.107.kind=field
scope.107.startLine=607
scope.107.endLine=607
scope.107.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.108.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjYxMA
scope.108.kind=field
scope.108.startLine=610
scope.108.endLine=610
scope.108.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.109.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxDYXJkVXNlZCNwbGF5ZXI6NzM5
scope.109.kind=field
scope.109.startLine=739
scope.109.endLine=739
scope.109.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.110.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxEb3VibGVzUm9sbGVkI3BsYXllcjo3NDI
scope.110.kind=field
scope.110.startLine=742
scope.110.endLine=742
scope.110.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.111.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI2NhdXNlOjczMw
scope.111.kind=field
scope.111.startLine=733
scope.111.endLine=733
scope.111.semanticHash=f0527b6e66e3f950052646384f5a7c874593c4a39f0fb849428625529221f17b
scope.112.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI3BsYXllcjo3MzM
scope.112.kind=field
scope.112.startLine=733
scope.112.endLine=733
scope.112.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.113.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNmaW5lOjczNg
scope.113.kind=field
scope.113.startLine=736
scope.113.endLine=736
scope.113.semanticHash=e365883bc779c1a28df50988a532d020ef388a587ebb4e7379f58733cfb94b21
scope.114.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNwbGF5ZXI6NzM2
scope.114.kind=field
scope.114.startLine=736
scope.114.endLine=736
scope.114.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.115.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxTdGF5ZWQjcGxheWVyOjc0NQ
scope.115.kind=field
scope.115.startLine=745
scope.115.endLine=745
scope.115.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.116.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNidXllcjo3MDM
scope.116.kind=field
scope.116.startLine=703
scope.116.endLine=703
scope.116.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.117.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNsYW5kOjcwMw
scope.117.kind=field
scope.117.startLine=703
scope.117.endLine=703
scope.117.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.118.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNwcmljZTo3MDM
scope.118.kind=field
scope.118.startLine=703
scope.118.endLine=703
scope.118.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.119.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNzZWxsZXI6NzAz
scope.119.kind=field
scope.119.startLine=703
scope.119.endLine=703
scope.119.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.120.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2J1eWVyOjcwMA
scope.120.kind=field
scope.120.startLine=700
scope.120.endLine=700
scope.120.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.121.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2xhbmQ6NzAw
scope.121.kind=field
scope.121.startLine=700
scope.121.endLine=700
scope.121.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.122.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3ByaWNlOjcwMA
scope.122.kind=field
scope.122.startLine=700
scope.122.endLine=700
scope.122.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.123.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3NlbGxlcjo3MDA
scope.123.kind=field
scope.123.startLine=700
scope.123.endLine=700
scope.123.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.124.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI2Ftb3VudDo2Njk
scope.124.kind=field
scope.124.startLine=669
scope.124.endLine=669
scope.124.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.125.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI25hbWU6NjY5
scope.125.kind=field
scope.125.startLine=669
scope.125.endLine=669
scope.125.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.126.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI3NoYXJlaG9sZGVyczo2Njk
scope.126.kind=field
scope.126.startLine=669
scope.126.endLine=669
scope.126.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.127.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI25hbWU6NjYw
scope.127.kind=field
scope.127.startLine=660
scope.127.endLine=660
scope.127.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.128.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI3NoYXJlaG9sZGVyczo2NjA
scope.128.kind=field
scope.128.startLine=660
scope.128.endLine=660
scope.128.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.129.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNhbW91bnQ6NjYz
scope.129.kind=field
scope.129.startLine=663
scope.129.endLine=663
scope.129.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.130.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNuYW1lOjY2Mw
scope.130.kind=field
scope.130.startLine=663
scope.130.endLine=663
scope.130.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.131.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNzaGFyZWhvbGRlcnM6NjYz
scope.131.kind=field
scope.131.startLine=663
scope.131.endLine=663
scope.131.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.132.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNuYW1lOjY2Ng
scope.132.kind=field
scope.132.startLine=666
scope.132.endLine=666
scope.132.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.133.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNwcmluY2lwYWw6NjY2
scope.133.kind=field
scope.133.startLine=666
scope.133.endLine=666
scope.133.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.134.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNyZXBheW1lbnQ6NjY2
scope.134.kind=field
scope.134.startLine=666
scope.134.endLine=666
scope.134.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.135.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNzaGFyZWhvbGRlcjo2NjY
scope.135.kind=field
scope.135.startLine=666
scope.135.endLine=666
scope.135.semanticHash=5afb4f38ca9ee8f6c22bd1cea0ff3bcc6387deb8673bd78cb1c57d4e6b9e3e1d
scope.136.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNpbnRlcmVzdDo2OTc
scope.136.kind=field
scope.136.startLine=697
scope.136.endLine=697
scope.136.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.137.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNsYW5kOjY5Nw
scope.137.kind=field
scope.137.startLine=697
scope.137.endLine=697
scope.137.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.138.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNwbGF5ZXI6Njk3
scope.138.kind=field
scope.138.startLine=697
scope.138.endLine=697
scope.138.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.139.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2ludGVyZXN0OjY5MQ
scope.139.kind=field
scope.139.startLine=691
scope.139.endLine=691
scope.139.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.140.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2xhbmQ6Njkx
scope.140.kind=field
scope.140.startLine=691
scope.140.endLine=691
scope.140.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.141.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3BsYXllcjo2OTE
scope.141.kind=field
scope.141.startLine=691
scope.141.endLine=691
scope.141.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.142.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3RvdGFsOjY5MQ
scope.142.kind=field
scope.142.startLine=691
scope.142.endLine=691
scope.142.semanticHash=c19b9ea10da3846e1c37942dc47f59b13649fda020125cea6df5d3a470490575
scope.143.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNsYW5kOjY4OA
scope.143.kind=field
scope.143.startLine=688
scope.143.endLine=688
scope.143.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.144.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNwbGF5ZXI6Njg4
scope.144.kind=field
scope.144.startLine=688
scope.144.endLine=688
scope.144.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.145.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCN2YWx1ZTo2ODg
scope.145.kind=field
scope.145.startLine=688
scope.145.endLine=688
scope.145.semanticHash=0d022e0e6113de33cad355012469518c3a21832e276a9c3f3a6893880214fa0a
scope.146.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206NjI3
scope.146.kind=field
scope.146.startLine=627
scope.146.endLine=627
scope.146.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.147.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb21TcGFjZTo2Mjc
scope.147.kind=field
scope.147.startLine=627
scope.147.endLine=627
scope.147.semanticHash=fdcd833bf3c0613749af9aa35feb23fbe7068c7d720cdb3a09bbbebeefbe4e7c
scope.148.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjo2Mjc
scope.148.kind=field
scope.148.startLine=627
scope.148.endLine=627
scope.148.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.149.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjYyNw
scope.149.kind=field
scope.149.startLine=627
scope.149.endLine=627
scope.149.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.150.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvU3BhY2U6NjI3
scope.150.kind=field
scope.150.startLine=627
scope.150.endLine=627
scope.150.semanticHash=061c4ba46bf16ef78d0e00d27fbe750d73f969cccf700678171eb04b70eab629
scope.151.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNvZmZlcmVkOjY0OA
scope.151.kind=field
scope.151.startLine=648
scope.151.endLine=648
scope.151.semanticHash=649b65565a280b6fb6d03fec31d684ad9ab5a25ce6bab147d7a18dd5ae60c190
scope.152.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNwYXJ0bmVyOjY0OA
scope.152.kind=field
scope.152.startLine=648
scope.152.endLine=648
scope.152.semanticHash=95af23a2c982143b2ae56ecefdadd5af27a308d33e43ffd831ee7dabec5ab90b
scope.153.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSN0cmFkZXI6NjQ4
scope.153.kind=field
scope.153.startLine=648
scope.153.endLine=648
scope.153.semanticHash=1d660dfe29231866caa76a65bb832b7e5d382d4fc7d41cec6b19f988a2357cf4
scope.154.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSN3YW50ZWQ6NjQ4
scope.154.kind=field
scope.154.startLine=648
scope.154.endLine=648
scope.154.semanticHash=bd6096bdbf00201b8b36b0ea0e225711c7485226561a01fef0ded8ce1c44ea48
scope.155.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjYW1vdW50OjY3OQ
scope.155.kind=field
scope.155.startLine=679
scope.155.endLine=679
scope.155.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.156.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZWU6Njc5
scope.156.kind=field
scope.156.startLine=679
scope.156.endLine=679
scope.156.semanticHash=289e0e3b44081936aa54790d8a0cebe895c1f5ad30a87142f45cb4ec4a20f8d3
scope.157.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZXI6Njc5
scope.157.kind=field
scope.157.startLine=679
scope.157.endLine=679
scope.157.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.158.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjbGFuZDo2NzI
scope.158.kind=field
scope.158.startLine=672
scope.158.endLine=672
scope.158.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.159.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcGxheWVyOjY3Mg
scope.159.kind=field
scope.159.startLine=672
scope.159.endLine=672
scope.159.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.160.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcHJpY2U6Njcy
scope.160.kind=field
scope.160.startLine=672
scope.160.endLine=672
scope.160.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.161.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcmVhc29uOjY3Mw
scope.161.kind=field
scope.161.startLine=673
scope.161.endLine=673
scope.161.semanticHash=9925e2b957cf3e5ae356bb085657ef3bece891d34dc0ab901046c1292ffc60fd
scope.162.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcmVzZXJ2ZTo2NzM
scope.162.kind=field
scope.162.startLine=673
scope.162.endLine=673
scope.162.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.163.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI2xhbmQ6Njc2
scope.163.kind=field
scope.163.startLine=676
scope.163.endLine=676
scope.163.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.164.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI293bmVyOjY3Ng
scope.164.kind=field
scope.164.startLine=676
scope.164.endLine=676
scope.164.semanticHash=4dc1b9a409163bdb6ca915d1f2c9f0426b20999866c7edce9557fe1f7cfb7c14
scope.165.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3JlbnQ6Njc2
scope.165.kind=field
scope.165.startLine=676
scope.165.endLine=676
scope.165.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.166.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3RlbmFudDo2NzY
scope.166.kind=field
scope.166.startLine=676
scope.166.endLine=676
scope.166.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.167.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6NjI0
scope.167.kind=field
scope.167.startLine=624
scope.167.endLine=624
scope.167.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.168.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDo2MjQ
scope.168.kind=field
scope.168.startLine=624
scope.168.endLine=624
scope.168.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.169.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6NjM3
scope.169.kind=field
scope.169.startLine=637
scope.169.endLine=637
scope.169.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.170.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6NjM3
scope.170.kind=field
scope.170.startLine=637
scope.170.endLine=637
scope.170.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.171.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI2Ftb3VudDo2NTc
scope.171.kind=field
scope.171.startLine=657
scope.171.endLine=657
scope.171.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.172.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI3BheWVlOjY1Nw
scope.172.kind=field
scope.172.startLine=657
scope.172.endLine=657
scope.172.semanticHash=289e0e3b44081936aa54790d8a0cebe895c1f5ad30a87142f45cb4ec4a20f8d3
scope.173.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI3BheWVyOjY1Nw
scope.173.kind=field
scope.173.startLine=657
scope.173.endLine=657
scope.173.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.174.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jbG9zZXI6NjU0
scope.174.kind=field
scope.174.startLine=654
scope.174.endLine=654
scope.174.semanticHash=878e93ca653f3f39cf25b2c3775677351abe7c49bd9a13f0aa882a3a8db96732
scope.175.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jd2lubmVyOjY1NA
scope.175.kind=field
scope.175.startLine=654
scope.175.endLine=654
scope.175.semanticHash=1f6f344bd703491733c82249fd05cc65806c907d8c6d3cc869164207c368c138
scope.176.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZVRyYWRpbmcjZW5hYmxlZDo2NTE
scope.176.kind=field
scope.176.startLine=651
scope.176.endLine=651
scope.176.semanticHash=3e72e1b05fced05e3a99e662dfe70f6c5ed519247ae3422c53c101825f46b1b8
scope.177.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6NjAz
scope.177.kind=field
scope.177.startLine=603
scope.177.endLine=603
scope.177.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.178.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI2FnZTo2MTQ
scope.178.kind=field
scope.178.startLine=614
scope.178.endLine=614
scope.178.semanticHash=73cf07319348f7df4be5ec1725d9e768231a03a1f8a9a8230686f74c2a9e64d3
scope.179.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI2JhbGFuY2U6NjE0
scope.179.kind=field
scope.179.startLine=614
scope.179.endLine=614
scope.179.semanticHash=9ba2008e7dc4127b70833455dbf24f667e36899897c47cbf225bbc8bd4f5575a
scope.180.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjo2MTQ
scope.180.kind=field
scope.180.startLine=614
scope.180.endLine=614
scope.180.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.181.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3Jlc2VydmU6NjE0
scope.181.kind=field
scope.181.startLine=614
scope.181.endLine=614
scope.181.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.182.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNwbGF5ZXI6NzYw
scope.182.kind=field
scope.182.startLine=760
scope.182.endLine=760
scope.182.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.183.id=ZmllbGQ6R2FtZS5Kb3VybmFsbGluZyNhZ2VzOjM0Ng
scope.183.kind=field
scope.183.startLine=346
scope.183.endLine=346
scope.183.semanticHash=2903e7a1268ae9cd26b2357b7ac21e59c98729950e8d7612d89fd04597741325
scope.184.id=ZmllbGQ6R2FtZS5Kb3VybmFsbGluZyNqb3VybmFsOjM0Ng
scope.184.kind=field
scope.184.startLine=346
scope.184.endLine=346
scope.184.semanticHash=85c5d503112071935eec23a4e6e40fde3bfadba06348aca287c46765762c68bf
scope.185.id=ZmllbGQ6R2FtZS5SZXN1bHQjZGVlZHM6NTgw
scope.185.kind=field
scope.185.startLine=580
scope.185.endLine=580
scope.185.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.186.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDo1ODA
scope.186.kind=field
scope.186.startLine=580
scope.186.endLine=580
scope.186.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.187.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjU4MA
scope.187.kind=field
scope.187.startLine=580
scope.187.endLine=580
scope.187.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.188.id=ZmllbGQ6R2FtZS5SZXN1bHQjd2lubmVyOjU4MA
scope.188.kind=field
scope.188.startLine=580
scope.188.endLine=580
scope.188.semanticHash=9e05c00db702321e24ecb1c4429dea5328a65101957c7f0b7699f23ee7c539a9
scope.189.id=bWV0aG9kOkdhbWUjYWxsT3duYWJsZVNwYWNlc093bmVkKDApOjI5Nw
scope.189.kind=method
scope.189.startLine=297
scope.189.endLine=300
scope.189.semanticHash=821da435108af6599de0db2f7083a8fd6fd049024fea6375a0521284793b5c56
scope.190.id=bWV0aG9kOkdhbWUjYW55U3BsaXRFeGlzdHMoMik6MjY5
scope.190.kind=method
scope.190.startLine=269
scope.190.endLine=271
scope.190.semanticHash=0e159d6c3a604e0cab363efb196c3aed27bbfe673c6cff777443dacea59f3e81
scope.191.id=bWV0aG9kOkdhbWUjYXBwbHlCdXlvdXQoMik6Mjg0
scope.191.kind=method
scope.191.startLine=284
scope.191.endLine=289
scope.191.semanticHash=3a8f04b7a443bf5fccacf64fe8dfd676f1b1282a90c56c011271c8d5d7666b6d
scope.192.id=bWV0aG9kOkdhbWUjY29tcGxldGVUcmFkZSgzKToyOTE
scope.192.kind=method
scope.192.startLine=291
scope.192.endLine=295
scope.192.semanticHash=966633eed085a866d0ce6dc8da93eb71c14814b19768339cd57e58b32fd37aed
scope.193.id=bWV0aG9kOkdhbWUjY3RvcigyKToxMDk
scope.193.kind=method
scope.193.startLine=109
scope.193.endLine=111
scope.193.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.194.id=bWV0aG9kOkdhbWUjY3RvcigzKToxMDQ
scope.194.kind=method
scope.194.startLine=104
scope.194.endLine=106
scope.194.semanticHash=6776d3f993630076b24a0ed0b3bc39a8d8cf1fb2c29184dba3c568e657a2a980
scope.195.id=bWV0aG9kOkdhbWUjY3RvcigzKToxMTQ
scope.195.kind=method
scope.195.startLine=114
scope.195.endLine=116
scope.195.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.196.id=bWV0aG9kOkdhbWUjY3Rvcig0KTo1OQ
scope.196.kind=method
scope.196.startLine=59
scope.196.endLine=61
scope.196.semanticHash=d4615ba990b44348e21394831d757cef04354db1b8751fb1a298772f84bb2d76
scope.197.id=bWV0aG9kOkdhbWUjY3Rvcig1KTo2Mw
scope.197.kind=method
scope.197.startLine=63
scope.197.endLine=65
scope.197.semanticHash=8f72f5dd6632da91ac15bbd4118e10ec925d3f7f35e6559ed82d3cfe56b10db1
scope.198.id=bWV0aG9kOkdhbWUjY3Rvcig2KTo2Nw
scope.198.kind=method
scope.198.startLine=67
scope.198.endLine=72
scope.198.semanticHash=201613e9dfbe05f1b87a4d5e480877d354f121084a686ec5d292531839832ee1
scope.199.id=bWV0aG9kOkdhbWUjY3Rvcig3KTo3NA
scope.199.kind=method
scope.199.startLine=74
scope.199.endLine=79
scope.199.semanticHash=ed3b862b8b56575f057bb3efc8c37f63dfda088f90226752db32af60f9b5fbb2
scope.200.id=bWV0aG9kOkdhbWUjY3Rvcig4KTo4MQ
scope.200.kind=method
scope.200.startLine=81
scope.200.endLine=86
scope.200.semanticHash=f74e2706eebc6a1ef10bac9fce2227079ba1ec4c8654585f692c3846c4306ffd
scope.201.id=bWV0aG9kOkdhbWUjY3Rvcig5KTo4OA
scope.201.kind=method
scope.201.startLine=88
scope.201.endLine=101
scope.201.semanticHash=26e219d09f602439a213ee05bd2265348f47de80e0c8e8e16a3165f46b8229b2
scope.202.id=bWV0aG9kOkdhbWUjZG9uZU9wZXJhdGluZ0Zvck5vdygxKToyMDc
scope.202.kind=method
scope.202.startLine=207
scope.202.endLine=209
scope.202.semanticHash=ffbccb94b5a9f4a859d6a05eaf8c1d93b4aeaa597cacdbd01d8fac778cbfa22c
scope.203.id=bWV0aG9kOkdhbWUjZW50aXR5TmFtZSgxKToyMzU
scope.203.kind=method
scope.203.startLine=235
scope.203.endLine=238
scope.203.semanticHash=63fedf93747ba25ad7ae7201643dc5ea04e06ae29cc5db0a4f58c0224e0bd74a
scope.204.id=bWV0aG9kOkdhbWUjaWRzKDEpOjU3MQ
scope.204.kind=method
scope.204.startLine=571
scope.204.endLine=573
scope.204.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.205.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6MzEx
scope.205.kind=method
scope.205.startLine=311
scope.205.endLine=315
scope.205.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.206.id=bWV0aG9kOkdhbWUjaXNCdWlsZGVyU3RpbGxTb2x2ZW50KDIpOjIxMQ
scope.206.kind=method
scope.206.startLine=211
scope.206.endLine=213
scope.206.semanticHash=79d8ca0d770672bbaf3379d890bdc37476fb65cff6f2770c581b9bbbac09c480
scope.207.id=bWV0aG9kOkdhbWUjaXNUaWVkV2l0aEl0c1BhcnRuZXIoMik6Mjc4
scope.207.kind=method
scope.207.startLine=278
scope.207.endLine=282
scope.207.semanticHash=57ada1100a85c9b9156ca849da5f508164593d3caa30ca1a8fc6c96fa9b4ecfd
scope.208.id=bWV0aG9kOkdhbWUjbGFuZGluZ3NGb3IoMyk6MzI1
scope.208.kind=method
scope.208.startLine=325
scope.208.endLine=343
scope.208.semanticHash=288a76391f035403c868df024ced5feb15dc22a1a78a4cb2b8c51752854acc39
scope.209.id=bWV0aG9kOkdhbWUjb3BlcmF0ZUxlZ2FsRW50aXRpZXMoMSk6MTg5
scope.209.kind=method
scope.209.startLine=189
scope.209.endLine=205
scope.209.semanticHash=6b3986d5af4425d872b5fd0c45d89ab8060ab5ed54f0f8615dbbb7d4181a6cd6
scope.210.id=bWV0aG9kOkdhbWUjcGxheSgwKToxMTg
scope.210.kind=method
scope.210.startLine=118
scope.210.endLine=120
scope.210.semanticHash=3bcadbbb1f6b598fdb83fbc0fdd237a7656cc24edc1054185a280a4b7b46cb3b
scope.211.id=bWV0aG9kOkdhbWUjcGxheSgyKToxNDM
scope.211.kind=method
scope.211.startLine=143
scope.211.endLine=159
scope.211.semanticHash=ae1b7e4863276aabcdc5b1bf55fec6888db9eaa7ff7baae035d31ee8011e1685
scope.212.id=bWV0aG9kOkdhbWUjcGxheVRvQ29tcGxldGlvbigwKToxMjM
scope.212.kind=method
scope.212.startLine=123
scope.212.endLine=125
scope.212.semanticHash=a60fc108488c55d28cf9d6828599290071eeae99381682b526b1392f2b106627
scope.213.id=bWV0aG9kOkdhbWUjcGxheVR1cm4oNik6MTcx
scope.213.kind=method
scope.213.startLine=171
scope.213.endLine=187
scope.213.semanticHash=dc7569ca1f2674cd2d7444c6ccf371c5257485a43662ded73d807581f7073bd4
scope.214.id=bWV0aG9kOkdhbWUjcGxheVR1cm5zKDcpOjE2MQ
scope.214.kind=method
scope.214.startLine=161
scope.214.endLine=169
scope.214.semanticHash=800110418ab82cafa8beb4fe4e18cea570d8d1ea81ac7ec577e48cf7acf957d5
scope.215.id=bWV0aG9kOkdhbWUjcGxheVVudGlsU3RvcHBlZCgxKToxMzI
scope.215.kind=method
scope.215.startLine=132
scope.215.endLine=134
scope.215.semanticHash=2159cc9b2267372bf24f16472c20269d3d5376d0624e178122a5a131ef094b22
scope.216.id=bWV0aG9kOkdhbWUjcGxheVVwVG9Sb3VuZHMoMSk6MTM3
scope.216.kind=method
scope.216.startLine=137
scope.216.endLine=141
scope.216.semanticHash=9880c4e7f4b4461f74e9347469dbfa896d201903300fe5ff176e1119895ecee4
scope.217.id=bWV0aG9kOkdhbWUjcmVtYWluaW5nUGxheWVycygwKTozMDI
scope.217.kind=method
scope.217.startLine=302
scope.217.endLine=304
scope.217.semanticHash=a0e051c1b866b1352982334442d470d1567187f7e091423c51fc78cf3a6f2874
scope.218.id=bWV0aG9kOkdhbWUjcmVzb2x2YWJsZUJ1eW91dCgyKToyNjM
scope.218.kind=method
scope.218.startLine=263
scope.218.endLine=267
scope.218.semanticHash=60193aea7acb7bb2bd806c5a07dbaa4667dce363ef321f1925a9fba671e138da
scope.219.id=bWV0aG9kOkdhbWUjcmVzb2x2ZUJ1eW91dEF0U3RhcnQoMyk6MjUz
scope.219.kind=method
scope.219.startLine=253
scope.219.endLine=261
scope.219.semanticHash=813be9ad2de20348d1c3f4e3ab32f44e77f3c733d62da6eb038615c74509f812
scope.220.id=bWV0aG9kOkdhbWUjcmVzb2x2ZUxlZ2FsRW50aXR5QXRTdGFydCgyKToyMTU
scope.220.kind=method
scope.220.startLine=215
scope.220.endLine=233
scope.220.semanticHash=d0ea82203bbc0dcabc53e18df2dfc2c6dd891c6e038f497eca3d172c61a5e949
scope.221.id=bWV0aG9kOkdhbWUjcmVzb2x2ZVNwbGl0T3duZXJzaGlwQXRTdGFydCgzKToyNDA
scope.221.kind=method
scope.221.startLine=240
scope.221.endLine=242
scope.221.semanticHash=ef2df8efc581363b012c0e7be3f055e9e5bd810881d603974bb10c6cd513850e
scope.222.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oNCk6MzE3
scope.222.kind=method
scope.222.startLine=317
scope.222.endLine=323
scope.222.semanticHash=f390c2edc5e763c9dd207eef3bd7f6dbaa6aae4b82c011607b19c5dbddcb07d5
scope.223.id=bWV0aG9kOkdhbWUjdHJhZGVBdFN0YXJ0KDMpOjI0NA
scope.223.kind=method
scope.223.startLine=244
scope.223.endLine=251
scope.223.semanticHash=00a2ae028121c54e1b0badb12cbea5b9ce67c3e61648228279a91cde6923402b
scope.224.id=bWV0aG9kOkdhbWUjd2lubmVyKDApOjMwNg
scope.224.kind=method
scope.224.startLine=306
scope.224.endLine=309
scope.224.semanticHash=702f44695db994b2e4908c5393ffd81fcd816cff000bc8cb31c6d97c66191345
scope.225.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6NTY4
scope.225.kind=method
scope.225.startLine=568
scope.225.endLine=568
scope.225.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.226.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjU4Mw
scope.226.kind=method
scope.226.startLine=1
scope.226.endLine=764
scope.226.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.227.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjU5Mw
scope.227.kind=method
scope.227.startLine=593
scope.227.endLine=595
scope.227.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.228.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6NTg4
scope.228.kind=method
scope.228.startLine=588
scope.228.endLine=591
scope.228.semanticHash=f2f4e1f3c7bd7244a0e0a2e125110a27d8516e8cb7036d71c5cb73f65468d33f
scope.229.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5BdWN0aW9uV29uI2N0b3IoMyk6NjQ1
scope.229.kind=method
scope.229.startLine=1
scope.229.endLine=764
scope.229.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.230.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUGFpZCNjdG9yKDIpOjcyNw
scope.230.kind=method
scope.230.startLine=1
scope.230.endLine=764
scope.230.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.231.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUmVjZWl2ZWQjY3RvcigyKTo3MzA
scope.231.kind=method
scope.231.startLine=1
scope.231.endLine=764
scope.231.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.232.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rcnVwdCNjdG9yKDIpOjc0OA
scope.232.kind=method
scope.232.startLine=1
scope.232.endLine=764
scope.232.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.233.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Cb3VnaHQjY3RvcigzKTo2NDE
scope.233.kind=method
scope.233.startLine=1
scope.233.endLine=764
scope.233.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.234.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CdWlsZGluZ1JlZnVzZWQjY3RvcigzKTo3MTg
scope.234.kind=method
scope.234.startLine=1
scope.234.endLine=764
scope.234.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.235.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5DaGFuY2VDYXJkRHJhd24jY3RvcigyKTo3MjE
scope.235.kind=method
scope.235.startLine=1
scope.235.endLine=764
scope.235.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.236.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Db21tdW5pdHlDaGVzdENhcmREcmF3biNjdG9yKDIpOjcyNA
scope.236.kind=method
scope.236.startLine=1
scope.236.endLine=764
scope.236.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.237.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkT2ZmZXIjY3RvcigzKTo3MTI
scope.237.kind=method
scope.237.startLine=1
scope.237.endLine=764
scope.237.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.238.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZU5vQmlkZGVyI2N0b3IoMik6NzA5
scope.238.kind=method
scope.238.startLine=1
scope.238.endLine=764
scope.238.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.239.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZVN0YXJ0ZWQjY3RvcigyKTo3MDY
scope.239.kind=method
scope.239.startLine=1
scope.239.endLine=764
scope.239.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.240.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZVdvbiNjdG9yKDMpOjcxNQ
scope.240.kind=method
scope.240.startLine=1
scope.240.endLine=764
scope.240.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.241.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5GaW5hbEFnZSNjdG9yKDIpOjc1Nw
scope.241.kind=method
scope.241.startLine=1
scope.241.endLine=764
scope.241.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.242.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5GaW5hbEJhbGFuY2UjY3RvcigyKTo3NTQ
scope.242.kind=method
scope.242.startLine=1
scope.242.endLine=764
scope.242.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.243.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZUJ1aWx0I2N0b3IoMyk6Njgy
scope.243.kind=method
scope.243.startLine=1
scope.243.endLine=764
scope.243.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.244.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZVNvbGQjY3RvcigzKTo2ODU
scope.244.kind=method
scope.244.startLine=1
scope.244.endLine=764
scope.244.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.245.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbmhlcml0ZWQjY3RvcigzKTo2OTQ
scope.245.kind=method
scope.245.startLine=1
scope.245.endLine=764
scope.245.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.246.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjYwNw
scope.246.kind=method
scope.246.startLine=1
scope.246.endLine=764
scope.246.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.247.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6NjEw
scope.247.kind=method
scope.247.startLine=1
scope.247.endLine=764
scope.247.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.248.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsQ2FyZFVzZWQjY3RvcigxKTo3Mzk
scope.248.kind=method
scope.248.startLine=1
scope.248.endLine=764
scope.248.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.249.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRG91Ymxlc1JvbGxlZCNjdG9yKDEpOjc0Mg
scope.249.kind=method
scope.249.startLine=1
scope.249.endLine=764
scope.249.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.250.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRW50ZXJlZCNjdG9yKDIpOjczMw
scope.250.kind=method
scope.250.startLine=1
scope.250.endLine=764
scope.250.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.251.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRmluZVBhaWQjY3RvcigyKTo3MzY
scope.251.kind=method
scope.251.startLine=1
scope.251.endLine=764
scope.251.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.252.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsU3RheWVkI2N0b3IoMSk6NzQ1
scope.252.kind=method
scope.252.startLine=1
scope.252.endLine=764
scope.252.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.253.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU2FsZVJlZnVzZWQjY3Rvcig0KTo3MDM
scope.253.kind=method
scope.253.startLine=1
scope.253.endLine=764
scope.253.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.254.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU29sZCNjdG9yKDQpOjcwMA
scope.254.kind=method
scope.254.startLine=1
scope.254.endLine=764
scope.254.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.255.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eURpdmlkZW5kUGFpZCNjdG9yKDMpOjY2OQ
scope.255.kind=method
scope.255.startLine=1
scope.255.endLine=764
scope.255.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.256.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUZvcm1lZCNjdG9yKDIpOjY2MA
scope.256.kind=method
scope.256.startLine=1
scope.256.endLine=764
scope.256.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.257.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUxvYW5SYWlzZWQjY3RvcigzKTo2NjM
scope.257.kind=method
scope.257.startLine=1
scope.257.endLine=764
scope.257.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.258.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUxvYW5SZXBhaWQjY3Rvcig0KTo2NjY
scope.258.kind=method
scope.258.startLine=1
scope.258.endLine=764
scope.258.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.259.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUtlcHQjY3RvcigzKTo2OTc
scope.259.kind=method
scope.259.startLine=1
scope.259.endLine=764
scope.259.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.260.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUxpZnRlZCNjdG9yKDQpOjY5MQ
scope.260.kind=method
scope.260.startLine=1
scope.260.endLine=764
scope.260.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.261.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZWQjY3RvcigzKTo2ODg
scope.261.kind=method
scope.261.startLine=1
scope.261.endLine=764
scope.261.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.262.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjYyOA
scope.262.kind=method
scope.262.startLine=628
scope.262.endLine=630
scope.262.semanticHash=a25dcf65a363730c6f293f8a1f1404f79f6c1932a440cc31c1262695a9baa056
scope.263.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDUpOjYyNw
scope.263.kind=method
scope.263.startLine=1
scope.263.endLine=764
scope.263.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.264.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNvZmZpY2lhbFNwYWNlQXQoMSk6NjMy
scope.264.kind=method
scope.264.startLine=632
scope.264.endLine=634
scope.264.semanticHash=d857123e25d1bd7ad9e99a5f83a2cc20dc70a077e141b0d2f4b1de0cd88b32ac
scope.265.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QZWVyVHJhZGUjY3Rvcig0KTo2NDg
scope.265.kind=method
scope.265.startLine=1
scope.265.endLine=764
scope.265.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.266.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QbGF5ZXJQYWlkI2N0b3IoMyk6Njc5
scope.266.kind=method
scope.266.startLine=1
scope.266.endLine=764
scope.266.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.267.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QdXJjaGFzZURlY2xpbmVkI2N0b3IoNSk6Njcy
scope.267.kind=method
scope.267.startLine=1
scope.267.endLine=764
scope.267.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.268.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5SZW50UGFpZCNjdG9yKDQpOjY3Ng
scope.268.kind=method
scope.268.startLine=1
scope.268.endLine=764
scope.268.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.269.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKTo2MjQ
scope.269.kind=method
scope.269.startLine=1
scope.269.endLine=764
scope.269.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.270.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKTo2Mzc
scope.270.kind=method
scope.270.startLine=1
scope.270.endLine=764
scope.270.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.271.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TcGxpdE1vbm9wb2x5UGFpZCNjdG9yKDMpOjY1Nw
scope.271.kind=method
scope.271.startLine=1
scope.271.endLine=764
scope.271.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.272.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TcGxpdE1vbm9wb2x5V29uI2N0b3IoMik6NjU0
scope.272.kind=method
scope.272.startLine=1
scope.272.endLine=764
scope.272.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.273.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFsZW1hdGUjY3RvcigwKTo3NTE
scope.273.kind=method
scope.273.startLine=1
scope.273.endLine=764
scope.273.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.274.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFsZW1hdGVUcmFkaW5nI2N0b3IoMSk6NjUx
scope.274.kind=method
scope.274.startLine=1
scope.274.endLine=764
scope.274.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.275.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjYwMw
scope.275.kind=method
scope.275.startLine=1
scope.275.endLine=764
scope.275.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.276.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDIpOjYxNQ
scope.276.kind=method
scope.276.startLine=615
scope.276.endLine=617
scope.276.semanticHash=4ee4b3a29bce9772f978446cb55e21f8821dbf401952e6475e372a345ad46138
scope.277.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDMpOjYxOQ
scope.277.kind=method
scope.277.startLine=619
scope.277.endLine=621
scope.277.semanticHash=1641f6f5ec3c77f0ec23bfd9fd1bc1ed7e1aeeb17c4bcba8f17a40b4ad21df48
scope.278.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDQpOjYxNA
scope.278.kind=method
scope.278.startLine=1
scope.278.endLine=764
scope.278.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.279.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Xb24jY3RvcigxKTo3NjA
scope.279.kind=method
scope.279.startLine=1
scope.279.endLine=764
scope.279.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.280.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYWdlKDEpOjM0OA
scope.280.kind=method
scope.280.startLine=348
scope.280.endLine=350
scope.280.semanticHash=00eaa719a25296d524cc698f5b08268bd0f905d99cabba453422a09e7d2e4050
scope.281.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYWdlQWZ0ZXIoMSk6MzUy
scope.281.kind=method
scope.281.startLine=352
scope.281.endLine=354
scope.281.semanticHash=3a9971607a214d19087738e8aa3fe147df5f7f16e5a759b13b956913f7a46cb7
scope.282.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYmFua3J1cHQoMik6NTQ5
scope.282.kind=method
scope.282.startLine=549
scope.282.endLine=552
scope.282.semanticHash=84c8992a880ac6e758541bcb41f72cb9a252c2de254e8f3a8ae03888beb87a3d
scope.283.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYm91Z2h0KDMpOjM3Mg
scope.283.kind=method
scope.283.startLine=372
scope.283.endLine=375
scope.283.semanticHash=79e1d988b41c146a0d4c76c5bf1404dcfb5f43bf5ed7caac3fd2b5b0fbd2c437
scope.284.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjYnVpbHRIb3VzZSgzKTo0ODM
scope.284.kind=method
scope.284.startLine=483
scope.284.endLine=486
scope.284.semanticHash=e51ffaaf9fc64c2ff825668ffee31babc9a49fd98e53b320a973887332b1074d
scope.285.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY29sbGVjdGVkU2FsYXJ5KDIpOjM2Ng
scope.285.kind=method
scope.285.startLine=366
scope.285.endLine=370
scope.285.semanticHash=0dbc1e9a73858863807742e595ee44ec3271a180baf14063b808f10ad8f45d18
scope.286.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjY3RvcigyKTozNDY
scope.286.kind=method
scope.286.startLine=1
scope.286.endLine=764
scope.286.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
scope.287.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGVjbGluZWRUb0J1eSg1KTo0Njc
scope.287.kind=method
scope.287.startLine=467
scope.287.endLine=471
scope.287.semanticHash=7dad8584ee95edba7ca11ce127d37d287aaf4042dd6742ea3aefd97724f21418
scope.288.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGlzdHJlc3NlZE9mZmVyKDMpOjQzNw
scope.288.kind=method
scope.288.startLine=437
scope.288.endLine=440
scope.288.semanticHash=2de1aefcb9d72a8e0bb36fc5d8e816aac1fbe6fd90d9c4145d845c338a798d42
scope.289.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGlzdHJlc3NlZFNhbGVOb0JpZGRlcigyKTo0MzI
scope.289.kind=method
scope.289.startLine=432
scope.289.endLine=435
scope.289.semanticHash=acf26ef678e6a966d6ec936898509474be5ac2a3523469a227f6f187fb42b522
scope.290.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGlzdHJlc3NlZFNhbGVTdGFydGVkKDIpOjQyNw
scope.290.kind=method
scope.290.startLine=427
scope.290.endLine=430
scope.290.semanticHash=f22257cc08d2d61fb18392696915ad141cd503c29d6399bc08ebc3d14e57019a
scope.291.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZGlzdHJlc3NlZFNhbGVXb24oMyk6NDQy
scope.291.kind=method
scope.291.startLine=442
scope.291.endLine=445
scope.291.semanticHash=bc40742d43eb9dc85baa428eeca7270774e1c81940b65489373b80dfe62946ce
scope.292.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZHJld0NoYW5jZUNhcmQoMik6NTAz
scope.292.kind=method
scope.292.startLine=503
scope.292.endLine=506
scope.292.semanticHash=c2d3dd8c5dd528d5bf8090da5f0547757d08ffc07fd3f699588877b9ab2cc644
scope.293.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZHJld0NvbW11bml0eUNoZXN0Q2FyZCgyKTo1MDg
scope.293.kind=method
scope.293.startLine=508
scope.293.endLine=511
scope.293.semanticHash=11d7ba10463c79d04b3ea80df07002fc939392f73649bbcb263b0c8ef1bc1e6a
scope.294.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZW50aXR5RGl2aWRlbmRQYWlkKDIpOjQyMg
scope.294.kind=method
scope.294.startLine=422
scope.294.endLine=425
scope.294.semanticHash=4f696cbad2953ed444cc476a5ff399ad352777442b613c7ef704b3f0081227b9
scope.295.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZW50aXR5Rm9ybWVkKDEpOjQwOA
scope.295.kind=method
scope.295.startLine=408
scope.295.endLine=411
scope.295.semanticHash=8ff561c8679c3e3005cc105baf739594db40f1b2e4a32471b00d48f08d2cda37
scope.296.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZW50aXR5TG9hblJhaXNlZCgyKTo0MTM
scope.296.kind=method
scope.296.startLine=413
scope.296.endLine=416
scope.296.semanticHash=1453e97b9f110cea4ea20bf3c070d0dd143b7eb772cbb162a2337ddeecdcca6c
scope.297.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjZW50aXR5TG9hblJlcGFpZCg0KTo0MTg
scope.297.kind=method
scope.297.startLine=418
scope.297.endLine=420
scope.297.semanticHash=ada48afadffebc6fc964081a0ff7e9f92ba75549e50419bb90ce8c997d35e94c
scope.298.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjaW5oZXJpdGVkKDMpOjQ1Mg
scope.298.kind=method
scope.298.startLine=452
scope.298.endLine=455
scope.298.semanticHash=e658917c0bba26af6652047ba4f32060a8892696ec360ac4153d5f122d64fd02
scope.299.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcja2VwdE1vcnRnYWdlKDMpOjQ1Nw
scope.299.kind=method
scope.299.startLine=457
scope.299.endLine=460
scope.299.semanticHash=af7c50b8b4adf43eed5c4914e9693ba3978e4f6aee623806da31fb725e53d74a
scope.300.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGVmdEphaWxCeVBheWluZygyKTo1Mjk
scope.300.kind=method
scope.300.startLine=529
scope.300.endLine=532
scope.300.semanticHash=993f52acd6ec0eceb0d216453eba1ca97476032ea358a9746d3f1225533220ce
scope.301.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGVmdEphaWxCeVJvbGxpbmdEb3VibGVzKDEpOjUzOQ
scope.301.kind=method
scope.301.startLine=539
scope.301.endLine=542
scope.301.semanticHash=7e3073f77b3c33d40e026561c595d8c975d4088635e159d6d48b196be1f41fcf
scope.302.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGVmdEphaWxXaXRoQ2FyZCgxKTo1MzQ
scope.302.kind=method
scope.302.startLine=534
scope.302.endLine=537
scope.302.semanticHash=0eca1027b49296bf1ffec48857f24051dc8ed65d5c3170ab1e113873da565fb9
scope.303.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbGlmdGVkTW9ydGdhZ2UoMyk6NDYy
scope.303.kind=method
scope.303.startLine=462
scope.303.endLine=465
scope.303.semanticHash=197a6e5001b712cbd7977cfde5e0bb401f45ad7fc8c5914f720c256c76f56656
scope.304.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW9ydGdhZ2VkKDMpOjQ0Nw
scope.304.kind=method
scope.304.startLine=447
scope.304.endLine=450
scope.304.semanticHash=4603f42f43f0d481d6e7fc4c95d250fddba4fdd3ff983e723313f111250d8d11
scope.305.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjbW92ZWQoNSk6MzYx
scope.305.kind=method
scope.305.startLine=361
scope.305.endLine=364
scope.305.semanticHash=57ba893b31d09539341b88a45dec4b8648b167b19f0a9b4afacd5710a34d446b
scope.306.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZCgzKTo0Nzg
scope.306.kind=method
scope.306.startLine=478
scope.306.endLine=481
scope.306.semanticHash=bbfe5de1f707f21da4dcef71f01afff91482740647e970d3c03072a7836b2269
scope.307.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZCg0KTo0NzM
scope.307.kind=method
scope.307.startLine=473
scope.307.endLine=476
scope.307.semanticHash=66317d89046f5bdcdf22cb407d9a450e9f7221f4020da0e087ac3b105a7beaa8
scope.308.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGFpZEJhbmsoMik6NTEz
scope.308.kind=method
scope.308.startLine=513
scope.308.endLine=516
scope.308.semanticHash=68b8289c6b9caa436a850d29ac9f703de981f579f49fc4af396225097d422309
scope.309.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcGVlclRyYWRlKDQpOjM5Mg
scope.309.kind=method
scope.309.startLine=392
scope.309.endLine=394
scope.309.semanticHash=b25a3124af96ace92a6c485833608aaee0fd714a1f0f880875605a533b6c6ef6
scope.310.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcmVjZWl2ZWRCYW5rKDIpOjUxOA
scope.310.kind=method
scope.310.startLine=518
scope.310.endLine=521
scope.310.semanticHash=a6258b0d1573f0be24eee767d38df0589a0ae88b68ecc2de87ace7150011325f
scope.311.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcmVmdXNlZEJ1aWxkaW5nKDMpOjQ5OA
scope.311.kind=method
scope.311.startLine=498
scope.311.endLine=501
scope.311.semanticHash=bc9150e16e6d26cf9949ae96894cd793c0d87ac4b6c0fb087c080025dd60a3a8
scope.312.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjcm9sbGVkKDIpOjM1Ng
scope.312.kind=method
scope.312.startLine=356
scope.312.endLine=359
scope.312.semanticHash=41570b54acd7a2a0ee9f75bcefcdd79248dd7db4784b77cea66150ac4e5a790f
scope.313.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc2FsZVJlZnVzZWQoNCk6NDkz
scope.313.kind=method
scope.313.startLine=493
scope.313.endLine=496
scope.313.semanticHash=902eb0534ab31b9b916eb8f3fd7fb549f669e096152fce279c949a5029c28717
scope.314.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc2VudFRvSmFpbCgyKTo1MjM
scope.314.kind=method
scope.314.startLine=523
scope.314.endLine=527
scope.314.semanticHash=03d2c67400cf8e59a2e1afea8d40ef8054591eec4955eacc4ed2d1e2f5cc2ef2
scope.315.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc29sZCg0KTo0ODg
scope.315.kind=method
scope.315.startLine=488
scope.315.endLine=491
scope.315.semanticHash=36ceffd86df9fb98c3fdd440c3cda480841b4012d082ae4a65009180a250f049
scope.316.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc29sZEhvdXNlKDMpOjM4Mg
scope.316.kind=method
scope.316.startLine=382
scope.316.endLine=385
scope.316.semanticHash=0df05f4707d5821c12844685ad057fc45a6be91a84fabf16d7cb0bbcbc606d1a
scope.317.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc29sZFRvUGVlcig0KTozODc
scope.317.kind=method
scope.317.startLine=387
scope.317.endLine=390
scope.317.semanticHash=07c2b312bb64bd273c79a4dde59a6df7e8d168e4e699a41e1e3fae8fc03119b7
scope.318.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc3BsaXRNb25vcG9seVBhaWQoMyk6NDA0
scope.318.kind=method
scope.318.startLine=404
scope.318.endLine=406
scope.318.semanticHash=028b3d323a01e41d86b9de8401ca7a8ab538db7dc4c5ccc676cb52d1fbb85c31
scope.319.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc3BsaXRNb25vcG9seVdvbigyKTo0MDA
scope.319.kind=method
scope.319.startLine=400
scope.319.endLine=402
scope.319.semanticHash=bfe8b0a09cd3886a1c3f7afe528f15ba4c294261cf23f85e16c92afae06fa8c9
scope.320.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc3RhbGVtYXRlVHJhZGluZygxKTozOTY
scope.320.kind=method
scope.320.startLine=396
scope.320.endLine=398
scope.320.semanticHash=76cc7221ec720c1061c9e70ffefe3a85638f58c8e35412a387459a87451c4953
scope.321.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjc3RheWVkSW5KYWlsKDEpOjU0NA
scope.321.kind=method
scope.321.startLine=544
scope.321.endLine=547
scope.321.semanticHash=295e9a5bc9cfede2c3707fd6f1cff98334d5b12e55a97f490737eb8088eadf4e
scope.322.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uKDEpOjU1NA
scope.322.kind=method
scope.322.startLine=554
scope.322.endLine=558
scope.322.semanticHash=a3cb12a1219cd1c4f35cb373d1aec981ee9b92689257395d04bf524c51d94957
scope.323.id=bWV0aG9kOkdhbWUuSm91cm5hbGxpbmcjd29uQXRBdWN0aW9uKDMpOjM3Nw
scope.323.kind=method
scope.323.startLine=377
scope.323.endLine=380
scope.323.semanticHash=db10ed18596729bcd7577e23594ec8451323564e17dc73c245ab874c151f6569
scope.324.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoNCk6NTgw
scope.324.kind=method
scope.324.startLine=1
scope.324.endLine=764
scope.324.semanticHash=eb1b93d90b8ce43fc1854da8e2b62f3704a637b79248fda3efdfd4bcc1b65602
*/
