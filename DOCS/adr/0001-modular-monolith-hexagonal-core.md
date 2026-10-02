# ADR 0001 — Modular monolith with a hexagonal core

- **Status:** Accepted (direction; migration in progress)
- **Context:** A ~128k-line, 10-year-old real-time bot. Packages group by subject,
  not dependency direction; measured, every major package is in a cycle with
  several others and there is no stable core. A big-bang rewrite is unacceptable.
- **Decision:** Target a **modular monolith** with a **hexagonal core**:
  `adapters → application → contexts → core`, dependencies inward only. The core
  owns the domain model and rules and must not import `bwapi` or other adapters.
- **Consequences:**
  - Ports are defined only where they earn their keep (`GameQuery`, `OrderSink`,
    `MapPort`, `ClockPort`, `LogPort`) — not a port per `bwapi` call.
  - Contexts communicate only through Published APIs.
  - Migration is incremental (strangler); the bot stays playable at every commit.
  - See `DOCS/ARCHITECTURE-CONTEXT-MAP.md` and `_AI/REVIEW.md` §16.
