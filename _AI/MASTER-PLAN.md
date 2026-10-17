# Master Plan — Atlantis, big phases in priority order

The whole road on one page, most important first. Each phase states its
exit criteria; detail lives in the linked documents, day-to-day truth in
`_AI/NEXT.md`. Order is by dependency, not by appeal: a phase starts
when the one above no longer blocks it.

## Phase 0 — Foundations (done, recorded here so nobody re-plans it)

OpenBW headless loop with hard bounds (CONVENTIONS §17); fast suite +
ArchUnit frozen store; Java 1.8 everywhere; twin diagnostics
(`EVENTS.jsonl`, `BUILDINGS.json`) and the query tooling. Done means:
a game attaches, plays bounded, and leaves queryable logs.

## Phase 1 — Production correctness (IN PROGRESS, top priority)

The bot must execute the simplest orders flawlessly before anything
clever: the memorized opening plays itself (BO ownership, worker flow,
reservations that never starve the pump), every issuance lands in
`DECISIONS.log` with its reason. Detail: `_AI/__PLAN-DECISIONS.md`.
Exit: an opening with zero builder churn, zero duplicate sites, worker
stream unbroken, every row attributable in the log.

## Phase 2 — Decision board completion

Per-gate legacy deletions with one game of proof each (ratchet - never
batch rewrites); decision absorption slice by slice until the old trio
dies at zero; feeder coverage measured, never assumed. Detail:
`_AI/NEXT.md` Next steps 1-2, goal-board track. Exit: one issuer, one
log, no silent authors.

## Phase 3 — Combat cluster (structure done, behavior pending)

Cutover steps 1-4+6 landed (`src-old/`, `atlantis.battle`, jars);
remaining: one cohesive cluster between mission anchors, evaluator
fight/flee with a 10 s retreat latch, then role-based executors with
parameterized kiting. Detail: `_AI/__PLAN-COMBAT.md`,
`_AI/redesign/02_COMBAT.md` Phase 0. Exit: an OpenBW game shows one
group moving between anchors and winning an even fight it should win.

## Phase 4 — Placement cutover completion

Catalogue planner becomes the default, legacy finder deleted per
`_AI/PLACEMENT-CUTOVER-PLAN.md` and `redesign/03_PLACEMENT.md`.
Depends on Phase 1 (sites must be right before the planner owns them).
Exit: zero `Can't find place` across retained runs, legacy path gone.

## Phase 5 — Production V2 LIVE + architecture migration

V2 default behind the #45 deletion ordering (`redesign/01_PRODUCTION.md`);
stages E–J (read model, cache purge, race strategies, god-class split,
physical migration, fortress). Depends on Phases 1-2 (the demand layer
must be correct before it is rewritten underneath). Exit: V2 LIVE by
default with parity numbers, ArchUnit store at zero violations.

## Phase 6 — Measurement and learning

Micro-scenario battery with a frozen baseline table; parameter search
over role vectors (random first, ES on plateaus); fingerprint-driven
strategy selection; knob-style strategy scalars. Depends on Phase 3
(something must execute what gets tuned) and the decision log (reward
attribution). Detail: `_AI/CHALLENGES/Architecture.md` (ML-tunability).
Exit: a tuning loop that improves a frozen scenario table, unattended.

## Phase 7 — Expansion

Second goal vocabulary (Terran or midgame, owner-directed), sweeps vs
real bots, OpenBW-vs-live parity. Everything above is the precondition;
nothing here starts early.

## Non-rules (read before reordering)

- Never batch rewrites; never gate commits on full games; never trade a
  measurement for an explanation (CONVENTIONS §3a).
- Deletions wait for per-gate game proof, not for courage.
- A phase's exit is verified by execution, never by reading green.
