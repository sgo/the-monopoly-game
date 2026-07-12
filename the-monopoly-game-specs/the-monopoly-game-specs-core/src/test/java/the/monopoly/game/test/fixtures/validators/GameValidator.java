package the.monopoly.game.test.fixtures.validators;

import org.springframework.stereotype.Service;
import the.monopoly.game.test.fixtures.model.GameResultReport;
import the.monopoly.game.test.fixtures.repository.GameResultReportRepository;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class GameValidator {
  private final GameResultReportRepository repository;

  public GameValidator(GameResultReportRepository repository) {
    this.repository = repository;
  }

  public void assertGameEndedInMonopolyEveryTime() {
    assertFalse(repository.isEmpty(), "Play the game before validating!");
    assertAll(repository.all()
        .map(report -> () -> assertGameEndedInMonopoly(report)));
  }

  private void assertGameEndedInMonopoly(GameResultReport report) {
    assertNotNull(report.monopolist(), "The game did not end with a monopoly!");
  }
}
