# mutation-stamp: sha256=7597d49d13d7a5b4346b17778b1de5ca3158c077c11fbbd7bdf7921b5f60a012
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-02T13:22:01.180726Z","feature_name":"chance and community chest cards","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/cards.feature","background_hash":"3eba7406624ca69ea442618544f7523c7122b7ad66c8b2f7f4495508c24d7aca","implementation_hash":"unknown","scenarios":[{"index":8,"name":"a card that advances to the nearest station lets the pawn buy it if unsold","scenario_hash":"9f776f3b486519ebaedbe9742c50c5debb0282b41b079bec11702cc8a664e6cf","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:39:59.511934Z"},{"index":0,"name":"a card that advances the pawn pays the START salary if it passes START","scenario_hash":"fd57d76311c6df4b3a17d5cfa6ef5cf88dfd7ee261a58df75b590f37b264f6a6","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":1,"name":"a card that instructs a flat payment to the bank","scenario_hash":"02855b52c36d6d73c627a14e3cdbbb9e746b24ffa0c62ca641512c117dfbd7cc","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":2,"name":"a card that instructs a flat payment from the bank","scenario_hash":"3442b691108a7df882f14478364b3f8a1a179475edcd2dd762606696a6f1ea53","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":3,"name":"a card that instructs paying every other player","scenario_hash":"6cb2388d36b4305808baa87be9ad12bde8439cb37ab3636a9099fafa3cc0cff8","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":4,"name":"a card that instructs collecting from every other player","scenario_hash":"ed8a3d2e20ad8805f660c0b1669a66ed496e07c708d530381e85d24ba33c9e7e","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":5,"name":"a card that sends the pawn directly to jail pays no salary","scenario_hash":"3edfeb703393b31e02b7510db176378739b5f4563161ca9b9512fc333c221793","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":6,"name":"drawing a \"Get Out of Jail Free\" card retains it instead of returning it to the deck","scenario_hash":"bffafe52c62daea525cd6f3e6860f43c4372254a7e05d2e0da1cb5cb8cc573d0","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":7,"name":"a retained \"Get Out of Jail Free\" card can be sold to another player","scenario_hash":"cc3487b82ece7180b06e80b5994b0ec5ce62defd4ecb451114e8ea9ab24616e0","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":9,"name":"a card that advances to the nearest station pays the owner double rent","scenario_hash":"412fa8babaef65e7ffd274951270eba5a1c5650ae9fced91aca06016e9b76140","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":10,"name":"a card that advances to the nearest utility pays the owner ten times a freshly rolled dice total","scenario_hash":"8a01d9fa19d85b3f35f33c0fae4082950d5f93798cfca0f60552dcb2a055bf9c","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":11,"name":"a card that renovates all owned properties pays per house and hotel across the whole board","scenario_hash":"1f8b6102d09a97377ed3537a2b8dc2d483e9cc163260addfe26cf5c5e61ad733","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":12,"name":"every chance card resolves without error","scenario_hash":"da7b05ba30f19d35f49df7081b9cb5582c73bbcc2eea961c2b13ce112f620806","mutation_count":30,"result":{"Total":30,"Killed":30,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"},{"index":13,"name":"every community chest card resolves without error","scenario_hash":"716ece423483c46f29ae8b21f815cc9069003d2f5590a06067ef78ed8f6771a8","mutation_count":32,"result":{"Total":32,"Killed":32,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:19.866007Z"}]}
# acceptance-mutation-manifest-end

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

  # cards-15
  Scenario Outline: landing on Chance without a scripted card still draws a real card from the official deck
    When pawn "<pawn>" lands on "Kans / Chance"
    Then the game journal records that pawn "<pawn>" draws a chance card

    Examples:
      | pawn |
      | dog  |

  # cards-16
  Scenario Outline: landing on Community Chest without a scripted card still draws a real card from the official deck
    When pawn "<pawn>" lands on "Algemeen Fonds / Caisse de Communauté"
    Then the game journal records that pawn "<pawn>" draws a community chest card

    Examples:
      | pawn |
      | dog  |
