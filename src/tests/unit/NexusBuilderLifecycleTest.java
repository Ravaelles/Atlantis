package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.TravelToConstruct;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A Nexus construction gets a builder, and the builder is ordered toward
 * the site - the two steps the live game never completes (builder #167
 * assigned, then silence, zero warps). The walk itself is covered by
 * BuilderArrivalSyncTest; this pins the Nexus half: assignment happens
 * and travel is ordered (no waiting on a bank that is already there).
 */
@Tag("production")
public class NexusBuilderLifecycleTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
    }

    private static final class ExposedTravel extends TravelToConstruct {
        ExposedTravel(AUnit unit) {
            super(unit);
        }

        boolean run(Construction construction) {
            return travelWhenReady(construction);
        }
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit builder) {
        return ((FakeOrderSink) builder.orderSink()).orders();
    }

    private static boolean hasMoveOrder(List<FakeOrderSink.Record> orders) {
        return orders.stream().anyMatch(order -> order.toString().contains("move"));
    }

    @Test
    public void nexusSiteGetsABuilderWhoIsOrderedThere() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit[] miners = new FakeUnit[6];
        for (int i = 0; i < miners.length; i++) {
            miners[i] = fake(AUnitType.Protoss_Probe, 10 + i, 11);
        }
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 24, 12);
        FakeUnit[] ours = new FakeUnit[8];
        ours[0] = nexus;
        System.arraycopy(miners, 0, ours, 1, miners.length);
        ours[7] = builder;

        String[] assignedName = new String[1];

        world(30, fakeOurs(ours), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now < 1 || now > 20) return;

            currentMinerals = 500;

            if (ConstructionRequests.constructions.isEmpty()) {
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();

                Construction construction = new Construction(AUnitType.Protoss_Nexus);
                construction.setPositionToBuild(APosition.create(30, 12));
                construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
                ConstructionRequests.constructions.add(construction);

                AUnit assigned = construction.assignOptimalBuilder();
                assignedName[0] = assigned == null ? null : assigned.typeWithHash();
                assertNotNull(assigned,
                    "a free pool around the site must yield a builder for the Nexus");
                assertEquals(builder, assigned,
                    "the nearest free worker gets the job");
                new ExposedTravel(assigned).run(construction);
            }
        });

        assertTrue(assignedName[0] != null, "assignment happened");
        assertTrue(hasMoveOrder(ordersOf(builder)),
            "with the bank stocked the builder is ordered out, never held waiting");
    }
}
