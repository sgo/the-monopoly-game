package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.HashSet;
import java.util.Set;

/** Keeps track of prisoners and resolves the ways a prisoner may leave jail. */
public final class Jail implements Landings {
  public static final Money FINE = new Money(50);

  private final Rule.Set rules;
  private Events events;
  private final Set<Player.ID> prisoners = new HashSet<>();
  private final Set<Player.ID> cardUsers = new HashSet<>();
  private final java.util.Map<Player.ID, Integer> failedDoubleAttempts = new java.util.HashMap<>();

  public Jail(Rule.Set rules) {
    this(rules, new Events() {
    });
  }

  public Jail(Rule.Set rules, Events events) {
    this.rules = rules;
    this.events = events;
  }

  @Override
  public void resolve(Player player, Street space, Roll roll) {
    if (space.kind() == Street.Kind.go_to_jail) {
      imprison(player);
      events.sentToJail(player, space.type());
    }
  }

  public void imprison(Player player) {
    prisoners.add(player.id());
    player.position().moveTo(rules.gameboard().positionOf(Street.Type.OpBezoek));
  }

  public boolean holds(Player player) {
    return prisoners.contains(player.id());
  }

  public void useCard(Player player) {
    cardUsers.add(player.id());
  }

  /** Whether the normal turn may proceed without a doubles-only attempt. */
  public boolean mayTakeTurn(Player player, Strategy strategy, Deeds deeds) {
    if (!holds(player)) return true;
    if (cardUsers.remove(player.id()) && deeds.releaseGetOutOfJailFreeCard(player)) {
      release(player);
      events.leftJailWithCard(player);
      return true;
    }
    Strategy.JailFine fine = new Strategy.JailFine(FINE, player.account().balance().amount());
    if (!strategy.pays(fine)) return false;

    player.account().withdraw(FINE);
    release(player);
    events.leftJailByPaying(player, FINE);
    return true;
  }

  /** A doubles roll frees a prisoner and supplies their movement for this turn. */
  public boolean leavesOn(Roll roll, Player player) {
    if (roll.isDouble()) {
      release(player);
      events.leftJailByRollingDoubles(player);
      return true;
    }
    int failedAttempts = failedDoubleAttempts.merge(player.id(), 1, Integer::sum);
    if (failedAttempts < 3) {
      events.stayedInJail(player);
      return false;
    }

    player.account().withdraw(FINE);
    release(player);
    events.leftJailByPaying(player, FINE);
    return true;
  }

  public void observe(Events events) {
    this.events = events;
  }

  private void release(Player player) {
    prisoners.remove(player.id());
    failedDoubleAttempts.remove(player.id());
  }

  public interface Events {
    default void sentToJail(Player player, Street.Type cause) {
    }

    default void leftJailByPaying(Player player, Money fine) {
    }

    default void leftJailWithCard(Player player) {
    }

    default void leftJailByRollingDoubles(Player player) {
    }

