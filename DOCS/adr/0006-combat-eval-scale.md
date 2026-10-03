# ADR 0006 — Combat evaluation scale (PROPOSED, not accepted)

- **Status:** Proposed. This ADR frames the decision `_AI/BUGS.md` B-1 asks
  for; it does not take it. Accepting needs a scenario sweep over the real
  evaluator plus game runs.
- **Context:** `AUnit.eval()` returns `enemyScore / (ourScore + 0.001)` from a
  ~60-frame JFAP simulation - an unbounded ratio where lower means stronger
  for our units. 228 production call sites compare it against thresholds in
  `[0.6, 3]`, while the javadoc promises "1.0 = even, 1.3 = 30% stronger".
  Three known distortions interact with any scale decision:
  - B-2: additive Protoss tweaks push near-zero ratios below zero (measured
    -0.3934 for a 0.0066 fight), flipping every `eval <= y` guard;
  - B-18: the 60-frame window scores long fights as even (0.98) that the
    colony wins outright;
  - the no-enemy reading is 9874.0 (and -1.0 absolute), which every
    `eval >= 2` guard reads as safe.
  Verified invariants that constrain the design: mirror pairs are reciprocal
  (`ourEval * theirEval ~= 1`), and both JFAP side scores are cost-like
  negative, so the raw ratio is never negative - only the tweaks break it.
- **Decision (recommended):** do NOT re-normalise the ratio. Any monotonic
  re-map preserves order, so all 228 thresholds would need re-mapping anyway -
  same churn as re-deriving, with none of the three distortions fixed. Instead:
  1. floor the tweaked eval at 0 (fixes B-2 at the source);
  2. document 9874.0 explicitly as "no threat in reach", not "infinitely
     strong", and audit the guards that must distinguish "safe" from "no
     data" (a dozen, not 228 - most high-eval reads are harmless when quiet);
  3. leave the horizon (B-18) to a simulation change, not a scale change.
- **Consequences:** thresholds keep their tuned meanings; the ratio stays
  unbounded above (fine - it is a ratio); the sign and no-data cases become
  total. All three steps change fight behaviour and need game runs.
- **Follow-up regardless of the decision:** freeze the WO-B1 evidence table
  as a golden test (like the ArchUnit store): any evaluator change that moves
  a number fails the build until the table is deliberately re-frozen. The
  fiction-table episode proved a green suite without that ratchet is not
  evidence.
- **Alternatives rejected:** re-normalising to a bounded scale (churn without
  fixing B-2/B-18/9874); re-deriving all thresholds from a sweep (re-tuning
  the bot by hand - years of game knowledge, no oracle).
