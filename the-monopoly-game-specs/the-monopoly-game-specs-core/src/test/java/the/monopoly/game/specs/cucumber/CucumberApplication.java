package the.monopoly.game.specs.cucumber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "the.monopoly.game")
public class CucumberApplication {
  public static void main(String... args) {
    SpringApplication.run(CucumberApplication.class);
  }
}
