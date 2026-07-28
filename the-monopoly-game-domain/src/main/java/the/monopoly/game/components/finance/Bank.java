package the.monopoly.game.components.finance;

import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.players.Player;

import java.util.Set;
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

  record Simple(Set<Account> accountsBuffer) implements Bank {
    @Override
    public Stream<Account> accounts() {
      return accountsBuffer.stream();
    }

    @Override
    public void createAccountFor(Player.ID id) {
      accountsBuffer.add(new Account(
          new Account.Owner(id.value()),
          Balance.of(0)
      ));
    }

    @Override
    public Account accountOf(Player.ID id) {
      return accounts()
          .filter(it -> it.owner.name().equals(id.value()))
          .findAny()
          .orElseThrow(() -> new IllegalArgumentException("No account for player " + id.value() + "."));
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=fe248bace9165fc7dee5705822dcabb2b9e2bb0ff4ba8972ea18233b4890f2f5
scope.0.id=Y2xhc3M6QmFuayNCYW5rOjk
scope.0.kind=class
scope.0.startLine=9
scope.0.endLine=95
scope.0.semanticHash=40bcdb3d315333d633055fc84cdb797bd596c1a9476100da94717f57c72581b5
scope.1.id=Y2xhc3M6QmFuay5BY2NvdW50I0FjY291bnQ6MTY
scope.1.kind=class
scope.1.startLine=16
scope.1.endLine=71
scope.1.semanticHash=baee4afe7af0f5ae6b2b1d01918fb4f7ab89dd44d1d5d962c52b8654b7e98405
scope.2.id=Y2xhc3M6QmFuay5BY2NvdW50LkJhbGFuY2UjQmFsYW5jZToyOA
scope.2.kind=class
scope.2.startLine=28
scope.2.endLine=70
scope.2.semanticHash=202974e1fb7095f9a3ccd245b77151fa6900538a5f3af29b8bac1085223a344c
scope.3.id=Y2xhc3M6QmFuay5BY2NvdW50Lk93bmVyI093bmVyOjI1
scope.3.kind=class
scope.3.startLine=25
scope.3.endLine=26
scope.3.semanticHash=5c899306de31a7095ceed9fefdd367fa034a3b53ba69cd257dc3616bcdac68a7
scope.4.id=Y2xhc3M6QmFuay5TaW1wbGUjU2ltcGxlOjcz
scope.4.kind=class
scope.4.startLine=73
scope.4.endLine=94
scope.4.semanticHash=7bb2899a511169559023e0d3f84fed5dd9b3d8b53b4bbd3ee8a911a443abe4ae
scope.5.id=ZmllbGQ6QmFuay5BY2NvdW50I2JhbGFuY2U6MTY
scope.5.kind=field
scope.5.startLine=16
scope.5.endLine=16
scope.5.semanticHash=6c644e19cc28bb596c081e0673fa98122414ce5a3851a790604d3532a819b04c
scope.6.id=ZmllbGQ6QmFuay5BY2NvdW50I293bmVyOjE2
scope.6.kind=field
scope.6.startLine=16
scope.6.endLine=16
scope.6.semanticHash=689bd7aa9aca59e2676434ea7fce64304ec3f337b862ddfc99706e365f8d8615
scope.7.id=ZmllbGQ6QmFuay5BY2NvdW50LkJhbGFuY2UjYW1vdW50OjI5
scope.7.kind=field
scope.7.startLine=29
scope.7.endLine=29
scope.7.semanticHash=75ae19bd7fb06ca70fe95b0a12986c6a35282bdb6dcedbb2f29c34ceeb904cf7
scope.8.id=ZmllbGQ6QmFuay5BY2NvdW50Lk93bmVyI25hbWU6MjU
scope.8.kind=field
scope.8.startLine=25
scope.8.endLine=25
scope.8.semanticHash=28e8b9d0b6d83cf0ec13b6130883495dc7fce33f007e60550987b5da71347153
scope.9.id=ZmllbGQ6QmFuay5TaW1wbGUjYWNjb3VudHNCdWZmZXI6NzM
scope.9.kind=field
scope.9.startLine=73
scope.9.endLine=73
scope.9.semanticHash=73b9b4d7bfefb936e6c610bacfd29e00b63fda73cf4cb5f18d685e40885090dc
scope.10.id=bWV0aG9kOkJhbmsjYWNjb3VudE9mKDEpOjE0
scope.10.kind=method
scope.10.startLine=14
scope.10.endLine=14
scope.10.semanticHash=6dcbe6f3105cc7b92243a16487d9a8076ad45948b8b93f180e1f2703e9e1e1b2
scope.11.id=bWV0aG9kOkJhbmsjYWNjb3VudHMoMCk6MTA
scope.11.kind=method
scope.11.startLine=10
scope.11.endLine=10
scope.11.semanticHash=eaa0f07236b68bfbabebba7fb072c161d6558a458414c77d6b846ecfca1f5e96
scope.12.id=bWV0aG9kOkJhbmsjY3JlYXRlQWNjb3VudEZvcigxKToxMg
scope.12.kind=method
scope.12.startLine=12
scope.12.endLine=12
scope.12.semanticHash=e33c8982a0c6a8c3ae841a14e7d293844cd07c358ab20efc70a862f2a59e7672
scope.13.id=bWV0aG9kOkJhbmsuQWNjb3VudCNjdG9yKDIpOjE2
scope.13.kind=method
scope.13.startLine=1
scope.13.endLine=95
scope.13.semanticHash=c87cc804f0b31dc9cefdd08511993a426f914ca879459e6866fc0f8919f98a55
scope.14.id=bWV0aG9kOkJhbmsuQWNjb3VudCNkZXBvc2l0KDEpOjE3
scope.14.kind=method
scope.14.startLine=17
scope.14.endLine=19
scope.14.semanticHash=fc92f5c77052106d4d752d35971c65cba57f6a5b3ae2cb5c7290f08849c00a2a
scope.15.id=bWV0aG9kOkJhbmsuQWNjb3VudCN3aXRoZHJhdygxKToyMQ
scope.15.kind=method
scope.15.startLine=21
scope.15.endLine=23
scope.15.semanticHash=b9bba4021ca59f54c85909826ca859a957cc7af7e41121e166c0c898bc266f37
scope.16.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2Ftb3VudCgwKTo0MA
scope.16.kind=method
scope.16.startLine=40
scope.16.endLine=42
scope.16.semanticHash=97312681f677ca49fcbaacfec7b151e4066c726d3d9da3de8c35a349ad5b35c6
scope.17.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2N0b3IoMSk6MzE
scope.17.kind=method
scope.17.startLine=31
scope.17.endLine=33
scope.17.semanticHash=0b95b706797a6d668131bad07dcf3976f4206bb327ffe6ae9e0c8afa851b84df
scope.18.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2RlY3JlbWVudFdpdGgoMSk6NDg
scope.18.kind=method
scope.18.startLine=48
scope.18.endLine=50
scope.18.semanticHash=3622eaaa85dbc1fdd61f916800d48d314bc68029353cd4cd06e2f505bdc0ca4f
scope.19.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2VxdWFscygxKTo1Mg
scope.19.kind=method
scope.19.startLine=52
scope.19.endLine=57
scope.19.semanticHash=ae9feb67d3c5d5426468cfde5830dae1cf53fcd9b2657a9a6cb527133420652a
scope.20.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2hhc2hDb2RlKDApOjU5
scope.20.kind=method
scope.20.startLine=59
scope.20.endLine=62
scope.20.semanticHash=8059553674b527e0d5bc0876aa9c17aa0af16bb4a4d9e7c7f56f74dc84e69ec5
scope.21.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI2luY3JlbWVudFdpdGgoMSk6NDQ
scope.21.kind=method
scope.21.startLine=44
scope.21.endLine=46
scope.21.semanticHash=e117b790836ec14e352ad7cbe7f74b75eb84f40f22eb7d85042f2288de18225f
scope.22.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI29mKDEpOjM1
scope.22.kind=method
scope.22.startLine=35
scope.22.endLine=37
scope.22.semanticHash=0cb6a859462d708d5a0c6b85c03301f9d5cec0f570f900171d6ca449b495fccc
scope.23.id=bWV0aG9kOkJhbmsuQWNjb3VudC5CYWxhbmNlI3RvU3RyaW5nKDApOjY0
scope.23.kind=method
scope.23.startLine=64
scope.23.endLine=69
scope.23.semanticHash=71a9a670e152042035c57e2a58d6f7681b1182c998f1d856eef7784e41c7449b
scope.24.id=bWV0aG9kOkJhbmsuQWNjb3VudC5Pd25lciNjdG9yKDEpOjI1
scope.24.kind=method
scope.24.startLine=1
scope.24.endLine=95
scope.24.semanticHash=c87cc804f0b31dc9cefdd08511993a426f914ca879459e6866fc0f8919f98a55
scope.25.id=bWV0aG9kOkJhbmsuU2ltcGxlI2FjY291bnRPZigxKTo4Nw
scope.25.kind=method
scope.25.startLine=87
scope.25.endLine=93
scope.25.semanticHash=c8dc67d30fe04cf21b2749b61f66133b5e0455ca4536783756772aeb20e9a905
scope.26.id=bWV0aG9kOkJhbmsuU2ltcGxlI2FjY291bnRzKDApOjc0
scope.26.kind=method
scope.26.startLine=74
scope.26.endLine=77
scope.26.semanticHash=b05b044eb4b96f15c5cc6e94e4bf5d5e8d81347fc53991688a43c2dd025fd1cd
scope.27.id=bWV0aG9kOkJhbmsuU2ltcGxlI2NyZWF0ZUFjY291bnRGb3IoMSk6Nzk
scope.27.kind=method
scope.27.startLine=79
scope.27.endLine=85
scope.27.semanticHash=69149e26c6fca7428d3913e291e56ba7f38aed248bfc2ee45b96ab1bc204329d
scope.28.id=bWV0aG9kOkJhbmsuU2ltcGxlI2N0b3IoMSk6NzM
scope.28.kind=method
scope.28.startLine=1
scope.28.endLine=95
scope.28.semanticHash=c87cc804f0b31dc9cefdd08511993a426f914ca879459e6866fc0f8919f98a55
*/
