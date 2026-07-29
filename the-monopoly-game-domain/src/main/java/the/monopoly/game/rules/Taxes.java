package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;

/** Collects the fixed tax charged when a pawn stops on a tax space. */
public final class Taxes implements Landings {
  private final Events events;

  public Taxes(Events events) {
    this.events = events;
  }

  @Override
  public void resolve(Player player, Street space, Roll roll) {
    if (!(space instanceof TaxSpace tax)) return;

    Money amount = tax.tax();
    player.account().withdraw(amount);
    events.paidBank(player, amount);
  }

  /** What a tax payment says happened, for whoever keeps the game journal. */
  public interface Events {
    void paidBank(Player player, Money amount);
  }
}

/* mutate4java-manifest
version=1
moduleHash=8ea4fc751e7ea93687959ab866d3affb283a1fc43ac4c8a15dd3c9aba194dc50
scope.0.id=Y2xhc3M6VGF4ZXMjVGF4ZXM6MTA
scope.0.kind=class
scope.0.startLine=10
scope.0.endLine=30
scope.0.semanticHash=b8c8b02cbc3eac5709e0bd47244f2af3129ef7708545622c512e7ddb6e4c1125
scope.1.id=Y2xhc3M6VGF4ZXMuRXZlbnRzI0V2ZW50czoyNw
scope.1.kind=class
scope.1.startLine=27
scope.1.endLine=29
scope.1.semanticHash=bc9db80e7fcc89852a78c8e689cb3e3b5fbd2d4816490f20f95282067e578e80
scope.2.id=ZmllbGQ6VGF4ZXMjZXZlbnRzOjEx
scope.2.kind=field
scope.2.startLine=11
scope.2.endLine=11
scope.2.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.3.id=bWV0aG9kOlRheGVzI2N0b3IoMSk6MTM
scope.3.kind=method
scope.3.startLine=13
scope.3.endLine=15
scope.3.semanticHash=9c3155eab38356dedbd10cb67c053b014a61ef75b7f3b3e370b8b22d5c54e6c8
scope.4.id=bWV0aG9kOlRheGVzI3Jlc29sdmUoMyk6MTc
scope.4.kind=method
scope.4.startLine=17
scope.4.endLine=24
scope.4.semanticHash=55bff8e2b931dd8805beb9be43d716479dd617a23c26f2782d386b86b3de22b4
scope.5.id=bWV0aG9kOlRheGVzLkV2ZW50cyNwYWlkQmFuaygyKToyOA
scope.5.kind=method
scope.5.startLine=28
scope.5.endLine=28
scope.5.semanticHash=1fe174b458bab202a8077f738aae77cbea617dcbc6b198b0265efeb681a4608c
*/
