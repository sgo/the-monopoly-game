# language: nl

Functionaliteit: officiële regels

  Achtergrond:
    Gegeven de officiële regels

  # officiele-regels-1
  Scenario: het spel wordt gespeeld met 2 dobbelstenen met 6 zijdes
    Dan spelen we met de volgende dobbelstenen
      | type     |
      | 6 zijdig |
      | 6 zijdig |

  # officiele-regels-2
  Scenario: het spel wordt gespeeld met een minimum van 2 tot een maximum van 8 spelers
    Dan spelen we met 2 tot 8 spelers

  # officiele-regels-3
  Scenario: layout van het spelbord
    Dan is de layout van het spelbord
      | ruimte naam                                    | type             | kleur groep |
      | Start                                          | start            | -           |
      | Rue Grande Dinant                              | straat           | bruin       |
      | Algemeen Fonds / Caisse de Communauté          | algemeen fonds   | -           |
      | Diestsestraat Leuven                           | straat           | bruin       |
      | Inkomsten Belasting / Impôts sur le revenu     | belasting        | -           |
      | Noord Station / Gare du Nord                   | station          | -           |
      | Steenstraat Brugge                             | straat           | lichtblauw  |
      | Kans / Chance                                  | kans             | -           |
      | Place du Monument Spa                          | straat           | lichtblauw  |
      | Kapellestraat Oostende                         | straat           | lichtblauw  |
      | Op Bezoek / Simple Visite                      | op bezoek        | -           |
      | Rue de Diekirch Arlon                          | straat           | roze        |
      | Elektriciteitscentrale / Centrale Électrique   | nutsbedrijf      | -           |
      | Bruul Mechelen                                 | straat           | roze        |
      | Place Verte Verviers                           | straat           | roze        |
      | Centraal Station / Gare Centrale               | station          | -           |
      | Lippenslaan Knokke                             | straat           | oranje      |
      | Algemeen Fonds / Caisse de Communauté          | algemeen fonds   | -           |
      | Rue Royale Tournai                             | straat           | oranje      |
      | Groenplaats Antwerpen                          | straat           | oranje      |
      | Gratis Parkeren / Parc Gratuit                 | gratis parkeren  | -           |
      | Rue St-Léonard Liège                           | straat           | rood        |
      | Kans / Chance                                  | kans             | -           |
      | Lange Steenstraat Kortrijk                     | straat           | rood        |
      | Grand Place Mons                               | straat           | rood        |
      | Buurtspoorwegen / Tramways Vicinaux            | station          | -           |
      | Grote Markt Hasselt                            | straat           | geel        |
      | Place de l'Ange Namur                          | straat           | geel        |
      | Watermaatschappij / Compagnie des Eaux         | nutsbedrijf      | -           |
      | Hoogstraat (Brussel) / Rue Haute (Bruxelles)   | straat           | geel        |
      | Naar de Gevangenis / Allez en Prison           | naar de gevangenis | -         |
      | Boulevard Tirou Charleroi                      | straat           | groen       |
      | Veldstraat Gent                                | straat           | groen       |
      | Algemeen Fonds / Caisse de Communauté          | algemeen fonds   | -           |
      | Boulevard d'Avroy Liège                        | straat           | groen       |
      | Zuid Station / Gare du Midi                    | station          | -           |
      | Kans / Chance                                  | kans             | -           |
      | Meir Antwerpen                                 | straat           | donkerblauw |
      | Extra Belasting / Taxe de Luxe                 | belasting        | -           |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | straat           | donkerblauw |

  # officiele-regels-4
  Scenario: de spelers in het spel kunnen geïdentificeerd worden aan de hand van hun pion
    Als we 8 spelers kiezen
    Dan staan de volgende pionnen in het spel
      | naam        |
      | hoge hoed   |
      | hond        |
      | kruiwagen   |
      | raceauto    |
      | schip       |
      | schoen      |
      | strijkijzer |
      | vingerhoed  |

  # officiele-regels-5
  Scenario: elke speler begint het spel met een start kapitaal
    Als we 8 spelers kiezen
    Dan bestaan de volgende bank rekeningen in het spel
      | eigenaar    | balans |
      | hoge hoed   | €1500  |
      | hond        | €1500  |
      | kruiwagen   | €1500  |
      | raceauto    | €1500  |
      | schip       | €1500  |
      | schoen      | €1500  |
      | strijkijzer | €1500  |
      | vingerhoed  | €1500  |

  # officiele-regels-6
  Scenario: spelers ontvangen een loon wanneer ze passeren langs start
    Gegeven een speler
    * met €1500 in zijn bank rekening
    Wanneer de speler langs de straat "Start" passeert
    Dan is de balans van de speler zijn bank rekening €1700

  # officiele-regels-7
  Scenario: spelers ontvangen een dubbel loon wanneer ze landen op start
    * met optionele dubbel loon bij het landen op Start regel
    En een speler
    * met €1500 in zijn bank rekening
    Wanneer de speler op de straat "Start" land
    Dan is de balans van de speler zijn bank rekening €1900
