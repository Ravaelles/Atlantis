package atlantis.util.log;

/**
 * Families of structured diagnostic events. One value per log family that
 * moved off plain strings - never per message: the variance lives in the
 * parameters, so grepping thousands of near-identical lines becomes
 * filtering one key.
 */
public enum LogKey {
    /** A worker was assigned to a prepared site: adoption, with the state. */
    BUILDING_STARTED,
    /** A fresh construction request was accepted: who asked, for which order. */
    CONSTRUCTION_REQUESTED,
    /** The goal board's opinion snapshot next to legacy's actions. */
    BOARD,
    /** A queued order refused to go READY, with the refusing term. */
    ORDER_NOT_READY,
    /** Per-second builder snapshot while it owns a construction. */
    BUILDER_STATE,
    /** The issue path was reached (gates inside decide). */
    ISSUE_BUILD_REACHED,
    /** A build order attempt with the engine preconditions. */
    BUILD_ATTEMPT,
    /** A request refused as duplicate, with the standing count. */
    REQUEST_REFUSED_DUPLICATE,
    /** Power inputs at decision time for power-needing buildings. */
    POWER_AT_REQUEST,
    /** Gas tile fix path (geyser found, snapped, or missing). */
    GAS_FIX,
    /** Singularity Charge decision inputs. */
    SINGULARITY_APPLIES,
    /** Dragoon pump decision inputs. */
    DRAGOON_TRAIN,
    /** A refresh attempt outcome (refreshed, or cancelled + re-requested). */
    POSITION_REFRESH,
    /** A worker stood still past the anomaly threshold, with its state. */
    STUCK_WORKER,
    /** The worker pump refused, with the refusing gate. */
    WORKER_GATE,
}
