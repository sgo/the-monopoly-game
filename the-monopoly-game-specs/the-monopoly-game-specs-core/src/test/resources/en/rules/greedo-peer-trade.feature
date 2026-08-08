# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-08T08:36:19.963424Z","feature_name":"Greedo peer trading between non-leading players","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-peer-trade.feature","background_hash":"e15f13aafcac0600c3aaaaf97d370d153eb29c5c34b3d00e93ab47602feefe9c","implementation_hash":"unknown","scenarios":[]}
# acceptance-mutation-manifest-end

# language: en

Feature: Greedo peer trading between non-leading players

  Background:
    Given the official rule set
    And we select 2 players

  # greedo-trade-1
  Scenario Outline: Greedo trades a spare street to complete a monopoly, but never gives up a highest-priority street
    Given pawn "dog" owns "<dog_owned>"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    When pawn "dog" considers trading "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    Then the "Greedo" strategy <decision> the trade

    Examples:
      | dog_owned         | dog_offered                                    | dog_wanted                                     | decision |
      | Rue Grande Dinant | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven                           | accepts  |
      | Rue Grande Dinant | Rue de Diekirch Arlon                          | Diestsestraat Leuven                           | accepts  |
      | Meir Antwerpen    | Rue de Diekirch Arlon                          | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | accepts  |

  # greedo-trade-2
  Scenario Outline: Greedo declines a trade that would not complete a monopoly
    Given pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" owns "Diestsestraat Leuven"
    When pawn "dog" considers trading "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)" to pawn "high hat" for "Diestsestraat Leuven"
    Then the "Greedo" strategy <decision> the trade

    Examples:
      | decision |
      | declines |

  # greedo-trade-3
  Scenario Outline: Greedo declines a trade with a partner who already owns a complete highest-priority monopoly
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" owns "Steenstraat Brugge"
    And pawn "high hat" owns "Place du Monument Spa"
    And pawn "high hat" owns "Kapellestraat Oostende"
    When pawn "dog" considers trading "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)" to pawn "high hat" for "Diestsestraat Leuven"
    Then the "Greedo" strategy <decision> the trade

    Examples:
      | decision |
      | declines |

  # greedo-trade-4
  Scenario Outline: Greedo declines a trade that would give up a highest-priority street
    Given pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "Diestsestraat Leuven"
    When pawn "dog" considers trading "<dog_offered>" to pawn "high hat" for "Diestsestraat Leuven"
    Then the "Greedo" strategy <decision> the trade

    Examples:
      | dog_offered            | decision |
      | Groenplaats Antwerpen  | declines |
