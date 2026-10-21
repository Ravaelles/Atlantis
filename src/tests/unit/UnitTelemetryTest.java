package tests.unit;

import atlantis.battle.twins.UnitTelemetry;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Per-second unit telemetry (NEXT #180): every combat unit reports hp,
 * shields, distance to the nearest enemy/dragoon/zealot and enemies
 * within 2 tiles, once per 24 frames; the last 5 snapshots stay in
 * memory so the death log prints the approach, not just the corpse.
 */
@Tag("combat")
public class UnitTelemetryTest extends WorldStubForTests {

    @BeforeEach
    public void cleanLog() {
        atlantis.util.log.EventLog.clearForTests();
        UnitTelemetry.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        atlantis.util.log.EventLog.clearForTests();
        UnitTelemetry.clearForTests();
    }

    @Test
    public void rowCarriesHealthDistancesAndCloseFoes() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 22);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 35, 20);
        foe.setEnemy();

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10), dragoon, zealot), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            UnitTelemetry.sample(dragoon);

            String rows = atlantis.util.log.EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"UNIT_TELEMETRY\""), "got: " + rows);
            assertTrue(rows.contains("\"hp\":"), "got: " + rows);
            assertTrue(rows.contains("\"foe\":"), "got: " + rows);
            assertTrue(rows.contains("\"dragoon\":"), "got: " + rows);
            assertTrue(rows.contains("\"zealot\":"), "got: " + rows);
            assertTrue(rows.contains("\"foes2\":"), "got: " + rows);
        });
    }

    @Test
    public void samplesThrottleToOnePerSecond() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(30, fakeOurs(fake(AUnitType.Protoss_Nexus, 10, 10), dragoon), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now == 3) {
                UnitTelemetry.sample(dragoon);
                UnitTelemetry.sample(dragoon);
            }
            if (now == 28) {
                UnitTelemetry.sample(dragoon);
                int traces = atlantis.util.log.EventLog.toJsonLines()
                    .split("\"key\":\"UNIT_TELEMETRY\"", -1).length - 1;
                assertEquals(2, traces, "one row at frame 3, one at frame 28");
            }
        });
    }

    @Test
    public void trailKeepsLastFiveForTheDeathLog() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(200, fakeOurs(fake(AUnitType.Protoss_Nexus, 10, 10), dragoon), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now % 24 == 1 && now <= 24 * 7) {
                UnitTelemetry.sample(dragoon);
            }
            if (now == 24 * 7 + 1) {
                assertEquals(UnitTelemetry.TRAIL_LENGTH, UnitTelemetry.trailFor(dragoon.id()).size(),
                    "ring keeps five: " + UnitTelemetry.trailFor(dragoon.id()));
                assertTrue(UnitTelemetry.trailFor(dragoon.id()).get(0).startsWith("f"),
                    "rows carry their frame: " + UnitTelemetry.trailFor(dragoon.id()));
            }
        });
    }

    @Test
    public void workersAreNotTraced() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);

        world(3, fakeOurs(fake(AUnitType.Protoss_Nexus, 10, 10), probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            UnitTelemetry.sample(probe);

            assertTrue(!atlantis.util.log.EventLog.toJsonLines().contains("UNIT_TELEMETRY"),
                "combat units only");
        });
    }
}
