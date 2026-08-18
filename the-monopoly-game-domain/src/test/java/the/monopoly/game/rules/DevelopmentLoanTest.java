package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;

import static org.assertj.core.api.Assertions.assertThat;

class DevelopmentLoanTest {
  @Test
  void firstPaymentSplitsInterestAndPrincipal() {
    DevelopmentLoan loan = new DevelopmentLoan(Money.fromDollars("20"));

    DevelopmentLoan.Payment payment = loan.serviceNextYear();

    assertThat(payment.interest()).isEqualTo(Money.fromDollars("1"));
    assertThat(payment.principal()).isEqualTo(Money.fromDollars("1"));
    assertThat(loan.outstanding()).isEqualTo(Money.fromDollars("19"));
  }

  @Test
  void aLoanOfOneDollarIsFullyRepaidByItsFirstPrincipalInstalment() {
    DevelopmentLoan loan = new DevelopmentLoan(Money.fromDollars("1"));

    DevelopmentLoan.Payment payment = loan.serviceNextYear();

    assertThat(payment.principal()).isEqualTo(Money.fromDollars("1"));
    assertThat(loan.isRepaid()).isTrue();
  }

  @Test
  void aLaterPaymentUsesBankersRoundingForBothInterestAndPrincipal() {
    DevelopmentLoan loan = new DevelopmentLoan(Money.fromDollars("400.10"), 1);

    DevelopmentLoan.Payment payment = loan.serviceNextYear();

    assertThat(payment.interest()).isEqualTo(Money.fromDollars("19.00"));
    assertThat(payment.principal()).isEqualTo(Money.fromDollars("20.00"));
    assertThat(loan.outstanding()).isEqualTo(Money.fromDollars("360.10"));
  }

  @Test
  void bondPaymentUsesThreePercentYieldAndKeepsTheTwoPointSpread() {
    DevelopmentLoan loan = new DevelopmentLoan(Money.fromDollars("20"));

    DevelopmentLoan.Payment payment = loan.serviceNextYear();

    assertThat(payment.bondInterest()).isEqualTo(Money.fromDollars("0.60"));
    assertThat(payment.bankSpread()).isEqualTo(Money.fromDollars("0.40"));
  }
}
