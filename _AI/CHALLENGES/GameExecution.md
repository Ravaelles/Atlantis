# Game execution: Wine vs OpenBW, and the accidental StarCraft launches

Setup/run challenges, in the order they cost time. Two separate game setups
exist in this workspace and mixing them is the whole story below.

## The two setups (know which one you are in)

| | Wine | OpenBW |
|---|---|---|
| scripts | `scripts/run-wine-game.sh`, `run-wine-full.sh` | `StardustDevEnvironment/scripts/run-openbw-server.sh`, `Atlantis/scripts/run-openbw-e2e.sh` |
| bot dir | `bots/AtlantisP/AI` | `bots/AtlantisOpenBW/AI` |
| `ENV` | `GAME_LAUNCHER=WINE` | `GAME_LAUNCHER=OPENBW` |
| engine | real StarCraft 1.16.1 under Wine + ChaosLauncher | headless `BWAPILauncher` (OpenBW) |
| who runs it | **the owner only** | models may run it |

## 1. "OpenBW" runs kept opening a real StarCraft game (three times)

- **Symptom:** a headless E2E run brought up StarCraft under Wine and
  ChaosLauncher. Had to be killed by hand.
- **Cause:** the bot reads `ENV` from its **working directory**, and the run was
  started with `cwd=bots/AtlantisP/AI`, whose `ENV` says `GAME_LAUNCHER=WINE`.
  With that ENV the bot starts the game itself. Nothing in the harness did it -
  the C++ side only `execvp`s `java -jar` (single spawn, verified by grep).
- **Fix:** an OpenBW run must use a bot directory whose `ENV` says
  `GAME_LAUNCHER=OPENBW`. `scripts/run-openbw-e2e.sh` creates and uses
  `bots/AtlantisOpenBW/AI` for exactly this reason, and refuses to run if
  `StarCraft.exe` or `Chaoslauncher` is already up.
- **Rule:** CONVENTIONS §14 - never launch StarCraft/ChaosLauncher/Wine;
  OpenBW is the engine for E2E work.

## 2. `pkill -f <pattern>` kills the shell that runs it

- **Symptom:** a command "ended without completion / SIGKILL" and produced no
  output, repeatedly. Looked like a hang.
- **Cause:** `pkill -f BWAPILauncher` (and `pkill -f "java -jar Atlantis"`)
  match the **full command line**, which includes the shell running the pkill
  itself. The shell kills itself before printing anything.
- **Fix:** `pkill -9 -x BWAPILauncher` (exact process name), or exclude self
  (`pgrep -f ... | grep -v $$`). Same trap for any `-f` pattern that appears in
  your own command line.

## 3. `bots/*/AI/ENV` and bot jars are git-ignored - edits vanish silently

- **Symptom:** an `ENV` edit (e.g. `PRODUCTION_V2=DRY_RUN`) was "undone" and
  `git status` showed nothing; a `git checkout` on the file appeared to do
  nothing.
- **Cause:** `ENV` and the jars under `bots/` are ignored, so git neither
  tracks nor restores them. `git checkout` is a no-op, not a revert.
- **Fix:** revert ENV edits by editing the file (or from the `ENV-EXAMPLE`
  template / `bwapi-data/AI/ENV`), never by asking git. Check
  `git check-ignore -v <path>` before trusting git to restore anything there.

## 4. A leftover `BWAPILauncher` poisons the next run

- **Symptom:** the next client attaches to a dead game table and fails
  randomly.
- **Cause:** the server owns the shared-memory game table; if it outlives the
  run, the next client finds a stale one.
- **Fix:** `scripts/run-openbw-e2e.sh` traps `EXIT INT TERM` and kills the
  server plus the bot jar process, so no host outlives the run.

## 5. Wine game run: the map name with a space

- **Symptom:** the game kept loading the wrong/absent map.
- **Cause:** the map file name contains a space (`1a2a 3a Micro 2.scx` vs
  `1a2a3a Micro 2.scx`) and the file on disk did not match the name passed.
- **Fix (owner):** the map was duplicated so both spellings exist. Also note
  CONVENTIONS §8-adjacent lesson: a bare map name living in a subfolder is not
  loadable - the resolution must be a real path.

## 6. Where OpenBW run artefacts land

- `Atlantis/out/openbw/server.log` - the OpenBW host side.
- `Atlantis/out/openbw/bot.log` - the Atlantis client side.
- Not `out/wine/*` - those belong to the Wine runs and must not be mixed in
  when diagnosing a headless run.
