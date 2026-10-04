# Code review — changes from the last two days (SOLID + architecture)

Reviewer: GLM. Date: 2026-10-04. Window reviewed: commits `e8962bda`
(2026-10-02 22:31) … `e4a2579c` (2026-10-04 20:58), ~120 commits, 295 files,
+10 382 / −5 349, plus the **uncommitted** `GameClock` work currently sitting in
the working tree (`AGame`, `TimeMoment`, `Cache`, `ConsoleLog`, `ErrorLog`,
`Log`, `AbstractTestWithUnits`, new `atlantis/util/GameClock.java`).

Not re-deriving the whole-system picture here: `_AI/REVIEW.md` (top-down
review) and `DOCS/SOLID-CHECKLIST.md` (normative rules) already exist, and this
review judges the last two days **against** them. Verification: full suite run
during this review — unit 117, architecture 10, acceptance 138, scenarios 6,
all green (EXIT=0).

## What was done well (keep doing this)

- **The port pattern is now a house style, and it is being applied with
  restraint.** `ATech.Source`, `MapTiles.Source`, `ATech`/`MapTiles` + `FakeResearch`/`FakeMapTiles` follow `LogPort`'s
  shape: interface defined next to the consumer, engine adapter as the default,
  fake in `tests.fakes`, zero call-site churn. `Env.isTesting()` count in
  `src/atlantis` is down to 38. Every one of these commits shrank the frozen
  ArchUnit store instead of growing it. This is exactly the checklist's DIP
  rule ("ports only where they earn their keep").
- **Boundary moves are argued in the code.** `CombatEvalScale`'s move to
  `atlantis.units` (`f99d7a0e`) and `CacheKey`'s move beside `Selection`
  (`a2955886`) both justify the direction in javadoc — "the evaluator depends
  on units all day; the other way round is the frozen edge". That is the
  context-map rule made visible at the move site; a reviewer of the diff alone
  can still follow why.
- **Fixes arrive with their mechanism measured and pinned.** B-19
  (`f21dadbf`), B-23 (`a716469c` + the `abc4e8c9` rework), B-22
  (`70e21dd6`), B-2/B-18 (`b54b8df2`/`f99d7a0e`/`b8c71d07`): each has a
  BUGS.md entry with the numbers, a test that pins the behaviour, and
  comments at the decision site. The review-gate question "does a new branch
  have a test" is being answered before I can ask it.
- **Deletions are real.** StarEngine (−1210 lines), `ACachedValue`,
  `Decisions.shouldMakeZerglings`, `Helpers`, `microCacheForFrames`: no callers
  was verified as no callers, not assumed.
- **The `GameClock` WIP is the right shape.** `publish()` mirrors the existing
  `A.now`/`A.s` protocol, the javadoc names the rule it serves, and
  `ErrorLog`'s throttle only starts working *because* of it (`A.seconds()` read
  0 in every stub world — a correctness improvement hiding inside a refactor).
  Ship it; see F-1 for the one caveat.

## F-1 — GameClock WIP: a second clock source that can silently disagree with the first (medium)

Two static clocks now hold the same two ints. The game path and
`AbstractTestWithUnits` keep them in sync by construction; `AbstractWorldCreatingTest.onFrameStart`
— which drives the **acceptance** tier — writes `A.now`/`A.s` without
publishing, so kernel code that reads `GameClock` silently sees the previous
frame there. Frames, not semantics: caches TTL by frame, so a one-frame skew
is invisible. But the window shows exactly how this happens: commit
`e2a4898e` already fixed this class of drift once, by hand, and the new field
recreates the possibility. `A.now` stays write-only — NEXT #18 says a
follow-up deletes it "with the two test setup writes" — so this is a known,
recorded endgame, not an orphan.

**Suggestion (small, do it in the same commit):** route all four writers
through one static helper (e.g. `A.setNow(int, int)`) that publishes both
views in one place, then delete the duplicate field writes. One writer, one
protocol, and the acceptance-tier gap closes itself. Delete `A.now` in the
recorded follow-up.

## F-2 — Race gating by hand-written `applies()` (OCP): the near-miss is worth one ADR line (low)

B-20's root cause was `DynamicBuildingsCommander` listing
`ProtossSpecificBuildingsCommander` / `TerranSpecificBuildingsCommander` as
generic; each commander had to remember `We.protoss()`/`We.terran()` itself
(`9b7f98cf`). The next race-scoped commander can forget the gate the same way.
Note `ZergNewGasBuildingCommander` gates itself (`We.zerg()` in `applies()`),
so the codebase already depends on a convention, not a mechanism.

Two observations to weigh:
1. `DynamicBuildingsCommander` *also* builds `raceSpecific` arrays behind
   `We.*` switches and exposes a static `get()` factory doing the same three-way
   branch — so the class contains **three** mechanisms for the same
   race-vs-generic decision (§16 Stage G exists for this).
