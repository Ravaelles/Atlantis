# LOCAL-STARCRAFT.md — the Wine desktop (real StarCraft) and OpenBW pointers

Status 2026-10-18: condensed from the 2026-10-05/06 investigation log. The
OpenBW attach archaeology moved out (recipe: `_AI/PLAN-OPENBW.md`; traps:
`_AI/CHALLENGES/OpenBW.md`); superseded BWEM speculation deleted outright
(BWEM initialises fine: `MAP_ANALYSIS areas=21 chokes=42`). What stays
here is Wine-only knowledge that exists nowhere else.

## The Wine desktop (real StarCraft), measured 2026-10-06

This is the `GAME_LAUNCHER=WINE` backend, the Linux twin of the Windows F5
workflow. It is separate from headless OpenBW. **Owner-run** (CONVENTIONS
§14); the model never starts it.

**How to run (the whole thing, one command):**

```
cd /sc-ai/Atlantis && bash scripts/run-wine-full.sh            # default map
bash scripts/run-wine-full.sh "ZealDrag_v_Lings.scm"           # any map name
```

It kills leftovers, rebuilds the jar, starts the client (Windows JRE under
Wine, `-Dos.name=Windows 10`), starts the game, watches `WINE_LOG` for
`HELLO_ATLANTIS`. The game window stays up on success — stop it with
`pkill -x StarCraft.exe; pkill -9 Chaoslauncher; wineserver -k`
(`Chaoslauncher.exe` is 17 chars, past the 15-char `pkill -x` comm limit —
see `_AI/CHALLENGES/Wine.md`).

**Fast unattended mode (owner 2026-10-19; explicit env files since
2026-10-19, never shared-ENV edits):** pass `--env=` with a file
carrying `WINE_SUPER_FAST_MODE=1` (speed 0, frameskip 1000, GUI off,
no slowdowns - the key-9/key-0 shape, no keypresses) plus the usual
debug horizon (`FORCE_END_AT_SUPPLY=50` - game ends once our supply
hits it; defects show by supply 25-30, longer runs only when a hunt
needs them). Ready-made: `--env=bwapi-data/AI/ENV-WINE-FAST`.
Without `--env` the game runs watchable (normal speed, GUI on).
Scripts never write to shared ENV files (owner: per-run flags leaked
into watched IDE games, so the borrow-and-restore mechanism is gone).
The Wine audio backends stay muted per run (missing device hangs
startup). Never edit a
running `run-wine-full.sh`: bash parses it as it executes, so an edit
mid-run kills the teardown with a syntax error (measured 2026-10-19 -
the game played on, the logs were collected by hand). Contained window
via `WINE_VIRTUAL_DESKTOP=WxH` (opt-in until injection through it is
proven).

