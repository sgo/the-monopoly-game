package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.StartSpace;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.Utility;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static the.monopoly.game.components.streets.Street.Type.AlgemeenFonds;
import static the.monopoly.game.components.streets.Street.Type.CentraalStation;
import static the.monopoly.game.components.streets.Street.Type.DiestsestraatLeuven;
import static the.monopoly.game.components.streets.Street.Type.Elektriciteitscentrale;
import static the.monopoly.game.components.streets.Street.Type.GrandPlaceMons;
import static the.monopoly.game.components.streets.Street.Type.NieuwstraatBrussel;
import static the.monopoly.game.components.streets.Street.Type.NoordStation;
import static the.monopoly.game.components.streets.Street.Type.OpBezoek;
import static the.monopoly.game.components.streets.Street.Type.RueDeDiekirchArlon;
import static the.monopoly.game.components.streets.Street.Type.start;

/** Resolves the card drawn when a pawn stops on Chance or Community Chest. */
public final class Cards implements Landings {
  private final Deeds deeds;
  private final Rule.Set rules;
  private final List<Player> players;
  private final Strategy.OfPlayers strategies;
  private final Decks decks;
  private final Events events;
  private final Cup cup;
  private final Jail jail;
  private final Map<String, Consumer<Player>> chanceEffects;
  private final Map<String, Consumer<Player>> communityChestEffects;

  public Cards(
      Deeds deeds, Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies,
      Decks decks, Events events, Cup cup
  ) {
    this(deeds, rules, players, strategies, decks, events, cup, new Jail(rules));
  }

