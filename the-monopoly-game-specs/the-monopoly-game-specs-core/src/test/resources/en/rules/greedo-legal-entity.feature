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
    And Pink Realty owes pawn "dog" $<principal>
    When we play up to 1 round
    Then Pink Realty repays pawn "dog" $<repayment> for the loan
    And pawn "dog" receives no dividend

    Examples:
      | principal | repayment |
      | 100       | 105       |

  # entity-7
  Scenario Outline: a dividend is paid equally to every shareholder once their age advances
    Given <entity_name> is formed
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then each of pawn "dog", pawn "high hat", and pawn "iron box" receives a dividend from <entity_name>

    Examples:
      | entity_name  |
      | Pink Realty  |
      | Yellow Realty |
      | Green Realty |

  # entity-8
  Scenario Outline: no dividend is paid while any shareholder loan to the entity is still outstanding
    Given Pink Realty is formed
    And Pink Realty owes pawn "dog" $<principal>
    When we play up to 1 round
    Then pawn "dog" receives no dividend from Pink Realty

    Examples:
      | principal |
      | 100       |

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
