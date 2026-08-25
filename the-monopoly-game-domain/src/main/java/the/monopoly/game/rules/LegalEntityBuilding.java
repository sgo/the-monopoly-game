package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/** Decides how much a legal entity builds on an operation, and how shareholders finance the shortfall. */
final class LegalEntityBuilding {
  private LegalEntityBuilding() {
  }

  /** Whether every shareholder would fund their share of the entity's next standard-cost improvement. */
  static boolean canFundNextImprovement(LegalEntity entity, Strategy.OfPlayers strategies, Rule.Set rules,
                                        Deeds deeds) {
    return ownedStreets(entity, deeds).stream().anyMatch(street -> !deeds.hasHotelOn(street))
        && allAgreeToBuild(entity, standardBuildCost(entity, deeds), strategies, rules, deeds);
  }

  static LegalEntity.Operation buildAsMuchAsAffordable(LegalEntity entity, Deeds deeds,
                                                        Strategy.OfPlayers strategies, Rule.Set rules) {
    return buildAsMuchAsAffordable(entity, deeds, strategies, rules, null, List.of());
  }

  static LegalEntity.Operation buildAsMuchAsAffordable(LegalEntity entity, Deeds deeds,
                                                        Strategy.OfPlayers strategies, Rule.Set rules,
                                                        DevelopmentLoanBook developmentLoanBook,
                                                        List<Player> players) {
    List<ColourStreet> plan = solicitCommitmentIfNeeded(entity, deeds, strategies, rules,
        affordableBuildPlan(entity, deeds));
    if (developmentLoanBook != null && players != null
        && entity.shareholders().stream().anyMatch(player -> strategies.forPlayer(player).developmentLoansEnabled())) {
      if (plan.isEmpty()) {
        ColourStreet candidate = cheapestBuildableStreet(entity, deeds, plan, canReachHotels(entity));
        if (candidate != null) plan = append(plan, candidate);
      }
    }
    if (plan.isEmpty()) return null;

    Money shortfall = totalConstructionCost(plan).minus(entity.bankBalance());
    Financing financing = financeShortfall(entity, plan, shortfall, strategies, rules, deeds,
        developmentLoanBook, players);
    if (shortfall.amount() > 0 && financing.amount().equals(Money.ZERO) && financing.position() == null) return null;
    plan.forEach(street -> buildOneImprovement(entity, deeds, street));

    ColourStreet firstBuilt = plan.getFirst();
    if (financing.position() != null)
      return new LegalEntity.Operation.DevelopmentLoanRaisedAndHouseBuilt(financing.position(), firstBuilt);
    return financing.amount().equals(Money.ZERO)
        ? new LegalEntity.Operation.HouseBuilt(firstBuilt)
        : new LegalEntity.Operation.LoanRaisedAndHouseBuilt(financing.amount(), firstBuilt);
  }

  /**
   * Asks eligible shareholders to fund the shortfall, and recomputes the plan
   * against their commitment, when the entity's own treasury cannot reach
   * every street it could otherwise afford to build on.
   */
  private static List<ColourStreet> solicitCommitmentIfNeeded(LegalEntity entity, Deeds deeds,
                                                               Strategy.OfPlayers strategies, Rule.Set rules,
                                                               List<ColourStreet> plan) {
    if (!canPrepareBuildCommitment(entity, strategies, rules)) return plan;
    Money amount = amountNeededToContinue(entity, deeds, plan);
    if (amount.equals(Money.ZERO) || !allAgreeToBuild(entity, amount, strategies, rules, deeds)) return plan;
    commitSharesToBuild(entity, amount);
    return affordableBuildPlan(entity, deeds);
  }

  private static Money amountNeededToContinue(LegalEntity entity, Deeds deeds, List<ColourStreet> plan) {
    Money required = standardBuildCost(entity, deeds);
    return required.exceeds(entity.bankBalance()) ? required.minus(entity.bankBalance()) : Money.ZERO;
  }