    default void stayedInJail(Player player) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=b070c53984e7e45c07fab6c29b4bc9e8cbcb59fe89794ba83500e7253e40d096
scope.0.id=Y2xhc3M6SmFpbCNKYWlsOjEz
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=114
scope.0.semanticHash=d7d782d2c6e10dac8209956ee18950022dd1ebb8647298fb679e89d999d21a1f
scope.1.id=Y2xhc3M6SmFpbC4jOjIz
scope.1.kind=class
scope.1.startLine=23
scope.1.endLine=24
scope.1.semanticHash=9265619237dc049c9efd5c01a5c74a7e99db1a67aa7ef3e2e396e54563e1bc57
scope.2.id=Y2xhc3M6SmFpbC5FdmVudHMjRXZlbnRzOjk4
scope.2.kind=class
scope.2.startLine=98
scope.2.endLine=113
scope.2.semanticHash=2f29905234e762641608b747c08aece03b4020c910c7f721a7e5ea6a4a6def10
scope.3.id=ZmllbGQ6SmFpbCNGSU5FOjE0
scope.3.kind=field
scope.3.startLine=14
scope.3.endLine=14
scope.3.semanticHash=acc97d20a10cb7c70e250671496eb2e96f6a38f8e083166a690cde2e09c52014
scope.4.id=ZmllbGQ6SmFpbCNjYXJkVXNlcnM6MTk
scope.4.kind=field
scope.4.startLine=19
scope.4.endLine=19
scope.4.semanticHash=3ef2490d8fb16cca80a8966fa8e0425436361f756d3319f5b7713f86c51d269f
scope.5.id=ZmllbGQ6SmFpbCNldmVudHM6MTc
scope.5.kind=field
scope.5.startLine=17
scope.5.endLine=17
scope.5.semanticHash=466aa39617c8d7c1d9cfcbb1a700a518c1952b68beee465ca2e31cbc8db1a1b1
scope.6.id=ZmllbGQ6SmFpbCNmYWlsZWREb3VibGVBdHRlbXB0czoyMA
scope.6.kind=field
scope.6.startLine=20
scope.6.endLine=20
scope.6.semanticHash=2a635586918ac273aaa5b09192467a4029b16dabb3c69742f7f84276eb14605e
scope.7.id=ZmllbGQ6SmFpbCNwcmlzb25lcnM6MTg
scope.7.kind=field
scope.7.startLine=18
scope.7.endLine=18
scope.7.semanticHash=72ff78ab8275e1b39a7b4684488857b44c17e412e338128c3630eaa07235cea2
scope.8.id=ZmllbGQ6SmFpbCNydWxlczoxNg
scope.8.kind=field
scope.8.startLine=16
scope.8.endLine=16
scope.8.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.9.id=bWV0aG9kOkphaWwjY3RvcigxKToyMg
scope.9.kind=method
scope.9.startLine=22
scope.9.endLine=25
scope.9.semanticHash=fc022f60713de12e6df51dc7cbcbb526234d16e494a433177739da5bd0467241
scope.10.id=bWV0aG9kOkphaWwjY3RvcigyKToyNw
scope.10.kind=method
scope.10.startLine=27
scope.10.endLine=30
scope.10.semanticHash=6c15dac9d934fe12a94a2a7b2fd500f53166595eb2f4e6a4ff5cc8fb581385d4
scope.11.id=bWV0aG9kOkphaWwjaG9sZHMoMSk6NDU
scope.11.kind=method
scope.11.startLine=45
scope.11.endLine=47
scope.11.semanticHash=62f6b72732bcfc4617b7ade8cf697c675b3e5b878dc337c5b293a6c4721ed58e
scope.12.id=bWV0aG9kOkphaWwjaW1wcmlzb24oMSk6NDA
scope.12.kind=method
scope.12.startLine=40
scope.12.endLine=43
scope.12.semanticHash=5699b04e09653df0c9faa976773123b838c0c92692630fa455646a085f8a8e62
scope.13.id=bWV0aG9kOkphaWwjbGVhdmVzT24oMik6NzE
scope.13.kind=method
scope.13.startLine=71
scope.13.endLine=87
scope.13.semanticHash=5e6ea20e76d06104f0603bd110de47ee56d5e1ad29038614c9b81702cb5d5c0e
scope.14.id=bWV0aG9kOkphaWwjbWF5VGFrZVR1cm4oMyk6NTQ
scope.14.kind=method
scope.14.startLine=54
scope.14.endLine=68
scope.14.semanticHash=7ae00746fd78068f757cdcc22a424f298e1f436b3fe121aec8e3a8759d1a988c
scope.15.id=bWV0aG9kOkphaWwjb2JzZXJ2ZSgxKTo4OQ
scope.15.kind=method
scope.15.startLine=89
scope.15.endLine=91
scope.15.semanticHash=d7c16b113a8f02ba8427913df69e4f443f00eb436831d4af90a7cbae2db99d0c
scope.16.id=bWV0aG9kOkphaWwjcmVsZWFzZSgxKTo5Mw
scope.16.kind=method
scope.16.startLine=93
scope.16.endLine=96
scope.16.semanticHash=489ee90cd5bdae088def9c4886c166016ce08525b0728bc1c40610f046ad64ee
scope.17.id=bWV0aG9kOkphaWwjcmVzb2x2ZSgzKTozMg
scope.17.kind=method
scope.17.startLine=32
scope.17.endLine=38
scope.17.semanticHash=b1595dc43bf573dfa1a2a75c06ee9c266c68ad5787133eac7e57f5fde15dc9bf
scope.18.id=bWV0aG9kOkphaWwjdXNlQ2FyZCgxKTo0OQ
scope.18.kind=method
scope.18.startLine=49
scope.18.endLine=51
scope.18.semanticHash=015cacf0f5f61223c0dc1b808978fe9cc635baf1693550b04cf79bd03d70e739
scope.19.id=bWV0aG9kOkphaWwuI2N0b3IoMCk6MjM
scope.19.kind=method
scope.19.startLine=1
scope.19.endLine=114
scope.19.semanticHash=96ca741644bea4ffa59c00c8203d805f688c4341ea5df80046a7cc1f8fc16c3a
scope.20.id=bWV0aG9kOkphaWwuRXZlbnRzI2xlZnRKYWlsQnlQYXlpbmcoMik6MTAy
scope.20.kind=method
scope.20.startLine=102
scope.20.endLine=103
scope.20.semanticHash=944ae10e25cb0fbec7b03b841dc79acb441c905f1f32afa328d14ef543ddfc71
scope.21.id=bWV0aG9kOkphaWwuRXZlbnRzI2xlZnRKYWlsQnlSb2xsaW5nRG91YmxlcygxKToxMDg
scope.21.kind=method
scope.21.startLine=108
scope.21.endLine=109
scope.21.semanticHash=280e545906a83647c9ea55f57cf90ef7ab0542b175af0212f86ac01c185dc963
scope.22.id=bWV0aG9kOkphaWwuRXZlbnRzI2xlZnRKYWlsV2l0aENhcmQoMSk6MTA1
scope.22.kind=method
scope.22.startLine=105
scope.22.endLine=106
scope.22.semanticHash=961eb31de63b4bd75ca1a8c66588158c8ab84e9a634714093457970d8347b865
scope.23.id=bWV0aG9kOkphaWwuRXZlbnRzI3NlbnRUb0phaWwoMik6OTk
scope.23.kind=method
scope.23.startLine=99
scope.23.endLine=100
scope.23.semanticHash=523ca188c17c3012eda0afddbcb10ab9a12585dd02352ea032a0559270d035b1
scope.24.id=bWV0aG9kOkphaWwuRXZlbnRzI3N0YXllZEluSmFpbCgxKToxMTE
scope.24.kind=method
scope.24.startLine=111
scope.24.endLine=112
scope.24.semanticHash=751bcd4acf734b49a47104b2a154ec8efab285df23295b0346b2ab854db576f0
*/
