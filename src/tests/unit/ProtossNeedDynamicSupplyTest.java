package tests.unit;

import atlantis.production.dynamic.supply.ProtossNeedDynamicSupply;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Protoss answer to "is another Pylon needed right now" (owner: never
 * more than 8 free supply below 40 used - measured 10+ free from
 * overlapping BO + dynamic Pylons; one Pylon at a time keeps the
 * headroom tight). Pure thresholds, no world needed.
 */
public class ProtossNeedDynamicSupplyTest {

    private final ProtossNeedDynamicSupply need = new ProtossNeedDynamicSupply();

    @Test
    public void unfinishedProviderVetoesBelowForty() {
        assertFalse(need.needsSupply(25, 33, 1), "a Pylon in flight covers the block");
        assertFalse(need.needsSupply(25, 41, 2), "two in flight cover it twice over");
    }

    @Test
    public void unfinishedProviderDoesNotVetoAtFortyPlus() {
        assertTrue(need.needsSupply(70, 76, 1), "at 40+ the pipeline pair stays - bands decide");
        assertFalse(need.needsSupply(150, 200, 1), "but free 50 is no need at any supply");
    }

    @Test
    public void earlyGameBand() {
        assertTrue(need.needsSupply(25, 25, 0), "capped at 25/25 orders now");
        assertTrue(need.needsSupply(30, 36, 0), "6 free at 30 orders");
        assertFalse(need.needsSupply(25, 33, 0), "8 free at 25/33 must wait (game-2 #30 shape)");
        assertFalse(need.needsSupply(28, 40, 0), "12 free is plenty");
    }

    @Test
    public void midGameBand() {
        assertTrue(need.needsSupply(70, 80, 0), "10 free at 70 orders");
        assertFalse(need.needsSupply(70, 82, 0), "12 free at 70 waits");
        assertTrue(need.needsSupply(100, 110, 0), "boundary 100/110 orders");
    }

    @Test
    public void lateGameBand() {
        assertTrue(need.needsSupply(150, 160, 0), "10 free at 150 orders");
        assertFalse(need.needsSupply(150, 170, 0), "20 free at 150 waits");
        assertTrue(need.needsSupply(199, 199, 0), "capped at 199/199 orders now");
    }
}
