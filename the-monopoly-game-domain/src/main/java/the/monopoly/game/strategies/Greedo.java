package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;

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
            || offer.available().minus(offer.land().price()).covers(reserve));
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
  public Money bidFor(Offer offer) {
    if (offer.utilityMonopolyOpportunity()) {
      return offer.available();
    }
    return new Money(Math.max(0, offer.available().amount() - reserve.amount()));
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
moduleHash=1f1184eddc1a9191b78e8c42a761532e5c703828cce8c1ee3ee1e6d4b1ebdf16
scope.0.id=Y2xhc3M6R3JlZWRvI0dyZWVkbzo5
scope.0.kind=class
scope.0.startLine=9
scope.0.endLine=59
scope.0.semanticHash=36990f0d50ae5d477976b2e16043aa7813ba437dc9cffa0ac8c975e2c60c5cc0
scope.1.id=ZmllbGQ6R3JlZWRvI3Jlc2VydmU6MTA
scope.1.kind=field
scope.1.startLine=10
scope.1.endLine=10
scope.1.semanticHash=022151ff347dc108fc1ab96c86b31089da850eb8390d5d07ac5112987a203360
scope.2.id=bWV0aG9kOkdyZWVkbyNhY2NlcHRzKDEpOjIw
scope.2.kind=method
scope.2.startLine=20
scope.2.endLine=25
scope.2.semanticHash=5cdfecb2f4fede471f1520f3d6e5dcc93ca699658a70dd99ad69f9c0d3415f55
scope.3.id=bWV0aG9kOkdyZWVkbyNiaWRGb3IoMSk6Mzc
scope.3.kind=method
scope.3.startLine=37
scope.3.endLine=43
scope.3.semanticHash=78c15163d7b06b10a99243e40bb889aff5e5a3a927f72d6329edaa11eda50245
scope.4.id=bWV0aG9kOkdyZWVkbyNidWlsZHMoMSk6NTA
scope.4.kind=method
scope.4.startLine=50
scope.4.endLine=53
scope.4.semanticHash=4f01974523df4662a52d66f5493b517a0923103fb4f0c05c789f0e20a2161ba2
scope.5.id=bWV0aG9kOkdyZWVkbyNjYXNoUmVzZXJ2ZSgwKTozMg
scope.5.kind=method
scope.5.startLine=32
scope.5.endLine=35
scope.5.semanticHash=71dea961caede545156063acfb60aaa924230b06a5f11c4e9ed9bcd22e65e869
scope.6.id=bWV0aG9kOkdyZWVkbyNjbGFpbXMoMSk6NDU
scope.6.kind=method
scope.6.startLine=45
scope.6.endLine=48
scope.6.semanticHash=507d9cac6317e7a85e3b057aed54560aaf53551ff97de821929348d3149eed56
scope.7.id=bWV0aG9kOkdyZWVkbyNjdG9yKDApOjEy
scope.7.kind=method
scope.7.startLine=12
scope.7.endLine=14
scope.7.semanticHash=8e0dfc0d7e56fcd16cf8a8a8dfff1de55d02172f0297df80ceafe07c3d1d2c20
scope.8.id=bWV0aG9kOkdyZWVkbyNjdG9yKDEpOjE2
scope.8.kind=method
scope.8.startLine=16
scope.8.endLine=18
scope.8.semanticHash=23927d22efbd903d78b3a7002338ce57323ddbcdd111445385b6a595fff1f969
scope.9.id=bWV0aG9kOkdyZWVkbyNkZWNsaW5lUmVhc29uKDEpOjI3
scope.9.kind=method
scope.9.startLine=27
scope.9.endLine=30
scope.9.semanticHash=f3aee34dfd7033144dfa3221a6e458baaa34e85c3674c1bbd03e01950d231e18
scope.10.id=bWV0aG9kOkdyZWVkbyNwYXlzKDEpOjU1
scope.10.kind=method
scope.10.startLine=55
scope.10.endLine=58
scope.10.semanticHash=2582c1709534ea40e6b58368ac921704c006661aea5482e52aa350caa137bc3b
*/
