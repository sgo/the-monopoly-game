package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

/**
 * A space that charges whoever lands on it a fixed amount. Nobody can buy it.
 */
public record TaxSpace(Street.Type type, Money tax) implements Street {
  @Override
  public Street.Kind kind() {
    return Street.Kind.tax;
  }

  static Street.Factory of(int tax) {
    Money amount = new Money(tax);
    return (type, activatedRules) -> new TaxSpace(type, amount);
  }
}

/* mutate4java-manifest
version=1
moduleHash=8f9710335b42523273d5993e293738effbb1b97c71c1c69abf310e3c11df6afc
scope.0.id=Y2xhc3M6VGF4U3BhY2UjVGF4U3BhY2U6OA
scope.0.kind=class
scope.0.startLine=8
scope.0.endLine=18
scope.0.semanticHash=46f1a5302e558e16c26fda9e25a80c686f4e5153a6c77cd2ac230cea1157990e
scope.1.id=ZmllbGQ6VGF4U3BhY2UjdGF4Ojg
scope.1.kind=field
scope.1.startLine=8
scope.1.endLine=8
scope.1.semanticHash=0fcc83e2555365e2589972de68e34ff8618c4ec1f4db6b4d93f35dee6d44cd82
scope.2.id=ZmllbGQ6VGF4U3BhY2UjdHlwZTo4
scope.2.kind=field
scope.2.startLine=8
scope.2.endLine=8
scope.2.semanticHash=578fb8351c3bd9fec9344a1ba176367ac2a41a0f427b28efd82181214901570e
scope.3.id=bWV0aG9kOlRheFNwYWNlI2N0b3IoMik6OA
scope.3.kind=method
scope.3.startLine=1
scope.3.endLine=18
scope.3.semanticHash=ef19298e665aea8bdb93f20b5f5f0ebd3998de8cb21551433aaa519dc3cba664
scope.4.id=bWV0aG9kOlRheFNwYWNlI2tpbmQoMCk6OQ
scope.4.kind=method
scope.4.startLine=9
scope.4.endLine=12
scope.4.semanticHash=2fde1b2f00df50223d499f66237d9f69fed2c70b90813d59c113b4238d3fe4cb
scope.5.id=bWV0aG9kOlRheFNwYWNlI29mKDEpOjE0
scope.5.kind=method
scope.5.startLine=14
scope.5.endLine=17
scope.5.semanticHash=a822b50b9954c62f1f949e5927078fc9a2bea3ddb6eaacea59920dab278a6944
*/
