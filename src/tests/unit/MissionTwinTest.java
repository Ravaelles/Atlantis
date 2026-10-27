package tests.unit;

import atlantis.battle.cluster.Missions;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MISSION twin (NEXT #96): global mission transitions row with from/to
 * and the reason, so the unenaction sanity check names what moved the
 * mission instead of guessing from posture twins. Rows only on actual
 * change - followPosture runs every frame and a row per frame would
 * drown the log it exists to clarify.
 */
@Tag("combat")
public class MissionTwinTest extends WorldStubForTests {

    private static int countRows(String text, String needle) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = text.indexOf(needle, from);
            if (at < 0) return count;

            count++;
            from = at + needle.length();
        }
    }

    @BeforeEach
    public void cleanLogBefore() {
        EventLog.clearForTests();
        Missions.reset();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        Missions.reset();
    }

    @Test
    public void transitionsRowWithReasonAndRepeatsStaySilent() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(2, fakeOurs(dragoon), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            Missions.forceGlobalMissionAttack("test-advance");
            Missions.forceGlobalMissionAttack("test-advance-again");
            Missions.forceGlobalMissionDefend("test-hold");
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"MISSION\""), "got: " + rows);
            assertTrue(rows.contains("\"from\":\"Defend\""), "got: " + rows);
            assertTrue(rows.contains("\"to\":\"Attack\""), "got: " + rows);
            assertTrue(rows.contains("\"reason\":\"test-advance\""), "got: " + rows);
            assertEquals(2, countRows(rows, "\"key\":\"MISSION\""),
                "one row per transition, repeats stay silent: " + rows);
        });
    }
}
