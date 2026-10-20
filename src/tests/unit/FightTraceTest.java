package tests.unit;

import atlantis.battle.twins.FightTrace;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
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
 * The per-unit fight trace (owner 2026-10-20): samples carry health and
 * distance to the nearest enemy, deaths always land. Throttled to one
 * sample per 30 frames so an 8v8 game stays near ~500 rows.
 */
@Tag("combat")
public class FightTraceTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanLog() {
        EventLog.clearForTests();
        FightTrace.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        FightTrace.clearForTests();
    }

    @Test
    public void sampleCarriesHealthAndDistance() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            FightTrace.sample(dragoon, zealot);

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"UNIT_TRACE\""), "got: " + rows);
            assertTrue(rows.contains("\"type\":\"Dragoon\""), "got: " + rows);
            assertTrue(rows.contains("\"hp\":"), "got: " + rows);
            assertTrue(rows.contains("\"dist\":"), "got: " + rows);
        });
    }

    @Test
    public void samplesThrottleToOnePerThirtyFrames() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);

        world(40, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            int now = atlantis.game.A.now();
            if (now == 3) {
                FightTrace.sample(dragoon, zealot);
                FightTrace.sample(dragoon, zealot);
            }
            if (now == 34) {
                FightTrace.sample(dragoon, zealot);
                int traces = EventLog.toJsonLines().split("\"key\":\"UNIT_TRACE\"", -1).length - 1;
                assertTrue(traces == 2, "one row at frame 3, one at frame 34, got: " + traces);
            }
        });
    }

    @Test
    public void workersAreNotTraced() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            FightTrace.sample(probe, null);

            assertFalse(EventLog.toJsonLines().contains("\"key\":\"UNIT_TRACE\""),
                "gathering probes are noise, not formation");
        });
    }

    @Test
    public void deathAlwaysLands() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            FightTrace.died(dragoon);

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"UNIT_DIED\""), "got: " + rows);
            assertTrue(rows.contains("\"dist\":"), "nearest-enemy distance at death: " + rows);
            assertTrue(rows.contains("\"killer\":"), "suspect id at death: " + rows);
        });
    }
}
