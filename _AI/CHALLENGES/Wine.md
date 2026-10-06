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
