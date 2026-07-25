package the.monopoly.game.rules;

import the.monopoly.game.components.board.Board;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static the.monopoly.game.components.dice.Dice.Type.six;
import static the.monopoly.game.components.streets.Street.Type.*;
import static the.monopoly.game.rules.Rule.Type.double_salary_when_landing_on_start;

public class Official implements Rule.Set.Factory {
  @Override
  public Rule.Set create() {
    Bank.Simple bank = new Bank.Simple(new HashSet<>());
    Board board = new Board(List.of(
        start,
        RueGrandeDinant,
        AlgemeenFonds,
        DiestsestraatLeuven,
        InkomstenBelasting,
        NoordStation,
        SteenstraatBrugge,
        Kans,
        PlaceDuMonumentSpa,
        KapellestraatOostende,
        OpBezoek,
        RueDeDiekirchArlon,
        Elektriciteitscentrale,
        BruulMechelen,
        PlaceVerteVerviers,
        CentraalStation,
        LippenslaanKnokke,
        AlgemeenFonds,
        RueRoyaleTournai,
        GroenplaatsAntwerpen,
        GratisParkeren,
        RueStLeonardLiege,
        Kans,
        LangeSteenstraatKortrijk,
        GrandPlaceMons,
        Buurtspoorwegen,
        GroteMarktHasselt,
        PlaceDeLAngeNamur,
        Watermaatschappij,
        HoogstraatBrussel,
        NaarDeGevangenis,
        BoulevardTirouCharleroi,
        VeldstraatGent,
        AlgemeenFonds,
        BoulevardDAvroyLiege,
        ZuidStation,
        Kans,
        MeirAntwerpen,
        ExtraBelasting,
        NieuwstraatBrussel
    ));
    Rule.Set.Simple ruleSet = new Rule.Set.Simple(
        board,
        Stream.of(six, six).map(Dice.Type::create).toList(),
        new Player.Pool(2, 8, bank, new Money(1500)),
        bank,
        new LinkedHashSet<>(),
        Map.of(
            double_salary_when_landing_on_start, new DoubleSalaryWhenLandingOnStart()
        )
    );
    board.ruleSet(ruleSet);
    return ruleSet;
  }

  public static class DoubleSalaryWhenLandingOnStart implements Rule {
    @Override
    public <T> T process(Processor<T> processor) {
      return processor.process(this);
    }
  }
}
