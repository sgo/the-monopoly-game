package the.monopoly.game.test.fixtures.repository;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Repository;
import the.monopoly.game.components.players.Player;

@Repository
@ScenarioScope
public class CurrentPlayerRepository extends AbstractSingleResultRepository<Player.ID> {
}
