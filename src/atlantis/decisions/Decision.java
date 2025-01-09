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

    public static Decision FALSE(String reason) {
        Decision decision = FALSE;
        decision.reason = reason;
        return decision;
    }

    public static Decision TRUE(String reason) {
        Decision decision = TRUE;
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

//    public boolean currentValue() {
//        return currentValue;
//    }

//    public Decision changeTo(Decision newDecision) {
//        if (newDecision.isIndifferent) {
////            this.isIndifferent = true;
//            return Decision.INDIFFERENT;
//        }
//
////        this.currentValue = newDecision.toBoolean();
//    }

//    public boolean initialValue() {
//        return initialValue;
//    }

    @Override
    public String toString() {
        String string;

        if (this.equals(TRUE)) {
            string = "TRUE";
        }
        else if (this.equals(ALLOWED)) {
            string = "ALLOWED";
        }
        else if (this.equals(INDIFFERENT)) {
            string = "INDIFFERENT";
        }
        else if (this.equals(FORBIDDEN)) {
            string = "FORBIDDEN";
        }
        else if (this.equals(FALSE)) {
            string = "FALSE";
        }
        else {
            string = "UNKNOWN";
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
