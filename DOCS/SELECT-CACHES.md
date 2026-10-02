# Inventory: the static caches in `Select` / `BaseSelect`

Measured on 2026-10-03 by parsing every `cache*.get(...)` / `getIfValid(...)`
call in `src/atlantis/units/select/Select.java` and counting the call sites of
the method that owns each key. This is item #4 in `_AI/NEXT.md` - an inventory
only, no code change.

## The caches

| Cache | Declared in | Value type | Cleared by |
|---|---|---|---|
| `Select.cache` | `Select.java:25` | `Selection` | `Select.clearCache()` |
| `Select.cacheInt` | `Select.java:27` | `Integer` | `Select.clearCache()` |
| `Select.cacheUnit` | `Select.java:28` | `AUnit` | `Select.clearCache()` |
| `Select.cacheObject` | `Select.java:26` | `Object` | **not** in `clearCache()` |
| `BaseSelect.cacheList` | `BaseSelect.java:16` | `List<AUnit>` | `Select.clearCache()` |

Two things to know before touching any of them:

- `Select.clearCache()` clears `cache`, `cacheList`, `cacheInt`, `cacheUnit` -
  **not `cacheObject`**. `mainOrAnyBuildingPosition` is therefore never
  invalidated by the usual event-driven clears; it relies purely on its 73-frame
  TTL. That looks like an oversight rather than a decision.
- TTL `0` means "valid until the frame counter moves" (the cache stores
  `A.now() + 0` and the entry is valid while `cachedUntil >= A.now()`), so `0`
  and `microCacheForFrames` (`= 1`) are nearly the same thing: one frame vs two.
  TTL `-1` means "never expires" and is only used by `AUnitType`'s own cache.

## The keys

