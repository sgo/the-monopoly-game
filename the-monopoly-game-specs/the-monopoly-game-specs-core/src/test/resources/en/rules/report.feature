# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-12T13:46:50.833666Z","feature_name":"game report","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/report.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":62,"name":"the report narrates that <entity_name> is formed, held in equal thirds by the three co-owners","scenario_hash":"9196d93ec765bea66608d3db159ed47334c1d136b882e526183d438b7dbd1bed","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:50.833666Z"},{"index":63,"name":"the report narrates that <entity_name> raises a loan to fund a build shortfall","scenario_hash":"c1f500c657df41b454dccbfb1f9e33760a02a7bd83b12f4c72c4770d7903097b","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:50.833666Z"},{"index":64,"name":"the report narrates that <entity_name> repays a shareholder loan","scenario_hash":"46ed4e1150fa7e53aad29ca65ba00cfde837ae7e1f396bdda0987dfd8073eaad","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:50.833666Z"},{"index":65,"name":"the report narrates an equal dividend paid by <entity_name> to each shareholder","scenario_hash":"b9180d90ab0f18be65ecfde70a1cf96e02a0d1d1d631add07668e3d1bfdbe255","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:50.833666Z"},{"index":67,"name":"the report narrates that <entity_name> builds a house on a street when its treasury can pay for it","scenario_hash":"81e24abdef55629c01dbd3ee9293f2348ac72f20b05139f9ee4cb157ee6a66b2","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:50.833666Z"},{"index":58,"name":"the report narrates a player's age increasing after passing start","scenario_hash":"dff1ad4b356f7a9ab9f2f0ac7761327ca3774c509f4b445b6295365efe9dfd7d","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-09T14:49:02.102540Z"},{"index":59,"name":"the report narrates a player's age increasing after being sent to jail","scenario_hash":"c20807683d3d97f2d66aa4d3ac844d0c7fbaf6a55850ab6af5e2eb4861931165","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-09T14:39:05.757502Z"},{"index":60,"name":"the report narrates each remaining player's final age once the game ends in a stalemate","scenario_hash":"4063f8803277df36b5c7ff1db210219d7f4fd58429bf23649900dbca8adf1b78","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-09T14:39:05.757502Z"},{"index":52,"name":"the report resolves a split monopoly buyout at the start of a turn, once the whole board is owned","scenario_hash":"93a2ea493e55ae1c8dd066d57a36fcd369a47fe9e077af30c2785ba055447678","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T19:52:37.947343Z"},{"index":54,"name":"a split monopoly resolves via buyout even with an uninvolved third player in the game","scenario_hash":"64a38c3d72bb9c27b26e1548dfd953a5ac5d3ff1761b93168c22ed2cec875d39","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T19:52:37.947343Z"},{"index":46,"name":"the report says that no one bids before it says the resulting mortgage","scenario_hash":"1c47aca04e128e035bd89066fbb403fc388d59c54264e89c1e887408e99a592c","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:01:31.475231Z"},{"index":47,"name":"the report says a peer trade completed at the start of a turn, once stalemate trading is enabled for the \"Greedo\" strategy and the whole board is owned","scenario_hash":"dfea75a73aacbe98b346ee8556cf5c6a23dc0b1695e5a9071daff7c8aa67ce77","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:01:31.475231Z"},{"index":48,"name":"the report does not say a peer trade happened when stalemate trading is not enabled for the \"Greedo\" strategy, even though the whole board is owned","scenario_hash":"86d01234459f051a777a1a3cc3aa0d7dae29e8925619a57439ada3563a7df9f9","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:01:31.475231Z"},{"index":49,"name":"the report does not say a peer trade happened while the board still has unowned space, even though stalemate trading is enabled for the \"Greedo\" strategy","scenario_hash":"1a905bf7448e6b9e8182ec877e6c534000b4ad68c7ed37ad4cde3838341e75cf","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:01:31.475231Z"},{"index":50,"name":"the report says that stalemate trading is enabled, near the start of the game","scenario_hash":"33a11f3556e6c567323ee78ad82a2f6914622c4446acf3594b6bea01ac106b1b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:01:31.475231Z"},{"index":51,"name":"the report says that stalemate trading is disabled by default, near the start of the game","scenario_hash":"009191abc765d5c0fa712f82ae124e6d14555374471259629cf6fe532645ed27","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:01:31.475231Z"},{"index":53,"name":"the report does not say a peer trade happened that would only benefit the trader, not the partner","scenario_hash":"5b3d2b94268c09ebb60f98913ff5aec701346a63db616f2f88b05ffde98af17c","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:01:31.475231Z"},{"index":57,"name":"peer trading resolves two complementary splits with one cash-free swap, before buyout ever considers either group","scenario_hash":"7be0d8ce65c19751b4ac5b7b9145f4a2b52dd13130dfa00ac34aa7a765b3cde6","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:01:31.475231Z"},{"index":30,"name":"the report narrates why a player declines to buy land they cannot afford","scenario_hash":"772f2ae44f12b1250766f30b052c930e4b93f07da4f77d85d533219a849d3639","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":31,"name":"the report narrates why a player keeping a reserve declines a purchase that would dip below it","scenario_hash":"1cdb9445a72224c7826f21e5b7332636acd0ba86efd37f8451600630d2f00e23","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":32,"name":"the report narrates a player's reserve alongside their balance at the start of a turn","scenario_hash":"98ae5ebaa880401da64eef7a58136b21a2771ad4d81ca25423706228323a5158","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":33,"name":"the report narrates why a player declines to buy a card-driven property they cannot afford","scenario_hash":"113da6d0f84e1de7eec880fcf328fb5b04ae42a00e3e466dabdd5f787be1b377","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":34,"name":"the report narrates why a player keeping a reserve declines a card-driven purchase that would dip below it","scenario_hash":"d07eae076bd199ff77da98a209c0d7e8fe991355823a07185e4eb1824de8e431","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":35,"name":"the report narrates a bank-forced auction win during another player's bankruptcy","scenario_hash":"91a571ae2b65433cd18c04d723e4c4230bed3022b04c95e1d3d2c45dc00a0f88","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":36,"name":"the report narrates land inherited by a creditor when a debtor goes bankrupt to them","scenario_hash":"af97df45568d3637150eb98ec4985cb4e77037f2fba3340fb6dee582b238a92b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":37,"name":"the report narrates a creditor paying interest to keep an inherited mortgage in place","scenario_hash":"31956dff261bea794e9df1834964c0dfd6c8b65f6ddbec7359869a3a071014cb","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":38,"name":"the report narrates a creditor immediately lifting an inherited mortgage","scenario_hash":"63c8de452ac45a8d92bd54ea5a2899aa6016e6f891e27357298aeca28c3f3979","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":39,"name":"the report narrates a decline with no reason when the strategy has no buying policy","scenario_hash":"a6b17884499994732a872026da072b235413395b856ce9cc0796f6c18a6bb668","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":41,"name":"the report narrates a debtor putting a property up for sale and the sole buyer's winning offer","scenario_hash":"3c4dca87812719c89264996d875c5ade084c21c9ef2c5dc4fd33ba3118a79f3e","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":42,"name":"the report narrates every $5 raise in a bidding war before the winning offer","scenario_hash":"6201a8656b9112edb697b153ffd63f238bc7f97bb6a4eae29d73c1df2ac46900","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":43,"name":"the report narrates a near-complete colour group's reserve only while its missing street remains affordable","scenario_hash":"d854949bb5c81e7ac2f9a98b61f686178d25edc579e7113594b3ab06f7af145e","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":45,"name":"the report says that the game ends in a stalemate once every remaining player clears the threshold","scenario_hash":"7aa5afcd1d7cfc6280b46c153e8ab010fb8696f798ac6a671a186ba6e70d1e2e","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:43.391855Z"},{"index":44,"name":"the report narrates a card drawn before the bank pays the player directly","scenario_hash":"b94366b842c5fe2edfd0c7aba3e3f1c956f72782ea60ffda37f8e3e5b6fcc5ec","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-05T05:37:38.151115Z"},{"index":40,"name":"the report narrates the reserve dynamically sized for a near-complete colour monopoly at the start of a turn","scenario_hash":"133225a5f9d981a16e12db0865be158cbfb0d7f8495e43e1c45e19759a5ea5b3","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-04T21:36:40.275943Z"},{"index":3,"name":"the report narrates an unowned-land purchase after the landing movement","scenario_hash":"0d0589a70f7099097d5e8839ded17cf6ae95819b34eee46872469ea58420848a","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:51.166037Z"},{"index":5,"name":"the report narrates rent paid after the landing movement","scenario_hash":"7f1b3c40cc010414b04fa2a3944ac6c203c70213430dddb686d3c7eeac84f777","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:51.166037Z"},{"index":6,"name":"the report narrates rent paid for a utility as a multiple of the dice roll that landed there","scenario_hash":"4c40b13a691aeaf95084dbbfdf9719ab1bca99d3b9c56ef2512152c62c15f6e5","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:51.166037Z"},{"index":7,"name":"the report narrates a house built during a player's turn","scenario_hash":"4da1bac2b55912ae9107c149cf2d9d6a6ae819f35fe94e4e03c43f42bce35b6d","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:51.166037Z"},{"index":13,"name":"the report narrates a build refused because a street in the colour group is mortgaged","scenario_hash":"fce20149c84a01a4453445b996e3dd1763cb86e4c9f670e86c67e6e88c6f1361","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:51.166037Z"},{"index":19,"name":"the report narrates a bankruptcy to the bank","scenario_hash":"9e86057b9f0de005af2356a7fc1b2f4fa35a32f5b01b7f488219aeabb16efc14","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:35:47.970672Z"},{"index":20,"name":"the report narrates a bankruptcy to another player","scenario_hash":"6fb239b46cbdc04e9f56bfb3f8e90a5be461c67c0f92ed5d45b2cb3d015185ef","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:35:47.970672Z"},{"index":21,"name":"the report narrates the game's winner","scenario_hash":"eab467bc2a30005a28f585cf1cfb32f709871af134cbdd166fe65959cf92e153","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:35:47.970672Z"},{"index":25,"name":"the report narrates a mortgage forced by an unaffordable debt","scenario_hash":"cc838603603ab8a32822739fe6699bf9532469ccb64b97bdae919baa64be0c1c","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:35:47.970672Z"},{"index":26,"name":"the report narrates a house sale forced by an unaffordable debt","scenario_hash":"f22201f2975f286b7d113de6df2a0e59de1c2e8121402e331fb50b5facc7eb09","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:35:47.970672Z"},{"index":27,"name":"the report narrates a jailed player staying in jail after failing to roll doubles","scenario_hash":"127311ac618786b875690d00344ce0186af32183396b74248ab5134348d870b4","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:35:47.970672Z"},{"index":28,"name":"the report narrates a jailed player leaving jail by rolling doubles","scenario_hash":"cbfdc4f3fef92367856b667f10a152c8f58de4b7f1ba20462844a29d3c2178e9","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:35:47.970672Z"},{"index":29,"name":"the report narrates a jailed player leaving jail with a Get Out of Jail Free card","scenario_hash":"d2aaca57ee845274de152216a8b68cc94dd40d48993182d249e663526be598e9","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:35:47.970672Z"},{"index":24,"name":"the report narrates a card drawn before the move it causes","scenario_hash":"2107adf5f387292fbf660be5eb92e09b98221bd9df3729e469d4be011a69a032","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T16:45:30.503766Z"},{"index":0,"name":"the report narrates game start and initiative in chronological order","scenario_hash":"92f4bedaea4fea6d9e3d0ef660c7c13088907cca3d2ba660701b2f2ddb93291d","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":1,"name":"the report narrates a turn in chronological order","scenario_hash":"76ced6613ef61ec94e56e5a69c2fe93722e7ee8bee891fb9b851b996c9810dd0","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":2,"name":"the report narrates a salary collected while passing start","scenario_hash":"09e8f9cce52c6f0f30d2e1a6a73f0435c3d66e3a10889cbaab242bed5691341e","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":8,"name":"the report narrates a house sold back to the bank","scenario_hash":"d950c42cfc816ffe29fc30dc0e6fdd351c03b38d44200639839a82e503d85510","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":9,"name":"the report narrates land being mortgaged","scenario_hash":"bc1259ebb9b6cf15e999937929fffab3bec210e3c4fb5dfaeff18853b820218e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":10,"name":"the report narrates a mortgage being lifted, including interest paid","scenario_hash":"c2fa0c9fcaacccb5e2c56bab0a7f25a7d01874f8ebb711e45e225fe6aa79aae2","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":11,"name":"the report narrates land sold between players","scenario_hash":"3ec190057fc1f46f4d0ddd5ac65f1adbc6ce6e9770a770e8fd513511c8413cf2","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":12,"name":"the report narrates a sale refused because the colour group has houses built","scenario_hash":"8e25271fb40ce7b99aba95ecc081c0659bd8adc883c69343fca9a5307ee558ee","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":14,"name":"the report narrates a card drawn before the effect it resolves","scenario_hash":"c897694bf976d88d0faf503fb1e49634deb136bf87018022a102150fa649fa82","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":15,"name":"the report narrates a tax payment after the landing movement","scenario_hash":"0e3b666a172b3e89849ffa36dcea1bbc9867c1a5977ddb739225fc015a2229a9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":16,"name":"the report narrates jail entry and its cause","scenario_hash":"4bc52b0ab79b49b4380dcb914a97db3f7102da4cec4f7344367db61210148d69","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":17,"name":"the report narrates jail exit and its method","scenario_hash":"4ae4a3cb40566c6692290a77f72685dcd885e32ba0d3c7acd404476b6762633e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":18,"name":"the report narrates landing on Free Parking even though nothing happens","scenario_hash":"6fd22fc01f645671ca51a587870166109d91f95374892f937c3535dc30489d4d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":22,"name":"the report narrates a card drawn before it pays every other player","scenario_hash":"ea3200ca228a68124c80347bc7ac69e9410941337ef3f854e70edf40893bfa2c","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":23,"name":"the report narrates a card drawn before it collects from every other player","scenario_hash":"af1a3a74a790face0b0d67365fd623da5ab393f318a3ce82b36b2e3d8e95d03e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:09.780946Z"},{"index":4,"name":"the report narrates an auction outcome after the landing movement","scenario_hash":"9fc7a7b5bf759f7b8b819c0087f111ddf5d72ece38ec911a59d62ebaa451977d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:14.344219Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: game report

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # report-1
  Scenario Outline: the report narrates game start and initiative in chronological order
    When we play the game
    Then the game report says that the game starts with pawn "dog" before pawn "high hat"
    And the game report says that the game starts before it says that pawn "dog" rolls for initiative
    And the game report says that pawn "dog" rolls <expected_dog_initiative_roll> for initiative before it says that pawn "high hat" rolls <expected_high_hat_initiative_roll> for initiative
    And the game report says that pawn "high hat" rolls <expected_high_hat_initiative_roll> for initiative before it says that pawn "<initiative_winner>" wins initiative
    And the game report says that pawn "<initiative_winner>" wins initiative before it says that pawn "<initiative_winner>" starts a turn

    Examples:
      | expected_dog_initiative_roll | expected_high_hat_initiative_roll | initiative_winner |
      | 10                            | 4                                  | dog               |

  # report-2
  Scenario Outline: the report narrates a turn in chronological order
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game report says that pawn "dog" starts a turn before it says that pawn "dog" rolls a total of <dog_roll_total>
    And the game report says that pawn "dog" rolls a total of <expected_dog_roll_total> before it says that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space              |
      | 2         | 3         | 5               | 5                       | 0                           | Start                     | 5                  | Noord Station / Gare du Nord |

  # report-3
  Scenario Outline: the report narrates a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game report says that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>) before it says that pawn "dog" collects a salary of $<dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space | dog_salary |
      | 37                 | 1         | 2         | 37                          | Meir Antwerpen            | 0                  | Start            | 200        |

  # report-4
  Scenario Outline: the report narrates an unowned-land purchase after the landing movement
    And pawn "dog" follows the "<strategy>" strategy
    When pawn "dog" lands on "<property>"
    Then the game report says that pawn "dog" moves before it says that pawn "dog" buys "<property>" for $<purchase_price>

    Examples:
      | strategy | property | purchase_price |
      | Greedo | Diestsestraat Leuven | 60 |
      | Billionaire | Diestsestraat Leuven | 60 |

  # report-5
  Scenario Outline: the report narrates an auction outcome after the landing movement
    And pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "dog" moves before it says that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<auction_price>

    Examples:
      | dog_bid | high_hat_bid | auction_winner | auction_price |
      | 90      | 120          | high hat       | 95            |

  # report-6
  Scenario Outline: the report narrates rent paid after the landing movement
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" follows the "<strategy>" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "dog" moves before it says that pawn "dog" pays pawn "high hat" $<rent> rent for "Diestsestraat Leuven"

    Examples:
      | strategy | rent |
      | Greedo | 4 |
      | Billionaire | 4 |

  # report-7
  Scenario Outline: the report narrates rent paid for a utility as a multiple of the dice roll that landed there
    And pawn "dog" starts at position 7
    And pawn "dog" will roll 1 and 4 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" follows the "<strategy>" strategy
    When we play the game
    Then the game report says that pawn "dog" moves before it says that pawn "dog" pays pawn "high hat" $<rent> rent for "Elektriciteitscentrale"

    Examples:
      | strategy | rent |
      | Greedo | 20 |
      | Billionaire | 20 |

  # report-8
  Scenario Outline: the report narrates a house built during a player's turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game report says that pawn "dog" builds a house on "Rue Grande Dinant" for $<cost>

    Examples:
      | strategy | cost |
      | Greedo | 50 |
      | Billionaire | 50 |

  # report-9
  Scenario Outline: the report narrates a house sold back to the bank
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has 1 house(s) built
    When pawn "dog" sells a house on "Diestsestraat Leuven" back to the bank
    Then the game report says that pawn "dog" sells a house on "Diestsestraat Leuven" for $<price>

    Examples:
      | price |
      | 25    |

  # report-10
  Scenario Outline: the report narrates land being mortgaged
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" mortgages "Diestsestraat Leuven"
    Then the game report says that pawn "dog" mortgages "Diestsestraat Leuven" for $<value>

    Examples:
      | value |
      | 30    |

  # report-11
  Scenario Outline: the report narrates a mortgage being lifted, including interest paid
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog" has $100 to spend
    When pawn "dog" lifts the mortgage on "Diestsestraat Leuven"
    Then the game report says that pawn "dog" lifts the mortgage on "Diestsestraat Leuven" for $<total> including $<interest> interest

    Examples:
      | total | interest |
      | 33    | 3        |

  # report-12
  Scenario Outline: the report narrates land sold between players
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game report says that pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<expected_price>

    Examples:
      | price | expected_price |
      | 90    | 90             |

  # report-13
  Scenario Outline: the report narrates a sale refused because the colour group has houses built
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game report says that pawn "dog" is refused selling "Diestsestraat Leuven" to pawn "high hat" for $<expected_price> because the colour group has houses built

    Examples:
      | price | expected_price |
      | 90    | 90              |

  # report-14
  Scenario Outline: the report narrates a build refused because a street in the colour group is mortgaged
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" will build a house on "Diestsestraat Leuven"
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game report says that pawn "dog" is refused building a house on "Diestsestraat Leuven" for $<cost> because a street in the colour group is mortgaged

    Examples:
      | strategy | cost |
      | Greedo | 50 |
      | Billionaire | 50 |

  # report-15
  Scenario Outline: the report narrates a card drawn before the effect it resolves
    Given the next chance card will be "Boete voor te snel rijden. Betaal M15."
    When pawn "dog" lands on "Kans / Chance"
    Then the game report says that pawn "dog" draws the chance card "Boete voor te snel rijden. Betaal M15." before it says that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 15     |

  # report-16
  Scenario Outline: the report narrates a tax payment after the landing movement
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game report says that pawn "dog" moves before it says that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 200    |

  # report-17
  Scenario Outline: the report narrates jail entry and its cause
    When pawn "dog" lands on "<space>"
    Then the game report says that pawn "dog" moves before it says that pawn "dog" is sent to jail from landing on "<space>"

    Examples:
      | space                                 |
      | Naar de Gevangenis / Allez en Prison   |

  # report-18
  Scenario Outline: the report narrates jail exit and its method
    Given pawn "dog" starts in jail
    And pawn "dog" will pay the fine to leave jail
    And pawn "dog" will roll 4 and 6 for their turn
    When we play the game
    Then the game report says that pawn "dog" leaves jail by paying the $<fine> fine

    Examples:
      | fine |
      | 50   |

  # report-19
  Scenario Outline: the report narrates landing on Free Parking even though nothing happens
    When pawn "dog" lands on "Gratis Parkeren / Parc Gratuit"
    Then the game report says that pawn "dog" moves from position <start position> (<start space>) to <position> (<space>)

    Examples:
      | start position | start space                            | position | space                           |
      | 17              | Algemeen Fonds / Caisse de Communauté | 20       | Gratis Parkeren / Parc Gratuit  |

  # report-20
  Scenario Outline: the report narrates a bankruptcy to the bank
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game report says that pawn "dog" goes bankrupt to the bank

    Examples:
      | starting balance |
      | 5                 |

  # report-21
  Scenario Outline: the report narrates a bankruptcy to another player
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "dog" goes bankrupt to pawn "high hat"

    Examples:
      | starting balance |
      | 5                 |

  # report-22
  Scenario Outline: the report narrates the game's winner
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game report says that pawn "high hat" wins the game

    Examples:
      | starting balance |
      | 5                 |

  # report-23
  Scenario Outline: the report narrates a card drawn before it pays every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next chance card will be "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game report says that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it says that pawn "dog" pays pawn "high hat" $<amount>
    And the game report says that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it says that pawn "dog" pays pawn "iron box" $<amount>

    Examples:
      | amount |
      | 50     |

  # report-24
  Scenario Outline: the report narrates a card drawn before it collects from every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next community chest card will be "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler."
    When pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    Then the game report says that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it says that pawn "high hat" pays pawn "dog" $<amount>
    And the game report says that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it says that pawn "iron box" pays pawn "dog" $<amount>

    Examples:
      | amount |
      | 10     |

  # report-25
  Scenario Outline: the report narrates a card drawn before the move it causes
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    When pawn "dog" lands on "Kans / Chance"
    Then the game report says that pawn "dog" draws the chance card "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200." before it says that pawn "dog" moves from position <chance_position> (<chance_space>) to <destination_position> (<destination_space>)

    Examples:
      | chance_position | chance_space  | destination_position | destination_space     |
      | 7                | Kans / Chance | 11                    | Rue de Diekirch Arlon |

  # report-26
  Scenario Outline: the report narrates a mortgage forced by an unaffordable debt
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $70 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game report says that pawn "dog" mortgages "Rue Grande Dinant" for $<value>

    Examples:
      | value |
      | 30    |

  # report-27
  Scenario Outline: the report narrates a house sale forced by an unaffordable debt
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    And the street "Diestsestraat Leuven" has 1 house(s) built
    And pawn "dog" has $80 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game report says that pawn "dog" sells a house on "Rue Grande Dinant" for $<price>

    Examples:
      | price |
      | 25    |

  # report-28
  Scenario Outline: the report narrates a jailed player staying in jail after failing to roll doubles
    Given pawn "dog" starts in jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game report says that pawn "dog" stays in jail

    Examples:
      | first_die | second_die |
      | 4         | 6          |
      | 2         | 5          |

  # report-29
  Scenario Outline: the report narrates a jailed player leaving jail by rolling doubles
    Given pawn "dog" starts in jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game report says that pawn "dog" leaves jail by rolling doubles

    Examples:
      | first_die | second_die |
      | 3         | 3          |
      | 5         | 5          |

  # report-30
  Scenario Outline: the report narrates a jailed player leaving jail with a Get Out of Jail Free card
    Given pawn "dog" starts in jail
    And pawn "dog" already holds a Get Out of Jail Free card
    And pawn "dog" will use the Get Out of Jail Free card to leave jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game report says that pawn "dog" leaves jail using the Get Out of Jail Free card

    Examples:
      | first_die | second_die |
      | 4         | 6          |
      | 3         | 3          |

  # report-31
  Scenario Outline: the report narrates why a player declines to buy land they cannot afford
    Given pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then the game report says that pawn "dog" declines to buy "<property>" because it cannot afford the $<price> price

    Examples:
      | strategy | property | dog_starting_balance | high_hat_bid | price |
      | Greedo | Diestsestraat Leuven | 59 | 60 | 60 |
      | Billionaire | Diestsestraat Leuven | 59 | 60 | 60 |

  # report-32
  Scenario Outline: the report narrates why a player keeping a reserve declines a purchase that would dip below it
    Given pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then the game report says that pawn "dog" declines to buy "<property>" because it would drop the balance below the $<reserve> reserve

    Examples:
      | strategy | property | dog_starting_balance | reserve | high_hat_bid |
      | Greedo | Rue Grande Dinant | 150 | 96 | 60 |
      | Billionaire | Rue Grande Dinant | 150 | 96 | 60 |

  # report-33
  Scenario Outline: the report narrates a player's reserve alongside their balance at the start of a turn
    Given pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game report says that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | strategy | dog_starting_balance | reserve |
      | Greedo | 1500 | 0 |
      | Billionaire | 1500 | 0 |
      | Greedo | 1500 | 100 |
      | Billionaire | 1500 | 100 |

  # report-34
  Scenario Outline: the report narrates why a player declines to buy a card-driven property they cannot afford
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Kans / Chance"
    Then the game report says that pawn "dog" declines to buy "Rue de Diekirch Arlon" because it cannot afford the $<price> price

    Examples:
      | strategy | dog_starting_balance | price |
      | Greedo | 100 | 140 |
      | Billionaire | 100 | 140 |

  # report-35
  Scenario Outline: the report narrates why a player keeping a reserve declines a card-driven purchase that would dip below it
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    And pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Kans / Chance"
    Then the game report says that pawn "dog" declines to buy "Rue de Diekirch Arlon" because it would drop the balance below the $<reserve> reserve

    Examples:
      | strategy | dog_starting_balance | reserve |
      | Greedo | 200 | 65 |
      | Billionaire | 200 | 65 |

  # report-36
  Scenario Outline: the report narrates a bank-forced auction win during another player's bankruptcy
    Given pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" has $5 to spend
    And pawn "high hat" will bid $<bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game report says that pawn "high hat" wins the auction for "Diestsestraat Leuven" at $<price>

    Examples:
      | bid | price |
      | 35  | 30    |

  # report-37
  Scenario Outline: the report narrates land inherited by a creditor when a debtor goes bankrupt to them
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" will claim rent for "Diestsestraat Leuven"
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "high hat" inherits "Rue Grande Dinant" from pawn "dog"

    Examples:
      | starting_balance |
      | 10                |

  # report-38
  Scenario Outline: the report narrates a creditor paying interest to keep an inherited mortgage in place
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<owner_starting_balance> to spend
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "high hat" pays $<interest> interest to keep the mortgage on "Rue Grande Dinant"

    Examples:
      | strategy | owner_starting_balance | starting_balance | interest |
      | Greedo | 0 | 2 | 3 |
      | Billionaire | 0 | 2 | 3 |

  # report-39
  Scenario Outline: the report narrates a creditor immediately lifting an inherited mortgage
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "high hat" lifts the mortgage on "Rue Grande Dinant" for $<total> including $<interest> interest

    Examples:
      | strategy | starting_balance | total | interest |
      | Greedo | 10 | 33 | 3 |
      | Billionaire | 10 | 33 | 3 |

  # report-40
  Scenario Outline: the report narrates a decline with no reason when the strategy has no buying policy
    Given pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "dog" declines to buy "Diestsestraat Leuven"

    Examples:
      | dog_starting_balance |
      | 100                   |

  # report-41
  Scenario Outline: the report narrates the reserve dynamically sized for a near-complete colour monopoly at the start of a turn
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game report says that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | strategy | dog_starting_balance | reserve |
      | Greedo | 1500 | 60 |
      | Billionaire | 1500 | 60 |

  # report-42
  Scenario Outline: the report narrates a debtor putting a property up for sale and the sole buyer's winning offer
    Given pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game report says that pawn "dog" puts "Lippenslaan Knokke" up for sale to avoid bankruptcy
    And the game report says that pawn "high hat" offers $<expected_bid> for "Lippenslaan Knokke"
    And the game report says that pawn "high hat" wins the distressed sale for "Lippenslaan Knokke" at $<expected_bid>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_bid |
      | Greedo | 0 | 200 | 100 |
      | Billionaire | 0 | 200 | 100 |

  # report-43
  Scenario Outline: the report narrates every $5 raise in a bidding war before the winning offer
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    Given pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "iron box" follows the "<strategy>" strategy
    And pawn "iron box" has $<iron_box_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game report says that pawn "high hat" offers $90 for "Lippenslaan Knokke"
    And the game report says that pawn "iron box" offers $95 for "Lippenslaan Knokke"
    And the game report says that pawn "high hat" offers $100 for "Lippenslaan Knokke"
    And the game report says that pawn "iron box" offers $105 for "Lippenslaan Knokke"
    And the game report says that pawn "high hat" offers $90 for "Lippenslaan Knokke" before it says that pawn "iron box" offers $95 for "Lippenslaan Knokke"
    And the game report says that pawn "iron box" offers $95 for "Lippenslaan Knokke" before it says that pawn "high hat" offers $100 for "Lippenslaan Knokke"
    And the game report says that pawn "high hat" offers $100 for "Lippenslaan Knokke" before it says that pawn "iron box" offers $105 for "Lippenslaan Knokke"
    And the game report says that pawn "iron box" offers $105 for "Lippenslaan Knokke" before it says that pawn "iron box" wins the distressed sale for "Lippenslaan Knokke" at $105
    And the game report says that pawn "iron box" wins the distressed sale for "Lippenslaan Knokke" at $105

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | iron_box_starting_balance |
      | Greedo | 0 | 100 | 320 |
      | Billionaire | 0 | 100 | 320 |

  # report-44
  Scenario Outline: the report narrates a near-complete colour group's reserve only while its missing street remains affordable
    Given pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game report says that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | strategy | dog_starting_balance | reserve |
      | Greedo | 200 | 160 |
      | Billionaire | 200 | 160 |
      | Greedo | 100 | 0 |
      | Billionaire | 100 | 0 |

  # report-45
  Scenario Outline: the report narrates a card drawn before the bank pays the player directly
    Given the next chance card will be "De bank betaald je een dividend van M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game report says that pawn "dog" draws the chance card "De bank betaald je een dividend van M50." before it says that pawn "dog" receives $<amount> from the bank

    Examples:
      | amount |
      | 50     |

  # report-46
  Scenario Outline: the report says that the game ends in a stalemate once every remaining player clears the threshold
    Given we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play the game
    Then the game report says that the game ends in a stalemate before it says that pawn "dog"'s final balance is $<dog_balance>
    And the game report says that pawn "dog"'s final balance is $<dog_balance> before it says that pawn "high hat"'s final balance is $<high_hat_balance>

    Examples:
      | dog_balance | high_hat_balance |
      | 25000       | 26000             |

  # report-47
  Scenario Outline: the report says that no one bids before it says the resulting mortgage
    Given pawn "high hat" follows the "<strategy>" strategy, keeping a $<high_hat_reserve> reserve
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game report says that pawn "dog" puts Lippenslaan Knokke up for sale to avoid bankruptcy before it says that pawn "dog" finds no bidder for Lippenslaan Knokke
    And the game report says that pawn "dog" finds no bidder for Lippenslaan Knokke before it says that pawn "dog" mortgages Lippenslaan Knokke for $<mortgage_value>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | high_hat_reserve | mortgage_value |
      | Greedo | 10 | 95 | 85 | 90 |
      | Billionaire | 10 | 95 | 85 | 90 |

  # report-48
  Scenario Outline: the report says a peer trade completed at the start of a turn, once stalemate trading is enabled for the "Greedo" strategy and the whole board is owned
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game report says that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>" before it says that pawn "dog" starts a turn
    And pawn "dog" owns "<street_dog_now_owns>"
    And pawn "high hat" owns "<street_high_hat_now_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_now_owns   | street_high_hat_now_owns                       |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  |

  # report-49
  Scenario Outline: the report does not say a peer trade happened when stalemate trading is not enabled for the "Greedo" strategy, even though the whole board is owned
    Given every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game report does not say that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_still_owns                          | street_high_hat_still_owns |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven       |

  # report-50
  Scenario Outline: the report does not say a peer trade happened while the board still has unowned space, even though stalemate trading is enabled for the "Greedo" strategy
    Given stalemate trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game report does not say that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_still_owns                          | street_high_hat_still_owns |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven       |

  # report-51
  Scenario Outline: the report says that stalemate trading is enabled, near the start of the game
    Given stalemate trading is enabled for the "Greedo" strategy
    When we play the game
    Then the game report says that stalemate trading is <state>

    Examples:
      | state   |
      | enabled |

  # report-52
  Scenario Outline: the report says that stalemate trading is disabled by default, near the start of the game
    When we play the game
    Then the game report says that stalemate trading is <state>

    Examples:
      | state    |
      | disabled |

  # report-53
  Scenario Outline: the report resolves a split monopoly buyout at the start of a turn, once the whole board is owned
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $1000 to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $100 to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play up to 1 round
    Then the game report says that pawn "dog" wins the split monopoly before it says that pawn "dog" starts a turn
    And the game report says that pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | price |
      | 40    |

  # report-54
  Scenario Outline: the report does not say a peer trade happened that would only benefit the trader, not the partner
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play up to 1 round
    Then the game report does not say that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered    | dog_wanted           | street_dog_still_owns | street_high_hat_still_owns |
      | Meir Antwerpen | Diestsestraat Leuven | Meir Antwerpen        | Diestsestraat Leuven       |

  # report-55
  Scenario Outline: a split monopoly resolves via buyout even with an uninvolved third player in the game
    Given we select 3 players
    And pawn "iron box" will roll 10 for initiative
    And pawn "dog" will roll 7 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "iron box"
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $1000 to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $100 to spend
    When we play up to 3 rounds
    Then the game report says that pawn "dog" wins the split monopoly
    And the game report says that pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | price |
      | 40    |

  # report-56
  Scenario Outline: peer trading never touches a colour group that is a genuine two-owner split, even while the buyout cannot yet afford it
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When we play up to 5 rounds
    Then the game report does not say that pawn "dog" trades "Meir Antwerpen" to pawn "high hat" for "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And the game report does not say that pawn "high hat" trades "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)" to pawn "dog" for "Meir Antwerpen"
    And pawn "high hat" does not own "Meir Antwerpen"
    And pawn "dog" does not own "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | dog_balance | high_hat_balance |
      | 114         | 50                |

  # report-57
  Scenario Outline: the majority owner within a split colour group wins the buyout during real play, even when poorer
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Place Verte Verviers"
    When we play up to 3 rounds
    Then the game report says that pawn "dog" wins the split monopoly
    And pawn "dog" owns "Place Verte Verviers"

    Examples:
      | dog_balance |
      | 100          |

  # report-58
  Scenario Outline: peer trading resolves two complementary splits with one cash-free swap, before buyout ever considers either group
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" owns "<dog_offered>"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" owns "Rue St-Léonard Liège"
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" has $1000 to spend
    And pawn "high hat" has $500 to spend
    When we play up to 1 round
    Then the game report says that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And the game report does not say that pawn "dog" wins the split monopoly
    And the game report does not say that pawn "high hat" wins the split monopoly
    And pawn "dog" owns "<street_dog_now_owns>"
    And pawn "high hat" owns "<street_high_hat_now_owns>"

    Examples:
      | dog_offered              | dog_wanted            | street_dog_now_owns   | street_high_hat_now_owns |
      | Boulevard Tirou Charleroi | Place Verte Verviers  | Place Verte Verviers  | Boulevard Tirou Charleroi |

  # report-59
  Scenario Outline: the report narrates a player's age increasing after passing start
    Given pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And pawn "dog" will roll 1 and 2 for their turn
    When we play up to 2 rounds
    Then the game report says that pawn "dog" starts a turn aged <starting_age> years before it says that pawn "dog" collects a salary of $<dog_salary>
    And the game report says that pawn "dog" collects a salary of $<dog_salary> before it says that pawn "dog" starts a turn aged <age_after_passing_start> years

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | starting_age | age_after_passing_start |
      | 29                  | 5         | 6         | 200        | 0            | 1                        |

  # report-60
  Scenario Outline: the report narrates a player's age increasing after being sent to jail
    Given pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And pawn "dog" will roll 1 and 2 for their turn
    When we play up to 2 rounds
    Then the game report says that pawn "dog" starts a turn aged <starting_age> years before it says that pawn "dog" is sent to jail from landing on "<space>"
    And the game report says that pawn "dog" is sent to jail from landing on "<space>" before it says that pawn "dog" starts a turn aged <age_after_jailed> years

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | space                                 | starting_age | age_after_jailed |
      | 27                  | 1         | 2         | Naar de Gevangenis / Allez en Prison  | 0            | 1                 |

  # report-61
  Scenario Outline: the report narrates each remaining player's final age once the game ends in a stalemate
    Given pawn "dog" starts at position 37
    And pawn "dog" will roll 1 and 2 for their turn
    And pawn "dog"'s account holds $<dog_starting_account>
    And pawn "high hat"'s account holds $<high_hat_starting_account>
    When we play the game
    Then the game report says that the game ends in a stalemate before it says that pawn "dog"'s final balance is $<dog_final_balance>
    And the game report says that pawn "dog"'s final balance is $<dog_final_balance> before it says that pawn "dog"'s final age is <dog_final_age> years
    And the game report says that pawn "dog"'s final age is <dog_final_age> years before it says that pawn "high hat"'s final balance is $<high_hat_final_balance>
    And the game report says that pawn "high hat"'s final balance is $<high_hat_final_balance> before it says that pawn "high hat"'s final age is <high_hat_final_age> years

    Examples:
      | dog_starting_account | dog_final_balance | high_hat_starting_account | high_hat_final_balance | dog_final_age | high_hat_final_age |
      | 24800                 | 25000              | 26000                      | 26000                   | 1              | 0                   |

  # report-62
  Scenario Outline: the report narrates the winner's final age once the game ends in an ordinary win
    Given pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game report says that pawn "high hat" wins the game before it says that pawn "high hat"'s final age is <high_hat_final_age> years

    Examples:
      | starting_balance | high_hat_final_age |
      | 5                  | 0                   |

  # report-63
  Scenario Outline: the report narrates that <entity_name> is formed, held in equal thirds by the three co-owners
    Given we select 3 players
    And pawn "iron box" will roll 10 for initiative
    And pawn "dog" will roll 7 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "high hat"
    When we play up to 1 round
    Then the game report says that <entity_name> is formed, held in equal thirds by pawn "dog", pawn "high hat", and pawn "iron box"

    Examples:
      | entity_name |
      | Pink Realty |

  # report-64
  Scenario Outline: the report narrates that <entity_name> raises a loan to fund a build shortfall
    Given <entity_name> is formed
    And <entity_name>'s bank account holds $<rent>
    And each shareholder commits $<commitment> toward Pink Realty's build
    When we play up to 1 round
    Then the game report says that <entity_name> raises a loan of $<shortfall> from pawn "dog", pawn "high hat", and pawn "iron box"

    Examples:
      | entity_name | rent | shortfall | commitment |
      | Pink Realty  | 50   | 50        | 25         |

  # report-65
  Scenario Outline: the report narrates that <entity_name> repays a shareholder loan
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And <entity_name> owes pawn "dog" $100
    And <entity_name>'s bank account holds $105
    When we play up to 1 round
    Then the game report says that <entity_name> pays pawn "dog" $105 for the loan

    Examples:
      | entity_name |
      | Pink Realty |

  # report-66
  Scenario Outline: the report narrates an equal dividend paid by <entity_name> to each shareholder
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And <entity_name>'s bank account holds $150
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game report says that <entity_name> pays each of pawn "dog", pawn "high hat", and pawn "iron box" an equal dividend

    Examples:
      | entity_name |
      | Pink Realty |

  # report-67
  Scenario Outline: the report narrates that pawn "<renter>" pays rent to <entity_name> for an entity-owned street
    Given we select 4 players
    And <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And pawn "<renter>" starts at position <renter_position>
    And pawn "<renter>" will claim rent for "<renter_street>"
    When pawn "<renter>" lands on "<renter_street>"
    Then the game report says that pawn "<renter>" pays $<rent> rent to <entity_name> for "<renter_street>"

    Examples:
      | entity_name | renter  | renter_position | renter_street   | rent |
      | Pink Realty  | racecar | 3               | Bruul Mechelen  | 625  |

  # report-68
  Scenario Outline: the report narrates that <entity_name> builds a house on a street when its treasury can pay for it
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And <entity_name>'s bank account holds $<treasury>
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game report says that <entity_name> builds a house on "<street>" for $100

    Examples:
      | entity_name | treasury | street             |
      | Pink Realty  | 100      | Rue de Diekirch Arlon |

  # report-69
  Scenario Outline: the report narrates that <entity_name> raises a loan and builds a house on a street when its treasury cannot pay for it
    Given <entity_name> is formed
    And <entity_name>'s bank account holds $<rent>
    And each shareholder commits $<commitment> toward Pink Realty's build
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game report says that <entity_name> raises a loan of $<shortfall> from pawn "dog", pawn "high hat", and pawn "iron box"
    And the game report says that <entity_name> builds a house on "<street>" for $100

    Examples:
      | entity_name | rent | shortfall | commitment | street             |
      | Pink Realty  | 50   | 50        | 25         | Rue de Diekirch Arlon |

  # report-70
  Scenario Outline: the report narrates each remaining player's final balance and age once the game ends because the year limit was reached
    Given the game is limited to <year limit> years
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "dog" starts at position 35
    And pawn "dog" will roll 5 for their turn
    And pawn "dog"'s account holds $<dog_starting_account>
    And pawn "high hat"'s account holds $<high_hat_starting_account>
    When we play the game
    Then the game report says that the game ends because the year limit was reached before it says that pawn "dog"'s final balance is $<dog_final_balance>
    And the game report says that pawn "dog"'s final balance is $<dog_final_balance> before it says that pawn "dog"'s final age is <dog_final_age> years
    And the game report says that pawn "dog"'s final age is <dog_final_age> years before it says that pawn "high hat"'s final balance is $<high_hat_final_balance>
    And the game report says that pawn "high hat"'s final balance is $<high_hat_final_balance> before it says that pawn "high hat"'s final age is <high_hat_final_age> years

    Examples:
      | year limit | dog_starting_account | dog_final_balance | dog_final_age | high_hat_starting_account | high_hat_final_balance | high_hat_final_age |
      | 1          | 1500                  | 1700               | 1              | 1500                       | 1500                    | 0                   |
