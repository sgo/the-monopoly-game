package the.monopoly.game;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A board space is named in two unrelated-looking places in {@link Report}:
 * where a move lands, and what sent a pawn to jail. Nothing in the compiler
 * ties those two mentions together — a future edit that special-cases one
 * and not the other would silently print two different names for the same
 * space depending on which entry mentioned it, the exact drift this class
 * already had once between a hand-written jail-cause table and a
 * hand-written movement table before they were folded into one lookup. This
 * sweeps every space currently on the board and pins both mentions, and the
 * lookup's own escape hatch, to agree.
 */
@Tag("property-test")
class ReportBoardSpaceNamePropertyTest {
  private static final Pattern RUN_TOGETHER_WORDS = Pattern.compile("[a-z][A-Z]");
  @Test
  void aSpaceIsNamedTheSameWayWhetherItIsWhereAMoveLandsOrWhatSentAPawnToJail() {
    checkOverEverySpace(space -> nameAsLandingSpot(space).equals(nameAsJailCause(space)));
  }

  @Test
  void everySpaceGetsANonBlankNameWithNoRunTogetherWords() {
    checkOverEverySpace(space -> {
      String name = nameAsLandingSpot(space);
      return !name.isBlank() && !RUN_TOGETHER_WORDS.matcher(name).find();
    });
  }

  /**
   * This is a finite domain, so traversing every value directly expresses the
   * invariant without relying on a random generator to produce a permutation.
   */
  private static void checkOverEverySpace(java.util.function.Predicate<Street.Type> property) {
    for (Street.Type space : Street.Type.values()) assertThat(property.test(space)).isTrue();
  }

  private static final String LANDING_SPOT_PREFIX = "dog moves from position 0 (Start) to 1 (";

  private static String nameAsLandingSpot(Street.Type space) {
    // A space's own name can itself contain parentheses (Hoogstraat Brussel's
    // does), so the wrapping paren can't be found by scanning for the last
    // one; it is exactly what is left after the known prefix and the line's
    // own final character, both fixed by how Report.line renders Moved.
    String line = Report.of(List.of(new Entry.Moved(Pawn.dog.id(), 0, 1, Street.Type.start, space)));
    return line.substring(LANDING_SPOT_PREFIX.length(), line.length() - 1);
  }

  private static String nameAsJailCause(Street.Type space) {
    String line = Report.of(List.of(new Entry.JailEntered(Pawn.dog.id(), space)));
    return line.substring(line.indexOf("landing on ") + "landing on ".length());
  }
}
