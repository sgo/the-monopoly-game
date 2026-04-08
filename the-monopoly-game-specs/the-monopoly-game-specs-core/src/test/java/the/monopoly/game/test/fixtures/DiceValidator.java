package the.monopoly.game.test.fixtures;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.test.fixtures.model.DiceRollReport;
import the.monopoly.game.test.fixtures.model.DiceRollReport.FaceResult;
import the.monopoly.game.test.fixtures.repository.CurrentDiceTypeRepository;
import the.monopoly.game.test.fixtures.repository.DiceRollReportRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class DiceValidator {
  private final DiceRollReportRepository diceRollReportRepository;
  private final CurrentDiceTypeRepository currentDiceTypeRepository;

  public DiceValidator(
      DiceRollReportRepository diceRollReportRepository,
      CurrentDiceTypeRepository currentDiceTypeRepository
  ) {
    this.diceRollReportRepository = diceRollReportRepository;
    this.currentDiceTypeRepository = currentDiceTypeRepository;
  }

  public void assertDiceRollReportMatches(List<FaceResult.Expectation> expectations) {
    Dice.Type type = currentDiceTypeRepository.get();
    DiceRollReport report = diceRollReportRepository.get(type);
    assertThat(report.results()).hasSize(expectations.size());
    expectations.forEach(expectation -> {
      assertThat(report.results())
          .extracting(FaceResult::symbol)
          .contains(expectation.result().symbol());

      assertThat(Stream.of(report.results())
          .filter(it -> it.symbol().equals(expectation.result().symbol()))
          .findFirst()
          .get()
          .timesSeen())
          .isBetween(
              expectation.lowerbound(),
              expectation.upperbound()
          );
    });
  }
}
