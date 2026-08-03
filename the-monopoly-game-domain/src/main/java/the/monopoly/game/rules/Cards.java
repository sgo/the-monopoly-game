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
  /**
   * Named once so the effects map, the official deck, and the deck's own
   * withholding check all agree on the exact card text: three independent
   * copies of a long, easy-to-mistype string could silently drift apart,
   * quietly turning off the "retain until used" rule for this one card.
   */
  static final String CHANCE_GET_OUT_OF_JAIL_FREE_CARD = "Verlaat de gevangenis zonder te betalen.";
  static final String COMMUNITY_CHEST_GET_OUT_OF_JAIL_FREE_CARD =
      "Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. "
          + "Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen.";

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
          (Consumer<Player>) player -> moveToAndResolve(player, NieuwstraatBrussel, false)
      ),
      Map.entry(
          "Ga door naar START (Ontvang M200).",
          (Consumer<Player>) player -> moveTo(player, start, true)
      ),
      Map.entry(
          "Ga door naar Grand Place (Mons). Als je langs START komt, ontvang je M200.",
          (Consumer<Player>) player -> moveToAndResolve(player, GrandPlaceMons, true)
      ),
      Map.entry(
          "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.",
          (Consumer<Player>) player -> moveToAndResolve(player, RueDeDiekirchArlon, true)
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
          CHANCE_GET_OUT_OF_JAIL_FREE_CARD,
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
          (Consumer<Player>) player -> moveToAndResolve(player, NoordStation, true)
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
          COMMUNITY_CHEST_GET_OUT_OF_JAIL_FREE_CARD,
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
    events.moved(player, from, to, officialSpaceAt(from), destination);
    if (collectSalaryIfPassing && passesStart(from, to)) {
      Money salary = player.pass(start());
      events.collectedSalary(player, salary);
    }
  }

  private void moveToAndResolve(Player player, Street.Type destination, boolean collectSalaryIfPassing) {
    moveTo(player, destination, collectSalaryIfPassing);
    Street land = rules.create(destination);
    if (!(land instanceof Ownable ownable)) return;
    if (deeds.isUnowned(ownable.type())) {
      buyIfAccepted(player, ownable);
      return;
    }
    new Rent(deeds, rules, players, strategies,
        (tenant, owner, rentedLand, rent) -> events.paid(tenant, owner, rentedLand, rent))
        .resolve(player, land, new Roll(0, 0));
  }

  private Street.Type officialSpaceAt(int position) {
    return rules.gameboard().layout().get(Math.floorMod(position, rules.gameboard().layout().size()));
  }

  private void advanceToNearestStation(Player player) {
    Street.Type nearestStation = nearestStationFrom(player.position().index());
    moveTo(player, nearestStation, false);
    Station station = (Station) rules.create(nearestStation);
    int stationsOwned = deeds.ownerOf(station.type()).flatMap(this::playerNamed)
        .map(this::ownedStations).orElse(0);
    Money rent = station.rentForOwning(stationsOwned).plus(station.rentForOwning(stationsOwned));
    resolveNearestOwnedLand(player, station, rent);
  }

  private int ownedStations(Player owner) {
    return (int) rules.streets().filter(Station.class::isInstance)
        .filter(it -> deeds.ownerOf(it.type()).filter(owner.id()::equals).isPresent())
        .count();
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
    Strategy strategy = strategies.forPlayer(player);
    Strategy.Offer offer = new Strategy.Offer(land, player.account().balance().amount(), strategy.cashReserve(),
        deeds.utilityMonopolyOpportunity(rules, land));
    if (!strategy.accepts(offer)) {
      events.declinedToBuy(player, land, land.price(), strategy.declineReason(offer), offer.reserve());
      return;
    }
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
      return official(new Deeds());
    }

    static Decks official(Deeds deeds) {
      return new OfficialDecks(deeds);
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
    private final WithholdingDeck chance;
    private final WithholdingDeck communityChest;

    private OfficialDecks(Deeds deeds) {
      chance = new WithholdingDeck(
          deeds, Deeds.RetainedCard.CHANCE_GET_OUT_OF_JAIL_FREE, CHANCE_GET_OUT_OF_JAIL_FREE_CARD,
          List.of(
              "Ga door naar Nieuwstraat (Brussel) / Rue Neuve (Bruxelles).",
              "Ga door naar START (Ontvang M200).",
              "Ga door naar Grand Place (Mons). Als je langs START komt, ontvang je M200.",
              "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.",
              "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
              "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
              "Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde.",
              "De bank betaald je een dividend van M50.",
              CHANCE_GET_OUT_OF_JAIL_FREE_CARD,
              "Keer 3 stappen terug.",
              "Ga naar de gevangenis. Passeer niet langs START, je ontvangt geen M200.",
              "Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel.",
              "Boete voor te snel rijden. Betaal M15.",
              "Ga door naar Noord Station / Gare du Nord. If you pass START, collect M200.",
              "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50.",
              "Je lening is afbetaald. Je ontvangt M150."
          )
      );
      communityChest = new WithholdingDeck(
          deeds, Deeds.RetainedCard.COMMUNITY_CHEST_GET_OUT_OF_JAIL_FREE, COMMUNITY_CHEST_GET_OUT_OF_JAIL_FREE_CARD,
          List.of(
              "Je maakt elke week tijd vrij voor je bejaarde buurman — Je hebt geweldige verhalen gehoord! Je ontvant M100.",
              "Je organiseert een groep om de voetpaden op te ruimen. Je ontvangt M50.",
              "Je bent vrijwilliger bij het rode kruis. Er waren gratis koekjes! Je ontvangt M10.",
              "Je koopt wat koekjes op het schoolfestival. Lekker! Je betaald M50.",
              COMMUNITY_CHEST_GET_OUT_OF_JAIL_FREE_CARD,
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
          )
      );
    }

    @Override
    public String drawChance() {
      return chance.draw();
    }

    @Override
    public String drawCommunityChest() {
      return communityChest.draw();
    }

    /**
     * A rotating deck that keeps one named card out of circulation for as
     * long as {@code deeds} says a player currently holds it: {@code
     * drawChance}/{@code drawCommunityChest} differed only in which deck,
     * which {@link Deeds.RetainedCard}, and which card text they closed over,
     * so that shape is named once here instead of twice.
     */
    private static final class WithholdingDeck {
      private final Deeds deeds;
      private final Deeds.RetainedCard retainedCard;
      private final String getOutOfJailFreeCard;
      private final Deque<String> cards;
      private String withheld;

      private WithholdingDeck(
          Deeds deeds, Deeds.RetainedCard retainedCard, String getOutOfJailFreeCard, List<String> cards
      ) {
        this.deeds = deeds;
        this.retainedCard = retainedCard;
        this.getOutOfJailFreeCard = getOutOfJailFreeCard;
        this.cards = shuffled(cards);
      }

      private String draw() {
        if (withheld != null && !deeds.holds(retainedCard)) {
          cards.addLast(withheld);
          withheld = null;
        }
        String card = cards.removeFirst();
        if (card.equals(getOutOfJailFreeCard)) withheld = card;
        else cards.addLast(card);
        return card;
      }

      private static Deque<String> shuffled(List<String> cards) {
        var shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled);
        return new ArrayDeque<>(shuffled);
      }
    }
  }

  public interface Events {
    default void moved(Player player, int from, int to, Street.Type fromSpace, Street.Type toSpace) {
    }

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

    default void declinedToBuy(Player player, Ownable land, Money price,
                               Strategy.DeclineReason reason, Money reserve) {
    }

    default void paid(Player tenant, Player owner, Ownable land, Money rent) {
    }

    default void paid(Player payer, Player payee, Money amount) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=00307e060fe2336be2d510c63482ffb24898b3746d389fae6631ed0014a44868
scope.0.id=Y2xhc3M6Q2FyZHMjQ2FyZHM6MzY
scope.0.kind=class
scope.0.startLine=36
scope.0.endLine=542
scope.0.semanticHash=40871d1881e8e1eeffb8dbe9bdb50427109a2518bcd9914f0e68a6049be5fdcc
scope.1.id=Y2xhc3M6Q2FyZHMuRGVja3MjRGVja3M6Mzg2
scope.1.kind=class
scope.1.startLine=386
scope.1.endLine=410
scope.1.semanticHash=868279e99ae35e959dad9984ee1b65f887703bd09203101b4e891595900178ba
scope.2.id=Y2xhc3M6Q2FyZHMuRGVja3MuIzozOTU
scope.2.kind=class
scope.2.startLine=395
scope.2.endLine=405
scope.2.semanticHash=2b576fb5dd021afd0922c9494d6778520a99a177e7335c905ad4552867f6acb0
scope.3.id=Y2xhc3M6Q2FyZHMuRXZlbnRzI0V2ZW50czo1MTM
scope.3.kind=class
scope.3.startLine=513
scope.3.endLine=541
scope.3.semanticHash=6d5233981f99262daddd0cb9cbf54b5886f7a49c3f99657c589ee2d618c1348d
scope.4.id=Y2xhc3M6Q2FyZHMuT2ZmaWNpYWxEZWNrcyNPZmZpY2lhbERlY2tzOjQxMg
scope.4.kind=class
scope.4.startLine=412
scope.4.endLine=511
scope.4.semanticHash=32dbe390b065112f1a9d2674fc07223020ed7b8c615d8acd7cf3421d6ed391e2
scope.5.id=Y2xhc3M6Q2FyZHMuT2ZmaWNpYWxEZWNrcy5XaXRoaG9sZGluZ0RlY2sjV2l0aGhvbGRpbmdEZWNrOjQ3OA
scope.5.kind=class
scope.5.startLine=478
scope.5.endLine=510
scope.5.semanticHash=12d5d249313e920e0b1a95571ac35fdad74b402965b3dbb01d005e77875eacde
scope.6.id=ZmllbGQ6Q2FyZHMjQ0hBTkNFX0dFVF9PVVRfT0ZfSkFJTF9GUkVFX0NBUkQ6NDM
scope.6.kind=field
scope.6.startLine=43
scope.6.endLine=43
scope.6.semanticHash=a6aa341987034b68d199393776109fe0a8ab04154ba369b3560c86145f417775
scope.7.id=ZmllbGQ6Q2FyZHMjQ09NTVVOSVRZX0NIRVNUX0dFVF9PVVRfT0ZfSkFJTF9GUkVFX0NBUkQ6NDQ
scope.7.kind=field
scope.7.startLine=44
scope.7.endLine=46
scope.7.semanticHash=c1a9681842f9bb88688e3deb4053f3c3047b4fd24d2652481e23eaffbaf3dd17
scope.8.id=ZmllbGQ6Q2FyZHMjY2hhbmNlRWZmZWN0czo1Ng
scope.8.kind=field
scope.8.startLine=56
scope.8.endLine=56
scope.8.semanticHash=38fa63a6cfd5e072f9c39e298b715aedabf0db812c61c362763d072b583efa3a
scope.9.id=ZmllbGQ6Q2FyZHMjY29tbXVuaXR5Q2hlc3RFZmZlY3RzOjU3
scope.9.kind=field
scope.9.startLine=57
scope.9.endLine=57
scope.9.semanticHash=7ec2d88fe299cda08ed48e4ceee233c417556379a6a766080d6f48b288c48780
scope.10.id=ZmllbGQ6Q2FyZHMjY3VwOjU0
scope.10.kind=field
scope.10.startLine=54
scope.10.endLine=54
scope.10.semanticHash=4ae53f57002dea57cceac893a4facafbe5d9e0989268accba8cc0b9b1b70e4ae
scope.11.id=ZmllbGQ6Q2FyZHMjZGVja3M6NTI
scope.11.kind=field
scope.11.startLine=52
scope.11.endLine=52
scope.11.semanticHash=fe10dbc7b79bda6410dc8beeb1c489acac5338e74ef6acecbeea5305c10841a2
scope.12.id=ZmllbGQ6Q2FyZHMjZGVlZHM6NDg
scope.12.kind=field
scope.12.startLine=48
scope.12.endLine=48
scope.12.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.13.id=ZmllbGQ6Q2FyZHMjZXZlbnRzOjUz
scope.13.kind=field
scope.13.startLine=53
scope.13.endLine=53
scope.13.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.14.id=ZmllbGQ6Q2FyZHMjamFpbDo1NQ
scope.14.kind=field
scope.14.startLine=55
scope.14.endLine=55
scope.14.semanticHash=c161aac5be9cd1c1c1418c0fab49c5b60881d3ef7be28bf436f95c3d566fb659
scope.15.id=ZmllbGQ6Q2FyZHMjcGxheWVyczo1MA
scope.15.kind=field
scope.15.startLine=50
scope.15.endLine=50
scope.15.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.16.id=ZmllbGQ6Q2FyZHMjcnVsZXM6NDk
scope.16.kind=field
scope.16.startLine=49
scope.16.endLine=49
scope.16.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.17.id=ZmllbGQ6Q2FyZHMjc3RyYXRlZ2llczo1MQ
scope.17.kind=field
scope.17.startLine=51
scope.17.endLine=51
scope.17.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.18.id=ZmllbGQ6Q2FyZHMuRGVja3MjRU1QVFk6Mzk1
scope.18.kind=field
scope.18.startLine=395
scope.18.endLine=405
scope.18.semanticHash=7820cd4ba7626263bb22a21f9d65fc8826d029f9ca9bbee46ff774b8136829b2
scope.19.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcyNjaGFuY2U6NDEz
scope.19.kind=field
scope.19.startLine=413
scope.19.endLine=413
scope.19.semanticHash=3d5878a812631e373fda3ee39e5a51c9b6256eeef91bb18f2c5cc098f4ad1f02
scope.20.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcyNjb21tdW5pdHlDaGVzdDo0MTQ
scope.20.kind=field
scope.20.startLine=414
scope.20.endLine=414
scope.20.semanticHash=05fd9021dd819dd0aaebddcfd61aaa7e1b932b2e8702ddc01b03ba25ad8331f6
scope.21.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcy5XaXRoaG9sZGluZ0RlY2sjY2FyZHM6NDgy
scope.21.kind=field
scope.21.startLine=482
scope.21.endLine=482
scope.21.semanticHash=f98b97fc37bf3b3b2654e735d859a9cb814812f6e10a6309f08ed6f7c2e0b1bc
scope.22.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcy5XaXRoaG9sZGluZ0RlY2sjZGVlZHM6NDc5
scope.22.kind=field
scope.22.startLine=479
scope.22.endLine=479
scope.22.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.23.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcy5XaXRoaG9sZGluZ0RlY2sjZ2V0T3V0T2ZKYWlsRnJlZUNhcmQ6NDgx
scope.23.kind=field
scope.23.startLine=481
scope.23.endLine=481
scope.23.semanticHash=4faaa654ec8b3edf3455032cbd729c5e87f07ace826112aa7c955bb98098cf83
scope.24.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcy5XaXRoaG9sZGluZ0RlY2sjcmV0YWluZWRDYXJkOjQ4MA
scope.24.kind=field
scope.24.startLine=480
scope.24.endLine=480
scope.24.semanticHash=c1a39754cc807de842af34de9816c7faaec4355029270a62381c1ff80ddd2592
scope.25.id=ZmllbGQ6Q2FyZHMuT2ZmaWNpYWxEZWNrcy5XaXRoaG9sZGluZ0RlY2sjd2l0aGhlbGQ6NDgz
scope.25.kind=field
scope.25.startLine=483
scope.25.endLine=483
scope.25.semanticHash=996ac95164e330a2b43f7e2f07ca3225b2ce3a149c7a42f4a7e33bc703f4636f
scope.26.id=bWV0aG9kOkNhcmRzI2FkdmFuY2VUb05lYXJlc3RTdGF0aW9uKDEpOjI2OQ
scope.26.kind=method
scope.26.startLine=269
scope.26.endLine=277
scope.26.semanticHash=0640d65a451b8bdbea5a1b92e7ab9f9f6a4ff8a5390231c4caedab7b23444bf6
scope.27.id=bWV0aG9kOkNhcmRzI2FkdmFuY2VUb05lYXJlc3RVdGlsaXR5KDEpOjI4NQ
scope.27.kind=method
scope.27.startLine=285
scope.27.endLine=295
scope.27.semanticHash=4f7c76fbf9d47269c2dd5a31e5582f84c9d70d14f03fd6ecd2937f1b107026df
scope.28.id=bWV0aG9kOkNhcmRzI2FwcGx5RWZmZWN0KDMpOjIzNg
scope.28.kind=method
scope.28.startLine=236
scope.28.endLine=239
scope.28.semanticHash=a85287f1158a29f45f46378c66be706d6334c4a24fe5dcdee0e1c46f64b95279
scope.29.id=bWV0aG9kOkNhcmRzI2J1eUlmQWNjZXB0ZWQoMik6MzA1
scope.29.kind=method
scope.29.startLine=305
scope.29.endLine=315
scope.29.semanticHash=7a48a770c12987d6dec38ba8c599124497d31abc8b30bf35393d9c7dddfeea2f
scope.30.id=bWV0aG9kOkNhcmRzI2NvbGxlY3RGcm9tRXZlcnlPdGhlclBsYXllcigyKTozMzA
scope.30.kind=method
scope.30.startLine=330
scope.30.endLine=332
scope.30.semanticHash=e2e3cabf4b21ae631f1e4b8950b7d933c9d26d62ebd359705c4047dc67ef4855
scope.31.id=bWV0aG9kOkNhcmRzI2N0b3IoNyk6NTk
scope.31.kind=method
scope.31.startLine=59
scope.31.endLine=64
scope.31.semanticHash=af85b0a8447badf564da1faeec4f1721091edb7faa3013c77406e6a3e20b58f1
scope.32.id=bWV0aG9kOkNhcmRzI2N0b3IoOCk6NjY
scope.32.kind=method
scope.32.startLine=66
scope.32.endLine=206
scope.32.semanticHash=319a6e8ee94b3de844aa95d3d53be7764aeeed93ea44f92048bccce91f7b9c7d
scope.33.id=bWV0aG9kOkNhcmRzI2ZvckV2ZXJ5T3RoZXJQbGF5ZXIoMik6MzM0
scope.33.kind=method
scope.33.startLine=334
scope.33.endLine=339
scope.33.semanticHash=c33add1da7ee7440d8cc8050b4ff7516cf27df6e4d0f399962bf5086c948666c
scope.34.id=bWV0aG9kOkNhcmRzI21vdmVUbygzKToyNDE
scope.34.kind=method
scope.34.startLine=241
scope.34.endLine=250
scope.34.semanticHash=f67be39847e8b94111a6129f763430008753572caf9c1fff4cab488357533a8a
scope.35.id=bWV0aG9kOkNhcmRzI21vdmVUb0FuZFJlc29sdmUoMyk6MjUy
scope.35.kind=method
scope.35.startLine=252
scope.35.endLine=263
scope.35.semanticHash=d8d8288df309596b958d84083f78fda219d92d124798780319459dbecba53bef
scope.36.id=bWV0aG9kOkNhcmRzI25lYXJlc3RTdGF0aW9uRnJvbSgxKTozNjM
scope.36.kind=method
scope.36.startLine=363
scope.36.endLine=368
scope.36.semanticHash=41ef05423396490061361fb466ef00845ae6267c90dbe0d6ee7073e489e30ce2
scope.37.id=bWV0aG9kOkNhcmRzI25lYXJlc3RVdGlsaXR5RnJvbSgxKTozNzA
scope.37.kind=method
scope.37.startLine=370
scope.37.endLine=372
scope.37.semanticHash=1cabe84ca4b4a6c50bb5be30bcd1e7e35e757eacb6217bbcbf816b7aae1f67fb
scope.38.id=bWV0aG9kOkNhcmRzI29mZmljaWFsU3BhY2VBdCgxKToyNjU
scope.38.kind=method
scope.38.startLine=265
scope.38.endLine=267
scope.38.semanticHash=877f29faa01353da0caab8d7eaa935cbb78a0b4f9e61649de0d603a70dc36468
scope.39.id=bWV0aG9kOkNhcmRzI293bmVkU3RhdGlvbnMoMSk6Mjc5
scope.39.kind=method
scope.39.startLine=279
scope.39.endLine=283
scope.39.semanticHash=77c5d2194a9a0f3051d5e90ac3887dbf0f0105257cd4bc6564b58d9ea7686333
scope.40.id=bWV0aG9kOkNhcmRzI3Bhc3Nlc1N0YXJ0KDIpOjM3NA
scope.40.kind=method
scope.40.startLine=374
scope.40.endLine=376
scope.40.semanticHash=06190d79e96c8b7ba59f65e9f47c6f432e31408f4a04d6cd9471568d0c89caa3
scope.41.id=bWV0aG9kOkNhcmRzI3BheUJhbmsoMik6MzU4
scope.41.kind=method
scope.41.startLine=358
scope.41.endLine=361
scope.41.semanticHash=1f39aeb2b57754514eeadc674bdc6a6da6af34eed477312be702ee7c46acb1f1
scope.42.id=bWV0aG9kOkNhcmRzI3BheUV2ZXJ5T3RoZXJQbGF5ZXIoMik6MzI2
scope.42.kind=method
scope.42.startLine=326
scope.42.endLine=328
scope.42.semanticHash=c43756e4a4036dd9bba5e6842a1c84247917f86e6606cd234b21a7cbb63eb86a
scope.43.id=bWV0aG9kOkNhcmRzI3BheVNwZWNpYWxSZW50KDMpOjMxNw
scope.43.kind=method
scope.43.startLine=317
scope.43.endLine=324
scope.43.semanticHash=0520e156371c476d179b995694eb74f5ac0c517308f75688b0c3acc85b618838
scope.44.id=bWV0aG9kOkNhcmRzI3BsYXllck5hbWVkKDEpOjM3OA
scope.44.kind=method
scope.44.startLine=378
scope.44.endLine=380
scope.44.semanticHash=fe784ad0d125f4f24c91a494994efaa90a23932b8683b3623632b72cf559a25c
scope.45.id=bWV0aG9kOkNhcmRzI3JlcGFpcigzKTozNDc
scope.45.kind=method
scope.45.startLine=347
scope.45.endLine=356
scope.45.semanticHash=0cd5e8a8469fa89ba6fcaca05a748f6d7cd427161d18f6b11e70dda455733b11
scope.46.id=bWV0aG9kOkNhcmRzI3Jlc29sdmUoMyk6MjA4
scope.46.kind=method
scope.46.startLine=208
scope.46.endLine=226
scope.46.semanticHash=78e21146aa1a6e4f5a0be32ae3f372f03c5eb4ced792fb7b2295885e0f308ba2
scope.47.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVDaGFuY2UoMik6MjI4
scope.47.kind=method
scope.47.startLine=228
scope.47.endLine=230
scope.47.semanticHash=1988adc70238943bdf1656e6039e31d358cf88f849f29059bff4d2719d4abb57
scope.48.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVDb21tdW5pdHlDaGVzdCgyKToyMzI
scope.48.kind=method
scope.48.startLine=232
scope.48.endLine=234
scope.48.semanticHash=9ee220be1a098761911e3f25168726d1285fa94b4db0d8ecf1fdb7245e383158
scope.49.id=bWV0aG9kOkNhcmRzI3Jlc29sdmVOZWFyZXN0T3duZWRMYW5kKDMpOjI5Nw
scope.49.kind=method
scope.49.startLine=297
scope.49.endLine=303
scope.49.semanticHash=b57bccc6249a032fb0172f9e981cc026f58a56e1f750caf66b245abd0efc39a7
scope.50.id=bWV0aG9kOkNhcmRzI3N0YXJ0KDApOjM4Mg
scope.50.kind=method
scope.50.startLine=382
scope.50.endLine=384
scope.50.semanticHash=0b7b648dc264a3be8475ab9fe4816d765dfd01de5d8fde789cca137a195e3940
scope.51.id=bWV0aG9kOkNhcmRzI3RyYW5zZmVyKDMpOjM0MQ
scope.51.kind=method
scope.51.startLine=341
scope.51.endLine=345
scope.51.semanticHash=f37b148b8b7412ffe2fad82fb7ebbd643ac92b1c232b830bef559bc518a377c3
scope.52.id=bWV0aG9kOkNhcmRzLkRlY2tzI2RyYXdDaGFuY2UoMCk6NDA3
scope.52.kind=method
scope.52.startLine=407
scope.52.endLine=407
scope.52.semanticHash=a84b4c10cc2fde301018e36d1204857190da2706ae8a907322e7b0e0369b1866
scope.53.id=bWV0aG9kOkNhcmRzLkRlY2tzI2RyYXdDb21tdW5pdHlDaGVzdCgwKTo0MDk
scope.53.kind=method
scope.53.startLine=409
scope.53.endLine=409
scope.53.semanticHash=6c5b3b712f6d4cd48469f646abbf574fa6f5dfd76e2ad10981ec5cf0c95498b9
scope.54.id=bWV0aG9kOkNhcmRzLkRlY2tzI29mZmljaWFsKDApOjM4Nw
scope.54.kind=method
scope.54.startLine=387
scope.54.endLine=389
scope.54.semanticHash=40d3a5d185fbda92984b2b9c20dad6c3612815cd530a5116a62eff48cb4252ae
scope.55.id=bWV0aG9kOkNhcmRzLkRlY2tzI29mZmljaWFsKDEpOjM5MQ
scope.55.kind=method
scope.55.startLine=391
scope.55.endLine=393
scope.55.semanticHash=f077c1a0adb81df6f0063cba8035cc35503c1884b38d11cc025b7d7920c79115
scope.56.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNjdG9yKDApOjM5NQ
scope.56.kind=method
scope.56.startLine=1
scope.56.endLine=542
scope.56.semanticHash=c4ae4303129965061d67515693f2d3ef1f992d72d9f8cf37845339738ad8b96d
scope.57.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNkcmF3Q2hhbmNlKDApOjM5Ng
scope.57.kind=method
scope.57.startLine=396
scope.57.endLine=399
scope.57.semanticHash=75f25725ea61196b43460e87d9eb7ac2f3793c53743dc71c8aa48c6b5295c86e
scope.58.id=bWV0aG9kOkNhcmRzLkRlY2tzLiNkcmF3Q29tbXVuaXR5Q2hlc3QoMCk6NDAx
scope.58.kind=method
scope.58.startLine=401
scope.58.endLine=404
scope.58.semanticHash=a9b96cb65b99d8a7e227376b20728f740ab1b699cd8ff8e6942ca7a361c4efae
scope.59.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNib3VnaHQoMyk6NTI5
scope.59.kind=method
scope.59.startLine=529
scope.59.endLine=530
scope.59.semanticHash=fe51180aade1580800fcb97fecb03c63966e127c7bc1fa39f2bef4fb96d1e8f3
scope.60.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNjb2xsZWN0ZWRTYWxhcnkoMik6NTI2
scope.60.kind=method
scope.60.startLine=526
scope.60.endLine=527
scope.60.semanticHash=6cd1f36caef5236a7b98bb77c90da39864e729d6d252fdc58b623f587a32a1b6
scope.61.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNkZWNsaW5lZFRvQnV5KDUpOjUzMg
scope.61.kind=method
scope.61.startLine=532
scope.61.endLine=534
scope.61.semanticHash=b1cefa03d5082fd2a376d8e48ca3bca37814ba8598c922cb1eccbe4d405f1cef
scope.62.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNkcmV3Q2hhbmNlQ2FyZCgyKTo1MTc
scope.62.kind=method
scope.62.startLine=517
scope.62.endLine=518
scope.62.semanticHash=7b0194aa9f23bf63efea81fd631b079753c33870544c4dca20241857204bfc7b
scope.63.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNkcmV3Q29tbXVuaXR5Q2hlc3RDYXJkKDIpOjUyMA
scope.63.kind=method
scope.63.startLine=520
scope.63.endLine=521
scope.63.semanticHash=9528ca3ad2486c9408cbdb3ef55d179c6130ef07e735e866a35ecf42335536e8
scope.64.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNtb3ZlZCg1KTo1MTQ
scope.64.kind=method
scope.64.startLine=514
scope.64.endLine=515
scope.64.semanticHash=6128ca97a5ec7331e18f8fe112bc03a1356c16e700bf8bc70d914185fe899fd2
scope.65.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkKDMpOjUzOQ
scope.65.kind=method
scope.65.startLine=539
scope.65.endLine=540
scope.65.semanticHash=a7b204c460e052f26c3c3bfb8f065a5aeea58037f43216563d5b349738c621d0
scope.66.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkKDQpOjUzNg
scope.66.kind=method
scope.66.startLine=536
scope.66.endLine=537
scope.66.semanticHash=a556c06e7cd612f2e84e1f18f111dee281ba093fb489ff34d85445cfef3df069
scope.67.id=bWV0aG9kOkNhcmRzLkV2ZW50cyNwYWlkQmFuaygyKTo1MjM
scope.67.kind=method
scope.67.startLine=523
scope.67.endLine=524
scope.67.semanticHash=eef709f7374719985ca7629b07bc1a836e3c62e85ca90eb3e71b0f0e0eca0eb2
scope.68.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MjY3RvcigxKTo0MTY
scope.68.kind=method
scope.68.startLine=416
scope.68.endLine=459
scope.68.semanticHash=c6b787907ba7e911f9189bb2f8242feb393cd032712aad44c6accac474a61d0e
scope.69.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MjZHJhd0NoYW5jZSgwKTo0NjE
scope.69.kind=method
scope.69.startLine=461
scope.69.endLine=464
scope.69.semanticHash=fce3045159b368e7fa9df2482476c0c5047748b75fb3e18a95931e7c9d741d18
scope.70.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MjZHJhd0NvbW11bml0eUNoZXN0KDApOjQ2Ng
scope.70.kind=method
scope.70.startLine=466
scope.70.endLine=469
scope.70.semanticHash=a3c2d8204c71efb7b428371f981fcf0267122fe6682467a2c7e21de0b8a9f17c
scope.71.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MuV2l0aGhvbGRpbmdEZWNrI2N0b3IoNCk6NDg1
scope.71.kind=method
scope.71.startLine=485
scope.71.endLine=492
scope.71.semanticHash=31d42dc14c504d2a28d8ac3d87b9b7d4e22fe37909f01a2122afe7e4b71ac250
scope.72.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MuV2l0aGhvbGRpbmdEZWNrI2RyYXcoMCk6NDk0
scope.72.kind=method
scope.72.startLine=494
scope.72.endLine=503
scope.72.semanticHash=f592ecd3dc85fb6f28c649d21ee9fa3ac18856dd2307429695a10f7cc90b522e
scope.73.id=bWV0aG9kOkNhcmRzLk9mZmljaWFsRGVja3MuV2l0aGhvbGRpbmdEZWNrI3NodWZmbGVkKDEpOjUwNQ
scope.73.kind=method
scope.73.startLine=505
scope.73.endLine=509
scope.73.semanticHash=dc9820895373c92c315e34f833d3b59577d2b013491c0321b443632b0c0553d2
*/
