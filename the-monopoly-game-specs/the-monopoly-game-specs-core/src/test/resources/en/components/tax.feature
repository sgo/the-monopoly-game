# mutation-stamp: sha256=219dcdd4d7938af3e6ccbc2179586c7bacb6ab5a08088acefa3930be11503b41
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T08:22:01.386626Z","feature_name":"tax","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/components/tax.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"tax space","scenario_hash":"487dcc060fadfaa690357169a1aac6e73c338f83276733d348793933b847293c","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-27T10:30:45.147738Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: tax

  # tax-1
  Scenario Outline: tax space
    Given the tax space "<name>"
    Then the tax is $<amount>

    Examples:
      | name                                        | amount |
      | Inkomsten Belasting / Impôts sur le revenu   | 200    |
      | Extra Belasting / Taxe de Luxe               | 100    |
