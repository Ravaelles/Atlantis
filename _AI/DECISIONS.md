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

## D5 - Worker-gate, mineral-distance and expansion-timing thresholds (NEXT #60, #58-timing)

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

## D9 - Engine-path early JVM exit (NEXT #69 follow-up)

Three jstack catches agree on the hang frame: after `leaveGame`, main and the
JBWAPI client thread are gone and only JNativeHook's non-daemon hook thread
holds the JVM until the outer timeout; all three runs flushed valid `.rep`
files. So engine-path lingering is load-bearing today - the runner `wait`s on
the bot process for artifact collection and assertions (`PLAN-OPENBW.md` even
documents the exit-124 shape for recording runs).
Options: (a) unregister the hook at game end and rework the runner to wait on
the host/match instead of the bot (runs end when the match ends instead of
burning the full cap; needs a replay-flush safety experiment first - a bot
that exits right after `leaveGame` must not truncate the launcher's flush);
(b) keep lingering (recording runs burn the full cap; zero risk). The
non-engine `leaveGame` paths stay backstop-guarded either way.
