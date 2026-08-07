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
