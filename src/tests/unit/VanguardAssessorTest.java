package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.ClusterTactic;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The gate listens to the hottest point of the cluster, not the leader
 * (owner 2026-10-19: one cluster, one area assessment - a leader at the
 * back reads a different fight than the vanguard taking contact).
 * The unit closest to an enemy within 15 tiles assesses; with no enemy
 * in the area the leader assesses (eval 9874, march on).
 */
public class VanguardAssessorTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void vanguardInContactAssesses() {
        FakeUnit leader = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit vanguard = fake(AUnitType.Protoss_Zealot, 40, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 42, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 20),
            leader, vanguard
        ), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 3) return;

            Alpha.get().addUnit(leader);
            Alpha.get().addUnit(vanguard);
            try {
                assertSame(vanguard,
                    ClusterTactic.assessVanguard(leader),
                    "the vanguard 2 tiles from the enemy assesses, not the leader 22 away");
            } finally {
                Alpha.get().removeUnit(leader);
                Alpha.get().removeUnit(vanguard);
            }
        });
    }

    @Test
    public void leaderAssessesQuietArea() {
        FakeUnit leader = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit vanguard = fake(AUnitType.Protoss_Zealot, 40, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 60, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 20),
            leader, vanguard
        ), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 3) return;

            Alpha.get().addUnit(leader);
            Alpha.get().addUnit(vanguard);
            try {
                assertSame(leader,
                    ClusterTactic.assessVanguard(leader),
                    "20 tiles to the enemy is march territory for the whole cluster");
            } finally {
                Alpha.get().removeUnit(leader);
                Alpha.get().removeUnit(vanguard);
            }
        });
    }

    @Test
    public void noLeaderFallsBackToContact() {
        FakeUnit vanguard = fake(AUnitType.Protoss_Zealot, 40, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 42, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 20),
            vanguard
        ), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 3) return;

            Alpha.get().addUnit(vanguard);
            try {
                assertSame(vanguard,
                    ClusterTactic.assessVanguard(null),
                    "contact assesses even with no leader");
            } finally {
                Alpha.get().removeUnit(vanguard);
            }
        });
    }
}
