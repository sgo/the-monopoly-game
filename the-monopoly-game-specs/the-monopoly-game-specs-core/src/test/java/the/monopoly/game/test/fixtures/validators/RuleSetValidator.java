package the.monopoly.game.test.fixtures.validators;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.test.fixtures.repository.CurrentRuleTypeRepository;
import the.monopoly.game.test.fixtures.repository.RuleSetRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class RuleSetValidator {
  private final RuleSetRepository ruleSetRepository;
  private final CurrentRuleTypeRepository currentRuleTypeRepository;

  public RuleSetValidator(RuleSetRepository ruleSetRepository, CurrentRuleTypeRepository currentRuleTypeRepository) {
    this.ruleSetRepository = ruleSetRepository;
    this.currentRuleTypeRepository = currentRuleTypeRepository;
  }

  public void assertPlayWithDice(List<Dice.Type> expectations) {
    assertThat(currentRuleSet().dice())
        .extracting(Dice::type)
        .containsExactlyElementsOf(expectations);
  }

  private Rule.Set currentRuleSet() {
    return ruleSetRepository.get(currentRuleTypeRepository.get());
  }

  public void assertPlayWithMinMaxPlayers(int min, int max) {
    assertThat(currentRuleSet().players())
        .extracting(Player.Pool::min)
        .isEqualTo(min);
    assertThat(currentRuleSet().players())
        .extracting(Player.Pool::max)
        .isEqualTo(max);
  }

  public void assertGameboardEquals(List<BoardLayoutExpectation> expectation) {
    assertThat(currentRuleSet().gameboard().streets())
        .extracting(it -> new BoardLayoutExpectation(it.type(), it.colourGroup()))
        .containsExactlyElementsOf(expectation);
  }

  public record BoardLayoutExpectation(Street.Type type, Street.Colour colour) {
  }
}
