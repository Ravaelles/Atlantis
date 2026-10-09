# Placement cut-over plan (option B for NEXT #47)

Status 2026-10-09: **plan, not started.** `NEXT.md` #47 mentions this file; the
detail lives here so #47 stays a list.

## Why this exists

The bot never places its first Pylon on OpenBW in the default configuration, so
it produces nothing and every E2E scenario fails. Measured 2026-10-09:

- The engine is **not** lying. On the probed tiles `isBuildable(tx, ty, false)`
  is `true` everywhere and only the occupancy-aware
  `isBuildable(tx, ty, true)` is `false` - and only for tile (7,44), which is
  genuinely occupied.
- The refusal therefore comes from the legacy path: `CanPhysicallyBuildHere`
  requires every tile of the 4x3 Pylon footprint to be free, and the legacy
  `APositionFinder` keeps handing it candidates whose footprint covers the
  occupied tile. The search fails to step past it.
- `PLACEMENT=catalogue` cannot help, because it only selects the planner for
  **Production V2**, and V2 is off unless `PRODUCTION_V2=LIVE` is set. In the
  default configuration the legacy planner is in charge.

The option-A shortcut (a fallback inside `isBuildable`) was investigated and
abandoned: it is not a fallback problem, and patching the legacy finder is
explicitly out of bounds (`_AI/POSITION-FINDER.md`, `redesign/03_PLACEMENT.md`
§4.6).

## What option B is

Make Production V2 the default policy and its catalogue-backed planner the only
placement implementation, then delete the legacy tree. This is the plan the
architecture already commits to; the Pylon is just the first thing that proves
it is not yet in force.

## Steps, smallest first, each ending green

1. **Prove V2+LIVE places a Pylon, unattended.** Run
   `PRODUCTION_V2=LIVE PLACEMENT=catalogue` and confirm `Can't find place for
   Pylon` disappears and a Pylon is actually built. This is the gate - nothing
   below starts until it holds. Note: an earlier claim that this already worked
   was measured with both flags set and is corrected in
   `redesign/03_PLACEMENT.md`.
2. **Make the pair the default** for the OpenBW bot directory (`ENV`), so no
   special flags are needed, then re-run the survival scenario
   (`EXPECT_MIN_INGAME_SECONDS=420`, `EXPECT_MIN_KILLED=12`,
   `EXPECT_MAX_KILLED=40`, `EXPECT_MIN_RESOURCE_BALANCE=-200`).
3. **Close the parity gap that LIVE still has** before any deletion: tech,
   race-specific army composition and strategic Play contributions are
   simplified in V2 (`_AI/STATUS.md` "what is still open"). Deleting the legacy
   dynamic commanders before that removes capability, not debt.
4. **Then the deletion order** from `NEXT.md` #45: dynamic commanders, then
   `Queue/**` and `ProductionOrder`, `Construction/**` last (V2 delegates builder
   execution to it today via `GameOrderDirector`).

## What must not happen

- No patch to `APositionFinder` or any `constructions/builders/position*` class;
  they are scheduled for deletion.
- No "temporary" third path where some buildings use the legacy planner and some
  the new one - that is the two-sources-of-truth problem this rewrite exists to
  end.
- No deletion from the bottom of the #45 list upward.

## Evidence to keep

- `out/openbw/bot.log` from the run that first places a Pylon without special
  flags (artefact of step 2).
- The survival-scenario verdict line (`verdict: ingame=... killed=...
  resourceBalance=...`) from the same run.
