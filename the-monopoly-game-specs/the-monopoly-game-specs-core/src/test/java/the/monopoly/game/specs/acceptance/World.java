package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Turn;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  private Rule.Set ruleSet = Rule.Set.Type.official.create();
  private Street space;
  private List<Player> players;
  private Player player;
  private Dice dice;
  private Map<Dice.Face, Integer> rolls;
  private final Deque<Roll> queuedRolls = new ArrayDeque<>();
  private final Map<String, Deque<Roll>> queuedPawnRolls = new HashMap<>();
  private List<Player> turnOrder;

  public void selectRuleSet(Rule.Set.Type type) {
    ruleSet = type.create();
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
    new Turn(ruleSet, this::nextQueuedRoll).take(player());
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
    turnOrder = new Game(ruleSet, players(), player -> () -> nextQueuedPawnRoll(player))
        .play()
        .turnOrder();
  }

  /**
   * The rules open every pawn's account with the same starting capital, and no
   * rule moves money before the first turn, so a scenario can only state the
   * balance a pawn already has. Stating a different one says the scenario needs
   * a rule that does not exist yet, which is what the failure says.
   */
  public void arrangePawnBalance(String pawnName, Money amount) {
    Balance balance = pawn(pawnName).account().balance();
    if (!balance.equals(new Balance(amount)))
      throw new AssertionError(
          "Pawn \"" + pawnName + "\" holds " + balance + ", and no rule can arrange "
              + new Balance(amount) + " before the game starts."
      );
  }

  private Roll nextQueuedPawnRoll(Player player) {
    Deque<Roll> queued = queuedPawnRolls.get(player.id().value());
    if (queued == null || queued.isEmpty())
      throw new AssertionError(
          "The game wanted another roll for \"" + player.id().value() + "\" but none was queued."
      );
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

  private List<Player> players() {
    if (players == null)
      throw new AssertionError("No players have been selected yet.");
    return players;
  }
}
