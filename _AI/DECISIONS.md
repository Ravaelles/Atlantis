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
Status 2026-10-18: the bug-fix half of the cutover was withdrawn (the
JBWAPI `hasPath` source patch builds Pylons through the legacy chain -
see `PLAN-STAGE4-COMMAND-PATH.md` "RESOLVED"); what executes stepwise is
the deletion ordering in NEXT #45, unchanged.

22 files use it and default runs play on it. Deleting without the catalogue
cutover breaks the bot. Options: (a) cut over to `PLACEMENT=catalogue` first,
then delete per `_AI/PLACEMENT-CUTOVER-PLAN.md` (recommended, but needs the
parity gap closed); (b) delete now and break default runs (not recommended).
Approval of (a) starts with the parity-gap scope, not the deletion itself.
