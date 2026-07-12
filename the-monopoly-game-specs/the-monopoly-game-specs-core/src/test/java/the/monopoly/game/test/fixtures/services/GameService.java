package the.monopoly.game.test.fixtures.services;

import org.springframework.stereotype.Service;
import the.monopoly.game.Game;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.test.fixtures.model.GameResultReport;
import the.monopoly.game.test.fixtures.repository.CurrentRuleTypeRepository;
import the.monopoly.game.test.fixtures.repository.GameResultReportRepository;
import the.monopoly.game.test.fixtures.repository.PlayerRepository;
import the.monopoly.game.test.fixtures.repository.RuleSetRepository;

@Service
public class GameService {
  private final RuleSetRepository ruleSetRepository;
  private final CurrentRuleTypeRepository currentRuleTypeRepository;
  private final GameResultReportRepository gameResultReportRepository;
  private final PlayerRepository playerRepository;

  public GameService(
      RuleSetRepository ruleSetRepository,
      CurrentRuleTypeRepository currentRuleTypeRepository,
      GameResultReportRepository gameResultReportRepository,
      PlayerRepository playerRepository
  ) {
    this.ruleSetRepository = ruleSetRepository;
    this.currentRuleTypeRepository = currentRuleTypeRepository;
    this.gameResultReportRepository = gameResultReportRepository;
    this.playerRepository = playerRepository;
  }

  public void play(int numberOfTimes) {
    Rule.Set rules = ruleSetRepository.get(currentRuleTypeRepository.get());
    Game game = new Game(
        rules,
        playerRepository.all().toList()
    );
    Game.Result result = game.play();
    gameResultReportRepository.put("game-1", new GameResultReport(null));
  }
}
