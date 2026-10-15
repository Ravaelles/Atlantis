package atlantis.util.log;

/**
 * Families of structured diagnostic events. One value per log family that
 * moved off plain strings - never per message: the variance lives in the
 * parameters, so grepping thousands of near-identical lines becomes
 * filtering one key.
 */
public enum LogKey {
    /** A queued order refused to go READY, with the refusing term. */
    ORDER_NOT_READY,
    /** The worker pump refused, with the refusing gate. */
    WORKER_GATE,
    /** A worker stood still past the anomaly threshold, with its state. */
    STUCK_WORKER,
    /** The goal board's opinion snapshot next to legacy's actions. */
    BOARD,
}
