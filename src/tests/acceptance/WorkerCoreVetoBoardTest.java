package tests.acceptance;

import atlantis.production.GoalBoardAdvisor;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * First Decision-absorption slice (NEXT #93 track): the worker Core-veto's
 * posture inputs arrive on the board snapshot now, and the trio is gone
 * with {@code EarlyGameProduceWorkers} (TRUE and INDIFFERENT both
 * proceeded - only FALSE("PrioritizeCore") ever vetoed).
 *
 * <p>The veto guards one narrow window: an adopted STANDARD Core row
 * (IN_PROGRESS order, NOT_STARTED construction, builder walking) while
 * poor. HIGH rows are covered by HigherPriorityWaits, not-started rows by
 * CriticalStuff - the veto is the only gate that sees the adopted window,
 * so the swap preserves it verbatim, reason string included.</p>
 */
public class WorkerCoreVetoBoardTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        fired = false;
    }

    private void vetoPosture() {
        currentSupplyUsed = 14;
        currentSupplyTotal = 25;
        currentMinerals = 150;
        currentGas = 100;
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        ConstructionRequests.constructions.clear();

        ProductionOrder core = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 14);
        core.setPriority(ProductionOrderPriority.STANDARD);
        core.setStatus(OrderStatus.IN_PROGRESS);
        Queue.get().addNew(0, core);

        ConstructionRequests.addConstruction(new Construction(AUnitType.Protoss_Cybernetics_Core));
    }

    @Test
    public void snapshotNamesCoreAwaitingSupply() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 11)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;

            vetoPosture();
            assertTrue(
                GoalBoardAdvisor.snapshot().coreAwaitingSupply,
                "adopted Core row, supply unlocked, construction unstarted");

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            assertFalse(
                GoalBoardAdvisor.snapshot().coreAwaitingSupply,
                "no Core row, no veto posture");

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 30));
            assertFalse(
                GoalBoardAdvisor.snapshot().coreAwaitingSupply,
                "a Core row 16 supply out is pre-seeded, not awaiting");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void pumpPausesWithTheSameReason() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 11),
            fake(AUnitType.Protoss_Probe, 12, 11),
            fake(AUnitType.Protoss_Probe, 11, 12)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;

            vetoPosture();
            assertFalse(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "the adopted Core window pauses the pump while poor");
            assertEquals(
                "PrioritizeCore", AutoProduceWorkersCommander.REASON,
                "same veto, same reason string - the trio died, the behavior did not");
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
