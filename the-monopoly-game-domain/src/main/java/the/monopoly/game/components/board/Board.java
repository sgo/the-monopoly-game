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

  /**
   * Where a space sits, counting from Start. Asked here rather than by reading
   * the layout, so that a board which has no such space answers for itself
   * instead of every caller finding the same -1 and deciding again what it
   * means.
   */
  public int positionOf(Street.Type space) {
    int at = layout.indexOf(space);
    if (at < 0) throw new IllegalStateException("This board has no " + space + " space.");
    return at;
  }
}

/* mutate4java-manifest
version=1
moduleHash=cd8b7d71ca67b1205f0e00619f6f13b740b91a0c42f13727d4f5ac2cd9c1bb7b
scope.0.id=Y2xhc3M6Qm9hcmQjQm9hcmQ6MTM
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=37
scope.0.semanticHash=9d6e0a3e5392244a1a2dce07c6d18c3c8685baafd9cf687a8f81f7c3375129e3
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
scope.3.id=bWV0aG9kOkJvYXJkI3Bvc2l0aW9uT2YoMSk6MzI
scope.3.kind=method
scope.3.startLine=32
scope.3.endLine=36
scope.3.semanticHash=86ee469c3ee529b79936e2631d3527e5f9d896c1668bb7d90d1c458d39f083c3
scope.4.id=bWV0aG9kOkJvYXJkI3NpemUoMCk6MjI
scope.4.kind=method
scope.4.startLine=22
scope.4.endLine=24
scope.4.semanticHash=6bcba7357691a853cbdc3216831e5de33a4ace56e0369ba5cced33ae2911086e
scope.5.id=bWV0aG9kOkJvYXJkI3NwYWNlcygwKToxOA
scope.5.kind=method
scope.5.startLine=18
scope.5.endLine=20
scope.5.semanticHash=e72a3cf5fd9bbc5bae7848acb09e9043aa0c432e19bda2f36e962d8597d64318
*/
