package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static the.monopoly.game.specs.acceptance.GameAccount.Claim;

/**
 * Regex fragments and journal/report claim builders shared by the step-handler classes.
 * Split out of {@link MonopolyStepHandlers} so each step-handler file stays within a
 * manageable mutation-site count.
 */
final class MonopolyStepHelpers {
  /** Either a literal amount or a {@code <placeholder>} naming an example column. */
  static final String VALUE = "(<[^<>]+>|-?[0-9,]+)";
  static final String MONEY = "(<[^<>]+>|-?[0-9,]+(?:\\.[0-9]{1,2})?)";
  static final String NAME = "(<[^<>]+>|[^\"]+)";
  static final String UNQUOTED_NAME = "(<[^<>]+>|.+?)";
  static final String UNQUOTED_NAME_WITHOUT_ORDERING =
      "(<[^<>]+>|(?:(?! before it records that pawn | before it says that pawn ).)+?)";
  static final Pattern DICE_DESCRIPTION = Pattern.compile("([0-9]+) faced");

  private MonopolyStepHelpers() {
  }

  static Claim initiativeRoll(String pawnName, int total) {
    return Claim.of(new Entry.InitiativeRoll(idOf(pawnName), total));
  }

  static Claim initiativeWon(String pawnName) {
    return Claim.of(new Entry.InitiativeWon(idOf(pawnName)));
  }

  /** Matches any balance: for steps that only care that the pawn's turn started. */
  static Claim turnStarted(String pawnName) {
    return new Claim(
        entry -> entry instanceof Entry.TurnStarted it && it.player().equals(idOf(pawnName)),
        pawnName + " starts a turn"
    );
  }

  static Claim turnStarted(String pawnName, int balance) {
    return Claim.of(new Entry.TurnStarted(idOf(pawnName), money(balance)));
  }

  static Claim turnStarted(String pawnName, int balance, int reserve) {
    return Claim.of(new Entry.TurnStarted(idOf(pawnName), money(balance), money(reserve)));
  }

  static Claim turnStartedAtAge(String pawnName, int age) {
    return new Claim(entry -> entry instanceof Entry.TurnStarted it
        && it.player().equals(idOf(pawnName)) && it.age() == age,
        pawnName + " starts a turn aged " + age + " years");
  }

  static Claim finalBalance(String pawnName, int balance) {
    return Claim.of(new Entry.FinalBalance(idOf(pawnName), money(balance)));
  }

  static Claim finalAge(String pawnName, int age) {
    return Claim.of(new Entry.FinalAge(idOf(pawnName), age));
  }

  static Claim warProfitsTaxPaid(String pawnName, int amount) {
    return Claim.of(new Entry.WarProfitsTaxPaid(idOf(pawnName), money(amount)));
  }

  static Claim governmentBalance(int amount) {
    return Claim.of(new Entry.GovernmentBalance(money(amount)));
  }

  static Claim warProfitsTax(String state) {
    return Claim.of(new Entry.WarProfitsTaxEnabled(state.equals("enabled")));
  }

  static Claim rentRelief(String state) {
    return Claim.of(new Entry.RentReliefEnabled(state.equals("enabled")));
  }

  static Claim rentReliefPaid(String landlord, int amount) {
    return Claim.of(new Entry.RentReliefPaid(idOf(landlord), money(amount)));
  }

  static String rentReliefPaidLine(String landlord, int amount) {
    return "The government pays " + landlord + " $" + amount + " in rent relief";
  }

  static Claim purchaseDeclined(String pawnName, String spaceName, int price,
                                Strategy.DeclineReason reason, int reserve) {
    if (reason == Strategy.DeclineReason.NO_BUYING_POLICY) {
      return new Claim(entry -> entry instanceof Entry.PurchaseDeclined it
          && it.player().equals(idOf(pawnName))
          && it.land().equals(SpaceNames.of(spaceName))
          && it.reason() == reason,
          pawnName + " declines to buy " + spaceName);
    }
    if (reason == Strategy.DeclineReason.CASH_RESERVE) {
      return new Claim(entry -> entry instanceof Entry.PurchaseDeclined it
          && it.player().equals(idOf(pawnName))
          && it.land().equals(SpaceNames.of(spaceName))
          && it.reason() == reason
          && it.reserve().equals(money(reserve)),
          pawnName + " declines to buy " + spaceName + " for its reserve");
    }
    return Claim.of(new Entry.PurchaseDeclined(
        idOf(pawnName), SpaceNames.of(spaceName), money(price), reason, money(reserve)));
  }

