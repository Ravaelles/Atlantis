package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.builders.RefreshConstructionPosition;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
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
 * A refresh pass re-sites only onto completed-building footprints -
 * never onto transient traffic (owner log: a Pylon re-placed twice in
 * minutes while miners crossed the site, "Refresh Nexus position"
 * spam for 4 minutes). A worker crossing the site is traffic, not a
 * verdict. A warping building is the construction itself in progress.
 * A standing building is hopeless.
 *
 * <p>Pure discriminator pins (no map, no finder): the cancel half of
 * the same contract rides the acceptance tier
 * (RefreshCancelNeedsBuildingTest).</p>
 */
@Tag("production")
public class RefreshOccupancyGateTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    private void check(boolean expected, APosition site, String message, FakeUnit... units) {
        FakeUnit[] all = new FakeUnit[units.length + 1];
        all[0] = fake(AUnitType.Protoss_Nexus, 30, 30);
        System.arraycopy(units, 0, all, 1, units.length);

        world(5, fakeOurs(all), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(
                RefreshConstructionPosition.siteHopelesslyBlocked(site, AUnitType.Protoss_Pylon) == expected,
                message + " at " + site);
        });
    }

    @Test
    public void crossingWorkerNeverBlocks() {
        check(false, APosition.create(12, 10),
            "a crossing worker is traffic, not a verdict",
            fake(AUnitType.Protoss_Probe, 11, 10),
            fake(AUnitType.Protoss_Probe, 12, 10));
    }

    @Test
    public void ownBuilderNeverBlocks() {
        check(false, APosition.create(12, 10),
            "the arriving builder must not trip its own site",
            fake(AUnitType.Protoss_Probe, 12, 10));
    }

    @Test
    public void warpingBuildingNeverBlocks() {
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 12, 10);
        warping.setCompleted(false);

        check(false, APosition.create(12, 10),
            "a warping building is the construction itself in progress",
            warping);
    }

    @Test
    public void standingBuildingBlocks() {
        check(true, APosition.create(12, 10),
            "a standing building on the site is hopeless",
            fake(AUnitType.Protoss_Pylon, 12, 10));
    }

    @Test
    public void emptySiteNeverBlocks() {
        check(false, APosition.create(50, 50),
            "an empty site is never hopeless");
    }

    @Test
    public void geyserDoesNotBlockItsOwnRefinery() {
        // #237, live root cause (twin named VespeneG@7,39#82 as the
        // blocker): the overlap guard ran before the geyser exemption,
        // so every gas site read as blocked by its own geyser and the
        // refresh cancelled it into a 100-frame loop with zero gas.
        // The exemption owns gas tiles; overlap owns everything else.
        neutralInWorld = new FakeUnit[]{ fake(AUnitType.Resource_Vespene_Geyser, 40, 40) };
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 38, 38);

        world(3, fakeOurs(fake(AUnitType.Protoss_Nexus, 30, 30), probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(
                CanPhysicallyBuildHere.check(probe, AUnitType.Protoss_Assimilator, APosition.create(40, 40)),
                "the geyser IS the site, not an obstacle standing on it");
        });
    }
}
