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

- [x] **Build-order goals must not repeat completed or committed rows.**
  `BuildOrderRow` + `BuildOrderProgress`: row K of item T is the N-th
  occurrence of T and is emitted only while the game has fewer than N
  (completed, building, queued or pending). Test: `BuildOrderGoalsTest` (9).
- [x] **Prerequisites and existing facilities must be modeled correctly.**
  `ExistingItems.availableFrom` returns the frame an item is available from
  (a Core under construction gates the Dragoon until its completion frame);
  `UnitProducible` lists every required building plus a Pylon for psi.
  Tested in `ProductionSchedulerTest`.
- [x] **Make placement and resource allocation atomic.** Placement is validated
  before `timeline.allocate`; a failed reservation leaves the timeline
  untouched (test: failed fake placement followed by an affordable item).
- [x] **Enforce producer assignment and frame uniqueness.** Items carry a
  concrete producer id, one facility holds one item per slot,
  `producerLimit` is honoured, `GameOrderDirector` refuses a second command on
  the same facility in one frame. Tests: single-Gateway serialization,
  two-Gateway parallelism, producer limit, consumed larva.
- [x] **Preserve absolute requested start frames and stable ordering.** All
  timeline operations are absolute frames; `targetStartFrame` is a floor;
  equal priority keeps the caller's order (stable sort). Tested.
- [x] **Supply timeline must be internally consistent.** Available and total
  supply are tracked, providers add supply at their completion frame, the
  total is capped at 200, and an item is never scheduled on supply that never
  arrives. Committed work is applied before scheduling. Tested.
- [x] **Prevent prerequisite recursion loops and duplicate prerequisite
  items.** A path guard cuts malformed recipe cycles; a shared prerequisite is
  planned once. Tested.

## Priority 2 — Complete game adapters and goal coverage

- [x] **Pending work accounting:** `CommittedWork` models BWAPI accounting -
  an unpaid construction reserves its cost at the first affordable frame, a
  provider under construction adds supply at completion, a worker only starts
  paying after its first trip. Placed buildings are not charged twice.
- [x] **Research and upgrade lifecycle:** an upgrade level is its own recipe
  with its own cost and requirements; the director answers success only for
  our own tech/upgrade and refuses an unfinished or already-busy facility.
- [x] **Dynamic goals:** one owner for the supply rule (the duplicate-Pylon
  regression is pinned), expansion counts a base under construction, the army
  goal is a gap to a floor. Tested in `GoalSourcesTest`.
- [ ] **Buildings:** keep the legacy `Construction` execution path until V2 is
  verified live (retained; a pending construction is re-offered every frame).
- [ ] **Dynamic goals (full parity):** tech, race-specific army composition and
  strategic (Play/Published API) contributions are still the simplified
  versions; the `CandidateResolver` seam exists, `PylonPlacementScore` is not
  yet wired to a multi-candidate finder.
- [ ] **Economic model:** `EconomyModel` holds the rates and the first-trip
  delay; worker/gas rates are still constants, not measurements.

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
