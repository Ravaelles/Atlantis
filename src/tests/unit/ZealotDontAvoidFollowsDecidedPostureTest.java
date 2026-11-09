package tests.unit;

import atlantis.battle.avoid.dont.protoss.ZealotDontAvoidEnemy;
import atlantis.battle.cluster.Missions;
import atlantis.combat.cluster.ClusterMission;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The close-workers dontAvoid gate follows the decided posture (owner
 * 2026-11-09, per-unit avoid fold): a noted ATTACK holds the zealot
 * on the workers, a noted DEFEND lets the outnumbered zealot avoid -
 * and "decided" means the frame's posture, not the global mirror it
 * is still catching up with. Red-first both directions.
 */
@Tag("combat")
public class ZealotDontAvoidFollowsDecidedPostureTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanBefore() {
        atlantis.combat.cluster.DecidedPosture.clearForTests();
        Missions.followPosture(ClusterMission.Posture.DEFEND);
        atlantis.util.log.EventLog.clearForTests();
    }

    @AfterEach
    public void cleanAfter() {
        atlantis.combat.cluster.DecidedPosture.clearForTests();
        Missions.followPosture(ClusterMission.Posture.DEFEND);
        atlantis.util.log.EventLog.clearForTests();
    }

    @Test
    public void notedAttackHoldsDespiteDefendMirror() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 15, 15);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        zealot.setShields(20);
        zealot.cooldown = 5;
        FakeUnit w1 = fake(AUnitType.Protoss_Probe, 30.5, 20);
        FakeUnit w2 = fake(AUnitType.Protoss_Probe, 31, 20);
        w1.setEnemy();
        w2.setEnemy();

        world(3, fakeOurs(nexus, zealot), fakeEnemies(w1, w2), () -> {
            if (atlantis.game.A.now() != 2) return;

            Missions.followPosture(ClusterMission.Posture.DEFEND);
            atlantis.combat.cluster.DecidedPosture.note(ClusterMission.Posture.ATTACK);

            assertTrue(ZealotDontAvoidEnemy.dontAvoid(zealot),
                "decided ATTACK holds the zealot on the workers even while the mirror still says DEFEND");
        });
    }

    @Test
    public void notedDefendAvoidsDespiteAttackMirror() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 15, 15);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        zealot.setShields(20);
        zealot.cooldown = 5;
        FakeUnit w1 = fake(AUnitType.Protoss_Probe, 30.5, 20);
        FakeUnit w2 = fake(AUnitType.Protoss_Probe, 31, 20);
        w1.setEnemy();
        w2.setEnemy();

        world(3, fakeOurs(nexus, zealot), fakeEnemies(w1, w2), () -> {
            if (atlantis.game.A.now() != 2) return;

            Missions.followPosture(ClusterMission.Posture.ATTACK);
            atlantis.combat.cluster.DecidedPosture.note(ClusterMission.Posture.DEFEND);

            assertFalse(ZealotDontAvoidEnemy.dontAvoid(zealot),
                "decided DEFEND lets the outnumbered zealot avoid even while the mirror still says ATTACK");
        });
    }
}
