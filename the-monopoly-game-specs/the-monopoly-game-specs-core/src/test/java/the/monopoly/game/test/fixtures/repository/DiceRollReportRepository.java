package the.monopoly.game.test.fixtures.repository;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Repository;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.test.fixtures.model.DiceRollReport;

@Repository
@ScenarioScope
public class DiceRollReportRepository extends AbstractRepository<Dice.Type, DiceRollReport> {
}
