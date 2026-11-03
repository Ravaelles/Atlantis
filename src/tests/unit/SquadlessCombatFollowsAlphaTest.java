package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.Missions;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Owner rule: a combat unit is never legitimately squadless - it is a
 * bug being surfaced (throttled trace), and meanwhile the unit follows
 * the army (Alpha) instead of a silent DEFEND. Workers keep the old
 * hold: squadless non-combat still reads DEFEND with no trace.
 *
 * <p>Fast pin for the mission-default slice: the acceptance AUnit
 * pair pins the same rule against the Alpha seam, this one guards it
 * in the fast loop.</p>
 */
@Tag("combat")
public class SquadlessCombatFollowsAlphaTest {

    @Test
    public void squadlessCombatFollowsAlpha() {
        FakeUnit vulture = fake(AUnitType.Terran_Vulture, 10, 10);

        Alpha.get().setMission(Missions.ATTACK);
        try {
            assertTrue(vulture.isMissionAttack(),
                "squadless combat follows the army's mission, never a silent DEFEND");
            assertFalse(vulture.isMissionDefend(),
                "ATTACK from Alpha reads as attack, not defend");
        }
        finally {
            Alpha.get().setMission(null);
        }
    }

    @Test
    public void squadlessWorkerStillHoldsDefend() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 10, 10);

        assertTrue(probe.isMissionDefend(),
            "the rule is combat-only: a squadless worker keeps DEFEND, quietly");
    }
}
