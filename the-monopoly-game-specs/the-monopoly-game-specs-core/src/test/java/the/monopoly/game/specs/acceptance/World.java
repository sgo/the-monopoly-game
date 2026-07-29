package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.Report;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Initiative;
import the.monopoly.game.rules.Jail;
import the.monopoly.game.rules.LandSale;
import the.monopoly.game.rules.Landings;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Turn;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    Game.Result result = new Game(
        ruleSet, players(), player -> () -> nextQueuedPawnRoll(player), this::strategyOf,
        deeds == null ? deeds = new Deeds() : deeds,
        new the.monopoly.game.rules.Cards.Decks() {
          @Override
          public String drawChance() {
            return queuedChanceCards.pollFirst();
          }

          @Override
          public String drawCommunityChest() {
            return queuedCommunityChestCards.pollFirst();
          }
        },
        jail
    ).play();
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

  public void givePawnGetOutOfJailFreeCard(String pawnName) {
    if (deeds == null) deeds = new Deeds();
    deeds.hold(Deeds.RetainedCard.CHANCE_GET_OUT_OF_JAIL_FREE, pawn(pawnName));
  }

  public void pawnWillUseGetOutOfJailFreeCard(String pawnName) {
    jail.useCard(pawn(pawnName));
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
    journal = List.of(new Entry.HouseSold(player.id(), street.type(), price));
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
    journal = List.of(new Entry.Mortgaged(player.id(), land, value));
  }

  public void liftMortgage(String pawnName, Street.Type land) {
    if (deeds == null)
      throw new AssertionError("No deeds exist yet, so no mortgage can be lifted.");
    Player player = pawn(pawnName);
    Deeds.MortgageCost cost = deeds.liftMortgage(ownable(land), player);
    journal = List.of(new Entry.MortgageLifted(player.id(), land, cost.total(), cost.interest()));
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
        journal = List.of(new Entry.LandSold(seller.id(), soldLand.type(), buyer.id(), soldPrice));
      }

      @Override
      public void saleRefused(Player seller, Ownable soldLand, Player buyer, Money soldPrice) {
        journal = List.of(new Entry.LandSaleRefused(seller.id(), soldLand.type(), buyer.id(), soldPrice));
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
  }
}
