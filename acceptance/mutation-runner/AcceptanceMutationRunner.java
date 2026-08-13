import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

/**
 * Runner adapter for {@code bb gherkin-mutator}, hosted in one long-lived JVM.
 * <p>
 * Reads one JSON job per line on stdin and writes one JSON response per line on
 * stdout. Diagnostics go to stderr; stdout carries protocol only.
 * <p>
 * The previous adapter shelled out to {@code mvn test} for every mutation, which
 * cost about twenty seconds of Maven and JVM startup to run assertions that take
 * milliseconds. This one pays that cost once: the entry point is compiled in
 * process and executed through the JUnit Platform launcher, so a mutation costs
 * roughly the time of the tests themselves.
 * <p>
 * This project's generator embeds the IR in the generated Java source rather
 * than reading it at run time, so a mutated IR still needs the entry point
 * regenerated and recompiled. Hiding that from the mutator is the adapter's job.
 */
public final class AcceptanceMutationRunner {
  private static final String GENERATED_PACKAGE = "the.monopoly.game.specs.acceptance.generated";

  /**
   * Upper bound on one mutation job, from entry-point generation through
   * compilation and JUnit execution. A mutant that sends a generated test
   * into an infinite loop must not tie up a worker indefinitely, or four such
   * mutations could strand every worker and hang the whole run.
   */
  private static final long JOB_TIMEOUT_MS = 5 * 60 * 1000;

  private final Path root;
  private final Path generator;
  private final AtomicReference<Thread> interruptionGate = new AtomicReference<>();
  private final AtomicReference<Process> generatorProcess = new AtomicReference<>();

  private AcceptanceMutationRunner(Path root) {
    this.root = root;
    this.generator = root.resolve("acceptance/acceptance-entrypoint-generator.bb");
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
    Path work;
    try {
      work = Files.createTempDirectory("acceptance-mutation-");
    } catch (Exception cause) {
      return response(id, "infrastructure_error", "", "cannot create work directory: " + cause, started);
    }

    AtomicReference<String> reply = new AtomicReference<>();
    Thread worker = new Thread(() -> reply.set(runJob(job, started, work, output)), "mutation-job-" + id);
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
      Process generator = generatorProcess.getAndSet(null);
      if (generator != null) generator.destroy();
      result = response(id, "infrastructure_error", output.toString(StandardCharsets.UTF_8),
          "mutation job exceeded its " + (JOB_TIMEOUT_MS / 1000) + "s bound; treating as a runner error", started);
    }
    interruptionGate.set(null);

    System.setOut(protocol);
    deleteTree(work);
    return result;
  }

  /** Processes a mutation job end to end and returns its JSON response. */
  private String runJob(Map<String, String> job, long started, Path work, ByteArrayOutputStream output) {
    String id = job.getOrDefault("id", "");
    try {
      Path sources = work.resolve("src");
      Path classes = Files.createDirectories(work.resolve("classes"));

      generate(job.get("feature_json"), sources);
      compile(sources, classes);
      TestExecutionSummary summary = execute(classes, entryPointClass(job.get("feature_json")));

      if (summary.getTestsFoundCount() == 0)
        return response(id, "infrastructure_error", output.toString(StandardCharsets.UTF_8),
            "no tests were discovered", started);
      return response(id, summary.getTotalFailureCount() > 0 ? "test_failure" : "test_success",
          output.toString(StandardCharsets.UTF_8), "", started);
    } catch (Exception cause) {
      System.err.println("job " + id + " failed: " + cause);
      return response(id, "infrastructure_error", output.toString(StandardCharsets.UTF_8),
          String.valueOf(cause.getMessage()), started);
    }
  }

  /** The generator derives the entry point class name from the IR file stem. */
  private static String entryPointClass(String irPath) {
    String stem = Path.of(irPath).getFileName().toString().replaceAll("\\.json$", "");
    StringBuilder name = new StringBuilder();
    for (String word : stem.split("[^A-Za-z0-9]+")) {
      if (word.isEmpty()) continue;
      name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
    }
    return GENERATED_PACKAGE + "." + name + "AcceptanceTest";
  }

  private void generate(String featureJson, Path into) throws Exception {
    Process process = new ProcessBuilder("bb", generator.toString(), featureJson, into.toString())
        .directory(root.toFile())
        .redirectErrorStream(true)
        .start();
    generatorProcess.set(process);
    String output;
    try {
      output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    } finally {
      generatorProcess.set(null);
    }
    if (process.waitFor() != 0)
      throw new IllegalStateException("entry point generation failed: " + output);
  }

  private void compile(Path sources, Path classes) throws Exception {
    List<String> files = new ArrayList<>();
    try (Stream<Path> tree = Files.walk(sources)) {
      tree.filter(it -> it.toString().endsWith(".java")).map(Path::toString).forEach(files::add);
    }
    if (files.isEmpty()) throw new IllegalStateException("no generated sources to compile");

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) throw new IllegalStateException("no system Java compiler; run on a JDK");

    List<String> arguments = new ArrayList<>(List.of(
        "-cp", System.getProperty("java.class.path"),
        "-d", classes.toString(),
        "-nowarn"
    ));
    arguments.addAll(files);

    StringWriter diagnostics = new StringWriter();
    if (compiler.run(null, null, null, arguments.toArray(String[]::new)) != 0
        || !Files.exists(classes))
      throw new IllegalStateException("generated entry point did not compile: " + diagnostics);
  }

  /**
   * Loads the freshly compiled entry point in its own class loader so one
   * mutation never sees the previous mutation's constants.
   */
  private TestExecutionSummary execute(Path classes, String className) throws Exception {
    ClassLoader previous = Thread.currentThread().getContextClassLoader();
    try (URLClassLoader loader = new URLClassLoader(
        new URL[]{classes.toUri().toURL()}, AcceptanceMutationRunner.class.getClassLoader())) {
      Thread.currentThread().setContextClassLoader(loader);

      LauncherDiscoveryRequest discovery = request().selectors(selectClass(loader.loadClass(className))).build();
      Launcher launcher = LauncherFactory.create();
      SummaryGeneratingListener listener = new SummaryGeneratingListener();
      launcher.execute(discovery, listener);
      return listener.getSummary();
    } finally {
      Thread.currentThread().setContextClassLoader(previous);
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

  private static void deleteTree(Path root) {
    if (root == null) return;
    try (Stream<Path> tree = Files.walk(root)) {
      tree.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (Exception ignored) {
          // A leftover temp file is not worth failing a mutation over.
        }
      });
    } catch (Exception ignored) {
      // Likewise.
    }
  }

  /**
   * Just enough JSON for the worker protocol: a flat object of string and
   * number values. Keeping it here avoids putting a JSON library on the test
   * classpath purely for the adapter.
   */
  static final class Json {
    private final String text;
    private int at;

    private Json(String text) {
      this.text = text;
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

    private String readValue() {
      char next = peek();
      if (next == '"') return readString();
      int start = at;
      while (at < text.length() && ",}".indexOf(text.charAt(at)) < 0) at++;
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
