package the.monopoly.game.components.streets;

/**
 * A space nobody can buy: chance, community chest, jail, free parking and go to
 * jail. What landing on one does is decided by the turn resolution, not by the
 * space itself.
 */
public record UnownableSpace(Street.Type type, Street.Kind kind) implements Street {
  static Street.Factory of(Street.Kind kind) {
    return (type, activatedRules) -> new UnownableSpace(type, kind);
  }
}

/* mutate4java-manifest
version=1
moduleHash=701a59f9cc2349a930d5a42c8b468c38289e9d905408f7434a4f201d48ded930
scope.0.id=Y2xhc3M6VW5vd25hYmxlU3BhY2UjVW5vd25hYmxlU3BhY2U6OA
scope.0.kind=class
scope.0.startLine=8
scope.0.endLine=12
scope.0.semanticHash=5c5d160b9a0d5912d7463856acd801032064f357ae3275c015c52c3348030edc
scope.1.id=ZmllbGQ6VW5vd25hYmxlU3BhY2Uja2luZDo4
scope.1.kind=field
scope.1.startLine=8
scope.1.endLine=8
scope.1.semanticHash=277dcb0a35560fb0e3b8deb598e0243e2a4992c69c83eec9f9baa0eaf554e8d9
scope.2.id=ZmllbGQ6VW5vd25hYmxlU3BhY2UjdHlwZTo4
scope.2.kind=field
scope.2.startLine=8
scope.2.endLine=8
scope.2.semanticHash=578fb8351c3bd9fec9344a1ba176367ac2a41a0f427b28efd82181214901570e
scope.3.id=bWV0aG9kOlVub3duYWJsZVNwYWNlI2N0b3IoMik6OA
scope.3.kind=method
scope.3.startLine=1
scope.3.endLine=12
scope.3.semanticHash=addee833390446036aaf0ac6eefced66f283adbb313084509fe53784a0896648
scope.4.id=bWV0aG9kOlVub3duYWJsZVNwYWNlI29mKDEpOjk
scope.4.kind=method
scope.4.startLine=9
scope.4.endLine=11
scope.4.semanticHash=d3962d3a6e9c40b3a10bf886dd9c6bce8abce7a369a439709b4729140c8696ba
*/
