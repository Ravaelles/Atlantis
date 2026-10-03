# WO-13 — mechanical ArchUnit sweep (NEXT.md #13, part)

## Goal

Remove **one** violation from a mechanical rule per commit, until either rule
is empty: `util -> units/game/map/production/information/combat/debug` (store:
`_AI/architecture/archunit-store/2284295e-d670-4f71-9107-34a04f969d2f`) or
`information -> combat/production` (store:
`_AI/architecture/archunit-store/d7e6e1bd-ded5-4c0f-aac7-12cd4e110e1e`).

## Non-goals (do not touch)

- The structural rules: `core -> ...` (267, store `7d867ffa-...`) and
  `architecture -> ...` (24, store `cd983284-...`). Those need Stage E/H
  design work, not this order. The rule-to-file mapping lives in
  `_AI/architecture/archunit-store/stored.rules`.
- No behaviour changes. If removing an edge changes what the bot does, stop
  (see Stop rules).

## Prerequisites

- Read `_AI/NEXT.md` #13 (the two traps) and `_AI/NOTES.md` "ArchUnit store
  mechanics" (re-freeze procedure, stale `.class` files, constructor refs).
- Baseline: full suite + ArchUnit numbers, written down.

## Steps (one violation per cycle)

1. Open the store file for the rule you are working. Take the **first**
   entry (top of file) - no judgment needed, that is the point.
2. Find the importing file and method named in the entry. Classify the edge:
   - (a) **Dead code**: the importing method has no callers (grep the method
     name under `src/`). Delete the method (or the import if nothing else
     uses it).
   - (b) **Wrong-layer helper**: a small pure helper in the upper package
     used by exactly one lower-layer-adjacent caller. Move the method to the
     caller's class (or inline it there) and delete the original.
   - (c) **Anything else** (framework edge, behaviour-carrying logic, more
     than one caller, you are unsure): STOP this entry, take the next one,
     and note the skip reason in your commit message.
3. Apply the edit.
4. If you moved, renamed or deleted a class file: `rm -rf out` first (javac
   leaves stale `.class` files behind and ArchUnit reads both - phantom
   violations). Then `bash scripts/run-architecture-tests.sh`.
5. Read the result:
   - The rule must report **fewer** violations than your baseline, and
     `git diff` of the store file must show **more deleted lines than added,
     ideally zero added**. Added lines mean you *moved* the violation into
     another spelling (the paid-for trap) - revert the edit, take the next
     entry, note it.
   - ArchUnit auto-prunes stale entries on every run: review the store diff
     line by line. A re-freeze that absorbs a merely reworded violation is a
     failure - revert.
6. `bash scripts/run-tests.sh --select-package tests` - must be green with
   the same counts as baseline (a behaviour change hiding in a "move" shows
   up here).
7. Commit: one violation per commit. Message names the edge, where it went
   (deleted/moved to X), and the suite + ArchUnit results.

## Verification

- `bash scripts/run-architecture-tests.sh` → 7/7, and the store diff of the
  commit deletes more than it adds (zero additions).
- `bash scripts/run-tests.sh --select-package tests` → green, counts match
  baseline.
- `_AI/NEXT.md` #13 counts updated in the commit (subtract what you removed).

## Stop rules

- The entry needs game knowledge to classify → skip it, next entry.
- The entry is in a structural rule file → out of scope, stop the order.
- ArchUnit counts do not drop, or the store diff only rewords → revert, stop
  and report (do not re-freeze a new edge to force green).
- A test fails → your change is wrong until proven otherwise: revert first
  (never weaken the assertion).
