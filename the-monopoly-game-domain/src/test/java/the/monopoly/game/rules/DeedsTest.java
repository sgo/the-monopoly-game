package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import static org.assertj.core.api.Assertions.assertThat;

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

  private Ownable land(Street.Type type) {
    return (Ownable) ruleSet.create(type);
  }

  private Player playerWith(int balance) {
    Bank bank = ruleSet.bank();
    Player.ID id = new Player.ID("buyer");
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }
}
