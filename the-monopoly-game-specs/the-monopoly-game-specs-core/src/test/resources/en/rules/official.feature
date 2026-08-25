# mutation-stamp: sha256=519d2c66e03d886af861de10c8639fbf213a482c085f6ad8333b830b66a073a8
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T08:46:11.310767Z","feature_name":"official rules","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/official.feature","background_hash":"9a3c0c411c487ac29cd37776a8a944a1b977e6980fe8a564f53528f8edf6826a","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the game is played with 2 dice with 6 faces","scenario_hash":"e6b7c9b1ecaca676e6ad08ff8ca1ba2b0422d5c500eec313b3f42996191fa32b","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-27T11:56:11.252629Z"},{"index":1,"name":"the game is played with a minimum of 2 to a maximum of 8 players","scenario_hash":"feeb129136baefc7772caa16089a6aa56d110434ea9ebf63ab0935112237db22","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-27T11:56:11.252629Z"},{"index":5,"name":"players receive a salary when passing by start","scenario_hash":"ca331eb621124df5bd7b30375558c71b69cfdc9f193f8ae4a5aaa6ce724db7b3","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-27T11:56:11.252629Z"},{"index":6,"name":"players receive a double salary when landing on start","scenario_hash":"ff0f41f07bd16ea5fba4fdd0905e2ceccbd39bba82b4feb43ea534a66d2bc410","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-27T11:56:11.252629Z"},{"index":7,"name":"players still receive a single salary when passing start with the double salary rule active","scenario_hash":"c649dfe32e9091ae18cfb378b653111a86c50d7ecc82bda9bfc7d648d513301c","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-27T11:56:11.252629Z"},{"index":2,"name":"board space","scenario_hash":"09128ee2487d2d9b8d6c78029278a2fc369d91b0985ff75aa1a4ad6e07c4ba00","mutation_count":160,"result":{"Total":160,"Killed":160,"Survived":0,"Errors":0},"tested_at":"2026-07-27T10:37:41.538530Z"},{"index":3,"name":"pawn","scenario_hash":"3027a961f68f42b735263d6c81d4fae84977a6166be27675ba3ae52c626bd95e","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-07-27T10:37:41.538530Z"},{"index":4,"name":"player starting balance","scenario_hash":"abe6e82c4f390574a76dd8936e96992c5ae8744ad3b47f1dedec4f21ae87a801","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-07-27T10:37:41.538530Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: official rules

  Background:
    Given the official rule set

  # official-rules-1
  Scenario Outline: the game is played with 2 dice with 6 faces
    Then dice 1 is <die 1 type>
    And dice 2 is <die 2 type>

    Examples:
      | die 1 type | die 2 type |
      | 6 faced    | 6 faced    |

  # official-rules-2
  Scenario Outline: the game is played with a minimum of 2 to a maximum of 8 players
    Then we play with <minimum> to <maximum> players

    Examples:
      | minimum | maximum |
      | 2       | 8       |

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
  Scenario Outline: players receive a salary when passing by start
    Given a player
    And with $<starting balance> in his account
    When the player passes the street "Start"
    Then the player's account balance is $<final balance>

    Examples:
      | starting balance | final balance |
      | 1500              | 1700          |

  # official-rules-7
  Scenario Outline: players receive a double salary when landing on start
    And with optional double salary when landing on Start rule
    And a player
    And with $<starting balance> in his account
    When the player lands on the street "Start"
    Then the player's account balance is $<final balance>

    Examples:
      | starting balance | final balance |
      | 1500              | 1900          |

  # official-rules-8
  Scenario Outline: players still receive a single salary when passing start with the double salary rule active
    And with optional double salary when landing on Start rule
    And a player
    And with $<starting balance> in his account
    When the player passes the street "Start"
    Then the player's account balance is $<final balance>

    Examples:
      | starting balance | final balance |
      | 1500              | 1700          |
