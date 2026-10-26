#!/usr/bin/env bash
# Run the acceptance scope: world, squad and commander behaviour.
#
# `scripts/run-tests.sh` defaults to `--select-package tests.unit`, which is the
# fast inner loop. The acceptance package is ~115 tests that actually build a
# world and step frames - the part of the suite that catches harness and
# integration defects. It used to run only when somebody remembered to pass
# `--select-package tests`, and 44 failures sat in the tree unnoticed for that
# reason. Hence a script, not a flag to know about.
#
# Usage (from anywhere):
#   bash scripts/run-acceptance-tests.sh
#   bash scripts/run-acceptance-tests.sh --allow-slow
#   bash scripts/run-acceptance-tests.sh --select-class tests.acceptance.Queue1Test
#
# Pass --everything to include tests.unit as well.
#
# Any flag must NOT cost us the selection: this script used to add
# `--select-package tests.acceptance` only when called with zero arguments, so
# `--allow-slow` (the flag you need for this scope - run-tests.sh refuses slow
# scopes otherwise) REPLACED it and the wrapper silently ran the unit scope
# instead. Measured 2026-10-19 (see _AI/NOTES.md) and again 2026-10-26: a bare
# `--allow-slow` reported the unit tier green while acceptance sat red - the
# exact failure this script's header exists to prevent. The default selection
# now survives any flag; only an explicit selector from the caller overrides it.
#
# See DOCS/TESTING.md for the current known-failing baseline.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ "${1:-}" = "--everything" ]; then
  shift
  exec bash scripts/run-tests.sh --select-package tests "$@"
fi

case " $* " in
  *" --select-package "*|*" --select-class "*|*" --select-method "*) ;;
  *) set -- --select-package tests.acceptance "$@" ;;
esac

exec bash scripts/run-tests.sh "$@"