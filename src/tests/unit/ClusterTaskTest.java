package tests.unit;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.ClusterTask;
import atlantis.combat.cluster.TaskAnchor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mission + task vocabulary (owner 2026-11-09): the mission names the
 * cluster's job, the task instantiates it for the frame - anchor kind,
 * posture, whether FLEE is allowed and where it rallies. Pure table,
 * no game reads, no positions (kinds, not tiles - the rally code
 * resolves points). Shadow only, like slice 1a: nothing reads the
 * answer yet, so these pins prove the table covers the MDC §2 rows,
 * they do not change behavior.
 */
@Tag("combat")
public class ClusterTaskTest {

    @Test
    public void holdMainStaysHomeAndNeverFlees() {
        ClusterTask task = ClusterTask.forMission(ArmyMission.HOLD_MAIN);

        assertEquals(TaskAnchor.MAIN_BASE, task.anchor());
        assertEquals(ClusterMission.Posture.DEFEND, task.posture());
        assertFalse(task.fleeAllowed(), "FLEE only on breach-overrun, never as a tactic");
        assertNull(task.fleeRally(), "no rally: the line does not leave");
    }

    @Test
    public void holdMainChokeFleesToItsOwnLine() {
        ClusterTask task = ClusterTask.forMission(ArmyMission.HOLD_MAIN_CHOKE);

        assertEquals(TaskAnchor.MAIN_CHOKE, task.anchor());
        assertEquals(ClusterMission.Posture.DEFEND, task.posture());
        assertTrue(task.fleeAllowed(), "losing at the choke falls back along it");
        assertEquals(TaskAnchor.MAIN_CHOKE, task.fleeRally(),
            "rally is the same line, never home past it");
    }

    @Test
    public void holdNaturalChokeMirrorsMain() {
        ClusterTask task = ClusterTask.forMission(ArmyMission.HOLD_NATURAL_CHOKE);

        assertEquals(TaskAnchor.NATURAL_CHOKE, task.anchor());
        assertEquals(ClusterMission.Posture.DEFEND, task.posture());
        assertTrue(task.fleeAllowed());
        assertEquals(TaskAnchor.NATURAL_CHOKE, task.fleeRally());
    }

    @Test
    public void stageMiddleFormsUpAndStopsRetreatAtTheLine() {
        ClusterTask task = ClusterTask.forMission(ArmyMission.STAGE_MIDDLE);

        assertEquals(TaskAnchor.MIDDLE_CHOKE, task.anchor());
        assertEquals(ClusterMission.Posture.DEFEND, task.posture());
        assertTrue(task.fleeAllowed());
        assertEquals(TaskAnchor.MIDDLE_CHOKE, task.fleeRally(),
            "retreat stops AT the middle line, never past it homeward");
    }

    @Test
    public void holdMiddleKeepsTheLine() {
        ClusterTask task = ClusterTask.forMission(ArmyMission.HOLD_MIDDLE);

        assertEquals(TaskAnchor.MIDDLE_CHOKE, task.anchor());
        assertTrue(task.fleeAllowed());
        assertEquals(TaskAnchor.MIDDLE_CHOKE, task.fleeRally());
    }

    @Test
    public void attackMarchesOnTheEnemyBase() {
        ClusterTask task = ClusterTask.forMission(ArmyMission.ATTACK_ENEMY_BASE);

        assertEquals(TaskAnchor.ENEMY_BASE, task.anchor());
        assertEquals(ClusterMission.Posture.ATTACK, task.posture());
        assertTrue(task.fleeAllowed(), "lost attacks fall back");
        assertNull(task.fleeRally(), "no mission rally: the retreat ladder owns routing");
    }
}
