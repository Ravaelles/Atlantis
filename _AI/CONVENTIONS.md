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

## 4. Completion notification (mandatory)

- `/home/ping.sh` is the **"I am completely done"** signal. The assistant runs it
  **once**, at the end of a work session, when everything the user asked for in
  that session is finished **and verified by execution** (tests run, ArchUnit
  green, game run where the item requires one).
- It must **not** be run after an intermediate step, a partial answer, or a
  question that is still waiting for a reply. A ping after every tool call would
  train the user to ignore it, which destroys the only thing the sound is for.
- If a session ends with work still open, the assistant says so in the summary
  and does not ping. The next session pings when it closes the remaining work.
- Commit messages and summaries do not need the ping; only the final message of
  the session does.

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

- All work stays inside **`/ravaelles/JAVA/starcraft-ai`**: `Atlantis/`,
  `StardustDevEnvironment/`, `bots/`, `starcraft/` and anything else in that
  tree. Reading, searching, writing, building and running tests happen there.
- **Never search, scan or read anything above that directory** - not the user's
  home directory, not other projects, not system paths - even read-only, and
  even when the goal is "find any data that would help". A `grep -r` or `find`
  rooted in the home directory turns a task into a crawl of private files, and
  whatever it finds is not evidence the project agreed to produce.
- The only paths outside the workspace that may be used:
  - `/home/ping.sh`, the completion notification of section 4, and only when
    section 4 allows it;
  - `/tmp/opencode`, the scratch directory the tooling provides, for throwaway
    tooling of the current task (a virtualenv, a downloaded archive, an
    intermediate file). Nothing produced there belongs to the repository;
    anything that has to survive goes into `Atlantis/` and is committed.
- A third-party tool installed to help (a package, a virtualenv) is a means, not
  a deliverable: do not add it to the repository and do not let the repository
  depend on it, unless the task is exactly about adding that dependency.
