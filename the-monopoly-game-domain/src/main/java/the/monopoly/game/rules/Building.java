package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.strategies.Strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
    Optional<Build> refused = refusedBuildFor(player);
    if (refused.isPresent()) {
      Build build = refused.get();
      events.refusedBuilding(player, build.street, build.price);
      return;
    }
    for (;;) {
      Optional<Build> build = nextBuildFor(player);
      if (build.isEmpty()) return;
      build.get().apply(deeds, player, events);
    }
  }

  private Optional<Build> nextBuildFor(Player player) {
    return buildableMonopoliesOwnedBy(player).stream()
        .flatMap(this::candidateBuildsFor)
        .filter(it -> strategies.forPlayer(player).builds(it.offer(player)))
        .findFirst();
  }

  private Optional<Build> refusedBuildFor(Player player) {
    return mortgagedMonopoliesOwnedBy(player).stream()
        .flatMap(this::candidateBuildsFor)
        .filter(it -> strategies.forPlayer(player).builds(it.offer(player)))
        .findFirst();
  }

  private Stream<Build> candidateBuildsFor(List<ColourStreet> group) {
    int lowestLevel = group.stream().mapToInt(this::levelOf).min().orElse(Integer.MAX_VALUE);
    return group.stream()
        .filter(it -> levelOf(it) == lowestLevel)
        .map(this::buildFor);
  }

  private int levelOf(ColourStreet street) {
    return deeds.hasHotelOn(street) ? street.hotelConstructionRequiresNumberOfHouses() + 1 : deeds.housesBuiltOn(street);
  }

  private Build buildFor(ColourStreet street) {
    return deeds.housesBuiltOn(street) == street.hotelConstructionRequiresNumberOfHouses()
        ? new Build(street, street.rentForOneHotel(), true)
        : new Build(street, street.houseConstructionCost(), false);
  }

  private List<List<ColourStreet>> buildableMonopoliesOwnedBy(Player player) {
    return monopoliesOwnedBy(player).stream()
        .filter(group -> group.stream().noneMatch(deeds::isMortgaged))
        .toList();
  }

  private List<List<ColourStreet>> mortgagedMonopoliesOwnedBy(Player player) {
    return monopoliesOwnedBy(player).stream()
        .filter(group -> group.stream().anyMatch(deeds::isMortgaged))
        .toList();
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

    default void refusedBuilding(Player player, ColourStreet street, Money price) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=2c77c7742aa5025f9971bcf621d21615f4c057dad342a139e50514c2ba687005
scope.0.id=Y2xhc3M6QnVpbGRpbmcjQnVpbGRpbmc6MTc
scope.0.kind=class
scope.0.startLine=17
scope.0.endLine=97
scope.0.semanticHash=118e47326c7f53b918a5b5e45eaf4d569317e8ad78652564265c0b3d0e1a8072
scope.1.id=Y2xhc3M6QnVpbGRpbmcuQnVpbGQjQnVpbGQ6Nzk
scope.1.kind=class
scope.1.startLine=79
scope.1.endLine=91
scope.1.semanticHash=1f2dc4280316e1b9a0b9af88b1267d2044f5fd55fc0c1521750fad5fede1b323
scope.2.id=Y2xhc3M6QnVpbGRpbmcuRXZlbnRzI0V2ZW50czo5Mw
scope.2.kind=class
scope.2.startLine=93
scope.2.endLine=96
scope.2.semanticHash=06860c2248b7500cdbaeb73e6594ff4f8337c47fba956cd7daa644fc002e6905
scope.3.id=ZmllbGQ6QnVpbGRpbmcjZGVlZHM6MTg
scope.3.kind=field
scope.3.startLine=18
scope.3.endLine=18
scope.3.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.4.id=ZmllbGQ6QnVpbGRpbmcjZXZlbnRzOjIx
scope.4.kind=field
scope.4.startLine=21
scope.4.endLine=21
scope.4.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.5.id=ZmllbGQ6QnVpbGRpbmcjcnVsZXM6MTk
scope.5.kind=field
scope.5.startLine=19
scope.5.endLine=19
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6QnVpbGRpbmcjc3RyYXRlZ2llczoyMA
scope.6.kind=field
scope.6.startLine=20
scope.6.endLine=20
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjaG90ZWw6Nzk
scope.7.kind=field
scope.7.startLine=79
scope.7.endLine=79
scope.7.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.8.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjcHJpY2U6Nzk
scope.8.kind=field
scope.8.startLine=79
scope.8.endLine=79
scope.8.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.9.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjc3RyZWV0Ojc5
scope.9.kind=field
scope.9.startLine=79
scope.9.endLine=79
scope.9.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.10.id=bWV0aG9kOkJ1aWxkaW5nI2J1aWxkRm9yKDEpOjU4
scope.10.kind=method
scope.10.startLine=58
scope.10.endLine=62
scope.10.semanticHash=0e37042dea42bf78c37b0485aee197582dad1be1fcc430c6dcd9ccefbb6f40f7
scope.11.id=bWV0aG9kOkJ1aWxkaW5nI2N0b3IoNCk6MjM
scope.11.kind=method
scope.11.startLine=23
scope.11.endLine=28
scope.11.semanticHash=043b912411555ef3ff3ef81fa25c1f784fb5bb8466558769c5ce44ae2e1fae75
scope.12.id=bWV0aG9kOkJ1aWxkaW5nI2RldmVsb3AoMSk6MzA
scope.12.kind=method
scope.12.startLine=30
scope.12.endLine=37
scope.12.semanticHash=7af59e32a053e67cb7ac2b04b92d88898ff516c05c93b3bf5db57a83984d7c34
scope.13.id=bWV0aG9kOkJ1aWxkaW5nI2xldmVsT2YoMSk6NTQ
scope.13.kind=method
scope.13.startLine=54
scope.13.endLine=56
scope.13.semanticHash=8b58cf36ef65433f644c5dd0477167130d40dcffefeffbbf712f9d1cd59f43a4
scope.14.id=bWV0aG9kOkJ1aWxkaW5nI21vbm9wb2xpZXNPd25lZEJ5KDEpOjY0
scope.14.kind=method
scope.14.startLine=64
scope.14.endLine=77
scope.14.semanticHash=4084534ad226d43010f078f00653c66b2c5a7a74547cd11eeddbb6b2e2d4f998
scope.15.id=bWV0aG9kOkJ1aWxkaW5nI25leHRCdWlsZEZvcigxKTozOQ
scope.15.kind=method
scope.15.startLine=39
scope.15.endLine=44
scope.15.semanticHash=d3501623b9ebba688efbb0c064b14191a1147f3a74edca7eb393641a1db239a0
scope.16.id=bWV0aG9kOkJ1aWxkaW5nI25leHRCdWlsZEZvcigxKTo0Ng
scope.16.kind=method
scope.16.startLine=46
scope.16.endLine=52
scope.16.semanticHash=bc9b1056a656c8e88bb491a213bc89ecc7ef98c3fd0713c3e644f992836f2b92
scope.17.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI2FwcGx5KDMpOjg0
scope.17.kind=method
scope.17.startLine=84
scope.17.endLine=90
scope.17.semanticHash=3cb49ba2fd24a48a1b9215b62c1d08ee2e14e735449dd9a3f0b57483d4250515
scope.18.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI2N0b3IoMyk6Nzk
scope.18.kind=method
scope.18.startLine=1
scope.18.endLine=97
scope.18.semanticHash=5d73fe7d29d603f7a02e250778ad39ea0427440777cbd5123c0ebf66e5121dc8
scope.19.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI29mZmVyKDEpOjgw
scope.19.kind=method
scope.19.startLine=80
scope.19.endLine=82
scope.19.semanticHash=e202f5c83012ca9614c62a26f2067e8ea38583317740b5e050832cfb2e9f0a4a
scope.20.id=bWV0aG9kOkJ1aWxkaW5nLkV2ZW50cyNidWlsdEhvdXNlKDMpOjk0
scope.20.kind=method
scope.20.startLine=94
scope.20.endLine=95
scope.20.semanticHash=ff18ab0cd6a6263c832319d58352d7babe54b659fb8fb7785a644b2cdec19294
*/
