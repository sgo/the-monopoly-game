package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Who owns which space. The rules lay the board out afresh every time they are
 * asked, so a space is a value rather than a thing that can be marked; the
 * title to it is kept here instead, against the space's type, for as long as a
 * game lasts.
 */
public class Deeds {
  private final Map<Street.Type, Player.ID> owners = new HashMap<>();
  private final Map<Street.Type, Improvement> improvements = new HashMap<>();

  public boolean isUnowned(Street.Type land) {
    return !owners.containsKey(land);
  }

  /** Who holds the title to this land, if anyone does. */
  public Optional<Player.ID> ownerOf(Street.Type land) {
    return Optional.ofNullable(owners.get(land));
  }

  /**
   * Hands the title to a buyer, who pays the bank what the land went for. That
   * is the price on the board when it is bought, and the winning bid when it is
   * auctioned, so the sale is told what it fetched rather than working it out.
   */
  public void sell(Ownable land, Player buyer, Money price) {
    buyer.account().withdraw(price);
    owners.put(land.type(), buyer.id());
  }

  public int housesBuiltOn(ColourStreet land) {
    return improvementOn(land).houses();
  }

  public boolean hasHotelOn(ColourStreet land) {
    return improvementOn(land).hotel();
  }

  public void arrangeHouses(ColourStreet land, int houses) {
    improvements.put(land.type(), Improvement.withHouses(houses));
  }

  public void arrangeHotel(ColourStreet land) {
    improvements.put(land.type(), Improvement.withHotel());
  }

  public void buildHouse(ColourStreet land, Player owner) {
    verifyOwner(land, owner);
    owner.account().withdraw(land.houseConstructionCost());
    improvements.put(land.type(), improvementOn(land).withAnotherHouse());
  }

  public void buildHotel(ColourStreet land, Player owner) {
    verifyOwner(land, owner);
    owner.account().withdraw(hotelValueOf(land));
    improvements.put(land.type(), Improvement.withHotel());
  }

  public Money sellHouse(ColourStreet land, Player owner) {
    verifyOwner(land, owner);
    return refund(land, owner, improvementOn(land).withOneLessHouse(), land.houseConstructionCost());
  }

  public Money exchangeHotelForHouses(ColourStreet land, Player owner) {
    verifyOwner(land, owner);
    return refund(
        land, owner,
        Improvement.withHouses(land.hotelConstructionRequiresNumberOfHouses()), hotelValueOf(land)
    );
  }

  private Money refund(ColourStreet land, Player owner, Improvement newImprovement, Money fullPrice) {
    improvements.put(land.type(), newImprovement);
    Money price = half(fullPrice);
    owner.account().deposit(price);
    return price;
  }

  private Improvement improvementOn(ColourStreet land) {
    return improvements.getOrDefault(land.type(), Improvement.UNIMPROVED);
  }

  private void verifyOwner(ColourStreet land, Player owner) {
    if (ownerOf(land.type()).filter(owner.id()::equals).isEmpty())
      throw new IllegalStateException(owner.id().value() + " does not own " + land.type() + ".");
  }

  private static Money hotelValueOf(ColourStreet land) {
    return land.rentForOneHotel();
  }

  private static Money half(Money price) {
    return new Money(price.amount() / 2);
  }

  private record Improvement(int houses, boolean hotel) {
    private static final Improvement UNIMPROVED = new Improvement(0, false);

    private static Improvement withHouses(int houses) {
      return new Improvement(houses, false);
    }

    private static Improvement withHotel() {
      return new Improvement(0, true);
    }

    private Improvement withAnotherHouse() {
      return new Improvement(houses + 1, false);
    }

    private Improvement withOneLessHouse() {
      return new Improvement(houses - 1, false);
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=b6df4f4a3105cf543edb8e134ddc4afda42b9d527989358f18d3cdff98f90752
scope.0.id=Y2xhc3M6RGVlZHMjRGVlZHM6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=39
scope.0.semanticHash=1f9718c3d14316be0131dbe9124c7eb16f264f539a9e2dc9f55a750fc200bbc5
scope.1.id=ZmllbGQ6RGVlZHMjb3duZXJzOjE5
scope.1.kind=field
scope.1.startLine=19
scope.1.endLine=19
scope.1.semanticHash=96142c84799464504dabd909915e6daee49568f895a220f99cbe848745aa4492
scope.2.id=bWV0aG9kOkRlZWRzI2N0b3IoMCk6MTg
scope.2.kind=method
scope.2.startLine=1
scope.2.endLine=39
scope.2.semanticHash=b84cf9ebfe0a012264e86c46e3e7eafe50daec06b80df02564432a72cc4681b9
scope.3.id=bWV0aG9kOkRlZWRzI2lzVW5vd25lZCgxKToyMQ
scope.3.kind=method
scope.3.startLine=21
scope.3.endLine=23
scope.3.semanticHash=b5fbaa45464bf90d554ea7add1f945f6f5027a3dbc4518f95d9f0a5ce602edfc
scope.4.id=bWV0aG9kOkRlZWRzI293bmVyT2YoMSk6MjY
scope.4.kind=method
scope.4.startLine=26
scope.4.endLine=28
scope.4.semanticHash=5b5d48e94bbee42661fe2a00386b3585e4b5f1ebe10a5df06318cbc242b22090
scope.5.id=bWV0aG9kOkRlZWRzI3NlbGwoMyk6MzU
scope.5.kind=method
scope.5.startLine=35
scope.5.endLine=38
scope.5.semanticHash=6c391fe5295e11609838f608937f07d26992a19798b259b55391a12ca146adc5
*/
