# Project Conventions

Normative for all current and future work, including AI assistants.
English-only (see §1). Section numbers are stable: never renumber,
never reuse a deleted number - other documents cite "CONVENTIONS §N".
(There is no §4; the number stays retired.)

## 1. Language rule

- Code, documentation, tools, scripts, commit messages, comments:
  English-only. No localized strings in identifiers, logs, or errors.
- Chat answers use the language of the user's message: Polish message
  → Polish answer, English → English.

## 2. Architecture

- Clean architecture is mandatory. All changes follow SOLID, above all
  **Single Responsibility**: one reason to change per unit.
- Backend selection behind a Strategy interface
  (`GameLauncher` / `GameLauncherFactory`), never scattered `if`s.
- Touch only what the task needs; refactoring stays inside that scope.
- Verify by execution: build, run the binary, check outputs.

## 3. AI assistant role: substantive, not agreeable

- The assistant is a technical advisor, not an order-taker. Say so
  directly when a proposal is wrong or weak: consequence, alternative,
  trade-off. Bare "bad idea" is not enough.
- Mark **evidence** (verified in code or by execution) vs **opinion**.
  Never silently comply with a harmful instruction; never trade
  long-term maintainability for short-term convenience.
- If the user overrules an argued objection, implement without
  re-litigating.

## 3a. Never manufacture a result (owner's ruling, 2026-10-10)

- Reading, tracing, testing a hypothesis, disproving one - all progress.
  A turn ending in "still checking X" is complete. Never fill a quiet
  stretch with plausible-sounding content.
- **Never write, quote, or summarise a tool output that was not
  received.** No recalled outputs, no filled-in values.
- Hypotheses may appear mid-message; the turn closes on the verified
  state, wrong readings labelled superseded.
- **Never guess where a check is cheap**: one command/grep/run settles
  it - run it first. State hypotheses with evidence; "X is the cause"
  needs an observed result.
- **Falsify before confirming.** "Engine bug" / "tool lies" claims need
  strong evidence - they divert the whole investigation.
- Withdrawing is cheap: name it, revert (revert commit, no rewrites),
  leave the item open.

## 5. Architecture direction (agreed, normative)

- Target: **modular monolith with a hexagonal core**
  (`DOCS/ARCHITECTURE-CONTEXT-MAP.md`); open items in `_AI/NEXT.md`.
- **Dependencies point inward**: `adapters → application → contexts →
  core`. Six bounded contexts: `Economy`, `Production`, `Combat`,
  `Intelligence`, `Map`, `Scouting`, plus `core`, `application`,
  `adapters`, `bootstrap`. One class, one context.
- Units move to a read model + stateless systems (`UnitSnapshot`,
  `UnitState`, `World`). No new `public` mutable unit state, no new
  static caches, no new `A.*` (God utility) in production code.
