package tests.unit;

import atlantis.battle.cluster.Missions;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.targeting.ChaseVeto;
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
 * The chase veto follows the decided posture (owner 2026-11-09, P2):
 * an ATTACK march is never vetoed, a DEFEND hold vetoes far marches -
 * and "decided" means the frame's posture, not the global mirror it
 * is still catching up with. Red-first both directions.
 */
@Tag("combat")
public class ChaseVetoFollowsDecidedPostureTest extends WorldStubForTests {

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
    public void notedDefendVetoesDespiteAttackMirror() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 27, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 36, 20);
        foe.setEnemy();

        world(3, fakeOurs(nexus, zealot), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            Missions.followPosture(ClusterMission.Posture.ATTACK);
            atlantis.combat.cluster.DecidedPosture.note(ClusterMission.Posture.DEFEND);

            assertTrue(ChaseVeto.veto(zealot, foe),
                "decided DEFEND vetoes the 9-tile march even while the mirror still says ATTACK");
        });
    }

    @Test
    public void notedAttackMarchesDespiteDefendMirror() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 27, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 36, 20);
        foe.setEnemy();

        world(3, fakeOurs(nexus, zealot), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            Missions.followPosture(ClusterMission.Posture.DEFEND);
            atlantis.combat.cluster.DecidedPosture.note(ClusterMission.Posture.ATTACK);

            assertFalse(ChaseVeto.veto(zealot, foe),
                "decided ATTACK marches even while the mirror still says DEFEND");
        });
    }
}
