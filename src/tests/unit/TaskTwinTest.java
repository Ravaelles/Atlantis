package tests.unit;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.twins.TaskTwin;
import atlantis.util.log.EventLog;
import bwapi.Race;
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
 * Task twin (mission + task direction, owner 2026-11-09): every frame
 * the cluster computes its task from the enforced mission and rows it
 * on transition (steady states heartbeat). Shadow twin first, gate
 * later (MDC P4): this proves the task is computed live and stable
 * before any enforcement reads it.
 */
@Tag("combat")
public class TaskTwinTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanLogBefore() {
        EventLog.clearForTests();
        TaskTwin.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        TaskTwin.clearForTests();
    }

    private static int countRows(String text) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = text.indexOf("\"key\":\"TASK\"", from);
            if (at < 0) return count;

            count++;
            from = at + 1;
        }
    }

    @Test
    public void transitionRowsWithTaskFieldsAndRepeatsStaySilent() {
        FakeUnit nexus = fake(atlantis.units.AUnitType.Protoss_Nexus, 20, 20);

        world(3, fakeOurs(nexus), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TaskTwin.sample(ArmyMission.HOLD_MAIN_CHOKE);
            TaskTwin.sample(ArmyMission.HOLD_MAIN_CHOKE);
            TaskTwin.sample(ArmyMission.HOLD_NATURAL_CHOKE);

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"mission\":\"HOLD_MAIN_CHOKE\""), "got: " + rows);
            assertTrue(rows.contains("\"anchor\":\"MAIN_CHOKE\""), "got: " + rows);
            assertTrue(rows.contains("\"posture\":\"DEFEND\""), "got: " + rows);
            assertEquals(2, countRows(rows),
                "one row per transition, repeats stay silent: " + rows);
        });
    }

    @Test
    public void steadyStateHeartbeats() {
        FakeUnit nexus = fake(atlantis.units.AUnitType.Protoss_Nexus, 20, 20);

        world(TaskTwin.HEARTBEAT_FRAMES + 5, fakeOurs(nexus), fakeEnemies(), () -> {
            if (atlantis.game.A.now() < 2) return;

            TaskTwin.sample(ArmyMission.HOLD_MAIN);
        });

        assertEquals(2, countRows(EventLog.toJsonLines()),
            "transition plus one heartbeat over the window");
    }
}
