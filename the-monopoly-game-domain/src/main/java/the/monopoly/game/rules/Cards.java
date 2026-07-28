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
import java.util.Optional;

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

  public Cards(
      Deeds deeds, Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies,
      Decks decks, Events events, Cup cup
  ) {
    this.deeds = deeds;
    this.rules = rules;
    this.players = players;
    this.strategies = strategies;
    this.decks = decks;
    this.events = events;
    this.cup = cup;
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
    switch (card) {
      case "Ga door naar Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)." ->
          moveTo(player, NieuwstraatBrussel, false);
      case "Ga door naar START (Ontvang M200)." ->
          moveTo(player, start, true);
      case "Ga door naar Grand Place (Mons). Als je langs START komt, ontvang je M200." ->
          moveTo(player, GrandPlaceMons, true);
      case "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200." ->
          moveTo(player, RueDeDiekirchArlon, true);
      case "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs." ->
          advanceToNearestStation(player);
      case "Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde." ->
          advanceToNearestUtility(player);
      case "De bank betaald je een dividend van M50." ->
          player.account().deposit(new Money(50));
      case "Verlaat de gevangenis zonder te betalen." ->
          deeds.hold(Deeds.RetainedCard.CHANCE_GET_OUT_OF_JAIL_FREE, player);
      case "Keer 3 stappen terug." ->
          player.position().moveTo(player.position().index() - 3);
      case "Ga naar de gevangenis. Passeer niet langs START, je ontvangt geen M200." ->
          player.position().moveTo(rules.gameboard().positionOf(OpBezoek));
      case "Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel." ->
          repair(player, new Money(25), new Money(100));
      case "Boete voor te snel rijden. Betaal M15." ->
          payBank(player, new Money(15));
      case "Ga door naar Noord Station / Gare du Nord. If you pass START, collect M200." ->
          moveTo(player, NoordStation, true);
      case "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." ->
          payEveryOtherPlayer(player, new Money(50));
      case "Je lening is afbetaald. Je ontvangt M150." ->
          player.account().deposit(new Money(150));
      default -> {
      }
    }
  }

  private void resolveCommunityChest(Player player, String card) {
    switch (card) {
      case "Je maakt elke week tijd vrij voor je bejaarde buurman — Je hebt geweldige verhalen gehoord! Je ontvant M100." ->
          player.account().deposit(new Money(100));
      case "Je organiseert een groep om de voetpaden op te ruimen. Je ontvangt M50." ->
          player.account().deposit(new Money(50));
      case "Je bent vrijwilliger bij het rode kruis. Er waren gratis koekjes! Je ontvangt M10." ->
          player.account().deposit(new Money(10));
      case "Je koopt wat koekjes op het schoolfestival. Lekker! Je betaald M50." ->
          payBank(player, new Money(50));
      case "Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen." ->
          deeds.hold(Deeds.RetainedCard.COMMUNITY_CHEST_GET_OUT_OF_JAIL_FREE, player);
      case "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." ->
          collectFromEveryOtherPlayer(player, new Money(10));
      case "Luide muziek diep in de nacht? Je buren zijn boos. Ga naar de gevangenis. Je komt niet langs start START. Je ontvangt geen M200." ->
          player.position().moveTo(rules.gameboard().positionOf(OpBezoek));
      case "Je helpt jouw buur met haar boodschappen. Ze bedankt je met een lekkere lunch! Je ontvangt M20." ->
          player.account().deposit(new Money(20));
      case "Je helpt met het bouwen van een nieuwe speelplaats! Je ontvangt M100." ->
          player.account().deposit(new Money(100));
      case "Je speelt de hele dag met de kinderen in het kinderhospitaal. Je ontvangt M100." ->
          player.account().deposit(new Money(100));
      case "Je ging naar de car wash inzamelactie van de school — Maar je vergat de ramen te sluiten! je betaald M100." ->
          payBank(player, new Money(100));
      case "Net wanneer je denkt dat je geen stap verder kan, bereik je de finish! Ga door naar START. je ontvangt M200." ->
          moveTo(player, start, true);
      case "Je helpt je buren hun tuin opruimen na het onweer. Je ontvangt M200." ->
          player.account().deposit(new Money(200));
      case "Je vrienden in het dierenasiel zijn je dankbaar voor je gulheid. je betaald M50." ->
          payBank(player, new Money(50));
      case "Je had beter deelgenomen aan het renovatie project — je zou waardevolle vaardigheden geleerd hebben! Betaal M40 voor elk huis wat je bezit. M115 voor elk hotel." ->
          repair(player, new Money(40), new Money(115));
      case "je organiseert een wafelbak voor de plaatstelijke school. Je ontvangt M25." ->
          player.account().deposit(new Money(25));
      default -> {
      }
    }
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
    moveTo(player, nearestStationFrom(player.position().index()), false);
    Station station = (Station) rules.create(CentraalStation);
    resolveNearestOwnedLand(player, station, station.rentForOwning(1).plus(station.rentForOwning(1)));
  }

  private void advanceToNearestUtility(Player player) {
    moveTo(player, nearestUtilityFrom(player.position().index()), false);
    Utility utility = (Utility) rules.create(Elektriciteitscentrale);
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
    for (Player other : players) {
      if (other.id().equals(player.id())) continue;
      player.account().withdraw(amount);
      other.account().deposit(amount);
    }
  }

  private void collectFromEveryOtherPlayer(Player player, Money amount) {
    for (Player other : players) {
      if (other.id().equals(player.id())) continue;
      other.account().withdraw(amount);
      player.account().deposit(amount);
    }
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
  }
}
