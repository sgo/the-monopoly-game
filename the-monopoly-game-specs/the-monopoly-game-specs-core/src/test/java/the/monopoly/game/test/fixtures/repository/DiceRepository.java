package the.monopoly.game.test.fixtures.repository;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Repository;
import the.monopoly.game.components.dice.Dice;

@Repository
@ScenarioScope
public class DiceRepository extends AbstractRepository<Dice.Type, Dice> {
}
