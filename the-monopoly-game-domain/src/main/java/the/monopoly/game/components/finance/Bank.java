package the.monopoly.game.components.finance;

import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.players.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface Bank {
  Stream<Account> accounts();

  void createAccountFor(Player.ID id);

  Account accountOf(Player.ID id);

  record Account(Owner owner, Balance balance) {
    public void deposit(Money amount) {
      balance.incrementWith(amount);
    }

    public void withdraw(Money amount) {
      balance.decrementWith(amount);
    }

    public record Owner(String name) {
    }

    public static class Balance {
      private Money amount;

      public Balance(Money amount) {
        this.amount = amount;
      }

      public static Balance of(int amount) {
        return new Balance(new Money(amount));
      }

      /** What is in the account, for whoever has to decide what it can pay for. */
      public Money amount() {
        return amount;
      }

      public void incrementWith(Money amount) {
        this.amount = this.amount.plus(amount);
      }

      public void decrementWith(Money amount) {
        this.amount = this.amount.minus(amount);
      }

      @Override
      public final boolean equals(Object o) {
        if (!(o instanceof Balance balance)) return false;

        return amount.equals(balance.amount);
      }

      @Override
      public int hashCode() {
        return amount.hashCode();
      }

      @Override
      public String toString() {
        return "Balance[" +
            "amount=" + amount +
            ']';
      }
    }
  }

  final class Simple implements Bank {
    private final Map<Account.Owner, Account> accounts = new HashMap<>();

    @Override
    public Stream<Account> accounts() {
      return accounts.values().stream();
    }

    @Override
    public void createAccountFor(Player.ID id) {
      Account.Owner owner = new Account.Owner(id.value());
      accounts.putIfAbsent(owner, new Account(owner, Balance.of(0)));
    }

    @Override
    public Account accountOf(Player.ID id) {
      Account account = accounts.get(new Account.Owner(id.value()));
      if (account == null)
        throw new IllegalArgumentException("No account for player " + id.value() + ".");
      return account;
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=da6e6b898474b67435ac417bead903ba2b9c64b32bd1a44cf29e155ff6747b69
scope.0.id=Y2xhc3M6QmFuayNCYW5rOjEw
scope.0.kind=class
scope.0.startLine=10
scope.0.endLine=96
scope.0.semanticHash=f5b3df6fa38a906fe931f71360c5b2c18d77df43cff98f9ee9190ab5ebb74fcb
scope.1.id=Y2xhc3M6QmFuay5BY2NvdW50I0FjY291bnQ6MTc
scope.1.kind=class
scope.1.startLine=17
scope.1.endLine=72
scope.1.semanticHash=baee4afe7af0f5ae6b2b1d01918fb4f7ab89dd44d1d5d962c52b8654b7e98405
scope.2.id=Y2xhc3M6QmFuay5BY2NvdW50LkJhbGFuY2UjQmFsYW5jZToyOQ
scope.2.kind=class
scope.2.startLine=29
scope.2.endLine=71
scope.2.semanticHash=202974e1fb7095f9a3ccd245b77151fa6900538a5f3af29b8bac1085223a344c
scope.3.id=Y2xhc3M6QmFuay5BY2NvdW50Lk93bmVyI093bmVyOjI2
scope.3.kind=class
scope.3.startLine=26
scope.3.endLine=27
scope.3.semanticHash=5c899306de31a7095ceed9fefdd367fa034a3b53ba69cd257dc3616bcdac68a7
scope.4.id=Y2xhc3M6QmFuay5TaW1wbGUjU2ltcGxlOjc0
scope.4.kind=class
scope.4.startLine=74
scope.4.endLine=95
scope.4.semanticHash=da9c3b9efc8feac2697b0478addc2d4b943d725b0e2dbc01027ee52aaf523e54
scope.5.id=ZmllbGQ6QmFuay5BY2NvdW50I2JhbGFuY2U6MTc
scope.5.kind=field
scope.5.startLine=17
scope.5.endLine=17
scope.5.semanticHash=6c644e19cc28bb596c081e0673fa98122414ce5a3851a790604d3532a819b04c
scope.6.id=ZmllbGQ6QmFuay5BY2NvdW50I293bmVyOjE3
scope.6.kind=field
scope.6.startLine=17
scope.6.endLine=17
scope.6.semanticHash=689bd7aa9aca59e2676434ea7fce64304ec3f337b862ddfc99706e365f8d8615
scope.7.id=ZmllbGQ6QmFuay5BY2NvdW50LkJhbGFuY2UjYW1vdW50OjMw
scope.7.kind=field
scope.7.startLine=30
scope.7.endLine=30
scope.7.semanticHash=75ae19bd7fb06ca70fe95b0a12986c6a35282bdb6dcedbb2f29c34ceeb904cf7
scope.8.id=ZmllbGQ6QmFuay5BY2NvdW50Lk93bmVyI25hbWU6MjY
scope.8.kind=field
scope.8.startLine=26
scope.8.endLine=26
scope.8.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.9.id=ZmllbGQ6QmFuay5TaW1wbGUjYWNjb3VudHM6NzU
scope.9.kind=field
scope.9.startLine=75
scope.9.endLine=75
scope.9.semanticHash=d16604df6e42bc4d0906cbea45873a89cd52dc9fa0159f385de2f141214869b7
scope.10.id=bWV0aG9kOkJhbmsjYWNjb3VudE9mKDEpOjE1
scope.10.kind=method
scope.10.startLine=15
scope.10.endLine=15
scope.10.semanticHash=6dcbe6f3105cc7b92243a16487d9a8076ad45948b8b93f180e1f2703e9e1e1b2
scope.11.id=bWV0aG9kOkJhbmsjYWNjb3VudHMoMCk6MTE
scope.11.kind=method
scope.11.startLine=11
scope.11.endLine=11
scope.11.semanticHash=eaa0f07236b68bfbabebba7fb072c161d6558a458414c77d6b846ecfca1f5e96
scope.12.id=bWV0aG9kOkJhbmsjY3JlYXRlQWNjb3VudEZvcigxKToxMw
scope.12.kind=method
scope.12.startLine=13
scope.12.endLine=13
scope.12.semanticHash=e33c8982a0c6a8c3ae841a14e7d293844cd07c358ab20efc70a862f2a59e7672
scope.13.id=bWV0aG9kOkJhbmsuQWNjb3VudCNjdG9yKDIpOjE3
scope.13.kind=method
scope.13.startLine=1
scope.13.endLine=96
scope.13.semanticHash=199df9c7d053db689e99dc3c5b63d0ad5ac6d7a8dfc68f0981bf5561cd248b27
scope.14.id=bWV0aG9kOkJhbmsuQWNjb3VudCNkZXBvc2l0KDEpOjE4
scope.14.kind=method
scope.14.startLine=18
scope.14.endLine=20
scope.14.semanticHash=fc92f5c77052106d4d752d35971c65cba57f6a5b3ae2cb5c7290f08849c00a2a
scope.15.id=bWV0aG9kOkJhbmsuQWNjb3VudCN3aXRoZHJhdygxKToyMg
scope.15.kind=method
scope.15.startLine=22
scope.15.endLine=24
scope.15.semanticHash=b9bba4021ca59f54c85909826ca859a957cc7af7e41121e166c0c898bc266f37
scope.16.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2Ftb3VudCgwKTo0MQ
scope.16.kind=method
scope.16.startLine=41
scope.16.endLine=43
scope.16.semanticHash=97312681f677ca49fcbaacfec7b151e4066c726d3d9da3de8c35a349ad5b35c6
scope.17.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2N0b3IoMSk6MzI
scope.17.kind=method
scope.17.startLine=32
scope.17.endLine=34
scope.17.semanticHash=0b95b706797a6d668131bad07dcf3976f4206bb327ffe6ae9e0c8afa851b84df
scope.18.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2RlY3JlbWVudFdpdGgoMSk6NDk
scope.18.kind=method
scope.18.startLine=49
scope.18.endLine=51
scope.18.semanticHash=3622eaaa85dbc1fdd61f916800d48d314bc68029353cd4cd06e2f505bdc0ca4f
scope.19.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2VxdWFscygxKTo1Mw
scope.19.kind=method
scope.19.startLine=53
scope.19.endLine=58
scope.19.semanticHash=ae9feb67d3c5d5426468cfde5830dae1cf53fcd9b2657a9a6cb527133420652a
scope.20.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2hhc2hDb2RlKDApOjYw
scope.20.kind=method
scope.20.startLine=60
scope.20.endLine=63
scope.20.semanticHash=8059553674b527e0d5bc0876aa9c17aa0af16bb4a4d9e7c7f56f74dc84e69ec5
scope.21.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2luY3JlbWVudFdpdGgoMSk6NDU
scope.21.kind=method
scope.21.startLine=45
scope.21.endLine=47
scope.21.semanticHash=e117b790836ec14e352ad7cbe7f74b75eb84f40f22eb7d85042f2288de18225f
scope.22.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI29mKDEpOjM2
scope.22.kind=method
scope.22.startLine=36
scope.22.endLine=38
scope.22.semanticHash=0cb6a859462d708d5a0c6b85c03301f9d5cec0f570f900171d6ca449b495fccc
scope.23.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI3RvU3RyaW5nKDApOjY1
scope.23.kind=method
scope.23.startLine=65
scope.23.endLine=70
scope.23.semanticHash=71a9a670e152042035c57e2a58d6f7681b1182c998f1d856eef7784e41c7449b
scope.24.id=bWV0aG9kOkJhbmsuQWNjb3VudC5Pd25lciNjdG9yKDEpOjI2
scope.24.kind=method
scope.24.startLine=1
scope.24.endLine=96
scope.24.semanticHash=199df9c7d053db689e99dc3c5b63d0ad5ac6d7a8dfc68f0981bf5561cd248b27
scope.25.id=bWV0aG9kOkJhbmsuU2ltcGxlI2FjY291bnRPZigxKTo4OA
scope.25.kind=method
scope.25.startLine=88
scope.25.endLine=94
scope.25.semanticHash=889fadc4924f4eb94c53c151588f4d4907f2b1af927deba194fb66010de93dfc
scope.26.id=bWV0aG9kOkJhbmsuU2ltcGxlI2FjY291bnRzKDApOjc3
scope.26.kind=method
scope.26.startLine=77
scope.26.endLine=80
scope.26.semanticHash=c40e769b9b98884c6ff9bcdbd7ffda1f9b495f65e6c1760c99e399eb9de60b1e
scope.27.id=bWV0aG9kOkJhbmsuU2ltcGxlI2NyZWF0ZUFjY291bnRGb3IoMSk6ODI
scope.27.kind=method
scope.27.startLine=82
scope.27.endLine=86
scope.27.semanticHash=73fa131dbc82d630608cf575987b0d9a6c964f3035e6f8aeb9c011b347b73279
scope.28.id=bWV0aG9kOkJhbmsuU2ltcGxlI2N0b3IoMCk6NzQ
scope.28.kind=method
scope.28.startLine=1
scope.28.endLine=96
scope.28.semanticHash=199df9c7d053db689e99dc3c5b63d0ad5ac6d7a8dfc68f0981bf5561cd248b27
*/
