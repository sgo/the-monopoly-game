#!/usr/bin/env bash
# Gherkin acceptance mutation: mutate the example values of each pipeline
# feature and check the acceptance tests notice.
#
# Usage: run-acceptance-mutation.sh [--level soft|hard|full] [feature ...]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SPECS="$ROOT/the-monopoly-game-specs/the-monopoly-game-specs-core"
FEATURES="$SPECS/src/test/resources"
WORK="$ROOT/build/acceptance-mutation"
APS="${APS_HOME:-$ROOT/tmp/aps}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-$ROOT/tmp/m2}"

LEVEL="soft"
if [[ "${1:-}" == "--level" ]]; then
  LEVEL="$2"
  shift 2
fi

# The pipeline features live in one file both scripts read; see
# pipeline-features.txt. A module-qualified entry is resolved from that
# module's test resources.
PIPELINE_FEATURES=()
while IFS= read -r feature_line; do
  # Features marked !no-mutation are held back; see pipeline-features.txt.
  case "$feature_line" in *" !no-mutation") continue ;; esac
  PIPELINE_FEATURES+=("${feature_line%% !*}")
done < <(grep -vE '^[[:space:]]*(#|$)' "$ROOT/acceptance/pipeline-features.txt")
FEATURE_FILES=("$@")
if [[ ${#FEATURE_FILES[@]} -eq 0 ]]; then
  FEATURE_FILES=("${PIPELINE_FEATURES[@]}")
fi

if [[ ! -f "$APS/bb.edn" ]]; then
  echo "APS checkout not found at $APS. Set APS_HOME or clone" >&2
  echo "  https://github.com/unclebob/Acceptance-Pipeline-Specification" >&2
  exit 1
fi

mkdir -p "$WORK"

# The runner adapter hosts JUnit itself, so it needs the module's test
# classpath and its compiled classes. Build both once, not once per mutation.
echo "preparing test classpath"
(cd "$ROOT" && mvn -B -q -Dmaven.repo.local="$MAVEN_REPO_LOCAL" \
  -pl the-monopoly-game-specs/the-monopoly-game-specs-core -am -DskipTests install)
(cd "$ROOT" && mvn -B -q -Dmaven.repo.local="$MAVEN_REPO_LOCAL" \
  -pl the-monopoly-game-specs/the-monopoly-game-specs-core \
  dependency:build-classpath -Dmdep.outputFile="$WORK/classpath.txt")

CP="$SPECS/target/test-classes:$SPECS/target/classes:$(cat "$WORK/classpath.txt")"

# --runner-worker takes a single command, so wrap the invocation.
WORKER="$WORK/runner-worker.sh"
cat > "$WORKER" <<EOF
#!/usr/bin/env bash
exec java -cp "$CP" "$ROOT/acceptance/mutation-runner/AcceptanceMutationRunner.java" "$ROOT"
EOF
chmod +x "$WORKER"

status=0
for feature in "${FEATURE_FILES[@]}"; do
  module="${feature%%:*}"
  feature_path="${feature#*:}"
  if [[ "$module" == "$feature" ]]; then
    module_root="$SPECS"
  elif [[ "$module" == specs-* ]]; then
    module_root="$ROOT/the-monopoly-game-specs/the-monopoly-game-$module"
  else
    module_root="$ROOT/the-monopoly-game-$module"
  fi
  feature_file="$module_root/src/test/resources/$feature_path"
  slug="$(echo "$feature" | tr '/' '-' | sed 's/\.feature$//')"
  work_dir="$WORK/$slug"
  generated="$work_dir/generated"

  rm -rf "$work_dir"
  mkdir -p "$generated"

  # The mutator reads the base entry point metadata from --generated-dir, so
  # generate it there from the unmutated IR first.
  ir="$work_dir/base.json"
  (cd "$APS" && bb gherkin-parser "$feature_file" "$ir")
  "$ROOT/acceptance/acceptance-entrypoint-generator.bb" --feature-path "$feature" "$ir" "$generated"

  echo "== mutating $feature (level $LEVEL) =="
  workers=4
  # Packaged-CLI scenarios invoke Maven against the shared target directory;
  # parallel mutations race while replacing the shaded jar and become runner
  # errors instead of meaningful test outcomes.
  if [[ "$feature" == "specs-cli:en/cli-packaged-jar.feature" ]]; then
    workers=1
  fi
  (cd "$APS" && bb gherkin-mutator \
    --feature "$feature_file" \
    --work-dir "$work_dir" \
    --generated-dir "$generated" \
    --runner-worker "$WORKER" \
    --workers "$workers" \
    --status-interval 30s \
    --level "$LEVEL") || status=$?
done

exit $status
