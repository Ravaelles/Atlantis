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
| work on **placement** | `redesign/03_PLACEMENT.md` (design + "NOT FINISHED" at the top), `PLACEMENT-CUTOVER-PLAN.md` (the V2 cut-over plan), `POSITION-FINDER.md` (why it was rewritten) |
| run or fix an **OpenBW game** | `PLAN-OPENBW.md` (limits + assertions), `CHALLENGES/OpenBW.md` (history) |
| work on **E2E testing** | `IDEA-E2E-TESTS.md` (§3.5 = the next steps) |
| run the bot on **Linux / Wine / IDE** | `LOCAL-STARCRAFT.md`, `CHALLENGES/GameExecution.md`, `CHALLENGES/BuildAndLogging.md` |
| know a **rule I must follow** | `CONVENTIONS.md` (normative; §13 = the 120 s limit) |
| understand **why the architecture is like this** | `REVIEW.md` §16, then `redesign/` and `DOCS/adr/` |
| check **known defects** | `BUGS.md` |

---

## The current priorities (owner's call, 2026-10-09)

1. **OpenBW runs must be bounded and useful** - CONVENTIONS §13/§17 cap every
   command at 120 s and every simulation at 20 game minutes; the runner can now
   assert scenario facts (`PLAN-OPENBW.md`).
2. **Placement is the live blocker** - no Pylon lands on TauCross, so the bot
   produces nothing and every survival scenario fails. Fixing it needs the
   cut-over decision in `redesign/03_PLACEMENT.md`.
3. **Finish Production V2** - spec in `redesign/01_PRODUCTION.md`, state in
   `STATUS.md`. M1-M5 done, M6 (cutover) partial.
4. **E2E testing** - `IDEA-E2E-TESTS.md` §3.5 has the ordered next steps.

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
- **`PLAN-OPENBW.md`** - how to run OpenBW: the two hard limits, the scenario
  assertions, and what OpenBW cannot do.
- **`IDEA-E2E-TESTS.md`** - what E2E means here, what exists today, and the next
  steps (§3.5).
- **`DOCS/adr/`** - accepted decisions (ADR 0001 modular monolith, 0003 DDD
  strategic-only, 0006 combat eval scale, 0007 race strategy seams). Read these
  before proposing a new architectural direction.

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
