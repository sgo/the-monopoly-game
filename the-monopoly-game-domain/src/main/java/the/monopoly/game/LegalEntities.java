package the.monopoly.game;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.DevelopmentLoanBook;
import the.monopoly.game.rules.LegalEntity;
import the.monopoly.game.rules.MonopolyBuyout;
import the.monopoly.game.rules.PeerTrading;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Optional;

/**
 * How legal entities behave in the market: a peer trade or monopoly buyout a
 * trader may resolve at the start of their turn, an entity forming when the
 * board deadlocks at a quiet round boundary, and an entity servicing its own
 * development loans or otherwise operating on the round it gets a turn.
 * Owning this here keeps {@link Game} narrated to the turn loop rather than
 * every legal-entity mechanic.
 */
final class LegalEntities {
  private final Rule.Set rules;
  private final Deeds deeds;
  private final List<Player> players;
  private final Strategy.OfPlayers strategies;
  private final DevelopmentLoanBook developmentLoanBook;
  private final boolean stalemateTrading;
  private final boolean legalEntityTrading;

  LegalEntities(Rule.Set rules, Deeds deeds, List<Player> players, Strategy.OfPlayers strategies,
               DevelopmentLoanBook developmentLoanBook, boolean stalemateTrading, boolean legalEntityTrading) {
    this.rules = rules;
    this.deeds = deeds;
    this.players = players;
    this.strategies = strategies;
    this.developmentLoanBook = developmentLoanBook;
    this.stalemateTrading = stalemateTrading;
    this.legalEntityTrading = legalEntityTrading;
  }

  /** Trades or resolves a monopoly buyout for this trader, if turn-start trading applies. Reports whether it consolidated ownership. */
  boolean resolveSplitOwnershipAtStart(Player trader, List<Player> turnOrder, Journalling journalling) {
    if (tradeAtStart(trader, turnOrder, journalling)) return true;
    return resolveBuyoutAtStart(trader, turnOrder, journalling);
  }

  private boolean tradeAtStart(Player trader, List<Player> turnOrder, Journalling journalling) {
    if (!stalemateTrading || !allOwnableSpacesOwned()) return false;
    return PeerTrading.select(trader, strategies.forPlayer(trader), turnOrder, rules, deeds)
        .map(offer -> {
          completeTrade(trader, offer, journalling);
          return true;
        }).orElse(false);
  }

  private boolean resolveBuyoutAtStart(Player trader, List<Player> turnOrder, Journalling journalling) {
    if (!stalemateTrading || !allOwnableSpacesOwned()) return false;
    if (!(strategies.forPlayer(trader) instanceof Greedo)) return false;
    if (isTiedWithItsPartner(trader, turnOrder)) return false;
    List<Player> partners = turnOrder.stream().filter(partner -> partner != trader).toList();
    return resolvableBuyout(trader, partners)
        .map(outcome -> applyBuyout(outcome, journalling))
        .orElseGet(() -> {
          anySplitExists(trader, partners);
          return false;
        });
  }

  private Optional<MonopolyBuyout.Outcome> resolvableBuyout(Player trader, List<Player> partners) {
    return partners.stream()
        .map(partner -> MonopolyBuyout.resolve(trader, partner, rules, deeds))
        .filter(Optional::isPresent).map(Optional::orElseThrow).findFirst();
  }

  private boolean anySplitExists(Player trader, List<Player> partners) {
    return partners.stream().anyMatch(partner -> MonopolyBuyout.hasSplit(trader, partner, rules, deeds));
  }

  /**
   * Turn-start resolution leaves an equal-cash tie to the established peer-trade
   * behavior — but only when every other player is tied; a lower-balance partner
   * elsewhere in turn order still leaves a real buyout to resolve against.
   */
  private boolean isTiedWithItsPartner(Player trader, List<Player> turnOrder) {
    return turnOrder.stream().filter(partner -> partner != trader)
        .allMatch(partner -> partner.account().balance().amount()
            .equals(trader.account().balance().amount()));
  }

  private boolean applyBuyout(MonopolyBuyout.Outcome outcome, Journalling journalling) {
    journalling.splitMonopolyWon(outcome.winner(), outcome.loser());
    if (!outcome.payment().equals(Money.ZERO)) journalling.splitMonopolyPaid(
        outcome.winner(), outcome.loser(), outcome.payment());
    return true;
  }

  private void completeTrade(Player trader, Strategy.TradeOffer offer, Journalling journalling) {
    deeds.transferWithoutPayment(offer.offered(), trader, offer.partner());
    deeds.transferWithoutPayment(offer.wanted(), offer.partner(), trader);
    journalling.peerTrade(trader, offer.offered(), offer.partner(), offer.wanted());
  }

  private boolean allOwnableSpacesOwned() {
    return rules.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .allMatch(it -> !deeds.isUnowned(it.type()));
  }

