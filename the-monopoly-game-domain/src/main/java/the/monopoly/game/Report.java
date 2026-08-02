package the.monopoly.game;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.List;

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
      case Entry.TurnStarted it -> name(it.player()) + " starts a turn with $" + it.balance().amount();
      case Entry.Rolled it -> name(it.player()) + " rolls a total of " + it.total();
      case Entry.Moved it -> name(it.player()) + " moves from position " + it.from() + " ("
          + boardSpaceName(it.fromSpace()) + ") to " + it.to() + " (" + boardSpaceName(it.toSpace()) + ")";
      case Entry.SalaryCollected it -> name(it.player()) + " collects a salary of $" + it.salary().amount();
      case Entry.Bought it -> name(it.player()) + " buys " + spaceName(it.land()) + " for $" + it.price().amount();
      case Entry.AuctionWon it ->
          name(it.player()) + " wins the auction for " + spaceName(it.land()) + " at $" + it.price().amount();
      case Entry.RentPaid it -> name(it.tenant()) + " pays " + name(it.owner()) + " $"
          + it.rent().amount() + " rent for " + spaceName(it.land());
      case Entry.HouseBuilt it ->
          name(it.player()) + " builds a house on " + spaceName(it.land()) + " for $" + it.price().amount();
      case Entry.HouseSold it ->
          name(it.player()) + " sells a house on " + spaceName(it.land()) + " for $" + it.price().amount();
      case Entry.Mortgaged it ->
          name(it.player()) + " mortgages " + spaceName(it.land()) + " for $" + it.value().amount();
      case Entry.MortgageLifted it ->
          name(it.player()) + " lifts the mortgage on " + spaceName(it.land()) + " for $"
              + it.total().amount() + " including $" + it.interest().amount() + " interest";
      case Entry.LandSold it ->
          name(it.seller()) + " sells " + spaceName(it.land()) + " to " + name(it.buyer())
              + " for $" + it.price().amount();
      case Entry.LandSaleRefused it ->
          name(it.seller()) + " is refused selling " + spaceName(it.land()) + " to " + name(it.buyer())
              + " for $" + it.price().amount() + " because the colour group has houses built";
      case Entry.BuildingRefused it ->
          name(it.player()) + " is refused building a house on " + spaceName(it.land())
              + " for $" + it.price().amount() + " because a street in the colour group is mortgaged";
      case Entry.ChanceCardDrawn it ->
          name(it.player()) + " draws the chance card \"" + it.card() + "\"";
      case Entry.CommunityChestCardDrawn it ->
          name(it.player()) + " draws the community chest card \"" + it.card() + "\"";
      case Entry.BankPaid it ->
          name(it.player()) + " pays the bank $" + it.amount().amount();
      case Entry.JailEntered it ->
          name(it.player()) + " is sent to jail from landing on " + jailCauseName(it.cause());
      case Entry.JailFinePaid it ->
          name(it.player()) + " leaves jail by paying the $" + it.fine().amount() + " fine";
      case Entry.Bankrupt it -> name(it.player()) + " goes bankrupt to "
          + (it.creditor() == null ? "the bank" : name(it.creditor()));
      case Entry.Won it -> name(it.player()) + " wins the game";
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

  private static String boardSpaceName(Street.Type space) {
    return switch (space) {
      case start -> "Start";
      case RueGrandeDinant -> "Rue Grande Dinant";
      case AlgemeenFonds -> "Algemeen Fonds / Caisse de Communauté";
      case DiestsestraatLeuven -> "Diestsestraat Leuven";
      case InkomstenBelasting -> "Inkomsten Belasting / Impôts sur le revenu";
      case NoordStation -> "Noord Station / Gare du Nord";
      case SteenstraatBrugge -> "Steenstraat Brugge";
      case Kans -> "Kans / Chance";
      case PlaceDuMonumentSpa -> "Place du Monument Spa";
      case KapellestraatOostende -> "Kapellestraat Oostende";
      case OpBezoek -> "Op Bezoek / Simple Visite";
      case RueDeDiekirchArlon -> "Rue de Diekirch Arlon";
      case Elektriciteitscentrale -> "Elektriciteitscentrale / Centrale Électrique";
      case BruulMechelen -> "Bruul Mechelen";
      case PlaceVerteVerviers -> "Place Verte Verviers";
      case CentraalStation -> "Centraal Station / Gare Centrale";
      case LippenslaanKnokke -> "Lippenslaan Knokke";
      case RueRoyaleTournai -> "Rue Royale Tournai";
      case GroenplaatsAntwerpen -> "Groenplaats Antwerpen";
      case GratisParkeren -> "Gratis Parkeren / Parc Gratuit";
      case RueStLeonardLiege -> "Rue St-Léonard Liège";
      case LangeSteenstraatKortrijk -> "Lange Steenstraat Kortrijk";
      case GrandPlaceMons -> "Grand Place Mons";
      case Buurtspoorwegen -> "Buurtspoorwegen / Tramways Vicinaux";
      case GroteMarktHasselt -> "Grote Markt Hasselt";
      case PlaceDeLAngeNamur -> "Place de l'Ange Namur";
      case Watermaatschappij -> "Watermaatschappij / Compagnie des Eaux";
      case HoogstraatBrussel -> "Hoogstraat (Brussel) / Rue Haute (Bruxelles)";
      case NaarDeGevangenis -> "Naar de Gevangenis / Allez en Prison";
      case BoulevardTirouCharleroi -> "Boulevard Tirou Charleroi";
      case VeldstraatGent -> "Veldstraat Gent";
      case BoulevardDAvroyLiege -> "Boulevard d'Avroy Liège";
      case ZuidStation -> "Zuid Station / Gare du Midi";
      case MeirAntwerpen -> "Meir Antwerpen";
      case ExtraBelasting -> "Extra Belasting / Taxe de Luxe";
      case NieuwstraatBrussel -> "Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)";
    };
  }

  private static String jailCauseName(Street.Type cause) {
    if (cause == Street.Type.NaarDeGevangenis) return "Naar de Gevangenis / Allez en Prison";
    return spaceName(cause);
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
moduleHash=1b139c1b5490182c9a2dd6846e88362a853d078de2bc1e7d7b481551f7bfe0b7
scope.0.id=Y2xhc3M6UmVwb3J0I1JlcG9ydDoxOA
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=113
scope.0.semanticHash=9c3cf35d0f361265873881f786cf8b59872e416982eab34b14cecb20598653c4
scope.1.id=bWV0aG9kOlJlcG9ydCNjdG9yKDApOjE5
scope.1.kind=method
scope.1.startLine=19
scope.1.endLine=20
scope.1.semanticHash=fa4ab1d8c774b5a49e26e7b36ba0ec25ba0d7069b207d38eb37beccb7d02dc9c
scope.2.id=bWV0aG9kOlJlcG9ydCNqYWlsQ2F1c2VOYW1lKDEpOjEwMQ
scope.2.kind=method
scope.2.startLine=101
scope.2.endLine=104
scope.2.semanticHash=aa983184321017d5720f121ab418e15d8f0b7a0307e4885e24e3eeb6e4c96601
scope.3.id=bWV0aG9kOlJlcG9ydCNsaW5lKDEpOjQz
scope.3.kind=method
scope.3.startLine=43
scope.3.endLine=89
scope.3.semanticHash=10974e17b8424dba5649b7083f54e88e7414fffb4ffabd0475ca23e0f9eb13f1
scope.4.id=bWV0aG9kOlJlcG9ydCNuYW1lKDEpOjExMA
scope.4.kind=method
scope.4.startLine=110
scope.4.endLine=112
scope.4.semanticHash=cd39b08d1576cbc2e9d2ae25bb54b6b608f1219509ee3d03086e8f3ab4c238fa
scope.5.id=bWV0aG9kOlJlcG9ydCNuYW1lcygxKToxMDY
scope.5.kind=method
scope.5.startLine=106
scope.5.endLine=108
scope.5.semanticHash=7baca973d9baa23bf1205536bbfd229d494b1e2c863e02f6e50c5ce7dcb42959
scope.6.id=bWV0aG9kOlJlcG9ydCNvZigxKToyMg
scope.6.kind=method
scope.6.startLine=22
scope.6.endLine=24
scope.6.semanticHash=3bddf559e6362200e39e3d2024808e23da1e3da3deb262e8b89f1402e9edd25e
scope.7.id=bWV0aG9kOlJlcG9ydCNzcGFjZU5hbWUoMSk6OTc
scope.7.kind=method
scope.7.startLine=97
scope.7.endLine=99
scope.7.semanticHash=56eb8b748063de6798efd6f1a96d5a30f07cf019c0478f7f004006d2fe61eaf8
*/
