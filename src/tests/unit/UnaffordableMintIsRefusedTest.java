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
 * V2 door: never mint what the bank does not cover (ex legacy wallet
 * funding). The scheduler plans affordable frames, but the bank moves
 * under it (units train off the same pool) - dispatch checks reality
 * and the demand retries next frame instead of parking a builder on
 * an unpaid row.
 */
@Tag("placement")
public class UnaffordableMintIsRefusedTest extends WorldStubForTests {

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
    public void brokeMintWaits() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 24),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(20, 25);
            currentMinerals = 50;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    PlacementReservation.success(30, 30, 100), 8),
                "50 bank cannot pay a 150 Gateway: the mint waits for funds");
            assertEquals(0, ConstructionRequests.constructions.size());

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BUILD_REFUSED\"")
                    && rows.contains("unaffordable"),
                "the refusal names itself: " + rows);
        });
    }

    @Test
    public void fundedMintProceeds() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 24),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(20, 25);
            currentMinerals = 300;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    PlacementReservation.success(30, 30, 100), 8),
                "300 bank pays the Gateway: the mint proceeds");
            assertEquals(1, ConstructionRequests.constructions.size());
        });
    }
}
