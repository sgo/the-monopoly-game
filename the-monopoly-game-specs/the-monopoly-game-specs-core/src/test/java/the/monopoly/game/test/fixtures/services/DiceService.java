package the.monopoly.game.test.fixtures.services;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.test.fixtures.model.DiceRollReport;
import the.monopoly.game.test.fixtures.repository.CurrentDiceTypeRepository;
import the.monopoly.game.test.fixtures.repository.DiceRepository;
import the.monopoly.game.test.fixtures.repository.DiceRollReportRepository;

import java.util.stream.IntStream;

@Service
public class DiceService {
  private final DiceRepository diceRepository;
  private final CurrentDiceTypeRepository currentDiceTypeRepository;
  private final DiceRollReportRepository diceRollReportRepository;

  public DiceService(
      DiceRepository diceRepository,
      CurrentDiceTypeRepository currentDiceTypeRepository,
      DiceRollReportRepository diceRollReportRepository
  ) {
    this.diceRepository = diceRepository;
    this.currentDiceTypeRepository = currentDiceTypeRepository;
    this.diceRollReportRepository = diceRollReportRepository;
  }

  public void createDice(Dice.Type type) {
    currentDiceTypeRepository.set(type);
    diceRepository.put(type, type.create());
  }

  public void rollDiceEqualTo(int numberOfTimes) {
    Dice.Type type = currentDiceTypeRepository.get();
    Dice dice = diceRepository.get(type);
    DiceRollReport report = IntStream.range(0, numberOfTimes)
        .mapToObj(ignored -> dice.roll())
        .collect(DiceRollReport.collectorFor(dice));
    diceRollReportRepository.put(type, report);
  }
}
