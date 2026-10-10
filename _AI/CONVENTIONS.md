# Project Conventions

This document is normative for all current and future work in this repository,
including work done by AI assistants. It is written in English, as required
by the language rule below.

## 1. Language rule

- 100% of code, documentation, tools, scripts, commit messages, and comments
  must be in English. This includes this document.
- Identifiers, log messages, error strings, and user-facing output are
  English-only. No localized strings in code.
- Model-human communication (chat answers, explanations, questions) must be
  in the language of the user's message. For example: a message written in
  Polish gets a Polish answer; a message written in English gets an English
  answer.

## 2. Architecture

- Maintaining a clean architecture is a hard requirement, not a nice-to-have.
- All changes must follow SOLID principles, in particular the
  **Single Responsibility Principle**: every unit (class, function, module,
  test binary) has exactly one reason to change.
- Consequences applied in this repo:
  - One test runner per opponent family (`tests-steamhammer`,
    `tests-locutus`) instead of one binary depending on mutually
    conflicting bot implementations.
  - Backend selection behind a Strategy interface
    (`GameLauncher` / `GameLauncherFactory`), never scattered `if`-branches.
  - Shared harness code (`test_common`: game loop, maps) depends on
    abstractions (AIModule factories), not on concrete bots.
- Do not modify unrelated parts of the codebase. Refactoring is allowed only
  within the scope of what the current task touches.
- Verify behavior by execution whenever reasonable: build, run the relevant
  binary, and check outputs before declaring work done.

## 3. AI assistant role: substantive, skeptical, not agreeable

- The assistant is a **substantive technical advisor**, not an order-taker and
  not a cheerleader. Its job is the quality of the design, not the satisfaction
  of the person asking.
- When the user proposes something the assistant believes is wrong, weak,
  over-engineered, or a poor fit for this project, the assistant **must say so
  directly and explain why**. Agreement is not the default.
- The assistant must distinguish clearly between:
  - **evidence** it has verified in the code or by execution, and
  - **opinion / inference**, which it must label as such.
- The assistant must push back on proposals that trade long-term
  maintainability for short-term convenience, and must not silently comply with
  an instruction it judges harmful to the project.
- Disagreement must be specific and actionable: name the concrete consequence,
  the alternative, and the trade-off. "I think this is a bad idea" is not an
  acceptable response on its own.
- If the user overrules a well-argued objection after hearing it, the assistant
  proceeds and implements the user's decision without re-litigating it.

## 4. Incoming-message notification (owner's ruling, 2026-10-10)

- `/home/ping.sh` signals **an incoming message from the assistant**. The
  assistant runs it **exactly once, at the end of every turn** in which it sends
  the owner a message - whether that message is a completed-work summary, an
  intermediate status, a partial answer or a question that still awaits a reply.
- The ping is a **new-message indicator, not a "done" indicator**. The sound means
  "the assistant has answered, look at the screen", never "the project is
  finished". Do not use the completion wording for it.
- Do not ping more than once per turn, and never mid-turn: one message, one ping,
  at the end.
- Superseded rule (kept for the history): the previous text made the ping a pure
  "I am completely done" signal, run once per session only when everything was
  finished and verified. The owner replaced it on 2026-10-10 because the ping is
  wanted on every incoming message.
- Commit messages and summaries still do not need any ping of their own beyond the
  one that closes their turn.

## 5. Architecture direction (agreed, normative)

- Target: a **modular monolith with a hexagonal core**. Full details in
  `DOCS/ARCHITECTURE-CONTEXT-MAP.md`; the canonical execution plan is
  `_AI/REVIEW.md` §16 (stages A–J), and open items are tracked in
  `_AI/NEXT.md`.
- **Dependencies point inward only**: `adapters → application → contexts →
  core`. The core must not depend on contexts or adapters; contexts may depend
  on the core and only on the **Published API** of other contexts.
- **Six bounded contexts**: `Economy`, `Production`, `Combat`,
  `Intelligence`, `Map`, `Scouting`, plus `core`, `application`, `adapters`,
  `bootstrap`. Every class belongs to exactly one.
