package the.monopoly.game.rules;

import the.monopoly.game.components.board.Board;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

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
    Bank.Simple bank = new Bank.Simple();
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
    return new Rule.Set.Simple(
        board,
        Stream.of(six, six).map(Dice.Type::create).toList(),
        new Player.Pool(2, 8, bank, new Money(1500)),
        bank,
        new LinkedHashSet<>(),
        Map.of(
            double_salary_when_landing_on_start, new DoubleSalaryWhenLandingOnStart()
        )
    );
  }

  public static class DoubleSalaryWhenLandingOnStart implements Rule {
    @Override
    public <T> T process(Processor<T> processor) {
      return processor.process(this);
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=10f6416bd6fc3b77bf1b8f6c398991d84ebe89fd8e17486235643ab09a8d3a75
scope.0.id=Y2xhc3M6T2ZmaWNpYWwjT2ZmaWNpYWw6MTk
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=83
scope.0.semanticHash=67b8a1999989b82b10a10d9d5190fedeb7d1b8ca894ae7296ceff3f9e3b7eecc
scope.1.id=Y2xhc3M6T2ZmaWNpYWwuRG91YmxlU2FsYXJ5V2hlbkxhbmRpbmdPblN0YXJ0I0RvdWJsZVNhbGFyeVdoZW5MYW5kaW5nT25TdGFydDo3Nw
scope.1.kind=class
scope.1.startLine=77
scope.1.endLine=82
scope.1.semanticHash=35aca69b99beeef4af6071c9351a6a6b674a5c49693bca4923e0a7a76dc99108
scope.2.id=bWV0aG9kOk9mZmljaWFsI2NyZWF0ZSgwKToyMA
scope.2.kind=method
scope.2.startLine=20
scope.2.endLine=75
scope.2.semanticHash=36a168be153ab19e53a57079408852eecdaca503d2b81bb5cae31e8b7bafad5c
scope.3.id=bWV0aG9kOk9mZmljaWFsI2N0b3IoMCk6MTk
scope.3.kind=method
scope.3.startLine=1
scope.3.endLine=83
scope.3.semanticHash=a61a7b553c84fce273c77a7aeea97c3df66919aaebb9bb33915e377ab51257c8
scope.4.id=bWV0aG9kOk9mZmljaWFsLkRvdWJsZVNhbGFyeVdoZW5MYW5kaW5nT25TdGFydCNjdG9yKDApOjc3
scope.4.kind=method
scope.4.startLine=1
scope.4.endLine=83
scope.4.semanticHash=a61a7b553c84fce273c77a7aeea97c3df66919aaebb9bb33915e377ab51257c8
scope.5.id=bWV0aG9kOk9mZmljaWFsLkRvdWJsZVNhbGFyeVdoZW5MYW5kaW5nT25TdGFydCNwcm9jZXNzKDEpOjc4
scope.5.kind=method
scope.5.startLine=78
scope.5.endLine=81
scope.5.semanticHash=1b0efb763135fc6b421585ff6bfc1cd9acb7ecdba1b66ad627a695b770db3633
*/
