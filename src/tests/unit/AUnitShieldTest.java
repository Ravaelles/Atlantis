package tests.unit;

import atlantis.units.AUnitShield;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Pins the shield predicates through the unit and through the seam
 * (NEXT #11): full shields, half shields and the NaN contract read the
 * same both ways, so the extraction keeps every assertion green.
 */
@Tag("infra")
public class AUnitShieldTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void fullShieldReadsHealthy() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 10, 10);

        world(1, fakeOurs(zealot), fakeEnemies(), () -> {
            assertTrue(zealot.shieldHealthy());
            assertTrue(AUnitShield.shieldHealthy(zealot));
            assertFalse(zealot.shieldWounded());
            assertFalse(AUnitShield.shieldWounded(zealot));
            assertEquals(100.0, zealot.shieldPercent(), 0.001);
            assertEquals(100.0, AUnitShield.shieldPercent(zealot), 0.001);
            assertEquals(0.0, zealot.shieldWound(), 0.001);
            assertEquals(0.0, AUnitShield.shieldWound(zealot), 0.001);
        });
    }

    @Test
    public void halfShieldReadsWounded() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 10, 10);

        world(1, fakeOurs(zealot), fakeEnemies(), () -> {
            zealot.setShields(zealot.maxShields() / 2);

            assertTrue(zealot.shieldWounded());
            assertTrue(AUnitShield.shieldWounded(zealot));
            assertFalse(zealot.shieldHealthy());
            assertEquals(50.0, zealot.shieldPercent(), 0.001);
            assertEquals(50.0, AUnitShield.shieldPercent(zealot), 0.001);
            assertEquals(50.0, zealot.shieldWoundPercent(), 0.001);
            assertEquals(50.0, AUnitShield.shieldWoundPercent(zealot), 0.001);
        });
    }

    @Test
    public void shieldlessUnitReadsNaNOnBothSides() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10, 10);

        world(1, fakeOurs(marine), fakeEnemies(), () -> {
            assertTrue(Double.isNaN(marine.shieldPercent()));
            assertTrue(Double.isNaN(AUnitShield.shieldPercent(marine)));
            assertTrue(Double.isNaN(marine.shieldWound()));
            // The boolean accessors stay meaningful (pinned in AUnitTest):
            // full-by-construction reads healthy, never wounded.
            assertTrue(marine.shieldHealthy());
            assertTrue(AUnitShield.shieldHealthy(marine));
            assertFalse(marine.shieldWounded());
            assertFalse(AUnitShield.shieldWounded(marine));
        });
    }

    @Test
    public void damageBoundsReadThroughTheSeam() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);

        world(1, fakeOurs(dragoon), fakeEnemies(), () -> {
            int half = dragoon.maxShields() / 2;
            dragoon.setShields(half);

            assertTrue(dragoon.shieldDamageAtMost(half));
            assertTrue(AUnitShield.shieldDamageAtMost(dragoon, half));
            assertFalse(dragoon.shieldDamageAtLeast(half + 1));
            assertFalse(AUnitShield.shieldDamageAtLeast(dragoon, half + 1));
            assertTrue(dragoon.shieldDamageAtLeast(
                dragoon.maxShields() - dragoon.shields()));
            assertTrue(AUnitShield.shieldDamageAtLeast(
                dragoon, dragoon.maxShields() - dragoon.shields()));
        });
    }
}
