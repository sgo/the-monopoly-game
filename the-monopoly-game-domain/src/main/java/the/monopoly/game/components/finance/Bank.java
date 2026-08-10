package the.monopoly.game.components.finance;

import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.players.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface Bank {
  Stream<Account> accounts();

  void createAccountFor(Player.ID id);

  default void createAccountFor(Account.Owner owner) {
  }

  Account accountOf(Player.ID id);

  default Account accountOf(Account.Owner owner) {
    throw new IllegalArgumentException("No account for " + owner.name() + ".");
  }

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
    public void createAccountFor(Account.Owner owner) {
      accounts.putIfAbsent(owner, new Account(owner, Balance.of(0)));
    }

    @Override
    public Account accountOf(Player.ID id) {
      Account account = accounts.get(new Account.Owner(id.value()));
      if (account == null)
        throw new IllegalArgumentException("No account for player " + id.value() + ".");
      return account;
    }

    @Override
    public Account accountOf(Account.Owner owner) {
      Account account = accounts.get(owner);
      if (account == null) throw new IllegalArgumentException("No account for " + owner.name() + ".");
      return account;
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=814aa709317f537ad1793e65f6455fc7718cddd1bb0dd130bfa1f37806169dbf
scope.0.id=Y2xhc3M6QmFuayNCYW5rOjEw
scope.0.kind=class
scope.0.startLine=10
scope.0.endLine=115
scope.0.semanticHash=fff4ba26a10176ebfc17f4c054bcd281ffa1a7dc6b12b427bd0105a9a73d19fd
scope.1.id=Y2xhc3M6QmFuay5BY2NvdW50I0FjY291bnQ6MjQ
scope.1.kind=class
scope.1.startLine=24
scope.1.endLine=79
scope.1.semanticHash=baee4afe7af0f5ae6b2b1d01918fb4f7ab89dd44d1d5d962c52b8654b7e98405
scope.2.id=Y2xhc3M6QmFuay5BY2NvdW50LkJhbGFuY2UjQmFsYW5jZTozNg
scope.2.kind=class
scope.2.startLine=36
scope.2.endLine=78
scope.2.semanticHash=202974e1fb7095f9a3ccd245b77151fa6900538a5f3af29b8bac1085223a344c
scope.3.id=Y2xhc3M6QmFuay5BY2NvdW50Lk93bmVyI093bmVyOjMz
scope.3.kind=class
scope.3.startLine=33
scope.3.endLine=34
scope.3.semanticHash=5c899306de31a7095ceed9fefdd367fa034a3b53ba69cd257dc3616bcdac68a7
scope.4.id=Y2xhc3M6QmFuay5TaW1wbGUjU2ltcGxlOjgx
scope.4.kind=class
scope.4.startLine=81
scope.4.endLine=114
scope.4.semanticHash=5b4a512bf45c0ee2ebab7d40fefc3f5554b9650bbe171ec372d5d629b95b6e11
scope.5.id=ZmllbGQ6QmFuay5BY2NvdW50I2JhbGFuY2U6MjQ
scope.5.kind=field
scope.5.startLine=24
scope.5.endLine=24
scope.5.semanticHash=6c644e19cc28bb596c081e0673fa98122414ce5a3851a790604d3532a819b04c
scope.6.id=ZmllbGQ6QmFuay5BY2NvdW50I293bmVyOjI0
scope.6.kind=field
scope.6.startLine=24
scope.6.endLine=24
scope.6.semanticHash=689bd7aa9aca59e2676434ea7fce64304ec3f337b862ddfc99706e365f8d8615
scope.7.id=ZmllbGQ6QmFuay5BY2NvdW50LkJhbGFuY2UjYW1vdW50OjM3
scope.7.kind=field
scope.7.startLine=37
scope.7.endLine=37
scope.7.semanticHash=75ae19bd7fb06ca70fe95b0a12986c6a35282bdb6dcedbb2f29c34ceeb904cf7
scope.8.id=ZmllbGQ6QmFuay5BY2NvdW50Lk93bmVyI25hbWU6MzM
scope.8.kind=field
scope.8.startLine=33
scope.8.endLine=33
scope.8.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.9.id=ZmllbGQ6QmFuay5TaW1wbGUjYWNjb3VudHM6ODI
scope.9.kind=field
scope.9.startLine=82
scope.9.endLine=82
scope.9.semanticHash=d16604df6e42bc4d0906cbea45873a89cd52dc9fa0159f385de2f141214869b7
scope.10.id=bWV0aG9kOkJhbmsjYWNjb3VudE9mKDEpOjE4
scope.10.kind=method
scope.10.startLine=18
scope.10.endLine=18
scope.10.semanticHash=6dcbe6f3105cc7b92243a16487d9a8076ad45948b8b93f180e1f2703e9e1e1b2
scope.11.id=bWV0aG9kOkJhbmsjYWNjb3VudE9mKDEpOjIw
scope.11.kind=method
scope.11.startLine=20
scope.11.endLine=22
scope.11.semanticHash=ffc0c10fe1454a12722aecc0e200c9898716a0b3f84b1508e09126afb724db39
scope.12.id=bWV0aG9kOkJhbmsjYWNjb3VudHMoMCk6MTE
scope.12.kind=method
scope.12.startLine=11
scope.12.endLine=11
scope.12.semanticHash=eaa0f07236b68bfbabebba7fb072c161d6558a458414c77d6b846ecfca1f5e96
scope.13.id=bWV0aG9kOkJhbmsjY3JlYXRlQWNjb3VudEZvcigxKToxMw
scope.13.kind=method
scope.13.startLine=13
scope.13.endLine=13
scope.13.semanticHash=e33c8982a0c6a8c3ae841a14e7d293844cd07c358ab20efc70a862f2a59e7672
scope.14.id=bWV0aG9kOkJhbmsjY3JlYXRlQWNjb3VudEZvcigxKToxNQ
scope.14.kind=method
scope.14.startLine=15
scope.14.endLine=16
scope.14.semanticHash=f48f524f4839dcf6c66349f80834590af5225b1d1e4d1bcb88a7d7c7fde669a7
scope.15.id=bWV0aG9kOkJhbmsuQWNjb3VudCNjdG9yKDIpOjI0
scope.15.kind=method
scope.15.startLine=1
scope.15.endLine=115
scope.15.semanticHash=7aba33b288b592b20bd2880ef26fb7795b9903611d8d12b120f9e0841cbf3865
scope.16.id=bWV0aG9kOkJhbmsuQWNjb3VudCNkZXBvc2l0KDEpOjI1
scope.16.kind=method
scope.16.startLine=25
scope.16.endLine=27
scope.16.semanticHash=fc92f5c77052106d4d752d35971c65cba57f6a5b3ae2cb5c7290f08849c00a2a
scope.17.id=bWV0aG9kOkJhbmsuQWNjb3VudCN3aXRoZHJhdygxKToyOQ
scope.17.kind=method
scope.17.startLine=29
scope.17.endLine=31
scope.17.semanticHash=b9bba4021ca59f54c85909826ca859a957cc7af7e41121e166c0c898bc266f37
scope.18.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2Ftb3VudCgwKTo0OA
scope.18.kind=method
scope.18.startLine=48
scope.18.endLine=50
scope.18.semanticHash=97312681f677ca49fcbaacfec7b151e4066c726d3d9da3de8c35a349ad5b35c6
scope.19.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2N0b3IoMSk6Mzk
scope.19.kind=method
scope.19.startLine=39
scope.19.endLine=41
scope.19.semanticHash=0b95b706797a6d668131bad07dcf3976f4206bb327ffe6ae9e0c8afa851b84df
scope.20.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2RlY3JlbWVudFdpdGgoMSk6NTY
scope.20.kind=method
scope.20.startLine=56
scope.20.endLine=58
scope.20.semanticHash=3622eaaa85dbc1fdd61f916800d48d314bc68029353cd4cd06e2f505bdc0ca4f
scope.21.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2VxdWFscygxKTo2MA
scope.21.kind=method
scope.21.startLine=60
scope.21.endLine=65
scope.21.semanticHash=ae9feb67d3c5d5426468cfde5830dae1cf53fcd9b2657a9a6cb527133420652a
scope.22.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2hhc2hDb2RlKDApOjY3
scope.22.kind=method
scope.22.startLine=67
scope.22.endLine=70
scope.22.semanticHash=8059553674b527e0d5bc0876aa9c17aa0af16bb4a4d9e7c7f56f74dc84e69ec5
scope.23.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2luY3JlbWVudFdpdGgoMSk6NTI
scope.23.kind=method
scope.23.startLine=52
scope.23.endLine=54
scope.23.semanticHash=e117b790836ec14e352ad7cbe7f74b75eb84f40f22eb7d85042f2288de18225f
scope.24.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI29mKDEpOjQz
scope.24.kind=method
scope.24.startLine=43
scope.24.endLine=45
scope.24.semanticHash=0cb6a859462d708d5a0c6b85c03301f9d5cec0f570f900171d6ca449b495fccc
scope.25.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI3RvU3RyaW5nKDApOjcy
scope.25.kind=method
scope.25.startLine=72
scope.25.endLine=77
scope.25.semanticHash=71a9a670e152042035c57e2a58d6f7681b1182c998f1d856eef7784e41c7449b
scope.26.id=bWV0aG9kOkJhbmsuQWNjb3VudC5Pd25lciNjdG9yKDEpOjMz
scope.26.kind=method
scope.26.startLine=1
scope.26.endLine=115
scope.26.semanticHash=7aba33b288b592b20bd2880ef26fb7795b9903611d8d12b120f9e0841cbf3865
scope.27.id=bWV0aG9kOkJhbmsuU2ltcGxlI2FjY291bnRPZigxKToxMDA
scope.27.kind=method
scope.27.startLine=100
scope.27.endLine=106
scope.27.semanticHash=889fadc4924f4eb94c53c151588f4d4907f2b1af927deba194fb66010de93dfc
scope.28.id=bWV0aG9kOkJhbmsuU2ltcGxlI2FjY291bnRPZigxKToxMDg
scope.28.kind=method
scope.28.startLine=108
scope.28.endLine=113
scope.28.semanticHash=a78f999da0650d201fdae9a67ed498b6fa7270eff3cefd17e7297770f78ac7e9
scope.29.id=bWV0aG9kOkJhbmsuU2ltcGxlI2FjY291bnRzKDApOjg0
scope.29.kind=method
scope.29.startLine=84
scope.29.endLine=87
scope.29.semanticHash=c40e769b9b98884c6ff9bcdbd7ffda1f9b495f65e6c1760c99e399eb9de60b1e
scope.30.id=bWV0aG9kOkJhbmsuU2ltcGxlI2NyZWF0ZUFjY291bnRGb3IoMSk6ODk
scope.30.kind=method
scope.30.startLine=89
scope.30.endLine=93
scope.30.semanticHash=73fa131dbc82d630608cf575987b0d9a6c964f3035e6f8aeb9c011b347b73279
scope.31.id=bWV0aG9kOkJhbmsuU2ltcGxlI2NyZWF0ZUFjY291bnRGb3IoMSk6OTU
scope.31.kind=method
scope.31.startLine=95
scope.31.endLine=98
scope.31.semanticHash=bbeb18e1b4eb21b18cb6a3bb101a8dde9449198308b85893af7e664f8ed2903e
scope.32.id=bWV0aG9kOkJhbmsuU2ltcGxlI2N0b3IoMCk6ODE
scope.32.kind=method
scope.32.startLine=1
scope.32.endLine=115
scope.32.semanticHash=7aba33b288b592b20bd2880ef26fb7795b9903611d8d12b120f9e0841cbf3865
*/
