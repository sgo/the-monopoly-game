package the.monopoly.game.cli;

import java.util.List;

/**
 * Argument-flag interpretation for {@link Simulator}: which optional rules a
 * command line requests and which tokens are recognised flags (as opposed to
 * player names). Kept separate from {@code Simulator} so the flag vocabulary
 * lives in one place instead of being re-derived across {@code main} and
 * {@code runSelected}.
 */
final class SimulatorFlags {
  static final String MAX_YEARS_FLAG = "--max-years=";
  static final String SEED_FLAG = "--seed=";

  private SimulatorFlags() {
  }

  static boolean stalemateTrading(String... arguments) {
    return present(arguments, "--optional-greedo-stalemate-trading");
  }

  static boolean legalEntityTrading(String... arguments) {
    return present(arguments, "--optional-greedo-legal-entity");
  }

  static boolean assetRichOpening(String... arguments) {
    return present(arguments, "--optional-asset-rich-billionaire");
  }

  static boolean developmentLoans(String... arguments) {
    return present(arguments, "--optional-development-loans");
  }

  static boolean fullDrawDevelopmentLoans(String... arguments) {
    return present(arguments, "--optional-development-loans-full-draw");
  }

  static boolean warProfitsTax(String... arguments) {
    return present(arguments, "--optional-war-profits-tax");
  }

  static boolean rentRelief(String... arguments) {
    return present(arguments, "--optional-rent-relief");
  }

  /** Whether {@code token} is a recognised flag rather than a strategy name. */
  static boolean recognized(String argument) {
    return argument.equals("--optional-greedo-stalemate-trading")
        || argument.equals("--optional-greedo-legal-entity")
        || argument.equals("--optional-asset-rich-billionaire")
        || argument.equals("--optional-development-loans")
        || argument.equals("--optional-development-loans-full-draw")
        || argument.equals("--optional-war-profits-tax")
        || argument.equals("--optional-rent-relief")
        || argument.startsWith(MAX_YEARS_FLAG)
        || argument.startsWith(SEED_FLAG);
  }

  /** Year limit from a {@code --max-years=N} argument, or -1 when absent. */
  static int maxYears(String... arguments) {
    for (String argument : arguments) {
      if (argument.startsWith(MAX_YEARS_FLAG)) {
        return Integer.parseInt(argument.substring(MAX_YEARS_FLAG.length()));
      }
    }
    return -1;
  }

  /** Seed from a {@code --seed=N} argument, or null when absent. */
  static Long seed(String... arguments) {
    for (String argument : arguments) {
      if (argument.startsWith(SEED_FLAG)) {
        return Long.parseLong(argument.substring(SEED_FLAG.length()));
      }
    }
    return null;
  }

  private static boolean present(String[] arguments, String flag) {
    return List.of(arguments).contains(flag);
  }
}

