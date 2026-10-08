# Production V2 — Remaining Work
Status: open. This is the working checklist for completing
`_AI/redesign/01_PRODUCTION.md`. The Gateway train failure is deliberately
**deferred**; do not let investigation of that report block the remaining V2
implementation. Its cause is not yet confirmed from the supplied stack trace.

## Deferred: Gateway `OrderSink.train` error

- Report: training `#138 Gateway` logs `OrderSink.train failed ... : 5` and an
  `ArrayIndexOutOfBoundsException` is subsequently printed.
- The `: 5` text has not been reliably identified as a BWAPI error code; the
  stack trace originates at the `BwapiOrderSink.issue()` catch/logging boundary
  and is not necessarily the original exception stack.
- Existing guards already check completed/alive state and avoid selecting a
  Gateway for Zealot production when `isTrainingAnyUnit()` reports busy. Do not
  add further speculative filters or claim a fix based only on the current
  report.
- Revisit after the V2 implementation is testable end-to-end: capture the
  original exception class/message and the Gateway's completion, training,
  queue, and command state at the call. Then add a regression test for the
  confirmed cause. Keep this separate from the Cybernetics Core prerequisite
  issue unless evidence ties them together.

## Priority 1 — Make the scheduler satisfy the written invariants

- [ ] **Build-order goals must not repeat completed or committed rows.**
  `ProductionEngine` regenerates goals from build-order rows every frame.
  `BuildOrderGoals.from(...)` currently walks rows and emits qualifying goals;
  verify against completed units/buildings, production queues, and pending
  constructions. Add a deterministic satisfaction snapshot/port and tests for
  the same row across consecutive frames, including an item already queued or
  under construction.
- [ ] **Prerequisites and existing facilities must be modeled correctly.**
  Verify that a missing Cybernetics Core is planned, a completed Core satisfies
  the Dragoon prerequisite at the current frame, and an incomplete Core only
  satisfies it at its completion frame. Do the same for Pylon/Gateway/Zealot.
  `ExistingItems.have()` currently treats any unfinished type as present; check
  how the scheduler obtains the availability frame for that facility.
- [ ] **Make placement and resource allocation atomic.**
  `ProductionScheduler.scheduleItem()` currently allocates resources before the
  placement reservation is validated. A failed placement must leave the
  timeline untouched so later goals can use the funds. Add a regression test
  with a failed fake placement followed by an affordable valid item.
- [ ] **Enforce producer assignment and frame uniqueness.** A registry currently
  supplies availability by producer type, while the plan item does not identify
  a concrete producer. Ensure one Gateway cannot receive two commands in one
  frame, producer limits are respected, and busy/queued facilities do not get
  assigned again. Add deterministic multi-Gateway and single-Gateway tests.
- [ ] **Preserve absolute requested start frames and stable ordering.** Verify
  `ProductionGoal.targetStartFrame()` is honored (rather than starting every
  goal at frame zero), equal-priority order is deterministic, counts greater
  than one are distinct, and continuous goals produce only the intended
  per-frame item count.
- [ ] **Supply timeline must be internally consistent.** Test available versus
  total supply, queued unit supply, planned provider completion, supply release
  on unit completion/death where represented, and solvency at every projected
  frame. Include the emergency-provider case at the cap.
- [ ] **Prevent prerequisite recursion loops and duplicate prerequisite items.**
  Add a cycle guard for malformed recipe graphs, plus tests that shared
  prerequisites (e.g. multiple Dragoon goals needing one Core) schedule once.

## Priority 2 — Complete game adapters and goal coverage

- [ ] **Pending work accounting:** review `GameStateSnapshot`'s pending-building
  mineral/gas deductions against actual BWAPI accounting. Confirm whether costs
  for started constructions have already left current stocks; avoid both
  double-counting and omitting pending orders. Test the arithmetic in a pure
  helper and the adapter with a controlled snapshot.
- [ ] **Research and upgrade lifecycle:** verify facility availability, already
  researched/upgraded state, upgrade level and maximum level, one active order
  per facility, and repeated-frame idempotence. Current `GameOrderDirector`
  only checks generic `isResearching()` / `isUpgrading()`; it must not report an
  unrelated active job as success for this goal.
