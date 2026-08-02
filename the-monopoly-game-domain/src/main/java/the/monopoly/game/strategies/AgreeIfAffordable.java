package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;

/**
 * Agrees to whatever it has the means to pay for: it buys land it can afford,
 * and at auction it bids the most it can afford rather than lose the land.
 */
public final class AgreeIfAffordable implements Strategy {
  private final Money reserve;

  public AgreeIfAffordable() {
    this(Money.ZERO);
  }

  public AgreeIfAffordable(Money reserve) {
    this.reserve = reserve;
  }

  @Override
  public boolean accepts(Offer offer) {
    return offer.isAffordable()
        && (offer.utilityMonopolyOpportunity()
            || offer.available().minus(offer.land().price()).covers(reserve));
  }

  @Override
  public Money cashReserve() {
    return reserve;
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
moduleHash=8fd23114809e3f8b1014836a27f4cd6039013f283ae69c759f8f10ec68527c46
scope.0.id=Y2xhc3M6QWdyZWVJZkFmZm9yZGFibGUjQWdyZWVJZkFmZm9yZGFibGU6OQ
scope.0.kind=class
scope.0.startLine=9
scope.0.endLine=51
scope.0.semanticHash=68457c0dcc31ee97c4d51b8a9a6f82ec3f4a642ba6d5fb745cdd9f5c5fba2762
scope.1.id=ZmllbGQ6QWdyZWVJZkFmZm9yZGFibGUjcmVzZXJ2ZToxMA
scope.1.kind=field
scope.1.startLine=10
scope.1.endLine=10
scope.1.semanticHash=022151ff347dc108fc1ab96c86b31089da850eb8390d5d07ac5112987a203360
scope.2.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2FjY2VwdHMoMSk6MjA
scope.2.kind=method
scope.2.startLine=20
scope.2.endLine=25
scope.2.semanticHash=5cdfecb2f4fede471f1520f3d6e5dcc93ca699658a70dd99ad69f9c0d3415f55
scope.3.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2JpZEZvcigxKTozMg
scope.3.kind=method
scope.3.startLine=32
scope.3.endLine=35
scope.3.semanticHash=05cf56723033ae6cd91dfe0c0ea7f4a4fc24266e132d960f7c7f715e3ed573d7
scope.4.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2J1aWxkcygxKTo0Mg
scope.4.kind=method
scope.4.startLine=42
scope.4.endLine=45
scope.4.semanticHash=4f01974523df4662a52d66f5493b517a0923103fb4f0c05c789f0e20a2161ba2
scope.5.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2Nhc2hSZXNlcnZlKDApOjI3
scope.5.kind=method
scope.5.startLine=27
scope.5.endLine=30
scope.5.semanticHash=71dea961caede545156063acfb60aaa924230b06a5f11c4e9ed9bcd22e65e869
scope.6.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2NsYWltcygxKTozNw
scope.6.kind=method
scope.6.startLine=37
scope.6.endLine=40
scope.6.semanticHash=507d9cac6317e7a85e3b057aed54560aaf53551ff97de821929348d3149eed56
scope.7.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2N0b3IoMCk6MTI
scope.7.kind=method
scope.7.startLine=12
scope.7.endLine=14
scope.7.semanticHash=3f5aa416a9a57d934dca74ba4a7b9dd8d00cb416d470693fbafb81c85ef05190
scope.8.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI2N0b3IoMSk6MTY
scope.8.kind=method
scope.8.startLine=16
scope.8.endLine=18
scope.8.semanticHash=c42e3ce5950e1e1139cf29a2be838a4b6e0824b92641374973729f0259bb058b
scope.9.id=bWV0aG9kOkFncmVlSWZBZmZvcmRhYmxlI3BheXMoMSk6NDc
scope.9.kind=method
scope.9.startLine=47
scope.9.endLine=50
scope.9.semanticHash=2582c1709534ea40e6b58368ac921704c006661aea5482e52aa350caa137bc3b
*/
