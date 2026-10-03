# Work orders — exact task plans

One file per backlog item (or part of one) that a weaker model can execute
without inventing anything. A work order is a contract: follow it exactly,
and stop where it says to stop.

## How to work one order (mandatory)

1. Read the whole order first. If any step contradicts what you see in the
   tree, **stop and report** - do not improvise around it.
2. Record your baseline before changing anything: full suite
   (`bash scripts/run-tests.sh --select-package tests`) and, when the order
   touches production or moves classes, ArchUnit
   (`bash scripts/run-architecture-tests.sh`). Write the numbers down; the
   order tells you what "done" compares against.
3. One order at a time. One logical change per commit
   (`_AI/CONVENTIONS.md` §6).
4. Before committing, run `git status` and `git diff --stat`. **Commit only
   files you edited yourself.** If the tree contains changes you did not
   make (another session works here too), leave them alone and say so in
   your report - never sweep someone else's work into your commit, and never
   "fix" it as a drive-by.
5. Never weaken a test assertion to make it pass (`DOCS/TESTING.md` rule).
   Never type a game number from memory (`_AI/CONVENTIONS.md` §9). A red
   test after your change means your change is wrong until proven otherwise:
   revert first, think second.

## Index

- `WO-13-mechanical-archunit-sweep.md` — take violations off the two
  mechanical ArchUnit rules, smallest first (NEXT.md #13, part).
- `WO-B1-eval-evidence-sweep.md` — measure the evaluator across a fixed
  scenario matrix for the ADR, changing nothing (evidence for BUGS.md B-1 /
  `DOCS/adr/0006-combat-eval-scale.md`).
- `WO-F-neutral-cache-cleanup.md` — the two behaviour-neutral cache cleanups
  that need no game run (NEXT.md Stage F intro, not #5 itself).
