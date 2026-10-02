# ArchUnit frozen baseline — do not delete, do not `.gitignore`

These files are the **frozen baseline** of `ArchitectureBoundaryTest`
(Stage B, `_AI/REVIEW.md` §16). They list the boundary violations that exist
**today** and that we have agreed to pay down over time.

- ArchUnit's `FreezingArchRule` stores one file per rule, named by a UUID
  derived from the rule text. It fails a build only on violations that are
  **not** in this store.
- **The files must be versioned.** If you add this directory to `.gitignore`,
  every machine regenerates an empty baseline and the ratchet stops working:
  new violations would silently become "the new normal".
- `stored.rules` maps each rule's text to its UUID file.

## Working rules

- **Never add a violation to the store to make a red build green.** Fix the
  dependency instead, or get an explicit agreement.
- The store is only updated when a *rule's text changes* (the UUID changes).
  If you edit a rule's `.because(...)` or package list, ArchUnit will re-freeze
  it — review that diff carefully, because it can silently absorb violations.
- Long-term goal (Stage J): shrink the store to empty, then delete it.
