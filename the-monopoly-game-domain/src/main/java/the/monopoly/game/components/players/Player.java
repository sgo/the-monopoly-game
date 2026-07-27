package the.monopoly.game.components.players;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.StartSpace;

import java.util.Comparator;
import java.util.stream.Stream;

public record Player(ID id, Bank.Account account) {
  public void pass(StartSpace start) {
    account.deposit(start.salary());
  }

  /**
   * Landing on Start pays the same salary as passing it. The two are separate
   * moves because the rules distinguish them, even though nothing yet pays them
   * differently: the double-salary rule is carried by {@link StartSpace}.
   */
  public void land(StartSpace start) {
    account.deposit(start.salary());
  }

  public void visit(ColourStreet street) {
    account.credit(street.vacantRent());
  }

  public record ID(String value) {
    public static class Comparators {
      public static Comparator<Player.ID> natural() {
        return Comparators::natural;
      }

      private static int natural(Player.ID x, Player.ID y) {
        return x.value().compareTo(y.value());
      }
    }
  }

  public record Pool(int min, int max, Bank bank, Money startingCapital) {
    public Stream<Player> select(int numberOfPlayers) {
      return stream().limit(numberOfPlayers);
    }

    private Stream<Player> stream() {
      return Stream.of(Pawn.values())
          .limit(max)
          .map(Pawn::id)
          .peek(bank::createAccountFor)
          .peek(id -> bank.accountOf(id).deposit(startingCapital))
          .map(id -> new Player(id, bank.accountOf(id)));
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=14fae003e02e85231608f3c0eaa300d74bdae1598d67e6f04a5f388c45dd4e86
scope.0.id=Y2xhc3M6UGxheWVyI1BsYXllcjoxMg
scope.0.kind=class
scope.0.startLine=12
scope.0.endLine=47
scope.0.semanticHash=35b90d582c908b7e456ab06b904bcaee85b10ea5d56bb761cc4bb6f22954a977
scope.1.id=Y2xhc3M6UGxheWVyLklEI0lEOjIx
scope.1.kind=class
scope.1.startLine=21
scope.1.endLine=31
scope.1.semanticHash=996488fc347146c47f30080f1c2486b438c8a1bc28fde9faa1b996e51b79ec67
scope.2.id=Y2xhc3M6UGxheWVyLklELkNvbXBhcmF0b3JzI0NvbXBhcmF0b3JzOjIy
scope.2.kind=class
scope.2.startLine=22
scope.2.endLine=30
scope.2.semanticHash=6a181655f4af1bccf4d2af926f179e5346a2b0744cb56a60c0987fab4190ed4a
scope.3.id=Y2xhc3M6UGxheWVyLlBvb2wjUG9vbDozMw
scope.3.kind=class
scope.3.startLine=33
scope.3.endLine=46
scope.3.semanticHash=ee92529c83d026cba6ded112951568f9bac767499fade0c863b7bf9e141a02ff
scope.4.id=ZmllbGQ6UGxheWVyI2FjY291bnQ6MTI
scope.4.kind=field
scope.4.startLine=12
scope.4.endLine=12
scope.4.semanticHash=6871411ac14ea92cb16c1ae615831ec392a18de3e7245aec9a7613165f022987
scope.5.id=ZmllbGQ6UGxheWVyI2lkOjEy
scope.5.kind=field
scope.5.startLine=12
scope.5.endLine=12
scope.5.semanticHash=2684701f5e38c358ba07111fcc90c02880929515a74444328394ee5f5d7e07e3
scope.6.id=ZmllbGQ6UGxheWVyLklEI3ZhbHVlOjIx
scope.6.kind=field
scope.6.startLine=21
scope.6.endLine=21
scope.6.semanticHash=a48d9fcc7d0c3f83e5ade1eb6946a1fd0740fa16c45390faaff8a981c71106c6
scope.7.id=ZmllbGQ6UGxheWVyLlBvb2wjYmFuazozMw
scope.7.kind=field
scope.7.startLine=33
scope.7.endLine=33
scope.7.semanticHash=ca2f8e7e1c77cdd8face64b1c7f3c3cd9bf0d26dac78e2d8225f4c334767d5a7
scope.8.id=ZmllbGQ6UGxheWVyLlBvb2wjbWF4OjMz
scope.8.kind=field
scope.8.startLine=33
scope.8.endLine=33
scope.8.semanticHash=dfa12e0401e85020bfe08c72ef190503490c52d47ec5ea0e5d83d3c8a03f3192
scope.9.id=ZmllbGQ6UGxheWVyLlBvb2wjbWluOjMz
scope.9.kind=field
scope.9.startLine=33
scope.9.endLine=33
scope.9.semanticHash=3d9188954a905632917c22b89b8844f4f94eb75f36bfc15e287962e3d5d127f0
scope.10.id=ZmllbGQ6UGxheWVyLlBvb2wjc3RhcnRpbmdDYXBpdGFsOjMz
scope.10.kind=field
scope.10.startLine=33
scope.10.endLine=33
scope.10.semanticHash=95e174f584c3a3b3ba2ff60a7549e9e88ff00bd67335665d1e4bf434683dc85a
scope.11.id=bWV0aG9kOlBsYXllciNjdG9yKDIpOjEy
scope.11.kind=method
scope.11.startLine=1
scope.11.endLine=47
scope.11.semanticHash=5fdda5dfec4a0d1e3a9799d3f1e9baf56cae324ea46708e42dd430725c3abdaa
scope.12.id=bWV0aG9kOlBsYXllciNwYXNzKDEpOjEz
scope.12.kind=method
scope.12.startLine=13
scope.12.endLine=15
scope.12.semanticHash=bc94811cabfc0210553fedb998f6f667896f83d09860c6e97156046dbc340dc3
scope.13.id=bWV0aG9kOlBsYXllciN2aXNpdCgxKToxNw
scope.13.kind=method
scope.13.startLine=17
scope.13.endLine=19
scope.13.semanticHash=03cc14e23d867feacd6fcfcd3be5f2735a382fc31d6c8597996dd14c8f58aa79
scope.14.id=bWV0aG9kOlBsYXllci5JRCNjdG9yKDEpOjIx
scope.14.kind=method
scope.14.startLine=1
scope.14.endLine=47
scope.14.semanticHash=5fdda5dfec4a0d1e3a9799d3f1e9baf56cae324ea46708e42dd430725c3abdaa
scope.15.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNjdG9yKDApOjIy
scope.15.kind=method
scope.15.startLine=1
scope.15.endLine=47
scope.15.semanticHash=5fdda5dfec4a0d1e3a9799d3f1e9baf56cae324ea46708e42dd430725c3abdaa
scope.16.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNuYXR1cmFsKDApOjIz
scope.16.kind=method
scope.16.startLine=23
scope.16.endLine=25
scope.16.semanticHash=f333f0f41d8b4e74f89e71551d6bd2a90efc2bb60fd7d53edbe1a9ee2bd22b3a
scope.17.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNuYXR1cmFsKDIpOjI3
scope.17.kind=method
scope.17.startLine=27
scope.17.endLine=29
scope.17.semanticHash=1ebf5bba0f500a67aa3fab1b2524aefdacc88b837de0c5013f31c83bf6e33ae2
scope.18.id=bWV0aG9kOlBsYXllci5Qb29sI2N0b3IoNCk6MzM
scope.18.kind=method
scope.18.startLine=1
scope.18.endLine=47
scope.18.semanticHash=5fdda5dfec4a0d1e3a9799d3f1e9baf56cae324ea46708e42dd430725c3abdaa
scope.19.id=bWV0aG9kOlBsYXllci5Qb29sI3NlbGVjdCgxKTozNA
scope.19.kind=method
scope.19.startLine=34
scope.19.endLine=36
scope.19.semanticHash=d17fd60cb657a87bc8be62e4b7886d65e90e90f2667c453a1dc4df045edf0199
scope.20.id=bWV0aG9kOlBsYXllci5Qb29sI3N0cmVhbSgwKTozOA
scope.20.kind=method
scope.20.startLine=38
scope.20.endLine=45
scope.20.semanticHash=56745c95ec615a7a2d7bb1e683d779c166c54b7165287f3b35756e9283dac934
*/