/* mutate4java-manifest
version=1
moduleHash=066adb4c856729ee4d61fc1231ee5e561192ffa45a39fbeb16ecffe93adbf85a
scope.0.id=Y2xhc3M6U2ltdWxhdG9yRmxhZ3MjU2ltdWxhdG9yRmxhZ3M6MTI
scope.0.kind=class
scope.0.startLine=12
scope.0.endLine=83
scope.0.semanticHash=5bdc335b849c5c17f0c35594051dce53b41e724f2ff8f93fb6c7ce84f42d2904
scope.1.id=ZmllbGQ6U2ltdWxhdG9yRmxhZ3MjTUFYX1lFQVJTX0ZMQUc6MTM
scope.1.kind=field
scope.1.startLine=13
scope.1.endLine=13
scope.1.semanticHash=54dcadc7c1eea4226bed705d5d1d99e4460a8156fb0c5e7663493f3d70631b91
scope.2.id=ZmllbGQ6U2ltdWxhdG9yRmxhZ3MjU0VFRF9GTEFHOjE0
scope.2.kind=field
scope.2.startLine=14
scope.2.endLine=14
scope.2.semanticHash=c2892f4cc1c24a4b977bbc954f12b11be8b91a95d71bf05ddbfe21ea03d4443d
scope.3.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI2Fzc2V0UmljaE9wZW5pbmcoMSk6Mjc
scope.3.kind=method
scope.3.startLine=27
scope.3.endLine=29
scope.3.semanticHash=155cc2f94f5dfe168b88ccc47f32fcb4c60530bb9d653c68b7eb4c8866e97692
scope.4.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI2N0b3IoMCk6MTY
scope.4.kind=method
scope.4.startLine=16
scope.4.endLine=17
scope.4.semanticHash=854f843460f660d5e26c51f08a6f9a2098b5db7c40eff7b944699bbe8cf8d171
scope.5.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI2RldmVsb3BtZW50TG9hbnMoMSk6MzE
scope.5.kind=method
scope.5.startLine=31
scope.5.endLine=33
scope.5.semanticHash=43aee1a870adf898110d1cd20688a5935906b2e2ef3dcc6d116e0d6726fc5151
scope.6.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI2Z1bGxEcmF3RGV2ZWxvcG1lbnRMb2FucygxKTozNQ
scope.6.kind=method
scope.6.startLine=35
scope.6.endLine=37
scope.6.semanticHash=bd6437989a420bbfebdfb116b914688e476ddb65d7aed3974502eb8ca1e56bf8
scope.7.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI2xlZ2FsRW50aXR5VHJhZGluZygxKToyMw
scope.7.kind=method
scope.7.startLine=23
scope.7.endLine=25
scope.7.semanticHash=7093fe1bc913a745fb190db15b8fa219deef0ab6683faf0a2ff0d80db4ea34d7
scope.8.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI21heFllYXJzKDEpOjYx
scope.8.kind=method
scope.8.startLine=61
scope.8.endLine=68
scope.8.semanticHash=be54d6b3f898c0e5a90206efa1f4131df1e9f055520d7e6030dce30c0b59c53c
scope.9.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI3ByZXNlbnQoMik6ODA
scope.9.kind=method
scope.9.startLine=80
scope.9.endLine=82
scope.9.semanticHash=df36669a52170c55139bb5414a0d7f6d0dfa3f509f3183b568030e09c6d5b3df
scope.10.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI3JlY29nbml6ZWQoMSk6NDg
scope.10.kind=method
scope.10.startLine=48
scope.10.endLine=58
scope.10.semanticHash=67cca9b02e6348f713e038407eda9766335b09bcfdb83c9c5003163e44a5460a
scope.11.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI3JlbnRSZWxpZWYoMSk6NDM
scope.11.kind=method
scope.11.startLine=43
scope.11.endLine=45
scope.11.semanticHash=e415455fd7bfe37ca43bc37452228dc90f9b2985770334594517fe48cadbe3ad
scope.12.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI3NlZWQoMSk6NzE
scope.12.kind=method
scope.12.startLine=71
scope.12.endLine=78
scope.12.semanticHash=4fcce6b51e0272c2564da06b1a67b80d820ee24b91f236c91a60ef803c9bffef
scope.13.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI3N0YWxlbWF0ZVRyYWRpbmcoMSk6MTk
scope.13.kind=method
scope.13.startLine=19
scope.13.endLine=21
scope.13.semanticHash=19574fcb41982a566e5544444ba5e33c4d8a758c689febc91ab5c5fea28fd387
scope.14.id=bWV0aG9kOlNpbXVsYXRvckZsYWdzI3dhclByb2ZpdHNUYXgoMSk6Mzk
scope.14.kind=method
scope.14.startLine=39
scope.14.endLine=41
scope.14.semanticHash=5807d326414856ce55d1204974c6ef9d925fb7c81943a8b46e4987a7a985e40e
*/
