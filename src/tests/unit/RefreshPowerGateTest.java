package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.builders.RefreshConstructionPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The refresh pass re-sites a power-needing building held on a tile no
 * finished Pylon covers and no warping Pylon will cover (live TauCross:
 * Gateway [12,54] with no covering Pylon - 80 s of refused builds,
 * "took too long" cancel, re-request the same tile, one Gateway all
 * game). A warping Pylon that already covers the site is not hopeless:
 * power is on its way and the row correctly waits for it. Gas and
 * power-free buildings never trip this gate.
 *
 * <p>Pure discriminator pins (no map, no finder), next to the occupancy
 * gate ({@code RefreshOccupancyGateTest}): the refresh half reads this
 * seam, so a red here names the gate instead of a whole game.</p>
 */
@Tag("production")
public class RefreshPowerGateTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    private void check(boolean expected, APosition site, AUnitType type, String message,
                       FakeUnit... units) {
        FakeUnit[] all = new FakeUnit[units.length + 1];
        all[0] = fake(AUnitType.Protoss_Nexus, 30, 30);
        System.arraycopy(units, 0, all, 1, units.length);

        world(5, fakeOurs(all), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(
                RefreshConstructionPosition.siteHopelesslyUnpowered(site, type) == expected,
                message + " at " + site);
        });
    }

    @Test
    public void unpoweredSiteWithNoPylonIsHopeless() {
        check(true, APosition.create(40, 40), AUnitType.Protoss_Gateway,
            "no Pylon anywhere - the engine refuses every attempt");
    }

    @Test
    public void poweredSiteIsNotHopeless() {
        check(false, APosition.create(24, 19), AUnitType.Protoss_Gateway,
            "a finished Pylon covers the site - the warp goes through",
            completedPylonAt(20, 20));
    }

    @Test
    public void incomingCoveringPylonIsNotHopeless() {
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 20, 20);
        warping.setCompleted(false);
        check(false, APosition.create(24, 19), AUnitType.Protoss_Gateway,
            "a warping Pylon already covers the site - power is on its way",
            warping);
    }

    @Test
    public void incomingButNotCoveringPylonIsHopeless() {
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 60, 60);
        warping.setCompleted(false);
        check(true, APosition.create(24, 19), AUnitType.Protoss_Gateway,
            "a warping Pylon that cannot reach the site is not cover",
            warping, completedPylonAt(80, 80));
    }

    @Test
    public void gasBuildingNeverTripsPower() {
        check(false, APosition.create(40, 40), AUnitType.Protoss_Assimilator,
            "gas needs no psi - power is never the blocker");
    }

    @Test
    public void pylonItselfNeverTripsPower() {
        check(false, APosition.create(40, 40), AUnitType.Protoss_Pylon,
            "Pylons are power, they never wait for it");
    }

    private static FakeUnit completedPylonAt(int tx, int ty) {
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, tx, ty);
        pylon.setCompleted(true);

        return pylon;
    }
}
