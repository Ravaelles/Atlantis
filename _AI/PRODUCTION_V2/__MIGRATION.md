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
