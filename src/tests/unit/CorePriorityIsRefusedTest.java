package tests.unit;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.execution.GameOrderDirector;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner rule, V2 door: a waiting Core outranks Pylon spending while
 * contended (ex legacy CorePriorityGate) - the 100 minerals stay saved
 * for the Core instead of going into a forward Pylon. Started Cores
 * never block (money spent); rich banks never block either.
 */
@Tag("placement")
public class CorePriorityIsRefusedTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        GameOrderDirector.clearForTests();
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) Queue.get().allOrders().list().clear();
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        GameOrderDirector.clearForTests();
    }

    @Test
    public void pylonYieldsToWaitingCore() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(20, 25);
            currentMinerals = 200;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();
            Queue.get().allOrders().list().clear();

            ProductionOrder core = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 18);
            Queue.get().addNew(0, core);

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 8),
                "a siteless Core waits while contended (free>=2, bank<=284): the Pylon yields");
            assertEquals(0, ConstructionRequests.constructions.size());

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BUILD_REFUSED\"")
                    && rows.contains("core-priority"),
                "the refusal names itself: " + rows);
        });
    }

    @Test
    public void richBankIgnoresTheWaiter() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(20, 25);
            currentMinerals = 400;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();
            Queue.get().allOrders().list().clear();

            ProductionOrder core = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 18);
            Queue.get().addNew(0, core);

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 8),
                "400 bank: no contention, the Pylon mints");
            assertEquals(1, ConstructionRequests.constructions.size());
        });
    }
}
