package the.monopoly.game.components.dice;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A dice is its faces: rolling one picks a face, and nothing about it is fixed
 * to six of them but the one {@link Type} the rules currently call for.
 */
public class Dice {
  private final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
  private final Face[] faces;

  public Dice(Face... faces) {
    this.faces = faces;
  }

  public Stream<Face> faces() {
    return Stream.of(faces);
  }

  public Face roll() {
    return faces[random.get().nextInt(faces.length)];
  }

  public record Face(String symbol) {
  }

  public enum Type {
    six(IntStream.range(1, 7).mapToObj(it -> Integer.toString(it)).map(Face::new).toArray(Face[]::new));

    private final Face[] faces;

    Type(Face... faces) {
      this.faces = faces;
    }

    public Dice create() {
      return new Dice(faces);
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=82f866d3c2e3ff15b378e4cc273aa772212a201701ff1ec937893736d8bb9d7d
scope.0.id=Y2xhc3M6RGljZSNEaWNlOjEx
scope.0.kind=class
scope.0.startLine=11
scope.0.endLine=43
scope.0.semanticHash=1941703207152fce04f24102458bf867eb33c49243ac66a9de3bc888c75cfd9a
scope.1.id=Y2xhc3M6RGljZS5GYWNlI0ZhY2U6Mjc
scope.1.kind=class
scope.1.startLine=27
scope.1.endLine=28
scope.1.semanticHash=599c55337ecb67992f3e7f1437c3fb4280fc1f9e90f47d7533f184ebb4b8c1f6
scope.2.id=Y2xhc3M6RGljZS5UeXBlI1R5cGU6MzA
scope.2.kind=class
scope.2.startLine=30
scope.2.endLine=42
scope.2.semanticHash=9ff5c2d60a9eb0d1f8e9661e955349e36351ebf96f07d20da53f4699987b633d
scope.3.id=ZmllbGQ6RGljZSNmYWNlczoxMw
scope.3.kind=field
scope.3.startLine=13
scope.3.endLine=13
scope.3.semanticHash=5281864879ba6a360ad3623d5a69c23e79d4d75c5dd3f0a94758c0a1dc0c8dda
scope.4.id=ZmllbGQ6RGljZSNyYW5kb206MTI
scope.4.kind=field
scope.4.startLine=12
scope.4.endLine=12
scope.4.semanticHash=99a504297705960c72b6b9d132d6fd2ef7235f4ba5284fb07d1beb51b0e3008b
scope.5.id=ZmllbGQ6RGljZS5GYWNlI3N5bWJvbDoyNw
scope.5.kind=field
scope.5.startLine=27
scope.5.endLine=27
scope.5.semanticHash=847c05e68bbab410e0100d5832e3f74531428f91f1c0e6cdc830cb7ec0806872
scope.6.id=ZmllbGQ6RGljZS5UeXBlI2ZhY2VzOjMz
scope.6.kind=field
scope.6.startLine=33
scope.6.endLine=33
scope.6.semanticHash=5281864879ba6a360ad3623d5a69c23e79d4d75c5dd3f0a94758c0a1dc0c8dda
scope.7.id=ZmllbGQ6RGljZS5UeXBlI3NpeDozMQ
scope.7.kind=field
scope.7.startLine=31
scope.7.endLine=31
scope.7.semanticHash=93e763254d08485731adab598e793a2a33b16140c06bd38d2a601a2d7fc9866c
scope.8.id=bWV0aG9kOkRpY2UjY3RvcigxKToxNQ
scope.8.kind=method
scope.8.startLine=15
scope.8.endLine=17
scope.8.semanticHash=ec58e0ab71c2f40a1d4fd2f7e2677093d75a14b9fbea5198eedc6901b6582590
scope.9.id=bWV0aG9kOkRpY2UjZmFjZXMoMCk6MTk
scope.9.kind=method
scope.9.startLine=19
scope.9.endLine=21
scope.9.semanticHash=446a674819192262d27b3691c367e06b41bf9ece53734888f31df312a7b4e576
scope.10.id=bWV0aG9kOkRpY2Ujcm9sbCgwKToyMw
scope.10.kind=method
scope.10.startLine=23
scope.10.endLine=25
scope.10.semanticHash=097a5bc68cf7d5069af7c3e5311b200339ad90874519d16a22597881dec14f27
scope.11.id=bWV0aG9kOkRpY2UuRmFjZSNjdG9yKDEpOjI3
scope.11.kind=method
scope.11.startLine=1
scope.11.endLine=43
scope.11.semanticHash=0f810d6d72a0f5beca641fd99253b25a50c0c554c04b6f7f6c1dee37cc36bc52
scope.12.id=bWV0aG9kOkRpY2UuVHlwZSNjcmVhdGUoMCk6Mzk
scope.12.kind=method
scope.12.startLine=39
scope.12.endLine=41
scope.12.semanticHash=6a5356e4c353c94fcec5006fb86104dab09e291c3b7d3546e7a907eb00add70e
scope.13.id=bWV0aG9kOkRpY2UuVHlwZSNjdG9yKDEpOjM1
scope.13.kind=method
scope.13.startLine=35
scope.13.endLine=37
scope.13.semanticHash=d51295f03c9e7ba71fc766d3ecf454672c5e42e21d8b0a7dbe3d9e6935931694
*/
