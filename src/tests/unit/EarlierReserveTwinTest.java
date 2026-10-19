package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * No building spends an earlier-queued building's reserve (owner): a
 * PREPARING Gateway (150) plus a Pylon (100) needs a 250 bank before the
 * Pylon may take a builder. The earlier construction exists as an object
 * from pick time, so the door can price it instead of guessing.
 */
public class EarlierReserveTwinTest extends AbstractTestWithWorld {

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
        EventLog.clearForTests();
        ProduceBuilding.clearRefusedThrottleForTests();
    }

    @Test
    public void laterPylonWaitsForEarlierGatewaysReserve() {
        currentMinerals = 150;
        currentSupplyUsed = 10;
        currentSupplyTotal = 17;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            // Earlier demand, already picked and waiting.
            ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            Construction preparing = new Construction(AUnitType.Protoss_Gateway);
            preparing.setPositionToBuild(APosition.create(100, 100));
            preparing.setStatus(ConstructionOrderStatus.PREPARING);
            preparing.setReservedBuilder(fake(AUnitType.Protoss_Probe, 22, 21));
            preparing.setProductionOrder(gateway);
            ConstructionRequests.constructions.add(preparing);

            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 8);

            assertEquals(
                150, ProduceBuilding.earlierUnfinishedMinerals(pylon),
                "the earlier Gateway reserves 150: " + ProduceBuilding.earlierUnfinishedMinerals(pylon));

            EventLog.clearForTests();
            assertFalse(
                ProduceBuilding.requestBuilding(AUnitType.Protoss_Pylon, pylon),
                "150 bank cannot cover the Pylon (100) plus the earlier Gateway (150)");
            assertTrue(
                EventLog.toJsonLines().contains("earlier-reserve"),
                "the refusal names its guard, got: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void coveredReserveLetsLaterBuildingThrough() {
        currentMinerals = 350;
        currentSupplyUsed = 10;
        currentSupplyTotal = 17;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            Construction preparing = new Construction(AUnitType.Protoss_Gateway);
            preparing.setPositionToBuild(APosition.create(100, 100));
            preparing.setStatus(ConstructionOrderStatus.PREPARING);
            preparing.setReservedBuilder(fake(AUnitType.Protoss_Probe, 22, 21));
            preparing.setProductionOrder(gateway);
            ConstructionRequests.constructions.add(preparing);

            EventLog.clearForTests();
            ProduceBuilding.requestBuilding(AUnitType.Protoss_Pylon, new ProductionOrder(AUnitType.Protoss_Pylon, 8));

            assertFalse(
                EventLog.toJsonLines().contains("earlier-reserve"),
                "350 covers Pylon (100) plus the earlier Gateway (150) - no reserve twin: "
                    + EventLog.toJsonLines());
        });
    }
}
