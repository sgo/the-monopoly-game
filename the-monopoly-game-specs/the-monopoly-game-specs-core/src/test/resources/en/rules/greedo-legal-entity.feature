# mutation-stamp: sha256=b1e18a8ddccdd6d2423e50f3bf914ea25a80b7194326fe3783276a81e7d74f90
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-24T20:30:13.866345Z","feature_name":"Greedo legal entity for a three-way colour-group split","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-legal-entity.feature","background_hash":"fd82b050e2caf6974e2f44e0c1ce996c7bd99ce3d3e5d035db5a75223654814e","implementation_hash":"unknown","scenarios":[{"index":5,"name":"the entity forms at market deadlock when a full round passes with no ownership-consolidating action and an eligible three-owner split can collectively fund the next improvement after base reserves","scenario_hash":"cb45e842a965618311f29255015edab565feecffc241d6b109837bf7c2bad9bb","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":8,"name":"the pre-stalemate formation trigger is independent of the final cash-threshold stalemate gate","scenario_hash":"02ffcd3181fc06cf387c5199d44d7afed866f50c2fd4cde8e1f0afc81cae6cb5","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":10,"name":"the entity still forms at market deadlock when an unrelated player takes a consolidating action elsewhere on the board","scenario_hash":"0ba6f934fab057e77b7926ff880e902ffc7fcb3e86174c0c8c9d82537e5ed60b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":11,"name":"the entity repays a shareholder loan with five percent interest on top before paying any dividend","scenario_hash":"1248d51274232b0ca23e30d4fb9b8c04b74fce14661f09a29bb06735505a0789","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":12,"name":"the entity builds houses from rent at the end of the turn before repaying its loan","scenario_hash":"cdb489be813171e815f6973a98ce19b9d20f5deb536edb97849048c0021a555d","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":14,"name":"a dividend is paid only after the entire loan plus interest has been repaid and the entity is fully developed","scenario_hash":"d5bfdd307cb8935bf94ee99002f9eaebfc8793a74450f95c7af31cd76510c8c8","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":15,"name":"the entity cannot build beyond a shareholder's personal affordability ceiling","scenario_hash":"6f6bcf7b36aadf74a6becfc846ff52197000c5ef0f5c09b8d157fe6f88d8c2b6","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":16,"name":"rent collected from a tenant is deposited into the entity's bank account","scenario_hash":"f18ea96dd01ff7cb31ea6175bd43f776622097da3982bfc1bb25c1fcb9312a53","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":17,"name":"a shareholder pays rent when landing on their own legal entity's street","scenario_hash":"70a93151262c7f7df463aa656bcf7928fbb2bbfcaf17f7bcb6668ee175a8dfab","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":18,"name":"a tenant who cannot pay a legal entity's rent from cash becomes a distressed seller, exactly as for a player-owned street","scenario_hash":"197448f3fb45c9a8779371a0ca56a1be3556dab8d1aeccb76deddd37761740f9","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":20,"name":"a raised loan is deposited into the entity's bank account","scenario_hash":"dbcf2d53611a03e2f83ba3f3c2075db083c526579c4f64a5a4a77b442c24cc44","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":21,"name":"the entity uses its rent before raising a loan to build when its shareholders decline the build-loan commitment","scenario_hash":"35e0ca727309e7cc68e8d28ddb62b591c3c82b60486890d35b99eb3b5b048d86","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":23,"name":"an entity whose streets are already fully developed is financially inactive at the round boundary","scenario_hash":"945de4c701666b1e82f4f6d2db3b72e40e69691050319b47b85625ad1d6bb0c4","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":24,"name":"an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund","scenario_hash":"d2ada5a489eeaa9a1f8feb55661da220665e03a5d146214217735dee3eebf982","mutation_count":20,"result":{"Total":20,"Killed":20,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":25,"name":"an entity whose shareholders commit to a group build loan builds a hotel on every street when it can fund the hotels","scenario_hash":"8bfe698a0cbde5332916d8b35161c9f206659c62b0b8ffeb663ceb5f4b8274ce","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":27,"name":"the entity pays an equal dividend when the last-capitalised shareholder grows a year older and the entity is fully developed","scenario_hash":"d03f0e947766c21c9f3697507707c7ac26d6ba1315dbe629614714ebd3efff52","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":28,"name":"a dividend is still paid when the last-capitalised shareholder has gone bankrupt and can never grow older to unlock it","scenario_hash":"d82bfbf0e648392b4e46b2384c3378b70025e814f27342ee80459379a042f2bb","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":29,"name":"the entity forms from exactly the three co-owners of a colour group even when the game has more than three players","scenario_hash":"f28dc07fe2fde19242e5ecfd5edda006e9856cb8f206f163669115ac33666d33","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":30,"name":"the entity does not form when the colour group is split across only two owners in a larger game","scenario_hash":"832c1a2d52c5699f617a6ed64567857817e09438658f6cd9841b087ded6326ba","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":31,"name":"the entity raises a build loan when every Greedo shareholder can afford its share, is solvent, and its reserve allows it","scenario_hash":"7f3321ae8a47da19af557ffdb31efe93984db1d5573ef7a1642e01a5b5e74c04","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":32,"name":"the entity does not raise a build loan when a Greedo shareholder cannot afford its share","scenario_hash":"fd0d91aa67a444d731412b074fe62d68108255c68a8042c42f79c7837de14e0b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":34,"name":"the entity builds a hotel when every Greedo shareholder commits to a full build loan","scenario_hash":"206e980fd82ae1dcd7a8dd9bef7537d62549b6fe5395c72d3ead6febc5a6de08","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":35,"name":"the entity automatically solicits its Greedo shareholders to fund the build shortfall after applying its rent, builds, and pays no dividend even though its bank is not empty","scenario_hash":"0e43d2c93849ea7afc61247a5225449c61fac4b1bd7992e867502f8415d3a42e","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":36,"name":"an under-developed entity whose shareholders decline to finance a build loan spends its treasury on building rather than paying a dividend, even when the treasury exceeds the dividend threshold and the last-capitalised shareholder has aged","scenario_hash":"639aaf5725345794b3f5353f3ce811ad2148d591fdf33d39bb7fd0d1843f572a","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":39,"name":"an entity short on cash for the annual payment mortgages another street in its own group to cover it, rather than defaulting","scenario_hash":"c14a29f9a77234cf6d32bdfe503bbbd8e19db5d42ec0bfb23fd576b2c35a44c2","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":43,"name":"the full-draw flag always borrows the full 80% loan-to-value cap, regardless of the actual shortfall","scenario_hash":"7140067816b135468d18523c12bcc659c6fdc38abe859f7729949e72aa21ec56","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":45,"name":"the first annual payment splits into interest and principal, paying down the outstanding balance","scenario_hash":"91189a04b6dbd57d97c3e206edcce2bdcf6e7a620b5c0aed30187cd7d3e99c3f","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":46,"name":"the loan is fully repaid once its final annual payment is made","scenario_hash":"48534ab92c592af33f780cf938ff028676c867b672310ee9614a281dcf3badb7","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":47,"name":"the bondholder receives their annual payout, split into yield and principal, as the entity repays","scenario_hash":"fb4e21ca29d13e8e0c68de2900ab6f1020f6360c8291308e717f85d28c7b47f2","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":48,"name":"on default, the entity's bond is not cashed out but re-collateralized; the bank recovers the full outstanding value before any surplus reaches the entity","scenario_hash":"8a0c5aa67939e21ef684a2e452d32a7ad93278eb1e2d4204591d60dfd56b6ec2","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":50,"name":"a later annual payment shows the entity's interest and principal genuinely diverge, not just totalling correctly, and resolves two exact half-cent ties via banker's rounding","scenario_hash":"2a07592ef75fba6c40531069c7f6de217cf9e2bf36da8981a5bac08e332170ee","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":51,"name":"the bank's own account accumulates the 2-point spread on the entity's scheduled payment","scenario_hash":"3b2ffe00b1f5a08bb70515de3d73d39c5f016cd69baffaa83512b76fb8512236","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:38.712794Z"},{"index":0,"name":"three Greedo co-owners of a colour group automatically coalesce into a legal entity at market deadlock, holding equal shares","scenario_hash":"2bc9a192965de723cc5b3195c7717145d564f42373202be38ef483f50bfd4162","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":6,"name":"the entity does not form at market deadlock when the round contained an ownership-consolidating action","scenario_hash":"3b5013e7a4162c877b92d06a4bd2cbda01a5fd97d11ad5e2dae16bb3a05bf312","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":19,"name":"a tenant who cannot cover a legal entity's rent even after liquidating goes bankrupt, exactly as for a player-owned street","scenario_hash":"ec0bfd048814d22b1861f406ac69d694d1cf94d0f2cd2f56bd9e75648a852643","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":22,"name":"the entity builds as many houses as it can afford at the end of the turn","scenario_hash":"2fb2bcce463e71807420f255baea8b84f75d80be4d796e4d29d0e1e70d9b84e4","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":37,"name":"Pink Realty takes out a development loan from the bank, funded by a bondholder, when its shareholders decline to finance a build loan","scenario_hash":"ec68607aebafa26fc398eb68fe669d5e5bc91958b559f88197b63fdacf287cb3","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":40,"name":"with no cash and no other street left to mortgage, the entity's loan defaults; foreclosure takes only the collateralized street and the entity carries on with what remains","scenario_hash":"d4ece6d98986e65de4d705f69f02001cca59e1e13ee75a8b6f04fe1e88c20d80","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":41,"name":"without the flag, an entity short on cash still cannot develop","scenario_hash":"540dc70d2216f9d587b86a3892e4930b7cd563feca886aca7ca1620a2a6d1fb4","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":42,"name":"the 80% loan-to-value cap blocks development once the shortfall exceeds it, even with a bondholder available","scenario_hash":"57b8b098b005056f6e931adf6a22f2a6634fe4d157c8dfb46c1ddb3f9e8606b6","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":44,"name":"without a bondholder able to fund it, no loan is raised and development does not happen even with the flag enabled","scenario_hash":"f5339381824ce09e9248a82df597f08a4c8b2fd0cdb45fe4b34fa5f1038cece1","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":49,"name":"the loan mechanism is not tied to which street in the group secures it","scenario_hash":"1e1715fb59ecc113df792f10f6e8dfcbd5d6d85454125a3c6f962c84329dc41f","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:21:56.075657Z"},{"index":9,"name":"the entity is not auto-formed when the eligible split's streets are already fully developed, because there is no real next improvement to fund","scenario_hash":"258a822dcb52adc6f5b3081902ad0e57a5c70be7f80fd21ff327bc17902985d6","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-13T02:29:07.814312Z"},{"index":3,"name":"the entity never auto-consolidates a highest-priority colour group, at the round boundary","scenario_hash":"97ccaf56aa722a52dd867b9da3a4c313fe398073545c0fc236ffa306907ad4e3","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T23:13:57.837338Z"},{"index":4,"name":"a two-player split of an eligible colour group does not auto-form an entity, at the round boundary","scenario_hash":"763023a56e4a499f6aa9f8363af159280ba4ad82c713f3c5e7f7e840b284ac08","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T23:13:57.837338Z"},{"index":7,"name":"the entity does not form at market deadlock when the split's shareholders cannot collectively fund the next improvement after base reserves","scenario_hash":"c0353a58d2acd0c6b8d8e9a4b271646668a4b05e5ed9420711a2562f5d112552","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T23:13:57.837338Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: Greedo legal entity for a three-way colour-group split

  Background:
    Given the official rule set
    And we select 3 players

  # entity-m1
  Scenario Outline: three Greedo co-owners of a colour group automatically coalesce into a legal entity at market deadlock, holding equal shares
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And pawn "racecar" will roll 3 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "<street_dog>"
    And pawn "high hat" owns "<street_high_hat>"
    And pawn "iron box" owns "<street_iron_box>"
    And every other ownable space is owned by pawn "racecar"
    And the <group> split's shareholders can collectively fund the next improvement after their base reserves
    When we play up to 1 round
    Then the <group> colour group is owned by <entity_name>
    And each of pawn "dog", pawn "high hat", and pawn "iron box" holds a third of <entity_name>

    Examples:
      | group  | street_dog             | street_high_hat    | street_iron_box          | entity_name  |
      | pink   | Rue de Diekirch Arlon  | Bruul Mechelen     | Place Verte Verviers     | Pink Realty  |
      | yellow | Grote Markt Hasselt    | Place de l'Ange Namur | Hoogstraat (Brussel) / Rue Haute (Bruxelles) | Yellow Realty |
      | green  | Boulevard Tirou Charleroi | Veldstraat Gent  | Boulevard d'Avroy Liège  | Green Realty |

  # entity-m2
  Scenario Outline: the entity is not auto-formed while the board still holds unowned space, at the round boundary
    Given legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    When we play up to 1 round
    Then the pink colour group is not owned by a legal entity

  # entity-m3
  Scenario Outline: the entity is not auto-formed while only stalemate trading (not legal-entity trading) is enabled, at the round boundary
    Given stalemate trading is enabled for the "Greedo" strategy
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "high hat"
    And the pink split's shareholders can collectively fund the next improvement after their base reserves
    When we play up to 1 round
    Then the pink colour group is not owned by a legal entity

  # entity-m4
  Scenario Outline: the entity never auto-consolidates a highest-priority colour group, at the round boundary
    Given legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "iron box" owns "Groenplaats Antwerpen"
    And every other ownable space is owned by pawn "high hat"
    And the <group> split's shareholders can collectively fund the next improvement after their base reserves
    When we play up to 1 round
    Then the <group> colour group is not owned by a legal entity

    Examples:
      | group   |
      | orange  |

  # entity-m5
  Scenario Outline: a two-player split of an eligible colour group does not auto-form an entity, at the round boundary
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select <player_count> players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And every other ownable space is owned by pawn "high hat"
    When we play up to 1 round
    Then the pink colour group is not owned by a legal entity

    Examples:
      | player_count |
      | 2            |

  # entity-m6
  Scenario Outline: the entity forms at market deadlock when a full round passes with no ownership-consolidating action and an eligible three-owner split can collectively fund the next improvement after base reserves
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And pawn "racecar" will roll 3 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "racecar"
    And the <group> split is an eligible three-owner split
    And the <group> split's shareholders can collectively fund the next improvement after their base reserves
    When we play up to 1 round
    Then the <group> colour group is auto-formed into Pink Realty

    Examples:
      | group |
      | pink  |

  # entity-m7
  Scenario Outline: the entity does not form at market deadlock when the round contained an ownership-consolidating action
    Given legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "high hat"
    And the <group> split is an eligible three-ownesplit
    And the <group> split's shareholders can collectively fund the next improvement after their base reserves
    And pawn "high hat" will build a house on "Lippenslaan Knokke"
    When we play up to 1 round
    Then the <group> colour group is not owned by a legal entity

    Examples:
      | group |
      | pink  |

  # entity-m8
  Scenario Outline: the entity does not form at market deadlock when the split's shareholders cannot collectively fund the next improvement after base reserves
    Given legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "high hat"
    And the <group> split is an eligible three-owner split
    And the <group> split's shareholders cannot collectively fund the next improvement after their base reserves
    When we play up to 1 round
    Then the <group> colour group is not owned by a legal entity

    Examples:
      | group |
      | pink  |

  # entity-m9
  Scenario Outline: the pre-stalemate formation trigger is independent of the final cash-threshold stalemate gate
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And pawn "racecar" will roll 3 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "racecar"
    And the <group> split is an eligible three-owner split
    And the <group> split's shareholders can collectively fund the next improvement after their base reserves
    When we play up to 1 round
    Then the <group> colour group is auto-formed into Pink Realty

    Examples:
      | group |
      | pink  |

  # entity-m10
  Scenario Outline: the entity is not auto-formed when the eligible split's streets are already fully developed, because there is no real next improvement to fund
    Given legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "high hat"
    And the <group> split is an eligible three-owner split
    And the <group> split's shareholders can collectively fund the next improvement after their base reserves
    When we play up to 1 round
    Then the <group> colour group is not owned by a legal entity

    Examples:
      | group |
      | pink  |

  # entity-m11
  Scenario Outline: the entity still forms at market deadlock when an unrelated player takes a consolidating action elsewhere on the board
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And pawn "racecar" will roll 3 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "racecar"
    And the <group> split's shareholders can collectively fund the next improvement after their base reserves
    And pawn "racecar" will build a house on "Rue Grande Dinant"
    When we play up to 1 round
    Then the <group> colour group is auto-formed into Pink Realty

    Examples:
      | group |
      | pink  |

  # entity-6
  Scenario Outline: the entity repays a shareholder loan with five percent interest on top before paying any dividend
    Given we select 4 players
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's bank account holds $<funds>
    And every other ownable space is owned by pawn "racecar"
    When we play up to 1 round
    Then the game journal records that Pink Realty repays pawn "dog" $<repayment> for the loan
    And Pink Realty's bank account holds $<funds_remaining>
    And pawn "dog" receives no dividend

    Examples:
      | principal | funds | repayment | funds_remaining |
      | 100       | 105   | 105       | 0               |

  # entity-7
  Scenario Outline: the entity builds houses from rent at the end of the turn before repaying its loan
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<rent>
    And Pink Realty owes pawn "dog" $<loan>
    When we play up to 1 round
    Then the pink colour group is developed up to at least <houses_at_least> houses
    And Pink Realty's bank account holds $<rent_remaining>
    And Pink Realty still owes pawn "dog" $<principal>

    Examples:
      | loan | rent | houses_at_least | rent_remaining | principal |
      | 200  | 200  | 2               | 0               | 200        |

  # entity-8
  Scenario Outline: no dividend is paid while any shareholder loan to the entity is still outstanding, even when the entity is fully developed
    Given we select 4 players
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's bank account holds $<surplus>
    And every other ownable space is owned by pawn "racecar"
    When we play up to 1 round
    Then pawn "dog" receives no dividend from Pink Realty

    Examples:
      | principal | surplus |
      | 100       | 150     |

  # entity-11
  Scenario Outline: a dividend is paid only after the entire loan plus interest has been repaid and the entity is fully developed
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's loan has been fully repaid
    And Pink Realty's bank account holds $<surplus>
    And the last-capitalised shareholder of Pink Realty is pawn "dog"
    And the last-capitalised shareholder of Pink Realty grows a year older
    When we play up to 1 round
    Then each of pawn "dog", pawn "high hat", and pawn "iron box" receives a $<dividend_share> dividend from Pink Realty
    And Pink Realty's bank account holds $<remainder>

    Examples:
      | principal | surplus | dividend_share | remainder |
      | 100       | 150     | 50             | 0         |
      | 100       | 170     | 56             | 2         |

  # entity-12
  Scenario Outline: the entity cannot build beyond a shareholder's personal affordability ceiling
    Given Pink Realty is formed
    And Pink Realty owes pawn "dog" $<loan>
    And pawn "high hat" has a balance that allows only $<share> toward the entity
    And pawn "iron box" has a balance that allows only $<share> toward the entity
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the pink colour group is developed up to no more than <total_houses> houses
    And no shareholder has paid more than $<ceiling_share> to the entity

    Examples:
      | loan | share | total_houses | ceiling_share |
      | 100  | 40     | 1             | 0              |

  # entity-13
  Scenario Outline: rent collected from a tenant is deposited into the entity's bank account
    Given we select 5 players
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And pawn "<renter>" starts at position <renter_position>
    And pawn "<renter>" will claim rent for "<renter_street>"
    When pawn "<renter>" lands on "<renter_street>"
    Then pawn "<renter>" has paid $<rent> in rent
    And pawn "<renter>"'s account balance is $<tenant_balance>

    Examples:
      | renter | renter_position | renter_street          | rent | tenant_balance |
      | ship   | 3               | Bruul Mechelen         | 625  | 875           |

  # entity-21
  Scenario Outline: a shareholder pays rent when landing on their own legal entity's street
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<treasury>
    And the last-capitalised shareholder of Pink Realty has not aged since funding a build
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And pawn "<renter>" starts at position <renter_position>
    And pawn "<renter>" will claim rent for "<renter_street>"
    When pawn "<renter>" lands on "<renter_street>"
    Then pawn "<renter>" has paid $<rent> in rent
    And pawn "<renter>"'s account balance is $<tenant_balance>

    Examples:
      | treasury | renter    | renter_position | renter_street          | rent | tenant_balance |
      | 5000     | iron box  | 3               | Bruul Mechelen         | 625  | 875           |

  # entity-34
  Scenario Outline: a tenant who cannot pay a legal entity's rent from cash becomes a distressed seller, exactly as for a player-owned street
    Given legal-entity trading is enabled for the "Greedo" strategy
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And pawn "high hat" returns every street except "Rue de Diekirch Arlon" to the bank
    And pawn "iron box" owns "Meir Antwerpen"
    And pawn "iron box" starts at position 8
    And pawn "dog" starts at position 15
    And pawn "high hat" starts at position 16
    And pawn "iron box" will roll 2 and 1 for their turn
    And pawn "dog" will roll 4 and 1 for their turn
    And pawn "high hat" will roll 2 and 1 for their turn
    And pawn "iron box" has $<tenant_balance> to spend
    And pawn "dog" has $<peer_balance> to spend
    And pawn "high hat" has $<peer_balance> to spend
    When we play up to 1 round
    Then the land "Meir Antwerpen" is mortgaged
    And pawn "iron box"'s account balance is $<expected_balance>
    And pawn "iron box" is not bankrupt

    Examples:
      | tenant_balance | peer_balance | expected_balance |
      | 600            | 20           | 25               |

  # entity-35
  Scenario Outline: a tenant who cannot cover a legal entity's rent even after liquidating goes bankrupt, exactly as for a player-owned street
    Given legal-entity trading is enabled for the "Greedo" strategy
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And pawn "high hat" returns every street except "Rue de Diekirch Arlon" to the bank
    And pawn "iron box" starts at position 8
    And pawn "dog" starts at position 15
    And pawn "high hat" starts at position 16
    And pawn "iron box" will roll 2 and 1 for their turn
    And pawn "dog" will roll 4 and 1 for their turn
    And pawn "high hat" will roll 2 and 1 for their turn
    And pawn "iron box" has $<tenant_balance> to spend
    And pawn "dog" has $<peer_balance> to spend
    And pawn "high hat" has $<peer_balance> to spend
    When we play up to 1 round
    Then pawn "iron box" is bankrupt
    And pawn "iron box" holds no shares of any legal entity
    And pawn "iron box"'s final balance is $<expected_final_balance>

    Examples:
      | tenant_balance | peer_balance | expected_final_balance |
      | 50             | 20           | -693                   |

  # entity-14
  Scenario Outline: a raised loan is deposited into the entity's bank account
    Given Pink Realty is formed
    When Pink Realty raises a loan of $<loan>
    Then Pink Realty's bank account holds $<bank_balance>

    Examples:
      | loan | bank_balance |
      | 150  | 150           |

  # entity-15
  Scenario Outline: the entity uses its rent before raising a loan to build when its shareholders decline the build-loan commitment
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<rent>
    And pawn "dog" has a balance of $<balance>
    And pawn "high hat" has a balance of $<balance>
    And pawn "iron box" has a balance of $<balance>
    And pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "high hat" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "iron box" follows the "Greedo" strategy, keeping a $<reserve> reserve
    When we play up to 1 round
    Then Pink Realty raises no more than $<max_loan> in loans
    And the pink colour group is developed up to at least <houses_at_least> houses
    And Pink Realty's bank account holds $<rent_remaining>

    Examples:
      | rent | balance | reserve | max_loan | houses_at_least | rent_remaining |
      | 100  | 150     | 150     | 0        | 1               | 0              |

  # entity-16
  Scenario Outline: the entity builds as many houses as it can afford at the end of the turn
    Given Pink Realty is formed
    And Pink Realty's bank account holds $<rent>
    When we play up to 1 round
    Then the <street_1>, the <street_2>, and the <street_3> each have a house built
    And the pink colour group is developed up to <total_houses> houses
    And Pink Realty's bank account holds $<rent_remaining>

    Examples:
      | rent | street_1               | street_2       | street_3              | total_houses | rent_remaining |
      | 300  | Rue de Diekirch Arlon  | Bruul Mechelen | Place Verte Verviers  | 3            | 0              |

  # entity-17
  Scenario Outline: an entity whose streets are already fully developed is financially inactive at the round boundary
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And Pink Realty owns no outstanding loan
    And Pink Realty's bank account is empty
    When we play up to 1 round
    Then Pink Realty's bank account holds $<bank_ending>
    And Pink Realty raises no more than $<max_loan> in loans
    And pawn "dog" receives no dividend from Pink Realty

    Examples:
      | bank_ending | max_loan |
      | 0           | 0        |

  # entity-22
  Scenario Outline: an entity with an empty bank whose shareholders commit to a group build loan develops one house on every street it can fund
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And each shareholder commits $<share> toward Pink Realty's build
    When we play up to 1 round
    Then the game journal records that Pink Realty raises a loan of $<loan> from pawn "dog", pawn "high hat", and pawn "iron box"
    And the street "<street_1>" has <houses_per_street> houses built
    And the street "<street_2>" has <houses_per_street> houses built
    And the street "<street_3>" has <houses_per_street> houses built

    Examples:
      | loan | street_1              | street_2       | street_3              | houses_per_street | share |
      | 300  | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 1                 | 100   |
      | 600  | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 2                 | 200   |
      | 900  | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 3                 | 300   |
      | 1200 | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 4                 | 400   |

  # entity-23
  Scenario Outline: an entity whose shareholders commit to a group build loan builds a hotel on every street when it can fund the hotels
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And each shareholder commits $<share> toward Pink Realty's build
    When we play up to 1 round
    Then the game journal records that Pink Realty raises a loan of $<loan> from pawn "dog", pawn "high hat", and pawn "iron box"
    And the street "<street_1>" has a hotel built
    And the street "<street_2>" has a hotel built
    And the street "<street_3>" has a hotel built

    Examples:
      | loan | street_1              | street_2       | street_3              | share |
      | 1500 | Rue de Diekirch Arlon | Bruul Mechelen | Place Verte Verviers  | 500   |

  # entity-18
  Scenario Outline: no dividend is paid unless the last-capitalised shareholder grows a year older, even when the entity is fully developed
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's loan has been fully repaid
    And Pink Realty's bank account holds $<surplus>
    And the last-capitalised shareholder of Pink Realty has not aged since funding a build
    When we play up to 1 round
    Then pawn "dog" receives no dividend from Pink Realty

    Examples:
      | principal | surplus |
      | 0         | 150     |

  # entity-19
  Scenario Outline: the entity pays an equal dividend when the last-capitalised shareholder grows a year older and the entity is fully developed
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's loan has been fully repaid
    And Pink Realty's bank account holds $<surplus>
    And the last-capitalised shareholder of Pink Realty is pawn "dog"
    And the last-capitalised shareholder of Pink Realty grows a year older
    When we play up to 1 round
    Then each of pawn "dog", pawn "high hat", and pawn "iron box" receives a $<dividend_share> dividend from Pink Realty

    Examples:
      | principal | surplus | dividend_share |
      | 0         | 150     | 50             |

  # entity-33
  Scenario Outline: a dividend is still paid when the last-capitalised shareholder has gone bankrupt and can never grow older to unlock it
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And Pink Realty owes pawn "dog" $<principal>
    And Pink Realty's loan has been fully repaid
    And Pink Realty's bank account holds $<surplus>
    And the last-capitalised shareholder of Pink Realty is pawn "high hat"
    And pawn "high hat" is bankrupt
    When we play up to 1 round
    Then pawn "dog"'s account balance is $<dog_expected_balance>
    And pawn "iron box"'s account balance is $<iron_box_expected_balance>

    Examples:
      | principal | surplus | dog_expected_balance | iron_box_expected_balance |
      | 0         | 150     | 1575                 | 1575                      |

  # entity-9
  Scenario Outline: the entity forms from exactly the three co-owners of a colour group even when the game has more than three players
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select <player_count> players
    And pawn "dog" owns "<street_dog>"
    And pawn "high hat" owns "<street_high_hat>"
    And pawn "iron box" owns "<street_iron_box>"
    And every other ownable space is owned by pawn "racecar"
    When pawn "dog" considers forming a legal entity over the <group> colour group
    Then the <group> colour group is owned by <entity_name>
    And each of pawn "dog", pawn "high hat", and pawn "iron box" holds a third of <entity_name>

    Examples:
      | player_count | group  | street_dog                | street_high_hat         | street_iron_box          | entity_name  |
      | 4            | pink   | Rue de Diekirch Arlon     | Bruul Mechelen          | Place Verte Verviers     | Pink Realty  |
      | 8            | yellow | Grote Markt Hasselt       | Place de l'Ange Namur   | Hoogstraat (Brussel) / Rue Haute (Bruxelles) | Yellow Realty |

  # entity-10
  Scenario Outline: the entity does not form when the colour group is split across only two owners in a larger game
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select <player_count> players
    And pawn "dog" owns "<street_dog>"
    And pawn "high hat" owns "<street_high_hat>"
    And pawn "high hat" owns "<street_high_hat_extras>"
    And every other ownable space is owned by pawn "racecar"
    When pawn "dog" considers forming a legal entity over the <group> colour group
    Then the <group> colour group is not owned by a legal entity

    Examples:
      | player_count | group | street_dog            | street_high_hat      | street_high_hat_extras |
      | 4            | pink  | Rue de Diekirch Arlon | Bruul Mechelen       | Place Verte Verviers   |
      | 8            | green | Boulevard Tirou Charleroi | Veldstraat Gent  | Boulevard d'Avroy Liège |

  # entity-24
  Scenario Outline: the entity raises a build loan when every Greedo shareholder can afford its share, is solvent, and its reserve allows it
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And pawn "dog" has a balance of $<balance>
    And pawn "high hat" has a balance of $<balance>
    And pawn "iron box" has a balance of $<balance>
    And pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "high hat" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "iron box" follows the "Greedo" strategy, keeping a $<reserve> reserve
    When we play up to 1 round
    Then the game journal records that Pink Realty raises a loan of $<loan> from pawn "dog", pawn "high hat", and pawn "iron box"
    And the street "<street>" has at least <houses> houses built

    Examples:
      | balance | reserve | loan | street              | houses |
      | 200     | 0       | 300  | Rue de Diekirch Arlon | 1      |

  # entity-25
  Scenario Outline: the entity does not raise a build loan when a Greedo shareholder cannot afford its share
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And pawn "high hat" has a balance that allows only $<ceiling_share> toward the entity
    And pawn "iron box" has a balance that allows only $<ceiling_share> toward the entity
    When we play up to 1 round
    Then Pink Realty raises no more than $0 in loans
    And the pink colour group is developed up to no more than <total_houses> houses

    Examples:
      | ceiling_share | total_houses |
      | 40            | 0            |

  # entity-27
  Scenario Outline: the entity does not raise a build loan when a Greedo shareholder's reserve would be breached
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And pawn "dog" has a balance of $<balance>
    And pawn "high hat" has a balance of $<balance>
    And pawn "iron box" has a balance of $<balance>
    And pawn "high hat" follows the "Greedo" strategy, keeping a $<reserve> reserve
    When we play up to 1 round
    Then Pink Realty raises no more than $0 in loans
    And the pink colour group is developed up to no more than <total_houses> houses

    Examples:
      | balance | reserve | total_houses |
      | 150     | 150     | 0            |

  # entity-29
  Scenario Outline: the entity builds a hotel when every Greedo shareholder commits to a full build loan
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account is empty
    And each shareholder commits $<share> toward Pink Realty's build
    When we play up to 1 round
    Then the game journal records that Pink Realty raises a loan of $<loan> from pawn "dog", pawn "high hat", and pawn "iron box"
    And the street "<street>" has a hotel built

    Examples:
      | loan | street          | share |
      | 1500 | Rue de Diekirch Arlon | 500  |

  # entity-30
  Scenario Outline: the entity automatically solicits its Greedo shareholders to fund the build shortfall after applying its rent, builds, and pays no dividend even though its bank is not empty
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<rent>
    And pawn "dog" has a balance of $<balance>
    And pawn "high hat" has a balance of $<balance>
    And pawn "iron box" has a balance of $<balance>
    And pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "high hat" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "iron box" follows the "Greedo" strategy, keeping a $<reserve> reserve
    When we play up to 1 round
    Then the game journal records that Pink Realty raises a loan of $<loan> from pawn "dog", pawn "high hat", and pawn "iron box"
    And the pink colour group is developed up to <total_houses> houses
    And pawn "dog" receives no dividend from Pink Realty

    Examples:
      | rent | balance | reserve | loan | total_houses |
      | 100  | 200     | 0       | 200  | 3            |

  # entity-32
  Scenario Outline: an under-developed entity whose shareholders decline to finance a build loan spends its treasury on building rather than paying a dividend, even when the treasury exceeds the dividend threshold and the last-capitalised shareholder has aged
    Given we select 4 players
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has <houses> house(s) built
    And the street "Bruul Mechelen" has <houses> house(s) built
    And the street "Place Verte Verviers" has <houses> house(s) built
    And Pink Realty owns no outstanding loan
    And Pink Realty's bank account holds $<surplus>
    And the last-capitalised shareholder of Pink Realty is pawn "dog"
    And the last-capitalised shareholder of Pink Realty grows a year older
    And pawn "high hat" has a balance that allows only $<ceiling_share> toward the entity
    And pawn "iron box" has a balance that allows only $<ceiling_share> toward the entity
    When we play up to 1 round
    Then pawn "dog" receives no dividend from Pink Realty
    And the pink colour group is developed up to <developed_total> houses
    And Pink Realty's bank account holds $<surplus_remaining>

    Examples:
      | houses | surplus | ceiling_share | developed_total | surplus_remaining |
      | 0      | 150     | 0             | 1               | 50                 |
      | 0      | 300     | 0             | 3               | 0                  |

  # entity-36
  Scenario Outline: Pink Realty takes out a development loan from the bank, funded by a bondholder, when its shareholders decline to finance a build loan
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<bank_funds>
    And Pink Realty owns no outstanding loan
    And pawn "dog" has a balance that allows only $0 toward the entity
    And pawn "high hat" has a balance that allows only $0 toward the entity
    And pawn "iron box" has a balance that allows only $0 toward the entity
    And pawn "racecar" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then Pink Realty raises no more than $0 in loans
    And Pink Realty raises a development loan of $<loan> secured by "Rue de Diekirch Arlon"
    And the street "Rue de Diekirch Arlon" has 1 house(s) built

    Examples:
      | bank_funds | loan |
      | 60         | 40   |
      | 30         | 70   |

  # entity-37
  Scenario Outline: no dividend is paid while a bank development loan is still outstanding, even when the entity is fully developed
    Given we select 4 players
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<surplus>
    And every other ownable space is owned by pawn "racecar"
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then pawn "dog" receives no dividend from Pink Realty

    Examples:
      | principal | surplus |
      | 100       | 150     |

  # entity-38
  Scenario Outline: an entity short on cash for the annual payment mortgages another street in its own group to cover it, rather than defaulting
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<bank_funds>
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the land "Bruul Mechelen" is mortgaged
    And Pink Realty owns "Rue de Diekirch Arlon"
    And Pink Realty owes the bank $<remaining> on the development loan

    Examples:
      | principal | bank_funds | remaining |
      | 40        | 0          | 38        |

  # entity-39
  Scenario Outline: with no cash and no other street left to mortgage, the entity's loan defaults; foreclosure takes only the collateralized street and the entity carries on with what remains
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And the land "Bruul Mechelen" is mortgaged
    And the land "Place Verte Verviers" is mortgaged
    And Pink Realty's bank account holds $0
    And pawn "racecar" will bid $<bid> for "Rue de Diekirch Arlon" at auction
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then Pink Realty does not own "Rue de Diekirch Arlon"
    And pawn "racecar" owns "Rue de Diekirch Arlon"
    And the land "Rue de Diekirch Arlon" is mortgaged
    And Pink Realty owns "Bruul Mechelen"
    And Pink Realty owns "Place Verte Verviers"
    And Pink Realty's bank account holds $<bank_ending>

    Examples:
      | principal | bid | bank_ending |
      | 40        | 25  | 35          |

  # entity-40
  Scenario Outline: without the flag, an entity short on cash still cannot develop
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<bank_funds>
    And pawn "dog" has a balance that allows only $0 toward the entity
    And pawn "high hat" has a balance that allows only $0 toward the entity
    And pawn "iron box" has a balance that allows only $0 toward the entity
    When we play up to 1 round
    Then the street "Rue de Diekirch Arlon" has 0 house(s) built
    And Pink Realty's bank account holds $<bank_ending>

    Examples:
      | bank_funds | bank_ending |
      | 60         | 60          |

  # entity-41
  Scenario Outline: the 80% loan-to-value cap blocks development once the shortfall exceeds it, even with a bondholder available
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<bank_funds>
    And pawn "dog" has a balance that allows only $0 toward the entity
    And pawn "high hat" has a balance that allows only $0 toward the entity
    And pawn "iron box" has a balance that allows only $0 toward the entity
    And pawn "racecar" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue de Diekirch Arlon" has 0 house(s) built
    And Pink Realty's bank account holds $<bank_ending>

    Examples:
      | bank_funds | bank_ending |
      | 5          | 5           |

  # entity-42
  Scenario Outline: the full-draw flag always borrows the full 80% loan-to-value cap, regardless of the actual shortfall
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<bank_funds>
    And pawn "dog" has a balance that allows only $0 toward the entity
    And pawn "high hat" has a balance that allows only $0 toward the entity
    And pawn "iron box" has a balance that allows only $0 toward the entity
    And pawn "racecar" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    And development loans draw the full amount for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue de Diekirch Arlon" has 1 house(s) built
    And Pink Realty raises a development loan of $<loan> secured by "Rue de Diekirch Arlon"

    Examples:
      | bank_funds | loan |
      | 60         | 80   |
      | 30         | 80   |

  # entity-43
  Scenario Outline: without a bondholder able to fund it, no loan is raised and development does not happen even with the flag enabled
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<bank_funds>
    And pawn "dog" has a balance that allows only $0 toward the entity
    And pawn "high hat" has a balance that allows only $0 toward the entity
    And pawn "iron box" has a balance that allows only $0 toward the entity
    And pawn "racecar" has $5 to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Rue de Diekirch Arlon" has 0 house(s) built
    And Pink Realty's bank account holds $<bank_ending>

    Examples:
      | bank_funds | bank_ending |
      | 60         | 60          |

  # entity-44
  Scenario Outline: the first annual payment splits into interest and principal, paying down the outstanding balance
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<bank_funds>
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then Pink Realty pays the bank $<interest> in interest on the development loan
    And Pink Realty pays the bank $<principal_payment> in principal on the development loan
    And Pink Realty owes the bank $<remaining> on the development loan

    Examples:
      | principal | bank_funds | interest | principal_payment | remaining |
      | 40        | 100        | 2        | 2                  | 38        |

  # entity-45
  Scenario Outline: the loan is fully repaid once its final annual payment is made
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<bank_funds>
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then Pink Realty's development loan on "Rue de Diekirch Arlon" has been fully repaid
    And Pink Realty owns no development loan

    Examples:
      | principal | bank_funds |
      | 1         | 100        |

  # entity-46
  Scenario Outline: the bondholder receives their annual payout, split into yield and principal, as the entity repays
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And pawn "racecar" holds the development loan bond secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<bank_funds>
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then pawn "racecar" receives $<yield> interest and $<principal_payment> principal on the development loan bond secured by "Rue de Diekirch Arlon"

    Examples:
      | principal | bank_funds | yield | principal_payment |
      | 100       | 200        | 3     | 5                  |

  # entity-47
  Scenario Outline: on default, the entity's bond is not cashed out but re-collateralized; the bank recovers the full outstanding value before any surplus reaches the entity
    Given we select 5 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And pawn "ship" holds the development loan bond secured by "Rue de Diekirch Arlon"
    And pawn "ship" has $<bond_cash> to spend
    And the bank's account holds $0
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And the land "Bruul Mechelen" is mortgaged
    And the land "Place Verte Verviers" is mortgaged
    And Pink Realty's bank account holds $0
    And pawn "racecar" will bid $<bid> for "Rue de Diekirch Arlon" at auction
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then pawn "ship"'s account balance is $<bond_cash>
    And the bank's account holds $<bank_account>
    And Pink Realty's bank account holds $<bank_ending>

    Examples:
      | principal | bond_cash | bid | bank_account | bank_ending |
      | 40        | 500       | 25  | 42           | 33          |

  # entity-48
  Scenario Outline: the loan mechanism is not tied to which street in the group secures it
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<bank_funds>
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And pawn "dog" has a balance that allows only $0 toward the entity
    And pawn "high hat" has a balance that allows only $0 toward the entity
    And pawn "iron box" has a balance that allows only $0 toward the entity
    And pawn "racecar" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the street "Bruul Mechelen" has 1 house(s) built
    And Pink Realty raises a development loan of $<loan> secured by "Bruul Mechelen"

    Examples:
      | bank_funds | loan |
      | 60         | 40   |

  # entity-49
  Scenario Outline: a later annual payment shows the entity's interest and principal genuinely diverge, not just totalling correctly, and resolves two exact half-cent ties via banker's rounding
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon", 1 year into its 20-year term
    And Pink Realty's bank account holds $<bank_funds>
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then Pink Realty pays the bank $<interest> interest and $<principal_payment> principal on the development loan secured by "Rue de Diekirch Arlon"
    And Pink Realty owes the bank $<remaining> on the development loan

    Examples:
      | principal | bank_funds | interest | principal_payment | remaining |
      | 400.10    | 100        | 19       | 20                 | 360.10    |

  # entity-50
  Scenario Outline: the bank's own account accumulates the 2-point spread on the entity's scheduled payment
    Given we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And pawn "racecar" holds the development loan bond secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<bank_funds>
    And the bank's account holds $0
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the bank's account holds $<bank_spread>

    Examples:
      | principal | bank_funds | bank_spread |
      | 40        | 200        | 0.80        |

  # Commentary: harness steps required to drive the Greedo entity-build-commit scenarios.
  # The strategy decision point (entity build loan commitment) and these steps are new.
  # Existing reusable steps: "each shareholder commits $X", "Pink Realty raises a loan of
  # $X", "Pink Realty raises no more than $X in loans", "pawn X has a balance that allows
  # only $Y toward the entity", "the pink colour group is developed up to no more than N
  # houses", "the street S has a hotel built", "each of ... receives a $X dividend from
  # Pink Realty", "the last-capitalised shareholder of Pink Realty is pawn X", "... grows a
  # year older", "pawn X follows the \"Greedo\" strategy, keeping a $Y reserve".
  # NEW steps needed:
  #   - "pawn X has a balance of $Y"  (set a pre-game balance via holdPawnBalance)
  #   - "pawn X is in debt by $Y"     (set a negative balance -> distress)
  #   - "pawn X has a balance that allows only $Y ..." already exists for the ceiling case
  #   - "raises no loan" -> expressed as "raises no more than $0 in loans" (existing)
  # NOTE: build-loan commitment is ALL-OR-NOTHING. No partial loan. Reasoning: a shareholder
  # who did not commit would otherwise free-ride on the rental income the loan buys, collecting
  # dividends/rent from improvements they did not help fund. So the entity raises a build loan
  # only when EVERY shareholder commits (affordable + solvent + reserve allows). A declined
  # shareholder blocks the loan entirely.
