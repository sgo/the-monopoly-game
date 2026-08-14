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
    deeds.entityOwnerOf(land.type()).ifPresent(entity -> collect(entity, tenant, (ColourStreet) land));
  }

  private void collect(LegalEntity entity, Player tenant, ColourStreet land) {
    if (deeds.isMortgaged(land)) return;
    Money rent = entityRent(land);
    tenant.account().withdraw(rent);
    entity.depositToBank(rent);
    entity.receiveRent(land);
    events.paid(tenant, entity, land, rent);
  }

  private Money entityRent(ColourStreet street) {
    if (deeds.hasHotelOn(street)) return street.rentForOneHotel();
    int houses = deeds.housesBuiltOn(street);
    return houses > 0 ? street.rentForHouses(houses)
        : street.vacantRent().plus(street.vacantRent());
  }

  private void collect(Player owner, Player tenant, Ownable land, Roll roll) {
    if (owner.id().equals(tenant.id())) return;
    if (deeds.isMortgaged(land)) return;
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
        .allMatch(it -> deeds.ownerOf(it.type()).filter(owner.id()::equals).isPresent() && !deeds.isMortgaged(it));
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

    default void paid(Player tenant, LegalEntity entity, ColourStreet land, Money rent) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=32a6c00379c10558f43fa2147b23952e44ecc84af1c1e64b2395063c657885bd
scope.0.id=Y2xhc3M6UmVudCNSZW50OjE3
scope.0.kind=class
scope.0.startLine=17
scope.0.endLine=102
scope.0.semanticHash=a5c47752042d8cd7084d633304609e11a453f733324676790a0eb795e8eb6e2d
scope.1.id=Y2xhc3M6UmVudC5FdmVudHMjRXZlbnRzOjk2
scope.1.kind=class
scope.1.startLine=96
scope.1.endLine=101
scope.1.semanticHash=a97bcb02463d73b6aac2fedfc6fb62bb8ca04361469c4e34aa48be230caf93e7
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
scope.7.id=bWV0aG9kOlJlbnQjY29sbGVjdCgzKTozOQ
scope.7.kind=method
scope.7.startLine=39
scope.7.endLine=46
scope.7.semanticHash=9affb654b4b6fa174421120cff172a306a269474bf8b63aed5b252b696634c19
scope.8.id=bWV0aG9kOlJlbnQjY29sbGVjdCg0KTo1NQ
scope.8.kind=method
scope.8.startLine=55
scope.8.endLine=63
scope.8.semanticHash=906dfb31a4a778b3f0476eae5abcb2256233ee63ec144e847d06482b00854845
scope.9.id=bWV0aG9kOlJlbnQjY29sb3VyU3RyZWV0UmVudCgyKTo3NA
scope.9.kind=method
scope.9.startLine=74
scope.9.endLine=84
scope.9.semanticHash=16e6d98241514c1aad7d123c40d4e58c3f78ab087096f9fef45f34ad0a7ab475
scope.10.id=bWV0aG9kOlJlbnQjY3Rvcig1KToyNA
scope.10.kind=method
scope.10.startLine=24
scope.10.endLine=30
scope.10.semanticHash=2c805b1f02919623a3df7643f4080941db6014ed1f24ee698789d06fcc6a1689
scope.11.id=bWV0aG9kOlJlbnQjZW50aXR5UmVudCgxKTo0OA
scope.11.kind=method
scope.11.startLine=48
scope.11.endLine=53
scope.11.semanticHash=7ffcc55ebffd519a11c4860b8ba24b632f5bbe6d057c0f5f0472a61d58816dcd
scope.12.id=bWV0aG9kOlJlbnQjb3duZWQoMik6ODY
scope.12.kind=method
scope.12.startLine=86
scope.12.endLine=89
scope.12.semanticHash=3a3cc3a4127442b176d4bb18120267dcd184a0f1452e6a88afdaa657ab2fd358
scope.13.id=bWV0aG9kOlJlbnQjcGxheWVyTmFtZWQoMSk6OTE
scope.13.kind=method
scope.13.startLine=91
scope.13.endLine=93
scope.13.semanticHash=fe784ad0d125f4f24c91a494994efaa90a23932b8683b3623632b72cf559a25c
scope.14.id=bWV0aG9kOlJlbnQjcmVudEZvcigzKTo2NQ
scope.14.kind=method
scope.14.startLine=65
scope.14.endLine=72
scope.14.semanticHash=927f0a52ad48acf992edfc026934c19e8d7fb7da90296c34d13c42815e2b7b68
scope.15.id=bWV0aG9kOlJlbnQjcmVzb2x2ZSgzKTozMg
scope.15.kind=method
scope.15.startLine=32
scope.15.endLine=37
scope.15.semanticHash=de8570595b73affa06e32ae14a42b1e1b823cfc6c509b8b4294be431643b894e
scope.16.id=bWV0aG9kOlJlbnQuRXZlbnRzI3BhaWQoNCk6OTc
scope.16.kind=method
scope.16.startLine=97
scope.16.endLine=97
scope.16.semanticHash=71d83f0a2565ae7b89740e40b53df44902a39a9e2b7225fcf1fe5ef2fd8283bb
scope.17.id=bWV0aG9kOlJlbnQuRXZlbnRzI3BhaWQoNCk6OTk
scope.17.kind=method
scope.17.startLine=99
scope.17.endLine=100
scope.17.semanticHash=2d1b6db01f8977714501e8aeb2a6656a122c33b6a5b3aac95129086943f837c8
*/
