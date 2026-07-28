package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

import java.util.List;

import static the.monopoly.game.components.finance.Money.ZERO;

/**
 * A station cannot be built on. Its rent depends on how many of the four
 * stations its owner holds, doubling with each additional one.
 */
public record Station(Street.Type type) implements Ownable {
  private static final Money PRICE = new Money(200);
  private static final Money LAND_MORTGAGE_VALUE = new Money(100);
  private static final List<Money> RENT_BY_OWNED_COUNT = List.of(
      ZERO,
      new Money(25),
      new Money(50),
      new Money(100),
      new Money(200)
  );

  @Override
  public Street.Kind kind() {
    return Street.Kind.station;
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
   * Rent owed on this station, which depends on how many stations its owner
   * holds.
   */
  public Money rentForOwning(int stations) {
    return RENT_BY_OWNED_COUNT.get(
        OwnedCount.checked(stations, RENT_BY_OWNED_COUNT, type, "stations")
    );
  }

  static Street.Factory factory() {
    return (type, activatedRules) -> new Station(type);
  }
}

/* mutate4java-manifest
version=1
moduleHash=dddc624fdd844110c54a132393ce900b97b2287ea34d29623f8f8a8f9b859347
scope.0.id=Y2xhc3M6U3RhdGlvbiNTdGF0aW9uOjEz
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=52
scope.0.semanticHash=bec11e8cec17ddb26139b9ab9597f9cd45014f0509d3006a05a76a24df817114
scope.1.id=ZmllbGQ6U3RhdGlvbiNMQU5EX01PUlRHQUdFX1ZBTFVFOjE1
scope.1.kind=field
scope.1.startLine=15
scope.1.endLine=15
scope.1.semanticHash=9c2281f7618f7323210170248ce209379d60f137d7d95bbba74e37804bd04434
scope.2.id=ZmllbGQ6U3RhdGlvbiNQUklDRToxNA
scope.2.kind=field
scope.2.startLine=14
scope.2.endLine=14
scope.2.semanticHash=dfdb583d19f3aeec0a9828d1bea6901f84b58a60706b886dec936c26cf23725e
scope.3.id=ZmllbGQ6U3RhdGlvbiNSRU5UX0JZX09XTkVEX0NPVU5UOjE2
scope.3.kind=field
scope.3.startLine=16
scope.3.endLine=22
scope.3.semanticHash=7b73389887ad63b750e0dc192d9a71dc953643a8015e4ccc54f8e2c753385ecb
scope.4.id=ZmllbGQ6U3RhdGlvbiN0eXBlOjEz
scope.4.kind=field
scope.4.startLine=13
scope.4.endLine=13
scope.4.semanticHash=578fb8351c3bd9fec9344a1ba176367ac2a41a0f427b28efd82181214901570e
scope.5.id=bWV0aG9kOlN0YXRpb24jY3RvcigxKToxMw
scope.5.kind=method
scope.5.startLine=1
scope.5.endLine=52
scope.5.semanticHash=63ee6ced8305931c3ae8d647e3dd8cb57261c18c0e3385fbe63b1737e85c4a92
scope.6.id=bWV0aG9kOlN0YXRpb24jZmFjdG9yeSgwKTo0OQ
scope.6.kind=method
scope.6.startLine=49
scope.6.endLine=51
scope.6.semanticHash=5138d83bee78630083868f1598b8a10661e094e4df08fa783ffb4ee154d8ef71
scope.7.id=bWV0aG9kOlN0YXRpb24ja2luZCgwKToyNA
scope.7.kind=method
scope.7.startLine=24
scope.7.endLine=27
scope.7.semanticHash=7b218da6b02d07f6981804f8e30919766b9ff331c50747a60c9b7fc0ae2dad8a
scope.8.id=bWV0aG9kOlN0YXRpb24jbGFuZE1vcnRnYWdlVmFsdWUoMCk6MzQ
scope.8.kind=method
scope.8.startLine=34
scope.8.endLine=37
scope.8.semanticHash=b5bd6c70411988569b2806a4f95e042d7014482a5d29550897222d8d1fb5e547
scope.9.id=bWV0aG9kOlN0YXRpb24jcHJpY2UoMCk6Mjk
scope.9.kind=method
scope.9.startLine=29
scope.9.endLine=32
scope.9.semanticHash=a02532204b133cd9a056a8b312e7027afd14e006065edbd89b2998e7bf575b9a
scope.10.id=bWV0aG9kOlN0YXRpb24jcmVudEZvck93bmluZygxKTo0Mw
scope.10.kind=method
scope.10.startLine=43
scope.10.endLine=47
scope.10.semanticHash=d4806a38e0bf2f14363a5697625fa6b9b9a4b5eaf09d0a17aa7009d6f209dfb5
*/
