#!/usr/bin/env bash
# Send a handoff to another agent.
#
#   ./swarmtools/notify-agent.sh <role>[,<role>...] --file <message-file>
#                                [--priority NN] [--type git_handoff|note]
#                                [--dry-run]
#
# The message file is the handoff message as the constitution requires it to be
# written: an opening line, then the sender role, specifier handoff name, branch
# name and commit hash. The task name and commit are read back out of it, so the
# message and what is actually delivered cannot drift apart.
#
# This is tracked in the repository rather than generated at startup. Every
# checkout and linked worktree gets it from git, which is what makes it present
# in all of them, and it resolves its own paths so the same file works anywhere.
set -euo pipefail

die() {
  echo "notify-agent: $*" >&2
  exit 1
}

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HANDOFF="$ROOT/swarmforge/scripts/swarm_handoff.sh"

[[ $# -ge 1 ]] || die "usage: notify-agent.sh <role>[,<role>...] --file <message-file> [--priority NN] [--type TYPE]"

TARGET="$1"
shift

FILE=""
PRIORITY="50"
TYPE="git_handoff"
DRY_RUN=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --file) FILE="${2:-}"; shift 2 ;;
    --priority) PRIORITY="${2:-}"; shift 2 ;;
    --type) TYPE="${2:-}"; shift 2 ;;
    --dry-run) DRY_RUN=1; shift ;;
    *) die "unknown argument: $1" ;;
  esac
done

[[ -n "$FILE" ]] || die "--file is required"
[[ -f "$FILE" ]] || die "no such message file: $FILE"
[[ -x "$HANDOFF" ]] || die "swarm_handoff.sh not found at $HANDOFF; run ./swarm to install the scripts"
[[ "$PRIORITY" =~ ^[0-9][0-9]$ ]] || die "priority must be two digits, got: $PRIORITY"
[[ -n "${SWARMFORGE_ROLE:-}" ]] || die "SWARMFORGE_ROLE is not set"

field() {
  sed -n "s/^$1:[[:space:]]*//p" "$FILE" | head -1
}

DRAFT="$(mktemp)"
trap 'rm -f "$DRAFT"' EXIT

if [[ "$TYPE" == "note" ]]; then
  MESSAGE="$(field 'message')"
  [[ -n "$MESSAGE" ]] || die "a note needs a 'message:' line in $FILE"
  printf 'type: note\nto: %s\npriority: %s\nmessage: %s\n' "$TARGET" "$PRIORITY" "$MESSAGE" > "$DRAFT"
else
  TASK="$(field 'specifier handoff name')"
  COMMIT="$(field 'commit hash')"
  [[ -n "$TASK" ]] || die "no 'specifier handoff name:' line in $FILE"
  [[ -n "$COMMIT" ]] || die "no 'commit hash:' line in $FILE"

  # Fail here rather than let a stale or mistyped hash reach another agent.
  git -C "$ROOT" cat-file -e "${COMMIT}^{commit}" 2>/dev/null \
    || die "commit $COMMIT does not exist in $ROOT"

  printf 'type: git_handoff\nto: %s\npriority: %s\ntask: %s\ncommit: %s\n' \
    "$TARGET" "$PRIORITY" "$TASK" "$COMMIT" > "$DRAFT"
fi

if [[ -n "$DRY_RUN" ]]; then
  echo "would send:"
  cat "$DRAFT"
  exit 0
fi

exec "$HANDOFF" "$DRAFT"
