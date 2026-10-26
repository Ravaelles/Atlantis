package tests.unit;

import atlantis.cherryvis.simple.ACherryVis_GlobalStates;
import atlantis.cherryvis.simple.ACherryVis_GlobalValues;
import atlantis.cherryvis.simple.ArmyBoard;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.config.env.Env;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * NEXT #214: the army's current task, focus and strength ride the
 * CherryVis board on change, the strength graph samples on a fixed
 * cadence - transitions, never per-frame rows.
 */
@Tag("infra")
public class ArmyBoardTest extends WorldStubForTests {

    @BeforeEach
    public void enable() {
        Env.setCherryVisEnabledForTest(true);
        ArmyBoard.clearForTests();
    }

    @AfterEach
    public void disable() {
        ArmyBoard.clearForTests();
        Env.setCherryVisEnabledForTest(false);
    }

    @Test
    public void boardKeysWriteOncePerValue() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        APosition anchor = APosition.create(90, 20);

        world(64, fakeOurs(nexus, dragoon), new FakeUnit[] {}, () -> {
            if (atlantis.game.A.now() != 5) return;

            ArmyBoard.publish(0, ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FIGHT,
                "Not enough Goons (0/2) vZ", anchor);
            ArmyBoard.publish(1, ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FIGHT,
                "Not enough Goons (0/2) vZ", anchor);

            String board = ACherryVis_GlobalStates.build();
            assertTrue(countKeys(board, "Task") == 1, "same task writes once: " + board);
            assertTrue(countKeys(board, "Focus") == 1, "same focus writes once: " + board);
            assertTrue(countKeys(board, "Strength") == 1, "same strength writes once: " + board);
            assertTrue(countKeys(board, "Reason") == 1, "same reason writes once: " + board);

            ArmyBoard.publish(31, ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FLEE,
                "Outgunned (1 goon, 100%), coming home", anchor);
            board = ACherryVis_GlobalStates.build();
            assertTrue(countKeys(board, "Task") == 2, "changed task writes again: " + board);
            assertTrue(board.contains("\"Task\":\"DEFEND/FLEE\""), "task names posture/tactic: " + board);
            assertTrue(countKeys(board, "Reason") == 2, "changed reason writes again: " + board);
            assertTrue(board.contains("\"Reason\":\"Outgunned (1 goon, 100%), coming home\""),
                "reason speaks with numbers: " + board);

            ArmyBoard.publish(32, ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FLEE,
                "Outgunned (1 goon, 100%), coming home", anchor);
            assertTrue(countKeys(ACherryVis_GlobalStates.build(), "Task") == 2,
                "unchanged task stays silent: " + board);

            String values = ACherryVis_GlobalValues.build(null);
            assertTrue(values.contains("\"31\":{\"Strength\":"), "strength graphs on cadence: " + values);
            assertTrue(!values.contains("\"32\""), "off-cadence frames stay out: " + values);
        });
    }

    private static int countKeys(String json, String key) {
        int count = 0;
        int from = 0;
        String needle = "\"" + key + "\":";
        while ((from = json.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
