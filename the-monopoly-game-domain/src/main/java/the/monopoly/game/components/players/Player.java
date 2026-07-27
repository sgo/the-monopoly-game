package the.monopoly.game.components.players;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.StartSpace;

import java.util.Comparator;
import java.util.stream.Stream;

public record Player(ID id, Bank.Account account, Position position) {
  /** A player joins the game on Start. */
  public Player(ID id, Bank.Account account) {
    this(id, account, new Position(0));
  }

  public void pass(StartSpace start) {
    account.deposit(start.salary());
  }

  /**
   * Landing exactly on Start is the only move the optional double-salary rule
   * pays twice over; passing Start keeps paying the single salary.
   */
  public void land(StartSpace start) {
    account.deposit(start.salaryForLanding());
  }

  public void visit(ColourStreet street) {
    account.credit(street.vacantRent());
  }

  /**
   * Where a pawn stands, as an index into the board's spaces. A pawn moves
   * around one board for the whole game, so the position is carried rather
   * than replaced, in the same way an account carries its balance.
   */
  public static final class Position {
    private int index;

    public Position(int index) {
      this.index = index;
    }

    public int index() {
      return index;
    }

    public void moveTo(int index) {
      this.index = index;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Position position && index == position.index;
    }

    @Override
    public int hashCode() {
      return Integer.hashCode(index);
    }

