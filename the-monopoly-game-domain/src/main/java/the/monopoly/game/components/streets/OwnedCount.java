package the.monopoly.game.components.streets;

import java.util.List;

/**
 * Several spaces look a figure up in a table indexed by a count: how many
 * stations an owner holds, how many houses stand on a street. The table decides
 * which counts exist, so the bounds check belongs next to the lookup.
 */
final class OwnedCount {
  private OwnedCount() {
  }

  static int checked(int count, List<?> valuesByCount, Street.Type type, String counted) {
    if (count < 0 || count >= valuesByCount.size())
      throw new IllegalArgumentException(
          "Cannot have " + count + " " + counted + " on " + type
              + "; the board allows at most " + (valuesByCount.size() - 1) + "."
      );
    return count;
  }
}

/* mutate4java-manifest
version=1
moduleHash=2c7e08fc8971ffec7c99f2f17bb44663e635e1fd18b5588a25ca170d1208b26f
scope.0.id=Y2xhc3M6T3duZWRDb3VudCNPd25lZENvdW50OjEw
scope.0.kind=class
scope.0.startLine=10
scope.0.endLine=22
scope.0.semanticHash=fc2fc74065a444f62005ccd9686e10d7ad51b4721901cc32d9458d24ad0ea793
scope.1.id=bWV0aG9kOk93bmVkQ291bnQjY2hlY2tlZCg0KToxNA
scope.1.kind=method
scope.1.startLine=14
scope.1.endLine=21
scope.1.semanticHash=eafea14e0b54d5fc34069dabea5ab407104e860d22ba2a9003cce9b932dd244a
scope.2.id=bWV0aG9kOk93bmVkQ291bnQjY3RvcigwKToxMQ
scope.2.kind=method
scope.2.startLine=11
scope.2.endLine=12
scope.2.semanticHash=174a7ab6f6ce65e67ea42bae67a64e0f05d8d3cc079ffe5bf868dc581bcc3096
*/
