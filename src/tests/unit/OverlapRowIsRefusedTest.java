package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
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
 * Cross-order footprint overlap (owner 2026-11-08): a STARTED Pylon row
 * holds [93,103] with the builder walking, a Gateway demand mints
 * [92,103] on top of it - same-tile dedupe is blind (different tiles)
 * and the unit overlap guards are blind (no warping unit yet), so the
 * builder spins 110 s on refused Builds with a banked supply block.
 * The mint door refuses overlapping ground; touching edges stay legal.
 */
@Tag("placement")
public class OverlapRowIsRefusedTest extends WorldStubForTests {

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
    public void mintOnOverlappingRowIsRefused() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(24, 33);
            currentMinerals = 400;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            // The live shape: a STARTED Pylon row, builder walking, no
            // warping unit yet - invisible to every unit-based guard.
            Construction pylon = new Construction(AUnitType.Protoss_Pylon);
            pylon.setPositionToBuild(APosition.create(93, 103));
            ConstructionRequests.addConstruction(pylon);

            assertTrue(ConstructionRequests.overlapsInFlightRow(
                APosition.create(92, 103), 4, 3),
                "Gateway [92,103] shares footprint columns with Pylon row [93,103]");

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    PlacementReservation.success(92, 103, 100), 0),
                "overlapping ground is refused, the demand retries elsewhere");
            assertEquals(1, ConstructionRequests.constructions.size(),
                "refused mint leaves no row behind");

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BUILD_REFUSED\"")
                    && rows.contains("overlap-row"),
                "the refusal names itself: " + rows);
        });
    }

    @Test
    public void touchingEdgesStayLegal() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(24, 33);
            currentMinerals = 400;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            Construction pylon = new Construction(AUnitType.Protoss_Pylon);
            pylon.setPositionToBuild(APosition.create(93, 103));
            ConstructionRequests.addConstruction(pylon);

            // Packed bases touch edges: [96,103] starts where the Pylon
            // ends, sharing no tile.
            assertFalse(ConstructionRequests.overlapsInFlightRow(
                APosition.create(95, 103), 1, 1),
                "touching edges are legal ground");
        });
    }
}
