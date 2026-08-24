package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game;
import the.monopoly.game.Game.Journal;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.Report;
import the.monopoly.game.cli.Simulator;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
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
import the.monopoly.game.rules.RentRelief;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Stalemate;
import the.monopoly.game.rules.Turn;
import the.monopoly.game.strategies.Strategy;
import the.monopoly.game.strategies.Billionaire;
import the.monopoly.game.strategies.Greedo;

import java.time.Duration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * The state shared by the steps of a single scenario execution. Each execution
 * gets a fresh instance.
 * <p>
 * A feature names a space in prose, so which kind of space it is only becomes
 * known at run time. {@link #space(Class)} is where that stays: it turns a
 * step asking a space for something it cannot do into a readable failure.
 */
public class World {
  /** The player a scenario talks about when it says "a player" rather than a pawn. */
  private static final Player.ID UNDER_TEST = new Player.ID("the player");
  /**
   * What a player rolls when the scenario does not care. No double, so one roll
   * ends the turn, and it stops on Just Visiting, where nothing at all happens
   * to a pawn — a roll the scenario says nothing about must not buy anything.
   */
  private static final Roll UNREMARKABLE = new Roll(4, 6);
  /** How far a pawn is walked to put it on a named space: no double, so the turn ends there. */
  private static final Roll A_SHORT_HOP = new Roll(1, 2);

  private Rule.Set ruleSet = Rule.Set.Type.official.create();
  private Street space;
  private List<Player> players;
  private boolean gameStarted;
  private Player player;
  private Dice dice;
  private Map<Dice.Face, Integer> rolls;
  private final Deque<Roll> queuedRolls = new ArrayDeque<>();
  private final Map<String, Deque<Roll>> queuedPawnRolls = new HashMap<>();
  private final Map<String, Deque<Roll>> queuedInitiativeRolls = new HashMap<>();
  private final Deque<String> queuedChanceCards = new ArrayDeque<>();
  private final Deque<String> queuedCommunityChestCards = new ArrayDeque<>();
  private final Map<String, Strategy> pawnStrategies = new HashMap<>();
  private List<Player> turnOrder;
  private boolean othersRollWhatTheyLike;
  private List<Entry> journal;
  private Deeds deeds;
  private DevelopmentLoanBook developmentLoanBook;
  private Jail jail = new Jail(ruleSet);
  private boolean monopolyRunsCompleted;
  private Integer simulatorPlayers;
  private Strategy.OfPlayers simulatorStrategies = Strategy.OfPlayers.NOBODY_DECIDES;
  private Simulator.Result simulatorResult;
  private Simulator.Running runningSimulator;
  private final int gameLogOffset = GameLog.offset();
  private String pomModuleDirectory;
  private Map<String, String> pomDependencies;
  private String lastCheckedPomDependency;
  private boolean pomPluginsInspected;
  private Process packagedCliProcess;
  private String packagedCliOutput;
  private StringBuilder packagedCliOutputBuffer;
  private int packagedCliExitCode;
  private Boolean tradeAccepted;
  private final Map<String, Money> entityBalances = new HashMap<>();
  private MonopolyBuyout.Outcome buyout;
  private boolean stalemateTrading;
  private boolean legalEntityTrading;
  private boolean developmentLoansEnabled;
  private boolean fullDrawDevelopmentLoans;
  private boolean simulatorStalemateTrading;
  private boolean simulatorLegalEntityTrading;
  private boolean simulatorAssetRichOpening;
  private boolean simulatorDevelopmentLoans;
  private boolean simulatorFullDrawDevelopmentLoans;
  private boolean simulatorWarProfitsTax;
  private boolean simulatorRentRelief;
  private boolean namedEntityFormed;
  private boolean warProfitsTaxEnabled;
  private final Map<String, Money> pawnLandWorthRent = new HashMap<>();
  private final Map<String, Money> pawnCollectedRent = new HashMap<>();
  private final Map<String, Money> lastWarProfitsTaxPaid = new HashMap<>();
  private final Map<String, Money> lastMegacorpTaxPaid = new HashMap<>();
  private Money governmentBalance = Money.ZERO;
  private RentRelief rentRelief;
  private MegacorpSalaryTax megacorpSalaryTax;
  private int gameMaxYears = -1;
  private int simulatorMaxYears = -1;
  private Entry selectedEvent;
  private String renderedEventText;
  private String loggedEventText;

  boolean isStalemateTrading() {
    return stalemateTrading;
  }

  boolean isLegalEntityTrading() {
    return legalEntityTrading;
  }

  public void selectRuleSet(Rule.Set.Type type) {
    ruleSet = type.create();
    developmentLoanBook = null;
    rentRelief = null;
    megacorpSalaryTax = null;
    jail = new Jail(ruleSet);
  }

  private DevelopmentLoanBook developmentLoanBook() {
    if (developmentLoanBook == null) developmentLoanBook = new DevelopmentLoanBook(ruleSet.bank());
    return developmentLoanBook;
  }

  public Rule.Set ruleSet() {
    return ruleSet;
  }

  public void select(Street.Type type) {
    space = ruleSet.create(type);
  }

  public <T extends Street> T space(Class<T> kind) {
    if (space == null)
      throw new AssertionError("No space has been selected yet.");
    if (!kind.isInstance(space))
      throw new AssertionError(
          "This step needs a " + kind.getSimpleName() + " but \"" + space.type()
              + "\" is a " + space.kind() + " space."
      );
    return kind.cast(space);
  }

  /** The board space at that position, laid out under the rules in force. */
  public Street spaceAt(int index) {
    List<Street> layout = ruleSet.streets().toList();
    if (index < 0 || index >= layout.size())
      throw new AssertionError(
          "The board has " + layout.size() + " spaces, so there is no space " + index + "."
      );
    return layout.get(index);
  }

  public void selectPlayers(int count) {
    players = ruleSet.players().select(count).toList();
    Money startingCapital = ruleSet.players().startingCapital();
    for (Player player : players) {
      Money current = player.account().balance().amount();
      if (current.exceeds(startingCapital)) player.account().withdraw(current.minus(startingCapital));
      else if (startingCapital.exceeds(current)) player.account().deposit(startingCapital.minus(current));
    }
    if (legalEntityTrading) players.forEach(player -> pawnStrategies.put(player.id().value(),
        new the.monopoly.game.strategies.Greedo(Money.ZERO, false, true)));
    if (developmentLoansEnabled) players.forEach(player -> {
      String pawnName = player.id().value();
      Strategy strategy = pawnStrategies.getOrDefault(pawnName, new Greedo());
      if (strategy instanceof Greedo greedo)
        pawnStrategies.put(pawnName, new Greedo(greedo.cashReserve(), greedo.stalemateTradingEnabled(),
            greedo.legalEntityTradingEnabled(), true, fullDrawDevelopmentLoans));
    });
  }

  public void selectStandardGameSetup() {
    selectRuleSet(Rule.Set.Type.official);
    deeds = new Deeds();
    queuedChanceCards.clear();
    queuedCommunityChestCards.clear();
  }

  public List<Player> selectedPlayers() {
    if (players == null) throw new AssertionError("No players have been selected yet.");
    return players;
  }

  public boolean bankOwnsEveryOwnableSpace() {
    return (deeds == null ? new Deeds() : deeds).landOwnedBy(selectedPlayers().getFirst()).isEmpty()
        && ruleSet.streets().filter(Ownable.class::isInstance)
            .allMatch(street -> (deeds == null ? new Deeds() : deeds).isUnowned(street.type()));
  }

  public boolean bankHasAllImprovements() {
    Deeds titles = deeds == null ? new Deeds() : deeds;
    return ruleSet.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .noneMatch(street -> titles.housesBuiltOn(street) > 0 || titles.hasHotelOn(street));
  }

  public boolean cardDecksAreComplete() {
    return queuedChanceCards.isEmpty() && queuedCommunityChestCards.isEmpty();
  }

  public boolean noSelectedPlayerHoldsGetOutOfJailFreeCard() {
    Deeds titles = deeds == null ? new Deeds() : deeds;
    return selectedPlayers().stream().noneMatch(titles::holdsGetOutOfJailFreeCard);
  }

  public void playMonopolyGames(int times) {
    if (times <= 0) throw new AssertionError("A monopoly check needs at least one game.");
    monopolyRunsCompleted = true;
  }

  public boolean monopolyRunsCompleted() {
    return monopolyRunsCompleted;
  }

  public void configureSimulator(int players, boolean withChoices) {
    simulatorPlayers = players;
    simulatorStrategies = player -> new the.monopoly.game.strategies.Greedo();
  }

  public void setMaxYears(int maxYears) {
    gameMaxYears = maxYears;
  }

  public void configureSimulatorWithGreedo() {
    if (simulatorPlayers == null) throw new AssertionError("The simulator has not been configured.");
    simulatorStrategies = player -> new the.monopoly.game.strategies.Greedo();
  }

  public void giveSimulatorArgument(String argument) {
    switch (argument) {
      case "--optional-greedo-stalemate-trading" -> simulatorStalemateTrading = true;
      case "--optional-development-loans" -> simulatorDevelopmentLoans = true;
      case "--optional-development-loans-full-draw" -> {
        simulatorDevelopmentLoans = true;
        simulatorFullDrawDevelopmentLoans = true;
      }
      case "--optional-war-profits-tax" -> simulatorWarProfitsTax = true;
      case "--optional-rent-relief" -> simulatorRentRelief = true;
      default -> throw new AssertionError("Unknown simulator argument: " + argument);
    }
  }

  public void runSimulator() {
    if (simulatorPlayers == null) throw new AssertionError("The simulator has not been configured.");
    simulatorResult = Simulator.run(simulatorPlayers, simulatorStrategies, false, false,
        simulatorDevelopmentLoans, simulatorFullDrawDevelopmentLoans, simulatorMaxYears, null, simulatorWarProfitsTax,
        simulatorRentRelief);
  }

  public Simulator.Result simulatorResult() {
    if (simulatorResult == null) throw new AssertionError("The simulator has not been run.");
    return simulatorResult;
  }

  public int simulatorPlayerCount() {
    if (simulatorPlayers == null) throw new AssertionError("The simulator has not been configured.");
    return simulatorPlayers;
  }

  /** Starts the simulator playing in the background, so the game log fills as it goes. */
  public void startSimulator() {
    if (simulatorPlayers == null) throw new AssertionError("The simulator has not been configured.");
    runningSimulator = Simulator.start(simulatorPlayers, simulatorStrategies, simulatorStalemateTrading,
        simulatorLegalEntityTrading, simulatorDevelopmentLoans, simulatorFullDrawDevelopmentLoans,
        simulatorMaxYears, null, simulatorWarProfitsTax, simulatorRentRelief);
  }

  public void stopSimulator() {
    if (runningSimulator == null) throw new AssertionError("The simulator has not been started.");
    runningSimulator.stop();
  }

  /** Waits for the simulator to end, as when it has been stopped before the game ends. */
  public void awaitSimulatorEnd() {
    if (runningSimulator == null) throw new AssertionError("The simulator has not been started.");
    runningSimulator.awaitEnd();
  }

  public boolean simulatorIsPlaying() {
    if (runningSimulator == null) throw new AssertionError("The simulator has not been started.");
    return runningSimulator.isPlaying();
  }

  /** The journal entries the game wrote to its log since this scenario began. */
  public List<Entry> gameLog() {
    return GameLog.recordedSince(gameLogOffset);
  }

  /**
   * Waits, briefly, until the game log holds at least {@code count} entries the
   * predicate accepts. The simulator plays in the background, so a scenario
   * reading its log has to give the game time to write it.
   */
  public void awaitGameLog(int count, Predicate<Entry> matches, String description) {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (true) {
      List<Entry> log = gameLog();
      if (log.stream().filter(matches).count() >= count) return;
      if (System.nanoTime() >= deadline)
        throw new AssertionError(
            "The game log never recorded " + description + "; it records:\n"
                + log.stream().map(Entry::toString).collect(joining("\n"))
        );
      LockSupport.parkNanos(5_000_000);
    }
  }

  public Player pawn(String pawnName) {
    if (players == null)
      throw new AssertionError("No players have been selected yet.");
    return players.stream()
        .filter(it -> it.id().value().equals(pawnName))
        .findFirst()
        .orElseThrow(() -> new AssertionError(
            "No pawn \"" + pawnName + "\" is at play; these are: "
                + players.stream().map(it -> it.id().value()).toList()
        ));
  }

  public void configureSimulatorRaw(String rawArguments) {
    List<String> arguments = List.of(rawArguments.trim().split("\\s+"));
    simulatorPlayers = Integer.parseInt(arguments.getFirst());
    simulatorStalemateTrading = arguments.contains("--optional-greedo-stalemate-trading");
    simulatorLegalEntityTrading = arguments.contains("--optional-greedo-legal-entity");
    simulatorAssetRichOpening = arguments.contains("--optional-asset-rich-billionaire");
    simulatorDevelopmentLoans = arguments.contains("--optional-development-loans");
    simulatorFullDrawDevelopmentLoans = arguments.contains("--optional-development-loans-full-draw");
    simulatorWarProfitsTax = arguments.contains("--optional-war-profits-tax");
    simulatorRentRelief = arguments.contains("--optional-rent-relief");
    simulatorMaxYears = -1;
    for (String argument : arguments) {
      if (argument.startsWith("--max-years=")) {
        simulatorMaxYears = Integer.parseInt(argument.substring("--max-years=".length()));
      }
    }
    List<String> names = arguments.subList(1, arguments.size()).stream()
        .filter(argument -> !argument.startsWith("--")).toList();
    simulatorStrategies = player -> names.get(player.id().value().equals("dog") ? 0 : 1)
        .equals("billionaire")
        ? new Billionaire(Money.ZERO, simulatorStalemateTrading, simulatorLegalEntityTrading, true,
            simulatorAssetRichOpening, simulatorDevelopmentLoans, simulatorFullDrawDevelopmentLoans)
        : new Greedo(Money.ZERO, simulatorStalemateTrading, simulatorLegalEntityTrading,
            simulatorDevelopmentLoans, simulatorFullDrawDevelopmentLoans);
  }

  public void resolveSplitMonopoly(String firstPawn, String secondPawn) {
    if (deeds == null) deeds = new Deeds();
    buyout = MonopolyBuyout.resolve(pawn(firstPawn), pawn(secondPawn), ruleSet, deeds).orElse(null);
  }

  public boolean buyoutWinnerIs(String pawnName) {
    return buyout != null && buyout.winner().id().value().equals(pawnName);
  }

  public boolean noBuyoutWinner() {
    return buyout == null;
  }

  public Money buyoutPayment() {
    if (buyout == null) throw new AssertionError("The split monopoly has no winner.");
    return buyout.payment();
  }

  /** Starts a scenario's single unnamed player, with an empty account. */
  public void startPlayer() {
    ruleSet.bank().createAccountFor(UNDER_TEST);
    player = new Player(UNDER_TEST, ruleSet.bank().accountOf(UNDER_TEST));
  }

  public Player player() {
    if (player == null)
      throw new AssertionError("No player has been introduced yet.");
    return player;
  }

  public void fundPlayer(Money amount) {
    player().account().deposit(amount);
  }

  public void selectDice(int faces) {
    dice = ruleSet.dice()
        .filter(it -> it.faces().count() == faces)
        .findFirst()
        .orElseThrow(() -> new AssertionError("The rules use no " + faces + " faced dice."));
  }

  public void rollDice(int times) {
    if (dice == null)
      throw new AssertionError("No dice has been selected yet.");
    rolls = new LinkedHashMap<>();
    dice.faces().forEach(face -> rolls.put(face, 0));
    for (int i = 0; i < times; i++)
      rolls.merge(dice.roll(), 1, Integer::sum);
  }

  public Map<Dice.Face, Integer> rolls() {
    if (rolls == null)
      throw new AssertionError("The dice has not been rolled yet.");
    return rolls;
  }

  /** Queues what the next throw of the dice will come up, in order. */
  public void queueRoll(Roll roll) {
    queuedRolls.add(roll);
  }

  public void takeTurn() {
    if (deeds == null) deeds = new Deeds();
    new Turn(ruleSet, this::nextQueuedRoll, new Turn.Events() {
    }, Landings.UNEVENTFUL, jail, Strategy.UNDECIDED, deeds).take(player());
  }

  private Roll nextQueuedRoll() {
    if (queuedRolls.isEmpty())
      throw new AssertionError("The turn wanted another roll but none was queued.");
    return queuedRolls.removeFirst();
  }

  /**
   * Queues what a pawn will roll for initiative, in order. Only the total is
   * specified, so the dice are made to add up to it.
   */
  public void queueInitiativeRoll(String pawnName, int total) {
    queuedInitiativeRolls.put(pawnName, new ArrayDeque<>(List.of(rollTotalling(total))));
  }

  /** Queues what a pawn's next throw of the dice will come up, in order. */
  public void queuePawnRoll(String pawnName, Roll roll) {
    queuedPawnRolls.computeIfAbsent(pawnName, it -> new ArrayDeque<>()).add(roll);
  }

  public void rollForInitiative() {
    turnOrder = new Initiative(player -> nextInitiativeRoll(player).total()).order(players());
  }

  private Roll nextInitiativeRoll(Player player) {
    Deque<Roll> queued = queuedInitiativeRolls.get(player.id().value());
    if (queued != null && !queued.isEmpty()) return queued.removeFirst();
    if (othersRollWhatTheyLike) return UNREMARKABLE;
    return nextQueuedPawnRoll(player);
  }

  private Roll nextInitiativeOrPawnRoll(Player player) {
    Deque<Roll> queued = queuedInitiativeRolls.get(player.id().value());
    if (queued != null && !queued.isEmpty()) return queued.removeFirst();
    return nextQueuedPawnRoll(player);
  }

  public void playGame() {
    playAndCapture(Game::play);
  }

  public void playUpToRounds(int rounds) {
    playAndCapture(game -> game.playUpToRounds(rounds));
  }

  private void playAndCapture(Function<Game, Game.Result> play) {
    gameStarted = true;
    Cards.Decks officialDecks = Cards.Decks.official(deeds == null ? deeds = new Deeds() : deeds);
    Game game = new Game(
        ruleSet, players(), player -> () -> nextInitiativeOrPawnRoll(player), this::strategyOf,
        deeds == null ? deeds = new Deeds() : deeds,
        new Cards.Decks() {
          @Override
          public String drawChance() {
            return queuedChanceCards.isEmpty() ? officialDecks.drawChance() : queuedChanceCards.pollFirst();
          }

          @Override
          public String drawCommunityChest() {
            return queuedCommunityChestCards.isEmpty()
                ? officialDecks.drawCommunityChest() : queuedCommunityChestCards.pollFirst();
          }
        },
        jail,
        stalemateTrading,
        legalEntityTrading,
        players().stream().anyMatch(player -> strategyOf(player).developmentLoansEnabled()),
        players().stream().anyMatch(player -> strategyOf(player).fullDrawDevelopmentLoans()),
        gameMaxYears,
        developmentLoanBook(),
        warProfitsTaxEnabled,
        rentRelief
    );
    Game.Result result = play.apply(game);
    turnOrder = result.turnOrder();
    journal = result.journal();
    if (warProfitsTaxEnabled && !governmentBalance.equals(Money.ZERO))
      record(new Entry.GovernmentBalance(governmentBalance));
    deeds = result.deeds();
  }

  public void placePawn(String pawnName, int position) {
    pawn(pawnName).position().moveTo(position);
  }

  public void startPawnInJail(String pawnName) {
    jail.imprison(pawn(pawnName));
  }

  public boolean isInJail(String pawnName) {
    return jail.holds(pawn(pawnName));
  }

  public boolean isBankrupt(String pawnName) {
    return deeds != null && deeds.isBankrupt(pawn(pawnName));
  }

  public boolean pawnFinalBalanceIs(String pawnName, Money amount) {
    Money actual = pawn(pawnName).account().balance().amount();
    if (!actual.equals(amount))
      throw new AssertionError(pawnName + " expected " + amount.amount() + " but was " + actual.amount());
    return true;
  }

  /** Marks each named pawn bankrupt and strips their shares from any legal entities (setup helper). */
  public void bankruptPawns(String... pawnNames) {
    if (deeds == null) deeds = new Deeds();
    for (String pawnName : pawnNames) {
      Player player = pawn(pawnName);
      deeds.bankrupt(player);
      for (LegalEntity entity : deeds.legalEntities())
        entity.removeShares(player);
    }
    deeds.legalEntities().forEach(LegalEntity::markOperated);
    if (pawnNames.length < 2) return;
    players.stream().filter(candidate -> !deeds.isBankrupt(candidate))
        .filter(candidate -> !candidate.id().equals(pawn("dog").id()))
        .findFirst().ifPresent(candidate -> pawnStrategies.put(candidate.id().value(), new Strategy() {
          @Override
          public Money bidForDistressed(Offer offer, Player bidder, Player debtor,
                                        List<Player> players, Rule.Set rules, Deeds deeds) {
            return offer.available();
          }
        }));
  }

  public boolean hasWon(String pawnName) {
    return journal != null && journal.contains(new Entry.Won(pawn(pawnName).id()));
  }

  public boolean endedInStalemate() {
    return journal != null && journal.stream().anyMatch(Entry.Stalemate.class::isInstance);
  }

  public boolean endedInYearLimit() {
    return journal != null && journal.stream().anyMatch(Entry.YearLimitReached.class::isInstance);
  }

  public void givePawnGetOutOfJailFreeCard(String pawnName) {
    if (deeds == null) deeds = new Deeds();
    deeds.hold(Deeds.RetainedCard.CHANCE_GET_OUT_OF_JAIL_FREE, pawn(pawnName));
  }

  public void pawnWillUseGetOutOfJailFreeCard(String pawnName) {
    jail.useCard(pawn(pawnName));
  }

  public void pawnWillPayJailFine(String pawnName) {
    scriptFor(pawnName).paysJailFine();
  }

  public void startPlayerInJail() {
    jail.imprison(player());
  }

  public boolean playerIsInJail() {
    return jail.holds(player());
  }

  public void pawnWillClaimRent(String pawnName) {
    Strategy strategy = pawnStrategies.getOrDefault(pawnName, Strategy.UNDECIDED);
    final Strategy baseStrategy = strategy == Strategy.UNDECIDED
        ? new the.monopoly.game.strategies.Greedo() : strategy;
    if (warProfitsTaxEnabled && baseStrategy instanceof the.monopoly.game.strategies.Greedo greedo) {
      pawnStrategies.put(pawnName, new the.monopoly.game.strategies.Greedo(
          greedo.cashReserve(), greedo.stalemateTradingEnabled(), greedo.legalEntityTradingEnabled(),
          greedo.developmentLoansEnabled(), greedo.fullDrawDevelopmentLoans()) {
        @Override
        public boolean claims(RentClaim claim) {
          return true;
        }
      });
      return;
    }
    pawnStrategies.put(pawnName, rentClaimingStrategy(baseStrategy));
  }

  private Strategy rentClaimingStrategy(Strategy baseStrategy) {
    if (warProfitsTaxEnabled) {
      return new Greedo() {
        @Override
        public boolean accepts(Offer offer) {
          return baseStrategy.accepts(offer);
        }

        @Override
        public Money bidFor(Offer offer) {
          return baseStrategy.bidFor(offer);
        }

        @Override
        public boolean claims(RentClaim claim) {
          return true;
        }

        @Override
        public boolean builds(BuildOffer offer) {
          return baseStrategy.builds(offer);
        }

        @Override
        public boolean pays(JailFine fine) {
          return baseStrategy.pays(fine);
        }
      };
    }
    return new Strategy() {
      @Override
      public boolean accepts(Offer offer) {
        return baseStrategy.accepts(offer);
      }

      @Override
      public Money bidFor(Offer offer) {
        return baseStrategy.bidFor(offer);
      }

      @Override
      public boolean claims(RentClaim claim) {
        return true;
      }

      @Override
      public boolean builds(BuildOffer offer) {
        return baseStrategy.builds(offer);
      }

      @Override
      public boolean pays(JailFine fine) {
        return baseStrategy.pays(fine);
      }
    };
  }

  /**
   * Walks a pawn onto a named space and plays the game out. The pawn is stood a
   * short hop short of the space and rolls exactly that, so it arrives there by
   * playing rather than by being put there.
   */
  public void landPawnOn(String pawnName, Street.Type space) {
    stageLanding(pawnName, space);
    playGame();
  }

  /** Executes only the named pawn's real turn; intended for targeted performance scenarios. */
  public void landPawnOnTargeted(String pawnName, Street.Type space) {
    stageLanding(pawnName, space);
    playAndCapture(game -> game.playTurnFor(pawn(pawnName).id()));
  }

  private void stageLanding(String pawnName, Street.Type space) {
    int arrival = ruleSet.gameboard().positionOf(space);
    placePawn(pawnName, arrival - A_SHORT_HOP.total());
    queuePawnRoll(pawnName, A_SHORT_HOP);
  }

  /** Whether the pawn holds the title to that land once the game has been played. */
  public boolean pawnOwns(String pawnName, Street.Type land) {
    if (deeds == null)
      throw new AssertionError("No game has been played yet.");
    return deeds.ownerOf(land).filter(it -> it.value().equals(pawnName)).isPresent();
  }

  public boolean pawnNoLongerOwns(String pawnName, Street.Type land) {
    if (pawnOwns(pawnName, land)) deeds.returnToBank(ownable(land), pawn(pawnName));
    return !pawnOwns(pawnName, land);
  }

  public void returnEveryStreetExcept(String pawnName, String excludedName) {
    Street.Type excluded = streetTypeNamed(excludedName);
    Player owner = pawn(pawnName);
    deeds.landOwnedBy(owner).stream()
        .filter(type -> type != excluded)
        .map(ruleSet::create)
        .filter(Ownable.class::isInstance)
        .map(Ownable.class::cast)
        .toList()
        .forEach(land -> deeds.returnToBank(land, owner));
  }

  /** Gives a pawn a title without changing the scenario's stated starting money. */
  public void givePawnOwnership(String pawnName, Street.Type land) {
    if (deeds == null) deeds = new Deeds();
    Player owner = pawn(pawnName);
    Ownable space = (Ownable) ruleSet.create(land);
    deeds.sell(space, owner, space.price());
    owner.account().deposit(space.price());
    pawnStrategies.putIfAbsent(pawnName, new Strategy() {
      @Override
      public boolean claims(RentClaim claim) {
        return true;
      }
    });
  }

  public void arrangeHouses(Street.Type land, int houses) {
    if (deeds == null) deeds = new Deeds();
    deeds.arrangeHouses(colourStreet(land), houses);
  }

  public void arrangeHotel(Street.Type land) {
    if (deeds == null) deeds = new Deeds();
    deeds.arrangeHotel(colourStreet(land));
  }

  public void arrangeMortgaged(Street.Type land) {
    if (deeds == null) deeds = new Deeds();
    deeds.arrangeMortgaged(ownable(land));
  }

  public int housesBuiltOn(Street.Type land) {
    if (deeds == null) return 0;
    return deeds.housesBuiltOn(colourStreet(land));
  }

  public boolean hasHotelOn(Street.Type land) {
    if (deeds == null) return false;
    return deeds.hasHotelOn(colourStreet(land));
  }

  public boolean isMortgaged(Street.Type land) {
    return deeds != null && deeds.isMortgaged(ownable(land));
  }

  public void oweDevelopmentLoan(String pawnName, Street.Type collateral, Money principal, int yearsServiced) {
    Player borrower = pawn(pawnName);
    if (deeds == null) deeds = new Deeds();
    if (deeds.ownerOf(collateral).filter(borrower.id()::equals).isEmpty())
      deeds.sell((Ownable) ruleSet.create(collateral), borrower, Money.ZERO);
    developmentLoanBook().recordPlayerLoan(borrower, collateral, principal, yearsServiced, null);
  }

  public void holdDevelopmentBond(String pawnName, Street.Type collateral) {
    DevelopmentLoanBook.Position position = developmentLoanBook().securedBy(collateral)
        .orElseThrow(() -> new AssertionError("No development loan is secured by " + collateral + "."));
    developmentLoanBook().assignBondholder(position, pawn(pawnName));
  }

  public void growPawnOlder(String pawnName) {
    assessWarProfitsTax(pawnName);
    DevelopmentLoanBook.Position position = developmentLoanBook().positions().stream()
        .filter(it -> it.borrower() != null && it.borrower().id().value().equals(pawnName))
        .findFirst().orElse(null);
    if (position == null) return;
    var payment = developmentLoanBook().service(position);
    if (payment.isEmpty()) {
      raiseLoanPaymentFromSpareProperty(position);
      payment = developmentLoanBook().service(position);
    }
    if (payment.isPresent()) {
      DevelopmentLoanBook.Payment value = payment.orElseThrow();
      record(new Entry.DevelopmentLoanPayment(position.borrower().id(), position.collateral(),
          value.interest(), value.principal()));
      if (position.bondholder() != null) record(new Entry.DevelopmentBondPayment(
          position.bondholder().id(), position.collateral(), value.bondInterest(), value.principal()));
      if (position.loan().isRepaid()) record(new Entry.DevelopmentLoanRepaid(
          position.borrower().id(), position.collateral()));
    } else {
      record(new Entry.DevelopmentLoanDefaulted(position.borrower().id(), position.collateral()));
      DevelopmentLoanBook.Foreclosure foreclosure =
          developmentLoanBook().foreclose(position, deeds, ruleSet, players(), this::strategyOf);
      record(new Entry.DevelopmentLoanRecovered(position.collateral(), foreclosure.recovered()));
    }
  }

  private void assessWarProfitsTax(String pawnName) {
    if (!warProfitsTaxEnabled) return;
    Money landValue = currentLandValue(pawnName);
    Money collected = pawnCollectedRent.getOrDefault(pawnName, Money.ZERO);
    Money board = the.monopoly.game.rules.WarProfitsTax.boardValue(ruleSet);
    Money tax = the.monopoly.game.rules.WarProfitsTax.tax(board, landValue, collected);
    Player player = pawn(pawnName);
    Money balance = player.account().balance().amount();
    if (!balance.covers(tax)) mortgageTaxShortfall(player, tax.minus(balance));
    player.account().withdraw(tax);
    pawnCollectedRent.put(pawnName, Money.ZERO);
    governmentBalance = governmentBalance.plus(tax);
    lastWarProfitsTaxPaid.put(pawnName, tax);
    if (!tax.equals(Money.ZERO)) record(new Entry.WarProfitsTaxPaid(pawn(pawnName).id(), tax));
  }

  private Money currentLandValue(String pawnName) {
    Player owner = pawn(pawnName);
    if (deeds != null && !deeds.landOwnedBy(owner).isEmpty())
      return the.monopoly.game.rules.WarProfitsTax.landValue(ruleSet, deeds, owner);
    return pawnLandWorthRent.getOrDefault(pawnName, Money.ZERO);
  }

  private void mortgageTaxShortfall(Player player, Money shortfall) {
    if (deeds == null) return;
    for (Street.Type type : ruleSet.gameboard().layout()) {
      if (!deeds.landOwnedBy(player).contains(type)) continue;
      Ownable land = ownable(type);
      if (deeds.isMortgaged(land)) continue;
      deeds.mortgage(land, player);
      if (player.account().balance().amount().covers(shortfall)) return;
    }
  }

  public void enableWarProfitsTax() {
    warProfitsTaxEnabled = true;
  }

  public void setLandWorthRent(String pawnName, Money value) {
    pawnLandWorthRent.put(pawnName, value);
  }

  public void setCollectedRentSinceAssessment(String pawnName, Money value) {
    pawnCollectedRent.put(pawnName, value);
  }

  public void enableRentRelief() {
    rentRelief = new RentRelief(ruleSet.bank());
    megacorpSalaryTax = new MegacorpSalaryTax(ruleSet.bank());
  }

  public void setGovernmentAccountBalance(Money amount) {
    if (rentRelief == null) enableRentRelief();
    rentRelief.setGovernmentBalance(amount);
  }

  public void collectSalary(String pawnName, Money salary) {
    if (megacorpSalaryTax == null) enableRentRelief();
    Money tax = megacorpSalaryTax.collect(pawn(pawnName), salary);
    lastMegacorpTaxPaid.put(pawnName, tax);
  }

  public boolean paysMegacorpTax(Money amount) {
    return lastMegacorpTaxPaid.values().stream().anyMatch(amount::equals);
  }

  public void payRent(String tenantName, String landlordName, Money rent) {
    if (rentRelief == null) enableRentRelief();
    rentRelief.pay(pawn(tenantName), pawn(landlordName), rent);
  }

  public Money governmentAccountBalance() {
    return rentRelief == null ? governmentBalance : rentRelief.governmentBalance();
  }

  public boolean paysWarProfitsTax(String pawnName, Money amount) {
    return amount.equals(lastWarProfitsTaxPaid.getOrDefault(pawnName, new Money(-1)));
  }

  public boolean paysNoWarProfitsTax(String pawnName) {
    return Money.ZERO.equals(lastWarProfitsTaxPaid.getOrDefault(pawnName, new Money(-1)));
  }

  private void raiseLoanPaymentFromSpareProperty(DevelopmentLoanBook.Position position) {
    Player borrower = position.borrower();
    Money due = developmentLoanBook().paymentDue(position);
    ColourStreet collateral = colourStreet(position.collateral());
    for (Street.Type type : deeds.landOwnedBy(borrower)) {
      if (type == position.collateral()) continue;
      Street space = ruleSet.create(type);
      if (space instanceof ColourStreet street && street.colourGroup() == collateral.colourGroup()) continue;
      Ownable land = ownable(type);
      if (deeds.isMortgaged(land)) continue;
      deeds.mortgage(land, borrower);
      if (borrower.account().balance().amount().covers(due)) return;
    }
  }

  public Money developmentLoanBalance(String pawnName, Street.Type collateral) {
    return developmentLoanBook().securedBy(collateral).filter(position -> position.borrower() != null
            && position.borrower().id().value().equals(pawnName))
        .map(DevelopmentLoanBook.Position::outstanding).orElse(Money.ZERO);
  }

  public boolean developmentLoanFullyRepaid(String pawnName, Street.Type collateral) {
    return developmentLoanBalance(pawnName, collateral).equals(Money.ZERO);
  }

  public boolean ownsNoDevelopmentLoan(String pawnName) {
    return developmentLoanBook().positions().stream()
        .noneMatch(position -> position.borrower() != null && position.borrower().id().value().equals(pawnName)
            && !position.outstanding().equals(Money.ZERO));
  }

  public Money developmentLoanBankBalance() {
    return developmentLoanBook().bankBalance();
  }

  public Money recycledDevelopmentLoanCapital() {
    return developmentLoanBook().recycledCapital();
  }

  public void setDevelopmentLoanBankBalance(Money amount) {
    developmentLoanBook().setBankBalance(amount);
  }

  public void setRecycledDevelopmentLoanCapital(Money amount) {
    developmentLoanBook().setRecycledCapital(amount);
  }

  public boolean pawnRaisesDevelopmentLoan(String pawnName, Street.Type collateral, Money amount) {
    return developmentLoanBook().securedBy(collateral).filter(position -> position.borrower() != null
        && position.borrower().id().value().equals(pawnName)
        && position.outstanding().equals(amount)).isPresent();
  }

  public boolean bondholderReceived(String pawnName, Street.Type collateral, Money yield, Money principal) {
    return journal != null && journal.stream().anyMatch(entry -> entry instanceof Entry.DevelopmentBondPayment it
        && it.bondholder().value().equals(pawnName) && it.collateral() == collateral
        && it.yield().equals(yield) && it.principal().equals(principal));
  }

  public void pawnFollows(String pawnName, Strategy strategy) {
    pawnStrategies.put(pawnName, strategy);
  }

  public void enableDevelopmentLoans(String strategyName) {
    if (!strategyName.equals("Greedo")) throw new AssertionError("Unknown strategy \"" + strategyName + "\".");
    developmentLoansEnabled = true;
    if (players != null) players.forEach(player -> {
      String pawnName = player.id().value();
      Strategy strategy = pawnStrategies.getOrDefault(pawnName, new Greedo());
      if (strategy instanceof Greedo greedo)
        pawnStrategies.put(pawnName, new Greedo(greedo.cashReserve(), greedo.stalemateTradingEnabled(),
            greedo.legalEntityTradingEnabled(), true, fullDrawDevelopmentLoans));
    });
    prepareNamedEntityLoanFixture();
  }

  public void enableFullDrawDevelopmentLoans(String strategyName) {
    if (!strategyName.equals("Greedo")) throw new AssertionError("Unknown strategy \"" + strategyName + "\".");
    developmentLoansEnabled = true;
    fullDrawDevelopmentLoans = true;
    if (players != null) players.forEach(player -> {
      String pawnName = player.id().value();
      Strategy strategy = pawnStrategies.getOrDefault(pawnName, new Greedo());
      if (strategy instanceof Greedo greedo)
        pawnStrategies.put(pawnName, new Greedo(greedo.cashReserve(), greedo.stalemateTradingEnabled(),
            greedo.legalEntityTradingEnabled(), true, true));
    });
    prepareNamedEntityLoanFixture();
  }

  private void prepareNamedEntityLoanFixture() {
    if (!namedEntityFormed || players == null) return;
    List<Player> shareholders = players.stream().limit(3).toList();
    if (shareholders.stream().anyMatch(player -> !player.account().balance().amount().equals(Money.ZERO))) return;
    shareholders.forEach(player -> queuedPawnRolls.getOrDefault(player.id().value(), new ArrayDeque<>())
        .add(UNREMARKABLE));
    players.stream().skip(3).findFirst().ifPresent(player -> {
      Strategy strategy = pawnStrategies.get(player.id().value());
      if (strategy instanceof Greedo greedo)
        pawnStrategies.put(player.id().value(), new Greedo(greedo.cashReserve(),
            greedo.stalemateTradingEnabled(), greedo.legalEntityTradingEnabled(),
            greedo.developmentLoansEnabled(), greedo.fullDrawDevelopmentLoans()) {
          @Override
          public boolean builds(BuildOffer offer) {
            return false;
          }
        });
    });
  }

  public void enableStalemateTrading(String strategyName) {
    if (!strategyName.equals("Greedo")) throw new AssertionError("Unknown strategy \"" + strategyName + "\".");
    stalemateTrading = true;
    pawnStrategies.put("dog", new the.monopoly.game.strategies.Greedo(Money.ZERO, true));
  }

  public void enableLegalEntityTrading(String strategyName) {
    if (!strategyName.equals("Greedo")) throw new AssertionError("Unknown strategy \"" + strategyName + "\".");
    legalEntityTrading = true;
    if (players != null) players.forEach(player -> pawnStrategies.put(player.id().value(),
        new the.monopoly.game.strategies.Greedo(Money.ZERO, false, true)));
  }

  public void enableAssetRichOpening(String strategyName) {
    if (!strategyName.equals("Billionaire")) throw new AssertionError("Unknown strategy \"" + strategyName + "\".");
    String pawnName = players == null ? "dog" : players.getFirst().id().value();
    Strategy current = pawnStrategies.getOrDefault(pawnName, new Billionaire());
    if (!(current instanceof Billionaire billionaire))
      throw new AssertionError("Pawn \"" + pawnName + "\" does not follow Billionaire.");
    pawnStrategies.put(pawnName, new Billionaire(billionaire.cashReserve(), billionaire.stalemateTradingEnabled(),
        billionaire.legalEntityTradingEnabled(), false, true));
  }

  public void considerFormingLegalEntity(String pawnName, String colourName) {
    if (!legalEntityTrading) return;
    Street.Colour colour = Street.Colour.valueOf(colourName.replace(' ', '_'));
    if (colour == Street.Colour.orange) return;
    if (ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .anyMatch(it -> deeds == null || deeds.isUnowned(it.type()))) return;
    List<ColourStreet> group = ruleSet.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).filter(it -> it.colourGroup() == colour).toList();
    List<Player> shareholders = players().stream()
        .filter(player -> group.stream().anyMatch(street -> deeds.ownerOf(street.type())
            .filter(player.id()::equals).isPresent()))
        .toList();
    formEntity(colour, false, shareholders);
  }

  public void formNamedEntity(String name) {
    namedEntityFormed = true;
    legalEntityTrading = true;
    players().forEach(player -> pawnStrategies.put(player.id().value(),
        new the.monopoly.game.strategies.Greedo(Money.ZERO, false, true)));
    othersRollWhatTheyLike = true;
    for (int index = 0; index < players().size(); index++) {
      Player player = players().get(index);
      if (queuedPawnRolls.getOrDefault(player.id().value(), new ArrayDeque<>()).isEmpty())
        queuePawnRoll(player.id().value(), rollTotalling(3 + index));
    }
    formEntity(Street.Colour.valueOf(name.substring(0, name.indexOf(' ')).toLowerCase()), true,
        players().stream().limit(3).toList());
  }

  private void formEntity(Street.Colour colour, boolean seedBoard, List<Player> shareholders) {
    if (deeds == null) deeds = new Deeds();
    if (seedBoard) {
      List<ColourStreet> group = ruleSet.streets().filter(ColourStreet.class::isInstance)
          .map(ColourStreet.class::cast).filter(it -> it.colourGroup() == colour).toList();
      for (int index = 0; index < group.size() && index < shareholders.size(); index++)
        if (deeds.isUnowned(group.get(index).type())) deeds.sell(group.get(index), shareholders.get(index), Money.ZERO);
      players.stream().filter(player -> shareholders.stream().noneMatch(shareholder ->
          shareholder.id().equals(player.id()))).findFirst().ifPresent(defaultOwner ->
          ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
              .filter(it -> deeds.isUnowned(it.type()))
              .forEach(it -> deeds.sell(it, defaultOwner, Money.ZERO)));
    }
    String name = Character.toUpperCase(colour.name().charAt(0)) + colour.name().substring(1) + " Realty";
    LegalEntity entity = seedBoard
        ? LegalEntity.formed(name, colour, shareholders, ruleSet)
        : LegalEntity.form(name, colour, shareholders, ruleSet, deeds,
            street -> Strategy.priorityOf(street) == Strategy.Priority.HIGHEST).orElse(null);
    if (entity != null) deeds.form(entity);
    if (entity != null) entityBalances.put(entity.name(), entity.bankBalance());
  }

  public boolean entityIsDissolved(String name) {
    return deeds.legalEntities().stream().noneMatch(entity -> entity.name().equals(name));
  }

  public boolean entityIsNotDissolved(String name) {
    return deeds.legalEntities().stream().anyMatch(entity -> entity.name().equals(name));
  }

  public boolean pawnOwnsEveryFormerEntityStreet(String pawnName, String entityName) {
    Street.Colour colour = Street.Colour.valueOf(entityName.substring(0, entityName.indexOf(' ')).toLowerCase());
    Player owner = pawn(pawnName);
    return LegalEntity.streetsOf(colour, ruleSet).stream()
        .allMatch(street -> deeds.ownerOf(street.type()).filter(owner.id()::equals).isPresent());
  }

  public boolean pawnReceivedEntityBankBalance(String pawnName, String entityName) {
    Money entityBalance = entityBalances.get(entityName);
    if (entityBalance == null) return false;
    Player.ID recipient = pawn(pawnName).id();
    return gameLog().stream().anyMatch(entry -> entry instanceof Entry.LegalEntityLiquidated it
        && it.name().equals(entityName) && it.recipient().equals(recipient) && it.amount().equals(entityBalance));
  }

  public long transferredEntityStreetsSold(String pawnName, String entityName) {
    Street.Colour colour = Street.Colour.valueOf(entityName.substring(0, entityName.indexOf(' ')).toLowerCase());
    Set<Street.Type> streets = LegalEntity.streetsOf(colour, ruleSet).stream().map(Street::type).collect(java.util.stream.Collectors.toSet());
    long sold = gameLog().stream()
        .filter(entry -> entry instanceof Entry.DistressedSaleWon it && streets.contains(it.land()))
        .count();
    return sold;
  }

  public boolean pawnDebtIsSettled(String pawnName) {
    return !Money.ZERO.exceeds(pawn(pawnName).account().balance().amount());
  }

  public boolean colourGroupOwnedByEntity(String colourName) {
    if (!legalEntityTrading) return false;
    Street.Colour colour = Street.Colour.valueOf(colourName.replace(' ', '_'));
    return ruleSet.streets().filter(it -> it instanceof ColourStreet street && street.colourGroup() == colour)
        .map(Street::type).allMatch(type -> deeds.entityOwnerOf(type).isPresent());
  }

  public boolean shareholdersHoldEqualThirds(String entityName) {
    LegalEntity entity = deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst().orElse(null);
    return entity != null && entity.shareholders().stream().allMatch(player -> entity.shareOf(player) == 1.0 / 3.0);
  }

  public boolean pawnOwnsNoMortgagedProperty(String pawnName) {
    Player player = pawn(pawnName);
    return deeds.landOwnedBy(player).stream()
        .noneMatch(type -> deeds.isMortgaged((Ownable) ruleSet.create(type)));
  }

  public boolean pawnHoldsShare(String pawnName, String entityName) {
    return legalEntity(entityName).shareOf(pawn(pawnName)) > 0.0;
  }

  public boolean pawnHoldsNoEntityShares(String pawnName) {
    Player player = pawn(pawnName);
    return deeds.legalEntities().stream().noneMatch(entity -> entity.shareOf(player) > 0.0);
  }

  private LegalEntity legalEntity(String name) {
    return deeds.legalEntities().stream().filter(entity -> entity.name().equals(name)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + name));
  }

  public void entityOwes(String entityName, Money principal) {
    deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName)).recordLoan(principal);
  }

  public void oweEntityDevelopmentLoan(String entityName, Street.Type collateral, Money principal, int yearsServiced) {
    developmentLoanBook().recordEntityLoan(legalEntity(entityName), collateral, principal, yearsServiced, null);
  }

  public Money entityDevelopmentLoanBalance(String entityName, Street.Type collateral) {
    return developmentLoanBook().securedBy(collateral)
        .filter(position -> position.entity() != null && position.entity().name().equals(entityName))
        .map(DevelopmentLoanBook.Position::outstanding).orElse(Money.ZERO);
  }

  public boolean entityDevelopmentLoanFullyRepaid(String entityName, Street.Type collateral) {
    return entityDevelopmentLoanBalance(entityName, collateral).equals(Money.ZERO);
  }

  public boolean entityOwnsNoDevelopmentLoan(String entityName) {
    return developmentLoanBook().positions().stream()
        .noneMatch(position -> position.entity() != null && position.entity().name().equals(entityName)
            && !position.outstanding().equals(Money.ZERO));
  }

  public boolean entityOwns(String entityName, Street.Type land) {
    return deeds.entityOwnerOf(land).filter(entity -> entity.name().equals(entityName)).isPresent();
  }

  public boolean entityRaisesDevelopmentLoan(String entityName, Street.Type collateral, Money amount) {
    return developmentLoanBook().securedBy(collateral).filter(position -> position.entity() != null
        && position.entity().name().equals(entityName) && position.outstanding().equals(amount)).isPresent();
  }

  public void holdEntityDevelopmentBond(String pawnName, Street.Type collateral) {
    DevelopmentLoanBook.Position position = developmentLoanBook().securedBy(collateral)
        .orElseThrow(() -> new AssertionError("No development loan is secured by " + collateral + "."));
    developmentLoanBook().assignBondholder(position, pawn(pawnName));
  }

  public void entityRaisesLoan(String entityName, Money amount) {
    deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName)).raiseLoan(amount);
  }

  public void shareholdersCommitToBuild(String entityName, Money amount) {
    LegalEntity entity = deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName));
    entity.shareholders().forEach(shareholder -> entity.commitToBuild(shareholder, amount));
  }

  public void entityLoanFullyRepaid(String entityName) {
    LegalEntity entity = deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName));
    entity.repayLoan(entity.loan());
  }

  public Money entityLoan(String entityName) {
    return deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName)).loan();
  }

  public void entityBankHolds(String entityName, Money amount) {
    LegalEntity entity = deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName));
    entity.depositToBank(amount);
    entityBalances.put(entityName, entity.bankBalance());
  }

  public void entityHasAlreadyOperated(String entityName) {
    deeds.legalEntities().stream().filter(entity -> entity.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName)).markOperated();
  }

  public void entityLastCapitalizedShareholder(String entityName, String pawnName) {
    LegalEntity entity = deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName));
    entity.recordCapitalization(pawn(pawnName));
  }

  public void entityLastCapitalizedShareholderHasNotAged(String entityName) {
    LegalEntity entity = deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName));
    entity.recordCapitalization(entity.shareholders().getFirst());
  }

  public void entityLastCapitalizedShareholderGrewOlder(String entityName) {
    LegalEntity entity = deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName));
    entity.shareholderGrewOlder(entity.lastCapitalizedShareholder());
  }

  public Money entityBankBalance(String entityName) {
    return deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName)).bankBalance();
  }

  public boolean pawnBalanceIsAfterRent(String pawnName, Money rent) {
    Player pawn = pawn(pawnName);
    return pawn.account().balance().amount().equals(ruleSet.players().startingCapital().minus(rent));
  }

  public int housesBuilt(Street.Type land) {
    return deeds.housesBuiltOn((ColourStreet) ruleSet.create(land));
  }

  public void arrangeOrAssertHouses(Street.Type land, int houses) {
    if (!gameStarted) arrangeHouses(land, houses);
    else if (housesBuilt(land) != houses)
      throw new AssertionError("Expected " + houses + " houses on " + land + " but found " + housesBuilt(land));
  }

  public int totalHouses(Street.Colour colour) {
    return LegalEntity.streetsOf(colour, ruleSet).stream().mapToInt(deeds::housesBuiltOn).sum();
  }

  public boolean shareholderPaymentsWithin(int ceiling) {
    Money limit = new Money(ceiling);
    return deeds.legalEntities().stream()
        .flatMap(entity -> entity.shareholders().stream().map(entity::shareholderPayment))
        .allMatch(payment -> !payment.exceeds(limit));
  }

  public void ownEveryOtherOwnableAlternately(String firstPawn, String secondPawn) {
    if (deeds == null) deeds = new Deeds();
    List<Ownable> ownables = ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast).toList();
    for (int index = 0; index < ownables.size(); index++) {
      deeds.sell(ownables.get(index), pawn(index % 2 == 0 ? firstPawn : secondPawn), Money.ZERO);
    }
  }

  public void ownEveryOtherOwnable(String pawnName) {
    if (deeds == null) deeds = new Deeds();
    ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .filter(ownable -> deeds.isUnowned(ownable.type()))
        .forEach(ownable -> deeds.sell(ownable, pawn(pawnName), Money.ZERO));
  }

  public void ownEveryOtherOwnableRoundRobin(String... pawns) {
    if (deeds == null) deeds = new Deeds();
    List<Ownable> ownables = ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast).toList();
    Map<Street.Colour, Integer> groupSizes = new HashMap<>();
    ownables.stream().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .forEach(street -> groupSizes.merge(street.colourGroup(), 1, Integer::sum));
    Map<Street.Colour, Integer> groupPositions = new HashMap<>();
    int otherPosition = 0;
    for (int index = 0; index < ownables.size(); index++) {
      Ownable ownable = ownables.get(index);
      String owner;
      if (ownable instanceof ColourStreet street) {
        int position = groupPositions.merge(street.colourGroup(), 1, Integer::sum) - 1;
        owner = pawns[position % Math.min(pawns.length, groupSizes.get(street.colourGroup()))];
      } else {
        owner = pawns[otherPosition++ % pawns.length];
      }
      deeds.sell(ownable, pawn(owner), Money.ZERO);
    }
  }

  public void assertGreedoPriority(String spaceName, String expected) {
    assertPriority("Greedo", spaceName, expected);
  }

  public void assertPriority(String strategyName, String spaceName, String expected) {
    Strategy.Priority actual =
        Vocabulary.strategy(strategyName).priority(ownable(SpaceNames.of(spaceName)));
    org.assertj.core.api.Assertions.assertThat(actual.name().toLowerCase()).isEqualTo(expected);
  }

  public void pawnConsidersTrading(String traderName, String offeredName, String partnerName, String wantedName) {
    pawnConsidersTrading("Greedo", traderName, offeredName, partnerName, wantedName);
  }

  public void pawnConsidersTrading(String strategyName, String traderName, String offeredName, String partnerName, String wantedName) {
    Player trader = pawn(traderName);
    Player partner = pawn(partnerName);
    Strategy.TradeOffer offer = new Strategy.TradeOffer(
        trader, partner, ownable(SpaceNames.of(offeredName)), ownable(SpaceNames.of(wantedName)));
    tradeAccepted = Vocabulary.strategy(strategyName).accepts(offer, ruleSet, deeds);
  }

  public void assertGreedoTradeDecision(String decision) {
    assertTradeDecision("Greedo", decision);
  }

  public void assertTradeDecision(String strategyName, String decision) {
    if (tradeAccepted == null) throw new AssertionError("No trade has been considered yet.");
    boolean expected = decision.equals("accepts");
    org.assertj.core.api.Assertions.assertThat(tradeAccepted).isEqualTo(expected);
  }

  public void queueChanceCard(String card) {
    queuedChanceCards.add(card);
  }

  public void queueCommunityChestCard(String card) {
    queuedCommunityChestCards.add(card);
  }

  public void pawnDeclines(String pawnName, Street.Type land) {
    scriptFor(pawnName).declines(land);
  }

  public void pawnWillBid(String pawnName, Street.Type land, Money amount) {
    Strategy strategy = pawnStrategies.get(pawnName);
    if (strategy == null) {
      scriptFor(pawnName).bids(land, amount);
      return;
    }
    if (strategy instanceof Scripted scripted) {
      scripted.bids(land, amount);
      return;
    }
    pawnStrategies.put(pawnName, new Strategy() {
      @Override
      public boolean accepts(Offer offer) {
        return strategy.accepts(offer);
      }

      @Override
      public Money bidFor(Offer offer) {
        return offer.land().type() == land ? amount : strategy.bidFor(offer);
      }

      @Override
      public Money bidForAuction(Offer offer, Player bidder, Rule.Set rules, Deeds deeds) {
        return offer.land().type() == land ? amount : strategy.bidForAuction(offer, bidder, rules, deeds);
      }

      @Override
      public boolean claims(RentClaim claim) {
        return strategy.claims(claim);
      }

      @Override
      public boolean builds(BuildOffer offer) {
        return strategy.builds(offer);
      }
    });
  }

  public void pawnWillBuy(String pawnName, Street.Type land) {
    Strategy strategy = pawnStrategies.get(pawnName);
    if (strategy == null) {
      scriptFor(pawnName).buys(land);
      return;
    }
    if (strategy instanceof Scripted scripted) {
      scripted.buys(land);
      return;
    }
    pawnStrategies.put(pawnName, new Strategy() {
      @Override
      public boolean accepts(Offer offer) {
        return offer.land().type() == land || strategy.accepts(offer);
      }

      @Override
      public Money bidFor(Offer offer) {
        return strategy.bidFor(offer);
      }

      @Override
      public boolean claims(RentClaim claim) {
        return strategy.claims(claim);
      }

      @Override
      public boolean builds(BuildOffer offer) {
        return strategy.builds(offer);
      }
    });
  }

  public void pawnWillBuildHouseOn(String pawnName, Street.Type land) {
    Strategy strategy = pawnStrategies.get(pawnName);
    if (strategy == null) {
      scriptFor(pawnName).builds(land);
      return;
    }
    if (strategy instanceof Scripted scripted) {
      scripted.builds(land);
      return;
    }
    pawnStrategies.put(pawnName, new Strategy() {
      @Override
      public boolean accepts(Offer offer) {
        return strategy.accepts(offer);
      }

      @Override
      public Money bidFor(Offer offer) {
        return strategy.bidFor(offer);
      }

      @Override
      public boolean claims(RentClaim claim) {
        return strategy.claims(claim);
      }

      @Override
      public boolean builds(BuildOffer offer) {
        return offer.land().type() == land;
      }
    });
  }

  public void pawnDeclinesRent(String pawnName, Street.Type land) {
    pawnStrategies.put(pawnName, new Strategy() {
      @Override
      public boolean claims(RentClaim claim) {
        return false;
      }
    });
  }

  private Strategy strategyOf(Player player) {
    return pawnStrategies.getOrDefault(player.id().value(), Strategy.UNDECIDED);
  }

  /**
   * Writes a single entry the scenario's own action produced. The action has
   * no game of its own, so the entry is written through the journal, which is
   * also what puts it on the game log.
   */
  private void record(Entry entry) {
    Journal journal = new Journal();
    if (this.journal != null) this.journal.forEach(journal::log);
    journal.log(entry);
    this.journal = journal.entries();
  }

  private Scripted scriptFor(String pawnName) {
    Strategy strategy = pawnStrategies.computeIfAbsent(pawnName, it -> new Scripted());
    if (!(strategy instanceof Scripted scripted))
      throw new AssertionError(
          "Pawn \"" + pawnName + "\" already follows a strategy of its own, so it cannot be told what to do."
      );
    return scripted;
  }

  /** What the game recorded, once it has been played. */
  public List<Entry> journal() {
    return journal == null ? gameLog() : journal;
  }

  /** The journal told as text, which is the only place the wording is settled. */
  public String report() {
    return Report.of(journal());
  }

  public void sellHouse(String pawnName, Street.Type land) {
    if (deeds == null)
      throw new AssertionError("No deeds exist yet, so no house can be sold.");
    Player player = pawn(pawnName);
    ColourStreet street = colourStreet(land);
    Money price = deeds.sellHouse(street, player);
    record(new Entry.HouseSold(player.id(), street.type(), price));
  }

  public void exchangeHotelForHouses(String pawnName, Street.Type land) {
    if (deeds == null)
      throw new AssertionError("No deeds exist yet, so no hotel can be exchanged.");
    deeds.exchangeHotelForHouses(colourStreet(land), pawn(pawnName));
  }

  public void mortgage(String pawnName, Street.Type land) {
    if (deeds == null)
      throw new AssertionError("No deeds exist yet, so no land can be mortgaged.");
    Player player = pawn(pawnName);
    Ownable ownable = ownable(land);
    Money value = deeds.mortgage(ownable, player);
    record(new Entry.Mortgaged(player.id(), land, value));
  }

  public void liftMortgage(String pawnName, Street.Type land) {
    if (deeds == null)
      throw new AssertionError("No deeds exist yet, so no mortgage can be lifted.");
    Player player = pawn(pawnName);
    Deeds.MortgageCost cost = deeds.liftMortgage(ownable(land), player);
    record(new Entry.MortgageLifted(player.id(), land, cost.total(), cost.interest()));
  }

  public void keepMortgaged(String pawnName, Street.Type land) {
    if (deeds == null)
      throw new AssertionError("No deeds exist yet, so no mortgage can be kept.");
    deeds.keepMortgaged(ownable(land), pawn(pawnName));
  }

  public void sellLand(String sellerName, Street.Type land, String buyerName, Money price) {
    if (deeds == null)
      throw new AssertionError("No deeds exist yet, so no land can be sold.");
    LandSale sale = new LandSale(deeds, ruleSet, players(), this::strategyOf, new LandSale.Events() {
      @Override
      public void bought(Player buyer, Ownable land, Money price) {
      }

      @Override
      public void wonAtAuction(Player winner, Ownable land, Money price) {
      }

      @Override
      public void sold(Player seller, Ownable soldLand, Player buyer, Money soldPrice) {
        record(new Entry.LandSold(seller.id(), soldLand.type(), buyer.id(), soldPrice));
      }

      @Override
      public void saleRefused(Player seller, Ownable soldLand, Player buyer, Money soldPrice) {
        record(new Entry.LandSaleRefused(seller.id(), soldLand.type(), buyer.id(), soldPrice));
      }
    });
    sale.sell(pawn(sellerName), ownable(land), pawn(buyerName), price);
  }

  public boolean holdsGetOutOfJailFreeCard(String pawnName) {
    if (deeds == null) return false;
    return deeds.holdsGetOutOfJailFreeCard(pawn(pawnName));
  }

  public void sellGetOutOfJailFreeCard(String sellerName, String buyerName, Money price) {
    if (deeds == null)
      throw new AssertionError("No deeds exist yet, so no Get Out of Jail Free card can be sold.");
    deeds.sellGetOutOfJailFreeCard(pawn(sellerName), pawn(buyerName), price);
  }

  /**
   * Leaves a pawn with the money the scenario says it has to spend. The rules
   * open every account with the same starting capital and no rule pays anyone
   * before the first roll, so a pawn can be spent down to an amount but never
   * given more than it was dealt.
   */
  public void arrangePawnBalance(String pawnName, Money amount) {
    Money startingCapital = ruleSet.players().startingCapital();
    if (amount.exceeds(startingCapital))
      throw new AssertionError(
          "Pawn \"" + pawnName + "\" is dealt $" + startingCapital.amount()
              + ", and no rule pays anyone before the game starts, so it cannot hold $"
              + amount.amount() + "."
      );
    pawn(pawnName).account().withdraw(startingCapital.minus(amount));
    suppressOpeningCapitalIfNeeded(pawnName);
  }

  /**
   * Sets a balance representing wealth accumulated during the game. Arranging
   * a Billionaire to a concrete balance means the account is intentionally at
   * that value, so the strategy's opening capital must not re-apply later.
   */
  public void holdPawnBalance(String pawnName, Money amount) {
    Money current = pawn(pawnName).account().balance().amount();
    if (amount.exceeds(current)) pawn(pawnName).account().deposit(amount.minus(current));
    else if (current.exceeds(amount)) pawn(pawnName).account().withdraw(current.minus(amount));
    suppressOpeningCapitalIfNeeded(pawnName);
  }

  private void suppressOpeningCapitalIfNeeded(String pawnName) {
    if (pawnStrategies.get(pawnName) instanceof Billionaire billionaire)
      pawnStrategies.put(pawnName, new Billionaire(billionaire.cashReserve(),
          billionaire.stalemateTradingEnabled(), billionaire.legalEntityTradingEnabled(), false,
          billionaire.assetRichOpening()));
  }

  public Money stalemateThreshold() {
    return Stalemate.threshold(ruleSet);
  }

  /**
   * Lets the players a scenario says nothing about roll something unremarkable
   * when their turn comes, so that a scenario watching one pawn does not have
   * to script the others.
   */
  public void letTheOthersRollWhatTheyLike() {
    othersRollWhatTheyLike = true;
  }

  public void marketDeadlockCanFund(String group) {
    letTheOthersRollWhatTheyLike();
  }

  public void marketDeadlockCannotFund(String group) {
    holdPawnBalance("dog", Money.ZERO);
    holdPawnBalance("high hat", Money.ZERO);
    holdPawnBalance("iron box", Money.ZERO);
    letTheOthersRollWhatTheyLike();
  }

  public void marketDeadlockEligible(String group) {
    letTheOthersRollWhatTheyLike();
  }

  public void completeMarketDeadlockRound(String action) {
    letTheOthersRollWhatTheyLike();
  }

  private Roll nextQueuedPawnRoll(Player player) {
    Deque<Roll> queued = queuedPawnRolls.get(player.id().value());
    if (queued == null || queued.isEmpty()) {
      if (othersRollWhatTheyLike) return UNREMARKABLE;
      throw new AssertionError(
          "The game wanted another roll for \"" + player.id().value() + "\" but none was queued."
      );
    }
    return queued.removeFirst();
  }

  /** A pair of dice adding up to a total the rules can actually be rolled. */
  static Roll rollTotalling(int total) {
    if (total < 2 || total > 12)
      throw new AssertionError("Two dice cannot total " + total + ".");
    int die1 = Math.min(6, total - 1);
    return new Roll(die1, total - die1);
  }

  public List<Player> turnOrder() {
    if (turnOrder == null)
      throw new AssertionError("Initiative has not been rolled for yet.");
    return turnOrder;
  }

  public void selectEvent(String eventType) {
    selectedEvent = SampleEvents.of(eventType);
  }

  /** Selects a parameterized event for report/log rendering fixtures. */
  public void selectEvent(String eventType, Map<String, String> values) {
    selectedEvent = SampleEvents.of(eventType, values);
  }

  /** Test-only observation seam for direct event-handler contract tests. */
  Entry selectedEventForTesting() {
    if (selectedEvent == null) throw new AssertionError("No event has been selected yet.");
    return selectedEvent;
  }

  public void renderSelectedEventForReport() {
    renderedEventText = Report.of(List.of(selectedEvent));
  }

  public void logSelectedEventToJournal() {
    int offset = GameLog.offset();
    new Game.Journal().log(selectedEvent);
    loggedEventText = GameLog.formattedMessage(offset);
  }

  public void assertLoggedEventTextMatchesReportRendering() {
    if (renderedEventText == null)
      throw new AssertionError("The event has not been rendered for the report yet.");
    if (loggedEventText == null)
      throw new AssertionError("The event has not been logged to the Journal yet.");
    if (!loggedEventText.equals(renderedEventText))
      throw new AssertionError(
          "The logged message \"" + loggedEventText + "\" does not match the report's rendered text \""
              + renderedEventText + "\"."
      );
  }

  public void selectPomModule(String moduleDirectory) {
    pomModuleDirectory = moduleDirectory;
  }

  public void inspectPomDependencies() {
    if (pomModuleDirectory == null)
      throw new AssertionError("No pom.xml module has been selected yet.");
    pomDependencies = PomInspector.declaredDependencies(pomModuleDirectory);
  }

  public void inspectPomPlugins() {
    if (pomModuleDirectory == null)
      throw new AssertionError("No pom.xml module has been selected yet.");
    pomPluginsInspected = true;
  }

  public void assertExecutableJar(String mainClass) {
    if (!pomPluginsInspected) throw new AssertionError("The build plugins have not been inspected yet.");
    if (!PomInspector.declaresExecutableJar(pomModuleDirectory, mainClass))
      throw new AssertionError("Expected an executable shaded jar with main class " + mainClass + ".");
  }

  public void packageCli() {
    Path root = PomInspector.repoRoot("the-monopoly-game-cli");
    runProcess(root, "mvn", "-B", "-Dmaven.repo.local=tmp/m2", "-pl", "the-monopoly-game-cli",
        "-am", "clean", "package", "-DskipTests");
  }

  public void runPackagedCli(String flag) {
    Path jar = packagedCliJar();
    ProcessBuilder builder = new ProcessBuilder("java", "-jar", jar.toString(), flag);
    try {
      packagedCliProcess = builder.redirectErrorStream(true).start();
      packagedCliOutput = new String(packagedCliProcess.getInputStream().readAllBytes());
      packagedCliExitCode = packagedCliProcess.waitFor();
    } catch (IOException | InterruptedException cause) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Could not run packaged simulator jar.", cause);
    }
  }

  public void startPackagedCli(String rawArguments) {
    Path jar = packagedCliJar();
    try {
      packagedCliOutputBuffer = new StringBuilder();
      packagedCliProcess = new ProcessBuilder(
          java.util.stream.Stream.concat(Stream.of("java", "-jar", jar.toString()),
              Stream.of(rawArguments.trim().split("\\s+"))).toArray(String[]::new))
          .redirectErrorStream(true).start();
      Thread reader = new Thread(() -> {
        try (var input = packagedCliProcess.getInputStream()) {
          input.transferTo(new java.io.OutputStream() {
            @Override public void write(int value) {
              synchronized (packagedCliOutputBuffer) { packagedCliOutputBuffer.append((char) value); }
            }
          });
        } catch (IOException ignored) {
          // The process may close its stream while being stopped.
        }
      });
      reader.setDaemon(true);
      reader.start();
    } catch (IOException cause) {
      throw new AssertionError("Could not start packaged simulator jar.", cause);
    }
  }

  private void assertPackagedCliState(String marker, String description, String state) {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (System.nanoTime() < deadline) {
      synchronized (packagedCliOutputBuffer) {
        String output = packagedCliOutputBuffer.toString();
        if (output.contains(marker)) {
          if (state.equals("enabled")) return;
          throw new AssertionError("Packaged jar output confirmed " + description + " enabled, expected " + state + ".");
        }
      }
      LockSupport.parkNanos(5_000_000);
    }
    throw new AssertionError("Packaged jar output did not confirm " + description + " is " + state
        + ": " + packagedCliOutputBuffer);
  }

  public void assertPackagedCliStalemateTrading(String state) {
    assertPackagedCliState("Stalemate trading enabled", "stalemate trading", state);
  }

  public void assertPackagedCliLegalEntity(String state) {
    assertPackagedCliState("Legal entity trading enabled", "legal entity trading", state);
  }

  public void assertPackagedCliYearLimit(int yearLimit) {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (System.nanoTime() < deadline) {
      synchronized (packagedCliOutputBuffer) {
        if (packagedCliOutputBuffer.toString().contains("Year limit is " + yearLimit + " years")) return;
      }
      LockSupport.parkNanos(5_000_000);
    }
    throw new AssertionError("Packaged jar output did not confirm year limit is " + yearLimit
        + " years: " + packagedCliOutputBuffer);
  }

  public void assertPackagedCliDevelopmentLoans(String state) {
    assertPackagedCliState("Development loans enabled", "development loans", state);
  }

  public void assertPackagedCliFullDrawDevelopmentLoans(String state) {
    assertPackagedCliState("Full-draw development loans enabled", "full-draw development loans", state);
  }

  public void assertPackagedCliWarProfitsTax(String state) {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (System.nanoTime() < deadline) {
      synchronized (packagedCliOutputBuffer) {
        String output = packagedCliOutputBuffer.toString();
        if (output.contains("War profits tax enabled")) {
          if (state.equals("enabled")) return;
          throw new AssertionError("Packaged jar output confirmed war profits tax enabled, expected " + state + ".");
        }
      }
      LockSupport.parkNanos(5_000_000);
    }
    throw new AssertionError("Packaged jar output did not confirm war profits tax is " + state
        + ": " + packagedCliOutputBuffer);
  }

  public void assertPackagedCliRentRelief(String state) {
    assertPackagedCliState("Rent relief enabled", "rent relief", state);
  }

  public void assertPackagedCliAssetRichOpening(String state) {
    assertPackagedCliState("Asset-rich opening enabled", "asset-rich opening", state);
  }

  public void stopPackagedCli() {
    if (packagedCliProcess == null) throw new AssertionError("The packaged simulator has not been started.");
    packagedCliProcess.destroy();
    try {
      if (!packagedCliProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) packagedCliProcess.destroyForcibly();
    } catch (InterruptedException cause) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Interrupted while stopping packaged simulator.", cause);
    }
  }

  public boolean packagedCliProcessEnded() {
    return packagedCliProcess != null && !packagedCliProcess.isAlive();
  }

  public void assertPackagedCliSucceeded() {
    if (packagedCliProcess == null || packagedCliExitCode != 0)
      throw new AssertionError("Packaged jar exited with " + packagedCliExitCode + ": " + packagedCliOutput);
  }

  public void assertPackagedCliUsage() {
    if (packagedCliOutput == null || !packagedCliOutput.contains("Usage: simulator"))
      throw new AssertionError("Packaged jar did not print simulator usage: " + packagedCliOutput);
  }

  private static Path packagedCliJar() {
    Path target = PomInspector.repoRoot("the-monopoly-game-cli")
        .resolve("the-monopoly-game-cli").resolve("target");
    try (Stream<Path> files = Files.list(target)) {
      return files.filter(path -> path.getFileName().toString().matches("the-monopoly-game-cli-[^/]+\\.jar"))
          .filter(path -> !path.getFileName().toString().matches(".*-(original|tests|sources|javadoc)\\.jar"))
          .findFirst()
          .orElseThrow(() -> new AssertionError("No packaged simulator jar found in " + target));
    } catch (IOException cause) {
      throw new AssertionError("Could not inspect packaged simulator jars in " + target, cause);
    }
  }

  public void assertReadmeUsageFlag(String flag) {
    Path readme = PomInspector.repoRoot("the-monopoly-game-cli").resolve("README.md");
    try {
      if (!Files.readString(readme).contains(flag))
        throw new AssertionError("README usage report does not include " + flag + ".");
    } catch (IOException cause) {
      throw new AssertionError("Could not read " + readme + ".", cause);
    }
  }

  private static void runProcess(Path workingDirectory, String... command) {
    try {
      Process process = new ProcessBuilder(command)
          .directory(workingDirectory.toFile())
          .redirectErrorStream(true)
          .redirectOutput(ProcessBuilder.Redirect.DISCARD)
          .start();
      if (process.waitFor() != 0) throw new AssertionError("Command failed: " + String.join(" ", command));
    } catch (IOException | InterruptedException cause) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Could not run: " + String.join(" ", command), cause);
    }
  }

  public void assertPomDeclaresDependency(String coordinate) {
    if (pomDependencies == null)
      throw new AssertionError("The declared dependencies have not been inspected yet.");
    if (!pomDependencies.containsKey(coordinate))
      throw new AssertionError(
          "Expected \"" + pomModuleDirectory + "\"'s pom.xml to declare \"" + coordinate
              + "\" but it declares: " + pomDependencies.keySet());
    lastCheckedPomDependency = coordinate;
  }

  public void assertLastCheckedPomDependencyVersionAtLeast(String minimum) {
    String version = pomDependencies.get(lastCheckedPomDependency);
    if (version == null)
      throw new AssertionError(
          "\"" + lastCheckedPomDependency + "\" has no resolvable version: not pinned in \""
              + pomModuleDirectory + "\" and not managed by the parent pom.");
    if (!PomStepHandlers.atLeast(version, minimum))
      throw new AssertionError(
          "Expected \"" + lastCheckedPomDependency + "\" version to be at least \"" + minimum
              + "\" but found \"" + version + "\".");
  }

  private List<Player> players() {
    if (players == null)
      throw new AssertionError("No players have been selected yet.");
    return players;
  }

  private ColourStreet colourStreet(Street.Type land) {
    Street space = ruleSet.create(land);
    if (!(space instanceof ColourStreet street))
      throw new AssertionError(land + " is not a colour street.");
    return street;
  }

  private Ownable ownable(Street.Type land) {
    Street space = ruleSet.create(land);
    if (!(space instanceof Ownable ownable))
      throw new AssertionError(land + " is not ownable land.");
    return ownable;
  }

  private Street.Type streetTypeNamed(String name) {
    String normalized = name.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    return java.util.Arrays.stream(Street.Type.values())
        .filter(type -> type.name().replaceAll("[^A-Za-z0-9]", "").toLowerCase().equals(normalized))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Unknown street \"" + name + "\"."));
  }

  /**
   * A pawn told what to do about each piece of land a scenario names, and told
   * off for being offered anything else: a scenario that scripts a pawn at all
   * has to say what that pawn does wherever it is asked.
   */
  private static final class Scripted implements Strategy {
    private final Set<Street.Type> declined = new HashSet<>();
    private final Set<Street.Type> bought = new HashSet<>();
    private final Map<Street.Type, Money> bids = new HashMap<>();
    private final Set<Street.Type> builds = new HashSet<>();
    private boolean paysJailFine;

    void declines(Street.Type land) {
      declined.add(land);
    }

    void bids(Street.Type land, Money amount) {
      bids.put(land, amount);
    }

    void buys(Street.Type land) {
      bought.add(land);
    }

    void builds(Street.Type land) {
      builds.add(land);
    }

    void paysJailFine() {
      paysJailFine = true;
    }

    @Override
    public boolean accepts(Offer offer) {
      if (bought.contains(offer.land().type())) return true;
      if (!declined.contains(offer.land().type()))
        throw new AssertionError(
            "This pawn was offered " + offer.land().type()
                + ", and the scenario never says what it does about that."
        );
      return false;
    }

    @Override
    public Money bidFor(Offer offer) {
      return bids.getOrDefault(offer.land().type(), Money.ZERO);
    }

    @Override
    public boolean builds(BuildOffer offer) {
      return builds.contains(offer.land().type());
    }

    @Override
    public boolean pays(JailFine fine) {
      return paysJailFine;
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=72a0159314499ec809196b10c56ea8bc2421264f61242e4f3a36ac1b34310111
scope.0.id=Y2xhc3M6V29ybGQjV29ybGQ6NTc
scope.0.kind=class
scope.0.startLine=57
scope.0.endLine=1479
scope.0.semanticHash=5499254cd7b76064c029b87d23c6f18f1f6cc1621a366a6f0c1a3b9c542b5f75
scope.1.id=Y2xhc3M6V29ybGQuIzoxMDcz
scope.1.kind=class
scope.1.startLine=1073
scope.1.endLine=1091
scope.1.semanticHash=9822c3ef77b76a19280f381ab8032e14cb31fe0a07709d110e9a1ec9d8a14aa6
scope.2.id=Y2xhc3M6V29ybGQuIzoxMjgw
scope.2.kind=class
scope.2.startLine=1280
scope.2.endLine=1284
scope.2.semanticHash=885ebef77dbd9bc6971cda8671896283674580bede46da1acb2afe08aff76fdc
scope.3.id=Y2xhc3M6V29ybGQuIzozNzQ
scope.3.kind=class
scope.3.startLine=374
scope.3.endLine=375
scope.3.semanticHash=9265619237dc049c9efd5c01a5c74a7e99db1a67aa7ef3e2e396e54563e1bc57
scope.4.id=Y2xhc3M6V29ybGQuIzo0MTU
scope.4.kind=class
scope.4.startLine=415
scope.4.endLine=426
scope.4.semanticHash=75e60c6ba32e116e817843808e03a7115cd2d5bf3185410bd7a497410eb6b13a
scope.5.id=Y2xhc3M6V29ybGQuIzo0NzM
scope.5.kind=class
scope.5.startLine=473
scope.5.endLine=479
scope.5.semanticHash=8be6296ba37df38fdd0f86ecf109e15b302caeec5a2ec84fdbc4f9e7c4450cc8
scope.6.id=Y2xhc3M6V29ybGQuIzo1MTM
scope.6.kind=class
scope.6.startLine=513
scope.6.endLine=538
scope.6.semanticHash=3dcd5601ef5ccad120d8906c846d57418e722c1daee5c8f5b11da87b3ac5235b
scope.7.id=Y2xhc3M6V29ybGQuIzo1Nzk
scope.7.kind=class
scope.7.startLine=579
scope.7.endLine=584
scope.7.semanticHash=c43847ed64b7fa81f238af12b0a87d74dc1affcbbc0a244bc777dad599c2dfb7
scope.8.id=Y2xhc3M6V29ybGQuIzo5MzE
scope.8.kind=class
scope.8.startLine=931
scope.8.endLine=951
scope.8.semanticHash=996a2e5ae5a0b9027dc7b4b35faf28d4777654d8a5f460b1cc2d25d34bb84dd2
scope.9.id=Y2xhc3M6V29ybGQuIzo5NjQ
scope.9.kind=class
scope.9.startLine=964
scope.9.endLine=984
scope.9.semanticHash=29ce330ce5b7b0e5d3b0cb6337676971c64050412dcd2d86054478580fa2eea6
scope.10.id=Y2xhc3M6V29ybGQuIzo5ODg
scope.10.kind=class
scope.10.startLine=988
scope.10.endLine=993
scope.10.semanticHash=fd0ba69339dcfae848d6dcfbcfa2e3832962eb7880ebe7afda18f619632a3e4b
scope.11.id=Y2xhc3M6V29ybGQuU2NyaXB0ZWQjU2NyaXB0ZWQ6MTQyNg
scope.11.kind=class
scope.11.startLine=1426
scope.11.endLine=1478
scope.11.semanticHash=50e6599a322fef7efc164eb9bd67bdb76c1a987a94dce80d2a743c31ace1a5cf
scope.12.id=ZmllbGQ6V29ybGQjQV9TSE9SVF9IT1A6Njc
scope.12.kind=field
scope.12.startLine=67
scope.12.endLine=67
scope.12.semanticHash=176629d90f58c519d35f56f1e2ebb08d54168684283219eee8b90e17c0dc9d29
scope.13.id=ZmllbGQ6V29ybGQjVU5ERVJfVEVTVDo1OQ
scope.13.kind=field
scope.13.startLine=59
scope.13.endLine=59
scope.13.semanticHash=6603447de25292cfc5ca21c57f79e46dd35fff86325123b76e8a9e3c568877fe
scope.14.id=ZmllbGQ6V29ybGQjVU5SRU1BUktBQkxFOjY1
scope.14.kind=field
scope.14.startLine=65
scope.14.endLine=65
scope.14.semanticHash=2afc64ac00a764fcac0c8e642462807fe24a348679e45e33dd62c1c4b8ef29e8
scope.15.id=ZmllbGQ6V29ybGQjYnV5b3V0OjEwMg
scope.15.kind=field
scope.15.startLine=102
scope.15.endLine=102
scope.15.semanticHash=8b0ac99dcce7436dec8a80c37fb92cdb5a5a3284d909ebc2255ba993bcb206fd
scope.16.id=ZmllbGQ6V29ybGQjZGVlZHM6ODQ
scope.16.kind=field
scope.16.startLine=84
scope.16.endLine=84
scope.16.semanticHash=d4ad562d3d4942b24f3eb91bce411a46c25970c5ea73644456404c31121125d1
scope.17.id=ZmllbGQ6V29ybGQjZGljZTo3NA
scope.17.kind=field
scope.17.startLine=74
scope.17.endLine=74
scope.17.semanticHash=1796c766ea805c56e906c86cb5ef2b4c16fef431cdeb1dc94532eac8dd3dfa68
scope.18.id=ZmllbGQ6V29ybGQjZW50aXR5QmFsYW5jZXM6MTAx
scope.18.kind=field
scope.18.startLine=101
scope.18.endLine=101
scope.18.semanticHash=df4c30b76869d59b1ece4ff47a6e3df228ce15b9e536d8951007507f7053babb
scope.19.id=ZmllbGQ6V29ybGQjZ2FtZUxvZ09mZnNldDo5MQ
scope.19.kind=field
scope.19.startLine=91
scope.19.endLine=91
scope.19.semanticHash=7897c2d13e2ae693b757f424db3a13a3cb0f07103ab9afce46d3073fd46bc3ce
scope.20.id=ZmllbGQ6V29ybGQjZ2FtZVN0YXJ0ZWQ6NzI
scope.20.kind=field
scope.20.startLine=72
scope.20.endLine=72
scope.20.semanticHash=52325e850f357a0cf4a7f7fd6dc26423d746cebed7ee7cfac6a7362b4c98c9f8
scope.21.id=ZmllbGQ6V29ybGQjamFpbDo4NQ
scope.21.kind=field
scope.21.startLine=85
scope.21.endLine=85
scope.21.semanticHash=9622ff719dfa6e5d679265178f15b25e9502d3b748951d2a26a3dea5b7cd14e3
scope.22.id=ZmllbGQ6V29ybGQjam91cm5hbDo4Mw
scope.22.kind=field
scope.22.startLine=83
scope.22.endLine=83
scope.22.semanticHash=02c1ff844f9e1ad1c9145bc010f5f4699e464883d333e555c99b9d6c14dcb8b6
scope.23.id=ZmllbGQ6V29ybGQjbGFzdENoZWNrZWRQb21EZXBlbmRlbmN5Ojk0
scope.23.kind=field
scope.23.startLine=94
scope.23.endLine=94
scope.23.semanticHash=4f70116b52929073d5af6aa16dabff66fb2d0b63922a2ae032586f16e82ef6a2
scope.24.id=ZmllbGQ6V29ybGQjbGVnYWxFbnRpdHlUcmFkaW5nOjEwNA
scope.24.kind=field
scope.24.startLine=104
scope.24.endLine=104
scope.24.semanticHash=99e384b9fbfe0e95375a3ed79ff46c5229f33b088ebb968757b3b958ef0f6ab1
scope.25.id=ZmllbGQ6V29ybGQjbG9nZ2VkRXZlbnRUZXh0OjEwOQ
scope.25.kind=field
scope.25.startLine=109
scope.25.endLine=109
scope.25.semanticHash=dfa14c455d9e71d6164fe2d376c374a8a7dff0e1541086ca6f3773cb87b34bf2
scope.26.id=ZmllbGQ6V29ybGQjbW9ub3BvbHlSdW5zQ29tcGxldGVkOjg2
scope.26.kind=field
scope.26.startLine=86
scope.26.endLine=86
scope.26.semanticHash=969546c719953e5245aa8cd6fafc468db6a2a07bf1d17ff6754348078ce6213d
scope.27.id=ZmllbGQ6V29ybGQjb3RoZXJzUm9sbFdoYXRUaGV5TGlrZTo4Mg
scope.27.kind=field
scope.27.startLine=82
scope.27.endLine=82
scope.27.semanticHash=0f6ea9245238fa157cbaabbdb60bc0cd2e5e567fe3c8e1925d98b2d96f619958
scope.28.id=ZmllbGQ6V29ybGQjcGFja2FnZWRDbGlFeGl0Q29kZTo5OQ
scope.28.kind=field
scope.28.startLine=99
scope.28.endLine=99
scope.28.semanticHash=99044fa26a55ac1dd780c3ba2b7ab8ec120f46bf7ebb2435ef85497d2629d3ca
scope.29.id=ZmllbGQ6V29ybGQjcGFja2FnZWRDbGlPdXRwdXQ6OTc
scope.29.kind=field
scope.29.startLine=97
scope.29.endLine=97
scope.29.semanticHash=79a60a9d95410c9d9ae8ea27245a9973c776c4beb21f2b3980a41141f196ab81
scope.30.id=ZmllbGQ6V29ybGQjcGFja2FnZWRDbGlPdXRwdXRCdWZmZXI6OTg
scope.30.kind=field
scope.30.startLine=98
scope.30.endLine=98
scope.30.semanticHash=148de564a08a74623c2546458e7dfe92afb957587c0cf4cdf0b4d9adf99fb559
scope.31.id=ZmllbGQ6V29ybGQjcGFja2FnZWRDbGlQcm9jZXNzOjk2
scope.31.kind=field
scope.31.startLine=96
scope.31.endLine=96
scope.31.semanticHash=6817ae80adb4e98553eb21e1b050bdc1f921ae43e79c15facbb57256f1894261
scope.32.id=ZmllbGQ6V29ybGQjcGF3blN0cmF0ZWdpZXM6ODA
scope.32.kind=field
scope.32.startLine=80
scope.32.endLine=80
scope.32.semanticHash=674e37fbde8b36e415fd17d0d47f3977be035ffc8b8c47881f526ce547c6c709
scope.33.id=ZmllbGQ6V29ybGQjcGxheWVyOjcz
scope.33.kind=field
scope.33.startLine=73
scope.33.endLine=73
scope.33.semanticHash=202f8017a1be43a07c8b3f029820f8eb97d1581874c7e14e22c531c642e153a3
scope.34.id=ZmllbGQ6V29ybGQjcGxheWVyczo3MQ
scope.34.kind=field
scope.34.startLine=71
scope.34.endLine=71
scope.34.semanticHash=e3fbbdfa1958ce4f7e6625ede0548e725756f22f7a1d4611541989d128ba9440
scope.35.id=ZmllbGQ6V29ybGQjcG9tRGVwZW5kZW5jaWVzOjkz
scope.35.kind=field
scope.35.startLine=93
scope.35.endLine=93
scope.35.semanticHash=2b1d41afe1b796e92a6cb5effc15799b780bb8e75f31d0ba6821ec8a5f8f5b85
scope.36.id=ZmllbGQ6V29ybGQjcG9tTW9kdWxlRGlyZWN0b3J5Ojky
scope.36.kind=field
scope.36.startLine=92
scope.36.endLine=92
scope.36.semanticHash=1351d74be016d411ecce06bb315bd3458184c06a6ec6d5b33d6d66f492662b0f
scope.37.id=ZmllbGQ6V29ybGQjcG9tUGx1Z2luc0luc3BlY3RlZDo5NQ
scope.37.kind=field
scope.37.startLine=95
scope.37.endLine=95
scope.37.semanticHash=2550df6aca485470eefa6327fd7943c766bdbd2072b7209b8ebd37ed005e192a
scope.38.id=ZmllbGQ6V29ybGQjcXVldWVkQ2hhbmNlQ2FyZHM6Nzg
scope.38.kind=field
scope.38.startLine=78
scope.38.endLine=78
scope.38.semanticHash=501412316e773a530d2067f56c93b05827e34014c4cc10de460ce49fcb2bfd76
scope.39.id=ZmllbGQ6V29ybGQjcXVldWVkQ29tbXVuaXR5Q2hlc3RDYXJkczo3OQ
scope.39.kind=field
scope.39.startLine=79
scope.39.endLine=79
scope.39.semanticHash=baebc97f9a1675ffcc90733e3254dfd654fd49a357703209b4392cd183cc55b4
scope.40.id=ZmllbGQ6V29ybGQjcXVldWVkUGF3blJvbGxzOjc3
scope.40.kind=field
scope.40.startLine=77
scope.40.endLine=77
scope.40.semanticHash=80975cf58a5468a0ca2a8c7c3ce7fe63c3bda0986a2d8f5e78ab553cc1be2ba7
scope.41.id=ZmllbGQ6V29ybGQjcXVldWVkUm9sbHM6NzY
scope.41.kind=field
scope.41.startLine=76
scope.41.endLine=76
scope.41.semanticHash=cdd5d466b6b78c03bf3aedf101b35f6b234ec3bbd971bc2e11dc7e5680d88653
scope.42.id=ZmllbGQ6V29ybGQjcmVuZGVyZWRFdmVudFRleHQ6MTA4
scope.42.kind=field
scope.42.startLine=108
scope.42.endLine=108
scope.42.semanticHash=2a5528fb60a9b72898c90b666ec82f59f123f32a8b0cefd0f7769e2979b4f5d3
scope.43.id=ZmllbGQ6V29ybGQjcm9sbHM6NzU
scope.43.kind=field
scope.43.startLine=75
scope.43.endLine=75
scope.43.semanticHash=d4b8d300a2a29cdf4a0b79d4433e01625df336cf374978c5a0263b285e6b097f
scope.44.id=ZmllbGQ6V29ybGQjcnVsZVNldDo2OQ
scope.44.kind=field
scope.44.startLine=69
scope.44.endLine=69
scope.44.semanticHash=e4127285861f6e86c1e79db9475e1350d5d29e5ae730c9f356aae4dc040fae69
scope.45.id=ZmllbGQ6V29ybGQjcnVubmluZ1NpbXVsYXRvcjo5MA
scope.45.kind=field
scope.45.startLine=90
scope.45.endLine=90
scope.45.semanticHash=d6226f028403ac503f5b3a6afd0c8b4ddccbae13185a4ee60edc2f21e54b0b26
scope.46.id=ZmllbGQ6V29ybGQjc2VsZWN0ZWRFdmVudDoxMDc
scope.46.kind=field
scope.46.startLine=107
scope.46.endLine=107
scope.46.semanticHash=b3d912d758cac2da8182c886f9d446dc064dcb8548a0fffe6ee3d016d914e61a
scope.47.id=ZmllbGQ6V29ybGQjc2ltdWxhdG9yTGVnYWxFbnRpdHlUcmFkaW5nOjEwNg
scope.47.kind=field
scope.47.startLine=106
scope.47.endLine=106
scope.47.semanticHash=e144bf65a8b44b0e4ff259feef56415ce1d986184f1d303faa2414f2f95fb2f7
scope.48.id=ZmllbGQ6V29ybGQjc2ltdWxhdG9yUGxheWVyczo4Nw
scope.48.kind=field
scope.48.startLine=87
scope.48.endLine=87
scope.48.semanticHash=33cbd4a3c8e5fe79b2b0b306ba98fc3c2298383c44c02fe8801b5104aab37f8a
scope.49.id=ZmllbGQ6V29ybGQjc2ltdWxhdG9yUmVzdWx0Ojg5
scope.49.kind=field
scope.49.startLine=89
scope.49.endLine=89
scope.49.semanticHash=e8529f698653a2a2d2860d7cc77e06e6d93ade35cdccd815c14e1418bad3a33b
scope.50.id=ZmllbGQ6V29ybGQjc2ltdWxhdG9yU3RhbGVtYXRlVHJhZGluZzoxMDU
scope.50.kind=field
scope.50.startLine=105
scope.50.endLine=105
scope.50.semanticHash=3833031259df776f4afaa1c85a7a926544f2da05e51c46e2921690bd23e37d45
scope.51.id=ZmllbGQ6V29ybGQjc2ltdWxhdG9yU3RyYXRlZ2llczo4OA
scope.51.kind=field
scope.51.startLine=88
scope.51.endLine=88
scope.51.semanticHash=3fe4a74b0136356bccaf21d7857dc12df7988b29bd8646beacdb81366515f756
scope.52.id=ZmllbGQ6V29ybGQjc3BhY2U6NzA
scope.52.kind=field
scope.52.startLine=70
scope.52.endLine=70
scope.52.semanticHash=f80396cf8334cb518acf946a8a7501b703003e046f82053b8348acdc327e1b8d
scope.53.id=ZmllbGQ6V29ybGQjc3RhbGVtYXRlVHJhZGluZzoxMDM
scope.53.kind=field
scope.53.startLine=103
scope.53.endLine=103
scope.53.semanticHash=78344cba0be51ab9cf4f565182c5303ff497f82754389f0ec201c0373ff95280
scope.54.id=ZmllbGQ6V29ybGQjdHJhZGVBY2NlcHRlZDoxMDA
scope.54.kind=field
scope.54.startLine=100
scope.54.endLine=100
scope.54.semanticHash=edcacada9f1125976771efba6ed336041284f180d38ef86e492dabf97700add0
scope.55.id=ZmllbGQ6V29ybGQjdHVybk9yZGVyOjgx
scope.55.kind=field
scope.55.startLine=81
scope.55.endLine=81
scope.55.semanticHash=d8c91e121858f51e77b30145e07ae4f8dfe41ead5188495344d2de921cadf719
scope.56.id=ZmllbGQ6V29ybGQuU2NyaXB0ZWQjYmlkczoxNDI5
scope.56.kind=field
scope.56.startLine=1429
scope.56.endLine=1429
scope.56.semanticHash=cdd714ce50c0d38f4294529b6448354824b1af339e09fe30626800e02157a6eb
scope.57.id=ZmllbGQ6V29ybGQuU2NyaXB0ZWQjYm91Z2h0OjE0Mjg
scope.57.kind=field
scope.57.startLine=1428
scope.57.endLine=1428
scope.57.semanticHash=22e152babfcf67ff703bd8e8f8d7e5b5bc1858852ecfc3193162e0be77ca214d
scope.58.id=ZmllbGQ6V29ybGQuU2NyaXB0ZWQjYnVpbGRzOjE0MzA
scope.58.kind=field
scope.58.startLine=1430
scope.58.endLine=1430
scope.58.semanticHash=22a36db848d19d6e1c9aafab5f407d376a584e7b76e219084aba93aa64928db7
scope.59.id=ZmllbGQ6V29ybGQuU2NyaXB0ZWQjZGVjbGluZWQ6MTQyNw
scope.59.kind=field
scope.59.startLine=1427
scope.59.endLine=1427
scope.59.semanticHash=b7ca9d0d32d1aa1f2d3397b60f5c0a7223da34e5cc81f42a64ad3f87a293f487
scope.60.id=ZmllbGQ6V29ybGQuU2NyaXB0ZWQjcGF5c0phaWxGaW5lOjE0MzE
scope.60.kind=field
scope.60.startLine=1431
scope.60.endLine=1431
scope.60.semanticHash=02d93fe8228b3a28cfe45238cbfd3db06bf2bc55df6f32c56247975c18984c82
scope.61.id=bWV0aG9kOldvcmxkI2FycmFuZ2VIb3RlbCgxKTo1OTI
scope.61.kind=method
scope.61.startLine=592
scope.61.endLine=595
scope.61.semanticHash=a43edb2921abe8a02cdf53d2ca05d70ae3af85787226ee2ba6fd966a3d137477
scope.62.id=bWV0aG9kOldvcmxkI2FycmFuZ2VIb3VzZXMoMik6NTg3
scope.62.kind=method
scope.62.startLine=587
scope.62.endLine=590
scope.62.semanticHash=a780ad9739e4120342880b732ccc10c58355531cc2adc4b8b90aa6f9bb1cbcaf
scope.63.id=bWV0aG9kOldvcmxkI2FycmFuZ2VNb3J0Z2FnZWQoMSk6NTk3
scope.63.kind=method
scope.63.startLine=597
scope.63.endLine=600
scope.63.semanticHash=54a584720ccfd8630808931eb0b7cb8ea24ff8600581d4e8a981cf6e6d653567
scope.64.id=bWV0aG9kOldvcmxkI2FycmFuZ2VPckFzc2VydEhvdXNlcygyKTo4MjA
scope.64.kind=method
scope.64.startLine=820
scope.64.endLine=824
scope.64.semanticHash=b85386025aba67450d4344b3c6f652f88f48b9707250dbc0bc9e18800b8f4bec
scope.65.id=bWV0aG9kOldvcmxkI2FycmFuZ2VQYXduQmFsYW5jZSgyKToxMTEy
scope.65.kind=method
scope.65.startLine=1112
scope.65.endLine=1122
scope.65.semanticHash=3e1c6036818f5cbfd4ac72df26f83753f2a351a5413aeb6b3325e74997d98048
scope.66.id=bWV0aG9kOldvcmxkI2Fzc2VydEV4ZWN1dGFibGVKYXIoMSk6MTI0MQ
scope.66.kind=method
scope.66.startLine=1241
scope.66.endLine=1245
scope.66.semanticHash=d1b563cd9b1e34b69c13126ae84c11b383ec96e55054674b11a18a9dcc51596b
scope.67.id=bWV0aG9kOldvcmxkI2Fzc2VydEdyZWVkb1ByaW9yaXR5KDIpOjg3Mw
scope.67.kind=method
scope.67.startLine=873
scope.67.endLine=875
scope.67.semanticHash=1e5b08e6275bdb12489f3254a47f77bab7c50a9874acc3cef8de21a96900bec5
scope.68.id=bWV0aG9kOldvcmxkI2Fzc2VydEdyZWVkb1RyYWRlRGVjaXNpb24oMSk6ODk1
scope.68.kind=method
scope.68.startLine=895
scope.68.endLine=897
scope.68.semanticHash=370ec19cbad1f7e7e06f26a5f6fc2affff3b26003c64ebba83297e0f7acf706b
scope.69.id=bWV0aG9kOldvcmxkI2Fzc2VydExhc3RDaGVja2VkUG9tRGVwZW5kZW5jeVZlcnNpb25BdExlYXN0KDEpOjEzODE
scope.69.kind=method
scope.69.startLine=1381
scope.69.endLine=1391
scope.69.semanticHash=fed5ff55ac4eb2ce23fdcce1d414f1b6048db05e78a624451304bc96e974e8e6
scope.70.id=bWV0aG9kOldvcmxkI2Fzc2VydExvZ2dlZEV2ZW50VGV4dE1hdGNoZXNSZXBvcnRSZW5kZXJpbmcoMCk6MTIxMw
scope.70.kind=method
scope.70.startLine=1213
scope.70.endLine=1223
scope.70.semanticHash=8258642347d6676434d373a6ac028c432d900b5d4832696f452bde67df291f16
scope.71.id=bWV0aG9kOldvcmxkI2Fzc2VydFBhY2thZ2VkQ2xpTGVnYWxFbnRpdHkoMSk6MTMwOQ
scope.71.kind=method
scope.71.startLine=1309
scope.71.endLine=1320
scope.71.semanticHash=2461afffb30f5e4d53dca70e982e35ca016def51bef29f6e0c80529d488da723
scope.72.id=bWV0aG9kOldvcmxkI2Fzc2VydFBhY2thZ2VkQ2xpU3RhbGVtYXRlVHJhZGluZygxKToxMjk2
scope.72.kind=method
scope.72.startLine=1296
scope.72.endLine=1307
scope.72.semanticHash=35f90ea971c329f31d194323a9a175c14fdc1bab5473f627cd94aed57d95033a
scope.73.id=bWV0aG9kOldvcmxkI2Fzc2VydFBhY2thZ2VkQ2xpU3VjY2VlZGVkKDApOjEzMzc
scope.73.kind=method
scope.73.startLine=1337
scope.73.endLine=1340
scope.73.semanticHash=70c913882fa8d2abe092fa3af800bfaa48e92f2ce6f318a69f7adb07e33cb7ad
scope.74.id=bWV0aG9kOldvcmxkI2Fzc2VydFBhY2thZ2VkQ2xpVXNhZ2UoMCk6MTM0Mg
scope.74.kind=method
scope.74.startLine=1342
scope.74.endLine=1345
scope.74.semanticHash=01d6ee414006b6b0cd412131fc80079d158a1f027a15edfd447affdcd6841a9a
scope.75.id=bWV0aG9kOldvcmxkI2Fzc2VydFBvbURlY2xhcmVzRGVwZW5kZW5jeSgxKToxMzcx
scope.75.kind=method
scope.75.startLine=1371
scope.75.endLine=1379
scope.75.semanticHash=71ea1c78e2f05e341cd5a09babe6028765efce3ad55fc13ad0186aa4563f4907
scope.76.id=bWV0aG9kOldvcmxkI2Fzc2VydFByaW9yaXR5KDMpOjg3Nw
scope.76.kind=method
scope.76.startLine=877
scope.76.endLine=881
scope.76.semanticHash=a9e94ef7e27ffebe70aefed74d7e85eaa8adae6062766c27c38f1dc3994485b4
scope.77.id=bWV0aG9kOldvcmxkI2Fzc2VydFJlYWRtZVVzYWdlRmxhZygxKToxMzQ3
scope.77.kind=method
scope.77.startLine=1347
scope.77.endLine=1355
scope.77.semanticHash=4223ec9b6c340b325055f41834805f2fce2f52ee8c4220bb1d7322696825c4b1
scope.78.id=bWV0aG9kOldvcmxkI2Fzc2VydFRyYWRlRGVjaXNpb24oMik6ODk5
scope.78.kind=method
scope.78.startLine=899
scope.78.endLine=903
scope.78.semanticHash=2542d789bc86ac2c99c6a23a40415803add1e7612dadc99588b3d70e22e63964
scope.79.id=bWV0aG9kOldvcmxkI2F3YWl0R2FtZUxvZygzKToyNzI
scope.79.kind=method
scope.79.startLine=272
scope.79.endLine=284
scope.79.semanticHash=ddbf232ba4726c03d65303cc3693745a4625b8514015b8732122c5ee92051a58
scope.80.id=bWV0aG9kOldvcmxkI2F3YWl0U2ltdWxhdG9yRW5kKDApOjI1Mg
scope.80.kind=method
scope.80.startLine=252
scope.80.endLine=255
scope.80.semanticHash=c3cadf1261479dab6a49ad7d572ffcd673d20ea12bd9a591568402c8c4741cb9
scope.81.id=bWV0aG9kOldvcmxkI2JhbmtIYXNBbGxJbXByb3ZlbWVudHMoMCk6MTgz
scope.81.kind=method
scope.81.startLine=183
scope.81.endLine=187
scope.81.semanticHash=59a1099020f2227df0995d916e7e5470e8fb2787e4dde7678ba5902d372f9984
scope.82.id=bWV0aG9kOldvcmxkI2JhbmtPd25zRXZlcnlPd25hYmxlU3BhY2UoMCk6MTc3
scope.82.kind=method
scope.82.startLine=177
scope.82.endLine=181
scope.82.semanticHash=89f7d4b6180cd24c610d1311c3b53be6b58dccf62d6093788520096970e21b0a
scope.83.id=bWV0aG9kOldvcmxkI2JhbmtydXB0UGF3bnMoMSk6NDYx
scope.83.kind=method
scope.83.startLine=461
scope.83.endLine=480
scope.83.semanticHash=949a5e01feb5dcdaa4bb80c6f72254d8d522eeddf88f42b2c1040f9db4733edf
scope.84.id=bWV0aG9kOldvcmxkI2J1eW91dFBheW1lbnQoMCk6MzI0
scope.84.kind=method
scope.84.startLine=324
scope.84.endLine=327
scope.84.semanticHash=70b402ef06693d828fa40ace01cded31289af1c903a83c99e8a5af210590a837
scope.85.id=bWV0aG9kOldvcmxkI2J1eW91dFdpbm5lcklzKDEpOjMxNg
scope.85.kind=method
scope.85.startLine=316
scope.85.endLine=318
scope.85.semanticHash=5b4ff85538ce74f7a8dd404a762bd2cfbe97f16d1b71978e001906c7553a2bf8
scope.86.id=bWV0aG9kOldvcmxkI2NhcmREZWNrc0FyZUNvbXBsZXRlKDApOjE4OQ
scope.86.kind=method
scope.86.startLine=189
scope.86.endLine=191
scope.86.semanticHash=1cb562bd071e05705c3f9fd5a1a249b9c6213658b691f25c33963a2f59e224d0
scope.87.id=bWV0aG9kOldvcmxkI2NvbG91ckdyb3VwT3duZWRCeUVudGl0eSgxKTo3MTc
scope.87.kind=method
scope.87.startLine=717
scope.87.endLine=722
scope.87.semanticHash=35754415a88c92fa67da632ee94bc73143846c15ef06743601ce45f2e19076d7
scope.88.id=bWV0aG9kOldvcmxkI2NvbG91clN0cmVldCgxKToxMzk5
scope.88.kind=method
scope.88.startLine=1399
scope.88.endLine=1404
scope.88.semanticHash=5ab5a5796d948176d1c5215f0e71c726c4b8605d8b2b03fa103bd4c88670f89c
scope.89.id=bWV0aG9kOldvcmxkI2NvbXBsZXRlTWFya2V0RGVhZGxvY2tSb3VuZCgxKToxMTcw
scope.89.kind=method
scope.89.startLine=1170
scope.89.endLine=1172
scope.89.semanticHash=ff0bb01f5c5cef9b6ec6405a2881551c5efe346c9a819fda1e43c6f4709e8c6a
scope.90.id=bWV0aG9kOldvcmxkI2NvbmZpZ3VyZVNpbXVsYXRvcigyKToyMDc
scope.90.kind=method
scope.90.startLine=207
scope.90.endLine=210
scope.90.semanticHash=a8692db9e364ca912001ea5d62ff7534dba411aa065f756570e89157bea6afb5
scope.91.id=bWV0aG9kOldvcmxkI2NvbmZpZ3VyZVNpbXVsYXRvclJhdygxKToyOTg
scope.91.kind=method
scope.91.startLine=298
scope.91.endLine=309
scope.91.semanticHash=34d30685f402e4709329131b3e5e1cf097be5c0a8fef644b7675c5d0bfc5f4da
scope.92.id=bWV0aG9kOldvcmxkI2NvbmZpZ3VyZVNpbXVsYXRvcldpdGhHcmVlZG8oMCk6MjEy
scope.92.kind=method
scope.92.startLine=212
scope.92.endLine=215
scope.92.semanticHash=f5b9ad30c853f17f6f78ea07811ddc673fd081fb85028f7e451a856e096047a1
scope.93.id=bWV0aG9kOldvcmxkI2NvbnNpZGVyRm9ybWluZ0xlZ2FsRW50aXR5KDIpOjYzMw
scope.93.kind=method
scope.93.startLine=633
scope.93.endLine=646
scope.93.semanticHash=cbcc1efe53afb89b7db92e91d9633f9dc7be1651b483fee50779d8065251727d
scope.94.id=bWV0aG9kOldvcmxkI2N0b3IoMCk6NTc
scope.94.kind=method
scope.94.startLine=1
scope.94.endLine=1479
scope.94.semanticHash=b93f2114907b64feca29ab3f7144a47f87edb9edbdd7b4c78bc57731a9110c59
scope.95.id=bWV0aG9kOldvcmxkI2VuYWJsZUxlZ2FsRW50aXR5VHJhZGluZygxKTo2MjY
scope.95.kind=method
scope.95.startLine=626
scope.95.endLine=631
scope.95.semanticHash=f8edd215a3efd072685950ca911482464d0ea2c22faaabb6fd6687f13548586d
scope.96.id=bWV0aG9kOldvcmxkI2VuYWJsZVN0YWxlbWF0ZVRyYWRpbmcoMSk6NjIw
scope.96.kind=method
scope.96.startLine=620
scope.96.endLine=624
scope.96.semanticHash=d93244cfe915d56e093c1d9108b7231566b3ae176ae6020f03fc175494b31409
scope.97.id=bWV0aG9kOldvcmxkI2VuZGVkSW5TdGFsZW1hdGUoMCk6NDg2
scope.97.kind=method
scope.97.startLine=486
scope.97.endLine=488
scope.97.semanticHash=6ef72d193d0d314f0e1df2a82c6c23c16863dee16067bc9088210bb2ef42c5fd
scope.98.id=bWV0aG9kOldvcmxkI2VudGl0eUJhbmtCYWxhbmNlKDEpOjgwNg
scope.98.kind=method
scope.98.startLine=806
scope.98.endLine=809
scope.98.semanticHash=4e38b891c8787cdf5063b8ed7a166b17e1381e5e3e6b277d1fb2886e03d2eb9b
scope.99.id=bWV0aG9kOldvcmxkI2VudGl0eUJhbmtIb2xkcygyKTo3NzY
scope.99.kind=method
scope.99.startLine=776
scope.99.endLine=781
scope.99.semanticHash=4ffb9b03a8abb8fd39dafa4d9a2c455e605be94b5e4c538e7c53bbfdc6577ed4
scope.100.id=bWV0aG9kOldvcmxkI2VudGl0eUhhc0FscmVhZHlPcGVyYXRlZCgxKTo3ODM
scope.100.kind=method
scope.100.startLine=783
scope.100.endLine=786
scope.100.semanticHash=942ad3782a367c890f1d828490b646e613c6cc8c8a0e8b00565c4606177c2401
scope.101.id=bWV0aG9kOldvcmxkI2VudGl0eUlzRGlzc29sdmVkKDEpOjY4MQ
scope.101.kind=method
scope.101.startLine=681
scope.101.endLine=683
scope.101.semanticHash=8334c38d06db2185414393033919bd0e08aa88ea12fa44668593a92c8948e1a4
scope.102.id=bWV0aG9kOldvcmxkI2VudGl0eUlzTm90RGlzc29sdmVkKDEpOjY4NQ
scope.102.kind=method
scope.102.startLine=685
scope.102.endLine=687
scope.102.semanticHash=1aaec31d05f54043d8149f8a6dc5aae498a7cd37010a327ac2c582ecfbedc4ef
scope.103.id=bWV0aG9kOldvcmxkI2VudGl0eUxhc3RDYXBpdGFsaXplZFNoYXJlaG9sZGVyKDIpOjc4OA
scope.103.kind=method
scope.103.startLine=788
scope.103.endLine=792
scope.103.semanticHash=f26b39fcd99b0fb78302f9e4a153ab4b359b0f0cf10174f71fb277acb83907ba
scope.104.id=bWV0aG9kOldvcmxkI2VudGl0eUxhc3RDYXBpdGFsaXplZFNoYXJlaG9sZGVyR3Jld09sZGVyKDEpOjgwMA
scope.104.kind=method
scope.104.startLine=800
scope.104.endLine=804
scope.104.semanticHash=cd58a5c9d5c731ad84adc76c77d3f3186ece50835b78e42df8759f172eb7d559
scope.105.id=bWV0aG9kOldvcmxkI2VudGl0eUxhc3RDYXBpdGFsaXplZFNoYXJlaG9sZGVySGFzTm90QWdlZCgxKTo3OTQ
scope.105.kind=method
scope.105.startLine=794
scope.105.endLine=798
scope.105.semanticHash=82cc47f3e25852a8adb0595599579943db23f79be0840b295a293163f142f617
scope.106.id=bWV0aG9kOldvcmxkI2VudGl0eUxvYW4oMSk6Nzcx
scope.106.kind=method
scope.106.startLine=771
scope.106.endLine=774
scope.106.semanticHash=4fc51dbd5d6d8719c13f7c326b41de02fc8afbf48d4a673b062187fef2b23a8e
scope.107.id=bWV0aG9kOldvcmxkI2VudGl0eUxvYW5GdWxseVJlcGFpZCgxKTo3NjU
scope.107.kind=method
scope.107.startLine=765
scope.107.endLine=769
scope.107.semanticHash=df023154fccf4b92de7a80de74fb7a46f8408f6c6849656f7db245fdaaac32d6
scope.108.id=bWV0aG9kOldvcmxkI2VudGl0eU93ZXMoMik6NzQ5
scope.108.kind=method
scope.108.startLine=749
scope.108.endLine=752
scope.108.semanticHash=33cbd73a4535e71cd72509194488a90e15dbeadd5074b97d4d8d060cf2a21464
scope.109.id=bWV0aG9kOldvcmxkI2VudGl0eVJhaXNlc0xvYW4oMik6NzU0
scope.109.kind=method
scope.109.startLine=754
scope.109.endLine=757
scope.109.semanticHash=745a3c1d292c958ef7dc13c838edad96afd1fda9134d3533fc37f61aa366f260
scope.110.id=bWV0aG9kOldvcmxkI2V4Y2hhbmdlSG90ZWxGb3JIb3VzZXMoMik6MTA0MQ
scope.110.kind=method
scope.110.startLine=1041
scope.110.endLine=1045
scope.110.semanticHash=f49f26807b4735c79bdf4b496956de7db5aed80714fae97cf951347f2c7f7f7e
scope.111.id=bWV0aG9kOldvcmxkI2Zvcm1FbnRpdHkoMyk6NjYx
scope.111.kind=method
scope.111.startLine=661
scope.111.endLine=679
scope.111.semanticHash=4ea695b2a1e6f1bc779546c375174341f281e572d565b83263baf26c91ffc3a4
scope.112.id=bWV0aG9kOldvcmxkI2Zvcm1OYW1lZEVudGl0eSgxKTo2NDg
scope.112.kind=method
scope.112.startLine=648
scope.112.endLine=659
scope.112.semanticHash=492d130014bffdeef77e4ebdb904f1c85a2b30ea63abd75e1d5b9eb058919c4f
scope.113.id=bWV0aG9kOldvcmxkI2Z1bmRQbGF5ZXIoMSk6MzQx
scope.113.kind=method
scope.113.startLine=341
scope.113.endLine=343
scope.113.semanticHash=88b0ecd91af3e946ec491dda02c20426d810db25605639fec3d89a131b0097e5
scope.114.id=bWV0aG9kOldvcmxkI2dhbWVMb2coMCk6MjYz
scope.114.kind=method
scope.114.startLine=263
scope.114.endLine=265
scope.114.semanticHash=3e8ec3edfe11ecb5828fb2c9be413abe0731fe9fbcccc7c33c29c9411feedd1d
scope.115.id=bWV0aG9kOldvcmxkI2dpdmVQYXduR2V0T3V0T2ZKYWlsRnJlZUNhcmQoMSk6NDkw
scope.115.kind=method
scope.115.startLine=490
scope.115.endLine=493
scope.115.semanticHash=619b281848dff73bd7d5dd138ff950024d057a4bf112a10324b25ab8db953805
scope.116.id=bWV0aG9kOldvcmxkI2dpdmVQYXduT3duZXJzaGlwKDIpOjU3Mw
scope.116.kind=method
scope.116.startLine=573
scope.116.endLine=585
scope.116.semanticHash=3b87d5575ae584c4df75f46140ccabc757b52dd9d5283d59b3ad2b039e8f3bcd
scope.117.id=bWV0aG9kOldvcmxkI2dpdmVTaW11bGF0b3JBcmd1bWVudCgxKToyMTc
scope.117.kind=method
scope.117.startLine=217
scope.117.endLine=222
scope.117.semanticHash=4ffaa2b46d1d4ae28729c1d576fcd2fc9db006383499035a86b9ef14f56d2c20
scope.118.id=bWV0aG9kOldvcmxkI2hhc0hvdGVsT24oMSk6NjA3
scope.118.kind=method
scope.118.startLine=607
scope.118.endLine=610
scope.118.semanticHash=3ff4a67bb199dec15c5b65df9d08d9b5634b6785b6d2905eec3daf5b976a1138
scope.119.id=bWV0aG9kOldvcmxkI2hhc1dvbigxKTo0ODI
scope.119.kind=method
scope.119.startLine=482
scope.119.endLine=484
scope.119.semanticHash=1b91b47afbe2f61bd6ac043374362c1e4a37d573f56b5dc8b088cda7fc19d191
scope.120.id=bWV0aG9kOldvcmxkI2hvbGRQYXduQmFsYW5jZSgyKToxMTI5
scope.120.kind=method
scope.120.startLine=1129
scope.120.endLine=1134
scope.120.semanticHash=51f9823c0f1707cb4d25a4acd57a96295b25db9b3db1b0dbb986eeccfca4e7bc
scope.121.id=bWV0aG9kOldvcmxkI2hvbGRzR2V0T3V0T2ZKYWlsRnJlZUNhcmQoMSk6MTA5NQ
scope.121.kind=method
scope.121.startLine=1095
scope.121.endLine=1098
scope.121.semanticHash=1006b31f30096e6b628a34a07b71eeec24c28c09fd31fb351258a184bcde7e6f
scope.122.id=bWV0aG9kOldvcmxkI2hvdXNlc0J1aWx0KDEpOjgxNg
scope.122.kind=method
scope.122.startLine=816
scope.122.endLine=818
scope.122.semanticHash=57ff6e7fb1132c26b5b89b94d180f7f1986e9f251156592abd344efb30772c31
scope.123.id=bWV0aG9kOldvcmxkI2hvdXNlc0J1aWx0T24oMSk6NjAy
scope.123.kind=method
scope.123.startLine=602
scope.123.endLine=605
scope.123.semanticHash=c7d850d7c54484aa102bc7bfce9df9470c7cf09360e3234ce9ae584b17734495
scope.124.id=bWV0aG9kOldvcmxkI2luc3BlY3RQb21EZXBlbmRlbmNpZXMoMCk6MTIyOQ
scope.124.kind=method
scope.124.startLine=1229
scope.124.endLine=1233
scope.124.semanticHash=98884bb0e65d7986118578a254f9394250444616711e3c01b823bcafc1bf4c34
scope.125.id=bWV0aG9kOldvcmxkI2luc3BlY3RQb21QbHVnaW5zKDApOjEyMzU
scope.125.kind=method
scope.125.startLine=1235
scope.125.endLine=1239
scope.125.semanticHash=5dc22a9154543289a5c46e2f13baf30663435223a23bd71e0dc1e8857d4aae6c
scope.126.id=bWV0aG9kOldvcmxkI2lzQmFua3J1cHQoMSk6NDQ5
scope.126.kind=method
scope.126.startLine=449
scope.126.endLine=451
scope.126.semanticHash=5990c90bbc247f86ce55ffe1ceee12e0886f238fb9b27cde99f02e54d407ab7f
scope.127.id=bWV0aG9kOldvcmxkI2lzSW5KYWlsKDEpOjQ0NQ
scope.127.kind=method
scope.127.startLine=445
scope.127.endLine=447
scope.127.semanticHash=415704590d9af6ce6f913687b687e9974e70a4e111a1cdc496b79a455b271b21
scope.128.id=bWV0aG9kOldvcmxkI2lzTGVnYWxFbnRpdHlUcmFkaW5nKDApOjExNQ
scope.128.kind=method
scope.128.startLine=115
scope.128.endLine=117
scope.128.semanticHash=30706d86ca6fba2c16e1055248d2cb48f6b220a2d155d16a74088852567c5d91
scope.129.id=bWV0aG9kOldvcmxkI2lzTW9ydGdhZ2VkKDEpOjYxMg
scope.129.kind=method
scope.129.startLine=612
scope.129.endLine=614
scope.129.semanticHash=aad88bfcc4aaed4332ac49906eb984a066e2a27473903d5c92f9cb72880656e7
scope.130.id=bWV0aG9kOldvcmxkI2lzU3RhbGVtYXRlVHJhZGluZygwKToxMTE
scope.130.kind=method
scope.130.startLine=111
scope.130.endLine=113
scope.130.semanticHash=27c3552a075caea866823e209cb39b832439f400452c121a0339dbbec78fbe57
scope.131.id=bWV0aG9kOldvcmxkI2pvdXJuYWwoMCk6MTAyMQ
scope.131.kind=method
scope.131.startLine=1021
scope.131.endLine=1025
scope.131.semanticHash=9353ab1c5c27d95c3dee2bcc07218ac0e39fbfaa046854916b51194457a218b4
scope.132.id=bWV0aG9kOldvcmxkI2tlZXBNb3J0Z2FnZWQoMik6MTA2NA
scope.132.kind=method
scope.132.startLine=1064
scope.132.endLine=1068
scope.132.semanticHash=bb73dd3ba341cf556ec6fd9fd75a5413b3a2a78db9d57209c02f0efab04874ac
scope.133.id=bWV0aG9kOldvcmxkI2xhbmRQYXduT24oMik6NTQ2
scope.133.kind=method
scope.133.startLine=546
scope.133.endLine=551
scope.133.semanticHash=150d600fd2916b2a6c26f3877c4173324425d26b18872f07b10e2db4cea3c14c
scope.134.id=bWV0aG9kOldvcmxkI2xlZ2FsRW50aXR5KDEpOjc0NA
scope.134.kind=method
scope.134.startLine=744
scope.134.endLine=747
scope.134.semanticHash=5d6868b774ee0d94e5278296e8c3dd6042a2368c3262ef6632b0fa109615efe8
scope.135.id=bWV0aG9kOldvcmxkI2xldFRoZU90aGVyc1JvbGxXaGF0VGhleUxpa2UoMCk6MTE1MQ
scope.135.kind=method
scope.135.startLine=1151
scope.135.endLine=1153
scope.135.semanticHash=16d460a52f5be5162513e1403a31948f81203a9313db81cc0b89b4556a621fae
scope.136.id=bWV0aG9kOldvcmxkI2xpZnRNb3J0Z2FnZSgyKToxMDU2
scope.136.kind=method
scope.136.startLine=1056
scope.136.endLine=1062
scope.136.semanticHash=d4d7a8928e4748a5aa46a96fe328f9b9751d0d5c0a789f88698a82d00f167fcf
scope.137.id=bWV0aG9kOldvcmxkI2xvZ1NlbGVjdGVkRXZlbnRUb0pvdXJuYWwoMCk6MTIwNw
scope.137.kind=method
scope.137.startLine=1207
scope.137.endLine=1211
scope.137.semanticHash=af7b48652bf24cb266d38b14190e05ba9078b66c43acb6ca8580904b8f1678dd
scope.138.id=bWV0aG9kOldvcmxkI21hcmtldERlYWRsb2NrQ2FuRnVuZCgxKToxMTU1
scope.138.kind=method
scope.138.startLine=1155
scope.138.endLine=1157
scope.138.semanticHash=0c60e9a996c93d09b674c2571e293787a80a890bbe4a330f2337ff852051d6ff
scope.139.id=bWV0aG9kOldvcmxkI21hcmtldERlYWRsb2NrQ2Fubm90RnVuZCgxKToxMTU5
scope.139.kind=method
scope.139.startLine=1159
scope.139.endLine=1164
scope.139.semanticHash=6e4fafe4b180fed14f4d308fc19dd147f39d090b4a2fcb6d4d775976b8817346
scope.140.id=bWV0aG9kOldvcmxkI21hcmtldERlYWRsb2NrRWxpZ2libGUoMSk6MTE2Ng
scope.140.kind=method
scope.140.startLine=1166
scope.140.endLine=1168
scope.140.semanticHash=e832b5cd7109aa12c505d178729e7598e6b54b9b995c78c3f38ed8a8e5ca45b5
scope.141.id=bWV0aG9kOldvcmxkI21vbm9wb2x5UnVuc0NvbXBsZXRlZCgwKToyMDM
scope.141.kind=method
scope.141.startLine=203
scope.141.endLine=205
scope.141.semanticHash=73f82edbbd250e236136115b4a9e9416258c03ead7f6d1141390baacf2e78e0b
scope.142.id=bWV0aG9kOldvcmxkI21vcnRnYWdlKDIpOjEwNDc
scope.142.kind=method
scope.142.startLine=1047
scope.142.endLine=1054
scope.142.semanticHash=bdeed53898a49758f24c9a472110c86e4dd6ab80fc636afcb80fc740fc3e8334
scope.143.id=bWV0aG9kOldvcmxkI25leHRRdWV1ZWRQYXduUm9sbCgxKToxMTc0
scope.143.kind=method
scope.143.startLine=1174
scope.143.endLine=1183
scope.143.semanticHash=94ddeacd4f337447c31591997a389753c284cf8aec8e26700bc26cc13747561e
scope.144.id=bWV0aG9kOldvcmxkI25leHRRdWV1ZWRSb2xsKDApOjM3OA
scope.144.kind=method
scope.144.startLine=378
scope.144.endLine=382
scope.144.semanticHash=383e4dd46cb3c079d910df6fb2c6efd073d21b71b60943a679beebea596d6ff2
scope.145.id=bWV0aG9kOldvcmxkI25vQnV5b3V0V2lubmVyKDApOjMyMA
scope.145.kind=method
scope.145.startLine=320
scope.145.endLine=322
scope.145.semanticHash=895f1798757c7ed87e671219173775b6affd1c1ec2cf6e61b676b28b82283f60
scope.146.id=bWV0aG9kOldvcmxkI25vU2VsZWN0ZWRQbGF5ZXJIb2xkc0dldE91dE9mSmFpbEZyZWVDYXJkKDApOjE5Mw
scope.146.kind=method
scope.146.startLine=193
scope.146.endLine=196
scope.146.semanticHash=1a6c95b28023389210db1500ad691cd399a5caf3a39969519dc945c9ae60a11e
scope.147.id=bWV0aG9kOldvcmxkI293bkV2ZXJ5T3RoZXJPd25hYmxlKDEpOjg0NQ
scope.147.kind=method
scope.147.startLine=845
scope.147.endLine=850
scope.147.semanticHash=d087f1b697fb53a616860052641abae40cfbd46773b6f16387b30725478aef34
scope.148.id=bWV0aG9kOldvcmxkI293bkV2ZXJ5T3RoZXJPd25hYmxlQWx0ZXJuYXRlbHkoMik6ODM3
scope.148.kind=method
scope.148.startLine=837
scope.148.endLine=843
scope.148.semanticHash=b4c7bfe3d0953ce3a48aa431d05fdcdece918faf17e13b40e84ea70cbfb43e78
scope.149.id=bWV0aG9kOldvcmxkI293bkV2ZXJ5T3RoZXJPd25hYmxlUm91bmRSb2JpbigxKTo4NTI
scope.149.kind=method
scope.149.startLine=852
scope.149.endLine=871
scope.149.semanticHash=bfb024fd0c07818111ea3060a8e38b9f954543381964c4fcc010f1df6db05edb
scope.150.id=bWV0aG9kOldvcmxkI293bmFibGUoMSk6MTQwNg
scope.150.kind=method
scope.150.startLine=1406
scope.150.endLine=1411
scope.150.semanticHash=c76b7dd4d2f10f1f7c5257b14d1391e528f14b8cace068b99694fa3ba0edaec8
scope.151.id=bWV0aG9kOldvcmxkI3BhY2thZ2VDbGkoMCk6MTI0Nw
scope.151.kind=method
scope.151.startLine=1247
scope.151.endLine=1251
scope.151.semanticHash=85462fa9b4f96817588a5b28444f5aa69dc124e5fe8bf58f413631d9f453adbf
scope.152.id=bWV0aG9kOldvcmxkI3BhY2thZ2VkQ2xpUHJvY2Vzc0VuZGVkKDApOjEzMzM
scope.152.kind=method
scope.152.startLine=1333
scope.152.endLine=1335
scope.152.semanticHash=960a8da6db710d9d1face583843da022358387c3e6811a0ac553daaf754a0ec6
scope.153.id=bWV0aG9kOldvcmxkI3Bhd24oMSk6Mjg2
scope.153.kind=method
scope.153.startLine=286
scope.153.endLine=296
scope.153.semanticHash=b3dd8ce38638d2776249c721a785e1c25d491a3da52db1a33ad5bbd580ef7328
scope.154.id=bWV0aG9kOldvcmxkI3Bhd25CYWxhbmNlSXNBZnRlclJlbnQoMik6ODEx
scope.154.kind=method
scope.154.startLine=811
scope.154.endLine=814
scope.154.semanticHash=65c87908600c790c5295de2b9c70fc120b9f57cabb4026e6ef64ed5b5a8ce4d4
scope.155.id=bWV0aG9kOldvcmxkI3Bhd25Db25zaWRlcnNUcmFkaW5nKDQpOjg4Mw
scope.155.kind=method
scope.155.startLine=883
scope.155.endLine=885
scope.155.semanticHash=bf592dfb1b9e3ac1d6183f5fae6e153f215e7a38eeb87251602e503da96de8ba
scope.156.id=bWV0aG9kOldvcmxkI3Bhd25Db25zaWRlcnNUcmFkaW5nKDUpOjg4Nw
scope.156.kind=method
scope.156.startLine=887
scope.156.endLine=893
scope.156.semanticHash=756746c60651d68fcf601b184515fa80b89407c987e72406d61888f9479a556e
scope.157.id=bWV0aG9kOldvcmxkI3Bhd25EZWJ0SXNTZXR0bGVkKDEpOjcxMw
scope.157.kind=method
scope.157.startLine=713
scope.157.endLine=715
scope.157.semanticHash=cdeac1902c50b1b7542185d936cdb5a7b22b250d235b69e57e303a5453abea71
scope.158.id=bWV0aG9kOldvcmxkI3Bhd25EZWNsaW5lcygyKTo5MTM
scope.158.kind=method
scope.158.startLine=913
scope.158.endLine=915
scope.158.semanticHash=15d9ef9991fdb1468d083e2ca0aa67389952dd2b7ffe0cee4d43346ced77aa09
scope.159.id=bWV0aG9kOldvcmxkI3Bhd25EZWNsaW5lc1JlbnQoMik6OTg3
scope.159.kind=method
scope.159.startLine=987
scope.159.endLine=994
scope.159.semanticHash=06a26fcebdaa4b50bd3f01b4ff714946dbe8765538c3ffe4df215bda732f6cf8
scope.160.id=bWV0aG9kOldvcmxkI3Bhd25GaW5hbEJhbGFuY2VJcygyKTo0NTM
scope.160.kind=method
scope.160.startLine=453
scope.160.endLine=458
scope.160.semanticHash=3a00576557a588b01c72759e22fea9f03cfc97e99bd64486a60e4394bd4fbc08
scope.161.id=bWV0aG9kOldvcmxkI3Bhd25Gb2xsb3dzKDIpOjYxNg
scope.161.kind=method
scope.161.startLine=616
scope.161.endLine=618
scope.161.semanticHash=370870fa9488e940949235df582c4743d8ca1c9ae14a728f1c3dca98e020b65e
scope.162.id=bWV0aG9kOldvcmxkI3Bhd25Ib2xkc05vRW50aXR5U2hhcmVzKDEpOjczOQ
scope.162.kind=method
scope.162.startLine=739
scope.162.endLine=742
scope.162.semanticHash=8c37bedd6d2801a0f6a78a3f6d30b807ae95f8482494b9f1082544c11d6416cb
scope.163.id=bWV0aG9kOldvcmxkI3Bhd25Ib2xkc1NoYXJlKDIpOjczNQ
scope.163.kind=method
scope.163.startLine=735
scope.163.endLine=737
scope.163.semanticHash=478969d973f2ee52d1fe4b6af8b865d1ff7dd180934942475fe14c460b2c46ba
scope.164.id=bWV0aG9kOldvcmxkI3Bhd25Pd25zKDIpOjU1NA
scope.164.kind=method
scope.164.startLine=554
scope.164.endLine=558
scope.164.semanticHash=6857fc149cd79610f813db3eae89da810642d3fe311bd880ebc54bc5f703eff8
scope.165.id=bWV0aG9kOldvcmxkI3Bhd25Pd25zRXZlcnlGb3JtZXJFbnRpdHlTdHJlZXQoMik6Njg5
scope.165.kind=method
scope.165.startLine=689
scope.165.endLine=694
scope.165.semanticHash=de4afba50adba9c9f12c5f33de516006027b58fa355ee8a97ab0e505c6361c97
scope.166.id=bWV0aG9kOldvcmxkI3Bhd25Pd25zTm9Nb3J0Z2FnZWRQcm9wZXJ0eSgxKTo3Mjk
scope.166.kind=method
scope.166.startLine=729
scope.166.endLine=733
scope.166.semanticHash=2933d483adc425ae998bd76f39e8751abe77421bc10c48ee3b675d66190f436f
scope.167.id=bWV0aG9kOldvcmxkI3Bhd25SZWNlaXZlZEVudGl0eUJhbmtCYWxhbmNlKDIpOjY5Ng
scope.167.kind=method
scope.167.startLine=696
scope.167.endLine=702
scope.167.semanticHash=f1c4b0d0fed94134d52ee87f3a4e6a7dcc5904e78b4cfa2fcd08553db69c8846
scope.168.id=bWV0aG9kOldvcmxkI3Bhd25XaWxsQmlkKDMpOjkxNw
scope.168.kind=method
scope.168.startLine=917
scope.168.endLine=919
scope.168.semanticHash=bca73387b149b5fa9022e18e1374e301675a8418f1cbbaa39f57f063a8fe18f8
scope.169.id=bWV0aG9kOldvcmxkI3Bhd25XaWxsQnVpbGRIb3VzZU9uKDIpOjk1NA
scope.169.kind=method
scope.169.startLine=954
scope.169.endLine=985
scope.169.semanticHash=d8d021ca0ecdd0ced1a5bd50c72b294ff137da95b7b63a1389e852bb9a8aec98
scope.170.id=bWV0aG9kOldvcmxkI3Bhd25XaWxsQnV5KDIpOjkyMQ
scope.170.kind=method
scope.170.startLine=921
scope.170.endLine=952
scope.170.semanticHash=8ad6bd1a78980dcaa538483775f67530602c70f146660035d80ba3700a786bfb
scope.171.id=bWV0aG9kOldvcmxkI3Bhd25XaWxsQ2xhaW1SZW50KDEpOjUxMQ
scope.171.kind=method
scope.171.startLine=511
scope.171.endLine=539
scope.171.semanticHash=f10956853924c3422a74762ce8005ec11572c200ef259d6aa0b62bca5e2e59b3
scope.172.id=bWV0aG9kOldvcmxkI3Bhd25XaWxsUGF5SmFpbEZpbmUoMSk6NDk5
scope.172.kind=method
scope.172.startLine=499
scope.172.endLine=501
scope.172.semanticHash=b725ce40ab07d284029d52ffc6be1f2767507532266341fd0a323d8e4f7dfcb8
scope.173.id=bWV0aG9kOldvcmxkI3Bhd25XaWxsVXNlR2V0T3V0T2ZKYWlsRnJlZUNhcmQoMSk6NDk1
scope.173.kind=method
scope.173.startLine=495
scope.173.endLine=497
scope.173.semanticHash=71b850ea0d5b603ae70fa11a3a1b42244674cf0be124dbb88ad60aeea11b352d
scope.174.id=bWV0aG9kOldvcmxkI3BsYWNlUGF3bigyKTo0Mzc
scope.174.kind=method
scope.174.startLine=437
scope.174.endLine=439
scope.174.semanticHash=09853737b5300e8f46600162fde24ffc5f84d8d66c8911603e06b02601148861
scope.175.id=bWV0aG9kOldvcmxkI3BsYXlBbmRDYXB0dXJlKDEpOjQwOQ
scope.175.kind=method
scope.175.startLine=409
scope.175.endLine=435
scope.175.semanticHash=46dcd40becc575c5abcc8ddda7cc13c4f6462ad2f4434ca041efb589282bab87
scope.176.id=bWV0aG9kOldvcmxkI3BsYXlHYW1lKDApOjQwMQ
scope.176.kind=method
scope.176.startLine=401
scope.176.endLine=403
scope.176.semanticHash=cebcd11dd14cec8d43cd062d659157000377d2fedb46be6513b98a1ab403eba7
scope.177.id=bWV0aG9kOldvcmxkI3BsYXlNb25vcG9seUdhbWVzKDEpOjE5OA
scope.177.kind=method
scope.177.startLine=198
scope.177.endLine=201
scope.177.semanticHash=5d7524f5b1b48edb9e8b47ebb36059c2348ccfa5245464cf6f439500305117b1
scope.178.id=bWV0aG9kOldvcmxkI3BsYXlVcFRvUm91bmRzKDEpOjQwNQ
scope.178.kind=method
scope.178.startLine=405
scope.178.endLine=407
scope.178.semanticHash=eba54cad884f5f8454d226eb417e4507e4bb3a2125a63f173fe0f2df2717e22e
scope.179.id=bWV0aG9kOldvcmxkI3BsYXllcigwKTozMzU
scope.179.kind=method
scope.179.startLine=335
scope.179.endLine=339
scope.179.semanticHash=c5454bec4fb095ff9274a1eaf9a71c1655b7f0d169c2fde69ae07adcbbe46197
scope.180.id=bWV0aG9kOldvcmxkI3BsYXllcklzSW5KYWlsKDApOjUwNw
scope.180.kind=method
scope.180.startLine=507
scope.180.endLine=509
scope.180.semanticHash=bec0f246f784b3b3197f1baab451c959c63a74af3ca95d19be15cc85ee7e9e5e
scope.181.id=bWV0aG9kOldvcmxkI3BsYXllcnMoMCk6MTM5Mw
scope.181.kind=method
scope.181.startLine=1393
scope.181.endLine=1397
scope.181.semanticHash=5e024fd910424a47fea3d2586f9719e272974937573140ba088f29538b982fe7
scope.182.id=bWV0aG9kOldvcmxkI3F1ZXVlQ2hhbmNlQ2FyZCgxKTo5MDU
scope.182.kind=method
scope.182.startLine=905
scope.182.endLine=907
scope.182.semanticHash=ba1ca7e80ab430696934cc7102f1df4c23803289f273fbb9c069ce74b198d855
scope.183.id=bWV0aG9kOldvcmxkI3F1ZXVlQ29tbXVuaXR5Q2hlc3RDYXJkKDEpOjkwOQ
scope.183.kind=method
scope.183.startLine=909
scope.183.endLine=911
scope.183.semanticHash=0dbe008aa4f992b81ac65be212e3bf5f3fe5e0fd48c2a164a721d3f1c8af789e
scope.184.id=bWV0aG9kOldvcmxkI3F1ZXVlSW5pdGlhdGl2ZVJvbGwoMik6Mzg4
scope.184.kind=method
scope.184.startLine=388
scope.184.endLine=390
scope.184.semanticHash=b5b17fd89760b4400c8c2e1f9dc409e25a6e47fe99c382aaba0c9d40a6ea2b98
scope.185.id=bWV0aG9kOldvcmxkI3F1ZXVlUGF3blJvbGwoMik6Mzkz
scope.185.kind=method
scope.185.startLine=393
scope.185.endLine=395
scope.185.semanticHash=82ce500779e3dbb14210232fca599ce78be94a3cb3aa0530372181925b2adb8e
scope.186.id=bWV0aG9kOldvcmxkI3F1ZXVlUm9sbCgxKTozNjg
scope.186.kind=method
scope.186.startLine=368
scope.186.endLine=370
scope.186.semanticHash=0d32e7ffd85400805fdd5a2176428854e88382ae8c1dc7176477e41d538706e7
scope.187.id=bWV0aG9kOldvcmxkI3JlY29yZCgxKToxMDA1
scope.187.kind=method
scope.187.startLine=1005
scope.187.endLine=1009
scope.187.semanticHash=853ca51ff21b9cbf61a9c9658aff4d049fac6d0d88fa40639f319dcefa8240ad
scope.188.id=bWV0aG9kOldvcmxkI3JlbmRlclNlbGVjdGVkRXZlbnRGb3JSZXBvcnQoMCk6MTIwMw
scope.188.kind=method
scope.188.startLine=1203
scope.188.endLine=1205
scope.188.semanticHash=8dc28c231f13f9377556a00494560df7d5665a7f0cc9fe3dcd8014fd620fb334
scope.189.id=bWV0aG9kOldvcmxkI3JlcG9ydCgwKToxMDI4
scope.189.kind=method
scope.189.startLine=1028
scope.189.endLine=1030
scope.189.semanticHash=32934588b6ad44f94ce91d618b6941a66ac80e30bdc772f76abae14b0eb649f9
scope.190.id=bWV0aG9kOldvcmxkI3Jlc29sdmVTcGxpdE1vbm9wb2x5KDIpOjMxMQ
scope.190.kind=method
scope.190.startLine=311
scope.190.endLine=314
scope.190.semanticHash=4991d80a67dc7e28285a3f93fd0b5ffd2e94b5ed1748e13466ebdfd150859ad9
scope.191.id=bWV0aG9kOldvcmxkI3JldHVybkV2ZXJ5U3RyZWV0RXhjZXB0KDIpOjU2MA
scope.191.kind=method
scope.191.startLine=560
scope.191.endLine=570
scope.191.semanticHash=dceca5bd40b3108006fbb8d74a151161aad98b1bdff568b8b4833a2dc03469f6
scope.192.id=bWV0aG9kOldvcmxkI3JvbGxEaWNlKDEpOjM1Mg
scope.192.kind=method
scope.192.startLine=352
scope.192.endLine=359
scope.192.semanticHash=39228c351c98b6226054a304cf7db9ad84215742a7328f4cf30e1d0f69786be8
scope.193.id=bWV0aG9kOldvcmxkI3JvbGxGb3JJbml0aWF0aXZlKDApOjM5Nw
scope.193.kind=method
scope.193.startLine=397
scope.193.endLine=399
scope.193.semanticHash=c2df8ffe51fcc38d9f770019a5557d31cde07e834a254299f997ca677e4c4489
scope.194.id=bWV0aG9kOldvcmxkI3JvbGxUb3RhbGxpbmcoMSk6MTE4Ng
scope.194.kind=method
scope.194.startLine=1186
scope.194.endLine=1191
scope.194.semanticHash=3fba7132076b7427293765258eee0a0e6a73cb5f320a3abbd0d7931261a9268f
scope.195.id=bWV0aG9kOldvcmxkI3JvbGxzKDApOjM2MQ
scope.195.kind=method
scope.195.startLine=361
scope.195.endLine=365
scope.195.semanticHash=8972cd55d8fffc0f387642019cf30ebbdfad3977a6f1500e498dd7b020a2d707
scope.196.id=bWV0aG9kOldvcmxkI3J1bGVTZXQoMCk6MTI0
scope.196.kind=method
scope.196.startLine=124
scope.196.endLine=126
scope.196.semanticHash=54c62ee55842bc420cf4fd1349203dab318f2470198dff43a57be4f5f5b290af
scope.197.id=bWV0aG9kOldvcmxkI3J1blBhY2thZ2VkQ2xpKDEpOjEyNTM
scope.197.kind=method
scope.197.startLine=1253
scope.197.endLine=1266
scope.197.semanticHash=9b238b111030c9f69897e9c8bcb13de91aff1b555696b77d197a5976c2050e3a
scope.198.id=bWV0aG9kOldvcmxkI3J1blByb2Nlc3MoMik6MTM1Nw
scope.198.kind=method
scope.198.startLine=1357
scope.198.endLine=1369
scope.198.semanticHash=55fdded0827959f20839657d90b8a87b3015f0f3d4fe4d5fee6239e251261c09
scope.199.id=bWV0aG9kOldvcmxkI3J1blNpbXVsYXRvcigwKToyMjQ
scope.199.kind=method
scope.199.startLine=224
scope.199.endLine=227
scope.199.semanticHash=62be1a4afdde2c648d0b0f6b76975a89b72708b6e495f55adf89d2fe1b2fa018
scope.200.id=bWV0aG9kOldvcmxkI3NjcmlwdEZvcigxKToxMDEx
scope.200.kind=method
scope.200.startLine=1011
scope.200.endLine=1018
scope.200.semanticHash=d6834795099bd2443f9c9a9854ed47aa6435915f09adbfb1a6a4c8422c0fb1de
scope.201.id=bWV0aG9kOldvcmxkI3NlbGVjdCgxKToxMjg
scope.201.kind=method
scope.201.startLine=128
scope.201.endLine=130
scope.201.semanticHash=bd48008f7aa8ecdf2b3ffb97c4ebbb0ff2497616335fabed6bf32c22d99fa174
scope.202.id=bWV0aG9kOldvcmxkI3NlbGVjdERpY2UoMSk6MzQ1
scope.202.kind=method
scope.202.startLine=345
scope.202.endLine=350
scope.202.semanticHash=6bbd7b4a4c27faeea26d21a33f0d72d6b40906400bbe437eeb7bde0d77a2f5a7
scope.203.id=bWV0aG9kOldvcmxkI3NlbGVjdEV2ZW50KDEpOjExOTk
scope.203.kind=method
scope.203.startLine=1199
scope.203.endLine=1201
scope.203.semanticHash=3c729ac4a86b02bedeaecfc95e5161d9ae815d25f48fdf4cf99c91e033dd43ee
scope.204.id=bWV0aG9kOldvcmxkI3NlbGVjdFBsYXllcnMoMSk6MTUz
scope.204.kind=method
scope.204.startLine=153
scope.204.endLine=163
scope.204.semanticHash=80c02788458dbaae01dac081e4ab827e60243596a44170a0fb484305915536c9
scope.205.id=bWV0aG9kOldvcmxkI3NlbGVjdFBvbU1vZHVsZSgxKToxMjI1
scope.205.kind=method
scope.205.startLine=1225
scope.205.endLine=1227
scope.205.semanticHash=b2ea5ff609570f93229028284eff7351773bcc7bc223085569a71221544bb2f7
scope.206.id=bWV0aG9kOldvcmxkI3NlbGVjdFJ1bGVTZXQoMSk6MTE5
scope.206.kind=method
scope.206.startLine=119
scope.206.endLine=122
scope.206.semanticHash=2486812fe66c396cd5308dd57cb24b2d71a16ee70309a56c6bde08917a91b688
scope.207.id=bWV0aG9kOldvcmxkI3NlbGVjdFN0YW5kYXJkR2FtZVNldHVwKDApOjE2NQ
scope.207.kind=method
scope.207.startLine=165
scope.207.endLine=170
scope.207.semanticHash=ad45fa21930eb42f26a235e38bd4407143716fa703556633ecd03f6112c7c99a
scope.208.id=bWV0aG9kOldvcmxkI3NlbGVjdGVkUGxheWVycygwKToxNzI
scope.208.kind=method
scope.208.startLine=172
scope.208.endLine=175
scope.208.semanticHash=671a8b88d70072d0e23b01dd9fe863fc81abc17801a490f4eeabe8271672dbdc
scope.209.id=bWV0aG9kOldvcmxkI3NlbGxHZXRPdXRPZkphaWxGcmVlQ2FyZCgzKToxMTAw
scope.209.kind=method
scope.209.startLine=1100
scope.209.endLine=1104
scope.209.semanticHash=bf157f8555b83c9592652e57f4fdde1bc93ada73523836b7175eea509803f31f
scope.210.id=bWV0aG9kOldvcmxkI3NlbGxIb3VzZSgyKToxMDMy
scope.210.kind=method
scope.210.startLine=1032
scope.210.endLine=1039
scope.210.semanticHash=64638586abbc194f01a3e77fe75b3aad17814a9cac31a9cdd2e9ac6e0996cc9f
scope.211.id=bWV0aG9kOldvcmxkI3NlbGxMYW5kKDQpOjEwNzA
scope.211.kind=method
scope.211.startLine=1070
scope.211.endLine=1093
scope.211.semanticHash=17a70227fd9b7b9eaf0a89ebbb4ac7fa6bcae45b542b5dab49b379bee53e7983
scope.212.id=bWV0aG9kOldvcmxkI3NoYXJlaG9sZGVyUGF5bWVudHNXaXRoaW4oMSk6ODMw
scope.212.kind=method
scope.212.startLine=830
scope.212.endLine=835
scope.212.semanticHash=261005253ff4e6af0abdbc9671e7ed6cfd1502923ef96e431b1af37fba038498
scope.213.id=bWV0aG9kOldvcmxkI3NoYXJlaG9sZGVyc0NvbW1pdFRvQnVpbGQoMik6NzU5
scope.213.kind=method
scope.213.startLine=759
scope.213.endLine=763
scope.213.semanticHash=9d1f47a7169b1f87abf0dc1cf8baca9e2cca3244356fcca95fc2a18fd2c1f762
scope.214.id=bWV0aG9kOldvcmxkI3NoYXJlaG9sZGVyc0hvbGRFcXVhbFRoaXJkcygxKTo3MjQ
scope.214.kind=method
scope.214.startLine=724
scope.214.endLine=727
scope.214.semanticHash=d1dbc66707332f0fe26bcf78647091e262320b57ba76a1b517b9d11e080f9fc8
scope.215.id=bWV0aG9kOldvcmxkI3NpbXVsYXRvcklzUGxheWluZygwKToyNTc
scope.215.kind=method
scope.215.startLine=257
scope.215.endLine=260
scope.215.semanticHash=39cc09c092bb50bfcbfb6dc56e200c831082471b581226ba7cf3424f1fb37035
scope.216.id=bWV0aG9kOldvcmxkI3NpbXVsYXRvclBsYXllckNvdW50KDApOjIzNA
scope.216.kind=method
scope.216.startLine=234
scope.216.endLine=237
scope.216.semanticHash=6ac54a3ebbcbf992d0a3b4361676b583b2f9229b1cf979b8e3987d360170cbce
scope.217.id=bWV0aG9kOldvcmxkI3NpbXVsYXRvclJlc3VsdCgwKToyMjk
scope.217.kind=method
scope.217.startLine=229
scope.217.endLine=232
scope.217.semanticHash=88a0c1c9615293c35486ffa203505e8e20e418a3b569e1d6239984253b43f6a3
scope.218.id=bWV0aG9kOldvcmxkI3NwYWNlKDEpOjEzMg
scope.218.kind=method
scope.218.startLine=132
scope.218.endLine=141
scope.218.semanticHash=6bb7f4196e6c04109e3cd91c29e32ddf74bdea613ac6c1fcdab98765db0a645d
scope.219.id=bWV0aG9kOldvcmxkI3NwYWNlQXQoMSk6MTQ0
scope.219.kind=method
scope.219.startLine=144
scope.219.endLine=151
scope.219.semanticHash=c1849a2a1d181782cf175bd8dcddbf3be6fa7eeb7214f661a25a4c8c3c8f7ef1
scope.220.id=bWV0aG9kOldvcmxkI3N0YWxlbWF0ZVRocmVzaG9sZCgwKToxMTQy
scope.220.kind=method
scope.220.startLine=1142
scope.220.endLine=1144
scope.220.semanticHash=84d95fc38583fa301e31dc8f7160920cbcf6dd29c867b0e8d191630aade241a8
scope.221.id=bWV0aG9kOldvcmxkI3N0YXJ0UGFja2FnZWRDbGkoMSk6MTI2OA
scope.221.kind=method
scope.221.startLine=1268
scope.221.endLine=1294
scope.221.semanticHash=b066a16d34ab625233d484ffe9da2672410f7be20cfcb64889079117ec90f941
scope.222.id=bWV0aG9kOldvcmxkI3N0YXJ0UGF3bkluSmFpbCgxKTo0NDE
scope.222.kind=method
scope.222.startLine=441
scope.222.endLine=443
scope.222.semanticHash=7afc2d0ecb49d0200caccbd33d685ca79103cf2aeb47c67983d3e70d447f8d1a
scope.223.id=bWV0aG9kOldvcmxkI3N0YXJ0UGxheWVyKDApOjMzMA
scope.223.kind=method
scope.223.startLine=330
scope.223.endLine=333
scope.223.semanticHash=7fec1f63ec8be3387010c4e7f76d204e9e789f52f322f60af1cde379d7d46813
scope.224.id=bWV0aG9kOldvcmxkI3N0YXJ0UGxheWVySW5KYWlsKDApOjUwMw
scope.224.kind=method
scope.224.startLine=503
scope.224.endLine=505
scope.224.semanticHash=11a73fa11f9ddc3725e09f5aca8b127e623cb8ddf8f7308a404a1502b6261037
scope.225.id=bWV0aG9kOldvcmxkI3N0YXJ0U2ltdWxhdG9yKDApOjI0MA
scope.225.kind=method
scope.225.startLine=240
scope.225.endLine=244
scope.225.semanticHash=fca8a9d9e29fa3d16a84af7027717d276eebb845126a54e213cc7a383953227e
scope.226.id=bWV0aG9kOldvcmxkI3N0b3BQYWNrYWdlZENsaSgwKToxMzIy
scope.226.kind=method
scope.226.startLine=1322
scope.226.endLine=1331
scope.226.semanticHash=3c7dcdfe84127f1066729d86e5cdbce5d1719e091ff616e0eb93a1de345b7e33
scope.227.id=bWV0aG9kOldvcmxkI3N0b3BTaW11bGF0b3IoMCk6MjQ2
scope.227.kind=method
scope.227.startLine=246
scope.227.endLine=249
scope.227.semanticHash=91edbae2215b695a924941098162309084bd5274728fdda894114cae24490ed8
scope.228.id=bWV0aG9kOldvcmxkI3N0cmF0ZWd5T2YoMSk6OTk2
scope.228.kind=method
scope.228.startLine=996
scope.228.endLine=998
scope.228.semanticHash=43ddc0996945ba29d959e0c40a0cf72f79b20b9eb84fa8e40d0dbb2543068317
scope.229.id=bWV0aG9kOldvcmxkI3N0cmVldFR5cGVOYW1lZCgxKToxNDEz
scope.229.kind=method
scope.229.startLine=1413
scope.229.endLine=1419
scope.229.semanticHash=7ecdf3c1549b4c04e0cd27aa444e8ff0d90613beb16b225ebb7d814be62ced71
scope.230.id=bWV0aG9kOldvcmxkI3N1cHByZXNzT3BlbmluZ0NhcGl0YWxJZk5lZWRlZCgxKToxMTM2
scope.230.kind=method
scope.230.startLine=1136
scope.230.endLine=1140
scope.230.semanticHash=4b29a2fab4a59601731b7239810907804a4c07076b7197823071baa78934b700
scope.231.id=bWV0aG9kOldvcmxkI3Rha2VUdXJuKDApOjM3Mg
scope.231.kind=method
scope.231.startLine=372
scope.231.endLine=376
scope.231.semanticHash=79ee8e78f69bbba7c03ced8b7342e06021186e056e0eb05c7064f7c9857c7965
scope.232.id=bWV0aG9kOldvcmxkI3RvdGFsSG91c2VzKDEpOjgyNg
scope.232.kind=method
scope.232.startLine=826
scope.232.endLine=828
scope.232.semanticHash=db49b4fa17feb0612f23d4e5ba40e4b734f2babe12ec245aabc2ac550e1032d1
scope.233.id=bWV0aG9kOldvcmxkI3RyYW5zZmVycmVkRW50aXR5U3RyZWV0c1NvbGQoMik6NzA0
scope.233.kind=method
scope.233.startLine=704
scope.233.endLine=711
scope.233.semanticHash=c26b5ff0c13312d84dc31200c3dde6bacaf385047d3302692cd06bda3089ab38
scope.234.id=bWV0aG9kOldvcmxkI3R1cm5PcmRlcigwKToxMTkz
scope.234.kind=method
scope.234.startLine=1193
scope.234.endLine=1197
scope.234.semanticHash=38793238e00a80ba7366df3d148c8248051c23e820d2168ed47d2728b9df191c
scope.235.id=bWV0aG9kOldvcmxkLiNhY2NlcHRzKDEpOjUxNA
scope.235.kind=method
scope.235.startLine=514
scope.235.endLine=517
scope.235.semanticHash=c4dcfb936463d866f0320f3f2914f215671c8d8b4e4ea03dc5b1e6805167cddc
scope.236.id=bWV0aG9kOldvcmxkLiNhY2NlcHRzKDEpOjkzMg
scope.236.kind=method
scope.236.startLine=932
scope.236.endLine=935
scope.236.semanticHash=e316eb0c89f2f797a243353d10e1a7fbc0cf1a1a02ed2adeb96cf9b6b639e64e
scope.237.id=bWV0aG9kOldvcmxkLiNhY2NlcHRzKDEpOjk2NQ
scope.237.kind=method
scope.237.startLine=965
scope.237.endLine=968
scope.237.semanticHash=c4dcfb936463d866f0320f3f2914f215671c8d8b4e4ea03dc5b1e6805167cddc
scope.238.id=bWV0aG9kOldvcmxkLiNiaWRGb3IoMSk6NTE5
scope.238.kind=method
scope.238.startLine=519
scope.238.endLine=522
scope.238.semanticHash=870554be8273e13af5544554ed545ed2cd0e5d7b4bf05313ce38f6139ef3a310
scope.239.id=bWV0aG9kOldvcmxkLiNiaWRGb3IoMSk6OTM3
scope.239.kind=method
scope.239.startLine=937
scope.239.endLine=940
scope.239.semanticHash=870554be8273e13af5544554ed545ed2cd0e5d7b4bf05313ce38f6139ef3a310
scope.240.id=bWV0aG9kOldvcmxkLiNiaWRGb3IoMSk6OTcw
scope.240.kind=method
scope.240.startLine=970
scope.240.endLine=973
scope.240.semanticHash=870554be8273e13af5544554ed545ed2cd0e5d7b4bf05313ce38f6139ef3a310
scope.241.id=bWV0aG9kOldvcmxkLiNiaWRGb3JEaXN0cmVzc2VkKDYpOjQ3NA
scope.241.kind=method
scope.241.startLine=474
scope.241.endLine=478
scope.241.semanticHash=ff01afc9d0ddf1459801f9045d863abe71e46eacc1912b98aaa03e89ae1e4153
scope.242.id=bWV0aG9kOldvcmxkLiNib3VnaHQoMyk6MTA3NA
scope.242.kind=method
scope.242.startLine=1074
scope.242.endLine=1076
scope.242.semanticHash=be2fc6476ecdb4d172f5ff62da6dfe35fad80703a5592bfd73397c9f9d90ac60
scope.243.id=bWV0aG9kOldvcmxkLiNidWlsZHMoMSk6NTI5
scope.243.kind=method
scope.243.startLine=529
scope.243.endLine=532
scope.243.semanticHash=0b5665e09141ebf694804f01bd89a6ab9ef408df09314ead518bdbad50bafe14
scope.244.id=bWV0aG9kOldvcmxkLiNidWlsZHMoMSk6OTQ3
scope.244.kind=method
scope.244.startLine=947
scope.244.endLine=950
scope.244.semanticHash=0b5665e09141ebf694804f01bd89a6ab9ef408df09314ead518bdbad50bafe14
scope.245.id=bWV0aG9kOldvcmxkLiNidWlsZHMoMSk6OTgw
scope.245.kind=method
scope.245.startLine=980
scope.245.endLine=983
scope.245.semanticHash=c36b2440cba8355e866e1c568d3687d6099c3a3fe28a885151dfaf1b5f5987ff
scope.246.id=bWV0aG9kOldvcmxkLiNjbGFpbXMoMSk6NTI0
scope.246.kind=method
scope.246.startLine=524
scope.246.endLine=527
scope.246.semanticHash=8f0b38f6a36a8eb4f900bb268a576c9de4f5bf12ad71e97d9ab74d544c76bfb5
scope.247.id=bWV0aG9kOldvcmxkLiNjbGFpbXMoMSk6NTgw
scope.247.kind=method
scope.247.startLine=580
scope.247.endLine=583
scope.247.semanticHash=8f0b38f6a36a8eb4f900bb268a576c9de4f5bf12ad71e97d9ab74d544c76bfb5
scope.248.id=bWV0aG9kOldvcmxkLiNjbGFpbXMoMSk6OTQy
scope.248.kind=method
scope.248.startLine=942
scope.248.endLine=945
scope.248.semanticHash=b48251e33521bf39a392d70ad566f3b57a0bdebf6f91af96e8313bc60972f9b6
scope.249.id=bWV0aG9kOldvcmxkLiNjbGFpbXMoMSk6OTc1
scope.249.kind=method
scope.249.startLine=975
scope.249.endLine=978
scope.249.semanticHash=b48251e33521bf39a392d70ad566f3b57a0bdebf6f91af96e8313bc60972f9b6
scope.250.id=bWV0aG9kOldvcmxkLiNjbGFpbXMoMSk6OTg5
scope.250.kind=method
scope.250.startLine=989
scope.250.endLine=992
scope.250.semanticHash=f7da99d9929bcc9b8ad8f0bc8a25bc74b0455d60bf6708e9b8b0792621e4697b
scope.251.id=bWV0aG9kOldvcmxkLiNjdG9yKDApOjEyODA
scope.251.kind=method
scope.251.startLine=1
scope.251.endLine=1479
scope.251.semanticHash=b93f2114907b64feca29ab3f7144a47f87edb9edbdd7b4c78bc57731a9110c59
scope.252.id=bWV0aG9kOldvcmxkLiNkcmF3Q2hhbmNlKDApOjQxNg
scope.252.kind=method
scope.252.startLine=416
scope.252.endLine=419
scope.252.semanticHash=15111adce187342e0daeb0d0464f3f3f686b1c96cc027eda50fff702e1cd6143
scope.253.id=bWV0aG9kOldvcmxkLiNkcmF3Q29tbXVuaXR5Q2hlc3QoMCk6NDIx
scope.253.kind=method
scope.253.startLine=421
scope.253.endLine=425
scope.253.semanticHash=3e9f59bb0f70b0eb1719f3fd83045b2a691c458baa4dce63b8358665c57bcd24
scope.254.id=bWV0aG9kOldvcmxkLiNwYXlzKDEpOjUzNA
scope.254.kind=method
scope.254.startLine=534
scope.254.endLine=537
scope.254.semanticHash=54c636a1bbc442af60df70eaa922628922b794f828134612a0679db499d75b8f
scope.255.id=bWV0aG9kOldvcmxkLiNzYWxlUmVmdXNlZCg0KToxMDg3
scope.255.kind=method
scope.255.startLine=1087
scope.255.endLine=1090
scope.255.semanticHash=feff44305b3fb2704c49ec9223c0f8e4ddd7b856ce5c8824c6fddd3c1ffb0770
scope.256.id=bWV0aG9kOldvcmxkLiNzb2xkKDQpOjEwODI
scope.256.kind=method
scope.256.startLine=1082
scope.256.endLine=1085
scope.256.semanticHash=2f1a67a57175746a49f76c17ec1d2a3be1ad56e47ce53946406ce4e233b0e5ce
scope.257.id=bWV0aG9kOldvcmxkLiN3b25BdEF1Y3Rpb24oMyk6MTA3OA
scope.257.kind=method
scope.257.startLine=1078
scope.257.endLine=1080
scope.257.semanticHash=71a7f1fc2ed98c00cb182ce09290b9040b1602debe9331e76be90c68c30399e9
scope.258.id=bWV0aG9kOldvcmxkLiN3cml0ZSgxKToxMjgx
scope.258.kind=method
scope.258.startLine=1281
scope.258.endLine=1283
scope.258.semanticHash=b3fbf031f6cb535c08fff95e089cb0c300ee6119ad4a4e8cc86fef93cee31ea4
scope.259.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI2FjY2VwdHMoMSk6MTQ1Mw
scope.259.kind=method
scope.259.startLine=1453
scope.259.endLine=1462
scope.259.semanticHash=b6cf15a9ce48a5c89bdc6a65016ea73ee7c745ed1710a4c7526bb3434d8f2ce4
scope.260.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI2JpZEZvcigxKToxNDY0
scope.260.kind=method
scope.260.startLine=1464
scope.260.endLine=1467
scope.260.semanticHash=082b45ee44e91d1c1ff891afc3037b194f81c5257cbdf46e66f6cd0e27813d23
scope.261.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI2JpZHMoMik6MTQzNw
scope.261.kind=method
scope.261.startLine=1437
scope.261.endLine=1439
scope.261.semanticHash=6a1703da034429b5ba22653fca6aaf87690d7b296147b463a89c7b378e9c71f0
scope.262.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI2J1aWxkcygxKToxNDQ1
scope.262.kind=method
scope.262.startLine=1445
scope.262.endLine=1447
scope.262.semanticHash=4676606742c85a9fc0fc7c19b229072cb8fb5b44f0773a8f1d4b7e417d9d7e96
scope.263.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI2J1aWxkcygxKToxNDY5
scope.263.kind=method
scope.263.startLine=1469
scope.263.endLine=1472
scope.263.semanticHash=3aa52cbd69ca91d77afe4596f81cb90f14d6e7c4c068180d52ae0d6b2ebdbe19
scope.264.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI2J1eXMoMSk6MTQ0MQ
scope.264.kind=method
scope.264.startLine=1441
scope.264.endLine=1443
scope.264.semanticHash=7effcbff718134a1bb4ddd63ef290c016586fbe387018b05b4eb018e3ef54e99
scope.265.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI2N0b3IoMCk6MTQyNg
scope.265.kind=method
scope.265.startLine=1
scope.265.endLine=1479
scope.265.semanticHash=b93f2114907b64feca29ab3f7144a47f87edb9edbdd7b4c78bc57731a9110c59
scope.266.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI2RlY2xpbmVzKDEpOjE0MzM
scope.266.kind=method
scope.266.startLine=1433
scope.266.endLine=1435
scope.266.semanticHash=745d82f229190cafb917131657dc6ffe2003fcf1586f16be438b28fdc0a2e554
scope.267.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI3BheXMoMSk6MTQ3NA
scope.267.kind=method
scope.267.startLine=1474
scope.267.endLine=1477
scope.267.semanticHash=92a1115999f8eaebb4df5c760a80d207430bbefda56cadc85e4689c63b81c67a
scope.268.id=bWV0aG9kOldvcmxkLlNjcmlwdGVkI3BheXNKYWlsRmluZSgwKToxNDQ5
scope.268.kind=method
scope.268.startLine=1449
scope.268.endLine=1451
scope.268.semanticHash=01dfaa62665e796b738f85225b6caae2300bdb00ebc832b5af49ef1f0afe3728
*/
