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
import java.util.Optional;

/**
 * Agrees to whatever it has the means to pay for: it buys land it can afford,
 * and at auction it bids the most it can afford rather than lose the land.
 */
public final class Greedo implements Strategy {
  private final Money reserve;
  private final boolean stalemateTrading;
  private final boolean legalEntityTrading;

  public Greedo() {
    this(Money.ZERO, false);
  }

  public Greedo(Money reserve) {
    this(reserve, false);
  }

  public Greedo(Money reserve, boolean stalemateTrading) {
    this(reserve, stalemateTrading, false);
  }

  public Greedo(Money reserve, boolean stalemateTrading, boolean legalEntityTrading) {
    this.reserve = reserve;
    this.stalemateTrading = stalemateTrading;
    this.legalEntityTrading = legalEntityTrading;
  }

  public boolean stalemateTradingEnabled() {
    return stalemateTrading;
  }

  @Override
  public boolean legalEntityTradingEnabled() {
    return legalEntityTrading;
  }

  @Override
  public boolean accepts(Offer offer) {
    return offer.isAffordable()
        && (offer.utilityMonopolyOpportunity()
            || offer.available().minus(offer.land().price()).covers(offer.reserve()));
  }

  @Override
  public boolean commitToEntityBuild(EntityBuildOffer offer) {
    return legalEntityTrading
        && offer.available().amount() >= 0
        && offer.available().covers(offer.share())
        && offer.available().minus(offer.share()).covers(offer.reserve());
  }

  @Override
  public boolean accepts(TradeOffer offer, Rule.Set rules, Deeds deeds) {
    if (deeds.ownerOf(offer.offered().type()).filter(offer.trader().id()::equals).isEmpty()) return false;
    if (deeds.ownerOf(offer.wanted().type()).filter(offer.partner().id()::equals).isEmpty()) return false;
    if (sameColourGroup(offer.offered(), offer.wanted())) return false;
    if (priority(offer.offered()) == Priority.HIGHEST) return false;
    if (!deeds.completesColourGroup(rules, offer.wanted(), offer.trader())) return false;
    return !ownsHighestPriorityMonopoly(offer.partner(), rules, deeds);
  }

