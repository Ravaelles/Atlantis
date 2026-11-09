package tests.unit;

import atlantis.production.execution.GameOrderDirector;
import atlantis.production.orders.production.queue.Queue;
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
 * Archives-bound Citadel naming (owner 2026-11-09): a Citadel minted
 * below the standalone floor rows CITADEL_PREREQ with the templar
 * line's state - planned, warping, standing, or none (in which case
 * the gate was jumped and the row says so). The scheduler prereq and
 * the legacy direct queue both bypass the standalone goal, so without
 * this twin the early Citadel reads as a violation.
 */
@Tag("placement")
public class CitadelPrereqTwinTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        atlantis.production.constructions.ConstructionRequests.constructions.clear();
        atlantis.util.log.EventLog.clearForTests();
        GameOrderDirector.clearForTests();
    }

    @AfterEach
    public void clean() {
        atlantis.production.constructions.ConstructionRequests.constructions.clear();
        atlantis.util.log.EventLog.clearForTests();
        GameOrderDirector.clearForTests();
    }

    private static boolean hasCitadelPrereqTwin() {
        for (String line : atlantis.util.log.EventLog.toJsonLines().split("\n")) {
            if (line.contains("\"key\":\"CITADEL_PREREQ\"")) return true;
        }

        return false;
    }

    private static String citadelPrereqTwin() {
        for (String line : atlantis.util.log.EventLog.toJsonLines().split("\n")) {
            if (line.contains("\"key\":\"CITADEL_PREREQ\"")) return line;
        }

        return "";
    }

    @Test
    public void belowFloorWithoutArchivesNamesTheJump() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 28, 28),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(60, 70);
            currentMinerals = 300;
            currentGas = 200;
            atlantis.util.log.EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Citadel_of_Adun),
                PlacementReservation.success(30, 30, 100), 60),
                "the mint itself still goes through; the twin names it");

            assertTrue(hasCitadelPrereqTwin(), "below 110 the mint is twinned");
            String twin = citadelPrereqTwin();
            assertTrue(twin.contains("\"archivesPlanned\":false"), "no Archives anywhere: " + twin);
            assertTrue(twin.contains("\"archivesWarping\":false"), "no Archives anywhere: " + twin);
            assertTrue(twin.contains("\"archivesStanding\":false"), "no Archives anywhere: " + twin);
        });
    }

    @Test
    public void plannedArchivesNamesTheException() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 28, 28),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(60, 70);
            currentMinerals = 300;
            currentGas = 200;
            atlantis.util.log.EventLog.clearForTests();

            ProductionOrder archives = new ProductionOrder(AUnitType.Protoss_Templar_Archives, 60);
            Queue.get().addNew(0, archives);

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Citadel_of_Adun),
                PlacementReservation.success(30, 30, 100), 60),
                "the mint itself still goes through; the twin names it");

            assertTrue(hasCitadelPrereqTwin(), "below 110 the mint is twinned");
            assertTrue(citadelPrereqTwin().contains("\"archivesPlanned\":true"),
                "the queued Archives owns this Citadel: " + citadelPrereqTwin());
        });
    }

    @Test
    public void atOrAbove110StaysSilent() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 28, 28),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(120, 130);
            currentMinerals = 300;
            currentGas = 200;
            atlantis.util.log.EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Citadel_of_Adun),
                PlacementReservation.success(30, 30, 100), 110);

            assertFalse(hasCitadelPrereqTwin(),
                "at/above the floor the standalone rule owns it: no exception twin");
        });
    }
}
