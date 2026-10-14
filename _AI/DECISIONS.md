# Decisions awaiting the owner

Each entry: what is decided, why it cannot be decided autonomously, the
options with costs, and the recommendation. The owner approves by saying so
(one line per entry is enough, e.g. "D4: wire it"); the entry is then
executed and **deleted** (git history is the archive).

Conventions obeyed while waiting: no blind patches for undecided items,
probes and measurements first (CONVENTIONS §3a), one logical change per
commit (§6).

## D1 - Delete the legacy position path (`APositionFinder` and friends)

Approved 2026-10-14: full cutover (V2 default + delete legacy), executing stepwise; entry closes with the deletion.

22 files use it and default runs play on it. Deleting without the catalogue
cutover breaks the bot. Options: (a) cut over to `PLACEMENT=catalogue` first,
then delete per `_AI/PLACEMENT-CUTOVER-PLAN.md` (recommended, but needs the
parity gap closed); (b) delete now and break default runs (not recommended).
Approval of (a) starts with the parity-gap scope, not the deletion itself.

## D5 - Worker-gate, mineral-distance and expansion-timing thresholds (NEXT #60, #58-timing)

Strategy tuning, not defects (`--long` games confirm: 7 Dragoons at 429 s
with no expansion - gates, not bugs). Same for cannon producers (#39) and
the three missing build orders (#46: rename entries, add files, or delete).


## D7 - `BaseUnderAttack` cache vs seam (NEXT #36) and construction-recovery
branch (NEXT #40)

Both need a design/intent decision before any code: a static cache violates
CONVENTIONS §5, and #40's unreachable branch needs the intended behavior
named by the owner first.

## D8 - Terran deployment target (NEXT #15)

No approved Terran artifact path. Either supply one or verify none is needed.
