# ADR 0004 — Units as a read model plus stateless systems

- **Status:** Accepted (target; migration Stage E)
- **Context:** `AUnit` fuses four responsibilities: engine binding, identity and
  lifecycle (static `instances` map), domain state, and ~623 behaviour methods —
  plus ~24 `public` mutable `_` fields mutated from outside. This is the root of
  the recurring "unit management is repeated everywhere" pain.
- **Decision:** Split units into:
  - `UnitSnapshot` — immutable per-frame projection;
  - `World` — the only owner of identity and the snapshot map;
  - `UnitState` — derived state behind a small API (no external mutation);
  - stateless **systems** over snapshots, emitting intentions applied once.
- **Consequences:** No new `public` mutable unit state, no new static caches, no
  new `A.*` global usage in production. Migration is incremental (Stage E).
