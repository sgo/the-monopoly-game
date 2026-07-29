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
    And pawn "dog" has $<starting balance> to spend
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
      | starting balance | bid | debtor final balance | owner final balance |
      | 5                 | 10  | 0                     | 1487                 |

  # bankruptcy-4
  Scenario Outline: a Get Out of Jail Free card returns to the bottom of its deck when its holder goes bankrupt to the bank
    Given pawn "dog" already holds a Get Out of Jail Free card
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "dog" is bankrupt
    And pawn "dog" no longer holds a Get Out of Jail Free card
    And pawn "high hat" wins the game

    Examples:
      | starting balance |
      | 5                 |

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
