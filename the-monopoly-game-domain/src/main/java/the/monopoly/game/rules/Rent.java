package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.Utility;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Optional;

/** Collects rent when a visitor stops on an owned, unimproved colour street. */
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
  public void resolve(Player tenant, Street space) {
    resolve(tenant, space, null);
  }

  @Override
  public void resolve(Player tenant, Street space, Roll roll) {
    deeds.ownerOf(space.type()).flatMap(this::playerNamed).ifPresent(owner -> collect(owner, tenant, space, roll));
  }

  private void collect(Player owner, Player tenant, Street land, Roll roll) {
    if (owner.id().equals(tenant.id())) return;
    Money rent = rentFor(owner, land, roll);
    if (!strategies.forPlayer(owner).claims(new Strategy.RentClaim(tenant, land, rent))) return;
    tenant.account().withdraw(rent);
    owner.account().deposit(rent);
    events.paid(tenant, owner, land, rent);
  }

  private Money rentFor(Player owner, Street land, Roll roll) {
    if (land instanceof Station station)
      return station.rentForOwning(owned(owner, Station.class));
    if (land instanceof Utility utility)
      return new Money(utility.rentDiceMultiplierForOwning(owned(owner, Utility.class)) * roll.total());
    ColourStreet street = (ColourStreet) land;
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
    void paid(Player tenant, Player owner, Street land, Money rent);
  }
}

/* mutate4java-manifest
version=1
moduleHash=9128bb8ca206f255c4161b82fcd39d18442e7f1703193c11b453144d1a660528
scope.0.id=Y2xhc3M6UmVudCNSZW50OjEz
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=60
scope.0.semanticHash=e8f3e057d2ff58b92ba56aab7e0dc4ceb1fb47931611bd76ae094b76b970ce85
scope.1.id=Y2xhc3M6UmVudC5FdmVudHMjRXZlbnRzOjU3
scope.1.kind=class
scope.1.startLine=57
scope.1.endLine=59
scope.1.semanticHash=30ad8e5487fd82544b1aa8573c9e45f95844f173a6da0dd9ba39857078226a11
scope.2.id=ZmllbGQ6UmVudCNkZWVkczoxNA
scope.2.kind=field
scope.2.startLine=14
scope.2.endLine=14
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6UmVudCNldmVudHM6MTg
scope.3.kind=field
scope.3.startLine=18
scope.3.endLine=18
scope.3.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.4.id=ZmllbGQ6UmVudCNwbGF5ZXJzOjE2
scope.4.kind=field
scope.4.startLine=16
scope.4.endLine=16
scope.4.semanticHash=ba011afd51a7e6dfe6280940e28a5adb3bd4bfde375a6834eed1ae35dff4ff1c
scope.5.id=ZmllbGQ6UmVudCNydWxlczoxNQ
scope.5.kind=field
scope.5.startLine=15
scope.5.endLine=15
scope.5.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.6.id=ZmllbGQ6UmVudCNzdHJhdGVnaWVzOjE3
scope.6.kind=field
scope.6.startLine=17
scope.6.endLine=17
scope.6.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.7.id=bWV0aG9kOlJlbnQjY29sbGVjdCgzKTozNA
scope.7.kind=method
scope.7.startLine=34
scope.7.endLine=41
scope.7.semanticHash=4652192ec6593831f8148a9a8baf9c8581643be190bda873f0285c88ac416513
scope.8.id=bWV0aG9kOlJlbnQjY3Rvcig1KToyMA
scope.8.kind=method
scope.8.startLine=20
scope.8.endLine=26
scope.8.semanticHash=2c805b1f02919623a3df7643f4080941db6014ed1f24ee698789d06fcc6a1689
scope.9.id=bWV0aG9kOlJlbnQjcGxheWVyTmFtZWQoMSk6NTI
scope.9.kind=method
scope.9.startLine=52
scope.9.endLine=54
scope.9.semanticHash=fe784ad0d125f4f24c91a494994efaa90a23932b8683b3623632b72cf559a25c
scope.10.id=bWV0aG9kOlJlbnQjcmVudEZvcigyKTo0Mw
scope.10.kind=method
scope.10.startLine=43
scope.10.endLine=50
scope.10.semanticHash=f602d979397f9c829518fb23b855ce13bd25462396d1de7a028c50842bd9fd57
scope.11.id=bWV0aG9kOlJlbnQjcmVzb2x2ZSgyKToyOA
scope.11.kind=method
scope.11.startLine=28
scope.11.endLine=32
scope.11.semanticHash=812179aee565f107c2c332b338703ddf0ee7674f1a24f6b95a5db79b61cb5879
scope.12.id=bWV0aG9kOlJlbnQuRXZlbnRzI3BhaWQoNCk6NTg
scope.12.kind=method
scope.12.startLine=58
scope.12.endLine=58
scope.12.semanticHash=719a63ff6cd149f4fbf8c215569efc26c4481a365c78453c82b93038fca2e26a
*/
