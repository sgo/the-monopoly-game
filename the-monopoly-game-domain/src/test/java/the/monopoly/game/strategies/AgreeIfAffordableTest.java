package the.monopoly.game.strategies;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

class AgreeIfAffordableTest {
  private static final Ownable LAND = ownable(Street.Type.DiestsestraatLeuven);

  private final Strategy strategy = new AgreeIfAffordable();

  @Test
  void landWithinReachIsBought() {
    assertThat(strategy.accepts(offerWith(new Money(60)))).isTrue();
  }

  @Test
  void landPricedExactlyAtWhatIsLeftIsStillBought() {
    assertThat(strategy.accepts(offerWith(LAND.price()))).isTrue();
  }

  @Test
  void landOnePennyOutOfReachIsDeclined() {
    assertThat(strategy.accepts(offerWith(new Money(LAND.price().amount() - 1)))).isFalse();
  }

  @Test
  void ratherThanLoseTheLandItBidsEverythingItHas() {
    assertThat(strategy.bidFor(offerWith(new Money(1500)))).isEqualTo(new Money(1500));
  }

  @Test
  void aPlayerWithNothingLeftBidsNothing() {
    assertThat(strategy.bidFor(offerWith(Money.ZERO))).isEqualTo(Money.ZERO);
  }

  @Test
  void aReservedPlayerBidsOnlyWhatItCanSpendWithoutUsingItsReserve() {
    Strategy reserved = new AgreeIfAffordable(new Money(100));

    assertThat(reserved.bidFor(offerWith(new Money(150)))).isEqualTo(new Money(50));
  }

  @Test
  void aReservedPlayerMaySpendItsReserveToCompleteAUtilityMonopoly() {
    Ownable utility = ownable(Street.Type.Watermaatschappij);
    Strategy reserved = new AgreeIfAffordable(new Money(100));
    Strategy.Offer offer = new Strategy.Offer(utility, new Money(150), new Money(100), true);

    assertThat(reserved.bidFor(offer)).isEqualTo(new Money(150));
  }

  /** A strategy that has an opinion about nothing leaves the land alone. */
  @Test
  void aStrategyThatAnswersNothingBuysNothingAndBidsNothing() {
    Strategy indifferent = Strategy.UNDECIDED;
    ColourStreet street = (ColourStreet) LAND;

    assertThat(indifferent.accepts(offerWith(new Money(1500)))).isFalse();
    assertThat(indifferent.bidFor(offerWith(new Money(1500)))).isEqualTo(Money.ZERO);
    assertThat(indifferent.claims(new Strategy.RentClaim(
        new Player(new Player.ID("tenant"), null), LAND, new Money(4)
    ))).isFalse();
    assertThat(indifferent.builds(new Strategy.BuildOffer(
        street, street.houseConstructionCost(), new Money(1500), false
    ))).isFalse();
  }

  private static Strategy.Offer offerWith(Money available) {
    return new Strategy.Offer(LAND, available);
  }

  private static Ownable ownable(Street.Type type) {
    return (Ownable) type.create(emptySet());
  }
}
