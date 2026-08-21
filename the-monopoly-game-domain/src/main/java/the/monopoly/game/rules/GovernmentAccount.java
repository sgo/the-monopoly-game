package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;

/** The government account shared by the optional tax and relief rules. */
public final class GovernmentAccount {
  private static final Bank.Account.Owner OWNER = new Bank.Account.Owner("government");

  private final Bank.Account account;

  public GovernmentAccount(Bank bank) {
    bank.createAccountFor(OWNER);
    account = bank.accountOf(OWNER);
  }

  public Money balance() {
    return account.balance().amount();
  }

  public void deposit(Money amount) {
    account.deposit(amount);
  }

  public void withdraw(Money amount) {
    account.withdraw(amount);
  }

  public void setBalance(Money amount) {
    Money current = balance();
    if (amount.exceeds(current)) deposit(amount.minus(current));
    else if (current.exceeds(amount)) withdraw(current.minus(amount));
  }
}

/* mutate4java-manifest
version=1
moduleHash=2ac95534129819c84b4b209f2a42cfc4e2eb670d6e76e9242b19d8ebcac6571e
scope.0.id=Y2xhc3M6R292ZXJubWVudEFjY291bnQjR292ZXJubWVudEFjY291bnQ6Nw
scope.0.kind=class
scope.0.startLine=7
scope.0.endLine=34
scope.0.semanticHash=ffd175e4a5a7719a982685a8447861f26bb67b188d8328bfa95d95ddb15f3f49
scope.1.id=ZmllbGQ6R292ZXJubWVudEFjY291bnQjT1dORVI6OA
scope.1.kind=field
scope.1.startLine=8
scope.1.endLine=8
scope.1.semanticHash=64161599ec0adc97b8e388f8a78f79be67e8ad1a2884b7bfa9842fa795c105f6
scope.2.id=ZmllbGQ6R292ZXJubWVudEFjY291bnQjYWNjb3VudDoxMA
scope.2.kind=field
scope.2.startLine=10
scope.2.endLine=10
scope.2.semanticHash=3764b4c16caf41f3c42de769702372f9026d3dd5d8398658fc4bc7f83d8c868b
scope.3.id=bWV0aG9kOkdvdmVybm1lbnRBY2NvdW50I2JhbGFuY2UoMCk6MTc
scope.3.kind=method
scope.3.startLine=17
scope.3.endLine=19
scope.3.semanticHash=d0938ad0151b031f273556c1f0c2d1ddba2e53ef0b07b19b3f07ab7718387b61
scope.4.id=bWV0aG9kOkdvdmVybm1lbnRBY2NvdW50I2N0b3IoMSk6MTI
scope.4.kind=method
scope.4.startLine=12
scope.4.endLine=15
scope.4.semanticHash=c5696f52a5177ed42a367569dc7b6784927f43bc1b53b35a122af90e0eb79891
scope.5.id=bWV0aG9kOkdvdmVybm1lbnRBY2NvdW50I2RlcG9zaXQoMSk6MjE
scope.5.kind=method
scope.5.startLine=21
scope.5.endLine=23
scope.5.semanticHash=08accd6c2ba6e56da49c3939f93d6325bcbf1910c13c9a7a758b985bfabd8a75
scope.6.id=bWV0aG9kOkdvdmVybm1lbnRBY2NvdW50I3NldEJhbGFuY2UoMSk6Mjk
scope.6.kind=method
scope.6.startLine=29
scope.6.endLine=33
scope.6.semanticHash=bf442a0b9c4202046139f06dbbaeee7b685309d05c5c6953f9c5bcb2253677a5
scope.7.id=bWV0aG9kOkdvdmVybm1lbnRBY2NvdW50I3dpdGhkcmF3KDEpOjI1
scope.7.kind=method
scope.7.startLine=25
scope.7.endLine=27
scope.7.semanticHash=11f46b10471f315d8f04b4de1fd9621cf8966d74a925839f7aeedaf98ff2d714
*/
