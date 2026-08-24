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
import the.monopoly.game.rules.DevelopmentLoanBook;
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.Jail;
import the.monopoly.game.rules.LandSale;
import the.monopoly.game.rules.Landings;
import the.monopoly.game.rules.LegalEntity;
import the.monopoly.game.rules.MegacorpSalaryTax;
import the.monopoly.game.rules.MonopolyBuyout;
import the.monopoly.game.rules.PeerTrading;
import the.monopoly.game.rules.Rent;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Stalemate;
import the.monopoly.game.rules.Taxes;
import the.monopoly.game.rules.WarProfitsTax;
import the.monopoly.game.rules.WarProfitsTaxBook;
import the.monopoly.game.rules.Turn;
import the.monopoly.game.strategies.Strategy;
import the.monopoly.game.strategies.Greedo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  private final boolean developmentLoans;
  private final boolean fullDrawDevelopmentLoans;
  private final DevelopmentLoanBook developmentLoanBook;
  private final boolean warProfitsTax;
  private final boolean rentRelief;
  private the.monopoly.game.rules.RentRelief rentReliefBook;
  private MegacorpSalaryTax megacorpSalaryTax;
  private final WarProfitsTaxBook warProfitsTaxBook;
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
      Cards.Decks decks, Jail jail, boolean stalemateTrading, boolean legalEntityTrading,
      boolean developmentLoans, boolean fullDrawDevelopmentLoans, int maxYears,
      DevelopmentLoanBook developmentLoanBook, boolean warProfitsTax,
      the.monopoly.game.rules.RentRelief rentReliefBook
  ) {
    this(rules, players, cups, strategies, deeds, decks, jail, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, developmentLoanBook, warProfitsTax,
        rentReliefBook != null);
    this.rentReliefBook = rentReliefBook;
    this.megacorpSalaryTax = rentReliefBook == null ? null
        : new MegacorpSalaryTax(rentReliefBook.government());
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
    this(rules, players, cups, strategies, deeds, decks, jail, stalemateTrading, legalEntityTrading,
        anyStrategyEnablesDevelopmentLoans(players, strategies), anyStrategyUsesFullLoanDraw(players, strategies), maxYears);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds,
      Cards.Decks decks, Jail jail, boolean stalemateTrading, boolean legalEntityTrading,
      boolean developmentLoans, boolean fullDrawDevelopmentLoans, int maxYears
  ) {
    this(rules, players, cups, strategies, deeds, decks, jail, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, null);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds,
      Cards.Decks decks, Jail jail, boolean stalemateTrading, boolean legalEntityTrading,
      boolean developmentLoans, boolean fullDrawDevelopmentLoans, int maxYears,
      DevelopmentLoanBook developmentLoanBook
  ) {
    this(rules, players, cups, strategies, deeds, decks, jail, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, developmentLoanBook, false);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds,
      Cards.Decks decks, Jail jail, boolean stalemateTrading, boolean legalEntityTrading,
      boolean developmentLoans, boolean fullDrawDevelopmentLoans, int maxYears,
      DevelopmentLoanBook developmentLoanBook, boolean warProfitsTax
  ) {
    this(rules, players, cups, strategies, deeds, decks, jail, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, developmentLoanBook, warProfitsTax, false);
  }

  public Game(
      Rule.Set rules, List<Player> players, Cups cups, Strategy.OfPlayers strategies, Deeds deeds,
      Cards.Decks decks, Jail jail, boolean stalemateTrading, boolean legalEntityTrading,
      boolean developmentLoans, boolean fullDrawDevelopmentLoans, int maxYears,
      DevelopmentLoanBook developmentLoanBook, boolean warProfitsTax, boolean rentRelief
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
    this.developmentLoans = developmentLoans;
    this.fullDrawDevelopmentLoans = fullDrawDevelopmentLoans;
    this.developmentLoanBook = developmentLoanBook == null ? new DevelopmentLoanBook(rules.bank()) : developmentLoanBook;
    this.warProfitsTax = warProfitsTax;
    this.rentRelief = rentRelief;
    this.rentReliefBook = rentRelief ? new the.monopoly.game.rules.RentRelief(rules.bank()) : null;
    this.megacorpSalaryTax = rentReliefBook == null ? null
        : new MegacorpSalaryTax(rentReliefBook.government());
    this.warProfitsTaxBook = new WarProfitsTaxBook(rules.bank(), WarProfitsTax.boardValue(rules));
    this.maxYears = maxYears;
    applyOpeningCapital();
    applyAssetRichOpening();
  }

  private static boolean anyStrategyEnablesDevelopmentLoans(List<Player> players, Strategy.OfPlayers strategies) {
    return players.stream().anyMatch(player -> strategies.forPlayer(player).developmentLoansEnabled());
  }

  private static boolean anyStrategyUsesFullLoanDraw(List<Player> players, Strategy.OfPlayers strategies) {
    return players.stream().anyMatch(player -> strategies.forPlayer(player).fullDrawDevelopmentLoans());
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
    return play(false, () -> true, null);
  }

  /** Plays only the selected player's next turn, retaining real rule and journal behavior. */
  public Result playTurnFor(Player.ID target) {
    Objects.requireNonNull(target, "target");
    return play(false, () -> true, target);
  }

  /** Plays successive turns until only one player remains at the table. */
  public Result playToCompletion() {
    return play(true, () -> true, null);
  }

  /**
   * Plays successive turns until the game is told to stop or only one player
   * remains at the table. Stopping is cooperative: the game finishes the round
   * it is on and then stops, however long the game would still have gone on.
   */
  public Result playUntilStopped(BooleanSupplier keepPlaying) {
    return play(true, keepPlaying, null);
  }

  /** Plays no more than the requested number of rounds, even if nobody wins. */
  public Result playUpToRounds(int rounds) {
    if (rounds == 0) return new Result(List.copyOf(players), List.of(), deeds, Optional.empty());
    if (rounds <= 0) throw new IllegalArgumentException("A game needs at least one round.");
    java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(rounds);
    return playUntilStopped(() -> remaining.getAndDecrement() > 1);
  }

  private Result play(boolean untilComplete, BooleanSupplier keepPlaying, Player.ID target) {
    var journal = new Journal();
    Map<Player.ID, Integer> ages = new HashMap<>();
    Journalling journalling = new Journalling(journal, ages, deeds, developmentLoanBook,
        rules, players, strategies, warProfitsTaxBook, warProfitsTax,
        rentReliefBook, megacorpSalaryTax);
    journal.log(new Journal.Entry.Start(ids(players)));
    deeds.legalEntities().forEach(journalling::entityFormed);
    journalling.stalemateTrading(stalemateTrading);
    journalling.developmentLoans(developmentLoans, fullDrawDevelopmentLoans);
    journal.log(new Journal.Entry.WarProfitsTaxEnabled(warProfitsTax));
    journal.log(new Journal.Entry.RentReliefEnabled(rentRelief));
    journal.log(new Journal.Entry.MegacorpSalaryTaxEnabled(megacorpSalaryTax != null));
    players.forEach(player -> journalling.strategyNamed(player, strategies.forPlayer(player)));
    List<Player> turnOrder = new Initiative(player -> initiativeRollFor(player, journal)).order(players);
    journal.log(new Journal.Entry.InitiativeWon(turnOrder.getFirst().id()));

    jail.observe(journalling);
    Building building = new Building(deeds, rules, strategies, journalling, developmentLoanBook, players);
    playTurns(turnOrder, journal, journalling, building, untilComplete, keepPlaying, target);
    // Asset-rich openings may be inspected or played with a scripted turn flow
    // that never gives the strategy its ordinary development opportunity.
    players.stream().filter(player -> strategies.forPlayer(player).assetRichOpening())
        .forEach(building::develop);

    Money governmentBalance = rentReliefBook == null ? warProfitsTaxBook.governmentBalance()
        : rentReliefBook.governmentBalance();
    if (!governmentBalance.equals(Money.ZERO)
        && journal.entries().stream().noneMatch(entry -> entry instanceof Journal.Entry.GovernmentBalance))
      journal.log(new Journal.Entry.GovernmentBalance(governmentBalance));

    return new Result(turnOrder, journal.entries(), deeds, winner());
  }

  private void playTurns(List<Player> turnOrder, Journal journal,
                         Journalling journalling, Building building, boolean untilComplete,
                         BooleanSupplier keepPlaying, Player.ID target) {
    do {
      roundHadConsolidatingAction = false;
      int roundJournalStart = journal.entries().size();
      for (Player player : turnOrder) {
        if (target != null && !player.id().equals(target)) continue;
        if (turnEndsTheGame(player, turnOrder, journal, journalling, building)) return;
      }
      if (target != null) return;
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

  private boolean turnEndsTheGame(Player player, List<Player> turnOrder, Journal journal,
                                  Journalling journalling, Building building) {
    return playTurn(player, turnOrder, journal, journalling, building)
        || yearLimitJustReached(journal, journalling);
  }

  private boolean playTurn(Player player, List<Player> turnOrder, Journal journal,
                           Journalling journalling, Building building) {
    if (deeds.isBankrupt(player)) return false;
    resolveSplitOwnershipAtStart(player, turnOrder, journalling);
    takeTurn(player, journal, journalling, landingsFor(player, turnOrder, journalling));
    if (isPlayerStillSolvent(player)) building.develop(player);
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
    Money governmentBalance = rentReliefBook == null ? warProfitsTaxBook.governmentBalance()
        : rentReliefBook.governmentBalance();
    if (!governmentBalance.equals(Money.ZERO)) journal.log(new Journal.Entry.GovernmentBalance(governmentBalance));
  }

  private boolean operateLegalEntities(Journalling journalling) {
    deeds.legalEntities().forEach(entity -> {
      boolean loanServiced = developmentLoanBook.positions().stream()
          .filter(position -> position.entity() == entity && !position.outstanding().equals(Money.ZERO))
          .map(position -> serviceEntityDevelopmentLoan(position, journalling))
          .reduce(false, (serviced, current) -> serviced || current);
      if (!loanServiced) operateEntity(entity, journalling);
    });
    return true;
  }

  private boolean serviceEntityDevelopmentLoan(DevelopmentLoanBook.Position position, Journalling journalling) {
    Optional<DevelopmentLoanBook.Payment> payment = developmentLoanBook.service(position);
    if (payment.isEmpty()) {
      mortgageEntitySpareProperty(position);
      payment = developmentLoanBook.service(position);
    }
    if (payment.isPresent()) {
      journalling.serviceDevelopmentLoan(position, payment.orElseThrow());
      return true;
    }
    DevelopmentLoanBook.Foreclosure foreclosure =
        developmentLoanBook.forecloseEntity(position, deeds, rules, players, strategies);
    journalling.developmentLoanDefaulted(position);
    journalling.developmentLoanRecovered(position, foreclosure.recovered());
    return true;
  }

  private void mortgageEntitySpareProperty(DevelopmentLoanBook.Position position) {
    LegalEntity entity = position.entity();
    rules.streets()
        .filter(Ownable.class::isInstance)
        .map(Ownable.class::cast)
        .filter(land -> deeds.entityOwnerOf(land.type()).filter(entity::equals).isPresent())
        .filter(land -> land.type() != position.collateral())
        .filter(land -> !deeds.isMortgaged(land))
        .findFirst()
        .ifPresent(land -> deeds.mortgage(land, entity));
  }

  private void operateEntity(LegalEntity entity, Journalling journalling) {
    if (!entity.hasShareholders()) return;
    journalOperation(entity, journalling);
  }

  private void journalOperation(LegalEntity entity, Journalling journalling) {
    switch (entity.operate(deeds, strategies, rules, developmentLoanBook, players)) {
      case LegalEntity.Operation.LoanRepaid it ->
          journalling.entityLoanRepaid(entity, it.shareholder(), it.principal(), it.repayment());
      case LegalEntity.Operation.HouseBuilt it -> journalling.entityHouseBuilt(entity, it.street());
      case LegalEntity.Operation.LoanRaisedAndHouseBuilt it -> {
        journalling.entityLoanRaised(entity, it.loan());
        journalling.entityHouseBuilt(entity, it.street());
      }
      case LegalEntity.Operation.DevelopmentLoanRaisedAndHouseBuilt it -> {
        journalling.entityDevelopmentLoanRaised(entity, it.position());
        journalling.entityHouseBuilt(entity, it.street());
      }
      case LegalEntity.Operation.DividendPaid it -> journalling.entityDividendPaid(entity, it.amount());
      case LegalEntity.Operation.NoAction ignored -> { }
    }
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
    Landings rent = new Rent(deeds, rules, turnOrder, strategies, journalling, journalling.rentRelief());
    Landings landSale = new LandSale(deeds, rules, turnOrder, strategies, journalling);
    Landings cards = new Cards(deeds, rules, turnOrder, strategies, decks, journalling, cups.forPlayer(player), jail);
    Landings taxes = new Taxes(journalling, journalling.rentRelief() == null
        ? null : journalling.rentRelief().government());
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

      record MegacorpSalaryTaxPaid(Player.ID player, Money amount) implements Entry {
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

      record DevelopmentLoans(boolean enabled, boolean fullDraw) implements Entry {
      }

      record DevelopmentLoanRaised(Player.ID borrower, Street.Type collateral, Money amount,
                                   Player.ID bondholder) implements Entry {
      }

      record DevelopmentLoanPayment(Player.ID borrower, Street.Type collateral,
                                    Money interest, Money principal) implements Entry {
      }

      record DevelopmentBondPayment(Player.ID bondholder, Street.Type collateral,
                                    Money yield, Money principal) implements Entry {
      }

      record DevelopmentLoanRepaid(Player.ID borrower, Street.Type collateral) implements Entry {
      }

      record DevelopmentLoanDefaulted(Player.ID borrower, Street.Type collateral) implements Entry {
      }

      record DevelopmentLoanRecovered(Street.Type collateral, Money amount) implements Entry {
      }

      record EntityDevelopmentLoanRaised(String name, Street.Type collateral, Money amount,
                                          Player.ID bondholder) implements Entry {
      }

      record EntityDevelopmentLoanPayment(String name, Street.Type collateral,
                                          Money interest, Money principal) implements Entry {
      }

      record EntityDevelopmentLoanRepaid(String name, Street.Type collateral) implements Entry {
      }

      record EntityDevelopmentLoanDefaulted(String name, Street.Type collateral) implements Entry {
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

      record RentReliefPaid(Player.ID landlord, Money amount) implements Entry {
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

      record WarProfitsTaxEnabled(boolean enabled) implements Entry {
      }

      record RentReliefEnabled(boolean enabled) implements Entry {
      }

      record MegacorpSalaryTaxEnabled(boolean enabled) implements Entry {
      }

      record WarProfitsTaxPaid(Player.ID payer, Money amount) implements Entry {
      }

      record GovernmentBalance(Money amount) implements Entry {
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
moduleHash=833e0134376b9ada4184250a686fe1078939baacf27c87d3dd64e8c7a9a734f2
scope.0.id=Y2xhc3M6R2FtZSNHYW1lOjUy
scope.0.kind=class
scope.0.startLine=52
scope.0.endLine=835
scope.0.semanticHash=e6fe14b1ece7863e6dcf80f8bcfb2d5ee8532cc099cf27680bf4881e02a89841
scope.1.id=Y2xhc3M6R2FtZS5DdXBzI0N1cHM6NTY1
scope.1.kind=class
scope.1.startLine=565
scope.1.endLine=568
scope.1.semanticHash=b9c03a817f03781e5c4b7232d0868f0df438a089e4ae43c404ad3c45d3817f41
scope.2.id=Y2xhc3M6R2FtZS5Kb3VybmFsI0pvdXJuYWw6NTgy
scope.2.kind=class
scope.2.startLine=582
scope.2.endLine=834
scope.2.semanticHash=8fe1de7ffae6a4b9efd387f47d60faa8be9fc9d66912937923a882afc16dc4b1
scope.3.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5I0VudHJ5OjYwMQ
scope.3.kind=class
scope.3.startLine=601
scope.3.endLine=833
scope.3.semanticHash=202a47082173fca85922bc8ee2bd643f1415c96294d488d8372ada9b407b5b34
scope.4.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jQXVjdGlvbldvbjo2NDQ
scope.4.kind=class
scope.4.startLine=644
scope.4.endLine=645
scope.4.semanticHash=71ee77afd5451e73d54900eb221aa9640cfe5a2b42680f570ad5efdf9e473cb9
scope.5.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI0JhbmtQYWlkOjc4Mw
scope.5.kind=class
scope.5.startLine=783
scope.5.endLine=784
scope.5.semanticHash=cce5d5e05e58aed8d75d4ca2deb97d6d8388449cd41d7b4bc422482a269ff5b4
scope.6.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNCYW5rUmVjZWl2ZWQ6Nzg2
scope.6.kind=class
scope.6.startLine=786
scope.6.endLine=787
scope.6.semanticHash=02d04a8dd004416ac824aee0a5687eb08034ac9dcbe0bae2355581bd183f3790
scope.7.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I0JhbmtydXB0OjgwNA
scope.7.kind=class
scope.7.startLine=804
scope.7.endLine=805
scope.7.semanticHash=16825b9c28c79a36f8a880d0adc21014ea4b665f40f0fb2eb70ef7ece3155e0b
scope.8.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNCb3VnaHQ6NjQw
scope.8.kind=class
scope.8.startLine=640
scope.8.endLine=641
scope.8.semanticHash=27025028a74b83acd66fd8557558fb235025cf183bcc4e3623183b26da003aec
scope.9.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNCdWlsZGluZ1JlZnVzZWQ6Nzc0
scope.9.kind=class
scope.9.startLine=774
scope.9.endLine=775
scope.9.semanticHash=c46235e296f36ff19ba4b74246e14db6236dced240c6de450bc4186fef89d59b
scope.10.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNDaGFuY2VDYXJkRHJhd246Nzc3
scope.10.kind=class
scope.10.startLine=777
scope.10.endLine=778
scope.10.semanticHash=a926e22751d95373a632fb1725c91e97b535d7cf2eb62c29d7d515570e3e3344
scope.11.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI0NvbW11bml0eUNoZXN0Q2FyZERyYXduOjc4MA
scope.11.kind=class
scope.11.startLine=780
scope.11.endLine=781
scope.11.semanticHash=3a1c04eb7d87bf7ea391ae3f28c5fecb5d887626ebce02e8f7ab581a4ce85290
scope.12.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50Qm9uZFBheW1lbnQjRGV2ZWxvcG1lbnRCb25kUGF5bWVudDo2NjQ
scope.12.kind=class
scope.12.startLine=664
scope.12.endLine=666
scope.12.semanticHash=b28b04dfaaa91134e95c557dd8f593ba7d7c16bc8b71b2a14e659cbb3483c62f
scope.13.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hbkRlZmF1bHRlZCNEZXZlbG9wbWVudExvYW5EZWZhdWx0ZWQ6Njcx
scope.13.kind=class
scope.13.startLine=671
scope.13.endLine=672
scope.13.semanticHash=5a3a7aaad7a138c24dbd26eba622dec526423769fa63c8a128237c9808979ac4
scope.14.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblBheW1lbnQjRGV2ZWxvcG1lbnRMb2FuUGF5bWVudDo2NjA
scope.14.kind=class
scope.14.startLine=660
scope.14.endLine=662
scope.14.semanticHash=a2cc304a2727d74b47aa10f08903f5f70fbe915e49ae9815fa2e2f62dc803b11
scope.15.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJhaXNlZCNEZXZlbG9wbWVudExvYW5SYWlzZWQ6NjU2
scope.15.kind=class
scope.15.startLine=656
scope.15.endLine=658
scope.15.semanticHash=b3669df1dec8ba2c6e30643eb2f22086ca89b808b20cca716e3bccb0aacd43cd
scope.16.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJlY292ZXJlZCNEZXZlbG9wbWVudExvYW5SZWNvdmVyZWQ6Njc0
scope.16.kind=class
scope.16.startLine=674
scope.16.endLine=675
scope.16.semanticHash=1e7bbc40a265f0d186c046bcd902a9c52cd5004a398e90b6a65e221190d7aeee
scope.17.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJlcGFpZCNEZXZlbG9wbWVudExvYW5SZXBhaWQ6NjY4
scope.17.kind=class
scope.17.startLine=668
scope.17.endLine=669
scope.17.semanticHash=49b2887a41145453be97590242ee3f462c293c1d960af90ad2a80d03e1fd6691
scope.18.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hbnMjRGV2ZWxvcG1lbnRMb2Fuczo2NTM
scope.18.kind=class
scope.18.startLine=653
scope.18.endLine=654
scope.18.semanticHash=ffdfc5881b7cf7754414906cee1206387676465c6e7194b70a43f3148f0a6dfd
scope.19.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNEaXN0cmVzc2VkT2ZmZXI6NzY4
scope.19.kind=class
scope.19.startLine=768
scope.19.endLine=769
scope.19.semanticHash=a6b26851b984f848f04bdd88b35a8e6173605e1d87739ac1f81895b2f786a8cf
scope.20.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjRGlzdHJlc3NlZFNhbGVOb0JpZGRlcjo3NjU
scope.20.kind=class
scope.20.startLine=765
scope.20.endLine=766
scope.20.semanticHash=75e912e170f8d8fa05b68bb0cf8b559956b819929182eb968e2b53d51012c9b7
scope.21.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNEaXN0cmVzc2VkU2FsZVN0YXJ0ZWQ6NzYy
scope.21.kind=class
scope.21.startLine=762
scope.21.endLine=763
scope.21.semanticHash=5b2163fb1a971085705c59755fe2387b0bdd1a91016841925222a61184d97e11
scope.22.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI0Rpc3RyZXNzZWRTYWxlV29uOjc3MQ
scope.22.kind=class
scope.22.startLine=771
scope.22.endLine=772
scope.22.semanticHash=7665fa2235db4f1f740916093d7b1cb0f3a1bcdd186c0b53f9ff6e2d5652f1f6
scope.23.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hbkRlZmF1bHRlZCNFbnRpdHlEZXZlbG9wbWVudExvYW5EZWZhdWx0ZWQ6Njg4
scope.23.kind=class
scope.23.startLine=688
scope.23.endLine=689
scope.23.semanticHash=d5af7d0d14c2374598119e1c9c8bb6009558f706ee6a480bab9083f1271e6663
scope.24.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblBheW1lbnQjRW50aXR5RGV2ZWxvcG1lbnRMb2FuUGF5bWVudDo2ODE
scope.24.kind=class
scope.24.startLine=681
scope.24.endLine=683
scope.24.semanticHash=6b05047caa24201825ef92c19c55056693f9d60560b4a87e71ee5eaa57d47662
scope.25.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblJhaXNlZCNFbnRpdHlEZXZlbG9wbWVudExvYW5SYWlzZWQ6Njc3
scope.25.kind=class
scope.25.startLine=677
scope.25.endLine=679
scope.25.semanticHash=8e30636ba39ed424b0e37d50fd09b41bbddd6e6f9dccfe974e1bd468be9b57af
scope.26.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblJlcGFpZCNFbnRpdHlEZXZlbG9wbWVudExvYW5SZXBhaWQ6Njg1
scope.26.kind=class
scope.26.startLine=685
scope.26.endLine=686
scope.26.semanticHash=c131915ae27c29c40f7828c5d8fd1972d1ab17ca8321a6bf4994c5a18d87053b
scope.27.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI0ZpbmFsQWdlOjgyOA
scope.27.kind=class
scope.27.startLine=828
scope.27.endLine=829
scope.27.semanticHash=174faa5146bf4e6b710a1dc3a9e2a96bc71d0c264c37895b044997623e4c691d
scope.28.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNGaW5hbEJhbGFuY2U6ODI1
scope.28.kind=class
scope.28.startLine=825
scope.28.endLine=826
scope.28.semanticHash=f991eb829ddb2423403d242bcdbdd98ba3199698ebd5c3ebd2dcb0d5cfe0a627
scope.29.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkdvdmVybm1lbnRCYWxhbmNlI0dvdmVybm1lbnRCYWxhbmNlOjgyMg
scope.29.kind=class
scope.29.startLine=822
scope.29.endLine=823
scope.29.semanticHash=2f6ef7069829cc2c8ab982547cd0037a1b4e56633b79d00979933c0b8d74af8c
scope.30.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjSG91c2VCdWlsdDo3Mzg
scope.30.kind=class
scope.30.startLine=738
scope.30.endLine=739
scope.30.semanticHash=27575972b2787c07a6fa98a725c5bed4487591e647678d5880869a7c637aee97
scope.31.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNIb3VzZVNvbGQ6NzQx
scope.31.kind=class
scope.31.startLine=741
scope.31.endLine=742
scope.31.semanticHash=7bfd22802262e4e36e5ffa57b44a79dc928c71eaa883b7f110cd1b9e7f7230b2
scope.32.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNJbmhlcml0ZWQ6NzUw
scope.32.kind=class
scope.32.startLine=750
scope.32.endLine=751
scope.32.semanticHash=4e87cf40a11022ccf4933f9a448697b3a8224c48633ac93bd888d686f9632d19
scope.33.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI0luaXRpYXRpdmVSb2xsOjYwNg
scope.33.kind=class
scope.33.startLine=606
scope.33.endLine=607
scope.33.semanticHash=9ddc6422e30605908b3cda5ed1303d72e1d88481156a9fda83e4e47d7f6f8565
scope.34.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jSW5pdGlhdGl2ZVdvbjo2MDk
scope.34.kind=class
scope.34.startLine=609
scope.34.endLine=610
scope.34.semanticHash=3157a07e9b9b634b42b6299b6402e6896ddfcc1fb9010371d58879f3a6c6e7ff
scope.35.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxDYXJkVXNlZCNKYWlsQ2FyZFVzZWQ6Nzk1
scope.35.kind=class
scope.35.startLine=795
scope.35.endLine=796
scope.35.semanticHash=78d932232a0f5e673d3dc6c6d78e5ba0e266df879e171af6995e7c6686e39ff5
scope.36.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxEb3VibGVzUm9sbGVkI0phaWxEb3VibGVzUm9sbGVkOjc5OA
scope.36.kind=class
scope.36.startLine=798
scope.36.endLine=799
scope.36.semanticHash=7103e2c440de0b5645f3f7249799dd79a41fc35d18ec6f0287ae995d1d07be51
scope.37.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI0phaWxFbnRlcmVkOjc4OQ
scope.37.kind=class
scope.37.startLine=789
scope.37.endLine=790
scope.37.semanticHash=72be50c2af861ce8f500dde65a8bfa767ceb307161772739134a6968fd43b907
scope.38.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNKYWlsRmluZVBhaWQ6Nzky
scope.38.kind=class
scope.38.startLine=792
scope.38.endLine=793
scope.38.semanticHash=88ed119890c309e1480e4400ae91bdb79250c1dd70940253050a847641cb4283
scope.39.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxTdGF5ZWQjSmFpbFN0YXllZDo4MDE
scope.39.kind=class
scope.39.startLine=801
scope.39.endLine=802
scope.39.semanticHash=15c417a86539b6369b8adabdfdc67525574d0262be87768fc04e199c4b2daa60
scope.40.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNMYW5kU2FsZVJlZnVzZWQ6NzU5
scope.40.kind=class
scope.40.startLine=759
scope.40.endLine=760
scope.40.semanticHash=071a81b29122c94c28ba6cee1630b23eeb7b6acc5cf69c07a622eb04893d79ef
scope.41.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI0xhbmRTb2xkOjc1Ng
scope.41.kind=class
scope.41.startLine=756
scope.41.endLine=757
scope.41.semanticHash=fd9ac419a61f440251c0473ce96aa04eb9d1e3761c450d6e7d55b0f1d92a105e
scope.42.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI0xlZ2FsRW50aXR5RGl2aWRlbmRQYWlkOjcxMw
scope.42.kind=class
scope.42.startLine=713
scope.42.endLine=714
scope.42.semanticHash=a6b973b482e59b7949c8ceb0e26d5ba94a15e1a58524a46c28931c27273d0ba3
scope.43.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI0xlZ2FsRW50aXR5Rm9ybWVkOjcwNA
scope.43.kind=class
scope.43.startLine=704
scope.43.endLine=705
scope.43.semanticHash=631fdf7745dde5d4380f5cdef077abbf6488d087a9fdfc52335a91183bbda172
scope.44.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5SG91c2VCdWlsdCNMZWdhbEVudGl0eUhvdXNlQnVpbHQ6NzI1
scope.44.kind=class
scope.44.startLine=725
scope.44.endLine=726
scope.44.semanticHash=9744de81e9f930bb9fb4e78771dc03aaa7ce97ca0d24a6af6d47e7476d2ebf81
scope.45.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TGlxdWlkYXRlZCNMZWdhbEVudGl0eUxpcXVpZGF0ZWQ6NzE5
scope.45.kind=class
scope.45.startLine=719
scope.45.endLine=720
scope.45.semanticHash=aa6213161a5232da2eb3836e961532f7ba19584f86f7da6b956a668c0ffe23ac
scope.46.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNMZWdhbEVudGl0eUxvYW5SYWlzZWQ6NzA3
scope.46.kind=class
scope.46.startLine=707
scope.46.endLine=708
scope.46.semanticHash=da73a08e8b2cd04078088cbd60125d7682d83240f4184b8a74a41930af31edf1
scope.47.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNMZWdhbEVudGl0eUxvYW5SZXBhaWQ6NzEw
scope.47.kind=class
scope.47.startLine=710
scope.47.endLine=711
scope.47.semanticHash=136375fc94d9d917f2bed703b3d18023767213fe5b45a803857ec6e27243300d
scope.48.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjTGVnYWxFbnRpdHlSZW50UGFpZDo3MjI
scope.48.kind=class
scope.48.startLine=722
scope.48.endLine=723
scope.48.semanticHash=806d2d19304f51898673df0adbd7aaddb857527a93dffd2a5788f9137a315e04
scope.49.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI0xlZ2FsRW50aXR5U2hhcmVTb2xkOjcxNg
scope.49.kind=class
scope.49.startLine=716
scope.49.endLine=717
scope.49.semanticHash=b94d627e181f8cae0481a74bb579c3ff62ff127d9a0199587e685a200da18864
scope.50.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNNb3J0Z2FnZUtlcHQ6NzUz
scope.50.kind=class
scope.50.startLine=753
scope.50.endLine=754
scope.50.semanticHash=bf247aef5b7c272b93350d039dbbc80307604012fab76265fc78befb32c6355d
scope.51.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI01vcnRnYWdlTGlmdGVkOjc0Nw
scope.51.kind=class
scope.51.startLine=747
scope.51.endLine=748
scope.51.semanticHash=876fc18a90cbc579ad9618bc95f03fa33b7cdeae3c7f18f0ead53f7795f64237
scope.52.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNNb3J0Z2FnZWQ6NzQ0
scope.52.kind=class
scope.52.startLine=744
scope.52.endLine=745
scope.52.semanticHash=2536842d77794ad82293557093f6b95391662318c0cf866281f0bfeb54b47212
scope.53.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI01vdmVkOjYyNg
scope.53.kind=class
scope.53.startLine=626
scope.53.endLine=634
scope.53.semanticHash=ed37919856542e0d29f91d0622487a42cbe6023a70d3c23b3950fc66a5e8f1ab
scope.54.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNQZWVyVHJhZGU6NjQ3
scope.54.kind=class
scope.54.startLine=647
scope.54.endLine=648
scope.54.semanticHash=0b4feb9a22fe8ab12f1803c2626a29366e17eca02670c18aa1efec0c2512fb6d
scope.55.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjUGxheWVyUGFpZDo3MzU
scope.55.kind=class
scope.55.startLine=735
scope.55.endLine=736
scope.55.semanticHash=ecda18178391ece7e75c3e72ec3f854adff15a3950fc135b48bdf7e6cb119a23
scope.56.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjUHVyY2hhc2VEZWNsaW5lZDo3Mjg
scope.56.kind=class
scope.56.startLine=728
scope.56.endLine=730
scope.56.semanticHash=72af27050d45a9fbfac729c104126892ecd90a709dfdce45deca6935b40546a4
scope.57.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI1JlbnRQYWlkOjczMg
scope.57.kind=class
scope.57.startLine=732
scope.57.endLine=733
scope.57.semanticHash=47c40c5b19c0d8df73eda2478d761c7a7e8604713b0719926b1f46a2f2b6f104
scope.58.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRSZWxpZWZFbmFibGVkI1JlbnRSZWxpZWZFbmFibGVkOjgxNg
scope.58.kind=class
scope.58.startLine=816
scope.58.endLine=817
scope.58.semanticHash=e8c4b8c967f02263f819b8d946802cb29c496651f0a1d81b77934e2d5ae78466
scope.59.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNSb2xsZWQ6NjIz
scope.59.kind=class
scope.59.startLine=623
scope.59.endLine=624
scope.59.semanticHash=7d8a48153303722d15694dd8b3a8043d216b2c1afae4a042406e024b35006af7
scope.60.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNTYWxhcnlDb2xsZWN0ZWQ6NjM2
scope.60.kind=class
scope.60.startLine=636
scope.60.endLine=637
scope.60.semanticHash=1095e4c2841088589df224088bd7c7f8f8939ea5833b3b47fb596e1cf0d59e71
scope.61.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI1NwbGl0TW9ub3BvbHlQYWlkOjcwMQ
scope.61.kind=class
scope.61.startLine=701
scope.61.endLine=702
scope.61.semanticHash=088ba655dca222db0149859641809b25dcc4469fc60702c2129eec19d7cf93ee
scope.62.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jU3BsaXRNb25vcG9seVdvbjo2OTg
scope.62.kind=class
scope.62.startLine=698
scope.62.endLine=699
scope.62.semanticHash=7eab67f65f325bc29dc9eed6c1b7f342f135afe2dadc8b640001776424af9ad8
scope.63.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZSNTdGFsZW1hdGU6ODA3
scope.63.kind=class
scope.63.startLine=807
scope.63.endLine=808
scope.63.semanticHash=d706ac5ec3788f780b9dced589058470dac53a78cd99870750604517e057e2b4
scope.64.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZVRyYWRpbmcjU3RhbGVtYXRlVHJhZGluZzo2NTA
scope.64.kind=class
scope.64.startLine=650
scope.64.endLine=651
scope.64.semanticHash=b5eb812ee87ce82f7b000eb8f037883ca2ca2e04a1679052bb93a2d2020c4eca
scope.65.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I1N0YXJ0OjYwMg
scope.65.kind=class
scope.65.startLine=602
scope.65.endLine=603
scope.65.semanticHash=22caafc08a8cb27e7f665863616373e4c4b554313a681196fbf4ab16c3483c2c
scope.66.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjU3RyYXRlZ3lOYW1lZDo2OTE
scope.66.kind=class
scope.66.startLine=691
scope.66.endLine=696
scope.66.semanticHash=973caa0971b4f2e4b8bc8a44fa50149d837f3dc255349b7065c839c2623f027e
scope.67.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI1R1cm5TdGFydGVkOjYxMw
scope.67.kind=class
scope.67.startLine=613
scope.67.endLine=621
scope.67.semanticHash=611be8f7912e6193ac83d3badf59a472e4cb21571d4be0e07854eaec325c9099
scope.68.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LldhclByb2ZpdHNUYXhFbmFibGVkI1dhclByb2ZpdHNUYXhFbmFibGVkOjgxMw
scope.68.kind=class
scope.68.startLine=813
scope.68.endLine=814
scope.68.semanticHash=39d3dc727743c636a24cfea35e9e9fb7207f214331e043283a720f8a52fb7f9f
scope.69.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LldhclByb2ZpdHNUYXhQYWlkI1dhclByb2ZpdHNUYXhQYWlkOjgxOQ
scope.69.kind=class
scope.69.startLine=819
scope.69.endLine=820
scope.69.semanticHash=f89fc28ad161cbb232935013fc7dc472ff3cb6e8d3c35d667c72e663c9c76a1f
scope.70.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNXb246ODMx
scope.70.kind=class
scope.70.startLine=831
scope.70.endLine=832
scope.70.semanticHash=1018a3f41b5571c335e5fbf1476a6a3112c2284616837f2e0c7fbd00dd3d8b76
scope.71.id=Y2xhc3M6R2FtZS5Kb3VybmFsLkVudHJ5LlllYXJMaW1pdFJlYWNoZWQjWWVhckxpbWl0UmVhY2hlZDo4MTA
scope.71.kind=class
scope.71.startLine=810
scope.71.endLine=811
scope.71.semanticHash=e3605769311df1d0de936dc2ecfde31f3e09144c63e0739ae4525e663fb01002
scope.72.id=Y2xhc3M6R2FtZS5SZXN1bHQjUmVzdWx0OjU3OQ
scope.72.kind=class
scope.72.startLine=579
scope.72.endLine=580
scope.72.semanticHash=024a5de82b58c6e09d33d689b003f51dcd43a63a5e94cb88b5d8b96d1706df96
scope.73.id=ZmllbGQ6R2FtZSNBU1NFVF9SSUNIX0NPTE9VUlM6NTM
scope.73.kind=field
scope.73.startLine=53
scope.73.endLine=53
scope.73.semanticHash=dc2e75c3d89687b23d7590e013c91f733cf3b46100d977d5b44d9b0b3ac9df7f
scope.74.id=ZmllbGQ6R2FtZSNhdXRvbWF0aWNNYXJrZXREZWFkbG9jazo3MQ
scope.74.kind=field
scope.74.startLine=71
scope.74.endLine=71
scope.74.semanticHash=17af0925d6fc4bdd873e9243b773529589dc75a853b704ef553e424e86c8ad6c
scope.75.id=ZmllbGQ6R2FtZSNjdXBzOjU3
scope.75.kind=field
scope.75.startLine=57
scope.75.endLine=57
scope.75.semanticHash=a6fb87f8ab447c8e819bded88a8ed401bbccc80a4c70dad7c99e801de943bb94
scope.76.id=ZmllbGQ6R2FtZSNkZWNrczo2MA
scope.76.kind=field
scope.76.startLine=60
scope.76.endLine=60
scope.76.semanticHash=130541f31392b2fc32d3c8343ebd76de366010b8930395e22ba244946f508252
scope.77.id=ZmllbGQ6R2FtZSNkZWVkczo1OQ
scope.77.kind=field
scope.77.startLine=59
scope.77.endLine=59
scope.77.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.78.id=ZmllbGQ6R2FtZSNkZXZlbG9wbWVudExvYW5Cb29rOjY2
scope.78.kind=field
scope.78.startLine=66
scope.78.endLine=66
scope.78.semanticHash=7287418b832cd23339025a8ae895666658b2fdeb9517c07bc40eb6cfdf1b56d7
scope.79.id=ZmllbGQ6R2FtZSNkZXZlbG9wbWVudExvYW5zOjY0
scope.79.kind=field
scope.79.startLine=64
scope.79.endLine=64
scope.79.semanticHash=f23a827302b2b75b6897212a1ad1cd1d932c3cbc2a81690aad4beabb26d6e401
scope.80.id=ZmllbGQ6R2FtZSNmdWxsRHJhd0RldmVsb3BtZW50TG9hbnM6NjU
scope.80.kind=field
scope.80.startLine=65
scope.80.endLine=65
scope.80.semanticHash=1520133bafad11a2ed33129725160fe8ff1e0f0fbbf9e5d3d953e2e9acf8b627
scope.81.id=ZmllbGQ6R2FtZSNqYWlsOjYx
scope.81.kind=field
scope.81.startLine=61
scope.81.endLine=61
scope.81.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.82.id=ZmllbGQ6R2FtZSNsZWdhbEVudGl0eVRyYWRpbmc6NjM
scope.82.kind=field
scope.82.startLine=63
scope.82.endLine=63
scope.82.semanticHash=79e35a24ea51a961b285f1431176277ba37f40aebb7dc85ea35c6c3e9ef9567e
scope.83.id=ZmllbGQ6R2FtZSNtYXhZZWFyczo3MA
scope.83.kind=field
scope.83.startLine=70
scope.83.endLine=70
scope.83.semanticHash=32059abe263315354f8e91ea653d395a023f3272c81d2506899568171495cdba
scope.84.id=ZmllbGQ6R2FtZSNwbGF5ZXJzOjU2
scope.84.kind=field
scope.84.startLine=56
scope.84.endLine=56
scope.84.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.85.id=ZmllbGQ6R2FtZSNyZW50UmVsaWVmOjY4
scope.85.kind=field
scope.85.startLine=68
scope.85.endLine=68
scope.85.semanticHash=74c9cb4ddfbd9e72423920d858f1aa0c8f5e9b5f500fffb903fb313c1ca7c195
scope.86.id=ZmllbGQ6R2FtZSNyb3VuZEhhZENvbnNvbGlkYXRpbmdBY3Rpb246NzI
scope.86.kind=field
scope.86.startLine=72
scope.86.endLine=72
scope.86.semanticHash=4b742b5592bea6f2abde227ca75f29f5f05380aeb9c069a22db7fd39a93d18cc
scope.87.id=ZmllbGQ6R2FtZSNydWxlczo1NQ
scope.87.kind=field
scope.87.startLine=55
scope.87.endLine=55
scope.87.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.88.id=ZmllbGQ6R2FtZSNzdGFsZW1hdGVUcmFkaW5nOjYy
scope.88.kind=field
scope.88.startLine=62
scope.88.endLine=62
scope.88.semanticHash=3fb0db6ec778e457ec4b9262d01f922604291d8cbeefa5df7e177c0d5beea6b1
scope.89.id=ZmllbGQ6R2FtZSNzdHJhdGVnaWVzOjU4
scope.89.kind=field
scope.89.startLine=58
scope.89.endLine=58
scope.89.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.90.id=ZmllbGQ6R2FtZSN3YXJQcm9maXRzVGF4OjY3
scope.90.kind=field
scope.90.startLine=67
scope.90.endLine=67
scope.90.semanticHash=75388e95810d1de97cc51398740c3aa1829b6165f0623ba4e9ee020f5cb72f6a
scope.91.id=ZmllbGQ6R2FtZSN3YXJQcm9maXRzVGF4Qm9vazo2OQ
scope.91.kind=field
scope.91.startLine=69
scope.91.endLine=69
scope.91.semanticHash=bf5479ebdcf848889718c84c3417b5ba418df9a23174f23641b8de9ecdaf3cf4
scope.92.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2VudHJpZXM6NTg1
scope.92.kind=field
scope.92.startLine=585
scope.92.endLine=585
scope.92.semanticHash=a2bd826f59cdea2acb80e00ce2977b53d68d774b0ac67ebd95eea8a52b56640d
scope.93.id=ZmllbGQ6R2FtZS5Kb3VybmFsI2xvZ2dlcjo1ODM
scope.93.kind=field
scope.93.startLine=583
scope.93.endLine=583
scope.93.semanticHash=a0bab15e355b1d279f740b159c50300217f3b455d4532ccbb4f3c5b19c16d582
scope.94.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jbGFuZDo2NDQ
scope.94.kind=field
scope.94.startLine=644
scope.94.endLine=644
scope.94.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.95.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcGxheWVyOjY0NA
scope.95.kind=field
scope.95.startLine=644
scope.95.endLine=644
scope.95.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.96.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkF1Y3Rpb25Xb24jcHJpY2U6NjQ0
scope.96.kind=field
scope.96.startLine=644
scope.96.endLine=644
scope.96.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.97.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI2Ftb3VudDo3ODM
scope.97.kind=field
scope.97.startLine=783
scope.97.endLine=783
scope.97.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.98.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtQYWlkI3BsYXllcjo3ODM
scope.98.kind=field
scope.98.startLine=783
scope.98.endLine=783
scope.98.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.99.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNhbW91bnQ6Nzg2
scope.99.kind=field
scope.99.startLine=786
scope.99.endLine=786
scope.99.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.100.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtSZWNlaXZlZCNwbGF5ZXI6Nzg2
scope.100.kind=field
scope.100.startLine=786
scope.100.endLine=786
scope.100.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.101.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I2NyZWRpdG9yOjgwNA
scope.101.kind=field
scope.101.startLine=804
scope.101.endLine=804
scope.101.semanticHash=04806e2a3ca47061887c26b1a6e5df08f09b4b4e10f22dac41fe60a342b7338b
scope.102.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJhbmtydXB0I3BsYXllcjo4MDQ
scope.102.kind=field
scope.102.startLine=804
scope.102.endLine=804
scope.102.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.103.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNsYW5kOjY0MA
scope.103.kind=field
scope.103.startLine=640
scope.103.endLine=640
scope.103.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.104.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwbGF5ZXI6NjQw
scope.104.kind=field
scope.104.startLine=640
scope.104.endLine=640
scope.104.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.105.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJvdWdodCNwcmljZTo2NDA
scope.105.kind=field
scope.105.startLine=640
scope.105.endLine=640
scope.105.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.106.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNsYW5kOjc3NA
scope.106.kind=field
scope.106.startLine=774
scope.106.endLine=774
scope.106.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.107.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwbGF5ZXI6Nzc0
scope.107.kind=field
scope.107.startLine=774
scope.107.endLine=774
scope.107.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.108.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkJ1aWxkaW5nUmVmdXNlZCNwcmljZTo3NzQ
scope.108.kind=field
scope.108.startLine=774
scope.108.endLine=774
scope.108.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.109.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNjYXJkOjc3Nw
scope.109.kind=field
scope.109.startLine=777
scope.109.endLine=777
scope.109.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.110.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNoYW5jZUNhcmREcmF3biNwbGF5ZXI6Nzc3
scope.110.kind=field
scope.110.startLine=777
scope.110.endLine=777
scope.110.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.111.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI2NhcmQ6Nzgw
scope.111.kind=field
scope.111.startLine=780
scope.111.endLine=780
scope.111.semanticHash=f3cc1541b007319516a6bee9e9d38d5d6b1ff8e95fed2aced1a356a006227b2c
scope.112.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkNvbW11bml0eUNoZXN0Q2FyZERyYXduI3BsYXllcjo3ODA
scope.112.kind=field
scope.112.startLine=780
scope.112.endLine=780
scope.112.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.113.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50Qm9uZFBheW1lbnQjYm9uZGhvbGRlcjo2NjQ
scope.113.kind=field
scope.113.startLine=664
scope.113.endLine=664
scope.113.semanticHash=9ec3775488084be096f9d7c37eaec6b46e99d259cb0c2ca485d8766ba1034199
scope.114.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50Qm9uZFBheW1lbnQjY29sbGF0ZXJhbDo2NjQ
scope.114.kind=field
scope.114.startLine=664
scope.114.endLine=664
scope.114.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.115.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50Qm9uZFBheW1lbnQjcHJpbmNpcGFsOjY2NQ
scope.115.kind=field
scope.115.startLine=665
scope.115.endLine=665
scope.115.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.116.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50Qm9uZFBheW1lbnQjeWllbGQ6NjY1
scope.116.kind=field
scope.116.startLine=665
scope.116.endLine=665
scope.116.semanticHash=5c30fe3d056c213c4eb189b791b850d86258517d62839043a257aa2ce0354dcf
scope.117.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hbkRlZmF1bHRlZCNib3Jyb3dlcjo2NzE
scope.117.kind=field
scope.117.startLine=671
scope.117.endLine=671
scope.117.semanticHash=b30d1fec0938779231123cf8eba36cc0d598dc33a3470bf8388f6d53d8410523
scope.118.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hbkRlZmF1bHRlZCNjb2xsYXRlcmFsOjY3MQ
scope.118.kind=field
scope.118.startLine=671
scope.118.endLine=671
scope.118.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.119.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblBheW1lbnQjYm9ycm93ZXI6NjYw
scope.119.kind=field
scope.119.startLine=660
scope.119.endLine=660
scope.119.semanticHash=b30d1fec0938779231123cf8eba36cc0d598dc33a3470bf8388f6d53d8410523
scope.120.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblBheW1lbnQjY29sbGF0ZXJhbDo2NjA
scope.120.kind=field
scope.120.startLine=660
scope.120.endLine=660
scope.120.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.121.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblBheW1lbnQjaW50ZXJlc3Q6NjYx
scope.121.kind=field
scope.121.startLine=661
scope.121.endLine=661
scope.121.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.122.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblBheW1lbnQjcHJpbmNpcGFsOjY2MQ
scope.122.kind=field
scope.122.startLine=661
scope.122.endLine=661
scope.122.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.123.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJhaXNlZCNhbW91bnQ6NjU2
scope.123.kind=field
scope.123.startLine=656
scope.123.endLine=656
scope.123.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.124.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJhaXNlZCNib25kaG9sZGVyOjY1Nw
scope.124.kind=field
scope.124.startLine=657
scope.124.endLine=657
scope.124.semanticHash=9ec3775488084be096f9d7c37eaec6b46e99d259cb0c2ca485d8766ba1034199
scope.125.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJhaXNlZCNib3Jyb3dlcjo2NTY
scope.125.kind=field
scope.125.startLine=656
scope.125.endLine=656
scope.125.semanticHash=b30d1fec0938779231123cf8eba36cc0d598dc33a3470bf8388f6d53d8410523
scope.126.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJhaXNlZCNjb2xsYXRlcmFsOjY1Ng
scope.126.kind=field
scope.126.startLine=656
scope.126.endLine=656
scope.126.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.127.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJlY292ZXJlZCNhbW91bnQ6Njc0
scope.127.kind=field
scope.127.startLine=674
scope.127.endLine=674
scope.127.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.128.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJlY292ZXJlZCNjb2xsYXRlcmFsOjY3NA
scope.128.kind=field
scope.128.startLine=674
scope.128.endLine=674
scope.128.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.129.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJlcGFpZCNib3Jyb3dlcjo2Njg
scope.129.kind=field
scope.129.startLine=668
scope.129.endLine=668
scope.129.semanticHash=b30d1fec0938779231123cf8eba36cc0d598dc33a3470bf8388f6d53d8410523
scope.130.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hblJlcGFpZCNjb2xsYXRlcmFsOjY2OA
scope.130.kind=field
scope.130.startLine=668
scope.130.endLine=668
scope.130.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.131.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hbnMjZW5hYmxlZDo2NTM
scope.131.kind=field
scope.131.startLine=653
scope.131.endLine=653
scope.131.semanticHash=3e72e1b05fced05e3a99e662dfe70f6c5ed519247ae3422c53c101825f46b1b8
scope.132.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRldmVsb3BtZW50TG9hbnMjZnVsbERyYXc6NjUz
scope.132.kind=field
scope.132.startLine=653
scope.132.endLine=653
scope.132.semanticHash=607c5db592eca0b36e633cac46eb24d0cb93b60990e715e9cfee5a3c49dd47c6
scope.133.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNiaWRkZXI6NzY4
scope.133.kind=field
scope.133.startLine=768
scope.133.endLine=768
scope.133.semanticHash=b8ec71792a9a472362073b97719425ae8b5e956ba271d8efedb75371e8312526
scope.134.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNsYW5kOjc2OA
scope.134.kind=field
scope.134.startLine=768
scope.134.endLine=768
scope.134.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.135.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRPZmZlciNwcmljZTo3Njg
scope.135.kind=field
scope.135.startLine=768
scope.135.endLine=768
scope.135.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.136.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjbGFuZDo3NjU
scope.136.kind=field
scope.136.startLine=765
scope.136.endLine=765
scope.136.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.137.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlTm9CaWRkZXIjc2VsbGVyOjc2NQ
scope.137.kind=field
scope.137.startLine=765
scope.137.endLine=765
scope.137.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.138.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNsYW5kOjc2Mg
scope.138.kind=field
scope.138.startLine=762
scope.138.endLine=762
scope.138.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.139.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlU3RhcnRlZCNzZWxsZXI6NzYy
scope.139.kind=field
scope.139.startLine=762
scope.139.endLine=762
scope.139.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.140.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI2JpZGRlcjo3NzE
scope.140.kind=field
scope.140.startLine=771
scope.140.endLine=771
scope.140.semanticHash=b8ec71792a9a472362073b97719425ae8b5e956ba271d8efedb75371e8312526
scope.141.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI2xhbmQ6Nzcx
scope.141.kind=field
scope.141.startLine=771
scope.141.endLine=771
scope.141.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.142.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkRpc3RyZXNzZWRTYWxlV29uI3ByaWNlOjc3MQ
scope.142.kind=field
scope.142.startLine=771
scope.142.endLine=771
scope.142.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.143.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hbkRlZmF1bHRlZCNjb2xsYXRlcmFsOjY4OA
scope.143.kind=field
scope.143.startLine=688
scope.143.endLine=688
scope.143.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.144.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hbkRlZmF1bHRlZCNuYW1lOjY4OA
scope.144.kind=field
scope.144.startLine=688
scope.144.endLine=688
scope.144.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.145.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblBheW1lbnQjY29sbGF0ZXJhbDo2ODE
scope.145.kind=field
scope.145.startLine=681
scope.145.endLine=681
scope.145.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.146.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblBheW1lbnQjaW50ZXJlc3Q6Njgy
scope.146.kind=field
scope.146.startLine=682
scope.146.endLine=682
scope.146.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.147.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblBheW1lbnQjbmFtZTo2ODE
scope.147.kind=field
scope.147.startLine=681
scope.147.endLine=681
scope.147.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.148.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblBheW1lbnQjcHJpbmNpcGFsOjY4Mg
scope.148.kind=field
scope.148.startLine=682
scope.148.endLine=682
scope.148.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.149.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblJhaXNlZCNhbW91bnQ6Njc3
scope.149.kind=field
scope.149.startLine=677
scope.149.endLine=677
scope.149.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.150.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblJhaXNlZCNib25kaG9sZGVyOjY3OA
scope.150.kind=field
scope.150.startLine=678
scope.150.endLine=678
scope.150.semanticHash=9ec3775488084be096f9d7c37eaec6b46e99d259cb0c2ca485d8766ba1034199
scope.151.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblJhaXNlZCNjb2xsYXRlcmFsOjY3Nw
scope.151.kind=field
scope.151.startLine=677
scope.151.endLine=677
scope.151.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.152.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblJhaXNlZCNuYW1lOjY3Nw
scope.152.kind=field
scope.152.startLine=677
scope.152.endLine=677
scope.152.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.153.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblJlcGFpZCNjb2xsYXRlcmFsOjY4NQ
scope.153.kind=field
scope.153.startLine=685
scope.153.endLine=685
scope.153.semanticHash=fa4f575f311bcaf618dc11167679110fb722a9515b96319aaf9852627d0b22d8
scope.154.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkVudGl0eURldmVsb3BtZW50TG9hblJlcGFpZCNuYW1lOjY4NQ
scope.154.kind=field
scope.154.startLine=685
scope.154.endLine=685
scope.154.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.155.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI2FnZTo4Mjg
scope.155.kind=field
scope.155.startLine=828
scope.155.endLine=828
scope.155.semanticHash=73cf07319348f7df4be5ec1725d9e768231a03a1f8a9a8230686f74c2a9e64d3
scope.156.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQWdlI3BsYXllcjo4Mjg
scope.156.kind=field
scope.156.startLine=828
scope.156.endLine=828
scope.156.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.157.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNiYWxhbmNlOjgyNQ
scope.157.kind=field
scope.157.startLine=825
scope.157.endLine=825
scope.157.semanticHash=9ba2008e7dc4127b70833455dbf24f667e36899897c47cbf225bbc8bd4f5575a
scope.158.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkZpbmFsQmFsYW5jZSNwbGF5ZXI6ODI1
scope.158.kind=field
scope.158.startLine=825
scope.158.endLine=825
scope.158.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.159.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkdvdmVybm1lbnRCYWxhbmNlI2Ftb3VudDo4MjI
scope.159.kind=field
scope.159.startLine=822
scope.159.endLine=822
scope.159.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.160.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjbGFuZDo3Mzg
scope.160.kind=field
scope.160.startLine=738
scope.160.endLine=738
scope.160.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.161.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcGxheWVyOjczOA
scope.161.kind=field
scope.161.startLine=738
scope.161.endLine=738
scope.161.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.162.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlQnVpbHQjcHJpY2U6NzM4
scope.162.kind=field
scope.162.startLine=738
scope.162.endLine=738
scope.162.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.163.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNsYW5kOjc0MQ
scope.163.kind=field
scope.163.startLine=741
scope.163.endLine=741
scope.163.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.164.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwbGF5ZXI6NzQx
scope.164.kind=field
scope.164.startLine=741
scope.164.endLine=741
scope.164.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.165.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkhvdXNlU29sZCNwcmljZTo3NDE
scope.165.kind=field
scope.165.startLine=741
scope.165.endLine=741
scope.165.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.166.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNkZWJ0b3I6NzUw
scope.166.kind=field
scope.166.startLine=750
scope.166.endLine=750
scope.166.semanticHash=7187277bc5d3a4f7eb1846526a3403b2a46995f8b6f5195af4e3989efac8c17f
scope.167.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNsYW5kOjc1MA
scope.167.kind=field
scope.167.startLine=750
scope.167.endLine=750
scope.167.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.168.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaGVyaXRlZCNwbGF5ZXI6NzUw
scope.168.kind=field
scope.168.startLine=750
scope.168.endLine=750
scope.168.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.169.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3BsYXllcjo2MDY
scope.169.kind=field
scope.169.startLine=606
scope.169.endLine=606
scope.169.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.170.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVSb2xsI3RvdGFsOjYwNg
scope.170.kind=field
scope.170.startLine=606
scope.170.endLine=606
scope.170.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.171.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkluaXRpYXRpdmVXb24jcGxheWVyOjYwOQ
scope.171.kind=field
scope.171.startLine=609
scope.171.endLine=609
scope.171.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.172.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxDYXJkVXNlZCNwbGF5ZXI6Nzk1
scope.172.kind=field
scope.172.startLine=795
scope.172.endLine=795
scope.172.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.173.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxEb3VibGVzUm9sbGVkI3BsYXllcjo3OTg
scope.173.kind=field
scope.173.startLine=798
scope.173.endLine=798
scope.173.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.174.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI2NhdXNlOjc4OQ
scope.174.kind=field
scope.174.startLine=789
scope.174.endLine=789
scope.174.semanticHash=f0527b6e66e3f950052646384f5a7c874593c4a39f0fb849428625529221f17b
scope.175.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxFbnRlcmVkI3BsYXllcjo3ODk
scope.175.kind=field
scope.175.startLine=789
scope.175.endLine=789
scope.175.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.176.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNmaW5lOjc5Mg
scope.176.kind=field
scope.176.startLine=792
scope.176.endLine=792
scope.176.semanticHash=e365883bc779c1a28df50988a532d020ef388a587ebb4e7379f58733cfb94b21
scope.177.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxGaW5lUGFpZCNwbGF5ZXI6Nzky
scope.177.kind=field
scope.177.startLine=792
scope.177.endLine=792
scope.177.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.178.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkphaWxTdGF5ZWQjcGxheWVyOjgwMQ
scope.178.kind=field
scope.178.startLine=801
scope.178.endLine=801
scope.178.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.179.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNidXllcjo3NTk
scope.179.kind=field
scope.179.startLine=759
scope.179.endLine=759
scope.179.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.180.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNsYW5kOjc1OQ
scope.180.kind=field
scope.180.startLine=759
scope.180.endLine=759
scope.180.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.181.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNwcmljZTo3NTk
scope.181.kind=field
scope.181.startLine=759
scope.181.endLine=759
scope.181.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.182.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTYWxlUmVmdXNlZCNzZWxsZXI6NzU5
scope.182.kind=field
scope.182.startLine=759
scope.182.endLine=759
scope.182.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.183.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2J1eWVyOjc1Ng
scope.183.kind=field
scope.183.startLine=756
scope.183.endLine=756
scope.183.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.184.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI2xhbmQ6NzU2
scope.184.kind=field
scope.184.startLine=756
scope.184.endLine=756
scope.184.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.185.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3ByaWNlOjc1Ng
scope.185.kind=field
scope.185.startLine=756
scope.185.endLine=756
scope.185.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.186.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxhbmRTb2xkI3NlbGxlcjo3NTY
scope.186.kind=field
scope.186.startLine=756
scope.186.endLine=756
scope.186.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.187.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI2Ftb3VudDo3MTM
scope.187.kind=field
scope.187.startLine=713
scope.187.endLine=713
scope.187.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.188.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI25hbWU6NzEz
scope.188.kind=field
scope.188.startLine=713
scope.188.endLine=713
scope.188.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.189.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5RGl2aWRlbmRQYWlkI3NoYXJlaG9sZGVyczo3MTM
scope.189.kind=field
scope.189.startLine=713
scope.189.endLine=713
scope.189.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.190.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI25hbWU6NzA0
scope.190.kind=field
scope.190.startLine=704
scope.190.endLine=704
scope.190.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.191.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5Rm9ybWVkI3NoYXJlaG9sZGVyczo3MDQ
scope.191.kind=field
scope.191.startLine=704
scope.191.endLine=704
scope.191.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.192.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5SG91c2VCdWlsdCNsYW5kOjcyNQ
scope.192.kind=field
scope.192.startLine=725
scope.192.endLine=725
scope.192.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.193.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5SG91c2VCdWlsdCNuYW1lOjcyNQ
scope.193.kind=field
scope.193.startLine=725
scope.193.endLine=725
scope.193.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.194.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5SG91c2VCdWlsdCNwcmljZTo3MjU
scope.194.kind=field
scope.194.startLine=725
scope.194.endLine=725
scope.194.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.195.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TGlxdWlkYXRlZCNhbW91bnQ6NzE5
scope.195.kind=field
scope.195.startLine=719
scope.195.endLine=719
scope.195.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.196.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TGlxdWlkYXRlZCNuYW1lOjcxOQ
scope.196.kind=field
scope.196.startLine=719
scope.196.endLine=719
scope.196.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.197.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TGlxdWlkYXRlZCNyZWNpcGllbnQ6NzE5
scope.197.kind=field
scope.197.startLine=719
scope.197.endLine=719
scope.197.semanticHash=672b1c509fd6fdd87931787528a8e9d324c264aeb5d13fe775aa6e5220d9a69a
scope.198.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNhbW91bnQ6NzA3
scope.198.kind=field
scope.198.startLine=707
scope.198.endLine=707
scope.198.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.199.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNuYW1lOjcwNw
scope.199.kind=field
scope.199.startLine=707
scope.199.endLine=707
scope.199.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.200.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJhaXNlZCNzaGFyZWhvbGRlcnM6NzA3
scope.200.kind=field
scope.200.startLine=707
scope.200.endLine=707
scope.200.semanticHash=c1e4f420acdf67e7b0a1f03f941e1a1232190f4642817bc52b4b613b854b6b17
scope.201.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNuYW1lOjcxMA
scope.201.kind=field
scope.201.startLine=710
scope.201.endLine=710
scope.201.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.202.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNwcmluY2lwYWw6NzEw
scope.202.kind=field
scope.202.startLine=710
scope.202.endLine=710
scope.202.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.203.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNyZXBheW1lbnQ6NzEw
scope.203.kind=field
scope.203.startLine=710
scope.203.endLine=710
scope.203.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.204.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5TG9hblJlcGFpZCNzaGFyZWhvbGRlcjo3MTA
scope.204.kind=field
scope.204.startLine=710
scope.204.endLine=710
scope.204.semanticHash=5afb4f38ca9ee8f6c22bd1cea0ff3bcc6387deb8673bd78cb1c57d4e6b9e3e1d
scope.205.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjbGFuZDo3MjI
scope.205.kind=field
scope.205.startLine=722
scope.205.endLine=722
scope.205.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.206.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjbmFtZTo3MjI
scope.206.kind=field
scope.206.startLine=722
scope.206.endLine=722
scope.206.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.207.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjcmVudDo3MjI
scope.207.kind=field
scope.207.startLine=722
scope.207.endLine=722
scope.207.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.208.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5UmVudFBhaWQjdGVuYW50OjcyMg
scope.208.kind=field
scope.208.startLine=722
scope.208.endLine=722
scope.208.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.209.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI2J1eWVyOjcxNg
scope.209.kind=field
scope.209.startLine=716
scope.209.endLine=716
scope.209.semanticHash=cdc1a186755692bc4eaae6e2b6ee87d4da855edaf1267345e5ed294c71c949c8
scope.210.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI25hbWU6NzE2
scope.210.kind=field
scope.210.startLine=716
scope.210.endLine=716
scope.210.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.211.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI3ByaWNlOjcxNg
scope.211.kind=field
scope.211.startLine=716
scope.211.endLine=716
scope.211.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.212.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LkxlZ2FsRW50aXR5U2hhcmVTb2xkI3NlbGxlcjo3MTY
scope.212.kind=field
scope.212.startLine=716
scope.212.endLine=716
scope.212.semanticHash=7314b5c8c9ada3b90e9ea2065a0b96b342c75ce56b236cc2a190d4de95e59a5b
scope.213.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNpbnRlcmVzdDo3NTM
scope.213.kind=field
scope.213.startLine=753
scope.213.endLine=753
scope.213.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.214.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNsYW5kOjc1Mw
scope.214.kind=field
scope.214.startLine=753
scope.214.endLine=753
scope.214.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.215.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlS2VwdCNwbGF5ZXI6NzUz
scope.215.kind=field
scope.215.startLine=753
scope.215.endLine=753
scope.215.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.216.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2ludGVyZXN0Ojc0Nw
scope.216.kind=field
scope.216.startLine=747
scope.216.endLine=747
scope.216.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.217.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI2xhbmQ6NzQ3
scope.217.kind=field
scope.217.startLine=747
scope.217.endLine=747
scope.217.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.218.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3BsYXllcjo3NDc
scope.218.kind=field
scope.218.startLine=747
scope.218.endLine=747
scope.218.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.219.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlTGlmdGVkI3RvdGFsOjc0Nw
scope.219.kind=field
scope.219.startLine=747
scope.219.endLine=747
scope.219.semanticHash=c19b9ea10da3846e1c37942dc47f59b13649fda020125cea6df5d3a470490575
scope.220.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNsYW5kOjc0NA
scope.220.kind=field
scope.220.startLine=744
scope.220.endLine=744
scope.220.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.221.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCNwbGF5ZXI6NzQ0
scope.221.kind=field
scope.221.startLine=744
scope.221.endLine=744
scope.221.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.222.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vcnRnYWdlZCN2YWx1ZTo3NDQ
scope.222.kind=field
scope.222.startLine=744
scope.222.endLine=744
scope.222.semanticHash=0d022e0e6113de33cad355012469518c3a21832e276a9c3f3a6893880214fa0a
scope.223.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb206NjI2
scope.223.kind=field
scope.223.startLine=626
scope.223.endLine=626
scope.223.semanticHash=e4f7d7ea083c2d5c93e9738307ca8eed444fe64f0a57ee8008fb28ad774f2c8b
scope.224.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI2Zyb21TcGFjZTo2MjY
scope.224.kind=field
scope.224.startLine=626
scope.224.endLine=626
scope.224.semanticHash=fdcd833bf3c0613749af9aa35feb23fbe7068c7d720cdb3a09bbbebeefbe4e7c
scope.225.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3BsYXllcjo2MjY
scope.225.kind=field
scope.225.startLine=626
scope.225.endLine=626
scope.225.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.226.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvOjYyNg
scope.226.kind=field
scope.226.startLine=626
scope.226.endLine=626
scope.226.semanticHash=eb749c165b5ddff3f2a4105d6abd099cfdfb868d73da7f229996cc9c2c6fb592
scope.227.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5Lk1vdmVkI3RvU3BhY2U6NjI2
scope.227.kind=field
scope.227.startLine=626
scope.227.endLine=626
scope.227.semanticHash=061c4ba46bf16ef78d0e00d27fbe750d73f969cccf700678171eb04b70eab629
scope.228.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNvZmZlcmVkOjY0Nw
scope.228.kind=field
scope.228.startLine=647
scope.228.endLine=647
scope.228.semanticHash=649b65565a280b6fb6d03fec31d684ad9ab5a25ce6bab147d7a18dd5ae60c190
scope.229.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSNwYXJ0bmVyOjY0Nw
scope.229.kind=field
scope.229.startLine=647
scope.229.endLine=647
scope.229.semanticHash=95af23a2c982143b2ae56ecefdadd5af27a308d33e43ffd831ee7dabec5ab90b
scope.230.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSN0cmFkZXI6NjQ3
scope.230.kind=field
scope.230.startLine=647
scope.230.endLine=647
scope.230.semanticHash=1d660dfe29231866caa76a65bb832b7e5d382d4fc7d41cec6b19f988a2357cf4
scope.231.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBlZXJUcmFkZSN3YW50ZWQ6NjQ3
scope.231.kind=field
scope.231.startLine=647
scope.231.endLine=647
scope.231.semanticHash=bd6096bdbf00201b8b36b0ea0e225711c7485226561a01fef0ded8ce1c44ea48
scope.232.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjYW1vdW50OjczNQ
scope.232.kind=field
scope.232.startLine=735
scope.232.endLine=735
scope.232.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.233.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZWU6NzM1
scope.233.kind=field
scope.233.startLine=735
scope.233.endLine=735
scope.233.semanticHash=289e0e3b44081936aa54790d8a0cebe895c1f5ad30a87142f45cb4ec4a20f8d3
scope.234.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlBsYXllclBhaWQjcGF5ZXI6NzM1
scope.234.kind=field
scope.234.startLine=735
scope.234.endLine=735
scope.234.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.235.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjbGFuZDo3Mjg
scope.235.kind=field
scope.235.startLine=728
scope.235.endLine=728
scope.235.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.236.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcGxheWVyOjcyOA
scope.236.kind=field
scope.236.startLine=728
scope.236.endLine=728
scope.236.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.237.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcHJpY2U6NzI4
scope.237.kind=field
scope.237.startLine=728
scope.237.endLine=728
scope.237.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.238.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcmVhc29uOjcyOQ
scope.238.kind=field
scope.238.startLine=729
scope.238.endLine=729
scope.238.semanticHash=9925e2b957cf3e5ae356bb085657ef3bece891d34dc0ab901046c1292ffc60fd
scope.239.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlB1cmNoYXNlRGVjbGluZWQjcmVzZXJ2ZTo3Mjk
scope.239.kind=field
scope.239.startLine=729
scope.239.endLine=729
scope.239.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.240.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI2xhbmQ6NzMy
scope.240.kind=field
scope.240.startLine=732
scope.240.endLine=732
scope.240.semanticHash=28e825f53ee083991bf0e5d6ab6e8552061ab8660b4ca623968d4ed89d7c5dff
scope.241.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI293bmVyOjczMg
scope.241.kind=field
scope.241.startLine=732
scope.241.endLine=732
scope.241.semanticHash=4dc1b9a409163bdb6ca915d1f2c9f0426b20999866c7edce9557fe1f7cfb7c14
scope.242.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3JlbnQ6NzMy
scope.242.kind=field
scope.242.startLine=732
scope.242.endLine=732
scope.242.semanticHash=5e2b4ccb93b6ac83ee7acf7ee9441dd045cc9d0cb1088f86b1ebc03d0e500455
scope.243.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRQYWlkI3RlbmFudDo3MzI
scope.243.kind=field
scope.243.startLine=732
scope.243.endLine=732
scope.243.semanticHash=f3564b5480882244a5ebd7ab4acca5065cd34832ac13afc32ddd667c8c36e093
scope.244.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJlbnRSZWxpZWZFbmFibGVkI2VuYWJsZWQ6ODE2
scope.244.kind=field
scope.244.startLine=816
scope.244.endLine=816
scope.244.semanticHash=3e72e1b05fced05e3a99e662dfe70f6c5ed519247ae3422c53c101825f46b1b8
scope.245.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCNwbGF5ZXI6NjIz
scope.245.kind=field
scope.245.startLine=623
scope.245.endLine=623
scope.245.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.246.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlJvbGxlZCN0b3RhbDo2MjM
scope.246.kind=field
scope.246.startLine=623
scope.246.endLine=623
scope.246.semanticHash=4c384a147eacfdc87d6a3c17f579cb6bad74c3a9a2bfc704d6e84e4edf8f4161
scope.247.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNwbGF5ZXI6NjM2
scope.247.kind=field
scope.247.startLine=636
scope.247.endLine=636
scope.247.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.248.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNhbGFyeUNvbGxlY3RlZCNzYWxhcnk6NjM2
scope.248.kind=field
scope.248.startLine=636
scope.248.endLine=636
scope.248.semanticHash=d9b0cbd5d9fbabbb09b15cd6ae18d92c1489196951b85dfde66747f68b0b5787
scope.249.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI2Ftb3VudDo3MDE
scope.249.kind=field
scope.249.startLine=701
scope.249.endLine=701
scope.249.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.250.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI3BheWVlOjcwMQ
scope.250.kind=field
scope.250.startLine=701
scope.250.endLine=701
scope.250.semanticHash=289e0e3b44081936aa54790d8a0cebe895c1f5ad30a87142f45cb4ec4a20f8d3
scope.251.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlQYWlkI3BheWVyOjcwMQ
scope.251.kind=field
scope.251.startLine=701
scope.251.endLine=701
scope.251.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.252.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jbG9zZXI6Njk4
scope.252.kind=field
scope.252.startLine=698
scope.252.endLine=698
scope.252.semanticHash=878e93ca653f3f39cf25b2c3775677351abe7c49bd9a13f0aa882a3a8db96732
scope.253.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlNwbGl0TW9ub3BvbHlXb24jd2lubmVyOjY5OA
scope.253.kind=field
scope.253.startLine=698
scope.253.endLine=698
scope.253.semanticHash=1f6f344bd703491733c82249fd05cc65806c907d8c6d3cc869164207c368c138
scope.254.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YWxlbWF0ZVRyYWRpbmcjZW5hYmxlZDo2NTA
scope.254.kind=field
scope.254.startLine=650
scope.254.endLine=650
scope.254.semanticHash=3e72e1b05fced05e3a99e662dfe70f6c5ed519247ae3422c53c101825f46b1b8
scope.255.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0YXJ0I3BsYXllcnM6NjAy
scope.255.kind=field
scope.255.startLine=602
scope.255.endLine=602
scope.255.semanticHash=bac06617eef6d96a46137371e070a20020b1336319f16f0348b5075f2db38c6e
scope.256.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjYXNzZXRSaWNoT3BlbmluZzo2OTI
scope.256.kind=field
scope.256.startLine=692
scope.256.endLine=692
scope.256.semanticHash=7efc696172cbec5ed85408de8733c6da3f2b2cc5724c20466e169a6bb814a8d2
scope.257.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjbGVnYWxFbnRpdHlFbmFibGVkOjY5MQ
scope.257.kind=field
scope.257.startLine=691
scope.257.endLine=691
scope.257.semanticHash=3a439c68b10c6447b43eedcb90e029072821e3d882b40b96c05daca4711b31ec
scope.258.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjbmFtZTo2OTE
scope.258.kind=field
scope.258.startLine=691
scope.258.endLine=691
scope.258.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.259.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjcGxheWVyOjY5MQ
scope.259.kind=field
scope.259.startLine=691
scope.259.endLine=691
scope.259.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.260.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlN0cmF0ZWd5TmFtZWQjc3RhbGVtYXRlRW5hYmxlZDo2OTI
scope.260.kind=field
scope.260.startLine=692
scope.260.endLine=692
scope.260.semanticHash=b9cf07e63923db3b13851ddc329a43bc3fdd5989f2dd5423302648247c104691
scope.261.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI2FnZTo2MTM
scope.261.kind=field
scope.261.startLine=613
scope.261.endLine=613
scope.261.semanticHash=73cf07319348f7df4be5ec1725d9e768231a03a1f8a9a8230686f74c2a9e64d3
scope.262.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI2JhbGFuY2U6NjEz
scope.262.kind=field
scope.262.startLine=613
scope.262.endLine=613
scope.262.semanticHash=9ba2008e7dc4127b70833455dbf24f667e36899897c47cbf225bbc8bd4f5575a
scope.263.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3BsYXllcjo2MTM
scope.263.kind=field
scope.263.startLine=613
scope.263.endLine=613
scope.263.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.264.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LlR1cm5TdGFydGVkI3Jlc2VydmU6NjEz
scope.264.kind=field
scope.264.startLine=613
scope.264.endLine=613
scope.264.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.265.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LldhclByb2ZpdHNUYXhFbmFibGVkI2VuYWJsZWQ6ODEz
scope.265.kind=field
scope.265.startLine=813
scope.265.endLine=813
scope.265.semanticHash=3e72e1b05fced05e3a99e662dfe70f6c5ed519247ae3422c53c101825f46b1b8
scope.266.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LldhclByb2ZpdHNUYXhQYWlkI2Ftb3VudDo4MTk
scope.266.kind=field
scope.266.startLine=819
scope.266.endLine=819
scope.266.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.267.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LldhclByb2ZpdHNUYXhQYWlkI3BheWVyOjgxOQ
scope.267.kind=field
scope.267.startLine=819
scope.267.endLine=819
scope.267.semanticHash=43ec7b64660d69d88e9669b849d5635ed1eae6b2768df64436b00b0c69c91151
scope.268.id=ZmllbGQ6R2FtZS5Kb3VybmFsLkVudHJ5LldvbiNwbGF5ZXI6ODMx
scope.268.kind=field
scope.268.startLine=831
scope.268.endLine=831
scope.268.semanticHash=32b80de627af50a2ac3b7b1ca6a7a328516820ab2c55800e9fec15ab8b01e3d3
scope.269.id=ZmllbGQ6R2FtZS5SZXN1bHQjZGVlZHM6NTc5
scope.269.kind=field
scope.269.startLine=579
scope.269.endLine=579
scope.269.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.270.id=ZmllbGQ6R2FtZS5SZXN1bHQjam91cm5hbDo1Nzk
scope.270.kind=field
scope.270.startLine=579
scope.270.endLine=579
scope.270.semanticHash=5ba9245a58b5efd38657a1e17874c8f1d81301c483e6bf37077a9f5d034fc091
scope.271.id=ZmllbGQ6R2FtZS5SZXN1bHQjdHVybk9yZGVyOjU3OQ
scope.271.kind=field
scope.271.startLine=579
scope.271.endLine=579
scope.271.semanticHash=2c17e31d4d5ca1f912bce32bdcc9470925dee60820fbc92658d66a327a242e05
scope.272.id=ZmllbGQ6R2FtZS5SZXN1bHQjd2lubmVyOjU3OQ
scope.272.kind=field
scope.272.startLine=579
scope.272.endLine=579
scope.272.semanticHash=9e05c00db702321e24ecb1c4429dea5328a65101957c7f0b7699f23ee7c539a9
scope.273.id=bWV0aG9kOkdhbWUjYWxsT3duYWJsZVNwYWNlc093bmVkKDApOjQ3Ng
scope.273.kind=method
scope.273.startLine=476
scope.273.endLine=479
scope.273.semanticHash=821da435108af6599de0db2f7083a8fd6fd049024fea6375a0521284793b5c56
scope.274.id=bWV0aG9kOkdhbWUjYW55U3BsaXRFeGlzdHMoMik6NDQ2
scope.274.kind=method
scope.274.startLine=446
scope.274.endLine=448
scope.274.semanticHash=0e159d6c3a604e0cab363efb196c3aed27bbfe673c6cff777443dacea59f3e81
scope.275.id=bWV0aG9kOkdhbWUjYW55U3RyYXRlZ3lFbmFibGVzRGV2ZWxvcG1lbnRMb2FucygyKToxNzM
scope.275.kind=method
scope.275.startLine=173
scope.275.endLine=175
scope.275.semanticHash=1bf885f40dc7a1ef7377d4b3039e1a49acc1553be280c12c5c453d2eb78fab7c
scope.276.id=bWV0aG9kOkdhbWUjYW55U3RyYXRlZ3lVc2VzRnVsbExvYW5EcmF3KDIpOjE3Nw
scope.276.kind=method
scope.276.startLine=177
scope.276.endLine=179
scope.276.semanticHash=7801c07132d07331c2788ed1e3fd76d152191f25279376361c2742c8f51fe036
scope.277.id=bWV0aG9kOkdhbWUjYXBwbHlBc3NldFJpY2hPcGVuaW5nKDApOjE5NQ
scope.277.kind=method
scope.277.startLine=195
scope.277.endLine=202
scope.277.semanticHash=efae9e7878f02dc2189f799a02377ad7db70ea1f02411c43dda663117a60f0f9
scope.278.id=bWV0aG9kOkdhbWUjYXBwbHlCdXlvdXQoMik6NDYx
scope.278.kind=method
scope.278.startLine=461
scope.278.endLine=467
scope.278.semanticHash=490e3f2a0e9634f39baf8c29b0caef13e593049258aefa0480cb32aa22f7b814
scope.279.id=bWV0aG9kOkdhbWUjYXBwbHlPcGVuaW5nQ2FwaXRhbCgwKToxODg
scope.279.kind=method
scope.279.startLine=188
scope.279.endLine=193
scope.279.semanticHash=e5aafb70c5f830d43879b0f8cd4d6e6d1f730b2231cafa0e4e5725fb968895e2
scope.280.id=bWV0aG9kOkdhbWUjY2FuRm9ybUF0TWFya2V0RGVhZGxvY2soMik6NDk1
scope.280.kind=method
scope.280.startLine=495
scope.280.endLine=497
scope.280.semanticHash=7bdfe09e3027f05de8ecc064bfe3cb25b1f8ec5944ace0d47c94f70055deb2a6
scope.281.id=bWV0aG9kOkdhbWUjY29tcGxldGVSb3VuZCgzKToyOTM
scope.281.kind=method
scope.281.startLine=293
scope.281.endLine=299
scope.281.semanticHash=edf97dd0bf953aaeaab539f0028777cd32de6377d104b6cca4a9854a1b2b0d41
scope.282.id=bWV0aG9kOkdhbWUjY29tcGxldGVUcmFkZSgzKTo0Njk
scope.282.kind=method
scope.282.startLine=469
scope.282.endLine=474
scope.282.semanticHash=cc6b8e4a00dd1403a07cfda0fd3f3877446d6d7c6e0ecb0b9faa32ea6dbcaba7
scope.283.id=bWV0aG9kOkdhbWUjY3RvcigxMCk6MTEw
scope.283.kind=method
scope.283.startLine=110
scope.283.endLine=116
scope.283.semanticHash=3c3bb34937bdcc66ebdca64b80d05634b171ab8f499cc4554922964d8e1a4162
scope.284.id=bWV0aG9kOkdhbWUjY3RvcigxMik6MTE4
scope.284.kind=method
scope.284.startLine=118
scope.284.endLine=125
scope.284.semanticHash=93d0ef07b06c0859184e0934aaf66c9ed85cc9828dc8fabb5dd655a094035721
scope.285.id=bWV0aG9kOkdhbWUjY3RvcigxMyk6MTI3
scope.285.kind=method
scope.285.startLine=127
scope.285.endLine=135
scope.285.semanticHash=c8e0309db20dee371f5361e388bb1c1196f0e2c46d02f925169f2ea95ba50ad7
scope.286.id=bWV0aG9kOkdhbWUjY3RvcigxNCk6MTM3
scope.286.kind=method
scope.286.startLine=137
scope.286.endLine=145
scope.286.semanticHash=358a350f8d52712876ade29bbf7ef470b9fe30dff50e9d7864a388b257e1fccb
scope.287.id=bWV0aG9kOkdhbWUjY3RvcigxNSk6MTQ3
scope.287.kind=method
scope.287.startLine=147
scope.287.endLine=171
scope.287.semanticHash=8ef651800d652b6a81eaa4e694bf9de56ddfc28504e5e64fee2c1286269a3a44
scope.288.id=bWV0aG9kOkdhbWUjY3RvcigyKToyMTA
scope.288.kind=method
scope.288.startLine=210
scope.288.endLine=212
scope.288.semanticHash=bbfd6ea205f99103df0bc3a9b80016fa81d78de36b673cfb9b946fc66975fdb8
scope.289.id=bWV0aG9kOkdhbWUjY3RvcigzKToyMDU
scope.289.kind=method
scope.289.startLine=205
scope.289.endLine=207
scope.289.semanticHash=6776d3f993630076b24a0ed0b3bc39a8d8cf1fb2c29184dba3c568e657a2a980
scope.290.id=bWV0aG9kOkdhbWUjY3RvcigzKToyMTU
scope.290.kind=method
scope.290.startLine=215
scope.290.endLine=217
scope.290.semanticHash=8a1727802fb098b4014a7084e87d372aee8e94d4798202635f868ac099b293ec
scope.291.id=bWV0aG9kOkdhbWUjY3Rvcig0KTo3NA
scope.291.kind=method
scope.291.startLine=74
scope.291.endLine=76
scope.291.semanticHash=d4615ba990b44348e21394831d757cef04354db1b8751fb1a298772f84bb2d76
scope.292.id=bWV0aG9kOkdhbWUjY3Rvcig1KTo3OA
scope.292.kind=method
scope.292.startLine=78
scope.292.endLine=80
scope.292.semanticHash=8f72f5dd6632da91ac15bbd4118e10ec925d3f7f35e6559ed82d3cfe56b10db1
scope.293.id=bWV0aG9kOkdhbWUjY3Rvcig2KTo4Mg
scope.293.kind=method
scope.293.startLine=82
scope.293.endLine=87
scope.293.semanticHash=201613e9dfbe05f1b87a4d5e480877d354f121084a686ec5d292531839832ee1
scope.294.id=bWV0aG9kOkdhbWUjY3Rvcig3KTo4OQ
scope.294.kind=method
scope.294.startLine=89
scope.294.endLine=94
scope.294.semanticHash=ed3b862b8b56575f057bb3efc8c37f63dfda088f90226752db32af60f9b5fbb2
scope.295.id=bWV0aG9kOkdhbWUjY3Rvcig4KTo5Ng
scope.295.kind=method
scope.295.startLine=96
scope.295.endLine=101
scope.295.semanticHash=f74e2706eebc6a1ef10bac9fce2227079ba1ec4c8654585f692c3846c4306ffd
scope.296.id=bWV0aG9kOkdhbWUjY3Rvcig5KToxMDM
scope.296.kind=method
scope.296.startLine=103
scope.296.endLine=108
scope.296.semanticHash=5ad5cadd5f8b1f50a3d3531989a80130eb1771fa011b74e876e06f9245f401b3
scope.297.id=bWV0aG9kOkdhbWUjZW50aXR5TmFtZSgxKTo0MTI
scope.297.kind=method
scope.297.startLine=412
scope.297.endLine=415
scope.297.semanticHash=63fedf93747ba25ad7ae7201643dc5ea04e06ae29cc5db0a4f58c0224e0bd74a
scope.298.id=bWV0aG9kOkdhbWUjZm9ybUlmRnVuZGFibGUoMSk6NTA2
scope.298.kind=method
scope.298.startLine=506
scope.298.endLine=515
scope.298.semanticHash=d9614db053d0971cf855334379998fbab91459c72dedaac6184ab3e57c1613a7
scope.299.id=bWV0aG9kOkdhbWUjZnVuZGFibGVFbnRpdHlBdE1hcmtldERlYWRsb2NrKDApOjQ5OQ
scope.299.kind=method
scope.299.startLine=499
scope.299.endLine=504
scope.299.semanticHash=a22a689ae1f634ce29b92baaa2d15e65d5f78f87257692f68b7b3fe8f44196ed
scope.300.id=bWV0aG9kOkdhbWUjaWRzKDEpOjU3MA
scope.300.kind=method
scope.300.startLine=570
scope.300.endLine=572
scope.300.semanticHash=a28f139fe6a5208bcdfb8c608d7ef22e03aab9a941367fef51041725dd669ffb
scope.301.id=bWV0aG9kOkdhbWUjaW5pdGlhdGl2ZVJvbGxGb3IoMik6NTI2
scope.301.kind=method
scope.301.startLine=526
scope.301.endLine=530
scope.301.semanticHash=02c9a3abe68c0c5fe0c310580ea38a24c05fb58641f8a6c89c8a323d77193731
scope.302.id=bWV0aG9kOkdhbWUjaXNQbGF5ZXJTdGlsbFNvbHZlbnQoMSk6NDA4
scope.302.kind=method
scope.302.startLine=408
scope.302.endLine=410
scope.302.semanticHash=7f544413a575f6a989f89237d18e333f45762a295f96ba031340b613e4790a45
scope.303.id=bWV0aG9kOkdhbWUjaXNUaWVkV2l0aEl0c1BhcnRuZXIoMik6NDU1
scope.303.kind=method
scope.303.startLine=455
scope.303.endLine=459
scope.303.semanticHash=57ada1100a85c9b9156ca849da5f508164593d3caa30ca1a8fc6c96fa9b4ecfd
scope.304.id=bWV0aG9kOkdhbWUjam91cm5hbE9wZXJhdGlvbigyKTozOTA
scope.304.kind=method
scope.304.startLine=390
scope.304.endLine=406
scope.304.semanticHash=4339fa853fbaafab5bac31784066a07c5caa33cf7a39b87fad76e926afa2e720
scope.305.id=bWV0aG9kOkdhbWUjbGFuZGluZ3NGb3IoMyk6NTQw
scope.305.kind=method
scope.305.startLine=540
scope.305.endLine=558
scope.305.semanticHash=288a76391f035403c868df024ced5feb15dc22a1a78a4cb2b8c51752854acc39
scope.306.id=bWV0aG9kOkdhbWUjbG9nR2FtZUVuZCgzKTozMzY
scope.306.kind=method
scope.306.startLine=336
scope.306.endLine=343
scope.306.semanticHash=cb91dbb0b2c65a397a1853b92b14c9d45bfc2704a23242b38f8f9f7279d72765
scope.307.id=bWV0aG9kOkdhbWUjbG9nU3RhbGVtYXRlKDIpOjMyNQ
scope.307.kind=method
scope.307.startLine=325
scope.307.endLine=327
scope.307.semanticHash=93c57570f440efe0424434f7ba39a512b3ad0fe40a47746abf2b5073cca5cf50
scope.308.id=bWV0aG9kOkdhbWUjbW9ydGdhZ2VFbnRpdHlTcGFyZVByb3BlcnR5KDEpOjM3Mw
scope.308.kind=method
scope.308.startLine=373
scope.308.endLine=383
scope.308.semanticHash=2d4554b9bc218c4f1afc5ef2d51a6de4c74a8618eff4709c2e78658d3c818595
scope.309.id=bWV0aG9kOkdhbWUjb3BlcmF0ZUVudGl0eSgyKTozODU
scope.309.kind=method
scope.309.startLine=385
scope.309.endLine=388
scope.309.semanticHash=ebf1882bb72055149f4b7ea291611d31d54863f0afa364ee089d6c5cd0a45357
scope.310.id=bWV0aG9kOkdhbWUjb3BlcmF0ZUxlZ2FsRW50aXRpZXMoMSk6MzQ1
scope.310.kind=method
scope.310.startLine=345
scope.310.endLine=354
scope.310.semanticHash=2524097253eb52c710db0afe8eed43873bc8b77ed58eacab22ada63aa15776dd
scope.311.id=bWV0aG9kOkdhbWUjcGxheSgwKToyMTk
scope.311.kind=method
scope.311.startLine=219
scope.311.endLine=221
scope.311.semanticHash=3bcadbbb1f6b598fdb83fbc0fdd237a7656cc24edc1054185a280a4b7b46cb3b
scope.312.id=bWV0aG9kOkdhbWUjcGxheSgyKToyNDU
scope.312.kind=method
scope.312.startLine=245
scope.312.endLine=269
scope.312.semanticHash=2ea51684cebe88a0402c28cc38c13d6411b20af4f487d338cb0aac7c0a4beec9
scope.313.id=bWV0aG9kOkdhbWUjcGxheVRvQ29tcGxldGlvbigwKToyMjQ
scope.313.kind=method
scope.313.startLine=224
scope.313.endLine=226
scope.313.semanticHash=a60fc108488c55d28cf9d6828599290071eeae99381682b526b1392f2b106627
scope.314.id=bWV0aG9kOkdhbWUjcGxheVR1cm4oNSk6MzEy
scope.314.kind=method
scope.314.startLine=312
scope.314.endLine=323
scope.314.semanticHash=15a3b6f599f100df1307d0231fc147ab6107789cea7e06775b7fb2d0a80e6917
scope.315.id=bWV0aG9kOkdhbWUjcGxheVR1cm5zKDYpOjI3MQ
scope.315.kind=method
scope.315.startLine=271
scope.315.endLine=287
scope.315.semanticHash=a0c3905dc946ca7adabbf004d05a02af67b9dd536045b1f0f8cc6a14c6526da1
scope.316.id=bWV0aG9kOkdhbWUjcGxheVVudGlsU3RvcHBlZCgxKToyMzM
scope.316.kind=method
scope.316.startLine=233
scope.316.endLine=235
scope.316.semanticHash=2159cc9b2267372bf24f16472c20269d3d5376d0624e178122a5a131ef094b22
scope.317.id=bWV0aG9kOkdhbWUjcGxheVVwVG9Sb3VuZHMoMSk6MjM4
scope.317.kind=method
scope.317.startLine=238
scope.317.endLine=243
scope.317.semanticHash=5561780af095da6a7387c86fefebb99d462fe27e5bfd7e0d9d8c2775fee408be
scope.318.id=bWV0aG9kOkdhbWUjcmVtYWluaW5nUGxheWVycygwKTo1MTc
scope.318.kind=method
scope.318.startLine=517
scope.318.endLine=519
scope.318.semanticHash=a0e051c1b866b1352982334442d470d1567187f7e091423c51fc78cf3a6f2874
scope.319.id=bWV0aG9kOkdhbWUjcmVzb2x2YWJsZUJ1eW91dCgyKTo0NDA
scope.319.kind=method
scope.319.startLine=440
scope.319.endLine=444
scope.319.semanticHash=60193aea7acb7bb2bd806c5a07dbaa4667dce363ef321f1925a9fba671e138da
scope.320.id=bWV0aG9kOkdhbWUjcmVzb2x2ZUJ1eW91dEF0U3RhcnQoMyk6NDMw
scope.320.kind=method
scope.320.startLine=430
scope.320.endLine=438
scope.320.semanticHash=813be9ad2de20348d1c3f4e3ab32f44e77f3c733d62da6eb038615c74509f812
scope.321.id=bWV0aG9kOkdhbWUjcmVzb2x2ZU1hcmtldERlYWRsb2NrQXRSb3VuZEJvdW5kYXJ5KDIpOjQ4Mg
scope.321.kind=method
scope.321.startLine=482
scope.321.endLine=484
scope.321.semanticHash=30bb4d0b870d5ad6505155cb400c14f5789c1f54ed04985056ae7eddee4c0f08
scope.322.id=bWV0aG9kOkdhbWUjcmVzb2x2ZU1hcmtldERlYWRsb2NrQXRSb3VuZEJvdW5kYXJ5KDMpOjQ4Ng
scope.322.kind=method
scope.322.startLine=486
scope.322.endLine=493
scope.322.semanticHash=f39506884a4af361259929b2c90adfb0133010019f668651bcae1ce13dcd08f9
scope.323.id=bWV0aG9kOkdhbWUjcmVzb2x2ZVNwbGl0T3duZXJzaGlwQXRTdGFydCgzKTo0MTc
scope.323.kind=method
scope.323.startLine=417
scope.323.endLine=419
scope.323.semanticHash=ef2df8efc581363b012c0e7be3f055e9e5bd810881d603974bb10c6cd513850e
scope.324.id=bWV0aG9kOkdhbWUjcm91bmRMb2dnZWRBQmFua3J1cHRjeSgyKTozMDE
scope.324.kind=method
scope.324.startLine=301
scope.324.endLine=304
scope.324.semanticHash=53d6ee7e6b5f4c7c208f88782a1182b4adfcb2cbca20a468cea6d85a7da78947
scope.325.id=bWV0aG9kOkdhbWUjc2VydmljZUVudGl0eURldmVsb3BtZW50TG9hbigyKTozNTY
scope.325.kind=method
scope.325.startLine=356
scope.325.endLine=371
scope.325.semanticHash=4a7402a6d2beca08cf0459379d344a1ddaf628fd883ad9da878fd6e270d58226
scope.326.id=bWV0aG9kOkdhbWUjc2hvdWxkQ29udGludWVQbGF5aW5nKDIpOjI4OQ
scope.326.kind=method
scope.326.startLine=289
scope.326.endLine=291
scope.326.semanticHash=3519a825edcf4cda2f7bd302139da772ba8e824d0b3b9288a06b22cf004a416a
scope.327.id=bWV0aG9kOkdhbWUjdGFrZVR1cm4oNCk6NTMy
scope.327.kind=method
scope.327.startLine=532
scope.327.endLine=538
scope.327.semanticHash=f390c2edc5e763c9dd207eef3bd7f6dbaa6aae4b82c011607b19c5dbddcb07d5
scope.328.id=bWV0aG9kOkdhbWUjdHJhZGVBdFN0YXJ0KDMpOjQyMQ
scope.328.kind=method
scope.328.startLine=421
scope.328.endLine=428
scope.328.semanticHash=00a2ae028121c54e1b0badb12cbea5b9ce67c3e61648228279a91cde6923402b
scope.329.id=bWV0aG9kOkdhbWUjdHVybkVuZHNUaGVHYW1lKDUpOjMwNg
scope.329.kind=method
scope.329.startLine=306
scope.329.endLine=310
scope.329.semanticHash=83e945a397db313c7a52512d8ae14ffe5714039693825b87cb01c68c34f22bfe
scope.330.id=bWV0aG9kOkdhbWUjd2lubmVyKDApOjUyMQ
scope.330.kind=method
scope.330.startLine=521
scope.330.endLine=524
scope.330.semanticHash=702f44695db994b2e4908c5393ffd81fcd816cff000bc8cb31c6d97c66191345
scope.331.id=bWV0aG9kOkdhbWUjeWVhckxpbWl0SnVzdFJlYWNoZWQoMik6MzI5
scope.331.kind=method
scope.331.startLine=329
scope.331.endLine=334
scope.331.semanticHash=b0b634960610b577ed0eace4c0b299fc0673cc3d695f2cf18bc20d0938d71212
scope.332.id=bWV0aG9kOkdhbWUuQ3VwcyNmb3JQbGF5ZXIoMSk6NTY3
scope.332.kind=method
scope.332.startLine=567
scope.332.endLine=567
scope.332.semanticHash=00fab9708d6c73acccd913824e67a53f61e660b995153a30a990a433d93a4fc3
scope.333.id=bWV0aG9kOkdhbWUuSm91cm5hbCNjdG9yKDApOjU4Mg
scope.333.kind=method
scope.333.startLine=1
scope.333.endLine=835
scope.333.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.334.id=bWV0aG9kOkdhbWUuSm91cm5hbCNlbnRyaWVzKDApOjU5Mg
scope.334.kind=method
scope.334.startLine=592
scope.334.endLine=594
scope.334.semanticHash=fc7196066ad4361fe5d8d57ac91738e1af40e9f4427dc7eabfdf50f3c66a63c5
scope.335.id=bWV0aG9kOkdhbWUuSm91cm5hbCNsb2coMSk6NTg3
scope.335.kind=method
scope.335.startLine=587
scope.335.endLine=590
scope.335.semanticHash=f2f4e1f3c7bd7244a0e0a2e125110a27d8516e8cb7036d71c5cb73f65468d33f
scope.336.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5BdWN0aW9uV29uI2N0b3IoMyk6NjQ0
scope.336.kind=method
scope.336.startLine=1
scope.336.endLine=835
scope.336.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.337.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUGFpZCNjdG9yKDIpOjc4Mw
scope.337.kind=method
scope.337.startLine=1
scope.337.endLine=835
scope.337.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.338.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rUmVjZWl2ZWQjY3RvcigyKTo3ODY
scope.338.kind=method
scope.338.startLine=1
scope.338.endLine=835
scope.338.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.339.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CYW5rcnVwdCNjdG9yKDIpOjgwNA
scope.339.kind=method
scope.339.startLine=1
scope.339.endLine=835
scope.339.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.340.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Cb3VnaHQjY3RvcigzKTo2NDA
scope.340.kind=method
scope.340.startLine=1
scope.340.endLine=835
scope.340.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.341.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5CdWlsZGluZ1JlZnVzZWQjY3RvcigzKTo3NzQ
scope.341.kind=method
scope.341.startLine=1
scope.341.endLine=835
scope.341.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.342.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5DaGFuY2VDYXJkRHJhd24jY3RvcigyKTo3Nzc
scope.342.kind=method
scope.342.startLine=1
scope.342.endLine=835
scope.342.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.343.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Db21tdW5pdHlDaGVzdENhcmREcmF3biNjdG9yKDIpOjc4MA
scope.343.kind=method
scope.343.startLine=1
scope.343.endLine=835
scope.343.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.344.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EZXZlbG9wbWVudEJvbmRQYXltZW50I2N0b3IoNCk6NjY0
scope.344.kind=method
scope.344.startLine=1
scope.344.endLine=835
scope.344.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.345.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EZXZlbG9wbWVudExvYW5EZWZhdWx0ZWQjY3RvcigyKTo2NzE
scope.345.kind=method
scope.345.startLine=1
scope.345.endLine=835
scope.345.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.346.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EZXZlbG9wbWVudExvYW5QYXltZW50I2N0b3IoNCk6NjYw
scope.346.kind=method
scope.346.startLine=1
scope.346.endLine=835
scope.346.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.347.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EZXZlbG9wbWVudExvYW5SYWlzZWQjY3Rvcig0KTo2NTY
scope.347.kind=method
scope.347.startLine=1
scope.347.endLine=835
scope.347.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.348.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EZXZlbG9wbWVudExvYW5SZWNvdmVyZWQjY3RvcigyKTo2NzQ
scope.348.kind=method
scope.348.startLine=1
scope.348.endLine=835
scope.348.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.349.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EZXZlbG9wbWVudExvYW5SZXBhaWQjY3RvcigyKTo2Njg
scope.349.kind=method
scope.349.startLine=1
scope.349.endLine=835
scope.349.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.350.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EZXZlbG9wbWVudExvYW5zI2N0b3IoMik6NjUz
scope.350.kind=method
scope.350.startLine=1
scope.350.endLine=835
scope.350.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.351.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkT2ZmZXIjY3RvcigzKTo3Njg
scope.351.kind=method
scope.351.startLine=1
scope.351.endLine=835
scope.351.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.352.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZU5vQmlkZGVyI2N0b3IoMik6NzY1
scope.352.kind=method
scope.352.startLine=1
scope.352.endLine=835
scope.352.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.353.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZVN0YXJ0ZWQjY3RvcigyKTo3NjI
scope.353.kind=method
scope.353.startLine=1
scope.353.endLine=835
scope.353.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.354.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5EaXN0cmVzc2VkU2FsZVdvbiNjdG9yKDMpOjc3MQ
scope.354.kind=method
scope.354.startLine=1
scope.354.endLine=835
scope.354.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.355.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5FbnRpdHlEZXZlbG9wbWVudExvYW5EZWZhdWx0ZWQjY3RvcigyKTo2ODg
scope.355.kind=method
scope.355.startLine=1
scope.355.endLine=835
scope.355.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.356.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5FbnRpdHlEZXZlbG9wbWVudExvYW5QYXltZW50I2N0b3IoNCk6Njgx
scope.356.kind=method
scope.356.startLine=1
scope.356.endLine=835
scope.356.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.357.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5FbnRpdHlEZXZlbG9wbWVudExvYW5SYWlzZWQjY3Rvcig0KTo2Nzc
scope.357.kind=method
scope.357.startLine=1
scope.357.endLine=835
scope.357.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.358.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5FbnRpdHlEZXZlbG9wbWVudExvYW5SZXBhaWQjY3RvcigyKTo2ODU
scope.358.kind=method
scope.358.startLine=1
scope.358.endLine=835
scope.358.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.359.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5GaW5hbEFnZSNjdG9yKDIpOjgyOA
scope.359.kind=method
scope.359.startLine=1
scope.359.endLine=835
scope.359.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.360.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5GaW5hbEJhbGFuY2UjY3RvcigyKTo4MjU
scope.360.kind=method
scope.360.startLine=1
scope.360.endLine=835
scope.360.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.361.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Hb3Zlcm5tZW50QmFsYW5jZSNjdG9yKDEpOjgyMg
scope.361.kind=method
scope.361.startLine=1
scope.361.endLine=835
scope.361.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.362.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZUJ1aWx0I2N0b3IoMyk6NzM4
scope.362.kind=method
scope.362.startLine=1
scope.362.endLine=835
scope.362.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.363.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Ib3VzZVNvbGQjY3RvcigzKTo3NDE
scope.363.kind=method
scope.363.startLine=1
scope.363.endLine=835
scope.363.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.364.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbmhlcml0ZWQjY3RvcigzKTo3NTA
scope.364.kind=method
scope.364.startLine=1
scope.364.endLine=835
scope.364.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.365.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlUm9sbCNjdG9yKDIpOjYwNg
scope.365.kind=method
scope.365.startLine=1
scope.365.endLine=835
scope.365.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.366.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Jbml0aWF0aXZlV29uI2N0b3IoMSk6NjA5
scope.366.kind=method
scope.366.startLine=1
scope.366.endLine=835
scope.366.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.367.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsQ2FyZFVzZWQjY3RvcigxKTo3OTU
scope.367.kind=method
scope.367.startLine=1
scope.367.endLine=835
scope.367.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.368.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRG91Ymxlc1JvbGxlZCNjdG9yKDEpOjc5OA
scope.368.kind=method
scope.368.startLine=1
scope.368.endLine=835
scope.368.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.369.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRW50ZXJlZCNjdG9yKDIpOjc4OQ
scope.369.kind=method
scope.369.startLine=1
scope.369.endLine=835
scope.369.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.370.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsRmluZVBhaWQjY3RvcigyKTo3OTI
scope.370.kind=method
scope.370.startLine=1
scope.370.endLine=835
scope.370.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.371.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5KYWlsU3RheWVkI2N0b3IoMSk6ODAx
scope.371.kind=method
scope.371.startLine=1
scope.371.endLine=835
scope.371.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.372.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU2FsZVJlZnVzZWQjY3Rvcig0KTo3NTk
scope.372.kind=method
scope.372.startLine=1
scope.372.endLine=835
scope.372.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.373.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MYW5kU29sZCNjdG9yKDQpOjc1Ng
scope.373.kind=method
scope.373.startLine=1
scope.373.endLine=835
scope.373.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.374.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eURpdmlkZW5kUGFpZCNjdG9yKDMpOjcxMw
scope.374.kind=method
scope.374.startLine=1
scope.374.endLine=835
scope.374.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.375.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUZvcm1lZCNjdG9yKDIpOjcwNA
scope.375.kind=method
scope.375.startLine=1
scope.375.endLine=835
scope.375.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.376.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUhvdXNlQnVpbHQjY3RvcigzKTo3MjU
scope.376.kind=method
scope.376.startLine=1
scope.376.endLine=835
scope.376.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.377.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUxpcXVpZGF0ZWQjY3RvcigzKTo3MTk
scope.377.kind=method
scope.377.startLine=1
scope.377.endLine=835
scope.377.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.378.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUxvYW5SYWlzZWQjY3RvcigzKTo3MDc
scope.378.kind=method
scope.378.startLine=1
scope.378.endLine=835
scope.378.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.379.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eUxvYW5SZXBhaWQjY3Rvcig0KTo3MTA
scope.379.kind=method
scope.379.startLine=1
scope.379.endLine=835
scope.379.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.380.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eVJlbnRQYWlkI2N0b3IoNCk6NzIy
scope.380.kind=method
scope.380.startLine=1
scope.380.endLine=835
scope.380.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.381.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5MZWdhbEVudGl0eVNoYXJlU29sZCNjdG9yKDQpOjcxNg
scope.381.kind=method
scope.381.startLine=1
scope.381.endLine=835
scope.381.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.382.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUtlcHQjY3RvcigzKTo3NTM
scope.382.kind=method
scope.382.startLine=1
scope.382.endLine=835
scope.382.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.383.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZUxpZnRlZCNjdG9yKDQpOjc0Nw
scope.383.kind=method
scope.383.startLine=1
scope.383.endLine=835
scope.383.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.384.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3J0Z2FnZWQjY3RvcigzKTo3NDQ
scope.384.kind=method
scope.384.startLine=1
scope.384.endLine=835
scope.384.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.385.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDMpOjYyNw
scope.385.kind=method
scope.385.startLine=627
scope.385.endLine=629
scope.385.semanticHash=a25dcf65a363730c6f293f8a1f1404f79f6c1932a440cc31c1262695a9baa056
scope.386.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNjdG9yKDUpOjYyNg
scope.386.kind=method
scope.386.startLine=1
scope.386.endLine=835
scope.386.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.387.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Nb3ZlZCNvZmZpY2lhbFNwYWNlQXQoMSk6NjMx
scope.387.kind=method
scope.387.startLine=631
scope.387.endLine=633
scope.387.semanticHash=d857123e25d1bd7ad9e99a5f83a2cc20dc70a077e141b0d2f4b1de0cd88b32ac
scope.388.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QZWVyVHJhZGUjY3Rvcig0KTo2NDc
scope.388.kind=method
scope.388.startLine=1
scope.388.endLine=835
scope.388.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.389.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QbGF5ZXJQYWlkI2N0b3IoMyk6NzM1
scope.389.kind=method
scope.389.startLine=1
scope.389.endLine=835
scope.389.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.390.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5QdXJjaGFzZURlY2xpbmVkI2N0b3IoNSk6NzI4
scope.390.kind=method
scope.390.startLine=1
scope.390.endLine=835
scope.390.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.391.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5SZW50UGFpZCNjdG9yKDQpOjczMg
scope.391.kind=method
scope.391.startLine=1
scope.391.endLine=835
scope.391.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.392.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5SZW50UmVsaWVmRW5hYmxlZCNjdG9yKDEpOjgxNg
scope.392.kind=method
scope.392.startLine=1
scope.392.endLine=835
scope.392.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.393.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Sb2xsZWQjY3RvcigyKTo2MjM
scope.393.kind=method
scope.393.startLine=1
scope.393.endLine=835
scope.393.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.394.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TYWxhcnlDb2xsZWN0ZWQjY3RvcigyKTo2MzY
scope.394.kind=method
scope.394.startLine=1
scope.394.endLine=835
scope.394.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.395.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TcGxpdE1vbm9wb2x5UGFpZCNjdG9yKDMpOjcwMQ
scope.395.kind=method
scope.395.startLine=1
scope.395.endLine=835
scope.395.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.396.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TcGxpdE1vbm9wb2x5V29uI2N0b3IoMik6Njk4
scope.396.kind=method
scope.396.startLine=1
scope.396.endLine=835
scope.396.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.397.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFsZW1hdGUjY3RvcigwKTo4MDc
scope.397.kind=method
scope.397.startLine=1
scope.397.endLine=835
scope.397.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.398.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFsZW1hdGVUcmFkaW5nI2N0b3IoMSk6NjUw
scope.398.kind=method
scope.398.startLine=1
scope.398.endLine=835
scope.398.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.399.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdGFydCNjdG9yKDEpOjYwMg
scope.399.kind=method
scope.399.startLine=1
scope.399.endLine=835
scope.399.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.400.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdHJhdGVneU5hbWVkI2N0b3IoNCk6Njkz
scope.400.kind=method
scope.400.startLine=693
scope.400.endLine=695
scope.400.semanticHash=73cbe69a0658916dd39c96ca2abdc2693291c41bcb145af34b2b5e8731ca92b8
scope.401.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5TdHJhdGVneU5hbWVkI2N0b3IoNSk6Njkx
scope.401.kind=method
scope.401.startLine=1
scope.401.endLine=835
scope.401.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.402.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDIpOjYxNA
scope.402.kind=method
scope.402.startLine=614
scope.402.endLine=616
scope.402.semanticHash=4ee4b3a29bce9772f978446cb55e21f8821dbf401952e6475e372a345ad46138
scope.403.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDMpOjYxOA
scope.403.kind=method
scope.403.startLine=618
scope.403.endLine=620
scope.403.semanticHash=1641f6f5ec3c77f0ec23bfd9fd1bc1ed7e1aeeb17c4bcba8f17a40b4ad21df48
scope.404.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5UdXJuU3RhcnRlZCNjdG9yKDQpOjYxMw
scope.404.kind=method
scope.404.startLine=1
scope.404.endLine=835
scope.404.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.405.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5XYXJQcm9maXRzVGF4RW5hYmxlZCNjdG9yKDEpOjgxMw
scope.405.kind=method
scope.405.startLine=1
scope.405.endLine=835
scope.405.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.406.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5XYXJQcm9maXRzVGF4UGFpZCNjdG9yKDIpOjgxOQ
scope.406.kind=method
scope.406.startLine=1
scope.406.endLine=835
scope.406.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.407.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5Xb24jY3RvcigxKTo4MzE
scope.407.kind=method
scope.407.startLine=1
scope.407.endLine=835
scope.407.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.408.id=bWV0aG9kOkdhbWUuSm91cm5hbC5FbnRyeS5ZZWFyTGltaXRSZWFjaGVkI2N0b3IoMCk6ODEw
scope.408.kind=method
scope.408.startLine=1
scope.408.endLine=835
scope.408.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
scope.409.id=bWV0aG9kOkdhbWUuUmVzdWx0I2N0b3IoNCk6NTc5
scope.409.kind=method
scope.409.startLine=1
scope.409.endLine=835
scope.409.semanticHash=94145ac654a4e2d193844b2eb984e85ac0c94e19fcf17310221238d44f10c508
*/
