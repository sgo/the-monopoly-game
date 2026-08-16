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
import java.util.Set;
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
  private static final Set<Street.Colour> ASSET_RICH_COLOURS = Set.of(Street.Colour.orange, Street.Colour.red);

  private final Rule.Set rules;
  private final List<Player> players;
  private final Cups cups;
  private final Strategy.OfPlayers strategies;
  private final Deeds deeds;
  private final Cards.Decks decks;
  private final Jail jail;
  private final boolean stalemateTrading;
  private final boolean legalEntityTrading;
  private final int maxYears;
  private boolean automaticMarketDeadlock = true;
  private boolean roundHadConsolidatingAction;

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
    this(rules, players, cups, strategies, deeds, decks, jail, stalemateTrading, legalEntityTrading, 0);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds,
      Cards.Decks decks, Jail jail, boolean stalemateTrading, boolean legalEntityTrading, int maxYears
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
    this.maxYears = maxYears;
    applyOpeningCapital();
    applyAssetRichOpening();
  }

  /**
   * Game setup opens every account with the standard starting capital and no
   * rule pays anyone before the first roll, so a strategy that supplies an
   * opening capital replaces the account's balance with that capital in place
   * of the rule-set default. Owning this here keeps the setup invariant at the
   * game boundary rather than in a CLI adapter or acceptance harness.
   */
  private void applyOpeningCapital() {
    players.forEach(player -> strategies.forPlayer(player).openingCapital().ifPresent(capital -> {
      Money current = player.account().balance().amount();
      player.account().deposit(capital.minus(current));
    }));
  }

  private void applyAssetRichOpening() {
    players.forEach(player -> {
      if (!strategies.forPlayer(player).assetRichOpening()) return;
      rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
          .filter(street -> ASSET_RICH_COLOURS.contains(street.colourGroup()))
          .forEach(land -> deeds.grant(land, player));
    });
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
    if (rounds == 0) return new Result(List.copyOf(players), List.of(), deeds, Optional.empty());
    if (rounds <= 0) throw new IllegalArgumentException("A game needs at least one round.");
    java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(rounds);
    return playUntilStopped(() -> remaining.getAndDecrement() > 1);
  }

  private Result play(boolean untilComplete, BooleanSupplier keepPlaying) {
    var journal = new Journal();
    Map<Player.ID, Integer> ages = new HashMap<>();
    Journalling journalling = new Journalling(journal, ages, deeds);
    journal.log(new Journal.Entry.Start(ids(players)));
    deeds.legalEntities().forEach(journalling::entityFormed);
    journalling.stalemateTrading(stalemateTrading);
    players.forEach(player -> journalling.strategyNamed(player, strategies.forPlayer(player)));
    List<Player> turnOrder = new Initiative(player -> initiativeRollFor(player, journal)).order(players);
    journal.log(new Journal.Entry.InitiativeWon(turnOrder.getFirst().id()));

    jail.observe(journalling);
    Building building = new Building(deeds, rules, strategies, journalling);
    Player builder = turnOrder.getFirst();
    playTurns(turnOrder, builder, journal, journalling, building, untilComplete, keepPlaying);
    // Asset-rich openings may be inspected or played with a scripted turn flow
    // that never gives the strategy its ordinary development opportunity.
    players.stream().filter(player -> strategies.forPlayer(player).assetRichOpening())
        .forEach(building::develop);

    return new Result(turnOrder, journal.entries(), deeds, winner());
  }

  private void playTurns(List<Player> turnOrder, Player builder, Journal journal,
                         Journalling journalling, Building building, boolean untilComplete,
                         BooleanSupplier keepPlaying) {
    do {
      roundHadConsolidatingAction = false;
      int roundJournalStart = journal.entries().size();
      for (Player player : turnOrder) {
        if (turnEndsTheGame(player, builder, turnOrder, journal, journalling, building)) return;
      }
      completeRound(journal, roundJournalStart, journalling);
      if (remainingPlayers().size() <= 1) return;
      if (Stalemate.reached(rules, players, deeds)) {
        logStalemate(journal, journalling);
        return;
      }
    } while (shouldContinuePlaying(untilComplete, keepPlaying));
  }

  private boolean shouldContinuePlaying(boolean untilComplete, BooleanSupplier keepPlaying) {
    return untilComplete && keepPlaying.getAsBoolean() && remainingPlayers().size() > 1;
  }

  private void completeRound(Journal journal, int roundJournalStart, Journalling journalling) {
    if (roundLoggedABankruptcy(journal, roundJournalStart)) roundHadConsolidatingAction = true;
    operateLegalEntities(journalling);
    if (automaticMarketDeadlock) {
      resolveMarketDeadlockAtRoundBoundary(!roundHadConsolidatingAction, true, journalling);
    }
  }

  private boolean roundLoggedABankruptcy(Journal journal, int roundJournalStart) {
    return journal.entries().subList(roundJournalStart, journal.entries().size())
        .stream().anyMatch(entry -> entry instanceof Journal.Entry.Bankrupt);
  }

  private boolean turnEndsTheGame(Player player, Player builder, List<Player> turnOrder, Journal journal,
                                  Journalling journalling, Building building) {
    return playTurn(player, builder, turnOrder, journal, journalling, building)
        || yearLimitJustReached(journal, journalling);
  }

  private boolean playTurn(Player player, Player builder, List<Player> turnOrder, Journal journal,
                           Journalling journalling, Building building) {
    if (deeds.isBankrupt(player)) return false;
    resolveSplitOwnershipAtStart(player, turnOrder, journalling);
    takeTurn(player, journal, journalling, landingsFor(player, turnOrder, journalling));
    if (mayDevelopThisTurn(player, builder)) developAndTrackConsolidation(player, building);
    if (remainingPlayers().size() <= 1) return true;
    if (!Stalemate.reached(rules, players, deeds)) return false;
    if (player.id().equals(turnOrder.getLast().id())) operateLegalEntities(journalling);
    logStalemate(journal, journalling);
    return true;
  }

  private void logStalemate(Journal journal, Journalling journalling) {
    logGameEnd(journal, new Journal.Entry.Stalemate(), journalling);
  }

  private boolean yearLimitJustReached(Journal journal, Journalling journalling) {
    if (maxYears <= 0 || remainingPlayers().stream().noneMatch(player -> journalling.age(player) >= maxYears))
      return false;
    logGameEnd(journal, new Journal.Entry.YearLimitReached(), journalling);
    return true;
  }

  private void logGameEnd(Journal journal, Journal.Entry endEntry, Journalling journalling) {
    journal.log(endEntry);
    remainingPlayers().forEach(it -> {
      journal.log(new Journal.Entry.FinalBalance(it.id(), it.account().balance().amount()));
      journal.log(new Journal.Entry.FinalAge(it.id(), journalling.age(it)));
    });
  }

  private boolean operateLegalEntities(Journalling journalling) {
    deeds.legalEntities().forEach(entity -> operateEntity(entity, journalling));
    return true;
  }

  private void operateEntity(LegalEntity entity, Journalling journalling) {
    if (!entity.hasShareholders()) return;
    journalOperation(entity, journalling);
  }

  private void journalOperation(LegalEntity entity, Journalling journalling) {
    switch (entity.operate(deeds, strategies, rules)) {
      case LegalEntity.Operation.LoanRepaid it ->
          journalling.entityLoanRepaid(entity, it.shareholder(), it.principal(), it.repayment());
      case LegalEntity.Operation.HouseBuilt it -> journalling.entityHouseBuilt(entity, it.street());
      case LegalEntity.Operation.LoanRaisedAndHouseBuilt it -> {
        journalling.entityLoanRaised(entity, it.loan());
        journalling.entityHouseBuilt(entity, it.street());
      }
      case LegalEntity.Operation.DividendPaid it -> journalling.entityDividendPaid(entity, it.amount());
      case LegalEntity.Operation.NoAction ignored -> { }
    }
  }

  private boolean mayDevelopThisTurn(Player player, Player builder) {
    return isPlayerStillSolvent(player) && (!legalEntityTrading || player.id().equals(builder.id()));
  }

  private boolean isPlayerStillSolvent(Player player) {
    return !deeds.isBankrupt(player);
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
    roundHadConsolidatingAction = true;
    journalling.splitMonopolyWon(outcome.winner(), outcome.loser());
    if (!outcome.payment().equals(Money.ZERO)) journalling.splitMonopolyPaid(
        outcome.winner(), outcome.loser(), outcome.payment());
    return true;
  }

  private void completeTrade(Player trader, Strategy.TradeOffer offer, Journalling journalling) {
    roundHadConsolidatingAction = true;
    deeds.transferWithoutPayment(offer.offered(), trader, offer.partner());
    deeds.transferWithoutPayment(offer.wanted(), offer.partner(), trader);
    journalling.peerTrade(trader, offer.offered(), offer.partner(), offer.wanted());
  }

  private boolean allOwnableSpacesOwned() {
    return rules.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .allMatch(it -> !deeds.isUnowned(it.type()));
  }

  private void developAndTrackConsolidation(Player player, Building building) {
    int housesBefore = totalDevelopments();
    building.develop(player);
    if (totalDevelopments() > housesBefore) roundHadConsolidatingAction = true;
  }

  private int totalDevelopments() {
    return rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .mapToInt(street -> deeds.housesBuiltOn(street) + (deeds.hasHotelOn(street)
            ? street.hotelConstructionRequiresNumberOfHouses() + 1 : 0)).sum();
  }

  /** Applies the automatic legal-entity formation check at a completed quiet round boundary. */
  public void resolveMarketDeadlockAtRoundBoundary(boolean quietRound, boolean collectiveFunding) {
    resolveMarketDeadlockAtRoundBoundary(quietRound, collectiveFunding, null);
  }

  private void resolveMarketDeadlockAtRoundBoundary(boolean quietRound, boolean collectiveFunding,
                                                    Journalling journalling) {
    if (!canFormAtMarketDeadlock(quietRound, collectiveFunding)) return;
    fundableEntityAtMarketDeadlock().ifPresent(entity -> {
      deeds.form(entity);
      if (journalling != null) journalling.entityFormed(entity);
    });
  }

  private boolean canFormAtMarketDeadlock(boolean quietRound, boolean collectiveFunding) {
    return quietRound && collectiveFunding && legalEntityTrading && allOwnableSpacesOwned();
  }

  private Optional<LegalEntity> fundableEntityAtMarketDeadlock() {
    return rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .map(ColourStreet::colourGroup).distinct()
        .map(this::formIfFundable)
        .filter(Optional::isPresent).map(Optional::orElseThrow).findFirst();
  }

  private Optional<LegalEntity> formIfFundable(Street.Colour colour) {
    List<ColourStreet> streets = LegalEntity.streetsOf(colour, rules);
    List<Player> shareholders = players.stream()
        .filter(player -> streets.stream().anyMatch(street -> deeds.ownerOf(street.type())
            .filter(player.id()::equals).isPresent()))
        .toList();
    return LegalEntity.form(entityName(colour), colour, shareholders, rules, deeds,
        street -> Strategy.priorityOf(street) == Strategy.Priority.HIGHEST)
        .filter(entity -> entity.canFundNextImprovement(strategies, rules, deeds));
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

      record StrategyNamed(Player.ID player, String name, boolean legalEntityEnabled,
                           boolean stalemateEnabled, boolean assetRichOpening) implements Entry {
        public StrategyNamed(Player.ID player, String name, boolean legalEntityEnabled, boolean stalemateEnabled) {
          this(player, name, legalEntityEnabled, stalemateEnabled, false);
        }
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

      record LegalEntityShareSold(String name, Player.ID seller, Player.ID buyer, Money price) implements Entry {
      }

      record LegalEntityLiquidated(String name, Player.ID recipient, Money amount) implements Entry {
      }

      record LegalEntityRentPaid(String name, Player.ID tenant, Street.Type land, Money rent) implements Entry {
      }

      record LegalEntityHouseBuilt(String name, Street.Type land, Money price) implements Entry {
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

      record YearLimitReached() implements Entry {
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
moduleHash=dcc9530b715dec8e1f26d2b0ab4ee241f80f5e783240fbb07035596d2d87e5a2
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjQ5
scope.0.kind=class
scope.0.startLine=49
scope.0.endLine=696
scope.0.semanticHash=ff9bccd6580047363d1102b88783ef25e3120f4a22cd414b7598bb438140ec17
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6NDc2
scope.1.kind=class
scope.1.startLine=476
scope.1.endLine=479
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6NDkz
scope.2.kind=class
scope.2.startLine=493
scope.2.endLine=695
scope.2.semanticHash=c76c12dc2f101b30c7d9232c3e33e715d1c3e1da158613321640c6e5f19f0034
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjUxMg
scope.3.kind=class
scope.3.startLine=512
scope.3.endLine=694
scope.3.semanticHash=3c72e5a0ed03a40c76a5fee324b92c4cdd104eea6f67a65c7f40275e86cee60b
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jQXVjdGlvbldvbjo1NTU
scope.4.kind=class
scope.4.startLine=555
scope.4.endLine=556
scope.4.semanticHash=71ee77afd5451e73d54900eb221aa9640cfe5a2b42680f570ad5efdf9e473cb9
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI0JhbmtQYWlkOjY1Ng
scope.5.kind=class
scope.5.startLine=656
scope.5.endLine=657
scope.5.semanticHash=cce5d5e05e58aed8d75d4ca2deb97d6d8388449cd41d7b4bc422482a269ff5b4
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNCYW5rUmVjZWl2ZWQ6NjU5
scope.6.kind=class
scope.6.startLine=659
scope.6.endLine=660
scope.6.semanticHash=02d04a8dd004416ac824aee0a5687eb08034ac9dcbe0bae2355581bd183f3790
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I0JhbmtydXB0OjY3Nw
scope.7.kind=class
scope.7.startLine=677
scope.7.endLine=678
scope.7.semanticHash=16825b9c28c79a36f8a880d0adc21014ea4b665f40f0fb2eb70ef7ece3155e0b
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNCb3VnaHQ6NTUx
scope.8.kind=class
scope.8.startLine=551
scope.8.endLine=552
scope.8.semanticHash=27025028a74b83acd66fd8557558fb235025cf183bcc4e3623183b26da003aec
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNCdWlsZGluZ1JlZnVzZWQ6NjQ3
scope.9.kind=class
scope.9.startLine=647
scope.9.endLine=648
scope.9.semanticHash=c46235e296f36ff19ba4b74246e14db6236dced240c6de450bc4186fef89d59b
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNDaGFuY2VDYXJkRHJhd246NjUw
scope.10.kind=class
scope.10.startLine=650
scope.10.endLine=651
scope.10.semanticHash=a926e22751d95373a632fb1725c91e97b535d7cf2eb62c29d7d515570e3e3344
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI0NvbW11bml0eUNoZXN0Q2FyZERyYXduOjY1Mw
scope.11.kind=class
scope.11.startLine=653
scope.11.endLine=654
scope.11.semanticHash=3a1c04eb7d87bf7ea391ae3f28c5fecb5d887626ebce02e8f7ab581a4ce85290
scope.12.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNEaXN0cmVzc2VkT2ZmZXI6NjQx
scope.12.kind=class
scope.12.startLine=641
scope.12.endLine=642
scope.12.semanticHash=a6b26851b984f848f04bdd88b35a8e6173605e1d87739ac1f81895b2f786a8cf
scope.13.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjRGlzdHJlc3NlZFNhbGVOb0JpZGRlcjo2Mzg
scope.13.kind=class
scope.13.startLine=638
scope.13.endLine=639
scope.13.semanticHash=75e912e170f8d8fa05b68bb0cf8b559956b819929182eb968e2b53d51012c9b7
scope.14.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNEaXN0cmVzc2VkU2FsZVN0YXJ0ZWQ6NjM1
scope.14.kind=class
scope.14.startLine=635
scope.14.endLine=636
scope.14.semanticHash=5b2163fb1a971085705c59755fe2387b0bdd1a91016841925222a61184d97e11
scope.15.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI0Rpc3RyZXNzZWRTYWxlV29uOjY0NA
scope.15.kind=class
scope.15.startLine=644
scope.15.endLine=645
scope.15.semanticHash=7665fa2235db4f1f740916093d7b1cb0f3a1bcdd186c0b53f9ff6e2d5652f1f6
scope.16.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI0ZpbmFsQWdlOjY4OQ
scope.16.kind=class
scope.16.startLine=689
scope.16.endLine=690
scope.16.semanticHash=174faa5146bf4e6b710a1dc3a9e2a96bc71d0c264c37895b044997623e4c691d
scope.17.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNGaW5hbEJhbGFuY2U6Njg2
scope.17.kind=class
scope.17.startLine=686
scope.17.endLine=687
scope.17.semanticHash=f991eb829ddb2423403d242bcdbdd98ba3199698ebd5c3ebd2dcb0d5cfe0a627
scope.18.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjSG91c2VCdWlsdDo2MTE
scope.18.kind=class
scope.18.startLine=611
scope.18.endLine=612
scope.18.semanticHash=27575972b2787c07a6fa98a725c5bed4487591e647678d5880869a7c637aee97
scope.19.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNIb3VzZVNvbGQ6NjE0
scope.19.kind=class
scope.19.startLine=614
scope.19.endLine=615
scope.19.semanticHash=7bfd22802262e4e36e5ffa57b44a79dc928c71eaa883b7f110cd1b9e7f7230b2
scope.20.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNJbmhlcml0ZWQ6NjIz
scope.20.kind=class
scope.20.startLine=623
scope.20.endLine=624
scope.20.semanticHash=4e87cf40a11022ccf4933f9a448697b3a8224c48633ac93bd888d686f9632d19
scope.21.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjUxNw
scope.21.kind=class
scope.21.startLine=517
scope.21.endLine=518
scope.21.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.22.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjo1MjA
scope.22.kind=class
scope.22.startLine=520
scope.22.endLine=521
scope.22.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.23.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxDYXJkVXNlZCNKYWlsQ2FyZFVzZWQ6NjY4
scope.23.kind=class
scope.23.startLine=668
scope.23.endLine=669
scope.23.semanticHash=78d932232a0f5e673d3dc6c6d78e5ba0e266df879e171af6995e7c6686e39ff5
scope.24.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxEb3VibGVzUm9sbGVkI0phaWxEb3VibGVzUm9sbGVkOjY3MQ
scope.24.kind=class
scope.24.startLine=671
scope.24.endLine=672
scope.24.semanticHash=7103e2c440de0b5645f3f7249799dd79a41fc35d18ec6f0287ae995d1d07be51
scope.25.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI0phaWxFbnRlcmVkOjY2Mg
scope.25.kind=class
scope.25.startLine=662
scope.25.endLine=663
scope.25.semanticHash=72be50c2af861ce8f500dde65a8bfa767ceb307161772739134a6968fd43b907
scope.26.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNKYWlsRmluZVBhaWQ6NjY1
scope.26.kind=class
scope.26.startLine=665
scope.26.endLine=666
scope.26.semanticHash=88ed119890c309e1480e4400ae91bdb79250c1dd70940253050a847641cb4283
scope.27.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxTdGF5ZWQjSmFpbFN0YXllZDo2NzQ
scope.27.kind=class
scope.27.startLine=674
scope.27.endLine=675
scope.27.semanticHash=15c417a86539b6369b8adabdfdc67525574d0262be87768fc04e199c4b2daa60
scope.28.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNMYW5kU2FsZVJlZnVzZWQ6NjMy
scope.28.kind=class
scope.28.startLine=632
scope.28.endLine=633
scope.28.semanticHash=071a81b29122c94c28ba6cee1630b23eeb7b6acc5cf69c07a622eb04893d79ef
scope.29.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI0xhbmRTb2xkOjYyOQ
scope.29.kind=class
scope.29.startLine=629
scope.29.endLine=630
scope.29.semanticHash=fd9ac419a61f440251c0473ce96aa04eb9d1e3761c450d6e7d55b0f1d92a105e
scope.30.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI0xlZ2FsRW50aXR5RGl2aWRlbmRQYWlkOjU4Ng
scope.30.kind=class
scope.30.startLine=586
scope.30.endLine=587
scope.30.semanticHash=a6b973b482e59b7949c8ceb0e26d5ba94a15e1a58524a46c28931c27273d0ba3
scope.31.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI0xlZ2FsRW50aXR5Rm9ybWVkOjU3Nw
scope.31.kind=class
scope.31.startLine=577
scope.31.endLine=578
scope.31.semanticHash=631fdf7745dde5d4380f5cdef077abbf6488d087a9fdfc52335a91183bbda172
scope.32.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5SG91c2VCdWlsdCNMZWdhbEVudGl0eUhvdXNlQnVpbHQ6NTk4
scope.32.kind=class
scope.32.startLine=598
scope.32.endLine=599
scope.32.semanticHash=9744de81e9f930bb9fb4e78771dc03aaa7ce97ca0d24a6af6d47e7476d2ebf81
scope.33.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TGlxdWlkYXRlZCNMZWdhbEVudGl0eUxpcXVpZGF0ZWQ6NTky
scope.33.kind=class
scope.33.startLine=592
scope.33.endLine=593
scope.33.semanticHash=aa6213161a5232da2eb3836e961532f7ba19584f86f7da6b956a668c0ffe23ac
scope.34.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNMZWdhbEVudGl0eUxvYW5SYWlzZWQ6NTgw
scope.34.kind=class
scope.34.startLine=580
scope.34.endLine=581
scope.34.semanticHash=da73a08e8b2cd04078088cbd60125d7682d83240f4184b8a74a41930af31edf1
scope.35.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNMZWdhbEVudGl0eUxvYW5SZXBhaWQ6NTgz
scope.35.kind=class
scope.35.startLine=583
scope.35.endLine=584
scope.35.semanticHash=136375fc94d9d917f2bed703b3d18023767213fe5b45a803857ec6e27243300d
scope.36.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjTGVnYWxFbnRpdHlSZW50UGFpZDo1OTU
scope.36.kind=class
scope.36.startLine=595
scope.36.endLine=596
scope.36.semanticHash=806d2d19304f51898673df0adbd7aaddb857527a93dffd2a5788f9137a315e04
scope.37.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI0xlZ2FsRW50aXR5U2hhcmVTb2xkOjU4OQ
scope.37.kind=class
scope.37.startLine=589
scope.37.endLine=590
scope.37.semanticHash=b94d627e181f8cae0481a74bb579c3ff62ff127d9a0199587e685a200da18864
scope.38.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNNb3J0Z2FnZUtlcHQ6NjI2
scope.38.kind=class
scope.38.startLine=626
scope.38.endLine=627
scope.38.semanticHash=bf247aef5b7c272b93350d039dbbc80307604012fab76265fc78befb32c6355d
scope.39.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI01vcnRnYWdlTGlmdGVkOjYyMA
scope.39.kind=class
scope.39.startLine=620
scope.39.endLine=621
scope.39.semanticHash=876fc18a90cbc579ad9618bc95f03fa33b7cdeae3c7f18f0ead53f7795f64237
scope.40.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNNb3J0Z2FnZWQ6NjE3
scope.40.kind=class
scope.40.startLine=617
scope.40.endLine=618
scope.40.semanticHash=2536842d77794ad82293557093f6b95391662318c0cf866281f0bfeb54b47212
scope.41.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjUzNw
scope.41.kind=class
scope.41.startLine=537
scope.41.endLine=545
scope.41.semanticHash=ed37919856542e0d29f91d0622487a42cbe6023a70d3c23b3950fc66a5e8f1ab
scope.42.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNQZWVyVHJhZGU6NTU4
scope.42.kind=class
scope.42.startLine=558
scope.42.endLine=559
scope.42.semanticHash=0b4feb9a22fe8ab12f1803c2626a29366e17eca02670c18aa1efec0c2512fb6d
scope.43.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjUGxheWVyUGFpZDo2MDg
scope.43.kind=class
scope.43.startLine=608
scope.43.endLine=609
scope.43.semanticHash=ecda18178391ece7e75c3e72ec3f854adff15a3950fc135b48bdf7e6cb119a23
scope.44.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjUHVyY2hhc2VEZWNsaW5lZDo2MDE
scope.44.kind=class
scope.44.startLine=601
scope.44.endLine=603
scope.44.semanticHash=72af27050d45a9fbfac729c104126892ecd90a709dfdce45deca6935b40546a4
scope.45.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI1JlbnRQYWlkOjYwNQ
scope.45.kind=class
scope.45.startLine=605
scope.45.endLine=606
scope.45.semanticHash=47c40c5b19c0d8df73eda2478d761c7a7e8604713b0719926b1f46a2f2b6f104
scope.46.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6NTM0
scope.46.kind=class
scope.46.startLine=534
scope.46.endLine=535
scope.46.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.47.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6NTQ3
scope.47.kind=class
scope.47.startLine=547
scope.47.endLine=548
scope.47.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.48.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI1NwbGl0TW9ub3BvbHlQYWlkOjU3NA
scope.48.kind=class
scope.48.startLine=574
scope.48.endLine=575
scope.48.semanticHash=088ba655dca222db0149859641809b25dcc4469fc60702c2129eec19d7cf93ee
scope.49.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jU3BsaXRNb25vcG9seVdvbjo1NzE
scope.49.kind=class
scope.49.startLine=571
scope.49.endLine=572
scope.49.semanticHash=7eab67f65f325bc29dc9eed6c1b7f342f135afe2dadc8b640001776424af9ad8
scope.50.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZSNTdGFsZW1hdGU6Njgw
scope.50.kind=class
scope.50.startLine=680
scope.50.endLine=681
scope.50.semanticHash=d706ac5ec3788f780b9dced589058470dac53a78cd99870750604517e057e2b4
scope.51.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZVRyYWRpbmcjU3RhbGVtYXRlVHJhZGluZzo1NjE
scope.51.kind=class
scope.51.startLine=561
scope.51.endLine=562
scope.51.semanticHash=b5eb812ee87ce82f7b000eb8f037883ca2ca2e04a1679052bb93a2d2020c4eca
scope.52.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjUxMw
scope.52.kind=class
scope.52.startLine=513
scope.52.endLine=514
scope.52.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.53.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjU3RyYXRlZ3lOYW1lZDo1NjQ
scope.53.kind=class
scope.53.startLine=564
scope.53.endLine=569
scope.53.semanticHash=973caa0971b4f2e4b8bc8a44fa50149d837f3dc255349b7065c839c2623f027e
scope.54.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjUyNA
scope.54.kind=class
scope.54.startLine=524
scope.54.endLine=532
scope.54.semanticHash=611be8f7912e6193ac83d3badf59a472e4cb21571d4be0e07854eaec325c9099
scope.55.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNXb246Njky
scope.55.kind=class
scope.55.startLine=692
scope.55.endLine=693
scope.55.semanticHash=1018a3f41b5571c335e5fbf1476a6a3112c2284616837f2e0c7fbd00dd3d8b76
scope.56.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlllYXJMaW1pdFJlYWNoZWQjWWVhckxpbWl0UmVhY2hlZDo2ODM
scope.56.kind=class
scope.56.startLine=683
scope.56.endLine=684
scope.56.semanticHash=e3605769311df1d0de936dc2ecfde31f3e09144c63e0739ae4525e663fb01002
scope.57.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjQ5MA
scope.57.kind=class
scope.57.startLine=490
scope.57.endLine=491
scope.57.semanticHash=024a5de82b58c6e09d33d689b003f51dcd43a63a5e94cb88b5d8b96d1706df96
scope.58.id=ZmllbGQ6R2FtZSNBU1NFVF9SSUNIX0NPTE9VUlM6NTA
scope.58.kind=field
scope.58.startLine=50
scope.58.endLine=50
scope.58.semanticHash=dc2e75c3d89687b23d7590e013c91f733cf3b46100d977d5b44d9b0b3ac9df7f
scope.59.id=ZmllbGQ6R2FtZSNhdXRvbWF0aWNNYXJrZXREZWFkbG9jazo2Mg
scope.59.kind=field
scope.59.startLine=62
scope.59.endLine=62
scope.59.semanticHash=17af0925d6fc4bdd873e9243b773529589dc75a853b704ef553e424e86c8ad6c
scope.60.id=ZmllbGQ6R2FtZSNjdXBzOjU0
scope.60.kind=field
scope.60.startLine=54
scope.60.endLine=54
scope.60.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.61.id=ZmllbGQ6R2FtZSNkZWNrczo1Nw
scope.61.kind=field
scope.61.startLine=57
scope.61.endLine=57
scope.61.semanticHash=130541f31392b2fc32d3c8343ebd76de366010b8930395e22ba244946f508252
scope.62.id=ZmllbGQ6R2FtZSNkZWVkczo1Ng
scope.62.kind=field
scope.62.startLine=56
scope.62.endLine=56
scope.62.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.63.id=ZmllbGQ6R2FtZSNqYWlsOjU4
scope.63.kind=field
scope.63.startLine=58
scope.63.endLine=58
scope.63.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.64.id=ZmllbGQ6R2FtZSNsZWdhbEVudGl0eVRyYWRpbmc6NjA
scope.64.kind=field
scope.64.startLine=60
scope.64.endLine=60
scope.64.semanticHash=79e35a24ea51a961b285f1431176277ba37f40aebb7dc85ea35c6c3e9ef9567e
scope.65.id=ZmllbGQ6R2FtZSNtYXhZZWFyczo2MQ
scope.65.kind=field
scope.65.startLine=61
scope.65.endLine=61
scope.65.semanticHash=32059abe263315354f8e91ea653d395a023f3272c81d2506899568171495cdba
scope.66.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjUz
scope.66.kind=field
scope.66.startLine=53
scope.66.endLine=53
scope.66.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.67.id=ZmllbGQ6R2FtZSNyb3VuZEhhZENvbnNvbGlkYXRpbmdBY3Rpb246NjM
scope.67.kind=field
scope.67.startLine=63
scope.67.endLine=63
scope.67.semanticHash=4b742b5592bea6f2abde227ca75f29f5f05380aeb9c069a22db7fd39a93d18cc
scope.68.id=ZmllbGQ6R2FtZSNydWxlczo1Mg
scope.68.kind=field
scope.68.startLine=52
scope.68.endLine=52
scope.68.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.69.id=ZmllbGQ6R2FtZSNzdGFsZW1hdGVUcmFkaW5nOjU5
scope.69.kind=field
scope.69.startLine=59
scope.69.endLine=59
scope.69.semanticHash=3fb0db6ec778e457ec4b9262d01f922604291d8cbeefa5df7e177c0d5beea6b1
scope.70.id=ZmllbGQ6R2FtZSNzdHJhdGVnaWVzOjU1
scope.70.kind=field
scope.70.startLine=55
scope.70.endLine=55
scope.70.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.71.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6NDk2
scope.71.kind=field
scope.71.startLine=496
scope.71.endLine=496
scope.71.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.72.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjo0OTQ
scope.72.kind=field
scope.72.startLine=494
scope.72.endLine=494
scope.72.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.73.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jbGFuZDo1NTU
scope.73.kind=field
scope.73.startLine=555
scope.73.endLine=555
scope.73.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.74.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcGxheWVyOjU1NQ
scope.74.kind=field
scope.74.startLine=555
scope.74.endLine=555
scope.74.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.75.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcHJpY2U6NTU1
scope.75.kind=field
scope.75.startLine=555
scope.75.endLine=555
scope.75.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.76.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI2Ftb3VudDo2NTY
scope.76.kind=field
scope.76.startLine=656
scope.76.endLine=656
scope.76.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.77.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI3BsYXllcjo2NTY
scope.77.kind=field
scope.77.startLine=656
scope.77.endLine=656
scope.77.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.78.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNhbW91bnQ6NjU5
scope.78.kind=field
scope.78.startLine=659
scope.78.endLine=659
scope.78.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.79.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNwbGF5ZXI6NjU5
scope.79.kind=field
scope.79.startLine=659
scope.79.endLine=659
scope.79.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.80.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I2NyZWRpdG9yOjY3Nw
scope.80.kind=field
scope.80.startLine=677
scope.80.endLine=677
scope.80.semanticHash=04806e2a3ca47061887c26b1a6e5df08f09b4b4e10f22dac41fe60a342b7338b
scope.81.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I3BsYXllcjo2Nzc
scope.81.kind=field
scope.81.startLine=677
scope.81.endLine=677
scope.81.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.82.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNsYW5kOjU1MQ
scope.82.kind=field
scope.82.startLine=551
scope.82.endLine=551
scope.82.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.83.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwbGF5ZXI6NTUx
scope.83.kind=field
scope.83.startLine=551
scope.83.endLine=551
scope.83.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.84.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwcmljZTo1NTE
scope.84.kind=field
scope.84.startLine=551
scope.84.endLine=551
scope.84.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.85.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNsYW5kOjY0Nw
scope.85.kind=field
scope.85.startLine=647
scope.85.endLine=647
scope.85.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.86.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwbGF5ZXI6NjQ3
scope.86.kind=field
scope.86.startLine=647
scope.86.endLine=647
scope.86.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.87.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwcmljZTo2NDc
scope.87.kind=field
scope.87.startLine=647
scope.87.endLine=647
scope.87.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.88.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNjYXJkOjY1MA
scope.88.kind=field
scope.88.startLine=650
scope.88.endLine=650
scope.88.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.89.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNwbGF5ZXI6NjUw
scope.89.kind=field
scope.89.startLine=650
scope.89.endLine=650
scope.89.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.90.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI2NhcmQ6NjUz
scope.90.kind=field
scope.90.startLine=653
scope.90.endLine=653
scope.90.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.91.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI3BsYXllcjo2NTM
scope.91.kind=field
scope.91.startLine=653
scope.91.endLine=653
scope.91.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.92.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNiaWRkZXI6NjQx
scope.92.kind=field
scope.92.startLine=641
scope.92.endLine=641
scope.92.semanticHash=b8ec71792a9a472362073b97719425ae8b5e956ba271d8efedb75371e8312526
scope.93.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNsYW5kOjY0MQ
scope.93.kind=field
scope.93.startLine=641
scope.93.endLine=641
scope.93.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.94.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNwcmljZTo2NDE
scope.94.kind=field
scope.94.startLine=641
scope.94.endLine=641
scope.94.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.95.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjbGFuZDo2Mzg
scope.95.kind=field
scope.95.startLine=638
scope.95.endLine=638
scope.95.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.96.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjc2VsbGVyOjYzOA
scope.96.kind=field
scope.96.startLine=638
scope.96.endLine=638
scope.96.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.97.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNsYW5kOjYzNQ
scope.97.kind=field
scope.97.startLine=635
scope.97.endLine=635
scope.97.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.98.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNzZWxsZXI6NjM1
scope.98.kind=field
scope.98.startLine=635
scope.98.endLine=635
scope.98.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.99.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI2JpZGRlcjo2NDQ
scope.99.kind=field
scope.99.startLine=644
scope.99.endLine=644
scope.99.semanticHash=b8ec71792a9a472362073b97719425ae8b5e956ba271d8efedb75371e8312526
scope.100.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI2xhbmQ6NjQ0
scope.100.kind=field
scope.100.startLine=644
scope.100.endLine=644
scope.100.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.101.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI3ByaWNlOjY0NA
scope.101.kind=field
scope.101.startLine=644
scope.101.endLine=644
scope.101.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.102.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI2FnZTo2ODk
scope.102.kind=field
scope.102.startLine=689
scope.102.endLine=689
scope.102.semanticHash=73cf07319348f7df4be5ec1725d9e768231a03a1f8a9a8230686f74c2a9e64d3
scope.103.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI3BsYXllcjo2ODk
scope.103.kind=field
scope.103.startLine=689
scope.103.endLine=689
scope.103.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.104.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNiYWxhbmNlOjY4Ng
scope.104.kind=field
scope.104.startLine=686
scope.104.endLine=686
scope.104.semanticHash=9ba2008e7dc4127b70833455dbf24f667e36899897c47cbf225bbc8bd4f5575a
scope.105.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNwbGF5ZXI6Njg2
scope.105.kind=field
scope.105.startLine=686
scope.105.endLine=686
scope.105.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.106.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjbGFuZDo2MTE
scope.106.kind=field
scope.106.startLine=611
scope.106.endLine=611
scope.106.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.107.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcGxheWVyOjYxMQ
scope.107.kind=field
scope.107.startLine=611
scope.107.endLine=611
scope.107.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.108.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcHJpY2U6NjEx
scope.108.kind=field
scope.108.startLine=611
scope.108.endLine=611
scope.108.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.109.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNsYW5kOjYxNA
scope.109.kind=field
scope.109.startLine=614
scope.109.endLine=614
scope.109.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.110.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwbGF5ZXI6NjE0
scope.110.kind=field
scope.110.startLine=614
scope.110.endLine=614
scope.110.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.111.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwcmljZTo2MTQ
scope.111.kind=field
scope.111.startLine=614
scope.111.endLine=614
scope.111.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.112.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNkZWJ0b3I6NjIz
scope.112.kind=field
scope.112.startLine=623
scope.112.endLine=623
scope.112.semanticHash=7187277bc5d3a4f7eb1846526a3403b2a46995f8b6f5195af4e3989efac8c17f
scope.113.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNsYW5kOjYyMw
scope.113.kind=field
scope.113.startLine=623
scope.113.endLine=623
scope.113.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.114.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNwbGF5ZXI6NjIz
scope.114.kind=field
scope.114.startLine=623
scope.114.endLine=623
scope.114.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.115.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjo1MTc
scope.115.kind=field
scope.115.startLine=517
scope.115.endLine=517
scope.115.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.116.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjUxNw
scope.116.kind=field
scope.116.startLine=517
scope.116.endLine=517
scope.116.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.117.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjUyMA
scope.117.kind=field
scope.117.startLine=520
scope.117.endLine=520
scope.117.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.118.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxDYXJkVXNlZCNwbGF5ZXI6NjY4
scope.118.kind=field
scope.118.startLine=668
scope.118.endLine=668
scope.118.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.119.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxEb3VibGVzUm9sbGVkI3BsYXllcjo2NzE
scope.119.kind=field
scope.119.startLine=671
scope.119.endLine=671
scope.119.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.120.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI2NhdXNlOjY2Mg
scope.120.kind=field
scope.120.startLine=662
scope.120.endLine=662
scope.120.semanticHash=f0527b6e66e3f950052646384f5a7c874593c4a39f0fb849428625529221f17b
scope.121.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI3BsYXllcjo2NjI
scope.121.kind=field
scope.121.startLine=662
scope.121.endLine=662
scope.121.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.122.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNmaW5lOjY2NQ
scope.122.kind=field
scope.122.startLine=665
scope.122.endLine=665
scope.122.semanticHash=e365883bc779c1a28df50988a532d020ef388a587ebb4e7379f58733cfb94b21
scope.123.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNwbGF5ZXI6NjY1
scope.123.kind=field
scope.123.startLine=665
scope.123.endLine=665
scope.123.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.124.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxTdGF5ZWQjcGxheWVyOjY3NA
scope.124.kind=field
scope.124.startLine=674
scope.124.endLine=674
scope.124.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.125.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNidXllcjo2MzI
scope.125.kind=field
scope.125.startLine=632
scope.125.endLine=632
scope.125.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.126.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNsYW5kOjYzMg
scope.126.kind=field
scope.126.startLine=632
scope.126.endLine=632
scope.126.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.127.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNwcmljZTo2MzI
scope.127.kind=field
scope.127.startLine=632
scope.127.endLine=632
scope.127.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.128.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNzZWxsZXI6NjMy
scope.128.kind=field
scope.128.startLine=632
scope.128.endLine=632
scope.128.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.129.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2J1eWVyOjYyOQ
scope.129.kind=field
scope.129.startLine=629
scope.129.endLine=629
scope.129.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.130.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2xhbmQ6NjI5
scope.130.kind=field
scope.130.startLine=629
scope.130.endLine=629
scope.130.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.131.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3ByaWNlOjYyOQ
scope.131.kind=field
scope.131.startLine=629
scope.131.endLine=629
scope.131.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.132.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3NlbGxlcjo2Mjk
scope.132.kind=field
scope.132.startLine=629
scope.132.endLine=629
scope.132.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.133.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI2Ftb3VudDo1ODY
scope.133.kind=field
scope.133.startLine=586
scope.133.endLine=586
scope.133.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.134.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI25hbWU6NTg2
scope.134.kind=field
scope.134.startLine=586
scope.134.endLine=586
scope.134.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.135.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI3NoYXJlaG9sZGVyczo1ODY
scope.135.kind=field
scope.135.startLine=586
scope.135.endLine=586
scope.135.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.136.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI25hbWU6NTc3
scope.136.kind=field
scope.136.startLine=577
scope.136.endLine=577
scope.136.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.137.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI3NoYXJlaG9sZGVyczo1Nzc
scope.137.kind=field
scope.137.startLine=577
scope.137.endLine=577
scope.137.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.138.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5SG91c2VCdWlsdCNsYW5kOjU5OA
scope.138.kind=field
scope.138.startLine=598
scope.138.endLine=598
scope.138.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.139.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5SG91c2VCdWlsdCNuYW1lOjU5OA
scope.139.kind=field
scope.139.startLine=598
scope.139.endLine=598
scope.139.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.140.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5SG91c2VCdWlsdCNwcmljZTo1OTg
scope.140.kind=field
scope.140.startLine=598
scope.140.endLine=598
scope.140.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.141.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TGlxdWlkYXRlZCNhbW91bnQ6NTky
scope.141.kind=field
scope.141.startLine=592
scope.141.endLine=592
scope.141.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.142.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TGlxdWlkYXRlZCNuYW1lOjU5Mg
scope.142.kind=field
scope.142.startLine=592
scope.142.endLine=592
scope.142.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.143.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TGlxdWlkYXRlZCNyZWNpcGllbnQ6NTky
scope.143.kind=field
scope.143.startLine=592
scope.143.endLine=592
scope.143.semanticHash=672b1c509fd6fdd87931787528a8e9d324c264aeb5d13fe775aa6e5220d9a69a
scope.144.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNhbW91bnQ6NTgw
scope.144.kind=field
scope.144.startLine=580
scope.144.endLine=580
scope.144.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.145.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNuYW1lOjU4MA
scope.145.kind=field
scope.145.startLine=580
scope.145.endLine=580
scope.145.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.146.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNzaGFyZWhvbGRlcnM6NTgw
scope.146.kind=field
scope.146.startLine=580
scope.146.endLine=580
scope.146.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.147.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNuYW1lOjU4Mw
scope.147.kind=field
scope.147.startLine=583
scope.147.endLine=583
scope.147.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.148.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNwcmluY2lwYWw6NTgz
scope.148.kind=field
scope.148.startLine=583
scope.148.endLine=583
scope.148.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.149.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNyZXBheW1lbnQ6NTgz
scope.149.kind=field
scope.149.startLine=583
scope.149.endLine=583
scope.149.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.150.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNzaGFyZWhvbGRlcjo1ODM
scope.150.kind=field
scope.150.startLine=583
scope.150.endLine=583
scope.150.semanticHash=5afb4f38ca9ee8f6c22bd1cea0ff3bcc6387deb8673bd78cb1c57d4e6b9e3e1d
scope.151.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjbGFuZDo1OTU
scope.151.kind=field
scope.151.startLine=595
scope.151.endLine=595
scope.151.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.152.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjbmFtZTo1OTU
scope.152.kind=field
scope.152.startLine=595
scope.152.endLine=595
scope.152.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.153.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjcmVudDo1OTU
scope.153.kind=field
scope.153.startLine=595
scope.153.endLine=595
scope.153.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.154.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjdGVuYW50OjU5NQ
scope.154.kind=field
scope.154.startLine=595
scope.154.endLine=595
scope.154.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.155.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI2J1eWVyOjU4OQ
scope.155.kind=field
scope.155.startLine=589
scope.155.endLine=589
scope.155.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.156.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI25hbWU6NTg5
scope.156.kind=field
scope.156.startLine=589
scope.156.endLine=589
scope.156.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.157.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI3ByaWNlOjU4OQ
scope.157.kind=field
scope.157.startLine=589
scope.157.endLine=589
scope.157.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.158.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI3NlbGxlcjo1ODk
scope.158.kind=field
scope.158.startLine=589
scope.158.endLine=589
scope.158.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.159.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNpbnRlcmVzdDo2MjY
scope.159.kind=field
scope.159.startLine=626
scope.159.endLine=626
scope.159.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.160.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNsYW5kOjYyNg
scope.160.kind=field
scope.160.startLine=626
scope.160.endLine=626
scope.160.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.161.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNwbGF5ZXI6NjI2
scope.161.kind=field
scope.161.startLine=626
scope.161.endLine=626
scope.161.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.162.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2ludGVyZXN0OjYyMA
scope.162.kind=field
scope.162.startLine=620
scope.162.endLine=620
scope.162.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.163.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2xhbmQ6NjIw
scope.163.kind=field
scope.163.startLine=620
scope.163.endLine=620
scope.163.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.164.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3BsYXllcjo2MjA
scope.164.kind=field
scope.164.startLine=620
scope.164.endLine=620
scope.164.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.165.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3RvdGFsOjYyMA
scope.165.kind=field
scope.165.startLine=620
scope.165.endLine=620
scope.165.semanticHash=c19b9ea10da3846e1c37942dc47f59b13649fda020125cea6df5d3a470490575
scope.166.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNsYW5kOjYxNw
scope.166.kind=field
scope.166.startLine=617
scope.166.endLine=617
scope.166.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.167.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNwbGF5ZXI6NjE3
scope.167.kind=field
scope.167.startLine=617
scope.167.endLine=617
scope.167.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.168.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCN2YWx1ZTo2MTc
scope.168.kind=field
scope.168.startLine=617
scope.168.endLine=617
scope.168.semanticHash=0d022e0e6113de33cad355012469518c3a21832e276a9c3f3a6893880214fa0a
scope.169.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206NTM3
scope.169.kind=field
scope.169.startLine=537
scope.169.endLine=537
scope.169.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.170.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb21TcGFjZTo1Mzc
scope.170.kind=field
scope.170.startLine=537
scope.170.endLine=537
scope.170.semanticHash=fdcd833bf3c0613749af9aa35feb23fbe7068c7d720cdb3a09bbbebeefbe4e7c
scope.171.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjo1Mzc
scope.171.kind=field
scope.171.startLine=537
scope.171.endLine=537
scope.171.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.172.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjUzNw
scope.172.kind=field
scope.172.startLine=537
scope.172.endLine=537
scope.172.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.173.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvU3BhY2U6NTM3
scope.173.kind=field
scope.173.startLine=537
scope.173.endLine=537
scope.173.semanticHash=061c4ba46bf16ef78d0e00d27fbe750d73f969cccf700678171eb04b70eab629
scope.174.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNvZmZlcmVkOjU1OA
scope.174.kind=field
scope.174.startLine=558
scope.174.endLine=558
scope.174.semanticHash=649b65565a280b6fb6d03fec31d684ad9ab5a25ce6bab147d7a18dd5ae60c190
scope.175.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNwYXJ0bmVyOjU1OA
scope.175.kind=field
scope.175.startLine=558
scope.175.endLine=558
scope.175.semanticHash=95af23a2c982143b2ae56ecefdadd5af27a308d33e43ffd831ee7dabec5ab90b
scope.176.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSN0cmFkZXI6NTU4
scope.176.kind=field
scope.176.startLine=558
scope.176.endLine=558
scope.176.semanticHash=1d660dfe29231866caa76a65bb832b7e5d382d4fc7d41cec6b19f988a2357cf4
scope.177.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSN3YW50ZWQ6NTU4
scope.177.kind=field
scope.177.startLine=558
scope.177.endLine=558
scope.177.semanticHash=bd6096bdbf00201b8b36b0ea0e225711c7485226561a01fef0ded8ce1c44ea48
scope.178.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjYW1vdW50OjYwOA
scope.178.kind=field
scope.178.startLine=608
scope.178.endLine=608
scope.178.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.179.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZWU6NjA4
scope.179.kind=field
scope.179.startLine=608
scope.179.endLine=608
scope.179.semanticHash=289e0e3b44081936aa54790d8a0cebe895c1f5ad30a87142f45cb4ec4a20f8d3
scope.180.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZXI6NjA4
scope.180.kind=field
scope.180.startLine=608
scope.180.endLine=608
scope.180.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.181.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjbGFuZDo2MDE
scope.181.kind=field
scope.181.startLine=601
scope.181.endLine=601
scope.181.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.182.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcGxheWVyOjYwMQ
scope.182.kind=field
scope.182.startLine=601
scope.182.endLine=601
scope.182.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.183.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcHJpY2U6NjAx
scope.183.kind=field
scope.183.startLine=601
scope.183.endLine=601
scope.183.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.184.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcmVhc29uOjYwMg
scope.184.kind=field
scope.184.startLine=602
scope.184.endLine=602
scope.184.semanticHash=9925e2b957cf3e5ae356bb085657ef3bece891d34dc0ab901046c1292ffc60fd
scope.185.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcmVzZXJ2ZTo2MDI
scope.185.kind=field
scope.185.startLine=602
scope.185.endLine=602
scope.185.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.186.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI2xhbmQ6NjA1
scope.186.kind=field
scope.186.startLine=605
scope.186.endLine=605
scope.186.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.187.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI293bmVyOjYwNQ
scope.187.kind=field
scope.187.startLine=605
scope.187.endLine=605
scope.187.semanticHash=4dc1b9a409163bdb6ca915d1f2c9f0426b20999866c7edce9557fe1f7cfb7c14
scope.188.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3JlbnQ6NjA1
scope.188.kind=field
scope.188.startLine=605
scope.188.endLine=605
scope.188.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.189.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3RlbmFudDo2MDU
scope.189.kind=field
scope.189.startLine=605
scope.189.endLine=605
scope.189.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.190.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6NTM0
scope.190.kind=field
scope.190.startLine=534
scope.190.endLine=534
scope.190.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.191.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDo1MzQ
scope.191.kind=field
scope.191.startLine=534
scope.191.endLine=534
scope.191.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.192.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6NTQ3
scope.192.kind=field
scope.192.startLine=547
scope.192.endLine=547
scope.192.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.193.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6NTQ3
scope.193.kind=field
scope.193.startLine=547
scope.193.endLine=547
scope.193.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.194.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI2Ftb3VudDo1NzQ
scope.194.kind=field
scope.194.startLine=574
scope.194.endLine=574
scope.194.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.195.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI3BheWVlOjU3NA
scope.195.kind=field
scope.195.startLine=574
scope.195.endLine=574
scope.195.semanticHash=289e0e3b44081936aa54790d8a0cebe895c1f5ad30a87142f45cb4ec4a20f8d3
scope.196.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI3BheWVyOjU3NA
scope.196.kind=field
scope.196.startLine=574
scope.196.endLine=574
scope.196.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.197.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jbG9zZXI6NTcx
scope.197.kind=field
scope.197.startLine=571
scope.197.endLine=571
scope.197.semanticHash=878e93ca653f3f39cf25b2c3775677351abe7c49bd9a13f0aa882a3a8db96732
scope.198.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jd2lubmVyOjU3MQ
scope.198.kind=field
scope.198.startLine=571
scope.198.endLine=571
scope.198.semanticHash=1f6f344bd703491733c82249fd05cc65806c907d8c6d3cc869164207c368c138
scope.199.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZVRyYWRpbmcjZW5hYmxlZDo1NjE
scope.199.kind=field
scope.199.startLine=561
scope.199.endLine=561
scope.199.semanticHash=3e72e1b05fced05e3a99e662dfe70f6c5ed519247ae3422c53c101825f46b1b8
scope.200.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6NTEz
scope.200.kind=field
scope.200.startLine=513
scope.200.endLine=513
scope.200.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.201.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjYXNzZXRSaWNoT3BlbmluZzo1NjU
scope.201.kind=field
scope.201.startLine=565
scope.201.endLine=565
scope.201.semanticHash=7efc696172cbec5ed85408de8733c6da3f2b2cc5724c20466e169a6bb814a8d2
scope.202.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjbGVnYWxFbnRpdHlFbmFibGVkOjU2NA
scope.202.kind=field
scope.202.startLine=564
scope.202.endLine=564
scope.202.semanticHash=3a439c68b10c6447b43eedcb90e029072821e3d882b40b96c05daca4711b31ec
scope.203.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjbmFtZTo1NjQ
scope.203.kind=field
scope.203.startLine=564
scope.203.endLine=564
scope.203.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.204.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjcGxheWVyOjU2NA
scope.204.kind=field
scope.204.startLine=564
scope.204.endLine=564
scope.204.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.205.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjc3RhbGVtYXRlRW5hYmxlZDo1NjU
scope.205.kind=field
scope.205.startLine=565
scope.205.endLine=565
scope.205.semanticHash=b9cf07e63923db3b13851ddc329a43bc3fdd5989f2dd5423302648247c104691
scope.206.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI2FnZTo1MjQ
scope.206.kind=field
scope.206.startLine=524
scope.206.endLine=524
scope.206.semanticHash=73cf07319348f7df4be5ec1725d9e768231a03a1f8a9a8230686f74c2a9e64d3
scope.207.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI2JhbGFuY2U6NTI0
scope.207.kind=field
scope.207.startLine=524
scope.207.endLine=524
scope.207.semanticHash=9ba2008e7dc4127b70833455dbf24f667e36899897c47cbf225bbc8bd4f5575a
scope.208.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjo1MjQ
scope.208.kind=field
scope.208.startLine=524
scope.208.endLine=524
scope.208.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.209.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3Jlc2VydmU6NTI0
scope.209.kind=field
scope.209.startLine=524
scope.209.endLine=524
scope.209.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.210.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNwbGF5ZXI6Njky
scope.210.kind=field
scope.210.startLine=692
scope.210.endLine=692
scope.210.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.211.id=ZmllbGQ6R2FtZS5SZXN1bHQjZGVlZHM6NDkw
scope.211.kind=field
scope.211.startLine=490
scope.211.endLine=490
scope.211.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.212.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDo0OTA
scope.212.kind=field
scope.212.startLine=490
scope.212.endLine=490
scope.212.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.213.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjQ5MA
scope.213.kind=field
scope.213.startLine=490
scope.213.endLine=490
scope.213.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.214.id=ZmllbGQ6R2FtZS5SZXN1bHQjd2lubmVyOjQ5MA
scope.214.kind=field
scope.214.startLine=490
scope.214.endLine=490
scope.214.semanticHash=9e05c00db702321e24ecb1c4429dea5328a65101957c7f0b7699f23ee7c539a9
scope.215.id=bWV0aG9kOkdhbWUjYWxsT3duYWJsZVNwYWNlc093bmVkKDApOjM3NQ
scope.215.kind=method
scope.215.startLine=375
scope.215.endLine=378
scope.215.semanticHash=821da435108af6599de0db2f7083a8fd6fd049024fea6375a0521284793b5c56
scope.216.id=bWV0aG9kOkdhbWUjYW55U3BsaXRFeGlzdHMoMik6MzQ1
scope.216.kind=method
scope.216.startLine=345
scope.216.endLine=347
scope.216.semanticHash=0e159d6c3a604e0cab363efb196c3aed27bbfe673c6cff777443dacea59f3e81
scope.217.id=bWV0aG9kOkdhbWUjYXBwbHlBc3NldFJpY2hPcGVuaW5nKDApOjEzMw
scope.217.kind=method
scope.217.startLine=133
scope.217.endLine=140
scope.217.semanticHash=efae9e7878f02dc2189f799a02377ad7db70ea1f02411c43dda663117a60f0f9
scope.218.id=bWV0aG9kOkdhbWUjYXBwbHlCdXlvdXQoMik6MzYw
scope.218.kind=method
scope.218.startLine=360
scope.218.endLine=366
scope.218.semanticHash=490e3f2a0e9634f39baf8c29b0caef13e593049258aefa0480cb32aa22f7b814
scope.219.id=bWV0aG9kOkdhbWUjYXBwbHlPcGVuaW5nQ2FwaXRhbCgwKToxMjY
scope.219.kind=method
scope.219.startLine=126
scope.219.endLine=131
scope.219.semanticHash=e5aafb70c5f830d43879b0f8cd4d6e6d1f730b2231cafa0e4e5725fb968895e2
scope.220.id=bWV0aG9kOkdhbWUjY2FuRm9ybUF0TWFya2V0RGVhZGxvY2soMik6NDA2
scope.220.kind=method
scope.220.startLine=406
scope.220.endLine=408
scope.220.semanticHash=7bdfe09e3027f05de8ecc064bfe3cb25b1f8ec5944ace0d47c94f70055deb2a6
scope.221.id=bWV0aG9kOkdhbWUjY29tcGxldGVSb3VuZCgzKToyMjg
scope.221.kind=method
scope.221.startLine=228
scope.221.endLine=234
scope.221.semanticHash=edf97dd0bf953aaeaab539f0028777cd32de6377d104b6cca4a9854a1b2b0d41
scope.222.id=bWV0aG9kOkdhbWUjY29tcGxldGVUcmFkZSgzKTozNjg
scope.222.kind=method
scope.222.startLine=368
scope.222.endLine=373
scope.222.semanticHash=cc6b8e4a00dd1403a07cfda0fd3f3877446d6d7c6e0ecb0b9faa32ea6dbcaba7
scope.223.id=bWV0aG9kOkdhbWUjY3RvcigxMCk6MTAx
scope.223.kind=method
scope.223.startLine=101
scope.223.endLine=117
scope.223.semanticHash=57862766cbe0101f09862b57f34a5153c99ddc1b58a450121ea17ecedf7b00c5
scope.224.id=bWV0aG9kOkdhbWUjY3RvcigyKToxNDg
scope.224.kind=method
scope.224.startLine=148
scope.224.endLine=150
scope.224.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.225.id=bWV0aG9kOkdhbWUjY3RvcigzKToxNDM
scope.225.kind=method
scope.225.startLine=143
scope.225.endLine=145
scope.225.semanticHash=6776d3f993630076b24a0ed0b3bc39a8d8cf1fb2c29184dba3c568e657a2a980
scope.226.id=bWV0aG9kOkdhbWUjY3RvcigzKToxNTM
scope.226.kind=method
scope.226.startLine=153
scope.226.endLine=155
scope.226.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.227.id=bWV0aG9kOkdhbWUjY3Rvcig0KTo2NQ
scope.227.kind=method
scope.227.startLine=65
scope.227.endLine=67
scope.227.semanticHash=d4615ba990b44348e21394831d757cef04354db1b8751fb1a298772f84bb2d76
scope.228.id=bWV0aG9kOkdhbWUjY3Rvcig1KTo2OQ
scope.228.kind=method
scope.228.startLine=69
scope.228.endLine=71
scope.228.semanticHash=8f72f5dd6632da91ac15bbd4118e10ec925d3f7f35e6559ed82d3cfe56b10db1
scope.229.id=bWV0aG9kOkdhbWUjY3Rvcig2KTo3Mw
scope.229.kind=method
scope.229.startLine=73
scope.229.endLine=78
scope.229.semanticHash=201613e9dfbe05f1b87a4d5e480877d354f121084a686ec5d292531839832ee1
scope.230.id=bWV0aG9kOkdhbWUjY3Rvcig3KTo4MA
scope.230.kind=method
scope.230.startLine=80
scope.230.endLine=85
scope.230.semanticHash=ed3b862b8b56575f057bb3efc8c37f63dfda088f90226752db32af60f9b5fbb2
scope.231.id=bWV0aG9kOkdhbWUjY3Rvcig4KTo4Nw
scope.231.kind=method
scope.231.startLine=87
scope.231.endLine=92
scope.231.semanticHash=f74e2706eebc6a1ef10bac9fce2227079ba1ec4c8654585f692c3846c4306ffd
scope.232.id=bWV0aG9kOkdhbWUjY3Rvcig5KTo5NA
scope.232.kind=method
scope.232.startLine=94
scope.232.endLine=99
scope.232.semanticHash=5ad5cadd5f8b1f50a3d3531989a80130eb1771fa011b74e876e06f9245f401b3
scope.233.id=bWV0aG9kOkdhbWUjZGV2ZWxvcEFuZFRyYWNrQ29uc29saWRhdGlvbigyKTozODA
scope.233.kind=method
scope.233.startLine=380
scope.233.endLine=384
scope.233.semanticHash=5b0b0e40b99de7c4bc22cee65d507b933613cdedb0fd8834966dc2bc7038cf93
scope.234.id=bWV0aG9kOkdhbWUjZW50aXR5TmFtZSgxKTozMTE
scope.234.kind=method
scope.234.startLine=311
scope.234.endLine=314
scope.234.semanticHash=63fedf93747ba25ad7ae7201643dc5ea04e06ae29cc5db0a4f58c0224e0bd74a
scope.235.id=bWV0aG9kOkdhbWUjZm9ybUlmRnVuZGFibGUoMSk6NDE3
scope.235.kind=method
scope.235.startLine=417
scope.235.endLine=426
scope.235.semanticHash=d9614db053d0971cf855334379998fbab91459c72dedaac6184ab3e57c1613a7
scope.236.id=bWV0aG9kOkdhbWUjZnVuZGFibGVFbnRpdHlBdE1hcmtldERlYWRsb2NrKDApOjQxMA
scope.236.kind=method
scope.236.startLine=410
scope.236.endLine=415
scope.236.semanticHash=a22a689ae1f634ce29b92baaa2d15e65d5f78f87257692f68b7b3fe8f44196ed
scope.237.id=bWV0aG9kOkdhbWUjaWRzKDEpOjQ4MQ
scope.237.kind=method
scope.237.startLine=481
scope.237.endLine=483
scope.237.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.238.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6NDM3
scope.238.kind=method
scope.238.startLine=437
scope.238.endLine=441
scope.238.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.239.id=bWV0aG9kOkdhbWUjaXNQbGF5ZXJTdGlsbFNvbHZlbnQoMSk6MzA3
scope.239.kind=method
scope.239.startLine=307
scope.239.endLine=309
scope.239.semanticHash=7f544413a575f6a989f89237d18e333f45762a295f96ba031340b613e4790a45
scope.240.id=bWV0aG9kOkdhbWUjaXNUaWVkV2l0aEl0c1BhcnRuZXIoMik6MzU0
scope.240.kind=method
scope.240.startLine=354
scope.240.endLine=358
scope.240.semanticHash=57ada1100a85c9b9156ca849da5f508164593d3caa30ca1a8fc6c96fa9b4ecfd
scope.241.id=bWV0aG9kOkdhbWUjam91cm5hbE9wZXJhdGlvbigyKToyODk
scope.241.kind=method
scope.241.startLine=289
scope.241.endLine=301
scope.241.semanticHash=f055221d9bebbbe880865721ae2ece61c306ccbb3b4c5843ef80b087ec0e43e5
scope.242.id=bWV0aG9kOkdhbWUjbGFuZGluZ3NGb3IoMyk6NDUx
scope.242.kind=method
scope.242.startLine=451
scope.242.endLine=469
scope.242.semanticHash=288a76391f035403c868df024ced5feb15dc22a1a78a4cb2b8c51752854acc39
scope.243.id=bWV0aG9kOkdhbWUjbG9nR2FtZUVuZCgzKToyNzE
scope.243.kind=method
scope.243.startLine=271
scope.243.endLine=277
scope.243.semanticHash=f257d556c26858104e522b6c2b3a4a05f8debecffd702289f2194e4c10a331b1
scope.244.id=bWV0aG9kOkdhbWUjbG9nU3RhbGVtYXRlKDIpOjI2MA
scope.244.kind=method
scope.244.startLine=260
scope.244.endLine=262
scope.244.semanticHash=93c57570f440efe0424434f7ba39a512b3ad0fe40a47746abf2b5073cca5cf50
scope.245.id=bWV0aG9kOkdhbWUjbWF5RGV2ZWxvcFRoaXNUdXJuKDIpOjMwMw
scope.245.kind=method
scope.245.startLine=303
scope.245.endLine=305
scope.245.semanticHash=e2e6796c076f1ac4e244b3eb73d9d7878546ae1fd1d0c2105f46f7b57ca31a7b
scope.246.id=bWV0aG9kOkdhbWUjb3BlcmF0ZUVudGl0eSgyKToyODQ
scope.246.kind=method
scope.246.startLine=284
scope.246.endLine=287
scope.246.semanticHash=ebf1882bb72055149f4b7ea291611d31d54863f0afa364ee089d6c5cd0a45357
scope.247.id=bWV0aG9kOkdhbWUjb3BlcmF0ZUxlZ2FsRW50aXRpZXMoMSk6Mjc5
scope.247.kind=method
scope.247.startLine=279
scope.247.endLine=282
scope.247.semanticHash=b66b36ce56fc6b38c5c4a5c8dfb203b32e51d6963afde882c96f698cc0632daa
scope.248.id=bWV0aG9kOkdhbWUjcGxheSgwKToxNTc
scope.248.kind=method
scope.248.startLine=157
scope.248.endLine=159
scope.248.semanticHash=3bcadbbb1f6b598fdb83fbc0fdd237a7656cc24edc1054185a280a4b7b46cb3b
scope.249.id=bWV0aG9kOkdhbWUjcGxheSgyKToxODM
scope.249.kind=method
scope.249.startLine=183
scope.249.endLine=204
scope.249.semanticHash=3dd298f43a346ccf7b395ed2feadd794f175c70469e73b188e97ac2784c0e4d4
scope.250.id=bWV0aG9kOkdhbWUjcGxheVRvQ29tcGxldGlvbigwKToxNjI
scope.250.kind=method
scope.250.startLine=162
scope.250.endLine=164
scope.250.semanticHash=a60fc108488c55d28cf9d6828599290071eeae99381682b526b1392f2b106627
scope.251.id=bWV0aG9kOkdhbWUjcGxheVR1cm4oNik6MjQ3
scope.251.kind=method
scope.251.startLine=247
scope.251.endLine=258
scope.251.semanticHash=fadaa432cb08e5f9cf9ab70c2fa9ea535bf17b4ccdca85b16648bdac7433fd87
scope.252.id=bWV0aG9kOkdhbWUjcGxheVR1cm5zKDcpOjIwNg
scope.252.kind=method
scope.252.startLine=206
scope.252.endLine=222
scope.252.semanticHash=9351b90faebdd74eec417dcfd5aa7ba16d96599b3d519fa3e1b0f58b571f4a36
scope.253.id=bWV0aG9kOkdhbWUjcGxheVVudGlsU3RvcHBlZCgxKToxNzE
scope.253.kind=method
scope.253.startLine=171
scope.253.endLine=173
scope.253.semanticHash=2159cc9b2267372bf24f16472c20269d3d5376d0624e178122a5a131ef094b22
scope.254.id=bWV0aG9kOkdhbWUjcGxheVVwVG9Sb3VuZHMoMSk6MTc2
scope.254.kind=method
scope.254.startLine=176
scope.254.endLine=181
scope.254.semanticHash=5561780af095da6a7387c86fefebb99d462fe27e5bfd7e0d9d8c2775fee408be
scope.255.id=bWV0aG9kOkdhbWUjcmVtYWluaW5nUGxheWVycygwKTo0Mjg
scope.255.kind=method
scope.255.startLine=428
scope.255.endLine=430
scope.255.semanticHash=a0e051c1b866b1352982334442d470d1567187f7e091423c51fc78cf3a6f2874
scope.256.id=bWV0aG9kOkdhbWUjcmVzb2x2YWJsZUJ1eW91dCgyKTozMzk
scope.256.kind=method
scope.256.startLine=339
scope.256.endLine=343
scope.256.semanticHash=60193aea7acb7bb2bd806c5a07dbaa4667dce363ef321f1925a9fba671e138da
scope.257.id=bWV0aG9kOkdhbWUjcmVzb2x2ZUJ1eW91dEF0U3RhcnQoMyk6MzI5
scope.257.kind=method
scope.257.startLine=329
scope.257.endLine=337
scope.257.semanticHash=813be9ad2de20348d1c3f4e3ab32f44e77f3c733d62da6eb038615c74509f812
scope.258.id=bWV0aG9kOkdhbWUjcmVzb2x2ZU1hcmtldERlYWRsb2NrQXRSb3VuZEJvdW5kYXJ5KDIpOjM5Mw
scope.258.kind=method
scope.258.startLine=393
scope.258.endLine=395
scope.258.semanticHash=30bb4d0b870d5ad6505155cb400c14f5789c1f54ed04985056ae7eddee4c0f08
scope.259.id=bWV0aG9kOkdhbWUjcmVzb2x2ZU1hcmtldERlYWRsb2NrQXRSb3VuZEJvdW5kYXJ5KDMpOjM5Nw
scope.259.kind=method
scope.259.startLine=397
scope.259.endLine=404
scope.259.semanticHash=f39506884a4af361259929b2c90adfb0133010019f668651bcae1ce13dcd08f9
scope.260.id=bWV0aG9kOkdhbWUjcmVzb2x2ZVNwbGl0T3duZXJzaGlwQXRTdGFydCgzKTozMTY
scope.260.kind=method
scope.260.startLine=316
scope.260.endLine=318
scope.260.semanticHash=ef2df8efc581363b012c0e7be3f055e9e5bd810881d603974bb10c6cd513850e
scope.261.id=bWV0aG9kOkdhbWUjcm91bmRMb2dnZWRBQmFua3J1cHRjeSgyKToyMzY
scope.261.kind=method
scope.261.startLine=236
scope.261.endLine=239
scope.261.semanticHash=53d6ee7e6b5f4c7c208f88782a1182b4adfcb2cbca20a468cea6d85a7da78947
scope.262.id=bWV0aG9kOkdhbWUjc2hvdWxkQ29udGludWVQbGF5aW5nKDIpOjIyNA
scope.262.kind=method
scope.262.startLine=224
scope.262.endLine=226
scope.262.semanticHash=3519a825edcf4cda2f7bd302139da772ba8e824d0b3b9288a06b22cf004a416a
scope.263.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oNCk6NDQz
scope.263.kind=method
scope.263.startLine=443
scope.263.endLine=449
scope.263.semanticHash=f390c2edc5e763c9dd207eef3bd7f6dbaa6aae4b82c011607b19c5dbddcb07d5
scope.264.id=bWV0aG9kOkdhbWUjdG90YWxEZXZlbG9wbWVudHMoMCk6Mzg2
scope.264.kind=method
scope.264.startLine=386
scope.264.endLine=390
scope.264.semanticHash=ef3c71dee10e5d323fa0e9b3f4b968e20f6a18c63e0aa75405abef16bafa1628
scope.265.id=bWV0aG9kOkdhbWUjdHJhZGVBdFN0YXJ0KDMpOjMyMA
scope.265.kind=method
scope.265.startLine=320
scope.265.endLine=327
scope.265.semanticHash=00a2ae028121c54e1b0badb12cbea5b9ce67c3e61648228279a91cde6923402b
scope.266.id=bWV0aG9kOkdhbWUjdHVybkVuZHNUaGVHYW1lKDYpOjI0MQ
scope.266.kind=method
scope.266.startLine=241
scope.266.endLine=245
scope.266.semanticHash=82fd5ad56f210182b431805ff2559b329645d70662ed16c46b9afd79152616cc
scope.267.id=bWV0aG9kOkdhbWUjd2lubmVyKDApOjQzMg
scope.267.kind=method
scope.267.startLine=432
scope.267.endLine=435
scope.267.semanticHash=702f44695db994b2e4908c5393ffd81fcd816cff000bc8cb31c6d97c66191345
scope.268.id=bWV0aG9kOkdhbWUjeWVhckxpbWl0SnVzdFJlYWNoZWQoMik6MjY0
scope.268.kind=method
scope.268.startLine=264
scope.268.endLine=269
scope.268.semanticHash=b0b634960610b577ed0eace4c0b299fc0673cc3d695f2cf18bc20d0938d71212
scope.269.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6NDc4
scope.269.kind=method
scope.269.startLine=478
scope.269.endLine=478
scope.269.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.270.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjQ5Mw
scope.270.kind=method
scope.270.startLine=1
scope.270.endLine=696
scope.270.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.271.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjUwMw
scope.271.kind=method
scope.271.startLine=503
scope.271.endLine=505
scope.271.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.272.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6NDk4
scope.272.kind=method
scope.272.startLine=498
scope.272.endLine=501
scope.272.semanticHash=f2f4e1f3c7bd7244a0e0a2e125110a27d8516e8cb7036d71c5cb73f65468d33f
scope.273.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5BdWN0aW9uV29uI2N0b3IoMyk6NTU1
scope.273.kind=method
scope.273.startLine=1
scope.273.endLine=696
scope.273.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.274.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUGFpZCNjdG9yKDIpOjY1Ng
scope.274.kind=method
scope.274.startLine=1
scope.274.endLine=696
scope.274.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.275.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUmVjZWl2ZWQjY3RvcigyKTo2NTk
scope.275.kind=method
scope.275.startLine=1
scope.275.endLine=696
scope.275.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.276.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rcnVwdCNjdG9yKDIpOjY3Nw
scope.276.kind=method
scope.276.startLine=1
scope.276.endLine=696
scope.276.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.277.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Cb3VnaHQjY3RvcigzKTo1NTE
scope.277.kind=method
scope.277.startLine=1
scope.277.endLine=696
scope.277.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.278.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CdWlsZGluZ1JlZnVzZWQjY3RvcigzKTo2NDc
scope.278.kind=method
scope.278.startLine=1
scope.278.endLine=696
scope.278.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.279.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5DaGFuY2VDYXJkRHJhd24jY3RvcigyKTo2NTA
scope.279.kind=method
scope.279.startLine=1
scope.279.endLine=696
scope.279.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.280.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Db21tdW5pdHlDaGVzdENhcmREcmF3biNjdG9yKDIpOjY1Mw
scope.280.kind=method
scope.280.startLine=1
scope.280.endLine=696
scope.280.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.281.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkT2ZmZXIjY3RvcigzKTo2NDE
scope.281.kind=method
scope.281.startLine=1
scope.281.endLine=696
scope.281.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.282.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZU5vQmlkZGVyI2N0b3IoMik6NjM4
scope.282.kind=method
scope.282.startLine=1
scope.282.endLine=696
scope.282.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.283.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZVN0YXJ0ZWQjY3RvcigyKTo2MzU
scope.283.kind=method
scope.283.startLine=1
scope.283.endLine=696
scope.283.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.284.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZVdvbiNjdG9yKDMpOjY0NA
scope.284.kind=method
scope.284.startLine=1
scope.284.endLine=696
scope.284.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.285.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5GaW5hbEFnZSNjdG9yKDIpOjY4OQ
scope.285.kind=method
scope.285.startLine=1
scope.285.endLine=696
scope.285.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.286.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5GaW5hbEJhbGFuY2UjY3RvcigyKTo2ODY
scope.286.kind=method
scope.286.startLine=1
scope.286.endLine=696
scope.286.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.287.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZUJ1aWx0I2N0b3IoMyk6NjEx
scope.287.kind=method
scope.287.startLine=1
scope.287.endLine=696
scope.287.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.288.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZVNvbGQjY3RvcigzKTo2MTQ
scope.288.kind=method
scope.288.startLine=1
scope.288.endLine=696
scope.288.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.289.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbmhlcml0ZWQjY3RvcigzKTo2MjM
scope.289.kind=method
scope.289.startLine=1
scope.289.endLine=696
scope.289.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.290.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjUxNw
scope.290.kind=method
scope.290.startLine=1
scope.290.endLine=696
scope.290.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.291.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6NTIw
scope.291.kind=method
scope.291.startLine=1
scope.291.endLine=696
scope.291.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.292.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsQ2FyZFVzZWQjY3RvcigxKTo2Njg
scope.292.kind=method
scope.292.startLine=1
scope.292.endLine=696
scope.292.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.293.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRG91Ymxlc1JvbGxlZCNjdG9yKDEpOjY3MQ
scope.293.kind=method
scope.293.startLine=1
scope.293.endLine=696
scope.293.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.294.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRW50ZXJlZCNjdG9yKDIpOjY2Mg
scope.294.kind=method
scope.294.startLine=1
scope.294.endLine=696
scope.294.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.295.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRmluZVBhaWQjY3RvcigyKTo2NjU
scope.295.kind=method
scope.295.startLine=1
scope.295.endLine=696
scope.295.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.296.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsU3RheWVkI2N0b3IoMSk6Njc0
scope.296.kind=method
scope.296.startLine=1
scope.296.endLine=696
scope.296.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.297.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU2FsZVJlZnVzZWQjY3Rvcig0KTo2MzI
scope.297.kind=method
scope.297.startLine=1
scope.297.endLine=696
scope.297.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.298.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU29sZCNjdG9yKDQpOjYyOQ
scope.298.kind=method
scope.298.startLine=1
scope.298.endLine=696
scope.298.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.299.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eURpdmlkZW5kUGFpZCNjdG9yKDMpOjU4Ng
scope.299.kind=method
scope.299.startLine=1
scope.299.endLine=696
scope.299.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.300.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUZvcm1lZCNjdG9yKDIpOjU3Nw
scope.300.kind=method
scope.300.startLine=1
scope.300.endLine=696
scope.300.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.301.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUhvdXNlQnVpbHQjY3RvcigzKTo1OTg
scope.301.kind=method
scope.301.startLine=1
scope.301.endLine=696
scope.301.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.302.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUxpcXVpZGF0ZWQjY3RvcigzKTo1OTI
scope.302.kind=method
scope.302.startLine=1
scope.302.endLine=696
scope.302.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.303.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUxvYW5SYWlzZWQjY3RvcigzKTo1ODA
scope.303.kind=method
scope.303.startLine=1
scope.303.endLine=696
scope.303.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.304.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUxvYW5SZXBhaWQjY3Rvcig0KTo1ODM
scope.304.kind=method
scope.304.startLine=1
scope.304.endLine=696
scope.304.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.305.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eVJlbnRQYWlkI2N0b3IoNCk6NTk1
scope.305.kind=method
scope.305.startLine=1
scope.305.endLine=696
scope.305.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.306.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eVNoYXJlU29sZCNjdG9yKDQpOjU4OQ
scope.306.kind=method
scope.306.startLine=1
scope.306.endLine=696
scope.306.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.307.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUtlcHQjY3RvcigzKTo2MjY
scope.307.kind=method
scope.307.startLine=1
scope.307.endLine=696
scope.307.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.308.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUxpZnRlZCNjdG9yKDQpOjYyMA
scope.308.kind=method
scope.308.startLine=1
scope.308.endLine=696
scope.308.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.309.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZWQjY3RvcigzKTo2MTc
scope.309.kind=method
scope.309.startLine=1
scope.309.endLine=696
scope.309.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.310.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjUzOA
scope.310.kind=method
scope.310.startLine=538
scope.310.endLine=540
scope.310.semanticHash=a25dcf65a363730c6f293f8a1f1404f79f6c1932a440cc31c1262695a9baa056
scope.311.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDUpOjUzNw
scope.311.kind=method
scope.311.startLine=1
scope.311.endLine=696
scope.311.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.312.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNvZmZpY2lhbFNwYWNlQXQoMSk6NTQy
scope.312.kind=method
scope.312.startLine=542
scope.312.endLine=544
scope.312.semanticHash=d857123e25d1bd7ad9e99a5f83a2cc20dc70a077e141b0d2f4b1de0cd88b32ac
scope.313.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QZWVyVHJhZGUjY3Rvcig0KTo1NTg
scope.313.kind=method
scope.313.startLine=1
scope.313.endLine=696
scope.313.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.314.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QbGF5ZXJQYWlkI2N0b3IoMyk6NjA4
scope.314.kind=method
scope.314.startLine=1
scope.314.endLine=696
scope.314.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.315.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QdXJjaGFzZURlY2xpbmVkI2N0b3IoNSk6NjAx
scope.315.kind=method
scope.315.startLine=1
scope.315.endLine=696
scope.315.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.316.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5SZW50UGFpZCNjdG9yKDQpOjYwNQ
scope.316.kind=method
scope.316.startLine=1
scope.316.endLine=696
scope.316.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.317.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKTo1MzQ
scope.317.kind=method
scope.317.startLine=1
scope.317.endLine=696
scope.317.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.318.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKTo1NDc
scope.318.kind=method
scope.318.startLine=1
scope.318.endLine=696
scope.318.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.319.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TcGxpdE1vbm9wb2x5UGFpZCNjdG9yKDMpOjU3NA
scope.319.kind=method
scope.319.startLine=1
scope.319.endLine=696
scope.319.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.320.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TcGxpdE1vbm9wb2x5V29uI2N0b3IoMik6NTcx
scope.320.kind=method
scope.320.startLine=1
scope.320.endLine=696
scope.320.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.321.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFsZW1hdGUjY3RvcigwKTo2ODA
scope.321.kind=method
scope.321.startLine=1
scope.321.endLine=696
scope.321.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.322.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFsZW1hdGVUcmFkaW5nI2N0b3IoMSk6NTYx
scope.322.kind=method
scope.322.startLine=1
scope.322.endLine=696
scope.322.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.323.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjUxMw
scope.323.kind=method
scope.323.startLine=1
scope.323.endLine=696
scope.323.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.324.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdHJhdGVneU5hbWVkI2N0b3IoNCk6NTY2
scope.324.kind=method
scope.324.startLine=566
scope.324.endLine=568
scope.324.semanticHash=73cbe69a0658916dd39c96ca2abdc2693291c41bcb145af34b2b5e8731ca92b8
scope.325.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdHJhdGVneU5hbWVkI2N0b3IoNSk6NTY0
scope.325.kind=method
scope.325.startLine=1
scope.325.endLine=696
scope.325.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.326.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDIpOjUyNQ
scope.326.kind=method
scope.326.startLine=525
scope.326.endLine=527
scope.326.semanticHash=4ee4b3a29bce9772f978446cb55e21f8821dbf401952e6475e372a345ad46138
scope.327.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDMpOjUyOQ
scope.327.kind=method
scope.327.startLine=529
scope.327.endLine=531
scope.327.semanticHash=1641f6f5ec3c77f0ec23bfd9fd1bc1ed7e1aeeb17c4bcba8f17a40b4ad21df48
scope.328.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDQpOjUyNA
scope.328.kind=method
scope.328.startLine=1
scope.328.endLine=696
scope.328.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.329.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Xb24jY3RvcigxKTo2OTI
scope.329.kind=method
scope.329.startLine=1
scope.329.endLine=696
scope.329.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.330.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5ZZWFyTGltaXRSZWFjaGVkI2N0b3IoMCk6Njgz
scope.330.kind=method
scope.330.startLine=1
scope.330.endLine=696
scope.330.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
scope.331.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoNCk6NDkw
scope.331.kind=method
scope.331.startLine=1
scope.331.endLine=696
scope.331.semanticHash=ba1947d8f69833d391184de0da29fb9f65ff51f903a10f2dcaf233ecd4642bf2
*/