  private static boolean canPrepareBuildCommitment(LegalEntity entity, Strategy.OfPlayers strategies, Rule.Set rules) {
    return strategies != null && rules != null && entity.buildCommitmentsEmpty()
        && entity.shareholders().stream().allMatch(player -> strategies.forPlayer(player).legalEntityTradingEnabled());
  }

  private static Financing financeShortfall(LegalEntity entity, List<ColourStreet> plan, Money shortfall,
                                             Strategy.OfPlayers strategies, Rule.Set rules, Deeds deeds,
                                             DevelopmentLoanBook developmentLoanBook, List<Player> players) {
    if (shortfall.amount() <= 0) return Financing.none();
    if (strategies != null && rules != null) {
      commitToBuildIfAllAgree(entity, shortfall, strategies, rules, deeds);
      if (canBorrowForBuilding(entity, shortfall)) return new Financing(borrowShortfall(entity, shortfall), null);
      if (developmentLoanBook != null && players != null && !plan.isEmpty()
          && entity.shareholders().stream().anyMatch(player -> strategies.forPlayer(player).developmentLoansEnabled())) {
        return developmentLoanBook.raise(entity, totalConstructionCost(plan), plan.getFirst().type(),
                entity.shareholders().stream().anyMatch(player -> strategies.forPlayer(player).fullDrawDevelopmentLoans()), players)
            .map(position -> new Financing(position.loan().originalPrincipal(), position))
            .orElseGet(Financing::none);
      }
      return Financing.none();
    }
    return new Financing(borrowShortfall(entity, shortfall), null);
  }

  private static List<ColourStreet> affordableBuildPlan(LegalEntity entity, Deeds deeds) {
    List<ColourStreet> plan = new ArrayList<>();
    Money totalCost = Money.ZERO;
    boolean startedWithTreasuryFunds = entity.bankBalance().amount() > 0;
    boolean canReachHotels = canReachHotels(entity);
    while (true) {
      ColourStreet next = cheapestBuildableStreet(entity, deeds, plan, canReachHotels);
      if (next == null) break;
      Money candidateCost = totalCost.plus(next.houseConstructionCost());
      Money shortfall = candidateCost.minus(entity.bankBalance());
      if (cannotExtendPlan(entity, deeds, shortfall, startedWithTreasuryFunds, plan)) break;
      plan.add(next);
      totalCost = candidateCost;
    }
    return plan;
  }

  private static boolean canReachHotels(LegalEntity entity) {
    if (!entity.loan().equals(Money.ZERO)) return false;
    if (!entity.buildCommitmentsEmpty()) return true;
    return entity.lastCapitalizedShareholder() == null
        && entity.bankBalance().amount() > 0 && entity.bankBalance().amount() < 150;
  }

  private static boolean cannotExtendPlan(LegalEntity entity, Deeds deeds, Money shortfall, boolean startedWithTreasuryFunds,
                                          List<ColourStreet> plan) {
    if (shortfall.amount() <= 0) return false;
    if (startedWithTreasuryFunds && !plan.isEmpty()
        && (entity.buildCommitmentsEmpty() || plan.size() >= ownedStreets(entity, deeds).size())) return true;
    return !canBorrowForBuilding(entity, shortfall);
  }

  private static List<ColourStreet> append(List<ColourStreet> plan, ColourStreet street) {
    List<ColourStreet> extended = new ArrayList<>(plan);
    extended.add(street);
    return extended;
  }

  private static Money totalConstructionCost(List<ColourStreet> plan) {
    return plan.stream().map(ColourStreet::houseConstructionCost).reduce(Money.ZERO, Money::plus);
  }

