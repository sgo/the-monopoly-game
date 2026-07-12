package the.monopoly.game.test.fixtures.repository;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

public class AbstractRepository<Key, Value> {
  private LinkedHashMap<Key, Value> records = new LinkedHashMap<>();

  public Value get(Key key) {
    return records.get(key);
  }

  public void put(Key key, Value value) {
    records.put(key, value);
  }

  public Stream<Value> all() {
    return records.values().stream();
  }

  public boolean isEmpty() {
    return records.isEmpty();
  }
}
