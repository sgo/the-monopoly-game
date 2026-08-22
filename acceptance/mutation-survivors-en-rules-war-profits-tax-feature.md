# Mutation survivors

Format: feature | scenario name | example index | key | original | mutated.

en/rules/war-profits-tax.feature | below 25% ownership, no war profits tax is owed no matter how much rent was collected | 0 | collected | 1000 | 1009
en/rules/war-profits-tax.feature | below 25% ownership, no war profits tax is owed no matter how much rent was collected | 0 | land_value | 5000 | 5005
en/rules/war-profits-tax.feature | below 25% ownership, no war profits tax is owed no matter how much rent was collected | 1 | collected | 50000 | 49998
en/rules/war-profits-tax.feature | below 25% ownership, no war profits tax is owed no matter how much rent was collected | 1 | land_value | 5697 | 5689
en/rules/war-profits-tax.feature | the tax rate climbs in bands as ownership share crosses 25%, applied to the rent collected that year | 0 | land_value | 5698 | 5702
en/rules/war-profits-tax.feature | the tax rate climbs in bands as ownership share crosses 25%, applied to the rent collected that year | 1 | land_value | 9115 | 9113
en/rules/war-profits-tax.feature | the tax rate climbs in bands as ownership share crosses 25%, applied to the rent collected that year | 3 | land_value | 13673 | 13666
en/rules/war-profits-tax.feature | the tax rate climbs in bands as ownership share crosses 25%, applied to the rent collected that year | 6 | land_value | 18232 | 18234
en/rules/war-profits-tax.feature | the tax rate climbs in bands as ownership share crosses 25%, applied to the rent collected that year | 7 | land_value | 22789 | 22786
en/rules/war-profits-tax.feature | the tax rate climbs in bands as ownership share crosses 25%, applied to the rent collected that year | 8 | land_value | 22790 | 22797
en/rules/war-profits-tax.feature | the rent-collected counter resets to zero after each assessment, so a quiet year owes nothing even at a high ownership share | 0 | land_value | 10000 | 10003
en/rules/war-profits-tax.feature | buying land does not by itself trigger a large tax bill, because undeveloped land is worth its vacant rent, not its hotel rent | 0 | collected | 500 | 499
en/rules/war-profits-tax.feature | a legal entity's ownership share never taxes the entity, and its land does not count toward any shareholder's own share | 0 | collected | 500 | 506
en/rules/war-profits-tax.feature | selling back below 25% ownership stops the tax on the following assessment | 0 | collected | 1000 | 992
en/rules/war-profits-tax.feature | tax paid by multiple players accumulates together in the same government account | 0 | land_value | 6000 | 5992
en/rules/war-profits-tax.feature | a tax bill larger than the player's cash forces a mortgage, the same as any other unpayable debt | 0 | collected | 90 | 84