**Who may run Wine games (standing rule, owner 2026-10-18):**
CONVENTIONS §14 still gates this - no Wine/StarCraft launch unless the
owner asks for exactly that. Standing clarification: Wine games are
worth running for now, but kept to the minimum - only when a hunt or a
fix proof needs the real engine/opponent (OpenBW spawns none, NEXT
#94), or on the owner's explicit "you may run Wine now". Procedure and
post-run checks: `_AI/CHALLENGES/GameExecution.md` §8.

**Exit at supply 50 is the default because the opening is where the
defects live (owner 2026-10-18):** the standing war is excessive
Pylons - duplicate same-need orders land together and the game slides
from e.g. 30/48 toward 38/65: free supply ballooning while the bank
hoards, with no own losses to explain it (i.e. not a post-battle
rebuild). That shape IS the wrong state, and it is visible by supply
25-30 - so the vast majority of Wine games end at Supply Used = 50 via
`FORCE_END_AT_SUPPLY`. It catches practically every defect under work
(supply duplicates/blocks, power gating, siting, builder stalls) at a
fraction of the run cost. Longer games only when the hunt needs them
(late tech, #71/#85 windows, real fights). Duplicate-class detail:
NEXT #144 / `work-orders/WO-144-supply-single-author.md`.

**Why it cannot be started from the IDE the way Windows was:** the bot JVM
must be a Windows process (under Wine with the Windows JRE) or its JBWAPI
never sees the BWAPI server's shared memory. IntelliJ runs the Linux JVM
(POSIX backend, `/dev/shm` — a namespace the Wine-side server never
populates), so it loops on `Game table mapping not found` forever.
`run-wine-full.sh` is the replacement for F5.

## Java bot attaching to Wine StarCraft — the real mechanism (2026-10-06)

Three JVM variants were tested against a live Wine StarCraft: native Linux
JVM, Linux JVM under Wine, Windows JRE under Wine. All three looped on
`Game table mapping not found` — none ever saw the server's table.

Root cause in the vendored jar: two connection backends,
`ClientConnectionPosix` (shm_open, `/dev/shm`) and `ClientConnectionW32`
(Kernel32 `OpenFileMapping`), picked from `os.name`. On Linux the POSIX
backend is chosen — but BWAPI under Wine creates Windows named sections
via wineserver, so `/dev/shm` stays empty (measured during a live game).

**The working override: `-Dos.name=Windows 10`.** The W32 backend is then
selected and the game table becomes visible. Full recipe, all pieces
required:

- `-Dos.name=Windows 10` — selects `ClientConnectionW32`.
- Windows JRE under Wine (`drive_c/Java`, Temurin 8 x64) — the client must
  be a Windows process for Win32 IPC.
- `GAME_LAUNCHER=WINECLIENT` in `bwapi-data/AI/ENV` — the Wine-side JVM
  must not run the Linux launcher; it only picks the map and waits.
- **Client first, then the game.** With the game up first the client
  attaches to an already-finished match (`Connection successful` ->
  `failed, disconnecting`); with the client first, the server finds it in
  the game table as soon as the match starts.

## Window solution: W-MODE doublesize (owner-measured 2026-10-06)

W-MODE **enabled**, doublesize on (`wmode.ini DblSizeMode=1`), **no Wine
virtual desktop** (registry `Desktop` value deleted). Both on = the game
ends up invisible in the taskbar; W-MODE off = unusably small 640x480.
`bwapi.ini [window] windowed = ON` puts the game window far off-screen.
Wine has no scaler (API layer, not image zoom — `LogPixels` does nothing
for the game) and no virtual-desktop position option (`wmctrl` on the
window titled `<name> - Wine desktop` is the lever). Position/size levers
are `wmode.ini` keys (`WindowClientX/Y`, `WindowClientXDblSized/YDblSized`);
ALT+F9 toggles doublesize at runtime. The BWAPI injector must stay
enabled — without it there is no bot.

Registry pattern: `wine reg add ... /f`, query with `wine reg query`;
values reach `user.reg` only when wineserver flushes.

## Process and code traps (Wine-side, each measured once)

- **Long-running game processes need `setsid ... < /dev/null &`** with
  redirected output — a terminal session reaching its tool timeout kills
  the game with it (several false "StarCraft died" observations).
- **`Runtime.exec(String)` word-splits**: shell builtins (`eval`) or
  redirections need a vector: `{"sh", "-c", script}`.
- **`taskkill` does not exist on Linux**: both kill paths answer the Wine
  backend with `pkill -9 -x` (Windows path untouched).
- **`GameSpeed.changeSpeedTo` needed the null guard itself**: the keyboard
  path skipped the `game() != null` guard `changeSpeed()` has — pressing a
  key before BWAPI attached threw NPE on the hook thread.
- **Two JVMs, one keyboard:** the bot's Wine JVM sees only Wine-window keys;
  the Linux supervisor sees every key but has no BWAPI — so it captures
  and forwards through `out/wine/keys.txt` (KeyRelay). New shortcuts go in
  `AKeyboard.dispatchKeyCode`, the single entry both feed. Detail:
  `_AI/CHALLENGES/Keyboard.md`.
- **Map names need exact paths**: `--map=<name>` in a subfolder never
  starts the game; `ActiveMap.activeMapPath()` resolves bare names under
  `maps/ums` and `maps/sscai` first.

## Economy footnote (fixed 2026-10-05, kept because the fallback is in-tree)

OpenBW refuses JBWAPI `gather()` client-side (`canGather` → `hasPath`
gate); `BwapiOrderSink.gather` falls back to `rightClick` on minerals/gas,
which the engine accepts (`MoveToMinerals`, bank 50 → 226 over 1200
frames in the probe). On real StarCraft `gather` succeeds and the
fallback never runs. Companion traps from the same session: ~30 s delay
between server start and first client frame is normal (wait it out);
`bwapi.Unit.getDistance(Unit)` NPEs on mineral fields in OpenBW (use
`getDistance(Position)`); Java must be Corretto 8 (bundled junixsocket
breaks on 17).