  public Cards(
      Deeds deeds, Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies,
      Decks decks, Events events, Cup cup, Jail jail
  ) {
    this.deeds = deeds;
    this.rules = rules;
    this.players = players;
    this.strategies = strategies;
    this.decks = decks;
    this.events = events;
    this.cup = cup;
    this.jail = jail;
    this.chanceEffects = Map.ofEntries(
      Map.entry(
          "Ga door naar Nieuwstraat (Brussel) / Rue Neuve (Bruxelles).",
          (Consumer<Player>) player -> moveTo(player, NieuwstraatBrussel, false)
      ),
      Map.entry(
          "Ga door naar START (Ontvang M200).",
          (Consumer<Player>) player -> moveTo(player, start, true)
      ),
      Map.entry(
          "Ga door naar Grand Place (Mons). Als je langs START komt, ontvang je M200.",
          (Consumer<Player>) player -> moveTo(player, GrandPlaceMons, true)
      ),
      Map.entry(
          "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.",
          (Consumer<Player>) player -> moveTo(player, RueDeDiekirchArlon, true)
      ),
      Map.entry(
          "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
          (Consumer<Player>) player -> advanceToNearestStation(player)
      ),
      Map.entry(
          "Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde.",
          (Consumer<Player>) player -> advanceToNearestUtility(player)
      ),
      Map.entry(
          "De bank betaald je een dividend van M50.",
          (Consumer<Player>) player -> player.account().deposit(new Money(50))
      ),
      Map.entry(
          "Verlaat de gevangenis zonder te betalen.",
          (Consumer<Player>) player -> deeds.hold(Deeds.RetainedCard.CHANCE_GET_OUT_OF_JAIL_FREE, player)
      ),
      Map.entry(
          "Keer 3 stappen terug.",
          (Consumer<Player>) player -> player.position().moveTo(player.position().index() - 3)
      ),
      Map.entry(
          "Ga naar de gevangenis. Passeer niet langs START, je ontvangt geen M200.",
          (Consumer<Player>) jail::imprison
      ),
      Map.entry(
          "Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel.",
          (Consumer<Player>) player -> repair(player, new Money(25), new Money(100))
      ),
      Map.entry(
          "Boete voor te snel rijden. Betaal M15.",
          (Consumer<Player>) player -> payBank(player, new Money(15))
      ),
      Map.entry(
          "Ga door naar Noord Station / Gare du Nord. If you pass START, collect M200.",
          (Consumer<Player>) player -> moveTo(player, NoordStation, true)
      ),
      Map.entry(
          "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50.",
          (Consumer<Player>) player -> payEveryOtherPlayer(player, new Money(50))
      ),
      Map.entry(
          "Je lening is afbetaald. Je ontvangt M150.",
          (Consumer<Player>) player -> player.account().deposit(new Money(150))
      )
    );
    this.communityChestEffects = Map.ofEntries(
      Map.entry(
          "Je maakt elke week tijd vrij voor je bejaarde buurman — Je hebt geweldige verhalen gehoord! Je ontvant M100.",
          (Consumer<Player>) player -> player.account().deposit(new Money(100))
      ),
      Map.entry(
          "Je organiseert een groep om de voetpaden op te ruimen. Je ontvangt M50.",
          (Consumer<Player>) player -> player.account().deposit(new Money(50))
      ),
      Map.entry(
          "Je bent vrijwilliger bij het rode kruis. Er waren gratis koekjes! Je ontvangt M10.",
          (Consumer<Player>) player -> player.account().deposit(new Money(10))
      ),
      Map.entry(
          "Je koopt wat koekjes op het schoolfestival. Lekker! Je betaald M50.",
          (Consumer<Player>) player -> payBank(player, new Money(50))
      ),
      Map.entry(
          "Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen.",
          (Consumer<Player>) player -> deeds.hold(Deeds.RetainedCard.COMMUNITY_CHEST_GET_OUT_OF_JAIL_FREE, player)
      ),
      Map.entry(
          "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler.",
          (Consumer<Player>) player -> collectFromEveryOtherPlayer(player, new Money(10))
      ),
      Map.entry(
          "Luide muziek diep in de nacht? Je buren zijn boos. Ga naar de gevangenis. Je komt niet langs start START. Je ontvangt geen M200.",
          (Consumer<Player>) jail::imprison
      ),
      Map.entry(
          "Je helpt jouw buur met haar boodschappen. Ze bedankt je met een lekkere lunch! Je ontvangt M20.",
          (Consumer<Player>) player -> player.account().deposit(new Money(20))
      ),
      Map.entry(
          "Je helpt met het bouwen van een nieuwe speelplaats! Je ontvangt M100.",
          (Consumer<Player>) player -> player.account().deposit(new Money(100))
      ),
      Map.entry(
          "Je speelt de hele dag met de kinderen in het kinderhospitaal. Je ontvangt M100.",
          (Consumer<Player>) player -> player.account().deposit(new Money(100))
      ),
      Map.entry(
          "Je ging naar de car wash inzamelactie van de school — Maar je vergat de ramen te sluiten! je betaald M100.",
          (Consumer<Player>) player -> payBank(player, new Money(100))
      ),
      Map.entry(
          "Net wanneer je denkt dat je geen stap verder kan, bereik je de finish! Ga door naar START. je ontvangt M200.",
          (Consumer<Player>) player -> moveTo(player, start, true)
      ),
      Map.entry(
          "Je helpt je buren hun tuin opruimen na het onweer. Je ontvangt M200.",
          (Consumer<Player>) player -> player.account().deposit(new Money(200))
      ),
      Map.entry(
          "Je vrienden in het dierenasiel zijn je dankbaar voor je gulheid. je betaald M50.",
          (Consumer<Player>) player -> payBank(player, new Money(50))
      ),
      Map.entry(
          "Je had beter deelgenomen aan het renovatie project — je zou waardevolle vaardigheden geleerd hebben! Betaal M40 voor elk huis wat je bezit. M115 voor elk hotel.",
          (Consumer<Player>) player -> repair(player, new Money(40), new Money(115))
      ),
      Map.entry(
          "je organiseert een wafelbak voor de plaatstelijke school. Je ontvangt M25.",
          (Consumer<Player>) player -> player.account().deposit(new Money(25))
      )
    );
  }

  @Override
  public void resolve(Player player, Street space, Roll roll) {
    switch (space.kind()) {
      case chance -> {
        String card = decks.drawChance();
        if (card == null) return;
        events.drewChanceCard(player, card);
        resolveChance(player, card);
      }
      case community_chest -> {
        String card = decks.drawCommunityChest();
        if (card == null) return;
        events.drewCommunityChestCard(player, card);
        resolveCommunityChest(player, card);
      }
      default -> {
      }
    }
  }

  private void resolveChance(Player player, String card) {
    applyEffect(chanceEffects, card, player);
  }

