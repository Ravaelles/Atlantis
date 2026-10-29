# Full migration: V2 production + catalogue placement, old code deleted

Owner direction 2026-10-19: fix fighting first (win at least average
battles), then cut the old production code. Combat is the entry gate:
nothing below starts until the cluster holds, and every phase below
closes only on a game proof, never on reading green.

## Phase 0 — combat works (entry gate, autonomous except the Wine proofs)

Win condition for the phase: average battles are won, not just survived.
In this order:

1. **Breach discipline** (LANDED, stub-pinned): breach forces FIGHT and
   resets the retreat latch; retreat on a breach is per-unit only
   (hp <= 40 with cooldown >= 5); the rally is the choke-side point,
   never the mineral line; a breach forces DEFEND posture so the cluster
   does not march out while the base is under attack.
2. **UMS discipline** (this plan): on UMS maps there is no base to run
   to, so FLEE is usually the wrong answer. Rule: no retreat for the
   first 15 seconds (`A.isUms() && frame < 360` forces FIGHT); after
   that a baseless retreat rallies at a point AWAY from the enemy to
   regroup (`MoveAway.bestDestination`), never at the enemy base and
   never by standing still. Maps: `ums/rav/protoss/8Drag_v_8Drag.scm`
   and `ums/rav/protoss/ZealDrag_v_LingsHydra.scm` (owner call).
3. **7th_rav acceptance** (owner-run Wine, `ums/rav/7th_rav.scx`): the
   enemy cheats masses in (Stardust trained here; winning outright is
   NOT the bar). The bar: the army holds the main base together with
   the workers, no retreat while in the main with 2+ combat units, and
   fighting reads as FIGHT rows, not FLEE rows. Losing to 5:1 cheated
   odds is data, not a defect - running from winnable fights is.
4. **Micro battery** (owner-run Wine, same maps): kite cadence, focus
   fire, retreat latch - the #152 leftovers that only a spawned enemy
   can prove.

Phase exit (owner judges): average battles won on 7th_rav; CLUSTER twin
shows FIGHT in the main, DEFEND posture under breach, rallies away
from the enemy on baseless maps.

## Phase 1 — V2 parity (autonomous OpenBW runs)

`scripts/run-openbw-e2e.sh` is the model-facing vehicle (150 s wall,
20 game-min cap). Measure, in order: power-smart siting effect (my
`PylonPowerSiting` slice: `Not powered` refusals down, tech ground
powered), warps/min vs legacy 0.83, no supply blocks at 17/17 and
25/25, Dragoon flow post-ASAP-rule. Then DRY_RUN parity rows
(WO-144 step 2). No behavior changes here unless a twin names a
mechanism - numbers first.

## Phase 2 — single author (WO-144)

