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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
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
    static Decks official() {
      return new OfficialDecks();
    }

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

  private static final class OfficialDecks implements Decks {
    private final Deque<String> chance;
    private final Deque<String> communityChest;

    private OfficialDecks() {
      chance = shuffled(List.of(
          "Ga door naar Nieuwstraat (Brussel) / Rue Neuve (Bruxelles).",
          "Ga door naar START (Ontvang M200).",
          "Ga door naar Grand Place (Mons). Als je langs START komt, ontvang je M200.",
          "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.",
          "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
          "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
          "Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde.",
          "De bank betaald je een dividend van M50.",
          "Verlaat de gevangenis zonder te betalen.",
          "Keer 3 stappen terug.",
          "Ga naar de gevangenis. Passeer niet langs START, je ontvangt geen M200.",
          "Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel.",
          "Boete voor te snel rijden. Betaal M15.",
          "Ga door naar Noord Station / Gare du Nord. If you pass START, collect M200.",
          "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50.",
          "Je lening is afbetaald. Je ontvangt M150."
      ));
      communityChest = shuffled(List.of(
          "Je maakt elke week tijd vrij voor je bejaarde buurman — Je hebt geweldige verhalen gehoord! Je ontvant M100.",
          "Je organiseert een groep om de voetpaden op te ruimen. Je ontvangt M50.",
          "Je bent vrijwilliger bij het rode kruis. Er waren gratis koekjes! Je ontvangt M10.",
          "Je koopt wat koekjes op het schoolfestival. Lekker! Je betaald M50.",
          "Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen.",
          "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler.",
          "Luide muziek diep in de nacht? Je buren zijn boos. Ga naar de gevangenis. Je komt niet langs start START. Je ontvangt geen M200.",
          "Je helpt jouw buur met haar boodschappen. Ze bedankt je met een lekkere lunch! Je ontvangt M20.",
          "Je helpt met het bouwen van een nieuwe speelplaats! Je ontvangt M100.",
          "Je speelt de hele dag met de kinderen in het kinderhospitaal. Je ontvangt M100.",
          "Je ging naar de car wash inzamelactie van de school — Maar je vergat de ramen te sluiten! je betaald M100.",
          "Net wanneer je denkt dat je geen stap verder kan, bereik je de finish! Ga door naar START. je ontvangt M200.",
          "Je helpt je buren hun tuin opruimen na het onweer. Je ontvangt M200.",
          "Je vrienden in het dierenasiel zijn je dankbaar voor je gulheid. je betaald M50.",
          "Je had beter deelgenomen aan het renovatie project — je zou waardevolle vaardigheden geleerd hebben! Betaal M40 voor elk huis wat je bezit. M115 voor elk hotel.",
          "je organiseert een wafelbak voor de plaatstelijke school. Je ontvangt M25."
      ));
    }

    private static Deque<String> shuffled(List<String> cards) {
      var shuffled = new ArrayList<>(cards);
      Collections.shuffle(shuffled);
      return new ArrayDeque<>(shuffled);
    }

    @Override
    public String drawChance() {
      return draw(chance);
    }

    @Override
    public String drawCommunityChest() {
      return draw(communityChest);
    }

    private static String draw(Deque<String> deck) {
      String card = deck.removeFirst();
      deck.addLast(card);
      return card;
    }
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
moduleHash=c027361ad605f84b2a4a57b5f6957f363afc238ec3afd5975db0fc8d567e9867
scope.0.id=Y2xhc3M6Q2FyZHMjQ2FyZHM6MzY
scope.0.kind=class
scope.0.startLine=36
scope.0.endLine=452
scope.0.semanticHash=d7a2932ce002dadd9e012976fdd95dbd62c917288b8be72b54b546b820ea6409
scope.1.id=Y2xhc3M6Q2FyZHMuRGVja3MjRGVja3M6MzQy
scope.1.kind=class
scope.1.startLine=342
scope.1.endLine=362
scope.1.semanticHash=1b96663f0d2cb2840c11c9e1166190f49fc9a6f737960e5700987602275ae48d
scope.2.id=Y2xhc3M6Q2FyZHMuRGVja3MuIzozNDc
scope.2.kind=class
scope.2.startLine=347
scope.2.endLine=357
scope.2.semanticHash=2b576fb5dd021afd0922c9494d6778520a99a177e7335c905ad4552867f6acb0
scope.3.id=Y2xhc3M6Q2FyZHMuRXZlbnRzI0V2ZW50czo0MzA
scope.3.kind=class
scope.3.startLine=430
scope.3.endLine=451
scope.3.semanticHash=a60bd502de4ebcc546a2b14a3ff78b570e43791d0d957d9427eb3ce8e1b65a9c
scope.4.id=Y2xhc3M6Q2FyZHMuT2ZmaWNpYWxEZWNrcyNPZmZpY2lhbERlY2tzOjM2NA
scope.4.kind=class
scope.4.startLine=364
scope.4.endLine=428
scope.4.semanticHash=77c1a359f24870d5d186bee2f6263e51b0df2748f1d15ae1f8256d20f3561311
scope.5.id=ZmllbGQ6Q2FyZHMjY2hhbmNlRWZmZWN0czo0NQ
scope.5.kind=field
scope.5.startLine=45
scope.5.endLine=45
scope.5.semanticHash=38fa63a6cfd5e072f9c39e298b715aedabf0db812c61c362763d072b583efa3a
scope.6.id=ZmllbGQ6Q2FyZHMjY29tbXVuaXR5Q2hlc3RFZmZlY3RzOjQ2
scope.6.kind=field
scope.6.startLine=46
scope.6.endLine=46
scope.6.semanticHash=7ec2d88fe299cda08ed48e4ceee233c417556379a6a766080d6f48b288c48780
scope.7.id=ZmllbGQ6Q2FyZHMjY3VwOjQz
scope.7.kind=field
scope.7.startLine=43
scope.7.endLine=43
scope.7.semanticHash=4ae53f57002dea57cceac893a4facafbe5d9e0989268accba8cc0b9b1b70e4ae
scope.8.id=ZmllbGQ6Q2FyZHMjZGVja3M6NDE
scope.8.kind=field
scope.8.startLine=41
scope.8.endLine=41
scope.8.semanticHash=fe10dbc7b79bda6410dc8beeb1c489acac5338e74ef6acecbeea5305c10841a2
scope.9.id=ZmllbGQ6Q2FyZHMjZGVlZHM6Mzc
scope.9.kind=field
scope.9.startLine=37
scope.9.endLine=37
scope.9.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.10.id=ZmllbGQ6Q2FyZHMjZXZlbnRzOjQy
scope.10.kind=field
scope.10.startLine=42
scope.10.endLine=42
scope.10.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.11.id=ZmllbGQ6Q2FyZHMjamFpbDo0NA
scope.11.kind=field
scope.11.startLine=44
scope.11.endLine=44
scope.11.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.12.id=ZmllbGQ6Q2FyZHMjcGxheWVyczozOQ
scope.12.kind=field
scope.12.startLine=39
scope.12.endLine=39
scope.12.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.13.id=ZmllbGQ6Q2FyZHMjcnVsZXM6Mzg
scope.13.kind=field
scope.13.startLine=38
scope.13.endLine=38
scope.13.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.14.id=ZmllbGQ6Q2FyZHMjc3RyYXRlZ2llczo0MA
scope.14.kind=field
scope.14.startLine=40
scope.14.endLine=40
scope.14.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.15.id=ZmllbGQ6Q2FyZHMuRGVja3MjRU1QVFk6MzQ3
scope.15.kind=field
scope.15.startLine=347
scope.15.endLine=357
scope.15.semanticHash=7820cd4ba7626263bb22a21f9d65fc8826d029f9ca9bbee46ff774b8136829b2
scope.16.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcyNjaGFuY2U6MzY1
scope.16.kind=field
scope.16.startLine=365
scope.16.endLine=365
scope.16.semanticHash=d6a70ebd46c4e0c81742d471a2b254fd7cb924e71494c1a1fa10b9cb2a99e0fd
scope.17.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcyNjb21tdW5pdHlDaGVzdDozNjY
scope.17.kind=field
scope.17.startLine=366
scope.17.endLine=366
scope.17.semanticHash=9ad84cd3fdb5c8123f4b2fc0ea8bf16ef44a7b708cd5841d11456b0aecb99859
scope.18.id=bWV0aG9kOkNhcmRzI2FkdmFuY2VUb05lYXJlc3RTdGF0aW9uKDEpOjI0MA
scope.18.kind=method
scope.18.startLine=240
scope.18.endLine=245
scope.18.semanticHash=9680a9310759657639f715cf979a3a00db593bb91c1cb5f773d1dabb693b1ad0
scope.19.id=bWV0aG9kOkNhcmRzI2FkdmFuY2VUb05lYXJlc3RVdGlsaXR5KDEpOjI0Nw
scope.19.kind=method
scope.19.startLine=247
scope.19.endLine=257
scope.19.semanticHash=4f7c76fbf9d47269c2dd5a31e5582f84c9d70d14f03fd6ecd2937f1b107026df
scope.20.id=bWV0aG9kOkNhcmRzI2FwcGx5RWZmZWN0KDMpOjIyNQ
scope.20.kind=method
scope.20.startLine=225
scope.20.endLine=228
scope.20.semanticHash=a85287f1158a29f45f46378c66be706d6334c4a24fe5dcdee0e1c46f64b95279
scope.21.id=bWV0aG9kOkNhcmRzI2J1eUlmQWNjZXB0ZWQoMik6MjY3
scope.21.kind=method
scope.21.startLine=267
scope.21.endLine=271
scope.21.semanticHash=681cff6cb2a99e43c37acb803c9210c75d4f477d4b6d8d872edf368ab2449ab4
scope.22.id=bWV0aG9kOkNhcmRzI2NvbGxlY3RGcm9tRXZlcnlPdGhlclBsYXllcigyKToyODY
scope.22.kind=method
scope.22.startLine=286
scope.22.endLine=288
scope.22.semanticHash=e2e3cabf4b21ae631f1e4b8950b7d933c9d26d62ebd359705c4047dc67ef4855
scope.23.id=bWV0aG9kOkNhcmRzI2N0b3IoNyk6NDg
scope.23.kind=method
scope.23.startLine=48
scope.23.endLine=53
scope.23.semanticHash=af85b0a8447badf564da1faeec4f1721091edb7faa3013c77406e6a3e20b58f1
scope.24.id=bWV0aG9kOkNhcmRzI2N0b3IoOCk6NTU
scope.24.kind=method
scope.24.startLine=55
scope.24.endLine=195
scope.24.semanticHash=4dbe29a7f8653964bdc95f5c8fb5c0ac23cef6db98cf2b3ecb02ee7a1fbd26de
scope.25.id=bWV0aG9kOkNhcmRzI2ZvckV2ZXJ5T3RoZXJQbGF5ZXIoMik6Mjkw
scope.25.kind=method
scope.25.startLine=290
scope.25.endLine=295
scope.25.semanticHash=c33add1da7ee7440d8cc8050b4ff7516cf27df6e4d0f399962bf5086c948666c
scope.26.id=bWV0aG9kOkNhcmRzI21vdmVUbygzKToyMzA
scope.26.kind=method
scope.26.startLine=230
scope.26.endLine=238
scope.26.semanticHash=aa3c08b2181fc8ed84e3c87d4ba1d71a3ad92f15dddf0329dc7d8586411b8bbb
scope.27.id=bWV0aG9kOkNhcmRzI25lYXJlc3RTdGF0aW9uRnJvbSgxKTozMTk
scope.27.kind=method
scope.27.startLine=319
scope.27.endLine=324
scope.27.semanticHash=41ef05423396490061361fb466ef00845ae6267c90dbe0d6ee7073e489e30ce2
scope.28.id=bWV0aG9kOkNhcmRzI25lYXJlc3RVdGlsaXR5RnJvbSgxKTozMjY
scope.28.kind=method
scope.28.startLine=326
scope.28.endLine=328
scope.28.semanticHash=1cabe84ca4b4a6c50bb5be30bcd1e7e35e757eacb6217bbcbf816b7aae1f67fb
scope.29.id=bWV0aG9kOkNhcmRzI3Bhc3Nlc1N0YXJ0KDIpOjMzMA
scope.29.kind=method
scope.29.startLine=330
scope.29.endLine=332
scope.29.semanticHash=06190d79e96c8b7ba59f65e9f47c6f432e31408f4a04d6cd9471568d0c89caa3
scope.30.id=bWV0aG9kOkNhcmRzI3BheUJhbmsoMik6MzE0
scope.30.kind=method
scope.30.startLine=314
scope.30.endLine=317
scope.30.semanticHash=1f39aeb2b57754514eeadc674bdc6a6da6af34eed477312be702ee7c46acb1f1
scope.31.id=bWV0aG9kOkNhcmRzI3BheUV2ZXJ5T3RoZXJQbGF5ZXIoMik6Mjgy
scope.31.kind=method
scope.31.startLine=282
scope.31.endLine=284
scope.31.semanticHash=c43756e4a4036dd9bba5e6842a1c84247917f86e6606cd234b21a7cbb63eb86a
scope.32.id=bWV0aG9kOkNhcmRzI3BheVNwZWNpYWxSZW50KDMpOjI3Mw
scope.32.kind=method
scope.32.startLine=273
scope.32.endLine=280
scope.32.semanticHash=0520e156371c476d179b995694eb74f5ac0c517308f75688b0c3acc85b618838
scope.33.id=bWV0aG9kOkNhcmRzI3BsYXllck5hbWVkKDEpOjMzNA
scope.33.kind=method
scope.33.startLine=334
scope.33.endLine=336
scope.33.semanticHash=fe784ad0d125f4f24c91a494994efaa90a23932b8683b3623632b72cf559a25c
scope.34.id=bWV0aG9kOkNhcmRzI3JlcGFpcigzKTozMDM
scope.34.kind=method
scope.34.startLine=303
scope.34.endLine=312
scope.34.semanticHash=0cd5e8a8469fa89ba6fcaca05a748f6d7cd427161d18f6b11e70dda455733b11
scope.35.id=bWV0aG9kOkNhcmRzI3Jlc29sdmUoMyk6MTk3
scope.35.kind=method
scope.35.startLine=197
scope.35.endLine=215
scope.35.semanticHash=78e21146aa1a6e4f5a0be32ae3f372f03c5eb4ced792fb7b2295885e0f308ba2
scope.36.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVDaGFuY2UoMik6MjE3
scope.36.kind=method
scope.36.startLine=217
scope.36.endLine=219
scope.36.semanticHash=1988adc70238943bdf1656e6039e31d358cf88f849f29059bff4d2719d4abb57
scope.37.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVDb21tdW5pdHlDaGVzdCgyKToyMjE
scope.37.kind=method
scope.37.startLine=221
scope.37.endLine=223
scope.37.semanticHash=9ee220be1a098761911e3f25168726d1285fa94b4db0d8ecf1fdb7245e383158
scope.38.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVOZWFyZXN0T3duZWRMYW5kKDMpOjI1OQ
scope.38.kind=method
scope.38.startLine=259
scope.38.endLine=265
scope.38.semanticHash=b57bccc6249a032fb0172f9e981cc026f58a56e1f750caf66b245abd0efc39a7
scope.39.id=bWV0aG9kOkNhcmRzI3N0YXJ0KDApOjMzOA
scope.39.kind=method
scope.39.startLine=338
scope.39.endLine=340
scope.39.semanticHash=0b7b648dc264a3be8475ab9fe4816d765dfd01de5d8fde789cca137a195e3940
scope.40.id=bWV0aG9kOkNhcmRzI3RyYW5zZmVyKDMpOjI5Nw
scope.40.kind=method
scope.40.startLine=297
scope.40.endLine=301
scope.40.semanticHash=f37b148b8b7412ffe2fad82fb7ebbd643ac92b1c232b830bef559bc518a377c3
scope.41.id=bWV0aG9kOkNhcmRzLkRlY2tzI2RyYXdDaGFuY2UoMCk6MzU5
scope.41.kind=method
scope.41.startLine=359
scope.41.endLine=359
scope.41.semanticHash=a84b4c10cc2fde301018e36d1204857190da2706ae8a907322e7b0e0369b1866
scope.42.id=bWV0aG9kOkNhcmRzLkRlY2tzI2RyYXdDb21tdW5pdHlDaGVzdCgwKTozNjE
scope.42.kind=method
scope.42.startLine=361
scope.42.endLine=361
scope.42.semanticHash=6c5b3b712f6d4cd48469f646abbf574fa6f5dfd76e2ad10981ec5cf0c95498b9
scope.43.id=bWV0aG9kOkNhcmRzLkRlY2tzI29mZmljaWFsKDApOjM0Mw
scope.43.kind=method
scope.43.startLine=343
scope.43.endLine=345
scope.43.semanticHash=37dcb3ae52b733350d1693b4b8fcbf096381ea14cbd80e5bd16d1694e3fe19df
scope.44.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNjdG9yKDApOjM0Nw
scope.44.kind=method
scope.44.startLine=1
scope.44.endLine=452
scope.44.semanticHash=4f1d45e216d2b7ad34f1da381419dd58d3793d97fe3770fa72740fd3bfbd76a7
scope.45.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNkcmF3Q2hhbmNlKDApOjM0OA
scope.45.kind=method
scope.45.startLine=348
scope.45.endLine=351
scope.45.semanticHash=75f25725ea61196b43460e87d9eb7ac2f3793c53743dc71c8aa48c6b5295c86e
scope.46.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNkcmF3Q29tbXVuaXR5Q2hlc3QoMCk6MzUz
scope.46.kind=method
scope.46.startLine=353
scope.46.endLine=356
scope.46.semanticHash=a9b96cb65b99d8a7e227376b20728f740ab1b699cd8ff8e6942ca7a361c4efae
scope.47.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNib3VnaHQoMyk6NDQz
scope.47.kind=method
scope.47.startLine=443
scope.47.endLine=444
scope.47.semanticHash=fe51180aade1580800fcb97fecb03c63966e127c7bc1fa39f2bef4fb96d1e8f3
scope.48.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNjb2xsZWN0ZWRTYWxhcnkoMik6NDQw
scope.48.kind=method
scope.48.startLine=440
scope.48.endLine=441
scope.48.semanticHash=6cd1f36caef5236a7b98bb77c90da39864e729d6d252fdc58b623f587a32a1b6
scope.49.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNkcmV3Q2hhbmNlQ2FyZCgyKTo0MzE
scope.49.kind=method
scope.49.startLine=431
scope.49.endLine=432
scope.49.semanticHash=7b0194aa9f23bf63efea81fd631b079753c33870544c4dca20241857204bfc7b
scope.50.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNkcmV3Q29tbXVuaXR5Q2hlc3RDYXJkKDIpOjQzNA
scope.50.kind=method
scope.50.startLine=434
scope.50.endLine=435
scope.50.semanticHash=9528ca3ad2486c9408cbdb3ef55d179c6130ef07e735e866a35ecf42335536e8
scope.51.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkKDMpOjQ0OQ
scope.51.kind=method
scope.51.startLine=449
scope.51.endLine=450
scope.51.semanticHash=a7b204c460e052f26c3c3bfb8f065a5aeea58037f43216563d5b349738c621d0
scope.52.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkKDQpOjQ0Ng
scope.52.kind=method
scope.52.startLine=446
scope.52.endLine=447
scope.52.semanticHash=a556c06e7cd612f2e84e1f18f111dee281ba093fb489ff34d85445cfef3df069
scope.53.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkQmFuaygyKTo0Mzc
scope.53.kind=method
scope.53.startLine=437
scope.53.endLine=438
scope.53.semanticHash=eef709f7374719985ca7629b07bc1a836e3c62e85ca90eb3e71b0f0e0eca0eb2
scope.54.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MjY3RvcigwKTozNjg
scope.54.kind=method
scope.54.startLine=368
scope.54.endLine=405
scope.54.semanticHash=297a31e9b8a149e86d04542f83bc1bb7c5d438e28e031613c02389e6fa749fb9
scope.55.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MjZHJhdygxKTo0MjM
scope.55.kind=method
scope.55.startLine=423
scope.55.endLine=427
scope.55.semanticHash=53663e5a50f2551674af04d87f804da013c2ddd8b432536bd6552dab3598e98e
scope.56.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MjZHJhd0NoYW5jZSgwKTo0MTM
scope.56.kind=method
scope.56.startLine=413
scope.56.endLine=416
scope.56.semanticHash=7a54a8020fd6a162b2a865884276316c185d96ed664188f575ad5dc2a00383f9
scope.57.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MjZHJhd0NvbW11bml0eUNoZXN0KDApOjQxOA
scope.57.kind=method
scope.57.startLine=418
scope.57.endLine=421
scope.57.semanticHash=bb229e25ff271a33a76d84290917e0d0ff74fb236c0a6152be8db511dd41d7ff
scope.58.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3Mjc2h1ZmZsZWQoMSk6NDA3
scope.58.kind=method
scope.58.startLine=407
scope.58.endLine=411
scope.58.semanticHash=41b4c289f78497a686d06ea3c4351d87ccbef7321ad94aedc92b9182e31617c8
*/
