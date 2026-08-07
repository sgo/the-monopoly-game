package the.monopoly.game.rules;

import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Picks the peer trade a stalemate-trading Greedo player would make to relieve a deadlock. */
public final class PeerTrading {
  private PeerTrading() {
  }

  public static Optional<Strategy.TradeOffer> select(
      Player trader, Strategy strategy, List<Player> turnOrder, Rule.Set rules, Deeds deeds) {
    if (!(strategy instanceof Greedo greedo) || !greedo.stalemateTradingEnabled()) return Optional.empty();
    List<ColourStreet> colourStreets =
        rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast).toList();
    return turnOrder.stream()
        .filter(partner -> partner != trader)
        .flatMap(partner -> offersTo(trader, partner, colourStreets, deeds))
        .filter(offer -> strategy.accepts(offer, rules, deeds))
        .min(bySelectionOrder(greedo));
  }

  private static Stream<Strategy.TradeOffer> offersTo(
      Player trader, Player partner, List<ColourStreet> colourStreets, Deeds deeds) {
    return colourStreets.stream()
        .filter(offered -> ownedBy(deeds, offered, trader))
        .flatMap(offered -> colourStreets.stream()
            .filter(wanted -> ownedBy(deeds, wanted, partner))
            .map(wanted -> new Strategy.TradeOffer(trader, partner, offered, wanted)));
  }

  private static boolean ownedBy(Deeds deeds, ColourStreet street, Player player) {
    return deeds.ownerOf(street.type()).filter(player.id()::equals).isPresent();
  }

  private static Comparator<Strategy.TradeOffer> bySelectionOrder(Greedo greedo) {
    return Comparator
        .<Strategy.TradeOffer>comparingInt(offer -> rank(greedo.priority(offer.offered())))
        .thenComparing(Comparator.comparingInt(
            (Strategy.TradeOffer offer) -> offer.offered().price().amount()).reversed());
  }

  private static int rank(Greedo.Priority priority) {
    return switch (priority) {
      case LOWEST -> 0;
      case MIDDLE -> 1;
      case HIGHEST -> 2;
    };
  }
}

/* mutate4java-manifest
version=1
moduleHash=a9129440f2123b74f1dc5cd33a16b1a7546ad3f997f4a1c1b55d93deb011efe2
scope.0.id=Y2xhc3M6UGVlclRyYWRpbmcjUGVlclRyYWRpbmc6MTQ
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=57
scope.0.semanticHash=465e71f0d8c95635081e01abce294df91ae141ed381c906f225c0cc42ed4231b
scope.1.id=bWV0aG9kOlBlZXJUcmFkaW5nI2J5U2VsZWN0aW9uT3JkZXIoMSk6NDM
scope.1.kind=method
scope.1.startLine=43
scope.1.endLine=48
scope.1.semanticHash=ac780b84dc46f65b8f2274007ef0bc11048b1783512a4efd20dafc0264ea513c
scope.2.id=bWV0aG9kOlBlZXJUcmFkaW5nI2N0b3IoMCk6MTU
scope.2.kind=method
scope.2.startLine=15
scope.2.endLine=16
scope.2.semanticHash=bf3ef1a78dbdff8b1cf398821a50951d416b6b34176ab064e5d3a448f54be7be
scope.3.id=bWV0aG9kOlBlZXJUcmFkaW5nI29mZmVyc1RvKDQpOjMw
scope.3.kind=method
scope.3.startLine=30
scope.3.endLine=37
scope.3.semanticHash=eb2003856ff29de7823dac3b0e43c9e560bf429ad2041d2368a4c57b36f360fa
scope.4.id=bWV0aG9kOlBlZXJUcmFkaW5nI293bmVkQnkoMyk6Mzk
scope.4.kind=method
scope.4.startLine=39
scope.4.endLine=41
scope.4.semanticHash=1871b841d92fb40a3d7adf5151a776ca87072172918988a5993ec6ec66ab2e8b
scope.5.id=bWV0aG9kOlBlZXJUcmFkaW5nI3JhbmsoMSk6NTA
scope.5.kind=method
scope.5.startLine=50
scope.5.endLine=56
scope.5.semanticHash=99a282f645365c80bde111d124adf70d03a128d70c0b3518b160539e1fe96d58
scope.6.id=bWV0aG9kOlBlZXJUcmFkaW5nI3NlbGVjdCg1KToxOA
scope.6.kind=method
scope.6.startLine=18
scope.6.endLine=28
scope.6.semanticHash=f418a218d49c3ccc24545101896d45d7c8c95ab240b74b988bc480aa9ab7e81f
*/
