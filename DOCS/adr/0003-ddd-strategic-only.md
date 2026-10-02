# ADR 0003 — DDD: strategic patterns only

- **Status:** Accepted
- **Context:** Domain-Driven Design was considered. DDD has strategic patterns
  (bounded contexts, ubiquitous language, context map) and tactical ones
  (aggregates, repositories, domain events, transactional consistency).
- **Decision:** Adopt the **strategic** half; reject the **tactical** half.
- **Rationale:** There is no database and no transactional invariant to protect.
  The "domain" is a read/write projection of engine memory; `AUnit` is not an
  aggregate root. Tactical DDD would add ceremony without invariants.
- **Consequences:** Six bounded contexts with Published APIs (see ADR 0001);
  no repositories/aggregates introduced.