  /** Parses a scenario's "$1500" example value into its numeric amount. */
  static int dollars(String text) {
    if (!text.startsWith("$"))
      throw new AssertionError("Expected an amount like \"$1500\" but got \"" + text + "\".");
    return Integer.parseInt(text.substring(1).replace(",", ""));
  }

  static Claim rolled(String pawnName, int total) {
    return Claim.of(new Entry.Rolled(idOf(pawnName), total));
  }

  static Claim moved(String pawnName, int from, String fromSpace, int to, String toSpace) {
    return Claim.of(new Entry.Moved(
        idOf(pawnName), from, to, SpaceNames.of(fromSpace), SpaceNames.of(toSpace)));
  }

  static Claim salaryCollected(String pawnName, int salary) {
    return Claim.of(new Entry.SalaryCollected(idOf(pawnName), money(salary)));
  }

  static Claim megacorpTaxPaid(String pawnName, int amount) {
    return Claim.of(new Entry.MegacorpSalaryTaxPaid(idOf(pawnName), money(amount)));
  }

  static Claim bought(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.Bought(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  static Claim auctionWon(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.AuctionWon(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  static String auctionWonLine(String pawnName, String spaceName, int price) {
    return pawnName + " wins the auction for " + spaceName + " at $" + price;
  }

  static Claim rentPaid(String tenant, String owner, String spaceName, int rent) {
    return Claim.of(new Entry.RentPaid(idOf(tenant), idOf(owner), SpaceNames.of(spaceName), money(rent)));
  }

  static Claim houseBuilt(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.HouseBuilt(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  static Claim houseSold(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.HouseSold(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  static Claim mortgaged(String pawnName, String spaceName, int value) {
    return Claim.of(new Entry.Mortgaged(idOf(pawnName), SpaceNames.of(spaceName), money(value)));
  }

  static Claim mortgageLifted(String pawnName, String spaceName, int total, int interest) {
    return Claim.of(new Entry.MortgageLifted(
        idOf(pawnName), SpaceNames.of(spaceName), money(total), money(interest)));
  }

  static Claim inherited(String creditor, String spaceName, String debtor) {
    return Claim.of(new Entry.Inherited(idOf(creditor), SpaceNames.of(spaceName), idOf(debtor)));
  }

  static Claim mortgageKept(String pawnName, String spaceName, int interest) {
    return Claim.of(new Entry.MortgageKept(idOf(pawnName), SpaceNames.of(spaceName), money(interest)));
  }

  static Claim landSold(String seller, String spaceName, String buyer, int price) {
    return Claim.of(new Entry.LandSold(
        idOf(seller), SpaceNames.of(spaceName), idOf(buyer), money(price)));
  }

  static Claim landSaleRefused(String seller, String spaceName, String buyer, int price) {
    return Claim.of(new Entry.LandSaleRefused(
        idOf(seller), SpaceNames.of(spaceName), idOf(buyer), money(price)));
  }

  static Claim distressedStarted(String seller, String spaceName) {
    return Claim.of(new Entry.DistressedSaleStarted(idOf(seller), SpaceNames.of(spaceName)));
  }

  static Claim distressedNoBidder(String seller, String spaceName) {
    return Claim.of(new Entry.DistressedSaleNoBidder(idOf(seller), SpaceNames.of(spaceName)));
  }

  static Claim peerTrade(String trader, String offered, String partner, String wanted) {
    return Claim.of(new Entry.PeerTrade(idOf(trader), SpaceNames.of(offered), idOf(partner), SpaceNames.of(wanted)));
  }

  static Claim stalemateTrading(String state) {
    return Claim.of(new Entry.StalemateTrading(state.equals("enabled")));
  }

  static Claim developmentLoans(String state, boolean fullDraw) {
    boolean enabled = state.equals("enabled");
    return Claim.of(new Entry.DevelopmentLoans(enabled, fullDraw && enabled));
  }

  static Claim splitMonopolyWon(String winner, String loser) {
    return Claim.of(new Entry.SplitMonopolyWon(idOf(winner), idOf(loser)));
  }

  static Claim splitMonopolyWon(String winner) {
    return new Claim(entry -> entry instanceof Entry.SplitMonopolyWon it
        && it.winner().equals(idOf(winner)), winner + " wins the split monopoly");
  }

  static Claim splitMonopolyPaid(String payer, String payee, int amount) {
    return Claim.of(new Entry.SplitMonopolyPaid(idOf(payer), idOf(payee), money(amount)));
  }

  static Claim distressedOffer(String bidder, String spaceName, int price) {
    return Claim.of(new Entry.DistressedOffer(idOf(bidder), SpaceNames.of(spaceName), money(price)));
  }

  static Claim distressedWon(String bidder, String spaceName, int price) {
    return Claim.of(new Entry.DistressedSaleWon(idOf(bidder), SpaceNames.of(spaceName), money(price)));
  }

  static Claim buildingRefused(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.BuildingRefused(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  static Claim chanceCardDrawn(String pawnName, String card) {
    return Claim.of(new Entry.ChanceCardDrawn(idOf(pawnName), card));
  }

  static Claim chanceCardDrawn(String pawnName) {
    return new Claim(entry -> entry instanceof Entry.ChanceCardDrawn it
        && it.player().equals(idOf(pawnName)), pawnName + " draws a chance card");
  }

  static Claim communityChestCardDrawn(String pawnName, String card) {
    return Claim.of(new Entry.CommunityChestCardDrawn(idOf(pawnName), card));
  }

  static Claim communityChestCardDrawn(String pawnName) {
    return new Claim(entry -> entry instanceof Entry.CommunityChestCardDrawn it
        && it.player().equals(idOf(pawnName)), pawnName + " draws a community chest card");
  }

  static Claim bankPaid(String pawnName, int amount) {
    return Claim.of(new Entry.BankPaid(idOf(pawnName), money(amount)));
  }

  static Claim bankReceived(String pawnName, int amount) {
    return Claim.of(new Entry.BankReceived(idOf(pawnName), money(amount)));
  }

  static Claim playerPaid(String payer, String payee, int amount) {
    return Claim.of(new Entry.PlayerPaid(idOf(payer), idOf(payee), money(amount)));
  }

  static Claim jailEntered(String pawnName, String spaceName) {
    return Claim.of(new Entry.JailEntered(idOf(pawnName), SpaceNames.of(spaceName)));
  }

  static Claim jailFinePaid(String pawnName, int fine) {
    return Claim.of(new Entry.JailFinePaid(idOf(pawnName), money(fine)));
  }

  static Claim jailCardUsed(String pawnName) {
    return Claim.of(new Entry.JailCardUsed(idOf(pawnName)));
  }

  static Claim jailDoublesRolled(String pawnName) {
    return Claim.of(new Entry.JailDoublesRolled(idOf(pawnName)));
  }

  static Claim jailStayed(String pawnName) {
    return Claim.of(new Entry.JailStayed(idOf(pawnName)));
  }

  /** A pawn moving anywhere, for a step that says when it moved rather than where to. */
  static Claim moves(String pawnName) {
    return new Claim(
        entry -> entry instanceof Entry.Moved it && it.player().equals(idOf(pawnName)),
        "move by " + pawnName
    );
  }

  /** The report reads as the features read, so a claim is words the report must carry. */
  static String rollsForInitiative(String pawnName, int total) {
    return pawnName + " rolls " + total + " for initiative";
  }

  static String rollsATotalOf(String pawnName, int total) {
    return pawnName + " rolls a total of " + total;
  }

  static String movesFromPosition(String pawnName, int from, String fromSpace, int to, String toSpace) {
    return pawnName + " moves from position " + from + " (" + fromSpace + ") to "
        + to + " (" + toSpace + ")";
  }

  /** A pawn moving anywhere, for a step that says when it moved rather than where to. */
  static String movesAnywhere(String pawnName) {
    return pawnName + " moves from position ";
  }

  static String soldAHouse(String pawnName, String spaceName, int price) {
    return pawnName + " sells a house on " + spaceName + " for $" + price;
  }

  static String builtAHouse(String pawnName, String spaceName, int price) {
    return pawnName + " builds a house on " + spaceName + " for $" + price;
  }

  static String mortgagedLine(String pawnName, String spaceName, int value) {
    return pawnName + " mortgages " + spaceName + " for $" + value;
  }

  static String mortgageLiftedLine(String pawnName, String spaceName, int total, int interest) {
    return pawnName + " lifts the mortgage on " + spaceName + " for $" + total
        + " including $" + interest + " interest";
  }

  static String inheritedLine(String creditor, String spaceName, String debtor) {
    return creditor + " inherits " + spaceName + " from " + debtor;
  }

  static String mortgageKeptLine(String pawnName, String spaceName, int interest) {
    return pawnName + " pays $" + interest + " interest to keep the mortgage on " + spaceName;
  }

  static String landSoldLine(String seller, String spaceName, String buyer, int price) {
    return seller + " sells " + spaceName + " to " + buyer + " for $" + price;
  }

  static String landSaleRefusedLine(String seller, String spaceName, String buyer, int price) {
    return seller + " is refused selling " + spaceName + " to " + buyer
        + " for $" + price + " because the colour group has houses built";
  }

  static String buildingRefusedLine(String pawnName, String spaceName, int price) {
    return pawnName + " is refused building a house on " + spaceName
        + " for $" + price + " because a street in the colour group is mortgaged";
  }

  static String purchaseDeclinedLine(String pawnName, String spaceName, int price) {
    return pawnName + " declines to buy " + spaceName
        + " because it cannot afford the $" + price + " price";
  }

  static String purchaseDeclinedForReserveLine(String pawnName, String spaceName, int reserve) {
    return pawnName + " declines to buy " + spaceName
        + " because it would drop the balance below the $" + reserve + " reserve";
  }

  static String purchaseDeclinedLine(String pawnName, String spaceName) {
    return pawnName + " declines to buy " + spaceName;
  }

  static String chanceCardDrawnLine(String pawnName, String card) {
    return pawnName + " draws the chance card \"" + card + "\"";
  }

  static String communityChestCardDrawnLine(String pawnName, String card) {
    return pawnName + " draws the community chest card \"" + card + "\"";
  }

  static String bankPaidLine(String pawnName, int amount) {
    return pawnName + " pays the bank $" + amount;
  }

  static String bankReceivedLine(String pawnName, int amount) {
    return pawnName + " receives $" + amount + " from the bank";
  }

  static String warProfitsTaxPaidLine(String pawnName, int amount) {
    return pawnName + " pays a war profits tax of $" + amount;
  }

  static String governmentBalanceLine(int amount) {
    return "The government's account holds $" + amount;
  }

  static String megacorpTaxPaidLine(int amount) {
    return "MegaCorp pays the government an individual income tax of $" + amount;
  }

  static String warProfitsTaxLine(String state) {
    return "war profits tax is " + state;
  }

  static String rentReliefLine(String state) {
    return "rent relief is " + state;
  }

  static String playerPaidLine(String payer, String payee, int amount) {
    return payer + " pays " + payee + " $" + amount;
  }

  static String jailEnteredLine(String pawnName, String spaceName) {
    return pawnName + " is sent to jail from landing on " + spaceName;
  }

  static String jailFinePaidLine(String pawnName, int fine) {
    return pawnName + " leaves jail by paying the $" + fine + " fine";
  }

  static String jailCardUsedLine(String pawnName) {
    return pawnName + " leaves jail using the Get Out of Jail Free card";
  }

  static String jailDoublesRolledLine(String pawnName) {
    return pawnName + " leaves jail by rolling doubles";
  }

  static String jailStayedLine(String pawnName) {
    return pawnName + " stays in jail";
  }

  static Player.ID idOf(String pawnName) {
    return new Player.ID(pawnName);
  }

  /** A dice is described by how many faces it has, as in "6 faced". */
  static int facesOf(String description) {
    Matcher faces = DICE_DESCRIPTION.matcher(description);
    if (!faces.matches())
      throw new AssertionError("A dice is described as \"<n> faced\", not \"" + description + "\".");
    return Integer.parseInt(faces.group(1));
  }

  static long diceFaceCount(World world, int position) {
    List<Dice> dice = world.ruleSet().dice().toList();
    if (position < 1 || position > dice.size())
      throw new AssertionError("The rules use " + dice.size() + " dice, so there is no dice " + position + ".");
    return dice.get(position - 1).faces().count();
  }

  /** Only a colour street belongs to a colour group; every other space has none. */
  static Street.Colour colourGroupOf(Street space) {
    return space instanceof ColourStreet street ? street.colourGroup() : null;
  }

  static Money money(int amount) {
    return new Money(amount);
  }

  static Money money(String amount) {
    return Money.fromDollars(amount);
  }
}
