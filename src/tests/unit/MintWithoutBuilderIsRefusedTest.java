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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The V2 mint door refuses what no builder can start (owner order
 * 2026-11-04): during an overrun with no free builders, dispatching
 * a Pylon minted-and-cancelled the same tile 170 times in 7 seconds
 * (live GAME_D1AA02FE: fresh cid+oid every frame, "looks bugged"
 * cancelling the order, de-dup guards blind because each twin died
 * instantly). A refused mint names itself and retries next frame;
 * the demand outlives the refusal, the churn does not.
 */
@Tag("production")
public class MintWithoutBuilderIsRefusedTest extends WorldStubForTests {

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
    public void mintWithNoBuilderAvailableIsRefusedNotMinted() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(49, 51);
            currentMinerals = 150;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 8),
                "no builder, no mint - the demand retries next frame instead of churning");
            assertEquals(0, ConstructionRequests.constructions.size(),
                "refused mint leaves no twin row behind");

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BUILD_REFUSED\"")
                    && rows.contains("no-builder"),
                "the refusal names itself: " + rows);
        });
    }

    @Test
    public void mintWithBuilderAvailableStillMints() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(49, 51);
            currentMinerals = 150;
            ConstructionRequests.constructions.clear();
            EventLog.clearForTests();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                    UnitProducible.of(AUnitType.Protoss_Pylon),
                    PlacementReservation.success(30, 30, 100), 8),
                "a free builder still mints on the first pass");
            assertEquals(1, ConstructionRequests.constructions.size());
        });
    }
}
