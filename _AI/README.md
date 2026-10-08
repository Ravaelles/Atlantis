# `_AI/` — index

Where the working knowledge of this project lives. If you are picking the work up
cold, read this file first; it says which document answers which question and
which of them are current.

Everything here is English (CONVENTIONS §1). "Owner" is the human; "model" is the
AI assistant.

---

## Start here, by task

| I want to… | Read |
|---|---|
| know **what to work on next** | `NEXT.md` (open backlog) |
| **finish Production V2** | `STATUS.md`, then `redesign/01_PRODUCTION.md` |
| work on **placement** | `redesign/03_PLACEMENT.md` (design + "NOT FINISHED" at the top), `POSITION-FINDER.md` (why it was rewritten) |
| run or fix an **OpenBW game** | `PLAN-OPENBW.md`, `CHALLENGES/OpenBW.md` |
| work on **E2E testing** | `IDEA-E2E-TESTS.md` (§3.5 = the next steps) |
| run the bot on **Linux / Wine / IDE** | `LOCAL-STARCRAFT.md`, `CHALLENGES/GameExecution.md`, `CHALLENGES/BuildAndLogging.md` |
| know a **rule I must follow** | `CONVENTIONS.md` (normative) |
| understand **why the architecture is like this** | `REVIEW.md`, then `redesign/` |
| check **known defects** | `BUGS.md` |

---

## The current priorities (owner's call, 2026-10-08)

1. **Finish Production V2** - spec in `redesign/01_PRODUCTION.md`, state in
   `STATUS.md`. M1-M5 done, M6 (cutover) partial; the live blocker is the plan
   re-planning instead of remembering what it already ordered.
2. **Placement** - the rewrite landed (`redesign/03_PLACEMENT.md`) and works in a
   live game; what remains is the cut-over, Terran/Zerg, and the list at the top of
   that file.
3. **OpenBW tests** - `IDEA-E2E-TESTS.md` §3.5 has the ordered next steps.

---

## Living documents (update as work proceeds)

- **`NEXT.md`** - the single source of truth for open work. Stable numbers, never
  renumbered; a closed item's line is **deleted** and the commit says
  `Closes #<n>` with the evidence (CONVENTIONS §7).
- **`STATUS.md`** - Production V2 status (the old `__01_PRODUCTION_TODO.md` was
  folded into it and deleted, 2026-10-08).
- **`BUGS.md`** - things that are wrong *today*, each with how it was measured.
  Not a TODO list.
- **`NOTES.md`** - operational learnings that do not fit anywhere else.

## Reference documents (change rarely)

- **`CONVENTIONS.md`** - normative rules. Numbered sections; other files cite
  them as "CONVENTIONS §N".
- **`REVIEW.md`** - the architecture assessment and the stage plan (§16). Long;
  read §16 for the plan, the rest as background.
- **`POSITION-FINDER.md`** - short: why the old finder was rewritten, and the
  traps the rewrite had to avoid.
- **`LOCAL-STARCRAFT.md`** - Wine/StarCraft setup and what OpenBW does and does not
  provide (e.g. the JBWEB JNI gap).
- **`PLAN-OPENBW.md`** - how to run OpenBW, and what it cannot do.
- **`IDEA-E2E-TESTS.md`** - what E2E means here, and the next steps.
- **`__CLEAN-UP.md`** - a working note (the `__` prefix marks it as not part of
  the reference set).

## `redesign/`

Specs for the two migration tracks, from the Stardust comparison:
`01_PRODUCTION.md` (higher priority), `02_COMBAT.md`, and
`STARDUST_VS_ATLANTIS.md` (the baseline comparison the tracks came from).

## `CHALLENGES/`

One file per system, each a **quick reference of what cost a research cycle** -
one or two lines per insight, no narrative (CONVENTIONS §11). Read the relevant
one before touching game/process code; every failure in them looked like a
different problem than it was.

`OpenBW.md`, `GameExecution.md`, `BuildAndLogging.md`, `StationaryEnemy.md`,
`Wine.md`, `ChaosLauncher.md`, `Bwapi.md`, `Keyboard.md`.

## Other directories

- **`e2e/`** — verdict tables from real scbw games (`_AI/e2e/README.md`).
- **`architecture/archunit-store/`** — the frozen ArchUnit baseline. Versioned on
  purpose; it must shrink or stay unchanged, never grow (CONVENTIONS §5).
- **`work-orders/`** — one-off investigation briefs with their own README.
- **`benchmarks/`** — raw measurement dumps.

---

## Conventions for this directory

- One file per topic, and a file is named after the **system**, not the bug.
- A long narrative belongs in the commit message; the file keeps the fact.
- If a document's advice is superseded, say so **at the top** of it, as
  `IDEA-E2E-TESTS.md` and `PLAN-OPENBW.md` do - do not leave a reader to
  discover it halfway down.
