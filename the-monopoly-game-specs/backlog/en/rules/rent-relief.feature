# language: en

# Backlogged from ../../../the-monopoly-game-specs-core/src/test/resources/
# en/rules/rent-relief.feature's round 1, which proves the $200 cap in
# isolation over a directly-stated rent amount. Scenario 4 needs a way to
# seed a live played game's real government account from outside it before
# play starts - today nothing does, the same gap flagged in
# ../en/rules/megacorp-salary-tax.feature's backlog for reading one back
# out afterward. Scenario 5's exact positions and dice rolls are
# illustrative only and need verification against the real board layout
# once this round is picked up; the roll numbers below have not been run.

Feature: rent relief

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And rent relief is enabled

  # rent-relief-3
  Scenario Outline: rent relief applies the same way when the landlord is a legal entity
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And pawn "high hat" has $1500 to spend
    And the government's account already holds $<government_start>
    When pawn "high hat" lands on "Rue de Diekirch Arlon"
    Then pawn "high hat"'s account balance is $<tenant_final>

    Examples:
      | government_start | tenant_final |
      | 550                | 1300          |
      | 549                | 750           |

  # rent-relief-4
  Scenario Outline: a live played game's rent payment honors an already-funded government account, not just the isolated computation
    Given pawn "dog" owns "Rue de Diekirch Arlon"
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the government's account already holds $<government_start>
    And pawn "high hat" has $1500 to spend
    When pawn "high hat" lands on "Rue de Diekirch Arlon"
    Then the game journal records that pawn "high hat" pays pawn "dog" $200 rent for "Rue de Diekirch Arlon"

    Examples:
      | government_start |
      | 550                |

  # rent-relief-5
  # Illustrative only: high hat's roll is meant to land exactly on "Meir
  # Antwerpen" for the rent, and dog's roll is meant to pass Start
  # afterward in the same continuous play so the real yearly assessment
  # (tied to a real salary collection, the same as
  # ../en/rules/megacorp-salary-tax.feature's round 1 assumes) fires for
  # real. Neither pawn's exact starting position nor roll has been checked
  # against the real board layout.
  Scenario Outline: war profits tax still accumulates the full nominal rent a landlord receives, whether or not relief covered part of it
    Given the war profits tax is enabled
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Nieuwstraat Brussel"
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Veldstraat Gent"
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    And the street "Meir Antwerpen" has a hotel built
    And the street "Nieuwstraat Brussel" has a hotel built
    And the street "Boulevard Tirou Charleroi" has a hotel built
    And the street "Veldstraat Gent" has a hotel built
    And the street "Boulevard d'Avroy Liège" has a hotel built
    And the government's account already holds $1300
    And pawn "high hat" has $1500 to spend
    And pawn "high hat" starts at position <high_hat_start_position>
    And pawn "high hat" will roll <high_hat_die_1> and <high_hat_die_2> for their turn
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play up to <rounds> rounds
    Then the game journal records that pawn "dog" pays the government a war profits tax of $<tax>

    Examples:
      | high_hat_start_position | high_hat_die_1 | high_hat_die_2 | dog_start_position | dog_die_1 | dog_die_2 | rounds | tax  |
      | 37                       | 1               | 2               | 38                  | 1          | 1          | 2       | 1500 |
