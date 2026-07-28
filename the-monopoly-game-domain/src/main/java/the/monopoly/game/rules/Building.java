package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.strategies.Strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Builds houses and hotels for players who own a full colour group and want to
 * keep improving it.
 */
public class Building {
  private final Deeds deeds;
  private final Rule.Set rules;
  private final Strategy.OfPlayers strategies;
  private final Events events;

  public Building(Deeds deeds, Rule.Set rules, Strategy.OfPlayers strategies, Events events) {
    this.deeds = deeds;
    this.rules = rules;
    this.strategies = strategies;
    this.events = events;
  }

  public void develop(Player player) {
    while (true) {
      Optional<Build> build = nextBuildFor(player)
          .filter(it -> strategies.forPlayer(player).builds(it.offer(player)));
      if (build.isEmpty()) return;
      build.get().apply(deeds, player, events);
    }
  }

  private Optional<Build> nextBuildFor(Player player) {
    return monopoliesOwnedBy(player).stream()
        .map(this::nextBuildFor)
        .flatMap(Optional::stream)
        .findFirst();
  }

  private Optional<Build> nextBuildFor(List<ColourStreet> group) {
    int lowestLevel = group.stream().mapToInt(this::levelOf).min().orElse(Integer.MAX_VALUE);
    return group.stream()
        .filter(it -> levelOf(it) == lowestLevel)
        .map(this::buildFor)
        .findFirst();
  }

  private int levelOf(ColourStreet street) {
    return deeds.hasHotelOn(street) ? street.hotelConstructionRequiresNumberOfHouses() + 1 : deeds.housesBuiltOn(street);
  }

  private Build buildFor(ColourStreet street) {
    return deeds.housesBuiltOn(street) == street.hotelConstructionRequiresNumberOfHouses()
        ? new Build(street, street.rentForOneHotel(), true)
        : new Build(street, street.houseConstructionCost(), false);
  }

  private List<List<ColourStreet>> monopoliesOwnedBy(Player player) {
    return rules.streets()
        .filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast)
        .collect(Collectors.groupingBy(
            ColourStreet::colourGroup,
            Collectors.collectingAndThen(Collectors.toList(), List::copyOf)
        ))
        .values().stream()
        .filter(group -> group.stream().allMatch(it -> deeds.ownerOf(it.type()).filter(player.id()::equals).isPresent()))
        .sorted(Comparator.comparing(group -> rules.gameboard().positionOf(group.getFirst().type())))
        .toList();
  }

  private record Build(ColourStreet street, Money price, boolean hotel) {
    private Strategy.BuildOffer offer(Player player) {
      return new Strategy.BuildOffer(street, price, player.account().balance().amount(), hotel);
    }

    private void apply(Deeds deeds, Player player, Events events) {
      if (hotel) deeds.buildHotel(street, player);
      else {
        deeds.buildHouse(street, player);
        events.builtHouse(player, street, price);
      }
    }
  }

  public interface Events {
    default void builtHouse(Player player, ColourStreet street, Money price) {
    }
  }
}
