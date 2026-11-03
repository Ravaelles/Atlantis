# NEW: requirement template (read this before filing anything)

Every new piece of work starts as a task file, never as a NEXT line
alone. Copy this skeleton into `_AI/NEXT/<WORKSTREAM>.md`, fill every
section, and add exactly one delegation line to NEXT.md Active steps.
No section may stay blank: a blank section means the requirement is
not ready, not that it is obvious.

```markdown
# <WORKSTREAM>: one-line mission statement

Status: TODO | DOING | DONE (evidence: <test run / game id / gate>)
Owner decision ref: <chat date or "standing order", or BLOCKED ON: <what>>

## Context (3 sentences max)
Why this exists. What hurts today, measured (not "feels slow").

## Goal (one paragraph)
The end state, observable from outside. No mechanism here.

## Non-goals (explicit list)
What this slice deliberately does NOT change. Every "not now"
needs a home: a later slice name or a NEXT item number.

## DELETE (exact paths)
Files to remove or move to `src-old/`, one per line with the reason
in 5 words. A path without a reason is not a deletion, it is hope.

## MODIFY (exact paths + what changes in each)
One bullet per file: what changes and why. No file may appear here
without a named test or twin that covers the change.

## ADD (exact paths)
New files with one-line contracts. New packages need a
CONVENTIONS §5 check (dependencies point inward) written down.

## Verification (commands, not adjectives)
- Red-first pin: <test class + what fails before the fix>
- Fast gate: `bash scripts/run-tests.sh` green (attach EXIT + counts)
- Slow tiers: <which scope, or "N/A - no sim/map surface">
- Live proof: <game id + what it must show, or "deferred: <why>">

## Exit criteria (checkable, not vibes)
- [ ] `grep -rn <forbidden-pattern> src/...` returns nothing
- [ ] <test class> green in the gate
- [ ] <game id> shows <observable behavior>
When every box has its evidence, delete the NEXT delegation line
(§7). The task file stays as the record until its slice lands,
then it is deleted with the closing commit.

## Ratchet rule (mandatory for migration slices)
The pin must fail if the old path is still referenced. One ratchet
per seam/door, never a battery per case. When the migration closes,
delete the ratchet test or move it to `src/tests-aux/` - ratchets
never become permanent suite weight.

## Siting tests run on the TauCross snapshot (mandatory for placement work)
`src/tests/resources/taucross-snapshot.json` via the map-snapshot
harness, never flat stubs. Flat terrain is allowed only for rules
with no map geometry in them (pure predicates); the test's javadoc
must name why the stub is sufficient.
```

## Worked example (shape, not content)

```markdown
# Pylon edge rule: no served tile within 4 tiles of any map edge

Status: TODO
Owner decision ref: owner 2026-11-04 ("4 kratki wszędzie")

## Context
Live 2026-11-03 vs Steamhammer fielded edge pylons while the legacy
path enforces 7 tiles: the catalogue/V2 path only checks 1 tile.
Measured: <game id + tile>.

## Goal
One edge rule (4 tiles), enforced by the single verdict, on every path.

## Non-goals
- No ranker retuning in this slice (later slice: slot preservation).
- No UMS reachability change (separate item).

## DELETE
(none - rule change only)

## MODIFY
- `src/atlantis/placement/core/SitingVerdict.java`: add the 4-tile check.
- `src/atlantis/production/constructions/position/protoss/ProtossTooCloseToMapBoundaries.java`: delete (superseded; callers move to the verdict).
- `src/tests/unit/ProtossTooCloseToMapBoundariesTest.java`: update pinned numbers 7 -> 4.

## ADD
- `src/tests/unit/SitingEdgeRuleTest.java`: snapshot-map pins per building type (served inside, refused outside).

## Verification
- Red-first: new pin fails on the old per-path rules.
- Fast gate green; placement domain green.
- Live proof: deferred (halted) - note the game id when played.

## Exit criteria
- [ ] `grep -rn "TooCloseToMapBoundaries" src/atlantis` returns nothing
- [ ] edge pins green in the gate
```

Rules for this template: English only (§1). Numbers never reused
(§7). A task file without an owner decision ref is a draft, not a
work order - drafts do not get NEXT lines.
