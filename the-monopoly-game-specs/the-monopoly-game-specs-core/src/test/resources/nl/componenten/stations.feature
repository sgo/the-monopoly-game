# language: nl

Functionaliteit: stations

  # stations-1
  Abstract Scenario: station
    Gegeven het station "<naam>"
    Dan is de waarde van het station €200
    En huur voor het bezitten van 1 station is €25
    En huur voor het bezitten van 2 stations is €50
    En huur voor het bezitten van 3 stations is €100
    En huur voor het bezitten van 4 stations is €200
    En hypotheekwaarde van het land is €100

    Voorbeelden:
      | naam                                  |
      | Noord Station / Gare du Nord          |
      | Centraal Station / Gare Centrale      |
      | Buurtspoorwegen / Tramways Vicinaux   |
      | Zuid Station / Gare du Midi           |
