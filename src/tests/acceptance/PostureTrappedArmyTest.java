package tests.acceptance;

import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.focus.PostureReason;
import atlantis.combat.focus.PostureReasons;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NEXT #197: the trapped shape from the live report, wired end to end -
 * four dragoons boxed against six early zealots flip the posture to
 * ATTACK with the trapped reason (pre-wire: DEFEND by headcount).
 */
public class PostureTrappedArmyTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void resetLatch() {
        // The latch is per-game state (its own test seam says never
        // cross-test): without this a DEFEND latched by an earlier test
        // holds past frame 12 and the flip never fires - order-dependent
        // red, measured in the full acceptance package.
        ClusterMission.resetForTests();
    }

    @AfterEach
    public void clearLatch() {
        ClusterMission.resetForTests();
    }

    @Test
    public void trappedPostureHoldsAfterContactLost() {
        FakeUnit d1 = fake(AUnitType.Protoss_Dragoon, 48, 10);
        FakeUnit d2 = fake(AUnitType.Protoss_Dragoon, 49, 10);
        FakeUnit d3 = fake(AUnitType.Protoss_Dragoon, 50, 10);
        FakeUnit d4 = fake(AUnitType.Protoss_Dragoon, 51, 10);
        FakeUnit[] foes = new FakeUnit[]{
            fake(AUnitType.Protoss_Zealot, 30, 9), fake(AUnitType.Protoss_Zealot, 31, 9),
            fake(AUnitType.Protoss_Zealot, 32, 9), fake(AUnitType.Protoss_Zealot, 30, 10),
            fake(AUnitType.Protoss_Zealot, 31, 10), fake(AUnitType.Protoss_Zealot, 32, 10)};
        for (FakeUnit zealot : foes) zealot.setEnemy();

        world(400, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            d1, d2, d3, d4
        ), foes, () -> {
            int now = atlantis.game.A.now();
            if (now == 12) {
                assertEquals(ClusterMission.Posture.ATTACK,
                    ClusterMission.postureFor(Count.ourCombatUnits(), false),
                    "trapped shape marches out");
            }
            if (now == 40) {
                for (FakeUnit zealot : foes) zealot.hp = 0;
            }
            if (now == 60) {
                assertEquals(ClusterMission.Posture.ATTACK,
                    ClusterMission.postureFor(Count.ourCombatUnits(), false),
                    "contact lost, but the flip holds its window");
            }
            if (now == 260) {
                assertEquals(ClusterMission.Posture.DEFEND,
                    ClusterMission.postureFor(Count.ourCombatUnits(), false),
                    "window over: small army with no threat holds home");
            }
        });
    }

    @Test
    public void trappedArmyMarchesOut() {
        FakeUnit d1 = fake(AUnitType.Protoss_Dragoon, 48, 10);
        FakeUnit d2 = fake(AUnitType.Protoss_Dragoon, 49, 10);
        FakeUnit d3 = fake(AUnitType.Protoss_Dragoon, 50, 10);
        FakeUnit d4 = fake(AUnitType.Protoss_Dragoon, 51, 10);
        FakeUnit z1 = fake(AUnitType.Protoss_Zealot, 30, 9);
        FakeUnit z2 = fake(AUnitType.Protoss_Zealot, 31, 9);
        FakeUnit z3 = fake(AUnitType.Protoss_Zealot, 32, 9);
        FakeUnit z4 = fake(AUnitType.Protoss_Zealot, 30, 10);
        FakeUnit z5 = fake(AUnitType.Protoss_Zealot, 31, 10);
        FakeUnit z6 = fake(AUnitType.Protoss_Zealot, 32, 10);
        for (FakeUnit zealot : new FakeUnit[]{z1, z2, z3, z4, z5, z6}) zealot.setEnemy();

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            d1, d2, d3, d4
        ), new FakeUnit[]{z1, z2, z3, z4, z5, z6}, () -> {
            if (atlantis.game.A.now() != 12) return;

            PostureReasons.Verdict verdict = PostureReasons.decideLive();

            assertEquals(PostureReason.TRAPPED_BY_MASS_ZEALOTS, verdict.reason,
                "four dragoons against six early zealots name the trap");
            assertEquals(ClusterMission.Posture.ATTACK,
                ClusterMission.postureFor(Count.ourCombatUnits(), false),
                "the posture marches out instead of holding the pocket");
        });
    }
}
