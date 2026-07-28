package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

/**
 * What happens where a pawn stops. A turn moves the pawn and then hands the
 * space over, so what a space is worth stays out of the moving of pawns.
 */
@FunctionalInterface
public interface Landings {
  /** A board where stopping anywhere is worth nothing. */
  Landings UNEVENTFUL = (player, space, roll) -> {
  };

  void resolve(Player player, Street space, Roll roll);
}

/* mutate4java-manifest
version=1
moduleHash=631fcba298819900bbe9e3811087506a0efe03289b20554a2a92a87170f268c7
scope.0.id=Y2xhc3M6TGFuZGluZ3MjTGFuZGluZ3M6MTE
scope.0.kind=class
scope.0.startLine=11
scope.0.endLine=18
scope.0.semanticHash=23fb19ba2b08217e35903535c5752485fc7d9c3b9bec6935ddc6468e8af9c359
scope.1.id=ZmllbGQ6TGFuZGluZ3MjVU5FVkVOVEZVTDoxNA
scope.1.kind=field
scope.1.startLine=14
scope.1.endLine=15
scope.1.semanticHash=ae3aae0a53e9727714965715254844620fcbb3a0a82714f8b226644922a8fb20
scope.2.id=bWV0aG9kOkxhbmRpbmdzI3Jlc29sdmUoMyk6MTc
scope.2.kind=method
scope.2.startLine=17
scope.2.endLine=17
scope.2.semanticHash=b46fc30be8cf8b2a41b4e7a556dbae8e4a15519feccd62ece6ea6cf2ea920cc3
*/
