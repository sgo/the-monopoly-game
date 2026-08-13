package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Rule;

import java.util.List;
import java.util.Optional;

/**
 * How a player decides. The game asks a strategy wherever a player has a
 * choice, and every choice has a default of leaving well alone, so a strategy
 * answers only the questions it has an opinion about and a new question can be
 * asked without every strategy being rewritten.
 */
public interface Strategy {
  /** A player who leaves every choice alone, which is what the defaults amount to. */
  Strategy UNDECIDED = new Strategy() {
  };

  /** Whether to buy the land on offer at the price on the board. */
  default boolean accepts(Offer offer) {
    return false;
  }

  /** Whether the trader should exchange one owned property for another player's property. */
  default boolean accepts(TradeOffer offer, Rule.Set rules, Deeds deeds) {
    return false;
  }

  default DeclineReason declineReason(Offer offer) {
    return DeclineReason.NO_BUYING_POLICY;
  }

  /** What to bid for the land at auction. Nothing is not a bid. */
  default Money bidFor(Offer offer) {
    return Money.ZERO;
  }

  /** Whether to collect the rent owed by a visitor to land this player owns. */
  default boolean claims(RentClaim claim) {
    return false;
  }

  /** Whether to buy the next house or hotel offered for an owned street. */
  default boolean builds(BuildOffer offer) {
    return false;
  }

  /** Whether this shareholder commits their share of a legal-entity build loan. */
  default boolean commitToEntityBuild(EntityBuildOffer offer) {
    return false;
  }

  /** Whether this strategy has opted into automatic legal-entity development. */
  default boolean legalEntityTradingEnabled() {
    return false;
  }

  /** Whether to pay the fine to begin this turn outside jail. */
  default boolean pays(JailFine fine) {
    return false;
  }

  /** A visitor, the land they stopped on, and the rent its owner may claim. */
  record RentClaim(Player tenant, Ownable land, Money amount) {
  }

  /** The land a player is being asked about, and what they have to spend on it. */
  record Offer(Ownable land, Money available, Money reserve, boolean utilityMonopolyOpportunity) {
    public Offer(Ownable land, Money available) {
      this(land, available, Money.ZERO, false);
    }

    public boolean isAffordable() {
      return available.covers(land.price());
    }
  }

  record TradeOffer(Player trader, Player partner, Ownable offered, Ownable wanted) {
  }

  enum DeclineReason {
    CANNOT_AFFORD,
    CASH_RESERVE,
    NO_BUYING_POLICY
  }

  default Money cashReserve() {
    return Money.ZERO;
  }

  /** Reserve calculated with the player's current holdings, when a strategy has one. */
  default Money cashReserve(Player player, Rule.Set rules, Deeds deeds) {
    return cashReserve();
  }

  /** Opening balance supplied by a strategy, when it replaces the rule-set default. */
  default Optional<Money> openingCapital() {
    return Optional.empty();
  }

  enum Priority {
    HIGHEST, MIDDLE, LOWEST
  }

  default Priority priority(Ownable land) {
    return Priority.LOWEST;
  }

  static Priority priorityOf(Ownable land) {
    return switch (land.type()) {
      case LippenslaanKnokke, RueRoyaleTournai, GroenplaatsAntwerpen,
          RueStLeonardLiege, LangeSteenstraatKortrijk, GrandPlaceMons,
          SteenstraatBrugge, PlaceDuMonumentSpa, KapellestraatOostende -> Priority.HIGHEST;
      case RueGrandeDinant, DiestsestraatLeuven, RueDeDiekirchArlon,
          BruulMechelen, PlaceVerteVerviers, GroteMarktHasselt,
          PlaceDeLAngeNamur, HoogstraatBrussel -> Priority.MIDDLE;
      default -> Priority.LOWEST;
    };
  }

  /** Maximum offer for a property sold by a debtor trying to avoid bankruptcy. */
  default Money bidForDistressed(Offer offer, Player bidder, Player debtor,
                                 List<Player> players, Rule.Set rules, Deeds deeds) {
    return bidFor(offer);
  }

