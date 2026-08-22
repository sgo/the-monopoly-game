# Mutation survivors

Format: feature | scenario name | example index | key | original | mutated.

en/rules/greedo-share-sale.feature | a shareholder in distress offers their legal-entity share to a fellow shareholder instead of going bankrupt | 0 | dog_balance | 40 | 33
en/rules/greedo-share-sale.feature | a shareholder in distress offers their legal-entity share to a fellow shareholder instead of going bankrupt | 0 | high_hat_balance | 1000 | 1002
en/rules/greedo-share-sale.feature | a shareholder in distress offers their legal-entity share to a fellow shareholder instead of going bankrupt | 0 | iron_box_balance | 200 | 194
en/rules/greedo-share-sale.feature | a shareholder in distress offers their legal-entity share to a fellow shareholder instead of going bankrupt | 0 | ship_balance | 200 | 197
en/rules/greedo-share-sale.feature | a shareholder does not go bankrupt because the share's value covers the tax debt | 0 | dog_balance | 40 | 41
en/rules/greedo-share-sale.feature | a shareholder does not go bankrupt because the share's value covers the tax debt | 0 | high_hat_balance | 1000 | 999
en/rules/greedo-share-sale.feature | a shareholder sells a cheaper personal asset before offering their legal-entity share | 0 | dog_balance | 10 | 18
en/rules/greedo-share-sale.feature | a shareholder sells a cheaper personal asset before offering their legal-entity share | 0 | high_hat_balance | 1000 | 1006
en/rules/greedo-share-sale.feature | a legal-entity share does not change hands when no fellow shareholder will bid | 0 | dog_balance | 40 | 41
en/rules/greedo-share-sale.feature | a fellow shareholder bids up to a third of their bank balance, and the highest bid wins | 0 | dog_balance | 40 | 38
en/rules/greedo-share-sale.feature | a fellow shareholder bids up to a third of their bank balance, and the highest bid wins | 0 | high_hat_balance | 1200 | 1207
en/rules/greedo-share-sale.feature | a fellow shareholder bids up to a third of their bank balance, and the highest bid wins | 1 | dog_balance | 40 | 48
en/rules/greedo-share-sale.feature | a fellow shareholder bids up to a third of their bank balance, and the highest bid wins | 1 | high_hat_balance | 3000 | 2997
en/rules/greedo-share-sale.feature | a fellow shareholder bids up to a third of their bank balance, and the highest bid wins | 2 | dog_balance | 40 | 31
en/rules/greedo-share-sale.feature | a fellow shareholder bids up to a third of their bank balance, and the highest bid wins | 2 | high_hat_balance | 1600 | 1607
en/rules/greedo-share-sale.feature | the final shareholder sells newly-acquired entity assets when liquidation cash is insufficient | 0 | dog_balance | 40 | 34
en/rules/greedo-share-sale.feature | the final shareholder sells newly-acquired entity assets when liquidation cash is insufficient | 0 | entity_balance | 0 | 3
en/rules/greedo-share-sale.feature | an entity whose every share has been bought out by one shareholder is liquidated for that shareholder's debt, exactly as a naturally-reduced one is | 0 | dog_balance | 10000 | 9996
en/rules/greedo-share-sale.feature | an entity whose every share has been bought out by one shareholder is liquidated for that shareholder's debt, exactly as a naturally-reduced one is | 0 | dog_final_balance | 50 | 55
en/rules/greedo-share-sale.feature | an entity whose every share has been bought out by one shareholder is liquidated for that shareholder's debt, exactly as a naturally-reduced one is | 0 | high_hat_balance | 150 | 152
