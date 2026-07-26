package the.monopoly.game.components.streets;

import the.monopoly.game.rules.Rule;

import java.util.Set;

import static java.util.Collections.emptySet;
import static the.monopoly.game.components.streets.Street.Colour.*;
import static the.monopoly.game.components.streets.Street.Kind.*;

/**
 * A space on the board.
 * <p>
 * Every space knows which space it is and what kind of space it is. What else a
 * space can do depends on its kind, so the rest lives on the subtypes: only an
 * {@link Ownable} has a price, only a {@link ColourStreet} can be built on,
 * only a {@link TaxSpace} charges tax. Asking a station for its house rent is
 * therefore a compile error rather than a runtime failure.
 */
public sealed interface Street
    permits Ownable, StartSpace, TaxSpace, UnownableSpace {
  Type type();

  Kind kind();

  enum Type {
    start(StartSpace.factory()),

    RueGrandeDinant(ColourStreet.of(brown, 60, 2, 10, 30, 90, 160, 250, 50, 30)),
    DiestsestraatLeuven(ColourStreet.of(brown, 60, 4, 20, 60, 180, 320, 450, 50, 30)),
    SteenstraatBrugge(ColourStreet.of(light_blue, 100, 6, 30, 90, 270, 400, 550, 50, 50)),
    PlaceDuMonumentSpa(ColourStreet.of(light_blue, 100, 6, 30, 90, 270, 400, 550, 50, 50)),
    KapellestraatOostende(ColourStreet.of(light_blue, 120, 8, 40, 100, 300, 450, 600, 50, 60)),
    RueDeDiekirchArlon(ColourStreet.of(pink, 140, 10, 50, 150, 450, 625, 750, 100, 70)),
    BruulMechelen(ColourStreet.of(pink, 140, 10, 50, 150, 450, 625, 750, 100, 70)),
    PlaceVerteVerviers(ColourStreet.of(pink, 160, 12, 60, 180, 500, 700, 900, 100, 80)),
    LippenslaanKnokke(ColourStreet.of(orange, 180, 14, 70, 200, 550, 750, 950, 100, 90)),
    RueRoyaleTournai(ColourStreet.of(orange, 180, 14, 70, 200, 550, 750, 950, 100, 90)),
    GroenplaatsAntwerpen(ColourStreet.of(orange, 200, 16, 80, 220, 600, 800, 1000, 100, 100)),
    RueStLeonardLiege(ColourStreet.of(red, 220, 18, 90, 250, 700, 875, 1050, 150, 110)),
    LangeSteenstraatKortrijk(ColourStreet.of(red, 220, 18, 90, 250, 700, 875, 1050, 150, 110)),
    GrandPlaceMons(ColourStreet.of(red, 240, 20, 100, 300, 750, 925, 1100, 150, 120)),
    GroteMarktHasselt(ColourStreet.of(yellow, 260, 22, 110, 330, 800, 975, 1150, 150, 130)),
    PlaceDeLAngeNamur(ColourStreet.of(yellow, 260, 22, 110, 330, 800, 975, 1150, 150, 130)),
    HoogstraatBrussel(ColourStreet.of(yellow, 280, 24, 120, 360, 850, 1025, 1200, 150, 140)),
    BoulevardTirouCharleroi(ColourStreet.of(green, 300, 26, 130, 390, 900, 1100, 1275, 200, 150)),
    VeldstraatGent(ColourStreet.of(green, 300, 26, 130, 390, 900, 1100, 1275, 200, 150)),
    BoulevardDAvroyLiege(ColourStreet.of(green, 320, 28, 150, 450, 1000, 1200, 1400, 200, 160)),
    MeirAntwerpen(ColourStreet.of(dark_blue, 350, 35, 175, 500, 1100, 1300, 1500, 200, 175)),
    NieuwstraatBrussel(ColourStreet.of(dark_blue, 400, 50, 200, 600, 1400, 1700, 2000, 200, 200)),

    NoordStation(Station.factory()),
    CentraalStation(Station.factory()),
    Buurtspoorwegen(Station.factory()),
    ZuidStation(Station.factory()),

    Elektriciteitscentrale(Utility.factory()),
    Watermaatschappij(Utility.factory()),

    InkomstenBelasting(TaxSpace.of(200)),
    ExtraBelasting(TaxSpace.of(100)),

    Kans(UnownableSpace.of(chance)),
    AlgemeenFonds(UnownableSpace.of(community_chest)),
    OpBezoek(UnownableSpace.of(jail)),
    GratisParkeren(UnownableSpace.of(free_parking)),
    NaarDeGevangenis(UnownableSpace.of(go_to_jail));

    private final Factory factory;

    Type(Factory factory) {
      this.factory = factory;
    }

    public Street create(Set<Rule> activatedRules) {
      return factory.create(this, activatedRules == null ? emptySet() : activatedRules);
    }
  }

  enum Kind {
    start, street, station, utility, tax, chance, community_chest, jail, free_parking, go_to_jail
  }

  enum Colour {
    brown, light_blue, pink, orange, red, yellow, green, dark_blue
  }

  interface Factory {
    Street create(Type type, Set<Rule> activatedRules);
  }
}

