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

  /** Whether this amount is enough to pay that one. */
  public boolean covers(Money price) {
    return amount >= price.amount;
  }

  /** Whether this amount is more than that one, as one bid beats another. */
  public boolean exceeds(Money other) {
    return amount > other.amount;
  }
}

/* mutate4java-manifest
version=1
moduleHash=30a58b626ab1f1add55ffa8473d9000fea76b6d48124c69912cec6cd42fe2a90
scope.0.id=Y2xhc3M6TW9uZXkjTW9uZXk6Nw
scope.0.kind=class
scope.0.startLine=7
scope.0.endLine=27
scope.0.semanticHash=8c556c810671b8798160b28eeae7e7c16c24000304893082d376049b76005c1f
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
scope.3.id=bWV0aG9kOk1vbmV5I2NvdmVycygxKToxOQ
scope.3.kind=method
scope.3.startLine=19
scope.3.endLine=21
scope.3.semanticHash=65d9eafc116054e38536bc8ee1804b2dec0cd2874ce1aae2d24abd0dfca9631e
scope.4.id=bWV0aG9kOk1vbmV5I2N0b3IoMSk6Nw
scope.4.kind=method
scope.4.startLine=1
scope.4.endLine=27
scope.4.semanticHash=4e4e9aa6712e8a804c0bae7b675f3a3d8f25511e01599b3ff583b33be5dcb936
scope.5.id=bWV0aG9kOk1vbmV5I2V4Y2VlZHMoMSk6MjQ
scope.5.kind=method
scope.5.startLine=24
scope.5.endLine=26
scope.5.semanticHash=c2cae08dfbc603a58fee951a6527a60865e59fc137ed10d5a06bb2c827a80424
scope.6.id=bWV0aG9kOk1vbmV5I21pbnVzKDEpOjE0
scope.6.kind=method
scope.6.startLine=14
scope.6.endLine=16
scope.6.semanticHash=cbe14f084c86fe74a49e62a5d1f047fc4829a70e897f7b9fac309e8b70d01572
scope.7.id=bWV0aG9kOk1vbmV5I3BsdXMoMSk6MTA
scope.7.kind=method
scope.7.startLine=10
scope.7.endLine=12
scope.7.semanticHash=a6f97af5ee51e7f21deab9056e4ee980f4f65f2dc8e7a9475192834ac62e0a3b
*/