  private void resolveCommunityChest(Player player, String card) {
    applyEffect(communityChestEffects, card, player);
  }

  private void applyEffect(Map<String, Consumer<Player>> effects, String card, Player player) {
    Consumer<Player> effect = effects.get(card);
    if (effect != null) effect.accept(player);
  }

  private void moveTo(Player player, Street.Type destination, boolean collectSalaryIfPassing) {
    int from = player.position().index();
    int to = rules.gameboard().positionOf(destination);
    player.position().moveTo(to);
    if (collectSalaryIfPassing && passesStart(from, to)) {
      Money salary = player.pass(start());
      events.collectedSalary(player, salary);
    }
  }

  private void advanceToNearestStation(Player player) {
    Street.Type nearestStation = nearestStationFrom(player.position().index());
    moveTo(player, nearestStation, false);
    Station station = (Station) rules.create(nearestStation);
    resolveNearestOwnedLand(player, station, station.rentForOwning(1).plus(station.rentForOwning(1)));
  }

  private void advanceToNearestUtility(Player player) {
    Street.Type nearestUtility = nearestUtilityFrom(player.position().index());
    moveTo(player, nearestUtility, false);
    Utility utility = (Utility) rules.create(nearestUtility);
    if (deeds.isUnowned(utility.type())) {
      buyIfAccepted(player, utility);
      return;
    }
    Roll extraRoll = cup.roll();
    paySpecialRent(player, utility, new Money(extraRoll.total() * 10));
  }

  private void resolveNearestOwnedLand(Player player, Ownable land, Money rent) {
    if (deeds.isUnowned(land.type())) {
      buyIfAccepted(player, land);
      return;
    }
    paySpecialRent(player, land, rent);
  }

  private void buyIfAccepted(Player player, Ownable land) {
    if (!strategies.forPlayer(player).accepts(new Strategy.Offer(land, player.account().balance().amount()))) return;
    deeds.sell(land, player, land.price());
    events.bought(player, land, land.price());
  }

  private void paySpecialRent(Player player, Ownable land, Money amount) {
    Optional<Player> owner = deeds.ownerOf(land.type()).flatMap(this::playerNamed);
    if (owner.isEmpty() || owner.get().id().equals(player.id()) || deeds.isMortgaged(land)) return;
    Player landlord = owner.get();
    player.account().withdraw(amount);
    landlord.account().deposit(amount);
    events.paid(player, landlord, land, amount);
  }

  private void payEveryOtherPlayer(Player player, Money amount) {
    forEveryOtherPlayer(player, other -> transfer(player, other, amount));
  }

  private void collectFromEveryOtherPlayer(Player player, Money amount) {
    forEveryOtherPlayer(player, other -> transfer(other, player, amount));
  }

  private void forEveryOtherPlayer(Player player, Consumer<Player> action) {
    for (Player other : players) {
      if (other.id().equals(player.id())) continue;
      action.accept(other);
    }
  }

  private void transfer(Player payer, Player payee, Money amount) {
    payer.account().withdraw(amount);
    payee.account().deposit(amount);
    events.paid(payer, payee, amount);
  }

  private void repair(Player player, Money perHouse, Money perHotel) {
    Money cost = Money.ZERO;
    for (Street space : rules.streets().toList()) {
      if (!(space instanceof ColourStreet street)) continue;
      if (deeds.ownerOf(street.type()).filter(player.id()::equals).isEmpty()) continue;
      cost = cost.plus(new Money(deeds.housesBuiltOn(street) * perHouse.amount()));
      if (deeds.hasHotelOn(street)) cost = cost.plus(perHotel);
    }
    if (!cost.equals(Money.ZERO)) payBank(player, cost);
  }

  private void payBank(Player player, Money amount) {
    player.account().withdraw(amount);
    events.paidBank(player, amount);
  }

  private Street.Type nearestStationFrom(int position) {
    if (position < 15) return CentraalStation;
    if (position < 25) return Street.Type.Buurtspoorwegen;
    if (position < 35) return Street.Type.ZuidStation;
    return Street.Type.NoordStation;
  }

