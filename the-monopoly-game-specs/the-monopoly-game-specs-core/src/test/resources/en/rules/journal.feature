# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-12T13:46:14.842750Z","feature_name":"game journal","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/journal.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":62,"name":"the journal records that <entity_name> is formed, held in equal thirds by the three co-owners","scenario_hash":"a4be6db637620220800cb059671334feb8326d9953aad4a249aa10d7e78920af","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:14.842750Z"},{"index":63,"name":"the journal records that <entity_name> raises a loan to fund a build shortfall","scenario_hash":"e9ad49e4d27e265f711d816fec223872e8013539b5aff829547cff1fcd475dfb","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:14.842750Z"},{"index":64,"name":"the journal records that <entity_name> repays a shareholder loan","scenario_hash":"b7656ecfc842bf80e3dfe1c036c63197705128b8bf75b24d05eb13cf2601a269","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:14.842750Z"},{"index":65,"name":"the journal records an equal dividend paid by <entity_name> to each shareholder","scenario_hash":"2cf04b7772f3a11ccfefafcaf4f80d0908c70e64d184a35dbc2ef8656d6bbda5","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:14.842750Z"},{"index":67,"name":"the journal records that <entity_name> builds a house on a street when its treasury can pay for it","scenario_hash":"22634a8959cebedffc8ad88ae9b5dbdb88f7aea5d55ab007fba8b62aa8f119f1","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:14.842750Z"},{"index":58,"name":"the journal records a player's age increasing after passing start","scenario_hash":"7fbcf17d55d38120e9bde5aac3772e247ab50c4dc2d574872f57dc642ed180f5","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-09T14:48:54.313473Z"},{"index":59,"name":"the journal records a player's age increasing after being sent to jail","scenario_hash":"3b6bde007a02c2116978a5d7afc8d4a9e96f1d149393ca688dd6bc948f1200e8","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-09T14:38:55.200601Z"},{"index":60,"name":"the journal records each remaining player's final age once the game ends in a stalemate","scenario_hash":"e0821c5013abe965b2f8f7c853669be5e2e6bda897dc5d991b36d04f269e79db","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-09T14:38:55.200601Z"},{"index":52,"name":"the journal resolves a split monopoly buyout at the start of a turn, once the whole board is owned","scenario_hash":"8cf2bc8afb9010ca4e2566e3ef2ecb339c5cc54fe2543940c391e0a1004223c0","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T19:52:25.979062Z"},{"index":54,"name":"a split monopoly resolves via buyout even with an uninvolved third player in the game","scenario_hash":"6438ad6ec5d0e04cfebba2f1dc3eca2a8948b9c0f79c941f9c08421e317af45e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T19:52:25.979062Z"},{"index":46,"name":"the journal records that no one bids before it records the resulting mortgage","scenario_hash":"d2e686def68bf6699b900db1c86a15ad3bb34a891e5f527cfbb5f68e1559bb30","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:00:34.116668Z"},{"index":47,"name":"the journal records a peer trade completed at the start of a turn, once stalemate trading is enabled for the \"Greedo\" strategy and the whole board is owned","scenario_hash":"786444445f500aae64ebc7926917fec8d81350c96cbd0a9bf980bfd13d23d3c9","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:00:34.116668Z"},{"index":48,"name":"the journal does not record a peer trade when stalemate trading is not enabled for the \"Greedo\" strategy, even though the whole board is owned","scenario_hash":"3555bb72c91d1e104512857953033a8e2d5115932fc2d521b042a54561bf7e7e","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:00:34.116668Z"},{"index":49,"name":"the journal does not record a peer trade while the board still has unowned space, even though stalemate trading is enabled for the \"Greedo\" strategy","scenario_hash":"36e852ff876d15dd0afd0e5a1709b1e9a8dec701a14aab830cf644479dd0bd57","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:00:34.116668Z"},{"index":50,"name":"the journal records that stalemate trading is enabled, near the start of the game","scenario_hash":"adf7698bc6a9e602df589fc01a946da8a9418594ca095470408ea59fdf60f069","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:00:34.116668Z"},{"index":51,"name":"the journal records that stalemate trading is disabled by default, near the start of the game","scenario_hash":"3776f3cbc4bce31f63b73e2593b843a3098acc56d5c94a250b74b61d909d9fd1","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:00:34.116668Z"},{"index":53,"name":"the journal does not record a peer trade that would only benefit the trader, not the partner","scenario_hash":"a5945c251319a733d190527a23552b9573f2e173509e5f65a0b5d7462ee64a11","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:00:34.116668Z"},{"index":57,"name":"peer trading resolves two complementary splits with one cash-free swap, before buyout ever considers either group","scenario_hash":"52e9792fab620ed1ee1b43dbbc61a15fe9e94cdd739b3f35f37fee69905aecf2","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:00:34.116668Z"},{"index":30,"name":"the journal records why a player declines to buy land they cannot afford","scenario_hash":"4c8aee3e219901736c9d357d2ab1d58f6401199255155a09cf78aaa8185fe82c","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":31,"name":"the journal records why a player keeping a reserve declines a purchase that would dip below it","scenario_hash":"76dc0219ecea9523c7805ccc2098df702b383f12cc70b92fd92b936ac1693da6","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":32,"name":"the journal records a player's reserve alongside their balance at the start of a turn","scenario_hash":"f1c4f9a5a3ba6590d74a30883fecd5478c0c6386f3a134cd160d8cbc29e49eed","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":33,"name":"the journal records why a player declines to buy a card-driven property they cannot afford","scenario_hash":"564d9399f922fecf01ca83e2bad6e3e72392c941d1b5f55ece763dc64a854a32","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":34,"name":"the journal records why a player keeping a reserve declines a card-driven purchase that would dip below it","scenario_hash":"f62a6238a4a210defe2801aab0902b2ef186d3bcce82b658e2ca1185ba5eddcb","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":35,"name":"the journal records a bank-forced auction win during another player's bankruptcy","scenario_hash":"b99d9ccf3eba7b012fdf7fee20c78e00690b1fad4a59f766efc9c380b89888cf","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":36,"name":"the journal records land inherited by a creditor when a debtor goes bankrupt to them","scenario_hash":"809e1936c97e070129b4df4f9b5f720a13db4c4aa9d8631afcb18ca44296171b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":37,"name":"the journal records a creditor paying interest to keep an inherited mortgage in place","scenario_hash":"6cd166946bdb45559f6c7b511f022aea5a897fb10d48f33ce5b2c34b7b6d177a","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":38,"name":"the journal records a creditor immediately lifting an inherited mortgage","scenario_hash":"f45e2217e440375582d7aa41e092aa4dd848ea46ed1615464e71cbfffde924da","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":39,"name":"the journal records a decline with no reason when the strategy has no buying policy","scenario_hash":"ee79353e5c4b053544ed44a82a5e3963acf0b2156e25ad2c33acd80cb5584993","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":41,"name":"the journal records a debtor putting a property up for sale and the sole buyer's winning offer","scenario_hash":"89831e423e3ac33a00a95fced77ab3572976bfb6e0ad60e39af720b248d5a66d","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":42,"name":"the journal records every $5 raise in a bidding war before the winning offer","scenario_hash":"c4fac609ce25c080dee40fc5aefc17cfaa8074f9192a5082a6921c9d24a32747","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":43,"name":"the journal records a near-complete colour group's reserve only while its missing street remains affordable","scenario_hash":"37a0c428614050fa0154ea7392a03bc246eee1f9e3b17a6bedb63cc92bd44c61","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":45,"name":"the journal records that the game ends in a stalemate once every remaining player clears the threshold","scenario_hash":"07b52fab92fcaa397ea3ce028c3742a7ce5dedc3634235ef2220c50c2b34877c","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:08.169059Z"},{"index":44,"name":"the journal records a card drawn before the bank pays the player directly","scenario_hash":"b7f14f3099501a75da09ccd11251fc13efc675b60b0f71f3411d7850ef2c8e26","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-05T05:37:03.391444Z"},{"index":40,"name":"the journal records the reserve dynamically sized for a near-complete colour monopoly at the start of a turn","scenario_hash":"a6b7c8d249a790fa9abdb234f9327155b3beed62f6ddd794fbe877c757a11bd7","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-04T21:36:05.297048Z"},{"index":3,"name":"the journal records an unowned-land purchase after the landing movement","scenario_hash":"bfeb22c634f1b7747c02cfed8d070004056f5727be03b4f56841377194137bfe","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":5,"name":"the journal records rent paid after the landing movement","scenario_hash":"729b65a730a774a565eff9c40e2d092f83fa9b5b8814460d8daf19ffeefed19f","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":6,"name":"the journal records rent paid for a utility as a multiple of the dice roll that landed there","scenario_hash":"55de404e33475d2b0003b1cc791d4a82c3927a9e6605ca721e767eb15d5dacf9","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":7,"name":"the journal records a house built during a player's turn","scenario_hash":"57a9eed08dfd7102becc299c391a325af767d81f7f3e3d8805365e29123241f1","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":13,"name":"the journal records a build refused because a street in the colour group is mortgaged","scenario_hash":"6c68747f294495435d828f230572d80138a9e19d000f6c1249962471c0e77a4b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":19,"name":"the journal records a bankruptcy to the bank","scenario_hash":"9a4690aaabd97b99707d3aa8c9fdb08f8e7e34508dbdcd89c6f79b04927eb9d5","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":20,"name":"the journal records a bankruptcy to another player","scenario_hash":"8aea23c0c5a6b549c7ae9ca6e3fd0b640e801cff614f0acf27f472f075396e3d","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":21,"name":"the journal records the game's winner","scenario_hash":"97a5787a82dbd16a8352bd18e631baf96b702d384bb3f6ca3025b46fd02dd109","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":25,"name":"the journal records a mortgage forced by an unaffordable debt","scenario_hash":"deb7140946b6b70950be9d8ebb145bf6c60c0e11d809447995f3e7fdcaaaae9e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":26,"name":"the journal records a house sale forced by an unaffordable debt","scenario_hash":"53cce6780f31c6df485b07ff5b545069b3bd139a4e3eaf28f47391ba707f4688","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":27,"name":"the journal records a jailed player staying in jail after failing to roll doubles","scenario_hash":"28b43257b22a013b5316612e9a8868b0d855bc7fe3a941e5c75f8c1abd2d85d0","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":28,"name":"the journal records a jailed player leaving jail by rolling doubles","scenario_hash":"12e9796f87b2029cc27eaadab39718d22c158bff4ed2fb71e35124724fcff50d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":29,"name":"the journal records a jailed player leaving jail with a Get Out of Jail Free card","scenario_hash":"ec31fa01760e5eaeb54ee9bcb6b683ea744ad9c081f350df919ea1f425b0372d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":24,"name":"the journal records a card drawn before the move it causes","scenario_hash":"d121e12df0db250e665642f7e09430246213f12d4bf7655cdfc00953645bf9c7","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T16:45:15.436363Z"},{"index":0,"name":"the journal records game start and initiative","scenario_hash":"2e7a8628431ead85b28e68eb7a6c1c79223c5d8a7b37315aa3a204b4d770f730","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":1,"name":"the journal records a pawn's turn, roll, and movement","scenario_hash":"2f94b9a5c5f2f6549c407bc028a81adc5c302a11da4f9c949bffb4bcdea0064f","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":2,"name":"the journal records a salary collected while passing start","scenario_hash":"fade8b7f9787cac8169e2d7a0ec3b625c6b615955c549b0f58f36ff2769586ae","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":8,"name":"the journal records a house sold back to the bank","scenario_hash":"c08e1685f14b09566c50668ecd475023568227c04af9b0fcb54d4ab6a81935d9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":9,"name":"the journal records land being mortgaged","scenario_hash":"6f9feeaf37794c9f44050361cebab29cd7e116c809f58e778839d8d8f53d17d9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":10,"name":"the journal records a mortgage being lifted, including interest paid","scenario_hash":"37fddc6c1e943a6c64d33185599391f2203cfc9f0740ed851abb2acf77b82888","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":11,"name":"the journal records land sold between players","scenario_hash":"f28df5ecbcc6ba68402201dd345b42a67e1251671940ee743a4a12fe77070d19","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":12,"name":"the journal records a sale refused because the colour group has houses built","scenario_hash":"2c400d4c73dcc7bd4a7e93a7b3f3700a03a26e7b072a5fc5dc8e50943cd0ce51","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":14,"name":"the journal records a card drawn before the effect it resolves","scenario_hash":"bc27ca16bdeda5e6a1ad0460af415d5f7af8422423c0764fc670c94daccce8e0","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":15,"name":"the journal records a tax payment after the landing movement","scenario_hash":"74eb521f23ec7091d0208fc0c851c65032779cd0f2e04ea62a16fcede9fce74e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":16,"name":"the journal records jail entry and its cause","scenario_hash":"6d7b8656f204ec9c0e8fbe67be2fe3262ca7dd522a93715d60d7d5a8e5991b40","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":17,"name":"the journal records jail exit and its method","scenario_hash":"c4394c650e5a39b8b5e45c900c5854598501e038bc52e4c5d84071593f560fa7","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":18,"name":"the journal records landing on Free Parking even though nothing happens","scenario_hash":"dbb3150a6daa7381ec82d4dcc20cfcdd83841154f99d30bf3e010a94a5cf8a55","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":22,"name":"the journal records a card drawn before it pays every other player","scenario_hash":"30f57d39a048adf84c4fdea2c2376990d5515603e4f0bfe427198bc2fc5f6371","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":23,"name":"the journal records a card drawn before it collects from every other player","scenario_hash":"dd40d3685cd2b6b4664621b62225816b50579bec1675e5e756cb10f1a7177209","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":4,"name":"the journal records the winner and price of an auction after the landing movement","scenario_hash":"2270f449276e8c4a76dc3ee65d642dc84efc8720a2d57fa3db479f393387ebc5","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:12.510519Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: game journal

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # journal-1
  Scenario Outline: the journal records game start and initiative
    When we play the game
    Then the game journal records that the game starts with pawn "dog" before pawn "high hat"
    And the game journal records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game journal records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game journal records that pawn "<initiative_winner>" wins initiative
    And the game journal records game start before it records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game journal records that pawn "dog" rolls <dog_initiative_roll> for initiative before it records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game journal records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative before it records that pawn "<initiative_winner>" wins initiative
    And the game journal records that pawn "<initiative_winner>" wins initiative before starting a turn

    Examples:
      | dog_initiative_roll | high_hat_initiative_roll | initiative_winner |
      | 10                  | 4                        | dog               |

  # journal-2
  Scenario Outline: the journal records a pawn's turn, roll, and movement
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn
    And the game journal records that pawn "dog" rolls a total of <dog_roll_total>
    And the game journal records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)
    And the game journal records that pawn "dog" starts a turn before it records that pawn "dog" rolls a total of <expected_dog_roll_total>
    And the game journal records that pawn "dog" rolls a total of <expected_dog_roll_total> before it records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <expected_dog_final_position> (<expected_dog_final_space>)
    And the game journal records that pawn "dog" starts its turn before pawn "high hat"

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space               | expected_dog_final_position | expected_dog_final_space      |
      | 2         | 3         | 5               | 5                       | 0                           | Start                     | 5                  | Noord Station / Gare du Nord  | 5                            | Noord Station / Gare du Nord  |

  # journal-3
  Scenario Outline: the journal records a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)
    And the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <expected_dog_final_position> (<expected_dog_final_space>) before it records that pawn "dog" collects a salary of $<expected_dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space | expected_dog_final_position | expected_dog_final_space | dog_salary | expected_dog_salary |
      | 37                 | 1         | 2         | 37                          | Meir Antwerpen            | 0                  | Start            | 0                            | Start                     | 200        | 200                  |

  # journal-4
  Scenario Outline: the journal records an unowned-land purchase after the landing movement
    And pawn "dog" follows the "Greedo" strategy
    When pawn "dog" lands on "<property>"
    Then the game journal records that pawn "dog" buys "<property>" for $<purchase_price>
    And the game journal records that pawn "dog" moves before it records that pawn "dog" buys "<property>" for $<expected_purchase_price>

    Examples:
      | property            | purchase_price | expected_purchase_price |
      | Diestsestraat Leuven | 60             | 60                      |

  # journal-5
  Scenario Outline: the journal records the winner and price of an auction after the landing movement
    And pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<auction_price>
    And the game journal records that pawn "dog" moves before it records that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<expected_auction_price>

    Examples:
      | dog_bid | high_hat_bid | auction_winner | auction_price | expected_auction_price |
      | 90      | 120          | high hat       | 120           | 120                    |

  # journal-6
  Scenario Outline: the journal records rent paid after the landing movement
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" follows the "Greedo" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" pays pawn "high hat" $<rent> rent for "Diestsestraat Leuven"
    And the game journal records that pawn "dog" moves before it records that pawn "dog" pays pawn "high hat" $<expected_rent> rent for "Diestsestraat Leuven"

    Examples:
      | rent | expected_rent |
      | 4    | 4             |

  # journal-7
  Scenario Outline: the journal records rent paid for a utility as a multiple of the dice roll that landed there
    And pawn "dog" starts at position 7
    And pawn "dog" will roll 1 and 4 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" follows the "Greedo" strategy
    When we play the game
    Then the game journal records that pawn "dog" pays pawn "high hat" $<rent> rent for "Elektriciteitscentrale"
    And the game journal records that pawn "dog" moves before it records that pawn "dog" pays pawn "high hat" $<expected_rent> rent for "Elektriciteitscentrale"

    Examples:
      | rent | expected_rent |
      | 20   | 20            |

  # journal-8
  Scenario Outline: the journal records a house built during a player's turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game journal records that pawn "dog" builds a house on "Rue Grande Dinant" for $<cost>

    Examples:
      | cost |
      | 50   |

  # journal-9
  Scenario Outline: the journal records a house sold back to the bank
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has 1 house(s) built
    When pawn "dog" sells a house on "Diestsestraat Leuven" back to the bank
    Then the game journal records that pawn "dog" sells a house on "Diestsestraat Leuven" for $<price>

    Examples:
      | price |
      | 25    |

  # journal-10
  Scenario Outline: the journal records land being mortgaged
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" mortgages "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" mortgages "Diestsestraat Leuven" for $<value>

    Examples:
      | value |
      | 30    |

  # journal-11
  Scenario Outline: the journal records a mortgage being lifted, including interest paid
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog" has $100 to spend
    When pawn "dog" lifts the mortgage on "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" lifts the mortgage on "Diestsestraat Leuven" for $<total> including $<interest> interest

    Examples:
      | total | interest |
      | 33    | 3        |

  # journal-12
  Scenario Outline: the journal records land sold between players
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game journal records that pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<expected_price>

    Examples:
      | price | expected_price |
      | 90    | 90             |

  # journal-13
  Scenario Outline: the journal records a sale refused because the colour group has houses built
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game journal records that pawn "dog" is refused selling "Diestsestraat Leuven" to pawn "high hat" for $<expected_price> because the colour group has houses built

    Examples:
      | price | expected_price |
      | 90    | 90              |

  # journal-14
  Scenario Outline: the journal records a build refused because a street in the colour group is mortgaged
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" will build a house on "Diestsestraat Leuven"
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game journal records that pawn "dog" is refused building a house on "Diestsestraat Leuven" for $<cost> because a street in the colour group is mortgaged

    Examples:
      | cost |
      | 50   |

  # journal-15
  Scenario Outline: the journal records a card drawn before the effect it resolves
    Given the next chance card will be "Boete voor te snel rijden. Betaal M15."
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "Boete voor te snel rijden. Betaal M15." before it records that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 15     |

  # journal-16
  Scenario Outline: the journal records a tax payment after the landing movement
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "dog" moves before it records that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 200    |

  # journal-17
  Scenario Outline: the journal records jail entry and its cause
    When pawn "dog" lands on "<space>"
    Then the game journal records that pawn "dog" moves before it records that pawn "dog" is sent to jail from landing on "<space>"

    Examples:
      | space                                 |
      | Naar de Gevangenis / Allez en Prison   |

  # journal-18
  Scenario Outline: the journal records jail exit and its method
    Given pawn "dog" starts in jail
    And pawn "dog" will pay the fine to leave jail
    And pawn "dog" will roll 4 and 6 for their turn
    When we play the game
    Then the game journal records that pawn "dog" leaves jail by paying the $<fine> fine

    Examples:
      | fine |
      | 50   |

  # journal-19
  Scenario Outline: the journal records landing on Free Parking even though nothing happens
    When pawn "dog" lands on "Gratis Parkeren / Parc Gratuit"
    Then the game journal records that pawn "dog" moves from position <start position> (<start space>) to <position> (<space>)

    Examples:
      | start position | start space                            | position | space                           |
      | 17              | Algemeen Fonds / Caisse de Communauté | 20       | Gratis Parkeren / Parc Gratuit  |

  # journal-20
  Scenario Outline: the journal records a bankruptcy to the bank
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "dog" goes bankrupt to the bank

    Examples:
      | starting balance |
      | 5                 |

  # journal-21
  Scenario Outline: the journal records a bankruptcy to another player
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" goes bankrupt to pawn "high hat"

    Examples:
      | starting balance |
      | 5                 |

  # journal-22
  Scenario Outline: the journal records the game's winner
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "high hat" wins the game

    Examples:
      | starting balance |
      | 5                 |

  # journal-23
  Scenario Outline: the journal records a card drawn before it pays every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next chance card will be "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it records that pawn "dog" pays pawn "high hat" $<amount>
    And the game journal records that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it records that pawn "dog" pays pawn "iron box" $<amount>

    Examples:
      | amount |
      | 50     |

  # journal-24
  Scenario Outline: the journal records a card drawn before it collects from every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next community chest card will be "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler."
    When pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    Then the game journal records that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it records that pawn "high hat" pays pawn "dog" $<amount>
    And the game journal records that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it records that pawn "iron box" pays pawn "dog" $<amount>

    Examples:
      | amount |
      | 10     |

  # journal-25
  Scenario Outline: the journal records a card drawn before the move it causes
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200." before it records that pawn "dog" moves from position <chance_position> (<chance_space>) to <destination_position> (<destination_space>)

    Examples:
      | chance_position | chance_space  | destination_position | destination_space     |
      | 7                | Kans / Chance | 11                    | Rue de Diekirch Arlon |

  # journal-26
  Scenario Outline: the journal records a mortgage forced by an unaffordable debt
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $70 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game journal records that pawn "dog" mortgages "Rue Grande Dinant" for $<value>

    Examples:
      | value |
      | 30    |

  # journal-27
  Scenario Outline: the journal records a house sale forced by an unaffordable debt
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    And the street "Diestsestraat Leuven" has 1 house(s) built
    And pawn "dog" has $80 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game journal records that pawn "dog" sells a house on "Rue Grande Dinant" for $<price>

    Examples:
      | price |
      | 25    |

  # journal-28
  Scenario Outline: the journal records a jailed player staying in jail after failing to roll doubles
    Given pawn "dog" starts in jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game journal records that pawn "dog" stays in jail

    Examples:
      | first_die | second_die |
      | 4         | 6          |
      | 2         | 5          |

  # journal-29
  Scenario Outline: the journal records a jailed player leaving jail by rolling doubles
    Given pawn "dog" starts in jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game journal records that pawn "dog" leaves jail by rolling doubles

    Examples:
      | first_die | second_die |
      | 3         | 3          |
      | 5         | 5          |

  # journal-30
  Scenario Outline: the journal records a jailed player leaving jail with a Get Out of Jail Free card
    Given pawn "dog" starts in jail
    And pawn "dog" already holds a Get Out of Jail Free card
    And pawn "dog" will use the Get Out of Jail Free card to leave jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game journal records that pawn "dog" leaves jail using the Get Out of Jail Free card

    Examples:
      | first_die | second_die |
      | 4         | 6          |
      | 3         | 3          |

  # journal-31
  Scenario Outline: the journal records why a player declines to buy land they cannot afford
    Given pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then the game journal records that pawn "dog" declines to buy "<property>" because it cannot afford the $<price> price

    Examples:
      | property             | dog_starting_balance | high_hat_bid | price |
      | Diestsestraat Leuven | 59                    | 60           | 60    |

  # journal-32
  Scenario Outline: the journal records why a player keeping a reserve declines a purchase that would dip below it
    Given pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then the game journal records that pawn "dog" declines to buy "<property>" because it would drop the balance below the $<reserve> reserve

    Examples:
      | property         | dog_starting_balance | reserve | high_hat_bid |
      | Rue Grande Dinant | 150                  | 96      | 60           |

  # journal-33
  Scenario Outline: the journal records a player's reserve alongside their balance at the start of a turn
    Given pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | dog_starting_balance | reserve |
      | 1500                 | 0       |
      | 1500                 | 100     |

  # journal-34
  Scenario Outline: the journal records why a player declines to buy a card-driven property they cannot afford
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" declines to buy "Rue de Diekirch Arlon" because it cannot afford the $<price> price

    Examples:
      | dog_starting_balance | price |
      | 100                  | 140   |

  # journal-35
  Scenario Outline: the journal records why a player keeping a reserve declines a card-driven purchase that would dip below it
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    And pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" declines to buy "Rue de Diekirch Arlon" because it would drop the balance below the $<reserve> reserve

    Examples:
      | dog_starting_balance | reserve |
      | 200                  | 65      |

  # journal-36
  Scenario Outline: the journal records a bank-forced auction win during another player's bankruptcy
    Given pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" has $5 to spend
    And pawn "high hat" will bid $<bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "high hat" wins the auction for "Diestsestraat Leuven" at $<bid>

    Examples:
      | bid |
      | 10  |

  # journal-37
  Scenario Outline: the journal records land inherited by a creditor when a debtor goes bankrupt to them
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" will claim rent for "Diestsestraat Leuven"
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "high hat" inherits "Rue Grande Dinant" from pawn "dog"

    Examples:
      | starting_balance |
      | 10                |

  # journal-38
  Scenario Outline: the journal records a creditor paying interest to keep an inherited mortgage in place
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<owner_starting_balance> to spend
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "high hat" pays $<interest> interest to keep the mortgage on "Rue Grande Dinant"

    Examples:
      | owner_starting_balance | starting_balance | interest |
      | 0                       | 2                 | 3        |

  # journal-39
  Scenario Outline: the journal records a creditor immediately lifting an inherited mortgage
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "high hat" lifts the mortgage on "Rue Grande Dinant" for $<total> including $<interest> interest

    Examples:
      | starting_balance | total | interest |
      | 10                | 33    | 3        |

  # journal-40
  Scenario Outline: the journal records a decline with no reason when the strategy has no buying policy
    Given pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" declines to buy "Diestsestraat Leuven"

    Examples:
      | dog_starting_balance |
      | 100                   |

  # journal-41
  Scenario Outline: the journal records the reserve dynamically sized for a near-complete colour monopoly at the start of a turn
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | dog_starting_balance | reserve |
      | 1500                  | 60      |

  # journal-42
  Scenario Outline: the journal records a debtor putting a property up for sale and the sole buyer's winning offer
    Given pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game journal records that pawn "dog" puts "Lippenslaan Knokke" up for sale to avoid bankruptcy
    And the game journal records that pawn "high hat" offers $<expected_bid> for "Lippenslaan Knokke"
    And the game journal records that pawn "high hat" wins the distressed sale for "Lippenslaan Knokke" at $<expected_bid>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_bid |
      | 0                     | 200                        | 100           |

  # journal-43
  Scenario Outline: the journal records every $5 raise in a bidding war before the winning offer
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    Given pawn "dog" follows the "Greedo" strategy
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "iron box" follows the "Greedo" strategy
    And pawn "iron box" has $<iron_box_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "high hat" offers $90 for "Lippenslaan Knokke"
    And the game journal records that pawn "iron box" offers $95 for "Lippenslaan Knokke"
    And the game journal records that pawn "high hat" offers $100 for "Lippenslaan Knokke"
    And the game journal records that pawn "iron box" offers $105 for "Lippenslaan Knokke"
    And the game journal records that pawn "high hat" offers $90 for "Lippenslaan Knokke" before it records that pawn "iron box" offers $95 for "Lippenslaan Knokke"
    And the game journal records that pawn "iron box" offers $95 for "Lippenslaan Knokke" before it records that pawn "high hat" offers $100 for "Lippenslaan Knokke"
    And the game journal records that pawn "high hat" offers $100 for "Lippenslaan Knokke" before it records that pawn "iron box" offers $105 for "Lippenslaan Knokke"
    And the game journal records that pawn "iron box" offers $105 for "Lippenslaan Knokke" before it records that pawn "iron box" wins the distressed sale for "Lippenslaan Knokke" at $105
    And the game journal records that pawn "iron box" wins the distressed sale for "Lippenslaan Knokke" at $105

    Examples:
      | dog_starting_balance | high_hat_starting_balance | iron_box_starting_balance |
      | 0                     | 100                        | 320                        |

  # journal-44
  Scenario Outline: the journal records a near-complete colour group's reserve only while its missing street remains affordable
    Given pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | dog_starting_balance | reserve |
      | 200                   | 160     |
      | 100                   | 0       |

  # journal-45
  Scenario Outline: the journal records a card drawn before the bank pays the player directly
    Given the next chance card will be "De bank betaald je een dividend van M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "De bank betaald je een dividend van M50." before it records that pawn "dog" receives $<amount> from the bank

    Examples:
      | amount |
      | 50     |

  # journal-46
  Scenario Outline: the journal records that the game ends in a stalemate once every remaining player clears the threshold
    Given we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play the game
    Then the game journal records that the game ends in a stalemate before it records that pawn "dog"'s final balance is $<dog_balance>
    And the game journal records that pawn "dog"'s final balance is $<dog_balance> before it records that pawn "high hat"'s final balance is $<high_hat_balance>

    Examples:
      | dog_balance | high_hat_balance |
      | 25000       | 26000             |

  # journal-47
  Scenario Outline: the journal records that no one bids before it records the resulting mortgage
    Given pawn "high hat" follows the "Greedo" strategy, keeping a $<high_hat_reserve> reserve
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game journal records that pawn "dog" puts Lippenslaan Knokke up for sale to avoid bankruptcy before it records that pawn "dog" finds no bidder for Lippenslaan Knokke
    And the game journal records that pawn "dog" finds no bidder for Lippenslaan Knokke before it records that pawn "dog" mortgages Lippenslaan Knokke for $<mortgage_value>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | high_hat_reserve | mortgage_value |
      | 10                    | 95                         | 85                | 90               |

  # journal-48
  Scenario Outline: the journal records a peer trade completed at the start of a turn, once stalemate trading is enabled for the "Greedo" strategy and the whole board is owned
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal records that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>" before it records that pawn "dog" starts a turn
    And pawn "dog" owns "<street_dog_now_owns>"
    And pawn "high hat" owns "<street_high_hat_now_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_now_owns   | street_high_hat_now_owns                       |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  |

  # journal-49
  Scenario Outline: the journal does not record a peer trade when stalemate trading is not enabled for the "Greedo" strategy, even though the whole board is owned
    Given every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal does not record that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_still_owns                          | street_high_hat_still_owns |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven       |

  # journal-50
  Scenario Outline: the journal does not record a peer trade while the board still has unowned space, even though stalemate trading is enabled for the "Greedo" strategy
    Given stalemate trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal does not record that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_still_owns                          | street_high_hat_still_owns |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven       |

  # journal-51
  Scenario Outline: the journal records that stalemate trading is enabled, near the start of the game
    Given stalemate trading is enabled for the "Greedo" strategy
    When we play the game
    Then the game journal records that stalemate trading is <state>

    Examples:
      | state   |
      | enabled |

  # journal-52
  Scenario Outline: the journal records that stalemate trading is disabled by default, near the start of the game
    When we play the game
    Then the game journal records that stalemate trading is <state>

    Examples:
      | state    |
      | disabled |

  # journal-53
  Scenario Outline: the journal resolves a split monopoly buyout at the start of a turn, once the whole board is owned
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $1000 to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $100 to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play up to 1 round
    Then the game journal records that pawn "dog" wins the split monopoly before it records that pawn "dog" starts a turn
    And the game journal records that pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | price |
      | 40    |

  # journal-54
  Scenario Outline: the journal does not record a peer trade that would only benefit the trader, not the partner
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play up to 1 round
    Then the game journal does not record that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered    | dog_wanted           | street_dog_still_owns | street_high_hat_still_owns |
      | Meir Antwerpen | Diestsestraat Leuven | Meir Antwerpen        | Diestsestraat Leuven       |

  # journal-55
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
    Then the game journal records that pawn "dog" wins the split monopoly
    And the game journal records that pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | price |
      | 40    |

  # journal-56
  Scenario Outline: peer trading never touches a colour group that is a genuine two-owner split, even while the buyout cannot yet afford it
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When we play up to 5 rounds
    Then the game journal does not record that pawn "dog" trades "Meir Antwerpen" to pawn "high hat" for "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And the game journal does not record that pawn "high hat" trades "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)" to pawn "dog" for "Meir Antwerpen"
    And pawn "high hat" does not own "Meir Antwerpen"
    And pawn "dog" does not own "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | dog_balance | high_hat_balance |
      | 114         | 50                |

  # journal-57
  Scenario Outline: the majority owner within a split colour group wins the buyout during real play, even when poorer
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Place Verte Verviers"
    When we play up to 3 rounds
    Then the game journal records that pawn "dog" wins the split monopoly
    And pawn "dog" owns "Place Verte Verviers"

    Examples:
      | dog_balance |
      | 100          |

  # journal-58
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
    Then the game journal records that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And the game journal does not record that pawn "dog" wins the split monopoly
    And the game journal does not record that pawn "high hat" wins the split monopoly
    And pawn "dog" owns "<street_dog_now_owns>"
    And pawn "high hat" owns "<street_high_hat_now_owns>"

    Examples:
      | dog_offered              | dog_wanted            | street_dog_now_owns   | street_high_hat_now_owns |
      | Boulevard Tirou Charleroi | Place Verte Verviers  | Place Verte Verviers  | Boulevard Tirou Charleroi |

  # journal-59
  Scenario Outline: the journal records a player's age increasing after passing start
    Given pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And pawn "dog" will roll 1 and 2 for their turn
    When we play up to 2 rounds
    Then the game journal records that pawn "dog" starts a turn aged <starting_age> years before it records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that pawn "dog" collects a salary of $<dog_salary> before it records that pawn "dog" starts a turn aged <age_after_passing_start> years

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | starting_age | age_after_passing_start |
      | 29                  | 5         | 6         | 200        | 0            | 1                        |

  # journal-60
  Scenario Outline: the journal records a player's age increasing after being sent to jail
    Given pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And pawn "dog" will roll 1 and 2 for their turn
    When we play up to 2 rounds
    Then the game journal records that pawn "dog" starts a turn aged <starting_age> years before it records that pawn "dog" is sent to jail from landing on "<space>"
    And the game journal records that pawn "dog" is sent to jail from landing on "<space>" before it records that pawn "dog" starts a turn aged <age_after_jailed> years

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | space                                 | starting_age | age_after_jailed |
      | 27                  | 1         | 2         | Naar de Gevangenis / Allez en Prison  | 0            | 1                 |

  # journal-61
  Scenario Outline: the journal records each remaining player's final age once the game ends in a stalemate
    Given pawn "dog" starts at position 37
    And pawn "dog" will roll 1 and 2 for their turn
    And pawn "dog"'s account holds $<dog_starting_account>
    And pawn "high hat"'s account holds $<high_hat_starting_account>
    When we play the game
    Then the game journal records that the game ends in a stalemate before it records that pawn "dog"'s final balance is $<dog_final_balance>
    And the game journal records that pawn "dog"'s final balance is $<dog_final_balance> before it records that pawn "dog"'s final age is <dog_final_age> years
    And the game journal records that pawn "dog"'s final age is <dog_final_age> years before it records that pawn "high hat"'s final balance is $<high_hat_final_balance>
    And the game journal records that pawn "high hat"'s final balance is $<high_hat_final_balance> before it records that pawn "high hat"'s final age is <high_hat_final_age> years

    Examples:
      | dog_starting_account | dog_final_balance | high_hat_starting_account | high_hat_final_balance | dog_final_age | high_hat_final_age |
      | 24800                 | 25000              | 26000                      | 26000                   | 1              | 0                   |

  # journal-62
  Scenario Outline: the journal records the winner's final age once the game ends in an ordinary win
    Given pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "high hat" wins the game before it records that pawn "high hat"'s final age is <high_hat_final_age> years

    Examples:
      | starting_balance | high_hat_final_age |
      | 5                  | 0                   |

  # journal-63
  Scenario Outline: the journal records that <entity_name> is formed, held in equal thirds by the three co-owners
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
    Then the game journal records that <entity_name> is formed, held in equal thirds by pawn "dog", pawn "high hat", and pawn "iron box"

    Examples:
      | entity_name |
      | Pink Realty |

  # journal-64
  Scenario Outline: the journal records that <entity_name> raises a loan to fund a build shortfall
    Given <entity_name> is formed
    And <entity_name>'s bank account holds $<rent>
    And each shareholder commits $<commitment> toward Pink Realty's build
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game journal records that <entity_name> raises a loan of $<shortfall> from pawn "dog", pawn "high hat", and pawn "iron box"

    Examples:
      | entity_name | rent | shortfall | commitment |
      | Pink Realty  | 50   | 50        | 25         |

  # journal-65
  Scenario Outline: the journal records that <entity_name> repays a shareholder loan
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And <entity_name> owes pawn "dog" $100
    And <entity_name>'s bank account holds $105
    When we play up to 1 round
    Then the game journal records that <entity_name> repays pawn "dog" $105 for the loan

    Examples:
      | entity_name |
      | Pink Realty |

  # journal-66
  Scenario Outline: the journal records an equal dividend paid by <entity_name> to each shareholder
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And <entity_name>'s bank account holds $150
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game journal records that <entity_name> pays each of pawn "dog", pawn "high hat", and pawn "iron box" an equal dividend

    Examples:
      | entity_name |
      | Pink Realty |

  # journal-67
  Scenario Outline: the journal records that pawn "<renter>" pays rent to <entity_name> for an entity-owned street
    Given we select 4 players
    And <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And pawn "<renter>" starts at position <renter_position>
    And pawn "<renter>" will claim rent for "<renter_street>"
    When pawn "<renter>" lands on "<renter_street>"
    Then the game journal records that pawn "<renter>" pays $<rent> rent to <entity_name> for "<renter_street>"

    Examples:
      | entity_name | renter  | renter_position | renter_street   | rent |
      | Pink Realty  | racecar | 3               | Bruul Mechelen  | 625  |

  # journal-68
  Scenario Outline: the journal records that <entity_name> builds a house on a street when its treasury can pay for it
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And <entity_name>'s bank account holds $<treasury>
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game journal records that <entity_name> builds a house on "<street>" for $100

    Examples:
      | entity_name | treasury | street             |
      | Pink Realty  | 100      | Rue de Diekirch Arlon |

  # journal-69
  Scenario Outline: the journal records that <entity_name> raises a loan and builds a house on a street when its treasury cannot pay for it
    Given <entity_name> is formed
    And <entity_name>'s bank account holds $<rent>
    And each shareholder commits $<commitment> toward Pink Realty's build
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game journal records that <entity_name> raises a loan of $<shortfall> from pawn "dog", pawn "high hat", and pawn "iron box"
    And the game journal records that <entity_name> builds a house on "<street>" for $100

    Examples:
      | entity_name | rent | shortfall | commitment | street             |
      | Pink Realty  | 50   | 50        | 25         | Rue de Diekirch Arlon |
