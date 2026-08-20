# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-20T10:10:59.770127Z","feature_name":"movement","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/movement.feature","background_hash":"80dedfe78ed247941df255c70f71da4b7cf0de570f204b8a3391201f79c63954","implementation_hash":"unknown","scenarios":[{"index":4,"name":"rolling doubles while in jail releases the player and moves that number of spaces","scenario_hash":"2ba3764eaccf02fe9ccb41204174a8fb2cb47d65cb87fc902baca2193abd1da2","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:39:49.716230Z"},{"index":3,"name":"rolling doubles three times in a row sends the player directly to jail","scenario_hash":"a95792d42b70bcdc0dcf19fe4124aeae4ead786d1b788c0969a6671ff1f292ad","mutation_count":7,"result":{"Total":7,"Killed":7,"Survived":0,"Errors":0},"tested_at":"2026-07-27T16:28:53.985888Z"},{"index":0,"name":"moving without passing start","scenario_hash":"14282012d566e032ec8b165feb525cd919c194e181ca1c811a30ddad2e67f4db","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-27T13:10:56.549823Z"},{"index":1,"name":"the double salary rule applies only when a turn lands exactly on start","scenario_hash":"330ed740db11936196b9126ee3373e1dc2326482a36182d8ea39b35d2241a97c","mutation_count":12,"result":{"Total":12,"Killed":12,"Survived":0,"Errors":0},"tested_at":"2026-07-27T13:10:56.549823Z"},{"index":2,"name":"rolling doubles grants another roll in the same turn","scenario_hash":"5d719a6fe0dcbe7f248f3a2d320bd81abdf03bfced28be951596138a56ab42cc","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-27T13:10:56.549823Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: movement

  Background:
    Given the official rule set
    And a player

  # movement-1
  Scenario Outline: moving without passing start
    And with $<starting balance> in his account
    And the player is at position <starting position>
    And the next roll will be <die 1> and <die 2>
    When the player takes a turn
    Then the player is at position <final position>
    And the player's account balance is $<final balance>

    Examples:
      | starting balance | starting position | die 1 | die 2 | final position | final balance |
      | 1500              | 5                  | 2     | 3     | 10             | 1500          |

  # movement-2
  Scenario Outline: the double salary rule applies only when a turn lands exactly on start
    And with optional double salary when landing on Start rule
    And with $<starting balance> in his account
    And the player is at position <starting position>
    And the next roll will be <die 1> and <die 2>
    When the player takes a turn
    Then the player is at position <final position>
    And the player's account balance is $<final balance>

    Examples:
      | starting balance | starting position | die 1 | die 2 | final position | final balance |
      | 1500              | 37                 | 1     | 2     | 0              | 1900          |
      | 1500              | 37                 | 2     | 3     | 2              | 1700          |

  # movement-3
  Scenario Outline: rolling doubles grants another roll in the same turn
    And with $<starting balance> in his account
    And the player is at position <starting position>
    And the next roll will be <double roll> and <double roll>
    And the next roll will be <second roll die 1> and <second roll die 2>
    When the player takes a turn
    Then the player is at position <final position>

    Examples:
      | starting balance | starting position | double roll | second roll die 1 | second roll die 2 | final position |
      | 1500              | 0                  | 3           | 2                  | 4                  | 12             |

  # movement-4
  Scenario Outline: rolling doubles three times in a row sends the player directly to jail
    And with $<starting balance> in his account
    And the player is at position <starting position>
    And the next roll will be <first double> and <first double>
    And the next roll will be <second double> and <second double>
    And the next roll will be <third double> and <third double>
    When the player takes a turn
    Then the player is at position <final position>
    And the player's account balance is $<final balance>

    Examples:
      | starting balance | starting position | first double | second double | third double | final position | final balance |
      | 1500              | 0                  | 2             | 5              | 1            | 10              | 1500          |

  # movement-5
  Scenario Outline: rolling doubles while in jail releases the player and moves that number of spaces
    And the player is in jail
    And the next roll will be <die> and <die>
    When the player takes a turn
    Then the player is at position <final position>
    And the player is no longer in jail

    Examples:
      | die | final position |
      | 3   | 16             |

  # movement-6
  Scenario Outline: failing to roll doubles for three turns in jail forces the fine before moving
    And with $<starting balance> in his account
    And the player is in jail
    And the next roll will be <first die 1> and <first die 2>
    And the next roll will be <second die 1> and <second die 2>
    And the next roll will be <third die 1> and <third die 2>
    When the player takes a turn
    And the player takes a turn
    And the player takes a turn
    Then the player is at position <final position>
    And the player's account balance is $<final balance>
    And the player is no longer in jail

    Examples:
      | starting balance | first die 1 | first die 2 | second die 1 | second die 2 | third die 1 | third die 2 | final position | final balance |
      | 100               | 1           | 2           | 2            | 4            | 3           | 5           | 18             | 50            |
