package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;

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
  static final String NAME = "(<[^<>]+>|[^\"]+)";
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

  static Claim bought(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.Bought(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  static Claim auctionWon(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.AuctionWon(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
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

  static Claim landSold(String seller, String spaceName, String buyer, int price) {
    return Claim.of(new Entry.LandSold(
        idOf(seller), SpaceNames.of(spaceName), idOf(buyer), money(price)));
  }

  static Claim landSaleRefused(String seller, String spaceName, String buyer, int price) {
    return Claim.of(new Entry.LandSaleRefused(
        idOf(seller), SpaceNames.of(spaceName), idOf(buyer), money(price)));
  }

  static Claim buildingRefused(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.BuildingRefused(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  static Claim chanceCardDrawn(String pawnName, String card) {
    return Claim.of(new Entry.ChanceCardDrawn(idOf(pawnName), card));
  }

  static Claim communityChestCardDrawn(String pawnName, String card) {
    return Claim.of(new Entry.CommunityChestCardDrawn(idOf(pawnName), card));
  }

  static Claim bankPaid(String pawnName, int amount) {
    return Claim.of(new Entry.BankPaid(idOf(pawnName), money(amount)));
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

  static String chanceCardDrawnLine(String pawnName, String card) {
    return pawnName + " draws the chance card \"" + card + "\"";
  }

  static String communityChestCardDrawnLine(String pawnName, String card) {
    return pawnName + " draws the community chest card \"" + card + "\"";
  }

  static String bankPaidLine(String pawnName, int amount) {
    return pawnName + " pays the bank $" + amount;
  }

  static String jailEnteredLine(String pawnName, String spaceName) {
    return pawnName + " is sent to jail from landing on " + spaceName;
  }

  static String jailFinePaidLine(String pawnName, int fine) {
    return pawnName + " leaves jail by paying the $" + fine + " fine";
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
}
