package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Rule;

import java.util.List;

/**
 * Agrees to whatever it has the means to pay for: it buys land it can afford,
 * and at auction it bids the most it can afford rather than lose the land.
 */
public final class Greedo implements Strategy {
  private final Money reserve;
  private final boolean stalemateTrading;

  public Greedo() {
    this(Money.ZERO, false);
  }

  public Greedo(Money reserve) {
    this(reserve, false);
  }

  public Greedo(Money reserve, boolean stalemateTrading) {
    this.reserve = reserve;
    this.stalemateTrading = stalemateTrading;
  }

  public boolean stalemateTradingEnabled() {
    return stalemateTrading;
  }

  @Override
  public boolean accepts(Offer offer) {
    return offer.isAffordable()
        && (offer.utilityMonopolyOpportunity()
            || offer.available().minus(offer.land().price()).covers(offer.reserve()));
  }

  @Override
  public boolean accepts(TradeOffer offer, Rule.Set rules, Deeds deeds) {
    if (deeds.ownerOf(offer.offered().type()).filter(offer.trader().id()::equals).isEmpty()) return false;
    if (deeds.ownerOf(offer.wanted().type()).filter(offer.partner().id()::equals).isEmpty()) return false;
    if (offer.offered() instanceof ColourStreet offered && offer.wanted() instanceof ColourStreet wanted
        && offered.colourGroup() == wanted.colourGroup()) return false;
    if (priority(offer.offered()) == Priority.HIGHEST) return false;
    if (!deeds.completesColourGroup(rules, offer.wanted(), offer.trader())) return false;
    return !ownsHighestPriorityMonopoly(offer.partner(), rules, deeds);
  }

  private boolean ownsHighestPriorityMonopoly(Player player, Rule.Set rules, Deeds deeds) {
    return rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .filter(street -> priority(street) == Priority.HIGHEST)
        .filter(street -> deeds.ownerOf(street.type()).filter(player.id()::equals).isPresent())
        .anyMatch(street -> deeds.completesColourGroup(rules, street, player));
  }

  @Override
  public DeclineReason declineReason(Offer offer) {
    return offer.isAffordable() ? DeclineReason.CASH_RESERVE : DeclineReason.CANNOT_AFFORD;
  }

  @Override
  public Money cashReserve() {
    return reserve;
  }

  @Override
  public Money cashReserve(Player player, Rule.Set rules, Deeds deeds) {
    int reserveAmount = reserve.amount();
    int bestTier = 3;
    for (ColourStreet street : rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).toList()) {
      List<ColourStreet> group = rules.streets().filter(ColourStreet.class::isInstance)
          .map(ColourStreet.class::cast).filter(it -> it.colourGroup() == street.colourGroup()).toList();
      List<ColourStreet> missing = group.stream()
          .filter(it -> deeds.isUnowned(it.type())).toList();
      long owned = group.stream().filter(it -> deeds.ownerOf(it.type()).filter(player.id()::equals).isPresent()).count();
      if (owned == group.size() - 1 && missing.size() == 1
          && missing.getFirst().price().amount() <= player.account().balance().amount().amount()) {
        int tier = priorityTier(priority(missing.getFirst()));
        if (tier < bestTier) {
          bestTier = tier;
          reserveAmount = Math.max(reserve.amount(), missing.getFirst().price().amount());
        } else if (tier == bestTier) {
          reserveAmount = Math.max(reserveAmount, missing.getFirst().price().amount());
        }
      }
    }
    List<Street.Type> stations = rules.streets().filter(it -> it instanceof Station)
        .map(Street::type).toList();
    long ownedStations = stations.stream().filter(type -> deeds.ownerOf(type).filter(player.id()::equals).isPresent()).count();
    if (ownedStations == stations.size() - 1 && 2 <= bestTier) reserveAmount = Math.max(reserveAmount, 200);
    return new Money(reserveAmount);
  }

  private int priorityTier(Priority priority) {
    return switch (priority) {
      case HIGHEST -> 0;
      case MIDDLE -> 1;
      case LOWEST -> 2;
    };
  }

  @Override
  public Priority priority(Ownable land) {
    return switch (land.type()) {
      case LippenslaanKnokke, RueRoyaleTournai, GroenplaatsAntwerpen,
          RueStLeonardLiege, LangeSteenstraatKortrijk, GrandPlaceMons,
          SteenstraatBrugge, PlaceDuMonumentSpa, KapellestraatOostende -> Priority.HIGHEST;
      case RueGrandeDinant, DiestsestraatLeuven, RueDeDiekirchArlon,
          BruulMechelen, PlaceVerteVerviers, GroteMarktHasselt,
          PlaceDeLAngeNamur, HoogstraatBrussel -> Priority.MIDDLE;
      default -> Priority.LOWEST;
    };
  }

  @Override
  public Money bidForDistressed(Offer offer, Player bidder, Player debtor,
                                List<Player> players, Rule.Set rules, Deeds deeds) {
    boolean winsByBankruptcy = wouldWinByBankruptcy(bidder, debtor, players, deeds, rules);
    if (winsByBankruptcy) return Money.ZERO;
    boolean completesOwnGroup = deeds.completesColourGroup(rules, offer.land(), bidder);
    if (completesOwnGroup) return offer.available();
    boolean deniesOpponent = priority(offer.land()) == Priority.HIGHEST;
    if (deniesOpponent) {
      int available = offer.available().amount();
      return new Money(Math.min(available, available * 35 / 100));
    }
    return Money.ZERO;
  }

  private boolean wouldWinByBankruptcy(Player bidder, Player debtor, List<Player> players, Deeds deeds,
                                       Rule.Set rules) {
    List<Player> survivingOpponents = players.stream()
        .filter(it -> !it.id().equals(debtor.id()) && !deeds.isBankrupt(it)).toList();
    if (survivingOpponents.size() != 1 || !survivingOpponents.getFirst().id().equals(bidder.id())) return false;
    int debtorPropertyWorth = deeds.landOwnedBy(debtor).stream()
        .map(type -> ((Ownable) rules.create(type)).price().amount())
        .reduce(0, Integer::sum);
    int debt = Math.max(0, -debtor.account().balance().amount().amount());
    return bidder.account().balance().amount().amount() > debtorPropertyWorth + debt;
  }

  @Override
  public Money bidFor(Offer offer) {
    if (offer.utilityMonopolyOpportunity()) {
      return offer.available();
    }
    Money effectiveReserve = offer.reserve().equals(Money.ZERO) ? reserve : offer.reserve();
    return new Money(Math.max(0, offer.available().amount() - effectiveReserve.amount()));
  }

  @Override
  public boolean claims(RentClaim claim) {
    return true;
  }

  @Override
  public boolean builds(BuildOffer offer) {
    return offer.isAffordable();
  }

  @Override
  public boolean pays(JailFine fine) {
    return fine.isAffordable();
  }
}

