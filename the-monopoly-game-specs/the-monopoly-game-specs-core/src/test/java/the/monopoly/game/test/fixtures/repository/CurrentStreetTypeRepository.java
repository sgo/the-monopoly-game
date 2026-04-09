package the.monopoly.game.test.fixtures.repository;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Repository;
import the.monopoly.game.components.streets.Street;

@Repository
@ScenarioScope
public class CurrentStreetTypeRepository extends AbstractSingleResultRepository<Street.Type> {
}
