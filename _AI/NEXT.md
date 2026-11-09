# TODO — Atlantis (open issues)

This file is a hub, not a backlog: one line per workstream with a
pointer and a status, no per-game rows, no datum archaeology. The
work itself lives in task files under `_AI/NEXT/` (full context:
what to change, exact DELETE/MODIFY/ADD lists, verification,
exit criteria), each created from `_AI/NEW.md` with an owner
decision behind it. A line here dies when its task file's work
lands; history lives in git. Game-verdict data belongs in the
gitignored `_AI/WINE-MACRO-DATUMS.md`, never here.
Direction: `_AI/00-GOAL-APPROACH.md`; operational learnings:
`_AI/NOTES.md`.

## Phases (the quarter)

- [ ] **1 — Production correctness (top priority).** Substantially met; remaining #95, #96. (→ PRODUCTION.md.)
- [ ] **2 — Decision board completion.** Per-gate legacy deletions, one game of proof each; exit: one issuer, one log. (Next steps 1–2, #91 → PRODUCTION.md.)
- [ ] **3 — Combat cluster.** Structure done; behavior pending live proof. Blocked on #94. (→ COMBAT.md.)
- [x] **4 — Placement cutover.** Catalogue default; remainder: resite chain (#146 → V3).
- [x] **5 — Production V2 LIVE.** V2 is the default (no OFF path); open gaps ride their items (→ PRODUCTION.md).
- [ ] **6 — Measurement and learning.** Unattended tuning loop (parked: #160/#185, review 2026-11-09).
- [ ] **7 — Expansion.** Nothing starts early (owner gate).

Work rules: never batch rewrites; never gate commits on full games; never trade a measurement for an explanation (§3a).

## Current owner priorities
0. Mission-driven command (#232 - one named Mission owns the cluster;
   posture/tactic/approach derive from it; blocked missions downgrade
   explicitly, never silently).
1. Decision reasons in CherryVis (#214 - the army dies without a trace;
   needs a fresh live game proving an unattributed death is now
   attributed - owner-gated vehicle).
2. Fast, self-terminating OpenBW sims (§17).
3. OpenBW/E2E testing (`IDEA-E2E-TESTS.md` §3.5).

## Process (normative)

- **#232 — Mission-driven command (priority 0).** Full spec:
  `_AI/MISSION-DRIVEN-COMMAND.md`. One named Mission owns the cluster
  (HOLD_MAIN / HOLD_MAIN_CHOKE / HOLD_NATURAL_CHOKE / STAGE_MIDDLE /
  HOLD_MIDDLE / ATTACK_ENEMY_BASE); posture, tactic, approach and
  behaviors derive from it instead of reading live state independently.
  Technical fallbacks report upward and force re-decision; blocked
  missions downgrade explicitly, never silently. Slices: shadow decider
  + twin → divergence measurement → one mission enforced at a time →
  fallbacks report upward → delete shadowed deciders. Each slice its
  own commit, suite green, game proof where named. Includes the
  post-mortem of how command scattered (7 mechanisms) and proposed
  process rules P1–P7.
  SLICES 1a/1b LANDED 2026-10-30/31 (vocabulary + pure `decide` +
  `ShadowTwin`; `ArmyMissionTest` + `ArmyMissionShadowTest`). SLICE 2
  lesson: postures flap without contact (indecision shape);
  enforcement order per spec: HOLD_MAIN_CHOKE first. SLICE 3a LANDED
  2026-11-01 (code + world pin; live proof OPEN): `MissionHold` seam
  + `holdForMission` gate; twin change-only `MISSION_ENFORCED`.
  PROOF GAME live-safe but unproven; needs a melee-choke pressure
  vehicle, not another macro.

- Every item has a **stable number** (`#1`, `#2`, ...). Numbers are never
  reused and never renumbered, so an old commit or chat answer can always be
  mapped back to the item it referred to.
- **When an item is closed, delete its line from this file** (do not move it
  to a "done" section — the git history is the archive) and say in the commit
  message `Closes #<n>` with the reason it is actually done (test result,
  game-run id, ArchUnit output).
- New items are appended with the next free number, grouped by area.
- Work proceeds in any order; prefer items that unblock the most downstream
  work, not the easiest ones.
- One logical change per commit, per `_AI/CONVENTIONS.md` §6.
- An item is only closed when it is **verified by execution** (build, test
  run, or game run), not when the code merely compiles.

## Active steps (one line per workstream; detail lives in the linked file)

- Production V3 single-door (owner order 2026-11-04): see
  `_AI/NEXT/PRODUCTION_V3.md` - the full context, file lists and step
  sequence live there; this line is the only NEXT entry until the
  slices land.
- #238 duplicate rows: authorship attribution still open (V2-direct
  silent mint is the prime suspect, unproven); needs the owner
  authorship decision before any guard. No code.
- #150 stuck-IN_PROGRESS rows: open parts (owner visual check of
  PREPARING, live overlap proof, WO-150b HP trail;
  discriminating demote-guard twin named, not implemented).
  Details: PRODUCTION.md.
- #214 decision reasons (owner: VERY HIGH priority): all three
  halves landed; needs a fresh live game proving an unattributed
  death is now attributed (owner-gated vehicle).
- Live games OPEN on owner order 2026-11-04 (halt lifted): one quiet
  game at a time, ideally one game per several issues - verification
  rides batched games, never alongside the suite.

## Next steps (pointer queue, not a backlog)

1. Retreat doctrine live proof: COMBAT.md (proof queue).
2. Per-gate legacy deletions: owner picks the family; one game of proof each. (PRODUCTION.md board.)
3. Decision absorption (#91): PRODUCTION.md (board workstream); Robotics/observer demand and the gateway threshold thicket stay with current owners until game proof.
4. Pylon closure: PRODUCTION.md (siting proof queue).
5. Game-proof backlog: see the proof queues in PRODUCTION.md / COMBAT.md (700 s+ run, L2/L3, builder variance).
6. Second goal vocabulary: untouched until owner grows it.
7. Live-proof queue (one quiet game at a time, never alongside the suite; StarEngine first, real game where the sim can't model it - CONVENTIONS §30). Owner 2026-10-26: StarEngine first. Owner 2026-11-04: final verification is always a real game. Vehicles as needed: lurker-with-gas, melee-choke, island/UMS.
8. Owner data: PRODUCTION.md (readiness queue: minerals at stall, dominant twin rows, Gateway count/state); #96 (painter labels + missions game); #34 (UMS opponent map).
9. Long-term ordering: #45 deletion order, rewrite order #144-#147 (PRODUCTION.md migration), stages E-J below. Parity runs + K-of-N review 2026-11-09.
11. Sweep remainder: executable slices land one commit each; game-proof and owner-data shapes wait in steps 7-8.

## Test health

Do not store suite counts here; measure with `scripts/run-tests.sh`
(fast gate) or `scripts/run-full-tests.sh` (owner-called slow tiers)
when needed. Inspect `out/` before cleaning: it can hold archived
game logs as well as build output.
- Fast gate green at HEAD (unit scope; 2 pre-existing ArchUnit
  advisories, advisory only).
- Acceptance: the known pre-existing set (Queue/InQueue/ResourcesReserved
  family - #147 deletion wave, owner-gated). AUnitTest green
  post-mission-slice (targeted run; full-tier confirmation rides the
  next slow run).
- e2e goldens green (SimOpeningToFifty et al).
- Known flakes (lived-with, mechanism pinned, do not "fix" blind):
  MidGameStallReproTest suite-load timeout; ReengageGlideTest
  NoClassDefFoundError (infra, non-recurring); CutoffSwampedTest
  order flake (no global id reset; needs K-of-N/harness work).
- Domain-run order dependence (measured 2026-11-09, 1812-test
  production/placement/combat run: 1807/5): RequestBuildingNearTest
  fails 2 methods in-domain but passes solo 6/6 both at fe0ecdf39 and
  with the branch (worktree-proven); GatewayWaitsOnBlockedBaseTest
  hangs >100 s solo (20 s cap violated) while failing fast in-domain.
  Neither test reads branch-changed code. Same harness class as the
  CutoffSwamped flake above - K-of-N work, no blind fix.

## Architecture migration — stages E–J

- **E · #3 / F · caches / G · #8** done (UnitIndex frozen lists;
  V2 parity pinned by goldens). FoggedUnit readers wait on E/H.
- **H · #9–#11** partial seam extractions; continue only when a
  clear seam appears.
- **I · #12** bounded-context moves after F–H.
- **J · #13** ArchUnit store to zero before rules go hard. Never
  move violations between rules; structural waits on E/H,
  information waits on G. Authoritative gate:
  `scripts/run-architecture-tests.sh` (details:
  `_AI/work-orders/WO-13-mechanical-archunit-sweep.md`).

## Blocked / parked (one line each; unblock = the named condition)

- #238 duplicate authorship (owner authorship decision).
- #150 open parts (owner visual PREPARING check; WO-150b).
- #96 Wine verification (owner-side: painter labels, missions game).
- #34 UMS opponent map (owner-authored; tier stays owner-run).
- #65/#68 CherryVis viewer (owner opens a real trace).
- #78/#79 replay desync (upstream OpenBW#27; live log is truth).
- #94 OpenBW combat blocked (upstream visibility; Wine is the vehicle until lifted).
- #149 causeless early exit (engine-side; no bot cause; revisit on recurrence).
- #95 reserved-250 ghost (unobserved; correlate if it returns).
- #82 Terran off-site (unobservable by design; Protoss-only D8).
- #160/#185 tuner + learner (parked: K-of-N data + Stage A fidelity; review 2026-11-09).
- #71 readiness run (blocked: per-run horizon; Wine default blocks it).
- #84 legacy-minted warp (needs non-V2 game; evaporates at V3 S4).
- #213 island/UMS proof (no such map in the vetted library; owner call).
- #216 OpenBW melee (no host; engine-side, OPENBW-HANDOFF owns it).
- #166 independent OpenBW (no done-signal; do not touch until handoff).
- #164 OpenBW takeover (install engine changes only through the harness).
