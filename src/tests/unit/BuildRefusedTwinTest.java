package tests.unit;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #148 probe: a ready building order that dies before siting must
 * name its refusing guard (20 Forge wants vanished with zero diagnostics
 * because every exit was silent). Only the stub-reachable guard fires
 * here; the rest fire only live, and the commit says so.
 */
public class BuildRefusedTwinTest extends AbstractTestWithWorld {

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
    }

    @Test
    public void poorBankBelowMinSupplyNamesItsGuard() {
        currentMinerals = 0;
        currentSupplyUsed = 10;
        currentSupplyTotal = 18;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            EventLog.clearForTests();
            assertFalse(
                ProduceBuilding.requestBuilding(
                    AUnitType.Protoss_Forge, new ProductionOrder(AUnitType.Protoss_Forge, 52)),
                "below min supply on an empty bank builds nothing");
            String rows = EventLog.toJsonLines();
            assertTrue(
                rows.contains("BUILD_REFUSED") && rows.contains("below-min-supply-and-poor"),
                "the refusal names its guard, got: " + rows);
        });
    }

    /**
     * Owner 2026-10-19 (Wine: the At-22 Gateway's builder started while
     * its At-21 Pylon was still PREPARING, then the bank sat below 100
     * for the Pylon): a power-needing building with no standing Pylon
     * waits for Pylon price + own price (Forge: 100 + 150).
     */
    @Test
    public void powerHungryBuildingWaitsForPylonBank() {
        currentMinerals = 200;
        currentSupplyUsed = 22;
        currentSupplyTotal = 25;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            EventLog.clearForTests();
            assertFalse(
                ProduceBuilding.requestBuilding(
                    AUnitType.Protoss_Forge, new ProductionOrder(AUnitType.Protoss_Forge, 22)),
                "no Pylon standing and bank below 250 builds nothing");
            String rows = EventLog.toJsonLines();
            assertTrue(
                rows.contains("BUILD_REFUSED") && rows.contains("power-not-funded"),
                "the refusal names its guard, got: " + rows);
        });
    }

    @Test
    public void fundedPylonBankPassesThePowerGate() {
        currentMinerals = 300;
        currentSupplyUsed = 22;
        currentSupplyTotal = 25;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            EventLog.clearForTests();
            ProduceBuilding.requestBuilding(
                AUnitType.Protoss_Forge, new ProductionOrder(AUnitType.Protoss_Forge, 22));
            assertFalse(
                EventLog.toJsonLines().contains("power-not-funded"),
                "bank 300 covers Pylon + Forge - the power gate stays open");
        });
    }
}
