import org.junit.jupiter.api.DynamicTest;
import the.monopoly.game.specs.acceptance.AcceptanceRuntime;
import the.monopoly.game.specs.acceptance.Ir;
import the.monopoly.game.specs.acceptance.MonopolyStepHandlers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runner adapter for {@code bb gherkin-mutator}, hosted in one long-lived JVM.
 * <p>
 * Reads one JSON job per line on stdin and writes one JSON response per line on
 * stdout. Diagnostics go to stderr; stdout carries protocol only.
 * <p>
 * The previous adapter shelled out to {@code mvn test} for every mutation, which
 * cost about twenty seconds of Maven and JVM startup to run assertions that take
 * milliseconds. This one pays that cost once by hosting the worker in one long-
 * lived JVM.
 * <p>
 * Mutated IR is read directly and handed to the existing acceptance runtime;
 * no generated source or per-mutant Java compilation is needed.
 */
public final class AcceptanceMutationRunner {

  /**
   * Upper bound on one mutation job, from reading the mutated IR through
   * execution. A mutant that sends a test into an infinite loop must not tie
   * up a worker indefinitely, or several such mutations could strand every
   * worker and hang the whole run.
   */
  private static final long JOB_TIMEOUT_MS = 5 * 60 * 1000;

  private final AtomicReference<Thread> interruptionGate = new AtomicReference<>();

