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
- Game proofs and their re-runs belong to the owner on Wine; OpenBW
  parity runs are autonomous; flagged Wine runs need explicit approval.
