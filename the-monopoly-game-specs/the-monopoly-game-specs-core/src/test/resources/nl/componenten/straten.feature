# language: nl

Functionaliteit: straten

  # straten-1
  Scenario: start
    Gegeven de straat "Start"
    Dan is je loon €200

  # straten-2
  Abstract Scenario: kleur straat
    Gegeven de straat "<naam>"
    Dan is de waarde van de straat €<waarde>
    En onbebouwde huur is €<onbebouwde huur>
    En huur voor 1 huis is €<huur voor 1 huis>
    En huur voor 2 huizen is €<huur voor 2 huizen>
    En huur voor 3 huizen is €<huur voor 3 huizen>
    En huur voor 4 huizen is €<huur voor 4 huizen>
    En huur voor 1 hotel is €<huur voor 1 hotel>
    En bouw kost voor een huis is €<bouw kost>
    En bouw kost voor een hotel is €<bouw kost>
    En bouw van een hotel vereist 4 bestaande huizen
    En hypotheekwaarde van het land is €<hypotheek>

    Voorbeelden:
      | naam                                           | waarde | onbebouwde huur | huur voor 1 huis | huur voor 2 huizen | huur voor 3 huizen | huur voor 4 huizen | huur voor 1 hotel | bouw kost | hypotheek |
      | Rue Grande Dinant                              | 60     | 2               | 10                | 30                  | 90                  | 160                 | 250               | 50        | 30        |
      | Diestsestraat Leuven                           | 60     | 4               | 20                | 60                  | 180                 | 320                 | 450               | 50        | 30        |
      | Steenstraat Brugge                             | 100    | 6               | 30                | 90                  | 270                 | 400                 | 550               | 50        | 50        |
      | Place du Monument Spa                          | 100    | 6               | 30                | 90                  | 270                 | 400                 | 550               | 50        | 50        |
      | Kapellestraat Oostende                         | 120    | 8               | 40                | 100                 | 300                 | 450                 | 600               | 50        | 60        |
      | Rue de Diekirch Arlon                          | 140    | 10              | 50                | 150                 | 450                 | 625                 | 750               | 100       | 70        |
      | Bruul Mechelen                                 | 140    | 10              | 50                | 150                 | 450                 | 625                 | 750               | 100       | 70        |
      | Place Verte Verviers                           | 160    | 12              | 60                | 180                 | 500                 | 700                 | 900               | 100       | 80        |
      | Lippenslaan Knokke                             | 180    | 14              | 70                | 200                 | 550                 | 750                 | 950               | 100       | 90        |
      | Rue Royale Tournai                             | 180    | 14              | 70                | 200                 | 550                 | 750                 | 950               | 100       | 90        |
      | Groenplaats Antwerpen                          | 200    | 16              | 80                | 220                 | 600                 | 800                 | 1000              | 100       | 100       |
      | Rue St-Léonard Liège                           | 220    | 18              | 90                | 250                 | 700                 | 875                 | 1050              | 150       | 110       |
      | Lange Steenstraat Kortrijk                     | 220    | 18              | 90                | 250                 | 700                 | 875                 | 1050              | 150       | 110       |
      | Grand Place Mons                               | 240    | 20              | 100               | 300                 | 750                 | 925                 | 1100              | 150       | 120       |
      | Grote Markt Hasselt                            | 260    | 22              | 110               | 330                 | 800                 | 975                 | 1150              | 150       | 130       |
      | Place de l'Ange Namur                          | 260    | 22              | 110               | 330                 | 800                 | 975                 | 1150              | 150       | 130       |
      | Hoogstraat (Brussel) / Rue Haute (Bruxelles)   | 280    | 24              | 120               | 360                 | 850                 | 1025                | 1200              | 150       | 140       |
      | Boulevard Tirou Charleroi                      | 300    | 26              | 130               | 390                 | 900                 | 1100                | 1275              | 200       | 150       |
      | Veldstraat Gent                                | 300    | 26              | 130               | 390                 | 900                 | 1100                | 1275              | 200       | 150       |
      | Boulevard d'Avroy Liège                        | 320    | 28              | 150               | 450                 | 1000                | 1200                | 1400              | 200       | 160       |
      | Meir Antwerpen                                 | 350    | 35              | 175               | 500                 | 1100                | 1300                | 1500              | 200       | 175       |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | 400    | 50              | 200               | 600                 | 1400                | 1700                | 2000              | 200       | 200       |
