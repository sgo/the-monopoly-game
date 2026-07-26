package the.monopoly.game.rules;

import the.monopoly.game.components.board.Board;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Rule {
  <T> T process(Processor<T> processor);

  enum Type {
    double_salary_when_landing_on_start
  }

  interface Processor<T> {
    T process(Official.DoubleSalaryWhenLandingOnStart rule);
  }

  interface Set {
    Stream<Dice> dice();

    Player.Pool players();

    Bank bank();

    void activate(Rule.Type type);

    Street create(Street.Type type);

    Board gameboard();

    /** The board laid out as playable spaces under the rules now in force. */
    Stream<Street> streets();

    enum Type implements Factory {
      official(new Official());

      private final Factory factory;

      Type(Factory factory) {
        this.factory = factory;
      }

      @Override
      public Set create() {
        return factory.create();
      }
    }

    interface Factory {
      Set create();
    }

    record Simple(
        Board board,
        List<Dice> diceBuffer,
        Player.Pool players,
        Bank bank,
        java.util.Set<Rule> activatedRules,
        Map<Rule.Type, Rule> optionalRules
    ) implements Set {
      @Override
      public Stream<Dice> dice() {
        return diceBuffer.stream();
      }

      @Override
      public void activate(Rule.Type type) {
        activatedRules.add(optionalRules.get(type));
      }

      @Override
      public Street create(Street.Type type) {
        return type.create(activatedRules);
      }

      @Override
      public Board gameboard() {
        return board;
      }

      @Override
      public Stream<Street> streets() {
        return board.spaces().map(this::create);
      }
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=28cb57f95437cfc70e2825a7ea3b3df023ef8447c5ea6d8db9957f3e62d83b94
scope.0.id=Y2xhc3M6UnVsZSNSdWxlOjEz
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=93
scope.0.semanticHash=8c7991df701dabc38b7af6382bf620552c80bccf9906cf69bc6c396658cf0a69
scope.1.id=Y2xhc3M6UnVsZS5Qcm9jZXNzb3IjUHJvY2Vzc29yOjIw
scope.1.kind=class
scope.1.startLine=20
scope.1.endLine=22
scope.1.semanticHash=0986a28be5b883fe938c6cb17269e7de0369fa0367eed9c6dda818af80ff17c5
scope.2.id=Y2xhc3M6UnVsZS5TZXQjU2V0OjI0
scope.2.kind=class
scope.2.startLine=24
scope.2.endLine=92
scope.2.semanticHash=a77d4b28da30f4a7287c94db9667a72e69909601c75fae858eb8da68da665bba
scope.3.id=Y2xhc3M6UnVsZS5TZXQuRmFjdG9yeSNGYWN0b3J5OjU1
scope.3.kind=class
scope.3.startLine=55
scope.3.endLine=57
scope.3.semanticHash=b1ec6e472291822b2742ff917137da00658df721731dbe27e825bbb71a7758d2
scope.4.id=Y2xhc3M6UnVsZS5TZXQuU2ltcGxlI1NpbXBsZTo1OQ
scope.4.kind=class
scope.4.startLine=59
scope.4.endLine=91
scope.4.semanticHash=d3132beaf534473010a329b7a128cbd45df223951e87abb70be523ba3a6f654a
scope.5.id=Y2xhc3M6UnVsZS5TZXQuVHlwZSNUeXBlOjQw
scope.5.kind=class
scope.5.startLine=40
scope.5.endLine=53
scope.5.semanticHash=bfcbfe9b291c38f4ae3e110e416ad585e4d209341b76b52a380c8d66489770fa
scope.6.id=Y2xhc3M6UnVsZS5UeXBlI1R5cGU6MTY
scope.6.kind=class
scope.6.startLine=16
scope.6.endLine=18
scope.6.semanticHash=22f561de9b54edc182f56923b8b0e99bf3799e190e8ac3e46890e8267f71518b
scope.7.id=ZmllbGQ6UnVsZS5TZXQuU2ltcGxlI2FjdGl2YXRlZFJ1bGVzOjY0
scope.7.kind=field
scope.7.startLine=64
scope.7.endLine=64
scope.7.semanticHash=3b0c5605fd54d0ca8dd3c860b9289a7884093217b983159aac961335eacb2281
scope.8.id=ZmllbGQ6UnVsZS5TZXQuU2ltcGxlI2Jhbms6NjM
scope.8.kind=field
scope.8.startLine=63
scope.8.endLine=63
scope.8.semanticHash=ca2f8e7e1c77cdd8face64b1c7f3c3cd9bf0d26dac78e2d8225f4c334767d5a7
scope.9.id=ZmllbGQ6UnVsZS5TZXQuU2ltcGxlI2JvYXJkOjYw
scope.9.kind=field
scope.9.startLine=60
scope.9.endLine=60
scope.9.semanticHash=c69e60c87a307e3d9047aee56184a020e7d63fb3ebf2e4f502d1abbaeb885039
scope.10.id=ZmllbGQ6UnVsZS5TZXQuU2ltcGxlI2RpY2VCdWZmZXI6NjE
scope.10.kind=field
scope.10.startLine=61
scope.10.endLine=61
scope.10.semanticHash=38829a22cd95280090de75edca7a65b083fd1aec7dc9e4c73d19cb350c1cd2cc
scope.11.id=ZmllbGQ6UnVsZS5TZXQuU2ltcGxlI29wdGlvbmFsUnVsZXM6NjU
scope.11.kind=field
scope.11.startLine=65
scope.11.endLine=65
scope.11.semanticHash=a5fc34698bb13b06181c5522321ce227dec1c78ed8be888807d913fa0f416267
scope.12.id=ZmllbGQ6UnVsZS5TZXQuU2ltcGxlI3BsYXllcnM6NjI
scope.12.kind=field
scope.12.startLine=62
scope.12.endLine=62
scope.12.semanticHash=0ec43aa44de872be73a96c46340e8e3e9b58168b3e93525298cd421d44da4d84
scope.13.id=ZmllbGQ6UnVsZS5TZXQuVHlwZSNmYWN0b3J5OjQz
scope.13.kind=field
scope.13.startLine=43
scope.13.endLine=43
scope.13.semanticHash=58dd23ca4a3e28f281e0e25469be60cefbb9fbc4f450e6b4abe03a46df3e3a0a
scope.14.id=ZmllbGQ6UnVsZS5TZXQuVHlwZSNvZmZpY2lhbDo0MQ
scope.14.kind=field
scope.14.startLine=41
scope.14.endLine=41
scope.14.semanticHash=2de43f8cde05538f06c2853beed097a2095f9765404ec2c2e8ac5b8e18910362
scope.15.id=ZmllbGQ6UnVsZS5UeXBlI2RvdWJsZV9zYWxhcnlfd2hlbl9sYW5kaW5nX29uX3N0YXJ0OjE3
scope.15.kind=field
scope.15.startLine=17
scope.15.endLine=17
scope.15.semanticHash=d86d6890ba0041b86f4354b169ba9d083e88912b532d6005338a5f1a1d137b5f
scope.16.id=bWV0aG9kOlJ1bGUjcHJvY2VzcygxKToxNA
scope.16.kind=method
scope.16.startLine=14
scope.16.endLine=14
scope.16.semanticHash=7c8965c1cfaa408d9aefa6768434b72903e3401259830a773e77b728bdd81693
scope.17.id=bWV0aG9kOlJ1bGUuUHJvY2Vzc29yI3Byb2Nlc3MoMSk6MjE
scope.17.kind=method
scope.17.startLine=21
scope.17.endLine=21
scope.17.semanticHash=ae191710b40974ff0b3cb75d8ef6cc97880f02cfbfb5603e03d20c7d70b8c048
scope.18.id=bWV0aG9kOlJ1bGUuU2V0I2FjdGl2YXRlKDEpOjMx
scope.18.kind=method
scope.18.startLine=31
scope.18.endLine=31
scope.18.semanticHash=5760e204dd97c25fd97789fb2944511971257ff3f8ed01f1150041e9e0d6dfef
scope.19.id=bWV0aG9kOlJ1bGUuU2V0I2JhbmsoMCk6Mjk
scope.19.kind=method
scope.19.startLine=29
scope.19.endLine=29
scope.19.semanticHash=cf7fc2b381c451bcd49c0d1f6294b9c55820797f1c830176b347593fd3f303d6
scope.20.id=bWV0aG9kOlJ1bGUuU2V0I2NyZWF0ZSgxKTozMw
scope.20.kind=method
scope.20.startLine=33
scope.20.endLine=33
scope.20.semanticHash=c2bb74be6839b3ad893665ca5f6acc41cf6e6c24815a448be9f01b500f727de3
scope.21.id=bWV0aG9kOlJ1bGUuU2V0I2RpY2UoMCk6MjU
scope.21.kind=method
scope.21.startLine=25
scope.21.endLine=25
scope.21.semanticHash=7cba09f9844240f8695fd18aeeb4254c96afb6fb6dc83633643c8a9c0ad12348
scope.22.id=bWV0aG9kOlJ1bGUuU2V0I2dhbWVib2FyZCgwKTozNQ
scope.22.kind=method
scope.22.startLine=35
scope.22.endLine=35
scope.22.semanticHash=33ed81f61b2314492fef3a31574fdaaba055703d9ebca2a24b611663f2051233
scope.23.id=bWV0aG9kOlJ1bGUuU2V0I3BsYXllcnMoMCk6Mjc
scope.23.kind=method
scope.23.startLine=27
scope.23.endLine=27
scope.23.semanticHash=f0c7e9827dff65ddb18bdc3ce278a442fa271df05b53f4a2418cad4dd227bdbf
scope.24.id=bWV0aG9kOlJ1bGUuU2V0I3N0cmVldHMoMCk6Mzg
scope.24.kind=method
scope.24.startLine=38
scope.24.endLine=38
scope.24.semanticHash=40b3f061b4d880084afee581503e0b07c1690e259f15685b6a40fd663a6d8ad4
scope.25.id=bWV0aG9kOlJ1bGUuU2V0LkZhY3RvcnkjY3JlYXRlKDApOjU2
scope.25.kind=method
scope.25.startLine=56
scope.25.endLine=56
scope.25.semanticHash=5d2e7df83c19dbaaa28b649da0c92731a92792ff3a4dd936bd24ae33a10a09b7
scope.26.id=bWV0aG9kOlJ1bGUuU2V0LlNpbXBsZSNhY3RpdmF0ZSgxKTo3Mg
scope.26.kind=method
scope.26.startLine=72
scope.26.endLine=75
scope.26.semanticHash=d35d7b1f6bcbb6816103b82b70da91ae44e535b6f2aa16bc9e81bfb8a538f266
scope.27.id=bWV0aG9kOlJ1bGUuU2V0LlNpbXBsZSNjcmVhdGUoMSk6Nzc
scope.27.kind=method
scope.27.startLine=77
scope.27.endLine=80
scope.27.semanticHash=73e7162910d945f98ea14511838208c9854f9ed974d230550f37d157bee2be62
scope.28.id=bWV0aG9kOlJ1bGUuU2V0LlNpbXBsZSNjdG9yKDYpOjU5
scope.28.kind=method
scope.28.startLine=1
scope.28.endLine=93
scope.28.semanticHash=7224ef840dec424204b93aa6af76972de9c62dd1450b16d1cc71f1d0a667cd32
scope.29.id=bWV0aG9kOlJ1bGUuU2V0LlNpbXBsZSNkaWNlKDApOjY3
scope.29.kind=method
scope.29.startLine=67
scope.29.endLine=70
scope.29.semanticHash=5aab2048224d379714baed562d649c57c502ee79d2cc5a47082710194f997d75
scope.30.id=bWV0aG9kOlJ1bGUuU2V0LlNpbXBsZSNnYW1lYm9hcmQoMCk6ODI
scope.30.kind=method
scope.30.startLine=82
scope.30.endLine=85
scope.30.semanticHash=8cb68e9b0a33f9076cc0fdaf33bf047377f0260e5a28089925f6c48c0478b4b3
scope.31.id=bWV0aG9kOlJ1bGUuU2V0LlNpbXBsZSNzdHJlZXRzKDApOjg3
scope.31.kind=method
scope.31.startLine=87
scope.31.endLine=90
scope.31.semanticHash=7e5d65f297960197363ec7040bf4364ae1e640365824bfdb69af3666eafc8278
scope.32.id=bWV0aG9kOlJ1bGUuU2V0LlR5cGUjY3JlYXRlKDApOjQ5
scope.32.kind=method
scope.32.startLine=49
scope.32.endLine=52
scope.32.semanticHash=f0b2dd2a9e4780e4bfe2d69a6e89d436d644cb1e440727b6d69150c2d53a1f3a
scope.33.id=bWV0aG9kOlJ1bGUuU2V0LlR5cGUjY3RvcigxKTo0NQ
scope.33.kind=method
scope.33.startLine=45
scope.33.endLine=47
scope.33.semanticHash=b0105bf5d3b9a292c1dfeb24cd0a812e77ff6a7b41cdea6c8b4102f254eed4a1
scope.34.id=bWV0aG9kOlJ1bGUuVHlwZSNjdG9yKDApOjE2
scope.34.kind=method
scope.34.startLine=1
scope.34.endLine=93
scope.34.semanticHash=7224ef840dec424204b93aa6af76972de9c62dd1450b16d1cc71f1d0a667cd32
*/
