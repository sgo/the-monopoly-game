package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.board.Board;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static the.monopoly.game.rules.Rule.Type.double_salary_when_landing_on_start;

class TurnTest {
  private static final int JAIL = 10;

  private final Rule.Set ruleSet = Rule.Set.Type.official.create();

  @Test
  void aTurnMovesThePawnTheTotalOfBothDice() {
    Player player = playerAt(5, 1500);

    takeTurn(player, new Roll(2, 3));

    assertThat(player.position().index()).isEqualTo(10);
  }

  @Test
  void movingWithoutReachingStartPaysNothing() {
    Player player = playerAt(5, 1500);

    takeTurn(player, new Roll(2, 3));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1500));
  }

  @Test
  void thePawnWrapsAroundTheBoard() {
    Player player = playerAt(37, 1500);

    takeTurn(player, new Roll(2, 3));

    assertThat(player.position().index()).isEqualTo(2);
  }

  @Test
  void passingStartPaysTheSalary() {
    Player player = playerAt(37, 1500);

    takeTurn(player, new Roll(2, 3));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1700));
  }

  @Test
  void landingExactlyOnStartPaysTheSalary() {
    Player player = playerAt(37, 1500);

    takeTurn(player, new Roll(1, 2));

    assertThat(player.position().index()).isZero();
    assertThat(player.account().balance()).isEqualTo(Balance.of(1700));
  }

  @Test
  void theDoubleSalaryRulePaysTwiceForLandingExactlyOnStart() {
    ruleSet.activate(double_salary_when_landing_on_start);
    Player player = playerAt(37, 1500);

    takeTurn(player, new Roll(1, 2));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1900));
  }

  @Test
  void theDoubleSalaryRuleStillPaysOnceForOnlyPassingStart() {
    ruleSet.activate(double_salary_when_landing_on_start);
    Player player = playerAt(37, 1500);

    takeTurn(player, new Roll(2, 3));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1700));
  }

  @Test
  void rollingDoublesGrantsAnotherRollInTheSameTurn() {
    Player player = playerAt(0, 1500);

    takeTurn(player, new Roll(3, 3), new Roll(2, 4));

    assertThat(player.position().index()).isEqualTo(12);
  }

  @Test
  void aTurnEndsAsSoonAsARollIsNotADouble() {
    Player player = playerAt(0, 1500);

    takeTurn(player, new Roll(3, 3), new Roll(2, 4), new Roll(1, 1));

    assertThat(player.position().index()).isEqualTo(12);
  }

  @Test
  void rollingDoublesThreeTimesInARowSendsThePawnStraightToJail() {
    Player player = playerAt(0, 1500);

    takeTurn(player, new Roll(2, 2), new Roll(5, 5), new Roll(1, 1));

    assertThat(player.position().index()).isEqualTo(JAIL);
  }

  @Test
  void goingToJailOnThreeDoublesTakesTheThirdMoveAway() {
    Player player = playerAt(0, 1500);

    takeTurn(player, new Roll(2, 2), new Roll(5, 5), new Roll(1, 1));

    assertThat(player.account().balance()).isEqualTo(Balance.of(1500));
  }

  @Test
  void aPawnSentToJailPastStartIsNotPaidForPassingIt() {
    Player player = playerAt(36, 1500);

    takeTurn(player, new Roll(1, 1), new Roll(2, 2), new Roll(3, 3));

    assertThat(player.position().index()).isEqualTo(JAIL);
    assertThat(player.account().balance()).isEqualTo(Balance.of(1700));
  }

  /**
   * A board with nowhere to send a cheat is not a board this can be played on,
   * and the turn says so rather than moving the pawn somewhere arbitrary.
   */
  @Test
  void aBoardWithNoJailCannotSendAnyoneToIt() {
    Rule.Set jailless = ruleSetOn(new Board(List.of(Street.Type.start, Street.Type.RueGrandeDinant)));
    Player player = playerAt(0, 1500);

    assertThatThrownBy(() -> new Turn(jailless, Cup.of(new Roll(1, 1), new Roll(2, 2), new Roll(3, 3))).take(player))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("OpBezoek");
  }

  /**
   * Nothing says the jail cannot be the first space on a board, so a cheat is
   * sent to space zero rather than the move being refused for looking like the
   * "no such space" answer.
   */
  @Test
  void aBoardMayKeepItsJailAtTheVeryFirstSpace() {
    Rule.Set jailFirst = ruleSetOn(new Board(List.of(
        Street.Type.OpBezoek, Street.Type.start, Street.Type.RueGrandeDinant
    )));
    Player player = playerAt(0, 1500);

    new Turn(jailFirst, Cup.of(new Roll(1, 1), new Roll(2, 2), new Roll(3, 3))).take(player);

    assertThat(player.position().index()).isZero();
  }

  private Rule.Set ruleSetOn(Board board) {
    return new Rule.Set.Simple(
        board,
        ruleSet.dice().toList(),
        ruleSet.players(),
        ruleSet.bank(),
        new LinkedHashSet<>(),
        Map.of()
    );
  }

  private void takeTurn(Player player, Roll... rolls) {
    new Turn(ruleSet, Cup.of(rolls)).take(player);
  }

  private Player playerAt(int position, int balance) {
    Bank bank = ruleSet.bank();
    Player.ID id = new Player.ID("under test");
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    player.position().moveTo(position);
    return player;
  }
}
