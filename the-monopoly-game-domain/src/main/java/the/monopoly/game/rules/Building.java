package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.strategies.Strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    refused.ifPresent(build -> events.refusedBuilding(player, build.street, build.price));
    for (;;) {
      Optional<Build> build = nextBuildFor(player);
      if (build.isEmpty()) return;
      build.get().apply(deeds, player, events);
    }
  }

  private Optional<Build> nextBuildFor(Player player) {
    return firstOfferedBuild(buildableMonopoliesOwnedBy(player), player);
  }

  private Optional<Build> refusedBuildFor(Player player) {
    return firstOfferedBuild(mortgagedMonopoliesOwnedBy(player), player);
  }

  private Optional<Build> firstOfferedBuild(List<List<ColourStreet>> monopolies, Player player) {
    return monopolies.stream()
        .flatMap(this::candidateBuildsFor)
        .filter(it -> strategies.forPlayer(player).builds(it.offer(player)))
        .findFirst();
  }

  private Stream<Build> candidateBuildsFor(List<ColourStreet> group) {
    int lowestLevel = group.stream().mapToInt(this::levelOf).min().orElse(Integer.MAX_VALUE);
    return group.stream()
        .filter(it -> !deeds.hasHotelOn(it))
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
    return monopoliesOwnedBy(player, group -> group.stream().noneMatch(deeds::isMortgaged));
  }

  private List<List<ColourStreet>> mortgagedMonopoliesOwnedBy(Player player) {
    return monopoliesOwnedBy(player, group -> group.stream().anyMatch(deeds::isMortgaged));
  }

  private List<List<ColourStreet>> monopoliesOwnedBy(Player player, Predicate<List<ColourStreet>> condition) {
    return monopoliesOwnedBy(player).stream().filter(condition).toList();
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
moduleHash=f73089d5c48a486438fae7b9675ed16ba4e7e3166de635292fe453b88131c215
scope.0.id=Y2xhc3M6QnVpbGRpbmcjQnVpbGRpbmc6MTk
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=121
scope.0.semanticHash=880eacacfa54030a6dad125e7ee78a39f23c640036cca0c6c24517a1a25e6c05
scope.1.id=Y2xhc3M6QnVpbGRpbmcuQnVpbGQjQnVpbGQ6MTAw
scope.1.kind=class
scope.1.startLine=100
scope.1.endLine=112
scope.1.semanticHash=1f2dc4280316e1b9a0b9af88b1267d2044f5fd55fc0c1521750fad5fede1b323
scope.2.id=Y2xhc3M6QnVpbGRpbmcuRXZlbnRzI0V2ZW50czoxMTQ
scope.2.kind=class
scope.2.startLine=114
scope.2.endLine=120
scope.2.semanticHash=e09f64dab3688b8a4e180c9de19e581de3816126d98bb144c7de44c7ab283e8e
scope.3.id=ZmllbGQ6QnVpbGRpbmcjZGVlZHM6MjA
scope.3.kind=field
scope.3.startLine=20
scope.3.endLine=20
scope.3.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.4.id=ZmllbGQ6QnVpbGRpbmcjZXZlbnRzOjIz
scope.4.kind=field
scope.4.startLine=23
scope.4.endLine=23
scope.4.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.5.id=ZmllbGQ6QnVpbGRpbmcjcnVsZXM6MjE
scope.5.kind=field
scope.5.startLine=21
scope.5.endLine=21
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6QnVpbGRpbmcjc3RyYXRlZ2llczoyMg
scope.6.kind=field
scope.6.startLine=22
scope.6.endLine=22
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjaG90ZWw6MTAw
scope.7.kind=field
scope.7.startLine=100
scope.7.endLine=100
scope.7.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.8.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjcHJpY2U6MTAw
scope.8.kind=field
scope.8.startLine=100
scope.8.endLine=100
scope.8.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.9.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjc3RyZWV0OjEwMA
scope.9.kind=field
scope.9.startLine=100
scope.9.endLine=100
scope.9.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.10.id=bWV0aG9kOkJ1aWxkaW5nI2J1aWxkRm9yKDEpOjY4
scope.10.kind=method
scope.10.startLine=68
scope.10.endLine=72
scope.10.semanticHash=0e37042dea42bf78c37b0485aee197582dad1be1fcc430c6dcd9ccefbb6f40f7
scope.11.id=bWV0aG9kOkJ1aWxkaW5nI2J1aWxkYWJsZU1vbm9wb2xpZXNPd25lZEJ5KDEpOjc0
scope.11.kind=method
scope.11.startLine=74
scope.11.endLine=76
scope.11.semanticHash=03c27062c2051b74ff9d47d4a1be56e5900b36cc0e021ddaf3e3e6aed9257324
scope.12.id=bWV0aG9kOkJ1aWxkaW5nI2NhbmRpZGF0ZUJ1aWxkc0ZvcigxKTo1Nw
scope.12.kind=method
scope.12.startLine=57
scope.12.endLine=62
scope.12.semanticHash=aa0e4fc48c80dab8bda83e2617c93e151fc9512c37cded8cb150a7ce739a381d
scope.13.id=bWV0aG9kOkJ1aWxkaW5nI2N0b3IoNCk6MjU
scope.13.kind=method
scope.13.startLine=25
scope.13.endLine=30
scope.13.semanticHash=043b912411555ef3ff3ef81fa25c1f784fb5bb8466558769c5ce44ae2e1fae75
scope.14.id=bWV0aG9kOkJ1aWxkaW5nI2RldmVsb3AoMSk6MzI
scope.14.kind=method
scope.14.startLine=32
scope.14.endLine=40
scope.14.semanticHash=4e8338856c08fb3082578dd5b8d53886852f4681f3e19c3e3a79089ab0bcfefa
scope.15.id=bWV0aG9kOkJ1aWxkaW5nI2ZpcnN0T2ZmZXJlZEJ1aWxkKDIpOjUw
scope.15.kind=method
scope.15.startLine=50
scope.15.endLine=55
scope.15.semanticHash=392632adb0955488b91677aed10b5ebc732482255a83b7bf497dbcbe0f11cc13
scope.16.id=bWV0aG9kOkJ1aWxkaW5nI2xldmVsT2YoMSk6NjQ
scope.16.kind=method
scope.16.startLine=64
scope.16.endLine=66
scope.16.semanticHash=8b58cf36ef65433f644c5dd0477167130d40dcffefeffbbf712f9d1cd59f43a4
scope.17.id=bWV0aG9kOkJ1aWxkaW5nI21vbm9wb2xpZXNPd25lZEJ5KDEpOjg2
scope.17.kind=method
scope.17.startLine=86
scope.17.endLine=98
scope.17.semanticHash=26bc97332a6f44440fab0864af6736f862c9f07d9607acc8871fc8bc11d9d289
scope.18.id=bWV0aG9kOkJ1aWxkaW5nI21vbm9wb2xpZXNPd25lZEJ5KDIpOjgy
scope.18.kind=method
scope.18.startLine=82
scope.18.endLine=84
scope.18.semanticHash=f9b4ee100bde039e6e61730b5f9c3d32756b2a28e268629ccf5b34ce4048a706
scope.19.id=bWV0aG9kOkJ1aWxkaW5nI21vcnRnYWdlZE1vbm9wb2xpZXNPd25lZEJ5KDEpOjc4
scope.19.kind=method
scope.19.startLine=78
scope.19.endLine=80
scope.19.semanticHash=aee80563c04ecfb057203fc889c41cbd2854398b103aa2398330e43469fc0e41
scope.20.id=bWV0aG9kOkJ1aWxkaW5nI25leHRCdWlsZEZvcigxKTo0Mg
scope.20.kind=method
scope.20.startLine=42
scope.20.endLine=44
scope.20.semanticHash=c7ebb1ccf693ed5ce346993cac54e7e2ce3a535411e0f0ed4ac8311c0313387e
scope.21.id=bWV0aG9kOkJ1aWxkaW5nI3JlZnVzZWRCdWlsZEZvcigxKTo0Ng
scope.21.kind=method
scope.21.startLine=46
scope.21.endLine=48
scope.21.semanticHash=477cea6a260d031e950e812cbc3cce0453ad669aabc0dcbb5112de93bd5e431c
scope.22.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI2FwcGx5KDMpOjEwNQ
scope.22.kind=method
scope.22.startLine=105
scope.22.endLine=111
scope.22.semanticHash=3cb49ba2fd24a48a1b9215b62c1d08ee2e14e735449dd9a3f0b57483d4250515
scope.23.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI2N0b3IoMyk6MTAw
scope.23.kind=method
scope.23.startLine=1
scope.23.endLine=121
scope.23.semanticHash=40cf3757054a39bfddad57b265916d9565c31ac25eb994822d11a46ea468021f
scope.24.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI29mZmVyKDEpOjEwMQ
scope.24.kind=method
scope.24.startLine=101
scope.24.endLine=103
scope.24.semanticHash=e202f5c83012ca9614c62a26f2067e8ea38583317740b5e050832cfb2e9f0a4a
scope.25.id=bWV0aG9kOkJ1aWxkaW5nLkV2ZW50cyNidWlsdEhvdXNlKDMpOjExNQ
scope.25.kind=method
scope.25.startLine=115
scope.25.endLine=116
scope.25.semanticHash=ff18ab0cd6a6263c832319d58352d7babe54b659fb8fb7785a644b2cdec19294
scope.26.id=bWV0aG9kOkJ1aWxkaW5nLkV2ZW50cyNyZWZ1c2VkQnVpbGRpbmcoMyk6MTE4
scope.26.kind=method
scope.26.startLine=118
scope.26.endLine=119
scope.26.semanticHash=d6b2257e1d25e368d5f9c38ff579ef4aa7c9438d8054422a6d8adb83fe9ffa45
*/
