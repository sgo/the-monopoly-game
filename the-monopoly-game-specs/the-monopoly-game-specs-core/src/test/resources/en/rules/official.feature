# language: en

Feature: official rules

  Background:
    Given the official rule set

  # official-rules-1
  Scenario: the game is played with 2 dice with 6 faces
    Then we play with the following dice
      | type    |
      | 6 faced |
      | 6 faced |

  # official-rules-2
  Scenario: the game is played with a minimum of 2 to a maximum of 8 players
    Then we play with 2 to 8 players

  # official-rules-3
  Scenario: gameboard layout
    Then the gameboard layout is
      | space name                                     | type             | colour group |
      | Start                                          | start            | -            |
      | Rue Grande Dinant                              | street           | brown        |
      | Algemeen Fonds / Caisse de Communauté          | community chest | -            |
      | Diestsestraat Leuven                           | street           | brown        |
      | Inkomsten Belasting / Impôts sur le revenu     | tax              | -            |
      | Noord Station / Gare du Nord                   | station          | -            |
      | Steenstraat Brugge                             | street           | light blue   |
      | Kans / Chance                                  | chance           | -            |
      | Place du Monument Spa                          | street           | light blue   |
      | Kapellestraat Oostende                         | street           | light blue   |
      | Op Bezoek / Simple Visite                      | jail             | -            |
      | Rue de Diekirch Arlon                          | street           | pink         |
      | Elektriciteitscentrale / Centrale Électrique   | utility          | -            |
      | Bruul Mechelen                                 | street           | pink         |
      | Place Verte Verviers                           | street           | pink         |
      | Centraal Station / Gare Centrale               | station          | -            |
      | Lippenslaan Knokke                             | street           | orange       |
      | Algemeen Fonds / Caisse de Communauté          | community chest | -            |
      | Rue Royale Tournai                             | street           | orange       |
      | Groenplaats Antwerpen                          | street           | orange       |
      | Gratis Parkeren / Parc Gratuit                 | free parking     | -            |
      | Rue St-Léonard Liège                           | street           | red          |
      | Kans / Chance                                  | chance           | -            |
      | Lange Steenstraat Kortrijk                     | street           | red          |
      | Grand Place Mons                               | street           | red          |
      | Buurtspoorwegen / Tramways Vicinaux            | station          | -            |
      | Grote Markt Hasselt                            | street           | yellow       |
      | Place de l'Ange Namur                          | street           | yellow       |
      | Watermaatschappij / Compagnie des Eaux         | utility          | -            |
      | Hoogstraat (Brussel) / Rue Haute (Bruxelles)   | street           | yellow       |
      | Naar de Gevangenis / Allez en Prison           | go to jail       | -            |
      | Boulevard Tirou Charleroi                      | street           | green        |
      | Veldstraat Gent                                | street           | green        |
      | Algemeen Fonds / Caisse de Communauté          | community chest | -            |
      | Boulevard d'Avroy Liège                        | street           | green        |
      | Zuid Station / Gare du Midi                    | station          | -            |
      | Kans / Chance                                  | chance           | -            |
      | Meir Antwerpen                                 | street           | dark blue    |
      | Extra Belasting / Taxe de Luxe                 | tax              | -            |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | street           | dark blue    |

  # official-rules-4
  Scenario: the players in the game can be identified by their pawn
    When we select 8 players
    Then the following pawns are at play
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
  Scenario: each player starts the game with starting capital
    When we select 8 players
    Then the following bank accounts are at play
      | owner       | balance |
      | dog         | $1500   |
      | high hat    | $1500   |
      | iron box    | $1500   |
      | racecar     | $1500   |
      | ship        | $1500   |
      | shoe        | $1500   |
      | thimble     | $1500   |
      | wheelbarrow | $1500   |

  # official-rules-6
  Scenario: players receive a salary when passing by start
    Given a player
    * with $1500 in his account
    When the player passes the street "Start"
    Then the player's account balance is $1700

  # official-rules-7
  Scenario: players receive a double salary when landing on start
    * with optional double salary when landing on Start rule
    And a player
    * with $1500 in his account
    When the player lands on the street "Start"
    Then the player's account balance is $1900
