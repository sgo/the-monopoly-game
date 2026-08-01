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

  /** Returns the salary collected, which is what the account was paid. */
  public Money pass(StartSpace start) {
    Money salary = start.salary();
    account.deposit(salary);
    return salary;
  }

  /**
   * Landing exactly on Start is the only move the optional double-salary rule
   * pays twice over; passing Start keeps paying the single salary.
   */
  public Money land(StartSpace start) {
    Money salary = start.salaryForLanding();
    account.deposit(salary);
    return salary;
  }

  public void visit(ColourStreet street) {
    account.withdraw(street.vacantRent());
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

    /**
     * Selecting the same id from the same bank twice must not fund it twice: a
     * pawn already holding an account here was already dealt its starting
     * capital by an earlier selection, so this one only hands back a
     * {@link Player} for the account as it stands rather than depositing again.
     */
    private Stream<Player> stream() {
      return Stream.of(Pawn.values())
          .limit(max)
          .map(Pawn::id)
          .peek(id -> {
            boolean alreadyFunded = bank.accounts().anyMatch(
                account -> account.owner().equals(new Bank.Account.Owner(id.value())));
            bank.createAccountFor(id);
            if (!alreadyFunded) bank.accountOf(id).deposit(startingCapital);
          })
          .map(id -> new Player(id, bank.accountOf(id)));
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=73673ff6a04db01c39897e043d40f4cc243c83c204e9d43dda64a9f3218fadf2
scope.0.id=Y2xhc3M6UGxheWVyI1BsYXllcjoxMQ
scope.0.kind=class
scope.0.startLine=11
scope.0.endLine=100
scope.0.semanticHash=2f8f4f8cb8d1877521c7f45f101b2109e2c84a2390ad5e10e601eab8dd536cbc
scope.1.id=Y2xhc3M6UGxheWVyLklEI0lEOjc0
scope.1.kind=class
scope.1.startLine=74
scope.1.endLine=84
scope.1.semanticHash=996488fc347146c47f30080f1c2486b438c8a1bc28fde9faa1b996e51b79ec67
scope.2.id=Y2xhc3M6UGxheWVyLklELkNvbXBhcmF0b3JzI0NvbXBhcmF0b3JzOjc1
scope.2.kind=class
scope.2.startLine=75
scope.2.endLine=83
scope.2.semanticHash=6a181655f4af1bccf4d2af926f179e5346a2b0744cb56a60c0987fab4190ed4a
scope.3.id=Y2xhc3M6UGxheWVyLlBvb2wjUG9vbDo4Ng
scope.3.kind=class
scope.3.startLine=86
scope.3.endLine=99
scope.3.semanticHash=d215f761f2bd3cb91c6e86f5b024a06095531bdb2212e993bf9d5ca6c03444e4
scope.4.id=Y2xhc3M6UGxheWVyLlBvc2l0aW9uI1Bvc2l0aW9uOjQz
scope.4.kind=class
scope.4.startLine=43
scope.4.endLine=72
scope.4.semanticHash=d0408d5bea3bbc54bd51ec4e62d150434d253dbcf543422a478203c165a5f75a
scope.5.id=ZmllbGQ6UGxheWVyI2FjY291bnQ6MTE
scope.5.kind=field
scope.5.startLine=11
scope.5.endLine=11
scope.5.semanticHash=6871411ac14ea92cb16c1ae615831ec392a18de3e7245aec9a7613165f022987
scope.6.id=ZmllbGQ6UGxheWVyI2lkOjEx
scope.6.kind=field
scope.6.startLine=11
scope.6.endLine=11
scope.6.semanticHash=2684701f5e38c358ba07111fcc90c02880929515a74444328394ee5f5d7e07e3
scope.7.id=ZmllbGQ6UGxheWVyI3Bvc2l0aW9uOjEx
scope.7.kind=field
scope.7.startLine=11
scope.7.endLine=11
scope.7.semanticHash=ac70a36a5f0182a0ae2437559adab4176d39ea892b3bae9eb0e873b02a4d7718
scope.8.id=ZmllbGQ6UGxheWVyLklEI3ZhbHVlOjc0
scope.8.kind=field
scope.8.startLine=74
scope.8.endLine=74
scope.8.semanticHash=a48d9fcc7d0c3f83e5ade1eb6946a1fd0740fa16c45390faaff8a981c71106c6
scope.9.id=ZmllbGQ6UGxheWVyLlBvb2wjYmFuazo4Ng
scope.9.kind=field
scope.9.startLine=86
scope.9.endLine=86
scope.9.semanticHash=ca2f8e7e1c77cdd8face64b1c7f3c3cd9bf0d26dac78e2d8225f4c334767d5a7
scope.10.id=ZmllbGQ6UGxheWVyLlBvb2wjbWF4Ojg2
scope.10.kind=field
scope.10.startLine=86
scope.10.endLine=86
scope.10.semanticHash=dfa12e0401e85020bfe08c72ef190503490c52d47ec5ea0e5d83d3c8a03f3192
scope.11.id=ZmllbGQ6UGxheWVyLlBvb2wjbWluOjg2
scope.11.kind=field
scope.11.startLine=86
scope.11.endLine=86
scope.11.semanticHash=3d9188954a905632917c22b89b8844f4f94eb75f36bfc15e287962e3d5d127f0
scope.12.id=ZmllbGQ6UGxheWVyLlBvb2wjc3RhcnRpbmdDYXBpdGFsOjg2
scope.12.kind=field
scope.12.startLine=86
scope.12.endLine=86
scope.12.semanticHash=95e174f584c3a3b3ba2ff60a7549e9e88ff00bd67335665d1e4bf434683dc85a
scope.13.id=ZmllbGQ6UGxheWVyLlBvc2l0aW9uI2luZGV4OjQ0
scope.13.kind=field
scope.13.startLine=44
scope.13.endLine=44
scope.13.semanticHash=0056889b1b64daca37d9b40335295e881e1074370b731d6321db2b12d6809411
scope.14.id=bWV0aG9kOlBsYXllciNjdG9yKDIpOjEz
scope.14.kind=method
scope.14.startLine=13
scope.14.endLine=15
scope.14.semanticHash=32678ab22c2607d491af14d43ee7edfec5df7a8131b262e747609fe6570f9668
scope.15.id=bWV0aG9kOlBsYXllciNjdG9yKDMpOjEx
scope.15.kind=method
scope.15.startLine=1
scope.15.endLine=100
scope.15.semanticHash=1dab0d683df3843026a16591ccf96d5e820185230b4b7d1fa9c84787903a10fd
scope.16.id=bWV0aG9kOlBsYXllciNsYW5kKDEpOjI4
scope.16.kind=method
scope.16.startLine=28
scope.16.endLine=32
scope.16.semanticHash=288bd3b9cbad693637cd6da676fd6ecb80f6c8594b12b6761d06fd349bc3da7a
scope.17.id=bWV0aG9kOlBsYXllciNwYXNzKDEpOjE4
scope.17.kind=method
scope.17.startLine=18
scope.17.endLine=22
scope.17.semanticHash=3689a5e9ecb688cb8fabdeef0b6f47a2016da6bc70dc9bdc428f335dbe5cc771
scope.18.id=bWV0aG9kOlBsYXllciN2aXNpdCgxKTozNA
scope.18.kind=method
scope.18.startLine=34
scope.18.endLine=36
scope.18.semanticHash=c3391457e2ee48c043f07639cc03fb209fbdf8ffb534b7d164f1f516258589f1
scope.19.id=bWV0aG9kOlBsYXllci5JRCNjdG9yKDEpOjc0
scope.19.kind=method
scope.19.startLine=1
scope.19.endLine=100
scope.19.semanticHash=1dab0d683df3843026a16591ccf96d5e820185230b4b7d1fa9c84787903a10fd
scope.20.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNjdG9yKDApOjc1
scope.20.kind=method
scope.20.startLine=1
scope.20.endLine=100
scope.20.semanticHash=1dab0d683df3843026a16591ccf96d5e820185230b4b7d1fa9c84787903a10fd
scope.21.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNuYXR1cmFsKDApOjc2
scope.21.kind=method
scope.21.startLine=76
scope.21.endLine=78
scope.21.semanticHash=f333f0f41d8b4e74f89e71551d6bd2a90efc2bb60fd7d53edbe1a9ee2bd22b3a
scope.22.id=bWV0aG9kOlBsYXllci5JRC5Db21wYXJhdG9ycyNuYXR1cmFsKDIpOjgw
scope.22.kind=method
scope.22.startLine=80
scope.22.endLine=82
scope.22.semanticHash=1ebf5bba0f500a67aa3fab1b2524aefdacc88b837de0c5013f31c83bf6e33ae2
scope.23.id=bWV0aG9kOlBsYXllci5Qb29sI2N0b3IoNCk6ODY
scope.23.kind=method
scope.23.startLine=1
scope.23.endLine=100
scope.23.semanticHash=1dab0d683df3843026a16591ccf96d5e820185230b4b7d1fa9c84787903a10fd
scope.24.id=bWV0aG9kOlBsYXllci5Qb29sI3NlbGVjdCgxKTo4Nw
scope.24.kind=method
scope.24.startLine=87
scope.24.endLine=89
scope.24.semanticHash=d17fd60cb657a87bc8be62e4b7886d65e90e90f2667c453a1dc4df045edf0199
scope.25.id=bWV0aG9kOlBsYXllci5Qb29sI3N0cmVhbSgwKTo5MQ
scope.25.kind=method
scope.25.startLine=91
scope.25.endLine=98
scope.25.semanticHash=d749632b4e476412c0b1e761f1bbe6d0b3cdcf8d99d56bf317be03a4950352af
scope.26.id=bWV0aG9kOlBsYXllci5Qb3NpdGlvbiNjdG9yKDEpOjQ2
scope.26.kind=method
scope.26.startLine=46
scope.26.endLine=48
scope.26.semanticHash=ca7b9bc9675edc759a53865c09fe78683461668e8d8e007c0575fdb0f5e4fdc8
scope.27.id=bWV0aG9kOlBsYXllci5Qb3NpdGlvbiNlcXVhbHMoMSk6NTg
scope.27.kind=method
scope.27.startLine=58
scope.27.endLine=61
scope.27.semanticHash=8f1f4f509fd7c8b37f67658324c6e58babbaa35ca61fb8db9444451b7a4e1955
scope.28.id=bWV0aG9kOlBsYXllci5Qb3NpdGlvbiNoYXNoQ29kZSgwKTo2Mw
scope.28.kind=method
scope.28.startLine=63
scope.28.endLine=66
scope.28.semanticHash=235535e5c18b9ad6df5c52b774e7a7cf60a19cbcad471bfcaf64d09b2ad8194e
scope.29.id=bWV0aG9kOlBsYXllci5Qb3NpdGlvbiNpbmRleCgwKTo1MA
scope.29.kind=method
scope.29.startLine=50
scope.29.endLine=52
scope.29.semanticHash=e57733aa4aca0765b32fce6335e4b32d232866af6d6fd816e63fb9aef5414de9
scope.30.id=bWV0aG9kOlBsYXllci5Qb3NpdGlvbiNtb3ZlVG8oMSk6NTQ
scope.30.kind=method
scope.30.startLine=54
scope.30.endLine=56
scope.30.semanticHash=aa66f5bef3528a5c3b3ad5f460c50e0b293379f876319df0ccfe175a7727e25f
scope.31.id=bWV0aG9kOlBsYXllci5Qb3NpdGlvbiN0b1N0cmluZygwKTo2OA
scope.31.kind=method
scope.31.startLine=68
scope.31.endLine=71
scope.31.semanticHash=94bdab56d9aafd1258a14ce8f7cd5ee4110fcf446f78f957f0a2d96118aaf9e4
*/
