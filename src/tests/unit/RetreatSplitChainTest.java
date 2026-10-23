package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.ClusterTactic;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.retreating.RetreatFromLostFight;
import atlantis.config.env.Env;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-23, from the lost hydra fight: the retreat splits by
 * contact. Chased (visible armed enemy in the threat envelope) keeps the
 * ClusterFlee scatter; lost-but-unchased walks home in formation
 * (ClusterOrganizedRetreat). And a blocking army between the cluster and
 * home is fought through, never fled into (Stardust rule).
 */
@Tag("combat")
public class RetreatSplitChainTest extends WorldStubForTests {

    @AfterEach
    public void resetTactic() {
        ClusterTactic.reset();
        Env.setLocalForTest(false);
    }

    @Test
    public void chasedRetreatKeepsClusterFlee() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 30);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() == 1) {
                Env.setLocalForTest(true);
                boolean acted = RetreatFromLostFight.retreat(
                    dragoon, FightOrFlight.Tactic.FLEE, nexus.position(), null);
                assertTrue(acted, "retreat issues an order when chased");
                assertEquals(Actions.RUN_ENEMY, dragoon.action(),
                    "chased retreat keeps the FLEE action");
            }
        });
    }

    @Test
    public void lostButUnchasedWalksHomeInFormation() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 30);

        world(3, fakeOurs(nexus, dragoon), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() == 1) {
                Env.setLocalForTest(true);
                boolean acted = RetreatFromLostFight.retreat(
                    dragoon, FightOrFlight.Tactic.FLEE, nexus.position(), null);
                assertTrue(acted, "organized retreat issues an order");
                assertEquals(Actions.MOVE_FORMATION, dragoon.action(),
                    "unchased retreat walks home in formation");
            }
        });
    }

    @Test
    public void blockingArmyIsFoughtThrough() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 30);
        FakeUnit h1 = fake(AUnitType.Zerg_Hydralisk, 45, 33);
        FakeUnit h2 = fake(AUnitType.Zerg_Hydralisk, 47, 35);
        FakeUnit h3 = fake(AUnitType.Zerg_Hydralisk, 43, 31);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(h1, h2, h3), () -> {
            if (atlantis.game.A.now() == 1) {
                Alpha.gather();
                assertEquals(FightOrFlight.Tactic.FIGHT, ClusterTactic.decide(),
                    "armed enemies between the cluster and home are fought, never fled into");
            }
        });
    }

    @Test
    public void unblockedLosingFightStillFlees() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 30);
        FakeUnit h1 = fake(AUnitType.Zerg_Hydralisk, 75, 27);
        FakeUnit h2 = fake(AUnitType.Zerg_Hydralisk, 77, 29);
        FakeUnit h3 = fake(AUnitType.Zerg_Hydralisk, 73, 25);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(h1, h2, h3), () -> {
            if (atlantis.game.A.now() == 1) {
                Alpha.gather();
                assertEquals(FightOrFlight.Tactic.FLEE, ClusterTactic.decide(),
                    "losing with a clear way home keeps the retreat");
            }
        });
    }
}
