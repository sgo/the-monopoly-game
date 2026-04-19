package the.monopoly.game.components.players;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;

import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Player(ID id) {
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
          .map(Player::new)
          .peek(bank::createAccountFor)
          .peek(player -> bank.accountOf(player).deposit(startingCapital));
    }
  }
}