STATUS 2026-10-28: the supply contract (`dynamic/supply/NeedDynamicSupply`
+ `ProtossNeedDynamicSupply`) is already board-consumed and pinned - no
refactor needed. Open: convergence of the flat-4 V2 goal with the
6/10/14 bands (WO-144 delta 1), then the legacy callers move; both need
an owner-run Wine proof. Walk queue: `PRODUCTION_V2/legacy-production-queue.md`.
Buildings group triaged 2026-10-28 (Q33-Q49): 10 rows covered by V2
(board goals, scheduler prereqs, demand goals - no port), Stargate +
Corsairs ported as V2 goals (`StargateCorsairTest` 6/6), 5 rows open
behind their unit chains (Citadel/Archives, SupportBay, Tribunal,
per-base Pylons) and ShieldBattery dead upstream.
Decisions/units/tech triaged 2026-10-28: AllowProduce gates and the
ling mix ported (`ArmyGatesTest` 9/9), observer eyes scaled
(`HiddenUnitDetectionGoalsTest` +1), Citadel + Legs ported
(`CitadelLegsTest` 6/6). Open: armor/weapons ladders (game-proof
first), Archives/Storm + DT/HT, SupportBay + Reaver/Shuttle/Arbiter,
per-base Pylons, Q70 proximity siting, Q82 dispatcher expediting,
Q53 gateway rush tuning. Suite 1767/1767, arch advisory unchanged.
Templar chain landed 2026-10-28 (Archives + HT + Storm + DT as V2
goals, `TemplarChainTest` 7/7; mission-attack and BO-permission
clauses dropped with notes, lifetime DT cap becomes loss replacement,
free-Robo refinement stays behind). Suite 1774/1774, arch advisory
unchanged.
Reaver chain landed 2026-10-28 (Bay + Reaver + Shuttle + Tribunal +
Arbiter as V2 goals, `ReaverChainTest` 8/8; Terran floors and
mission-diversion dropped with notes, air-unprepared becomes
eyes-first, lifetime caps become loss replacement; scarabs stay with
the specialist behavior). Queue tally: 69 done, 61 open, 11
Phase-gated, 82 excluded.
Dead code cut 2026-10-28: ProduceShieldBatteryAtNatural (disabled
upstream, sole caller unhooked) - first legacy file deleted, not
moved. Queue tally now: 70 done. Open: armor/weapons ladders, Archives/Storm
ports... (Storm landed), DT/HT (landed), SupportBay chain (landed),
per-base Pylons, Q70 proximity, Q82 expediting, Q53 tuning, Q9/Q16
expansion deltas, Phase-4 queue + produce-pipe + build-orders.
Second dead cut 2026-10-28: PendingNextBase (zero callers).
Queue tally now: 77 done, 7 open (all game-gated: Q9/Q16 expansion
deltas, Q44 per-base power, Q53 gateway rush, Q63/64 armor/weapons,
Q70 proximity siting), 11 Phase-gated, 82 excluded, rest covered.
#226 landed 2026-10-28: retreat validity gate in decide()
(`RetreatStandableTest` 5/5, DIP predicate). Suite 1787/1787.
#225 landed 2026-10-28: refresh re-sites on completed-building
footprints only (`RefreshOccupancyGateTest` 5/5, worker/warping/builder
never block, standing building does; cooldown deliberately skipped -
papers over refused-tile memory, which is #145). Suite 1792/1792, arch
advisory unchanged.
REGRESSION 2026-10-28 (game 235312, PvsZ, DEFEAT f14029): 4 warps
then flood death; 176 honest unaffordable refusals, no stalls, no
duplicates, zero refresh/unwalkable traces. The batch never fires
meaningfully (death-rattle detection only). #225/#226 live proof
still open - shapes unobserved.
SUPPLY VERDICT 2026-10-29 (game 111114, PvsZ, loss on time f28800):
296 supply-blocked rows, ZERO engine-refused (was 222+148). The stall
was supply-edge churn, not phantom-busy gateways - the engine
correctly refuses every train at 0 free supply while the Pylon
pipeline lags. Fix is upstream (warp the Pylon sooner, #150/#151),
not in the facilities. 61 warps, 3rd live Forge, full-length survival.
UPGRADES 2026-10-29: weapons/armor ladders + PvP 6-gateway cap ported
verbatim (`TechUpgradesTest` 10/10); too-weak veto keeps its minute-10
expiry. Suite 1808/1808.
STUCK-GATEWAY SHAPE 2026-10-29 (game 001939, PvsZ, DEFEAT f24389):
bank 1800->3800 with 175 refused Zealot + 23 Dragoon trains ("no free
facility") while 3 Gateways stand idle - first refused twin split now
lands: the dispatcher used to collapse 5 denial branches into one
label, so a stall read as one shape. GameOrderDirector.trainFacility
returns the branch reason ("no such facility" / "facility unfinished"
/ "already commanded" / "already training {type}" / "engine refused"),
pinned in TrainRefusalReasonsTest 4/4 + updated dispatcher pins.
Next live game adjudicates which branch dominates.
FACILITY STATE 2026-10-29: denial details now carry the facility
snapshot (id, idle flag, training flag, queue depth + head) so busy
reads apart from stuck. Zero behavior change.

Needs exactly one V2 LIVE game (step 3: which goal re-fires pairs -
the V2_GOAL_SOURCE twin decides). Then move `SupplyCommander/**` +
fallback to `src-old/` (step 4). Exit: one author, supply pumps
silent.

## Phase 3 — siting cutover (WO-145 + WO-146)

Gateway-arm coverage first (the known `185357` gap: 20x no-builder,
2x dedicated-finder, 7x legacy-refused, 1 residual Can't-find-place),
parity game, flip catalogue default, then move the chain
(`NewConstructionRequest` + `FindPosition`/`DefineExactPosition` +
`APositionFinder`, ~24 files incl. comments) to `src-old/`.
Blocked until then: `base/*` (waits on C14 expansion siting -
**owner decision: backoff vs real siting**), `PositionModifier` 6
call sites (one catalogue arm or justified dedicated finder each),
Terran SupplyDepot (D8 Protoss-only).

## Phase 4 — queue deletion (WO-147, LAST)

Re-home the 83 out-of-queue `ProductionOrder` users by group (demand
to #144, siting to #145, construction links to plan-item links,
listeners to ledger/plan, the rest dies with the manager tree in
stages E/H). Then V2 LIVE by default with parity numbers, then
`Queue/**` + order types to `src-old/`. A bot that can't queue can't
play - this is why it is last, not first.

## Phase 5 — fortress (stages E-H leftovers, then I, then J)

#10 remainder (container views + designed splits), #11 only if a
per-condition seam appears, #9 facades with Stage G/I, #13 down to
zero violations, then the physical package move per context and the
frozen store flipped to hard rules.

## Phase 6 — completion: no version checks, every file earns its place
(owner order 2026-10-29)

Priority: at no point should anyone (human or model) have to ask which
solution is live - there is exactly one, and every remaining old file
is either refactored into it or gone. Per-file verdicts live in
`legacy-production-queue.md`; this phase tracks the cross-cutting
deletions the per-file walk cannot do one class at a time.

### 6A — kill every version switch (NEXT #227)

`Env.productionV2()` has three modes (LIVE default, DRY_RUN, OFF) and
every branch on it is a second behavior to maintain, test, and doubt
in post-mortems. Target state: no production code reads the mode at
all - V2 is not an option, it is the code. Enumerated 2026-10-29
(all `productionV2().isLive|isDryRun|isEnabled` reads outside `Env`
itself; tweaker engines excluded - they are debug tooling, decided
under #228 if they stay):

1. `ProductionCommander.java:26` (`isLive()` route: LIVE list vs full
   legacy list) -> delete the legacy branch, keep
   `ProductionOrdersCommander + ConstructionsCommander + ForceExit* +
   ProductionHealthWatcher`. The OFF-mode game path dies with it
   (owner call already made: V2 LIVE plays everywhere).
2. `ProductionOrdersCommander.java:52,61,67` (LIVE skips the queue
   drain; OFF-only supply witness; DRY_RUN twin) -> delete the legacy
   drain and the OFF witness; DRY_RUN goes away with the mode (twin
   coverage moves to normal V2 twins).
3. `ProductionEngine.java:107` (`isEnabled()`) and `:147`
   (`isDryRun()`) -> the engine always runs live; delete both gates.
4. `Queue.java:35` (`refresh()` early-return in LIVE) -> delete the
   whole `refresh()` path with the queue (Phase 4), not a guard fix:
   the guard is correct today and must not be "cleaned" into a live
   legacy path.
5. `ProductionV2Mode.java` + `Env` §218/§399-411 -> delete the enum,
   the parser, and the ENV knob once 1-4 land (single commit, suite
   green; no doc-only commit).

Order matters: 1-3 are pure deletions behind a suite run each (one
commit per file, per §6); 5 closes the knob. The DRY_RUN comparison
vehicle (WO-144 step 2) must bank its verdicts BEFORE 2 lands, or the
parity evidence has no vehicle.

### 6B — every used old file gets a verdict (NEXT #228)

Per-class walk is tracked in `legacy-production-queue.md` (223 rows:
82 done, 138 excluded/archived Terran-Zerg/dead, 3 open tuning,
rest `[>]` Phase-gated). The remainder that is still READ in LIVE
(stale reads, not stale files) closes here, group by group, each with
a pin test and suite green before the next:
- `Queue.get()` readers in live paths (forecast in
  `ShouldNotTravelToConstructYet`, `GoalBoard*`, `Have`, listeners):
  re-point each at V2 state or delete the read with its caller.
  The legacy detection writes (`RequestProtossDetection` etc.) are
  already dead letters in LIVE (nothing drains) - delete the writes,
  not just the drain.
- `ProductionOrder` attached to every V2 construction
  (`GameOrderDirector.orderFor` synthetic + `position`/`modifier`
  readers in siting): replace with the V2 demand carrier (WO-147).
- BO files as the opening source (`BuildOrderRow.fromLegacy`,
  `CurrentBuildOrder`, `BuildOrderOwnsGame`): keep the .txt format,
  delete the queue-flavored machinery around it (WO-147).
- `constructions/**` execution chain: NOT for deletion (V2 delegates
  builders there, #147 retain). Refactor target instead: each file
  gets a one-line purpose or is merged; no behavior change, pins stay
  green.
- `Tweaker/*Engine` debug vehicles: keep or delete - owner call,
  recorded in #228, not silently kept.

A file is "done" when: it is V2-native, or deleted to `src-old/`, or
explicitly retained with the reason written next to it in the queue
row. Absence of a verdict is the defect this phase removes.

### 6C — StarEngine gaps (NEXT #229 canBuild, #230 fog)

What sim covers today (62 files, 39 test classes, ~109 sim tests):
faithful movement (accel/braking/turn), walkability/buildability/
reachability/regions/chokes, bank/mining, training queues + research
+ warps with engine-dedup mirror, per-unit cooldowns. What it
demonstrably does not (each found by a live-or-harness falsification,
not by audit):
- **engine refusals** (no `canBuild`-vs-engine, no stuck training,
  no placement-vs-engine validation): the #150/#225 class and the
  phantom-busy shape are invisible in sim by construction.
- **fog-of-war**: mentioned in 4 sim files, no perception model -
  hidden units are either present or not, never *unseen*.
- **order latency frames**: no model (hence once-per-minute stack
  traces live that sim never shows).

**canBuild in sim (#229).** Implement the engine's answer the way the
bot must treat it: a tile is buildable iff walkable terrain AND inside
bounds AND footprint-clear of buildings AND (Protoss) powered AND
explored - evaluated against the sim map, returning the same refusal
family the twin names live (`no-placement` vs `unaffordable` vs
`producer-busy`). Fidelity bar: the sim replays game 133317's Nexus
site and refuses for the same reason the live twin logged. Out of
scope: JBWAPI exception shapes (AIOOBE on busy training) - twin them
live instead.

**Basic fog-of-war (#230, owner spec 2026-10-29).** Good enough for a
start, deliberately crude: every 15 frames, each known enemy unit is
visible iff any of our units is within 8 tiles of it; otherwise it is
not visible (stays in fog memory with last-seen position). No
direction cones, no terrain occlusion, no detection-type differences -
those are follow-ups with their own pins only after this one proves
useful (kills a sim-vs-live divergence or enables a hidden-unit sim
scenario, e.g. lurker response without a live game). Pin: a 15-frame
cadence test (no per-frame recompute) + a visibility-boundary test
(7.9 visible / 8.1 not, both sides of the seam).

## Stop rules (apply to every phase)

- No bottom-up deletions: a phase's exit holds in a game first.
- Never gate commits on full games; never trade a measurement for an
  explanation (§3a).
- A twin that names no mechanism is write-only: instrument, game,
  decide, then code.
- Game proofs: routine proof macros run autonomously (CONVENTIONS §30 -
Wine needs no permission; StarEngine first, Wine when the sim cannot
model it or must confirm). The owner runs only the adversarial /
acceptance vehicles (7th_rav, micro battery, mission games).