| `cache` | `our` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.our()` | 91 call sites in 46 files |
| `cache` | `enemy` | 0 | current frame only | `Select.enemy()` | 72 call sites in 44 files |
| `cache` | `enemyFoggedUnits` | 0 | current frame only | `Select.enemyFoggedUnits()` | **nobody** |
| `cache` | `all` | 0 | current frame only | `Select.all()` | 27 call sites in 21 files |
| `cache` | `ourBases` | 0 | current frame only | `Select.ourBases()` | 44 call sites in 36 files |
| `cache` | `ourBasesWithUnfinished` | 1 | current + next frame | `Select.ourBasesWithUnfinished()` | 27 call sites in 23 files |
| `cache` | `ourWorkers` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourWorkers()` | 40 call sites in 28 files |
| `cache` | `allOfType:_<arg>` | 0 | current frame only | `Select.allOfType()` | **nobody** |
| `cache` | `enemyCombatUnits` | 0 | current frame only | `Select.enemyCombatUnits()` | 23 call sites in 18 files |
| `cache` | `enemies:_<arg>` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.enemies()` | 9 call sites in 7 files |
| `cache` | `ourOfType:_<arg>` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourOfType()` | 121 call sites in 77 files |
| `cache` | `ourOfTypeWithUnfinished:_<arg>` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourOfTypeWithUnfinished()` | 12 call sites in 9 files |
| `cache` | `ourOfType:_<arg>` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourOfType()` | 121 call sites in 77 files |
| `cacheInt` | `countOurOfTypes:_<arg>` | 0 | current frame only | `Select.countOurOfTypes()` | **nobody** |
| `cacheInt` | `countOurUnfinishedOfType:_<arg>` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.countOurUnfinishedOfType()` | 1 call sites in 1 files |
| `cache` | `ourOfTypeWithUnfinished:_<arg>` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourWithUnfinished()` | 43 call sites in 27 files |
| `cache` | `ourCombatUnits` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourCombatUnits()` | 63 call sites in 32 files |
| `cache` | `ourWithUnfinished` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourWithUnfinished()` | 43 call sites in 27 files |
| `cache` | `ourWithUnfinishedOfType:_<arg>` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourWithUnfinishedOfType()` | 13 call sites in 10 files |
| `cache` | `ourUnfinished` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourUnfinished()` | 23 call sites in 11 files |
| `cache` | `ourRealUnits` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourRealUnits()` | 15 call sites in 10 files |
| `cache` | `ourUnfinishedRealUnits` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourUnfinishedRealUnits()` | 1 call sites in 1 files |
| `cache` | `enemyRealUnits` | 0 | current frame only | `Select.enemyRealUnits()` | 18 call sites in 11 files |
| `cache` | `enemyRealUnitsWithBuildings` | 0 | current frame only | `Select.enemyRealUnitsWithBuildings()` | 1 call sites in 1 files |
| `cache` | `enemyRealUnits` | 0 | current frame only | `Select.enemyRealUnits()` | 18 call sites in 11 files |
| `cache` | `neutral` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.neutral()` | 5 call sites in 4 files |
| `cache` | `minerals` | 30 | 1 s | `Select.minerals()` | 24 call sites in 17 files |
| `cache` | `mineralsAndGeysers` | 30 | 1 s | `Select.mineralsAndGeysers()` | 5 call sites in 3 files |
| `cache` | `geysers` | 53 | 1.8 s | `Select.geysers()` | 14 call sites in 7 files |
| `cache` | `geysersAndGasBuildings` | 31 | 1 s | `Select.geysersAndGasBuildings()` | 1 call sites in 1 files |
| `cacheUnit` | `main` | 73 | 2.4 s | `Select.main()` | 90 call sites in 56 files |
| `cacheUnit` | `mainOrAnyBuilding` | 73 | 2.4 s | `Select.mainOrAnyBuilding()` | 95 call sites in 70 files |
| `cacheUnit` | `mainOrAnyUnit` | 293 | 9.8 s | `Select.mainOrAnyUnit()` | 4 call sites in 3 files |
| `cacheObject` | `mainOrAnyBuildingPosition` | 73 | 2.4 s | `Select.mainOrAnyBuildingPosition()` | 13 call sites in 12 files |
| `cacheUnit` | `firstBuilding` | 91 | 3 s | `Select.firstBuilding()` | **nobody** |
| `cacheUnit` | `naturalOrMain` | 0 | current frame only | `Select.naturalOrMain()` | 5 call sites in 5 files |
| `cache` | `ourTanks` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourTanks()` | 14 call sites in 9 files |
| `cache` | `ourTanksSieged` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourTanksSieged()` | **nobody** |
| `cache` | `ourTerranInfantry` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourTerranInfantry()` | 2 call sites in 2 files |
| `cache` | `ourTerranInfantryWithoutMedics` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourTerranInfantryWithoutMedics()` | 1 call sites in 1 files |
| `cache` | `ourLarva` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourLarva()` | 3 call sites in 3 files |
| `cache` | `ourBuildingsWithUnfinished` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourBuildingsWithUnfinished()` | 24 call sites in 20 files |
| `cache` | `ourUnfinishedBuildings` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourUnfinishedBuildings()` | 1 call sites in 1 files |
| `cache` | `ourBuildings` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.ourBuildings()` | 33 call sites in 26 files |
| `cache` | `enemyBuildings` | microCacheForFrames | current + next frame (`microCacheForFrames = 1`) | `Select.enemyBuildings()` | 1 call sites in 1 files |
| `cache` | `ourFree:_<arg>` | 0 | current frame only | `Select.ourFree()` | 26 call sites in 19 files |

## What the TTLs cluster into

- **0 / 1 frame (14 keys)** - "current frame only". These are per-frame selects
  over the unit collections. A frame is the natural unit of change here, so this
  is the right granularity and needs no migration.
- **`microCacheForFrames` (24 keys)** - identical to TTL 1, spelled differently
  and via a mutable static field. Pure inconsistency: 24 of the 46 entries could
  be written `1` and the field deleted. First candidate for the cache work in
  #5, because it changes no behaviour.
- **30 / 31 frames (~1 s, 3 keys)** - `minerals`, `mineralsAndGeysers`,
  `geysersAndGasBuildings`. `31` next to `30` with no comment is the kind of
  thing that becomes `30` the moment somebody tidies it, and then a mineral
  patch mined 30.9 s late is missed for one extra frame.
- **53 frames (~1.8 s, 1 key)** - `geysers`, and nothing else. No rationale
  anywhere in the code.
- **73 frames (~2.4 s, 3 keys)** - `main`, `mainOrAnyBuilding`,
  `mainOrAnyBuildingPosition`. `main` is the most-read method in the whole
  codebase (90 call sites) and is cached for 2.4 s, so for that window after
  our main dies every caller gets a dead main. `getIfValid` catches the dead
  unit and recomputes, which is why the accessor is `getIfValid` and not `get`.
- **91 / 293 frames (3 s / 9.8 s, 2 keys)** - `firstBuilding` and
  `mainOrAnyUnit`. The longest TTLs in the file.

So of eight distinct TTL values, four (`30`, `31`, `53`, `91`, plus `293`) have
no stated reason, and the two that do matter most (`main`, `main`) are the ones
with the least documentation. That is the actual finding: this is not a
cache-purge problem, it is a *missing-reason* problem. Deleting the caches (what
#5 and #6 propose) would fix the TTLs by removing them and would cost a
measurable amount of per-frame work; the inventory exists so that decision is
made with the numbers in front of it.

## Five dead entries

`enemyFoggedUnits`, `allOfType`, `countOurOfTypes`, `firstBuilding` and
`ourTanksSieged` have **zero call sites outside `Select.java`**. They are
computed lazily, so they cost nothing until called - but they are four public
methods and one cache entry that exist for nobody. Deleting them is a strictly
behaviour-neutral change and is the natural first commit of the cache work.

## How to regenerate this table

The table is generated, not hand-maintained:

```bash
grep -rn "cache\\.get(" src/atlantis/units/select/Select.java   # then the parser
```

The generator lives outside the repo (it was a one-off script); the counts are
reproducible with `grep -rc "Select.<method>(" src/atlantis src/tests`.
