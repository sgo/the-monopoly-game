package the.monopoly.game.test.fixtures.validators;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Owner;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.test.fixtures.repository.CurrentRuleTypeRepository;
import the.monopoly.game.test.fixtures.repository.RuleSetRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class BankValidator {
  private final CurrentRuleTypeRepository currentRuleTypeRepository;
  private final RuleSetRepository ruleSetRepository;
  private final NormalisationUtils normaliser;

  public BankValidator(
      CurrentRuleTypeRepository currentRuleTypeRepository,
      RuleSetRepository ruleSetRepository,
      NormalisationUtils normaliser
  ) {
    this.currentRuleTypeRepository = currentRuleTypeRepository;
    this.ruleSetRepository = ruleSetRepository;
    this.normaliser = normaliser;
  }

  public void assertBankAccountsAtPlay(List<BankAccountExpectation> expectation, Locale locale) {
    assertThat(ruleSet().bank().accounts()
        .map(BankAccountExpectation::of)
        .map(it -> normaliser.normalise(it, locale))
        .sorted(BankAccountExpectation.Comparators.natural())
    ).containsExactlyElementsOf(expectation);
  }

  private Rule.Set ruleSet() {
    return ruleSetRepository.get(currentRuleTypeRepository.get());
  }

  public record BankAccountExpectation(Owner owner, Bank.Account.Balance balance) {
    public static BankAccountExpectation of(Bank.Account account) {
      return new BankAccountExpectation(account.owner(), account.balance());
    }

    public static class Comparators {
      public static Comparator<BankAccountExpectation> natural() {
        return BankAccountExpectation.Comparators::natural;
      }

      private static int natural(BankAccountExpectation x, BankAccountExpectation y) {
        return x.owner().name().compareTo(y.owner().name());
      }
    }
  }
}
