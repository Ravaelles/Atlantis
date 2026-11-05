package tests.unit;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.execution.GameOrderDirector;
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
 * Owner rule, V2 door: a power-needing building with no standing Pylon
 * waits until the bank covers the Pylon plus itself (ex legacy
 * PowerNotFundedGate) - never send the worker before the power is paid
 * for. Warping counts as not built.
 */
@Tag("placement")
public class PowerNotFundedIsRefusedTest extends WorldStubForTests {

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
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        GameOrderDirector.clearForTests();
    }

    @Test
    public void unfundedGatewayWaitsForPylonMoney() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(11, 17);
            currentMinerals = 200;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    PlacementReservation.success(30, 30, 100), 11),
                "no Pylon and 200 < 100+150: the worker is not sent before the power is paid for");
            assertEquals(0, ConstructionRequests.constructions.size());

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BUILD_REFUSED\"")
                    && rows.contains("power-not-funded"),
                "the refusal names itself: " + rows);
        });
    }

    @Test
    public void fundedGatewayProceeds() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(11, 17);
            currentMinerals = 300;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    PlacementReservation.success(30, 30, 100), 11),
                "300 covers Pylon+Gateway: the mint proceeds");
            assertEquals(1, ConstructionRequests.constructions.size());
        });
    }

    @Test
    public void standingPylonLiftsTheWait() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 24),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(11, 17);
            currentMinerals = 160;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    PlacementReservation.success(30, 30, 100), 11),
                "a standing Pylon means power is paid for - 160 mints the Gateway");
            assertEquals(1, ConstructionRequests.constructions.size());
        });
    }
}
