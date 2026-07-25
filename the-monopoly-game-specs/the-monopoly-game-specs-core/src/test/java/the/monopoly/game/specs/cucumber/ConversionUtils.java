package the.monopoly.game.specs.cucumber;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.Street;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static the.monopoly.game.components.streets.Street.Type.*;

public class ConversionUtils {
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public static String value(Map<String, String> record, String... keys) {
    return Stream.of(keys).map(record::get).filter(Objects::nonNull).findFirst().get();
  }

  public static Money money(String value) {
    return new Money(parseInt(value.replaceAll("\\$", "")));
  }

  public static Street.Colour colour(String colour) {
    return colour.equals("-") ? null : Street.Colour.valueOf(switch (colour) {
      case "bruin" -> "brown";
      default -> colour;
    });
  }

  public static Street.Type streetType(String type) {
    return switch (type) {
      case "Start" -> start;
      case "Rue Grande Dinant" -> RueGrandeDinant;
      case "Diestsestraat Leuven" -> DiestsestraatLeuven;
      default -> throw new IllegalArgumentException("Unknown street type! [" + type + "]");
    };
  }
}
