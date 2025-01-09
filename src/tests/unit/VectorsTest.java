package tests.unit;

import atlantis.util.Angle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorsTest extends AbstractTestWithUnits {
    @Test
    public void degreesToRadians() {
        assertEquals(0, Angle.degreesToRadians(0), 0.001);
        assertEquals(Math.PI / 2, Angle.degreesToRadians(90), 0.001);
        assertEquals(Math.PI, Angle.degreesToRadians(180), 0.001);
        assertEquals(3 * Math.PI / 2, Angle.degreesToRadians(270), 0.001);
    }
}