  private static ColourStreet cheapestBuildableStreet(LegalEntity entity, Deeds deeds, List<ColourStreet> plan,
                                                       boolean canReachHotels) {
    return ownedStreets(entity, deeds).stream()
        .filter(street -> !deeds.hasHotelOn(street)
            && ((deeds.housesBuiltOn(street)
                + (int) plan.stream().filter(street::equals).count())
                < street.hotelConstructionRequiresNumberOfHouses()
                || (canReachHotels && deeds.housesBuiltOn(street)
                    + (int) plan.stream().filter(street::equals).count()
                    == street.hotelConstructionRequiresNumberOfHouses())))
        .min(Comparator.comparingInt(street -> deeds.housesBuiltOn(street)
            + (int) plan.stream().filter(street::equals).count()))
        .orElse(null);
  }

  private static boolean canBorrowForBuilding(LegalEntity entity, Money shortfall) {
    if (!entity.hasShareholders() || !entity.loan().equals(Money.ZERO)) return false;
    List<Player> shareholders = entity.shareholders();
    List<Money> shares = sharesOf(shareholders, shortfall);
    return IntStream.range(0, shareholders.size()).allMatch(index ->
        shareholders.get(index).account().balance().amount().amount() >= shares.get(index).amount()
            && entity.buildCommitmentOf(shareholders.get(index)).amount() >= shares.get(index).amount());
  }

  private static void commitToBuildIfAllAgree(LegalEntity entity, Money shortfall, Strategy.OfPlayers strategies,
                                              Rule.Set rules, Deeds deeds) {
    if (!allAgreeToBuild(entity, shortfall, strategies, rules, deeds)) {
      entity.clearBuildCommitments();
      return;
    }
    commitSharesToBuild(entity, shortfall);
  }

  private static void commitSharesToBuild(LegalEntity entity, Money amount) {
    List<Player> shareholders = entity.shareholders();
    List<Money> shares = sharesOf(shareholders, amount);
    for (int index = 0; index < shareholders.size(); index++)
      entity.commitToBuild(shareholders.get(index), shares.get(index));
  }

  private static boolean allAgreeToBuild(LegalEntity entity, Money amount, Strategy.OfPlayers strategies,
                                         Rule.Set rules, Deeds deeds) {
    List<Player> shareholders = entity.shareholders();
    List<Money> shares = sharesOf(shareholders, amount);
    return IntStream.range(0, shareholders.size()).allMatch(index -> {
      Player shareholder = shareholders.get(index);
      Strategy strategy = strategies.forPlayer(shareholder);
      Money reserve = strategy.cashReserve();
      return strategy.commitToEntityBuild(new Strategy.EntityBuildOffer(
          shares.get(index), shareholder.account().balance().amount(), reserve));
    });
  }

  private static Money standardBuildCost(LegalEntity entity, Deeds deeds) {
    return ownedStreets(entity, deeds).stream().filter(street -> !deeds.hasHotelOn(street))
        .map(ColourStreet::houseConstructionCost).reduce(Money.ZERO, Money::plus);
  }

  static List<ColourStreet> ownedStreets(LegalEntity entity, Deeds deeds) {
    if (deeds == null) return entity.streets();
    if (!deeds.legalEntities().contains(entity)) return entity.streets();
    return entity.streets().stream()
        .filter(street -> deeds.entityOwnerOf(street.type()).filter(entity::equals).isPresent())
        .toList();
  }

  private static Money borrowShortfall(LegalEntity entity, Money shortfall) {
    entity.recordLoan(shortfall);
    entity.depositToBank(shortfall);
    List<Player> shareholders = entity.shareholders();
    List<Money> shares = sharesOf(shareholders, shortfall);
    for (int index = 0; index < shareholders.size(); index++) {
      Player shareholder = shareholders.get(index);
      Money share = shares.get(index);
      shareholder.account().withdraw(share);
      entity.recordShareholderPayment(shareholder, share);
      if (!share.equals(Money.ZERO)) entity.recordCapitalization(shareholder);
    }
    entity.clearBuildCommitments();
    return shortfall;
  }