  /** The improvement a player is being asked to buy for a street they already own. */
  record BuildOffer(ColourStreet land, Money price, Money available, boolean hotel) {
    public boolean isAffordable() {
      return available.covers(price);
    }
  }

  record EntityBuildOffer(Money share, Money available, Money reserve) {
  }

  record JailFine(Money amount, Money available) {
    public boolean isAffordable() {
      return available.covers(amount);
    }
  }

  /** Which strategy each player at the table is playing. */
  @FunctionalInterface
  interface OfPlayers {
    /** Everyone leaving every choice alone, for a game where nobody decides. */
    OfPlayers NOBODY_DECIDES = player -> UNDECIDED;

    Strategy forPlayer(Player player);
  }
}

/* mutate4java-manifest
version=1
moduleHash=e89ee974b423af19aeff18a9124c3d02b7c15d6ec5660216e92a94f247744f46
scope.0.id=Y2xhc3M6U3RyYXRlZ3kjU3RyYXRlZ3k6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=150
scope.0.semanticHash=9bda68cb4e489c7d0860b73598aae14d228d6b8b9dffa8010ae017710cc297b6
scope.1.id=Y2xhc3M6U3RyYXRlZ3kuIzoyMA
scope.1.kind=class
scope.1.startLine=20
scope.1.endLine=21
scope.1.semanticHash=f43c41c22e45eb1f9525f8be15e1c07728f4ca3b5268d4e1ae476949ee958ea2
scope.2.id=Y2xhc3M6U3RyYXRlZ3kuQnVpbGRPZmZlciNCdWlsZE9mZmVyOjEyNw
scope.2.kind=class
scope.2.startLine=127
scope.2.endLine=131
scope.2.semanticHash=ed3ceeeb156aea50b62f3f6599d6d4144af1f8d29586c01c219259c33849bb19
scope.3.id=Y2xhc3M6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNEZWNsaW5lUmVhc29uOjg1
scope.3.kind=class
scope.3.startLine=85
scope.3.endLine=89
scope.3.semanticHash=d8497c5c1662d16ecb6ba17e836492b159af8f5e497a9da2ffc6af14d5dd4322
scope.4.id=Y2xhc3M6U3RyYXRlZ3kuRW50aXR5QnVpbGRPZmZlciNFbnRpdHlCdWlsZE9mZmVyOjEzMw
scope.4.kind=class
scope.4.startLine=133
scope.4.endLine=134
scope.4.semanticHash=c9e0aff1ab33870175767a5c58820a534dfa73b89a6685b82fa9d324eb1a84c7
scope.5.id=Y2xhc3M6U3RyYXRlZ3kuSmFpbEZpbmUjSmFpbEZpbmU6MTM2
scope.5.kind=class
scope.5.startLine=136
scope.5.endLine=140
scope.5.semanticHash=475585c5238183cdae22a88d6cf475cbf0fb23e46d258ade7757621811afeda0
scope.6.id=Y2xhc3M6U3RyYXRlZ3kuT2ZQbGF5ZXJzI09mUGxheWVyczoxNDM
scope.6.kind=class
scope.6.startLine=143
scope.6.endLine=149
scope.6.semanticHash=5f8f19ae06d88ffcfc16424a4863b524eeb7e0f5fd047520027bffe020887d9e
scope.7.id=Y2xhc3M6U3RyYXRlZ3kuT2ZmZXIjT2ZmZXI6NzI
scope.7.kind=class
scope.7.startLine=72
scope.7.endLine=80
scope.7.semanticHash=7f2cd56eda30901389ecac24e05691990687d70540c4e9be4470d751f7e8452c
scope.8.id=Y2xhc3M6U3RyYXRlZ3kuUHJpb3JpdHkjUHJpb3JpdHk6MTAw
scope.8.kind=class
scope.8.startLine=100
scope.8.endLine=102
scope.8.semanticHash=a2a404b2f42d68270f65d74737b7fb841f3116105222137422b6c68e3b2c5b2c
scope.9.id=Y2xhc3M6U3RyYXRlZ3kuUmVudENsYWltI1JlbnRDbGFpbTo2OA
scope.9.kind=class
scope.9.startLine=68
scope.9.endLine=69
scope.9.semanticHash=be96e18726a00a7334fc1bb5c3555281345e5de8343d57b6c0acc978405306e3
scope.10.id=Y2xhc3M6U3RyYXRlZ3kuVHJhZGVPZmZlciNUcmFkZU9mZmVyOjgy
scope.10.kind=class
scope.10.startLine=82
scope.10.endLine=83
scope.10.semanticHash=2059acf82196db4175dc32e0bbb0927bd10e949950b23c77c695517103c2a370
scope.11.id=ZmllbGQ6U3RyYXRlZ3kjVU5ERUNJREVEOjIw
scope.11.kind=field
scope.11.startLine=20
scope.11.endLine=21
scope.11.semanticHash=f00239ff23e22226b9cac10cc3cca46af5715c686a108e5ffe8fab4442477604
scope.12.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNhdmFpbGFibGU6MTI3
scope.12.kind=field
scope.12.startLine=127
scope.12.endLine=127
scope.12.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.13.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNob3RlbDoxMjc
scope.13.kind=field
scope.13.startLine=127
scope.13.endLine=127
scope.13.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.14.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNsYW5kOjEyNw
scope.14.kind=field
scope.14.startLine=127
scope.14.endLine=127
scope.14.semanticHash=35f9733d561459c6d821f37d194db007baffa148da826ae273a60a12e0e55476
scope.15.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNwcmljZToxMjc
scope.15.kind=field
scope.15.startLine=127
scope.15.endLine=127
scope.15.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.16.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQU5OT1RfQUZGT1JEOjg2
scope.16.kind=field
scope.16.startLine=86
scope.16.endLine=86
scope.16.semanticHash=0562a15b653fe383269ebd77aa29e1c50390797b07f7a8166e9de3b296c3fc21
scope.17.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQVNIX1JFU0VSVkU6ODc
scope.17.kind=field
scope.17.startLine=87
scope.17.endLine=87
scope.17.semanticHash=c36ca0607e5d5b006445adf3206eeb33ccc63ab058d0b09a3b5a0f1580409514
scope.18.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNOT19CVVlJTkdfUE9MSUNZOjg4
scope.18.kind=field
scope.18.startLine=88
scope.18.endLine=88
scope.18.semanticHash=573981a3034acaf2d9db7c0ea32b36b35c43345919b99c88bb91e7feb003d593
scope.19.id=ZmllbGQ6U3RyYXRlZ3kuRW50aXR5QnVpbGRPZmZlciNhdmFpbGFibGU6MTMz
scope.19.kind=field
scope.19.startLine=133
scope.19.endLine=133
scope.19.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.20.id=ZmllbGQ6U3RyYXRlZ3kuRW50aXR5QnVpbGRPZmZlciNyZXNlcnZlOjEzMw
scope.20.kind=field
scope.20.startLine=133
scope.20.endLine=133
scope.20.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.21.id=ZmllbGQ6U3RyYXRlZ3kuRW50aXR5QnVpbGRPZmZlciNzaGFyZToxMzM
scope.21.kind=field
scope.21.startLine=133
scope.21.endLine=133
scope.21.semanticHash=afb400ce79fcde739869a877b5f09510d4ef896e3c5a8342e3f8e152eb485720
scope.22.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYW1vdW50OjEzNg
scope.22.kind=field
scope.22.startLine=136
scope.22.endLine=136
scope.22.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.23.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYXZhaWxhYmxlOjEzNg
scope.23.kind=field
scope.23.startLine=136
scope.23.endLine=136
scope.23.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.24.id=ZmllbGQ6U3RyYXRlZ3kuT2ZQbGF5ZXJzI05PQk9EWV9ERUNJREVTOjE0Ng
scope.24.kind=field
scope.24.startLine=146
scope.24.endLine=146
scope.24.semanticHash=c9a5335263a411687db98645598d3bb57e8e329f99505da857a008b62ddbc8b3
scope.25.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjYXZhaWxhYmxlOjcy
scope.25.kind=field
scope.25.startLine=72
scope.25.endLine=72
scope.25.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.26.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjbGFuZDo3Mg
scope.26.kind=field
scope.26.startLine=72
scope.26.endLine=72
scope.26.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.27.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjcmVzZXJ2ZTo3Mg
scope.27.kind=field
scope.27.startLine=72
scope.27.endLine=72
scope.27.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.28.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjdXRpbGl0eU1vbm9wb2x5T3Bwb3J0dW5pdHk6NzI
scope.28.kind=field
scope.28.startLine=72
scope.28.endLine=72
scope.28.semanticHash=4827abe12096fb0602e87255e1235bbd76c30e6faad4101e14d7c13a22b76c8b
scope.29.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjSElHSEVTVDoxMDE
scope.29.kind=field
scope.29.startLine=101
scope.29.endLine=101
scope.29.semanticHash=f18742671a135cae02cfdafc9c39c14bcbb737bac94d266b118d444b3a81fa62
scope.30.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjTE9XRVNUOjEwMQ
scope.30.kind=field
scope.30.startLine=101
scope.30.endLine=101
scope.30.semanticHash=466a4131f1e6ce62c929316d2b7fefa9339c0c6cc0df60c6db1c10407fd2892c
scope.31.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjTUlERExFOjEwMQ
scope.31.kind=field
scope.31.startLine=101
scope.31.endLine=101
scope.31.semanticHash=b0db1518f3cdfd4db883854f40d1c2b32f687162d0ffb4a5c70637de157a67b7
scope.32.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2Ftb3VudDo2OA
scope.32.kind=field
scope.32.startLine=68
scope.32.endLine=68
scope.32.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.33.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2xhbmQ6Njg
scope.33.kind=field
scope.33.startLine=68
scope.33.endLine=68
scope.33.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.34.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI3RlbmFudDo2OA
scope.34.kind=field
scope.34.startLine=68
scope.34.endLine=68
scope.34.semanticHash=f03ab23a67acec8e4db339095778b1e8cfea41719a41d12792d4d819cea02860
scope.35.id=ZmllbGQ6U3RyYXRlZ3kuVHJhZGVPZmZlciNvZmZlcmVkOjgy
scope.35.kind=field
scope.35.startLine=82
scope.35.endLine=82
scope.35.semanticHash=4a3c1a8e149716d4eff641f25e52ed346b5eaa070d3c26a4f73dea037494197f
scope.36.id=ZmllbGQ6U3RyYXRlZ3kuVHJhZGVPZmZlciNwYXJ0bmVyOjgy
scope.36.kind=field
scope.36.startLine=82
scope.36.endLine=82
scope.36.semanticHash=1c4b328c26083116ccd92908d023d04b5d1387bc0feed450661bd017d6917a10
scope.37.id=ZmllbGQ6U3RyYXRlZ3kuVHJhZGVPZmZlciN0cmFkZXI6ODI
scope.37.kind=field
scope.37.startLine=82
scope.37.endLine=82
scope.37.semanticHash=ee0f1344de9034bfc0e6c93ac47dc1267e98d4e6a099bec0d588c3945e9c54b1
scope.38.id=ZmllbGQ6U3RyYXRlZ3kuVHJhZGVPZmZlciN3YW50ZWQ6ODI
scope.38.kind=field
scope.38.startLine=82
scope.38.endLine=82
scope.38.semanticHash=2ed62d75695cc7464e1b52a329be624426651cc688cd32491385c68e2124a780
scope.39.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMSk6MjQ
scope.39.kind=method
scope.39.startLine=24
scope.39.endLine=26
scope.39.semanticHash=db7e918b1301c0a5e1e4e5190b554cca47be22517eb285731cc1d83f4f509bbe
scope.40.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMyk6Mjk
scope.40.kind=method
scope.40.startLine=29
scope.40.endLine=31
scope.40.semanticHash=697de314e8f022d7c259a387f9a057b565d86261593362bee48ec7722d888109
scope.41.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvcigxKTozOA
scope.41.kind=method
scope.41.startLine=38
scope.41.endLine=40
scope.41.semanticHash=68b3807991ccbffe92c86209b89fae2df2340f29d0a497561cdcd3f78e46ce79
scope.42.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvckRpc3RyZXNzZWQoNik6MTIx
scope.42.kind=method
scope.42.startLine=121
scope.42.endLine=124
scope.42.semanticHash=e4ab55653e41003e36f416113d350aa58b42e0c3740660f35a59ecbac92d2a71
scope.43.id=bWV0aG9kOlN0cmF0ZWd5I2J1aWxkcygxKTo0OA
scope.43.kind=method
scope.43.startLine=48
scope.43.endLine=50
scope.43.semanticHash=f7d3cc98f5a772d00d27eb7defef23be2865369310099a11a3c5b00ca3f3cae6
scope.44.id=bWV0aG9kOlN0cmF0ZWd5I2Nhc2hSZXNlcnZlKDApOjkx
scope.44.kind=method
scope.44.startLine=91
scope.44.endLine=93
scope.44.semanticHash=ad759992f3e478a058748ed385db64fd90320432890c3e55b970807c7b591a56
scope.45.id=bWV0aG9kOlN0cmF0ZWd5I2Nhc2hSZXNlcnZlKDMpOjk2
scope.45.kind=method
scope.45.startLine=96
scope.45.endLine=98
scope.45.semanticHash=ef54b4fa5744bd18de2343911b27ab8095fcfb809b8bdf5fa90f1644d9e6e640
scope.46.id=bWV0aG9kOlN0cmF0ZWd5I2NsYWltcygxKTo0Mw
scope.46.kind=method
scope.46.startLine=43
scope.46.endLine=45
scope.46.semanticHash=0de4b0d4da40a0870e5ed181c2e73a63e7ca91672ce5f14ac145936e8b2f7170
scope.47.id=bWV0aG9kOlN0cmF0ZWd5I2NvbW1pdFRvRW50aXR5QnVpbGQoMSk6NTM
scope.47.kind=method
scope.47.startLine=53
scope.47.endLine=55
scope.47.semanticHash=16c7bad63621c59224ebd47da96bf746b2b67dfe98df72745d2bad0bc1a895f9
scope.48.id=bWV0aG9kOlN0cmF0ZWd5I2RlY2xpbmVSZWFzb24oMSk6MzM
scope.48.kind=method
scope.48.startLine=33
scope.48.endLine=35
scope.48.semanticHash=b21810ac08d8bf76c5f02fc132ba14d1bc098521837310c97dbe43ebacc7dae6
scope.49.id=bWV0aG9kOlN0cmF0ZWd5I2xlZ2FsRW50aXR5VHJhZGluZ0VuYWJsZWQoMCk6NTg
scope.49.kind=method
scope.49.startLine=58
scope.49.endLine=60
scope.49.semanticHash=770f70e0f775c83e096cafd6f4005c6c687dca522c1a76b19d6766da76d8792f
scope.50.id=bWV0aG9kOlN0cmF0ZWd5I3BheXMoMSk6NjM
scope.50.kind=method
scope.50.startLine=63
scope.50.endLine=65
scope.50.semanticHash=9665aac958731b28c9d13787f8f077161467c026d849a454887fc1614714e7b1
scope.51.id=bWV0aG9kOlN0cmF0ZWd5I3ByaW9yaXR5KDEpOjEwNA
scope.51.kind=method
scope.51.startLine=104
scope.51.endLine=106
scope.51.semanticHash=926a6dd97684bd51f06e7ef41dee3d103d5130ff0c8513083cf4042fefdab937
scope.52.id=bWV0aG9kOlN0cmF0ZWd5I3ByaW9yaXR5T2YoMSk6MTA4
scope.52.kind=method
scope.52.startLine=108
scope.52.endLine=118
scope.52.semanticHash=818200a23df87c9c79635b9d1b17389d6de932e8d162d85ced0f43025a5991fe
scope.53.id=bWV0aG9kOlN0cmF0ZWd5LiNjdG9yKDApOjIw
scope.53.kind=method
scope.53.startLine=1
scope.53.endLine=150
scope.53.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
scope.54.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjY3Rvcig0KToxMjc
scope.54.kind=method
scope.54.startLine=1
scope.54.endLine=150
scope.54.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
scope.55.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjaXNBZmZvcmRhYmxlKDApOjEyOA
scope.55.kind=method
scope.55.startLine=128
scope.55.endLine=130
scope.55.semanticHash=e14c79b46e24f2513da2fac747d83f31ba0673fea6ee146454f89007dcf0d6a9
scope.56.id=bWV0aG9kOlN0cmF0ZWd5LkRlY2xpbmVSZWFzb24jY3RvcigwKTo4NQ
scope.56.kind=method
scope.56.startLine=1
scope.56.endLine=150
scope.56.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
scope.57.id=bWV0aG9kOlN0cmF0ZWd5LkVudGl0eUJ1aWxkT2ZmZXIjY3RvcigzKToxMzM
scope.57.kind=method
scope.57.startLine=1
scope.57.endLine=150
scope.57.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
scope.58.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2N0b3IoMik6MTM2
scope.58.kind=method
scope.58.startLine=1
scope.58.endLine=150
scope.58.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
scope.59.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2lzQWZmb3JkYWJsZSgwKToxMzc
scope.59.kind=method
scope.59.startLine=137
scope.59.endLine=139
scope.59.semanticHash=dff0ac3387fd0017fbe557e0ceb9169463894f542c375ca972afc49bc2a777e7
scope.60.id=bWV0aG9kOlN0cmF0ZWd5Lk9mUGxheWVycyNmb3JQbGF5ZXIoMSk6MTQ4
scope.60.kind=method
scope.60.startLine=148
scope.60.endLine=148
scope.60.semanticHash=66ad4c5c63cd26d01a2387bb0854f1d342eea8ef7fa68207a3fbca7556de35b6
scope.61.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoMik6NzM
scope.61.kind=method
scope.61.startLine=73
scope.61.endLine=75
scope.61.semanticHash=e3ad9be7fc0e9555f8686dffdf5eb9f7835da5074bb34b923f39f2c52eddc72d
scope.62.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoNCk6NzI
scope.62.kind=method
scope.62.startLine=1
scope.62.endLine=150
scope.62.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
scope.63.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2lzQWZmb3JkYWJsZSgwKTo3Nw
scope.63.kind=method
scope.63.startLine=77
scope.63.endLine=79
scope.63.semanticHash=19099acd48bb08c42f5cd2a1a4a768468607fba899c3a9614997294aeff6661e
scope.64.id=bWV0aG9kOlN0cmF0ZWd5LlByaW9yaXR5I2N0b3IoMCk6MTAw
scope.64.kind=method
scope.64.startLine=1
scope.64.endLine=150
scope.64.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
scope.65.id=bWV0aG9kOlN0cmF0ZWd5LlJlbnRDbGFpbSNjdG9yKDMpOjY4
scope.65.kind=method
scope.65.startLine=1
scope.65.endLine=150
scope.65.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
scope.66.id=bWV0aG9kOlN0cmF0ZWd5LlRyYWRlT2ZmZXIjY3Rvcig0KTo4Mg
scope.66.kind=method
scope.66.startLine=1
scope.66.endLine=150
scope.66.semanticHash=f5d2bea4d1967d9fb83c3e0668eeae9fa4a12a7bcbfb3b5bdde17c10589c87f9
*/
