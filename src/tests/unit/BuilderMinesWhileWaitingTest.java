package tests.unit;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.WorkerManager;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #212, first half: a worker held at a construction site that cannot
 * start (bank short, forecast says wait) returns to mining instead of
 * standing at the site. The assignment stays - no Builder/Gather tug of
 * war, unlike the IN_PROGRESS alternation pinned by
 * {@code HoldInProgressBuilderTest} - so when the bank covers the price
 * the same builder walks back and builds.
 */
@Tag("production")
public class BuilderMinesWhileWaitingTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @BeforeEach
    public void hermetic() {
        ConstructionRequests.constructions.clear();
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
    }

    @Test
    public void waitingBuilderMinesInsteadOfStanding() {
        currentMinerals = 10;
        neutralInWorld = new FakeUnit[] {
            neutralMineral(14, 19),
            neutralMineral(15, 19),
            neutralMineral(16, 18),
        };

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        probe.hp = 40;

        world(20, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (A.now() != 5) return;
            initSupply(10, 18);

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setPositionToBuild(APosition.create(30, 20));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, APosition.create(30, 20), 0);
            construction.setProductionOrder(order);
            Queue.get().addNew(0, order);
            construction.setBuilder(probe);
            ConstructionRequests.constructions.add(construction);

            new WorkerManager(probe).forceHandle();

            assertTrue(probe.isGatheringMinerals(),
                "a builder held at an unstartable site mines instead of "
                + "standing: bank 10 of 100, forecast says wait");
            assertEquals(probe, construction.builder(),
                "waiting does not unassign - the same builder comes back "
                + "when the bank covers the price");
            assertTrue(gatherOrders(probe) >= 1,
                "a gather order was actually issued, not just a flag");
        });
    }

    @Test
    public void affordableBuilderStaysPut() {
        currentMinerals = 400;
        neutralInWorld = new FakeUnit[] {
            neutralMineral(14, 19),
        };

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        probe.hp = 40;

        world(20, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (A.now() != 5) return;
            initSupply(10, 18);

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setPositionToBuild(APosition.create(30, 20));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, APosition.create(30, 20), 0);
            construction.setProductionOrder(order);
            Queue.get().addNew(0, order);
            construction.setBuilder(probe);
            ConstructionRequests.constructions.add(construction);

            new WorkerManager(probe).forceHandle();

            assertTrue(gatherOrders(probe) == 0,
                "an affordable build keeps its builder - no wandering off "
                + "to the mineral line");
            assertEquals(probe, construction.builder(),
                "the affordable builder stays assigned");
        });
    }

    private static FakeUnit neutralMineral(double tx, double ty) {
        return fake(AUnitType.Resource_Mineral_Field, tx, ty).setNeutral();
    }

    private static int gatherOrders(FakeUnit probe) {
        int n = 0;
        for (FakeOrderSink.Record r : ((FakeOrderSink) probe.orderSink()).orders()) {
            if (r.operation.equals("gather")) n++;
        }

        return n;
    }

    private static int moveOrders(FakeUnit probe) {
        int n = 0;
        for (FakeOrderSink.Record r : ((FakeOrderSink) probe.orderSink()).orders()) {
            if (r.operation.equals("move")) n++;
        }

        return n;
    }
}
