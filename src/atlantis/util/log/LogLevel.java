package atlantis.util.log;

/**
 * Severity of a diagnostic row. Orthogonal to the key (which names the
 * subsystem): most rows are NORMAL chatter, HIGH marks friction worth
 * filtering (churn loops, refusals), CRITICAL marks game-breaking states
 * (a wanted building got nothing, a builder stuck past the standstill
 * threshold). E2E gates fail on CRITICAL, never on chatter.
 */
public enum LogLevel {
    NORMAL,
    HIGH,
    CRITICAL;

    public boolean atLeast(LogLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
