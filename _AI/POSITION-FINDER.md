# PositionFinder: the rewrite (landed 2026-10-08)

**The rewrite landed.** The old `PositionFinder` / `APositionFinder` path is being
retired in favour of `atlantis.placement`, designed in
`_AI/redesign/03_PLACEMENT.md`. This file keeps only the facts that cost real time
and would cost it again.

## What the debugging established (the short version)

1. **The refused tiles were valid and empty.** When the bot could not place its
   first Pylon, instrumenting the refusal printed, for the refused positions:

   ```
   [118,6]  w=true b=true bi=true
   ```

   `w` = engine `isWalkable`, `b` = engine `isBuildable`, `bi` = `isBuildable`
   including buildings. The engine called the tiles fine while placement said no -
   the refusal was **ours**, not the engine's and not the map's.

2. **The culprit was our own occupancy predicate.** `BuildingTilesAreOccupied`
   compared tile rectangles against **every unit**, so in a mineral line a worker
   or a patch two tiles over "overlapped" a Pylon's footprint and every sensible
   position read as occupied. The rewrite counts **buildings only**.

3. **`JBWEB` is unusable on OpenBW.** It is a JNI library whose natives do not
   exist on Linux; `InitJBWEB.init()` fails and `AMap` catches it and continues.
   Anything consulting it answers nonsense there. The rewrite does not depend on
   it.

4. **`Game.hasPowerPrecise` is not usable either** (JBWAPI): it returned false for
   tiles a finished Pylon covered, so Forge and Cybernetics Core were placed
   unpowered. Power is now computed from our own Pylons at the engine's 6-tile
   radius.

5. **The standard finder was never even reached** for the Pylon: `FindPosition`
   returned null earlier in the chain (`DefineNearTo` -> builders ->
   `APositionFinder`). That chain of early returns is why the subsystem was
   rewritten rather than patched - and why the new planner has no such chain: one
   catalogue, one ranked list, one reservation.

## The traps the rewrite had to avoid (and does)

- **One question, one source of truth.** The old path had four answers to "can a
  building stand here" and they disagreed.
- **Booleans must be observable.** Four predicates returned plain `false` with no
  record of which spoke; that is what made this take two sessions.
- **No JNI in the placement path.**
- **"Occupied" needs a precise definition** - the ad-hoc overlap got it wrong in
  both directions inside one session.
- **Do not cache a null.** `APositionFinder`/`Construction` cached their answers,
  which made a stale failure look constant.

## Where this lives now

- `_AI/redesign/03_PLACEMENT.md` - the design, with the "NOT FINISHED" list at the
  top of that file.
- `src/atlantis/placement/` - the implementation (`core/`, `engine/`, `race/`,
  `policy/`, `blocks/`).
- `_AI/IDEA-E2E-TESTS.md` §3.1 - the first live OpenBW run and what it showed.