/* mutate4java-manifest
version=1
moduleHash=471beab72e0de2c5cbb8d8538d0ab77a1e9e67d1e64b39c708e72e202b9f752f
scope.0.id=Y2xhc3M6U3RyZWV0I1N0cmVldDoyMA
scope.0.kind=class
scope.0.startLine=20
scope.0.endLine=91
scope.0.semanticHash=f625258d8faa81683c0712e0d30f5f666e8eddce974fd5fcea01f5b718df7733
scope.1.id=Y2xhc3M6U3RyZWV0LkNvbG91ciNDb2xvdXI6ODQ
scope.1.kind=class
scope.1.startLine=84
scope.1.endLine=86
scope.1.semanticHash=13a3d5f50584bc329f304f2768ea654d28d76d3947f33d6438ba607f3ef2297b
scope.2.id=Y2xhc3M6U3RyZWV0LkZhY3RvcnkjRmFjdG9yeTo4OA
scope.2.kind=class
scope.2.startLine=88
scope.2.endLine=90
scope.2.semanticHash=41cd6f76819f6b7b9fc66084ae99bf424dfa75218d4dbe5e955a0dc0e1a83bc0
scope.3.id=Y2xhc3M6U3RyZWV0LktpbmQjS2luZDo4MA
scope.3.kind=class
scope.3.startLine=80
scope.3.endLine=82
scope.3.semanticHash=f86805e42d8a654139d7f1377f28e11a80a82cb2d0dc31e876ece18929e64eee
scope.4.id=Y2xhc3M6U3RyZWV0LlR5cGUjVHlwZToyNg
scope.4.kind=class
scope.4.startLine=26
scope.4.endLine=78
scope.4.semanticHash=46fda3a734ccc2be15b0d2aee075a8c73e2c6c64ee0c446e6fb018cd711baab9
scope.5.id=ZmllbGQ6U3RyZWV0LkNvbG91ciNicm93bjo4NQ
scope.5.kind=field
scope.5.startLine=85
scope.5.endLine=85
scope.5.semanticHash=5eb67f9f8409b9c3f739735633cbdf92121393d0e13bd0f464b1b2a6a15ad2dc
scope.6.id=ZmllbGQ6U3RyZWV0LkNvbG91ciNkYXJrX2JsdWU6ODU
scope.6.kind=field
scope.6.startLine=85
scope.6.endLine=85
scope.6.semanticHash=ea893c917a48a95a0feb501d9c4a7026025727a7a1cdd60dac1596604e123d7d
scope.7.id=ZmllbGQ6U3RyZWV0LkNvbG91ciNncmVlbjo4NQ
scope.7.kind=field
scope.7.startLine=85
scope.7.endLine=85
scope.7.semanticHash=ba4788b226aa8dc2e6dc74248bb9f618cfa8c959e0c26c147be48f6839a0b088
scope.8.id=ZmllbGQ6U3RyZWV0LkNvbG91ciNsaWdodF9ibHVlOjg1
scope.8.kind=field
scope.8.startLine=85
scope.8.endLine=85
scope.8.semanticHash=3af182ef731cc249b2e8b68caf253818d28f4f28e7912eb45c4569ffad51d0fd
scope.9.id=ZmllbGQ6U3RyZWV0LkNvbG91ciNvcmFuZ2U6ODU
scope.9.kind=field
scope.9.startLine=85
scope.9.endLine=85
scope.9.semanticHash=1b4c9133da73a711322404314402765ab0d23fd362a167d6f0c65bb215113d94
scope.10.id=ZmllbGQ6U3RyZWV0LkNvbG91ciNwaW5rOjg1
scope.10.kind=field
scope.10.startLine=85
scope.10.endLine=85
scope.10.semanticHash=a67a41c8bc79d5da917b5051f1f0d3f5aeb4b63ba246b3546a961ef7a3c7d931
scope.11.id=ZmllbGQ6U3RyZWV0LkNvbG91ciNyZWQ6ODU
scope.11.kind=field
scope.11.startLine=85
scope.11.endLine=85
scope.11.semanticHash=b1f51a511f1da0cd348b8f8598db32e61cb963e5fc69e2b41485bf99590ed75a
scope.12.id=ZmllbGQ6U3RyZWV0LkNvbG91ciN5ZWxsb3c6ODU
scope.12.kind=field
scope.12.startLine=85
scope.12.endLine=85
scope.12.semanticHash=c685a2c9bab235ccdd2ab0ea92281a521c8aaf37895493d080070ea00fc7f5d7
scope.13.id=ZmllbGQ6U3RyZWV0LktpbmQjY2hhbmNlOjgx
scope.13.kind=field
scope.13.startLine=81
scope.13.endLine=81
scope.13.semanticHash=6cb09fe72a3471a776f9dbb8509fa5befe73e878f23dd71be79d24ec90c1b9db
scope.14.id=ZmllbGQ6U3RyZWV0LktpbmQjY29tbXVuaXR5X2NoZXN0Ojgx
scope.14.kind=field
scope.14.startLine=81
scope.14.endLine=81
scope.14.semanticHash=afc1e3d4008dc623af5c5a39bc4d4500bea329dc5673d7998de0e7d8d53d647f
scope.15.id=ZmllbGQ6U3RyZWV0LktpbmQjZnJlZV9wYXJraW5nOjgx
scope.15.kind=field
scope.15.startLine=81
scope.15.endLine=81
scope.15.semanticHash=d23d1d124385fd9ac1066004d5164df9574f31ae4c2576b1476dace5e05fbf7b
scope.16.id=ZmllbGQ6U3RyZWV0LktpbmQjZ29fdG9famFpbDo4MQ
scope.16.kind=field
scope.16.startLine=81
scope.16.endLine=81
scope.16.semanticHash=66f19443d01d37e5243144250a519c503fdc30e61347c329e92f3a6db6f48dd9
scope.17.id=ZmllbGQ6U3RyZWV0LktpbmQjamFpbDo4MQ
scope.17.kind=field
scope.17.startLine=81
scope.17.endLine=81
scope.17.semanticHash=7d72fa8065e24a58a3c5d62c69090ab7d43b89ab026d68bf10fe0dfea52d5b87
scope.18.id=ZmllbGQ6U3RyZWV0LktpbmQjc3RhcnQ6ODE
scope.18.kind=field
scope.18.startLine=81
scope.18.endLine=81
scope.18.semanticHash=cced28c6dc3f99c2396a5eaad732bf6b28142335892b1cd0e6af6cdb53f5ccfa
scope.19.id=ZmllbGQ6U3RyZWV0LktpbmQjc3RhdGlvbjo4MQ
scope.19.kind=field
scope.19.startLine=81
scope.19.endLine=81
scope.19.semanticHash=9b4ef3e5b7b4d1759cb8f880e34dbd5ea8c3fcd79ce33b89d7357bf64fcbfc91
scope.20.id=ZmllbGQ6U3RyZWV0LktpbmQjc3RyZWV0Ojgx
scope.20.kind=field
scope.20.startLine=81
scope.20.endLine=81
scope.20.semanticHash=6008c26f4452392acb19374bc12a5ec0c360ae17356bce8b786fb128c8720951
scope.21.id=ZmllbGQ6U3RyZWV0LktpbmQjdGF4Ojgx
scope.21.kind=field
scope.21.startLine=81
scope.21.endLine=81
scope.21.semanticHash=1ffc0406b9891fcd265a225e83a668fa045f1282588f80c8d11c029bad156d85
scope.22.id=ZmllbGQ6U3RyZWV0LktpbmQjdXRpbGl0eTo4MQ
scope.22.kind=field
scope.22.startLine=81
scope.22.endLine=81
scope.22.semanticHash=57ffcc28d4b999760e3ccf1e65a07d585275d8138d9db41818cdcf63cd5478a8
scope.23.id=ZmllbGQ6U3RyZWV0LlR5cGUjQWxnZW1lZW5Gb25kczo2NA
scope.23.kind=field
scope.23.startLine=64
scope.23.endLine=64
scope.23.semanticHash=011995520c14a038ffa54ebc98694d5252eb5388aca9ffbb2a435db257a1cce8
scope.24.id=ZmllbGQ6U3RyZWV0LlR5cGUjQm91bGV2YXJkREF2cm95TGllZ2U6NDg
scope.24.kind=field
scope.24.startLine=48
scope.24.endLine=48
scope.24.semanticHash=4d9329dff56053928c3463d71398f8a26e93780d191606186dac66e7606195e5
scope.25.id=ZmllbGQ6U3RyZWV0LlR5cGUjQm91bGV2YXJkVGlyb3VDaGFybGVyb2k6NDY
scope.25.kind=field
scope.25.startLine=46
scope.25.endLine=46
scope.25.semanticHash=b1c866b9dded11d7d1f56e37dcecbae3cd5eadf5ed9dc566c296d0eb804480e0
scope.26.id=ZmllbGQ6U3RyZWV0LlR5cGUjQnJ1dWxNZWNoZWxlbjozNQ
scope.26.kind=field
scope.26.startLine=35
scope.26.endLine=35
scope.26.semanticHash=47a9e9295439600f29c3676dd101196895a7767a964edfd5844696ea5e7781ce
scope.27.id=ZmllbGQ6U3RyZWV0LlR5cGUjQnV1cnRzcG9vcndlZ2VuOjU0
scope.27.kind=field
scope.27.startLine=54
scope.27.endLine=54
scope.27.semanticHash=119c553fd9bf69bae723830a916127f1a97bab6f46cce4f23316c9b5e42b73ff
scope.28.id=ZmllbGQ6U3RyZWV0LlR5cGUjQ2VudHJhYWxTdGF0aW9uOjUz
scope.28.kind=field
scope.28.startLine=53
scope.28.endLine=53
scope.28.semanticHash=cb995aca9851838911075002b55fff9e5ac087782fa11652af850531d45ba3c7
scope.29.id=ZmllbGQ6U3RyZWV0LlR5cGUjRGllc3RzZXN0cmFhdExldXZlbjozMA
scope.29.kind=field
scope.29.startLine=30
scope.29.endLine=30
scope.29.semanticHash=1de9e9fde2eace7dd091ca5901b8fdad4b3e8fbe9dcb55c933f390b9d469d5ab
scope.30.id=ZmllbGQ6U3RyZWV0LlR5cGUjRWxla3RyaWNpdGVpdHNjZW50cmFsZTo1Nw
scope.30.kind=field
scope.30.startLine=57
scope.30.endLine=57
scope.30.semanticHash=c52f998d4dc63b1e9bf658af011425c817a3a6270654e2d84ed4cb8c10b04a43
scope.31.id=ZmllbGQ6U3RyZWV0LlR5cGUjRXh0cmFCZWxhc3Rpbmc6NjE
scope.31.kind=field
scope.31.startLine=61
scope.31.endLine=61
scope.31.semanticHash=2379b40617158874f892aa33e69b2a868542a1b1b6b31dc93b59f92a7cd4540e
scope.32.id=ZmllbGQ6U3RyZWV0LlR5cGUjR3JhbmRQbGFjZU1vbnM6NDI
scope.32.kind=field
scope.32.startLine=42
scope.32.endLine=42
scope.32.semanticHash=d2cb78677c964370b3a7166f1bd4f6c2e38369d27739ccd2f126fc61c24d6989
scope.33.id=ZmllbGQ6U3RyZWV0LlR5cGUjR3JhdGlzUGFya2VyZW46NjY
scope.33.kind=field
scope.33.startLine=66
scope.33.endLine=66
scope.33.semanticHash=39d22fe624fb6b74d00ab68d4a115534ade9c3ba297b48fa877578274d1a2297
scope.34.id=ZmllbGQ6U3RyZWV0LlR5cGUjR3JvZW5wbGFhdHNBbnR3ZXJwZW46Mzk
scope.34.kind=field
scope.34.startLine=39
scope.34.endLine=39
scope.34.semanticHash=79ff96e2f13e09c96ea47e04d670f73e79686796c430309cbc28ded9d80d55bc
scope.35.id=ZmllbGQ6U3RyZWV0LlR5cGUjR3JvdGVNYXJrdEhhc3NlbHQ6NDM
scope.35.kind=field
scope.35.startLine=43
scope.35.endLine=43
scope.35.semanticHash=4491f0e7cb1c358a88f694804a81fdf864d844fc4695013bb998b63521bef296
scope.36.id=ZmllbGQ6U3RyZWV0LlR5cGUjSG9vZ3N0cmFhdEJydXNzZWw6NDU
scope.36.kind=field
scope.36.startLine=45
scope.36.endLine=45
scope.36.semanticHash=581b5684648019f5fb9213a2dcfedf45f2010f319a1ca57a6c47bc46c1a8c26d
scope.37.id=ZmllbGQ6U3RyZWV0LlR5cGUjSW5rb21zdGVuQmVsYXN0aW5nOjYw
scope.37.kind=field
scope.37.startLine=60
scope.37.endLine=60
scope.37.semanticHash=b96a2333b6c20cced53421e71033c2a09ce6671c62bf556de53cb2ffd77f2ab7
scope.38.id=ZmllbGQ6U3RyZWV0LlR5cGUjS2Fuczo2Mw
scope.38.kind=field
scope.38.startLine=63
scope.38.endLine=63
scope.38.semanticHash=e931df0bc9d506c6fac70de6f7d358eed9585e641fed4c5c375ea96fb5fe20d9
scope.39.id=ZmllbGQ6U3RyZWV0LlR5cGUjS2FwZWxsZXN0cmFhdE9vc3RlbmRlOjMz
scope.39.kind=field
scope.39.startLine=33
scope.39.endLine=33
scope.39.semanticHash=1dc5378f1e3e1760f63f1e4ddf5426625dbf7a9819dc513914c752f6c6d7448b
scope.40.id=ZmllbGQ6U3RyZWV0LlR5cGUjTGFuZ2VTdGVlbnN0cmFhdEtvcnRyaWprOjQx
scope.40.kind=field
scope.40.startLine=41
scope.40.endLine=41
scope.40.semanticHash=689f41dc92ff134ab822c5172c995f4473b1e59a7ba2a3b2170dc06c5920a253
scope.41.id=ZmllbGQ6U3RyZWV0LlR5cGUjTGlwcGVuc2xhYW5Lbm9ra2U6Mzc
scope.41.kind=field
scope.41.startLine=37
scope.41.endLine=37
scope.41.semanticHash=0e03f75f0a700bc6165d9daed780d0fc7b4f32e6df985c40207c5af51268aede
scope.42.id=ZmllbGQ6U3RyZWV0LlR5cGUjTWVpckFudHdlcnBlbjo0OQ
scope.42.kind=field
scope.42.startLine=49
scope.42.endLine=49
scope.42.semanticHash=3f1942bbc6317ca4f19e3f2dcbd28d5f5b6cfee2bedfabb4e3cb8c1dd76ef3af
scope.43.id=ZmllbGQ6U3RyZWV0LlR5cGUjTmFhckRlR2V2YW5nZW5pczo2Nw
scope.43.kind=field
scope.43.startLine=67
scope.43.endLine=67
scope.43.semanticHash=cf4e0bfc1aaa06ec4622e0bb82a224cbadcde194665449ff8bf417840a47269c
scope.44.id=ZmllbGQ6U3RyZWV0LlR5cGUjTmlldXdzdHJhYXRCcnVzc2VsOjUw
scope.44.kind=field
scope.44.startLine=50
scope.44.endLine=50
scope.44.semanticHash=ae82a82382f37e4b11deca7279535300a541831e3f9a5d82d01e0c4ba90deeea
scope.45.id=ZmllbGQ6U3RyZWV0LlR5cGUjTm9vcmRTdGF0aW9uOjUy
scope.45.kind=field
scope.45.startLine=52
scope.45.endLine=52
scope.45.semanticHash=8787a5d11ecfa55c3f874641622a653d08dbcfae1e5af873f483416ad411eb9d
scope.46.id=ZmllbGQ6U3RyZWV0LlR5cGUjT3BCZXpvZWs6NjU
scope.46.kind=field
scope.46.startLine=65
scope.46.endLine=65
scope.46.semanticHash=aa67359711be5aedf8f98d0b65e80fd09214c1b4c81b78d9d44911b96b45791c
scope.47.id=ZmllbGQ6U3RyZWV0LlR5cGUjUGxhY2VEZUxBbmdlTmFtdXI6NDQ
scope.47.kind=field
scope.47.startLine=44
scope.47.endLine=44
scope.47.semanticHash=05b3ba3d327974c9a51426b557841cb15d557f9e5239c1cb42ffe361e7fb3fa0
scope.48.id=ZmllbGQ6U3RyZWV0LlR5cGUjUGxhY2VEdU1vbnVtZW50U3BhOjMy
scope.48.kind=field
scope.48.startLine=32
scope.48.endLine=32
scope.48.semanticHash=d2df8cd98fd0952c6da117bba6155d04cba05e237f719ab240921ed2102e59d7
scope.49.id=ZmllbGQ6U3RyZWV0LlR5cGUjUGxhY2VWZXJ0ZVZlcnZpZXJzOjM2
scope.49.kind=field
scope.49.startLine=36
scope.49.endLine=36
scope.49.semanticHash=8d369e9ace488c0f84da24b651ac9a55aaa8e878e44fdedd87797e9bf834ad12
scope.50.id=ZmllbGQ6U3RyZWV0LlR5cGUjUnVlRGVEaWVraXJjaEFybG9uOjM0
scope.50.kind=field
scope.50.startLine=34
scope.50.endLine=34
scope.50.semanticHash=978ec8bcf8bfc80f26b81fcb71ef4d1a031636dd9817881e674ee28260709102
scope.51.id=ZmllbGQ6U3RyZWV0LlR5cGUjUnVlR3JhbmRlRGluYW50OjI5
scope.51.kind=field
scope.51.startLine=29
scope.51.endLine=29
scope.51.semanticHash=afafc7f7a5ab99e077a48d4e846bf59627e0fbac21bc4b5fcd41a295b06e167f
scope.52.id=ZmllbGQ6U3RyZWV0LlR5cGUjUnVlUm95YWxlVG91cm5haTozOA
scope.52.kind=field
scope.52.startLine=38
scope.52.endLine=38
scope.52.semanticHash=09a250b213b1b3e258661b9d0c458dcb3e4f0b1628aa0fe07c10672e2e419b4c
scope.53.id=ZmllbGQ6U3RyZWV0LlR5cGUjUnVlU3RMZW9uYXJkTGllZ2U6NDA
scope.53.kind=field
scope.53.startLine=40
scope.53.endLine=40
scope.53.semanticHash=a91f6dc2d1e4be8deca459a284add43253bdf27179bef79497a66fff3c21b59c
scope.54.id=ZmllbGQ6U3RyZWV0LlR5cGUjU3RlZW5zdHJhYXRCcnVnZ2U6MzE
scope.54.kind=field
scope.54.startLine=31
scope.54.endLine=31
scope.54.semanticHash=636b4447839bafb1735098e6d1ca9dee2c72a0e55d6c84ab9cecbcf02e0dbd6e
scope.55.id=ZmllbGQ6U3RyZWV0LlR5cGUjVmVsZHN0cmFhdEdlbnQ6NDc
scope.55.kind=field
scope.55.startLine=47
scope.55.endLine=47
scope.55.semanticHash=7bb892ad0cfda077359ff0ab292c6b8b3b40d80554d640eb00aa1b35e7587c98
scope.56.id=ZmllbGQ6U3RyZWV0LlR5cGUjV2F0ZXJtYWF0c2NoYXBwaWo6NTg
scope.56.kind=field
scope.56.startLine=58
scope.56.endLine=58
scope.56.semanticHash=5d8b700af037b59265fd371ea21135f8726ff4b966b71c39f583f80d0034807b
scope.57.id=ZmllbGQ6U3RyZWV0LlR5cGUjWnVpZFN0YXRpb246NTU
scope.57.kind=field
scope.57.startLine=55
scope.57.endLine=55
scope.57.semanticHash=a5a6f278fa27884c68480ccc3e03bc827445a099e5e2d708b63e51c739280048
scope.58.id=ZmllbGQ6U3RyZWV0LlR5cGUjZmFjdG9yeTo2OQ
scope.58.kind=field
scope.58.startLine=69
scope.58.endLine=69
scope.58.semanticHash=58dd23ca4a3e28f281e0e25469be60cefbb9fbc4f450e6b4abe03a46df3e3a0a
scope.59.id=ZmllbGQ6U3RyZWV0LlR5cGUjc3RhcnQ6Mjc
scope.59.kind=field
scope.59.startLine=27
scope.59.endLine=27
scope.59.semanticHash=46f62becf1b0bed77094e9330e7cb0fa0667ee68bdf613568b07c3ce298986a8
scope.60.id=bWV0aG9kOlN0cmVldCNraW5kKDApOjI0
scope.60.kind=method
scope.60.startLine=24
scope.60.endLine=24
scope.60.semanticHash=6c7c96f3ae1fe54162b7a31e301571e4f4eb4b61d070e4dec8c124ee12acd135
scope.61.id=bWV0aG9kOlN0cmVldCN0eXBlKDApOjIy
scope.61.kind=method
scope.61.startLine=22
scope.61.endLine=22
scope.61.semanticHash=4b6fa913a7b303ac495e0a6fee1b3239d9a3ce212d1ee084a52ccc0971c47411
scope.62.id=bWV0aG9kOlN0cmVldC5Db2xvdXIjY3RvcigwKTo4NA
scope.62.kind=method
scope.62.startLine=1
scope.62.endLine=91
scope.62.semanticHash=fae3a36999abeaeb3805e7a722fe370efbb896372829f9ccf59756fcbf740112
scope.63.id=bWV0aG9kOlN0cmVldC5GYWN0b3J5I2NyZWF0ZSgyKTo4OQ
scope.63.kind=method
scope.63.startLine=89
scope.63.endLine=89
scope.63.semanticHash=fbd7a0c18fe1b5ef1eaea1320fb77d8d988d0c44bdfc7e5c0aa661029a6a66b4
scope.64.id=bWV0aG9kOlN0cmVldC5LaW5kI2N0b3IoMCk6ODA
scope.64.kind=method
scope.64.startLine=1
scope.64.endLine=91
scope.64.semanticHash=fae3a36999abeaeb3805e7a722fe370efbb896372829f9ccf59756fcbf740112
scope.65.id=bWV0aG9kOlN0cmVldC5UeXBlI2NyZWF0ZSgxKTo3NQ
scope.65.kind=method
scope.65.startLine=75
scope.65.endLine=77
scope.65.semanticHash=66fbbea5540a2d7cfe40aabc92dd548258aa7593e8a9395cece5d45a541d79a2
scope.66.id=bWV0aG9kOlN0cmVldC5UeXBlI2N0b3IoMSk6NzE
scope.66.kind=method
scope.66.startLine=71
scope.66.endLine=73
scope.66.semanticHash=e8dc649966f073d8db0b2a33dc3bc9d57c0216e1293960a554f46ace8436f85b
*/
