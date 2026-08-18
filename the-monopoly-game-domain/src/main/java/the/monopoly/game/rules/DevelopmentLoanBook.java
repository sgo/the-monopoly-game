package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tracks development loans independently from ordinary bankruptcy debt. The
 * collateral and bond are attached to one position, while recycled capital is
 * held by the bank until it funds another position.
 */
public final class DevelopmentLoanBook {
  private static final Bank.Account.Owner BANK_OWNER = new Bank.Account.Owner("development loans");

  private final Bank bank;
  private final List<Position> positions = new ArrayList<>();
  private Money recycledCapital = Money.ZERO;

  public DevelopmentLoanBook(Bank bank) {
    this.bank = bank;
    bank.createAccountFor(BANK_OWNER);
  }

  public Money bankBalance() {
    return bank.accountOf(BANK_OWNER).balance().amount();
  }

  public void setBankBalance(Money amount) {
    Money current = bankBalance();
    if (amount.exceeds(current)) bank.accountOf(BANK_OWNER).deposit(amount.minus(current));
    else if (current.exceeds(amount)) bank.accountOf(BANK_OWNER).withdraw(current.minus(amount));
  }

  public Money recycledCapital() {
    return recycledCapital;
  }

  public void setRecycledCapital(Money amount) {
    if (amount.cents() < 0) throw new IllegalArgumentException("Recycled capital cannot be negative.");
    recycledCapital = amount;
  }

  public List<Position> positions() {
    return List.copyOf(positions);
  }

  public Optional<Position> securedBy(Street.Type collateral) {
    return positions.stream().filter(position -> position.collateral() == collateral).findFirst();
  }

  public void assignBondholder(Position position, Player bondholder) {
    int index = positions.indexOf(position);
    if (index < 0) throw new IllegalArgumentException("Unknown development-loan position.");
    positions.set(index, new Position(position.borrowerName(), position.borrower(), position.entity(),
        position.collateral(), position.loan(), bondholder));
  }

  public Position recordPlayerLoan(Player borrower, Street.Type collateral, Money principal, int yearsServiced,
                                   Player bondholder) {
    Position position = new Position(borrower.id().value(), borrower, null, collateral,
        new DevelopmentLoan(principal, yearsServiced), bondholder);
    positions.add(position);
    return position;
  }

  public Position recordEntityLoan(LegalEntity borrower, Street.Type collateral, Money principal, int yearsServiced,
                                   Player bondholder) {
    Position position = new Position(borrower.name(), null, borrower, collateral,
        new DevelopmentLoan(principal, yearsServiced), bondholder);
    positions.add(position);
    return position;
  }

  /** Whether the requested house can be financed without changing any account. */
  public boolean canRaise(Player borrower, ColourStreet street, boolean fullDraw, List<Player> players) {
    return fundingFor(borrower.account().balance().amount(), borrower.id(), street,
        fullDraw, players).isPresent();
  }

  /** Raises the smallest permitted loan, or the full 80% cap when requested. */
  public Optional<Position> raise(Player borrower, ColourStreet street, boolean fullDraw,
                                  List<Player> players) {
    Optional<Funding> funding = fundingFor(borrower.account().balance().amount(), borrower.id(), street,
        fullDraw, players);
    if (funding.isEmpty()) return Optional.empty();
    Funding it = funding.orElseThrow();
    if (!it.fresh().equals(Money.ZERO)) it.bondholder().account().withdraw(it.fresh());
    recycledCapital = recycledCapital.minus(it.recycled());
    borrower.account().deposit(it.principal());
    return Optional.of(recordPlayerLoan(borrower, street.type(), it.principal(), 0, it.bondholder()));
  }

  public boolean canRaise(LegalEntity borrower, Money constructionCost, Street.Type collateral,
                          boolean fullDraw, List<Player> players) {
    return fundingFor(borrower.bankBalance(), borrower, constructionCost, collateral, fullDraw, players).isPresent();
  }

  public Optional<Position> raise(LegalEntity borrower, Money constructionCost, Street.Type collateral,
                                  boolean fullDraw, List<Player> players) {
    Optional<Funding> funding = fundingFor(borrower.bankBalance(), borrower, constructionCost, collateral,
        fullDraw, players);
    if (funding.isEmpty()) return Optional.empty();
    Funding it = funding.orElseThrow();
    if (!it.fresh().equals(Money.ZERO)) it.bondholder().account().withdraw(it.fresh());
    recycledCapital = recycledCapital.minus(it.recycled());
    borrower.depositToBank(it.principal());
    return Optional.of(recordEntityLoan(borrower, collateral, it.principal(), 0, it.bondholder()));
  }

