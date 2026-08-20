package the.monopoly.game;

import the.monopoly.game.Game.Journal;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Bankruptcy;
import the.monopoly.game.rules.Building;
import the.monopoly.game.rules.Cards;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Jail;
import the.monopoly.game.rules.LandSale;
import the.monopoly.game.rules.LegalEntity;
import the.monopoly.game.rules.Rent;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Taxes;
import the.monopoly.game.rules.Turn;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Map;

/** Writes down what a turn and a sale say they did, as the game's account of it. */
record Journalling(Journal journal, Map<Player.ID, Integer> ages, Deeds deeds,
                   the.monopoly.game.rules.DevelopmentLoanBook developmentLoanBook,
                   Rule.Set rules, List<Player> players, Strategy.OfPlayers strategies,
                   the.monopoly.game.rules.WarProfitsTaxBook warProfitsTaxBook, boolean warProfitsTax)
    implements Turn.Events, LandSale.Events, Rent.Events, Building.Events, Cards.Events, Taxes.Events, Jail.Events, Bankruptcy.Events {
  int age(Player player) {
    return ages.getOrDefault(player.id(), 0);
  }

  private void ageAfter(Player player) {
    ages.merge(player.id(), 1, Integer::sum);
  }

  @Override
  public void rolled(Player player, Roll roll) {
    journal.log(new Journal.Entry.Rolled(player.id(), roll.total()));
  }

  @Override
  public void moved(Player player, int from, int to, Street.Type fromSpace, Street.Type toSpace) {
    journal.log(new Journal.Entry.Moved(player.id(), from, to, fromSpace, toSpace));
  }

  @Override
  public void collectedSalary(Player player, Money salary) {
    ageAfter(player);
    deeds.legalEntities().forEach(entity -> entity.shareholderGrewOlder(player));
    journal.log(new Journal.Entry.SalaryCollected(player.id(), salary));
    assessWarProfitsTax(player);
    developmentLoanBook.positions().stream()
        .filter(position -> position.borrower() != null && position.borrower().id().equals(player.id()))
        .forEach(this::serviceDevelopmentLoan);
  }

  private void assessWarProfitsTax(Player player) {
    if (!warProfitsTax) return;
    Money owed = warProfitsTaxBook.assess(player,
        the.monopoly.game.rules.WarProfitsTax.landValue(rules, deeds, player),
        shortfall -> mortgageWarProfitsTaxCollateral(player, shortfall));
    if (!owed.equals(Money.ZERO)) journal.log(new Journal.Entry.WarProfitsTaxPaid(player.id(), owed));
  }

  private void mortgageWarProfitsTaxCollateral(Player player, Money shortfall) {
    for (Street.Type type : rules.gameboard().layout()) {
      if (deeds.landOwnedBy(player).contains(type)) {
        Ownable land = (Ownable) rules.create(type);
        if (!deeds.isMortgaged(land)) {
          deeds.mortgage(land, player);
          if (player.account().balance().amount().covers(shortfall)) return;
        }
      }
    }
  }

  private void serviceDevelopmentLoan(the.monopoly.game.rules.DevelopmentLoanBook.Position position) {
    java.util.Optional<the.monopoly.game.rules.DevelopmentLoanBook.Payment> payment =
        developmentLoanBook.service(position);
    if (payment.isPresent()) {
      serviceDevelopmentLoan(position, payment.orElseThrow());
      return;
    }
    mortgageSpareProperty(position);
    developmentLoanBook.service(position).ifPresentOrElse(
        value -> serviceDevelopmentLoan(position, value),
        () -> {
          developmentLoanDefaulted(position);
          the.monopoly.game.rules.DevelopmentLoanBook.Foreclosure foreclosure =
              developmentLoanBook.foreclose(position, deeds, rules, players, strategies);
          journal.log(new Journal.Entry.DevelopmentLoanRecovered(position.collateral(), foreclosure.recovered()));
        });
  }

  private void mortgageSpareProperty(the.monopoly.game.rules.DevelopmentLoanBook.Position position) {
    if (position.borrower() == null) return;
    ColourStreet collateral = (ColourStreet) rules.create(position.collateral());
    Money due = developmentLoanBook.paymentDue(position);
    for (Street.Type type : deeds.landOwnedBy(position.borrower())) {
      if (type == position.collateral()) continue;
      Street space = rules.create(type);
      if (space instanceof ColourStreet street && street.colourGroup() == collateral.colourGroup()) continue;
      Ownable land = (Ownable) space;
      if (deeds.isMortgaged(land)) continue;
      deeds.mortgage(land, position.borrower());
      if (position.borrower().account().balance().amount().covers(due)) return;
    }
  }

  void serviceDevelopmentLoan(the.monopoly.game.rules.DevelopmentLoanBook.Position position,
                              the.monopoly.game.rules.DevelopmentLoanBook.Payment payment) {
      if (position.borrower() != null) {
        journal.log(new Journal.Entry.DevelopmentLoanPayment(position.borrower().id(), position.collateral(),
            payment.interest(), payment.principal()));
        if (position.bondholder() != null) journal.log(new Journal.Entry.DevelopmentBondPayment(
            position.bondholder().id(), position.collateral(), payment.bondInterest(), payment.principal()));
        if (position.loan().isRepaid()) journal.log(new Journal.Entry.DevelopmentLoanRepaid(
            position.borrower().id(), position.collateral()));
      } else {
        journal.log(new Journal.Entry.EntityDevelopmentLoanPayment(position.entity().name(), position.collateral(),
            payment.interest(), payment.principal()));
        if (position.bondholder() != null) journal.log(new Journal.Entry.DevelopmentBondPayment(
            position.bondholder().id(), position.collateral(), payment.bondInterest(), payment.principal()));
        if (position.loan().isRepaid()) journal.log(new Journal.Entry.EntityDevelopmentLoanRepaid(
            position.entity().name(), position.collateral()));
      }
  }

  void developmentLoanDefaulted(the.monopoly.game.rules.DevelopmentLoanBook.Position position) {
    if (position.borrower() != null) journal.log(new Journal.Entry.DevelopmentLoanDefaulted(
        position.borrower().id(), position.collateral()));
    else journal.log(new Journal.Entry.EntityDevelopmentLoanDefaulted(position.entity().name(), position.collateral()));
  }

  void developmentLoanRecovered(the.monopoly.game.rules.DevelopmentLoanBook.Position position, Money amount) {
    journal.log(new Journal.Entry.DevelopmentLoanRecovered(position.collateral(), amount));
  }

  @Override
  public void bought(Player buyer, Ownable land, Money price) {
    journal.log(new Journal.Entry.Bought(buyer.id(), land.type(), price));
  }

  @Override
  public void wonAtAuction(Player winner, Ownable land, Money price) {
    journal.log(new Journal.Entry.AuctionWon(winner.id(), land.type(), price));
  }

  @Override
  public void soldHouse(Player player, ColourStreet street, Money price) {
    journal.log(new Journal.Entry.HouseSold(player.id(), street.type(), price));
  }

  @Override
  public void soldToPeer(Player seller, Ownable land, Player buyer, Money price) {
    journal.log(new Journal.Entry.LandSold(seller.id(), land.type(), buyer.id(), price));
  }

  @Override
  public void soldEntityShare(Player seller, LegalEntity entity, Player buyer, Money price) {
    journal.log(new Journal.Entry.LegalEntityShareSold(entity.name(), seller.id(), buyer.id(), price));
  }

  @Override
  public void entityLiquidated(Player recipient, LegalEntity entity, Money amount) {
    journal.log(new Journal.Entry.LegalEntityLiquidated(entity.name(), recipient.id(), amount));
  }

  public void peerTrade(Player trader, Ownable offered, Player partner, Ownable wanted) {
    journal.log(new Journal.Entry.PeerTrade(trader.id(), offered.type(), partner.id(), wanted.type()));
  }

  public void stalemateTrading(boolean enabled) {
    journal.log(new Journal.Entry.StalemateTrading(enabled));
  }

  public void developmentLoans(boolean enabled, boolean fullDraw) {
    journal.log(new Journal.Entry.DevelopmentLoans(enabled, fullDraw));
  }

  @Override
  public void developmentLoanRaised(Player borrower, the.monopoly.game.rules.DevelopmentLoanBook.Position position) {
    journal.log(new Journal.Entry.DevelopmentLoanRaised(borrower.id(), position.collateral(),
        position.loan().originalPrincipal(), position.bondholder() == null ? null : position.bondholder().id()));
  }

  public void strategyNamed(Player player, Strategy strategy) {
    boolean legalEntityEnabled = strategy instanceof Greedo greedo && greedo.legalEntityTradingEnabled();
    boolean stalemateEnabled = strategy instanceof Greedo greedo && greedo.stalemateTradingEnabled();
    String name = strategy == Strategy.UNDECIDED ? "undecided" : strategy.getClass().getSimpleName();
    if (name.isEmpty() && strategy.getClass().getSuperclass() != null)
      name = strategy.getClass().getSuperclass().getSimpleName();
    journal.log(new Journal.Entry.StrategyNamed(player.id(), name,
        legalEntityEnabled, stalemateEnabled, strategy.assetRichOpening()));
  }

  public void splitMonopolyWon(Player winner, Player loser) {
    journal.log(new Journal.Entry.SplitMonopolyWon(winner.id(), loser.id()));
  }

  public void splitMonopolyPaid(Player payer, Player payee, Money amount) {
    journal.log(new Journal.Entry.SplitMonopolyPaid(payer.id(), payee.id(), amount));
  }

  public void entityFormed(LegalEntity entity) {
    journal.log(new Journal.Entry.LegalEntityFormed(entity.name(),
        entity.shareholders().stream().map(Player::id).toList()));
  }

  public void entityLoanRaised(LegalEntity entity, Money amount) {
    journal.log(new Journal.Entry.LegalEntityLoanRaised(entity.name(), amount,
        entity.shareholders().stream().map(Player::id).toList()));
  }

  public void entityDevelopmentLoanRaised(LegalEntity entity, the.monopoly.game.rules.DevelopmentLoanBook.Position position) {
    journal.log(new Journal.Entry.EntityDevelopmentLoanRaised(entity.name(), position.collateral(),
        position.loan().originalPrincipal(), position.bondholder() == null ? null : position.bondholder().id()));
  }

  public void entityLoanRepaid(LegalEntity entity, Player shareholder, Money principal, Money repayment) {
    journal.log(new Journal.Entry.LegalEntityLoanRepaid(entity.name(), shareholder.id(), principal, repayment));
  }

  public void entityDividendPaid(LegalEntity entity, Money amount) {
    journal.log(new Journal.Entry.LegalEntityDividendPaid(entity.name(),
        entity.shareholders().stream().map(Player::id).toList(), amount));
  }

  public void entityHouseBuilt(LegalEntity entity, ColourStreet street) {
    journal.log(new Journal.Entry.LegalEntityHouseBuilt(entity.name(), street.type(), street.houseConstructionCost()));
  }

  @Override
  public void paid(Player tenant, LegalEntity entity, ColourStreet land, Money rent) {
    journal.log(new Journal.Entry.LegalEntityRentPaid(entity.name(), tenant.id(), land.type(), rent));
  }

  @Override
  public void distressedSaleStarted(Player seller, Ownable land) {
    journal.log(new Journal.Entry.DistressedSaleStarted(seller.id(), land.type()));
  }

  @Override
  public void distressedSaleNoBidder(Player seller, Ownable land) {
    journal.log(new Journal.Entry.DistressedSaleNoBidder(seller.id(), land.type()));
  }

  @Override
  public void distressedOffer(Player bidder, Ownable land, Money price) {
    journal.log(new Journal.Entry.DistressedOffer(bidder.id(), land.type(), price));
  }

  @Override
  public void distressedSaleWon(Player bidder, Ownable land, Money price) {
    journal.log(new Journal.Entry.DistressedSaleWon(bidder.id(), land.type(), price));
  }

  @Override
  public void mortgaged(Player player, Ownable land, Money value) {
    journal.log(new Journal.Entry.Mortgaged(player.id(), land.type(), value));
  }

  @Override
  public void inherited(Player creditor, Ownable land, Player debtor) {
    journal.log(new Journal.Entry.Inherited(creditor.id(), land.type(), debtor.id()));
  }

  @Override
  public void keptMortgage(Player player, Ownable land, Money interest) {
    journal.log(new Journal.Entry.MortgageKept(player.id(), land.type(), interest));
  }

  @Override
  public void liftedMortgage(Player player, Ownable land, Deeds.MortgageCost cost) {
    journal.log(new Journal.Entry.MortgageLifted(player.id(), land.type(), cost.total(), cost.interest()));
  }

  @Override
  public void declinedToBuy(Player player, Ownable land, Money price,
                            Strategy.DeclineReason reason, Money reserve) {
    journal.log(new Journal.Entry.PurchaseDeclined(player.id(), land.type(), price, reason, reserve));
  }

  @Override
  public void paid(Player tenant, Player owner, Ownable land, Money rent) {
    journal.log(new Journal.Entry.RentPaid(tenant.id(), owner.id(), land.type(), rent));
    if (warProfitsTax) warProfitsTaxBook.accumulate(owner, rent);
  }

  @Override
  public void paid(Player payer, Player payee, Money amount) {
    journal.log(new Journal.Entry.PlayerPaid(payer.id(), payee.id(), amount));
  }

  @Override
  public void builtHouse(Player player, ColourStreet street, Money price) {
    journal.log(new Journal.Entry.HouseBuilt(player.id(), street.type(), price));
  }

  @Override
  public void sold(Player seller, Ownable land, Player buyer, Money price) {
    journal.log(new Journal.Entry.LandSold(seller.id(), land.type(), buyer.id(), price));
  }

  @Override
  public void saleRefused(Player seller, Ownable land, Player buyer, Money price) {
    journal.log(new Journal.Entry.LandSaleRefused(seller.id(), land.type(), buyer.id(), price));
  }

  @Override
  public void refusedBuilding(Player player, ColourStreet street, Money price) {
    journal.log(new Journal.Entry.BuildingRefused(player.id(), street.type(), price));
  }

  @Override
  public void drewChanceCard(Player player, String card) {
    journal.log(new Journal.Entry.ChanceCardDrawn(player.id(), card));
  }

  @Override
  public void drewCommunityChestCard(Player player, String card) {
    journal.log(new Journal.Entry.CommunityChestCardDrawn(player.id(), card));
  }

  @Override
  public void paidBank(Player player, Money amount) {
    journal.log(new Journal.Entry.BankPaid(player.id(), amount));
  }

  @Override
  public void receivedBank(Player player, Money amount) {
    journal.log(new Journal.Entry.BankReceived(player.id(), amount));
  }

  @Override
  public void sentToJail(Player player, Street.Type cause) {
    ageAfter(player);
    journal.log(new Journal.Entry.JailEntered(player.id(), cause));
  }

  @Override
  public void leftJailByPaying(Player player, Money fine) {
    journal.log(new Journal.Entry.JailFinePaid(player.id(), fine));
  }

  @Override
  public void leftJailWithCard(Player player) {
    journal.log(new Journal.Entry.JailCardUsed(player.id()));
  }

  @Override
  public void leftJailByRollingDoubles(Player player) {
    journal.log(new Journal.Entry.JailDoublesRolled(player.id()));
  }

  @Override
  public void stayedInJail(Player player) {
    journal.log(new Journal.Entry.JailStayed(player.id()));
  }

  @Override
  public void bankrupt(Player debtor, Player creditor) {
    journal.log(new Journal.Entry.Bankrupt(debtor.id(), creditor == null ? null : creditor.id()));
  }

  @Override
  public void won(Player player) {
    journal.log(new Journal.Entry.Won(player.id()));
    journal.log(new Journal.Entry.FinalAge(player.id(), age(player)));
  }
}

