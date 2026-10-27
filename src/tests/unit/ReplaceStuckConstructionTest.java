package tests.unit;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.CancelTooLongConstructions;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeMapTiles;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A stuck construction gets a fresh site first - and only loses the old one
 * when the fresh site is genuinely different.
 *
 * <p>
 * Late-game supply cap (33/33) with a Pylon that never starts: blindly
 * cancelling loses the worker assignment, and re-requesting may land on the
 * same dead tile. So the replacement sends a fresh request with the old
 * construction still alive (the duplicate guard sees it): a different site
 * replaces the old one, anything else keeps it.
 * </p>
 */
@Tag("production")
public class ReplaceStuckConstructionTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
        FakeMapTiles.installAsSource();
    }

    @Test
    public void aBlockedSiteIsReplacedByAFreeOne() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 24, 22);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);
        // Something standing on the old site: the search must step past it.
        FakeUnit blocker = fake(AUnitType.Protoss_Probe, 40, 40);

        MapTiles.useSource(new FakeMapTiles());

        world(10, fakeOurs(nexus, pylon, probe, blocker), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 5) return;

            Construction old = new Construction(AUnitType.Protoss_Pylon);
            old.setBuilder(probe);
            old.setPositionToBuild(APosition.create(40, 40));
            old.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(old);

            assertTrue(
                CancelTooLongConstructions.tryReplaceWithFreshSite(
                    old, AUnitType.Protoss_Pylon, APosition.create(40, 40)),
                "a free tile nearby must replace the blocked site");

            assertFalse(
                ConstructionRequests.constructions.contains(old),
                "the stuck construction is gone once the fresh one owns a builder");

            assertEquals(
                1,
                ConstructionRequests.constructions.stream()
                    .filter(c -> c.buildingType().isPylon()).count(),
                "exactly one Pylon construction survives - no leak, no duplicate");
        });
    }

    @Test
    public void withoutAFreeSiteTheOldConstructionSurvives() {
        // Placement blocks us: no replacement exists, so the decision keeps
        // the original. (End to end, a missing fresh request returns null from
        // the search - e.g. no geyser for a refinery - and this is the branch
        // that runs. Here the decision itself is pinned directly.)
        Construction old = new Construction(AUnitType.Protoss_Pylon);
        old.setPositionToBuild(APosition.create(40, 40));

        assertFalse(
            CancelTooLongConstructions.shouldReplace(
                APosition.create(40, 40), null, AUnitType.Protoss_Pylon),
            "no replacement: the old construction must survive");

        Construction sameSpot = new Construction(AUnitType.Protoss_Pylon);
        sameSpot.setPositionToBuild(APosition.create(41, 40));

        assertFalse(
            CancelTooLongConstructions.shouldReplace(
                APosition.create(40, 40), sameSpot, AUnitType.Protoss_Pylon),
            "same footprint: cancelling would only re-create the same request");
    }

    @Test
    public void samenessIsMeasuredInFootprints() {
        APosition site = APosition.create(40, 40);

        assertFalse(
            CancelTooLongConstructions.isDifferentSite(site, APosition.create(40, 40), AUnitType.Protoss_Pylon),
            "the same tile is not a fresh site");

        assertFalse(
            CancelTooLongConstructions.isDifferentSite(site, APosition.create(41, 40), AUnitType.Protoss_Pylon),
            "one tile over still overlaps the 2x2 footprint");

        assertTrue(
            CancelTooLongConstructions.isDifferentSite(site, APosition.create(50, 50), AUnitType.Protoss_Pylon),
            "ten tiles away is genuinely different");
    }
}