/* mutate4java-manifest
version=1
moduleHash=9b784055dd4258a289f1f8e3f4b3a89836ebaf7411048b945078aaf1f3a8edf2
scope.0.id=Y2xhc3M6R3JlZWRvI0dyZWVkbzoxOA
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=172
scope.0.semanticHash=e266f730038d61c40f8d3056ca96f3b45756261c81d78e1e8524ed3af3de05e5
scope.1.id=ZmllbGQ6R3JlZWRvI3Jlc2VydmU6MTk
scope.1.kind=field
scope.1.startLine=19
scope.1.endLine=19
scope.1.semanticHash=022151ff347dc108fc1ab96c86b31089da850eb8390d5d07ac5112987a203360
scope.2.id=ZmllbGQ6R3JlZWRvI3N0YWxlbWF0ZVRyYWRpbmc6MjA
scope.2.kind=field
scope.2.startLine=20
scope.2.endLine=20
scope.2.semanticHash=3fb0db6ec778e457ec4b9262d01f922604291d8cbeefa5df7e177c0d5beea6b1
scope.3.id=bWV0aG9kOkdyZWVkbyNhY2NlcHRzKDEpOjM5
scope.3.kind=method
scope.3.startLine=39
scope.3.endLine=44
scope.3.semanticHash=3046213c02ec52cc24276e2e0c0268d5cee4d9f20ada852b38da09b73447c786
scope.4.id=bWV0aG9kOkdyZWVkbyNhY2NlcHRzKDMpOjQ2
scope.4.kind=method
scope.4.startLine=46
scope.4.endLine=53
scope.4.semanticHash=951c70f025626d2613713f6ed5a2d4331174dc0847388ebc5fd5d34daf835eb2
scope.5.id=bWV0aG9kOkdyZWVkbyNiaWRGb3IoMSk6MTQ5
scope.5.kind=method
scope.5.startLine=149
scope.5.endLine=156
scope.5.semanticHash=bd82389a84803b1754cc19ad5b28c3a9af96ccb021cd5c0f2fb145cc8510349b
scope.6.id=bWV0aG9kOkdyZWVkbyNiaWRGb3JEaXN0cmVzc2VkKDYpOjEyMg
scope.6.kind=method
scope.6.startLine=122
scope.6.endLine=135
scope.6.semanticHash=a2c828c3714b7c301eef3ecb5fc37e171584775b4f325dc4bed3ed4dcbc0c58e
scope.7.id=bWV0aG9kOkdyZWVkbyNidWlsZHMoMSk6MTYz
scope.7.kind=method
scope.7.startLine=163
scope.7.endLine=166
scope.7.semanticHash=4f01974523df4662a52d66f5493b517a0923103fb4f0c05c789f0e20a2161ba2
scope.8.id=bWV0aG9kOkdyZWVkbyNjYXNoUmVzZXJ2ZSgwKTo2Nw
scope.8.kind=method
scope.8.startLine=67
scope.8.endLine=70
scope.8.semanticHash=71dea961caede545156063acfb60aaa924230b06a5f11c4e9ed9bcd22e65e869
scope.9.id=bWV0aG9kOkdyZWVkbyNjYXNoUmVzZXJ2ZSgzKTo3Mg
scope.9.kind=method
scope.9.startLine=72
scope.9.endLine=99
scope.9.semanticHash=03c67c129eeba91fe47e3ef9cd4d20214b076fc0f80d60333278eba72e079485
scope.10.id=bWV0aG9kOkdyZWVkbyNjbGFpbXMoMSk6MTU4
scope.10.kind=method
scope.10.startLine=158
scope.10.endLine=161
scope.10.semanticHash=507d9cac6317e7a85e3b057aed54560aaf53551ff97de821929348d3149eed56
scope.11.id=bWV0aG9kOkdyZWVkbyNjdG9yKDApOjIy
scope.11.kind=method
scope.11.startLine=22
scope.11.endLine=24
scope.11.semanticHash=78dc1855c7d559d541f63852db874d93e65ffb127d5fff270765dc696d252b5b
scope.12.id=bWV0aG9kOkdyZWVkbyNjdG9yKDEpOjI2
scope.12.kind=method
scope.12.startLine=26
scope.12.endLine=28
scope.12.semanticHash=da6dbdc6233578fa2d355071c83da8141f67c6983c3bf1745770d1306c27cf59
scope.13.id=bWV0aG9kOkdyZWVkbyNjdG9yKDIpOjMw
scope.13.kind=method
scope.13.startLine=30
scope.13.endLine=33
scope.13.semanticHash=23f1ecc3135d882fe207243bd34e519cc8718bd2aa87ccfb1a4b4e9104e418f4
scope.14.id=bWV0aG9kOkdyZWVkbyNkZWNsaW5lUmVhc29uKDEpOjYy
scope.14.kind=method
scope.14.startLine=62
scope.14.endLine=65
scope.14.semanticHash=f3aee34dfd7033144dfa3221a6e458baaa34e85c3674c1bbd03e01950d231e18
scope.15.id=bWV0aG9kOkdyZWVkbyNvd25zSGlnaGVzdFByaW9yaXR5TW9ub3BvbHkoMyk6NTU
scope.15.kind=method
scope.15.startLine=55
scope.15.endLine=60
scope.15.semanticHash=dd95832c2ab126e9aa92d5d99d325c88381d873a42945dca909898b5532f3330
scope.16.id=bWV0aG9kOkdyZWVkbyNwYXlzKDEpOjE2OA
scope.16.kind=method
scope.16.startLine=168
scope.16.endLine=171
scope.16.semanticHash=2582c1709534ea40e6b58368ac921704c006661aea5482e52aa350caa137bc3b
scope.17.id=bWV0aG9kOkdyZWVkbyNwcmlvcml0eSgxKToxMDk
scope.17.kind=method
scope.17.startLine=109
scope.17.endLine=120
scope.17.semanticHash=9e17a96df0b90750a02484d2fb9f21ca0d6efc20313fcf887f1f7e20cde04c22
scope.18.id=bWV0aG9kOkdyZWVkbyNwcmlvcml0eVRpZXIoMSk6MTAx
scope.18.kind=method
scope.18.startLine=101
scope.18.endLine=107
scope.18.semanticHash=1fb89619bb56da0db017a4a8551e06eeda4f4cc5fea7977fc9e7d6ae5582c42d
scope.19.id=bWV0aG9kOkdyZWVkbyNzdGFsZW1hdGVUcmFkaW5nRW5hYmxlZCgwKTozNQ
scope.19.kind=method
scope.19.startLine=35
scope.19.endLine=37
scope.19.semanticHash=e6cd0b45019a684a87bb1fe4e7652d0cdccbbe304b1ee4c62ac033791f4097e2
scope.20.id=bWV0aG9kOkdyZWVkbyN3b3VsZFdpbkJ5QmFua3J1cHRjeSg1KToxMzc
scope.20.kind=method
scope.20.startLine=137
scope.20.endLine=147
scope.20.semanticHash=e2b363a88387a3d6ad085793bf4c37b59b4abfc1d5108642ba9890ef2be08b7a
*/
