package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

class PeerTradingTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Deeds deeds = new Deeds();
  private final Player dog = player("dog");
  private final Player highHat = player("high hat");

  @Test
  void aStrategyWithoutStalemateTradingEnabledTradesNothing() {
    deeds.sell(ownable(Street.Type.RueGrandeDinant), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), highHat, Money.ZERO);

    assertThat(select(new Greedo(Money.ZERO, false))).isEmpty();
  }

  @Test
  void aNonGreedoStrategyTradesNothing() {
    deeds.sell(ownable(Street.Type.RueGrandeDinant), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), highHat, Money.ZERO);

    assertThat(select(Strategy.UNDECIDED)).isEmpty();
  }

  @Test
  void selectsTheTradeThatCompletesAColourGroup() {
    deeds.sell(ownable(Street.Type.RueGrandeDinant), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), highHat, Money.ZERO);

    Optional<Strategy.TradeOffer> selected = select(new Greedo(Money.ZERO, true));

    assertThat(selected).contains(new Strategy.TradeOffer(
        dog, highHat, ownable(Street.Type.RueGrandeDinant), ownable(Street.Type.DiestsestraatLeuven)));
  }

  @Test
  void betweenAcceptedCandidatesTheLowestPriorityPropertyIsOffered() {
    deeds.sell(ownable(Street.Type.RueGrandeDinant), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.RueDeDiekirchArlon), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), highHat, Money.ZERO);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), highHat, Money.ZERO);

    Optional<Strategy.TradeOffer> selected = select(new Greedo(Money.ZERO, true));

    assertThat(selected).map(Strategy.TradeOffer::offered)
        .contains(ownable(Street.Type.NieuwstraatBrussel));
  }

  @Test
  void rejectsATradeThatOnlyBenefitsTheTrader() {
    deeds.sell(ownable(Street.Type.RueDeDiekirchArlon), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.PlaceVerteVerviers), highHat, Money.ZERO);

    Optional<Strategy.TradeOffer> selected = select(new Greedo(Money.ZERO, true));

    assertThat(selected).isEmpty();
  }

  @Test
  void noAcceptedTradeSelectsNothing() {
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), highHat, Money.ZERO);

    assertThat(select(new Greedo(Money.ZERO, true))).isEmpty();
  }

  private Optional<Strategy.TradeOffer> select(Strategy strategy) {
    return PeerTrading.select(dog, strategy, List.of(dog, highHat), rules, deeds);
  }

  private static Ownable ownable(Street.Type type) {
    return (Ownable) type.create(emptySet());
  }

  private Player player(String name) {
    Player.ID id = new Player.ID(name);
    Bank bank = rules.bank();
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}
