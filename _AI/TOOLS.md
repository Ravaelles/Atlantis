# Tools worth having (owner direction 2026-10-19)

Built because a micro duel cannot be watched every game, and a twin
stream nobody reads is write-only. Each entry: what it answers, where
it lives, what is still missing. New ideas append with the same shape.
Nothing here is a status file - built tools get a one-line entry each,
open ideas wait for a game or a need, then get built or dropped.

## Built

- `scripts/run-openbw-ums.sh` [3/5 - map vetting only; the duel needs Wine until an enemy spawns] "maps/ums/<map>.scm" [debug]` - headless
  UMS duel host. Answers: does the bot own the scenario units, does an
  enemy spawn. Verdict so far: ours yes, enemy no (NEXT #94) - Wine
  stays the duel vehicle.
- `scripts/run-wine-ums.sh` [5/5 - every micro proof runs through it; speed flag + fps report included] "<map.scm>" [fast=1]` - Wine on any map with
  an explicit speed flag. Verifies no leftover game survived (a stale
  StarCraft makes the new game attach to the old map - measured: an
  8v8 request played the previous 1v1) and reports honest fps.
  Default fast: speed 0, frameskip 500, GUI off.
- `scripts/run-wine-batch.sh` [5/5 - the only way to separate luck from mechanism; 60 games overnight] "<map>" N OUTDIR [KEY=VALUE ...]` -
  overnight tuning loop. Each game randomizes the registered ParamTweaker
  coefficients in-JVM (or takes explicit values), then collects
  EVENTS.jsonl + per-game CSV (W/L, seconds, killed, lost, hits taken,
  shots fired, score, values). Answers: which regions of parameter
  space win fast with few hits.
- `scripts/summarize-fight.py` [5/5 - the morning readout; invariants flag what a skimmed log never shows] EVENTS.jsonl` - one-game digest with
  invariant flags (HITS_TAKEN <= 2, shot cadence vs 30f cooldown,
  cornered ratio). Answers: did the duel meet the domain rules.
- `scripts/fight-timeline.py` [5/5 - the decision-vs-implementation split made visible, one line per cycle] EVENTS.jsonl` - one line per cooldown
  cycle: shot dist, step dists, hits with the decision right before
  each one. Answers the owner's core question: was it a wrong DECISION
  (move away, move away... reads as repeated lines) or a wrong
  IMPLEMENTATION (a single broken row).
- `scripts/fight-units.py` [5/5 - per-unit table for formation fights] EVENTS.jsonl` - id, type, frames alive, death frame or SURVIVED,
  min/avg hp, avg/last dist to nearest enemy. Answers: who died when,
  at what health, how far from the enemy.
- `scripts/summarize-game.py` [5/5 - the whole macro datum in one call] <run_dir>` - verdict, roster with tech times, the units table,
  refusal gates, tech gates, kite/shot/hit totals, tactic split.
  Answers everything the post-game analysis needs without hand-greps.
- `scripts/kill-leftovers.sh` [4/5 - box discipline before every game] - kills stray StarCraft/ChaosLauncher/BWAPILauncher/Atlantis
  processes and clears shm/socket transport, refuses to report quiet
  while strays live. Two Wine games on one box cross-attach and
  mutually kill (measured 2026-10-20).
- `scripts/run-staredit.sh` [3/5 - scenario editing for UMS proofs] [map]` - StarEdit under Wine from the game root.
  Needed to re-stage micro scenarios (e.g. spread armies so the
  formation has time to form).

## Open ideas (build on need, drop on disproof)

- Twin-gap alarm [3/5 - nice once silent stretches actually bite; twins cover the loud cases]: extend the digest to flag silent stretches (no KITE
  row for >60 frames while an enemy is near = the unit is not deciding
  at all). Needs: enemy-near signal in the stream (CLUSTER has it).
- Per-frame decision trace [2/5 - the timeline covers the need; full-frame costs log volume]: for one unit: act() writes one letter per
  frame (S shot, H hold, M move, G glide, .=sleep, X hit) to a compact
  string. Volume is fine for micro maps (one unit, ~1000 frames);
  macro needs throttling. Shows stutter (MAMAMA...) at a glance.
- Map-slot inspector [4/5 - would have answered OpenBW/8v8 without burning games; build on demand]: read a .scm/.scx slot table (owners, races,
  computer flags) without launching. Would answer "can OpenBW host
  this duel" statically - today that costs a full game each.
- CherryVis trace for Wine games [2/5 - twins answer faster than a viewer; revisit only for frame-stepping]: the viewer pairs replays, Wine
  leaves none. If the game wrote its own unit trace, the micro dance
  could be watched frame by frame instead of inferred from twins.
- Param search driver [4/5 - random mapped the landscape; ES pays off only if a new tunable dimension opens]: random sweep now; evolutionary search over the
  batch CSV once the first sweep maps the landscape (joint tuning,
  <=8 dims per tactic - see CHALLENGES/Architecture.md ML verdict).
- 8v8 + Zerg scenario battery [5/5 for the generalization proof the owner asked for - human slots block 8v8 on both engines]: `8Drag_v_8Drag.scm` (even brawl) and a
  Zerg map (owner's Main.java list) as the post-duel proof that the
  kiting generalizes past one Zealot. Blocked on nothing but time.
