package the.monopoly.game.test.fixtures.model;

public class Counter {
  private int number;

  public void increment() {
    number++;
  }

  public int number() {
    return number;
  }

  public void plus(int number) {
    this.number += number;
  }

  public Counter plus(Counter counter) {
    plus(counter.number);
    return this;
  }
}
