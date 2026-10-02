# ADR 0002 — Vertical Slice Architecture is not the system architecture

- **Status:** Accepted
- **Context:** VSA was considered as the primary architecture. VSA organizes code
  by user-triggered feature slices owning their full stack.
- **Decision:** **Do not adopt VSA as the system architecture.** A real-time bot
  has no discrete user requests: every game frame runs *all* subsystems against a
  shared, mutating world. Slices would all share the same loop and entities, i.e.
  fake verticals.
- **Consequences:**
  - One exception is allowed and encouraged: **per-unit behavior packs** (Marine,
  Dragoon, …) as self-contained modules *inside* the Combat context.
  - The overall structure stays a modular monolith (ADR 0001).