  private Optional<Funding> fundingFor(Money available, Player.ID borrower, ColourStreet street,
                                       boolean fullDraw, List<Player> players) {
    return fundingFor(available, borrower, street, fullDraw, players, candidate -> true);
  }

  private Optional<Funding> fundingFor(Money available, LegalEntity borrower, Money constructionCost,
                                       Street.Type collateral, boolean fullDraw, List<Player> players) {
    return fundingFor(available, null, constructionCost, collateral, fullDraw, players,
        candidate -> borrower.shareholders().stream().noneMatch(shareholder -> shareholder.id().equals(candidate.id())));
  }

  private Optional<Funding> fundingFor(Money available, Player.ID borrower, ColourStreet street,
                                       boolean fullDraw, List<Player> players,
                                       java.util.function.Predicate<Player> eligibleBondholder) {
    return fundingFor(available, borrower, street.houseConstructionCost(), street.type(), fullDraw, players,
        eligibleBondholder);
  }

  private Optional<Funding> fundingFor(Money available, Player.ID borrower, Money cost, Street.Type collateral,
                                       boolean fullDraw, List<Player> players,
                                       java.util.function.Predicate<Player> eligibleBondholder) {
    if (positions.stream().anyMatch(position -> position.collateral() == collateral
        && !position.outstanding().equals(Money.ZERO))) return Optional.empty();
    if (available.covers(cost)) return Optional.empty();
    Money cap = cost.percentage(80);
    Money shortfall = cost.minus(available);
    if (!fullDraw && shortfall.exceeds(cap)) return Optional.empty();
    Money principal = fullDraw ? cap : shortfall;
    if (principal.equals(Money.ZERO)) return Optional.empty();

    Money recycled = recycledCapital.covers(principal) ? principal : recycledCapital;
    Money fresh = principal.minus(recycled);
    Player bondholder = fresh.equals(Money.ZERO) ? null : players.stream()
        .filter(candidate -> borrower == null || !candidate.id().equals(borrower))
        .filter(eligibleBondholder)
        .filter(candidate -> candidate.account().balance().amount().covers(fresh))
        .findFirst().orElse(null);
    if (!fresh.equals(Money.ZERO) && bondholder == null) return Optional.empty();
    return Optional.of(new Funding(principal, recycled, fresh, bondholder));
  }

  private record Funding(Money principal, Money recycled, Money fresh, Player bondholder) {
  }

  public Optional<Payment> service(Position position) {
    Money total = paymentDue(position);
    if (!borrowerBalance(position).covers(total)) return Optional.empty();

    DevelopmentLoan.Payment payment = position.loan().serviceNextYear();
    withdrawFromBorrower(position, payment.borrowerTotal());
    if (position.bondholder() != null)
      position.bondholder().account().deposit(payment.bondInterest().plus(payment.principal()));
    bank.accountOf(BANK_OWNER).deposit(payment.bankSpread());
    return Optional.of(new Payment(payment.interest(), payment.principal(), payment.bondInterest(), payment.bankSpread()));
  }

  public Money paymentDue(Position position) {
    DevelopmentLoan loan = position.loan();
    Money interest = loan.outstanding().percentage(5);
    Money scheduledPrincipal = loan.originalPrincipal().percentage(5);
    if (scheduledPrincipal.cents() < 100) scheduledPrincipal = Money.fromCents(100);
    Money principal = loan.outstanding().covers(scheduledPrincipal) ? scheduledPrincipal : loan.outstanding();
    return interest.plus(principal);
  }

  private Money borrowerBalance(Position position) {
    if (position.borrower() != null) return position.borrower().account().balance().amount();
    return position.entity().bankBalance();
  }

  private void withdrawFromBorrower(Position position, Money amount) {
    if (position.borrower() != null) position.borrower().account().withdraw(amount);
    else position.entity().withdrawFromBank(amount);
  }

