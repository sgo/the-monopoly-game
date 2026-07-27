#!/usr/bin/env bash
# Normal acceptance run: parse each feature into JSON IR, generate the entry
# points, then run the generated tests.
#
# Only the features listed below are on the pipeline. en/monopoly.feature is
# the one held back: it parses, but it specifies a whole played-out game, and
# the turn loop behind it does not exist yet. See logbook.md.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SPECS="$ROOT/the-monopoly-game-specs/the-monopoly-game-specs-core"
FEATURES="$SPECS/src/test/resources"
IR_DIR="$ROOT/build/acceptance/ir"
GENERATED="$SPECS/target/generated-test-sources/acceptance"
APS="${APS_HOME:-$ROOT/tmp/aps}"

FEATURE_FILES=(
  "en/components/streets.feature"
  "en/components/stations.feature"
  "en/components/utilities.feature"
  "en/components/tax.feature"
  "en/components/dice.feature"
  "en/rules/official.feature"
)

if [[ ! -f "$APS/bb.edn" ]]; then
  echo "APS checkout not found at $APS. Set APS_HOME or clone" >&2
  echo "  https://github.com/unclebob/Acceptance-Pipeline-Specification" >&2
  exit 1
fi

rm -rf "$IR_DIR" "$GENERATED"
mkdir -p "$IR_DIR"

for feature in "${FEATURE_FILES[@]}"; do
  ir="$IR_DIR/$(echo "$feature" | tr '/' '-' | sed 's/\.feature$//').json"
  echo "parsing $feature"
  (cd "$APS" && bb gherkin-parser "$FEATURES/$feature" "$ir")
  echo "generating entry point for $feature"
  "$ROOT/acceptance/acceptance-entrypoint-generator.bb" --feature-path "$feature" "$ir" "$GENERATED"
done

echo "running generated acceptance tests"
cd "$ROOT"

# The compiled entry points must not outlive this run. They land in
# target/test-classes, where a later plain `mvn test` would find and run them,
# making the size of the default suite depend on whether this script had been
# run since the last clean.
compiled="$SPECS/target/test-classes/the/monopoly/game/specs/acceptance/generated"
trap 'rm -rf "$compiled"' EXIT

status=0
mvn -B -Pacceptance -pl the-monopoly-game-specs/the-monopoly-game-specs-core -am \
  -Dtest='*AcceptanceTest' -Dsurefire.failIfNoSpecifiedTests=false test || status=$?

exit $status
