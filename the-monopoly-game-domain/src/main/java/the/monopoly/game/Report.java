package the.monopoly.game;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * A game told as it happened, one line per entry, in the order the journal
 * recorded them.
 * <p>
 * The wording lives here and nowhere else: the journal keeps what happened as
 * data, so that how it reads can change without the game being touched.
 */
public final class Report {
  private Report() {
  }

  public static String of(List<Entry> journal) {
    return journal.stream().map(Report::line).collect(joining("\n"));
  }

  /**
   * Switching over the sealed {@link Entry} rather than looking a handler up by
   * class is what makes a new kind of entry a compile error here. Told any
   * other way — a map keyed by class, a chain of instanceof — an entry nobody
   * wrote a line for compiles, and goes silently unreported until a game hits
   * it. That is the guarantee {@link Entry} is sealed for, and it is what asked
   * for the last two lines below when buying and auctions arrived.
   * <p>
   * One case per entry is one branch per entry, so complexity counts as high as
   * the journal is wide. That is the shape of the thing being described, not
   * complication to be refactored away; a form that measures lower gets there
   * by hiding the same branching from the compiler as well as the metric. This
   * switch is exempt from the CRAP threshold by decision of 2026-07-28, after
   * three rounds of it being replaced by a map and put back. When the cases
   * outgrow a screen, give each one a named method and leave the switch itself
   * alone.
   */
  private static String line(Entry entry) {
    return switch (entry) {
      case Entry.Start it -> "The game starts with " + names(it.players());
      case Entry.InitiativeRoll it -> name(it.player()) + " rolls " + it.total() + " for initiative";
      case Entry.InitiativeWon it -> name(it.player()) + " wins initiative";
      case Entry.TurnStarted it -> name(it.player()) + " starts a turn with $" + it.balance().amount()
          + " and a $" + it.reserve().amount() + " reserve";
      case Entry.Rolled it -> name(it.player()) + " rolls a total of " + it.total();
      case Entry.Moved it -> name(it.player()) + " moves from position " + it.from() + " ("
          + boardSpaceName(it.fromSpace()) + ") to " + it.to() + " (" + boardSpaceName(it.toSpace()) + ")";
      case Entry.SalaryCollected it -> name(it.player()) + " collects a salary of $" + it.salary().amount();
      case Entry.Bought it -> name(it.player()) + " buys " + spaceName(it.land()) + " for $" + it.price().amount();
      case Entry.AuctionWon it ->
          name(it.player()) + " wins the auction for " + spaceName(it.land()) + " at $" + it.price().amount();
      case Entry.PeerTrade it -> name(it.trader()) + " trades " + boardSpaceName(it.offered()) + " to "
          + name(it.partner()) + " for " + boardSpaceName(it.wanted());
      case Entry.StalemateTrading it -> "stalemate trading is " + (it.enabled() ? "enabled" : "disabled");
      case Entry.PurchaseDeclined it -> declineLine(it);
      case Entry.RentPaid it -> name(it.tenant()) + " pays " + name(it.owner()) + " $"
          + it.rent().amount() + " rent for " + spaceName(it.land());
      case Entry.PlayerPaid it -> name(it.payer()) + " pays " + name(it.payee()) + " $" + it.amount().amount();
      case Entry.HouseBuilt it ->
          name(it.player()) + " builds a house on " + spaceName(it.land()) + " for $" + it.price().amount();
      case Entry.HouseSold it ->
          name(it.player()) + " sells a house on " + spaceName(it.land()) + " for $" + it.price().amount();
      case Entry.Mortgaged it ->
          name(it.player()) + " mortgages " + spaceName(it.land()) + " for $" + it.value().amount();
      case Entry.MortgageLifted it ->
          name(it.player()) + " lifts the mortgage on " + spaceName(it.land()) + " for $"
              + it.total().amount() + " including $" + it.interest().amount() + " interest";
      case Entry.Inherited it -> name(it.player()) + " inherits " + spaceName(it.land())
          + " from " + name(it.debtor());
      case Entry.MortgageKept it -> name(it.player()) + " pays $" + it.interest().amount()
          + " interest to keep the mortgage on " + spaceName(it.land());
      case Entry.LandSold it ->
          name(it.seller()) + " sells " + spaceName(it.land()) + " to " + name(it.buyer())
              + " for $" + it.price().amount();
      case Entry.LandSaleRefused it ->
          name(it.seller()) + " is refused selling " + spaceName(it.land()) + " to " + name(it.buyer())
              + " for $" + it.price().amount() + " because the colour group has houses built";
      case Entry.DistressedSaleStarted it ->
          name(it.seller()) + " puts " + spaceName(it.land()) + " up for sale to avoid bankruptcy";
      case Entry.DistressedSaleNoBidder it ->
          name(it.seller()) + " finds no bidder for " + spaceName(it.land());
      case Entry.DistressedOffer it ->
          name(it.bidder()) + " offers $" + it.price().amount() + " for " + spaceName(it.land());
      case Entry.DistressedSaleWon it ->
          name(it.bidder()) + " wins the distressed sale for " + spaceName(it.land())
              + " at $" + it.price().amount();
      case Entry.BuildingRefused it ->
          name(it.player()) + " is refused building a house on " + spaceName(it.land())
              + " for $" + it.price().amount() + " because a street in the colour group is mortgaged";
      case Entry.ChanceCardDrawn it ->
          name(it.player()) + " draws the chance card \"" + it.card() + "\"";
      case Entry.CommunityChestCardDrawn it ->
          name(it.player()) + " draws the community chest card \"" + it.card() + "\"";
      case Entry.BankPaid it ->
          name(it.player()) + " pays the bank $" + it.amount().amount();
      case Entry.BankReceived it ->
          name(it.player()) + " receives $" + it.amount().amount() + " from the bank";
      case Entry.JailEntered it ->
          name(it.player()) + " is sent to jail from landing on " + boardSpaceName(it.cause());
      case Entry.JailFinePaid it ->
          name(it.player()) + " leaves jail by paying the $" + it.fine().amount() + " fine";
      case Entry.JailCardUsed it -> name(it.player()) + " leaves jail using the Get Out of Jail Free card";
      case Entry.JailDoublesRolled it -> name(it.player()) + " leaves jail by rolling doubles";
      case Entry.JailStayed it -> name(it.player()) + " stays in jail";
      case Entry.Bankrupt it -> name(it.player()) + " goes bankrupt to "
          + (it.creditor() == null ? "the bank" : name(it.creditor()));
      case Entry.Stalemate it -> "The game ends in a stalemate";
      case Entry.FinalBalance it -> name(it.player()) + "'s final balance is $" + it.balance().amount();
      case Entry.Won it -> name(it.player()) + " wins the game";
    };
  }

