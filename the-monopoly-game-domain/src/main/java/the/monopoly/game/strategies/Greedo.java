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

  public Greedo() {
    this(Money.ZERO);
  }

  public Greedo(Money reserve) {
    this.reserve = reserve;
  }

  @Override
  public boolean accepts(Offer offer) {
    return offer.isAffordable()
        && (offer.utilityMonopolyOpportunity()
            || offer.available().minus(offer.land().price()).covers(offer.reserve()));
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
    boolean completesOwnGroup = completesGroup(offer.land(), bidder, rules, deeds);
    if (completesOwnGroup) return offer.available();
    boolean deniesOpponent = priority(offer.land()) == Priority.HIGHEST;
    if (deniesOpponent) {
      int available = offer.available().amount();
      return new Money(Math.min(available, available * 35 / 100));
    }
    return Money.ZERO;
  }

  private boolean completesGroup(Ownable land, Player bidder, Rule.Set rules, Deeds deeds) {
    if (!(land instanceof ColourStreet street)) return false;
    return rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .filter(it -> it.colourGroup() == street.colourGroup())
        .allMatch(it -> it.type() == land.type() || deeds.ownerOf(it.type()).filter(bidder.id()::equals).isPresent());
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
moduleHash=7ef02265999d0511d48c2f83696e851fbff00600c6fdd6a6c2a2d430689d6c23
scope.0.id=Y2xhc3M6R3JlZWRvI0dyZWVkbzoxOA
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=150
scope.0.semanticHash=7ad105e6cb3f24cf8c522e87887de43d2aa66bf7be4c848095efe5f5da6e5cd3
scope.1.id=ZmllbGQ6R3JlZWRvI3Jlc2VydmU6MTk
scope.1.kind=field
scope.1.startLine=19
scope.1.endLine=19
scope.1.semanticHash=022151ff347dc108fc1ab96c86b31089da850eb8390d5d07ac5112987a203360
scope.2.id=bWV0aG9kOkdyZWVkbyNhY2NlcHRzKDEpOjI5
scope.2.kind=method
scope.2.startLine=29
scope.2.endLine=34
scope.2.semanticHash=3046213c02ec52cc24276e2e0c0268d5cee4d9f20ada852b38da09b73447c786
scope.3.id=bWV0aG9kOkdyZWVkbyNiaWRGb3IoMSk6MTI3
scope.3.kind=method
scope.3.startLine=127
scope.3.endLine=134
scope.3.semanticHash=bd82389a84803b1754cc19ad5b28c3a9af96ccb021cd5c0f2fb145cc8510349b
scope.4.id=bWV0aG9kOkdyZWVkbyNiaWRGb3JEaXN0cmVzc2VkKDYpOjk2
scope.4.kind=method
scope.4.startLine=96
scope.4.endLine=111
scope.4.semanticHash=24475d0eddc8d7042645682e31fc0f4c08a7a8699bc9dbbe1663bbffebecf50b
scope.5.id=bWV0aG9kOkdyZWVkbyNidWlsZHMoMSk6MTQx
scope.5.kind=method
scope.5.startLine=141
scope.5.endLine=144
scope.5.semanticHash=4f01974523df4662a52d66f5493b517a0923103fb4f0c05c789f0e20a2161ba2
scope.6.id=bWV0aG9kOkdyZWVkbyNjYXNoUmVzZXJ2ZSgwKTo0MQ
scope.6.kind=method
scope.6.startLine=41
scope.6.endLine=44
scope.6.semanticHash=71dea961caede545156063acfb60aaa924230b06a5f11c4e9ed9bcd22e65e869
scope.7.id=bWV0aG9kOkdyZWVkbyNjYXNoUmVzZXJ2ZSgzKTo0Ng
scope.7.kind=method
scope.7.startLine=46
scope.7.endLine=73
scope.7.semanticHash=03c67c129eeba91fe47e3ef9cd4d20214b076fc0f80d60333278eba72e079485
scope.8.id=bWV0aG9kOkdyZWVkbyNjbGFpbXMoMSk6MTM2
scope.8.kind=method
scope.8.startLine=136
scope.8.endLine=139
scope.8.semanticHash=507d9cac6317e7a85e3b057aed54560aaf53551ff97de821929348d3149eed56
scope.9.id=bWV0aG9kOkdyZWVkbyNjb21wbGV0ZXNHcm91cCg0KToxMTM
scope.9.kind=method
scope.9.startLine=113
scope.9.endLine=118
scope.9.semanticHash=b5194735d1b29a96372419c9f3867ba4c0a19797eb4c00ca4bcc5b6173dd0356
scope.10.id=bWV0aG9kOkdyZWVkbyNjdG9yKDApOjIx
scope.10.kind=method
scope.10.startLine=21
scope.10.endLine=23
scope.10.semanticHash=8e0dfc0d7e56fcd16cf8a8a8dfff1de55d02172f0297df80ceafe07c3d1d2c20
scope.11.id=bWV0aG9kOkdyZWVkbyNjdG9yKDEpOjI1
scope.11.kind=method
scope.11.startLine=25
scope.11.endLine=27
scope.11.semanticHash=23927d22efbd903d78b3a7002338ce57323ddbcdd111445385b6a595fff1f969
scope.12.id=bWV0aG9kOkdyZWVkbyNkZWNsaW5lUmVhc29uKDEpOjM2
scope.12.kind=method
scope.12.startLine=36
scope.12.endLine=39
scope.12.semanticHash=f3aee34dfd7033144dfa3221a6e458baaa34e85c3674c1bbd03e01950d231e18
scope.13.id=bWV0aG9kOkdyZWVkbyNwYXlzKDEpOjE0Ng
scope.13.kind=method
scope.13.startLine=146
scope.13.endLine=149
scope.13.semanticHash=2582c1709534ea40e6b58368ac921704c006661aea5482e52aa350caa137bc3b
scope.14.id=bWV0aG9kOkdyZWVkbyNwcmlvcml0eSgxKTo4Mw
scope.14.kind=method
scope.14.startLine=83
scope.14.endLine=94
scope.14.semanticHash=9e17a96df0b90750a02484d2fb9f21ca0d6efc20313fcf887f1f7e20cde04c22
scope.15.id=bWV0aG9kOkdyZWVkbyNwcmlvcml0eVRpZXIoMSk6NzU
scope.15.kind=method
scope.15.startLine=75
scope.15.endLine=81
scope.15.semanticHash=1fb89619bb56da0db017a4a8551e06eeda4f4cc5fea7977fc9e7d6ae5582c42d
scope.16.id=bWV0aG9kOkdyZWVkbyN3b3VsZFdpbkJ5QmFua3J1cHRjeSg0KToxMjA
scope.16.kind=method
scope.16.startLine=120
scope.16.endLine=125
scope.16.semanticHash=249a095a78ec8c872ba71b4db7654dd93d0d9ac3a7db634a7ed1f6a7d556b172
*/
