package atlantis.decisions;

public class Decision {
    public static final Decision TRUE = new Decision(true);
    public static final Decision ALLOWED = new Decision(true);
    public static final Decision INDIFFERENT = new Decision();
    public static final Decision FORBIDDEN = new Decision(false);
    public static final Decision FALSE = new Decision(false);

    private final boolean isIndifferent;
    private final int initialValue;
    private final boolean currentValue;

    private String reason = null;

    // =========================================================

    public Decision(boolean initialValue) {
        this.isIndifferent = false;
        this.initialValue = initialValue ? 1 : -1;
        this.currentValue = initialValue;
    }

    public Decision() {
        this.isIndifferent = true;
        this.initialValue = 0;
        this.currentValue = false;
    }

    // =========================================================

    public static Decision fromBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }

    /**
     * Fresh instance every call: the old form mutated the shared singletons,
     * so two call sites' reasons overwrote each other (last writer wins,
     * silent cross-talk). No caller compares identity (verified), so sharing
     * bought nothing.
     */
    public static Decision FALSE(String reason) {
        Decision decision = new Decision(false);
        decision.reason = reason;

        return decision;
    }

    /**
     * Fresh instance every call, see {@link #FALSE(String)}.
     */
    public static Decision TRUE(String reason) {
        Decision decision = new Decision(true);
        decision.reason = reason;

        return decision;
    }

    public Decision getResetted() {
        if (initialValue == 0) return Decision.INDIFFERENT;
        if (initialValue == 1) return Decision.ALLOWED;
        if (initialValue == -1) return Decision.FORBIDDEN;

        throw new RuntimeException("Unknown decision value: " + initialValue);
    }

    // =========================================================

    @Override
    public String toString() {
        // State-based, never identity-based: TRUE and ALLOWED (resp. FALSE
        // and FORBIDDEN) behave identically, so they print identically.
        // Printing them apart was the alias confusion.
        String string;
        if (isIndifferent) {
            string = "INDIFFERENT";
        }
        else {
            string = currentValue ? "TRUE" : "FALSE";
        }

        return "Decision:" + string;
    }

    public boolean isTrue() {
        if (isIndifferent) return false;

        return currentValue;
    }

    public boolean isFalse() {
        if (isIndifferent) return false;

        return !currentValue;
    }

    public boolean isAllowed() {
        if (isIndifferent) return false;

        return currentValue;
    }

    public boolean isForbidden() {
        if (isIndifferent) return false;

        return !currentValue;
    }

    public boolean isIndifferent() {
        return isIndifferent;
    }

    public boolean notIndifferent() {
        return !isIndifferent;
    }

    public boolean notForbidden() {
        if (isIndifferent) return true;

        return !currentValue;
    }

    public boolean toBoolean() {
        assert !isIndifferent;

        return currentValue;
    }

    public String reason() {
        return reason;
    }
}
