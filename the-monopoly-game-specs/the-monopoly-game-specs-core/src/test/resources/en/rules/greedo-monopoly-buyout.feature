# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-07T23:10:10.444602Z","feature_name":"Greedo split-monopoly buyout","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-monopoly-buyout.feature","background_hash":"e15f13aafcac0600c3aaaaf97d370d153eb29c5c34b3d00e93ab47602feefe9c","implementation_hash":"unknown","scenarios":[{"index":4,"name":"an exact tie in cash with no eligible streets leaves the monopoly split","scenario_hash":"5ad5cbfff85dfb65890d9e7a0244197dbdcd8fecdc532c09a16fe84de59c2e6f","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-07T23:10:10.444602Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: Greedo split-monopoly buyout

  Background:
    Given the official rule set
    And we select 2 players

  # buyout-1
  Scenario Outline: the richer co-owner wins a split monopoly with cash alone
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then pawn "dog" wins the split monopoly
    And pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | dog_balance | high_hat_balance | price |
      | 1000        | 100               | 40    |

  # buyout-2
  Scenario Outline: the richer co-owner wins a split monopoly regardless of which pawn initiates
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then pawn "high hat" wins the split monopoly
    And pawn "high hat" pays pawn "dog" $<price> for the split monopoly
    And pawn "high hat" owns "Meir Antwerpen"

    Examples:
      | dog_balance | high_hat_balance | price |
      | 100         | 1000              | 40    |

  # buyout-3
  Scenario Outline: a co-owner wins by giving up a spare street they cannot afford to buy back with cash
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then pawn "dog" wins the split monopoly
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "dog" does not own "Diestsestraat Leuven"
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | dog_balance | high_hat_balance |
      | 1000        | 100               |

  # buyout-4
  Scenario Outline: a co-owner keeps a spare street by paying double its rent value in cash instead
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then pawn "dog" wins the split monopoly
    And pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "high hat" does not own "Diestsestraat Leuven"
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | dog_balance | high_hat_balance | price |
      | 3000        | 100               | 900   |

  # buyout-5
  Scenario Outline: an exact tie in cash with no eligible streets leaves the monopoly split
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then no one wins the split monopoly
    And pawn "dog" does not own "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" does not own "Meir Antwerpen"

    Examples:
      | dog_balance | high_hat_balance |
      | 100         | 100               |

  # buyout-6
  Scenario Outline: a tied co-owner breaks the tie by combining a spare street with cash
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then pawn "dog" wins the split monopoly
    And pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "high hat" owns "Rue Grande Dinant"
    And pawn "dog" does not own "Rue Grande Dinant"
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | dog_balance | high_hat_balance | price |
      | 1000        | 1000              | 105   |

  # buyout-7
  Scenario Outline: a spare sweetener is withheld when it would split the winner's own complete monopoly
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then pawn "dog" wins the split monopoly
    And pawn "high hat" does not own "Rue Grande Dinant"
    And pawn "high hat" does not own "Diestsestraat Leuven"

    Examples:
      | dog_balance | high_hat_balance |
      | 1900        | 50                |

  # buyout-8
  Scenario Outline: a spare sweetener still comes from a group the winner does not already own complete
    Given we select 3 players
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    And pawn "iron box" owns "Bruul Mechelen"
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then pawn "dog" wins the split monopoly
    And pawn "high hat" owns "Rue de Diekirch Arlon"
    And pawn "dog" does not own "Rue de Diekirch Arlon"

    Examples:
      | dog_balance | high_hat_balance |
      | 1900        | 50                |
