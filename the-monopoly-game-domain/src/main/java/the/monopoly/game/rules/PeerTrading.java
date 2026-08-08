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
        .min(bySelectionOrder(greedo))
        .filter(offer -> deeds.completesColourGroup(rules, offer.offered(), offer.partner()));
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
moduleHash=4b8276fa321237f2a0fd9103dcda6fc43f0be264fbc2d172f49a530c99049089
scope.0.id=Y2xhc3M6UGVlclRyYWRpbmcjUGVlclRyYWRpbmc6MTQ
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=58
scope.0.semanticHash=540f481eb6220ff8178e8e8942f817d360da32707a022f0be13521f79b2ea12d
scope.1.id=bWV0aG9kOlBlZXJUcmFkaW5nI2J5U2VsZWN0aW9uT3JkZXIoMSk6NDQ
scope.1.kind=method
scope.1.startLine=44
scope.1.endLine=49
scope.1.semanticHash=ac780b84dc46f65b8f2274007ef0bc11048b1783512a4efd20dafc0264ea513c
scope.2.id=bWV0aG9kOlBlZXJUcmFkaW5nI2N0b3IoMCk6MTU
scope.2.kind=method
scope.2.startLine=15
scope.2.endLine=16
scope.2.semanticHash=bf3ef1a78dbdff8b1cf398821a50951d416b6b34176ab064e5d3a448f54be7be
scope.3.id=bWV0aG9kOlBlZXJUcmFkaW5nI29mZmVyc1RvKDQpOjMx
scope.3.kind=method
scope.3.startLine=31
scope.3.endLine=38
scope.3.semanticHash=eb2003856ff29de7823dac3b0e43c9e560bf429ad2041d2368a4c57b36f360fa
scope.4.id=bWV0aG9kOlBlZXJUcmFkaW5nI293bmVkQnkoMyk6NDA
scope.4.kind=method
scope.4.startLine=40
scope.4.endLine=42
scope.4.semanticHash=1871b841d92fb40a3d7adf5151a776ca87072172918988a5993ec6ec66ab2e8b
scope.5.id=bWV0aG9kOlBlZXJUcmFkaW5nI3JhbmsoMSk6NTE
scope.5.kind=method
scope.5.startLine=51
scope.5.endLine=57
scope.5.semanticHash=99a282f645365c80bde111d124adf70d03a128d70c0b3518b160539e1fe96d58
scope.6.id=bWV0aG9kOlBlZXJUcmFkaW5nI3NlbGVjdCg1KToxOA
scope.6.kind=method
scope.6.startLine=18
scope.6.endLine=29
scope.6.semanticHash=a179102ff52fce9f6e5867b8466aa9e9303ec958fd5d801393640a6824c67270
*/
