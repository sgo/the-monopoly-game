# mutation-stamp: sha256=5b2907d32ca1528df5d6f3102fc8e7943e5bde6d037459df1cb8c58fa958a3fc
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-24T20:30:02.989752Z","feature_name":"streets","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/components/streets.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"start","scenario_hash":"473c0354276bf032dcd814c88903bf3a7d7240938d6efbd3cae9352baac422a9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-27T11:55:41.409197Z"},{"index":1,"name":"colour street","scenario_hash":"847b71962e86c66d5748b1f0b864cbb6d15fc6dac30e10b7bd6ce71f5d499295","mutation_count":220,"result":{"Total":220,"Killed":220,"Survived":0,"Errors":0},"tested_at":"2026-07-27T10:30:40.689503Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: streets

  # streets-1
  Scenario Outline: start
    Given the street "Start"
    Then your salary is $<salary>

    Examples:
      | salary |
      | 200    |

  # streets-2
  Scenario Outline: colour street
    Given the street "<name>"
    Then the street value is $<value>
    And vacant rent is $<vacant rent>
    And rent for 1 house is $<rent for 1 house>
    And rent for 2 houses is $<rent for 2 houses>
    And rent for 3 houses is $<rent for 3 houses>
    And rent for 4 houses is $<rent for 4 houses>
    And rent for 1 hotel is $<rent for 1 hotel>
    And house construction cost is $<construction cost>
    And hotel construction cost is $<construction cost>
    And hotel construction requires 4 existing houses
    And mortgage value of the land is $<mortgage>

    Examples:
      | name                                          | value | vacant rent | rent for 1 house | rent for 2 houses | rent for 3 houses | rent for 4 houses | rent for 1 hotel | construction cost | mortgage |
      | Rue Grande Dinant                              | 60    | 2           | 10                | 30                 | 90                 | 160                | 250              | 50                 | 30       |
      | Diestsestraat Leuven                           | 60    | 4           | 20                | 60                 | 180                | 320                | 450              | 50                 | 30       |
      | Steenstraat Brugge                             | 100   | 6           | 30                | 90                 | 270                | 400                | 550              | 50                 | 50       |
      | Place du Monument Spa                          | 100   | 6           | 30                | 90                 | 270                | 400                | 550              | 50                 | 50       |
      | Kapellestraat Oostende                         | 120   | 8           | 40                | 100                | 300                | 450                | 600              | 50                 | 60       |
      | Rue de Diekirch Arlon                          | 140   | 10          | 50                | 150                | 450                | 625                | 750              | 100                | 70       |
      | Bruul Mechelen                                 | 140   | 10          | 50                | 150                | 450                | 625                | 750              | 100                | 70       |
      | Place Verte Verviers                           | 160   | 12          | 60                | 180                | 500                | 700                | 900              | 100                | 80       |
      | Lippenslaan Knokke                             | 180   | 14          | 70                | 200                | 550                | 750                | 950              | 100                | 90       |
      | Rue Royale Tournai                             | 180   | 14          | 70                | 200                | 550                | 750                | 950              | 100                | 90       |
      | Groenplaats Antwerpen                          | 200   | 16          | 80                | 220                | 600                | 800                | 1000             | 100                | 100      |
      | Rue St-Léonard Liège                           | 220   | 18          | 90                | 250                | 700                | 875                | 1050             | 150                | 110      |
      | Lange Steenstraat Kortrijk                     | 220   | 18          | 90                | 250                | 700                | 875                | 1050             | 150                | 110      |
      | Grand Place Mons                               | 240   | 20          | 100               | 300                | 750                | 925                | 1100             | 150                | 120      |
      | Grote Markt Hasselt                            | 260   | 22          | 110               | 330                | 800                | 975                | 1150             | 150                | 130      |
      | Place de l'Ange Namur                          | 260   | 22          | 110               | 330                | 800                | 975                | 1150             | 150                | 130      |
      | Hoogstraat (Brussel) / Rue Haute (Bruxelles)   | 280   | 24          | 120               | 360                | 850                | 1025               | 1200             | 150                | 140      |
      | Boulevard Tirou Charleroi                      | 300   | 26          | 130               | 390                | 900                | 1100               | 1275             | 200                | 150      |
      | Veldstraat Gent                                | 300   | 26          | 130               | 390                | 900                | 1100               | 1275             | 200                | 150      |
      | Boulevard d'Avroy Liège                        | 320   | 28          | 150               | 450                | 1000               | 1200               | 1400             | 200                | 160      |
      | Meir Antwerpen                                 | 350   | 35          | 175               | 500                | 1100               | 1300               | 1500             | 200                | 175      |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | 400   | 50          | 200               | 600                | 1400               | 1700               | 2000             | 200                | 200      |
