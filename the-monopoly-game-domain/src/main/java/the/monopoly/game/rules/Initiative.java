package the.monopoly.game.rules;

import the.monopoly.game.components.players.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Who starts, and who follows. The highest roll takes the first turn and play
 * then continues clockwise, which is the order the players are sitting in.
 */
public class Initiative {
  private final Rolls rolls;

  public Initiative(Rolls rolls) {
    this.rolls = rolls;
  }

  /** The seated players, rotated so that whoever wins initiative goes first. */
  public List<Player> order(List<Player> seated) {
    int winner = seated.indexOf(winnerAmong(seated));
    List<Player> order = new ArrayList<>(seated.subList(winner, seated.size()));
    order.addAll(seated.subList(0, winner));
    return order;
  }

  /**
   * Everyone rolls; the highest wins. Players who tie for the highest roll
   * settle it among themselves, as many times as it takes.
   */
  private Player winnerAmong(List<Player> players) {
    List<Player> contenders = players;
    while (contenders.size() > 1) {
      Map<Integer, List<Player>> byRoll = contenders.stream()
          .collect(groupingBy(rolls::totalFor, LinkedHashMap::new, toList()));
      contenders = byRoll.get(Collections.max(byRoll.keySet()));
    }
    return contenders.getFirst();
  }

  /** What a player rolls when rolling for initiative. */
  @FunctionalInterface
  public interface Rolls {
    int totalFor(Player player);
  }
}

/* mutate4java-manifest
version=1
moduleHash=ed267bf3f7524fbcaef04b2db0a4023b2a4de6802d1996263c615b587775420e
scope.0.id=Y2xhc3M6SW5pdGlhdGl2ZSNJbml0aWF0aXZlOjE4
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=52
scope.0.semanticHash=ece30d7f9bb304953141ed18ef11de5b8fdb77e5132697ce3757da05979ab9a5
scope.1.id=Y2xhc3M6SW5pdGlhdGl2ZS5Sb2xscyNSb2xsczo0OA
scope.1.kind=class
scope.1.startLine=48
scope.1.endLine=51
scope.1.semanticHash=ba41884004f4c0dd03cbd271bb445564623335c0ceda3abb54f4bab85db7f06c
scope.2.id=ZmllbGQ6SW5pdGlhdGl2ZSNyb2xsczoxOQ
scope.2.kind=field
scope.2.startLine=19
scope.2.endLine=19
scope.2.semanticHash=79336ad31a60872f2b3173c071f8ff3a878fcfb30467d05f78c056fea50a7789
scope.3.id=bWV0aG9kOkluaXRpYXRpdmUjY3RvcigxKToyMQ
scope.3.kind=method
scope.3.startLine=21
scope.3.endLine=23
scope.3.semanticHash=22edb672429bad870d460aa188c15c199478252b300c5d01ef210a9818db004e
scope.4.id=bWV0aG9kOkluaXRpYXRpdmUjb3JkZXIoMSk6MjY
scope.4.kind=method
scope.4.startLine=26
scope.4.endLine=31
scope.4.semanticHash=80d659f5d775fe8b225aa358f050e155dd79b1ca63230163491ce8887c5bd114
scope.5.id=bWV0aG9kOkluaXRpYXRpdmUjd2lubmVyQW1vbmcoMSk6Mzc
scope.5.kind=method
scope.5.startLine=37
scope.5.endLine=45
scope.5.semanticHash=f1ff70480417e7ca8729ea4c5516ff66e244dbf6b968f1d8b0edf9c445f9b3ea
scope.6.id=bWV0aG9kOkluaXRpYXRpdmUuUm9sbHMjdG90YWxGb3IoMSk6NTA
scope.6.kind=method
scope.6.startLine=50
scope.6.endLine=50
scope.6.semanticHash=46292001e3a3e166e2d24839c89e99aec3298362fc11832c3534bfd6e06e82ff
*/
