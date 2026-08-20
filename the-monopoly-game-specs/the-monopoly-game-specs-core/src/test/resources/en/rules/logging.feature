# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-20T13:46:52.441541Z","feature_name":"game logging","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/logging.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":87,"name":"the log records that the war profits tax is enabled, near the start of the game","scenario_hash":"e2c126b6e96c34dc82d99f6834af4ee47cc4127e029c6d72b0801026fb6fa3d8","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T13:46:52.441541Z"},{"index":4,"name":"the log records an unowned-land purchase after the landing movement","scenario_hash":"94939681746bed378921588193009b0a18cbae801015aab00ea2c92982a2c7ab","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":7,"name":"the log records rent paid for a utility as a multiple of the dice roll that landed there","scenario_hash":"8acbd90992981d81f4defbb21f5b8231d6e9fd9693e2083cd8ba5defbc6d0c67","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":14,"name":"the log records a build refused because a street in the colour group is mortgaged","scenario_hash":"ae1b1b9a5945c88198ef27112687d65535836bb6edd3b39fe0346ee38140aaaa","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":63,"name":"the log records that <entity_name> is formed, held in equal thirds by the three co-owners","scenario_hash":"04905991f5aee73419d85b87e96eebfe7f96e31f70d11a9fb49a4c03f82db647","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":65,"name":"the log records that <entity_name> repays a shareholder loan","scenario_hash":"746e853b961a8bb433aedd1880dff3aff0990c50936c520c810075f1c41943a6","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":66,"name":"the log records an equal dividend paid by <entity_name> to each shareholder","scenario_hash":"8d17c09873d67eacd88fbc02d56ea1ce1ff81253e6d2fc546328e3c682a2a282","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":70,"name":"the log records each remaining player's final balance and age once the game ends because the year limit was reached","scenario_hash":"aa7ca13e677e0f7c61984fab8ce0d360129074fdf04b357ea7d36b22f55ff301","mutation_count":7,"result":{"Total":7,"Killed":7,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":71,"name":"the log records a player raising a development loan","scenario_hash":"a4b4e519c73b21f389d46d11a9f678c6e6a1249d000c88ad9d7ab8d745d4fe48","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":72,"name":"the log records a legal entity raising a development loan","scenario_hash":"f43b6496fd7ca02745a650c72af62ac473635b59809c9a054281fc8b3d99ad53","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":83,"name":"the log records that development loans are enabled, near the start of the game","scenario_hash":"79c642f1e6caddfb3fad27a1a76e424de2104a5b2d1fc69bd024b7bd0c25fd8d","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:39.689586Z"},{"index":69,"name":"the log records that <entity_name> raises a loan and builds a house on a street when its treasury cannot pay for it","scenario_hash":"279b3c24bef26377275f7e12c80e413636f59eb89e66835fadcddaeb47e5606f","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:42.639922Z"},{"index":60,"name":"the log records a player's age increasing after being sent to jail","scenario_hash":"0120589f63eb6d30cf0d557d96e49095f0aa7d8fdb126e24314ffcf7cea0f6c7","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-09T14:39:00.659486Z"},{"index":61,"name":"the log records each remaining player's final age once the game ends in a stalemate","scenario_hash":"33bfbbef88e1d4c8f22ae8b114f36d2866bbb25279a38b4b46cddbd265dab3e4","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-09T14:39:00.659486Z"},{"index":53,"name":"the log resolves a split monopoly buyout at the start of a turn, once the whole board is owned","scenario_hash":"291323795e92ecee1d873e0765ad36d389449604eadf5d99f30bcd1581136e9e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T19:52:34.319580Z"},{"index":55,"name":"a split monopoly resolves via buyout even with an uninvolved third player in the game","scenario_hash":"18d40d8912255b54eaf67c3ceaa34db69ca1c1a4cdbcfadac25d16e2581fe65d","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T19:52:34.319580Z"},{"index":48,"name":"the log records a peer trade completed at the start of a turn, once stalemate trading is enabled for the \"Greedo\" strategy and the whole board is owned","scenario_hash":"95bae1408faa39b64f3ce405f1a5548d3c311f78a7e228aacb64afe29f42a53a","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:02:03.626782Z"},{"index":49,"name":"the log does not record a peer trade when stalemate trading is not enabled for the \"Greedo\" strategy, even though the whole board is owned","scenario_hash":"62c0924b3e2c14b6170afb4bb1613c76b90b3f88905714e546e1978f78d97a6c","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:02:03.626782Z"},{"index":50,"name":"the log does not record a peer trade while the board still has unowned space, even though stalemate trading is enabled for the \"Greedo\" strategy","scenario_hash":"9065331129f1abdd6e02138267b9e7988d02741651ab981c766d05bb439aeebc","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:02:03.626782Z"},{"index":51,"name":"the log records that stalemate trading is enabled, near the start of the game","scenario_hash":"e467331a9cb949bce3cafc31f9e17df8942eb583c40deec486f01195bc309b23","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:02:03.626782Z"},{"index":52,"name":"the log records that stalemate trading is disabled by default, near the start of the game","scenario_hash":"b0013af952e92fee7f5312c5449b744f005389e6b2df6f0e2ae75cc31c5857c6","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:02:03.626782Z"},{"index":54,"name":"the log does not record a peer trade that would only benefit the trader, not the partner","scenario_hash":"40fc8b257c733ece9c6508cc018dee29fc111e90c6b13776fd6dd4d704e7b6e0","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:02:03.626782Z"},{"index":58,"name":"peer trading resolves two complementary splits with one cash-free swap, before buyout ever considers either group","scenario_hash":"12ba2bdc5cd2f436a7334b5e0085d33e87da362939031e133d09dc77f25db6aa","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-08T18:02:03.626782Z"},{"index":37,"name":"the log records land inherited by a creditor when a debtor goes bankrupt to them","scenario_hash":"811e72cb7a30f4b11fd8d60768b6b71b01ddeb80c624824370b76d4b76a383fd","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:37.687907Z"},{"index":40,"name":"the log records a decline with no reason when the strategy has no buying policy","scenario_hash":"c59022390748f1b39189bcca5f7d1f15be40e19483fbf9ba3935f396715d6962","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:37.687907Z"},{"index":46,"name":"the log records that the game ends in a stalemate once every remaining player clears the threshold","scenario_hash":"97309e6b179967ce1f37619edd750daa5f8528554fccbace68aabef0e7781ebb","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:40:37.687907Z"},{"index":45,"name":"the log records a card drawn before the bank pays the player directly","scenario_hash":"a82e9b34f1b2259250cb550a25d446a554f75f3796eeaa3da55a3363f4e5e0bb","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-05T05:37:32.928413Z"},{"index":20,"name":"the log records a bankruptcy to the bank","scenario_hash":"949343d307dff987ade9ecea5fe9e7beccc3f61b11aadbb5be3d53b6a47f8b67","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:36:29.743331Z"},{"index":21,"name":"the log records a bankruptcy to another player","scenario_hash":"1f0b70a84eb4a0eafa6654e259ef9e610c3dba96ef2d272e2b956571300e32e0","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:36:29.743331Z"},{"index":22,"name":"the log records the game's winner","scenario_hash":"31b4d7386cdce00ea4777fed5a7f4c727e9b59dcd2246e099dfaed2d8da7a30e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:36:29.743331Z"},{"index":26,"name":"the log records a mortgage forced by an unaffordable debt","scenario_hash":"0fe392618079f313660792cc3fe42197ce2a699b2f91d5eb94b1e2f4057f64f7","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:36:29.743331Z"},{"index":27,"name":"the log records a house sale forced by an unaffordable debt","scenario_hash":"53438f460e35a8ad4b6f46c6d098bc7d85ae55b34997c864abe3c1543caff34a","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:36:29.743331Z"},{"index":28,"name":"the log records a jailed player staying in jail after failing to roll doubles","scenario_hash":"aad0ee0983e079bc0fda5416c9f814b8cac1d7115d1789c6b181d9021bc9fafb","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:36:29.743331Z"},{"index":29,"name":"the log records a jailed player leaving jail by rolling doubles","scenario_hash":"5bac6901b88f3f177f1232bf1477f2384b7b25276e4c8a18de16f38ad033fff6","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:36:29.743331Z"},{"index":30,"name":"the log records a jailed player leaving jail with a Get Out of Jail Free card","scenario_hash":"d05101ae5bea46da831611829746cd9bd3fbb9448299bce48082c98b630a6e98","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:36:29.743331Z"},{"index":25,"name":"the log records a card drawn before the move it causes","scenario_hash":"9f83ec98c617606f7c73d7ec34c84912b21d512c6c2ee9ca648a556ac6efb13b","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T16:45:28.091115Z"},{"index":2,"name":"the log records a pawn's turn, roll, and movement","scenario_hash":"7ac58d5128703ec1445b01b7a312461c1a878a5f3ca7927d817c3399cce49e01","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":3,"name":"the log records a salary collected while passing start","scenario_hash":"700e9bab2bb369e062331a4faa912979717ce887d68d7b1933266631f78f289a","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":19,"name":"the log records landing on Free Parking even though nothing happens","scenario_hash":"152d741a90091b911cac0241773663d237f9678e95d21699e85220180ca32c6e","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":23,"name":"the log records a card drawn before it pays every other player","scenario_hash":"a63499f8e082c4f0e6b488d8e8d3f05a817372c810b0be1945384ca7a9974548","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":24,"name":"the log records a card drawn before it collects from every other player","scenario_hash":"882b25b969fefaa0ce4db56412acd4119bba7ad9dc8d7306c0278c7dc2c05491","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":0,"name":"logged event text matches report rendering","scenario_hash":"f835ddb36be9c4b3df983c8fb0d309b4d4900ccb172e1b73b0ad7f91dee23892","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":1,"name":"the log records game start and initiative","scenario_hash":"a2b92a55e28235ba80668a83c039c56dcf6188c05dea393ffd5cdcf8e9e2b996","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":9,"name":"the log records a house sold back to the bank","scenario_hash":"3cfc0073d188e9170feca30de7767b31a393f3ac1d8193ed39883410a53baaa5","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":10,"name":"the log records land being mortgaged","scenario_hash":"e57add729a6e6abc7a6ecdfef35f6c1ef3d88c5c98b645d5b00414e22fbad84f","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":11,"name":"the log records a mortgage being lifted, including interest paid","scenario_hash":"4c8e414e048527a726da52a655359c92d93534d70fcb451a860f7f25112f3ebf","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":12,"name":"the log records land sold between players","scenario_hash":"10eb426dfd6598520acef844cbdac94b51af1184ea4ba1975c3c1084db923f22","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":13,"name":"the log records a sale refused because the colour group has houses built","scenario_hash":"acec232a2c18797a361f9dda2526ddfbbf2c0b88d275c38c9e0bed00a1c71cea","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":15,"name":"the log records a card drawn before the effect it resolves","scenario_hash":"65627bc06a4c5b7a80a5e349718afd5e916a35de3c8563f87ecd6360fba51ce6","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":16,"name":"the log records a tax payment after the landing movement","scenario_hash":"285b1540daae2d937dfa0eb9f68c84e03bed6c62bd7982a0ebad33af2821c353","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":17,"name":"the log records jail entry and its cause","scenario_hash":"f4cf0a54beff41bb258129e259e3c1b0423ff2317fcebc240dfb46ab78d247c2","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":18,"name":"the log records jail exit and its method","scenario_hash":"67bfb95cd8583adb7f41a95cef2aed5242e117e11d7847e25be50bf215fa597e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: game logging

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # log-1
  Scenario Outline: logged event text matches report rendering
    Given a game with an event of type "<event_type>"
    When the event is rendered for the report
    And the event is logged to the Journal
    Then the logged message text is identical to the report's rendered text

    Examples:
      | event_type           |
      | player_buys_property |
      | player_pays_rent     |
      | player_passes_go     |
      | player_draws_card    |

  # logging-1
  Scenario Outline: the log records game start and initiative
    When we play the game
    Then the game log records that the game starts with pawn "dog" before pawn "high hat"
    And the game log records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game log records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game log records that pawn "<initiative_winner>" wins initiative
    And the game log records game start before it records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game log records that pawn "dog" rolls <dog_initiative_roll> for initiative before it records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game log records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative before it records that pawn "<initiative_winner>" wins initiative
    And the game log records that pawn "<initiative_winner>" wins initiative before starting a turn

    Examples:
      | dog_initiative_roll | high_hat_initiative_roll | initiative_winner |
      | 10                  | 4                        | dog               |

  # logging-2
  Scenario Outline: the log records a pawn's turn, roll, and movement
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game log records that pawn "dog" starts a turn with balance "<dog_starting_balance>"
    And the game log records that pawn "dog" rolls a total of <dog_roll_total>
    And the game log records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)
    And the game log records that pawn "dog" starts a turn before it records that pawn "dog" rolls a total of <expected_dog_roll_total>
    And the game log records that pawn "dog" rolls a total of <expected_dog_roll_total> before it records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <expected_dog_final_position> (<expected_dog_final_space>)
    And the game log records that pawn "dog" starts its turn before pawn "high hat"

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space               | expected_dog_final_position | expected_dog_final_space      | dog_starting_balance |
      | 2         | 3         | 5               | 5                       | 0                           | Start                     | 5                  | Noord Station / Gare du Nord  | 5                            | Noord Station / Gare du Nord  | $1500                |

  # logging-3
  Scenario Outline: the log records a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game log records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)
    And the game log records that pawn "dog" collects a salary of $<dog_salary>
    And the game log records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <expected_dog_final_position> (<expected_dog_final_space>) before it records that pawn "dog" collects a salary of $<expected_dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space | expected_dog_final_position | expected_dog_final_space | dog_salary | expected_dog_salary |
      | 37                 | 1         | 2         | 37                          | Meir Antwerpen            | 0                  | Start            | 0                            | Start                     | 200        | 200                  |

  # logging-4
  Scenario Outline: the log records an unowned-land purchase after the landing movement
    And pawn "dog" follows the "<strategy>" strategy
    When pawn "dog" lands on "<property>"
    Then the game log records that pawn "dog" buys "<property>" for $<purchase_price>
    And the game log records that pawn "dog" moves before it records that pawn "dog" buys "<property>" for $<expected_purchase_price>

    Examples:
      | strategy | property | purchase_price | expected_purchase_price |
      | Greedo | Diestsestraat Leuven | 60 | 60 |
      | Billionaire | Diestsestraat Leuven | 60 | 60 |

  # logging-5
  Scenario Outline: the log records the winner and price of an auction after the landing movement
    And pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<auction_price>
    And the game log records that pawn "dog" moves before it records that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<expected_auction_price>

    Examples:
      | dog_bid | high_hat_bid | auction_winner | auction_price | expected_auction_price |
      | 90      | 120          | high hat       | 95            | 95                     |

  # logging-6
  Scenario Outline: the log records rent paid after the landing movement
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" follows the "<strategy>" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "dog" pays pawn "high hat" $<rent> rent for "Diestsestraat Leuven"
    And the game log records that pawn "dog" moves before it records that pawn "dog" pays pawn "high hat" $<expected_rent> rent for "Diestsestraat Leuven"

    Examples:
      | strategy | rent | expected_rent |
      | Greedo | 4 | 4 |
      | Billionaire | 4 | 4 |

  # logging-7
  Scenario Outline: the log records rent paid for a utility as a multiple of the dice roll that landed there
    And pawn "dog" starts at position 7
    And pawn "dog" will roll 1 and 4 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" follows the "<strategy>" strategy
    When we play the game
    Then the game log records that pawn "dog" pays pawn "high hat" $<rent> rent for "Elektriciteitscentrale"
    And the game log records that pawn "dog" moves before it records that pawn "dog" pays pawn "high hat" $<expected_rent> rent for "Elektriciteitscentrale"

    Examples:
      | strategy | rent | expected_rent |
      | Greedo | 20 | 20 |
      | Billionaire | 20 | 20 |

  # logging-8
  Scenario Outline: the log records a house built during a player's turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game log records that pawn "dog" builds a house on "Rue Grande Dinant" for $<cost>

    Examples:
      | strategy | cost |
      | Greedo | 50 |
      | Billionaire | 50 |

  # logging-9
  Scenario Outline: the log records a house sold back to the bank
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has 1 house(s) built
    When pawn "dog" sells a house on "Diestsestraat Leuven" back to the bank
    Then the game log records that pawn "dog" sells a house on "Diestsestraat Leuven" for $<price>

    Examples:
      | price |
      | 25    |

  # logging-10
  Scenario Outline: the log records land being mortgaged
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" mortgages "Diestsestraat Leuven"
    Then the game log records that pawn "dog" mortgages "Diestsestraat Leuven" for $<value>

    Examples:
      | value |
      | 30    |

  # logging-11
  Scenario Outline: the log records a mortgage being lifted, including interest paid
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog" has $100 to spend
    When pawn "dog" lifts the mortgage on "Diestsestraat Leuven"
    Then the game log records that pawn "dog" lifts the mortgage on "Diestsestraat Leuven" for $<total> including $<interest> interest

    Examples:
      | total | interest |
      | 33    | 3        |

  # logging-12
  Scenario Outline: the log records land sold between players
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game log records that pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<expected_price>

    Examples:
      | price | expected_price |
      | 90    | 90             |

  # logging-13
  Scenario Outline: the log records a sale refused because the colour group has houses built
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game log records that pawn "dog" is refused selling "Diestsestraat Leuven" to pawn "high hat" for $<expected_price> because the colour group has houses built

    Examples:
      | price | expected_price |
      | 90    | 90              |

  # logging-14
  Scenario Outline: the log records a build refused because a street in the colour group is mortgaged
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" will build a house on "Diestsestraat Leuven"
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game log records that pawn "dog" is refused building a house on "Diestsestraat Leuven" for $<cost> because a street in the colour group is mortgaged

    Examples:
      | strategy | cost |
      | Greedo | 50 |
      | Billionaire | 50 |

  # logging-15
  Scenario Outline: the log records a card drawn before the effect it resolves
    Given the next chance card will be "Boete voor te snel rijden. Betaal M15."
    When pawn "dog" lands on "Kans / Chance"
    Then the game log records that pawn "dog" draws the chance card "Boete voor te snel rijden. Betaal M15." before it records that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 15     |

  # logging-16
  Scenario Outline: the log records a tax payment after the landing movement
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "dog" moves before it records that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 200    |

  # logging-17
  Scenario Outline: the log records jail entry and its cause
    When pawn "dog" lands on "<space>"
    Then the game log records that pawn "dog" moves before it records that pawn "dog" is sent to jail from landing on "<space>"

    Examples:
      | space                                 |
      | Naar de Gevangenis / Allez en Prison   |

  # logging-18
  Scenario Outline: the log records jail exit and its method
    Given pawn "dog" starts in jail
    And pawn "dog" will pay the fine to leave jail
    And pawn "dog" will roll 4 and 6 for their turn
    When we play the game
    Then the game log records that pawn "dog" leaves jail by paying the $<fine> fine

    Examples:
      | fine |
      | 50   |

  # logging-19
  Scenario Outline: the log records landing on Free Parking even though nothing happens
    When pawn "dog" lands on "Gratis Parkeren / Parc Gratuit"
    Then the game log records that pawn "dog" moves from position <start position> (<start space>) to <position> (<space>)

    Examples:
      | start position | start space                            | position | space                           |
      | 17              | Algemeen Fonds / Caisse de Communauté | 20       | Gratis Parkeren / Parc Gratuit  |

  # logging-20
  Scenario Outline: the log records a bankruptcy to the bank
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "dog" goes bankrupt to the bank

    Examples:
      | starting balance |
      | 5                 |

  # logging-21
  Scenario Outline: the log records a bankruptcy to another player
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "dog" goes bankrupt to pawn "high hat"

    Examples:
      | starting balance |
      | 5                 |

  # logging-22
  Scenario Outline: the log records the game's winner
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "high hat" wins the game

    Examples:
      | starting balance |
      | 5                 |

  # logging-23
  Scenario Outline: the log records a card drawn before it pays every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next chance card will be "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game log records that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it records that pawn "dog" pays pawn "high hat" $<amount>
    And the game log records that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it records that pawn "dog" pays pawn "iron box" $<amount>

    Examples:
      | amount |
      | 50     |

  # logging-24
  Scenario Outline: the log records a card drawn before it collects from every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next community chest card will be "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler."
    When pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    Then the game log records that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it records that pawn "high hat" pays pawn "dog" $<amount>
    And the game log records that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it records that pawn "iron box" pays pawn "dog" $<amount>

    Examples:
      | amount |
      | 10     |

  # logging-25
  Scenario Outline: the log records a card drawn before the move it causes
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    When pawn "dog" lands on "Kans / Chance"
    Then the game log records that pawn "dog" draws the chance card "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200." before it records that pawn "dog" moves from position <chance_position> (<chance_space>) to <destination_position> (<destination_space>)

    Examples:
      | chance_position | chance_space  | destination_position | destination_space     |
      | 7                | Kans / Chance | 11                    | Rue de Diekirch Arlon |

  # logging-26
  Scenario Outline: the log records a mortgage forced by an unaffordable debt
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $70 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game log records that pawn "dog" mortgages "Rue Grande Dinant" for $<value>

    Examples:
      | value |
      | 30    |

  # logging-27
  Scenario Outline: the log records a house sale forced by an unaffordable debt
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    And the street "Diestsestraat Leuven" has 1 house(s) built
    And pawn "dog" has $80 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game log records that pawn "dog" sells a house on "Rue Grande Dinant" for $<price>

    Examples:
      | price |
      | 25    |

  # logging-28
  Scenario Outline: the log records a jailed player staying in jail after failing to roll doubles
    Given pawn "dog" starts in jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game log records that pawn "dog" stays in jail

    Examples:
      | first_die | second_die |
      | 4         | 6          |
      | 2         | 5          |

  # logging-29
  Scenario Outline: the log records a jailed player leaving jail by rolling doubles
    Given pawn "dog" starts in jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game log records that pawn "dog" leaves jail by rolling doubles

    Examples:
      | first_die | second_die |
      | 3         | 3          |
      | 5         | 5          |

  # logging-30
  Scenario Outline: the log records a jailed player leaving jail with a Get Out of Jail Free card
    Given pawn "dog" starts in jail
    And pawn "dog" already holds a Get Out of Jail Free card
    And pawn "dog" will use the Get Out of Jail Free card to leave jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game log records that pawn "dog" leaves jail using the Get Out of Jail Free card

    Examples:
      | first_die | second_die |
      | 4         | 6          |
      | 3         | 3          |

  # logging-31
  Scenario Outline: the log records why a player declines to buy land they cannot afford
    Given pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then the game log records that pawn "dog" declines to buy "<property>" because it cannot afford the $<price> price

    Examples:
      | strategy | property | dog_starting_balance | high_hat_bid | price |
      | Greedo | Diestsestraat Leuven | 59 | 60 | 60 |
      | Billionaire | Diestsestraat Leuven | 59 | 60 | 60 |

  # logging-32
  Scenario Outline: the log records why a player keeping a reserve declines a purchase that would dip below it
    Given pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then the game log records that pawn "dog" declines to buy "<property>" because it would drop the balance below the $<reserve> reserve

    Examples:
      | strategy | property | dog_starting_balance | reserve | high_hat_bid |
      | Greedo | Rue Grande Dinant | 150 | 96 | 60 |
      | Billionaire | Rue Grande Dinant | 150 | 96 | 60 |

  # logging-33
  Scenario Outline: the log records a player's reserve alongside their balance at the start of a turn
    Given pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game log records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | strategy | dog_starting_balance | reserve |
      | Greedo | 1500 | 0 |
      | Billionaire | 1500 | 0 |
      | Greedo | 1500 | 100 |
      | Billionaire | 1500 | 100 |

  # logging-34
  Scenario Outline: the log records why a player declines to buy a card-driven property they cannot afford
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Kans / Chance"
    Then the game log records that pawn "dog" declines to buy "Rue de Diekirch Arlon" because it cannot afford the $<price> price

    Examples:
      | strategy | dog_starting_balance | price |
      | Greedo | 100 | 140 |
      | Billionaire | 100 | 140 |

  # logging-35
  Scenario Outline: the log records why a player keeping a reserve declines a card-driven purchase that would dip below it
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    And pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Kans / Chance"
    Then the game log records that pawn "dog" declines to buy "Rue de Diekirch Arlon" because it would drop the balance below the $<reserve> reserve

    Examples:
      | strategy | dog_starting_balance | reserve |
      | Greedo | 200 | 65 |
      | Billionaire | 200 | 65 |

  # logging-36
  Scenario Outline: the log records a bank-forced auction win during another player's bankruptcy
    Given pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" has $5 to spend
    And pawn "high hat" will bid $<bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "high hat" wins the auction for "Diestsestraat Leuven" at $<price>

    Examples:
      | bid | price |
      | 35  | 30    |

  # logging-37
  Scenario Outline: the log records land inherited by a creditor when a debtor goes bankrupt to them
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" will claim rent for "Diestsestraat Leuven"
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "high hat" inherits "Rue Grande Dinant" from pawn "dog"

    Examples:
      | starting_balance |
      | 10                |

  # logging-38
  Scenario Outline: the log records a creditor paying interest to keep an inherited mortgage in place
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<owner_starting_balance> to spend
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "high hat" pays $<interest> interest to keep the mortgage on "Rue Grande Dinant"

    Examples:
      | strategy | owner_starting_balance | starting_balance | interest |
      | Greedo | 0 | 2 | 3 |
      | Billionaire | 0 | 2 | 3 |

  # logging-39
  Scenario Outline: the log records a creditor immediately lifting an inherited mortgage
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "high hat" lifts the mortgage on "Rue Grande Dinant" for $<total> including $<interest> interest

    Examples:
      | strategy | starting_balance | total | interest |
      | Greedo | 10 | 33 | 3 |
      | Billionaire | 10 | 33 | 3 |

  # logging-40
  Scenario Outline: the log records a decline with no reason when the strategy has no buying policy
    Given pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "dog" declines to buy "Diestsestraat Leuven"

    Examples:
      | dog_starting_balance |
      | 100                   |

  # logging-41
  Scenario Outline: the log records the reserve dynamically sized for a near-complete colour monopoly at the start of a turn
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game log records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | strategy | dog_starting_balance | reserve |
      | Greedo | 1500 | 60 |
      | Billionaire | 1500 | 60 |

  # logging-42
  Scenario Outline: the log records a debtor putting a property up for sale and the sole buyer's winning offer
    Given pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game log records that pawn "dog" puts "Lippenslaan Knokke" up for sale to avoid bankruptcy
    And the game log records that pawn "high hat" offers $<expected_bid> for "Lippenslaan Knokke"
    And the game log records that pawn "high hat" wins the distressed sale for "Lippenslaan Knokke" at $<expected_bid>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_bid |
      | Greedo | 0 | 200 | 100 |
      | Billionaire | 0 | 200 | 100 |

  # logging-43
  Scenario Outline: the log records every $5 raise in a bidding war before the winning offer
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
    Then the game log records that pawn "high hat" offers $90 for "Lippenslaan Knokke"
    And the game log records that pawn "iron box" offers $95 for "Lippenslaan Knokke"
    And the game log records that pawn "high hat" offers $100 for "Lippenslaan Knokke"
    And the game log records that pawn "iron box" offers $105 for "Lippenslaan Knokke"
    And the game log records that pawn "high hat" offers $90 for "Lippenslaan Knokke" before it records that pawn "iron box" offers $95 for "Lippenslaan Knokke"
    And the game log records that pawn "iron box" offers $95 for "Lippenslaan Knokke" before it records that pawn "high hat" offers $100 for "Lippenslaan Knokke"
    And the game log records that pawn "high hat" offers $100 for "Lippenslaan Knokke" before it records that pawn "iron box" offers $105 for "Lippenslaan Knokke"
    And the game log records that pawn "iron box" offers $105 for "Lippenslaan Knokke" before it records that pawn "iron box" wins the distressed sale for "Lippenslaan Knokke" at $105
    And the game log records that pawn "iron box" wins the distressed sale for "Lippenslaan Knokke" at $105

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | iron_box_starting_balance |
      | Greedo | 0 | 100 | 320 |
      | Billionaire | 0 | 100 | 320 |

  # logging-44
  Scenario Outline: the log records a near-complete colour group's reserve only while its missing street remains affordable
    Given pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game log records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | strategy | dog_starting_balance | reserve |
      | Greedo | 200 | 160 |
      | Billionaire | 200 | 160 |
      | Greedo | 100 | 0 |
      | Billionaire | 100 | 0 |

  # logging-45
  Scenario Outline: the log records a card drawn before the bank pays the player directly
    Given the next chance card will be "De bank betaald je een dividend van M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game log records that pawn "dog" draws the chance card "De bank betaald je een dividend van M50." before it records that pawn "dog" receives $<amount> from the bank

    Examples:
      | amount |
      | 50     |

  # logging-46
  Scenario Outline: the log records that the game ends in a stalemate once every remaining player clears the threshold
    Given we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play the game
    Then the game log records that the game ends in a stalemate before it records that pawn "dog"'s final balance is $<dog_balance>
    And the game log records that pawn "dog"'s final balance is $<dog_balance> before it records that pawn "high hat"'s final balance is $<high_hat_balance>

    Examples:
      | dog_balance | high_hat_balance |
      | 25000       | 26000             |

  # logging-47
  Scenario Outline: the log records that no one bids before it records the resulting mortgage
    Given pawn "high hat" follows the "<strategy>" strategy, keeping a $<high_hat_reserve> reserve
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game log records that pawn "dog" puts Lippenslaan Knokke up for sale to avoid bankruptcy before it records that pawn "dog" finds no bidder for Lippenslaan Knokke
    And the game log records that pawn "dog" finds no bidder for Lippenslaan Knokke before it records that pawn "dog" mortgages Lippenslaan Knokke for $<mortgage_value>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | high_hat_reserve | mortgage_value |
      | Greedo | 10 | 95 | 85 | 90 |
      | Billionaire | 10 | 95 | 85 | 90 |

  # logging-48
  Scenario Outline: the log records a peer trade completed at the start of a turn, once stalemate trading is enabled for the "Greedo" strategy and the whole board is owned
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game log records that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>" before it records that pawn "dog" starts a turn
    And pawn "dog" owns "<street_dog_now_owns>"
    And pawn "high hat" owns "<street_high_hat_now_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_now_owns   | street_high_hat_now_owns                       |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  |

  # logging-49
  Scenario Outline: the log does not record a peer trade when stalemate trading is not enabled for the "Greedo" strategy, even though the whole board is owned
    Given every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game log does not record that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_still_owns                          | street_high_hat_still_owns |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven       |

  # logging-50
  Scenario Outline: the log does not record a peer trade while the board still has unowned space, even though stalemate trading is enabled for the "Greedo" strategy
    Given stalemate trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "high hat" owns "Meir Antwerpen"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game log does not record that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered                                    | dog_wanted            | street_dog_still_owns                          | street_high_hat_still_owns |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven  | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | Diestsestraat Leuven       |

  # logging-51
  Scenario Outline: the log records that stalemate trading is enabled, near the start of the game
    Given stalemate trading is enabled for the "Greedo" strategy
    When we play the game
    Then the game log records that stalemate trading is <state>

    Examples:
      | state   |
      | enabled |

  # logging-52
  Scenario Outline: the log records that stalemate trading is disabled by default, near the start of the game
    When we play the game
    Then the game log records that stalemate trading is <state>

    Examples:
      | state    |
      | disabled |

  # logging-53
  Scenario Outline: the log resolves a split monopoly buyout at the start of a turn, once the whole board is owned
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $1000 to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $100 to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play up to 1 round
    Then the game log records that pawn "dog" wins the split monopoly before it records that pawn "dog" starts a turn
    And the game log records that pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | price |
      | 40    |

  # logging-54
  Scenario Outline: the log does not record a peer trade that would only benefit the trader, not the partner
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned alternately by pawn "dog" and pawn "high hat" in board order
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "<dog_offered>"
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" owns "<dog_wanted>"
    And pawn "dog" will roll 2 and 3 for their turn
    When we play up to 1 round
    Then the game log does not record that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And pawn "dog" owns "<street_dog_still_owns>"
    And pawn "high hat" owns "<street_high_hat_still_owns>"

    Examples:
      | dog_offered    | dog_wanted           | street_dog_still_owns | street_high_hat_still_owns |
      | Meir Antwerpen | Diestsestraat Leuven | Meir Antwerpen        | Diestsestraat Leuven       |

  # logging-55
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
    Then the game log records that pawn "dog" wins the split monopoly
    And the game log records that pawn "dog" pays pawn "high hat" $<price> for the split monopoly
    And pawn "dog" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | price |
      | 40    |

  # logging-56
  Scenario Outline: peer trading never touches a colour group that is a genuine two-owner split, even while the buyout cannot yet afford it
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And pawn "high hat" has $<high_hat_balance> to spend
    When we play up to 5 rounds
    Then the game log does not record that pawn "dog" trades "Meir Antwerpen" to pawn "high hat" for "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"
    And the game log does not record that pawn "high hat" trades "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)" to pawn "dog" for "Meir Antwerpen"
    And pawn "high hat" does not own "Meir Antwerpen"
    And pawn "dog" does not own "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"

    Examples:
      | dog_balance | high_hat_balance |
      | 114         | 50                |

  # logging-57
  Scenario Outline: the majority owner within a split colour group wins the buyout during real play, even when poorer
    Given stalemate trading is enabled for the "Greedo" strategy
    And every other ownable space is owned by pawn "high hat"
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" owns "Place Verte Verviers"
    When we play up to 3 rounds
    Then the game log records that pawn "dog" wins the split monopoly
    And pawn "dog" owns "Place Verte Verviers"

    Examples:
      | dog_balance |
      | 100          |

  # logging-58
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
    Then the game log records that pawn "dog" trades "<dog_offered>" to pawn "high hat" for "<dog_wanted>"
    And the game log does not record that pawn "dog" wins the split monopoly
    And the game log does not record that pawn "high hat" wins the split monopoly
    And pawn "dog" owns "<street_dog_now_owns>"
    And pawn "high hat" owns "<street_high_hat_now_owns>"

    Examples:
      | dog_offered              | dog_wanted            | street_dog_now_owns   | street_high_hat_now_owns |
      | Boulevard Tirou Charleroi | Place Verte Verviers  | Place Verte Verviers  | Boulevard Tirou Charleroi |

  # logging-59
  Scenario Outline: the log records a player's age increasing after passing start
    Given pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And pawn "dog" will roll 1 and 2 for their turn
    When we play up to 2 rounds
    Then the game log records that pawn "dog" starts a turn aged <starting_age> years before it records that pawn "dog" collects a salary of $<dog_salary>
    And the game log records that pawn "dog" collects a salary of $<dog_salary> before it records that pawn "dog" starts a turn aged <age_after_passing_start> years

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | starting_age | age_after_passing_start |
      | 29                  | 5         | 6         | 200        | 0            | 1                        |

  # logging-60
  Scenario Outline: the log records a player's age increasing after being sent to jail
    Given pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And pawn "dog" will roll 1 and 2 for their turn
    When we play up to 2 rounds
    Then the game log records that pawn "dog" starts a turn aged <starting_age> years before it records that pawn "dog" is sent to jail from landing on "<space>"
    And the game log records that pawn "dog" is sent to jail from landing on "<space>" before it records that pawn "dog" starts a turn aged <age_after_jailed> years

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | space                                 | starting_age | age_after_jailed |
      | 27                  | 1         | 2         | Naar de Gevangenis / Allez en Prison  | 0            | 1                 |

  # logging-61
  Scenario Outline: the log records each remaining player's final age once the game ends in a stalemate
    Given pawn "dog" starts at position 37
    And pawn "dog" will roll 1 and 2 for their turn
    And pawn "dog"'s account holds $<dog_starting_account>
    And pawn "high hat"'s account holds $<high_hat_starting_account>
    When we play the game
    Then the game log records that the game ends in a stalemate before it records that pawn "dog"'s final balance is $<dog_final_balance>
    And the game log records that pawn "dog"'s final balance is $<dog_final_balance> before it records that pawn "dog"'s final age is <dog_final_age> years
    And the game log records that pawn "dog"'s final age is <dog_final_age> years before it records that pawn "high hat"'s final balance is $<high_hat_final_balance>
    And the game log records that pawn "high hat"'s final balance is $<high_hat_final_balance> before it records that pawn "high hat"'s final age is <high_hat_final_age> years

    Examples:
      | dog_starting_account | dog_final_balance | high_hat_starting_account | high_hat_final_balance | dog_final_age | high_hat_final_age |
      | 24800                 | 25000              | 26000                      | 26000                   | 1              | 0                   |

  # logging-62
  Scenario Outline: the log records the winner's final age once the game ends in an ordinary win
    Given pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "high hat" wins the game before it records that pawn "high hat"'s final age is <high_hat_final_age> years

    Examples:
      | starting_balance | high_hat_final_age |
      | 5                  | 0                   |

  # logging-63
  Scenario Outline: the log records that <entity_name> is formed, held in equal thirds by the three co-owners
    Given we select 4 players
    And pawn "iron box" will roll 10 for initiative
    And pawn "dog" will roll 7 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "racecar" will roll 2 for initiative
    And every other player can complete their turn
    And legal-entity trading is enabled for the "Greedo" strategy
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "iron box" owns "Place Verte Verviers"
    And every other ownable space is owned by pawn "racecar"
    When we play up to 1 round
    Then the game log records that <entity_name> is formed, held in equal thirds by pawn "dog", pawn "high hat", and pawn "iron box"

    Examples:
      | entity_name |
      | Pink Realty |

  # logging-64
  Scenario Outline: the log records that <entity_name> raises a loan to fund a build shortfall
    Given <entity_name> is formed
    And <entity_name>'s bank account holds $<rent>
    And each shareholder commits $<commitment> toward Pink Realty's build
    When we play up to 1 round
    Then the game log records that <entity_name> raises a loan of $<shortfall> from pawn "dog", pawn "high hat", and pawn "iron box"

    Examples:
      | entity_name | rent | shortfall | commitment |
      | Pink Realty  | 50   | 50        | 25         |

  # logging-65
  Scenario Outline: the log records that <entity_name> repays a shareholder loan
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And <entity_name> owes pawn "dog" $100
    And <entity_name>'s bank account holds $105
    When we play up to 1 round
    Then the game log records that <entity_name> repays pawn "dog" $105 for the loan

    Examples:
      | entity_name |
      | Pink Realty |

  # logging-66
  Scenario Outline: the log records an equal dividend paid by <entity_name> to each shareholder
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And <entity_name>'s bank account holds $150
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game log records that <entity_name> pays each of pawn "dog", pawn "high hat", and pawn "iron box" an equal dividend

    Examples:
      | entity_name |
      | Pink Realty |

  # logging-67
  Scenario Outline: the log records that pawn "<renter>" pays rent to <entity_name> for an entity-owned street
    Given we select 4 players
    And <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And pawn "<renter>" starts at position <renter_position>
    And pawn "<renter>" will claim rent for "<renter_street>"
    When pawn "<renter>" lands on "<renter_street>"
    Then the game log records that pawn "<renter>" pays $<rent> rent to <entity_name> for "<renter_street>"

    Examples:
      | entity_name | renter  | renter_position | renter_street   | rent |
      | Pink Realty  | racecar | 3               | Bruul Mechelen  | 625  |

  # logging-68
  Scenario Outline: the log records that <entity_name> builds a house on a street when its treasury can pay for it
    Given <entity_name> is formed
    And the street "Rue de Diekirch Arlon" has 4 houses built
    And the street "Bruul Mechelen" has 4 houses built
    And the street "Place Verte Verviers" has 4 houses built
    And <entity_name>'s bank account holds $<treasury>
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game log records that <entity_name> builds a house on "<street>" for $100

    Examples:
      | entity_name | treasury | street             |
      | Pink Realty  | 100      | Rue de Diekirch Arlon |

  # logging-69
  Scenario Outline: the log records that <entity_name> raises a loan and builds a house on a street when its treasury cannot pay for it
    Given <entity_name> is formed
    And <entity_name>'s bank account holds $<rent>
    And each shareholder commits $<commitment> toward Pink Realty's build
    And pawn "dog" will roll 12 for their turn
    When we play up to 1 round
    Then the game log records that <entity_name> raises a loan of $<shortfall> from pawn "dog", pawn "high hat", and pawn "iron box"
    And the game log records that <entity_name> builds a house on "<street>" for $100

    Examples:
      | entity_name | rent | shortfall | commitment | street             |
      | Pink Realty  | 50   | 50        | 25         | Rue de Diekirch Arlon |

  # logging-70
  Scenario Outline: the log records each remaining player's final balance and age once the game ends because the year limit was reached
    Given the game is limited to <year limit> years
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "dog" starts at position 35
    And pawn "dog" will roll 5 for their turn
    And pawn "dog"'s account holds $<dog_starting_account>
    And pawn "high hat"'s account holds $<high_hat_starting_account>
    When we play the game
    Then the game log records that the game ends because the year limit was reached before it records that pawn "dog"'s final balance is $<dog_final_balance>
    And the game log records that pawn "dog"'s final balance is $<dog_final_balance> before it records that pawn "dog"'s final age is <dog_final_age> years
    And the game log records that pawn "dog"'s final age is <dog_final_age> years before it records that pawn "high hat"'s final balance is $<high_hat_final_balance>
    And the game log records that pawn "high hat"'s final balance is $<high_hat_final_balance> before it records that pawn "high hat"'s final age is <high_hat_final_age> years

    Examples:
      | year limit | dog_starting_account | dog_final_balance | dog_final_age | high_hat_starting_account | high_hat_final_balance | high_hat_final_age |
      | 1          | 1500                  | 1700               | 1              | 1500                       | 1500                    | 0                   |
  # logging-71
  Scenario Outline: the log records a player raising a development loan
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<cash> to spend
    And pawn "high hat" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the game log records that pawn "dog" raises a development loan of $<loan> from the bank, secured by "Rue Grande Dinant", funded by pawn "high hat"'s bond purchase

    Examples:
      | cash | loan |
      | 30   | 20   |

  # logging-72
  Scenario Outline: the log records a legal entity raising a development loan
    Given the official rule set
    And we select 4 players
    And Pink Realty is formed
    And Pink Realty's bank account holds $<bank_funds>
    And pawn "dog" has a balance that allows only $0 toward the entity
    And pawn "high hat" has a balance that allows only $0 toward the entity
    And pawn "iron box" has a balance that allows only $0 toward the entity
    And pawn "racecar" has $500 to spend
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the game log records that Pink Realty raises a development loan of $<loan> from the bank, secured by "Rue de Diekirch Arlon", funded by pawn "racecar"'s bond purchase

    Examples:
      | bank_funds | loan |
      | 60         | 40   |

  # logging-73
  Scenario Outline: the log records a player's annual development-loan payment split into interest and principal
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "dog" has $<cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then the game log records that pawn "dog" pays the bank $<interest> interest and $<principal_payment> principal on the development loan secured by "Rue Grande Dinant"

    Examples:
      | principal | cash | interest | principal_payment |
      | 20        | 100  | 1        | 1                  |

  # logging-74
  Scenario Outline: the log records a legal entity's annual development-loan payment split into interest and principal
    Given the official rule set
    And we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<bank_funds>
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the game log records that Pink Realty pays the bank $<interest> interest and $<principal_payment> principal on the development loan secured by "Rue de Diekirch Arlon"

    Examples:
      | principal | bank_funds | interest | principal_payment |
      | 40        | 100        | 2        | 2                  |

  # logging-75
  Scenario Outline: the log records a player's development loan being fully repaid
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "dog" has $<cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then the game log records that pawn "dog"'s development loan on "Rue Grande Dinant" has been fully repaid

    Examples:
      | principal | cash |
      | 1         | 100  |

  # logging-76
  Scenario Outline: the log records a legal entity's development loan being fully repaid
    Given the official rule set
    And we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<bank_funds>
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the game log records that Pink Realty's development loan on "Rue de Diekirch Arlon" has been fully repaid

    Examples:
      | principal | bank_funds |
      | 1         | 100        |

  # logging-77
  Scenario Outline: the log records a player defaulting on a development loan and the bank foreclosing
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And the street "Rue Grande Dinant" has 1 house(s) built
    And pawn "dog" has $0 to spend
    And pawn "high hat" will bid $<bid> for "Rue Grande Dinant" at auction
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then the game log records that pawn "dog" defaults on the development loan secured by "Rue Grande Dinant"; the bank forecloses

    Examples:
      | principal | bid | interest |
      | 20        | 30  | 0.60     |

  # logging-78
  Scenario Outline: the log records a legal entity defaulting on a development loan and the bank foreclosing
    Given the official rule set
    And we select 4 players
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
    Then the game log records that Pink Realty defaults on the development loan secured by "Rue de Diekirch Arlon"; the bank forecloses

    Examples:
      | principal | bid | interest |
      | 40        | 25  | 1.20     |
  # logging-79
  Scenario Outline: the log records a bondholder's annual payout on a player's development loan
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" owes the bank $<principal> on a development loan secured by "Rue Grande Dinant"
    And pawn "high hat" holds the development loan bond secured by "Rue Grande Dinant"
    And pawn "dog" has $<cash> to spend
    And pawn "high hat" has $<bond_cash> to spend
    And development loans are enabled for the "Greedo" strategy
    When pawn "dog" grows a year older
    Then the game log records that pawn "high hat" receives $<yield> interest and $<principal_payment> principal on the development loan bond secured by "Rue Grande Dinant"

    Examples:
      | principal | cash | bond_cash | yield | principal_payment |
      | 100       | 200  | 500       | 3     | 5                  |

  # logging-80
  Scenario Outline: the log records a bondholder's annual payout on a legal entity's development loan
    Given the official rule set
    And we select 4 players
    And Pink Realty is formed
    And Pink Realty owns no outstanding loan
    And Pink Realty owes the bank $<principal> on a development loan secured by "Rue de Diekirch Arlon"
    And pawn "racecar" holds the development loan bond secured by "Rue de Diekirch Arlon"
    And Pink Realty's bank account holds $<bank_funds>
    And the last-capitalised shareholder of Pink Realty grows a year older
    And development loans are enabled for the "Greedo" strategy
    When we play up to 1 round
    Then the game log records that pawn "racecar" receives $<yield> interest and $<principal_payment> principal on the development loan bond secured by "Rue de Diekirch Arlon"

    Examples:
      | principal | bank_funds | yield | principal_payment |
      | 100       | 200        | 3     | 5                  |

  # logging-81
  Scenario Outline: the log records the bank recovering a player's foreclosure proceeds into its own account, rather than paying the bondholder out
    Given the official rule set
    And we select 3 players
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
    Then the game log records that the bank recovers $<recovered> from the foreclosure of "Rue Grande Dinant", added to its own account

    Examples:
      | principal | bond_cash | bid | recovered |
      | 20        | 500       | 30  | 21        |

  # logging-82
  Scenario Outline: the log records the bank recovering a legal entity's foreclosure proceeds into its own account, rather than paying the bondholder out
    Given the official rule set
    And we select 5 players
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
    Then the game log records that the bank recovers $<recovered> from the foreclosure of "Rue de Diekirch Arlon", added to its own account

    Examples:
      | principal | bond_cash | bid | recovered |
      | 40        | 500       | 25  | 42        |

  # logging-83
  Scenario Outline: the log records that development loans are enabled, near the start of the game
    Given development loans are enabled for the "Greedo" strategy
    When we play the game
    Then the game log records that development loans are <state>

    Examples:
      | state   |
      | enabled |

  # logging-84
  Scenario Outline: the log records that development loans are disabled by default, near the start of the game
    When we play the game
    Then the game log records that development loans are <state>

    Examples:
      | state    |
      | disabled |

  # logging-85
  Scenario Outline: the log records a player's war profits tax payment
    Given the war profits tax is enabled
    And pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    Then the game log records that pawn "dog" pays the government a war profits tax of $<tax>

    Examples:
      | land_value | collected | tax  |
      | 6000       | 1000      | 1000 |

  # logging-86
  Scenario Outline: the log records the government's final account balance once the game ends
    Given the war profits tax is enabled
    And pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    And pawn "dog" grows a year older
    When we play up to 1 round
    Then the game log records that the government's final account balance is $<balance>

    Examples:
      | land_value | collected | balance |
      | 6000       | 1000      | 1000    |

  # logging-89
  Scenario Outline: the log records the government's final account balance once an ordinary win ends the game too
    Given the war profits tax is enabled
    And pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    And pawn "dog" grows a year older
    And pawn "high hat" has $<high hat balance> to spend
    When pawn "high hat" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "dog" wins the game
    And the game log records that the government's final account balance is $<balance>

    Examples:
      | land_value | collected | high hat balance | balance |
      | 6000       | 1000      | 5                 | 1000    |

  # logging-87
  Scenario Outline: the log records that the war profits tax is enabled, near the start of the game
    Given the war profits tax is enabled
    When we play the game
    Then the game log records that the war profits tax is <state>

    Examples:
      | state   |
      | enabled |

  # logging-88
  Scenario Outline: the log records that the war profits tax is disabled by default, near the start of the game
    When we play the game
    Then the game log records that the war profits tax is <state>

    Examples:
      | state    |
      | disabled |