- [ ] **Buildings:** preserve exact-tile/neighbourhood/base-location constraints,
  builder reservation, travel readiness, pending-construction idempotence, and
  reassignment when a builder dies. The current adapter delegates to legacy
  `Construction`; do not remove that execution path until V2 live behavior is
  verified.
- [ ] **Build-order compatibility:** validate the text-file parser mapping for
  every row kind (unit/building/tech/upgrade/mission/setting), counts, positions,
  and supply gates. A currently unqualified row must return on a later frame,
  not be forgotten.
- [ ] **Dynamic goals:** replace the simplified worker/supply/army/expansion
  rules with behavior-compatible goals for workers, supply, expansion, tech,
  army, and race-specific mechanics. Use immutable snapshots and narrow policy
  seams; avoid adding race branches to the pure scheduler.
- [ ] **Strategic goal contribution:** the redesign mentions active Plays
  contributing goals, but Atlantis currently has no equivalent Published API
  established for this track. Inventory existing strategic requests, define a
  narrow production-goal contribution interface, and add it only where a real
  strategy source exists. Do not invent a `Play` subsystem from the combat
  redesign.
- [ ] **Placement scoring:** `PylonPlacementScore` is pure but is not wired into
  `LegacyPlacementPlanner`, because the legacy finder returns one validated
  candidate. Add a candidate-list placement port/resolver that validates every
  candidate before scoring powered buildable tiles; retain the safe legacy
  fallback until this exists.
- [ ] **Economic model:** verify worker mining/gas rates, worker movement cost,
  income from workers completing in the horizon, and projection boundaries.
  Keep estimates configurable/testable and label approximations; the current
  snapshot uses simple constants and does not model all reassignment events.

## Priority 3 — Test the actual production path

- [ ] Add fast deterministic unit tests for each pure rule above; no test should
  launch a game or depend on process-global game state.
- [ ] Keep an owner-tier OpenBW E2E scenario on a real TauCross melee map. It
  must record minerals when the first Pylon and first Gateway start and assert
  the agreed maximum bank of 16, plus Cybernetics Core before Dragoon, repeated
  Gateway production, no duplicate orders, crash-free run, and supply recovery.
- [ ] Test Dry Run and Live composition separately. Dry Run must issue zero
  engine commands. Live must route every item to the correct command
  (train/build/research/upgrade) and must not also run the legacy dynamic/supply
  policy.
- [ ] Add a reproducible broken-plan or broken-bot comparison that fails the
  key acceptance assertion, so the E2E test demonstrates it can catch the
  regression.
- [ ] Keep all commands within 360 seconds. Do not launch StarCraft, Wine, or
  ChaosLauncher; owner runs the OpenBW host/client recipe in
  `_AI/PLAN-OPENBW.md` §8 and supplies the logs/verdict.

## Priority 4 — Cutover and cleanup (only after verified LIVE E2E)

- [ ] Owner verifies the OpenBW LIVE scenario with the documented setup; record
  exact command, map, run result and relevant log excerpts.
- [ ] Fix all newly observed live-only issues and add tests before deleting the
  fallback.
- [ ] Remove legacy `Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`,
  `ReservedResources`/`OrderReservations` where no longer referenced, and
  `Construction/**` healing/recovery commanders only when V2 fully replaces
  their behavior. Retain shared placement/execution functionality until a
  replacement is proven.
- [ ] Re-run source/reference checks, Java 8 compilation, fast unit suite and
  ArchUnit. The frozen ArchUnit baseline must shrink or stay unchanged; never
  grow it to accept a new violation.
- [ ] Update `_AI/STATUS.md` after each verified cycle. M6 is complete only after
  the owner-verified LIVE OpenBW run and the scoped legacy cleanup.

## Working rules

- Work one small, logical cycle at a time: inspect -> focused test -> fix ->
  verify (`timeout 360`) -> update `_AI/STATUS.md` -> commit.
- Keep the legacy path playable until the LIVE E2E acceptance criteria pass.
- Distinguish confirmed code/execution evidence from hypotheses. In particular,
  the Gateway `: 5` report remains deferred and unexplained until its original
  exception/error state is captured.
- Source language and code comments stay English per `_AI/CONVENTIONS.md`.
