package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.BuildGate;
import atlantis.production.requests.produce.BuildGates;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Pins the single-instance gate after the mutual-lock fix (NEXT #155,
 * measured Wine TauCross 2026-10-20): a started-then-cancelled BO site
 * and a siteless dynamic row refused each other for 8 game-minutes at
 * bank 3000+ because every siteless row counted every other. Now only
 * genuinely coming work (sited sites, warping units) blocks, and among
 * siteless waiters the oldest ready row proceeds - a poor older row
 * never parks a ready younger one.
 */
@Tag("production")
public class CoreSingleInstanceGateTest extends AbstractTestWithWorld {

    private static final AUnitType CORE = AUnitType.Protoss_Cybernetics_Core;

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
    public void oldestReadyWaiterProceedsYoungerWaits() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11, 10);

        world(30, AbstractTestWithWorld.units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10) return;

            currentSupplyUsed = 30;
            currentMinerals = 1000;

            ProductionOrder older = new ProductionOrder(CORE, 8);
            ProductionOrder younger = new ProductionOrder(CORE, 8);
            Queue.get().addNew(0, older);
            Queue.get().addNew(1, younger);

            assertNull(gate().check(CORE, older),
                    "oldest ready waiter proceeds - no symmetric lock");
            assertEquals("core-single-instance", gate().check(CORE, younger),
                    "younger ready waiter still waits behind the older ready one");
        });
    }

    @Test
    public void poorOlderRowNeverParksReadyYounger() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11, 10);

        world(30, AbstractTestWithWorld.units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10) return;

            currentSupplyUsed = 10;
            currentMinerals = 10;

            ProductionOrder poorOlder = new ProductionOrder(CORE, 100);
            ProductionOrder richYounger = new ProductionOrder(CORE, 8);
            Queue.get().addNew(0, poorOlder);
            Queue.get().addNew(1, richYounger);

            assertNull(gate().check(CORE, richYounger),
                    "the older row cannot fire (poor) - it must not park the ready younger one");
        });
    }

    @Test
    public void sitedSiteForAnotherOrderStillBlocks() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11, 10);

        world(30, AbstractTestWithWorld.units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10) return;

            currentSupplyUsed = 30;
            currentMinerals = 1000;

            ProductionOrder sited = new ProductionOrder(CORE, 8);
            ProductionOrder waiter = new ProductionOrder(CORE, 8);
            Queue.get().addNew(0, sited);
            Queue.get().addNew(1, waiter);
            linkNotStartedSite(sited);

            assertEquals("core-single-instance", gate().check(CORE, waiter),
                    "a live site for another order is genuinely coming - the waiter waits");
            assertNull(gate().check(CORE, sited),
                    "own sited site never blocks its own re-request");
        });
    }

    private static void linkNotStartedSite(ProductionOrder order) {
        Construction construction = new Construction(CORE);
        construction.setPositionToBuild(APosition.create(30, 30));
        construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
        construction.setProductionOrder(order);
        ConstructionRequests.constructions.add(construction);
    }

    private static BuildGate gate() {
        for (BuildGate gate : BuildGates.all()) {
            if (gate.name().equals("core-single-instance")) return gate;
        }
        throw new IllegalStateException("core-single-instance gate missing from the registry");
    }
}