- **Units move to a read model + stateless systems**: `UnitSnapshot`,
  `UnitState`, `World`. Do not add new `public` mutable unit state fields, new
  static caches, or new `A.*` (God utility) usage in production code.
- **Boundaries are enforced mechanically** by
  `src/tests/architecture/ArchitectureBoundaryTest.java`. Never grow the frozen
  baseline in `_AI/architecture/archunit-store/`; fix the dependency instead.
- **VSA is not the system architecture** (only per-unit behavior packs).
  **DDD is strategic only** (bounded contexts + ubiquitous language), not
  tactical (no aggregates/repositories).
- Large changes follow the stages in `_AI/REVIEW.md` §16, incrementally, and
  must keep the bot playable.
- Every change is also checked against `DOCS/SOLID-CHECKLIST.md`, which states
  the five principles as measurable rules for this repository (leaf utilities
  must not decide policy, new interfaces start narrow, the ArchUnit store must
  shrink or stay unchanged) plus the per-commit review gate.

## 6. Commit after every completed work cycle (mandatory)

- After each **completed cycle of work** (a finished task or stage from
  `_AI/REVIEW.md` §16), commit the changes before starting the next cycle.
- One logical change per commit. Do not mix unrelated changes in one commit.
- Write a concise, English commit message that explains the **why**, not just
  the what.
- The architecture baseline in `_AI/architecture/archunit-store/` is versioned
  on purpose (see its `README.md`). It is **not** a build artifact and must
  **not** be added to `.gitignore`.
- Never `git push` unless the user explicitly asks.

## 7. TODO tracking in `_AI/NEXT.md` (mandatory)

- `_AI/NEXT.md` is the single source of truth for open work. `_AI/REVIEW.md`
  keeps the stage narrative, `_AI/NOTES.md` keeps operational learnings.
- Every item carries a **stable number** (`#1`, `#2`, ...). Numbers are never
  reused and never renumbered, so old commits and chat answers stay
  interpretable. The list is expected to run to hundreds of items over time.
- When an item is finished, **delete its line from `_AI/NEXT.md`** (no "done"
  section — the git history is the archive) and record the closure in the
  commit message: `Closes #<n>: <what was actually verified>`. Naming a test
  result, a game-run id or the ArchUnit output is required, not optional.
- New items are appended with the next free number.
- Items may be worked in any order, but an item counts as closed only when it
  is verified by execution (build, test run, game run) — not when it merely
  compiles.

## 8. Workspace boundary (mandatory)

- All work stays inside **`/sc-ai`**: `Atlantis/`, `StardustDevEnvironment/`,
  `bots/`, `starcraft/` and anything else in that tree. Reading, searching,
  writing, building and running tests happen there. **`/sc-ai` is the name to
  use** - in commands, in scripts, in documentation and in commit messages -
  because it is stable; it is a symlink, and its target is the user's home
  directory.
- **`/ravaelles` is off limits, and so is asking for it** (added 2026-10-04).
  That path is the user's home directory, not a workspace, so: never search,
  scan, read, list or edit anything rooted there, not even read-only, and not
  even when the goal is "find any data that would help". A `grep -r` or `find`
  rooted there turns a task into a crawl of private files, and whatever it finds
  is not evidence the project agreed to produce. **Do not ask the user for
  access to `/ravaelles`, or to any path under it**, in order to locate a file,
  a log, a replay or a game. If something needed is not reachable from `/sc-ai`
  or from `~/.scbw`, then ask *where it is* - one question is cheap, and a
  crawl of the home directory is not. A tool call that needs a broader root
  than those two is a mistake in the task, not a permission request.
