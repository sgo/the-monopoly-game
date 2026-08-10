# language: en

Feature: Greedo legal entity for a three-way colour-group split

  Background:
    Given the official rule set
    And we select 3 players

  # entity-1
  Scenario Outline: three Greedo co-owners of a colour group coalesce into a legal entity holding equal shares
    Given legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "<street_dog>"
    And pawn "high hat" owns "<street_high_hat>"
    And pawn "iron box" owns "<street_iron_box>"
    And every other ownable space is owned by pawn "high hat"
    When pawn "dog" considers forming a legal entity over the <group> colour group
    Then the <group> colour group is owned by <entity_name>
    And each of pawn "dog", pawn "high hat", and pawn "iron box" holds a third of <entity_name>

    Examples:
      | group  | street_dog             | street_high_hat    | street_iron_box          | entity_name  |
      | pink   | Rue de Diekirch Arlon  | Bruul Mechelen     | Place Verte Verviers     | Pink Realty  |
      | yellow | Grote Markt Hasselt    | Place de l'Ange Namur | Hoogstraat (Brussel) / Rue Haute (Bruxelles) | Yellow Realty |
      | green  | Boulevard Tirou Charleroi | Veldstraat Gent  | Boulevard d'Avroy Liège  | Green Realty |

  # entity-2
  Scenario Outline: the entity is not formed while the board still holds unowned space
    Given legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    When pawn "dog" considers forming a legal entity over the pink colour group
    Then the pink colour group <outcome> a legal entity

    Examples:
      | outcome |
      | is not owned by |

  # entity-3
  Scenario Outline: the entity is not formed while only stalemate trading (not legal-entity trading) is enabled
    Given <enabled_flag> trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "high hat"
    When pawn "dog" considers forming a legal entity over the pink colour group
    Then the pink colour group is not owned by a legal entity

    Examples:
      | enabled_flag |
      | stalemate    |

  # entity-4
  Scenario Outline: the entity never consolidates a highest-priority colour group
    Given legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "iron box" owns "Groenplaats Antwerpen"
    And every other ownable space is owned by pawn "high hat"
    When pawn "dog" considers forming a legal entity over the <group> colour group
    Then the <group> colour group is not owned by a legal entity

    Examples:
      | group   |
      | orange  |

  # entity-5
  Scenario Outline: a two-player split of an eligible colour group does not form an entity
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select <player_count> players
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And every other ownable space is owned by pawn "high hat"
    When pawn "dog" considers forming a legal entity over the pink colour group
    Then the pink colour group is not owned by a legal entity

    Examples:
      | player_count |
      | 2            |

  # entity-6
  Scenario Outline: the entity repays a shareholder loan with five percent interest on top before paying any dividend
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's bank account holds $<funds>
    And every other ownable space is owned by pawn "racecar"
    When we play up to 1 round
    Then Pink Realty repays pawn "dog" $<repayment> for the loan
    And Pink Realty's bank account holds $<funds_remaining>
    And pawn "dog" receives no dividend

    Examples:
      | principal | funds | repayment | funds_remaining |
      | 100       | 105   | 105       | 0               |

  # entity-7
  Scenario Outline: the entity builds houses from rent at the end of the turn before repaying its loan
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<rent>
    And Pink Realty owes pawn "dog" $<principal>
    When we play up to 1 round
    Then the pink colour group is developed up to at least <houses_at_least> houses
    And Pink Realty's bank account holds $<rent_remaining>
    And Pink Realty still owes pawn "dog" $<principal>

    Examples:
      | principal | rent | houses_at_least | rent_remaining |
      | 200       | 200  | 2               | 0              |

  # entity-8
  Scenario Outline: no dividend is paid while any shareholder loan to the entity is still outstanding
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's bank account holds $<surplus>
    And every other ownable space is owned by pawn "racecar"
    When we play up to 1 round
    Then pawn "dog" receives no dividend from Pink Realty

    Examples:
      | principal | surplus |
      | 100       | 150     |

  # entity-11
  Scenario Outline: a dividend is paid only after the entire loan plus interest has been repaid
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's loan has been fully repaid
    And Pink Realty's bank account holds $<surplus>
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then each of pawn "dog", pawn "high hat", and pawn "iron box" receives a $<dividend_share> dividend from Pink Realty
    And Pink Realty's bank account holds $<surplus_remaining>

    Examples:
      | principal | surplus | dividend_share | surplus_remaining |
      | 100       | 150     | 50             | 0                 |

  # entity-12
  Scenario Outline: the entity cannot build beyond a shareholder's personal affordability ceiling
    Given Pink Realty is formed
    And Pink Realty owes pawn "dog" $<loan>
    And pawn "high hat" has a balance that allows only $<ceiling_share> toward the entity
    And pawn "iron box" has a balance that allows only $<ceiling_share> toward the entity
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the pink colour group is developed up to no more than <total_houses> houses
    And no shareholder has paid more than $<ceiling_share> to the entity

    Examples:
      | loan | ceiling_share | total_houses |
      | 100  | 40            | 1            |

  # entity-13
  Scenario Outline: rent collected from a tenant is deposited into the entity's bank account
    Given we select 4 players
    And Pink Realty is formed
    And pawn "<renter>" starts at position <renter_position>
    And pawn "<renter>" will claim rent for "<renter_street>"
    When pawn "<renter>" lands on "<renter_street>"
    Then Pink Realty's bank account holds $<rent>
    And pawn "<renter>" has paid $<rent> in rent

    Examples:
      | renter  | renter_position | renter_street          | rent |
      | racecar | 3               | Bruul Mechelen         | 20   |

  # entity-14
  Scenario Outline: a raised loan is deposited into the entity's bank account
    Given Pink Realty is formed
    When Pink Realty raises a loan of $<loan>
    Then Pink Realty's bank account holds $<loan>

    Examples:
      | loan |
      | 150  |

  # entity-15
  Scenario Outline: the entity uses its rent before raising a loan to build
    Given Pink Realty is formed
    And Pink Realty's bank account holds $<rent>
    When we play up to 1 round
    Then Pink Realty raises no more than $<max_loan> in loans
    And the pink colour group is developed up to at least <houses_at_least> houses
    And Pink Realty's bank account holds $<rent_remaining>

    Examples:
      | rent | max_loan | houses_at_least | rent_remaining |
      | 100  | 0        | 1               | 0              |

  # entity-16
  Scenario Outline: the entity builds as many houses as it can afford at the end of the turn
    Given Pink Realty is formed
    And Pink Realty's bank account holds $<rent>
    When we play up to 1 round
    Then the <street_1>, the <street_2>, and the <street_3> each have a house built
    And the pink colour group is developed up to <total_houses> houses
    And Pink Realty's bank account holds $<rent_remaining>

    Examples:
      | rent | street_1               | street_2       | street_3              | total_houses | rent_remaining |
      | 300  | Rue de Diekirch Arlon  | Bruul Mechelen | Place Verte Verviers  | 3            | 0              |

  # entity-17
  Scenario Outline: an entity with nothing to build, repay, or pay becomes idle instead of spinning
    Given Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty's bank account is empty
    When we play up to 1 round
    Then Pink Realty issues no loan, repayment, or dividend

    Examples:
      | scenario |
      | idle     |

  # entity-9
  Scenario Outline: the entity forms from exactly the three co-owners of a colour group even when the game has more than three players
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select <player_count> players
    And pawn "dog" owns "<street_dog>"
    And pawn "high hat" owns "<street_high_hat>"
    And pawn "iron box" owns "<street_iron_box>"
    And every other ownable space is owned by pawn "racecar"
    When pawn "dog" considers forming a legal entity over the <group> colour group
    Then the <group> colour group is owned by <entity_name>
    And each of pawn "dog", pawn "high hat", and pawn "iron box" holds a third of <entity_name>

    Examples:
      | player_count | group  | street_dog                | street_high_hat         | street_iron_box          | entity_name  |
      | 4            | pink   | Rue de Diekirch Arlon     | Bruul Mechelen          | Place Verte Verviers     | Pink Realty  |
      | 8            | yellow | Grote Markt Hasselt       | Place de l'Ange Namur   | Hoogstraat (Brussel) / Rue Haute (Bruxelles) | Yellow Realty |

  # entity-10
  Scenario Outline: the entity does not form when the colour group is split across only two owners in a larger game
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select <player_count> players
    And pawn "dog" owns "<street_dog>"
    And pawn "high hat" owns "<street_high_hat>"
    And pawn "high hat" owns "<street_high_hat_extras>"
    And every other ownable space is owned by pawn "racecar"
    When pawn "dog" considers forming a legal entity over the <group> colour group
    Then the <group> colour group is not owned by a legal entity

    Examples:
      | player_count | group | street_dog            | street_high_hat      | street_high_hat_extras |
      | 4            | pink  | Rue de Diekirch Arlon | Bruul Mechelen       | Place Verte Verviers   |
      | 8            | green | Boulevard Tirou Charleroi | Veldstraat Gent  | Boulevard d'Avroy Liège |
