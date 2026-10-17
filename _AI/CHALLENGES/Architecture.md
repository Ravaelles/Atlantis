# Architecture: what each reference bot teaches (read before borrowing)

One or two lines per insight - the fact, the measurement or the reason,
the verdict (steal / skip). Narratives live in `_AI/redesign/` and
`_AI/CASE-STUDIES.md`; this file is the quick reference. Lineage:
UAlbertaBot (A) -> Steamhammer (B) -> Locutus (C) -> Stardust (D).
We converge on D with selected loans from the rest.

## UAlbertaBot (A, 2014-2020, C++)

- Shape: military hierarchy GameCommander -> Worker/Production/Building/
  CombatCommanders -> Squads -> per-unit MicroManagers; Global singletons
  for shared info; vertical communication only.
- PRO: readable, modular per axis; replacing one MicroManager cannot break
  production. The `blocking` build-order flag (exact vs opportunistic rows)
  is a two-line idea solving real ordering races.
- PRO: named replan events (queue empty, supply deadlock, critical death,
  cloaked detected) instead of scattered heuristics - a ready-made reason
  vocabulary for an audit log.
- PRO: micro by ROLE (Melee/Ranged/DetectorManager, kiting built into
  Ranged), not by unit type.
- CON: Global singletons are the static-cache disease (our Stage F removes
  the same thing); per-unit-type manager sprawl is what we deleted;
  no forward planning (BOSS search bolted on, deferred by us with evidence).
- Verdict: STEAL blocking flag, replan-event vocabulary, role-based micro.
  SKIP singletons, BOSS search, per-type managers. (Full analysis in chat
  2026-10-17; decision: no full transition - it unlearns B/C/D.)

## TorchCraftAI / CherryPi (2017-2019, C++, archived)

- Shape: modules communicate over a Blackboard via UPCTuples (probability
  distributions over units x positions x commands), refined until dirac,
  then UPCToCommand executes; Tasks track realization 1:1 (status, unit
  allocation, failure proxying back to the decider).
- PRO: one inter-module action language; Task lifecycle (owner + status +
  failure returns to decider) is exactly our Construction idea done generally.
- CON: distributions serve neural nets, not audit - a 0.7 command has no
  "why"; no question gate (refusal = invisible absence); blackboard is
  global mutable state (the testability complaint in another costume).
- CON: archived, libtorch-era deps; resurrection costs a project, not a week.
- Measured against its own tables: the ES micro model scores 0.28 on
  vulture-kiting and 0.00 on regroup; 5-line attack-weakest beats it
  100%-0% in big brawls. Rules first, learning tunes numbers inside them.
- Verdict: STEAL Task lifecycle + failure-to-decider proxying. SKIP
  distributions, blackboard, the product itself.

## Stardust (D, C++ - our primary reference)

- Shape: Strategist -> Plays -> Squads -> UnitClusters (spatial, 480/480/600
  thresholds, sim-gated Activity consensus); Producer: stateless forward
  timeline over PREDICT_FRAMES, atomic commit, pull-forward heuristics;
  catalogue placement (blocks stamped whole-map, ranked lookup).
- PRO: stateless recompute kills entire bug classes (no order rot, no
  orphans); timeline makes starvation visible before it happens; catalogue
  makes placement O(lookup).
- CON: needs its own engine fork (bwgame) for exact simulation; C++ shapes
  (anonymous-namespace globals, 2400-line Producer) do not port literally -
  our splits (Timeline/Scheduler/Planner/Dispatcher) are the port.
- Status here: timeline + catalogue + board ported or porting
  (`_AI/redesign/01_PRODUCTION.md`, `03_PLACEMENT.md`); clusters simplified
  to Phase 0 (one cluster + evaluator gate) in `02_COMBAT.md`.
- Verdict: PRIMARY REFERENCE. Take the geometry (stateless, catalogue,
  clusters), leave the engine fork and the monoliths.

## PurpleWave (Scala - inspected 2026-10-17, /sc-ai/PurpleWave)