- The only paths outside the workspace that may be used:
  - `/home/ping.sh`, the incoming-message notification of section 4, once per
    turn that ends with a message to the owner;
  - `/tmp/opencode`, the scratch directory the tooling provides, for throwaway
    tooling of the current task (a virtualenv, a downloaded archive, an
    intermediate file). Nothing produced there belongs to the repository;
    anything that has to survive goes into `Atlantis/` and is committed;
  - **`~/.scbw/games/`**, **read-only**, and only the game result directories -
    added 2026-10-04 so the scbw E2E tier of `_AI/IDEA-E2E-TESTS.md` Stage 1 can
    parse real game verdicts. The scope is deliberately narrow: reading
    `GAME_*/result.json`, `GAME_*/*.log` and the replays next to them. Writing
    stays inside the workspace (the runner copies what it wants to keep into
    `Atlantis/_AI/`), and nothing else under `~/.scbw` - `bots/`, `maps/`,
    `bwapi-data/`, `docker/` - is opened, edited or enumerated as a way of
    finding something useful. That boundary is the point: the alternative is a
    grep rooted in the home directory, which is what the rule above forbids.
  - **`~/.scbw/bots/AtlantisP` and `~/.scbw/bots/AtlantisT`**, the two bot
    folders - added 2026-10-04 because `scbw run` resolves opponents only from
    its own bots directory and ours was empty. Narrow by construction: each
    folder holds only `bot.json` (race + `JAVA_MIRROR`), `BWAPI.dll`, an `AI/`
    (`Atlantis.jar`, `build_orders`, machine-local `ENV` copied from the repo
    template) and empty `read/` + `write/`. These are **real files, not
    symlinks**: scbw bind-mounts the bot folder read-only into the container,
    so links escaping it dangle (`Bot not found in '/app/bot/AI/Atlantis.jar'`,
    measured). **Hard links count as links here, not as files** (measured
    2026-10-05): both bot jars were hard-linked to one inode, something swapped
    that inode for a foreign Maven-built jar (`Built-By: aleksabl`, no
    `Main-Class`, tests and junit shipped inside), and every game failed with
    `no main manifest attribute, in /app/sc/bwapi-data/AI/Atlantis.jar` - for
    Protoss and Terran alike, with nothing in the log pointing at the swap.
    "Always newest" therefore means rebuilding straight into those
    `AI/` folders (`build-bot-jar.sh <that path>`, which writes `Main-Class:
    main.Main` into the manifest), not linking to the repo jar. Opponent bots downloaded there by scbw itself (`SscaitBotStorage`)
    are scbw's own cache, not ours - never enumerate or manage them from here.
- A third-party tool installed to help (a package, a virtualenv) is a means, not
  a deliverable: do not add it to the repository and do not let the repository
  depend on it, unless the task is exactly about adding that dependency.

## 9. StarCraft facts: source hierarchy (mandatory)

Game numbers (hit points, shields, ranges, damage, cooldowns, behaviour) come
from the list below, in order. They never come from memory - human or model -
and never from decompiling the game archives.

1. **The vendored `lib/JBWAPI-Rav.jar`.** This is the dataset the bot plays
   real games with, so it is production truth, not a test double. When the jar
   and anyone's memory disagree, the jar wins until a higher source says
   otherwise. Probe it directly instead of quoting it from memory:
   ```
   CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
   javac -cp "$CP" -d /tmp/opencode/probe Probe.java && java -cp "/tmp/opencode/probe:$CP" Probe
   ```
   where `Probe` prints `maxHitPoints/maxShields/isFlyer/groundWeapon/airWeapon`
   for `UnitType` and `maxRange/damageAmount/damageFactor/damageType` for
   `WeaponType`. Full procedure lives in `tests/fakes/UnitStatsTable`'s javadoc.
