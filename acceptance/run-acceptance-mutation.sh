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

LEVEL="soft"
if [[ "${1:-}" == "--level" ]]; then
  LEVEL="$2"
  shift 2
fi

# The pipeline features live in one file both scripts read; see
# pipeline-features.txt.
PIPELINE_FEATURES=()
while IFS= read -r feature_line; do
  PIPELINE_FEATURES+=("$feature_line")
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
(cd "$ROOT" && mvn -B -q -pl the-monopoly-game-specs/the-monopoly-game-specs-core -am -DskipTests test-compile)
(cd "$ROOT" && mvn -B -q -pl the-monopoly-game-specs/the-monopoly-game-specs-core \
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
  slug="$(echo "$feature" | tr '/' '-' | sed 's/\.feature$//')"
  work_dir="$WORK/$slug"
  generated="$work_dir/generated"

  rm -rf "$work_dir"
  mkdir -p "$generated"

  # The mutator reads the base entry point metadata from --generated-dir, so
  # generate it there from the unmutated IR first.
  ir="$work_dir/base.json"
  (cd "$APS" && bb gherkin-parser "$FEATURES/$feature" "$ir")
  "$ROOT/acceptance/acceptance-entrypoint-generator.bb" --feature-path "$feature" "$ir" "$generated"

  echo "== mutating $feature (level $LEVEL) =="
  (cd "$APS" && bb gherkin-mutator \
    --feature "$FEATURES/$feature" \
    --work-dir "$work_dir" \
    --generated-dir "$generated" \
    --runner-worker "$WORKER" \
    --workers 4 \
    --status-interval 30s \
    --level "$LEVEL") || status=$?
done

exit $status
