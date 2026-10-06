# ChaosLauncher — key challenges

## Plugins are enabled per-user in the registry, not in files
- `HKCU\Software\Chaoslauncher\PluginsEnabled`, values are the plugin display
  names (`"W-MODE 1.02"`). Values reach `user.reg` only when wineserver
  flushes — query with `wine reg query`, don't read the file.
- The BWAPI injector must stay enabled; switching it off removes the bot.
- `Chaoslauncher.log` (in the ChaosLauncher dir) is the source of truth for
  which plugins loaded and whether ApplyPatch ran per plugin.

## The launcher must be started from the game root
- It looks up `bwapi-data/BWAPI.dll` relative to the game dir; also
  "Run Starcraft on Startup" is what actually starts the game — without it the
  launcher sits in the menu and the bot loops waiting.

## "Already running" single-instance
- A leftover ChaosLauncher makes a new one exit with "Already running" and
  nothing else happens. Kill leftovers (`pkill -9 Chaoslauncher`) before
  starting.
