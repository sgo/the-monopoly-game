package the.monopoly.game.specs.acceptance;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a module's declared Maven dependencies, resolving each one's
 * effective version against the repository root's {@code dependencyManagement}
 * when the module itself does not pin a version.
 */
final class PomInspector {
  private PomInspector() {
  }

  /** {@code "groupId:artifactId" -> version}, or a {@code null} version when none can be resolved. */
  static Map<String, String> declaredDependencies(String moduleDirectory) {
    Path root = repoRoot(moduleDirectory);
    Map<String, String> managedVersions = dependencyManagementVersions(root.resolve("pom.xml"));

    Map<String, String> declared = new LinkedHashMap<>();
    for (Map.Entry<String, String> dependency
        : rawDependencies(root.resolve(moduleDirectory).resolve("pom.xml")).entrySet()) {
      String coordinate = dependency.getKey();
      String pinnedVersion = dependency.getValue();
      declared.put(coordinate, pinnedVersion != null ? pinnedVersion : managedVersions.get(coordinate));
    }
    return declared;
  }

  static boolean declaresExecutableJar(String moduleDirectory, String mainClass) {
    Path pom = repoRoot(moduleDirectory).resolve(moduleDirectory).resolve("pom.xml");
    try {
      String text = Files.readString(pom);
      return text.contains("maven-shade-plugin") && text.contains(mainClass);
    } catch (Exception cause) {
      throw new AssertionError("Could not read " + pom, cause);
    }
  }

  /** Walks up from the current working directory to find the checkout containing {@code moduleDirectory}. */
  private static Path repoRoot(String moduleDirectory) {
    Path directory = Path.of("").toAbsolutePath();
    while (directory != null) {
      if (Files.isDirectory(directory.resolve(moduleDirectory)))
        return directory;
      directory = directory.getParent();
    }
    throw new AssertionError(
        "Could not find module directory \"" + moduleDirectory + "\" above "
            + Path.of("").toAbsolutePath());
  }

  private static Map<String, String> rawDependencies(Path pomFile) {
    return dependencyMap(child(parse(pomFile).getDocumentElement(), "dependencies"));
  }

  private static Map<String, String> dependencyManagementVersions(Path pomFile) {
    Element management = child(parse(pomFile).getDocumentElement(), "dependencyManagement");
    return dependencyMap(management == null ? null : child(management, "dependencies"));
  }

  private static Map<String, String> dependencyMap(Element dependencies) {
    Map<String, String> result = new LinkedHashMap<>();
    if (dependencies == null) return result;
    for (Element dependency : children(dependencies, "dependency")) {
      String groupId = text(child(dependency, "groupId"));
      String artifactId = text(child(dependency, "artifactId"));
      result.put(groupId + ":" + artifactId, text(child(dependency, "version")));
    }
    return result;
  }

  private static Document parse(Path pomFile) {
    if (!Files.isRegularFile(pomFile))
      throw new AssertionError("No pom.xml found at " + pomFile);
    try {
      var factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      return factory.newDocumentBuilder().parse(pomFile.toFile());
    } catch (Exception cause) {
      throw new AssertionError("Could not parse " + pomFile, cause);
    }
  }

  private static Element child(Element parent, String tag) {
    List<Element> found = children(parent, tag);
    return found.isEmpty() ? null : found.get(0);
  }

  private static List<Element> children(Element parent, String tag) {
    List<Element> result = new ArrayList<>();
    NodeList nodes = parent.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      if (nodes.item(i) instanceof Element element && element.getTagName().equals(tag))
        result.add(element);
    }
    return result;
  }

  private static String text(Element element) {
    return element == null ? null : element.getTextContent().trim();
  }
}
