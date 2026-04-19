package the.monopoly.game.test.fixtures.services;

import org.springframework.stereotype.Service;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.test.fixtures.repository.CurrentPlayerRepository;
import the.monopoly.game.test.fixtures.repository.CurrentRuleTypeRepository;
import the.monopoly.game.test.fixtures.repository.PlayerRepository;
import the.monopoly.game.test.fixtures.repository.RuleSetRepository;

@Service
public class PlayerService {
  private final RuleSetRepository ruleSetRepository;
  private final CurrentRuleTypeRepository currentRuleTypeRepository;
  private final PlayerRepository playerRepository;
  private final CurrentPlayerRepository currentPlayerRepository;

  public PlayerService(
      RuleSetRepository ruleSetRepository,
      CurrentRuleTypeRepository currentRuleTypeRepository,
      PlayerRepository playerRepository,
      CurrentPlayerRepository currentPlayerRepository
  ) {
    this.ruleSetRepository = ruleSetRepository;
    this.currentRuleTypeRepository = currentRuleTypeRepository;
    this.playerRepository = playerRepository;
    this.currentPlayerRepository = currentPlayerRepository;
  }

  public void createAtLeastOnePLayer() {
    currentRules().players().select(currentRules().players().min());
  }

  private Rule.Set currentRules() {
    return ruleSetRepository.get(currentRuleTypeRepository.get());
  }

  public void create(int numberOfPlayers) {
    currentRules().players().select(numberOfPlayers).forEach(player ->
        playerRepository.put(player.id(), player)
    );
  }
}
