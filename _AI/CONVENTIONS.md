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
