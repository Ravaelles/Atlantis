# __PLAN-DECISIONS: every production act is an audited decision (owner 2026-10-17)

> DELETE THIS FILE WHEN DONE. Git history is the archive.

## Idea (owner's, shaped)

Every production act - train, build, research - is an `ADecision` with a
reason, emitted by an `AQuestion`, executed by one dispatcher, logged to
`DECISIONS.log`. Modules take anything as input, output only decisions.
The opening is a memorized script (explicit worker lines included);
dynamic pumps run only after the build order releases the game. Tests pin
decisions (state → decision), never the engine.

## Core types (`atlantis.decisions.board` - generic machinery, no game imports)

- [x] 0. Plan + value types. `ADecision` (immutable: kind, subject,
  reason, source BO|DYNAMIC, frame, supply, bank, children for HTN
  nesting). `AQuestion` (interface: `ask(BoardState) → ADecision|null`;
  null = refusal, nothing happens). Engine-free by construction
  (strings + ints) so tests pin them in milliseconds.
- [x] 1. `DecisionLog` → `bwapi-data/write/DECISIONS.log` (JSON lines,
  one row per ISSUED decision; refusals stay with the existing
  WORKER_GATE/ORDER_NOT_READY twins). Flushed on issuance (cheap -
  decisions are rare) + `OnGameEnd`. Both runners collect it like
  BUILDINGS.json/EVENTS.jsonl.
- [x] 2. `DecisionDispatcher`: `dispatch(ADecision) → bool`. Routes to
  the existing executors (worker train, building request, unit train,
  research). Strangler: decisions in, legacy execution out. Logs via
  DecisionLog. DONE = every issuance path goes through it.

## Opening script (the chess memorization)

- [x] 3. Explicit worker lines in the Protoss BO file(s) (`9 - Probe`
  style - the parser already takes any unit). No parser change.
- [x] 4. `BuildOrderOwnsGame`: true until the last BO row is done (or a
  supply ceiling). While owned, `DynamicProductionCommander` runs ONLY
  the supply pump (pylons - anti-death; a late/killed BO pylon must not
  supply-block the script). Everything else waits its turn. DONE = an
  OpenBW opening shows zero dynamic rows before the BO releases.

## Producer migration (each: question emits, dispatcher executes, test pins)

- [x] 5. Workers (`AutoProduceWorkersCommander` → WorkerQuestion, queue
  mechanics stay in the executor).
- [x] 6. Supply (`SupplyCommander` → SupplyQuestion).
- [x] 7. Buildings (Gateway/Core/Assimilator → thin per-type questions).
- [x] 8. Army (Zealot/Dragoon → ArmyQuestions).
- [x] 9. Tech + expansion + cannons - covered at the funnels, not per
  file: RESEARCH rows land audit rows at the queue-tech funnel, sited
  cannon/pylon requests at RequestBuildingNear, expansion Nexus rows
  flow through the BUILD wrapper like every queued building.

## Tests (decisions, never the engine)

- [x] 10. `ADecisionTest` (value semantics) + per-slice
  `*QuestionTest` (state → decision matrix incl. refusals) +
  sequencer test (BO owns → dynamic off except supply).
  DONE = suite green, no slow scope needed for any of them.

## Explicitly out of scope here

- Combat decisions (battle cluster owns its own questions in step 5 of
  __PLAN-COMBAT; same types, later).
- Gate simplification (Phase B): producers keep their internal gates at
  first, only the output form changes. Pure state→decision rewrites are
  follow-up slices, not this migration.
- ML/ES tuning (needs the micro battery first - separate track).
