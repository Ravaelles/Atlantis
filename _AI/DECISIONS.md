# Decisions awaiting the owner

Each entry: what is decided, why it cannot be decided autonomously, the
options with costs, and the recommendation. The owner approves by saying so
(one line per entry is enough, e.g. "D4: wire it"); the entry is then
executed and **deleted** (git history is the archive).

Conventions obeyed while waiting: no blind patches for undecided items,
probes and measurements first (CONVENTIONS §3a), one logical change per
commit (§6).

## D1 - Delete the legacy position path (`APositionFinder` and friends)

22 files use it and default runs play on it. Deleting without the catalogue
cutover breaks the bot. Options: (a) cut over to `PLACEMENT=catalogue` first,
then delete per `_AI/PLACEMENT-CUTOVER-PLAN.md` (recommended, but needs the
parity gap closed); (b) delete now and break default runs (not recommended).
Approval of (a) starts with the parity-gap scope, not the deletion itself.

## D2 - Raise early fallback-supply thresholds (`minFreeSupplyToAct` table)

Early Pylons build fine (5 by supply 49 in every Wine/OpenBW game); the miss
is late-search, not timing. Raising the table orders earlier at the price of
early minerals on every game. Not recommended without a game showing an early
supply stall (none exists). If approved: one number + a Wine-length A/B.

## D3 - Create constructions at decision time, wait for minerals

Queueing a building at 18 supply without bank reserves nothing (reservations
attach at READY), so early creation needs a reserve-timing redesign, not a
one-line change. Needs a Wine-length A/B by construction. Alternative kept:
the Core reservation (committed) achieves the economic effect through
existing machinery.

## D4 - Singularity Charge researcher: wire or delete (NEXT #72)

`ATechManager.researchDynamically` is the only dynamic upgrade path and has
zero callers - Dragoons fight without range all game. Wire (free combat
value, needs a long-game proof research completes) or delete (dead code).

## D5 - Worker-gate, mineral-distance and expansion-timing thresholds (NEXT #60, #51, #58-timing)

Strategy tuning, not defects (`--long` games confirm: 7 Dragoons at 429 s
with no expansion - gates, not bugs). Same for cannon producers (#39) and
the three missing build orders (#46: rename entries, add files, or delete).

## D6 - Production race-strategy seam (NEXT #8)

ADR 0007 is Proposed. Implementing its instance-injection seam needs owner
acceptance of the ADR first.

## D7 - `BaseUnderAttack` cache vs seam (NEXT #36) and construction-recovery
branch (NEXT #40)

Both need a design/intent decision before any code: a static cache violates
CONVENTIONS §5, and #40's unreachable branch needs the intended behavior
named by the owner first.

## D8 - Terran deployment target (NEXT #15)

No approved Terran artifact path. Either supply one or verify none is needed.

## D9 - Roster counts unfinished units (owner's "false readings" complaint)

The `###` roster reads the engine unit list 1:1
(`Select.ourWithUnfinishedOfType` over `playerUs().getUnits()`), unfinished
INCLUDED by the 2026-10-12 ruling. Measured 2026-10-13 (owner's 331 s run):
Gateway 2 completed vs 3 roster - the +1 ordered at 5:16, 15 s before the 5:31
ForceExit (warping at count time); Pylon 4/4, Assimilator 1/1, Core 1/1
completed==roster in the same run. Not desync (a reinstall changes nothing -
the list IS the engine list). Side effect: E2E EXPECT_* gates read the same
line, so 3-training Dragoons pass a Dragoon>=1 gate.
Options: (a) keep (the replay-visible argument from the ruling); (b)
completed-only everywhere; (c) keep the board line, add completed-only counts
for the gates (recommended - gates assert built, the board shows mid-build).
No code change until approved.