  private static String declineLine(Entry.PurchaseDeclined it) {
    String prefix = name(it.player()) + " declines to buy " + boardSpaceName(it.land());
    return switch (it.reason()) {
      case CANNOT_AFFORD -> prefix + " because it cannot afford the $" + it.price().amount() + " price";
      case CASH_RESERVE -> prefix + " because it would drop the balance below the $"
          + it.reserve().amount() + " reserve";
      case NO_BUYING_POLICY -> prefix;
    };
  }

  /**
   * A space is named on the board in words, and in the domain as one name, so
   * the run-together words are told apart again here. A space whose printed
   * name is not its own name spelled out will have to be given one when a
   * specification asks the report for it.
   */
  private static String spaceName(Street.Type land) {
    return land.name().replaceAll("(?<=[a-z])(?=[A-Z])", " ");
  }

  /**
   * Most spaces need no entry here at all: {@link #spaceName} already spells
   * the enum name out correctly, and is the fallback for every space not
   * listed. Only a name {@code spaceName} gets wrong — an accent, an
   * apostrophe, a parenthetical, or a bilingual pointer to the space's own
   * translation — needs an entry.
   */
  private static final Map<Street.Type, String> BOARD_SPACE_NAME_OVERRIDES = Map.ofEntries(
      Map.entry(Street.Type.start, "Start"),
      Map.entry(Street.Type.PlaceDuMonumentSpa, "Place du Monument Spa"),
      Map.entry(Street.Type.RueDeDiekirchArlon, "Rue de Diekirch Arlon"),
      Map.entry(Street.Type.RueStLeonardLiege, "Rue St-Léonard Liège"),
      Map.entry(Street.Type.PlaceDeLAngeNamur, "Place de l'Ange Namur"),
      Map.entry(Street.Type.HoogstraatBrussel, "Hoogstraat (Brussel) / Rue Haute (Bruxelles)"),
      Map.entry(Street.Type.BoulevardDAvroyLiege, "Boulevard d'Avroy Liège"),
      Map.entry(Street.Type.NieuwstraatBrussel, "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)"),
      Map.entry(Street.Type.NoordStation, "Noord Station / Gare du Nord"),
      Map.entry(Street.Type.CentraalStation, "Centraal Station / Gare Centrale"),
      Map.entry(Street.Type.Buurtspoorwegen, "Buurtspoorwegen / Tramways Vicinaux"),
      Map.entry(Street.Type.ZuidStation, "Zuid Station / Gare du Midi"),
      Map.entry(Street.Type.Elektriciteitscentrale, "Elektriciteitscentrale / Centrale Électrique"),
      Map.entry(Street.Type.Watermaatschappij, "Watermaatschappij / Compagnie des Eaux"),
      Map.entry(Street.Type.InkomstenBelasting, "Inkomsten Belasting / Impôts sur le revenu"),
      Map.entry(Street.Type.ExtraBelasting, "Extra Belasting / Taxe de Luxe"),
      Map.entry(Street.Type.Kans, "Kans / Chance"),
      Map.entry(Street.Type.AlgemeenFonds, "Algemeen Fonds / Caisse de Communauté"),
      Map.entry(Street.Type.OpBezoek, "Op Bezoek / Simple Visite"),
      Map.entry(Street.Type.GratisParkeren, "Gratis Parkeren / Parc Gratuit"),
      Map.entry(Street.Type.NaarDeGevangenis, "Naar de Gevangenis / Allez en Prison")
  );

