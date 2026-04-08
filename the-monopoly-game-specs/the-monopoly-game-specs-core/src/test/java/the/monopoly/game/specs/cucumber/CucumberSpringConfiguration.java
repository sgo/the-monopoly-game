package the.monopoly.game.specs.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = CucumberApplication.class)
public class CucumberSpringConfiguration {
}
