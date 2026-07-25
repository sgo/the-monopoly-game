# language: en

Feature: tax

  # tax-1
  Scenario Outline: tax space
    Given the tax space "<name>"
    Then the tax is $<amount>

    Examples:
      | name                                        | amount |
      | Inkomsten Belasting / Impôts sur le revenu   | 200    |
      | Extra Belasting / Taxe de Luxe               | 100    |