- Shape: Strategery (Strategist: fingerprint/history-driven strategy
  selection, swapIn/swapOut, legality) -> Planning (Blackboard TYPED knobs:
  wantToAttack, gasWorkerFloor/Ceiling, yoloing...; prioritized Plans) ->
  Macro (Requests carry REQUESTER; Scheduler collects; MacroSim simulates;
  MacroProducers execute) -> Tactic (Tactician/Missions/Squads) -> Micro
  (Agency agents, Coordination, Formation, Matchups tables, Targeting).
- PRO: requests attributed to requesters (audit twin of our decision
  reasons); strategy-as-knobs (pure numbers, most testable strategy layer
  of the four bots); fingerprint-driven opponent modeling (a real gap of
  ours - StrategyChooser is cruder); matchup TABLES as data (tunable
  without recompiles - our DancePolicies are code doing the same job);
  Gameplans as scripts (validates our BO-as-script direction).
- CON: Scala + sbt + JNI bridge is its own platform (nothing portable to
  our Java 1.8 tree except ideas); blackboard knobs global-mutable like
  CherryPi's (ours are snapshot fields instead - keep it that way);
  MacroSim duplicates what Stardust's timeline already gives us.
- Verdict: STEAL requester attribution (done: ADecision source/reason),
  knob-style strategy fields (add economy knobs to BoardState, not a
  blackboard), fingerprint strategy selection (later item, needs games),
  matchup tables (convert DancePolicy numbers when profiles stabilize).

## Build orders: three formats (owner question 2026-10-17)

- PurpleWave gameplans (`src/Gameplans/Protoss/PvP/PvPOpening.scala`,
  inspected live): imperative code, not rows. `get(N, Unit)` = want N
  TOTAL (cumulative), `get(Unit)` = want one, `pump(Unit)` = continuous.
  Essentials run BESIDE the script every frame (`doWorkers`,
  `RequireEssentials`, `doAutosupply`). Reactive swaps between sequences
  by scouting (ModalGameplan + fingerprints). Every build carries VOD
  timestamps as provenance. Workers are NEVER script rows.
- Stardust: NO sequences at all. Openings are continuous goal functions
  of state (`desiredZealots = max(4, enemyZealots)`, `required = desired
  - have`) fed to the timeline. Nothing to copy as a sequence.
- Ours: `At N - Unit` threshold rows (fire once at supply N). Weaknesses
  measured: never re-fire (dead probe = hole forever); gateway pairs need
  two rows (the duplicate-row class); numbers assume pump workers fill
  the gaps (proven by the 4/9-at-3:34 deadlock when the pump stopped).
- Verdict: take PurpleWave's FORMAT (cumulative `have N` goals +
  continuous worker pump beside the script + VOD/game-ID provenance per
  build), Stardust's ARITHMETIC (`required = desired - have`) for the
  dynamic half. Concrete next slice (not this turn): reinterpret BO rows
  as cumulative goals (refire when count drops; one "have 2 Gateway" row
  instead of At-10 + At-12), keep the file format, change completion
  semantics + one test. Fingerprint-selected openings stay a later item.

## Atlantis hybrid (what we actually build)

- Opening: memorized script (UAlbertaBot opening book + PurpleWave
  gameplans), one issuer, dynamic gated - own invention from evidence.
- Mid-game economy: Stardust timeline geometry (stateless, atomic commit,
  pull-forward) via the goal board + wallet.
- Placement: Stardust catalogue (blocks, ranked lookup), race strategies
  as plug-ins.
- Combat: one cluster + evaluator gate now (Phase 0); role-based
  executors + parameterized kiting later (UAlbertaBot roles, Stardust
  consensus when earned).
- Audit: ADecision with reason + source through one door into
  DECISIONS.log. NONE of the four bots has this - it is our invention,
  kept because every hunt here died on "who ordered that".
- Boundaries: ArchUnit-frozen contexts. None of the four has mechanical
  boundary enforcement; it is what lets a 600-class tree keep moving.
- Learning: numbers inside profiles/policies only (PurpleWave tables
  direction), never decisions - decisions stay auditable by construction.
