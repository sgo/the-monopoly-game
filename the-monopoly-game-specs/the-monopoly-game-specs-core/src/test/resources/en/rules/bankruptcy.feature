# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-29T12:14:09.900531Z","feature_name":"bankruptcy","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/bankruptcy.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"a player mortgages property to cover a debt they cannot otherwise afford","scenario_hash":"629f30b73b551822f50163fa0560170d095461a3440d28dd93f1dc04e4488b31","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T12:13:22.277174Z"},{"index":1,"name":"a player sells a house to cover a debt they cannot otherwise afford","scenario_hash":"428ce8b1475baabf7787d86ac2ed0311c4ed568b801732c2d6ef8ea830104ed6","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-29T12:13:22.277174Z"},{"index":4,"name":"a player who cannot cover a debt to another player forfeits their remaining money and mortgaged property to the creditor","scenario_hash":"5126955b9f9895249aa779bfb039d808ecd99ce96f889c7622ca2ef7b86b8695","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-29T12:13:22.277174Z"},{"index":5,"name":"\"Agree if affordable\" pays off an inherited mortgage immediately when it can afford to","scenario_hash":"8fdc69df870669cf539d0ce0504a632c399a47aa6eb87bc271b779771e59e0d5","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T12:13:22.277174Z"},{"index":6,"name":"\"Agree if affordable\" keeps an inherited mortgage when it cannot afford to pay it off, paying only the mandatory interest","scenario_hash":"24638a26b6ee9a04894e0f012990bba34520c2d33a0a46040ddf168ddfa6fb15","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-29T12:13:22.277174Z"}]}
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
      | 10  | 0                     | 1487                 |

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
  Scenario Outline: "Agree if affordable" pays off an inherited mortgage immediately when it can afford to
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "Agree if affordable" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" is bankrupt
    And pawn "high hat" owns "Rue Grande Dinant"
    And the land "Rue Grande Dinant" is not mortgaged
    And pawn "high hat"'s account balance is $<owner final balance>

    Examples:
      | starting balance | owner final balance |
      | 10                | 1507                 |

  # bankruptcy-7
  Scenario Outline: "Agree if affordable" keeps an inherited mortgage when it cannot afford to pay it off, paying only the mandatory interest
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "Agree if affordable" strategy
    And pawn "high hat" has $<owner starting balance> to spend
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" is bankrupt
    And pawn "high hat" owns "Rue Grande Dinant"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "high hat"'s account balance is $<owner final balance>

    Examples:
      | owner starting balance | starting balance | owner final balance |
      | 0                       | 2                 | 29                   |
