# language: en

Feature: chance and community chest cards

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # cards-1
  Scenario Outline: a card that advances the pawn pays the START salary if it passes START
    Given the next chance card will be "Ga door naar START (Ontvang M200)."
    When pawn "dog" lands on "Kans / Chance"
    Then pawn "dog" is at position <position>
    And pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | position | expected_balance |
      | 0        | 1700              |

  # cards-2
  Scenario Outline: a card that instructs a flat payment to the bank
    Given the next chance card will be "Boete voor te snel rijden. Betaal M15."
    When pawn "dog" lands on "Kans / Chance"
    Then pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | expected_balance |
      | 1485              |

  # cards-3
  Scenario Outline: a card that instructs a flat payment from the bank
    Given the next chance card will be "De bank betaald je een dividend van M50."
    When pawn "dog" lands on "Kans / Chance"
    Then pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | expected_balance |
      | 1550              |

  # cards-4
  Scenario Outline: a card that instructs paying every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next chance card will be "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50."
    When pawn "dog" lands on "Kans / Chance"
    Then pawn "dog"'s account balance is $<expected_payer_balance>
    And pawn "high hat"'s account balance is $<expected_payee_balance>
    And pawn "iron box"'s account balance is $<expected_payee_balance>

    Examples:
      | expected_payer_balance | expected_payee_balance |
      | 1400                    | 1550                    |

  # cards-5
  Scenario Outline: a card that instructs collecting from every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next community chest card will be "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler."
    When pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    Then pawn "dog"'s account balance is $<expected_payee_balance>
    And pawn "high hat"'s account balance is $<expected_payer_balance>
    And pawn "iron box"'s account balance is $<expected_payer_balance>

    Examples:
      | expected_payee_balance | expected_payer_balance |
      | 1520                    | 1490                    |

  # cards-6
  Scenario Outline: a card that sends the pawn directly to jail pays no salary
    Given the next chance card will be "Ga naar de gevangenis. Passeer niet langs START, je ontvangt geen M200."
    When pawn "dog" lands on "Kans / Chance"
    Then pawn "dog" is at position <position>
    And pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | position | expected_balance |
      | 10       | 1500              |

  # cards-7
  Scenario Outline: drawing a "Get Out of Jail Free" card retains it instead of returning it to the deck
    Given the next community chest card will be "<card>"
    When pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    Then pawn "dog" holds a "Get Out of Jail Free" card

    Examples:
      | card                                                                                                                                                                       |
      | Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen. |

  # cards-8
  Scenario Outline: a retained "Get Out of Jail Free" card can be sold to another player
    Given the next community chest card will be "Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen."
    And pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    When pawn "dog" sells the "Get Out of Jail Free" card to pawn "high hat" for $<price>
    Then pawn "high hat" holds a "Get Out of Jail Free" card
    And pawn "dog"'s account balance is $<expected_seller_balance>
    And pawn "high hat"'s account balance is $<expected_buyer_balance>

    Examples:
      | price | expected_seller_balance | expected_buyer_balance |
      | 50    | 1550                     | 1450                    |

  # cards-9
  Scenario Outline: a card that advances to the nearest station lets the pawn buy it if unsold
    Given the next chance card will be "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs."
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" will buy "Centraal Station"
    When pawn "dog" lands on "Kans / Chance"
    Then pawn "dog" is at position <position>
    And pawn "dog" owns "Centraal Station"
    And pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | position | expected_balance |
      | 15       | 1300              |

  # cards-10
  Scenario Outline: a card that advances to the nearest station pays the owner double rent
    Given pawn "high hat" owns "Centraal Station"
    And the next chance card will be "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs."
    When pawn "dog" lands on "Kans / Chance"
    Then pawn "dog" is at position <position>
    And pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | position | expected_tenant_final_balance | expected_owner_final_balance |
      | 15       | 1450                            | 1550                           |

  # cards-11
  Scenario Outline: a card that advances to the nearest utility pays the owner ten times a freshly rolled dice total
    Given pawn "dog" starts at position 4
    And pawn "dog" will roll 1 and 2 for their turn
    And pawn "dog" will roll <die_1> and <die_2> for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And the next chance card will be "Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde."
    When we play the game
    Then pawn "dog" is at position <position>
    And pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | die_1 | die_2 | position | expected_tenant_final_balance | expected_owner_final_balance |
      | 3     | 4     | 12       | 1430                            | 1570                           |

  # cards-12
  Scenario Outline: a card that renovates all owned properties pays per house and hotel across the whole board
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has a hotel built
    And the street "Diestsestraat Leuven" has 1 house(s) built
    And the next chance card will be "Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel."
    When pawn "dog" lands on "Kans / Chance"
    Then pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | expected_balance |
      | 1375              |

  # cards-13
  Scenario Outline: every chance card resolves without error
    Given the next chance card will be "<card>"
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "<expected_card>"

    Examples:
      | card                                                                                                                                                       | expected_card                                                                                                                                              |
      | Ga door naar Nieuwstraat (Brussel) / Rue Neuve (Bruxelles).                                                                                                | Ga door naar Nieuwstraat (Brussel) / Rue Neuve (Bruxelles).                                                                                                |
      | Ga door naar START (Ontvang M200).                                                                                                                         | Ga door naar START (Ontvang M200).                                                                                                                         |
      | Ga door naar Grand Place (Mons). Als je langs START komt, ontvang je M200.                                                                                 | Ga door naar Grand Place (Mons). Als je langs START komt, ontvang je M200.                                                                                 |
      | Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.                                                                            | Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.                                                                            |
      | Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs. | Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs. |
      | Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde. | Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde. |
      | De bank betaald je een dividend van M50.                                                                                                                   | De bank betaald je een dividend van M50.                                                                                                                   |
      | Verlaat de gevangenis zonder te betalen.                                                                                                                   | Verlaat de gevangenis zonder te betalen.                                                                                                                   |
      | Keer 3 stappen terug.                                                                                                                                      | Keer 3 stappen terug.                                                                                                                                      |
      | Ga naar de gevangenis. Passeer niet langs START, je ontvangt geen M200.                                                                                    | Ga naar de gevangenis. Passeer niet langs START, je ontvangt geen M200.                                                                                    |
      | Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel.                                                                           | Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel.                                                                           |
      | Boete voor te snel rijden. Betaal M15.                                                                                                                     | Boete voor te snel rijden. Betaal M15.                                                                                                                     |
      | Ga door naar Noord Station / Gare du Nord. If you pass START, collect M200.                                                                                | Ga door naar Noord Station / Gare du Nord. If you pass START, collect M200.                                                                                |
      | Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50.                                                                                         | Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50.                                                                                         |
      | Je lening is afbetaald. Je ontvangt M150.                                                                                                                  | Je lening is afbetaald. Je ontvangt M150.                                                                                                                  |

  # cards-14
  Scenario Outline: every community chest card resolves without error
    Given the next community chest card will be "<card>"
    When pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    Then the game journal records that pawn "dog" draws the community chest card "<expected_card>"

    Examples:
      | card                                                                                                                                       | expected_card                                                                                                                              |
      | Je maakt elke week tijd vrij voor je bejaarde buurman — Je hebt geweldige verhalen gehoord! Je ontvant M100.                                 | Je maakt elke week tijd vrij voor je bejaarde buurman — Je hebt geweldige verhalen gehoord! Je ontvant M100.                                 |
      | Je organiseert een groep om de voetpaden op te ruimen. Je ontvangt M50.                                                                       | Je organiseert een groep om de voetpaden op te ruimen. Je ontvangt M50.                                                                       |
      | Je bent vrijwilliger bij het rode kruis. Er waren gratis koekjes! Je ontvangt M10.                                                            | Je bent vrijwilliger bij het rode kruis. Er waren gratis koekjes! Je ontvangt M10.                                                            |
      | Je koopt wat koekjes op het schoolfestival. Lekker! Je betaald M50.                                                                           | Je koopt wat koekjes op het schoolfestival. Lekker! Je betaald M50.                                                                           |
      | Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen. | Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen. |
      | je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler.                                      | je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler.                                      |
      | Luide muziek diep in de nacht? Je buren zijn boos. Ga naar de gevangenis. Je komt niet langs start START. Je ontvangt geen M200.              | Luide muziek diep in de nacht? Je buren zijn boos. Ga naar de gevangenis. Je komt niet langs start START. Je ontvangt geen M200.              |
      | Je helpt jouw buur met haar boodschappen. Ze bedankt je met een lekkere lunch! Je ontvangt M20.                                                | Je helpt jouw buur met haar boodschappen. Ze bedankt je met een lekkere lunch! Je ontvangt M20.                                                |
      | Je helpt met het bouwen van een nieuwe speelplaats! Je ontvangt M100.                                                                         | Je helpt met het bouwen van een nieuwe speelplaats! Je ontvangt M100.                                                                         |
      | Je speelt de hele dag met de kinderen in het kinderhospitaal. Je ontvangt M100.                                                               | Je speelt de hele dag met de kinderen in het kinderhospitaal. Je ontvangt M100.                                                               |
      | Je ging naar de car wash inzamelactie van de school — Maar je vergat de ramen te sluiten! je betaald M100.                                     | Je ging naar de car wash inzamelactie van de school — Maar je vergat de ramen te sluiten! je betaald M100.                                     |
      | Net wanneer je denkt dat je geen stap verder kan, bereik je de finish! Ga door naar START. je ontvangt M200.                                   | Net wanneer je denkt dat je geen stap verder kan, bereik je de finish! Ga door naar START. je ontvangt M200.                                   |
      | Je helpt je buren hun tuin opruimen na het onweer. Je ontvangt M200.                                                                          | Je helpt je buren hun tuin opruimen na het onweer. Je ontvangt M200.                                                                          |
      | Je vrienden in het dierenasiel zijn je dankbaar voor je gulheid. je betaald M50.                                                              | Je vrienden in het dierenasiel zijn je dankbaar voor je gulheid. je betaald M50.                                                              |
      | Je had beter deelgenomen aan het renovatie project — je zou waardevolle vaardigheden geleerd hebben! Betaal M40 voor elk huis wat je bezit. M115 voor elk hotel. | Je had beter deelgenomen aan het renovatie project — je zou waardevolle vaardigheden geleerd hebben! Betaal M40 voor elk huis wat je bezit. M115 voor elk hotel. |
      | je organiseert een wafelbak voor de plaatstelijke school. Je ontvangt M25.                                                                     | je organiseert een wafelbak voor de plaatstelijke school. Je ontvangt M25.                                                                     |
