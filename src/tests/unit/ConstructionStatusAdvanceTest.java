package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A NOT_STARTED construction whose builder started must become
 * IN_PROGRESS - staying NOT_STARTED while the worker builds (or the
 * building warps) is the stuck-status defect behind NEXT #150.
 *
 * <p>Owner 2026-10-17, Wine opening: Gateway #2 sat NOT_STARTED while
 * its builder worked, and a Pylon showed no construction at all while
 * PREPARING. The advance below is the transition that has to fire;
 * the test drives it through the same static seam the frame loop uses,
 * with a constructing builder at the site, a walking one, and none.</p>
 */
public class ConstructionStatusAdvanceTest extends WorldStubForTests {

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    private Construction notStartedAt(APosition site, FakeUnit builder) {
        Construction construction = new Construction(AUnitType.Protoss_Gateway);
        construction.setPositionToBuild(site);
        construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
        construction.setBuilder(builder);
        ConstructionRequests.constructions.add(construction);
        return construction;
    }

    @Test
    public void constructingBuilderAtSiteAdvances() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 92, 111).setConstructing(true);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), builder);

            assertTrue(ConstructionStatusChanger.advanceToInProgressIfStarted(construction, builder));
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, construction.status());
        });
    }

    @Test
    public void walkingBuilderDoesNotAdvance() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 92, 111);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), builder);

            assertFalse(ConstructionStatusChanger.advanceToInProgressIfStarted(construction, builder));
            assertEquals(ConstructionOrderStatus.NOT_STARTED, construction.status());
        });
    }

    @Test
    public void distantBuilderDoesNotAdvance() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 20, 20).setConstructing(true);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), builder);

            assertFalse(ConstructionStatusChanger.advanceToInProgressIfStarted(construction, builder));
            assertEquals(ConstructionOrderStatus.NOT_STARTED, construction.status());
        });
    }

    @Test
    public void finishedBuildingOnSiteAdvancesWithoutBuilder() {
        FakeUnit standing = fake(AUnitType.Protoss_Gateway, 92, 111);

        world(1, fakeOurs(standing), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), null);

            assertTrue(ConstructionStatusChanger.advanceToInProgressIfStarted(construction, null));
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, construction.status());
        });
    }
}
