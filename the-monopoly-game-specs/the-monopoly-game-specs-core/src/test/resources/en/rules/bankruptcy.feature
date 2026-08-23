# mutation-stamp: sha256=6d929f090de91d21cb98e082499274b3d764d131a62bf53eda123940461185fd
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-23T00:22:24.123173Z","feature_name":"bankruptcy","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/bankruptcy.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":6,"name":"\"<strategy>\" keeps an inherited mortgage when it cannot afford to pay it off, paying only the mandatory interest","scenario_hash":"5e3498f3dd19e56a2198e4a5dad6e7d90dfe613fa84944eb7ec148ec9a47071f","mutation_count":7,"result":{"Total":7,"Killed":7,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:24.123173Z"},{"index":7,"name":"a doubles roll that causes bankruptcy does not grant a phantom extra move afterward","scenario_hash":"51ed99f97c1effaf44de16ef7f15d3e7ef6da30b2986293e8ab32ee435baafc9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:24.123173Z"},{"index":0,"name":"a player mortgages property to cover a debt they cannot otherwise afford","scenario_hash":"629f30b73b551822f50163fa0560170d095461a3440d28dd93f1dc04e4488b31","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:05.480390Z"},{"index":1,"name":"a player sells a house to cover a debt they cannot otherwise afford","scenario_hash":"428ce8b1475baabf7787d86ac2ed0311c4ed568b801732c2d6ef8ea830104ed6","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:05.480390Z"},{"index":2,"name":"a player who cannot cover a debt to the bank forfeits everything, and the bank auctions the land","scenario_hash":"45fcddac988dfff7d170a4b150d0f9337ac4d8b39555cb8d38bed4025497d90b","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:05.480390Z"},{"index":3,"name":"a Get Out of Jail Free card returns to the bottom of its deck when its holder goes bankrupt to the bank","scenario_hash":"8702d9e7105033454dd04d2ec297d87869fdb10d171cab1a0950aa8dd2d510c3","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:05.480390Z"},{"index":4,"name":"a player who cannot cover a debt to another player forfeits their remaining money and mortgaged property to the creditor","scenario_hash":"5126955b9f9895249aa779bfb039d808ecd99ce96f889c7622ca2ef7b86b8695","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:05.480390Z"},{"index":5,"name":"\"<strategy>\" pays off an inherited mortgage immediately when it can afford to","scenario_hash":"78dd63495a7923bec83d08fbfb67b4c968016ae54fd32f36b37b7d965a54804e","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:05.480390Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: bankruptcy

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # bankruptcy-1
  Scenario Outline: a player mortgages property to cover a debt they cannot otherwise afford
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog"'s account balance is $<final balance>
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" is not bankrupt

    Examples:
      | starting balance | final balance |
      | 70                | 0              |

  # bankruptcy-2
  Scenario Outline: a player sells a house to cover a debt they cannot otherwise afford
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    And the street "Diestsestraat Leuven" has 1 house(s) built
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog"'s account balance is $<final balance>
    And the street "Rue Grande Dinant" has <rue grande dinant houses> house(s) built
    And the street "Diestsestraat Leuven" has <diestsestraat leuven houses> house(s) built
    And pawn "dog" is not bankrupt

    Examples:
      | starting balance | final balance | rue grande dinant houses | diestsestraat leuven houses |
      | 80                | 5              | 0                          | 1                             |

  # bankruptcy-3
  Scenario Outline: a player who cannot cover a debt to the bank forfeits everything, and the bank auctions the land
    Given pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" has $5 to spend
    And pawn "high hat" will bid $<bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "dog" is bankrupt
    And pawn "dog"'s account balance is $<debtor final balance>
    And pawn "dog" does not own "Diestsestraat Leuven"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "high hat"'s account balance is $<owner final balance>
    And pawn "high hat" wins the game

    Examples:
      | bid | debtor final balance | owner final balance |
      | 35  | 0                     | 1467                 |

  # bankruptcy-4
  Scenario Outline: a Get Out of Jail Free card returns to the bottom of its deck when its holder goes bankrupt to the bank
    Given pawn "dog" already holds a Get Out of Jail Free card
    And pawn "dog" has $5 to spend
    When pawn "dog" lands on "<space>"
    Then pawn "dog" is bankrupt
    And pawn "dog" no longer holds a Get Out of Jail Free card
    And pawn "high hat" wins the game

    Examples:
      | space                                       |
      | Inkomsten Belasting / Impôts sur le revenu |

  # bankruptcy-5
  Scenario Outline: a player who cannot cover a debt to another player forfeits their remaining money and mortgaged property to the creditor
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" will claim rent for "Diestsestraat Leuven"
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" is bankrupt
    And pawn "dog"'s account balance is $<debtor final balance>
    And pawn "dog" does not own "Rue Grande Dinant"
    And pawn "high hat" owns "Rue Grande Dinant"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "high hat"'s account balance is $<owner final balance>
    And pawn "high hat" wins the game

    Examples:
      | starting balance | debtor final balance | owner final balance |
      | 10                | 0                     | 1537                 |

  # bankruptcy-6
  Scenario Outline: "<strategy>" pays off an inherited mortgage immediately when it can afford to
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" is bankrupt
    And pawn "high hat" owns "Rue Grande Dinant"
    And the land "Rue Grande Dinant" is not mortgaged
    And pawn "high hat"'s account balance is $<owner final balance>

    Examples:
      | strategy | starting balance | owner final balance |
      | Greedo   | 10                | 1507                 |
      | Billionaire | 10             | 57700007              |

  # bankruptcy-7
  Scenario Outline: "<strategy>" keeps an inherited mortgage when it cannot afford to pay it off, paying only the mandatory interest
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<owner starting balance> to spend
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" is bankrupt
    And pawn "high hat" owns "Rue Grande Dinant"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "high hat"'s account balance is $<owner final balance>

    Examples:
      | strategy | owner starting balance | starting balance | owner final balance |
      | Greedo   | 0                       | 2                 | 29                   |
      | Billionaire | 0                    | 2                 | 29                   |

  # bankruptcy-8
  Scenario Outline: a doubles roll that causes bankruptcy does not grant a phantom extra move afterward
    Given pawn "high hat" owns "Rue St-Léonard Liège"
    And pawn "high hat" will claim rent for "Rue St-Léonard Liège"
    And pawn "high hat" owns "Hoogstraat (Brussel) / Rue Haute (Bruxelles)"
    And pawn "high hat" will claim rent for "Hoogstraat (Brussel) / Rue Haute (Bruxelles)"
    And pawn "dog" starts at position 19
    And pawn "dog" has $<starting balance> to spend
    And pawn "dog" will roll 1 and 1 for their turn
    And pawn "dog" will roll 3 and 5 for their turn
    When we play the game
    Then pawn "dog" is bankrupt
    And pawn "high hat" wins the game
    And pawn "dog"'s account balance is $<expected final balance>

    Examples:
      | starting balance | expected final balance |
      | 10                | 0                       |
