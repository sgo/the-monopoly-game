package the.monopoly.game.test.fixtures.repository;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Repository;
import the.monopoly.game.rules.Rule;

@Repository
@ScenarioScope
public class RuleSetRepository extends AbstractRepository<Rule.Type, Rule.Set> {
}
