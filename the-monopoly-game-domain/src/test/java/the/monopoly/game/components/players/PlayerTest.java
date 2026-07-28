package the.monopoly.game.components.players;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.StartSpace;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static the.monopoly.game.components.streets.Street.Type.RueGrandeDinant;
import static the.monopoly.game.rules.Rule.Type.double_salary_when_landing_on_start;

class PlayerTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();
  private final Bank bank = new Bank.Simple();

  @Test
  void passingStartPaysTheSalaryIntoTheAccount() {
    Player player = playerWith(1500);

    player.pass((StartSpace) ruleSet.create(Street.Type.start));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1700));
  }

  @Test
  void theDoubleSalaryRuleLeavesWhatPassingStartPaysAlone() {
    ruleSet.activate(double_salary_when_landing_on_start);
    Player player = playerWith(1500);

    player.pass((StartSpace) ruleSet.create(Street.Type.start));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1700));
  }

  @Test
  void visitingAVacantStreetChargesItsRent() {
    Player player = playerWith(1500);

    player.visit((ColourStreet) ruleSet.create(RueGrandeDinant));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1498));
  }

  @Test
  void thePoolHandsOutTheRequestedNumberOfPlayersEachWithStartingCapital() {
    Player.Pool pool = new Player.Pool(2, 8, bank, new Money(1500));

    List<Player> players = pool.select(3).toList();

    assertThat(players).hasSize(3);
    assertThat(players).extracting(Player::id)
        .containsExactly(Pawn.dog.id(), Pawn.high_hat.id(), Pawn.iron_box.id());
    assertThat(players).allSatisfy(
        player -> assertThat(player.account().balance()).isEqualTo(Balance.of(1500))
    );
  }

  @Test
  void everyPlayerInAFullPoolIsADistinctPawn() {
    Player.Pool pool = new Player.Pool(2, 8, bank, new Money(1500));

    assertThat(pool.select(8).map(Player::id).distinct().toList()).hasSize(8);
  }

  @Test
  void landingOnStartPaysTheSalaryIntoTheAccount() {
    Player player = playerWith(1500);

    player.land((StartSpace) ruleSet.create(Street.Type.start));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1700));
  }

  @Test
  void theDoubleSalaryRuleDoublesWhatLandingOnStartPays() {
    ruleSet.activate(double_salary_when_landing_on_start);
    Player player = playerWith(1500);

    player.land((StartSpace) ruleSet.create(Street.Type.start));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1900));
  }

  @Test
  void thePoolNeverHandsOutMoreThanTheBoardAllows() {
    Player.Pool pool = new Player.Pool(2, 8, bank, new Money(1500));

    assertThat(pool.select(99).toList()).hasSize(8);
  }

  @Test
  void playersSortByTheirIdentifier() {
    Comparator<Player.ID> natural = Player.ID.Comparators.natural();

    assertThat(natural.compare(new Player.ID("0"), new Player.ID("1"))).isNegative();
    assertThat(natural.compare(new Player.ID("1"), new Player.ID("0"))).isPositive();
    assertThat(natural.compare(new Player.ID("1"), new Player.ID("1"))).isZero();
  }

  @Test
  void aPlayerJoinsTheGameOnStart() {
    assertThat(playerWith(1500).position()).isEqualTo(new Player.Position(0));
    assertThat(playerWith(1500).position().index()).isZero();
  }

  @Test
  void twoPawnsAreOnTheSameSpaceWhenTheirPositionsMatch() {
    assertThat(new Player.Position(7)).isEqualTo(new Player.Position(7));
    assertThat(new Player.Position(7)).hasSameHashCodeAs(new Player.Position(7));
    assertThat(new Player.Position(7)).isNotEqualTo(new Player.Position(8));
    assertThat(new Player.Position(7)).isNotEqualTo("7");
  }

  /**
   * A player is printed whole when something goes wrong, and a position that
   * did not say which space it was would take the useful part of that message
   * with it.
   */
  @Test
  void aPositionSaysWhichSpaceItIsWhenPrinted() {
    assertThat(new Player.Position(7)).hasToString("Position[index=7]");
    assertThat(playerWith(1500)).asString().contains("Position[index=0]");
  }

  @Test
  void aPawnCarriesItsPositionRatherThanBeingReplaced() {
    Player.Position position = new Player.Position(3);

    position.moveTo(11);

    assertThat(position.index()).isEqualTo(11);
    assertThat(position).isEqualTo(new Player.Position(11));
  }

  private Player playerWith(int startingCapital) {
    Player.ID id = new Player.ID("0");
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(startingCapital));
    return player;
  }
}