/* mutate4java-manifest
version=1
moduleHash=f85f74dc20cf714e6929ecb93980a8f5cf732fb6e94303cde92e8c769a3dd00b
scope.0.id=Y2xhc3M6Sm91cm5hbGxpbmcjSm91cm5hbGxpbmc6Mjg
scope.0.kind=class
scope.0.startLine=28
scope.0.endLine=363
scope.0.semanticHash=d8f4773b9cae769095f2f3aab4a54114a39b3abcbb82fc057b19acad65d3fd26
scope.1.id=ZmllbGQ6Sm91cm5hbGxpbmcjYWdlczoyOA
scope.1.kind=field
scope.1.startLine=28
scope.1.endLine=28
scope.1.semanticHash=2903e7a1268ae9cd26b2357b7ac21e59c98729950e8d7612d89fd04597741325
scope.2.id=ZmllbGQ6Sm91cm5hbGxpbmcjZGVlZHM6Mjg
scope.2.kind=field
scope.2.startLine=28
scope.2.endLine=28
scope.2.semanticHash=4b65d6397764d17be13a804619f7fb1d716ff911b5d080d04d671e6cc868e81c
scope.3.id=ZmllbGQ6Sm91cm5hbGxpbmcjZGV2ZWxvcG1lbnRMb2FuQm9vazoyOQ
scope.3.kind=field
scope.3.startLine=29
scope.3.endLine=29
scope.3.semanticHash=72be1f89171e5b1711cb7f128fc5ffc9d2100c847762db6d623cd291e6a5695e
scope.4.id=ZmllbGQ6Sm91cm5hbGxpbmcjam91cm5hbDoyOA
scope.4.kind=field
scope.4.startLine=28
scope.4.endLine=28
scope.4.semanticHash=85c5d503112071935eec23a4e6e40fde3bfadba06348aca287c46765762c68bf
scope.5.id=ZmllbGQ6Sm91cm5hbGxpbmcjcGxheWVyczozMA
scope.5.kind=field
scope.5.startLine=30
scope.5.endLine=30
scope.5.semanticHash=ae5d2e8c41de41fca338532c751a7a679eafcb486860ea5a70036947cc37c4e0
scope.6.id=ZmllbGQ6Sm91cm5hbGxpbmcjcnVsZXM6MzA
scope.6.kind=field
scope.6.startLine=30
scope.6.endLine=30
scope.6.semanticHash=ed497e01a36cc45680984842a5fb0537b4670b1599b4b4bccf1b2a2f0105dcbb
scope.7.id=ZmllbGQ6Sm91cm5hbGxpbmcjc3RyYXRlZ2llczozMA
scope.7.kind=field
scope.7.startLine=30
scope.7.endLine=30
scope.7.semanticHash=139d9ed4ef2872197ea9eeb6303b99fb59880f5204f7d5ae6e25bf7b49f33d26
scope.8.id=ZmllbGQ6Sm91cm5hbGxpbmcjd2FyUHJvZml0c1RheDozMQ
scope.8.kind=field
scope.8.startLine=31
scope.8.endLine=31
scope.8.semanticHash=51d26289285e719b2748f90aa36a5fa8dd549d50e00035f2db341b86a9cd3837
scope.9.id=ZmllbGQ6Sm91cm5hbGxpbmcjd2FyUHJvZml0c1RheEJvb2s6MzE
scope.9.kind=field
scope.9.startLine=31
scope.9.endLine=31
scope.9.semanticHash=03ca57f431bf139105b6e40f68a7ffa67cfdff0429987f347ad5bb2d4908c274
scope.10.id=bWV0aG9kOkpvdXJuYWxsaW5nI2FnZSgxKTozMw
scope.10.kind=method
scope.10.startLine=33
scope.10.endLine=35
scope.10.semanticHash=df1e6c03fb3f50576906edfca6dc252b69afdb857ffc4813ac8abbba27b3dbab
scope.11.id=bWV0aG9kOkpvdXJuYWxsaW5nI2FnZUFmdGVyKDEpOjM3
scope.11.kind=method
scope.11.startLine=37
scope.11.endLine=39
scope.11.semanticHash=0e91c90c74e548de173114e3102a5520d21d387bd7f829c0ccef1fae9a20b9ad
scope.12.id=bWV0aG9kOkpvdXJuYWxsaW5nI2Fzc2Vzc1dhclByb2ZpdHNUYXgoMSk6NjI
scope.12.kind=method
scope.12.startLine=62
scope.12.endLine=67
scope.12.semanticHash=d8e233f3ff132a601765a51e77a9424475ae191d4ca7e81d95fa6316552d9457
scope.13.id=bWV0aG9kOkpvdXJuYWxsaW5nI2JhbmtydXB0KDIpOjM1Mw
scope.13.kind=method
scope.13.startLine=353
scope.13.endLine=356
scope.13.semanticHash=2b00a29ade23b6b6f6d0c73efa7c5af5dd6c5e364aff4c34df7cbf6d183c4d74
scope.14.id=bWV0aG9kOkpvdXJuYWxsaW5nI2JvdWdodCgzKToxMzE
scope.14.kind=method
scope.14.startLine=131
scope.14.endLine=134
scope.14.semanticHash=719af0dd5380df66d4ab2db5d4952b113337b6dc18f0a0ac889d6a33e5cb8ede
scope.15.id=bWV0aG9kOkpvdXJuYWxsaW5nI2J1aWx0SG91c2UoMyk6Mjg3
scope.15.kind=method
scope.15.startLine=287
scope.15.endLine=290
scope.15.semanticHash=66ad7b1cf7ad6e2f15ffd1f4228e88be1d4fb8ee5e53ff73b2416ba38765175b
scope.16.id=bWV0aG9kOkpvdXJuYWxsaW5nI2NvbGxlY3RlZFNhbGFyeSgyKTo1MQ
scope.16.kind=method
scope.16.startLine=51
scope.16.endLine=60
scope.16.semanticHash=24bd44097d66c65a9279837a97f7c35de47027a0b0b39064bf52870bc13ac647
scope.17.id=bWV0aG9kOkpvdXJuYWxsaW5nI2N0b3IoOSk6Mjg
scope.17.kind=method
scope.17.startLine=1
scope.17.endLine=363
scope.17.semanticHash=d5f9ba5b1b832d8ca575a6e93b0c065d9d97a150e219ee573d355f2e9b4b4cfb
scope.18.id=bWV0aG9kOkpvdXJuYWxsaW5nI2RlY2xpbmVkVG9CdXkoNSk6Mjcw
scope.18.kind=method
scope.18.startLine=270
scope.18.endLine=274
scope.18.semanticHash=9ee4ca600116c11986af59ba574100cba9c434688d4d9298e5218e493bd5ca7f
scope.19.id=bWV0aG9kOkpvdXJuYWxsaW5nI2RldmVsb3BtZW50TG9hbkRlZmF1bHRlZCgxKToxMjE
scope.19.kind=method
scope.19.startLine=121
scope.19.endLine=125
scope.19.semanticHash=fa75f10a0a7e600a0639bb29836f810a70f5e30364d10031ad6c255ef65d9f07
scope.20.id=bWV0aG9kOkpvdXJuYWxsaW5nI2RldmVsb3BtZW50TG9hblJhaXNlZCgyKToxNzM
scope.20.kind=method
scope.20.startLine=173
scope.20.endLine=177
scope.20.semanticHash=eabb4b6f221428033eced04f651335de9a7ae17755c55a494c9219dc129d49c9
scope.21.id=bWV0aG9kOkpvdXJuYWxsaW5nI2RldmVsb3BtZW50TG9hblJlY292ZXJlZCgyKToxMjc
scope.21.kind=method
scope.21.startLine=127
scope.21.endLine=129
scope.21.semanticHash=a47477ddcbd973af01c939a6bb7a992918a9b89d4fcd3bb46e1dc2f57da6a47c
scope.22.id=bWV0aG9kOkpvdXJuYWxsaW5nI2RldmVsb3BtZW50TG9hbnMoMik6MTY5
scope.22.kind=method
scope.22.startLine=169
scope.22.endLine=171
scope.22.semanticHash=17c81ccde9f5a4f754fdbab710463d317188c5df595b547f8dd51ed0f3e9d858
scope.23.id=bWV0aG9kOkpvdXJuYWxsaW5nI2Rpc3RyZXNzZWRPZmZlcigzKToyNDA
scope.23.kind=method
scope.23.startLine=240
scope.23.endLine=243
scope.23.semanticHash=247fab434f079c526de3ba48f6af74528c890687d3574cff04dd1a183cdd684f
scope.24.id=bWV0aG9kOkpvdXJuYWxsaW5nI2Rpc3RyZXNzZWRTYWxlTm9CaWRkZXIoMik6MjM1
scope.24.kind=method
scope.24.startLine=235
scope.24.endLine=238
scope.24.semanticHash=9bf407f4fd0255f4d169bdbcb3fa71f8eddea79b669edec38aab67f8df432a58
scope.25.id=bWV0aG9kOkpvdXJuYWxsaW5nI2Rpc3RyZXNzZWRTYWxlU3RhcnRlZCgyKToyMzA
scope.25.kind=method
scope.25.startLine=230
scope.25.endLine=233
scope.25.semanticHash=d53a35578438854429942863bf90a573b330e432a13ec48ec0f57cc54deccaba
scope.26.id=bWV0aG9kOkpvdXJuYWxsaW5nI2Rpc3RyZXNzZWRTYWxlV29uKDMpOjI0NQ
scope.26.kind=method
scope.26.startLine=245
scope.26.endLine=248
scope.26.semanticHash=1d5a91192ef0fd3164242b1b6f57df924e104e0d8dcce895efffa17a887f44f0
scope.27.id=bWV0aG9kOkpvdXJuYWxsaW5nI2RyZXdDaGFuY2VDYXJkKDIpOjMwNw
scope.27.kind=method
scope.27.startLine=307
scope.27.endLine=310
scope.27.semanticHash=f414efc9de9972185a93e7948bffbd596a8f5dadd5b2e231c69749df3e71922d
scope.28.id=bWV0aG9kOkpvdXJuYWxsaW5nI2RyZXdDb21tdW5pdHlDaGVzdENhcmQoMik6MzEy
scope.28.kind=method
scope.28.startLine=312
scope.28.endLine=315
scope.28.semanticHash=f85f15ced6115b0bfcb7fbbebd4be560eac762d43df7dc1a874667d77ca08f16
scope.29.id=bWV0aG9kOkpvdXJuYWxsaW5nI2VudGl0eURldmVsb3BtZW50TG9hblJhaXNlZCgyKToyMDc
scope.29.kind=method
scope.29.startLine=207
scope.29.endLine=210
scope.29.semanticHash=6c1f332502b52cb92698c43cb42d7c1797c4159cb6d8d7b20d5654d9169e8b8b
scope.30.id=bWV0aG9kOkpvdXJuYWxsaW5nI2VudGl0eURpdmlkZW5kUGFpZCgyKToyMTY
scope.30.kind=method
scope.30.startLine=216
scope.30.endLine=219
scope.30.semanticHash=4ae40815f4f7919e41fad5998dd98f7c118067fa5ed2d8facfca2b470bf5f524
scope.31.id=bWV0aG9kOkpvdXJuYWxsaW5nI2VudGl0eUZvcm1lZCgxKToxOTc
scope.31.kind=method
scope.31.startLine=197
scope.31.endLine=200
scope.31.semanticHash=f56bfaf326fb2ae4b657535d60c1a5e91128e0782639046abbd88a3f95f35a82
scope.32.id=bWV0aG9kOkpvdXJuYWxsaW5nI2VudGl0eUhvdXNlQnVpbHQoMik6MjIx
scope.32.kind=method
scope.32.startLine=221
scope.32.endLine=223
scope.32.semanticHash=caa2c5f97b4a8829028e0d3c1bc3bfa737d11410b0107a95e1419ed57486da12
scope.33.id=bWV0aG9kOkpvdXJuYWxsaW5nI2VudGl0eUxpcXVpZGF0ZWQoMyk6MTU2
scope.33.kind=method
scope.33.startLine=156
scope.33.endLine=159
scope.33.semanticHash=612b6cb1db35be4bc7fbfdc57e9f93fc2297e5c13beae0cad37acc03af7e8131
scope.34.id=bWV0aG9kOkpvdXJuYWxsaW5nI2VudGl0eUxvYW5SYWlzZWQoMik6MjAy
scope.34.kind=method
scope.34.startLine=202
scope.34.endLine=205
scope.34.semanticHash=6496c830b125f4cb0ec461ea28f674e462a68eb1b2cefd8b8cec893d876e123a
scope.35.id=bWV0aG9kOkpvdXJuYWxsaW5nI2VudGl0eUxvYW5SZXBhaWQoNCk6MjEy
scope.35.kind=method
scope.35.startLine=212
scope.35.endLine=214
scope.35.semanticHash=44b154a53ce0952a21cd53eeffacdbe9eeb54509b81f6c1e863ed34f49803a35
scope.36.id=bWV0aG9kOkpvdXJuYWxsaW5nI2luaGVyaXRlZCgzKToyNTU
scope.36.kind=method
scope.36.startLine=255
scope.36.endLine=258
scope.36.semanticHash=ceec77f551a5857dce45718271775a58b6193f46d2e62bdb04c264003e4a179c
scope.37.id=bWV0aG9kOkpvdXJuYWxsaW5nI2tlcHRNb3J0Z2FnZSgzKToyNjA
scope.37.kind=method
scope.37.startLine=260
scope.37.endLine=263
scope.37.semanticHash=4d82a5859ed319d93ed182bfe868b36f7b80902ecb0739cafdfb49baeb988719
scope.38.id=bWV0aG9kOkpvdXJuYWxsaW5nI2xlZnRKYWlsQnlQYXlpbmcoMik6MzMz
scope.38.kind=method
scope.38.startLine=333
scope.38.endLine=336
scope.38.semanticHash=9f3c881ab309563a5cf375b612bbe908e34a95fccd0fd7287c8620a6a958ec49
scope.39.id=bWV0aG9kOkpvdXJuYWxsaW5nI2xlZnRKYWlsQnlSb2xsaW5nRG91YmxlcygxKTozNDM
scope.39.kind=method
scope.39.startLine=343
scope.39.endLine=346
scope.39.semanticHash=97c4449d633ee14d345e802c6a7bd0e54d7e84dab2a8e79c44637bafdc3fb30d
scope.40.id=bWV0aG9kOkpvdXJuYWxsaW5nI2xlZnRKYWlsV2l0aENhcmQoMSk6MzM4
scope.40.kind=method
scope.40.startLine=338
scope.40.endLine=341
scope.40.semanticHash=ed2944c658ddfb004d481102fc6c055496fb47abfddcd86055d2edd8c3d87e97
scope.41.id=bWV0aG9kOkpvdXJuYWxsaW5nI2xpZnRlZE1vcnRnYWdlKDMpOjI2NQ
scope.41.kind=method
scope.41.startLine=265
scope.41.endLine=268
scope.41.semanticHash=f4d1fe429539965ed3d4905b30695ba044e503f34dbe577b9d500ed8077f4631
scope.42.id=bWV0aG9kOkpvdXJuYWxsaW5nI21vcnRnYWdlU3BhcmVQcm9wZXJ0eSgxKTo4Nw
scope.42.kind=method
scope.42.startLine=87
scope.42.endLine=100
scope.42.semanticHash=baec9960ebd46f63854577f169c28f8c7918295d19721ba0a65b0e3de8aed9c9
scope.43.id=bWV0aG9kOkpvdXJuYWxsaW5nI21vcnRnYWdlZCgzKToyNTA
scope.43.kind=method
scope.43.startLine=250
scope.43.endLine=253
scope.43.semanticHash=9ac73150c259e7cc5855023f1e05407bc5573f4c1d02bdee8b683808969fe822
scope.44.id=bWV0aG9kOkpvdXJuYWxsaW5nI21vdmVkKDUpOjQ2
scope.44.kind=method
scope.44.startLine=46
scope.44.endLine=49
scope.44.semanticHash=5c5f41e3caf7fdb136e1e102475b1b2bd166e6ccf5a1599910692af866166492
scope.45.id=bWV0aG9kOkpvdXJuYWxsaW5nI3BhaWQoMyk6Mjgy
scope.45.kind=method
scope.45.startLine=282
scope.45.endLine=285
scope.45.semanticHash=6116cac85a6db2e8926e961fe5d265faa129d972a02e350e8ed5075e43e96f2c
scope.46.id=bWV0aG9kOkpvdXJuYWxsaW5nI3BhaWQoNCk6MjI1
scope.46.kind=method
scope.46.startLine=225
scope.46.endLine=228
scope.46.semanticHash=4dd554bb4e166415f00f9234100865618cbe792050f12d8542a4fb18b60ca68a
scope.47.id=bWV0aG9kOkpvdXJuYWxsaW5nI3BhaWQoNCk6Mjc2
scope.47.kind=method
scope.47.startLine=276
scope.47.endLine=280
scope.47.semanticHash=c761e7e035b4c4f0568298e1093212978a0b9b453622d15346ebfe8348429a5d
scope.48.id=bWV0aG9kOkpvdXJuYWxsaW5nI3BhaWRCYW5rKDIpOjMxNw
scope.48.kind=method
scope.48.startLine=317
scope.48.endLine=320
scope.48.semanticHash=dad81fded4faaed8e30304834dca2f555d6b3cc8d6b177a37294e442fa0ab9ba
scope.49.id=bWV0aG9kOkpvdXJuYWxsaW5nI3BlZXJUcmFkZSg0KToxNjE
scope.49.kind=method
scope.49.startLine=161
scope.49.endLine=163
scope.49.semanticHash=fa3439500eeca2f89812c599d03a22fc71de607d4270f09f48f7340a48b27708
scope.50.id=bWV0aG9kOkpvdXJuYWxsaW5nI3JlY2VpdmVkQmFuaygyKTozMjI
scope.50.kind=method
scope.50.startLine=322
scope.50.endLine=325
scope.50.semanticHash=49a8dd0837859df987ab523f0b0dfb67ec2f66cf0aaca9af9db0f4ab61d02b0f
scope.51.id=bWV0aG9kOkpvdXJuYWxsaW5nI3JlZnVzZWRCdWlsZGluZygzKTozMDI
scope.51.kind=method
scope.51.startLine=302
scope.51.endLine=305
scope.51.semanticHash=e98729b0d8a73e2ec3446f8b4db53f4b9e47be1c7af355cd579b98e7830c760b
scope.52.id=bWV0aG9kOkpvdXJuYWxsaW5nI3JvbGxlZCgyKTo0MQ
scope.52.kind=method
scope.52.startLine=41
scope.52.endLine=44
scope.52.semanticHash=dc1513fd8c86c2d8d55431da00ea12d7bb8f8bd0f605546a94ef5dac3344be4c
scope.53.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NhbGVSZWZ1c2VkKDQpOjI5Nw
scope.53.kind=method
scope.53.startLine=297
scope.53.endLine=300
scope.53.semanticHash=162d6f420761e8ec6aca8358f05ec85c7f505c3d181b7bdf7790bdd353d199d5
scope.54.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NlbnRUb0phaWwoMik6MzI3
scope.54.kind=method
scope.54.startLine=327
scope.54.endLine=331
scope.54.semanticHash=4b2f66bd3c1b7f4b5cda2020f3facda0a1c0b43830e7e5def4a466610de4d09a
scope.55.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NlcnZpY2VEZXZlbG9wbWVudExvYW4oMSk6Njk
scope.55.kind=method
scope.55.startLine=69
scope.55.endLine=85
scope.55.semanticHash=14b8bbaf34ac22ccaaad4ffe777fb6ba31ee9dadb547939abc176045c948ae3a
scope.56.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NlcnZpY2VEZXZlbG9wbWVudExvYW4oMik6MTAy
scope.56.kind=method
scope.56.startLine=102
scope.56.endLine=119
scope.56.semanticHash=423517538a8ffc8aac60178ca6aea7831580ed4917a44e51dbe8b2f63114ca3d
scope.57.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NvbGQoNCk6Mjky
scope.57.kind=method
scope.57.startLine=292
scope.57.endLine=295
scope.57.semanticHash=8e21351b16f4a9704ac69531ff94b6a20e1992bc89ac6105cdc175bf12e1f70b
scope.58.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NvbGRFbnRpdHlTaGFyZSg0KToxNTE
scope.58.kind=method
scope.58.startLine=151
scope.58.endLine=154
scope.58.semanticHash=0343e9e5ae8f70512ae68070b708fb90a5a3ae32c58442fe3328ba24b74277d6
scope.59.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NvbGRIb3VzZSgzKToxNDE
scope.59.kind=method
scope.59.startLine=141
scope.59.endLine=144
scope.59.semanticHash=321c15e0f323ae5002fb5e4538eab395aee66348a900759e112bb357109aa899
scope.60.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NvbGRUb1BlZXIoNCk6MTQ2
scope.60.kind=method
scope.60.startLine=146
scope.60.endLine=149
scope.60.semanticHash=6cd1831d320b99bc0e26f5d4e4ffe76326fab8255a6cee7f77b91f73001f5fee
scope.61.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NwbGl0TW9ub3BvbHlQYWlkKDMpOjE5Mw
scope.61.kind=method
scope.61.startLine=193
scope.61.endLine=195
scope.61.semanticHash=ffa1fcf5b0ab461d0d6029aac88d8acefff61bbb45bb0f0d4a8734aba99449a9
scope.62.id=bWV0aG9kOkpvdXJuYWxsaW5nI3NwbGl0TW9ub3BvbHlXb24oMik6MTg5
scope.62.kind=method
scope.62.startLine=189
scope.62.endLine=191
scope.62.semanticHash=cd84aeabd8a2ff15007d92ccea36f0f38259b89543fc1c93446b767793982aaf
scope.63.id=bWV0aG9kOkpvdXJuYWxsaW5nI3N0YWxlbWF0ZVRyYWRpbmcoMSk6MTY1
scope.63.kind=method
scope.63.startLine=165
scope.63.endLine=167
scope.63.semanticHash=8fecba8a2aae731b50ef79fbc287096e312c2466a0845bae67d1720744cfd4fe
scope.64.id=bWV0aG9kOkpvdXJuYWxsaW5nI3N0YXllZEluSmFpbCgxKTozNDg
scope.64.kind=method
scope.64.startLine=348
scope.64.endLine=351
scope.64.semanticHash=d96eaaec8fe99f0945f5678e25e8c9f2aa1816aced3947d83b45b227eaf604c4
scope.65.id=bWV0aG9kOkpvdXJuYWxsaW5nI3N0cmF0ZWd5TmFtZWQoMik6MTc5
scope.65.kind=method
scope.65.startLine=179
scope.65.endLine=187
scope.65.semanticHash=53cc02278b837f85287c7e3291da404569c401ec3e42f6dee35a77cd3d1b3672
scope.66.id=bWV0aG9kOkpvdXJuYWxsaW5nI3dvbigxKTozNTg
scope.66.kind=method
scope.66.startLine=358
scope.66.endLine=362
scope.66.semanticHash=164b8d421668f8bc5a0a969808e999cde39abf01230186b966401acaf242e536
scope.67.id=bWV0aG9kOkpvdXJuYWxsaW5nI3dvbkF0QXVjdGlvbigzKToxMzY
scope.67.kind=method
scope.67.startLine=136
scope.67.endLine=139
scope.67.semanticHash=2ebd9658b384ee1ab3d329ffe431ad4159fa491b72e773e2407d20d660b5eeb7
*/
