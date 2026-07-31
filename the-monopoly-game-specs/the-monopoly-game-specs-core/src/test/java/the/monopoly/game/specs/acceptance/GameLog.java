package the.monopoly.game.specs.acceptance;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;
import the.monopoly.game.Game;
import the.monopoly.game.Game.Journal.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * The SLF4J log of the game journal: what the journal wrote to the game's
 * logger as it went. The journal logs each entry with the entry itself, so a
 * scenario can ask the log the same questions it asks the journal.
 * <p>
 * The capture lives for the whole test JVM, because the game logs through a
 * static logger. A scenario therefore notes where the log stood when it began
 * and only reads what was written from that point on.
 */
final class GameLog {
  private static final List<ILoggingEvent> EVENTS = new ArrayList<>();

  private static final AppenderBase<ILoggingEvent> APPENDER = new AppenderBase<>() {
    @Override
    protected void append(ILoggingEvent event) {
      synchronized (EVENTS) {
        EVENTS.add(event);
      }
    }
  };

  static {
    APPENDER.start();
    Logger journal = (Logger) LoggerFactory.getLogger(Game.Journal.class);
    journal.setLevel(Level.DEBUG);
    journal.addAppender(APPENDER);
  }

  private GameLog() {
  }

  /** Where the log stood when a scenario began, so it only reads its own game. */
  static int offset() {
    synchronized (EVENTS) {
      return EVENTS.size();
    }
  }

  /** The journal entries the game wrote to its log from that point on. */
  static List<Entry> recordedSince(int offset) {
    synchronized (EVENTS) {
      return EVENTS.subList(offset, EVENTS.size()).stream().map(GameLog::entryOf).toList();
    }
  }

  private static Entry entryOf(ILoggingEvent event) {
    Object[] arguments = event.getArgumentArray();
    if (arguments == null || arguments.length == 0 || !(arguments[0] instanceof Entry entry))
      throw new AssertionError(
          "The game log line \"" + event.getFormattedMessage() + "\" carries no journal entry."
      );
    return entry;
  }
}
