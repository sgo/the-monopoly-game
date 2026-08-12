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
    return allAgreeToBuild(entity, standardBuildCost(entity), strategies, rules, deeds);
  }

  static LegalEntity.Operation buildAsMuchAsAffordable(LegalEntity entity, Deeds deeds,
                                                        Strategy.OfPlayers strategies, Rule.Set rules) {
    List<ColourStreet> plan = solicitCommitmentIfNeeded(entity, deeds, strategies, rules,
        affordableBuildPlan(entity, deeds));
    if (plan.isEmpty()) return null;

    Money shortfall = totalConstructionCost(plan).minus(entity.bankBalance());
    Money loanRaised = financeShortfall(entity, shortfall, strategies, rules, deeds);
    plan.forEach(street -> buildOneImprovement(entity, deeds, street));

    ColourStreet firstBuilt = plan.getFirst();
    return loanRaised.equals(Money.ZERO)
        ? new LegalEntity.Operation.HouseBuilt(firstBuilt)
        : new LegalEntity.Operation.LoanRaisedAndHouseBuilt(loanRaised, firstBuilt);
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
    Money required = standardBuildCost(entity);
    return required.exceeds(entity.bankBalance()) ? required.minus(entity.bankBalance()) : Money.ZERO;
  }

  private static boolean canPrepareBuildCommitment(LegalEntity entity, Strategy.OfPlayers strategies, Rule.Set rules) {
    return strategies != null && rules != null && entity.buildCommitmentsEmpty()
        && entity.shareholders().stream().allMatch(player -> strategies.forPlayer(player).legalEntityTradingEnabled());
  }

  private static Money financeShortfall(LegalEntity entity, Money shortfall, Strategy.OfPlayers strategies,
                                        Rule.Set rules, Deeds deeds) {
    if (shortfall.amount() <= 0) return Money.ZERO;
    if (strategies != null && rules != null) {
      commitToBuildIfAllAgree(entity, shortfall, strategies, rules, deeds);
      if (!canBorrowForBuilding(entity, shortfall)) return Money.ZERO;
    }
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
moduleHash=f5b46608b645c9818745c4b6109a695209ddf6b57accd941cb15940aac52cf71
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHlCdWlsZGluZyNMZWdhbEVudGl0eUJ1aWxkaW5nOjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=200
scope.0.semanticHash=cba87a253ca659ee632a5e40d62722ac7b53479a6f513988e1ef1efc34c1cbdb
scope.1.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYWZmb3JkYWJsZUJ1aWxkUGxhbigyKTo3NQ
scope.1.kind=method
scope.1.startLine=75
scope.1.endLine=90
scope.1.semanticHash=7341fe80104374b88e93ed5a556b230e4633b991d7d9e65e0d689ad4630bf16d
scope.2.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYWxsQWdyZWVUb0J1aWxkKDUpOjE1MQ
scope.2.kind=method
scope.2.startLine=151
scope.2.endLine=162
scope.2.semanticHash=3d2d539fcfd790af0eacf3a3cea7fcfe6cdcf1387cd4806af76112c6fb995145
scope.3.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYW1vdW50TmVlZGVkVG9Db250aW51ZSgzKTo1NQ
scope.3.kind=method
scope.3.startLine=55
scope.3.endLine=58
scope.3.semanticHash=55962af0114de3dec8697f8cb39cc8e96f8de8413cd198c2e24ce5e8b049171f
scope.4.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYm9ycm93U2hvcnRmYWxsKDIpOjE2OA
scope.4.kind=method
scope.4.startLine=168
scope.4.endLine=182
scope.4.semanticHash=eac72483452516f4515f71c0332ca5b829932984631e046be7d4420f0e15c014
scope.5.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYnVpbGRBc011Y2hBc0FmZm9yZGFibGUoNCk6MjQ
scope.5.kind=method
scope.5.startLine=24
scope.5.endLine=38
scope.5.semanticHash=f44b45370c1aca89b9ce2ae36ddf8d2a2e1eff8cf228fc6a11d035614429993d
scope.6.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjYnVpbGRPbmVJbXByb3ZlbWVudCgzKToxOTQ
scope.6.kind=method
scope.6.startLine=194
scope.6.endLine=199
scope.6.semanticHash=054877cf2bd99791889fa34ae3e0bfbb587a97144dc144de6e9d15f603757fe0
scope.7.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuQm9ycm93Rm9yQnVpbGRpbmcoMik6MTI2
scope.7.kind=method
scope.7.startLine=126
scope.7.endLine=133
scope.7.semanticHash=4b4000c08d78ed9f80c92459bc70f54e422fa68226456522feead8ea483308a4
scope.8.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuRnVuZE5leHRJbXByb3ZlbWVudCg0KToxOQ
scope.8.kind=method
scope.8.startLine=19
scope.8.endLine=22
scope.8.semanticHash=af99f2dd1775c27c78b68f49444d0a36148db98e95833130ec1a534892427eb8
scope.9.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuUHJlcGFyZUJ1aWxkQ29tbWl0bWVudCgzKTo2MA
scope.9.kind=method
scope.9.startLine=60
scope.9.endLine=63
scope.9.semanticHash=0fad891e079bd0b32c9e0bde9fd0be5efade7295a436605cf7cbe1e5d42d2e54
scope.10.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2FuUmVhY2hIb3RlbHMoMSk6OTI
scope.10.kind=method
scope.10.startLine=92
scope.10.endLine=97
scope.10.semanticHash=127bbd897ebbb82e439cd868fcca15a22e3895229700cefd5b9ff653fe5fa9d2
scope.11.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2Fubm90RXh0ZW5kUGxhbig0KTo5OQ
scope.11.kind=method
scope.11.startLine=99
scope.11.endLine=105
scope.11.semanticHash=4d23bda308aad284247cdd937208546a62991a24066489b5a28779a2adbded70
scope.12.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY2hlYXBlc3RCdWlsZGFibGVTdHJlZXQoNCk6MTEx
scope.12.kind=method
scope.12.startLine=111
scope.12.endLine=124
scope.12.semanticHash=bb9e7222519708a489b4103fb7e808cc687f49ade0adb4ef2429105de0634648
scope.13.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY29tbWl0U2hhcmVzVG9CdWlsZCgyKToxNDQ
scope.13.kind=method
scope.13.startLine=144
scope.13.endLine=149
scope.13.semanticHash=28759954d2d6622f2033a4fcc9177da581121a1b77ecda6a1aaf653d8a185cd8
scope.14.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY29tbWl0VG9CdWlsZElmQWxsQWdyZWUoNSk6MTM1
scope.14.kind=method
scope.14.startLine=135
scope.14.endLine=142
scope.14.semanticHash=a19b99baa0c9e79c6e8f1d75c9dc9a7297e35bf6c1f0bc489f7f89fc78e123c7
scope.15.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjY3RvcigwKToxNQ
scope.15.kind=method
scope.15.startLine=15
scope.15.endLine=16
scope.15.semanticHash=9a6b28c20f07b8fe1a2372a21f47930525f49ccb87da3f66f30bbdafc9e7b929
scope.16.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjZmluYW5jZVNob3J0ZmFsbCg1KTo2NQ
scope.16.kind=method
scope.16.startLine=65
scope.16.endLine=73
scope.16.semanticHash=2f6b6ee7372f32b887f6455e9c5233d585e2d5c88e8e428d337790bb487ed153
scope.17.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjc2hhcmVzT2YoMik6MTg1
scope.17.kind=method
scope.17.startLine=185
scope.17.endLine=192
scope.17.semanticHash=67971fcc300c2210733b86d9c0a053dae49907d830d9bd261f0f426481c50821
scope.18.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjc29saWNpdENvbW1pdG1lbnRJZk5lZWRlZCg1KTo0NQ
scope.18.kind=method
scope.18.startLine=45
scope.18.endLine=53
scope.18.semanticHash=d6f004195d811a2191882a3c24dce38cc7465a40119a8910f2b9768f473a3bf5
scope.19.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjc3RhbmRhcmRCdWlsZENvc3QoMSk6MTY0
scope.19.kind=method
scope.19.startLine=164
scope.19.endLine=166
scope.19.semanticHash=f68042b0d40f04446e4e06eecf9c6e563c14d88adfe3140e738220c90ce3f82d
scope.20.id=bWV0aG9kOkxlZ2FsRW50aXR5QnVpbGRpbmcjdG90YWxDb25zdHJ1Y3Rpb25Db3N0KDEpOjEwNw
scope.20.kind=method
scope.20.startLine=107
scope.20.endLine=109
scope.20.semanticHash=b1fb0dfdd2d2cc14a81357a808b7754004259e06baec2834b0fc817e12fc9fc9
*/
