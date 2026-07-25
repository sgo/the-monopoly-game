package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

import java.util.List;

/**
 * A utility cannot be built on. Its rent is the dice roll multiplied by a
 * factor that depends on how many of the two utilities its owner holds.
 */
public record Utility(Street.Type type) implements Ownable {
  private static final Money PRICE = new Money(150);
  private static final Money LAND_MORTGAGE_VALUE = new Money(75);
  private static final List<Integer> DICE_MULTIPLIER_BY_OWNED_COUNT = List.of(0, 4, 10);

  @Override
  public Street.Kind kind() {
    return Street.Kind.utility;
  }

  @Override
  public Money price() {
    return PRICE;
  }

  @Override
  public Money landMortgageValue() {
    return LAND_MORTGAGE_VALUE;
  }

  /**
   * The factor applied to the dice roll on this utility, which depends on how
   * many utilities its owner holds.
   */
  public int rentDiceMultiplierForOwning(int utilities) {
    return DICE_MULTIPLIER_BY_OWNED_COUNT.get(
        OwnedCount.checked(utilities, DICE_MULTIPLIER_BY_OWNED_COUNT, type, "utilities")
    );
  }

  static Street.Factory factory() {
    return (type, activatedRules) -> new Utility(type);
  }
}

/* mutate4java-manifest
version=1
moduleHash=d19bbee71f4aca472494630de3f8722195d6b9ff7e3ff339e4adb1b5ebf2e3e0
scope.0.id=Y2xhc3M6VXRpbGl0eSNVdGlsaXR5OjEx
scope.0.kind=class
scope.0.startLine=11
scope.0.endLine=44
scope.0.semanticHash=d92148f7ceed16c222341869c9b82a8166aa23cd838be4a44be3add2f338cf72
scope.1.id=ZmllbGQ6VXRpbGl0eSNESUNFX01VTFRJUExJRVJfQllfT1dORURfQ09VTlQ6MTQ
scope.1.kind=field
scope.1.startLine=14
scope.1.endLine=14
scope.1.semanticHash=7869b39400194b89f234910d841b235fc8b2a9f75f01d14172f2852d46c5f063
scope.2.id=ZmllbGQ6VXRpbGl0eSNMQU5EX01PUlRHQUdFX1ZBTFVFOjEz
scope.2.kind=field
scope.2.startLine=13
scope.2.endLine=13
scope.2.semanticHash=dcd9ff0024425c902e5f45fc46b65348a762a4b1320aba399f52f3ac58d03a5d
scope.3.id=ZmllbGQ6VXRpbGl0eSNQUklDRToxMg
scope.3.kind=field
scope.3.startLine=12
scope.3.endLine=12
scope.3.semanticHash=f388b23e1e35436438f21d15ecef7d20373f2bfce55dfdb8e0cba3fa7a14c154
scope.4.id=ZmllbGQ6VXRpbGl0eSN0eXBlOjEx
scope.4.kind=field
scope.4.startLine=11
scope.4.endLine=11
scope.4.semanticHash=578fb8351c3bd9fec9344a1ba176367ac2a41a0f427b28efd82181214901570e
scope.5.id=bWV0aG9kOlV0aWxpdHkjY3RvcigxKToxMQ
scope.5.kind=method
scope.5.startLine=1
scope.5.endLine=44
scope.5.semanticHash=d247fbd260a193f13db84d7428eb7b52e5508aa184dc35e05f8cb5e348d6abfb
scope.6.id=bWV0aG9kOlV0aWxpdHkjZmFjdG9yeSgwKTo0MQ
scope.6.kind=method
scope.6.startLine=41
scope.6.endLine=43
scope.6.semanticHash=f25ad9798a01199a6a4c1535eb5845467f39bfdbcc9a169590ddc118357c3058
scope.7.id=bWV0aG9kOlV0aWxpdHkja2luZCgwKToxNg
scope.7.kind=method
scope.7.startLine=16
scope.7.endLine=19
scope.7.semanticHash=6e5f968995e33b34b820b3682c9b3489363aece3f457b7264fd99b303045e070
scope.8.id=bWV0aG9kOlV0aWxpdHkjbGFuZE1vcnRnYWdlVmFsdWUoMCk6MjY
scope.8.kind=method
scope.8.startLine=26
scope.8.endLine=29
scope.8.semanticHash=b5bd6c70411988569b2806a4f95e042d7014482a5d29550897222d8d1fb5e547
scope.9.id=bWV0aG9kOlV0aWxpdHkjcHJpY2UoMCk6MjE
scope.9.kind=method
scope.9.startLine=21
scope.9.endLine=24
scope.9.semanticHash=a02532204b133cd9a056a8b312e7027afd14e006065edbd89b2998e7bf575b9a
scope.10.id=bWV0aG9kOlV0aWxpdHkjcmVudERpY2VNdWx0aXBsaWVyRm9yT3duaW5nKDEpOjM1
scope.10.kind=method
scope.10.startLine=35
scope.10.endLine=39
scope.10.semanticHash=9645de86dd0e7186d7a33e72d38633b10aeaae48b32cfdcdb7675a3b46225d6c
*/
