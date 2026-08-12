# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-11T07:41:25.554747Z","feature_name":"Greedo legal entity for a three-way colour-group split","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-legal-entity.feature","background_hash":"fd82b050e2caf6974e2f44e0c1ce996c7bd99ce3d3e5d035db5a75223654814e","implementation_hash":"unknown","scenarios":[{"index":13,"name":"the entity uses its rent before raising a loan to build","scenario_hash":"e3e0887ccd66ac4600387b38de334101acf5378e669a26e8a5e7aa11baa68d51","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-11T04:46:20.046607Z"},{"index":14,"name":"the entity builds as many houses as it can afford at the end of the turn","scenario_hash":"2fb2bcce463e71807420f255baea8b84f75d80be4d796e4d29d0e1e70d9b84e4","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-11T04:46:20.046607Z"},{"index":0,"name":"three Greedo co-owners of a colour group coalesce into a legal entity holding equal shares","scenario_hash":"2ef5e96b2ea70f8ff71751ea4b35eae5952a85776b607e56ec76b4482675c459","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-08-10T17:36:35.189910Z"},{"index":3,"name":"the entity never consolidates a highest-priority colour group","scenario_hash":"072ba692ad338f0bcd5a482bf5606f8b18a5d2a4d1062bd8c5f03beed450891f","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-10T17:36:35.189910Z"},{"index":4,"name":"a two-player split of an eligible colour group does not form an entity","scenario_hash":"afc4ab1a1890586142a97b70d380ecac4e4354146c8064aa6e08fc415602c178","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-10T17:36:35.189910Z"}]}
# acceptance-mutation-manifest-end

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
    And the last-capitalised shareholder of Pink Realty is pawn "dog"
    And the last-capitalised shareholder of Pink Realty grows a year older
    When we play up to 1 round
    Then each of pawn "dog", pawn "high hat", and pawn "iron box" receives a $<dividend_share> dividend from Pink Realty
    And Pink Realty's bank account holds $<remainder>

    Examples:
      | principal | surplus | dividend_share | remainder |
      | 100       | 150     | 50             | 0         |
      | 100       | 170     | 56             | 2         |

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
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And pawn "<renter>" starts at position <renter_position>
    And pawn "<renter>" will claim rent for "<renter_street>"
    When pawn "<renter>" lands on "<renter_street>"
    Then pawn "<renter>" has paid $<rent> in rent
    And pawn "<renter>"'s account balance is $<tenant_balance>

    Examples:
      | renter  | renter_position | renter_street          | rent | tenant_balance |
      | racecar | 3               | Bruul Mechelen         | 625  | 875           |

  # entity-21
  Scenario Outline: a shareholder pays rent when landing on their own legal entity's street
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<treasury>
    And the last-capitalised shareholder of Pink Realty has not aged since funding a build
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And pawn "<renter>" starts at position <renter_position>
    And pawn "<renter>" will claim rent for "<renter_street>"
    When pawn "<renter>" lands on "<renter_street>"
    Then pawn "<renter>" has paid $<rent> in rent
    And pawn "<renter>"'s account balance is $<tenant_balance>

    Examples:
      | treasury | renter    | renter_position | renter_street          | rent | tenant_balance |
      | 5000     | iron box  | 3               | Bruul Mechelen         | 625  | 875           |

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
  Scenario Outline: an entity whose streets are already fully developed is financially inactive at the round boundary
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And Pink Realty owns no outstanding loan
    And Pink Realty's bank account is empty
    When we play up to 1 round
    Then Pink Realty's bank account holds $<bank_ending>
    And Pink Realty raises no more than $<max_loan> in loans
    And pawn "dog" receives no dividend from Pink Realty

    Examples:
      | bank_ending | max_loan |
      | 0           | 0        |

  # entity-22
  Scenario Outline: an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And each shareholder commits $<share> toward Pink Realty's build
    When we play up to 1 round
    Then Pink Realty raises a loan of $<loan>
    And the street "<street_1>" has <houses_per_street> houses built
    And the street "<street_2>" has <houses_per_street> houses built
    And the street "<street_3>" has <houses_per_street> houses built

    Examples:
      | loan | street_1              | street_2       | street_3              | houses_per_street | share |
      | 300  | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 1                 | 100   |
      | 600  | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 2                 | 200   |
      | 900  | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 3                 | 300   |
      | 1200 | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 4                 | 400   |

  # entity-23
  Scenario Outline: an entity whose shareholders commit to a group build loan builds a hotel on every street when it can fund the hotels
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And each shareholder commits $<share> toward Pink Realty's build
    When we play up to 1 round
    Then Pink Realty raises a loan of $<loan>
    And the street "<street_1>" has a hotel built
    And the street "<street_2>" has a hotel built
    And the street "<street_3>" has a hotel built

    Examples:
      | loan | street_1              | street_2       | street_3              | share |
      | 1500 | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 500   |

  # entity-18
  Scenario Outline: no dividend is paid unless the last-capitalised shareholder grows a year older
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's loan has been fully repaid
    And Pink Realty's bank account holds $<surplus>
    And the last-capitalised shareholder of Pink Realty has not aged since funding a build
    When we play up to 1 round
    Then pawn "dog" receives no dividend from Pink Realty

    Examples:
      | principal | surplus |
      | 0         | 150     |

  # entity-19
  Scenario Outline: the entity pays an equal dividend when the last-capitalised shareholder grows a year older
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's loan has been fully repaid
    And Pink Realty's bank account holds $<surplus>
    And the last-capitalised shareholder of Pink Realty is pawn "dog"
    And the last-capitalised shareholder of Pink Realty grows a year older
    When we play up to 1 round
    Then each of pawn "dog", pawn "high hat", and pawn "iron box" receives a $<dividend_share> dividend from Pink Realty

    Examples:
      | principal | surplus | dividend_share |
      | 0         | 150     | 50             |

  

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

  # entity-24
  Scenario Outline: the entity raises a build loan when every Greedo shareholder can afford its share, is solvent, and its reserve allows it
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And pawn "dog" has a balance of $<balance>
    And pawn "high hat" has a balance of $<balance>
    And pawn "iron box" has a balance of $<balance>
    And pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "high hat" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "iron box" follows the "Greedo" strategy, keeping a $<reserve> reserve
    When we play up to 1 round
    Then Pink Realty raises a loan of $<loan>
    And the street "<street>" has at least <houses> houses built

    Examples:
      | balance | reserve | loan | street              | houses |
      | 200     | 0       | 300  | Rue de Diekirch Arlon | 1      |

  # entity-25
  Scenario Outline: the entity does not raise a build loan when a Greedo shareholder cannot afford its share
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And pawn "high hat" has a balance that allows only $<ceiling_share> toward the entity
    And pawn "iron box" has a balance that allows only $<ceiling_share> toward the entity
    When we play up to 1 round
    Then Pink Realty raises no more than $0 in loans
    And the pink colour group is developed up to no more than <total_houses> houses

    Examples:
      | ceiling_share | total_houses |
      | 40            | 0            |

  # entity-27
  Scenario Outline: the entity does not raise a build loan when a Greedo shareholder's reserve would be breached
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And pawn "dog" has a balance of $<balance>
    And pawn "high hat" has a balance of $<balance>
    And pawn "iron box" has a balance of $<balance>
    And pawn "high hat" follows the "Greedo" strategy, keeping a $<reserve> reserve
    When we play up to 1 round
    Then Pink Realty raises no more than $0 in loans
    And the pink colour group is developed up to no more than <total_houses> houses

    Examples:
      | balance | reserve | total_houses |
      | 150     | 150     | 0            |

  # entity-29
  Scenario Outline: the entity builds a hotel when every Greedo shareholder commits to a full build loan
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And each shareholder commits $<share> toward Pink Realty's build
    When we play up to 1 round
    Then Pink Realty raises a loan of $<loan>
    And the street "<street>" has a hotel built

    Examples:
      | loan | street          | share |
      | 1500 | Rue de Diekirch Arlon | 500  |

  # Commentary: harness steps required to drive the Greedo entity-build-commit scenarios.
  # The strategy decision point (entity build loan commitment) and these steps are new.
  # Existing reusable steps: "each shareholder commits $X", "Pink Realty raises a loan of
  # $X", "Pink Realty raises no more than $X in loans", "pawn X has a balance that allows
  # only $Y toward the entity", "the pink colour group is developed up to no more than N
  # houses", "the street S has a hotel built", "each of ... receives a $X dividend from
  # Pink Realty", "the last-capitalised shareholder of Pink Realty is pawn X", "... grows a
  # year older", "pawn X follows the \"Greedo\" strategy, keeping a $Y reserve".
  # NEW steps needed:
  #   - "pawn X has a balance of $Y"  (set a pre-game balance via holdPawnBalance)
  #   - "pawn X is in debt by $Y"     (set a negative balance -> distress)
  #   - "pawn X has a balance that allows only $Y ..." already exists for the ceiling case
  #   - "raises no loan" -> expressed as "raises no more than $0 in loans" (existing)
  # NOTE: build-loan commitment is ALL-OR-NOTHING. No partial loan. Reasoning: a shareholder
  # who did not commit would otherwise free-ride on the rental income the loan buys, collecting
  # dividends/rent from improvements they did not help fund. So the entity raises a build loan
  # only when EVERY shareholder commits (affordable + solvent + reserve allows). A declined
  # shareholder blocks the loan entirely.
