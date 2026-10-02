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
