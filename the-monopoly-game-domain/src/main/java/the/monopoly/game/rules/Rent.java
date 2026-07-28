package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.Utility;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Optional;

/** Collects rent when a visitor stops on land somebody else owns. */
public class Rent implements Landings {
  private final Deeds deeds;
  private final Rule.Set rules;
  private final List<Player> players;
  private final Strategy.OfPlayers strategies;
  private final Events events;

  public Rent(Deeds deeds, Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies, Events events) {
    this.deeds = deeds;
    this.rules = rules;
    this.players = players;
    this.strategies = strategies;
    this.events = events;
  }

  @Override
  public void resolve(Player tenant, Street space, Roll roll) {
    if (!(space instanceof Ownable land)) return;
    deeds.ownerOf(land.type()).flatMap(this::playerNamed).ifPresent(owner -> collect(owner, tenant, land, roll));
  }

  private void collect(Player owner, Player tenant, Ownable land, Roll roll) {
    if (owner.id().equals(tenant.id())) return;
    Money rent = rentFor(owner, land, roll);
    if (!strategies.forPlayer(owner).claims(new Strategy.RentClaim(tenant, land, rent))) return;
    tenant.account().withdraw(rent);
    owner.account().deposit(rent);
    events.paid(tenant, owner, land, rent);
  }

  private Money rentFor(Player owner, Ownable land, Roll roll) {
    return switch (land) {
      case ColourStreet street -> colourStreetRent(owner, street);
      case Station station -> station.rentForOwning(owned(owner, Station.class));
      case Utility utility ->
          new Money(utility.rentDiceMultiplierForOwning(owned(owner, Utility.class)) * roll.total());
    };
  }

  private Money colourStreetRent(Player owner, ColourStreet street) {
    if (deeds.hasHotelOn(street)) return street.rentForOneHotel();
    int houses = deeds.housesBuiltOn(street);
    if (houses > 0) return street.rentForHouses(houses);
    boolean monopoly = rules.streets()
        .filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast)
        .filter(it -> it.colourGroup() == street.colourGroup())
        .allMatch(it -> deeds.ownerOf(it.type()).filter(owner.id()::equals).isPresent());
    return monopoly ? street.vacantRent().plus(street.vacantRent()) : street.vacantRent();
  }

  private int owned(Player owner, Class<? extends Street> kind) {
    return (int) rules.streets().filter(kind::isInstance)
        .filter(it -> deeds.ownerOf(it.type()).filter(owner.id()::equals).isPresent()).count();
  }

  private Optional<Player> playerNamed(Player.ID id) {
    return players.stream().filter(it -> it.id().equals(id)).findFirst();
  }

  /** What the payment did, for whoever records the game. */
  public interface Events {
    void paid(Player tenant, Player owner, Ownable land, Money rent);
  }
}

/* mutate4java-manifest
version=1
moduleHash=ed2e629f504e53ff80b1756843c8591bbb317c238f155d79148eb0777c210de3
scope.0.id=Y2xhc3M6UmVudCNSZW50OjE3
scope.0.kind=class
scope.0.startLine=17
scope.0.endLine=78
scope.0.semanticHash=563a778059b09868eefafad314014b329cb9628e304fbf28b1a3b532dc50032a
scope.1.id=Y2xhc3M6UmVudC5FdmVudHMjRXZlbnRzOjc1
scope.1.kind=class
scope.1.startLine=75
scope.1.endLine=77
scope.1.semanticHash=fc09187a8709c77891ffe59ea917931ae99a82857d3ef90cca6983cd757786d2
scope.2.id=ZmllbGQ6UmVudCNkZWVkczoxOA
scope.2.kind=field
scope.2.startLine=18
scope.2.endLine=18
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6UmVudCNldmVudHM6MjI
scope.3.kind=field
scope.3.startLine=22
scope.3.endLine=22
scope.3.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.4.id=ZmllbGQ6UmVudCNwbGF5ZXJzOjIw
scope.4.kind=field
scope.4.startLine=20
scope.4.endLine=20
scope.4.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.5.id=ZmllbGQ6UmVudCNydWxlczoxOQ
scope.5.kind=field
scope.5.startLine=19
scope.5.endLine=19
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6UmVudCNzdHJhdGVnaWVzOjIx
scope.6.kind=field
scope.6.startLine=21
scope.6.endLine=21
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=bWV0aG9kOlJlbnQjY29sbGVjdCg0KTozOA
scope.7.kind=method
scope.7.startLine=38
scope.7.endLine=45
scope.7.semanticHash=d9fb83a761b1f7879348a786ef72064a14465301613e6a4ca30b2396fd2acdd7
scope.8.id=bWV0aG9kOlJlbnQjY29sb3VyU3RyZWV0UmVudCgyKTo1Ng
scope.8.kind=method
scope.8.startLine=56
scope.8.endLine=63
scope.8.semanticHash=9a023aeddc4fea10a4605661d984ec0a66c1ae24b9d072db8545310fb73af2cb
scope.9.id=bWV0aG9kOlJlbnQjY3Rvcig1KToyNA
scope.9.kind=method
scope.9.startLine=24
scope.9.endLine=30
scope.9.semanticHash=2c805b1f02919623a3df7643f4080941db6014ed1f24ee698789d06fcc6a1689
scope.10.id=bWV0aG9kOlJlbnQjb3duZWQoMik6NjU
scope.10.kind=method
scope.10.startLine=65
scope.10.endLine=68
scope.10.semanticHash=3a3cc3a4127442b176d4bb18120267dcd184a0f1452e6a88afdaa657ab2fd358
scope.11.id=bWV0aG9kOlJlbnQjcGxheWVyTmFtZWQoMSk6NzA
scope.11.kind=method
scope.11.startLine=70
scope.11.endLine=72
scope.11.semanticHash=fe784ad0d125f4f24c91a494994efaa90a23932b8683b3623632b72cf559a25c
scope.12.id=bWV0aG9kOlJlbnQjcmVudEZvcigzKTo0Nw
scope.12.kind=method
scope.12.startLine=47
scope.12.endLine=54
scope.12.semanticHash=927f0a52ad48acf992edfc026934c19e8d7fb7da90296c34d13c42815e2b7b68
scope.13.id=bWV0aG9kOlJlbnQjcmVzb2x2ZSgzKTozMg
scope.13.kind=method
scope.13.startLine=32
scope.13.endLine=36
scope.13.semanticHash=a1d62736864c4877583982867c33446900fbe7c4c6751a909fedc447bf47cbe8
scope.14.id=bWV0aG9kOlJlbnQuRXZlbnRzI3BhaWQoNCk6NzY
scope.14.kind=method
scope.14.startLine=76
scope.14.endLine=76
scope.14.semanticHash=71d83f0a2565ae7b89740e40b53df44902a39a9e2b7225fcf1fe5ef2fd8283bb
*/
