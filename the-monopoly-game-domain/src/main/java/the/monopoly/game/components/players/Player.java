package the.monopoly.game.components.players;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.Street;

import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Player(ID id, Bank.Account account) {
  public void pass(Street street) {
    account.credit(street.toll());
  }

  public void visit(Street street) {
    account.credit(street.rent());
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
      return IntStream.range(0, max)
          .mapToObj(Integer::toString)
          .map(ID::new)
          .peek(bank::createAccountFor)
          .peek(id -> bank.accountOf(id).deposit(startingCapital))
          .map(id -> new Player(id, bank.accountOf(id)));
    }
  }
}
