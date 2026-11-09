package tests.unit;

import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.formation.FormationOrderLatch;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The latch follows the decided posture (P2: decisions flow down, no
 * re-derivation). A noted ATTACK lets identical orders flow even when
 * the headcount alone would defend; a noted DEFEND throttles them
 * even when the headcount alone would march.
 */
@Tag("combat")
public class LatchFollowsDecidedPostureTest extends WorldStubForTests {

    @AfterEach
    public void cleanLatch() {
        FormationOrderLatch.clearForTests();
        atlantis.combat.cluster.DecidedPosture.clearForTests();
    }

    @Test
    public void notedAttackFlowsDespiteSmallArmy() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(3, fakeOurs(fake(AUnitType.Protoss_Nexus, 15, 15), zealot), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() != 2) return;

            atlantis.combat.cluster.DecidedPosture.note(ClusterMission.Posture.ATTACK);
            AUnit unit = atlantis.units.select.Select.ourCombatUnits().first();
            APosition dest = APosition.create(30, 20);

            assertTrue(FormationOrderLatch.shouldIssue(unit, dest), "first order flows");
            assertTrue(FormationOrderLatch.shouldIssue(unit, dest),
                "ATTACK forgets the latch: no throttle on a 1-zealot headcount-defend");
        });
    }

    @Test
    public void notedDefendThrottlesDespiteBigArmy() {
        FakeUnit[] army = new FakeUnit[9];
        army[0] = fake(AUnitType.Protoss_Nexus, 15, 15);
        for (int i = 1; i < 9; i++) army[i] = fake(AUnitType.Protoss_Zealot, 20 + i, 20);

        world(3, fakeOurs(army), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() != 2) return;

            atlantis.combat.cluster.DecidedPosture.note(ClusterMission.Posture.DEFEND);
            AUnit unit = atlantis.units.select.Select.ourCombatUnits().first();
            APosition dest = APosition.create(30, 20);

            assertTrue(FormationOrderLatch.shouldIssue(unit, dest), "first order flows");
            assertFalse(FormationOrderLatch.shouldIssue(unit, dest),
                "DEFEND throttles the repeat even at 8+ headcount march");
        });
    }
}
