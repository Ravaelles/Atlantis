package tests.unit;

import atlantis.game.listeners.OnOurNewUnitCompleted;
import atlantis.map.position.APosition;
import atlantis.production.GoalBoardFeeder;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.protoss.ProtossWarping;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceOrdersFromQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Same-supply build-order rows build in queue order with builders sent,
 * as soon as the bank covers each row plus still-unpaid earlier demand
 * (owner: UMS 7th_rav, where a Pylon waited on a warping Core at bank
 * 288 because the reserve counted an already-paid building).
 *
 * <p>Start: Nexus + Pylon + Gateway standing (power on, BO shape),
 * 14 workers + 1 Zealot (supply 16/17), artificial same-supply BO rows
 * Pylon, Core, Gateway - plus already-FINISHED Pylon/Gateway rows for
 * the opening. The seeds matter: an order with no linked construction
 * completes by existence, so without them the standing buildings would
 * instantly satisfy the queued rows and there would be nothing to
 * order (verified in a probe run). Each frame banks +30 minerals and
 * the real queue path runs (refresh + per-order handling); the engine
 * half (warp at the site, completion later) is injected exactly where
 * the engine would answer, like {@code OrphanedConstructionTest} does
 * for one building.</p>
 */
public class QueuedBuildingsBuildInOrderTest extends AbstractTestWithWorld {

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

    @BeforeEach
    public void hermeticBoard() {
        GoalBoardFeeder.clearForTests();
    }

    @AfterEach
    public void clean() {
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        GoalBoardFeeder.clearForTests();
    }

    @Test
    public void queuedBuildingsWarpInQueueOrderWithBuilders() {
        currentSupplyUsed = 16;
        currentSupplyTotal = 17;
        currentMinerals = 0;

        FakeUnit[] ours = new FakeUnit[18];
        ours[0] = fake(AUnitType.Protoss_Nexus, 20, 20);
        ours[1] = fake(AUnitType.Protoss_Pylon, 26, 20);
        ours[2] = fake(AUnitType.Protoss_Gateway, 32, 22);
        for (int i = 0; i < 14; i++) ours[3 + i] = fake(AUnitType.Protoss_Probe, 21 + i, 24);
        ours[17] = fake(AUnitType.Protoss_Zealot, 40, 21);

        List<String> warpOrder = new ArrayList<String>();
        final Map<Integer, Integer> warpedAtFrame = new HashMap<Integer, Integer>();
        final Map<Integer, Integer> assignedAtFrame = new HashMap<Integer, Integer>();
        final Map<Integer, Boolean> hadBuilderAtWarp = new HashMap<Integer, Boolean>();

        world(400, fakeOurs(ours), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();

            // Mineral drip, owner spec: +30 per frame.
            currentMinerals += 30;

            if (now == 1) {
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();
                // Opening already done: finished rows so the standing
                // buildings don't satisfy the queued rows by existence.
                ProductionOrder donePylon = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
                donePylon.setStatus(OrderStatus.FINISHED);
                ProductionOrder doneGateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
                doneGateway.setStatus(OrderStatus.FINISHED);
                Queue.get().addNew(0, donePylon);
                Queue.get().addNew(1, doneGateway);
                Queue.get().addNew(2, new ProductionOrder(AUnitType.Protoss_Pylon, 16));
                Queue.get().addNew(3, new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 16));
                Queue.get().addNew(4, new ProductionOrder(AUnitType.Protoss_Gateway, 16));
                EventLog.clearForTests();
            }

            // The real queue path, as ProductionOrdersCommander runs it.
            Queue.get().refresh();
            for (ProductionOrder order : new ArrayList<ProductionOrder>(
                Queue.get().readyToProduceOrders().list())) {
                ProduceOrdersFromQueue.handleProductionOrder(order);
            }

            // Engine half, injected where the engine would answer: a
            // builder-assigned, sited, not-yet-warped construction warps -
            // with a walk+warp delay, like a real builder (immediate warps
            // would leave no unpaid window for the reserve to cover).
            for (Construction construction : new ArrayList<Construction>(
                ConstructionRequests.notFinished())) {
                if (construction.builder() != null
                    && construction.buildingUnit() == null
                    && construction.buildPosition() != null
                    && !warpedAtFrame.containsKey(construction.id())) {
                    if (!assignedAtFrame.containsKey(construction.id())) {
                        assignedAtFrame.put(construction.id(), now);
                        continue;
                    }
                    if (now - assignedAtFrame.get(construction.id()) < 20) continue;
                    APosition site = construction.buildPosition();
                    FakeUnit warping = fake(construction.buildingType(), site.tx(), site.ty());
                    warping.setCompleted(false);
                    hadBuilderAtWarp.put(construction.id(), construction.builder() != null);
                    ProtossWarping.updateNewBuildingJustWarped(warping);
                    warpedAtFrame.put(construction.id(), now);
                    warpOrder.add(construction.buildingType().name());
                }
            }

            // Completions land 15 frames after each warp; the finished
            // construction must leave the list with its order FINISHED.
            for (Construction construction : new ArrayList<Construction>(
                ConstructionRequests.notFinished())) {
                Integer warpedAt = warpedAtFrame.get(construction.id());
                if (warpedAt != null && now - warpedAt >= 15
                    && construction.buildingUnit() != null) {
                    AUnit building = construction.buildingUnit();
                    ((FakeUnit) building).setCompleted(true);
                    OnOurNewUnitCompleted.ourNewUnitCompleted(building);
                }
            }
        });

        assertEquals(
            Arrays.asList("Pylon", "CyberneticsC", "Gateway"),
            warpOrder,
            "queue order is build order, even with same-supply rows: " + warpOrder);
        for (Map.Entry<Integer, Boolean> entry : hadBuilderAtWarp.entrySet()) {
            assertTrue(entry.getValue(), "a builder is sent for construction " + entry.getKey());
        }
        assertTrue(
            ConstructionRequests.notFinished().isEmpty(),
            "everything finished and left the list");
        for (ProductionOrder order : Queue.get().allOrders().list()) {
            assertEquals(
                OrderStatus.FINISHED, order.status(),
                "every queued order finished: " + order);
        }
        assertTrue(
            EventLog.toJsonLines().contains("earlier-reserve"),
            "unpaid earlier demand holds later rows and names it: "
                + EventLog.toJsonLines());
    }
}
