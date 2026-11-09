package tests.unit;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.retreating.RetreatFloor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Task rally on the floor twin (mission + task direction, owner
 * 2026-11-09): the RETREAT_FLOOR row carries what the enforced
 * mission wants the rally to be next to what the floor chose, so the
 * next enforcement slice reads the divergence instead of guessing
 * it. Pure mapping pin - the row plumbing rides the existing clamp
 * tests that execute the same twin path every suite run.
 */
@Tag("combat")
public class RetreatFloorTaskRallyTest {

    @Test
    public void everyMissionNamesItsRally() {
        assertEquals("-", RetreatFloor.taskRallyNameFor(null),
            "no mission, no rally");
        assertEquals("-", RetreatFloor.taskRallyNameFor(ArmyMission.HOLD_MAIN),
            "the main holds in place: no rally line");
        assertEquals("MAIN_CHOKE",
            RetreatFloor.taskRallyNameFor(ArmyMission.HOLD_MAIN_CHOKE));
        assertEquals("NATURAL_CHOKE",
            RetreatFloor.taskRallyNameFor(ArmyMission.HOLD_NATURAL_CHOKE));
        assertEquals("MIDDLE_CHOKE",
            RetreatFloor.taskRallyNameFor(ArmyMission.STAGE_MIDDLE));
        assertEquals("MIDDLE_CHOKE",
            RetreatFloor.taskRallyNameFor(ArmyMission.HOLD_MIDDLE));
        assertEquals("-", RetreatFloor.taskRallyNameFor(ArmyMission.ATTACK_ENEMY_BASE),
            "the attack leaves routing to the retreat ladder");
    }
}
