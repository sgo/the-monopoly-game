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
}

/* mutate4java-manifest
version=1
moduleHash=aea5f218c4cf8b38fa2ba0936f5270cdc51d7fbe3b5534ea9532dc5451660a41
scope.0.id=Y2xhc3M6QWdyZWVJZkFmZm9yZGFibGUjQWdyZWVJZkFmZm9yZGFibGU6OQ
scope.0.kind=class
scope.0.startLine=9
scope.0.endLine=19
scope.0.semanticHash=59f1a533c271f9e800d500c8632fc3fea59f7446e74d2a2318bcd8b16b1b4810
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
scope.3.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2N0b3IoMCk6OQ
scope.3.kind=method
scope.3.startLine=1
scope.3.endLine=19
scope.3.semanticHash=45d3cdaa87d8ff4ac5bc65b90aac8eede3943be713fd1be9cdf58264fce0cc17
*/
