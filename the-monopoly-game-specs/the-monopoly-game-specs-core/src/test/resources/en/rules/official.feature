# language: en

Feature: official rules

  Background:
    Given the official rule set

  # official-rules-1
  Scenario: the game is played with 2 dice with 6 faces
    Then dice 1 is 6 faced
    And dice 2 is 6 faced

  # official-rules-2
  Scenario: the game is played with a minimum of 2 to a maximum of 8 players
    Then we play with 2 to 8 players

  # official-rules-3
  Scenario Outline: board space
    Then space <index> is "<name>" of type <type> and colour group <colour group>

    Examples:
      | index | name                                           | type             | colour group |
      | 0     | Start                                          | start            | -            |
      | 1     | Rue Grande Dinant                              | street           | brown        |
      | 2     | Algemeen Fonds / Caisse de Communauté          | community chest | -            |
      | 3     | Diestsestraat Leuven                           | street           | brown        |
      | 4     | Inkomsten Belasting / Impôts sur le revenu     | tax              | -            |
      | 5     | Noord Station / Gare du Nord                   | station          | -            |
      | 6     | Steenstraat Brugge                             | street           | light blue   |
      | 7     | Kans / Chance                                  | chance           | -            |
      | 8     | Place du Monument Spa                          | street           | light blue   |
      | 9     | Kapellestraat Oostende                         | street           | light blue   |
      | 10    | Op Bezoek / Simple Visite                      | jail             | -            |
      | 11    | Rue de Diekirch Arlon                          | street           | pink         |
      | 12    | Elektriciteitscentrale / Centrale Électrique   | utility          | -            |
      | 13    | Bruul Mechelen                                 | street           | pink         |
      | 14    | Place Verte Verviers                           | street           | pink         |
      | 15    | Centraal Station / Gare Centrale               | station          | -            |
      | 16    | Lippenslaan Knokke                             | street           | orange       |
      | 17    | Algemeen Fonds / Caisse de Communauté          | community chest | -            |
      | 18    | Rue Royale Tournai                             | street           | orange       |
      | 19    | Groenplaats Antwerpen                          | street           | orange       |
      | 20    | Gratis Parkeren / Parc Gratuit                 | free parking     | -            |
      | 21    | Rue St-Léonard Liège                           | street           | red          |
      | 22    | Kans / Chance                                  | chance           | -            |
      | 23    | Lange Steenstraat Kortrijk                     | street           | red          |
      | 24    | Grand Place Mons                               | street           | red          |
      | 25    | Buurtspoorwegen / Tramways Vicinaux            | station          | -            |
      | 26    | Grote Markt Hasselt                            | street           | yellow       |
      | 27    | Place de l'Ange Namur                          | street           | yellow       |
      | 28    | Watermaatschappij / Compagnie des Eaux         | utility          | -            |
      | 29    | Hoogstraat (Brussel) / Rue Haute (Bruxelles)   | street           | yellow       |
      | 30    | Naar de Gevangenis / Allez en Prison           | go to jail       | -            |
      | 31    | Boulevard Tirou Charleroi                      | street           | green        |
      | 32    | Veldstraat Gent                                | street           | green        |
      | 33    | Algemeen Fonds / Caisse de Communauté          | community chest | -            |
      | 34    | Boulevard d'Avroy Liège                        | street           | green        |
      | 35    | Zuid Station / Gare du Midi                    | station          | -            |
      | 36    | Kans / Chance                                  | chance           | -            |
      | 37    | Meir Antwerpen                                 | street           | dark blue    |
      | 38    | Extra Belasting / Taxe de Luxe                 | tax              | -            |
      | 39    | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | street           | dark blue    |

  # official-rules-4
  Scenario Outline: pawn
    When we select 8 players
    Then pawn "<name>" is at play

    Examples:
      | name        |
      | dog         |
      | high hat    |
      | iron box    |
      | racecar     |
      | ship        |
      | shoe        |
      | thimble     |
      | wheelbarrow |

  # official-rules-5
  Scenario Outline: player starting balance
    When we select 8 players
    Then pawn "<name>"'s account balance is $1500

    Examples:
      | name        |
      | dog         |
      | high hat    |
      | iron box    |
      | racecar     |
      | ship        |
      | shoe        |
      | thimble     |
      | wheelbarrow |

  # official-rules-6
  Scenario: players receive a salary when passing by start
    Given a player
    And with $1500 in his account
    When the player passes the street "Start"
    Then the player's account balance is $1700

  # official-rules-7
  Scenario: players receive a double salary when landing on start
    And with optional double salary when landing on Start rule
    And a player
    And with $1500 in his account
    When the player lands on the street "Start"
    Then the player's account balance is $1900
