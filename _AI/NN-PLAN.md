# Master Plan — Atlantis: a board-first bot that learns WHEN

Owner direction: keep the behaviors, learn WHEN. The tuner tunes global
scalars; the pain is state-conditional gates (2.5 tiles here, a cooldown
there), each new Behavior multiplies hand-gating complexity. A policy
over the fixed Behavior set, from features (distance, hp, shields,
cooldown, counts, terrain), decides the behavior. Thresholds stop being
hand-written. No live snapshots (owner decision): synthetic scripted
maps + mandatory enemy-policy variety instead.

## Why this shape (not end-to-end, not pixels)

- The hand game already wins (V2 LIVE victory, duel V2 bands). The gap
  is *gating*, not mechanics: same behaviors, wrong moments.
- Features exist per tick (hp, shields, cooldown, distances, counts,
  foe positions, walkability); reward exists (win-then-hits-then-seconds);
  the ACTION SPACE exists (TargetLadder 9 rules + movement behaviors).
  Learning fills the missing middle: features -> Behavior.
- Interpretable first (contextual bandit / decision trees over the
  feature set, deployed as readable rules). Neural net only on plateau,
  behind the same seam (features in, Behavior out).
- Never a single point of failure: NN uncertain or output missing ->
  hand rule decides. Deterministic mode stays available permanently
  (same binary, flag-selected, doubles as the A/B baseline).

## Tracks (parallel, different gates)

1. **Production ratchet** (top priority, gates everything learner-side
   that touches the game): per-gate legacy deletions, one game of proof
   each (#45, #144-147 ordering). The learner never preempts this.
2. **Combat behaviors** (hand-written, sim-pinned, game-proven one by
   one): the fixed set the learner will gate (#170-186).
3. **Learning loop** (this file's track): fidelity -> scenarios ->
   learner -> gated rollout. Sim-scope work never touches production
   issuers, so it never waits on the ratchet.

## Phase gates (falsifiable, no vibes)

- **G0 speed**: sim runs ~1000x cheaper than a Wine game (measured:
  1 dragoon vs 5 lings, 600 ticks = 241 ms). Overnight batch-scale
  training fits. If a training loop ever costs like a game, stop:
  speed was never the bottleneck, fidelity is the work.
- **G1 fidelity loop** (gates everything downstream): enumerate the
  sim-vs-engine gaps and close them in Java, gap by gap, each
  sim-pinned (order latency/stickiness, accel/turn/top-speed per type,
  weapon cooldown/damage/animation timing, collision/pushing,
  fog/visibility for targeting inputs). Validation: twin-logged live
  trajectories (UNIT_TRACE ux/uy + cooldown + hits) vs sim replay of
  the same initial conditions (positional drift + outcome delta).
  **Falsification trigger, written down in advance**: if after closing
  the listed gaps the sim-vs-live outcome delta stays above bar
  (win-rate delta >20pp or mean positional drift >2 tiles at 600 ticks),
  THEN revisit C++ (Stardust shared-memory model) - until then Java
  stands. Abandoning the project over speed alone is off the table
  without this measurement.
- **G2 scenarios** (Stage B): scripted minimap fixtures as sim presets
  with deterministic seeds; enemy POLICY variety mandatory (static,
  chase-nearest, focus-weakest, kite-back) so gates cannot overfit one
  script. Winning shapes become fast-suite tripwires. Blocker rule:
  if the sim cannot discriminate two wirings at easy difficulty, the
  scenario is too easy - build harder ones, never tune on noise.
- **G3 learner**: supervised gate pins first (behavioral cloning of a
  hand gate on winning scenarios, held-out seeds + held-out policies),
  contextual bandit / decision trees next, NN only on plateau. Bar:
  held-out agreement beats the majority-class baseline by margin AND
  per-policy (no overfit-to-one-script), AND downstream sim wins are
  preserved (no regression vs the hand rule it shadows).
- **G4 gated rollout**: per-behavior flag (hand rule OR NN score,
  default OFF), global kill-switch, DRY_RUN mode (the net logs what it
  WOULD pick next to what deterministic picked - disagreement rows +
  outcomes become the evidence base), fallback mandatory (uncertain ->
  hand). Live takeover only after the G1 bar + production cut + a K-of-N
  A/B the net actually wins. Deterministic mode stays forever.

## What exists today (verified by execution, not claimed)

- Sim: `src/tests/unit/sim/` (SimWorld, SimArena, SimTunerBatch +
  manifests, ZombiePolicies x5, BattleFeatures 7-col CSV, EpisodeManifest
  per-tick CSV, FakePositionFinder spiral) - suite green.
- Action space: TargetLadder 9 ordered slots (`battle/targeting/`) +
  movement behaviors; Behavior vocabulary enum (`battle/Behavior.java`,
  frozen tags).
- Hand gates with pins: DuelKite.shouldYield + ProtossAttackHoldToShoot
  stop-point model (R* estimator), wired behind `holdEarlyDisabledForAB`
  (default off, audit line, toggle-restores-pre-wiring pin).
- Twins for measurement: UNIT_TRACE, UNIT_TELEMETRY, HIT/SHOT,
  CLUSTER (leader dest + nearest-foe tile), KITE stuck split
  (stuck-no-step vs stuck-order-refused + canCmd/canMove),
  HOLD twin (MOVE_FAILED + canCmd/canMove).

## Slice plan (each sim-pinned, suite green each step)

- **S1 (this change): learned hold-early gate.** Binary gate
  (yield-vs-hold) cloned from DuelKite.shouldYield on fake-map duels
  (spawn dists x enemy policies x seeds); zero-dep MLP (Adam,
  deterministic), held-out seeds + held-out policies; bar = loss down
  + agreement beats majority by margin + downstream duel wins preserved.
  Proves the vehicle: sim -> CSV -> train -> eval -> gate.
- **S2: harder Stage B scenarios** (pocket/wall variants incl. a losing
  shape for the wall funnel: ranged pressers outranging the guns;
  outnumbered variants) + K-of-N promotion bar.
- **S3: default-off NN gating seam** in the live path (mirror the
  holdEarlyDisabledForAB recipe: flag + audit + toggle-restores
  pre-wiring pin), DRY_RUN disagreement logging, no behavior change.
- **S4: contextual bandit over the feature set** (linear first, zero
  deps), then the order becomes data one Behavior at a time.
- **S5: live takeover** only after G1 + production cut + K-of-N A/B win.

## Non-goals (explicit, to stop scope creep)

C++ rewrite before the G1 falsification trigger; end-to-end pixel
policies; tuner scalar loop untouched (it keeps tuning globals);
#184 coordination review stays separate (action space, not gating);
training on a sim with unclosed dynamics gaps (G1 bar first);
faith in a number without a held-out replicate.
