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
 * Owner rule, V2 door: below minSupply only price+50 builds (the legacy
 * BelowMinSupplyGate in the dead request door). A poor early mint waits;
 * a funded early mint still goes (aim-while-poor stays).
 */
@Tag("placement")
public class BelowMinSupplyIsRefusedTest extends WorldStubForTests {

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
    public void poorEarlyMintWaits() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(8, 9);
            currentMinerals = 100;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 11),
                "supply 8 below min 11 with 100 bank: the mint waits");
            assertEquals(0, ConstructionRequests.constructions.size());

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BUILD_REFUSED\"")
                    && rows.contains("below-min-supply-and-poor"),
                "the refusal names itself: " + rows);
        });
    }

    @Test
    public void fundedEarlyMintProceeds() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(8, 9);
            currentMinerals = 300;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 11),
                "supply 8 below min 11 but bank covers price+50: aim-while-poor mints");
            assertEquals(1, ConstructionRequests.constructions.size());
        });
    }
}