  private AcceptanceMutationRunner(Path root) {
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("usage: AcceptanceMutationRunner <project-root>");
      System.exit(2);
    }
    AcceptanceMutationRunner runner = new AcceptanceMutationRunner(Path.of(args[0]).toAbsolutePath());
    System.err.println("runner adapter ready (hot JVM)");

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    for (String line = in.readLine(); line != null; line = in.readLine()) {
      if (line.isBlank()) continue;
      System.out.println(runner.handle(Json.readObject(line)));
      System.out.flush();
    }
  }

  private String handle(Map<String, String> job) {
    String id = job.getOrDefault("id", "");
    long started = System.nanoTime();
    PrintStream protocol = System.out;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
    AtomicReference<String> reply = new AtomicReference<>();
    Thread worker = new Thread(() -> reply.set(runJob(job, started, output)), "mutation-job-" + id);
    interruptionGate.set(worker);
    worker.setDaemon(true);
    worker.start();
    try {
      worker.join(JOB_TIMEOUT_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    String result = reply.get();
    if (result == null) {
      // The job did not finish within the bound. Interrupt the worker so the
      // running JUnit execution (and any blocked generator read) unwinds,
      // report a bounded result, and move on to keep the worker responsive.
      Thread active = interruptionGate.getAndSet(null);
      if (active != null) active.interrupt();
      result = response(id, "infrastructure_error", output.toString(StandardCharsets.UTF_8),
          "mutation job exceeded its " + (JOB_TIMEOUT_MS / 1000) + "s bound; treating as a runner error", started);
    }
    interruptionGate.set(null);

    System.setOut(protocol);
    return result;
  }

  /** Processes a mutation job end to end and returns its JSON response. */
  private String runJob(Map<String, String> job, long started, ByteArrayOutputStream output) {
    String id = job.getOrDefault("id", "");
    try {
      List<DynamicTest> tests = new AcceptanceRuntime(MonopolyStepHandlers.handlers())
          .execute(Json.readIr(Path.of(job.get("feature_json")))).toList();
      if (tests.isEmpty())
        return response(id, "infrastructure_error", output.toString(StandardCharsets.UTF_8),
            "no tests were discovered", started);
      for (DynamicTest test : tests) {
        try {
          test.getExecutable().execute();
        } catch (Throwable failure) {
          return response(id, "test_failure", output.toString(StandardCharsets.UTF_8),
              failure.toString(), started);
        }
      }
      return response(id, "test_success", output.toString(StandardCharsets.UTF_8), "", started);
    } catch (Exception cause) {
      System.err.println("job " + id + " failed: " + cause);
      return response(id, "infrastructure_error", output.toString(StandardCharsets.UTF_8),
          String.valueOf(cause.getMessage()), started);
    }
  }

  private static String response(String id, String outcome, String output, String error, long startedNanos) {
    return "{\"id\":" + Json.write(id)
        + ",\"outcome\":" + Json.write(outcome)
        + ",\"output\":" + Json.write(output)
        + ",\"error\":" + Json.write(error)
        + ",\"duration\":" + (System.nanoTime() - startedNanos)
        + "}";
  }

  /**
   * Just enough JSON for the worker protocol and parser-produced acceptance IR.
   * Keeping it here avoids putting a JSON library on the test classpath purely
   * for the adapter.
   */
  static final class Json {
    private final String text;
    private int at;

    private Json(String text) {
      this.text = text;
    }

    static Ir readIr(Path path) throws Exception {
      Object value = new Json(Files.readString(path)).readJsonValue();
      Map<?, ?> feature = object(value);
      return new Ir(
          string(feature, "name"),
          steps(feature.get("background")),
          scenarios(feature.get("scenarios")));
    }

    private static List<Ir.Step> steps(Object value) {
      if (value == null) return List.of();
      return list(value).stream().map(Json::step).toList();
    }

    private static Ir.Step step(Object value) {
      Map<?, ?> step = object(value);
      return new Ir.Step(string(step, "keyword"), string(step, "text"));
    }

    private static List<Ir.Scenario> scenarios(Object value) {
      return list(value).stream().map(Json::scenario).toList();
    }

    private static Ir.Scenario scenario(Object value) {
      Map<?, ?> scenario = object(value);
      List<Map<String, String>> examples = list(scenario.get("examples")).stream()
          .map(Json::example).toList();
      return new Ir.Scenario(string(scenario, "name"), steps(scenario.get("steps")), examples);
    }

    private static Map<String, String> example(Object value) {
      Map<?, ?> example = object(value);
      Map<String, String> result = new HashMap<>();
      example.forEach((key, item) -> result.put(String.valueOf(key), String.valueOf(item)));
      return result;
    }

    private static Map<?, ?> object(Object value) {
      if (value instanceof Map<?, ?> object) return object;
      throw new IllegalArgumentException("Expected JSON object but found " + value);
    }

    private static List<?> list(Object value) {
      if (value instanceof List<?> list) return list;
      throw new IllegalArgumentException("Expected JSON array but found " + value);
    }

    private static String string(Map<?, ?> object, String key) {
      Object value = object.get(key);
      if (value instanceof String text) return text;
      throw new IllegalArgumentException("Expected string field '" + key + "'");
    }

    static Map<String, String> readObject(String line) {
      Json json = new Json(line);
      Map<String, String> fields = new HashMap<>();
      json.skipSpace();
      json.expect('{');
      json.skipSpace();
      if (json.peek() == '}') return fields;
      while (true) {
        json.skipSpace();
        String key = json.readString();
        json.skipSpace();
        json.expect(':');
        json.skipSpace();
        fields.put(key, json.readValue());
        json.skipSpace();
        if (json.peek() == ',') {
          json.at++;
          continue;
        }
        json.expect('}');
        return fields;
      }
    }

    private Object readJsonValue() {
      skipSpace();
      return switch (peek()) {
        case '"' -> readString();
        case '{' -> readJsonObject();
        case '[' -> readJsonArray();
        default -> readToken();
      };
    }

    private Map<String, Object> readJsonObject() {
      expect('{');
      Map<String, Object> result = new HashMap<>();
      skipSpace();
      if (peek() == '}') {
        at++;
        return result;
      }
      while (true) {
        skipSpace();
        String key = readString();
        skipSpace();
        expect(':');
        result.put(key, readJsonValue());
        skipSpace();
        if (peek() == ',') {
          at++;
          continue;
        }
        expect('}');
        return result;
      }
    }

    private List<Object> readJsonArray() {
      expect('[');
      List<Object> result = new ArrayList<>();
      skipSpace();
      if (peek() == ']') {
        at++;
        return result;
      }
      while (true) {
        result.add(readJsonValue());
        skipSpace();
        if (peek() == ',') {
          at++;
          continue;
        }
        expect(']');
        return result;
      }
    }

    private String readValue() {
      char next = peek();
      if (next == '"') return readString();
      return readToken();
    }

    private String readToken() {
      int start = at;
      while (at < text.length() && ",}]".indexOf(text.charAt(at)) < 0) at++;
      return text.substring(start, at).trim();
    }

    private String readString() {
      expect('"');
      StringBuilder value = new StringBuilder();
      while (true) {
        char next = text.charAt(at++);
        if (next == '"') return value.toString();
        if (next != '\\') {
          value.append(next);
          continue;
        }
        char escaped = text.charAt(at++);
        switch (escaped) {
          case 'n' -> value.append('\n');
          case 't' -> value.append('\t');
          case 'r' -> value.append('\r');
          case 'b' -> value.append('\b');
          case 'f' -> value.append('\f');
          case 'u' -> {
            value.append((char) Integer.parseInt(text.substring(at, at + 4), 16));
            at += 4;
          }
          default -> value.append(escaped);
        }
      }
    }

    private char peek() {
      return text.charAt(at);
    }

    private void skipSpace() {
      while (at < text.length() && Character.isWhitespace(text.charAt(at))) at++;
    }

    private void expect(char expected) {
      char found = text.charAt(at++);
      if (found != expected)
        throw new IllegalArgumentException("Expected " + expected + " but found " + found + " at " + (at - 1));
    }

    static String write(String value) {
      StringBuilder out = new StringBuilder("\"");
      for (int i = 0; i < value.length(); i++) {
        char next = value.charAt(i);
        switch (next) {
          case '"' -> out.append("\\\"");
          case '\\' -> out.append("\\\\");
          case '\n' -> out.append("\\n");
          case '\r' -> out.append("\\r");
          case '\t' -> out.append("\\t");
          case '\b' -> out.append("\\b");
          case '\f' -> out.append("\\f");
          default -> {
            if (next < 0x20) out.append(String.format("\\u%04x", (int) next));
            else out.append(next);
          }
        }
      }
      return out.append('"').toString();
    }
  }
}
