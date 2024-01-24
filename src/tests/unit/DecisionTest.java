package tests.unit;

import atlantis.decions.Decision;
import org.junit.Test;

import static org.junit.Assert.*;

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
}
