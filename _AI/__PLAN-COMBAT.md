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

1. **Move** `src/atlantis/combat/` → `src-old/atlantis/combat/` (`git mv`,
   history preserved). Tree goes red - expected, fixed in step 2. DONE =
   `git status` shows renames only, nothing edited.
2. **Rewire the entry.** New thin package `atlantis.battle` (name TBD at
   implementation): `BattleCommander` owns every army unit each frame.
   `FramePipeline` + `CommanderFactory` point at it. All other combat
   imports at the 103 call sites are cut (delete the per-unit manager
   invocations, mission hooks, worker-defence links - no stubs left
   behind). Terran `repair/` + `chokeblockers/` + worker-defence stay,
   minus their combat refs. DONE = `grep -rn "atlantis.combat" src/`
   empty.
3. **Tests.** Delete combat-micro unit tests. Rush e2e twins
   (FourPool/NinePool, B-19 pins) are disabled with a pointer to this
   file - the bot will lose fights until the cluster learns them, and a
   red twin would only restate that. DONE = fast suite green.
4. **Architecture.** Suite green + `run-architecture-tests.sh`; the store
   SHRINKS (review the diff - only genuine removals, never additions).
   DONE = store diff reviewed, suite numbers in the commit message.
5. **Cluster behavior.** Single cluster gathers army units; defend posts
   it at the main, attack sends it at the enemy base; evaluator says
   fight or flee; flee latches ≥10 s before re-evaluation. DONE = an
   OpenBW game shows one cohesive group moving between the two anchors.
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
