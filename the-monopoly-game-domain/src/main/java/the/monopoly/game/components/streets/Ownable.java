package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

/**
 * A space a player can buy from the bank and later mortgage. What its owner
 * charges for a visit differs per kind, so rent lives on the subtypes.
 */
public sealed interface Ownable extends Street permits ColourStreet, Station, Utility {
  Money price();

  Money landMortgageValue();
}

/* mutate4java-manifest
version=1
moduleHash=bf1a87930de7b9f8638a5536b702c46054bdfbc612e9b5ce1db0059692f9c104
scope.0.id=Y2xhc3M6T3duYWJsZSNPd25hYmxlOjk
scope.0.kind=class
scope.0.startLine=9
scope.0.endLine=13
scope.0.semanticHash=d3e908174e9d71725792bbe802b3f16540929de44233373dbaa764cede7af6e4
scope.1.id=bWV0aG9kOk93bmFibGUjbGFuZE1vcnRnYWdlVmFsdWUoMCk6MTI
scope.1.kind=method
scope.1.startLine=12
scope.1.endLine=12
scope.1.semanticHash=e53b4f723d7fedf6b91bd79ccf941e8b317d3196a63ba8967e2dabc0f869b328
scope.2.id=bWV0aG9kOk93bmFibGUjcHJpY2UoMCk6MTA
scope.2.kind=method
scope.2.startLine=10
scope.2.endLine=10
scope.2.semanticHash=fdee531f4f48df76dd666a1176fc903e5e0f9a80a303479aea3febc15176191d
*/