  /** Forecloses one loan's collateral package and leaves every other holding untouched. */
  public Foreclosure foreclose(Position position, Deeds deeds, Rule.Set rules, List<Player> players,
                               Strategy.OfPlayers strategies) {
    if (position.borrower() == null) throw new IllegalArgumentException("Only player loans can be foreclosed here.");
    Player borrower = position.borrower();
    ColourStreet collateral = (ColourStreet) rules.create(position.collateral());
    Money proceeds = Money.ZERO;
    if (deeds.hasHotelOn(collateral)) {
      Money refund = deeds.exchangeHotelForHouses(collateral, borrower);
      borrower.account().withdraw(refund);
      proceeds = proceeds.plus(refund);
    }
    while (deeds.housesBuiltOn(collateral) > 0) {
      Money refund = deeds.sellHouse(collateral, borrower);
      borrower.account().withdraw(refund);
      proceeds = proceeds.plus(refund);
    }

    Ownable land = (Ownable) collateral;
    deeds.arrangeMortgaged(land);
    Auction.Bidders bidders = Auction.qualified(players.stream()
            .filter(candidate -> !candidate.id().equals(borrower.id()))
            .filter(candidate -> !deeds.isBankrupt(candidate)).toList(), land,
        candidate -> strategies.forPlayer(candidate).bidForAuction(
            new Strategy.Offer(land, candidate.account().balance().amount()), candidate, rules, deeds), false);
    if (bidders.players().isEmpty()) {
      deeds.returnToBank(land, borrower);
    } else {
      Auction.Result result = Auction.ascend(bidders);
      deeds.sell(land, result.winner(), result.bid());
      proceeds = proceeds.plus(result.bid());
    }
    Money obligation = position.bondholder() == null
        ? position.outstanding() : position.outstanding().plus(position.outstanding().percentage(5));
    Foreclosure result = recover(position, proceeds, obligation);
    if (proceeds.covers(obligation)) bank.accountOf(BANK_OWNER).deposit(obligation);
    return result;
  }

  public Foreclosure forecloseEntity(Position position, Deeds deeds, Rule.Set rules, List<Player> players,
                                      Strategy.OfPlayers strategies) {
    if (position.entity() == null) throw new IllegalArgumentException("Only entity loans can be foreclosed here.");
    LegalEntity borrower = position.entity();
    ColourStreet collateral = (ColourStreet) rules.create(position.collateral());
    Money proceeds = Money.ZERO;
    if (deeds.hasHotelOn(collateral)) proceeds = proceeds.plus(deeds.exchangeHotelForHouses(collateral, borrower));
    while (deeds.housesBuiltOn(collateral) > 0) proceeds = proceeds.plus(deeds.sellHouse(collateral, borrower));
    Ownable land = collateral;
    deeds.arrangeMortgaged(land);
    Auction.Bidders bidders = Auction.qualified(players, land,
        candidate -> strategies.forPlayer(candidate).bidForAuction(
            new Strategy.Offer(land, candidate.account().balance().amount()), candidate, rules, deeds), false);
    if (bidders.players().isEmpty()) deeds.returnToBank(land, borrower);
    else {
      Auction.Result result = Auction.ascend(bidders);
      deeds.sell(land, result.winner(), result.bid());
      proceeds = proceeds.plus(result.bid());
    }
    Money obligation = position.bondholder() == null
        ? position.outstanding() : position.outstanding().plus(position.outstanding().percentage(5));
    Foreclosure result = recover(position, proceeds, obligation);
    if (proceeds.covers(obligation)) bank.accountOf(BANK_OWNER).deposit(obligation);
    return result;
  }

  public Foreclosure recover(Position position, Money proceeds) {
    return recover(position, proceeds, position.outstanding());
  }

  private Foreclosure recover(Position position, Money proceeds, Money obligation) {
    Money recoveredFromSale = proceeds.covers(obligation) ? obligation : proceeds;
    Money shortfall = obligation.minus(recoveredFromSale);
    Money reserveContribution = bankBalance().covers(shortfall) ? shortfall : bankBalance();
    if (!reserveContribution.equals(Money.ZERO)) bank.accountOf(BANK_OWNER).withdraw(reserveContribution);
    Money recovered = recoveredFromSale.plus(reserveContribution);
    Money surplus = proceeds.minus(recoveredFromSale);
    recycledCapital = recycledCapital.plus(recovered);
    position.loan().serviceToZero();
    if (!surplus.equals(Money.ZERO)) {
      if (position.borrower() != null) position.borrower().account().deposit(surplus);
      else position.entity().depositToBank(surplus);
    }
    return new Foreclosure(recovered, surplus);
  }

  public record Foreclosure(Money recovered, Money surplus) {
  }

  public record Payment(Money interest, Money principal, Money bondInterest, Money bankSpread) {
  }

  public record Position(String borrowerName, Player borrower, LegalEntity entity,
                         Street.Type collateral, DevelopmentLoan loan, Player bondholder) {
    public boolean isEntityLoan() {
      return entity != null;
    }

    public Money outstanding() {
      return loan.outstanding();
    }
  }
}
