# language: nl

Functionaliteit: : officiële regels

  Scenario: het spel wordt gespeeld met 2 dobbelstenen met 6 zijdes
    Gegeven de officiële regels
    Dan spelen we met de volgende dobbelstenen
      | type     |
      | 6 zijdig |
      | 6 zijdig |

  Scenario: het spel wordt gespeeld met een minimum van 2 tot een maximum van 8 spelers
    Gegeven de officiële regels
    Dan spelen we met 2 tot 8 spelers

  Scenario: layout van het spelbord
    Gegeven de officiële regels
    Dan is de layout van het spelbord
      | straat namen         | kleur groep |
      | Start                |             |
      | Rue Grande Dinant    | bruin       |
      | Diestsestraat Leuven | bruin       |

  Scenario: de spelers in het spel kunnen geïdentificeerd worden aan de hand van hun pion
    Gegeven de officiële regels
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

  Scenario: elke speler begint het spel met een start kapitaal
    Gegeven de officiële regels
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

  Scenario: spelers ontvangen een loon wanneer ze passeren langs start
    Gegeven de officiële regels
    En een speler
    * met €1500 in zijn bank rekening
    Wanneer de speler langs de straat "Start" passeert
    Dan is de balans van de speler zijn bank rekening €1700

  Scenario: spelers ontvangen een dubbel loon wanneer ze landen op start
    Gegeven de officiële regels
    En met optionele dubbel loon bij het landen op Start regel
    En een speler
    * met €1500 in zijn bank rekening
    Wanneer de speler op de straat "Start" land
    Dan is de balans van de speler zijn bank rekening €1900