  private static String boardSpaceName(Street.Type space) {
    return BOARD_SPACE_NAME_OVERRIDES.getOrDefault(space, spaceName(space));
  }

  private static String names(List<Player.ID> players) {
    return players.stream().map(Report::name).collect(joining(", "));
  }

  private static String name(Player.ID player) {
    return player.value();
  }
}

/* mutate4java-manifest
version=1
moduleHash=48b8c24ef2c2c3b7d091ea281f520b3deef2eb7c2e42262b8dc09e87b9bec135
scope.0.id=Y2xhc3M6UmVwb3J0I1JlcG9ydDoxOQ
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=180
scope.0.semanticHash=455c46b0897908bcd32cb4b808069e897de5e8247f147c443b1c9e477b188011
scope.1.id=ZmllbGQ6UmVwb3J0I0JPQVJEX1NQQUNFX05BTUVfT1ZFUlJJREVTOjE0NQ
scope.1.kind=field
scope.1.startLine=145
scope.1.endLine=167
scope.1.semanticHash=6eb67eaa69a04e25ac5429d119e7ade52b31b349bfbef41507f8ce1692daff1b
scope.2.id=bWV0aG9kOlJlcG9ydCNib2FyZFNwYWNlTmFtZSgxKToxNjk
scope.2.kind=method
scope.2.startLine=169
scope.2.endLine=171
scope.2.semanticHash=7dcc143fee9458236e6143cec91f4c98d4b78c3de1afb24170abc26433f492c9
scope.3.id=bWV0aG9kOlJlcG9ydCNjdG9yKDApOjIw
scope.3.kind=method
scope.3.startLine=20
scope.3.endLine=21
scope.3.semanticHash=fa4ab1d8c774b5a49e26e7b36ba0ec25ba0d7069b207d38eb37beccb7d02dc9c
scope.4.id=bWV0aG9kOlJlcG9ydCNkZWNsaW5lTGluZSgxKToxMTg
scope.4.kind=method
scope.4.startLine=118
scope.4.endLine=126
scope.4.semanticHash=f8e93f31787af3801c915ba44f5de44f38ccb43e2a127b1f3987d720e4722a8c
scope.5.id=bWV0aG9kOlJlcG9ydCNsaW5lKDEpOjQ0
scope.5.kind=method
scope.5.startLine=44
scope.5.endLine=116
scope.5.semanticHash=2d756b959367e26af2fdb3bb501a8f682be6c75b689f5f83395cf8ffe8642b8f
scope.6.id=bWV0aG9kOlJlcG9ydCNuYW1lKDEpOjE3Nw
scope.6.kind=method
scope.6.startLine=177
scope.6.endLine=179
scope.6.semanticHash=cd39b08d1576cbc2e9d2ae25bb54b6b608f1219509ee3d03086e8f3ab4c238fa
scope.7.id=bWV0aG9kOlJlcG9ydCNuYW1lcygxKToxNzM
scope.7.kind=method
scope.7.startLine=173
scope.7.endLine=175
scope.7.semanticHash=7baca973d9baa23bf1205536bbfd229d494b1e2c863e02f6e50c5ce7dcb42959
scope.8.id=bWV0aG9kOlJlcG9ydCNvZigxKToyMw
scope.8.kind=method
scope.8.startLine=23
scope.8.endLine=25
scope.8.semanticHash=3bddf559e6362200e39e3d2024808e23da1e3da3deb262e8b89f1402e9edd25e
scope.9.id=bWV0aG9kOlJlcG9ydCNzcGFjZU5hbWUoMSk6MTM0
scope.9.kind=method
scope.9.startLine=134
scope.9.endLine=136
scope.9.semanticHash=56eb8b748063de6798efd6f1a96d5a30f07cf019c0478f7f004006d2fe61eaf8
*/