    @Override
    public String toString() {
      return "Position[index=" + index + ']';
    }
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
moduleHash=0e40230e46243cc0b9fa55f2dc7e86d08cb6fcfd3a7822798ce3a5848fedd39c
scope.0.id=Y2xhc3M6UGxheWVyI1BsYXllcjoxMQ
scope.0.kind=class
scope.0.startLine=11
scope.0.endLine=54
scope.0.semanticHash=10e0d54d29bd537fa67ae03bf94bd6a2a92f00524125723ebfd2c6f7f2a0c68c
scope.1.id=Y2xhc3M6UGxheWVyLklEI0lEOjI4
scope.1.kind=class
scope.1.startLine=28
scope.1.endLine=38
scope.1.semanticHash=996488fc347146c47f30080f1c2486b438c8a1bc28fde9faa1b996e51b79ec67
scope.2.id=Y2xhc3M6UGxheWVyLklELkNvbXBhcmF0b3JzI0NvbXBhcmF0b3JzOjI5
scope.2.kind=class
scope.2.startLine=29
scope.2.endLine=37
scope.2.semanticHash=6a181655f4af1bccf4d2af926f179e5346a2b0744cb56a60c0987fab4190ed4a
scope.3.id=Y2xhc3M6UGxheWVyLlBvb2wjUG9vbDo0MA
scope.3.kind=class
scope.3.startLine=40
scope.3.endLine=53
scope.3.semanticHash=d215f761f2bd3cb91c6e86f5b024a06095531bdb2212e993bf9d5ca6c03444e4
scope.4.id=ZmllbGQ6UGxheWVyI2FjY291bnQ6MTE
scope.4.kind=field
scope.4.startLine=11
scope.4.endLine=11
scope.4.semanticHash=6871411ac14ea92cb16c1ae615831ec392a18de3e7245aec9a7613165f022987
scope.5.id=ZmllbGQ6UGxheWVyI2lkOjEx
scope.5.kind=field
scope.5.startLine=11
scope.5.endLine=11
scope.5.semanticHash=2684701f5e38c358ba07111fcc90c02880929515a74444328394ee5f5d7e07e3
scope.6.id=ZmllbGQ6UGxheWVyLklEI3ZhbHVlOjI4
scope.6.kind=field
scope.6.startLine=28
scope.6.endLine=28
scope.6.semanticHash=a48d9fcc7d0c3f83e5ade1eb6946a1fd0740fa16c45390faaff8a981c71106c6
scope.7.id=ZmllbGQ6UGxheWVyLlBvb2wjYmFuazo0MA
scope.7.kind=field
scope.7.startLine=40
scope.7.endLine=40
scope.7.semanticHash=ca2f8e7e1c77cdd8face64b1c7f3c3cd9bf0d26dac78e2d8225f4c334767d5a7
scope.8.id=ZmllbGQ6UGxheWVyLlBvb2wjbWF4OjQw
scope.8.kind=field
scope.8.startLine=40
scope.8.endLine=40
scope.8.semanticHash=dfa12e0401e85020bfe08c72ef190503490c52d47ec5ea0e5d83d3c8a03f3192
scope.9.id=ZmllbGQ6UGxheWVyLlBvb2wjbWluOjQw
scope.9.kind=field
scope.9.startLine=40
scope.9.endLine=40
scope.9.semanticHash=3d9188954a905632917c22b89b8844f4f94eb75f36bfc15e287962e3d5d127f0
scope.10.id=ZmllbGQ6UGxheWVyLlBvb2wjc3RhcnRpbmdDYXBpdGFsOjQw
scope.10.kind=field
scope.10.startLine=40
scope.10.endLine=40
scope.10.semanticHash=95e174f584c3a3b3ba2ff60a7549e9e88ff00bd67335665d1e4bf434683dc85a
scope.11.id=bWV0aG9kOlBsYXllciNjdG9yKDIpOjEx
scope.11.kind=method
scope.11.startLine=1
scope.11.endLine=54
scope.11.semanticHash=1d133588ca58c1367234add267cc671856faf418aff7e833e5199c85837d0eb0
scope.12.id=bWV0aG9kOlBsYXllciNsYW5kKDEpOjIw
scope.12.kind=method
scope.12.startLine=20
scope.12.endLine=22
scope.12.semanticHash=7370766c811ba3c71011de24e3bebea4c7307e9bbbd2d482ebc83b80c0af0d62
scope.13.id=bWV0aG9kOlBsYXllciNwYXNzKDEpOjEy
scope.13.kind=method
scope.13.startLine=12
scope.13.endLine=14
scope.13.semanticHash=bc94811cabfc0210553fedb998f6f667896f83d09860c6e97156046dbc340dc3
scope.14.id=bWV0aG9kOlBsYXllciN2aXNpdCgxKToyNA
scope.14.kind=method
scope.14.startLine=24
scope.14.endLine=26
scope.14.semanticHash=03cc14e23d867feacd6fcfcd3be5f2735a382fc31d6c8597996dd14c8f58aa79
scope.15.id=bWV0aG9kOlBsYXllci5JRCNjdG9yKDEpOjI4
scope.15.kind=method
scope.15.startLine=1
scope.15.endLine=54
scope.15.semanticHash=1d133588ca58c1367234add267cc671856faf418aff7e833e5199c85837d0eb0
scope.16.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNjdG9yKDApOjI5
scope.16.kind=method
scope.16.startLine=1
scope.16.endLine=54
scope.16.semanticHash=1d133588ca58c1367234add267cc671856faf418aff7e833e5199c85837d0eb0
scope.17.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNuYXR1cmFsKDApOjMw
scope.17.kind=method
scope.17.startLine=30
scope.17.endLine=32
scope.17.semanticHash=f333f0f41d8b4e74f89e71551d6bd2a90efc2bb60fd7d53edbe1a9ee2bd22b3a
scope.18.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNuYXR1cmFsKDIpOjM0
scope.18.kind=method
scope.18.startLine=34
scope.18.endLine=36
scope.18.semanticHash=1ebf5bba0f500a67aa3fab1b2524aefdacc88b837de0c5013f31c83bf6e33ae2
scope.19.id=bWV0aG9kOlBsYXllci5Qb29sI2N0b3IoNCk6NDA
scope.19.kind=method
scope.19.startLine=1
scope.19.endLine=54
scope.19.semanticHash=1d133588ca58c1367234add267cc671856faf418aff7e833e5199c85837d0eb0
scope.20.id=bWV0aG9kOlBsYXllci5Qb29sI3NlbGVjdCgxKTo0MQ
scope.20.kind=method
scope.20.startLine=41
scope.20.endLine=43
scope.20.semanticHash=d17fd60cb657a87bc8be62e4b7886d65e90e90f2667c453a1dc4df045edf0199
scope.21.id=bWV0aG9kOlBsYXllci5Qb29sI3N0cmVhbSgwKTo0NQ
scope.21.kind=method
scope.21.startLine=45
scope.21.endLine=52
scope.21.semanticHash=d749632b4e476412c0b1e761f1bbe6d0b3cdcf8d99d56bf317be03a4950352af
*/
