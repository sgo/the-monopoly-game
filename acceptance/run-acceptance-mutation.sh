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

FEATURE_FILES=("$@")
if [[ ${#FEATURE_FILES[@]} -eq 0 ]]; then
  FEATURE_FILES=(
    "en/components/streets.feature"
    "en/components/stations.feature"
    "en/components/utilities.feature"
    "en/components/tax.feature"
  )
fi

if [[ ! -f "$APS/bb.edn" ]]; then
  echo "APS checkout not found at $APS. Set APS_HOME or clone" >&2
  echo "  https://github.com/unclebob/Acceptance-Pipeline-Specification" >&2
  exit 1
fi

# Warm the local repository so the offline per-mutation test runs resolve.
(cd "$ROOT" && mvn -B -q -pl the-monopoly-game-specs/the-monopoly-game-specs-core -am -DskipTests install >/dev/null)

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
    --runner-worker "$ROOT/acceptance/acceptance-mutation-runner.bb" \
    --workers 1 \
    --status-interval 30s \
    --level "$LEVEL") || status=$?
done

exit $status
