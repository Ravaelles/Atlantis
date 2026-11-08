package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.execution.GameOrderDirector;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * BO order owns the opening (owner 2026-11-09): below 40 supply a Pylon
 * mint waits while a build-order Gateway is due but unbuilt, unless
 * the bank covers both (250). Jumping it with a Pylon spends the
 * Gateway's minerals and delays it past the timing that holds
 * (measured live B189: a support Pylon minted at 24 supply on a 232
 * bank while the BO Gateway waited for 150 - the Pylon is affordable
 * first, so the wallet alone would keep jumping the queue).
 */
@Tag("placement")
public class GatewayFirstBlocksPylonTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        ConstructionRequests.constructions.clear();
        atlantis.util.log.EventLog.clearForTests();
        GameOrderDirector.clearForTests();
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        atlantis.util.log.EventLog.clearForTests();
        GameOrderDirector.clearForTests();
    }

    private static void withBoGateway(int minSupply) {
        ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, minSupply);
        gateway.markAsNotDynamic();
        Queue.get().addNew(0, gateway);
    }

    private static boolean refusedAsGatewayFirst() {
        for (String line : atlantis.util.log.EventLog.toJsonLines().split("\n")) {
            if (line.contains("\"key\":\"BUILD_REFUSED\"") && line.contains("gateway-first")) {
                return true;
            }
        }

        return false;
    }

    @Test
    public void pylonWaitsForDueGatewayBelow250Bank() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(17, 25);
            currentMinerals = 200;
            ConstructionRequests.constructions.clear();
            atlantis.util.log.EventLog.clearForTests();
            withBoGateway(12);

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                PlacementReservation.success(30, 30, 100), 8),
                "BO Gateway due and unbuilt at bank 200: the Pylon waits for it");
            assertTrue(refusedAsGatewayFirst(), "the refusal names the gate");
        });
    }

    @Test
    public void richBankLetsPylonThrough() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(17, 25);
            currentMinerals = 320;
            ConstructionRequests.constructions.clear();
            atlantis.util.log.EventLog.clearForTests();
            withBoGateway(12);

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                PlacementReservation.success(30, 30, 100), 8),
                "bank 320 covers both warps: no reason to hold the Pylon");
        });
    }

    @Test
    public void futureGatewayDoesNotBlock() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(9, 17);
            currentMinerals = 120;
            ConstructionRequests.constructions.clear();
            atlantis.util.log.EventLog.clearForTests();
            withBoGateway(12);

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                PlacementReservation.success(30, 30, 100), 8),
                "Gateway not due yet (12 > 9): the opening Pylon mints freely");
        });
    }

    @Test
    public void finishedGatewayReleasesPylon() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(17, 25);
            currentMinerals = 120;
            ConstructionRequests.constructions.clear();
            atlantis.util.log.EventLog.clearForTests();
            withBoGateway(12);
            for (Object order : Queue.get().notFinished().list().toArray()) {
                ((ProductionOrder) order).setStatus(OrderStatus.FINISHED);
            }

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                PlacementReservation.success(30, 30, 100), 8),
                "Gateway up: nothing holds the Pylon back");
        });
    }

    @Test
    public void legacyDoorHonoursTheSameGate() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;
            initSupply(17, 25);
            currentMinerals = 150;
            ConstructionRequests.constructions.clear();
            atlantis.util.log.EventLog.clearForTests();
            withBoGateway(12);

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 15);
            assertTrue(
                atlantis.production.constructions.NewConstructionRequest.requestConstructionOf(
                    AUnitType.Protoss_Pylon, null, order) == null,
                "the legacy door refuses the same way: no second Pylon ahead of the Gateway");
        });
    }
}
