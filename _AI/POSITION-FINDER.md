# PositionFinder: what we know before rewriting it

Status: **diagnosis only, no fix.** The owner's decision (2026-10-08): this
subsystem will be **deleted and rewritten**, so this file records what the
debugging established rather than what was changed. Nothing here is a plan to
patch the existing code.

Read this before starting the rewrite so the same hours are not spent again.
Measurements are from a live OpenBW game on `maps/cog/(3)TauCross1.1.scx`,
`scripts/run-openbw-e2e.sh`, 2026-10-08.

---

## 1. The failing symptom (the reason for the rewrite)

The bot cannot place its **first Pylon**. Every run, at the same frame:

```
0:39: Can't find place for `Pylon`, At 8 Pylon (READY_TO_PRODUCE)(#1)
(reason: Can't physically build here)
(Max search distance was: 36) near null
```

`Can't physically build here` is set in exactly one place -
`CanPhysicallyBuildHere.check`, after `MapTiles.canBuildHere` returned false.
`near null` is `order.aroundPosition()`, which is null for this order.

---

## 2. What was measured (each one is a fact, not an inference)

### 2.1 The tiles are valid; the answer that says otherwise is ours

Instrumenting the refusal for the Pylon printed, for the refused positions:

```
[118,6]  w=true b=true bi=true
[119,6]  w=true b=true bi=true
```

`w` = engine `isWalkable`, `b` = engine `isBuildable` (no buildings),
`bi` = engine `isBuildable` **including** buildings.

So the engine reports the tiles as **walkable, buildable and empty**, and the
placement is still refused. The refusal is therefore **not** the engine and
**not** the map data - it is a condition of ours that disagrees with both.

### 2.2 The occupancy guard was the refusal (and was wrong)

`BuildingTilesAreOccupied.check(position, building)` returned **true** for
`[118,6]` - on tiles the engine had just called empty (`bi=true`).

Cause of that false positive: the guard compared tile rectangles against
**every unit in `Select.all()`**, so in a mineral line a worker or a patch two
tiles over "overlapped" the Pylon's footprint. Fixed in this session (only
buildings count, and the guard is no longer consulted on the engine-tile path),
but the episode is the point for the rewrite: **a hand-rolled tile-geometry
predicate silently disagreed with the engine, and the disagreement was
invisible** - both answers were booleans, and only printing all three
side by side found it.

### 2.3 `JBWEB` is unusable for this on OpenBW - in two different ways

- `JBWEB` is a **JNI** library. Its natives do not exist on Linux;
  `InitJBWEB.init()` fails and `AMap` catches the exception and continues
  (`_AI/LOCAL-STARCRAFT.md` 187-189). So on OpenBW there is **no JBWEB** -
  yet `JBWEB.isInitialized()` (the version written this session, set only at the
  **end** of `onStart`) returned **true** in a live OpenBW game. `onStart` does
  run to completion there; the parts that fail are elsewhere. So the flag cannot
  be used to decide "is JBWEB's grid trustworthy here".
- `JBWEB.isPlaceable` refused tiles the engine called free. Whatever the cause,
  **on OpenBW JBWEB is not an answer worth consulting at all.**

### 2.4 The standard finder was never even reached

A print at the top of `ProtossPositionFinder.findStandardPositionFor` **never
fired** for the Pylon. So `FindPosition.findForBuildingRaw` returns null (or
takes another branch) **before** the standard finder runs. That is where the
search actually stops for this building, and it is the first thing the rewrite
must make observable.

(Note: that print was reverted with the rest of the diagnostics; the statement
above is from the run where the jar was verified to contain it:
`unzip -p ...jar .../ProtossPositionFinder.class | strings | grep "DIAG search"`.)

### 2.5 `near null`

The Pylon order reaches placement with `aroundPosition() == null`. The search
does have a `nearTo` (`DefineNearTo` falls back to `mainOrAnyBuilding`, then to a
hard-coded `APosition.create(50, 50)`), so the `near null` in the message is the
**order's** position, not the search anchor. Two different "near" values with
the same name in one call chain - worth eliminating in the rewrite.

---

## 3. What the rewrite should avoid (the traps this debugging hit)

1. **One question, one source of truth.** "Can a building stand here?" was
   answered by (a) the engine's composite `canBuildHere`, (b) the engine's
   per-tile `isBuildable`/`isWalkable`, (c) `JBWEB.isPlaceable`, and (d) our
   own `BuildingTilesAreOccupied`. All four ran, and they disagreed. On OpenBW
   (b) is the only one that is both available and correct.
2. **Boolean predicates must be observable.** The whole cost of this
   investigation came from four predicates returning plain `false` with no
   record of which one spoke. Every "can/cannot" decision in the rewrite needs a
   named reason attached at the moment it is made.
3. **No JNI dependency in the placement path.** JBWEB cannot load on the E2E
   engine, which is the engine the rewrite is for.
4. **Counts and coverage must be explicit.** "Occupied" needs a precise
   definition (which unit kinds? finished/unfinished? resources?) - the ad-hoc
   tile-rectangle overlap got it wrong in both directions within one session
   (first "occupied" for an empty mineral-line tile, earlier "free" for a tile
   with a building on it).
5. **Do not cache a null.** `APositionFinder`/`Construction` cache these
   answers; a cached `null`/`false` from a moment when the world was different
   makes the failure look constant (same tile coordinates, same frame, every
   run) rather than time-dependent.

---

## 4. Open questions for the rewrite (not answers)

- Why does `FindPosition.findForBuildingRaw` return null before the standard
  finder for a Pylon? The chain `DefineNearTo` -> builders -> `APositionFinder`
  -> `ProtossPositionFinder` has early returns on all four, and one of them is
  firing. This is step 1 of any rewrite investigation.
- Is the engine's per-tile `isBuildable`/`isWalkable` sufficient **by itself**
  for OpenBW, with no JBWEB and no hand-rolled overlap test? §2.1 says it agreed
  with reality in every case measured.
- Does the Wine path need a different answer than OpenBW, or can one predicate
  work on both? `MapTiles.Source` exists as the seam for exactly this question.

---

## 5. Related records

- `_AI/NEXT.md` #46 - this symptom, recorded as the remaining OpenBW item.
- `_AI/PLAN-OPENBW.md` §9 - the attach fix and the two issues it left.
- `_AI/LOCAL-STARCRAFT.md` 187-189 - JBWEB's natives do not exist on Linux.
- `_AI/BUGS.md` B-1 - the `eval()` scale, which is unrelated but is the other
  place where a number with no documented scale is compared against thresholds.
