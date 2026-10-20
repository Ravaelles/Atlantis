# Project Conventions

This document is normative for all current and future work in this repository,
including work done by AI assistants. It is written in English, as required
by the language rule below. Section numbers are stable: never renumber, never
reuse a deleted number - other documents cite them as "CONVENTIONS §N".

## 1. Language rule

- Code, documentation, tools, scripts, commit messages, and comments are
  English-only. No localized strings in identifiers, log messages, error
  strings, or user-facing output.
- Chat answers, explanations, and questions use the language of the user's
  message: a Polish message gets a Polish answer, an English one an English
  answer.

## 2. Architecture

- Clean architecture is a hard requirement, not a nice-to-have. All changes
  follow SOLID, in particular the **Single Responsibility Principle**: every
  unit (class, function, module, test binary) has exactly one reason to change.
- Applied here: backend selection behind a Strategy interface
  (`GameLauncher` / `GameLauncherFactory`), never scattered `if`-branches.
- Do not modify unrelated parts of the codebase; refactoring is allowed only
  within the scope of what the current task touches.
- Verify behavior by execution whenever reasonable: build, run the relevant
  binary, and check outputs before declaring work done.

## 3. AI assistant role: substantive, skeptical, not agreeable

- The assistant is a **substantive technical advisor**, not an order-taker and
  not a cheerleader. Its job is the quality of the design, not the satisfaction
  of the person asking. Agreement is not the default.
- When the user proposes something wrong, weak, over-engineered, or a poor fit,
  the assistant **must say so directly and explain why**: the concrete
  consequence, the alternative, and the trade-off. "I think this is a bad idea"
  is not acceptable on its own.
- Distinguish **evidence** (verified in code or by execution) from
  **opinion / inference**, labelled as such. Never silently comply with an
  instruction judged harmful, and push back on trading long-term
  maintainability for short-term convenience.
- If the user overrules a well-argued objection after hearing it, the assistant
  implements the decision without re-litigating it.

## 3a. Long research is progress; never manufacture a result (owner's ruling, 2026-10-10)

Written after a session in which the assistant produced code, tests, and
documentation on top of tool outputs it had never seen - eighteen commits had
to be withdrawn. The cause was the assumption that every turn must deliver a
finding. "Continue" means keep working, not keep reporting.

- Reading a file, tracing a call chain, testing a hypothesis, or disproving
  one are all progress. A turn ending in "still checking X; no result yet" is
  complete and correct. A stretch without a deliverable is fine while knowledge
  deepens; never fill it with plausible-sounding content.
- **Never write, quote, or summarise a tool output that was not received.**
  Do not "recall" outputs, fill in values, or describe what a file "would"
  say. This rule is not negotiable.
- **Thinking aloud is wanted; only the closing state is a claim** (owner's
  ruling, 2026-10-11). Hypotheses, confirmatory checks, and withdrawn readings
  may appear mid-message, but the turn must close on the verified state, marking
  what is measured and what is still open - wrong readings labelled as
  superseded, not left standing next to the conclusion.
- **Never guess where a check is cheap.** "I think" / "probably" may open an
  investigation, never close one: if a command, grep, `javap`, or one run
  would settle a cause, a number, or a behaviour, run it first. A wrong belief
  costs every step built on it.
- State hypotheses with their evidence ("I suspect X because of Y"); "X is the
  cause" requires an observed result. Anything that would cost days if wrong
  needs proof *before* entering a document, commit message, or status file.
- **Falsify before confirming.** Engine bugs, library defects, "the tool is
  lying" are strong claims needing strong evidence - they divert the whole
  investigation.
- Withdrawing is cheap and expected: name what is withdrawn, revert it (a
  revert commit, not history rewrite), leave the item open. Early admission
  costs a message; late admission costs a session and the owner's trust.
