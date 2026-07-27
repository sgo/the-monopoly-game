package the.monopoly.game.components.dice;

/**
 * What a player throws in one go: both dice, kept apart because the rules care
 * whether they came up the same.
 */
public record Roll(int die1, int die2) {
  public int total() {
    return die1 + die2;
  }

  public boolean isDouble() {
    return die1 == die2;
  }
}

/* mutate4java-manifest
version=1
moduleHash=0a955223fcc629d33f33e7c6e9166104456819ef6213d0ad49447650f782e134
scope.0.id=Y2xhc3M6Um9sbCNSb2xsOjc
scope.0.kind=class
scope.0.startLine=7
scope.0.endLine=15
scope.0.semanticHash=34e15d195152cbc1c006ad446c2dcd1452d542f4bad9d5d4d6e29a60881d964b
scope.1.id=ZmllbGQ6Um9sbCNkaWUxOjc
scope.1.kind=field
scope.1.startLine=7
scope.1.endLine=7
scope.1.semanticHash=6ddc234edb24264f777d33cee63c2596ecb64d7e93f4708db093e290f37a1a04
scope.2.id=ZmllbGQ6Um9sbCNkaWUyOjc
scope.2.kind=field
scope.2.startLine=7
scope.2.endLine=7
scope.2.semanticHash=66e5f616669c6ecdb652ba323864597281f8104cf48998bb45825a24747973fd
scope.3.id=bWV0aG9kOlJvbGwjY3RvcigyKTo3
scope.3.kind=method
scope.3.startLine=1
scope.3.endLine=15
scope.3.semanticHash=3db66c21975b4b99fae0cbe0e8f7e13d22343f29aad6cc694c1a92fb0e20d32c
scope.4.id=bWV0aG9kOlJvbGwjaXNEb3VibGUoMCk6MTI
scope.4.kind=method
scope.4.startLine=12
scope.4.endLine=14
scope.4.semanticHash=e3f9627bc1399eb6527cd8aba8bb2ff3543bafa49640f6bac6e378a63297eefa
scope.5.id=bWV0aG9kOlJvbGwjdG90YWwoMCk6OA
scope.5.kind=method
scope.5.startLine=8
scope.5.endLine=10
scope.5.semanticHash=0f089b03e7ad0de5018b6c59ec6e1ed4acc1907ba9158ed82f6102f2244c30ea
*/
