package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BankruptcyTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Player dog = player("dog");
  private final Player highHat = player("high hat");
  private final List<Player> players = List.of(dog, highHat);

  @Test
  void aSolventPlayerIsUntouched() {
    Events events = new Events();

    new Bankruptcy(new Deeds(), rules, players, Strategy.OfPlayers.NOBODY_DECIDES, events)
        .resolve(dog, null);

    assertThat(events.bankrupt).isFalse();
  }

  @Test
  void aPlayerSellsAHouseBeforeMortgagingLand() {
    Deeds deeds = new Deeds();
    ColourStreet street = (ColourStreet) rules.create(Street.Type.RueGrandeDinant);
    give(deeds, street, dog);
    deeds.arrangeHouses(street, 1);
    dog.account().withdraw(new Money(1510));

    new Bankruptcy(deeds, rules, players, Strategy.OfPlayers.NOBODY_DECIDES, new Events())
        .resolve(dog, null);

    assertThat(deeds.housesBuiltOn(street)).isZero();
    assertThat(dog.account().balance().amount().amount()).isEqualTo(15);
    assertThat(deeds.isBankrupt(dog)).isFalse();
  }

  @Test
  void aPlayerSellsEveryNeededHouseBeforeMortgagingLand() {
    Deeds deeds = new Deeds();
    ColourStreet street = (ColourStreet) rules.create(Street.Type.RueGrandeDinant);
    give(deeds, street, dog);
    deeds.arrangeHouses(street, 2);
    dog.account().withdraw(new Money(1550));

    new Bankruptcy(deeds, rules, players, Strategy.OfPlayers.NOBODY_DECIDES, new Events())
        .resolve(dog, null);

    assertThat(deeds.housesBuiltOn(street)).isZero();
    assertThat(deeds.isMortgaged(street)).isFalse();
    assertThat(dog.account().balance().amount().amount()).isZero();
    assertThat(deeds.isBankrupt(dog)).isFalse();
  }

  @Test
  void aPlayerExchangesAHotelThenSellsEnoughHousesBeforeMortgagingLand() {
    Deeds deeds = new Deeds();
    ColourStreet street = (ColourStreet) rules.create(Street.Type.RueGrandeDinant);
    give(deeds, street, dog);
    deeds.arrangeHotel(street);
    dog.account().withdraw(new Money(1700));

    new Bankruptcy(deeds, rules, players, Strategy.OfPlayers.NOBODY_DECIDES, new Events())
        .resolve(dog, null);

    assertThat(deeds.housesBuiltOn(street)).isEqualTo(1);
    assertThat(deeds.isMortgaged(street)).isFalse();
    assertThat(dog.account().balance().amount().amount()).isZero();
    assertThat(deeds.isBankrupt(dog)).isFalse();
  }

  @Test
  void aBankruptPlayerLosesLandToTheBankAuction() {
    Deeds deeds = new Deeds();
    Ownable land = (Ownable) rules.create(Street.Type.DiestsestraatLeuven);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(1600));
    Strategy.OfPlayers strategies = player -> player.equals(highHat) ? bidding(10) : Strategy.UNDECIDED;
    Events events = new Events();

    new Bankruptcy(deeds, rules, players, strategies, events).resolve(dog, null);

    assertThat(deeds.isBankrupt(dog)).isTrue();
    assertThat(deeds.ownerOf(land.type())).contains(highHat.id());
    assertThat(events.winner).isEqualTo(highHat);
  }

  @Test
  void aBankruptPlayerTransfersMortgagedLandToTheCreditor() {
    Deeds deeds = new Deeds();
    Ownable land = (Ownable) rules.create(Street.Type.RueGrandeDinant);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(2600));
    Events events = new Events();

    new Bankruptcy(deeds, rules, players, Strategy.OfPlayers.NOBODY_DECIDES, events)
        .resolve(dog, highHat);

    assertThat(deeds.isBankrupt(dog)).isTrue();
    assertThat(deeds.ownerOf(land.type())).contains(highHat.id());
    assertThat(deeds.isMortgaged(land)).isTrue();
    assertThat(events.winner).isEqualTo(highHat);
  }

  @Test
  void anAgreeableCreditorLiftsAnInheritedMortgageWhenAffordable() {
    Deeds deeds = new Deeds();
    Ownable land = (Ownable) rules.create(Street.Type.RueGrandeDinant);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(2600));
    Strategy.OfPlayers strategies = player -> player.equals(highHat)
        ? new the.monopoly.game.strategies.Greedo()
        : Strategy.UNDECIDED;

    new Bankruptcy(deeds, rules, players, strategies, new Events()).resolve(dog, highHat);

    assertThat(deeds.ownerOf(land.type())).contains(highHat.id());
    assertThat(deeds.isMortgaged(land)).isFalse();
  }

  /**
   * The previous test gives the creditor a balance far beyond what lifting
   * the mortgage costs, so it cannot tell the real mortgage-plus-interest
   * price from a wildly wrong one. This one funds the creditor with exactly
   * enough to absorb dog's $1,100 debt (withdrawn $2,600 against a $1,500
   * balance) and then the $30 mortgage plus its $3 (10%, rounded up)
   * interest, and nothing more.
   */
  @Test
  void anAgreeableCreditorLiftsAnInheritedMortgageForExactlyItsPricePlusInterest() {
    Deeds deeds = new Deeds();
    Ownable land = (Ownable) rules.create(Street.Type.RueGrandeDinant);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(2600));
    highHat.account().withdraw(highHat.account().balance().amount().minus(new Money(1100 + 33)));
    Strategy.OfPlayers strategies = player -> player.equals(highHat)
        ? new the.monopoly.game.strategies.Greedo()
        : Strategy.UNDECIDED;

    new Bankruptcy(deeds, rules, players, strategies, new Events()).resolve(dog, highHat);

    assertThat(deeds.ownerOf(land.type())).contains(highHat.id());
    assertThat(deeds.isMortgaged(land)).isFalse();
  }

  @Test
  void aDistressedSaleGoesToItsOnlyBidderAtTheNeededAmount() {
    Deeds deeds = new Deeds();
    Ownable land = (Ownable) rules.create(Street.Type.RueGrandeDinant);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(1540));
    Strategy.OfPlayers strategies = player -> player.equals(highHat) ? distressedBidder(40) : Strategy.UNDECIDED;
    Events events = new Events();

    new Bankruptcy(deeds, rules, players, strategies, events).resolve(dog, null);

    assertThat(deeds.ownerOf(land.type())).contains(highHat.id());
    assertThat(dog.account().balance().amount().amount()).isZero();
    assertThat(highHat.account().balance().amount().amount()).isEqualTo(1460);
    assertThat(deeds.isBankrupt(dog)).isFalse();
  }

  @Test
  void aDistressedSaleReportsWhenNobodyCanBid() {
    Deeds deeds = new Deeds();
    Ownable land = (Ownable) rules.create(Street.Type.LippenslaanKnokke);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(1514));
    Events events = new Events();

    new Bankruptcy(deeds, rules, players, Strategy.OfPlayers.NOBODY_DECIDES, events).resolve(dog, null);

    assertThat(events.noBidder).isTrue();
    assertThat(deeds.isMortgaged(land)).isTrue();
  }

  @Test
  void aDistressedOfferBelowTheMortgageValueIsRejected() {
    Deeds deeds = new Deeds();
    Ownable land = (Ownable) rules.create(Street.Type.LippenslaanKnokke);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(1514));
    Strategy.OfPlayers strategies = player -> player.equals(highHat) ? distressedBidder(40) : Strategy.UNDECIDED;

    new Bankruptcy(deeds, rules, players, strategies, new Events()).resolve(dog, null);

    assertThat(deeds.ownerOf(land.type())).contains(dog.id());
    assertThat(deeds.isMortgaged(land)).isTrue();
    assertThat(dog.account().balance().amount().amount()).isEqualTo(76);
    assertThat(deeds.isBankrupt(dog)).isFalse();
  }

  @Test
  void aDistressedSaleDoesNotMakeItsOnlyBidderOverpay() {
    Deeds deeds = new Deeds();
    Ownable land = (Ownable) rules.create(Street.Type.LippenslaanKnokke);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(1510));
    highHat.account().withdraw(new Money(1315));
    Strategy.OfPlayers strategies = player -> player.equals(highHat) ? distressedBidder(185) : Strategy.UNDECIDED;

    new Bankruptcy(deeds, rules, players, strategies, new Events()).resolve(dog, null);

    assertThat(deeds.ownerOf(land.type())).contains(highHat.id());
    assertThat(dog.account().balance().amount().amount()).isEqualTo(80);
    assertThat(highHat.account().balance().amount().amount()).isEqualTo(95);
  }

  @Test
  void aDistressedSaleAscendsInFiveDollarStepsUntilOnlyOneBidderCanStillRaise() {
    Deeds deeds = new Deeds();
    Player ironBox = player("iron box");
    List<Player> table = List.of(dog, highHat, ironBox);
    Ownable land = (Ownable) rules.create(Street.Type.RueGrandeDinant);
    give(deeds, land, dog);
    dog.account().withdraw(new Money(1525));
    Strategy.OfPlayers strategies = player -> {
      if (player.equals(highHat)) return distressedBidder(50);
      if (player.equals(ironBox)) return distressedBidder(70);
      return Strategy.UNDECIDED;
    };
    Events events = new Events();

    new Bankruptcy(deeds, rules, table, strategies, events).resolve(dog, null);

    assertThat(deeds.ownerOf(land.type())).contains(ironBox.id());
    assertThat(ironBox.account().balance().amount().amount()).isEqualTo(1445);
    assertThat(dog.account().balance().amount().amount()).isEqualTo(30);
    assertThat(deeds.isBankrupt(dog)).isFalse();
  }

  @Test
  void aCreditorInheritsTheBankruptPlayersGetOutOfJailFreeCard() {
    Deeds deeds = new Deeds();
    deeds.hold(Deeds.RetainedCard.CHANCE_GET_OUT_OF_JAIL_FREE, dog);
    dog.account().withdraw(new Money(1600));

    new Bankruptcy(deeds, rules, players, Strategy.OfPlayers.NOBODY_DECIDES, new Events())
        .resolve(dog, highHat);

    assertThat(deeds.holdsGetOutOfJailFreeCard(dog)).isFalse();
    assertThat(deeds.holdsGetOutOfJailFreeCard(highHat)).isTrue();
  }

  private void give(Deeds deeds, Ownable land, Player owner) {
    deeds.sell(land, owner, land.price());
    owner.account().deposit(land.price());
  }

  private Player player(String name) {
    Bank bank = rules.bank();
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(1500));
    return player;
  }

  private Strategy bidding(int amount) {
    return new Strategy() {
      @Override
      public Money bidFor(Offer offer) {
        return new Money(amount);
      }
    };
  }

  private Strategy distressedBidder(int maximum) {
    return new Strategy() {
      @Override
      public Money bidForDistressed(Offer offer, Player bidder, Player debtor,
                                    List<Player> players, Rule.Set rules, Deeds deeds) {
        return new Money(maximum);
      }
    };
  }

  private static final class Events implements Bankruptcy.Events {
    private boolean bankrupt;
    private Player winner;
    private boolean noBidder;

    @Override
    public void bankrupt(Player debtor, Player creditor) {
      bankrupt = true;
    }

    @Override
    public void won(Player player) {
      winner = player;
    }

    @Override
    public void distressedSaleNoBidder(Player seller, Ownable land) {
      noBidder = true;
    }
  }
}
