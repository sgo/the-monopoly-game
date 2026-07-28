package the.monopoly.game.rules;

import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.streets.Street;

/**
 * What happens where a pawn stops. A turn moves the pawn and then hands the
 * space over, so what a space is worth stays out of the moving of pawns.
 */
@FunctionalInterface
public interface Landings {
  /** A board where stopping anywhere is worth nothing. */
  Landings UNEVENTFUL = (player, space) -> {
  };

  void resolve(Player player, Street space);

  default void resolve(Player player, Street space, Roll roll) {
    resolve(player, space);
  }
}

/* mutate4java-manifest
version=1
moduleHash=068d6efdec3106cf9f1463cd80f1f4a5d2bdf1a459efff370fd48b99ca8976bd
scope.0.id=Y2xhc3M6TGFuZGluZ3MjTGFuZGluZ3M6MTA
scope.0.kind=class
scope.0.startLine=10
scope.0.endLine=17
scope.0.semanticHash=c32d286f7cf0dda0a2317724877331ae20264b65a02bc349d0beb52fe410df0b
scope.1.id=ZmllbGQ6TGFuZGluZ3MjVU5FVkVOVEZVTDoxMw
scope.1.kind=field
scope.1.startLine=13
scope.1.endLine=14
scope.1.semanticHash=74a4a7925157d6fc31d1c44069f43e3acbb81fbda43d23b9415e411d72015f9c
scope.2.id=bWV0aG9kOkxhbmRpbmdzI3Jlc29sdmUoMik6MTY
scope.2.kind=method
scope.2.startLine=16
scope.2.endLine=16
scope.2.semanticHash=f89019548848333b09bd9007aec62a12915e72c3ed4f76435ff6733e6e3d67f8
*/
