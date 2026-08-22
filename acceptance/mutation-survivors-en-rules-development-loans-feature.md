# Mutation survivors

Format: feature | scenario name | example index | key | original | mutated.

en/rules/development-loans.feature | the 80% loan-to-value cap blocks development once the shortfall exceeds it, even with a bondholder available | 0 | high_hat_cash | 500 | 494
en/rules/development-loans.feature | the full-draw flag always borrows the full 80% loan-to-value cap, regardless of the actual shortfall | 0 | cash | 30 | 26
en/rules/development-loans.feature | the full-draw flag always borrows the full 80% loan-to-value cap, regardless of the actual shortfall | 1 | cash | 15 | 11
en/rules/development-loans.feature | without a bondholder able to fund it, no loan is raised and development does not happen even with the flag enabled | 0 | high_hat_cash | 5 | -3
en/rules/development-loans.feature | the first annual payment splits into interest and principal, paying down the outstanding balance | 0 | cash | 100 | 91
en/rules/development-loans.feature | the loan is fully repaid once its final annual payment is made | 0 | cash | 100 | 103
en/rules/development-loans.feature | with no cash and nothing else to sell or mortgage, the loan defaults; foreclosure liquidates only the collateralized street and returns any surplus to the borrower | 0 | bid | 30 | 38
en/rules/development-loans.feature | the bondholder receives their annual payout, split into yield and principal, as the borrower repays | 0 | bond_cash | 500 | 494
en/rules/development-loans.feature | the bondholder receives their annual payout, split into yield and principal, as the borrower repays | 0 | cash | 200 | 204
en/rules/development-loans.feature | a later annual payment shows interest and principal genuinely diverge, not just totalling correctly, and resolves two exact half-cent ties via banker's rounding | 0 | cash | 100 | 99
en/rules/development-loans.feature | the bank's own account accumulates the 2-point spread on every scheduled payment | 0 | bond_cash | 500 | 502
en/rules/development-loans.feature | the bank's own account accumulates the 2-point spread on every scheduled payment | 0 | cash | 100 | 101
en/rules/development-loans.feature | when recycled capital only partly covers a new loan, a freshly-issued bond tops up the rest | 0 | fresh_bond_cash | 500 | 505
en/rules/development-loans.feature | when recycled capital only partly covers a new loan, a freshly-issued bond tops up the rest | 0 | recycled | 10 | 5
