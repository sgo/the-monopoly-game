package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LandSaleTest {
  private static final Street.Type LAND = Street.Type.DiestsestraatLeuven;
  private static final Money PRICE = new Money(60);

  private final Rule.Set ruleSet = Rule.Set.Type.official.create();
  private final Deeds deeds = new Deeds();
  private final Map<Player.ID, Strategy> strategies = new HashMap<>();
  private final Reported reported = new Reported();
  private final Player dog = playerWith("dog", 1500);
  private final Player highHat = playerWith("high hat", 1500);

  @Test
  void whoeverLandsOnUnownedLandIsAskedFirstAndBuysAtThePriceOnTheBoard() {
    plays(dog, new Greedo());

    landOn(dog, LAND);

    assertThat(deeds.ownerOf(LAND)).contains(dog.id());
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1440));
  }

  @Test
  void aPurchaseIsReportedForWhatItCost() {
    plays(dog, new Greedo());

    landOn(dog, LAND);

    assertThat(reported.events).containsExactly("dog bought DiestsestraatLeuven for 60");
  }

  @Test
  void landAlreadySoldIsNotSoldAgain() {
    deeds.sell(land(LAND), highHat, PRICE);
    plays(dog, new Greedo());

    landOn(dog, LAND);

    assertThat(deeds.ownerOf(LAND)).contains(highHat.id());
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
  }

  /** Nobody can buy the tax office, so nobody is asked to. */
  @Test
  void aSpaceNobodyCanOwnIsNotForSale() {
    plays(dog, new Greedo());

    landOn(dog, Street.Type.InkomstenBelasting);

    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(reported.events).isEmpty();
  }

  @Test
  void landTurnedDownGoesToTheHighestBidderAtTheBidTheyMade() {
    plays(dog, bidding(90));
    plays(highHat, bidding(120));

    landOn(dog, LAND);

    assertThat(deeds.ownerOf(LAND)).contains(highHat.id());
    assertThat(highHat.account().balance()).isEqualTo(Balance.of(1380));
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
  }

  @Test
  void thePlayerWhoTurnedTheLandDownBidsLikeAnyoneElseAndMayWinIt() {
    plays(dog, bidding(140));
    plays(highHat, bidding(120));

    landOn(dog, LAND);

    assertThat(deeds.ownerOf(LAND)).contains(dog.id());
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1360));
  }

  @Test
  void anAuctionIsReportedForWhatItFetchedRatherThanThePriceOnTheBoard() {
    plays(dog, bidding(90));
    plays(highHat, bidding(120));

    landOn(dog, LAND);

    assertThat(reported.events).containsExactly("high hat won DiestsestraatLeuven at 120");
  }

  /**
   * Two players wanting the land equally badly is settled by who spoke first,
   * so that the same game played twice sells the land to the same player.
   */
  @Test
  void equalBidsGoToWhoeverBidFirst() {
    plays(dog, bidding(120));
    plays(highHat, bidding(120));

    landOn(dog, LAND);

    assertThat(deeds.ownerOf(LAND)).contains(dog.id());
  }

  @Test
  void landNobodyBidsForStaysWithTheBank() {
    landOn(dog, LAND);

    assertThat(deeds.isUnowned(LAND)).isTrue();
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(reported.events).isEmpty();
  }

  @Test
  void unimprovedLandCanBeSoldBetweenPlayersAtAnAgreedPrice() {
    deeds.sell(land(LAND), dog, PRICE);
    dog.account().deposit(PRICE);

    sale().sell(dog, land(LAND), highHat, new Money(90));

    assertThat(deeds.ownerOf(LAND)).contains(highHat.id());
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1590));
    assertThat(highHat.account().balance()).isEqualTo(Balance.of(1410));
    assertThat(reported.events).containsExactly("dog sold DiestsestraatLeuven to high hat for 90");
  }

  @Test
  void aStationCanBeSoldBetweenPlayersAtAnAgreedPrice() {
    Ownable station = land(Street.Type.NoordStation);
    deeds.sell(station, dog, station.price());
    dog.account().deposit(station.price());

    sale().sell(dog, station, highHat, new Money(90));

    assertThat(deeds.ownerOf(Street.Type.NoordStation)).contains(highHat.id());
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1590));
    assertThat(highHat.account().balance()).isEqualTo(Balance.of(1410));
    assertThat(reported.events).containsExactly("dog sold NoordStation to high hat for 90");
  }

  @Test
  void aStreetInAColourGroupWithAnyHouseBuiltCannotBeSold() {
    deeds.sell(land(Street.Type.RueGrandeDinant), dog, new Money(60));
    dog.account().deposit(new Money(60));
    deeds.sell(land(LAND), dog, PRICE);
    dog.account().deposit(PRICE);
    deeds.arrangeHouses((ColourStreet) ruleSet.create(Street.Type.RueGrandeDinant), 1);

    sale().sell(dog, land(LAND), highHat, new Money(90));

    assertThat(deeds.ownerOf(LAND)).contains(dog.id());
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(highHat.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(reported.events).containsExactly("dog refused DiestsestraatLeuven to high hat for 90");
  }

  private void landOn(Player player, Street.Type space) {
    new LandSale(deeds, ruleSet, List.of(dog, highHat), this::strategyOf, reported)
        .resolve(player, ruleSet.create(space), new Roll(1, 1));
  }

  private LandSale sale() {
    return new LandSale(deeds, ruleSet, List.of(dog, highHat), this::strategyOf, reported);
  }

  private void plays(Player player, Strategy strategy) {
    strategies.put(player.id(), strategy);
  }

  private Strategy strategyOf(Player player) {
    return strategies.getOrDefault(player.id(), Strategy.UNDECIDED);
  }

  /** A player who wants the land at auction but never at the asking price. */
  private static Strategy bidding(int amount) {
    return new Strategy() {
      @Override
      public Money bidFor(Offer offer) {
        return new Money(amount);
      }
    };
  }

  private Ownable land(Street.Type type) {
    return (Ownable) ruleSet.create(type);
  }

  private Player playerWith(String name, int balance) {
    Bank bank = ruleSet.bank();
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }

  /** What a sale said happened, in the order it said it. */
  private static final class Reported implements LandSale.Events {
    private final List<String> events = new ArrayList<>();

    @Override
    public void bought(Player buyer, Ownable land, Money price) {
      events.add(buyer.id().value() + " bought " + land.type() + " for " + price.amount());
    }

    @Override
    public void wonAtAuction(Player winner, Ownable land, Money price) {
      events.add(winner.id().value() + " won " + land.type() + " at " + price.amount());
    }

    @Override
    public void sold(Player seller, Ownable land, Player buyer, Money price) {
      events.add(seller.id().value() + " sold " + land.type() + " to " + buyer.id().value() + " for " + price.amount());
    }

    @Override
    public void saleRefused(Player seller, Ownable land, Player buyer, Money price) {
      events.add(seller.id().value() + " refused " + land.type() + " to " + buyer.id().value() + " for " + price.amount());
    }
  }
}
