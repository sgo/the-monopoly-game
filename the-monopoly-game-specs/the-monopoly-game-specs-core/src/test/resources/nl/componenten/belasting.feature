# language: nl

Functionaliteit: belasting

  # belasting-1
  Abstract Scenario: belastingsruimte
    Gegeven de belastingsruimte "<naam>"
    Dan is de belasting €<bedrag>

    Voorbeelden:
      | naam                                        | bedrag |
      | Inkomsten Belasting / Impôts sur le revenu   | 200    |
      | Extra Belasting / Taxe de Luxe               | 100    |
