package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.protoss.ProtossWarping;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A Protoss building that just warped (exists but is not completed yet)
 * keeps its construction listed as IN_PROGRESS until it completes - the
 * painters iterate the list, so an uncompleted warp that leaves it would
 * be invisible in painting. Completion still finishes it (no leak back
 * to the pre-#53 shape): the unit-completed event removes exactly the
 * construction the warp linked.
 */
@Tag("infra")
public class WarpingVisibleWhileWarpingTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void uncompletedWarpStaysListedAsInProgressThenFinishesOnCompletion() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setBuilder(probe);
            construction.setProductionOrder(order);
            construction.setPositionToBuild(APosition.create(12, 10));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(construction);

            // Warp start: the building exists but is not completed.
            FakeUnit warping = fake(AUnitType.Protoss_Pylon, 12, 10);
            warping.setCompleted(false);

            ProtossWarping.updateNewBuildingJustWarped(warping);

            assertEquals(
                ConstructionOrderStatus.IN_PROGRESS, construction.status(),
                "a warping building's construction stays IN_PROGRESS, not FINISHED");
            assertTrue(
                ConstructionRequests.constructions.contains(construction),
                "a warping building stays listed (painters iterate the list)");
            assertNull(construction.builder(), "and its worker is still released");
            assertSame(warping.construction(), construction, "unit links back");

            // Completion: the unit-completed event finishes exactly this one.
            warping.setCompleted(true);
            atlantis.game.listeners.OnOurNewUnitCompleted.ourNewUnitCompleted(warping);

            assertFalse(
                ConstructionRequests.constructions.contains(construction),
                "a completed building leaves the list (no pre-#53 leak)");
            assertEquals(OrderStatus.FINISHED, order.status(), "order closes with it");
            assertNull(warping.construction(), "unit unlinks");
        });
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }
}
