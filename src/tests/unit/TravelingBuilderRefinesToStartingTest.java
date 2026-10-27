package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A NOT_STARTED construction with a live assigned builder that has not
 * started constructing is travelling, not waiting (owner 2026-10-19:
 * the travel half had no status). The changer refines it to STARTING;
 * producers keep writing NOT_STARTED, so no producer changes.
 */
@Tag("production")
public class TravelingBuilderRefinesToStartingTest extends WorldStubForTests {

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
    public void travellingBuilderRefines() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), builder);

            assertTrue(ConstructionStatusChanger.refineToStartingIfTravelling(construction, builder));
            assertEquals(ConstructionOrderStatus.STARTING, construction.status());
        });
    }

    @Test
    public void constructingBuilderIsAdvancersCase() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 92, 111).setConstructing(true);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), builder);

            assertFalse(ConstructionStatusChanger.refineToStartingIfTravelling(construction, builder));
            assertEquals(ConstructionOrderStatus.NOT_STARTED, construction.status());
            assertTrue(ConstructionStatusChanger.advanceToInProgressIfStarted(construction, builder));
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, construction.status());
        });
    }

    @Test
    public void noBuilderStaysNotStarted() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), null);

            assertFalse(ConstructionStatusChanger.refineToStartingIfTravelling(construction, null));
            assertEquals(ConstructionOrderStatus.NOT_STARTED, construction.status());
        });
    }

    @Test
    public void standingBuildingIsAdvancersCase() {
        FakeUnit standing = fake(AUnitType.Protoss_Gateway, 92, 111);
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(standing, builder), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), builder);

            assertFalse(ConstructionStatusChanger.refineToStartingIfTravelling(construction, builder),
                "a finished building on the site is completion, not travel");
        });
    }

    @Test
    public void advanceAcceptsStarting() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 92, 111).setConstructing(true);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), builder);
            construction.setStatus(ConstructionOrderStatus.STARTING);

            assertTrue(ConstructionStatusChanger.advanceToInProgressIfStarted(construction, builder),
                "a STARTING builder that starts must advance, or STARTING never leaves");
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, construction.status());
        });
    }

    @Test
    public void inProgressIsUntouchedByRefine() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 92, 111);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            Construction construction = notStartedAt(APosition.create(92, 111), builder);
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);

            assertFalse(ConstructionStatusChanger.refineToStartingIfTravelling(construction, builder));
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, construction.status());
        });
    }
}
