# mutation-stamp: sha256=b70b8f0756097e4617454493af05aa7fcd2f141b5a3c00bb3fd9a8aac441c273
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T10:51:57.373783Z","feature_name":"Greedo split-monopoly buyout","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-monopoly-buyout.feature","background_hash":"e15f13aafcac0600c3aaaaf97d370d153eb29c5c34b3d00e93ab47602feefe9c","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the richer co-owner wins a split monopoly with cash alone","scenario_hash":"794386fc8dad5761a39f1f91e9b526cdc45e0d65a3514676d66c5d7c304fb47e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:35.559464Z"},{"index":1,"name":"the richer co-owner wins a split monopoly regardless of which pawn initiates","scenario_hash":"3476d3ce4f6d06d8395d06e10383a0fa8cda0f2c74d1f73c7d46d9b3a012d45f","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:35.559464Z"},{"index":3,"name":"a co-owner keeps a spare street by paying double its rent value in cash instead","scenario_hash":"7c9c9c1d76e4bb81066be1cfec5e478bed7738c06242c59300d0fb52f53c21fb","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:35.559464Z"},{"index":5,"name":"a tied co-owner breaks the tie by combining a spare street with cash","scenario_hash":"78be51253607e5eb19cefc5620f1b1f9a2b8685cd611e55f898ffbd3ec325d61","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:35.559464Z"},{"index":9,"name":"the deal proceeds once 35% of the richer co-owner's balance just covers the price","scenario_hash":"54372e031c1be4cc9bbee6f7cf18b891614bd35955a525e4c0aaae0e5289f8aa","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:35.559464Z"},{"index":10,"name":"the co-owner who already holds more of a split colour group wins it, even when poorer","scenario_hash":"6911b03c00e75a2885dc9bcc8d80062868a7c06e43b49ef066f1c636882cd552","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:35.559464Z"},{"index":4,"name":"an exact tie in cash with no eligible streets leaves the monopoly split","scenario_hash":"5ad5cbfff85dfb65890d9e7a0244197dbdcd8fecdc532c09a16fe84de59c2e6f","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-07T23:10:10.444602Z"}]}
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

  # buyout-9
  Scenario Outline: a deal is deferred, not forced for free, when 35% of the richer co-owner's balance falls short of the price
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then no one wins the split monopoly
    And pawn "high hat" does not own "Meir Antwerpen"
    And pawn "dog" does not own "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | dog_balance | high_hat_balance |
      | 114         | 50                |

  # buyout-10
  Scenario Outline: the deal proceeds once 35% of the richer co-owner's balance just covers the price
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
      | 115         | 50                | 40    |

  # buyout-11
  Scenario Outline: the co-owner who already holds more of a split colour group wins it, even when poorer
    Given pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Place Verte Verviers"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then pawn "dog" wins the split monopoly
    And pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "dog" owns "Place Verte Verviers"

    Examples:
      | dog_balance | high_hat_balance | price |
      | 100         | 5000              | 10    |

  # buyout-12
  Scenario Outline: a split highest-priority colour group is never resolved by buyout
    Given pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" owns "Rue Royale Tournai"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" has $<high_hat_balance> to spend
    When the split monopoly between pawn "dog" and pawn "high hat" is resolved
    Then no one wins the split monopoly
    And pawn "dog" does not own "Groenplaats Antwerpen"
    And pawn "high hat" does not own "Lippenslaan Knokke"

    Examples:
      | dog_balance | high_hat_balance |
      | 1900        | 50                |
