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
 * The mint door evaluates guards in a fixed order and the first refusal
 * wins (ex legacy BuildGates registry order): an overstocked Pylon with
 * no builder and no bank names overstock, not the later guards. Locks
 * the door sequence against silent reordering.
 */
@Tag("placement")
public class MintGuardPrecedenceTest extends WorldStubForTests {

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
    public void firstRefusalWins() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(25, 37);
            currentMinerals = 0;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 8),
                "overstocked, builderless and broke: the mint refuses");
            assertEquals(0, ConstructionRequests.constructions.size());

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("pylon-overstock"),
                "overstock (door order 4) beats no-builder and unaffordable, got: " + rows);
            assertFalse(rows.contains("no-builder") || rows.contains("unaffordable"),
                "later guards stay silent once an earlier one fires, got: " + rows);
        });
    }
}
