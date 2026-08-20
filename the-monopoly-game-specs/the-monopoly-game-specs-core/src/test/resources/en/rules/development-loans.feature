# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-20T10:25:50.183233Z","feature_name":"development loans","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/development-loans.feature","background_hash":"bb7c6e218f7f991257a177e7ea9ddc16f59fff86a91120950fd57825efda6ef9","implementation_hash":"unknown","scenarios":[{"index":0,"name":"without the flag, a cash-short player still cannot develop","scenario_hash":"8e269b9f228bf97cce5d730a9069d3f538de0a578ab8580ede3c06829220eb06","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:50.183233Z"},{"index":1,"name":"a shortfall loan, under the 80% loan-to-value cap, is funded by a bondholder and lets development proceed","scenario_hash":"327d82ba4fd7f593c001454610518e99088edf85937e36aa1918570223a8fa9a","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:50.183233Z"},{"index":7,"name":"a player short on cash for the annual payment mortgages a spare property to cover it, rather than defaulting","scenario_hash":"5b63efa45429791079619c76e04f0b1573d525e4883ae99c17417d2904fc6d73","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:50.183233Z"},{"index":11,"name":"the loan mechanism is not tied to who won initiative; a player other than the Background's usual borrower can raise one too","scenario_hash":"4cf1ef5b72d10a640feee9a23deca4419feb91b6f01c886d4fa56b6cad4af994","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:50.183233Z"},{"index":15,"name":"a new loan is funded from recycled capital left over from an earlier foreclosure, without needing a fresh bond","scenario_hash":"90567ec26adeaa63f1d4804d95bbd1560da195658c5f55bf5ec407e2b6e1b66e","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:50.183233Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: development loans
  An opt-in flag, `--optional-development-loans`, lets a player short on cash
  borrow from the bank to finish paying for a development, instead of simply
  being unable to build. The loan is secured only by the street and the
  houses/hotel it finances; nothing else the borrower owns is at risk. The
  bank never lends more than 80% of the construction cost being financed
  (an 80% loan-to-value cap, no exceptions), and by default it only lends the
  shortfall between what the borrower has and what the development costs. A
  second flag, `--optional-development-loans-full-draw`, changes that: the
  borrower always draws the full 80%-of-cost amount, regardless of the
  actual shortfall.

  The bank does not create this money out of nothing: every development loan
  is funded, dollar for dollar, by another player or legal entity buying a
  matching bank bond. Without a buyer able to fund it, no loan is raised and
  the development does not happen, flag or no flag.

  The loan amortizes over 20 years at 5% annual interest, in equal annual
  principal instalments plus interest on the balance still outstanding. If a
  borrower cannot cover an annual payment in cash, they try to raise it the
  same way they already would for any other debt (mortgaging a spare
  property, selling to a peer, and so on) before the loan is treated as
  defaulted. If nothing can be raised, the bank forecloses, but only on the
  street and improvements that secured that specific loan; nothing else the
  borrower owns is touched, and the borrower is not automatically bankrupted
  by a foreclosure. Foreclosure liquidates the whole collateral package, the
  same way any other forced house sale and bank auction already works
  elsewhere in the game: houses/hotel sold back to the bank at half price,
  then the bare land auctioned off to another player. Once the outstanding
  loan balance is covered, any surplus goes back to the borrower, the same
  way a real-world foreclosure sale works.

  The bondholder is not a passive gate; buying the bond is a real
  investment, and buying it from the bank rather than the borrower: the
  bondholder never needs to know or care what specific loan their money
  ends up backing, because the bank manages that risk on their behalf.
  Each annual payment the borrower makes is passed straight through to the
  bondholder at the bond's 3% yield (versus the borrower's 5%), amortizing
  on the same 20-year schedule as the loan; the 2-point spread the bank
  keeps on every payment accumulates in the bank's own account, a reserve
  it can draw on when a foreclosure alone doesn't cover what's owed.

  On default, the bond is not cashed out — it is re-collateralized. The
  bank recovers as much of the outstanding loan's value as the foreclosure
  raises (principal plus the 5% interest for the payment that was missed,
  the same total the borrower would have owed to stay current); if that
  falls short, the bank's own account tops up the difference, so the full
  outstanding value is always recovered as long as that reserve holds.
  That recovered value becomes capital the bank can put straight to work
  funding the next loan that needs one — the bondholder's investment
  continues uninterrupted, now backed by whatever loan absorbs it, rather
  than being paid out in cash. If recycled capital only partly covers a
  new loan, a freshly-issued bond tops up the rest, the same as an
  ordinary loan with no recycled capital behind it at all. Only once the
  full outstanding value of a defaulted loan has been recovered does any
  further surplus reach the original borrower — the same 80%
  loan-to-value cushion that protects the loan protects the bond behind
  it too.

  This feature is what drove the game's money type to gain 2-decimal-place
  (cent) precision game-wide, rather than whole dollars only: 5% and 3%
  rarely divide evenly. A calculation landing on a whole number of cents
  needs no rounding; anything falling on a fractional cent is resolved by
  banker's rounding (round half to even) — an exact half-cent tie rounds to
  whichever neighbouring cent is even, not always up. Money display hides
  trailing zero cents ($140.00 shows as $140), so no scenario elsewhere in
  the game needed to change, since every existing example is a whole-dollar
  amount; only `development-loans-13`/`entity-49` exercise the rounding
  rule directly, using a principal chosen to land on an exact half-cent.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "Greedo" strategy

  # development-loans-1
  Scenario Outline: without the flag, a cash-short player still cannot develop
    Given pawn "dog" has $<cash> to spend
    When we play up to 1 round
    Then the street "Rue Grande Dinant" has 0 house(s) built
    And pawn "dog"'s account balance is $<dog_ending>

    Examples:
      | cash | dog_ending |
      | 30   | 30         |

  # development-loans-2
  Scenario Outline: a shortfall loan, under the 80% loan-to-value cap, is funded by a bondholder and lets development proceed
    Given pawn "dog" has $<cash> to spend
    And pawn "high hat" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue Grande Dinant" has 1 house(s) built
    And pawn "dog" raises a development loan of $<loan> secured by "Rue Grande Dinant"

    Examples:
      | cash | loan |
      | 30   | 20   |
      | 15   | 35   |

  # development-loans-3
  Scenario Outline: the 80% loan-to-value cap blocks development once the shortfall exceeds it, even with a bondholder available
    Given pawn "dog" has $<cash> to spend
    And pawn "high hat" has $<high_hat_cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue Grande Dinant" has 0 house(s) built
    And pawn "dog"'s account balance is $<dog_ending>

    Examples:
      | cash | high_hat_cash | dog_ending |
      | 5    | 500           | 5          |

  # development-loans-4
  Scenario Outline: the full-draw flag always borrows the full 80% loan-to-value cap, regardless of the actual shortfall
    Given pawn "dog" has $<cash> to spend
    And pawn "high hat" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    And development loans draw the full amount for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue Grande Dinant" has 1 house(s) built
    And pawn "dog" raises a development loan of $<loan> secured by "Rue Grande Dinant"

    Examples:
      | cash | loan |
      | 30   | 40   |
      | 15   | 40   |

  # development-loans-5
  Scenario Outline: without a bondholder able to fund it, no loan is raised and development does not happen even with the flag enabled
    Given pawn "dog" has $<cash> to spend
    And pawn "high hat" has $<high_hat_cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue Grande Dinant" has 0 house(s) built
    And pawn "dog"'s account balance is $<dog_ending>

    Examples:
      | cash | high_hat_cash | dog_ending |
      | 30   | 5             | 30         |

  # development-loans-6
  Scenario Outline: the first annual payment splits into interest and principal, paying down the outstanding balance
    Given pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "dog" has $<cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then pawn "dog" pays the bank $<interest> in interest on the development loan
    And pawn "dog" pays the bank $<principal_payment> in principal on the development loan
    And pawn "dog" owes the bank $<remaining> on the development loan

    Examples:
      | principal | cash | interest | principal_payment | remaining |
      | 20        | 100  | 1        | 1                  | 19        |

  # development-loans-7
  Scenario Outline: the loan is fully repaid once its final annual payment is made
    Given pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "dog" has $<cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then pawn "dog"'s development loan on "Rue Grande Dinant" has been fully repaid
    And pawn "dog" owns no development loan

    Examples:
      | principal | cash |
      | 1         | 100  |

  # development-loans-8
  Scenario Outline: a player short on cash for the annual payment mortgages a spare property to cover it, rather than defaulting
    Given pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then the land "Lippenslaan Knokke" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog" owes the bank $<remaining> on the development loan

    Examples:
      | principal | cash | remaining |
      | 20        | 0    | 19        |

  # development-loans-9
  Scenario Outline: with no cash and nothing else to sell or mortgage, the loan defaults; foreclosure liquidates only the collateralized street and returns any surplus to the borrower
    Given pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And the street "Rue Grande Dinant" has 1 house(s) built
    And pawn "dog" has $0 to spend
    And pawn "high hat" will bid $<bid> for "Rue Grande Dinant" at auction
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then pawn "dog" does not own "Rue Grande Dinant"
    And pawn "high hat" owns "Rue Grande Dinant"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<dog_ending>

    Examples:
      | principal | bid | dog_ending |
      | 20        | 30  | 35         |

  # development-loans-10
  Scenario Outline: the bondholder receives their annual payout, split into yield and principal, as the borrower repays
    Given pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "high hat" holds the development loan bond secured by "Rue Grande Dinant"
    And pawn "dog" has $<cash> to spend
    And pawn "high hat" has $<bond_cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then pawn "high hat" receives $<yield> interest and $<principal_payment> principal on the development loan bond secured by "Rue Grande Dinant"

    Examples:
      | principal | cash | bond_cash | yield | principal_payment |
      | 100       | 200  | 500       | 3     | 5                  |

  # development-loans-11
  Scenario Outline: on default, the bond is not cashed out but re-collateralized; the bank recovers the full outstanding value before any surplus reaches the borrower
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "high hat" holds the development loan bond secured by "Rue Grande Dinant"
    And pawn "high hat" has $<bond_cash> to spend
    And the bank's account holds $0
    And the street "Rue Grande Dinant" has 1 house(s) built
    And pawn "dog" has $0 to spend
    And pawn "iron box" will bid $<bid> for "Rue Grande Dinant" at auction
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then pawn "high hat"'s account balance is $<bond_cash>
    And the bank's account holds $<bank_account>
    And pawn "dog"'s account balance is $<dog_ending>

    Examples:
      | principal | bond_cash | bid | bank_account | dog_ending |
      | 20        | 500       | 30  | 21           | 34         |

  # development-loans-12
  Scenario Outline: the loan mechanism is not tied to who won initiative; a player other than the Background's usual borrower can raise one too
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "high hat" owns "Nieuwstraat Brussel"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<cash> to spend
    And pawn "dog" has $0 to spend
    And pawn "iron box" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Meir Antwerpen" has 1 house(s) built
    And pawn "high hat" raises a development loan of $<loan> secured by "Meir Antwerpen"

    Examples:
      | cash | loan |
      | 50   | 150  |

  # development-loans-13
  Scenario Outline: a later annual payment shows interest and principal genuinely diverge, not just totalling correctly, and resolves two exact half-cent ties via banker's rounding
    Given pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant", 1 year into its 20-year term
    And pawn "dog" has $<cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then pawn "dog" pays the bank $<interest> in interest on the development loan
    And pawn "dog" pays the bank $<principal_payment> in principal on the development loan
    And pawn "dog" owes the bank $<remaining> on the development loan

    Examples:
      | principal | cash | interest | principal_payment | remaining |
      | 400.10    | 100  | 19       | 20                 | 360.10    |

  # development-loans-14
  Scenario Outline: the bank's own account accumulates the 2-point spread on every scheduled payment
    Given pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "high hat" holds the development loan bond secured by "Rue Grande Dinant"
    And pawn "dog" has $<cash> to spend
    And pawn "high hat" has $<bond_cash> to spend
    And the bank's account holds $0
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then the bank's account holds $<bank_spread>

    Examples:
      | principal | cash | bond_cash | bank_spread |
      | 20        | 100  | 500       | 0.40        |

  # development-loans-15
  Scenario Outline: a shortfall between foreclosure proceeds and the outstanding loan value is topped up from the bank's own account
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "high hat" holds the development loan bond secured by "Rue Grande Dinant"
    And pawn "high hat" has $<bond_cash> to spend
    And the bank's account holds $<bank_starting>
    And the street "Rue Grande Dinant" has 1 house(s) built
    And pawn "dog" has $0 to spend
    And pawn "iron box" will bid $<bid> for "Rue Grande Dinant" at auction
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then pawn "high hat"'s account balance is $<bond_cash>
    And the bank's account holds $<bank_remaining>
    And pawn "dog"'s account balance is $0

    Examples:
      | principal | bond_cash | bid | bank_starting | bank_remaining |
      | 40        | 500       | 5   | 50            | 38             |

  # development-loans-16
  Scenario Outline: a new loan is funded from recycled capital left over from an earlier foreclosure, without needing a fresh bond
    Given pawn "dog" has $<cash> to spend
    And the bank holds $<recycled> in recycled development-loan capital, no longer securing any loan
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue Grande Dinant" has 1 house(s) built
    And pawn "dog" raises a development loan of $<loan> secured by "Rue Grande Dinant"
    And the bank holds $<recycled_remaining> in recycled development-loan capital

    Examples:
      | cash | recycled | loan | recycled_remaining |
      | 30   | 50       | 20   | 30                  |

  # development-loans-17
  Scenario Outline: when recycled capital only partly covers a new loan, a freshly-issued bond tops up the rest
    Given pawn "dog" has $<cash> to spend
    And pawn "high hat" has $<fresh_bond_cash> to spend
    And the bank holds $<recycled> in recycled development-loan capital, no longer securing any loan
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue Grande Dinant" has 1 house(s) built
    And pawn "dog" raises a development loan of $<loan> secured by "Rue Grande Dinant"
    And the bank holds $0 in recycled development-loan capital

    Examples:
      | cash | fresh_bond_cash | recycled | loan |
      | 15   | 500              | 10       | 35   |
