package tests.unit;

import atlantis.game.A;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ATest extends AbstractTestWithUnits {
    @Test
    public void decisionAllowedLogic() {
        int value = 33;

        System.out.println(A.gradual(value, 0, 100, 20, 30));

        assertEquals(
            A.gradual(value, 0, 100, 20, 30), 23.3, 0.1
        );
    }
}
