package the.monopoly.game.components.board;

import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.stream.Stream;

/**
 * The order of the spaces on the board. A board records which space sits where
 * and nothing else; turning a space into a playable {@link Street} depends on
 * the rules in force, so that is the rule set's job.
 */
public record Board(List<Street.Type> layout) {
  public Board(List<Street.Type> layout) {
    this.layout = List.copyOf(layout);
  }

  public Stream<Street.Type> spaces() {
    return layout.stream();
  }

  public int size() {
    return layout.size();
  }
}

/* mutate4java-manifest
version=1
moduleHash=758f6e7802efadde9206640cb30a6cee96f91388b4e1318aa6b5b65892ee6826
scope.0.id=Y2xhc3M6Qm9hcmQjQm9hcmQ6MTM
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=25
scope.0.semanticHash=6f5db6069d8e9547daff38c812462b582fafef2ac903f20875346937c516b6fb
scope.1.id=ZmllbGQ6Qm9hcmQjbGF5b3V0OjEz
scope.1.kind=field
scope.1.startLine=13
scope.1.endLine=13
scope.1.semanticHash=402c58ede059c525a08120bfbd6d79bd1e970d764695c47401a3097ecc2671e4
scope.2.id=bWV0aG9kOkJvYXJkI2N0b3IoMSk6MTQ
scope.2.kind=method
scope.2.startLine=14
scope.2.endLine=16
scope.2.semanticHash=e4c1134681cd3b34d110531a5da35a95049e4664b62cb47e598b247bb57e6ef3
scope.3.id=bWV0aG9kOkJvYXJkI3NpemUoMCk6MjI
scope.3.kind=method
scope.3.startLine=22
scope.3.endLine=24
scope.3.semanticHash=6bcba7357691a853cbdc3216831e5de33a4ace56e0369ba5cced33ae2911086e
scope.4.id=bWV0aG9kOkJvYXJkI3NwYWNlcygwKToxOA
scope.4.kind=method
scope.4.startLine=18
scope.4.endLine=20
scope.4.semanticHash=e72a3cf5fd9bbc5bae7848acb09e9043aa0c432e19bda2f36e962d8597d64318
*/
