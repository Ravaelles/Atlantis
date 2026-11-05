package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.execution.GameOrderDirector;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.producibles.UnitProducible;
import atlantis.map.position.APosition;
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
 * V2 door: no mint spends an earlier row's reserve (ex legacy
 * EarlierReserveGate). V2 has no queue sequence, so every other
 * unpaid unfinished row counts (warping rows paid at warp start and
 * never count). Same guard name in the twin.
 */
@Tag("placement")
public class EarlierReserveIsRefusedTest extends WorldStubForTests {

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
    public void earlierRowReserveBlocks() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 24),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(20, 25);
            currentMinerals = 150;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            Construction earlier = new Construction(AUnitType.Protoss_Gateway);
            earlier.setPositionToBuild(APosition.create(40, 40));
            earlier.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(earlier);

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 8),
                "150 bank cannot cover the Pylon (100) plus the earlier Gateway (150)");
            assertEquals(1, ConstructionRequests.constructions.size());

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BUILD_REFUSED\"")
                    && rows.contains("earlier-reserve"),
                "the refusal names itself: " + rows);
        });
    }

    @Test
    public void coveredReserveProceeds() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 24),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(20, 25);
            currentMinerals = 350;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            Construction earlier = new Construction(AUnitType.Protoss_Gateway);
            earlier.setPositionToBuild(APosition.create(40, 40));
            earlier.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(earlier);

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 8),
                "350 covers Pylon (100) plus the earlier Gateway (150)");
            assertEquals(2, ConstructionRequests.constructions.size());
        });
    }
}
