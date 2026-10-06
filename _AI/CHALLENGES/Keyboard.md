# Keyboard / Input — key challenges

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

## Tilde/` pause
- Key code 96 (backquote) is not wired in AKeyboard. If the game should pause
  on it, it needs a case there (or route through GameSpeed.pauseModeToggle).
