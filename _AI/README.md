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
| know **what to work on next** | `NEXT.md` (live backlog) + the two priorities below |
| **finish Production V2** (owner priority 1) | `STATUS.md`, then `__01_PRODUCTION_TODO.md`, then `redesign/01_PRODUCTION.md` |
| **rewrite PositionFinder** (owner priority 2, blocks the above) | `POSITION-FINDER.md` |
| run or fix an **OpenBW game** | `PLAN-OPENBW.md`, `CHALLENGES/OpenBW.md` |
| work on **E2E testing** | `IDEA-E2E-TESTS.md` |
| run the bot on **Linux / Wine / IDE** | `LOCAL-STARCRAFT.md`, `CHALLENGES/GameExecution.md`, `CHALLENGES/BuildAndLogging.md` |
| know a **rule I must follow** | `CONVENTIONS.md` (normative) |
| understand **why the architecture is like this** | `REVIEW.md`, then `redesign/` |
| check **known defects** | `BUGS.md` |

---

## The two current priorities (owner's call, 2026-10-08)

1. **Finish Production V2.** `redesign/01_PRODUCTION.md` is the spec; M1-M5 are
   done, M6 (cutover) is partial. `STATUS.md` has the milestone state,
   `__01_PRODUCTION_TODO.md` the actionable checklist.
2. **Rewrite `PositionFinder`** — it is a blocker for Production V2 itself, not
   just for E2E: placement is how production puts a building on the map.
   `POSITION-FINDER.md` records the measured facts and the traps. **Do not patch
   it in the meantime.**

---

## Living documents (update as work proceeds)

- **`NEXT.md`** — the single source of truth for open work. Stable numbers, never
  renumbered; a closed item's line is **deleted** and the commit says
  `Closes #<n>` with the evidence (CONVENTIONS §7). Large by design: closed-item
  narratives stay until the commit that closes them is old enough to be the
  record.
- **`STATUS.md`** — Production V2 milestone status. Update at the end of every
  work cycle.
- **`BUGS.md`** — things that are wrong *today*, each with how it was measured.
  Not a TODO list.
- **`NOTES.md`** — operational learnings that do not fit anywhere else.

## Reference documents (change rarely)

- **`CONVENTIONS.md`** — normative rules. Numbered sections; other files cite
  them as "CONVENTIONS §N". If you are about to do something unusual, this file
  has an opinion.
- **`REVIEW.md`** — the architecture assessment and the canonical stage plan
  (§16). Long; read §16 for the plan, the rest as background.
- **`POSITION-FINDER.md`** — everything measured about the placement failure, for
  the rewrite. Short, read it in full.
- **`LOCAL-STARCRAFT.md`** — Wine/StarCraft setup and what OpenBW does and does
  not provide (e.g. the JBWEB JNI gap).
- **`PLAN-OPENBW.md`** — the OpenBW plan and its status; §9 has the attach fix.
- **`IDEA-E2E-TESTS.md`** — what E2E means here, and what blocks the mega-test.
- **`__CLEAN-UP.md`**, **`__01_PRODUCTION_TODO.md`** — the `__` prefix marks a
  working note that is not part of the reference set.

## `redesign/`

Specs for the two migration tracks, from the Stardust comparison:
`01_PRODUCTION.md` (higher priority), `02_COMBAT.md`, and
`STARDUST_VS_ATLANTIS.md` (the baseline comparison the tracks came from).

## `CHALLENGES/`

One file per system, each a **quick reference of what cost a research cycle** -
one or two lines per insight, no narrative (CONVENTIONS §11). Read the relevant
one before touching game/process code; every failure in them looked like a
different problem than it was.

`OpenBW.md`, `GameExecution.md`, `BuildAndLogging.md`, `Wine.md`,
`ChaosLauncher.md`, `Bwapi.md`, `Keyboard.md`.

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
