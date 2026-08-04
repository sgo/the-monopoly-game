# language: en

Feature: Greedo strategy priority table

  # greedo-priority-1
  Scenario Outline: Greedo's monopoly-building priority for every purchaseable space on the board
    Then the "Greedo" strategy's priority for "<space>" is "<priority>"

    Examples:
      | space                                        | priority |
      | Lippenslaan Knokke                            | highest  |
      | Rue Royale Tournai                             | highest  |
      | Groenplaats Antwerpen                          | highest  |
      | Rue St-Léonard Liège                           | highest  |
      | Lange Steenstraat Kortrijk                     | highest  |
      | Grand Place Mons                               | highest  |
      | Steenstraat Brugge                             | highest  |
      | Place du Monument Spa                          | highest  |
      | Kapellestraat Oostende                         | highest  |
      | Rue Grande Dinant                              | middle   |
      | Diestsestraat Leuven                           | middle   |
      | Rue de Diekirch Arlon                          | middle   |
      | Bruul Mechelen                                 | middle   |
      | Place Verte Verviers                           | middle   |
      | Grote Markt Hasselt                            | middle   |
      | Place de l'Ange Namur                          | middle   |
      | Hoogstraat (Brussel) / Rue Haute (Bruxelles)   | middle   |
      | Boulevard Tirou Charleroi                      | lowest   |
      | Veldstraat Gent                                | lowest   |
      | Boulevard d'Avroy Liège                        | lowest   |
      | Meir Antwerpen                                 | lowest   |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | lowest   |
      | Elektriciteitscentrale                         | lowest   |
      | Watermaatschappij                              | lowest   |
      | Noord Station                                  | lowest   |
      | Centraal Station                               | lowest   |
      | Buurtspoorwegen                                | lowest   |
      | Zuid Station                                   | lowest   |
