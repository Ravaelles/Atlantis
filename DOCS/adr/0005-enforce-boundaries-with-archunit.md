# ADR 0005 — Enforce boundaries with ArchUnit (frozen baseline)

- **Status:** Accepted (implemented, Stage B)
- **Context:** Agreed boundaries decay within weeks if nothing checks them.
- **Decision:** Encode the dependency directions from
  `DOCS/ARCHITECTURE-CONTEXT-MAP.md` in
  `src/tests/architecture/ArchitectureBoundaryTest.java`, using ArchUnit's
  `FreezingArchRule`. Today's violations are stored in
  `_AI/architecture/archunit-store/` (versioned); the build fails only on **new**
  violations.
- **Consequences:**
  - Never add a violation to the store to make a build green.
  - Editing a rule's text re-freezes it (UUID changes) — review that diff.
  - Goal (Stage J): shrink the store to empty, then delete it.
  - Headless runner: `scripts/run-architecture-tests.sh`.
