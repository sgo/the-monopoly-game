# language: nl

Functionaliteit: dobbelsteen

  Scenario: elke zijde heeft een gelijke kans om gerold te worden
    Gegeven een dobbelsteen met 6 zijdes
    Als ik de dobbelsteen 600000 keer rol
    Dan werd elke zijde een gelijk aantal keren gerold
      | symbool | aantal keer gezien | foutmarge in % |
      | 1       | 100000             | 1              |
      | 2       | 100000             | 1              |
      | 3       | 100000             | 1              |
      | 4       | 100000             | 1              |
      | 5       | 100000             | 1              |
      | 6       | 100000             | 1              |
