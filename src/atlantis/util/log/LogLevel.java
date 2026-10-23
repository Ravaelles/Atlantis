package atlantis.util.log;

/**
 * Severity of a diagnostic row. Orthogonal to the key (which names the
 * subsystem): LOW is routine flow worth keeping, MEDIUM is unclassified
 * chatter (the default - an untagged call site did not classify itself),
 * HIGH marks friction worth filtering (churn loops, refusals), CRITICAL
 * marks game-breaking states (a wanted building got nothing, a builder
 * stuck past the standstill threshold). E2E gates fail on CRITICAL,
 * never on chatter.
 *
 * <p>Every rendered line carries its tag (CONVENTIONS §27): console lines
 * read {@code [LOW]}/{@code [MED]}/{@code [HIGH]}/{@code [CRIT]}, event
 * rows carry the short code in {@code "lvl"}. Temporary debugging lines
 * use {@code [TEMP]} instead of a level so they are easy to find and
 * remove later.
 */
public enum LogLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    /** Console tag: {@code [LOW]} / {@code [MED]} / {@code [HIGH]} / {@code [CRIT]}. */
    public String tag() {
        switch (this) {
            case LOW:
                return "[LOW]";
            case MEDIUM:
                return "[MED]";
            case HIGH:
                return "[HIGH]";
            default:
                return "[CRIT]";
        }
    }

    /** Short {@code "lvl"} value rendered in EVENTS.jsonl. */
    public String code() {
        switch (this) {
            case LOW:
                return "LOW";
            case MEDIUM:
                return "MED";
            case HIGH:
                return "HIGH";
            default:
                return "CRIT";
        }
    }

    public boolean atLeast(LogLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
