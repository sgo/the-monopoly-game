package the.monopoly.game.specs.cucumber;

import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.nl.Dan;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.test.fixtures.validators.BankValidator;
import the.monopoly.game.test.fixtures.validators.BankValidator.BankAccountExpectation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static the.monopoly.game.specs.cucumber.ConversionUtils.value;

public class BankSteps {
  private final BankValidator validator;

  public BankSteps(BankValidator validator) {
    this.validator = validator;
  }

  @Then("the following bank accounts are at play")
  public void theFollowingBankAccountsAreAtPlay(List<BankAccountExpectation> expectation) {
    validator.assertBankAccountsAtPlay(expectation, Locale.forLanguageTag("en"));
  }

  @Dan("bestaan de volgende bank rekeningen in het spel")
  public void bestaanDeVolgendeBankRekeningenInHetSpel(List<BankAccountExpectation> expectation) {
    validator.assertBankAccountsAtPlay(expectation, Locale.forLanguageTag("nl"));
  }

  @ParameterType(".*")
  public Money money(String amount) {
    int modifier = 1;
    if (amount.startsWith("-"))
      modifier = -1;
    return new Money(Integer.parseInt(amount.substring(modifier > 0 ? 1 : 2)) * modifier);
  }

  @DataTableType
  public BankAccountExpectation playerID(Map<String, String> record) {
    return new BankAccountExpectation(
        new Bank.Account.Owner(value(record, "owner", "eigenaar")),
        new Bank.Account.Balance(money(value(record, "balance", "balans")))
    );
  }
}
