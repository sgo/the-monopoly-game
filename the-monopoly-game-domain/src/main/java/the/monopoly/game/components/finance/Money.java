package the.monopoly.game.components.finance;

/**
 * An amount of game money. A plain value: it knows how to combine with other
 * amounts and nothing about where an amount came from.
 */
public record Money(int amount) {
  public static final Money ZERO = new Money(0);

  public Money plus(Money money) {
    return new Money(amount + money.amount());
  }

  public Money minus(Money money) {
    return new Money(amount - money.amount);
  }
}

/* mutate4java-manifest
version=1
moduleHash=6e96dbb71ffeeab36f0ee3d452d4ae0a8cfe4b097b01ad2f274ba3c9c3fd1749
scope.0.id=Y2xhc3M6TW9uZXkjTW9uZXk6Nw
scope.0.kind=class
scope.0.startLine=7
scope.0.endLine=17
scope.0.semanticHash=bc3ffb54204ad8de6aec754117dc4d3faaf10917a640553430d831722e0fb449
scope.1.id=ZmllbGQ6TW9uZXkjWkVSTzo4
scope.1.kind=field
scope.1.startLine=8
scope.1.endLine=8
scope.1.semanticHash=1de769763f6565ca427eefb0b3c4fa8cd40bb55d0424876d708d5a9b25742b25
scope.2.id=ZmllbGQ6TW9uZXkjYW1vdW50Ojc
scope.2.kind=field
scope.2.startLine=7
scope.2.endLine=7
scope.2.semanticHash=bb3bf5a90acc9f17565663cfbebe1fad1792f862adeb064c7a32707d19ce85fb
scope.3.id=bWV0aG9kOk1vbmV5I2N0b3IoMSk6Nw
scope.3.kind=method
scope.3.startLine=1
scope.3.endLine=17
scope.3.semanticHash=758105f211577d60ca44fd97676f1e5f7135295f9d3c0da9d280e21b2c94b5d9
scope.4.id=bWV0aG9kOk1vbmV5I21pbnVzKDEpOjE0
scope.4.kind=method
scope.4.startLine=14
scope.4.endLine=16
scope.4.semanticHash=cbe14f084c86fe74a49e62a5d1f047fc4829a70e897f7b9fac309e8b70d01572
scope.5.id=bWV0aG9kOk1vbmV5I3BsdXMoMSk6MTA
scope.5.kind=method
scope.5.startLine=10
scope.5.endLine=12
scope.5.semanticHash=a6f97af5ee51e7f21deab9056e4ee980f4f65f2dc8e7a9475192834ac62e0a3b
*/
