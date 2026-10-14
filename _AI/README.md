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
| know **what to work on next** | `NEXT.md` (open backlog; the top item, next priority and current plan are named at the top of the file) |
| **finish Production V2** | `NEXT.md` #45, then `redesign/01_PRODUCTION.md` |
| work on **placement** | `PLAN-STAGE4-COMMAND-PATH.md` (what to do next - the build command is refused inside the library), `PLAN-REGION-DETECTION.md` (own reachability; stages 1-3 done), `NEXT.md` #49, `PLACEMENT-CUTOVER-PLAN.md`, `redesign/03_PLACEMENT.md`, `POSITION-FINDER.md`, `BUILDING-BLOCKERS.md` (certain vs refuted vs open, per site) |
| run or fix an **OpenBW game** | `PLAN-OPENBW.md` (limits + assertions), `CHALLENGES/OpenBW.md` (history) |
| work on **E2E testing** | `IDEA-E2E-TESTS.md` (§3.5 = the next steps) |
| run the bot on **Linux / Wine / IDE** | `LOCAL-STARCRAFT.md`, `CHALLENGES/GameExecution.md`, `CHALLENGES/BuildAndLogging.md` |
| know a **rule I must follow** | `CONVENTIONS.md` (normative; §13 = the 120 s limit — read §3a before continuing a long investigation) |
| understand a **resolved defect's cause** | `CASE-STUDIES.md`, then `CHALLENGES/` for the system-level traps |
| understand **why the architecture is like this** | `REVIEW.md` §16, then `redesign/` and `DOCS/adr/` |
| check **known defects** | `BUGS.md` (things still wrong today, not a history) |

---

## Living documents (update as work proceeds)

- **`NEXT.md`** - the single source of truth for open work. Stable numbers, never
  renumbered; a closed item's line is **deleted** and the commit says
  `Closes #<n>` with the evidence (CONVENTIONS §7). It is meant to be **scannable**:
  long post-mortems of resolved defects live in `CASE-STUDIES.md`, not here.
- **`DECISIONS.md`** - choices only the owner can make, one per entry. Approve
  by saying so; the entry is then executed and deleted.
- **`CASE-STUDIES.md`** - the long narratives of resolved defects (mechanism,
  withdrawn readings, traps), moved out of `NEXT.md` so the backlog stays readable.
  Read it when a symptom looks familiar and the git log is not enough.
- **`STATUS.md`** - started-but-unfinished work that must survive a context
  change (slim by design since 2026-10-14; history before that is in git).
- **`BUGS.md`** - things that are wrong *today*, each with how it was measured.
  Not a TODO list; a fixed defect is **deleted**, so absence is not proof it never
  existed (the git log is the archive).
- **`NOTES.md`** - operational learnings that do not fit anywhere else.
- **`PLACEMENT-CUTOVER-PLAN.md`** - the cut-over plan behind NEXT #47/#49; it
  changes with the blocker, so re-read it rather than trusting a quotation.

`STATUS.md` is slim on purpose: one section per started-but-unfinished
piece of work. Session narratives live in git history, not in the file.

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
- **`CHERRYVIS-DOCKER.md`** - the Docker viewer setup we use
  (Ravaelles/cherryvis-docker): build/run/reload, the `.diff` patch pattern
  for viewer fixes, and the links.
- **`PLAN-REGION-DETECTION.md`** - why the first Pylon is refused (engine regions),
  what Stardust/PurpleWave do instead, and the staged plan for our own reachability.
  Stages 1-3 are done; stage 4 moved to its own file.
- **`PLAN-STAGE4-COMMAND-PATH.md`** - the remaining blocker: the library's own
  `canBuild` refuses the order, whatever our reachability says. The four options with
  their costs; the issuing path is now measured (the legacy builder chain, not V2).
- **`IDEA-TERRAIN-CACHE.md`** - whether to cache the per-map region analysis. Measured
  (~0.7 ms once per game) and recommended **not yet**, with the trigger to revisit.
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
