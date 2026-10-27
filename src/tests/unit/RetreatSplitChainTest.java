package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterTactic;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.config.env.Env;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

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

    @Test
    public void cutOffDragoonFightsInsteadOfMarchingThrough() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 70, 30);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 72, 30);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 74, 30);
        FakeUnit lone = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 31, 30);

        world(3, fakeOurs(nexus, a, b, c, lone), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 1) return;

            Env.setLocalForTest(true);
            try {
                clearAlpha();
                Alpha.gather();
                assertEquals(4, Alpha.get().size(), "test setup: the lone dragoon joined the cluster");

                boolean acted = RetreatFromLostFight.retreat(
                    lone, FightOrFlight.Tactic.FLEE, nexus.position(), null);

                assertFalse(acted,
                    "grabbed and cut off: falls through to fight instead of marching through the pack");
                assertNull(lone.targetPosition(),
                    "no march order issued while deciding to fight");
            }
            finally {
                clearAlpha();
            }
        });
    }

    @Test
    public void cutOffDragoonFleesAwayInsteadOfHome() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 70, 30);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 72, 30);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 74, 30);
        FakeUnit lone = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 38, 30);

        world(3, fakeOurs(nexus, a, b, c, lone), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 1) return;

            Env.setLocalForTest(true);
            try {
                clearAlpha();
                Alpha.gather();

                boolean acted = RetreatFromLostFight.retreat(
                    lone, FightOrFlight.Tactic.FLEE, nexus.position(), null);

                assertTrue(acted, "cut off dragoon with an approaching enemy still retreats");
                HasPosition dest = lone.targetPosition();
                assertNotNull(dest, "a local escape step is issued");
                assertTrue(dest.distTo(foe) > lone.distTo(foe),
                    "away from the pursuer, not toward home through it: foe at " + dest.distTo(foe));
            }
            finally {
                clearAlpha();
            }
        });
    }

    @Test
    public void dragoonWithTheArmyKeepsCoordinatedRetreat() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 60, 30);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 62, 30);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 64, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 61, 31);

        world(3, fakeOurs(nexus, a, b, c), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 1) return;

            Env.setLocalForTest(true);
            try {
                clearAlpha();
                Alpha.gather();

                boolean acted = RetreatFromLostFight.retreat(
                    a, FightOrFlight.Tactic.FLEE, nexus.position(), null);

                assertTrue(acted, "with the army the coordinated retreat still marches home");
                assertEquals(Actions.RUN_ENEMY, a.action(),
                    "chased with the pack: still the FLEE action, not a solo stand");
            }
            finally {
                clearAlpha();
            }
        });
    }

    private static void clearAlpha() {
        for (AUnit u : Alpha.get().selection().list()) Alpha.get().removeUnit(u);
    }
}
