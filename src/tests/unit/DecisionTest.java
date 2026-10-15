package tests.unit;

import atlantis.decisions.Decision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecisionTest extends AbstractTestWithUnits {
    @Test
    public void decisionAllowedLogic() {
        Decision decision = Decision.TRUE;

        assertTrue(decision.isAllowed());
        assertTrue(decision.isTrue());
        assertTrue(decision.notIndifferent());
        assertTrue(decision.toBoolean());

        assertFalse(decision.isIndifferent());
        assertFalse(decision.isForbidden());
        assertFalse(decision.isFalse());

        decision = Decision.ALLOWED;

//        System.err.println("decision = " + decision);
//        System.err.println("decision.toBoolean() = " + decision.toBoolean());
//        System.err.println("decision.isAllowed() = " + decision.isAllowed());
//        System.err.println("decision.isForbidden() = " + decision.isForbidden());

        assertTrue(decision.isAllowed());
        assertTrue(decision.isTrue());
        assertTrue(decision.notIndifferent());
        assertTrue(decision.toBoolean());

        assertFalse(decision.isIndifferent());
        assertFalse(decision.isForbidden());
        assertFalse(decision.isFalse());
    }

    @Test
    public void decisionForbiddenLogic() {
        Decision decision = Decision.FORBIDDEN;

        assertFalse(decision.isAllowed());
        assertFalse(decision.isTrue());
        assertFalse(decision.isIndifferent());
        assertFalse(decision.toBoolean());

        assertTrue(decision.notIndifferent());
        assertTrue(decision.isForbidden());
        assertTrue(decision.isFalse());

        decision = Decision.FALSE;

        assertFalse(decision.isAllowed());
        assertFalse(decision.isTrue());
        assertFalse(decision.isIndifferent());
        assertFalse(decision.toBoolean());

        assertTrue(decision.notIndifferent());
        assertTrue(decision.isForbidden());
        assertTrue(decision.isFalse());
    }

    @Test
    public void decisionChanging() {
        Decision decision = Decision.ALLOWED;

        assertTrue(decision.isAllowed());

        decision = Decision.FORBIDDEN;

        assertFalse(decision.isAllowed());

        decision = decision.getResetted();

//        System.err.println("decision = " + decision);
    }

    @Test
    public void reasonsDoNotLeakBetweenCallSites() {
        Decision first = Decision.FALSE("first");
        Decision second = Decision.FALSE("second");

        assertTrue("first".equals(first.reason()));
        assertTrue("second".equals(second.reason()));
        assertTrue(Decision.FALSE.reason() == null || "".equals(Decision.FALSE.reason()),
            "the shared singleton must stay reason-free, got: " + Decision.FALSE.reason());
    }

    @Test
    public void toStringReadsStateNotIdentity() {
        assertTrue(Decision.TRUE("x").toString().contains("TRUE"));
        assertTrue(Decision.FALSE("y").toString().contains("FALSE"));
        assertTrue(Decision.INDIFFERENT.toString().contains("INDIFFERENT"));
    }
}