- **Knowledge is the deliverable; a wrong decision is cheap, a wrong belief
  is not** (owner's ruling, 2026-10-10). "I do not know yet, and here is how I
  will find out" beats a confident answer. Never trade a measurement for an
  explanation, and never let "we need to decide" rush a one-command check.
- The recognisable failure: a placement refusal "explained" as a small vision
  disc around the Nexus - invented from an API signature, never checked. One
  `grep` killed it (`checkExplored=false` by default; 1149 invisible tiles
  answered buildable). **A mechanism read off an API signature is a hypothesis,
  not knowledge.**

## 5. Architecture direction (agreed, normative)

- Target: a **modular monolith with a hexagonal core**. Details in
  `DOCS/ARCHITECTURE-CONTEXT-MAP.md`; open items in `_AI/NEXT.md`.
- **Dependencies point inward only**: `adapters → application → contexts →
  core`. The core never depends on contexts or adapters; contexts use only the
  **Published API** of other contexts.
- **Six bounded contexts**: `Economy`, `Production`, `Combat`,
  `Intelligence`, `Map`, `Scouting`, plus `core`, `application`, `adapters`,
  `bootstrap`. Every class belongs to exactly one.
- **Units move to a read model + stateless systems**: `UnitSnapshot`,
  `UnitState`, `World`. No new `public` mutable unit state fields, no new
  static caches, no new `A.*` (God utility) usage in production code.
- **Boundaries are enforced mechanically** by
  `src/tests/architecture/ArchitectureBoundaryTest.java`. Never grow the frozen
  baseline in `_AI/architecture/archunit-store/`; fix the dependency instead.
- **In the fast inner loop the architecture scope is ADVISORY** (owner's ruling,
  2026-10-11): it reports a boundary failure as `ARCHITECTURE WARNING` with a
  zero exit code, after the normal scope's exit code is preserved. The rule
  still fires and names the dependency; it just no longer stops the build.
  **The authoritative gate is `scripts/run-architecture-tests.sh`** (strict).
  Fix the dependency, never grow the store.
- **VSA is not the system architecture** (only per-unit behavior packs).
  **DDD is strategic only** (bounded contexts + ubiquitous language), not
  tactical (no aggregates/repositories).
- Large changes proceed incrementally per `_AI/NEXT.md` and keep the bot
  playable. Every change is also checked against `DOCS/SOLID-CHECKLIST.md`.

## 6. Commit after every completed work cycle (mandatory, owner's ruling 2026-10-21: batch sensibly)

- After each **completed cycle of work**, commit before starting the next one.
- One **coherent unit of work** per commit, not one file or one
  micro-step: batch small commits with a sensible continuity into one
  bigger commit (including doc-only updates that describe the work -
  no doc-only commit chains). Pedantic cleanliness is not the goal
  (solo project); the message is: it must explain, to a future self,
  **what was done and why** - that documentary value is the whole
  point of committing. Never mix genuinely unrelated work.
- The ArchUnit baseline in `_AI/architecture/archunit-store/` is versioned on
  purpose (see its `README.md`): not a build artifact, never `.gitignore` it.
- Never `git push` unless the user explicitly asks.

## 7. TODO tracking in `_AI/NEXT.md` (mandatory)

- `_AI/NEXT.md` is the single source of truth for open work; `_AI/NOTES.md`
  keeps operational learnings.
- Every item has a **stable number**, never reused or renumbered, so old
  commits and answers stay interpretable.
- A finished item's line is **deleted** (no "done" section - git history is the
  archive); the commit says `Closes #<n>` with the evidence (test result,
  game-run id, ArchUnit output - required, not optional). New items append
  with the next free number.
- Items close only when **verified by execution** (build, test, game run) -
  never on "it compiles".

## 8. Workspace boundary (mandatory)

- All work stays inside **`/sc-ai`**. **`/sc-ai` is the name to use** - in
  commands, scripts, docs, and commits - because it is stable; it is a symlink
  and its target is the user's home directory.
- **Three reference projects may be read AND edited** (owner's ruling,
  2026-10-10): **`/sc-ai/Stardust`**, **`/sc-ai/StardustDevEnvironment`**,
  **`/sc-ai/PurpleWave`**. They are separate git repos symlinked into the home
  directory, so `readlink -f` lands outside `/sc-ai` - that does not make them
  off limits. Always address them by `/sc-ai/...`, never by resolved target.
  Editing them is normal work (harness, region code, reference bots); their own
  commit rules still apply - one logical change per commit, never `git push`.
- **`/ravaelles` is off limits, and so is asking for it.** Never search, read,
  list, or edit anything rooted there - not even read-only, not even "to find
  helpful data". A `grep -r` rooted there crawls private files, and whatever it
  finds is not evidence the project agreed to produce. **Never ask the user for
  access to `/ravaelles`** to locate a file, log, replay, or game. If something
  needed is not reachable from `/sc-ai` or `~/.scbw`, ask *where it is*. A tool
  call needing a broader root is a mistake in the task, not a permission request.
