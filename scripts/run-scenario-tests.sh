#!/usr/bin/env bash
# Run the scenario tier: the stub-world rush scenarios plus the StarEngine tests.
#
# Why a script: these two packages used to live inside tests.acceptance, where they
# were mixed into a scope that has nothing to do with them, and after moving to
# tests.e2e / tests.starengine (2026-10-04) they had no command of their own - they
# would only ever run as part of `--select-package tests`. A scope without a command
# is a scope that silently stops running, which is how 44 failures once sat in the
# tree unnoticed (NOTES.md, "The acceptance package was never run").
#
# The scenarios are the slow ones - a 900-frame stub-world game takes ~35 s per
# test, so the tier is minutes, not seconds. That is the point of them: they are the
# only end-to-end signal Atlantis has until the OpenBW runner can host it
# (_AI/IDEA-E2E-TESTS.md, Stage 3).
#
# Usage (from anywhere):
#   bash scripts/run-scenario-tests.sh
#   bash scripts/run-scenario-tests.sh --select-class tests.e2e.FourPoolDefenseTest
#
# See DOCS/TESTING.md for the current baseline.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ "$#" -eq 0 ]; then
  set -- --select-package tests.e2e --select-package tests.starengine
fi

exec bash scripts/run-tests.sh "$@"