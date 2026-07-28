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
moduleHash=44cd68557bbfa4ed33ba8966ee4c25dbb06e794e89ab351a7f610e97636ee021
scope.0.id=Y2xhc3M6QnVpbGRpbmcjQnVpbGRpbmc6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=124
scope.0.semanticHash=2574f207a2e632bebb885f1dbd6e8153a89c24e91b116c70979c0a50bdc4705a
scope.1.id=Y2xhc3M6QnVpbGRpbmcuQnVpbGQjQnVpbGQ6MTAz
scope.1.kind=class
scope.1.startLine=103
scope.1.endLine=115
scope.1.semanticHash=1f2dc4280316e1b9a0b9af88b1267d2044f5fd55fc0c1521750fad5fede1b323
scope.2.id=Y2xhc3M6QnVpbGRpbmcuRXZlbnRzI0V2ZW50czoxMTc
scope.2.kind=class
scope.2.startLine=117
scope.2.endLine=123
scope.2.semanticHash=e09f64dab3688b8a4e180c9de19e581de3816126d98bb144c7de44c7ab283e8e
scope.3.id=ZmllbGQ6QnVpbGRpbmcjZGVlZHM6MTk
scope.3.kind=field
scope.3.startLine=19
scope.3.endLine=19
scope.3.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.4.id=ZmllbGQ6QnVpbGRpbmcjZXZlbnRzOjIy
scope.4.kind=field
scope.4.startLine=22
scope.4.endLine=22
scope.4.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.5.id=ZmllbGQ6QnVpbGRpbmcjcnVsZXM6MjA
scope.5.kind=field
scope.5.startLine=20
scope.5.endLine=20
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6QnVpbGRpbmcjc3RyYXRlZ2llczoyMQ
scope.6.kind=field
scope.6.startLine=21
scope.6.endLine=21
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjaG90ZWw6MTAz
scope.7.kind=field
scope.7.startLine=103
scope.7.endLine=103
scope.7.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.8.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjcHJpY2U6MTAz
scope.8.kind=field
scope.8.startLine=103
scope.8.endLine=103
scope.8.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.9.id=ZmllbGQ6QnVpbGRpbmcuQnVpbGQjc3RyZWV0OjEwMw
scope.9.kind=field
scope.9.startLine=103
scope.9.endLine=103
scope.9.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.10.id=bWV0aG9kOkJ1aWxkaW5nI2J1aWxkRm9yKDEpOjcx
scope.10.kind=method
scope.10.startLine=71
scope.10.endLine=75
scope.10.semanticHash=0e37042dea42bf78c37b0485aee197582dad1be1fcc430c6dcd9ccefbb6f40f7
scope.11.id=bWV0aG9kOkJ1aWxkaW5nI2J1aWxkYWJsZU1vbm9wb2xpZXNPd25lZEJ5KDEpOjc3
scope.11.kind=method
scope.11.startLine=77
scope.11.endLine=81
scope.11.semanticHash=36c8aa7a5130b8f572bdd9bf1d075cb091c03cf25a7356fff785449cb31c1ff2
scope.12.id=bWV0aG9kOkJ1aWxkaW5nI2NhbmRpZGF0ZUJ1aWxkc0ZvcigxKTo2MA
scope.12.kind=method
scope.12.startLine=60
scope.12.endLine=65
scope.12.semanticHash=aa0e4fc48c80dab8bda83e2617c93e151fc9512c37cded8cb150a7ce739a381d
scope.13.id=bWV0aG9kOkJ1aWxkaW5nI2N0b3IoNCk6MjQ
scope.13.kind=method
scope.13.startLine=24
scope.13.endLine=29
scope.13.semanticHash=043b912411555ef3ff3ef81fa25c1f784fb5bb8466558769c5ce44ae2e1fae75
scope.14.id=bWV0aG9kOkJ1aWxkaW5nI2RldmVsb3AoMSk6MzE
scope.14.kind=method
scope.14.startLine=31
scope.14.endLine=43
scope.14.semanticHash=12354c853c5a0b2aa8b2185cc96fb80efbe9126743bde67a11915268b270a52a
scope.15.id=bWV0aG9kOkJ1aWxkaW5nI2ZpcnN0T2ZmZXJlZEJ1aWxkKDIpOjUz
scope.15.kind=method
scope.15.startLine=53
scope.15.endLine=58
scope.15.semanticHash=392632adb0955488b91677aed10b5ebc732482255a83b7bf497dbcbe0f11cc13
scope.16.id=bWV0aG9kOkJ1aWxkaW5nI2xldmVsT2YoMSk6Njc
scope.16.kind=method
scope.16.startLine=67
scope.16.endLine=69
scope.16.semanticHash=8b58cf36ef65433f644c5dd0477167130d40dcffefeffbbf712f9d1cd59f43a4
scope.17.id=bWV0aG9kOkJ1aWxkaW5nI21vbm9wb2xpZXNPd25lZEJ5KDEpOjg5
scope.17.kind=method
scope.17.startLine=89
scope.17.endLine=101
scope.17.semanticHash=26bc97332a6f44440fab0864af6736f862c9f07d9607acc8871fc8bc11d9d289
scope.18.id=bWV0aG9kOkJ1aWxkaW5nI21vcnRnYWdlZE1vbm9wb2xpZXNPd25lZEJ5KDEpOjgz
scope.18.kind=method
scope.18.startLine=83
scope.18.endLine=87
scope.18.semanticHash=267489113f4da5a72196f33d46abfde4780781c33ec42f08f5237bd4229feb8b
scope.19.id=bWV0aG9kOkJ1aWxkaW5nI25leHRCdWlsZEZvcigxKTo0NQ
scope.19.kind=method
scope.19.startLine=45
scope.19.endLine=47
scope.19.semanticHash=c7ebb1ccf693ed5ce346993cac54e7e2ce3a535411e0f0ed4ac8311c0313387e
scope.20.id=bWV0aG9kOkJ1aWxkaW5nI3JlZnVzZWRCdWlsZEZvcigxKTo0OQ
scope.20.kind=method
scope.20.startLine=49
scope.20.endLine=51
scope.20.semanticHash=477cea6a260d031e950e812cbc3cce0453ad669aabc0dcbb5112de93bd5e431c
scope.21.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI2FwcGx5KDMpOjEwOA
scope.21.kind=method
scope.21.startLine=108
scope.21.endLine=114
scope.21.semanticHash=3cb49ba2fd24a48a1b9215b62c1d08ee2e14e735449dd9a3f0b57483d4250515
scope.22.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI2N0b3IoMyk6MTAz
scope.22.kind=method
scope.22.startLine=1
scope.22.endLine=124
scope.22.semanticHash=73bb6a0d4b65ecf4478bd3319c4c9451ed2615c4ec8efa2aca22a2fceeb0126b
scope.23.id=bWV0aG9kOkJ1aWxkaW5nLkJ1aWxkI29mZmVyKDEpOjEwNA
scope.23.kind=method
scope.23.startLine=104
scope.23.endLine=106
scope.23.semanticHash=e202f5c83012ca9614c62a26f2067e8ea38583317740b5e050832cfb2e9f0a4a
scope.24.id=bWV0aG9kOkJ1aWxkaW5nLkV2ZW50cyNidWlsdEhvdXNlKDMpOjExOA
scope.24.kind=method
scope.24.startLine=118
scope.24.endLine=119
scope.24.semanticHash=ff18ab0cd6a6263c832319d58352d7babe54b659fb8fb7785a644b2cdec19294
scope.25.id=bWV0aG9kOkJ1aWxkaW5nLkV2ZW50cyNyZWZ1c2VkQnVpbGRpbmcoMyk6MTIx
scope.25.kind=method
scope.25.startLine=121
scope.25.endLine=122
scope.25.semanticHash=d6b2257e1d25e368d5f9c38ff579ef4aa7c9438d8054422a6d8adb83fe9ffa45
*/
