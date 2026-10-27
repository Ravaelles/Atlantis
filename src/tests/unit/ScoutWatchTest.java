package tests.unit;

import atlantis.combat.scout.ScoutUnitManagers;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.scout.ScoutCommander;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        EnemyMainBase.clear();
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
    public void lateDiscoveryStillLatchesTheMain() {
        // B-43: the first get() of a game runs while nothing is discovered
        // (combat focus reads this from the early frames) and that null used
        // to cache forever - ten live games at 0/5111 known-enemy scout rows
        // while the scout stood on the enemy Hatchery. One world: empty at
        // frame 1 (the early miss), the scout arrives at frame 3. Single
        // world on purpose - the harness frame-keyed Select index collides
        // across sequential worlds restarting at frame 1.
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, 117, 9);
        foeNexus.setEnemy();

        world(10, fakeOurs(nexus), new FakeUnit[]{}, () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                assertNull(EnemyMainBase.get(),
                    "nothing discovered yet - a miss, never a latch");
            }
            if (now == 3) {
                enemies = new FakeUnit[]{ foeNexus };
            }
            if (now == 4) {
                assertNotNull(EnemyMainBase.get(),
                    "the late discovery must land despite the early miss");
                assertEquals(117, EnemyMainBase.get().tx());
                assertEquals(9, EnemyMainBase.get().ty());
            }
        });
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
        assertFalse(rows.contains("\"key\":\"SCOUT_WATCH\""), "no scout, no watch rows: " + rows);
    }
}
