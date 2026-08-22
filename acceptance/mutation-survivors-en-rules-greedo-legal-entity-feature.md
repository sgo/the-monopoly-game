# Mutation survivors

Format: feature | scenario name | example index | key | original | mutated.

en/rules/greedo-legal-entity.feature | the entity is not auto-formed while only stalemate trading (not legal-entity trading) is enabled, at the round boundary | 0 | enabled_flag | stalemate | stAlemate
en/rules/greedo-legal-entity.feature | the entity forms at market deadlock when a full round passes with no ownership-consolidating action and an eligible three-owner split can collectively fund the next improvement after base reserves | 0 | entity_name | Pink Realty | Pink RealTy
en/rules/greedo-legal-entity.feature | the pre-stalemate formation trigger is independent of the final cash-threshold stalemate gate | 0 | formed_outcome | auto-formed | autO-formed
en/rules/greedo-legal-entity.feature | the entity still forms at market deadlock when an unrelated player takes a consolidating action elsewhere on the board | 0 | entity_name | Pink Realty | PinkxRealty
en/rules/greedo-legal-entity.feature | the entity repays a shareholder loan with five percent interest on top before paying any dividend | 0 | repayment | 105 | 100
en/rules/greedo-legal-entity.feature | the entity builds houses from rent at the end of the turn before repaying its loan | 0 | principal | 200 | 191
en/rules/greedo-legal-entity.feature | no dividend is paid while any shareholder loan to the entity is still outstanding, even when the entity is fully developed | 0 | principal | 100 | 91
en/rules/greedo-legal-entity.feature | no dividend is paid while any shareholder loan to the entity is still outstanding, even when the entity is fully developed | 0 | surplus | 150 | 159
en/rules/greedo-legal-entity.feature | a dividend is paid only after the entire loan plus interest has been repaid and the entity is fully developed | 0 | principal | 100 | 108
en/rules/greedo-legal-entity.feature | a dividend is paid only after the entire loan plus interest has been repaid and the entity is fully developed | 1 | principal | 100 | 93
en/rules/greedo-legal-entity.feature | the entity cannot build beyond a shareholder's personal affordability ceiling | 0 | ceiling_share | 40 | 34
en/rules/greedo-legal-entity.feature | the entity cannot build beyond a shareholder's personal affordability ceiling | 0 | loan | 100 | 93
en/rules/greedo-legal-entity.feature | rent collected from a tenant is deposited into the entity's bank account | 0 | renter_position | 3 | 11
en/rules/greedo-legal-entity.feature | a shareholder pays rent when landing on their own legal entity's street | 0 | renter_position | 3 | -1
en/rules/greedo-legal-entity.feature | a shareholder pays rent when landing on their own legal entity's street | 0 | treasury | 5000 | 5003
en/rules/greedo-legal-entity.feature | a tenant who cannot pay a legal entity's rent from cash becomes a distressed seller, exactly as for a player-owned street | 0 | peer_balance | 20 | 24
en/rules/greedo-legal-entity.feature | a raised loan is deposited into the entity's bank account | 0 | loan | 150 | 155
en/rules/greedo-legal-entity.feature | the entity uses its rent before raising a loan to build when its shareholders decline the build-loan commitment | 0 | balance | 150 | 143
en/rules/greedo-legal-entity.feature | the entity uses its rent before raising a loan to build when its shareholders decline the build-loan commitment | 0 | houses_at_least | 1 | -8
en/rules/greedo-legal-entity.feature | the entity uses its rent before raising a loan to build when its shareholders decline the build-loan commitment | 0 | max_loan | 0 | 8
en/rules/greedo-legal-entity.feature | the entity uses its rent before raising a loan to build when its shareholders decline the build-loan commitment | 0 | reserve | 150 | 146
en/rules/greedo-legal-entity.feature | an entity whose streets are already fully developed is financially inactive at the round boundary | 0 | max_loan | 0 | 8
en/rules/greedo-legal-entity.feature | an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund | 0 | loan | 300 | 295
en/rules/greedo-legal-entity.feature | an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund | 0 | share | 100 | 103
en/rules/greedo-legal-entity.feature | an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund | 1 | loan | 600 | 593
en/rules/greedo-legal-entity.feature | an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund | 1 | share | 200 | 209
en/rules/greedo-legal-entity.feature | an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund | 2 | loan | 900 | 897
en/rules/greedo-legal-entity.feature | an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund | 2 | share | 300 | 303
en/rules/greedo-legal-entity.feature | an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund | 3 | loan | 1200 | 1205
en/rules/greedo-legal-entity.feature | an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund | 3 | share | 400 | 401
en/rules/greedo-legal-entity.feature | an entity whose shareholders commit to a group build loan builds a hotel on every street when it can fund the hotels | 0 | loan | 1500 | 1491
en/rules/greedo-legal-entity.feature | no dividend is paid unless the last-capitalised shareholder grows a year older, even when the entity is fully developed | 0 | principal | 0 | 3
en/rules/greedo-legal-entity.feature | no dividend is paid unless the last-capitalised shareholder grows a year older, even when the entity is fully developed | 0 | surplus | 150 | 154
en/rules/greedo-legal-entity.feature | the entity pays an equal dividend when the last-capitalised shareholder grows a year older and the entity is fully developed | 0 | principal | 0 | -9
en/rules/greedo-legal-entity.feature | a dividend is still paid when the last-capitalised shareholder has gone bankrupt and can never grow older to unlock it | 0 | principal | 0 | 6
en/rules/greedo-legal-entity.feature | the entity forms from exactly the three co-owners of a colour group even when the game has more than three players | 0 | player_count | 4 | 10
en/rules/greedo-legal-entity.feature | the entity forms from exactly the three co-owners of a colour group even when the game has more than three players | 1 | player_count | 8 | 6
en/rules/greedo-legal-entity.feature | the entity does not form when the colour group is split across only two owners in a larger game | 0 | player_count | 4 | 11
en/rules/greedo-legal-entity.feature | the entity does not form when the colour group is split across only two owners in a larger game | 1 | player_count | 8 | 7
en/rules/greedo-legal-entity.feature | the entity raises a build loan when every Greedo shareholder can afford its share, is solvent, and its reserve allows it | 0 | balance | 200 | 201
en/rules/greedo-legal-entity.feature | the entity raises a build loan when every Greedo shareholder can afford its share, is solvent, and its reserve allows it | 0 | loan | 300 | 299
en/rules/greedo-legal-entity.feature | the entity raises a build loan when every Greedo shareholder can afford its share, is solvent, and its reserve allows it | 0 | reserve | 0 | 6
en/rules/greedo-legal-entity.feature | the entity does not raise a build loan when a Greedo shareholder cannot afford its share | 0 | ceiling_share | 40 | 42
en/rules/greedo-legal-entity.feature | the entity does not raise a build loan when a Greedo shareholder's reserve would be breached | 0 | balance | 150 | 151
en/rules/greedo-legal-entity.feature | the entity does not raise a build loan when a Greedo shareholder's reserve would be breached | 0 | reserve | 150 | 153
en/rules/greedo-legal-entity.feature | the entity does not raise a build loan when a Greedo shareholder's reserve would be breached | 0 | total_houses | 0 | 2
en/rules/greedo-legal-entity.feature | the entity builds a hotel when every Greedo shareholder commits to a full build loan | 0 | loan | 1500 | 1493
en/rules/greedo-legal-entity.feature | the entity builds a hotel when every Greedo shareholder commits to a full build loan | 0 | share | 500 | 495
en/rules/greedo-legal-entity.feature | the entity automatically solicits its Greedo shareholders to fund the build shortfall after applying its rent, builds, and pays no dividend even though its bank is not empty | 0 | balance | 200 | 207
en/rules/greedo-legal-entity.feature | the entity automatically solicits its Greedo shareholders to fund the build shortfall after applying its rent, builds, and pays no dividend even though its bank is not empty | 0 | loan | 200 | 203
en/rules/greedo-legal-entity.feature | the entity automatically solicits its Greedo shareholders to fund the build shortfall after applying its rent, builds, and pays no dividend even though its bank is not empty | 0 | rent | 100 | 108
en/rules/greedo-legal-entity.feature | the entity automatically solicits its Greedo shareholders to fund the build shortfall after applying its rent, builds, and pays no dividend even though its bank is not empty | 0 | reserve | 0 | 8
en/rules/greedo-legal-entity.feature | an under-developed entity whose shareholders decline to finance a build loan spends its treasury on building rather than paying a dividend, even when the treasury exceeds the dividend threshold and the last-capitalised shareholder has aged | 0 | ceiling_share | 0 | 7
en/rules/greedo-legal-entity.feature | an under-developed entity whose shareholders decline to finance a build loan spends its treasury on building rather than paying a dividend, even when the treasury exceeds the dividend threshold and the last-capitalised shareholder has aged | 1 | ceiling_share | 0 | -9
en/rules/greedo-legal-entity.feature | no dividend is paid while a bank development loan is still outstanding, even when the entity is fully developed | 0 | principal | 100 | 103
en/rules/greedo-legal-entity.feature | no dividend is paid while a bank development loan is still outstanding, even when the entity is fully developed | 0 | surplus | 150 | 146
en/rules/greedo-legal-entity.feature | an entity short on cash for the annual payment mortgages another street in its own group to cover it, rather than defaulting | 0 | bank_funds | 0 | -4
en/rules/greedo-legal-entity.feature | the full-draw flag always borrows the full 80% loan-to-value cap, regardless of the actual shortfall | 0 | bank_funds | 60 | 65
en/rules/greedo-legal-entity.feature | the full-draw flag always borrows the full 80% loan-to-value cap, regardless of the actual shortfall | 1 | bank_funds | 30 | 31
en/rules/greedo-legal-entity.feature | the first annual payment splits into interest and principal, paying down the outstanding balance | 0 | bank_funds | 100 | 99
en/rules/greedo-legal-entity.feature | the loan is fully repaid once its final annual payment is made | 0 | bank_funds | 100 | 109
en/rules/greedo-legal-entity.feature | the bondholder receives their annual payout, split into yield and principal, as the entity repays | 0 | bank_funds | 200 | 205
en/rules/greedo-legal-entity.feature | on default, the entity's bond is not cashed out but re-collateralized; the bank recovers the full outstanding value before any surplus reaches the entity | 0 | bond_cash | 500 | 501
en/rules/greedo-legal-entity.feature | a later annual payment shows the entity's interest and principal genuinely diverge, not just totalling correctly, and resolves two exact half-cent ties via banker's rounding | 0 | bank_funds | 100 | 97
en/rules/greedo-legal-entity.feature | the bank's own account accumulates the 2-point spread on the entity's scheduled payment | 0 | bank_funds | 200 | 197
