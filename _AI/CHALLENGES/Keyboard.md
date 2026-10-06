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
