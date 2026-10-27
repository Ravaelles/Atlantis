package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.GetOptimalBuilder;
import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner 2026-10-15: a waiting Core outranks Pylon spending, but only while
 * all three hold - supply total 17+, bank 284 or less, 2+ supply free.
 * Outside these the old behavior stands (a 9/9 game with no Pylon would
 * die; rich buys both; a blocked base must Pylon now).
 *
 * <p>Two halves: the spending guard (a Pylon row does not start while an
 * unstarted Core waits) and the anti-steal (a Pylon order near the Core
 * site takes a free miner, never the Core's probe). Both pin the bounds
 * too - each release valve (rich, blocked, pre-Pylon supply) restores the
 * old behavior, so no stuck state can deadlock on this rule.</p>
 */
@Tag("production")
public class CoreOutranksPylonsTest extends AbstractTestWithWorld {

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
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
    }

    @Test
    public void boundsHoldInAllThreeDirections() {
        world(5, fixture(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            currentSupplyUsed = 14;
            currentSupplyTotal = 16;
            currentMinerals = 200;
            assertFalse(
                PriorityWaiters.coreOutranksPylons(),
                "total 16: pre-Pylon supply, the rule is off");

            currentSupplyUsed = 20;
            currentSupplyTotal = 25;
            currentMinerals = 500;
            assertFalse(
                PriorityWaiters.coreOutranksPylons(),
                "bank 500: rich buys both, no arbitration needed");

            currentMinerals = 200;
            currentSupplyUsed = 24;
            assertFalse(
                PriorityWaiters.coreOutranksPylons(),
                "free 1: a looming block releases Pylons immediately");

            currentSupplyUsed = 20;
            assertTrue(
                PriorityWaiters.coreOutranksPylons(),
                "total 25, bank 200, free 5: all three hold, Core outranks");
        });
    }

    @Test
    public void pylonSpendingWaitsForUnstartedCore() {
        world(5, fixture(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            ProductionOrder core = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 18);
            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, core);
            Queue.get().addNew(1, pylon);

            currentSupplyUsed = 20;
            currentSupplyTotal = 25;
            currentMinerals = 200;
            assertFalse(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Pylon, pylon),
                "Core waits unstarted while contended: the Pylon must not start");
            assertTrue(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).isEmpty(),
                "no Pylon construction may exist while blocked");

            currentMinerals = 500;
            assertTrue(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Pylon, pylon),
                "bank 500 releases the rule: rich buys both");
            assertEquals(
                1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).size(),
                "released Pylon proceeds normally");
        });
    }

    @Test
    public void pylonOrderNeverStealsCoreBuilderWhileRuleHolds() {
        FakeUnit probeA = fake(AUnitType.Protoss_Probe, 30, 30);
        FakeUnit probeB = fake(AUnitType.Protoss_Probe, 33, 30);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 12, 10),
            probeA,
            probeB
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ConstructionRequests.constructions.clear();

            Construction core = new Construction(AUnitType.Protoss_Cybernetics_Core);
            core.setPositionToBuild(APosition.create(30, 30));
            core.setStatus(ConstructionOrderStatus.NOT_STARTED);
            core.setBuilder(probeA);
            ConstructionRequests.addConstruction(core);

            Construction pylon = new Construction(AUnitType.Protoss_Pylon);
            pylon.setPositionToBuild(APosition.create(32, 30));
            ProductionOrder pylonOrder = new ProductionOrder(AUnitType.Protoss_Pylon, 8);

            currentSupplyUsed = 20;
            currentSupplyTotal = 25;
            currentMinerals = 200;
            AUnit picked = GetOptimalBuilder.forPosition(pylon, pylonOrder);
            assertNotEquals(
                probeA, picked,
                "the Core's probe is not spare capacity while the rule holds");
            assertEquals(
                probeB, picked,
                "a free miner is taken instead");

            currentMinerals = 500;
            assertEquals(
                probeA, GetOptimalBuilder.forPosition(pylon, pylonOrder),
                "bank 500 releases the rule: the nearby builder is reusable");
        });
    }

    private FakeUnit[] fixture() {
        return fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 23, 21)
        );
    }
}
