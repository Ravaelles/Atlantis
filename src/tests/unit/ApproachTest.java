package tests.unit;

import atlantis.combat.cluster.Approach;
import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterTask;
import atlantis.combat.strategy.BattleStrategy;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Approach vocabulary + snapshot boundary (owner 2026-11-09, approach
 * narrowing slice 1): per-mission defaults from the MDC §2 table, and
 * the geometry record the narrowing slices will decide on. Shadow only:
 * nothing reads the answer yet, so these pins prove the table covers
 * every mission and the snapshot bottoms out sanely - they do not
 * change behavior.
 */
@Tag("combat")
public class ApproachTest {

    @Test
    public void holdsStandTheChoke() {
        assertEquals(BattleStrategy.CHOKE_HOLD, Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_MAIN)));
        assertEquals(BattleStrategy.CHOKE_HOLD, Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_MAIN_CHOKE)));
        assertEquals(BattleStrategy.CHOKE_HOLD, Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_NATURAL_CHOKE)));
        assertEquals(BattleStrategy.CHOKE_HOLD, Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_MIDDLE)));
    }

    @Test
    public void stagesAndAttacksMarchTheOpenField() {
        assertEquals(BattleStrategy.OPEN_FIELD, Approach.forTask(ClusterTask.forMission(ArmyMission.STAGE_MIDDLE)));
        assertEquals(BattleStrategy.OPEN_FIELD,
            Approach.forTask(ClusterTask.forMission(ArmyMission.ATTACK_ENEMY_BASE)));
    }

    @Test
    public void nullTaskHoldsLikeMain() {
        assertEquals(BattleStrategy.CHOKE_HOLD, Approach.forTask(null),
            "no task is a home hold, never a march");
    }

    @Test
    public void nullUnitCapturesDefaultsWithoutThrowing() {
        Approach.Geometry g = Approach.capture(null, null);

        assertFalse(g.chokeNear);
        assertFalse(g.inContact);
        assertEquals(0, g.combatants);
        assertEquals(0, g.enemiesNearChoke);
        assertEquals(0, g.oursNearChoke);
    }

    @Test
    public void emptyGeometryIsADuelAndCrushesNothing() {
        Approach.Geometry g = new Approach.Geometry();

        assertTrue(g.isDuel(), "baseless + headcount 0: duel scale by the same rule as the live check");
        assertTrue(g.crushing(), "no enemies around the choke: the line may push");
    }

    @Test
    public void crushNeedsTheDocumentedRatio() {
        Approach.Geometry g = new Approach.Geometry();
        g.oursNearChoke = 3;
        g.enemiesNearChoke = 2;

        assertFalse(g.crushing(), "3v2 is below the 2.0 crush ratio: hold, don't push");

        g.oursNearChoke = 4;

        assertTrue(g.crushing(), "4v2 meets it: collapse");
    }

    @Test
    public void holdCrushingAContestedChokeCollapses() {
        Approach.Geometry g = new Approach.Geometry();
        g.chokeNear = true;
        g.enemiesNearChoke = 2;
        g.oursNearChoke = 4;

        assertEquals(BattleStrategy.COLLAPSE,
            Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_NATURAL_CHOKE), g),
            "crush at a contested choke: join the bait instead of standing");
        assertEquals(BattleStrategy.COLLAPSE,
            Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_MAIN), g));
        assertEquals(BattleStrategy.COLLAPSE,
            Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_MIDDLE), g));
    }

    @Test
    public void holdWithoutCrushKeepsTheStandoff() {
        Approach.Geometry g = new Approach.Geometry();
        g.chokeNear = true;
        g.enemiesNearChoke = 2;
        g.oursNearChoke = 3;

        assertEquals(BattleStrategy.CHOKE_HOLD,
            Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_NATURAL_CHOKE), g),
            "no crush: hold, don't push");
    }

    @Test
    public void uncontestedChokeKeepsTheDefault() {
        Approach.Geometry g = new Approach.Geometry();
        g.chokeNear = true;
        g.enemiesNearChoke = 0;
        g.oursNearChoke = 4;

        assertEquals(BattleStrategy.CHOKE_HOLD,
            Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_MAIN_CHOKE), g),
            "nothing to collapse onto: the default stands");
    }

    @Test
    public void attackKeepsMarchingEvenCrushing() {
        Approach.Geometry g = new Approach.Geometry();
        g.chokeNear = true;
        g.enemiesNearChoke = 2;
        g.oursNearChoke = 4;

        assertEquals(BattleStrategy.OPEN_FIELD,
            Approach.forTask(ClusterTask.forMission(ArmyMission.ATTACK_ENEMY_BASE), g),
            "attack shapes get their own slice once observed");
        assertEquals(BattleStrategy.OPEN_FIELD,
            Approach.forTask(ClusterTask.forMission(ArmyMission.STAGE_MIDDLE), g));
    }

    @Test
    public void nullGeometryKeepsTheDefault() {
        assertEquals(BattleStrategy.CHOKE_HOLD,
            Approach.forTask(ClusterTask.forMission(ArmyMission.HOLD_MAIN), null));
        assertEquals(BattleStrategy.OPEN_FIELD,
            Approach.forTask(ClusterTask.forMission(ArmyMission.ATTACK_ENEMY_BASE), null));
    }
}
