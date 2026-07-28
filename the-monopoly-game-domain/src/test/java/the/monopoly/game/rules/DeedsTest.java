package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

class DeedsTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();
  private final Deeds deeds = new Deeds();

  @Test
  void landNobodyHasBoughtIsUnowned() {
    assertThat(deeds.isUnowned(Street.Type.DiestsestraatLeuven)).isTrue();
    assertThat(deeds.ownerOf(Street.Type.DiestsestraatLeuven)).isEmpty();
  }

  @Test
  void soldLandBelongsToItsBuyer() {
    Player buyer = playerWith(1500);

    deeds.sell(land(Street.Type.DiestsestraatLeuven), buyer, new Money(60));

    assertThat(deeds.isUnowned(Street.Type.DiestsestraatLeuven)).isFalse();
    assertThat(deeds.ownerOf(Street.Type.DiestsestraatLeuven)).contains(buyer.id());
  }

  @Test
  void aBuyerPaysWhatTheLandWentForRatherThanWhatItIsPriced() {
    Player buyer = playerWith(1500);

    deeds.sell(land(Street.Type.DiestsestraatLeuven), buyer, new Money(120));

    assertThat(buyer.account().balance()).isEqualTo(Balance.of(1380));
  }

  @Test
  void sellingOneSpaceLeavesTheRestOfTheBoardAlone() {
    deeds.sell(land(Street.Type.DiestsestraatLeuven), playerWith(1500), new Money(60));

    assertThat(deeds.isUnowned(Street.Type.RueGrandeDinant)).isTrue();
  }

  @Test
  void housesBuiltOnAStreetAreRemembered() {
    Ownership ownership = ownedStreet();

    deeds.buildHouse(ownership.street(), ownership.owner());
    deeds.buildHouse(ownership.street(), ownership.owner());

    assertThat(deeds.housesBuiltOn(ownership.street())).isEqualTo(2);
    assertThat(deeds.hasHotelOn(ownership.street())).isFalse();
  }

  @Test
  void aHotelReplacesTheFourHousesOnItsStreet() {
    Ownership ownership = ownedStreet();
    deeds.arrangeHouses(ownership.street(), 4);

    deeds.buildHotel(ownership.street(), ownership.owner());

    assertThat(deeds.housesBuiltOn(ownership.street())).isZero();
    assertThat(deeds.hasHotelOn(ownership.street())).isTrue();
  }

  @Test
  void sellingAHouseBackToTheBankPaysHalfItsConstructionCost() {
    Ownership ownership = ownedStreet();
    deeds.arrangeHouses(ownership.street(), 1);
    ownership.owner().account().withdraw(new Money(500));

    Money price = deeds.sellHouse(ownership.street(), ownership.owner());

    assertThat(price).isEqualTo(new Money(25));
    assertThat(ownership.owner().account().balance()).isEqualTo(Balance.of(1025));
    assertThat(deeds.housesBuiltOn(ownership.street())).isZero();
    assertThat(deeds.hasHotelOn(ownership.street())).isFalse();
  }

  @Test
  void exchangingAHotelBackForHousesPaysHalfTheHotelValue() {
    Ownership ownership = ownedStreet();
    deeds.arrangeHotel(ownership.street());
    ownership.owner().account().withdraw(new Money(500));

    Money price = deeds.exchangeHotelForHouses(ownership.street(), ownership.owner());

    assertThat(price).isEqualTo(new Money(225));
    assertThat(ownership.owner().account().balance()).isEqualTo(Balance.of(1225));
    assertThat(deeds.housesBuiltOn(ownership.street())).isEqualTo(4);
    assertThat(deeds.hasHotelOn(ownership.street())).isFalse();
  }

  @Test
  void aStreetCannotBeArrangedWithAnImpossibleNumberOfHouses() {
    ColourStreet street = colourStreet(Street.Type.DiestsestraatLeuven);

    deeds.arrangeHouses(street, 0);
    assertThat(deeds.housesBuiltOn(street)).isZero();
    deeds.arrangeHouses(street, 4);
    assertThat(deeds.housesBuiltOn(street)).isEqualTo(4);

    assertThatThrownBy(() -> deeds.arrangeHouses(street, -1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> deeds.arrangeHouses(street, 5))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aStreetCannotBeBuiltPastFourHouses() {
    Ownership ownership = ownedStreet();
    deeds.arrangeHouses(ownership.street(), 4);

    assertIllegalState(() -> deeds.buildHouse(ownership.street(), ownership.owner()));
  }

  @Test
  void aHotelCanOnlyReplaceFourHouses() {
    Ownership ownership = ownedStreet();
    deeds.arrangeHouses(ownership.street(), 3);

    assertIllegalState(() -> deeds.buildHotel(ownership.street(), ownership.owner()));
  }

  @Test
  void aHouseCanOnlyBeSoldWhenOneExists() {
    Ownership ownership = ownedStreet();

    assertIllegalState(() -> deeds.sellHouse(ownership.street(), ownership.owner()));
  }

  @Test
  void aHotelCanOnlyBeExchangedWhenOneExists() {
    Ownership ownership = ownedStreet();
    deeds.arrangeHouses(ownership.street(), 4);

    assertIllegalState(() -> deeds.exchangeHotelForHouses(ownership.street(), ownership.owner()));
  }

  private Ownable land(Street.Type type) {
    return (Ownable) ruleSet.create(type);
  }

  private ColourStreet colourStreet(Street.Type type) {
    return (ColourStreet) ruleSet.create(type);
  }

  private Player playerWith(int balance) {
    Bank bank = ruleSet.bank();
    Player.ID id = new Player.ID("buyer");
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }

  private Ownership ownedStreet() {
    Player owner = playerWith(1500);
    ColourStreet street = colourStreet(Street.Type.DiestsestraatLeuven);
    deeds.sell(street, owner, street.price());
    owner.account().deposit(street.price());
    return new Ownership(owner, street);
  }

  private static void assertIllegalState(Action action) {
    assertThat(catchThrowable(action::run)).isInstanceOf(IllegalStateException.class);
  }

  @FunctionalInterface
  private interface Action {
    void run();
  }

  private record Ownership(Player owner, ColourStreet street) {
  }
}
