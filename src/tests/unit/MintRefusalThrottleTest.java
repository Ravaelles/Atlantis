package tests.unit;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.execution.GameOrderDirector;
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
 * A stuck demand names itself once per throttle window, not once per
 * evaluation (ex legacy refuse() 300f throttle; the mint door heartbeat
 * is 600f like the no-builder twin). One refusal row per guard while
 * the demand waits, never a per-frame shout.
 */
@Tag("placement")
public class MintRefusalThrottleTest extends WorldStubForTests {

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
    public void stuckDemandTwinsOncePerWindow() {
        world(700, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now != 3 && now != 10 && now != 603) return;

            initSupply(8, 9);
            currentMinerals = 0;
            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 52);
            GameOrderDirector director = new GameOrderDirector();
            assertFalse(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                PlacementReservation.success(30, 30, 100), pylon.minSupply()));
        });

        int rows = countOccurrences(EventLog.toJsonLines(), "below-min-supply-and-poor");
        assertEquals(
            2, rows,
            "frames 3 and 603 twin, frame 10 stays silent - got: "
                + EventLog.toJsonLines());
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int from = 0;
        while ((from = haystack.indexOf(needle, from)) != -1) {
            count++;
            from += needle.length();
        }

        return count;
    }
}
