package the.monopoly.game.test.fixtures.repository;

import java.util.HashMap;
import java.util.Map;

public class AbstractRepository<Key, Value> {
  private Map<Key, Value> records = new HashMap<>();

  public Value get(Key key) {
    return records.get(key);
  }

  public void put(Key key, Value value) {
    records.put(key, value);
  }
}
