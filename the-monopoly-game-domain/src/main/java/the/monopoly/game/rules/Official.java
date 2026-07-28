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
moduleHash=5d351d0e1d007f6fd77cd405bdd24f3159f061878dd65995272d45a3ec719111
scope.0.id=Y2xhc3M6T2ZmaWNpYWwjT2ZmaWNpYWw6MjA
scope.0.kind=class
scope.0.startLine=20
scope.0.endLine=84
scope.0.semanticHash=68c9b31420aa9d325205647b26e5aa3fcdcd31153ca3eb518f89a69fce118619
scope.1.id=Y2xhc3M6T2ZmaWNpYWwuRG91YmxlU2FsYXJ5V2hlbkxhbmRpbmdPblN0YXJ0I0RvdWJsZVNhbGFyeVdoZW5MYW5kaW5nT25TdGFydDo3OA
scope.1.kind=class
scope.1.startLine=78
scope.1.endLine=83
scope.1.semanticHash=35aca69b99beeef4af6071c9351a6a6b674a5c49693bca4923e0a7a76dc99108
scope.2.id=bWV0aG9kOk9mZmljaWFsI2NyZWF0ZSgwKToyMQ
scope.2.kind=method
scope.2.startLine=21
scope.2.endLine=76
scope.2.semanticHash=1672a653dbc211eac54682a443677f1cc95a526a0120d0b3930e0d01c7b06fe2
scope.3.id=bWV0aG9kOk9mZmljaWFsI2N0b3IoMCk6MjA
scope.3.kind=method
scope.3.startLine=1
scope.3.endLine=84
scope.3.semanticHash=e65279958e842ab417fa87f527e3d96c69dd42db39af0dbae71e90b3098f9aad
scope.4.id=bWV0aG9kOk9mZmljaWFsLkRvdWJsZVNhbGFyeVdoZW5MYW5kaW5nT25TdGFydCNjdG9yKDApOjc4
scope.4.kind=method
scope.4.startLine=1
scope.4.endLine=84
scope.4.semanticHash=e65279958e842ab417fa87f527e3d96c69dd42db39af0dbae71e90b3098f9aad
scope.5.id=bWV0aG9kOk9mZmljaWFsLkRvdWJsZVNhbGFyeVdoZW5MYW5kaW5nT25TdGFydCNwcm9jZXNzKDEpOjc5
scope.5.kind=method
scope.5.startLine=79
scope.5.endLine=82
scope.5.semanticHash=1b0efb763135fc6b421585ff6bfc1cd9acb7ecdba1b66ad627a695b770db3633
*/
