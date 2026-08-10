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
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.Jail;
import the.monopoly.game.rules.LandSale;
import the.monopoly.game.rules.Landings;
import the.monopoly.game.rules.LegalEntity;
import the.monopoly.game.rules.MonopolyBuyout;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Stalemate;
import the.monopoly.game.rules.Turn;
import the.monopoly.game.strategies.Strategy;

import java.time.Duration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
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
  private Player player;
  private Dice dice;
  private Map<Dice.Face, Integer> rolls;
  private final Deque<Roll> queuedRolls = new ArrayDeque<>();
  private final Map<String, Deque<Roll>> queuedPawnRolls = new HashMap<>();
  private final Deque<String> queuedChanceCards = new ArrayDeque<>();
  private final Deque<String> queuedCommunityChestCards = new ArrayDeque<>();
  private final Map<String, Strategy> pawnStrategies = new HashMap<>();
  private List<Player> turnOrder;
  private boolean othersRollWhatTheyLike;
  private List<Entry> journal;
  private Deeds deeds;
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
  private MonopolyBuyout.Outcome buyout;
  private boolean stalemateTrading;
  private boolean legalEntityTrading;
  private boolean simulatorStalemateTrading;
  private Entry selectedEvent;
  private String renderedEventText;
  private String loggedEventText;

  public void selectRuleSet(Rule.Set.Type type) {
    ruleSet = type.create();
    jail = new Jail(ruleSet);
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

  public void configureSimulatorWithGreedo() {
    if (simulatorPlayers == null) throw new AssertionError("The simulator has not been configured.");
    simulatorStrategies = player -> new the.monopoly.game.strategies.Greedo();
  }

  public void giveSimulatorArgument(String argument) {
    if (!argument.equals("--optional-greedo-stalemate-trading")) {
      throw new AssertionError("Unknown simulator argument: " + argument);
    }
    simulatorStalemateTrading = true;
  }

  public void runSimulator() {
    if (simulatorPlayers == null) throw new AssertionError("The simulator has not been configured.");
    simulatorResult = Simulator.run(simulatorPlayers, simulatorStrategies);
  }

  public Simulator.Result simulatorResult() {
    if (simulatorResult == null) throw new AssertionError("The simulator has not been run.");
    return simulatorResult;
  }

  /** Starts the simulator playing in the background, so the game log fills as it goes. */
  public void startSimulator() {
    if (simulatorPlayers == null) throw new AssertionError("The simulator has not been configured.");
    runningSimulator = Simulator.start(simulatorPlayers, simulatorStrategies, simulatorStalemateTrading);
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
    simulatorStrategies = player -> new the.monopoly.game.strategies.Greedo();
    simulatorStalemateTrading = arguments.contains("--optional-greedo-stalemate-trading");
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
    queuePawnRoll(pawnName, rollTotalling(total));
  }

  /** Queues what a pawn's next throw of the dice will come up, in order. */
  public void queuePawnRoll(String pawnName, Roll roll) {
    queuedPawnRolls.computeIfAbsent(pawnName, it -> new ArrayDeque<>()).add(roll);
  }

  public void rollForInitiative() {
    turnOrder = new Initiative(player -> nextQueuedPawnRoll(player).total()).order(players());
  }

  public void playGame() {
    playAndCapture(Game::play);
  }

  public void playUpToRounds(int rounds) {
    playAndCapture(game -> game.playUpToRounds(rounds));
  }

  private void playAndCapture(Function<Game, Game.Result> play) {
    Cards.Decks officialDecks = Cards.Decks.official(deeds == null ? deeds = new Deeds() : deeds);
    Game game = new Game(
        ruleSet, players(), player -> () -> nextQueuedPawnRoll(player), this::strategyOf,
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
        legalEntityTrading
    );
    Game.Result result = play.apply(game);
    turnOrder = result.turnOrder();
    journal = result.journal();
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

  public boolean hasWon(String pawnName) {
    return journal != null && journal.contains(new Entry.Won(pawn(pawnName).id()));
  }

  public boolean endedInStalemate() {
    return journal != null && journal.stream().anyMatch(Entry.Stalemate.class::isInstance);
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
        return true;
      }

      @Override
      public boolean builds(BuildOffer offer) {
        return strategy.builds(offer);
      }

      @Override
      public boolean pays(JailFine fine) {
        return strategy.pays(fine);
      }
    });
  }

  /**
   * Walks a pawn onto a named space and plays the game out. The pawn is stood a
   * short hop short of the space and rolls exactly that, so it arrives there by
   * playing rather than by being put there.
   */
  public void landPawnOn(String pawnName, Street.Type space) {
    int arrival = ruleSet.gameboard().positionOf(space);
    placePawn(pawnName, arrival - A_SHORT_HOP.total());
    queuePawnRoll(pawnName, A_SHORT_HOP);
    playGame();
  }

  /** Whether the pawn holds the title to that land once the game has been played. */
  public boolean pawnOwns(String pawnName, Street.Type land) {
    if (deeds == null)
      throw new AssertionError("No game has been played yet.");
    return deeds.ownerOf(land).filter(it -> it.value().equals(pawnName)).isPresent();
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

  public void pawnFollows(String pawnName, Strategy strategy) {
    pawnStrategies.put(pawnName, strategy);
  }

  public void pawnFollowsGreedoWithReserve(String pawnName, Money reserve) {
    pawnStrategies.put(pawnName, new the.monopoly.game.strategies.Greedo(reserve));
  }

  public void enableStalemateTrading(String strategyName) {
    if (!strategyName.equals("Greedo")) throw new AssertionError("Unknown strategy \"" + strategyName + "\".");
    stalemateTrading = true;
    pawnStrategies.put("dog", new the.monopoly.game.strategies.Greedo(Money.ZERO, true));
  }

  public void enableLegalEntityTrading(String strategyName) {
    if (!strategyName.equals("Greedo")) throw new AssertionError("Unknown strategy \"" + strategyName + "\".");
    legalEntityTrading = true;
    pawnStrategies.put("dog", new the.monopoly.game.strategies.Greedo(Money.ZERO, false, true));
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
    othersRollWhatTheyLike = true;
    for (int index = 0; index < players().size(); index++) {
      Player player = players().get(index);
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
      Player defaultOwner = shareholders.get(1);
      ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
          .filter(it -> deeds.isUnowned(it.type())).forEach(it -> deeds.sell(it, defaultOwner, Money.ZERO));
    }
    String name = Character.toUpperCase(colour.name().charAt(0)) + colour.name().substring(1) + " Realty";
    LegalEntity entity = seedBoard
        ? LegalEntity.formed(name, colour, shareholders, ruleSet)
        : LegalEntity.form(name, colour, shareholders, ruleSet, deeds,
            street -> Strategy.priorityOf(street) == Strategy.Priority.HIGHEST).orElse(null);
    if (entity != null) deeds.form(entity);
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

  public void entityOwes(String entityName, Money principal) {
    deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName)).recordLoan(principal);
  }

  public void entityRaisesLoan(String entityName, Money amount) {
    deeds.legalEntities().stream().filter(it -> it.name().equals(entityName)).findFirst()
        .orElseThrow(() -> new AssertionError("Unknown entity " + entityName)).raiseLoan(amount);
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
    the.monopoly.game.strategies.Strategy.Priority actual =
        new the.monopoly.game.strategies.Greedo().priority(ownable(SpaceNames.of(spaceName)));
    org.assertj.core.api.Assertions.assertThat(actual.name().toLowerCase()).isEqualTo(expected);
  }

  public void pawnConsidersTrading(String traderName, String offeredName, String partnerName, String wantedName) {
    Player trader = pawn(traderName);
    Player partner = pawn(partnerName);
    Strategy.TradeOffer offer = new Strategy.TradeOffer(
        trader, partner, ownable(SpaceNames.of(offeredName)), ownable(SpaceNames.of(wantedName)));
    tradeAccepted = new the.monopoly.game.strategies.Greedo().accepts(offer, ruleSet, deeds);
  }

  public void assertGreedoTradeDecision(String decision) {
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
    scriptFor(pawnName).bids(land, amount);
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
    if (journal == null)
      throw new AssertionError("No game has been played yet.");
    return journal;
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
  }

  /** Sets a balance representing wealth accumulated during the game. */
  public void holdPawnBalance(String pawnName, Money amount) {
    Money current = pawn(pawnName).account().balance().amount();
    if (amount.exceeds(current)) pawn(pawnName).account().deposit(amount.minus(current));
    else if (current.exceeds(amount)) pawn(pawnName).account().withdraw(current.minus(amount));
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
  private static Roll rollTotalling(int total) {
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
        "-am", "package", "-DskipTests");
  }

  public void runPackagedCli(String flag) {
    Path root = PomInspector.repoRoot("the-monopoly-game-cli");
    Path jar = root.resolve("the-monopoly-game-cli").resolve("target")
        .resolve("the-monopoly-game-cli-0.2.0-SNAPSHOT.jar");
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
    Path root = PomInspector.repoRoot("the-monopoly-game-cli");
    Path jar = root.resolve("the-monopoly-game-cli").resolve("target")
        .resolve("the-monopoly-game-cli-0.2.0-SNAPSHOT.jar");
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

  public void assertPackagedCliStalemateTrading(String state) {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (System.nanoTime() < deadline) {
      synchronized (packagedCliOutputBuffer) {
        if (packagedCliOutputBuffer.toString().contains("Stalemate trading enabled")
            == state.equals("enabled")) return;
      }
      LockSupport.parkNanos(5_000_000);
    }
    throw new AssertionError("Packaged jar output did not confirm stalemate trading is " + state
        + ": " + packagedCliOutputBuffer);
  }

  public void assertPackagedCliLegalEntity(String state) {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (System.nanoTime() < deadline) {
      synchronized (packagedCliOutputBuffer) {
        if (packagedCliOutputBuffer.toString().contains("Legal entity trading enabled")
            == state.equals("enabled")) return;
      }
      LockSupport.parkNanos(5_000_000);
    }
    throw new AssertionError("Packaged jar output did not confirm legal entity trading is " + state
        + ": " + packagedCliOutputBuffer);
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

  private static void runProcess(Path workingDirectory, String... command) {
    try {
      Process process = new ProcessBuilder(command).directory(workingDirectory.toFile()).inheritIO().start();
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
