# Tools worth having (owner direction 2026-10-19)

Built because a micro duel cannot be watched every game, and a twin
stream nobody reads is write-only. Each entry: what it answers, where
it lives, what is still missing. New ideas append with the same shape.
Nothing here is a status file - built tools get a one-line entry each,
open ideas wait for a game or a need, then get built or dropped.

## Built

- `scripts/run-openbw-ums.sh "maps/ums/<map>.scm" [debug]` - headless
  UMS duel host. Answers: does the bot own the scenario units, does an
  enemy spawn. Verdict so far: ours yes, enemy no (NEXT #94) - Wine
  stays the duel vehicle.
- `scripts/run-wine-ums.sh "<map.scm>" [fast=1]` - Wine on any map with
  an explicit speed flag. Answers: did the speed flag reach the game
  (greps the log), honest fps (frames/wall). Default fast: speed 0,
  frameskip 500, GUI off.
- `scripts/run-wine-batch.sh "<map>" N OUTDIR [KEY=VALUE ...]` -
  overnight tuning loop. Each game randomizes the registered ParamTweaker
  coefficients in-JVM (or takes explicit values), then collects
  EVENTS.jsonl + per-game CSV (W/L, seconds, killed, lost, hits taken,
  shots fired, score, values). Answers: which regions of parameter
  space win fast with few hits.
- `scripts/summarize-fight.py EVENTS.jsonl` - one-game digest with
  invariant flags (HITS_TAKEN <= 2, shot cadence vs 30f cooldown,
  cornered ratio). Answers: did the duel meet the domain rules.
- `scripts/fight-timeline.py EVENTS.jsonl` - one line per cooldown
  cycle: shot dist, step dists, hits with the decision right before
  each one. Answers the owner's core question: was it a wrong DECISION
  (move away, move away... reads as repeated lines) or a wrong
  IMPLEMENTATION (a single broken row).

## Open ideas (build on need, drop on disproof)

- Twin-gap alarm: extend the digest to flag silent stretches (no KITE
  row for >60 frames while an enemy is near = the unit is not deciding
  at all). Needs: enemy-near signal in the stream (CLUSTER has it).
- Per-frame decision trace for one unit: act() writes one letter per
  frame (S shot, H hold, M move, G glide, .=sleep, X hit) to a compact
  string. Volume is fine for micro maps (one unit, ~1000 frames);
  macro needs throttling. Shows stutter (MAMAMA...) at a glance.
- Map-slot inspector: read a .scm/.scx slot table (owners, races,
  computer flags) without launching. Would answer "can OpenBW host
  this duel" statically - today that costs a full game each.
- CherryVis trace for Wine games: the viewer pairs replays, Wine
  leaves none. If the game wrote its own unit trace, the micro dance
  could be watched frame by frame instead of inferred from twins.
- Param search driver: random sweep now; evolutionary search over the
  batch CSV once the first sweep maps the landscape (joint tuning,
  <=8 dims per tactic - see CHALLENGES/Architecture.md ML verdict).
- 8v8 + Zerg scenario battery: `8Drag_v_8Drag.scm` (even brawl) and a
  Zerg map (owner's Main.java list) as the post-duel proof that the
  kiting generalizes past one Zealot. Blocked on nothing but time.