- Boundaries enforced by `ArchitectureBoundaryTest`. Never grow the
  frozen baseline in `_AI/architecture/archunit-store/`; fix the
  dependency instead. In the fast loop the scope is advisory
  (`ARCHITECTURE WARNING`, exit 0, owner's ruling 2026-10-11); the
  authoritative gate is `scripts/run-architecture-tests.sh` (strict).
- **VSA is per-unit behavior packs, not the system architecture.
  DDD is strategic only** (contexts + ubiquitous language), no
  aggregates/repositories.
- Large changes proceed incrementally per `_AI/NEXT.md`, bot playable,
  each checked against `DOCS/SOLID-CHECKLIST.md`.

## 6. Commit after every completed work cycle (owner's ruling 2026-10-21)

- One **coherent unit of work** per commit; batch small doc updates with
  the work they describe. Message says **what and why** (documentary
  value is the point). Never mix unrelated work. Never rewrite history.
- The ArchUnit store is versioned on purpose, never `.gitignore`d.
- Never `git push` unless the user explicitly asks.

## 7. TODO tracking in `_AI/NEXT.md` (mandatory)

- Single source of truth for open work; `_AI/NOTES.md` for learnings.
- Stable numbers, never reused/renumbered. Finished item's line is
  **deleted** (git history is the archive); commit says `Closes #<n>`
  with evidence (test result, game-run id, ArchUnit output).
- New items append with the next free number. Items close only when
  **verified by execution**, never on "it compiles".

## 8. Workspace boundary (mandatory)

- Work inside **`/sc-ai`** (stable symlink name - always use it, never
  the resolved target). **`/ravaelles` is off limits**, even read-only;
  never ask for access to it. If something is unreachable, ask *where
  it is*.
- **`/sc-ai/Stardust`, `/sc-ai/StardustDevEnvironment`,
  `/sc-ai/PurpleWave`** may be read AND edited (owner's ruling,
  2026-10-10); their own commit rules apply, never `git push`.
- Scratch: `/tmp/opencode` (throwaway; survivors go into `Atlantis/`).
- **`~/.scbw/games/` read-only** (scbw E2E verdicts only). **`~/.scbw/bots/AtlantisP`
  and `AtlantisT`** hold real files (scbw bind-mounts read-only; hard
  links count as links - a swapped jar once failed every game with `no
  main manifest attribute`). Always rebuild straight into those `AI/`
  folders (`build-bot-jar.sh <that path>`).
- Helper tooling (venvs, packages) is a means, never a deliverable.

## 9. StarCraft facts: source hierarchy (mandatory)

Game numbers come from this list, in order - never memory, never
decompiled archives:
1. Vendored `lib/jbwapi-2.2.0.jar` (production truth; probe it
   directly - `tests/fakes/UnitStatsTable` javadoc has the procedure).
2. `bwapi/bwapi` unit/weapon `TEST_METHOD`s (not the `.dox` docs).
3. `JavaBWAPI/JBWAPI` for Java mappings, not numbers.
- Dynamics (how the game *plays*): `OpenBW/openbw` - trust for
  dynamics, verify against real games, never cite for unit data.
- Every hand-maintained game-data table ships with a **guard test**
  (`UnitStatsTableTest`); unguarded hand tables prove nothing. Guard
  pins beat commit-message claims on mismatch.

## 10. Tests: TDD for key features, not trivia (normative)

- Key features ship with a pinning test, **in the same fix** - "X was
  broken and I fixed it" names its test (`Test: <ClassName>`).
- Trivial code gets no test (getters, logs, plumbing, one-liners); say
  so in the commit message.
- The **mega-test** (one OpenBW game over economy/production/combat/map)
  is the behaviour-coverage goal - worth dozens of unit tests.
- Budget: `scripts/run-tests.sh` stays well under 40 s (§12).

## 11. Challenge log: `_AI/CHALLENGES/` (added 2026-10-06)

- A problem costing 2+ research cycles gets 1-2 lines: fact,
  measurement, fix. No narrative. File named after the system, not bug.

## 11a. READ FIRST before touching games/clients/packaging (owner's ruling, 2026-10-07)

- `OpenBW.md` (client attach), `GameExecution.md` (Wine vs OpenBW
  mixups), `BuildAndLogging.md` (wrong jar, stdout, races). Every past
  failure here disguised itself as a different problem.

## 12. Test runtime budget (owner's ruling, 2026-10-06)

- `scripts/run-tests.sh` = fast inner loop (unit minus `full-tier`,
  advisory architecture, cheap e2e smokes). It **refuses** slow scopes
  (`tests.e2e`, `tests.acceptance`, bare `tests root`) with exit 2.
- Slow scopes are **owner-only** (`scripts/run-full-tests.sh`).

## 13. Command timeout: 120 seconds, everywhere (owner's ruling, 2026-10-09)

- Every command carries a hard 120 s timeout. Past it: stop, shrink the
  work (background + poll, narrower scope), never silently raise.
  Widening needs the owner's explicit permission for that run.
- A killed command's result must not be claimed.

## 14. Game runs: headless background Wine via `scripts/atlantis.sh` (allowance 2026-10-22)

- Headless only, background only, one game at a time (flock mutex).
  Wine is the default vehicle (faster, more stable than OpenBW).
- Wine and OpenBW setups stay separate; never start the OpenBW bot
  from the Wine directory (it silently opens a real game).
- Single Wine games are noisy - one result never decides. Small
  batches; timeouts are inconclusive (§17). A running game never
  blocks other verifiable work.

## 15. Game runs and client attachment: hard rules (owner's ruling, 2026-10-07)

- One command owns the whole lifecycle (host, client, teardown);
  `scripts/run-openbw-e2e.sh` is the only entry point.
- Never trust a PID you did not just observe; `BWAPILauncher` forks.
- Clear stale state before hosting (`pkill -9 -x BWAPILauncher`, shm +
  socket files); a dead host's PID is indistinguishable from a live one.
- **`pkill -x`, never `pkill -f`** (matches the calling shell → suicide).
- Split layers before debugging attach failures (bare-classpath socket
  probe vs packed jar). A jar that cannot attach is a build failure.

## 16. Java version: always 1.8 (owner's ruling; restated 2026-10-08)

- Everywhere: game jar, IDE SDK, every `javac`, every tool JDK. No
  upgrade plan - do not propose one.
- Every compile passes `--release 8` (class major 52). Guard:
  `tests.architecture.Java8BytecodeTest`. One forgotten flag = dead
  keyboard / never-attaching bot, not a compile error.

## 17. OpenBW simulation hard limit (owner's ruling, 2026-10-09; 150 s since 2026-10-15)

- `TIMEOUT_SECONDS=150` wall-clock + self-ending game at 20 game
  minutes (`INGAME_TIME=60*20`); whichever hits first ends the run.
  Wrap the runner in outer `timeout 150`. Timed-out = **inconclusive**.
- Run only through `scripts/run-openbw-e2e.sh`.

## 18. Unit-behaviour debugging: manager history first (owner's ruling, 2026-10-09)

- Inspect `unit.managerLogs()` before inferring from state or
  reordering managers. Read the focused frame window only.

## 19. Work rhythm: small steps, one-sentence next step (owner's ruling, 2026-10-12)

- Name the next action in one sentence, do it, report in one sentence,
  name the next. Long silent stretches are not acceptable.

## 20. Unfinished work is a commit or a NEXT item, never a prose file (owner's ruling, 2026-10-14)

- No "status" / "in progress" prose files under any name (precedents:
  `STATUS.md`, `__IN-PROGRESS.md` - deleted). They rot; nobody owns
  cleanup. A commit is timestamped evidence, a NEXT item has a number
  a commit can close. Neither → not started.

## 21. Domain knowledge in domain packages, never machinery (owner's ruling, 2026-10-15)

- Game meaning (what/when/in what order/at what price) lives in domain
  packages (`decisions.goals.protoss`, race strategies), never in
  generic algorithm packages (`decisions.board`). A tuning change is a
  domain diff. Check: `grep -rn '^import atlantis' <generic-package>`
  shows only `java.*` - a domain import there is a boundary violation.
- Vocabularies are siblings of the machinery, never children
  (`goals.protoss` next to `board`, never under it).
- No new `Decision` usages: new decision logic goes through the goal
  board (49 files tracked to zero before the type dies).

## 22. Test tiers: tag every test, run the smallest subset (owner's ruling, 2026-10-20)

- Every unit test carries a `@Tag` area (`production`, `combat`,
  `placement`, `map`, `infra`) plus `@Tag("full-tier")` when full-tier
  only. New tests default to the fast loop; widening needs the settled
  + slow justification written down. Detail: `DOCS/TESTING.md`.

## 23. Commanders speak the domain, never the machinery (owner's ruling, 2026-10-22)

- Commanders **delegate**: one public entry (`act`), one private step
  per domain move. The how lives in the step's own class. A commander
  that computes is a manager in disguise - split it.
- Names use StarCraft's ubiquitous language (retreat, formation, kite,
  regroup, breach). Never machinery words (`order`, `process`,
  `manager`, `strategy-tag`). Framework overrides keep framework names
  (`handle`, `subcommanders`) - bodies obey the rules anyway.
