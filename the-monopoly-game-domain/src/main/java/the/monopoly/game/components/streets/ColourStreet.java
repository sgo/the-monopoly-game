package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

import java.util.List;

/**
 * A buildable street in a colour group. Every such street differs only in its
 * financial figures, so they are all built from this one data-driven factory.
 */
public record ColourStreet(
    Street.Type type,
    Street.Colour colourGroup,
    Money price,
    List<Money> rentByHouses,
    Money rentForOneHotel,
    Money constructionCost,
    Money landMortgageValue
) implements Ownable {
  /** A street holds four houses before a hotel replaces them. */
  public static final int HOUSES_PER_HOTEL = 4;

  @Override
  public Street.Kind kind() {
    return Street.Kind.street;
  }

  /**
   * Rent owed by a visitor, which depends on how far the street has been built
   * up. Nought houses is the vacant rent.
   */
  public Money rentForHouses(int houses) {
    return rentByHouses.get(OwnedCount.checked(houses, rentByHouses, type, "houses"));
  }

  public Money vacantRent() {
    return rentForHouses(0);
  }

  /**
   * A hotel costs the same as a house on the official board, so one
   * construction cost covers both.
   */
  public Money houseConstructionCost() {
    return constructionCost;
  }

  public Money hotelConstructionCost() {
    return constructionCost;
  }

  public int hotelConstructionRequiresNumberOfHouses() {
    return HOUSES_PER_HOTEL;
  }

  static Street.Factory of(
      Street.Colour colourGroup,
      int price,
      int vacantRent,
      int rentForOneHouse,
      int rentForTwoHouses,
      int rentForThreeHouses,
      int rentForFourHouses,
      int rentForOneHotel,
      int constructionCost,
      int landMortgageValue
  ) {
    List<Money> rentByHouses = List.of(
        new Money(vacantRent),
        new Money(rentForOneHouse),
        new Money(rentForTwoHouses),
        new Money(rentForThreeHouses),
        new Money(rentForFourHouses)
    );
    return (type, activatedRules) -> new ColourStreet(
        type,
        colourGroup,
        new Money(price),
        rentByHouses,
        new Money(rentForOneHotel),
        new Money(constructionCost),
        new Money(landMortgageValue)
    );
  }
}

