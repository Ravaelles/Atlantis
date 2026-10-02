# SOLID checklist (normative)

Companion to `DOCS/ARCHITECTURE-CONTEXT-MAP.md` (where things go) and
`_AI/REVIEW.md` §16 (in which order). This file answers a different question:
**what must be true of a change before it may be committed.**

The principles are not quoted from textbooks. Each rule below is tied to a
measurable symptom in this repository, and each measurement is re-taken when
the table changes.

## Measured baseline

Taken from `src/atlantis` (1379 classes):

| Signal | Value | Why it matters |
|---|---|---|
| Interfaces in `src/atlantis` | 13 | Abstraction is the exception, not the rule; DIP has barely started |
| Files reaching into `A.*` | 605 | Game logic coupled to a static facade |
| Files reaching into `Select.*` | 395 | Read model behind statics (Stage F) |
| Files touching `We.*`/`Enemy.*` | 430 | Race decisions made from arbitrary call sites (Stage G) |
| Manager subclasses | 430 | OCP is fine (factories), but every one shares one fat base class |
| `AUnit` | 587 methods | SRP + ISP violation |
| `Selection` | 231 methods | SRP + ISP violation |
| `A` | 43 public static methods (was 163) | Was the worst offender; slices done, resource facades remain |
| Ports in place | `OrderSink`, `LogPort`, `ValidityCheck` | ADR 0001 also plans `GameQuery`, `MapPort`, `ClockPort` |

## The five principles, as rules for this repo

### SRP — one reason to change

- A class exists for one reason. If you cannot name it in one sentence without
  the word "and", split it.
- **A leaf must not decide policy.** Reading a file, formatting a number or
  writing a log line must never decide to quit the game, exit the JVM or
  ignore an error. It reports; the caller decides. (`AFile.loadFile` used to
  call `System.exit(-1)`; fixed, pinned by `AFileTest`.)
- **No new static caches, no new `public` mutable state.** State lives in the
  read model (`UnitState`, `UnitSnapshot`) or behind a port.
- **A new utility class needs justification.** `AFile`/`AMath`/`AConsole` are
  acceptable because they are *pure* (total functions of their arguments) or
  thin adapters over a port. A class that only forwards to other statics is a
  new god utility in disguise.
- Mechanical consequence: methods move out of `A` when they share no reason to
  change with the resource/clock facades. Every move must keep the class count
  of static facades from growing back.

### OCP — extend without touching

- Adding a manager/commander must not require editing its parent: it registers
  itself through `ManagerFactory`/`CommanderFactory` (Stage C) — keep it that
  way, do not reintroduce shared ordered lists in `A`.
- Adding a race behaviour must not require an `if (We.protoss())` in an
  unrelated class. That is Stage G (`#7`, `#8`); until the seam exists, new
  race branches go into the race packages, never into shared code.

### LSP — substitutability

- Anything passed through `OrderSink`, `LogPort`, `GameQuery` or `ValidityCheck`
  must be usable by every production and test implementation. No
  "works only for the bwapi adapter" methods.
- `Commander.handle()` (boolean, OR-accumulated) and `Manager.handle()`
  (non-null stops the chain) differ on purpose and are documented as such.
  Do not silently unify them — that changes traversal. Any new traversal
  contract must be documented where it is declared.

### ISP — narrow interfaces

- Consumers depend on what they use. A 587-method `AUnit` and a 231-method
  `Selection` cannot be "just injected"; the split has to happen first
  (`#10`, `#11`).
- New interfaces start narrow: `LogPort` has four methods, not a console
  abstraction with 20.

### DIP — depend on abstractions

- Every dependency the core needs from the outside world goes through a port
  defined next to the consumer, with the adapter next to the infrastructure.
  Ports are added only where they earn their keep (ADR 0001), not one per
  engine call.
- Transitional seams (a static port holder, `AUnit.setOrderSink(...)`) are
  allowed while there is no composition root, but each one is on the record in
  `_AI/NEXT.md` with the endgame stated.
- Test doubles are first-class: if a class cannot be exercised without a
  running game, it needs a port, not a workaround.

## Review gate (per commit)

1. Does the change make any class smaller or more cohesive? If it only adds,
   say why that is the right trade.
2. Does any leaf now decide policy it did not decide before?
3. Does anything new depend on a static facade instead of a port? If yes, is it
   on `_AI/NEXT.md`?
4. Does the frozen ArchUnit store shrink, or at least stay unchanged? Growth
   needs an explicit decision (see the store `README`).
5. Verification: `bash scripts/run-tests.sh`, `bash scripts/run-architecture-tests.sh`,
   and a game run when the change can affect play. A refactor is not "verified"
   because it compiles.

## Known violations (tracked, not tolerated silently)

- `#1` 11 pre-existing unit-test failures — listed in `DOCS/TESTING.md`.
- `#9` `A` still owns the resource/supply facades (~1400 call sites).
- `#10`, `#11` `Selection` and `AUnit` still god classes.
- `#16` `AUnit`/`Selection` need consumer-shaped interfaces (ISP).
- `#17` `System.exit` still reachable from domain code
  (`AtlantisRaceConfig`, `APositionFinder`, `Atlantis`, `AKeyboard`).
- `#18` `GameQuery`, `MapPort`, `ClockPort` from ADR 0001 still missing;
  `A.seconds()`, `A.now`, `AMap.*` are the last big static surface.
