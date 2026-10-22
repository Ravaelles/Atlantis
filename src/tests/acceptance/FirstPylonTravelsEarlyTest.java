package tests.acceptance;

import atlantis.production.constructions.builders.ShouldNotTravelToConstructYet;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-16: the first Pylon warped with ~130 banked against its 100
 * price - the builder arrived with a stockpile instead of just in time.
 * The travel gate already discounts the walk (1.3 minerals per tile), but
 * for the very first Pylon that still lands arrival past affordability.
 * So the first Pylon travels 24 minerals earlier: the builder waits briefly
 * at the site (cheap) rather than the base waiting on supply (expensive -
 * probe production blocks at 9/9 without it).
 *
 * <p>Numbers: price 100, margin 16, distance 8 tiles (10.4 discounted) -
 * travel used to unlock at ~74 banked, now at ~50. Only while no Pylon
 * stands: later Pylons keep the old threshold.</p>
 */
public class FirstPylonTravelsEarlyTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        fired = false;
    }

    @Test
    public void firstPylonLeavesTwentyFourMineralsEarly() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 11)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;

            currentGas = 100;

            currentMinerals = 40;
            assertTrue(
                ShouldNotTravelToConstructYet.check(AUnitType.Protoss_Pylon, 8.0),
                "at 40 banked the builder still waits - the bonus is 24, not a free pass");

            currentMinerals = 55;
            assertFalse(
                ShouldNotTravelToConstructYet.check(AUnitType.Protoss_Pylon, 8.0),
                "at 55 banked the first-Pylon builder is already walking "
                    + "(old threshold ~74 arrived with a stockpile)");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void laterPylonsKeepTheOldThreshold() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 20, 20),
            fake(AUnitType.Protoss_Probe, 11, 11)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;

            currentGas = 100;
            currentMinerals = 55;
            assertTrue(
                ShouldNotTravelToConstructYet.check(AUnitType.Protoss_Pylon, 8.0),
                "with a Pylon standing the 24 bonus is gone - old threshold holds");

            currentMinerals = 80;
            assertFalse(
                ShouldNotTravelToConstructYet.check(AUnitType.Protoss_Pylon, 8.0),
                "at 80 banked (past ~74) even later Pylons walk");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