- Per-unit vocabulary lives only in `atlantis.battle.Behavior`
  (`KITE`, `HOLD_FORMATION`, ...); `Actions` is the order (move/attack),
  `Behavior` the reason. Tag strings are frozen journal spellings.
- Table-of-contents test: a non-programmer retells the battle story
  from `handle()` + `act()` without opening an implementation.
- Zero machinery in commander bodies: no engine order types, no
  journal plumbing, no logging/twin assembly, no frame counting, no
  cross-concept conditionals (the gate lives with the concept).

## 24. Readability by extraction (owner's ruling, 2026-10-22)

- One level of abstraction per method. Compound conditions, inline
  best-tracking loops, multi-step computations get extracted - never
  left inline with an explaining comment. Density is not elegance:
  prefer a named local, then a named method, then a dedicated class.
- Extractions never change behavior: pinning tests stay green
  untouched. Applies to tests and scripts too.
- Blank lines around returns (enforced by owner-run checkstyle,
  auto-fix `--apply`): glued guard returns stay glued; a blank line
  separates the last guard (or any code) from the final `return`.

## 25. Checkstyle is owner-run; the assistant never runs it (owner's ruling, 2026-10-22)

- The assistant **never runs `scripts/checkstyle.sh`** - not to verify,
  not to count, not to auto-fix. Pre-commit verification is the test
  suite; checkstyle output is never part of the assistant's turn.

