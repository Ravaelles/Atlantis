package tests.unit;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.scout.ScoutUnitManagers;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.scout.ScoutCommander;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnitType;
import atlantis.util.cache.Cache;
import atlantis.util.log.EventLog;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #187: the scout watch must be witnessed, not inferred. No
 * SCOUT-family key exists in the twin vocabulary, so even a perfect
 * game leaves zero rows - the blindness is structural, not a dead
 * scout. SCOUT_WATCH rows the scout's tile next to the known enemy
 * main (throttled): a trail clustered at the main is watching, a
 * trail at far expos is wandering.
 */
@Tag("map")
public class ScoutWatchTest extends WorldStubForTests {

    @BeforeEach
    public void resetScoutState() throws Exception {
        resetSharedScoutStatic();
        EventLog.clearForTests();
    }

    @AfterEach
    public void clearScoutState() throws Exception {
        resetSharedScoutStatic();
        EventLog.clearForTests();
    }

    private void resetSharedScoutStatic() throws Exception {
        ScoutState.reset();
        ScoutState.scouts.clear();
        ScoutState.scoutsKilledCount = 0;
        Field last = EnemyMainBase.class.getDeclaredField("_lastNonNull");
        last.setAccessible(true);
        last.set(null, null);
        Field cache = EnemyMainBase.class.getDeclaredField("cache");
        cache.setAccessible(true);
        ((Cache<?>) cache.get(null)).clear();
    }

    @Test
    public void scoutAtMainRowsItsWatch() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 113, 12);
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, 117, 9);
        foeNexus.setEnemy();

        world(40, fakeOurs(nexus, scout), fakeEnemies(foeNexus), () -> {
            if (atlantis.game.A.now() == 1) {
                ScoutState.scoutUnitManager = new ScoutUnitManagers();
                ScoutState.scouts.add(scout);
                scout.setScout(true);
            }
            if (atlantis.game.A.now() >= 1) {
                new ScoutCommander().handle();
            }
        });

        String rows = EventLog.toJsonLines();
        assertTrue(rows.contains("\"key\":\"SCOUT_WATCH\""),
            "a scout at the enemy main must be witnessed: " + rows);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "\"ux\":(\\d+),\"uy\":(\\d+)").matcher(rows);
        assertTrue(m.find(), "the row carries the scout tile: " + rows);
        double scoutDistToMain = Math.hypot(
            Integer.parseInt(m.group(1)) - 117, Integer.parseInt(m.group(2)) - 9);
        assertTrue(scoutDistToMain <= 8,
            "the scout trails the main (watching), not far expos: " + rows);
        assertTrue(rows.contains("\"ex\":117") && rows.contains("\"ey\":9"),
            "the row carries the known enemy main: " + rows);
    }

    @Test
    public void noScoutNoRows() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, 117, 9);
        foeNexus.setEnemy();

        world(40, fakeOurs(nexus), fakeEnemies(foeNexus), () -> {
            if (atlantis.game.A.now() >= 1) {
                new ScoutCommander().handle();
            }
        });

        String rows = EventLog.toJsonLines();
        assertTrue(!rows.contains("\"key\":\"SCOUT_WATCH\""),
            "no scout, no watch rows: " + rows);
    }
}
