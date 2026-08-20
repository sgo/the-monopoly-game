# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-20T10:21:33.423894Z","feature_name":"Greedo peer trading between non-leading players","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-peer-trade.feature","background_hash":"e15f13aafcac0600c3aaaaf97d370d153eb29c5c34b3d00e93ab47602feefe9c","implementation_hash":"unknown","scenarios":[]}
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
    When pawn "dog" considers trading "<dog_offered>" to pawn "high hat" for "<dog_wanted>" with the "<strategy>" strategy
    Then the "<strategy>" strategy <decision> the trade

    Examples:
      | strategy | dog_owned | dog_offered | dog_wanted | decision |
      | Greedo | Rue Grande Dinant | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles) | Diestsestraat Leuven | accepts |
      | Billionaire | Rue Grande Dinant | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles) | Diestsestraat Leuven | accepts |
      | Greedo | Rue Grande Dinant | Rue de Diekirch Arlon | Diestsestraat Leuven | accepts |
      | Billionaire | Rue Grande Dinant | Rue de Diekirch Arlon | Diestsestraat Leuven | accepts |
      | Greedo | Meir Antwerpen | Rue de Diekirch Arlon | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles) | accepts |
      | Billionaire | Meir Antwerpen | Rue de Diekirch Arlon | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles) | accepts |

  # greedo-trade-2
  Scenario Outline: Greedo declines a trade that would not complete a monopoly
    Given pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" owns "Diestsestraat Leuven"
    When pawn "dog" considers trading "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)" to pawn "high hat" for "Diestsestraat Leuven" with the "<strategy>" strategy
    Then the "<strategy>" strategy <decision> the trade

    Examples:
      | strategy | decision |
      | Greedo | declines |
      | Billionaire | declines |

  # greedo-trade-3
  Scenario Outline: Greedo declines a trade with a partner who already owns a complete highest-priority monopoly
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" owns "Steenstraat Brugge"
    And pawn "high hat" owns "Place du Monument Spa"
    And pawn "high hat" owns "Kapellestraat Oostende"
    When pawn "dog" considers trading "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)" to pawn "high hat" for "Diestsestraat Leuven" with the "<strategy>" strategy
    Then the "<strategy>" strategy <decision> the trade

    Examples:
      | strategy | decision |
      | Greedo | declines |
      | Billionaire | declines |

  # greedo-trade-4
  Scenario Outline: Greedo declines a trade that would give up a highest-priority street
    Given pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "Diestsestraat Leuven"
    When pawn "dog" considers trading "<dog_offered>" to pawn "high hat" for "Diestsestraat Leuven" with the "<strategy>" strategy
    Then the "<strategy>" strategy <decision> the trade

    Examples:
      | strategy | dog_offered | decision |
      | Greedo | Groenplaats Antwerpen | declines |
      | Billionaire | Groenplaats Antwerpen | declines |

  # greedo-trade-5
  Scenario Outline: Greedo declines a trade where the offered and wanted streets belong to the same colour group
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    When pawn "dog" considers trading "Meir Antwerpen" to pawn "high hat" for "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)" with the "<strategy>" strategy
    Then the "<strategy>" strategy <decision> the trade

    Examples:
      | strategy | decision |
      | Greedo | declines |
      | Billionaire | declines |
