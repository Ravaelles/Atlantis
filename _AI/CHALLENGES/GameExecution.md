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
| who runs it | owner-run, plus model runs with the owner's explicit per-case go-ahead (2026-10-19: Wine games for debugging + proving fixes) | models may run it |

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

- `E2E_LOGS/<run>/server.log` - the OpenBW host side.
- `E2E_LOGS/<run>/E2E_LOG` - the game log (stdout: HELLO, GameSummary,
  build orders, placements).
- `E2E_LOGS/<run>/E2E_DEBUG` - diagnostics (stderr: worker gates, stuck
  workers, refusal breakdowns, stack traces). Split since 2026-10-15:
  the merged file drowned the game log in thousands of diagnostic lines.
- `E2E_LOGS/<run>/E2E_DECISIONS` - the audited production decisions.
- `E2E_LOGS/E2E_LOG`, `E2E_DEBUG`, `E2E_DECISIONS` - stable
  copies of the latest run's triple (Wine has its own triple, below).
- Not `out/wine/*` - those hold the Wine triple (`WINE_LOG`, `WINE_DEBUG`,
  `WINE_DECISIONS`) and must not be mixed in when diagnosing a headless run.

## 7. Never source a runner script

- `bash -n` and `read` inspect; `source script.sh` (even with `--help`)
  EXECUTES it. Measured 2026-10-18: sourcing `run-wine-full.sh` to test
  log-name derivation launched a real Wine game (had to be killed by
  hand). To exercise a shell function, copy it into a throwaway snippet,
  never source the runner.

## 8. Model-run Wine checklist (owner go-ahead required, 2026-10-19)

- Always pass an explicit melee map (`"(3)TauCross.scx"`): the script
  default is a UMS micro scenario (`1a2a3a Micro 2.scx`) that builds
  nothing and teaches nothing (measured: 226 s, idle base).
- The script takes test flags only via `--env=FILE` and never touches
  the shared ENV - `grep SUPER_FAST bwapi-data/AI/ENV` must be empty
  (any hit = a script edited the shared file, IDE games affected).
- After the run: `pgrep -x StarCraft.exe` must be empty and the
  `out/wine/` triple (WINE_LOG/_DEBUG/_DECISIONS) collected; a missing
  triple with a live game means the teardown died mid-script (it parses
  itself as it runs - never edit it mid-run, see Wine.md).
- `out/wine/` is wiped by the next run and by `rm -rf out`: copy logs
  worth keeping to `/tmp/opencode/` (or the live-copy dir) before any
  build/test command.
