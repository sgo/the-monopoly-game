package the.monopoly.game.specs.cucumber;

import the.monopoly.game.components.finance.Money;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

public class ConversionUtils {
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public static String value(Map<String, String> record, String... keys) {
    return Stream.of(keys).map(record::get).filter(Objects::nonNull).findFirst().get();
  }

  public static Money money(String value) {
    return new Money(parseInt(value.replaceAll("\\$", "")));
  }
}
