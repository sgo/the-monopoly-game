package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.IntDistribution;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The example-based coverage in {@link DevelopmentLoanBookForeclosureTest}
 * and {@link DevelopmentLoanBookTest} pins a couple of hand-picked bid
 * combinations. {@code Auction} itself had no dedicated test at all before
 * this - the exact mechanism behind loan-foreclosure-null-winner-desync,
 * where every {@code floorOpening=false}-qualified bidder's ceiling can sit
 * below the opening price and {@code ascendMany} legitimately returns a null
 * winner. This sweeps arbitrary ceiling combinations to pin the invariant
 * every multi-bidder caller must respect: with two or more bidders, a
 * winner exists exactly when someone's ceiling reaches the opening price,
 * and nobody ever wins for more than they offered or less than the floor.
 * A lone qualified bidder is a documented exception (see the class
 * javadoc): they always win, paying their ceiling capped at the opening
 * price, whether or not that ceiling itself reaches the opening price -
 * pinned separately below rather than folded into the same property.
 */
@Tag("property-test")
class AuctionPropertyTest {
  private static final Rule.Set RULES = Rule.Set.Type.official.create();
  private static final ColourStreet LAND = (ColourStreet) RULES.create(Street.Type.RueGrandeDinant);
  private static final int OPENING = LAND.landMortgageValue().amount();
  private static final Generator<Integer> CEILINGS = Generator.integers(0, OPENING * 2);
  private static final Generator<Integer> BIDDER_COUNTS = Generator.integers(2, 6);

  @Test
  void withTwoOrMoreBiddersAWinnerExistsExactlyWhenSomeCeilingReachesTheOpeningPrice() {
    PropertyChecker.forAll(cases(), c -> {
      List<Player> bidders = players(c.ceilings().size());
      Auction.Bidders qualified = new Auction.Bidders(bidders,
          c.ceilings().stream().map(Money::new).toList(), new Money(OPENING));

      Auction.Result result = Auction.ascend(qualified);

      boolean anyCeilingReachesOpening = c.ceilings().stream().anyMatch(ceiling -> ceiling >= OPENING);
      if (!anyCeilingReachesOpening) return result.winner() == null;
      if (result.winner() == null) return false;
      int winnerIndex = bidders.indexOf(result.winner());
      return result.bid().amount() >= OPENING && result.bid().amount() <= c.ceilings().get(winnerIndex);
    });
  }

  @Test
  void aLoneQualifiedBidderAlwaysWinsAtTheirCeilingCappedByTheOpeningPrice() {
    PropertyChecker.forAll(Generator.integers(0, 10_000), ceiling -> {
      List<Player> bidder = players(1);
      Auction.Bidders qualified = new Auction.Bidders(bidder, List.of(new Money(ceiling)), new Money(OPENING));

      Auction.Result result = Auction.ascend(qualified);

      return result.winner() == bidder.getFirst()
          && result.bid().amount() == Math.min(ceiling, OPENING);
    });
  }

  private Generator<Case> cases() {
    return BIDDER_COUNTS.flatMap(count ->
        Generator.listsOf(IntDistribution.uniform(count, count), CEILINGS).map(Case::new));
  }

  private List<Player> players(int count) {
    Bank bank = new Bank.Simple();
    return IntStream.range(0, count).mapToObj(index -> {
      Player.ID id = new Player.ID("bidder" + index);
      bank.createAccountFor(id);
      return new Player(id, bank.accountOf(id));
    }).toList();
  }

  private record Case(List<Integer> ceilings) {
  }
}
