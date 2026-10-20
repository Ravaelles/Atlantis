package tests.unit;

import atlantis.game.A;
import atlantis.util.AMath;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("infra")
public class ATest extends AbstractTestWithUnits {
    @Test
    public void decisionAllowedLogic() {
        int value = 33;

        assertEquals(
            AMath.gradual(value, 0, 100, 20, 30), 23.3, 0.1
        );
    }
}