/* mutate4java-manifest
version=1
moduleHash=9a4becefe940da56fca7a32748da7f9a97f5b1a5e0869b9106f52c9faa3528e1
scope.0.id=Y2xhc3M6Q29sb3VyU3RyZWV0I0NvbG91clN0cmVldDoxMQ
scope.0.kind=class
scope.0.startLine=11
scope.0.endLine=85
scope.0.semanticHash=6c00d1dc629698ec7d06cdf940624a41e3f43b5486a5c4e313a01f39cef13808
scope.1.id=ZmllbGQ6Q29sb3VyU3RyZWV0I0hPVVNFU19QRVJfSE9URUw6MjE
scope.1.kind=field
scope.1.startLine=21
scope.1.endLine=21
scope.1.semanticHash=42e94d005be740f3b88a5bdce85bf82056e5d13ad4672c310f73348f16815675
scope.2.id=ZmllbGQ6Q29sb3VyU3RyZWV0I2NvbG91ckdyb3VwOjEz
scope.2.kind=field
scope.2.startLine=13
scope.2.endLine=13
scope.2.semanticHash=61129927bc25cefc34cda3d4288074ab37128a3fc00d7f064157e15bedecc47b
scope.3.id=ZmllbGQ6Q29sb3VyU3RyZWV0I2NvbnN0cnVjdGlvbkNvc3Q6MTc
scope.3.kind=field
scope.3.startLine=17
scope.3.endLine=17
scope.3.semanticHash=40cbade023425ac520a67a9cbdd66d33628d689f0546d71e44799aa2bfb65a20
scope.4.id=ZmllbGQ6Q29sb3VyU3RyZWV0I2xhbmRNb3J0Z2FnZVZhbHVlOjE4
scope.4.kind=field
scope.4.startLine=18
scope.4.endLine=18
scope.4.semanticHash=92eaca18c83cc8c3fe6112f09d3f9f4bbf6cfde3037761d5c328fc4e61d1265a
scope.5.id=ZmllbGQ6Q29sb3VyU3RyZWV0I3ByaWNlOjE0
scope.5.kind=field
scope.5.startLine=14
scope.5.endLine=14
scope.5.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.6.id=ZmllbGQ6Q29sb3VyU3RyZWV0I3JlbnRCeUhvdXNlczoxNQ
scope.6.kind=field
scope.6.startLine=15
scope.6.endLine=15
scope.6.semanticHash=28f839c8ad002045c912ec7c31fbab7ed711036622efd1c56b1cd0f1de64fa70
scope.7.id=ZmllbGQ6Q29sb3VyU3RyZWV0I3JlbnRGb3JPbmVIb3RlbDoxNg
scope.7.kind=field
scope.7.startLine=16
scope.7.endLine=16
scope.7.semanticHash=6c84335d57df781c7134af7d93760f09981235dc184f11c069c82a0bd415629d
scope.8.id=ZmllbGQ6Q29sb3VyU3RyZWV0I3R5cGU6MTI
scope.8.kind=field
scope.8.startLine=12
scope.8.endLine=12
scope.8.semanticHash=578fb8351c3bd9fec9344a1ba176367ac2a41a0f427b28efd82181214901570e
scope.9.id=bWV0aG9kOkNvbG91clN0cmVldCNjdG9yKDcpOjEx
scope.9.kind=method
scope.9.startLine=1
scope.9.endLine=85
scope.9.semanticHash=872c89b9292e13024b99ad22e908314f60761e15e1c55959b80a40f71331e65c
scope.10.id=bWV0aG9kOkNvbG91clN0cmVldCNob3RlbENvbnN0cnVjdGlvbkNvc3QoMCk6NDg
scope.10.kind=method
scope.10.startLine=48
scope.10.endLine=50
scope.10.semanticHash=e64b9d263f392334625c45fb74b21be412f7898c77056c66dafc50bc5a996bad
scope.11.id=bWV0aG9kOkNvbG91clN0cmVldCNob3RlbENvbnN0cnVjdGlvblJlcXVpcmVzTnVtYmVyT2ZIb3VzZXMoMCk6NTI
scope.11.kind=method
scope.11.startLine=52
scope.11.endLine=54
scope.11.semanticHash=3975f27ea78e556a355540cc514fda75279365ee9769d1c407fa1d8e1c354079
scope.12.id=bWV0aG9kOkNvbG91clN0cmVldCNob3VzZUNvbnN0cnVjdGlvbkNvc3QoMCk6NDQ
scope.12.kind=method
scope.12.startLine=44
scope.12.endLine=46
scope.12.semanticHash=bb4eec778118270df437e210719eb849dd413742c595e872ed6d9f4d91cb82b7
scope.13.id=bWV0aG9kOkNvbG91clN0cmVldCNraW5kKDApOjIz
scope.13.kind=method
scope.13.startLine=23
scope.13.endLine=26
scope.13.semanticHash=8e22cf096097028d41f2e8aca749073a40e76cea0711d9e3e921f317f92c7e7a
scope.14.id=bWV0aG9kOkNvbG91clN0cmVldCNvZigxMCk6NTY
scope.14.kind=method
scope.14.startLine=56
scope.14.endLine=84
scope.14.semanticHash=3023d1c8702395802dbb21f0ccefdcaf263a8feb77d53482408f607840911f35
scope.15.id=bWV0aG9kOkNvbG91clN0cmVldCNyZW50Rm9ySG91c2VzKDEpOjMy
scope.15.kind=method
scope.15.startLine=32
scope.15.endLine=34
scope.15.semanticHash=1081e2188158c4ec37fef10e826b84c9c60911052f9ca2ec2978724aef41c9a9
scope.16.id=bWV0aG9kOkNvbG91clN0cmVldCN2YWNhbnRSZW50KDApOjM2
scope.16.kind=method
scope.16.startLine=36
scope.16.endLine=38
scope.16.semanticHash=2a75c5ce8e832ab982d2fe669e8b1f0e689fc08185599bed0bc32e29d8a4746b
*/