- The only paths outside the workspace that may be used:
  - `/tmp/opencode`, the tooling scratch dir, for throwaway artifacts of the
    current task. Nothing produced there belongs to the repo; survivors go into
    `Atlantis/` and are committed.
  - **`~/.scbw/games/`**, **read-only**, game result directories only - so the
    scbw E2E tier (`_AI/IDEA-E2E-TESTS.md`) can parse verdicts:
    `GAME_*/result.json`, `GAME_*/*.log`, adjacent replays. Nothing else under
    `~/.scbw` (`bots/`, `maps/`, `bwapi-data/`, `docker/`) is opened or
    enumerated to "find something useful".
  - **`~/.scbw/bots/AtlantisP` and `~/.scbw/bots/AtlantisT`**, the two bot
    folders - `scbw run` resolves opponents only from its own bots directory.
    Each holds only `bot.json`, `BWAPI.dll`, an `AI/` (`Atlantis.jar`,
    `build_orders`, machine-local `ENV`) and empty `read/` + `write/`. These
    are **real files, not symlinks** (scbw bind-mounts read-only; links
    escaping it dangle), and **hard links count as links**: both jars once
    shared one inode, something swapped it for a foreign Maven jar (no
    `Main-Class`), and every game failed with `no main manifest attribute`
    with nothing in the log pointing at the swap. Always rebuild straight
    into those `AI/` folders (`build-bot-jar.sh <that path>`). Opponent bots
    scbw downloads itself are its cache - never manage them from here.
- A helper tool (package, virtualenv) is a means, not a deliverable: do not add
  it to the repo or depend on it, unless adding it *is* the task.

## 9. StarCraft facts: source hierarchy (mandatory)

Game numbers (HP, shields, ranges, damage, cooldowns, behaviour) come from this
list, in order - never from memory, human or model, and never from decompiling
game archives.

1. **The vendored BWAPI binding, `lib/jbwapi-2.2.0.jar`** - production truth,
  not a test double. Jar beats memory until a higher source says otherwise.
  Probe it directly:
  ```
  CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
  javac -cp "$CP" -d /tmp/opencode/probe Probe.java && java -cp "/tmp/opencode/probe:$CP" Probe
  ```
  printing `maxHitPoints/maxShields/isFlyer/groundWeapon/airWeapon` per
  `UnitType` and `maxRange/damageAmount/damageFactor/damageType` per
  `WeaponType` (full procedure: `tests/fakes/UnitStatsTable` javadoc).
  History: the local fork `lib/JBWAPI-Rav.jar` was removed 2026-10-10 for
  stock 2.2.0 (`UnitType.class` byte-identical, class major 52, bot played on
  OpenBW); `bwapi.Pair.first/second` went private, so the code uses getters.
