package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.Set;

/**
 * The space every player starts on and is paid for passing.
 * <p>
 * The activated rules are held live rather than copied, so a rule switched on
 * after the board was laid out still applies.
 */
public record StartSpace(Street.Type type, Set<Rule> activatedRules) implements Street {
  private static final Money SALARY = new Money(200);

  /** The only optional rule that applies here pays the salary twice over. */
  private static final Rule.Processor<Boolean> DOUBLES_SALARY = rule -> true;

  @Override
  public Street.Kind kind() {
    return Street.Kind.start;
  }

  /** What the bank pays a player for passing Start. Passing is never doubled. */
  public Money salary() {
    return SALARY;
  }

  /**
   * What the bank pays a player for landing exactly on Start, which the
   * optional double-salary rule pays twice over.
   */
  public Money salaryForLanding() {
    return doublesSalary() ? SALARY.plus(SALARY) : SALARY;
  }

  private boolean doublesSalary() {
    return activatedRules.stream().anyMatch(rule -> rule.process(DOUBLES_SALARY));
  }

  static Street.Factory factory() {
    return StartSpace::new;
  }
}

/* mutate4java-manifest
version=1
moduleHash=d32abf1f7e363b707527c7c4c3734cc1e2b1dd6e638dddd3a4133bffbe65f571
scope.0.id=Y2xhc3M6U3RhcnRTcGFjZSNTdGFydFNwYWNlOjE0
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=37
scope.0.semanticHash=0fa13d763bd9169268f16aaa3603a08e687db5c3ad862623877d0b0b0a2360c2
scope.1.id=ZmllbGQ6U3RhcnRTcGFjZSNET1VCTEVTX1NBTEFSWToxOA
scope.1.kind=field
scope.1.startLine=18
scope.1.endLine=18
scope.1.semanticHash=5efc0de306b6168d871b9fda0988dadb897d228a5827c04c7a8dc1792b7e774f
scope.2.id=ZmllbGQ6U3RhcnRTcGFjZSNTQUxBUlk6MTU
scope.2.kind=field
scope.2.startLine=15
scope.2.endLine=15
scope.2.semanticHash=c7918170876eedccd187a57ec5947773fd03e73bf4c638a10b3606fba093ed8b
scope.3.id=ZmllbGQ6U3RhcnRTcGFjZSNhY3RpdmF0ZWRSdWxlczoxNA
scope.3.kind=field
scope.3.startLine=14
scope.3.endLine=14
scope.3.semanticHash=48efe0e75cf42ae61ae0a952494ae731cfe389f009303b9eb6c2560012d1faaf
scope.4.id=ZmllbGQ6U3RhcnRTcGFjZSN0eXBlOjE0
scope.4.kind=field
scope.4.startLine=14
scope.4.endLine=14
scope.4.semanticHash=578fb8351c3bd9fec9344a1ba176367ac2a41a0f427b28efd82181214901570e
scope.5.id=bWV0aG9kOlN0YXJ0U3BhY2UjY3RvcigyKToxNA
scope.5.kind=method
scope.5.startLine=1
scope.5.endLine=37
scope.5.semanticHash=a328db6dda46e9cce46a04851eb9d15f1ce09a70f84ffe5911d3492f111779a5
scope.6.id=bWV0aG9kOlN0YXJ0U3BhY2UjZG91Ymxlc1NhbGFyeSgwKTozMA
scope.6.kind=method
scope.6.startLine=30
scope.6.endLine=32
scope.6.semanticHash=82128d6971e7eddec5bf1efa747da4e13e3aabb38916c8c2f1b21e1c40b7a73f
scope.7.id=bWV0aG9kOlN0YXJ0U3BhY2UjZmFjdG9yeSgwKTozNA
scope.7.kind=method
scope.7.startLine=34
scope.7.endLine=36
scope.7.semanticHash=9a6448fea5bd31b90682428e4e49e7df8e47d08c11c687f21170083f196e2165
scope.8.id=bWV0aG9kOlN0YXJ0U3BhY2Uja2luZCgwKToyMA
scope.8.kind=method
scope.8.startLine=20
scope.8.endLine=23
scope.8.semanticHash=ca7cca43f7e8a70df0d122fc6177daaa45fea3dd1446e54ba218dd871dfbb1d9
scope.9.id=bWV0aG9kOlN0YXJ0U3BhY2Ujc2FsYXJ5KDApOjI2
scope.9.kind=method
scope.9.startLine=26
scope.9.endLine=28
scope.9.semanticHash=2938c04111ee144049ebfd54f84e67a673c63da9648be59c0487bcc2ac30a903
*/
