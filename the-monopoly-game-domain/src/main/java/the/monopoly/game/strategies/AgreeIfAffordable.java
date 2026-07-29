package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;

/**
 * Agrees to whatever it has the means to pay for: it buys land it can afford,
 * and at auction it bids the most it can afford rather than lose the land.
 */
public final class AgreeIfAffordable implements Strategy {
  @Override
  public boolean accepts(Offer offer) {
    return offer.isAffordable();
  }

  @Override
  public Money bidFor(Offer offer) {
    return offer.available();
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
moduleHash=90264cd813ce726ded4d8d7601387a987d22e63a1955acd867b92cc0ba1591dc
scope.0.id=Y2xhc3M6QWdyZWVJZkFmZm9yZGFibGUjQWdyZWVJZkFmZm9yZGFibGU6OQ
scope.0.kind=class
scope.0.startLine=9
scope.0.endLine=29
scope.0.semanticHash=0900f94326761b685b05aa5baff8f410ca73558826b17870b49ae0e4cdbe1167
scope.1.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2FjY2VwdHMoMSk6MTA
scope.1.kind=method
scope.1.startLine=10
scope.1.endLine=13
scope.1.semanticHash=311266ba536590363a0c153deefba4e7a897e5823cb6347c74b4d3799abb4fbc
scope.2.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2JpZEZvcigxKToxNQ
scope.2.kind=method
scope.2.startLine=15
scope.2.endLine=18
scope.2.semanticHash=05cf56723033ae6cd91dfe0c0ea7f4a4fc24266e132d960f7c7f715e3ed573d7
scope.3.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2J1aWxkcygxKToyNQ
scope.3.kind=method
scope.3.startLine=25
scope.3.endLine=28
scope.3.semanticHash=4f01974523df4662a52d66f5493b517a0923103fb4f0c05c789f0e20a2161ba2
scope.4.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2NsYWltcygxKToyMA
scope.4.kind=method
scope.4.startLine=20
scope.4.endLine=23
scope.4.semanticHash=507d9cac6317e7a85e3b057aed54560aaf53551ff97de821929348d3149eed56
scope.5.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2N0b3IoMCk6OQ
scope.5.kind=method
scope.5.startLine=1
scope.5.endLine=29
scope.5.semanticHash=5b0a01d544033f1ee8de08bf41fcbdf3d720cfbd7ac5c18c9b0497092ce47728
*/