2. **[bwapi/bwapi](https://github.com/bwapi/bwapi)** - verified per-type values
  in `BWAPILIBTest/unitTypesTest.cpp` and `weaponsTest.cpp` (18k lines; read
  the type's `TEST_METHOD`, not the file). The `.dox` docs are secondary.
3. **[JavaBWAPI/JBWAPI](https://github.com/JavaBWAPI/JBWAPI)** - how types map
  to Java, not the numbers themselves.
- **Simulation dynamics** (how the game *plays*) come from
  **[OpenBW/openbw](https://github.com/OpenBW/openbw)**: a reimplementation -
  trust for dynamics, verify against real games occasionally, never cite for
  unit data.
- **Not sources:** hand-transcribed numbers are hypotheses - they enter a test
  or table only with an independent source above, else the entry stays visibly
  missing (`UnitStatsTable`: `-1` fallback + guard test). Decompiling
  `STARDAT.MPQ`/`BROODAT.MPQ` is not a plan (encrypted tables, PKWARE-implode,
  no local tooling - a resource sink).
- **Every hand-maintained game-data table ships with a guard test**
  (`UnitStatsTableTest`: engine pins, entry ban, name + installation checks).
  A green suite against an unguarded hand table proves nothing.
- Quantitative game-data claims in commit messages must match the guard pins;
  on mismatch the claim is wrong, not the pins.

## 10. Tests: TDD for key features, not for trivia (normative, added 2026-10-06)

- **Key features are done only when a test proves them.** A fix for "X did not
  work", or a feature deciding game behaviour, gets its pinning test **with**
  the fix - never "some day".
- **Trivial code gets no test on purpose** (getters, log lines, plumbing,
  constants, one-liner delegation). If a case cannot plausibly regress in a
  way that matters, skip it and say so in the commit message.
- **The mega-test is the goal for behaviour-wide coverage**: one OpenBW game
  exercising economy, production, combat, map analysis, and base/choke data at
  once is worth dozens of unit tests - it proves the whole stack on real
  engine physics (`_AI/IDEA-E2E-TESTS.md`). Work towards it; do not replace it
  with scattered unit tests where the real question is "does the whole bot play".
- The bar: every **"X was broken and I fixed it"** commit names its test
  (`Test: <ClassName>`); a fix without one stays open unless trivial.
- Budget: `scripts/run-tests.sh` must stay well under 40 s (§12); the OpenBW
  mega-test joins the fast tier only if it fits, else it is owner-only too.

## 11. Challenge log: `_AI/CHALLENGES/` (added 2026-10-06)

- A problem costing more than one research cycle gets its key insight in the
  matching `_AI/CHALLENGES/<topic>.md`: one or two lines per insight - the
  fact, the measurement, the fix. No narrative. No matching file: create one
  named after the system, not the bug.
- `_AI/LOCAL-STARCRAFT.md` stays authoritative for full recipes,
  `_AI/NOTES.md` for repo-internal learnings; CHALLENGES is the cross-system
  quick reference.

### 11a. READ FIRST: the game/process challenges (owner's ruling, 2026-10-07)
**Before touching anything that starts a game, attaches a client, or packs the
bot jar, read these.** Every failure in them cost a full cycle, several
disguised as a different problem:
- **`OpenBW.md`** - client attach to the headless engine (junixsocket vs Java
  9+, native packaging, host registry/`game_list` lifetime).
- **`GameExecution.md`** - Wine vs OpenBW mixups (wrong-bot-dir launches,
  self-killing `pkill -f`, git-ignored `ENV` edits).
- **`BuildAndLogging.md`** - build/IDE/logging traps (wrong jar played,
  separate-process stdout, Java 9+ API in tests, race double-source).

## 12. Test runtime budget (owner's ruling, 2026-10-06)

- `scripts/run-tests.sh` is the model-facing inner loop (well under 40 s):
  `tests.unit` + `tests.architecture` (~11 s with compile). It **refuses**
  the slow scopes - `tests.e2e` (64 s), `tests.acceptance` (12 s), the bare
  `tests` root - with exit 2 pointing at `scripts/run-full-tests.sh`.
- Slow scopes are **owner-only** (`run-full-tests.sh`, per-scope timings);
  `--allow-slow` overrides for a one-off debug run.
- Measured budget, re-measure before widening any scope: compile ~8 s, unit
  2 s, architecture 1 s, acceptance 12 s, e2e 64 s.

## 13. Command timeout: 120 seconds, everywhere (owner's ruling, 2026-10-09)

- **Every command, script, and test run carries a hard 120-second timeout. No
  exceptions without the owner's explicit permission for that specific
  run.** Past 120 s a step is hung, misconfigured, or misshapen - stop it and
  shrink the work (background + poll, shorter horizon, narrower scope, or ask).
- Prefix anything that can block with `timeout 120`. To widen a timeout for a
  legitimate step, **ask first, naming the command and reason** - never
  silently pass `timeout 360`. Waiting 10-90 minutes has repeatedly produced
  nothing; a time-out signals "different shape", not "higher limit".
- A killed command's result must not be claimed: report the timeout, change
  the step. `run-tests.sh` already lives under its own 40 s budget; this rule
  bites builds, game runs, downloads, and anything not obviously seconds.

## 14. Never start StarCraft (owner's ruling, 2026-10-07)

- **Do not launch StarCraft, ChaosLauncher, or Wine (directly or via
  `scripts/run-wine-*.sh`) unless the owner asks for exactly that.** The owner
  runs real games himself.
- **The engine for E2E work is OpenBW** (headless, no Wine/window/license);
  the Wine path is the owner's verification vehicle, not for model runs.
- **The two setups are separate directories and must stay separate:**
  - **Wine**: `scripts/run-wine-*.sh` → `$HOME/.scbw/bots/AtlantisP/AI/`
    (Wine/ChaosLauncher backend) → real StarCraft 1.16.1 + ChaosLauncher.
  - **OpenBW**: `scripts/run-openbw-e2e.sh` → `/sc-ai/BOTS/AtlantisOpenBW/AI/`
    (`GAME_LAUNCHER=OPENBW`) → headless `BWAPILauncher`; the bot attaches as a
    BWAPI client.
- **Never start the OpenBW bot from the Wine bot directory.** Its backend
  starts StarCraft itself: a past "OpenBW" run from there silently opened a
  real game (measured 2026-10-07, three times). The OpenBW script's own bot
  directory exists for exactly this reason.

## 15. Game runs and client attachment: hard rules (owner's ruling, 2026-10-07)

Distilled from the OpenBW investigation (`_AI/CHALLENGES/OpenBW.md`) so the
same hours are not spent twice.

- **One command owns the whole lifecycle.** Host, client, wait, and teardown
  run **in a single command** - a host started in one call dies with its
  process group when the call ends, leaving the client chasing a dead PID.
  `scripts/run-openbw-e2e.sh` is the only supported entry point.
- **Never trust a PID you did not just observe.** `BWAPILauncher` forks;
  compare `pgrep -x BWAPILauncher` against `/dev/shm/bwapi_shared_memory_game_list`
  before concluding anything.
- **Clear stale state before hosting, never after**
  (`pkill -9 -x BWAPILauncher`, then `rm -f /dev/shm/bwapi_shared_memory_*
  /tmp/bwapi_socket_*`): a dead host's PID in `game_list` is indistinguishable
  from a live one ("No server proc ID").
- **`pkill -x`, never `pkill -f`** - a `-f` pattern matches the calling shell's
  own command line, so the shell SIGKILLs itself, reading as a hang.
- **Split the layers before changing anything** when a client will not attach:
  a 20-line socket program against a bare classpath vs the packed jar is what
  located a five-part packaging bug the client log blamed on the socket.
- **A jar that cannot attach is a build failure**: a silent mispackage
  surfacing as "the bot never joins" is worse than a red build.

## 16. Java version: always Java 1.8 (owner's ruling; restated 2026-10-08)

- **Always Java 1.8. Everywhere** - game jar, IDE project/module SDK, run
  configuration JRE, every `javac` in `scripts/`, every JDK a tool is told to
  use. **No upgrade plan; do not propose one**, and do not "temporarily" use a
  newer JDK.
- **Every compile of this tree passes `--release 8`** (class major 52
  regardless of machine default) - including `build-bot-jar.sh`,
  `run-tests.sh`, `run-full-tests.sh`, `run-architecture-tests.sh`, which all
  write `out/production/Atlantis`, the directory the IDE launches `main.Main`
  from. One forgotten flag writes Java 17 classes a Java 8 runtime loads -
  and the failure is not a compile error but a dead keyboard or a bot that
  never attaches (`UnsupportedClassVersionError ... 61.0 ... up to 52.0`,
  measured 2026-10-08; `atlantis.Atlantis` had already failed the same way
  once - never a third time).
- **The guard is `tests.architecture.Java8BytecodeTest`** (fast suite): any
  loaded class not at major 52 fails the build in seconds, not in a game run.
- **Runtimes:** the Wine bot is Temurin `1.8.0_504`; the IDE project SDK is
  `corretto-1.8` (`languageLevel="JDK_1_8"`, machine-local `.idea/`, so check
  the project SDK first on a version error); bare `java -jar` uses the machine
  default, which is **not** necessarily 8 - a Wine-vs-native difference may be
  a JVM difference, not a bot bug.
- **Vendored libraries are checked against 8 *and* the newer JVM in play**
  before changing (detail: `_AI/CHALLENGES/OpenBW.md` packaging rules). Keep
  the `lib/` override and the build assertions: one jar attaches on both.

## 17. OpenBW simulation hard limit (owner's ruling, 2026-10-09; raised to
150 s by the owner 2026-10-15 so capped runs reach further before the kill)

- **`TIMEOUT_SECONDS=150`** wall-clock plus a self-ending game at **20 game
  minutes** (`INGAME_TIME=60*20`, `FORCE_END_GAME_AFTER_INGAME_SECONDS`) -
  whichever hits first ends the run; the bot exits itself so the host
  outlives the client. (General command budget in §13 stays 120 s; this
  section alone was widened, on purpose, for simulation wall time.)
- Wrap the runner in an outer `timeout 150` (setup included; build the jar as
  its own command first). A timed-out run is **inconclusive**, never a verdict.
- The script refuses a widened `TIMEOUT_SECONDS`; `Env` re-caps
  `FORCE_END_GAME_AFTER_REAL_SECONDS` at 150. Run only through
  `scripts/run-openbw-e2e.sh` (§15); §14 still applies.

## 18. Unit-behaviour debugging: inspect manager history first (owner's ruling, 2026-10-09)

- When diagnosing what a unit did, **inspect `unit.managerLogs()` before
  inferring from current state or editing manager order** - it usually names
  the manager that took control or starved a later one.
- Read the specific unit and frame window only; no history dumps, no per-frame
  logging unless the focused history is insufficient. Preserve the output in
  the evidence when it establishes the cause.
- A manager in the history ran; a missing later one may mean an earlier
  manager handled the unit first. Verify against the chain and the return
  contract (`Commander` vs `Manager`) before reordering.

## 19. Work rhythm: small steps, one-sentence next step (owner's ruling, 2026-10-12)

- **Name the next action in one sentence, do it, report the outcome in one
  sentence, name the next action again.** Long silent stretches are not
  acceptable even with a correct final analysis; the owner must see the work's
  direction while it happens.
- Thinking aloud (§3a) counts only in these increments: a long unbroken
  reasoning block does not satisfy this rule.

## 20. Unfinished work is a commit or a NEXT item, never a prose file (owner's ruling, 2026-10-14)

- `_AI/STATUS.md` grew to ~1500 lines duplicating `_AI/NEXT.md` and was
  deleted; the `_AI/__IN-PROGRESS.md` proposal was rejected the same day for
  the same reason. Do not create a replacement ("current work", "session
  status", "in progress") under any name.
- Work that spans sessions must be either **committed** (even partial - on a
  branch, with a message saying what is missing) or a **`NEXT.md` item**
  (stable number, with the next measurement named). If it is neither, it was
  not started.
- A prose file rots because nobody owns its cleanup; a commit is timestamped
  evidence and a NEXT item has a number a commit can close. That is the whole
  difference.

## 21. Domain knowledge lives in domain packages, never in generic machinery (owner's ruling, 2026-10-15)

- Game meaning (what to want, when, in what order, at what price) lives in
  domain packages (`atlantis.decisions.board.goals`, race strategies) and
  never in generic algorithm packages (`atlantis.decisions.board`: ranking,
  snapshots, interfaces). Tuning strategy is a domain diff; changing the
  algorithm never names a game concept.
- Dependency direction is one-way, inward to generic: domain imports the
  machinery, the machinery imports nothing project-specific. A generic
  package importing a domain type is a boundary violation, same class as
  CONVENTIONS §5.
- Measurable check: `grep -rn '^import atlantis' <generic-package>` must
  show only `java.*` (plus its own package). The day it shows a domain
  import, split or revert before committing.
- Sibling, not child (owner's ruling, 2026-10-15): domain vocabularies
  grow without bound (Protoss opening today, Terran/Zerg/midgame
  tomorrow) while the machinery stays fixed - so they are sibling
  packages (`decisions.goals.protoss` next to `decisions.board`), never
  a child of the mechanism. A child package reads as "owned by", and
  the mechanism owns nothing; the contract between them (Goal,
  BoardState) is the shared kernel, everything else splits.
- No new `Decision` usages (owner's ruling, 2026-10-15): new decision
  logic goes through the goal board, not through `TRUE/FALSE/INDIFFERENT`
  chains. Not because the trio is evil - the singletons are defused -
  but because every new usage feeds the system being strangled (51 files,
  49 in prod, measured 2026-10-15, tracked to zero before the class dies). `toBoolean` still
  asserts off in production, so old call sites keep their
  `notIndifferent()` discipline until absorbed.
- New rule, not retroactive cleanup: existing violations elsewhere are
  tracked by ArchUnit (§5), not rewritten to satisfy this section.

## 22. Test tiers: tag every test, run the smallest subset (owner's ruling, 2026-10-20)

- Every unit test carries a JUnit `@Tag` area (`production`,
  `combat`, `placement`, `map`, `infra` - by subject, not fixtures)
  plus `@Tag("full-tier")` when it belongs to the full tier only.
  New tests default to the fast loop; moving one out needs the
  slow-and-settled justification written down (area settled AND the
  test alone costs ~0.3 s or more).
- `scripts/run-tests.sh` runs the fast subset (unit minus
  `full-tier`, advisory architecture, cheap e2e smokes); the full
  tier (`scripts/run-full-tests.sh`) runs everything, and runs at
  the end of every finished feature, not per edit. Detail and the
  area map live in `DOCS/TESTING.md`.
