# Map snapshots: dumper traps

One or two lines per insight: the fact, the measurement, the fix.

- BWAPI `isWalkable` takes walk coordinates, not tiles: the snapshot
  dumper passed tile coords and scrambled the whole walkability grid
  (measured 2026-10-27: live Gateways at [16,47]/[11,50] on tiles the
  dump called 'x'; re-dump with `*4` fixed it, buildable count 6227 ->
  11263). The bot's own `EngineWalkabilityGrid` multiplies by 4 - the
  dumper must mirror the bot, not guess the binding.
