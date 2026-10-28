package atlantis.util.log;

/**
 * Families of structured diagnostic events. One value per log family that
 * moved off plain strings - never per message: the variance lives in the
 * parameters, so grepping thousands of near-identical lines becomes
 * filtering one key.
 */
public enum LogKey {
    /**
     * A worker was assigned to a prepared site: adoption, with the state.
     */
    BUILDING_STARTED,
    /**
     * A fresh construction request was accepted: who asked, for which order.
     */
    CONSTRUCTION_REQUESTED,
    /**
     * The goal board's opinion snapshot next to legacy's actions.
     */
    BOARD,
    /**
     * A queued order refused to go READY, with the refusing term.
     */
    ORDER_NOT_READY,
    /**
     * Per-second builder snapshot while it owns a construction.
     */
    BUILDER_STATE,
    /**
     * The issue path was reached (gates inside decide).
     */
    ISSUE_BUILD_REACHED,
    /**
     * A build order attempt with the engine preconditions.
     */
    BUILD_ATTEMPT,
    /**
     * A request refused as duplicate, with the standing count.
     */
    REQUEST_REFUSED_DUPLICATE,
    /**
     * Power inputs at decision time for power-needing buildings.
     */
    POWER_AT_REQUEST,
    /**
     * Gas tile fix path (geyser found, snapped, or missing).
     */
    GAS_FIX,
    /**
     * Singularity Charge decision inputs.
     */
    SINGULARITY_APPLIES,
    /**
     * Singularity Charge decision vector (refusing gate included).
     */
    SINGULARITY_CHECK,
    /**
     * Dragoon pump decision inputs.
     */
    DRAGOON_TRAIN,
    /**
     * A refresh attempt outcome (refreshed, or cancelled + re-requested).
     */
    POSITION_REFRESH,
    /**
     * Full canBuild-term decomposition of a stalled builder's refusal.
     */
    BUILD_BREAKDOWN,
    /**
     * Who ended the game and with what state (NEXT #149: two causeless
     * early exits - won flag, frame, supply, bank - so the next one
     * arrives attributable).
     */
    GAME_END,
    /**
     * A ready building order refused before siting, with the refusing guard.
     */
    BUILD_REFUSED,
    /**
     * A picked early site (type, tile, supply row).
     */
    CONSTRUCTION_PREPARING,
    /**
     * A construction cancelled (type, reason, tile).
     */
    CONSTRUCTION_CANCEL,
    /**
     * The finder returned no tile for a ready order.
     */
    PLACEMENT_FAILED,
    /**
     * A tech decision vector (refusing gate included), any researcher.
     */
    TECH_CHECK,
    /**
     * An upgrade level the game never had before (L1/L2/L3 proof).
     */
    UPGRADE_COMPLETED,
    /**
     * A warp matched no reservation; oldest-waiting fallback adopted it, with candidates.
     */
    ADOPTED_CONSTRUCTION,
    /**
     * An IN_PROGRESS warp older than any legal build with a dead builder
     * and a warping entity still on the site (NEXT #150, cid-331 shape):
     * the demote path is shielded by its own self-match, so without this
     * row the freeze is silent. Names only, never removes.
     */
    FROZEN_WARP,
    /**
     * A stalled IN_PROGRESS construction demoted back to NOT_STARTED
     * (NEXT #70 + #150 lifecycle): cid, type, tile and which demote
     * path fired (builder gone vs stood-still). Fires per demote, not
     * once per construction - a re-armed construction can stall again.
     */
    DEMOTE_STALLED,
    /**
     * A V2 supply-provider goal reached the scheduler, with the emitting
     * list (WO-144 open question: which goal re-fires a duplicate Pylon
     * pair - build-order re-fire, pipeline overlap, or a door hole).
     */
    V2_GOAL_SOURCE,
    /**
     * A wall-piece type warped: tile vs the natural anchor row and vs the
     * exact planned tile (#97 M4 geometry gap - did the piece land where
     * the wall planner put it). anchorDist -1 means the map offered no
     * anchor row, not missing data.
     */
    WALL_ANCHOR_WARP,
    /**
     * A V2 plan item dispatched (or due but refused), with the director's
     * reason - the V2 half of the decision audit; legacy answers in
     * DECISIONS.log. Re-offers repeat while pending (idempotent builds), so
     * a long pending reads as many rows with the same tile.
     */
    V2_DISPATCH,
    /**
     * A worker stood still past the anomaly threshold, with its state.
     */
    STUCK_WORKER,
    /**
     * The worker pump refused, with the refusing gate.
     */
    WORKER_GATE,
    /**
     * Cluster posture/tactic/size/anchor, throttled.
     */
    CLUSTER,
    /**
     * Global mission transition (from, to, reason). Rows only on change.
     */
    MISSION,
    /**
     * A Nexus train/queue attempt (supply, remaining, engine answer).
     */
    TRAIN_ATTEMPT,
    /**
     * A catalogue answer for a legacy site search: answered tile or decline reason.
     */
    CATALOGUE_SITE,
    /**
     * A scout's tile next to the known enemy main, throttled (NEXT #187):
     * a trail clustered at the main is watching, at far expos wandering.
     */
    SCOUT_WATCH,
    /**
     * A kiting step or a kite with nowhere to go (dist x10, cooldown, result).
     */
    KITE,
    /**
     * A wanted item the scheduler could not place: no placement tile, no
     * affordable frame, no free producer. Transition-gated (one row per
     * item per reason), so an 18k-frame stall names its door once.
     */
    SCHEDULE_REFUSED,
    /**
     * Strength decomposition at contact transitions (NEXT #2 follow-up):
     * ours/enemy totals plus the visible-vs-remembered split, so a jump
     * on contact loss names which term moved. Rows only on transitions.
     */
    STRENGTH_WATCH,
    /**
     * The attack decision: where the army goes and why (NEXT #206 family).
     * Rows only when the decision changes - the same target re-asked is
     * not a new decision.
     */
    ATTACK_DECISION,
    /**
     * A hold decision: march, or which gate broke it (unit, target, tactic, verdict).
     */
    HOLD,
    /**
     * A healthy unit stepping out of a withdrawing ally's escape ray (helper, escaper, dest).
     */
    MAKE_WAY,
    /**
     * Covering the pursuer of a withdrawing ally instead of the nearest (helper, pursuer, escaper).
     */
    COVER,
    /**
     * Damage taken while fighting (lost HP+shields, dist x10, cooldown, moving).
     */
    HIT,
    /**
     * Attack order issued or animation hold (dist x10, cooldown, result).
     */
    SHOT,
    /**
     * Per-unit fight trace, throttled (hp, shields, dist x10 to nearest enemy).
     */
    UNIT_TRACE,
    /**
     * Per-second unit telemetry (hp, shields, dist x10 to nearest foe/dragoon/zealot, foes in 2).
     */
    UNIT_TELEMETRY,
    /**
     * Our unit died (type, frame, dist x10 to nearest enemy at death).
     */
    UNIT_DIED,
    /**
     * A unit ordered to move/attack stopped displacing (unit, still frames, order).
     */
    FROZEN,
    /**
     * Engaging a target that is not visible (unit, target, dist x10): the order cannot land.
     */
    GHOST_TARGET,
    /**
     * A supply provider wanted but suppressed by coming (unfinished) ones: who blocks.
     */
    SUPPLY_BLOCKED,
    /**
     * A combat unit still with no enemy near and weapon ready (unit, still frames, foe dist): the post-fight freeze signature.
     */
    IDLE,
    /**
     * The whole army still with no enemy near (center, count, still frames): the all-units freeze.
     */
    STALL,
    /**
     * A base order the refresh sees (throttled): type, status, bank, supply.
     */
    QUEUE_BASE,
    /**
     * The feeder's filled gap (throttled): which goal got its missing row.
     */
    BOARD_FED,
    /**
     * Free supply above 14 (edge-triggered): overstock, with bank and pylons coming.
     */
    SUPPLY_OVERSTOCK,
    /**
     * 3+ Pylons unfinished at once (edge-triggered): pipeline overlap allows two.
     */
    PYLON_FLOOD,
    /**
     * Same-type warps landing within 240 frames (owner 2026-10-19: Pylons
     * land 10-15 s apart; closer is a duplicate signal, not proof -
     * pipeline overlap also lands close).
     */
    CLOSE_WARPS,
    /**
     * Bank above 800 (edge-triggered): hoard, with free/reserved/not-ready/worker-gate why.
     */
    BANK_HOARD;

