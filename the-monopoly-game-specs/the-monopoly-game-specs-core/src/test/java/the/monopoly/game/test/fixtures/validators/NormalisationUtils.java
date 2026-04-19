package the.monopoly.game.test.fixtures.validators;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.test.fixtures.validators.BankValidator.BankAccountExpectation;

import java.util.Locale;

@Service
public class NormalisationUtils {
  private final MessageSource messages;

  public NormalisationUtils(MessageSource messages) {
    this.messages = messages;
  }

  public Player.ID normalise(Player.ID it, Locale locale) {
    return new Player.ID(humanReadablePawnName(it.value(), locale));
  }

  private String humanReadablePawnName(String pawnID, Locale locale) {
    return messages.getMessage(
        "pawn." + pawnID + ".name",
        new Object[0],
        locale
    );
  }

  public BankAccountExpectation normalise(BankAccountExpectation it, Locale locale) {
    return new BankAccountExpectation(normalise(it.owner(), locale), it.balance());
  }

  private Bank.Account.Owner normalise(Bank.Account.Owner owner, Locale locale) {
    return new Bank.Account.Owner(humanReadablePawnName(owner.name(), locale));
  }
}