## 26. The backlog is not a database (owner's ruling, 2026-10-23)

- A commit must move the work forward: code, a test, a measured game
  run with archived evidence, or a new verified conclusion with its
  evidence. `_AI/NEXT.md` describes what the commit did; it is never
  the commit's whole content.
- Forbidden: a diff touching only `_AI/NEXT.md` (or only docs) that
  adds no new information - restating the same verdict for the Nth
  game, reformatting, re-shuffling chains, fixing typos in old datum
  lines.
- No raw statistics or test results in `_AI/NEXT.md`. Per-game datum
  rows, frame numbers, supply tables, sweep counts, repeated verdicts
  live in `_AI/WINE-MACRO-DATUMS.md` (one line per game per chain) -
  or nowhere, if no decision depends on them. NEXT.md carries the
  conclusion or the single deciding number (best result on a map,
  current tally, open question). A repeat-verdict game adds its datum
  row to the log and nothing else - no commit until commit-worthy work
  exists.
- Backlog maintenance never justifies a commit alone: it rides inside
  the next work commit, or happens on the owner's explicit order
  (message says so). History is not rewritten: old commits stay.

## 27. Log priority tags (owner's ruling, 2026-10-26)

- Every console line carries a priority tag: `[LOW]` (routine flow),
  `[MED]` (unclassified chatter, the default), `[HIGH]` (friction worth
  filtering), `[CRIT]` (game-breaking). The tag is composed centrally
  in `AConsole` (`println(LogLevel, ...)`), so an untagged call site
  reads `[MED]` - it never reads untagged. Severity lives in one place:
  `LogLevel` (order LOW < MEDIUM < HIGH < CRITICAL) + `LogKey.level()`
  (which families are HIGH/CRITICAL), pinned by `LogLevelTest` +
  `LogKeySeverityTest`.
- Temporary debugging lines use `[TEMP]` (`AConsole.temp`), never a
  level. Grep `TEMP` to find every site; a temporary entry is deleted
  or classified before the session ends, it never graduates silently.
- Event rows carry the same priority as a short `"lvl"` code
  (`LOW`/`MED`/`HIGH`/`CRIT`, always rendered). Per-frame grouping by
  key is a read-time operation, not a write-time one: the twin already
  stores key + frame, `scripts/query-events.py --group KEY` prints
  `KEY:{frame:{params}}`. No per-frame keyed buffering in console code.
- E2E gates fail on CRITICAL only (`assert-openbw-scenario.sh`,
  `EXPECT_NO_CRITICAL`); old `CRITICAL` spellings in retained logs
  keep matching (the gate accepts both).
