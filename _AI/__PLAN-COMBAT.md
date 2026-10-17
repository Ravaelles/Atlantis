# __PLAN-COMBAT: delete combat, replace with one cluster (owner 2026-10-17)

> DELETE THIS FILE WHEN DONE (executed and deleted, like DECISIONS entries -
> CONVENTIONS §20: no rotting prose). Git history is the archive.

## Goal

Throw away all combat logic, keep the good micro (e.g. ProtossCorsair) as
reference, and replace it with: one combat cluster (sticks together, moves
together), mission following (defend = main base, attack = enemy base),
Combat Evaluator as the single fight/retreat gate, retreat latched for a
minimum duration (10 s). Production/building work takes precedence;
combat commits land after it unless trivially separable.

## Measured starting point

- 536 files under `src/atlantis/combat/`.
- 103 production files outside it import `atlantis.combat`.
- 29 test files touch combat (incl. the 4pool/9pool rush twins, BUGS B-19).
- Entry: `FramePipeline` → `CombatCommander` (+ `CommanderFactory`).
- Build compiles `find src -name '*.java'` - so `src-old/` (repo root
  sibling) is invisible to the build, the jar and ArchUnit.

## Steps (in order, one logical change per commit)

- [x] 1. **Move** — DONE 2026-10-17 (`f846d086`): `src/atlantis/combat/`
  (536 files) lives in `src-old/atlantis/combat/` (+ protoss micro
  under `src-old/atlantis/protoss/`); support machinery extracted to
  `atlantis.battle` instead of deleted (avoid, eval, targeting,
  advance-focus, generic, running, retreating, detectors, siting,
  Squad/Mission/Missions/Alpha thins). Sequencing went green-first
  (entry before move), not red-first.
2. **Rewire the entry** — DONE 2026-10-17 (same commit):
   `FramePipeline` drives `BattleCommander`; AUnit + 100 call sites cut
   off squads/missions/managers (Sparta never, DEFEND holds, tanks don't
   unsiege, idle doesn't auto-attack, BO mission rows unenacted, Terran
   building micro out). DONE = `grep -rn "atlantis.combat" src/`
   empty (verified).
3. **Tests** — DONE 2026-10-17 (`aa7df798`): 15 files deleted
   (incl. the rush twins - B-19 goes dark), kept-behavior tests
   repointed to battle. DONE = suite 606/0, 1 skipped.
4. **Architecture** — DONE 2026-10-17: store shrinks by 127 entries
   (pure removals, reviewed); the 1 failing rule is pre-existing.
   DONE = numbers in the commit message.
5. **Cluster behavior** — CODE DONE 2026-10-17, movement PROVEN,
   fighting BLOCKED (no enemy spawns): single cluster gathers army
   units; DEFEND posts at main, ATTACK marches on enemy base /
   unexplored starts; evaluator gate (1.0) with 240-frame retreat latch;
   per-unit attack-in-range else move; CLUSTER twin every 31f. Proven
   091624: 55-unit group, DEFEND→ATTACK, anchors main → natural →
   enemy corner. Fighting needs a game WITH an enemy - the host
   spawned none in 4 consecutive games (`enemy: ` empty, environmental,
   bot-independent). DONE = anchors proof (met) + fighting proof
   (retry when spawn works).
6. **Jar.** Rebuild both bot jars (Wine + OpenBW paths) so the next games
   play the cutover. DONE = jars verified to lack `atlantis/combat/`
   classes and contain `atlantis/battle/`.

## Explicitly out of scope here

- Kiting parameters, scenario battery, search loop (designed in
  `redesign/02_COMBAT.md` Phase 0 notes - later).
- Porting Corsair micro (reference stays in `src-old/`, port is its own
  future slice with its own game proof).
- Re-enabling the rush twins (needs a cluster that can fight - separate
  item when it wins one).

## Known regressions accepted upfront

- The bot loses real fights until step 5 matures (openings, production
  and placement measurement stay valid - those never needed combat).
- B-19 rush coverage goes dark (twins disabled, not deleted).
