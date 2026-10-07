# How to run Atlantis on Linux

Two ways to play, one rule that matters more than everything else:

> **The bot runs from a JAR. Whatever writes that JAR must be the only thing
> that writes it, and it must write the path the game loads.**

Two builders writing two jars is how a whole session was spent testing code from
hours earlier (`_AI/CHALLENGES/BuildAndLogging.md`). Read that section once.

---

## Setup (once)

- **StarCraft + Wine** installed; the game lives at `~/.wine/drive_c/sc`
  (`scripts/run-wine-game.sh` creates the links on first run).
- **A Windows JRE under Wine** at `~/.wine/drive_c/Java/bin/java.exe`
  (Temurin 8 — the bot targets **Java 8**, there is no upgrade plan:
  CONVENTIONS §16).
- **ENV**: the templates live next to the live file — copy one of
  `bwapi-data/AI/ENV LOCAL-EXAMPLE` (the everyday local setup) or
  `ENV OPENBW-EXAMPLE` to `bwapi-data/AI/ENV` and set `GAME_LAUNCHER=WINE`.
  The IDE reads `bwapi-data/AI/ENV`; the launcher script owns the live game-side
  ini. `ENV` files are **git-ignored** — a `git checkout` will not restore one.
- **Turn the IntelliJ artifact OFF** (see below). This is not optional.

---

## 1. From IntelliJ ("Run")

**Remove the artifact first:** `File → Project Structure → Artifacts` → delete
the Atlantis jar artifact, and clear any "Build artifact" entry from the Run
Configuration's *Before launch* list.

Why: the IDE artifact cannot produce the same jar as `scripts/build-bot-jar.sh`.
Only the script ships junixsocket 2.10.1 with its native payload, declares
`Multi-Release: true`, strips the old 1.0.x package and stale NAR/maven metadata,
and asserts all of it (`_AI/CHALLENGES/OpenBW.md`). With the artifact enabled,
the IDE writes one jar while the game loads another, and your changes never
reach the game.

**Then just Run `main.Main`.** What happens:

1. `UnixChaosGameLauncher` (a Linux JVM) starts
   `scripts/run-wine-full.sh` with inherited I/O.
2. The script **rebuilds the jar from `src/`**, starts the client under Wine,
   then starts StarCraft via ChaosLauncher, and waits for `HELLO_WORLD`.
3. The bot's output is streamed into the **IntelliJ console** live (and still
   written to `out/wine/client.log`).

`A.println(...)` and `System.out.println(...)` both reach the console — they go
through the same `LogPort` seam. If a print seems missing, `grep -c` it in
`out/wine/client.log` before touching code.

### Switches (environment variables)

| Variable | Default | Meaning |
|---|---|---|
| `JAR_OUT` | `$HOME/.scbw/bots/AtlantisP/AI/Atlantis.jar` | where the jar is built **and** loaded from |
| `BUILD` | `1` | `0` = play the existing jar, do not rebuild |
| `LIVE_OUTPUT` | `1` | `0` = quiet console (log file still written) |

All three are set at the top of `scripts/run-wine-full.sh`.

---

## 2. Standalone (terminal)

```bash
cd /sc-ai/Atlantis
bash scripts/run-wine-full.sh "sscai/(3)TauCross.scx"
```

That is the whole command: it builds the jar, plays one game, prints the log to
your terminal, and exits. Exit code 0 means the bot attached (`HELLO_WORLD`).

Useful variations:

```bash
BUILD=0 bash scripts/run-wine-full.sh "sscai/(3)TauCross.scx"   # keep the jar
LIVE_OUTPUT=0 bash scripts/run-wine-full.sh "maps/ums/x.scx"    # quiet
JAR_OUT=/tmp/x.jar bash scripts/run-wine-full.sh                # build elsewhere
```

- `scripts/run-wine-game.sh` only **starts the game** (no bot) — useful when you
  want to attach a client by hand; it prints the command to start one.
- `scripts/run-openbw-e2e.sh` is the **headless** path (no Wine, no window) —
  see `_AI/PLAN-OPENBW.md`.

---

## 3. Build the jar by hand

```bash
bash scripts/build-bot-jar.sh ~/.scbw/bots/AtlantisP/AI/Atlantis.jar
```

Always `--release 8` (inside the script). Never build with the IDE artifact, and
never copy a jar into place by hand — `run-wine-full.sh` rebuilds it anyway.

---

## 4. Tests

```bash
bash scripts/run-tests.sh          # fast: unit + architecture + smoke, < 40 s
bash scripts/run-full-tests.sh     # owner-only: adds acceptance + e2e scenarios
```

Remember: **every file in `src/`, tests included, must compile under
`--release 8`** — one `javac` invocation produces the game jar, so a Java 9+ API
in a test breaks the bot (CONVENTIONS §16).

---

## 5. When something looks wrong

Run this before reading any game logic:

1. `grep -c "<the print>" out/wine/client.log` — is it there at all?
2. `ls -la "$JAR_OUT"` vs `stat -c %y src/<file you changed>` — is the played
   jar newer than the sources?
3. Is the IntelliJ artifact still enabled? It should not be.
4. `bash scripts/run-tests.sh` — does everything still compile under Java 8?
5. Race mismatch? The log prints a loud `RACE MISMATCH` banner when the game
   started us as a different race than `Main.ourRace()` asks for.

More traps, with measurements: `_AI/CHALLENGES/BuildAndLogging.md`,
`_AI/CHALLENGES/GameExecution.md`, `_AI/CHALLENGES/OpenBW.md`.