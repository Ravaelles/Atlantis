# Idea: cache the terrain analysis per map (`bwapi-data/read/ATA/<map>`)

Status 2026-10-10: **idea + measurement; recommended as NOT worth it yet.** Recorded so
the next session does not have to re-derive the numbers, and so the decision is visible
rather than forgotten.

## The owner's proposal

Save the region computation to a file, one per map, so a later game on the same map does
not recompute connectivity. Location: `bwapi-data/read/ATA/<map-name>` (ATA = Atlantis
Terrain Analysis). The directory convention already exists in this project - `bwapi-data/
read/` and `bwapi-data/write/` are the BWAPI convention and `SaveGameResultsToFile`
already creates both.

## The measurement that decides it

`ConnectivityRegions` on a 128x128 map (TauCross-sized), 10 runs after warm-up:

```
regions=1
avg_us=1052   best_us=665      <- 0.7-1.1 ms
```

A game frame is ~42 ms. So the whole computation costs **under 3% of one frame**, once
per game - and it is currently paid once because `ReachableTiles` caches the resolved
`Reachability` for the game (which is what makes it one-off rather than per-call).

Cache file size for comparison: 64 KB as ints, 16 KB as bytes for a 128x128 map.

## Why it is correct to cache (so the idea is not wrong in principle)

Walkability is **static terrain**, not game state: `EngineWalkabilityGrid.isWalkable`
asks the engine's terrain query, which does not change during a match. So a cached
analysis for a map is valid for every game on that map - this is not one of those caches
that can go stale mid-game. The idea is sound; the question is only whether it pays.

## Recommendation: not yet, and here is what would change that

**Against, now:**

- **The cost is ~0.7 ms, once per game.** Writing and reading a file - with directory
  creation, a version/format guard, a checksum or size check, and the failure paths for
  a read-only or missing directory - is *more* code and more failure modes than the thing
  it saves. That trade is the wrong way round at this size.
- **The directory is machine-local and outside the repo** (`bwapi-data/`, git-ignored
  per `GameExecution.md` #3). A cache there is invisible to CI, absent in a fresh
  checkout, and its absence must be handled anyway - so the "fast path" is a special
  case, not the normal one.
- **A cache adds a correctness question that does not exist today.** Today the answer is
  computed from the live map, so it cannot be stale by construction. With a file there is
  a format, a map-identity key, and a staleness question - and this codebase's history is
  largely made of stale caches answering confidently (`_AI/NOTES.md` "Arch cache
  credentials", `Stage F — cache purge`, `BUGS.md` B-25). Adding one on purpose needs a
  better reason than 0.7 ms.

**In favour, and what would flip the recommendation:**

- **If the analysis grows.** The moment `ConnectivityRegions` is joined by real path
  distances, choke routing or per-choke analysis (stage 5, `TilePath`), the cost stops
  being sub-millisecond and the cache starts to pay. **That is the trigger to revisit**,
  not a date.
- **If it becomes useful across runs.** A cached analysis is inspectable - a human can
  look at which areas a map has, and a test can compare two maps. That is a debugging
  argument, not a performance one, and it is a legitimate reason to build it *later*.

## What to build if the trigger is hit

- Key: the map file name **plus a format version**, in `bwapi-data/read/ATA/`. The
  directory is created if missing (`AFile.createDirectory`, as `SaveGameResultsToFile`
  does), and a missing file is simply "compute it".
- Format: bytes, not ints (16 KB vs 64 KB), with the region id per tile.
- **A guard test**, in the spirit of `UnitStatsTableTest`: a cached file and a freshly
  computed grid for the same synthetic map must agree; a file with the wrong version or
  the wrong dimensions must be ignored rather than trusted.
- The cache must be a **pure optimisation**: with the file deleted the bot must behave
  identically, and a test should pin that (delete, recompute, same answer).

## Not decided here

The owner asked whether it makes sense; this records that it makes sense in principle and
does not pay yet, with the number that supports that. If it is wanted anyway - for the
debugging value, which is real - that is a legitimate call and the "what to build"
section above is the shape.
