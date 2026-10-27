package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.ShouldNotTravelToConstructYet;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the current disagreement between the earlier-demand models
 * (NEXT #156): the door counts earlier demand by queue id over the
 * construction list ({@code ProduceBuilding.earlierUnfinishedMinerals}),
 * the travel forecast by queue position
 * ({@code ShouldNotTravelToConstructYet}), and the ledger
 * ({@code ReservedResources}) runs its own capped counters. Two same-need
 * rows therefore read different sums depending on which door asks - the
 * measured duplicate-Pylon shape.
 *
 * <p>When the models unify behind one ordered reserve, the divergence
 * case below flips to agreement and this test is rewritten to pin that.
 * The third model (running ledger + caps) is not fixture-comparable and
 * stays out of this pin by design.</p>
 */
@Tag("production")
public class EarlierReserveDivergenceTest extends AbstractTestWithWorld {

    private static final AUnitType PYLON = AUnitType.Protoss_Pylon;

    private static void linkNotStartedSite(ProductionOrder order) {
        Construction construction = new Construction(PYLON);
        construction.setPositionToBuild(APosition.create(30, 30));
        construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
        construction.setProductionOrder(order);
        ConstructionRequests.constructions.add(construction);
    }

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
    public void clean() {
        ConstructionRequests.constructions.clear();
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        EventLog.clearForTests();
        ProduceBuilding.clearRefusedThrottleForTests();
    }

    @Test
    public void idOrderAndQueuePositionAgreeWhenTheyCoincide() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, AbstractTestWithWorld.units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10) return;

            ProductionOrder earlier = new ProductionOrder(PYLON, 8);
            ProductionOrder later = new ProductionOrder(PYLON, 21);
            linkNotStartedSite(earlier);
            Queue.get().addNew(0, earlier);
            Queue.get().addNew(1, later);

            int price = PYLON.mineralPrice();
            assertEquals(price, ProduceBuilding.earlierUnfinishedMinerals(later),
                "door model: the earlier id's unfinished site counts");
            assertEquals(price, ShouldNotTravelToConstructYet.earlierUnpaidForTests(later)[0],
                "forecast model: the row ahead in the queue counts the same");
        });
    }

    @Test
    public void queuePositionAndIdDisagreeWhenOrderDrifts() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, AbstractTestWithWorld.units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10) return;

            ProductionOrder earlier = new ProductionOrder(PYLON, 8);
            ProductionOrder later = new ProductionOrder(PYLON, 21);
            linkNotStartedSite(earlier);
            // Same two rows, queue order drifted from id order (re-requests
            // re-add while ids only grow): the later id is evaluated first.
            Queue.get().addNew(0, later);
            Queue.get().addNew(1, earlier);

            int price = PYLON.mineralPrice();
            assertEquals(price, ProduceBuilding.earlierUnfinishedMinerals(later),
                "door model still counts the earlier id - it reads ids, not positions");
            assertEquals(0, ShouldNotTravelToConstructYet.earlierUnpaidForTests(later)[0],
                "forecast model counts nothing - the evaluated row is first in the queue");
        });
    }
}
