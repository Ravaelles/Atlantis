package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * WO-150b slice 1: the warping-building HP progress signal. Static HP
 * over the window names a frozen warp; any movement, a single sample,
 * or no samples in the window reads as NOT frozen (fail-open: an
 * unproven freeze must never demote). Explicit frames, no clock.
 */
@Tag("production")
public class WarpHpSignalTest extends WorldStubForTests {

    @Test
    public void staticHpOverWindowIsFrozen() {
        Construction c = new Construction(AUnitType.Protoss_Gateway);

        c.noteWarpHp(1000, 300);
        c.noteWarpHp(1100, 300);
        c.noteWarpHp(1240, 300);

        assertTrue(c.isWarpHpStatic(240, 1240),
            "hp unmoved for a full window: frozen");
    }

    @Test
    public void movingHpIsNotFrozen() {
        Construction c = new Construction(AUnitType.Protoss_Gateway);

        c.noteWarpHp(1000, 300);
        c.noteWarpHp(1100, 300);
        c.noteWarpHp(1200, 305);

        assertFalse(c.isWarpHpStatic(240, 1200),
            "a progressing warp is never named frozen");
    }

    @Test
    public void singleSampleNeverFreezes() {
        Construction c = new Construction(AUnitType.Protoss_Gateway);

        c.noteWarpHp(1000, 300);

        assertFalse(c.isWarpHpStatic(240, 1000),
            "one sample proves nothing - fail open");
        assertFalse(new Construction(AUnitType.Protoss_Gateway).isWarpHpStatic(240, 5000),
            "no samples at all: fail open");
    }

    @Test
    public void staleSamplesFallOutOfTheWindow() {
        Construction c = new Construction(AUnitType.Protoss_Gateway);

        c.noteWarpHp(100, 300);
        c.noteWarpHp(200, 300);
        c.noteWarpHp(1100, 300);

        assertFalse(c.isWarpHpStatic(240, 1100),
            "one recent sample is not a window: fail open");

        c.noteWarpHp(1200, 300);
        assertTrue(c.isWarpHpStatic(240, 1200),
            "two recent samples, static: frozen");
    }

    @Test
    public void trailStaysBounded() {
        Construction c = new Construction(AUnitType.Protoss_Gateway);

        for (int f = 0; f <= 5000; f += 10) c.noteWarpHp(f, 300);

        assertTrue(c.isWarpHpStatic(240, 5000),
            "long freeze still reads frozen after pruning");
    }
}