2. **[bwapi/bwapi](https://github.com/bwapi/bwapi)** - the reference tests
   `bwapi/BWAPILIBTest/unitTypesTest.cpp` and `weaponsTest.cpp` carry verified
   per-type values (that file is 18k lines; read the `TEST_METHOD` for the type
   in question, not the whole file). The `.dox` documentation is secondary.
3. **[JavaBWAPI/JBWAPI](https://github.com/JavaBWAPI/JBWAPI)** - the Java
   binding this project uses; authoritative for how types and weapons map to
   Java, not for the numbers themselves.
- **Simulation dynamics** (how the game *plays*, as opposed to its numbers)
  come from **[OpenBW/openbw](https://github.com/OpenBW/openbw)**: the
  headless engine behind the E2E plan in `_AI/IDEA-E2E-TESTS.md` (scenario
  and sweep tests run on it). It is a reimplementation - trust it for
  dynamics, verify against real games occasionally (the parity games in that
  plan), and never cite it for unit data (that is items 1-2 above).
- **Not sources:** a number recalled from memory ("transcribed by hand") is a
  *hypothesis*, not data. It may enter a test or a table only with an
  independent source from the list above; otherwise the entry stays missing
  and visible (the `UnitStatsTable` pattern: `-1` fallback plus a guard test).
  Decompiling `STARDAT.MPQ`/`BROODAT.MPQ` is not a plan either: measured
  standard header geometry but encrypted tables plus PKWARE-implode sectors
  with no local tooling for either - a resource sink, not a next step.
- **Every hand-maintained game-data table ships with a guard test**, following
  `UnitStatsTableTest`: engine-value pins (a jar swap fails loudly instead of
  drifting), a ban on unjustified entries, a name-resolution check, and an
  installation check. A green suite against an unguarded hand table is not
  evidence of anything.
- Quantitative game-data claims in commit messages ("a tank reaches 5 and 6
  tiles") must match the guard pins. If they do not, the claim is wrong, not
  the pins - this exact inversion happened once and cost a full audit cycle.

## 10. Tests: TDD for key features, not for trivia (normative, added 2026-10-06)

- **Key features are done only when a test proves them.** When something was
  broken and got fixed ("X did not work"), or when a feature decides game
  behaviour, a test is written **after** the fix lands - the test pins the
  behaviour so the same break cannot return silently. This is TDD for the
  critical path: write the test with the fix, not "some day".
- **Trivial code gets no test on purpose.** Getters, log lines, plumbing,
  constants, delegation one-liners - writing tests for these is noise and cost
  with no regression-catching value. The rule is judgement, not dogma: if a
  case cannot plausibly regress in a way that matters, skip it and say so in
  the commit message instead.
- **The mega-test is the goal for behaviour-wide coverage.** A single OpenBW
  game run that exercises most of the bot's logic at once (economy, production,
  combat, map analysis, base/choke data) is expensive - and a single run is
  worth more than dozens of unit tests, because it proves the whole stack
  together on real engine physics. Work towards it (see
  `_AI/IDEA-E2E-TESTS.md` Stage 3), do not replace it with scattered unit
  tests where the real question is "does the whole bot play".
- Concretely, the bar is: every **"X was broken and I fixed it"** commit names
  its test in the message (`Test: <ClassName>`), and a fix without a test is
  not closed until one exists - except when the fix is trivial per rule 2.
- **Test-budget ruling (2026-10-06):** `scripts/run-tests.sh` (the
  model-facing inner loop) must finish well under 40 s. It runs unit +
  architecture + the quick e2e smoke (`QuickEconomySmokeTest`, ~1 s: the
  commander runs 120 stub-world frames and the basics must happen - production
  queue populated, workers alive, nothing throws). The slow scopes
  (acceptance 12 s, e2e scenarios 64 s) are refused there and belong to
  `scripts/run-full-tests.sh`, which only the owner runs. Models must not run
  the full suite.
- The future OpenBW mega-test slots into this same quick tier once it exists:
  a single real-engine run asserting the basics (buildings queued, expansion
  up, no exceptions) is worth more than dozens of unit tests - but it must fit
  the same budget or it becomes an owner-only scope too.

## 11. Challenge log: `_AI/CHALLENGES/` (added 2026-10-06)

- When a problem costs more than one research cycle (more than one prompt of
  investigation), record its key insight in the matching
  `_AI/CHALLENGES/<topic>.md` — Wine.md, ChaosLauncher.md, Bwapi.md, and
  whatever else accumulates. One or two lines per insight, only what would
  have saved the time: the fact, the measurement, the fix. No narrative.
- If no file matches, create one named after the system, not the bug.
- Existing sources stay authoritative: `_AI/LOCAL-STARCRAFT.md` for the full
  recipes, `_AI/NOTES.md` for repo-internal learnings. CHALLENGES is the
  cross-system quick reference — when in doubt, put the one-liner there and
  the details in the specialist file.

### 11a. READ FIRST: the game/process challenges (owner's ruling, 2026-10-07)
**Before touching anything that starts a game, attaches a client to one, or
packs the bot jar, read these files.** They are not optional background
reading: every failure in them cost a full research cycle, and several of them
looked like a different problem than they were (the OpenBW client, for
instance, reported "cannot open socket" while the actual cause was a Java
library that cannot run on Java 9+, several layers away).

- **`_AI/CHALLENGES/OpenBW-API.md`** — **which BWAPI queries answer correctly on
  OpenBW** (measured 2026-10-10). Read this before trusting any engine query:
  every map query (`isWalkable`/`isBuildable`/`isExplored`/`isVisible`) is fine,
  and **`hasPath` is always false** — even from a point to itself — which takes
  `canBuildHere`, `Unit.canBuild` and `Unit.build` with it, so no building can be
  placed through the normal BWAPI call on this engine. Reproduce the table with
  `OPENBW_PROBE=1` (the survey runs in seconds).
- **`_AI/CHALLENGES/OpenBW.md`** — attaching the Java client to the headless
  engine. Four independent blockers that all present as the same symptom
  ("the client does not attach"): an old junixsocket that cannot run on
  Java 9+, a mixed junixsocket package, the native-library packaging rules
  (version, compiler tag, descriptors, `Multi-Release: true`, stale NAR/maven
  metadata), and the server/`game_list` lifetime trap.
- **`_AI/CHALLENGES/GameExecution.md`** — Wine vs OpenBW setups and the
  accidental StarCraft launches: why an "OpenBW" run started a real game
  (the bot directory's `ENV`), `pkill -f` killing the calling shell, and
  git-ignored `ENV` edits that silently vanish.
- **`_AI/CHALLENGES/BuildAndLogging.md`** — the build/IDE/logging traps: the
  game playing a DIFFERENT jar than the one being built (two builders + a
  hardcoded Windows path), "my println does not appear" (the bot is a separate
  process; the prints were in the log 13 524 times), one Java 9+ API in a test
  breaking the game jar, and the two-sources-of-truth race bug. Starts with a
  five-point checklist to run before concluding "the code does not run".

Both files end in rules that are already in §13/§14; the files carry the
measurements, this section is the pointer so nobody has to rediscover them.

## 12. Test runtime budget (owner's ruling, 2026-10-06)

- `scripts/run-tests.sh` is the model-facing inner loop: it must finish well
  under 40 s. Its default scope is `tests.unit` + `tests.architecture`
  (~11 s with the compile, measured). It **refuses** the slow scopes —
  `tests.e2e` (64 s) and `tests.acceptance` (12 s) — and also the bare `tests`
  root package, which would include them. Exit code 2 with a pointer to
  `scripts/run-full-tests.sh`.
- The slow scopes are **owner-only**: `scripts/run-full-tests.sh` runs every
  scope with per-scope timings and is run by the owner manually, not by
  models. `--allow-slow` overrides for a one-off debug run.
- Measured budget (2026-10-06, do not trust these forever — re-measure before
  widening any scope): compile ~8 s, unit 2 s, architecture 1 s, acceptance
  12 s, e2e scenarios 64 s.
- The eventual target is a single OpenBW mega-test covering most of the bot's
  logic in one run (see §10). Until it exists, the stub scenarios stay in the
  owner-only tier: they are the best signal we have, but too slow for the
  inner loop.

## 13. Command timeout: 120 seconds, everywhere (owner's ruling, 2026-10-09)

- **Every command, script and test run carries a hard 120-second (2-minute)
  timeout. No exceptions without the owner's explicit permission for that
  specific run.** This replaces the earlier 360 s rule and applies to everything:
  builds, test suites, OpenBW simulations, `git`, greps, downloads. If a step
  cannot finish in two minutes it is hung, misconfigured, or the wrong approach -
  stop it and change the shape of the work (background + poll, a shorter horizon,
  a narrower scope, or ask the owner).
- **This is a work requirement, not a restriction.** Work that cannot complete in
  two minutes is work that stalls the session: waiting 10-90 minutes has produced
  no progress repeatedly. A time-out is the signal to shrink the step until it
  fits, not to raise the limit.
- Concretely: prefix anything that can block with `timeout 120`. Prefer a
  background operation plus a short poll over a foreground wait. If a legitimate
  step genuinely needs longer, **ask the owner first and name the exact command
  and the reason** - do not silently pass `timeout 360`.
- `scripts/run-tests.sh` is already bounded by its own 40 s budget, so the rule
  bites on builds, game runs, downloads and any script whose runtime is not
  obviously seconds. §17 states the same 120 s as a hard limit for OpenBW
  simulations, where the script itself refuses a larger value.
- When a command is killed by the timeout, say so in the summary and do not
  claim its result. A timed-out run is not evidence of anything except that the
  step needs a different shape.

## 14. Never start StarCraft (owner's ruling, 2026-10-07)

- **Do not launch StarCraft, ChaosLauncher or Wine (directly or via
  `scripts/run-wine-*.sh`) unless the owner asks for exactly that.** They were
  started several times in one session and had to be killed by hand. The owner
  runs real games himself.
- **The engine for E2E work is OpenBW**, headless and in-process: it needs no
  Wine, no game window and no licensed install to drive, and it is what
  `StardustDevEnvironment/` already runs. New E2E work targets OpenBW; the Wine
  path stays available for the owner's own verification, not for model runs.
- **The two setups are separate directories and must stay separate** (this is
  the concrete split, not a promise):
  - **Wine**: `scripts/run-wine-*.sh` → `bots/AtlantisP/AI/` (its `ENV` has
    `GAME_LAUNCHER=WINE`) → real StarCraft 1.16.1 + ChaosLauncher vs Blizzard AI.
  - **OpenBW**: `scripts/run-openbw-e2e.sh` → `bots/AtlantisOpenBW/AI/` (its
    `ENV` has `GAME_LAUNCHER=OPENBW`) → `StardustDevEnvironment/`'s
    `build/bin/BWAPILauncher` hosting a headless game; the bot attaches as a
    BWAPI client.
- **Never start the OpenBW bot from `bots/AtlantisP/AI/`.** That directory's
  `ENV` says `GAME_LAUNCHER=WINE`, so the bot starts StarCraft itself: an
  "OpenBW" run launched from there silently opens a real game (measured
  2026-10-07 - it happened three times). The OpenBW script uses its own bot
  directory for exactly this reason.

## 15. Game runs and client attachment: hard rules (owner's ruling, 2026-10-07)

These are the rules distilled from the OpenBW investigation
(`_AI/CHALLENGES/OpenBW.md`); they exist so the same hours are not spent twice.

- **One command owns the whole lifecycle.** A game host and the client that
  attaches to it must be started, waited for and torn down **in a single
  command**. Starting the host in one terminal call and the client in the next
  loses the host (the session's process group is killed when the call ends),
  and the client then chases a dead PID. `scripts/run-openbw-e2e.sh` is shaped
  exactly this way and is the only supported entry point.
- **Never trust a PID you did not just observe.** `BWAPILauncher` forks, so
  `$!` is not necessarily the PID that owns the transport; compare
  `pgrep -x BWAPILauncher` with the PID in
  `/dev/shm/bwapi_shared_memory_game_list` before concluding anything.
- **Clear stale state before hosting, never after.**
  `pkill -9 -x BWAPILauncher`, then
  `rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*`. A dead host leaves
  its PID in `game_list` and the next client adopts it ("No server proc ID").
  A stale entry is indistinguishable from a live one from the client's side.
- **`pkill -x`, never `pkill -f`.** A `-f` pattern matches the command line of
  the shell running it, so the shell SIGKILLs itself before it can print
  anything - which reads as a mysterious hang, not as a self-kill.
- **When a client will not attach, split the layers before changing anything.**
  A 20-line program that opens the socket with the same library separates
  "is the transport reachable" from "does the client work"; running it against
  a bare classpath and against the packed jar is what located a five-part
  packaging bug that the client log blamed on the socket.
- **A jar that cannot attach is a build failure.** `build-bot-jar.sh` asserts
  the packaged junixsocket is the working one; a silent mispackage is worse
  than a red build, because it surfaces as "the bot never joins the game".

## 16. Java version: always Java 1.8 (owner's ruling; restated 2026-10-08)

- **Always use Java 1.8. Everywhere.** This is a standing rule, not a preference:
  not only the game jar, but the IDE's project/module SDK, the run configuration's
  JRE, every `javac` invocation in `scripts/`, and the JDK a tool is told to use.
  **There is no upgrade plan; do not propose one**, and do not "temporarily" run or
  compile anything with a newer JDK.
- **Every compile of this tree must pass `--release 8`.** That is the mechanism
  that makes the rule enforceable rather than aspirational: it pins the class-file
  version to 52 (Java 8) regardless of which JDK happens to be the machine default.
  It applies to **all** scripts that compile into a directory a Java 8 runtime
  loads - `build-bot-jar.sh`, `run-tests.sh`, `run-full-tests.sh`,
  `run-architecture-tests.sh` - because they all write
  `out/production/Atlantis`, which is exactly the directory the IDE launches
  `main.Main` from.
- **Why "always", and not just "the jar":** the Wine bot JVM and the IDE's
  supervisor JVM are both **Java 8**, while the machine's default `javac` is
  whatever is installed (17 on this machine, measured 2026-10-08). A single
  compile that forgets `--release 8` therefore writes Java 17 classes into the
  directory a Java 8 runtime loads, and the failure is not a compile error - it is
  a dead keyboard or a bot that never attaches. Measured 2026-10-08, JNativeHook's
  dispatch thread:

  ```
  UnsupportedClassVersionError: atlantis/keyboard/KeyRelay has been compiled by a
  more recent version of the Java Runtime (class file version 61.0), this version
  of the Java Runtime only recognizes class file versions up to 52.0
  ```

  The same class of failure had already hit `atlantis.Atlantis` once (see the note
  in `AKeyboard.isBotJvm`). The rule exists so it cannot happen a third time.
- **The guard is `tests.architecture.Java8BytecodeTest`** (in the fast suite): it
  reads the class-file major version of the classes the Java 8 runtimes load
  (`KeyRelay`, `AKeyboard`, `Atlantis`, `main.Main`) and fails if any is not 52.
  A build that cannot load its own classes on the runtime it targets is a build
  failure, and this test says so in seconds instead of leaving it to a game run.
- **Production and the IDE runtimes:**
  - the Wine bot uses Temurin `1.8.0_504` at
    `~/.wine/drive_c/Java/bin/java.exe` (measured 2026-10-07) - the runtime that
    plays real games and tournaments;
  - the IDE project SDK is `corretto-1.8` with `languageLevel="JDK_1_8"`, and the
    `Main` run configuration resolves its JRE from the module SDK - when a run
    fails with a class-file-version error, check the project SDK first, because it
    is machine-local (`.idea/` is git-ignored) and not enforced by the repository;
  - a bare native `java -jar` outside Wine uses whatever the machine default is,
    which is **not** necessarily 8 - a difference between a Wine game and a native
    run may therefore be a JVM difference, not a bug in the bot.
- **Consequence for vendored libraries:** JBWAPI-Rav bundles junixsocket 1.0.x,
  whose `AFUNIXSocket` calls `java.net.Socket.setCreated()` - a method that
  exists in Java 8 and was **removed after it**. On Java 8 the bundled version
  works; on Java 9+ it throws and the client silently fails to attach. That is
  why the OpenBW path needed a junixsocket 2.10.1 override while the Wine path
  never did. A library that "is broken for us" must be checked against **8 and
  whatever newer JVM is in play** before it is changed.
- Keep the override in `lib/` and the build assertions: they are what makes the
  same jar attach on a newer JVM without regressing Java 8.


## 17. OpenBW simulation runtime: 20 s normal, 120 s hard ceiling (owner's ruling;
restated 2026-10-10)

- **Two different limits, and they are not interchangeable:**
  - **20 seconds is the normal budget for a single OpenBW test.** Any individual
    run that is not the mega-test is expected to finish well inside it - a
    building-placement test in particular is a short run and has no reason to
    approach 20 s. A single run that has not finished in 20 s is not "a slow
    test": it is a bot stuck in a loop, a hung host, or a broken path. Stop it
    and diagnose; do not wait longer and do not widen the limit.
  - **120 seconds is the hard ceiling, and only two things may use it:** the
    single OpenBW **mega-test** (`_AI/IDEA-E2E-TESTS.md` Stage 3) and the **full
    end-to-end scenario sweep**. Nothing else gets 120 s, and the ceiling is a
    cap, never a target.
- **A run that hits its limit is inconclusive, not a verdict** - record the
  timeout and inspect the logs. In particular, a non-mega run that reaches 20 s is
  evidence of a defect; treat it as a finding, not as a test to be waited out.
- **Never wait blind.** Do not `sleep <seconds>` to "let a run finish": that
  wastes the whole interval even when the run ends early and it hides a stuck run.
  Poll with a condition and a stop, e.g.
  `for i in $(seq 1 10); do pgrep -x BWAPILauncher >/dev/null || break; sleep 2; done`,
  or start the run in the background and inspect it after its own budget has
  elapsed. The measured cost of the blind-wait habit: most of one session spent in
  `sleep 118` calls against runs that never produced a verdict.
- `TIMEOUT_SECONDS` (120) is set once at the top of `scripts/run-openbw-e2e.sh`;
  the per-test 20 s budget is `RUN_BUDGET_SECONDS` in the same place, and a single
  run is expected to use a fraction of it.
- **The game also ends by itself at 20 game minutes** (`INGAME_TIME=60*20`,
  passed as `FORCE_END_GAME_AFTER_INGAME_SECONDS`). Whichever limit is reached
  first ends the game cleanly - the bot exits itself, so the host outlives the
  client and is torn down by the script.
- Pass an outer `timeout 120` around the complete runner command. Include
  setup time in that limit; if a jar build is needed, build it as its own
  command first, then freshness-check it before hosting the game.
- Do not extend the timeout to get a result. A timed-out run is **inconclusive**,
  not a pass/fail verdict; record the timeout and inspect the logs. Do not claim
  game verification without a completed runner verdict.
- The script refuses a widened `TIMEOUT_SECONDS`, and `Env` re-caps
  `FORCE_END_GAME_AFTER_REAL_SECONDS` at 120 so a hand-edited ENV cannot widen
  it. `OpenBWConfig.TIMEOUT_SECONDS` is the same 120 on the Java side.
- Run OpenBW only through `scripts/run-openbw-e2e.sh` so host/client lifetime and
  cleanup remain one command (see §15). Never launch the host separately to evade
  the 120-second cap. §14 still forbids Wine/StarCraft unless the owner asks for
  exactly that.


## 18. Unit-behaviour debugging: inspect manager history first (owner's ruling, 2026-10-09)

- When diagnosing what a unit did, **inspect `unit.managerLogs()` before inferring behavior from current state or editing manager order**. It records the recent active manager sequence and often identifies the manager that took control or prevented a later manager from running.
- Use the log for the specific unit and frame window under investigation; do not dump every unit's history or add per-frame logging unless the focused history is insufficient. Preserve the output in the test/run evidence when it establishes the cause.
- A manager appearing in the history proves it ran; a later manager missing from the sequence may indicate that an earlier manager handled the unit first. Verify that interpretation against the manager chain and the actual return contract (`Commander` vs `Manager`) before changing ordering.
