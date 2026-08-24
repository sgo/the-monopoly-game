package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

/** Caps a tenant's rent at the salary amount when government can fund the rest. */
public final class RentRelief {
  private static final Money RENT_CAP = new Money(200);

  private final GovernmentAccount government;

  public RentRelief(Bank bank) {
    government = new GovernmentAccount(bank);
  }

  public void pay(Player tenant, Player landlord, Money rent) {
    Money relief = reliefFor(rent);
    tenant.account().withdraw(rent.minus(relief));
    landlord.account().deposit(rent);
    if (!relief.equals(Money.ZERO)) government.withdraw(relief);
  }

  public void pay(Player tenant, LegalEntity landlord, Money rent) {
    Money relief = reliefFor(rent);
    tenant.account().withdraw(rent.minus(relief));
    landlord.depositToBank(rent);
    if (!relief.equals(Money.ZERO)) government.withdraw(relief);
  }

  public Money governmentBalance() {
    return government.balance();
  }

  public Money tenantPayment(Money rent) {
    return rent.minus(reliefFor(rent));
  }

  public void setGovernmentBalance(Money amount) {
    government.setBalance(amount);
  }

  private Money reliefFor(Money rent) {
    if (!rent.exceeds(RENT_CAP)) return Money.ZERO;
    Money difference = rent.minus(RENT_CAP);
    return government.balance().covers(difference) ? difference : Money.ZERO;
  }
}

/* mutate4java-manifest
version=1
moduleHash=23e550233af9aae3411b197036de1779e523876f94cb06a98e578e963170e054
scope.0.id=Y2xhc3M6UmVudFJlbGllZiNSZW50UmVsaWVmOjg
scope.0.kind=class
scope.0.startLine=8
scope.0.endLine=37
scope.0.semanticHash=acdf76ed268f6c8c5cc5cc9e887b54a856b0657fd944ecc0ca32e5cb1e3af7a7
scope.1.id=ZmllbGQ6UmVudFJlbGllZiNSRU5UX0NBUDo5
scope.1.kind=field
scope.1.startLine=9
scope.1.endLine=9
scope.1.semanticHash=4fac2c5a9607b12421c5a285cc3847862b2b5ba765a6658b51ff46fd3709b715
scope.2.id=ZmllbGQ6UmVudFJlbGllZiNnb3Zlcm5tZW50OjEx
scope.2.kind=field
scope.2.startLine=11
scope.2.endLine=11
scope.2.semanticHash=e0abeecdb8ad2c5a84be07f721ec67044477e0ae88b308c43f8307c4f9342009
scope.3.id=bWV0aG9kOlJlbnRSZWxpZWYjY3RvcigxKToxMw
scope.3.kind=method
scope.3.startLine=13
scope.3.endLine=15
scope.3.semanticHash=d8a5ba675e7a30112c077fad40080a94bfdb7c8b36770a5fc03516f0bb35369f
scope.4.id=bWV0aG9kOlJlbnRSZWxpZWYjZ292ZXJubWVudEJhbGFuY2UoMCk6MjQ
scope.4.kind=method
scope.4.startLine=24
scope.4.endLine=26
scope.4.semanticHash=d8bc138d727a3e5b764b1b831b98422bc7f6205bdc40ce3d0f6cec1395d86e8f
scope.5.id=bWV0aG9kOlJlbnRSZWxpZWYjcGF5KDMpOjE3
scope.5.kind=method
scope.5.startLine=17
scope.5.endLine=22
scope.5.semanticHash=cfb0dfe1b4db9813d53c89b729829d9c2263695655e41dbc39f6602ab41c7d12
scope.6.id=bWV0aG9kOlJlbnRSZWxpZWYjcmVsaWVmRm9yKDEpOjMy
scope.6.kind=method
scope.6.startLine=32
scope.6.endLine=36
scope.6.semanticHash=d160ee89b571073f54ef2aceb1085a584df34cc9cb32c464aa0ea75beb46862a
scope.7.id=bWV0aG9kOlJlbnRSZWxpZWYjc2V0R292ZXJubWVudEJhbGFuY2UoMSk6Mjg
scope.7.kind=method
scope.7.startLine=28
scope.7.endLine=30
scope.7.semanticHash=6423c2771330eacd8a690849279bf136d4d3141c1a95b0a7e57454b4fa6d9983
*/
