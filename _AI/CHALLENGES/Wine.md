# Wine — key challenges (cost more than one research cycle each)

## There is no way to scale or position the game window from Wine
- Wine is an API layer, not an image scaler: the only DPI machinery is
  non-client scaling an app must opt into; StarCraft 1.16.1 renders 640x480
  through DirectDraw and does not. LogPixels does nothing for the game.
- The only real scaling is ChaosLauncher's W-MODE plugin (ALT+F9 toggles
  doubles at runtime; geometry in `wmode.ini`, `DblSizeMode=1`).
- Wine has no option for the virtual desktop's window POSITION either.
  `wmctrl` on the window titled `<name> - Wine desktop` is the lever.
- The virtual desktop and W-MODE are alternatives for the same job: both on
  = the game ends up invisible in the taskbar. One must win (we chose W-MODE).

## Wine delivers keyboard events only to Wine applications
- A JVM running under Wine with `-Dos.name=Windows 10` gets the Windows
  JNativeHook native, which sees only keys typed into Wine windows. Global
  shortcuts must be captured by a Linux-side process (our supervisor JVM) and
  forwarded — hence KeyRelay.

## A native Linux JVM can never be the bot under Wine
- BWAPI inside Wine-side StarCraft uses Windows named sections; `/dev/shm`
  stays empty. JBWAPI picks its connection backend from `os.name`, so the
  client must also run under Wine AND with `-Dos.name=Windows 10` (W32
  backend, OpenFileMapping through wineserver). Three JVM variants were tried;
  only that combination reached the table.

## Environment/process hygiene
- `pkill -x` matches the 15-char kernel comm limit: `Chaoslauncher.exe`
  (17 chars) never matches with -x; use `pkill -9 Chaoslauncher`.
- Long-running game processes need `setsid ... < /dev/null &` with redirected
  output — a terminal session reaching its tool timeout kills the game with it
  and produces false "StarCraft died" observations.
- Running StarCraft bare (without the launcher) switches the X resolution and
  resets HiDPI scaling on exit. Never do it on the user's desktop.
- `Runtime.exec(String)` word-splits: shell builtins (`eval`) in the string
  are executed as programs. Pass a vector: `{sh, -c, script}`.

## Fast unattended Wine (owner 2026-10-19)

- `WINE_FAST=1` (bot ENV): speed 0, frameskip 500, GUI off, no dynamic
  slowdown - the key-9/key-0 shape applied at game start instead of by
  keypress. `FORCE_END_AT_SUPPLY=50` ends the game once our supply
  passes 50 (debug horizon, not a hard limit - longer runs on request).
- Both live in the shared machine-local `bwapi-data/AI/ENV`, so
  `run-wine-full.sh` borrows the file per run (backup → enforce →
  restore on exit, trap included): test flags must never leak into
  watched IDE games. Verify after a model-run game: `grep` the ENV
  for `WINE_FAST` / `FORCE_END_AT_SUPPLY` - any hit means the restore
  failed.
- The twin proof that it works: `WINE_FAST: speed 0, frameskip 500,
  GUI off` in the game log (owner-confirmed visibly faster on the
  desktop).
- The bot JVM under Wine cannot run Unix commands: a bare
  `executeInCommandLine("pkill ...")` / `("wineserver -k")` fails with
  `CreateProcess error=2` on every game exit. Route Wine-side kills
  through `{sh, -c, ...}` (Wine forwards those to the host) and keep
  them silent - nothing left to kill is the normal case.
- `WINEDLLOVERRIDES="winealsa.drv=d;winecoreaudio.drv=d;winepulse.drv=d"`
  mutes a game with no audio device to initialize; without it a missing
  device can hang startup. Script-scoped only (`export` in
  `run-wine-full.sh`) - IDE runs are untouched.
- Never edit `run-wine-full.sh` while it runs: bash parses the file as it
  executes, so an edit lands mid-parse, the teardown dies on a syntax
  error, and the game plus its logs need manual cleanup (measured
  2026-10-19: leftover StarCraft killed by hand, logs collected from
  `bwapi-data/write/` by hand).
- After any model-run Wine game, verify no StarCraft/ChaosLauncher
  survived it (`pgrep -x StarCraft.exe`, `pgrep -f "[C]haoslauncher"` -
  the brackets keep pgrep from matching its own command line).
