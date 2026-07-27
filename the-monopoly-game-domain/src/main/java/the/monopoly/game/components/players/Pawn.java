package the.monopoly.game.components.players;

/**
 * The pieces players are represented by. A player is identified by the pawn
 * they play, so the pawn's name is the player's identifier.
 */
public enum Pawn {
  dog,
  high_hat,
  iron_box,
  racecar,
  ship,
  shoe,
  thimble,
  wheelbarrow;

  /** The name the rules use, which spells multi-word pawns with a space. */
  public String pawnName() {
    return name().replace('_', ' ');
  }

  public Player.ID id() {
    return new Player.ID(pawnName());
  }
}

/* mutate4java-manifest
version=1
moduleHash=12f340fd1fff5284d5d53263b0ec510a910b38df8580cf1143b69606703bbcce
scope.0.id=Y2xhc3M6UGF3biNQYXduOjc
scope.0.kind=class
scope.0.startLine=7
scope.0.endLine=25
scope.0.semanticHash=903b41a8f0cfeaf2c1aba3cdf4134d06871b31077556becd18cca55be8cfd75c
scope.1.id=ZmllbGQ6UGF3biNkb2c6OA
scope.1.kind=field
scope.1.startLine=8
scope.1.endLine=8
scope.1.semanticHash=cd6357efdd966de8c0cb2f876cc89ec74ce35f0968e11743987084bd42fb8944
scope.2.id=ZmllbGQ6UGF3biNoaWdoX2hhdDo5
scope.2.kind=field
scope.2.startLine=9
scope.2.endLine=9
scope.2.semanticHash=7ce0806465f7b0429bd30cd86a7cd2a4b5915576a8a810e8f09974cdb0155a0e
scope.3.id=ZmllbGQ6UGF3biNpcm9uX2JveDoxMA
scope.3.kind=field
scope.3.startLine=10
scope.3.endLine=10
scope.3.semanticHash=eef0b7bf1a9616bc79153e88cac9e182caef45ac44515a082d0ffe8a5de63958
scope.4.id=ZmllbGQ6UGF3biNyYWNlY2FyOjEx
scope.4.kind=field
scope.4.startLine=11
scope.4.endLine=11
scope.4.semanticHash=e00f9ef51a95f6e854862eed28dc0f1a68f154d9f75ddd841ab00de6ede9209b
scope.5.id=ZmllbGQ6UGF3biNzaGlwOjEy
scope.5.kind=field
scope.5.startLine=12
scope.5.endLine=12
scope.5.semanticHash=e5d5b971139eefeb36d6edb9938fa246740c90da2003626487eb2d5d9646aec6
scope.6.id=ZmllbGQ6UGF3biNzaG9lOjEz
scope.6.kind=field
scope.6.startLine=13
scope.6.endLine=13
scope.6.semanticHash=efda1c925291a74c51bb958e56bf3589677a4a3880ddf40b8323b0235b3ced09
scope.7.id=ZmllbGQ6UGF3biN0aGltYmxlOjE0
scope.7.kind=field
scope.7.startLine=14
scope.7.endLine=14
scope.7.semanticHash=da62dfd88d3c7561df673e1409844014c4f94537f3c773e73a24ddfe6a3f218b
scope.8.id=ZmllbGQ6UGF3biN3aGVlbGJhcnJvdzoxNQ
scope.8.kind=field
scope.8.startLine=15
scope.8.endLine=15
scope.8.semanticHash=f7f50a48c3c98ab7f5a7ca137bdd676542d1f8acc7303dce204bfaf94cb1bc42
scope.9.id=bWV0aG9kOlBhd24jY3RvcigwKTo3
scope.9.kind=method
scope.9.startLine=1
scope.9.endLine=25
scope.9.semanticHash=872a6d2cc08bf33e107dbb489f42d595250ad7e3a8d4d6c941b05df29d19137a
scope.10.id=bWV0aG9kOlBhd24jaWQoMCk6MjI
scope.10.kind=method
scope.10.startLine=22
scope.10.endLine=24
scope.10.semanticHash=fa99ff78d16f6c4818fda22b2be269338875594b13fbc8b8b00bfce658072c66
scope.11.id=bWV0aG9kOlBhd24jcGF3bk5hbWUoMCk6MTg
scope.11.kind=method
scope.11.startLine=18
scope.11.endLine=20
scope.11.semanticHash=ef010f4481146a93daea795923130eac8111a9f4a924a8956dbf3067f4bb040f
*/
