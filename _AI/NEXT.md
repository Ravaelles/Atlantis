# TODO — Atlantis (open issues)

Living backlog for the architecture migration defined in `_AI/REVIEW.md` §16
and for defects found along the way. This file is the single source of truth
for "what is left"; `_AI/REVIEW.md` keeps the *stage* narrative and
`_AI/NOTES.md` keeps operational learnings.

## Process (normative)

- Every item has a **stable number** (`#1`, `#2`, ...). Numbers are never
  reused and never renumbered, so an old commit or chat answer can always be
  mapped back to the item it referred to.
- **When an item is closed, delete its line from this file** (do not move it
  to a "done" section — the git history is the archive) and say in the commit
  message `Closes #<n>` with the reason it is actually done (test result,
  game-run id, ArchUnit output).
- New items are appended with the next free number, grouped by area.
- Work proceeds in any order; prefer items that unblock the most downstream
  work, not the easiest ones.
- One logical change per commit, per `_AI/CONVENTIONS.md` §6.
- An item is only closed when it is **verified by execution** (build, test
  run, or game run), not when the code merely compiles.

## Test health

- **#1** Triage the 11 pre-existing unit-test failures
  (7× `ATargetingTest`, 2× `ProtossRetreatTest`, 2× `ProtossSmallRetreatTest`,
  1× `ChokeTest.distToChokes`, see `DOCS/TESTING.md`). Decide per failure:
  fix production behaviour, fix the expectation deliberately, or pin as a
  known-broken expectation with a comment. Do not weaken assertions to make
  the suite green.

## Stage E — read model (remaining)

- **#2** Decide the representation of unknown hit points in the read model
  (`AbstractFoggedUnit.hp()` returns the `-69` sentinel). Options: explicit
  `OptionalInt`/nullable in `UnitSnapshot`, a dedicated `Hp` value object, or
  a documented sentinel accessor on the snapshot. Needs a written rationale —
  the current magic number leaks into every comparison site.
- **#3** Migrate production readers of `FoggedUnit` to `UnitSnapshot`.
  Deliberately skipped before because a pure delegation switch has no value
  (same object, same values, worse GC). Do it only where it changes a
  decision, e.g. code that needs the *last known* vs *current* distinction or
  that can now be expressed without fog-awareness. List candidate sites
  first, migrate the ones with a real payoff, and record the rest as rejected.

## Stage F — cache purge

- **#4** Inventory the static caches in `Select` (4 caches, dozens of string
  keys, magic TTLs: 0, 1, 30, 31, 53, 73, 91, 293 frames) into one document
  table: key → TTL → why the TTL → who reads it. No code change yet; the
  inventory is what makes the migration safe.
- **#5** Introduce a per-frame query service in `core.world` owning the
  caches, with explicit invalidation per frame instead of TTL guesses. Move
  one cache at a time, starting with the one with the fewest readers.
- **#6** Delete the migrated static caches from `Select` (no compat shims —
  every call site rewired in the same commit) and re-freeze the ArchUnit
  store only if violations genuinely disappeared rather than moved.

## Stage G — race strategies

- **#7** Design the race-strategy seam (which decisions move out of the
  `protoss`/`terran`/`zerg` packages into a `RaceStrategy` port) and record
  it as an ADR before touching the 241 branching sites.
- **#8** Implement the seam and migrate one subsystem end-to-end as proof
  (candidate: production), measuring that the branching-site count drops
  without behaviour change.

## Stage H — god-class split

- **#9** Split `A` (163 public static methods) into cohesive collaborators.
  Two slices done: file/path I/O moved to `atlantis.util.AFile` (12 methods,
  9 dead helpers deleted); the Swing surface replaced by `atlantis.util.AGui`
  (4 popup methods) after 19 dead GUI helpers were deleted, so `A` no longer
  imports `javax.swing`/`java.awt` at all (1688 → 1044 lines).
  Remaining slices, in order of cohesion: string/`List`/debug-formatting
  helpers, date/time helpers (`getCurrentDateInFormatYMD`, `getToday`,
  `hourMin`, `daysBetween`), pure math helpers (`inRange`, `median`, `dist`,
  `gradual`, `chance`, …), game-clock helpers (`ago`, `secondsAgo`,
  `nowString`, `minSec`, …). `A` should keep only what is genuinely global
  (game clock, random, mineral/supply views) and callers should hold explicit
  references instead of reaching into statics.
- **#10** Split `Selection` (235 public methods) along its existing internal
  seams; verify with a test that pins selection semantics before and after.
- **#11** Split `AUnit` (~623 methods) — start by extracting the order-emission
  surface that `AUnitOrders` already half owns, then the per-frame manager
  orchestration, then per-condition logic.

## Stage I — physical migration

- **#12** Once F–H land, define the physical package move to the bounded
  contexts (`Economy`, `Production`, `Combat`, `Intelligence`, `Map`,
  `Scouting`) per `DOCS/ARCHITECTURE-CONTEXT-MAP.md`, as a mechanical,
  separately-revertable step per context.

## Stage J — fortress

- **#13** Reach a **zero-violation** ArchUnit baseline and flip
  `ArchitectureBoundaryTest` from frozen store to hard rules (keeping the
  store only as a history of how far the ratchet got).
- **#14** Add a benchmark guard for the frame pipeline
  (`scripts/benchmark-trees.sh` result checked in CI-style thresholds) so a
  Stage E/F/H refactor that slows the per-frame work fails visibly instead of
  silently.

## Housekeeping

- **#15** Rebuild `bots/AtlantisP/AI/Atlantis.jar` and
  `bots/AtlantisT/AI/Atlantis.jar` with `scripts/build-bot-jar.sh` and
  re-verify a real game (`scbw.play ... --headless`) after the current
  backlog round, so the deployed jars match the current source.
