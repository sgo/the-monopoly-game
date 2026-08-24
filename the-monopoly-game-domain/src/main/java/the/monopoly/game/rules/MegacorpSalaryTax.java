package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

/** Pays a player's salary and MegaCorp's 43% income tax into government. */
public final class MegacorpSalaryTax {
  private static final int RATE = 43;

  private final GovernmentAccount government;

  public MegacorpSalaryTax(Bank bank) {
    government = new GovernmentAccount(bank);
  }

  public MegacorpSalaryTax(GovernmentAccount government) {
    this.government = government;
  }

  public Money collect(Player player, Money salary) {
    player.account().deposit(salary);
    Money tax = taxOn(salary);
    government.deposit(tax);
    return tax;
  }

  public Money governmentBalance() {
    return government.balance();
  }

  public Money payTax(Money salary) {
    Money tax = taxOn(salary);
    government.deposit(tax);
    return tax;
  }

  private Money taxOn(Money netSalary) {
    return Money.fromCents(java.math.BigDecimal.valueOf(netSalary.cents())
        .multiply(java.math.BigDecimal.valueOf(RATE))
        .divide(java.math.BigDecimal.valueOf(100 - RATE), 0, java.math.RoundingMode.HALF_EVEN)
        .longValueExact());
  }
}

/* mutate4java-manifest
version=1
moduleHash=848e6546a616c4ef86e5d8edaf72f7522f8f53d3175a8bbea33ab5d73ef271dd
scope.0.id=Y2xhc3M6TWVnYWNvcnBTYWxhcnlUYXgjTWVnYWNvcnBTYWxhcnlUYXg6OA
scope.0.kind=class
scope.0.startLine=8
scope.0.endLine=27
scope.0.semanticHash=2924bdb1d5698fa5b06528ef751939b9750984138a75eaa29eec75154c9778a4
scope.1.id=ZmllbGQ6TWVnYWNvcnBTYWxhcnlUYXgjUkFURTo5
scope.1.kind=field
scope.1.startLine=9
scope.1.endLine=9
scope.1.semanticHash=7b9abc678f5748d38a0cc2f18bec7efe512709e1e1a0951edfa0672a7a4bfff9
scope.2.id=ZmllbGQ6TWVnYWNvcnBTYWxhcnlUYXgjZ292ZXJubWVudDoxMQ
scope.2.kind=field
scope.2.startLine=11
scope.2.endLine=11
scope.2.semanticHash=e0abeecdb8ad2c5a84be07f721ec67044477e0ae88b308c43f8307c4f9342009
scope.3.id=bWV0aG9kOk1lZ2Fjb3JwU2FsYXJ5VGF4I2NvbGxlY3QoMik6MTc
scope.3.kind=method
scope.3.startLine=17
scope.3.endLine=22
scope.3.semanticHash=181ea0f05aa1c0b6991c383d3d6000d6cb5bd2762a4c113fddb1719a045335b4
scope.4.id=bWV0aG9kOk1lZ2Fjb3JwU2FsYXJ5VGF4I2N0b3IoMSk6MTM
scope.4.kind=method
scope.4.startLine=13
scope.4.endLine=15
scope.4.semanticHash=d3137e90d798206256640bc7cbbdd50d3c2604de7b4c8c67b57b30bc7e698b86
scope.5.id=bWV0aG9kOk1lZ2Fjb3JwU2FsYXJ5VGF4I2dvdmVybm1lbnRCYWxhbmNlKDApOjI0
scope.5.kind=method
scope.5.startLine=24
scope.5.endLine=26
scope.5.semanticHash=d8bc138d727a3e5b764b1b831b98422bc7f6205bdc40ce3d0f6cec1395d86e8f
*/
