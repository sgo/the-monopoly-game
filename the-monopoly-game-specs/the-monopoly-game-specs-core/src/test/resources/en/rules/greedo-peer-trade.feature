# mutation-stamp: sha256=c0a3a330f71f80f78bf79c612904d84814db36a099cd1f92d874531e3b490aaa
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T08:30:33.177245Z","feature_name":"Greedo peer trading between non-leading players","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-peer-trade.feature","background_hash":"e15f13aafcac0600c3aaaaf97d370d153eb29c5c34b3d00e93ab47602feefe9c","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Greedo trades a spare street to complete a monopoly, but never gives up a highest-priority street","scenario_hash":"d04c30e78b22df0173591a4eefb2b7eb2ed56622a2b2e6252d293dfb07b82725","mutation_count":29,"result":{"Total":29,"Killed":29,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:34.151121Z"},{"index":1,"name":"Greedo declines a trade that would not complete a monopoly","scenario_hash":"97f8cbcc4a3541e67b55b4a1bfb4ee1534dd42204874a1b7b71add689b542cf5","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:34.151121Z"},{"index":2,"name":"Greedo declines a trade with a partner who already owns a complete highest-priority monopoly","scenario_hash":"9c80007c9d7c6ee9419b3bee36eefcd8a22c6aee4f2385e9615d2456ae5accd7","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:34.151121Z"},{"index":3,"name":"Greedo declines a trade that would give up a highest-priority street","scenario_hash":"99bf00ce0d02a6279defe1fb7ab802e26bfb6e0597aa7ca9b17e0866b21fe4c4","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:34.151121Z"},{"index":4,"name":"Greedo declines a trade where the offered and wanted streets belong to the same colour group","scenario_hash":"fab14dee1329ffdccb8ccb42d2c34c6ba40f20e1c1d5c6f664e7fe4d85a5629b","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:34.151121Z"}]}
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