  private Street.Type nearestUtilityFrom(int position) {
    return position < 12 || position >= 28 ? Elektriciteitscentrale : Street.Type.Watermaatschappij;
  }

  private boolean passesStart(int from, int to) {
    return to <= from;
  }

  private Optional<Player> playerNamed(Player.ID id) {
    return players.stream().filter(it -> it.id().equals(id)).findFirst();
  }

  private StartSpace start() {
    return (StartSpace) rules.create(start);
  }

  public interface Decks {
    Decks EMPTY = new Decks() {
      @Override
      public String drawChance() {
        return null;
      }

      @Override
      public String drawCommunityChest() {
        return null;
      }
    };

    String drawChance();

    String drawCommunityChest();
  }

  public interface Events {
    default void drewChanceCard(Player player, String card) {
    }

    default void drewCommunityChestCard(Player player, String card) {
    }

    default void paidBank(Player player, Money amount) {
    }

    default void collectedSalary(Player player, Money salary) {
    }

    default void bought(Player buyer, Ownable land, Money price) {
    }

    default void paid(Player tenant, Player owner, Ownable land, Money rent) {
    }

    default void paid(Player payer, Player payee, Money amount) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=5cb774692854ed90ac42fa24e1f7e2e4e01c4bb13037b7463df22b4e2a0dba0d
scope.0.id=Y2xhc3M6Q2FyZHMjQ2FyZHM6MzI
scope.0.kind=class
scope.0.startLine=32
scope.0.endLine=378
scope.0.semanticHash=d37a183490e389ba84eedc63a69ebc458fc65f7f4f1079e77bcf2cbc32db07a2
scope.1.id=Y2xhc3M6Q2FyZHMuRGVja3MjRGVja3M6MzM4
scope.1.kind=class
scope.1.startLine=338
scope.1.endLine=354
scope.1.semanticHash=ef5cf83a0b693680d125f93b5d224de301b13802ad968b301ca7f8c17d28aa03
scope.2.id=Y2xhc3M6Q2FyZHMuRGVja3MuIzozMzk
scope.2.kind=class
scope.2.startLine=339
scope.2.endLine=349
scope.2.semanticHash=2b576fb5dd021afd0922c9494d6778520a99a177e7335c905ad4552867f6acb0
scope.3.id=Y2xhc3M6Q2FyZHMuRXZlbnRzI0V2ZW50czozNTY
scope.3.kind=class
scope.3.startLine=356
scope.3.endLine=377
scope.3.semanticHash=a60bd502de4ebcc546a2b14a3ff78b570e43791d0d957d9427eb3ce8e1b65a9c
scope.4.id=ZmllbGQ6Q2FyZHMjY2hhbmNlRWZmZWN0czo0MQ
scope.4.kind=field
scope.4.startLine=41
scope.4.endLine=41
scope.4.semanticHash=38fa63a6cfd5e072f9c39e298b715aedabf0db812c61c362763d072b583efa3a
scope.5.id=ZmllbGQ6Q2FyZHMjY29tbXVuaXR5Q2hlc3RFZmZlY3RzOjQy
scope.5.kind=field
scope.5.startLine=42
scope.5.endLine=42
scope.5.semanticHash=7ec2d88fe299cda08ed48e4ceee233c417556379a6a766080d6f48b288c48780
scope.6.id=ZmllbGQ6Q2FyZHMjY3VwOjM5
scope.6.kind=field
scope.6.startLine=39
scope.6.endLine=39
scope.6.semanticHash=4ae53f57002dea57cceac893a4facafbe5d9e0989268accba8cc0b9b1b70e4ae
scope.7.id=ZmllbGQ6Q2FyZHMjZGVja3M6Mzc
scope.7.kind=field
scope.7.startLine=37
scope.7.endLine=37
scope.7.semanticHash=fe10dbc7b79bda6410dc8beeb1c489acac5338e74ef6acecbeea5305c10841a2
scope.8.id=ZmllbGQ6Q2FyZHMjZGVlZHM6MzM
scope.8.kind=field
scope.8.startLine=33
scope.8.endLine=33
scope.8.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.9.id=ZmllbGQ6Q2FyZHMjZXZlbnRzOjM4
scope.9.kind=field
scope.9.startLine=38
scope.9.endLine=38
scope.9.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.10.id=ZmllbGQ6Q2FyZHMjamFpbDo0MA
scope.10.kind=field
scope.10.startLine=40
scope.10.endLine=40
scope.10.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.11.id=ZmllbGQ6Q2FyZHMjcGxheWVyczozNQ
scope.11.kind=field
scope.11.startLine=35
scope.11.endLine=35
scope.11.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.12.id=ZmllbGQ6Q2FyZHMjcnVsZXM6MzQ
scope.12.kind=field
scope.12.startLine=34
scope.12.endLine=34
scope.12.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.13.id=ZmllbGQ6Q2FyZHMjc3RyYXRlZ2llczozNg
scope.13.kind=field
scope.13.startLine=36
scope.13.endLine=36
scope.13.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.14.id=ZmllbGQ6Q2FyZHMuRGVja3MjRU1QVFk6MzM5
scope.14.kind=field
scope.14.startLine=339
scope.14.endLine=349
scope.14.semanticHash=7820cd4ba7626263bb22a21f9d65fc8826d029f9ca9bbee46ff774b8136829b2
scope.15.id=bWV0aG9kOkNhcmRzI2FkdmFuY2VUb05lYXJlc3RTdGF0aW9uKDEpOjIzNg
scope.15.kind=method
scope.15.startLine=236
scope.15.endLine=241
scope.15.semanticHash=9680a9310759657639f715cf979a3a00db593bb91c1cb5f773d1dabb693b1ad0
scope.16.id=bWV0aG9kOkNhcmRzI2FkdmFuY2VUb05lYXJlc3RVdGlsaXR5KDEpOjI0Mw
scope.16.kind=method
scope.16.startLine=243
scope.16.endLine=253
scope.16.semanticHash=4f7c76fbf9d47269c2dd5a31e5582f84c9d70d14f03fd6ecd2937f1b107026df
scope.17.id=bWV0aG9kOkNhcmRzI2FwcGx5RWZmZWN0KDMpOjIyMQ
scope.17.kind=method
scope.17.startLine=221
scope.17.endLine=224
scope.17.semanticHash=a85287f1158a29f45f46378c66be706d6334c4a24fe5dcdee0e1c46f64b95279
scope.18.id=bWV0aG9kOkNhcmRzI2J1eUlmQWNjZXB0ZWQoMik6MjYz
scope.18.kind=method
scope.18.startLine=263
scope.18.endLine=267
scope.18.semanticHash=681cff6cb2a99e43c37acb803c9210c75d4f477d4b6d8d872edf368ab2449ab4
scope.19.id=bWV0aG9kOkNhcmRzI2NvbGxlY3RGcm9tRXZlcnlPdGhlclBsYXllcigyKToyODI
scope.19.kind=method
scope.19.startLine=282
scope.19.endLine=284
scope.19.semanticHash=e2e3cabf4b21ae631f1e4b8950b7d933c9d26d62ebd359705c4047dc67ef4855
scope.20.id=bWV0aG9kOkNhcmRzI2N0b3IoNyk6NDQ
scope.20.kind=method
scope.20.startLine=44
scope.20.endLine=49
scope.20.semanticHash=af85b0a8447badf564da1faeec4f1721091edb7faa3013c77406e6a3e20b58f1
scope.21.id=bWV0aG9kOkNhcmRzI2N0b3IoOCk6NTE
scope.21.kind=method
scope.21.startLine=51
scope.21.endLine=191
scope.21.semanticHash=4dbe29a7f8653964bdc95f5c8fb5c0ac23cef6db98cf2b3ecb02ee7a1fbd26de
scope.22.id=bWV0aG9kOkNhcmRzI2ZvckV2ZXJ5T3RoZXJQbGF5ZXIoMik6Mjg2
scope.22.kind=method
scope.22.startLine=286
scope.22.endLine=291
scope.22.semanticHash=c33add1da7ee7440d8cc8050b4ff7516cf27df6e4d0f399962bf5086c948666c
scope.23.id=bWV0aG9kOkNhcmRzI21vdmVUbygzKToyMjY
scope.23.kind=method
scope.23.startLine=226
scope.23.endLine=234
scope.23.semanticHash=aa3c08b2181fc8ed84e3c87d4ba1d71a3ad92f15dddf0329dc7d8586411b8bbb
scope.24.id=bWV0aG9kOkNhcmRzI25lYXJlc3RTdGF0aW9uRnJvbSgxKTozMTU
scope.24.kind=method
scope.24.startLine=315
scope.24.endLine=320
scope.24.semanticHash=41ef05423396490061361fb466ef00845ae6267c90dbe0d6ee7073e489e30ce2
scope.25.id=bWV0aG9kOkNhcmRzI25lYXJlc3RVdGlsaXR5RnJvbSgxKTozMjI
scope.25.kind=method
scope.25.startLine=322
scope.25.endLine=324
scope.25.semanticHash=1cabe84ca4b4a6c50bb5be30bcd1e7e35e757eacb6217bbcbf816b7aae1f67fb
scope.26.id=bWV0aG9kOkNhcmRzI3Bhc3Nlc1N0YXJ0KDIpOjMyNg
scope.26.kind=method
scope.26.startLine=326
scope.26.endLine=328
scope.26.semanticHash=06190d79e96c8b7ba59f65e9f47c6f432e31408f4a04d6cd9471568d0c89caa3
scope.27.id=bWV0aG9kOkNhcmRzI3BheUJhbmsoMik6MzEw
scope.27.kind=method
scope.27.startLine=310
scope.27.endLine=313
scope.27.semanticHash=1f39aeb2b57754514eeadc674bdc6a6da6af34eed477312be702ee7c46acb1f1
scope.28.id=bWV0aG9kOkNhcmRzI3BheUV2ZXJ5T3RoZXJQbGF5ZXIoMik6Mjc4
scope.28.kind=method
scope.28.startLine=278
scope.28.endLine=280
scope.28.semanticHash=c43756e4a4036dd9bba5e6842a1c84247917f86e6606cd234b21a7cbb63eb86a
scope.29.id=bWV0aG9kOkNhcmRzI3BheVNwZWNpYWxSZW50KDMpOjI2OQ
scope.29.kind=method
scope.29.startLine=269
scope.29.endLine=276
scope.29.semanticHash=0520e156371c476d179b995694eb74f5ac0c517308f75688b0c3acc85b618838
scope.30.id=bWV0aG9kOkNhcmRzI3BsYXllck5hbWVkKDEpOjMzMA
scope.30.kind=method
scope.30.startLine=330
scope.30.endLine=332
scope.30.semanticHash=fe784ad0d125f4f24c91a494994efaa90a23932b8683b3623632b72cf559a25c
scope.31.id=bWV0aG9kOkNhcmRzI3JlcGFpcigzKToyOTk
scope.31.kind=method
scope.31.startLine=299
scope.31.endLine=308
scope.31.semanticHash=0cd5e8a8469fa89ba6fcaca05a748f6d7cd427161d18f6b11e70dda455733b11
scope.32.id=bWV0aG9kOkNhcmRzI3Jlc29sdmUoMyk6MTkz
scope.32.kind=method
scope.32.startLine=193
scope.32.endLine=211
scope.32.semanticHash=78e21146aa1a6e4f5a0be32ae3f372f03c5eb4ced792fb7b2295885e0f308ba2
scope.33.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVDaGFuY2UoMik6MjEz
scope.33.kind=method
scope.33.startLine=213
scope.33.endLine=215
scope.33.semanticHash=1988adc70238943bdf1656e6039e31d358cf88f849f29059bff4d2719d4abb57
scope.34.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVDb21tdW5pdHlDaGVzdCgyKToyMTc
scope.34.kind=method
scope.34.startLine=217
scope.34.endLine=219
scope.34.semanticHash=9ee220be1a098761911e3f25168726d1285fa94b4db0d8ecf1fdb7245e383158
scope.35.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVOZWFyZXN0T3duZWRMYW5kKDMpOjI1NQ
scope.35.kind=method
scope.35.startLine=255
scope.35.endLine=261
scope.35.semanticHash=b57bccc6249a032fb0172f9e981cc026f58a56e1f750caf66b245abd0efc39a7
scope.36.id=bWV0aG9kOkNhcmRzI3N0YXJ0KDApOjMzNA
scope.36.kind=method
scope.36.startLine=334
scope.36.endLine=336
scope.36.semanticHash=0b7b648dc264a3be8475ab9fe4816d765dfd01de5d8fde789cca137a195e3940
scope.37.id=bWV0aG9kOkNhcmRzI3RyYW5zZmVyKDMpOjI5Mw
scope.37.kind=method
scope.37.startLine=293
scope.37.endLine=297
scope.37.semanticHash=f37b148b8b7412ffe2fad82fb7ebbd643ac92b1c232b830bef559bc518a377c3
scope.38.id=bWV0aG9kOkNhcmRzLkRlY2tzI2RyYXdDaGFuY2UoMCk6MzUx
scope.38.kind=method
scope.38.startLine=351
scope.38.endLine=351
scope.38.semanticHash=a84b4c10cc2fde301018e36d1204857190da2706ae8a907322e7b0e0369b1866
scope.39.id=bWV0aG9kOkNhcmRzLkRlY2tzI2RyYXdDb21tdW5pdHlDaGVzdCgwKTozNTM
scope.39.kind=method
scope.39.startLine=353
scope.39.endLine=353
scope.39.semanticHash=6c5b3b712f6d4cd48469f646abbf574fa6f5dfd76e2ad10981ec5cf0c95498b9
scope.40.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNjdG9yKDApOjMzOQ
scope.40.kind=method
scope.40.startLine=1
scope.40.endLine=378
scope.40.semanticHash=e97c9007ab4e500cfa58d6621ac77063f95ca5fc3a26cd6d5ad3230cfdeb693b
scope.41.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNkcmF3Q2hhbmNlKDApOjM0MA
scope.41.kind=method
scope.41.startLine=340
scope.41.endLine=343
scope.41.semanticHash=75f25725ea61196b43460e87d9eb7ac2f3793c53743dc71c8aa48c6b5295c86e
scope.42.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNkcmF3Q29tbXVuaXR5Q2hlc3QoMCk6MzQ1
scope.42.kind=method
scope.42.startLine=345
scope.42.endLine=348
scope.42.semanticHash=a9b96cb65b99d8a7e227376b20728f740ab1b699cd8ff8e6942ca7a361c4efae
scope.43.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNib3VnaHQoMyk6MzY5
scope.43.kind=method
scope.43.startLine=369
scope.43.endLine=370
scope.43.semanticHash=fe51180aade1580800fcb97fecb03c63966e127c7bc1fa39f2bef4fb96d1e8f3
scope.44.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNjb2xsZWN0ZWRTYWxhcnkoMik6MzY2
scope.44.kind=method
scope.44.startLine=366
scope.44.endLine=367
scope.44.semanticHash=6cd1f36caef5236a7b98bb77c90da39864e729d6d252fdc58b623f587a32a1b6
scope.45.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNkcmV3Q2hhbmNlQ2FyZCgyKTozNTc
scope.45.kind=method
scope.45.startLine=357
scope.45.endLine=358
scope.45.semanticHash=7b0194aa9f23bf63efea81fd631b079753c33870544c4dca20241857204bfc7b
scope.46.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNkcmV3Q29tbXVuaXR5Q2hlc3RDYXJkKDIpOjM2MA
scope.46.kind=method
scope.46.startLine=360
scope.46.endLine=361
scope.46.semanticHash=9528ca3ad2486c9408cbdb3ef55d179c6130ef07e735e866a35ecf42335536e8
scope.47.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkKDMpOjM3NQ
scope.47.kind=method
scope.47.startLine=375
scope.47.endLine=376
scope.47.semanticHash=a7b204c460e052f26c3c3bfb8f065a5aeea58037f43216563d5b349738c621d0
scope.48.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkKDQpOjM3Mg
scope.48.kind=method
scope.48.startLine=372
scope.48.endLine=373
scope.48.semanticHash=a556c06e7cd612f2e84e1f18f111dee281ba093fb489ff34d85445cfef3df069
scope.49.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkQmFuaygyKTozNjM
scope.49.kind=method
scope.49.startLine=363
scope.49.endLine=364
scope.49.semanticHash=eef709f7374719985ca7629b07bc1a836e3c62e85ca90eb3e71b0f0e0eca0eb2
*/
