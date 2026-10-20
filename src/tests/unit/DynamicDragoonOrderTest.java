package tests.unit;

import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.game.event.Events;
import atlantis.production.dynamic.protoss.units.TrainQueuedDragoonImmediately;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Dynamic Dragoon rows never wait on supply, and their own arrival fires
 * an event so the same frame can start the training (owner): a
 * minSupply-gated row behind dead units blocked workers and everything
 * behind it while supply could never grow back to the gate.
 */
@Tag("production")
public class DynamicDragoonOrderTest extends AbstractTestWithWorld {

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

    @Test
    public void dynamicDragoonRowHasZeroSupplyRequirement() {
        currentMinerals = 500;
        currentSupplyUsed = 31;
        currentSupplyTotal = 41;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().history().clear();
            Queue.get().clearCache();

            ProductionOrder dragoon = AddToQueue.withStandardPriority(AUnitType.Protoss_Dragoon);
            Queue.get().clearCache();
            assertTrue(dragoon != null, "dynamic Dragoon demand queues a row");
            assertEquals(
                0, dragoon.minSupply(),
                "dynamic Dragoon rows never wait on supply: " + dragoon.minSupply());

            ProductionOrder zealot = AddToQueue.withStandardPriority(AUnitType.Protoss_Zealot);
            Queue.get().clearCache();
            assertTrue(zealot != null, "control row queues");
            assertTrue(
                zealot.minSupply() > 0,
                "only Dragoons get the zero gate, the rest keeps its order: " + zealot.minSupply());
        });
    }

    @Test
    public void queuingDragoonFiresTheDynamicUnitEvent() {
        currentMinerals = 500;
        currentSupplyUsed = 31;
        currentSupplyTotal = 41;

        List<Object[]> seen = new ArrayList<>();
        Events.register(Event.DYNAMIC_UNIT_QUEUED, new AutomaticListener() {
            @Override
            public Event listensTo() {
                return Event.DYNAMIC_UNIT_QUEUED;
            }

            @Override
            public void onEvent(Event event, Object... data) {
                seen.add(data);
            }
        });

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().history().clear();
            Queue.get().clearCache();

            ProductionOrder dragoon = AddToQueue.withStandardPriority(AUnitType.Protoss_Dragoon);
            Queue.get().clearCache();

            assertTrue(dragoon != null, "row queued");
            assertEquals(1, seen.size(), "one dynamic-unit event per queued Dragoon");
            assertTrue(
                seen.get(0)[0] == dragoon,
                "the event carries the queued row itself");
        });
    }

    @Test
    public void listenerTrainsImmediatelyFromNearestGateway() {
        currentMinerals = 500;
        currentSupplyUsed = 31;
        currentSupplyTotal = 41;

        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 25, 20);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            gateway
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().history().clear();
            Queue.get().clearCache();
            tests.fakes.FakeUnitData.TRAIN.clear();

            ProductionOrder dragoon = AddToQueue.withStandardPriority(AUnitType.Protoss_Dragoon);
            Queue.get().clearCache();
            assertTrue(dragoon != null, "row queued");

            new TrainQueuedDragoonImmediately().onEvent(Event.DYNAMIC_UNIT_QUEUED, dragoon);

            assertTrue(
                tests.fakes.FakeUnitData.TRAIN.contains(AUnitType.Protoss_Dragoon),
                "same-frame train issued to a Gateway");
            assertEquals(
                0, Queue.get().notFinished().ofType(AUnitType.Protoss_Dragoon).size(),
                "the immediate train consumes the row - no double production");
        });
    }
}
