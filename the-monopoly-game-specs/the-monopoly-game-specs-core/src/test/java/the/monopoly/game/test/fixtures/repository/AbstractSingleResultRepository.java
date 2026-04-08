package the.monopoly.game.test.fixtures.repository;

public abstract class AbstractSingleResultRepository<T> {
  private T result;

  public void set(T result) {
    this.result = result;
  }

  public T get() {
    return result;
  }
}
