# language: nl

Functionaliteit: dobbelsteen

  Scenario: elke zijde heeft een gelijke kans om gerold te worden
    Gegeven een dobbelsteen met 6 zijdes
    Als ik de dobbelsteen 6000 keer rol
    Dan werd elke zijde een gelijk aantal keren gerold
      | zijde | aantal keer gezien |
      | 1     | 1000               |
      | 2     | 1000               |
      | 3     | 1000               |
      | 4     | 1000               |
      | 5     | 1000               |
      | 6     | 1000               |