  private record Financing(Money amount, DevelopmentLoanBook.Position position) {
    private static Financing none() {
      return new Financing(Money.ZERO, null);
    }
  }

  /** Splits an amount across shareholders as evenly as possible; earlier shareholders absorb any remainder. */
  static List<Money> sharesOf(List<Player> shareholders, Money amount) {
    if (shareholders.isEmpty()) return List.of();
    int base = amount.amount() / shareholders.size();
    int remainder = amount.amount() % shareholders.size();
    return IntStream.range(0, shareholders.size())
        .mapToObj(index -> new Money(base + (index < remainder ? 1 : 0)))
        .toList();
  }

  private static void buildOneImprovement(LegalEntity entity, Deeds deeds, ColourStreet street) {
    entity.withdrawFromBank(street.houseConstructionCost());
    if (deeds.housesBuiltOn(street) == street.hotelConstructionRequiresNumberOfHouses())
      deeds.arrangeHotel(street);
    else deeds.arrangeHouses(street, deeds.housesBuiltOn(street) + 1);
  }
}

/* mutate4java-manifest
version=1
moduleHash=bd915cf8805b75d96ac51c0796b4621e81ffacbad62c6398a83a71ca29bd556b
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHlCdWlsZGluZyNMZWdhbEVudGl0eUJ1aWxkaW5nOjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=202
scope.0.semanticHash=de4510707f63f94c41a298fe49857e1e7e4d6cf8ea2a240ca2809bc369167b5c
scope.1.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYWZmb3JkYWJsZUJ1aWxkUGxhbigyKTo3Ng
scope.1.kind=method
scope.1.startLine=76
scope.1.endLine=91
scope.1.semanticHash=7341fe80104374b88e93ed5a556b230e4633b991d7d9e65e0d689ad4630bf16d
scope.2.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYWxsQWdyZWVUb0J1aWxkKDUpOjE1Mg
scope.2.kind=method
scope.2.startLine=152
scope.2.endLine=163
scope.2.semanticHash=3d2d539fcfd790af0eacf3a3cea7fcfe6cdcf1387cd4806af76112c6fb995145
scope.3.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYW1vdW50TmVlZGVkVG9Db250aW51ZSgzKTo1Ng
scope.3.kind=method
scope.3.startLine=56
scope.3.endLine=59
scope.3.semanticHash=7b4dceae5171f7ac195e99756ca17dac7b50f873ade52788b0fcf3fcc8c81053
scope.4.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYm9ycm93U2hvcnRmYWxsKDIpOjE3MA
scope.4.kind=method
scope.4.startLine=170
scope.4.endLine=184
scope.4.semanticHash=eac72483452516f4515f71c0332ca5b829932984631e046be7d4420f0e15c014
scope.5.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYnVpbGRBc011Y2hBc0FmZm9yZGFibGUoNCk6MjU
scope.5.kind=method
scope.5.startLine=25
scope.5.endLine=39
scope.5.semanticHash=f44b45370c1aca89b9ce2ae36ddf8d2a2e1eff8cf228fc6a11d035614429993d
scope.6.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYnVpbGRPbmVJbXByb3ZlbWVudCgzKToxOTY
scope.6.kind=method
scope.6.startLine=196
scope.6.endLine=201
scope.6.semanticHash=054877cf2bd99791889fa34ae3e0bfbb587a97144dc144de6e9d15f603757fe0
scope.7.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuQm9ycm93Rm9yQnVpbGRpbmcoMik6MTI3
scope.7.kind=method
scope.7.startLine=127
scope.7.endLine=134
scope.7.semanticHash=4b4000c08d78ed9f80c92459bc70f54e422fa68226456522feead8ea483308a4
scope.8.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuRnVuZE5leHRJbXByb3ZlbWVudCg0KToxOQ
scope.8.kind=method
scope.8.startLine=19
scope.8.endLine=23
scope.8.semanticHash=0ca382872e269e635f30172474ae779282de2d95ac46d851d00f14f96fb37830
scope.9.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuUHJlcGFyZUJ1aWxkQ29tbWl0bWVudCgzKTo2MQ
scope.9.kind=method
scope.9.startLine=61
scope.9.endLine=64
scope.9.semanticHash=0fad891e079bd0b32c9e0bde9fd0be5efade7295a436605cf7cbe1e5d42d2e54
scope.10.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuUmVhY2hIb3RlbHMoMSk6OTM
scope.10.kind=method
scope.10.startLine=93
scope.10.endLine=98
scope.10.semanticHash=127bbd897ebbb82e439cd868fcca15a22e3895229700cefd5b9ff653fe5fa9d2
scope.11.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2Fubm90RXh0ZW5kUGxhbig0KToxMDA
scope.11.kind=method
scope.11.startLine=100
scope.11.endLine=106
scope.11.semanticHash=4d23bda308aad284247cdd937208546a62991a24066489b5a28779a2adbded70
scope.12.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2hlYXBlc3RCdWlsZGFibGVTdHJlZXQoNCk6MTEy
scope.12.kind=method
scope.12.startLine=112
scope.12.endLine=125
scope.12.semanticHash=bb9e7222519708a489b4103fb7e808cc687f49ade0adb4ef2429105de0634648
scope.13.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY29tbWl0U2hhcmVzVG9CdWlsZCgyKToxNDU
scope.13.kind=method
scope.13.startLine=145
scope.13.endLine=150
scope.13.semanticHash=28759954d2d6622f2033a4fcc9177da581121a1b77ecda6a1aaf653d8a185cd8
scope.14.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY29tbWl0VG9CdWlsZElmQWxsQWdyZWUoNSk6MTM2
scope.14.kind=method
scope.14.startLine=136
scope.14.endLine=143
scope.14.semanticHash=a19b99baa0c9e79c6e8f1d75c9dc9a7297e35bf6c1f0bc489f7f89fc78e123c7
scope.15.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY3RvcigwKToxNQ
scope.15.kind=method
scope.15.startLine=15
scope.15.endLine=16
scope.15.semanticHash=9a6b28c20f07b8fe1a2372a21f47930525f49ccb87da3f66f30bbdafc9e7b929
scope.16.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjZmluYW5jZVNob3J0ZmFsbCg1KTo2Ng
scope.16.kind=method
scope.16.startLine=66
scope.16.endLine=74
scope.16.semanticHash=2f6b6ee7372f32b887f6455e9c5233d585e2d5c88e8e428d337790bb487ed153
scope.17.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjc2hhcmVzT2YoMik6MTg3
scope.17.kind=method
scope.17.startLine=187
scope.17.endLine=194
scope.17.semanticHash=67971fcc300c2210733b86d9c0a053dae49907d830d9bd261f0f426481c50821
scope.18.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjc29saWNpdENvbW1pdG1lbnRJZk5lZWRlZCg1KTo0Ng
scope.18.kind=method
scope.18.startLine=46
scope.18.endLine=54
scope.18.semanticHash=d6f004195d811a2191882a3c24dce38cc7465a40119a8910f2b9768f473a3bf5
scope.19.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjc3RhbmRhcmRCdWlsZENvc3QoMik6MTY1
scope.19.kind=method
scope.19.startLine=165
scope.19.endLine=168
scope.19.semanticHash=ca908ef7c31e06022507a52433c01d553078e43b6f0985d65cdeb010cbd6c4c8
scope.20.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjdG90YWxDb25zdHJ1Y3Rpb25Db3N0KDEpOjEwOA
scope.20.kind=method
scope.20.startLine=108
scope.20.endLine=110
scope.20.semanticHash=b1fb0dfdd2d2cc14a81357a808b7754004259e06baec2834b0fc817e12fc9fc9
*/