2. Checklist OCP says "adding a race behaviour must not require an `if
   (We.protoss())` in an unrelated class" — yet the fixes here went exactly
   there, with a comment pointing at the array placement. That was the right
   call under the current structure, but it should be written down where the
   next author looks.

**Suggestion:** one line in SOLID-CHECKLIST (OCP section): *"A commander whose
subcommanders are race-scoped must gate itself in `applies()` — race gates
belong at the commander that owns the subtree, not at each leaf."* Optionally
add a tiny reflection-free ArchUnit-style test later (grep `protoss|terran`
import + missing `We.` in `applies()`); do not over-mechanize now.

## F-3 — `BaseUnderAttack` is static, and it is the policy class of B-19 (low)

The class is well-shaped (one question, two predicates, javadoc with the
evidence) and its placement in `atlantis.units` is argued correctly. But it is
all-static while everything around it converts to the `Manager`/`Commander`
tree, which is testable via `ManagerFactory` and the fake world. In a base
defence **all** worker managers now call it up to twice per frame
(`WorkerDefenceRun`, both fight managers) — the per-frame cost is bounded by
the `enemiesNear` 5-frame cache, so no perf issue, but the static seam means
no test can substitute a cheaper or forced answer, and `check()` recomputes
the same `Select.mainOrAnyBuilding()` query three times per frame per worker.

**Suggestion:** leave as is for this commit (it works, tests are green, the
scenario tier pins the behaviour). Record on NEXT as a candidate for the same
treatment `ExpansionUnderPressure`'s callers got: a tiny injectable or a
per-frame cache of `check()`. Not urgent.

## F-4 — `ProtossJfapTweaksConsiderChokesEtc` keeps static mutable state (`rawEval`) (low)

`private static double rawEval` written in `apply()` and read by the penalty
helpers below — mutable statics shared across every unit's evaluation in the
same frame. Safe only because evaluation is single-threaded and
`apply()`→helpers is strictly nested. It predates the window (only the
wrapping call changed), but the window touched this class three times
(B-2/B-18) without fixing the smell, and it is exactly the "no new static
mutable state" shape the checklist bans for new code. Verified: no other
reader in `src/atlantis`.

**Suggestion:** pass `rawEval` as a parameter into the penalty helpers (or
make the class instantiate-per-eval like `AtlantisJfap`). One small commit,
no behaviour change.

## F-5 — `BaseUnderAttack` and `ExpansionUnderPressure` duplicate the "pressure" concept (low / information)

Two decision classes now answer "is pressure real here" with different
inputs: enemy combat units near the base (B-19) vs enemy combat units or ≥3
workers near the expansion site (B-21). The thresholds differ (8 vs 12
tiles), the radius types differ, and the semantics differ (fight-or-flee vs
cancel-or-keep). This is *not* a merge request — but it *is* the second
appearance of the concept, which per the checklist is the moment to name it.
If a third appears, extract a shared "threat assessment" decision class;
until then leave both alone.

## F-6 — Class-name drift after the split (`CancelNotStartedBases`) (low)

`a716469c` correctly narrowed the method's behaviour (owner's rule: cancel
only when ≥2 unfinished bases), but the class still reads
"cancel-not-started-bases" while `redundantNotStartedBases` may keep the
oldest pending order alive — the docstrings carry the nuance, the name no
longer does. Callers (`OnOurUnitCreated`) and BUGS.md say "not started ones",
so the drift is contained.

**Suggestion:** when B-23 is next touched, consider renaming to
`PruneRedundantBaseOrders` or adding a class-level javadoc line ("may
deliberately keep one pending base even when the name says otherwise"). Cosmetic.

## F-7 — `scripts/run-e2e.sh` write policy and read-only claims (informational)

Correct per CONVENTIONS §8: scbw games live in `~/.scbw` and §8 allows
read-only access there; the script writes its table into `_AI/e2e/`. The
`--parse-only` mode matches what it claims. No violation found — this entry
exists so the next reviewer knows it was checked.

## F-8 — Situation: the per-frame `Commander.applies()` contract is quietly load-bearing (informational)

After B-20, the correctness of the frame depends on commanders' `applies()`
returning false for out-of-scope subtrees — the race gate is not a filter, it
is crash prevention (`HaveBunkerAtMainChoke` dereferenced a missing main
choke). `Commander.handle()` OR-accumulates and `Manager.handle()` non-null
stops the chain; both are documented in SOLID-CHECKLIST/LSP. Nothing to fix;
the next contract addition should follow the same "document where declared"
rule.

## Verification note for the GameClock WIP (the one open risk outside SOLID)

`run-architecture-tests.sh` compiles from `@out/arch-sources.txt` only when
`out/production/Atlantis/atlantis` is missing. A `git mv` between packages
leaves stale `.class` files behind and the store can absorb violations whose
text merely changed — the hazard NOTES.md already recorded. The current tree
has one modified store file (`2284295e…`) consistent with the seven util→game
violations going away. Before committing the WIP: `rm -rf out`, then
`run-architecture-tests.sh` from scratch, and eyeball the store diff for
entries that are *rewrites* rather than *removals*. The remaining
`ErrorLog`/`Cache`/`Log`/`ConsoleLog`/`TimeMoment` diffs all only swap `A.*`
clock calls for `GameClock.*` — no logic drift spotted (TTL comparison,
`% n` arithmetic and the 60-second throttle are semantically identical).

## Not followed up here (deliberately)

- B-1 (`eval()` scale + 228 threshold call sites) — open, has a work order
  (`WO-B1`) and ADR 0006; review adds nothing until the sweep data exists.
- B-24 (scenario-tier cost) — documented, and being attacked separately (the
  JFR profiling round this review accompanies).
- The 430-Manager granularity question and the god-class splits (`AUnit`,
  `Selection`, `A`) — pre-existing, tracked as #10/#11/#16/#17/#9.
