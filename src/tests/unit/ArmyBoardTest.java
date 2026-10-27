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

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #214: the army's current task, focus and strength ride the
 * CherryVis board on change, the strength graph samples on a fixed
 * cadence - transitions, never per-frame rows.
 */
@Tag("infra")
public class ArmyBoardTest extends WorldStubForTests {

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

        world(64, fakeOurs(nexus, dragoon), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() != 5) return;

            ArmyBoard.publish(0, ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FIGHT,
                "Not enough Goons (0/2) vZ", anchor);
            ArmyBoard.publish(1, ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FIGHT,
                "Not enough Goons (0/2) vZ", anchor);

            String board = ACherryVis_GlobalStates.build();
            assertEquals(1, countKeys(board, "AlphaMission"), "same mission writes once: " + board);
            assertEquals(1, countKeys(board, "AlphaTactic"), "same tactic writes once: " + board);
            assertEquals(1, countKeys(board, "AlphaFocus"), "same focus writes once: " + board);
            assertEquals(1, countKeys(board, "AlphaFocusName"), "same focus name writes once: " + board);
            assertEquals(1, countKeys(board, "AlphaStrength"), "same strength writes once: " + board);
            assertEquals(1, countKeys(board, "AlphaReason"), "same reason writes once: " + board);

            ArmyBoard.publish(31, ClusterMission.Posture.ATTACK, FightOrFlight.Tactic.FLEE,
                "Outgunned (1 goon, 100%), coming home", anchor);
            board = ACherryVis_GlobalStates.build();
            assertEquals(2, countKeys(board, "AlphaMission"), "changed mission writes again: " + board);
            assertTrue(board.contains("\"AlphaMission\":\"ATTACK\""), "mission names where the army belongs: " + board);
            assertEquals(2, countKeys(board, "AlphaTactic"), "changed tactic writes again: " + board);
            assertTrue(board.contains("\"AlphaTactic\":\"FLEE\""), "tactic names what it does: " + board);
            assertEquals(2, countKeys(board, "AlphaReason"), "changed reason writes again: " + board);
            assertTrue(board.contains("\"AlphaReason\":\"Outgunned (1 goon, 100%), coming home\""),
                "reason speaks with numbers: " + board);

            ArmyBoard.publish(32, ClusterMission.Posture.ATTACK, FightOrFlight.Tactic.FLEE,
                "Outgunned (1 goon, 100%), coming home", anchor);
            assertEquals(2, countKeys(ACherryVis_GlobalStates.build(), "AlphaMission"), "unchanged mission stays silent: " + board);

            String values = ACherryVis_GlobalValues.build(null);
            assertTrue(values.contains("\"31\":{\"Strength\":"), "strength graphs on cadence: " + values);
            assertFalse(values.contains("\"32\""), "off-cadence frames stay out: " + values);

            atlantis.battle.advance.focus.AFocusPoint named =
                new atlantis.battle.advance.focus.AFocusPoint(nexus, "MiddleFocusChoke");
            ArmyBoard.publish(33, ClusterMission.Posture.ATTACK, FightOrFlight.Tactic.FLEE,
                "Outgunned (1 goon, 100%), coming home", named);
            String namedBoard = ACherryVis_GlobalStates.build();
            assertTrue(namedBoard.contains("MiddleFocusChoke ["),
                "the focus name carries its tile: " + namedBoard);
        });
    }
}