    private static final java.util.Set<LogKey> HIGH = java.util.EnumSet.of(
        LogKey.REQUEST_REFUSED_DUPLICATE,
        LogKey.BUILD_REFUSED,
        LogKey.CONSTRUCTION_CANCEL,
        LogKey.CLOSE_WARPS,
        LogKey.STALL
    );

    private static final java.util.Set<LogKey> CRITICAL = java.util.EnumSet.of(
        LogKey.BUILD_BREAKDOWN,
        LogKey.PLACEMENT_FAILED,
        LogKey.SUPPLY_OVERSTOCK,
        LogKey.PYLON_FLOOD,
        LogKey.BANK_HOARD
    );

    /**
     * Severity policy, centralized: most families are MEDIUM chatter,
     * HIGH is friction worth filtering (churn loops, refusals, cancels),
     * CRITICAL is game-breaking (a wanted building got nothing, a builder
     * stuck past the standstill threshold). New keys pick explicitly by
     * editing the sets above - the compiler cannot, so
     * {@code LogKeySeverityTest} pins the intended set.
     */
    public LogLevel level() {
        if (CRITICAL.contains(this)) return LogLevel.CRITICAL;
        if (HIGH.contains(this)) return LogLevel.HIGH;

        return LogLevel.MEDIUM;
    }
}
