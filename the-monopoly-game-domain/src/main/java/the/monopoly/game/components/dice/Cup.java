package the.monopoly.game.components.dice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Where a roll comes from.
 * <p>
 * The rules never reach for a die directly, so replacing chance with a known
 * sequence of rolls is the one thing needed to play a game that repeats.
 */
@FunctionalInterface
public interface Cup {
  Roll roll();

  /** A cup of real dice, which needs exactly the two the rules are played with. */
  static Cup of(List<Dice> dice) {
    if (dice.size() != 2)
      throw new IllegalArgumentException("A roll is made with 2 dice, not " + dice.size() + ".");
    return () -> new Roll(faceOf(dice.get(0)), faceOf(dice.get(1)));
  }

  static Cup of(Dice first, Dice second) {
    return of(List.of(first, second));
  }

  /** A cup that yields exactly these rolls, for replaying a known game. */
  static Cup of(Roll... rolls) {
    Deque<Roll> remaining = new ArrayDeque<>(List.of(rolls));
    return () -> {
      if (remaining.isEmpty())
        throw new IllegalStateException("This cup has no more rolls left to give.");
      return remaining.removeFirst();
    };
  }

  private static int faceOf(Dice dice) {
    return Integer.parseInt(dice.roll().symbol());
  }
}

/* mutate4java-manifest
version=1
moduleHash=85b8bb809734d3480dee984aa902386acb9cf385d699d4858f10231c54820860
scope.0.id=Y2xhc3M6Q3VwI0N1cDoxMw
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=41
scope.0.semanticHash=5176921003f808e3def0fa41a58f4899c8cd051ae66bb23e8cf95568121f5a78
scope.1.id=bWV0aG9kOkN1cCNmYWNlT2YoMSk6Mzg
scope.1.kind=method
scope.1.startLine=38
scope.1.endLine=40
scope.1.semanticHash=58a0a64a2d1b12481f3bd4afc19ef2cd4342dcf27ecec08bf804fc322781a0b5
scope.2.id=bWV0aG9kOkN1cCNvZigxKToxOA
scope.2.kind=method
scope.2.startLine=18
scope.2.endLine=22
scope.2.semanticHash=2616138e02960a0ca7c57a3d255d9b60e2d005b6325f5331fce54e25a922d3c5
scope.3.id=bWV0aG9kOkN1cCNvZigxKToyOQ
scope.3.kind=method
scope.3.startLine=29
scope.3.endLine=36
scope.3.semanticHash=78b22ec53ae8703ea0292eff0a6b6a7bf926b8b8b5a3146f6baf160b4e8bd8f8
scope.4.id=bWV0aG9kOkN1cCNvZigyKToyNA
scope.4.kind=method
scope.4.startLine=24
scope.4.endLine=26
scope.4.semanticHash=f3f216da77a6a53d8a519a857ae46b96ec6abb7e829dc200e9d29250221ef4f9
scope.5.id=bWV0aG9kOkN1cCNyb2xsKDApOjE1
scope.5.kind=method
scope.5.startLine=15
scope.5.endLine=15
scope.5.semanticHash=a9e7409011d340ef95224d47126d35b503494f39ec2d1ee77b4d0650b7adb15e
*/