  private boolean sameColourGroup(Ownable offered, Ownable wanted) {
    return offered instanceof ColourStreet offeredStreet && wanted instanceof ColourStreet wantedStreet
        && offeredStreet.colourGroup() == wantedStreet.colourGroup();
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
      Optional<ColourStreet> missing = oneStreetFromMonopoly(street, player, rules, deeds);
      if (missing.isEmpty()) continue;
      int tier = priorityTier(priority(missing.get()));
      if (tier < bestTier) {
        bestTier = tier;
        reserveAmount = Math.max(reserve.amount(), missing.get().price().amount());
      } else if (tier == bestTier) {
        reserveAmount = Math.max(reserveAmount, missing.get().price().amount());
      }
    }
    reserveAmount = Math.max(reserveAmount, stationReserve(player, rules, deeds, bestTier));
    return new Money(reserveAmount);
  }

  /** The single street this player is missing to complete this street's colour group, if affordable now. */
  private Optional<ColourStreet> oneStreetFromMonopoly(ColourStreet street, Player player, Rule.Set rules, Deeds deeds) {
    List<ColourStreet> group = rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).filter(it -> it.colourGroup() == street.colourGroup()).toList();
    List<ColourStreet> missing = group.stream().filter(it -> deeds.isUnowned(it.type())).toList();
    long owned = group.stream().filter(it -> deeds.ownerOf(it.type()).filter(player.id()::equals).isPresent()).count();
    if (owned != group.size() - 1 || missing.size() != 1) return Optional.empty();
    if (missing.getFirst().price().amount() > player.account().balance().amount().amount()) return Optional.empty();
    return Optional.of(missing.getFirst());
  }

  /** The reserve bump for being one station short of a station monopoly, or 0 if not applicable. */
  private int stationReserve(Player player, Rule.Set rules, Deeds deeds, int bestTier) {
    List<Street.Type> stations = rules.streets().filter(it -> it instanceof Station)
        .map(Street::type).toList();
    long ownedStations = stations.stream().filter(type -> deeds.ownerOf(type).filter(player.id()::equals).isPresent()).count();
    return ownedStations == stations.size() - 1 && 2 <= bestTier ? 200 : 0;
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
    return priorityOf(land);
  }

  public static Priority priorityOf(Ownable land) {
    return Strategy.priorityOf(land);
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
moduleHash=f7a612ae0830265acc48eca868a61e771e299b113ec02fe31721eb3b632ed566
scope.0.id=Y2xhc3M6R3JlZWRvI0dyZWVkbzoxOQ
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=203
scope.0.semanticHash=95a848830f4658a1e137a780cacae81ec7dec30d984f0533b914dd5dcd69776a
scope.1.id=ZmllbGQ6R3JlZWRvI2xlZ2FsRW50aXR5VHJhZGluZzoyMg
scope.1.kind=field
scope.1.startLine=22
scope.1.endLine=22
scope.1.semanticHash=79e35a24ea51a961b285f1431176277ba37f40aebb7dc85ea35c6c3e9ef9567e
scope.2.id=ZmllbGQ6R3JlZWRvI3Jlc2VydmU6MjA
scope.2.kind=field
scope.2.startLine=20
scope.2.endLine=20
scope.2.semanticHash=022151ff347dc108fc1ab96c86b31089da850eb8390d5d07ac5112987a203360
scope.3.id=ZmllbGQ6R3JlZWRvI3N0YWxlbWF0ZVRyYWRpbmc6MjE
scope.3.kind=field
scope.3.startLine=21
scope.3.endLine=21
scope.3.semanticHash=3fb0db6ec778e457ec4b9262d01f922604291d8cbeefa5df7e177c0d5beea6b1
scope.4.id=bWV0aG9kOkdyZWVkbyNhY2NlcHRzKDEpOjUw
scope.4.kind=method
scope.4.startLine=50
scope.4.endLine=55
scope.4.semanticHash=3046213c02ec52cc24276e2e0c0268d5cee4d9f20ada852b38da09b73447c786
scope.5.id=bWV0aG9kOkdyZWVkbyNhY2NlcHRzKDMpOjY1
scope.5.kind=method
scope.5.startLine=65
scope.5.endLine=73
scope.5.semanticHash=f63c67d71e7888e6b4e1a9ba098a853c19d3dbd83b69270b1657d85829210d2b
scope.6.id=bWV0aG9kOkdyZWVkbyNiaWRGb3IoMSk6MTgw
scope.6.kind=method
scope.6.startLine=180
scope.6.endLine=187
scope.6.semanticHash=bd82389a84803b1754cc19ad5b28c3a9af96ccb021cd5c0f2fb145cc8510349b
scope.7.id=bWV0aG9kOkdyZWVkbyNiaWRGb3JEaXN0cmVzc2VkKDYpOjE1Mw
scope.7.kind=method
scope.7.startLine=153
scope.7.endLine=166
scope.7.semanticHash=a2c828c3714b7c301eef3ecb5fc37e171584775b4f325dc4bed3ed4dcbc0c58e
scope.8.id=bWV0aG9kOkdyZWVkbyNidWlsZHMoMSk6MTk0
scope.8.kind=method
scope.8.startLine=194
scope.8.endLine=197
scope.8.semanticHash=4f01974523df4662a52d66f5493b517a0923103fb4f0c05c789f0e20a2161ba2
scope.9.id=bWV0aG9kOkdyZWVkbyNjYXNoUmVzZXJ2ZSgwKTo5Mg
scope.9.kind=method
scope.9.startLine=92
scope.9.endLine=95
scope.9.semanticHash=71dea961caede545156063acfb60aaa924230b06a5f11c4e9ed9bcd22e65e869
scope.10.id=bWV0aG9kOkdyZWVkbyNjYXNoUmVzZXJ2ZSgzKTo5Nw
scope.10.kind=method
scope.10.startLine=97
scope.10.endLine=115
scope.10.semanticHash=0278a32f7d7cc780def4dd0f73fa6615e921c980c33661a2d54b3909396652f2
scope.11.id=bWV0aG9kOkdyZWVkbyNjbGFpbXMoMSk6MTg5
scope.11.kind=method
scope.11.startLine=189
scope.11.endLine=192
scope.11.semanticHash=507d9cac6317e7a85e3b057aed54560aaf53551ff97de821929348d3149eed56
scope.12.id=bWV0aG9kOkdyZWVkbyNjb21taXRUb0VudGl0eUJ1aWxkKDEpOjU3
scope.12.kind=method
scope.12.startLine=57
scope.12.endLine=63
scope.12.semanticHash=53943b85ba5f9f196efad1e4c68e3713a2d3231fce7f64ca5657a6e8c1787fc8
scope.13.id=bWV0aG9kOkdyZWVkbyNjdG9yKDApOjI0
scope.13.kind=method
scope.13.startLine=24
scope.13.endLine=26
scope.13.semanticHash=78dc1855c7d559d541f63852db874d93e65ffb127d5fff270765dc696d252b5b
scope.14.id=bWV0aG9kOkdyZWVkbyNjdG9yKDEpOjI4
scope.14.kind=method
scope.14.startLine=28
scope.14.endLine=30
scope.14.semanticHash=da6dbdc6233578fa2d355071c83da8141f67c6983c3bf1745770d1306c27cf59
scope.15.id=bWV0aG9kOkdyZWVkbyNjdG9yKDIpOjMy
scope.15.kind=method
scope.15.startLine=32
scope.15.endLine=34
scope.15.semanticHash=978e52748b553f47c0839d7326f35ff3e5773ed0fd44053622bbb5c91df6f154
scope.16.id=bWV0aG9kOkdyZWVkbyNjdG9yKDMpOjM2
scope.16.kind=method
scope.16.startLine=36
scope.16.endLine=40
scope.16.semanticHash=8fe47a1f6a943734b10e82ea486b23391745eec3824ca891a5177666af6c909c
scope.17.id=bWV0aG9kOkdyZWVkbyNkZWNsaW5lUmVhc29uKDEpOjg3
scope.17.kind=method
scope.17.startLine=87
scope.17.endLine=90
scope.17.semanticHash=f3aee34dfd7033144dfa3221a6e458baaa34e85c3674c1bbd03e01950d231e18
scope.18.id=bWV0aG9kOkdyZWVkbyNsZWdhbEVudGl0eVRyYWRpbmdFbmFibGVkKDApOjQ2
scope.18.kind=method
scope.18.startLine=46
scope.18.endLine=48
scope.18.semanticHash=70c444ba7e0190fe12b85d51fefe3351d2381635c1a0ee9b5e59afdc25bc5b5b
scope.19.id=bWV0aG9kOkdyZWVkbyNvbmVTdHJlZXRGcm9tTW9ub3BvbHkoNCk6MTE4
scope.19.kind=method
scope.19.startLine=118
scope.19.endLine=126
scope.19.semanticHash=9219362bd4575012fc302064fc4eefb85196523da30ee2e53062f8034ee4dc92
scope.20.id=bWV0aG9kOkdyZWVkbyNvd25zSGlnaGVzdFByaW9yaXR5TW9ub3BvbHkoMyk6ODA
scope.20.kind=method
scope.20.startLine=80
scope.20.endLine=85
scope.20.semanticHash=dd95832c2ab126e9aa92d5d99d325c88381d873a42945dca909898b5532f3330
scope.21.id=bWV0aG9kOkdyZWVkbyNwYXlzKDEpOjE5OQ
scope.21.kind=method
scope.21.startLine=199
scope.21.endLine=202
scope.21.semanticHash=2582c1709534ea40e6b58368ac921704c006661aea5482e52aa350caa137bc3b
scope.22.id=bWV0aG9kOkdyZWVkbyNwcmlvcml0eSgxKToxNDQ
scope.22.kind=method
scope.22.startLine=144
scope.22.endLine=147
scope.22.semanticHash=4bde7b10651c5a8cf7aa32b42582311e167b855e7f7647a7f5c84929f62cf582
scope.23.id=bWV0aG9kOkdyZWVkbyNwcmlvcml0eU9mKDEpOjE0OQ
scope.23.kind=method
scope.23.startLine=149
scope.23.endLine=151
scope.23.semanticHash=42c495bad622eb6e3c950229ec71a3d2678e5b7dd2caef0145f32e1217e224a4
scope.24.id=bWV0aG9kOkdyZWVkbyNwcmlvcml0eVRpZXIoMSk6MTM2
scope.24.kind=method
scope.24.startLine=136
scope.24.endLine=142
scope.24.semanticHash=1fb89619bb56da0db017a4a8551e06eeda4f4cc5fea7977fc9e7d6ae5582c42d
scope.25.id=bWV0aG9kOkdyZWVkbyNzYW1lQ29sb3VyR3JvdXAoMik6NzU
scope.25.kind=method
scope.25.startLine=75
scope.25.endLine=78
scope.25.semanticHash=ad02650979dfc2c46695e4d7bbf42fa572f3a4431a9906966ada33914674d50d
scope.26.id=bWV0aG9kOkdyZWVkbyNzdGFsZW1hdGVUcmFkaW5nRW5hYmxlZCgwKTo0Mg
scope.26.kind=method
scope.26.startLine=42
scope.26.endLine=44
scope.26.semanticHash=e6cd0b45019a684a87bb1fe4e7652d0cdccbbe304b1ee4c62ac033791f4097e2
scope.27.id=bWV0aG9kOkdyZWVkbyNzdGF0aW9uUmVzZXJ2ZSg0KToxMjk
scope.27.kind=method
scope.27.startLine=129
scope.27.endLine=134
scope.27.semanticHash=9ac1c21b98492b70340d70447c0f9c26cf4ffbf96cf2764b4cddaef2da866542
scope.28.id=bWV0aG9kOkdyZWVkbyN3b3VsZFdpbkJ5QmFua3J1cHRjeSg1KToxNjg
scope.28.kind=method
scope.28.startLine=168
scope.28.endLine=178
scope.28.semanticHash=e2b363a88387a3d6ad085793bf4c37b59b4abfc1d5108642ba9890ef2be08b7a
*/
