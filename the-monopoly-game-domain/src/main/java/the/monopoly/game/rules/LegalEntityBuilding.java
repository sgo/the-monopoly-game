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

  static LegalEntity.Operation buildAsMuchAsAffordable(LegalEntity entity, Deeds deeds,
                                                        Strategy.OfPlayers strategies, Rule.Set rules) {
    if (canPrepareBuildCommitment(entity, strategies, rules))
      prepareBuildCommitment(entity, strategies, rules, deeds);
    List<ColourStreet> plan = affordableBuildPlan(entity, deeds);
    if (plan.isEmpty()) return null;

    Money shortfall = totalConstructionCost(plan).minus(entity.bankBalance());
    Money loanRaised = financeShortfall(entity, shortfall, strategies, rules, deeds);
    plan.forEach(street -> buildOneImprovement(entity, deeds, street));

    ColourStreet firstBuilt = plan.getFirst();
    return loanRaised.equals(Money.ZERO)
        ? new LegalEntity.Operation.HouseBuilt(firstBuilt)
        : new LegalEntity.Operation.LoanRaisedAndHouseBuilt(loanRaised, firstBuilt);
  }

  private static boolean canPrepareBuildCommitment(LegalEntity entity, Strategy.OfPlayers strategies, Rule.Set rules) {
    return strategies != null && rules != null
        && entity.buildCommitmentsEmpty() && entity.bankBalance().equals(Money.ZERO);
  }

  private static Money financeShortfall(LegalEntity entity, Money shortfall, Strategy.OfPlayers strategies,
                                        Rule.Set rules, Deeds deeds) {
    if (shortfall.amount() <= 0) return Money.ZERO;
    if (strategies != null && rules != null) commitToBuildIfAllAgree(entity, shortfall, strategies, rules, deeds);
    return borrowShortfall(entity, shortfall);
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
      if (cannotExtendPlan(entity, shortfall, startedWithTreasuryFunds, plan)) break;
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

  private static boolean cannotExtendPlan(LegalEntity entity, Money shortfall, boolean startedWithTreasuryFunds,
                                          List<ColourStreet> plan) {
    if (shortfall.amount() <= 0) return false;
    if (startedWithTreasuryFunds && !plan.isEmpty()
        && (entity.buildCommitmentsEmpty() || plan.size() >= entity.streets().size())) return true;
    return !canBorrowForBuilding(entity, shortfall);
  }

  private static Money totalConstructionCost(List<ColourStreet> plan) {
    return plan.stream().map(ColourStreet::houseConstructionCost).reduce(Money.ZERO, Money::plus);
  }

  private static ColourStreet cheapestBuildableStreet(LegalEntity entity, Deeds deeds, List<ColourStreet> plan,
                                                       boolean canReachHotels) {
    return entity.streets().stream()
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

  private static void prepareBuildCommitment(LegalEntity entity, Strategy.OfPlayers strategies, Rule.Set rules,
                                             Deeds deeds) {
    Money amount = maximumHotelCost(entity);
    if (!allAgreeToBuild(entity, amount, strategies, rules, deeds)) amount = standardBuildCost(entity);
    if (allAgreeToBuild(entity, amount, strategies, rules, deeds)) commitSharesToBuild(entity, amount);
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
      Money reserve = strategy.cashReserve(shareholder, rules, deeds);
      return strategy.commitToEntityBuild(new Strategy.EntityBuildOffer(
          shares.get(index), shareholder.account().balance().amount(), reserve));
    });
  }

  private static Money maximumHotelCost(LegalEntity entity) {
    return entity.streets().stream()
        .map(street -> new Money(street.houseConstructionCost().amount()
            * (street.hotelConstructionRequiresNumberOfHouses() + 1)))
        .reduce(Money.ZERO, Money::plus);
  }

  private static Money standardBuildCost(LegalEntity entity) {
    return entity.streets().stream().map(ColourStreet::houseConstructionCost).reduce(Money.ZERO, Money::plus);
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
moduleHash=0d271f9b096f6a624ef3181e4a0d4c887b6f263afc7217c002f9a34ec98d3a60
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHlCdWlsZGluZyNMZWdhbEVudGl0eUJ1aWxkaW5nOjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=186
scope.0.semanticHash=77dc7bee23753a559e0addd18327ba42bf831fe601c5d29ebb416e0769da8587
scope.1.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYWZmb3JkYWJsZUJ1aWxkUGxhbigyKTo0Nw
scope.1.kind=method
scope.1.startLine=47
scope.1.endLine=62
scope.1.semanticHash=7341fe80104374b88e93ed5a556b230e4633b991d7d9e65e0d689ad4630bf16d
scope.2.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYWxsQWdyZWVUb0J1aWxkKDUpOjEzMA
scope.2.kind=method
scope.2.startLine=130
scope.2.endLine=141
scope.2.semanticHash=08e110dd0bc97a8496fd72786b7ef33526d625dc9fc7391e2e4bf496abfb206c
scope.3.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYm9ycm93U2hvcnRmYWxsKDIpOjE1NA
scope.3.kind=method
scope.3.startLine=154
scope.3.endLine=168
scope.3.semanticHash=eac72483452516f4515f71c0332ca5b829932984631e046be7d4420f0e15c014
scope.4.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYnVpbGRBc011Y2hBc0FmZm9yZGFibGUoNCk6MTg
scope.4.kind=method
scope.4.startLine=18
scope.4.endLine=33
scope.4.semanticHash=d2c234398087e6111408e23f3c79a4688fc9b32b383ae66a52e6245927c93e3d
scope.5.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYnVpbGRPbmVJbXByb3ZlbWVudCgzKToxODA
scope.5.kind=method
scope.5.startLine=180
scope.5.endLine=185
scope.5.semanticHash=054877cf2bd99791889fa34ae3e0bfbb587a97144dc144de6e9d15f603757fe0
scope.6.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuQm9ycm93Rm9yQnVpbGRpbmcoMik6OTg
scope.6.kind=method
scope.6.startLine=98
scope.6.endLine=105
scope.6.semanticHash=4b4000c08d78ed9f80c92459bc70f54e422fa68226456522feead8ea483308a4
scope.7.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuUHJlcGFyZUJ1aWxkQ29tbWl0bWVudCgzKTozNQ
scope.7.kind=method
scope.7.startLine=35
scope.7.endLine=38
scope.7.semanticHash=d0fb4069cb41eaa5031942a6c3666845ca09f1621a6a0e253ec4b3eb025b70a7
scope.8.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuUmVhY2hIb3RlbHMoMSk6NjQ
scope.8.kind=method
scope.8.startLine=64
scope.8.endLine=69
scope.8.semanticHash=127bbd897ebbb82e439cd868fcca15a22e3895229700cefd5b9ff653fe5fa9d2
scope.9.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2Fubm90RXh0ZW5kUGxhbig0KTo3MQ
scope.9.kind=method
scope.9.startLine=71
scope.9.endLine=77
scope.9.semanticHash=4d23bda308aad284247cdd937208546a62991a24066489b5a28779a2adbded70
scope.10.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2hlYXBlc3RCdWlsZGFibGVTdHJlZXQoNCk6ODM
scope.10.kind=method
scope.10.startLine=83
scope.10.endLine=96
scope.10.semanticHash=bb9e7222519708a489b4103fb7e808cc687f49ade0adb4ef2429105de0634648
scope.11.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY29tbWl0U2hhcmVzVG9CdWlsZCgyKToxMjM
scope.11.kind=method
scope.11.startLine=123
scope.11.endLine=128
scope.11.semanticHash=28759954d2d6622f2033a4fcc9177da581121a1b77ecda6a1aaf653d8a185cd8
scope.12.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY29tbWl0VG9CdWlsZElmQWxsQWdyZWUoNSk6MTA3
scope.12.kind=method
scope.12.startLine=107
scope.12.endLine=114
scope.12.semanticHash=a19b99baa0c9e79c6e8f1d75c9dc9a7297e35bf6c1f0bc489f7f89fc78e123c7
scope.13.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY3RvcigwKToxNQ
scope.13.kind=method
scope.13.startLine=15
scope.13.endLine=16
scope.13.semanticHash=9a6b28c20f07b8fe1a2372a21f47930525f49ccb87da3f66f30bbdafc9e7b929
scope.14.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjZmluYW5jZVNob3J0ZmFsbCg1KTo0MA
scope.14.kind=method
scope.14.startLine=40
scope.14.endLine=45
scope.14.semanticHash=09fbb6fa80fb3f97fa6a51c91199b10f1993e821d9919b74217b74d305a22792
scope.15.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjbWF4aW11bUhvdGVsQ29zdCgxKToxNDM
scope.15.kind=method
scope.15.startLine=143
scope.15.endLine=148
scope.15.semanticHash=d1f2340b403b8b41396011848e4ec4cb3793e0a9237d6f0bd2d0597c5cfbc042
scope.16.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjcHJlcGFyZUJ1aWxkQ29tbWl0bWVudCg0KToxMTY
scope.16.kind=method
scope.16.startLine=116
scope.16.endLine=121
scope.16.semanticHash=0503af461aae146ab272a828e759c2fd928d5c14b198e29ce378869c2aa7dd77
scope.17.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjc2hhcmVzT2YoMik6MTcx
scope.17.kind=method
scope.17.startLine=171
scope.17.endLine=178
scope.17.semanticHash=67971fcc300c2210733b86d9c0a053dae49907d830d9bd261f0f426481c50821
scope.18.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjc3RhbmRhcmRCdWlsZENvc3QoMSk6MTUw
scope.18.kind=method
scope.18.startLine=150
scope.18.endLine=152
scope.18.semanticHash=f68042b0d40f04446e4e06eecf9c6e563c14d88adfe3140e738220c90ce3f82d
scope.19.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjdG90YWxDb25zdHJ1Y3Rpb25Db3N0KDEpOjc5
scope.19.kind=method
scope.19.startLine=79
scope.19.endLine=81
scope.19.semanticHash=b1fb0dfdd2d2cc14a81357a808b7754004259e06baec2834b0fc817e12fc9fc9
*/