  /** Applies the automatic legal-entity formation check at a completed quiet round boundary. */
  void resolveMarketDeadlockAtRoundBoundary(boolean quietRound, boolean collectiveFunding, Journalling journalling) {
    if (!canFormAtMarketDeadlock(quietRound, collectiveFunding)) return;
    fundableEntityAtMarketDeadlock().ifPresent(entity -> {
      deeds.form(entity);
      if (journalling != null) journalling.entityFormed(entity);
    });
  }

  private boolean canFormAtMarketDeadlock(boolean quietRound, boolean collectiveFunding) {
    return quietRound && collectiveFunding && legalEntityTrading && allOwnableSpacesOwned();
  }

  private Optional<LegalEntity> fundableEntityAtMarketDeadlock() {
    return rules.streets().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast)
        .map(ColourStreet::colourGroup).distinct()
        .map(this::formIfFundable)
        .filter(Optional::isPresent).map(Optional::orElseThrow).findFirst();
  }

  private Optional<LegalEntity> formIfFundable(Street.Colour colour) {
    List<ColourStreet> streets = LegalEntity.streetsOf(colour, rules);
    List<Player> shareholders = players.stream()
        .filter(player -> streets.stream().anyMatch(street -> deeds.ownerOf(street.type())
            .filter(player.id()::equals).isPresent()))
        .toList();
    return LegalEntity.form(entityName(colour), colour, shareholders, rules, deeds,
        street -> Strategy.priorityOf(street) == Strategy.Priority.HIGHEST)
        .filter(entity -> entity.canFundNextImprovement(strategies, rules, deeds));
  }

  private static String entityName(Street.Colour colour) {
    String name = colour.name().replace('_', ' ');
    return Character.toUpperCase(name.charAt(0)) + name.substring(1) + " Realty";
  }

  /** Services each entity's outstanding development loans, then lets it operate normally if none needed servicing this round. */
  boolean operateLegalEntities(Journalling journalling) {
    deeds.legalEntities().forEach(entity -> {
      boolean loanServiced = developmentLoanBook.positions().stream()
          .filter(position -> position.entity() == entity && !position.outstanding().equals(Money.ZERO))
          .map(position -> serviceEntityDevelopmentLoan(position, journalling))
          .reduce(false, (serviced, current) -> serviced || current);
      if (!loanServiced) operateEntity(entity, journalling);
    });
    return true;
  }

  private boolean serviceEntityDevelopmentLoan(DevelopmentLoanBook.Position position, Journalling journalling) {
    Optional<DevelopmentLoanBook.Payment> payment = developmentLoanBook.service(position);
    if (payment.isEmpty()) {
      mortgageEntitySpareProperty(position);
      payment = developmentLoanBook.service(position);
    }
    if (payment.isPresent()) {
      journalling.serviceDevelopmentLoan(position, payment.orElseThrow());
      return true;
    }
    DevelopmentLoanBook.Foreclosure foreclosure =
        developmentLoanBook.forecloseEntity(position, deeds, rules, players, strategies);
    journalling.developmentLoanDefaulted(position);
    journalling.developmentLoanRecovered(position, foreclosure.recovered());
    return true;
  }

  private void mortgageEntitySpareProperty(DevelopmentLoanBook.Position position) {
    LegalEntity entity = position.entity();
    rules.streets()
        .filter(Ownable.class::isInstance)
        .map(Ownable.class::cast)
        .filter(land -> deeds.entityOwnerOf(land.type()).filter(entity::equals).isPresent())
        .filter(land -> land.type() != position.collateral())
        .filter(land -> !deeds.isMortgaged(land))
        .findFirst()
        .ifPresent(land -> deeds.mortgage(land, entity));
  }

  private void operateEntity(LegalEntity entity, Journalling journalling) {
    if (!entity.hasShareholders()) return;
    journalOperation(entity, journalling);
  }

  private void journalOperation(LegalEntity entity, Journalling journalling) {
    switch (entity.operate(deeds, strategies, rules, developmentLoanBook, players)) {
      case LegalEntity.Operation.LoanRepaid it ->
          journalling.entityLoanRepaid(entity, it.shareholder(), it.principal(), it.repayment());
      case LegalEntity.Operation.HouseBuilt it -> journalling.entityHouseBuilt(entity, it.street());
      case LegalEntity.Operation.LoanRaisedAndHouseBuilt it -> {
        journalling.entityLoanRaised(entity, it.loan());
        journalling.entityHouseBuilt(entity, it.street());
      }
      case LegalEntity.Operation.DevelopmentLoanRaisedAndHouseBuilt it -> {
        journalling.entityDevelopmentLoanRaised(entity, it.position());
        journalling.entityHouseBuilt(entity, it.street());
      }
      case LegalEntity.Operation.DividendPaid it -> journalling.entityDividendPaid(entity, it.amount());
      case LegalEntity.Operation.NoAction ignored -> { }
    }
  }
}
