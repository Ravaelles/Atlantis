package tests.unit;

import atlantis.units.AUnitHealth;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Pins the health predicates before/after their extraction from
 * {@link atlantis.units.AUnit} (NEXT #11, first slice): full health,
 * half health and shield-only damage read the same through the unit
 * and through the extracted class. Written against the delegates, so
 * the extraction keeps every assertion green by construction.
 */
@Tag("infra")
public class AUnitHealthTest {

    @Test
    public void fullHealthReadsClean() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 10, 10);

        assertTrue(zealot.isFullyHealthy());
        assertTrue(AUnitHealth.isFullyHealthy(zealot));
        assertFalse(zealot.isWounded());
        assertFalse(AUnitHealth.isWounded(zealot));
        assertEquals(0, zealot.woundHp());
        assertEquals(0, AUnitHealth.woundHp(zealot));
        assertEquals(100, zealot.hpPercent());
        assertTrue(zealot.hpPercent(100));
        assertTrue(zealot.woundPercentMax(0));
    }

    @Test
    public void halfHealthReadsWounded() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 10, 10);
        int half = probe.maxHp() / 2;
        probe.setHp(half);
        probe.setShields(0);

        assertFalse(probe.isFullyHealthy());
        assertTrue(probe.isWounded());
        assertEquals(50, probe.hpPercent());
        assertTrue(probe.hpPercent(50));
        assertFalse(probe.hpPercent(51));
        assertEquals(half, probe.woundHp());
        assertEquals(50.0, probe.woundPercent(), 0.001);
        assertTrue(probe.woundPercentMin(50));
        assertFalse(probe.woundPercentMin(51));
        assertTrue(probe.woundPercentMax(50));
        assertFalse(probe.woundPercentMax(49));

        assertEquals(AUnitHealth.hpPercent(probe), probe.hpPercent());
        assertEquals(AUnitHealth.woundHp(probe), probe.woundHp());
        assertEquals(AUnitHealth.woundPercent(probe), probe.woundPercent(), 0.0001);
    }

    @Test
    public void shieldOnlyDamageCountsAsWounded() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        dragoon.setHp(dragoon.maxHp());
        dragoon.setShields(0);

        assertTrue(dragoon.isWounded(),
            "full hp with empty shields is still wounded - the shield gap counts");
        assertTrue(AUnitHealth.isWounded(dragoon));
    }

    @Test
    public void thresholdsDelegateToTheSeam() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 10, 10);
        probe.setHp(10);
        probe.setShields(probe.maxShields());

        assertTrue(probe.hpLessThan(11));
        assertTrue(AUnitHealth.hpLessThan(probe, 11));
        assertFalse(probe.hpLessThan(10));
        assertTrue(probe.hpMoreThan(9));
        assertTrue(AUnitHealth.hpMoreThan(probe, 9));
        assertFalse(probe.hpMoreThan(10));
        assertFalse(probe.isHealthy());
        assertFalse(AUnitHealth.isHealthy(probe));
    }
}
