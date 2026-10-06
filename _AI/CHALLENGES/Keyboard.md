# Keyboard / Input — key challenges

## Key codes are JNativeHook VC_* constants, NOT Windows VK codes
- The codes in `AKeyboard` must come from `com.github.kwhat.jnativehook.keyboard
  .NativeKeyEvent` (vendored jar = source of truth). They look Windows-ish
  (VC_1 = 2, VC_A = 30...) but are the library's own X11-derived table; a code
  guessed from memory is a dead shortcut: the case exists, the key never
  arrives. Measured casualty: tilde wired as 96 (invented) instead of 41
  (VC_BACKQUOTE) - the pause key did nothing until 2026-10-06.
- Guard test: `AKeyboardKeyCodesTest` pins the real constants; extend it when
  wiring new keys, do not type numbers from memory.

## Right vs left Control is one code
- JNativeHook reports both Ctrl keys as VC_CONTROL = 29; there is no separate
  right-control constant in 2.2.1.

## The Wine setup has two JVMs and only one can see global keys
- The bot's JVM (under Wine, -Dos.name=Windows 10) gets the Windows JNativeHook
  native: it sees only keys typed into Wine windows. The Linux supervisor JVM
  sees every key but has no BWAPI. So the supervisor captures keys and hands
  them to the bot through a file (KeyRelay, out/wine/keys.txt); the bot drains
  and executes them. Escape stays with the supervisor (killing is its job).
- Consequence: every new shortcut must be added in AKeyboard.dispatchKeyCode
  (the single entry point both the hook and the relay feed).

## JNativeHook natives are per-OS
- .dll (Windows) and .so (Linux x86_64) must both be in lib/. Which one loads
  follows os.name — under Wine that's the Windows one, by design.

## Right Ctrl has its own code: 3665 (not 29 with a location)
- Live hook output from the owner (2026-10-06): right Ctrl reports
  keyCode=3665, keyLocation=1; left Ctrl reports 29/2. The library's
  VC_* constants do not name 3665, and a keyLocation==3 gate never matched -
  which is why right Ctrl was dead through two attempts. Wire right-side keys
  by their OBSERVED code, and extend the pins in AKeyboardKeyCodesTest.

## Left vs right Ctrl: same keycode; a location-based split does NOT work
- JNativeHook reports both Ctrl keys as VC_CONTROL=29 and exposes
  NativeKeyEvent.getKeyLocation() (2=left, 3=right) - but gating a shortcut on
  location==3 silently killed the RIGHT key: the location reported on X11 for
  the right key is not reliably 3 (measured 2026-10-06: right Ctrl stopped
  pausing entirely with the filter in). Both Ctrl keys pause now, owner's
  call. If a side split is ever needed again, debug what location the right
  key actually reports before filtering.
- KeyRelay still carries "code:location" lines - harmless, and the format is
  ready if a reliable split ever lands.
