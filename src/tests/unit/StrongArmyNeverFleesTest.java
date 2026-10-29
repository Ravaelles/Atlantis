package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterTactic;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.Army;
import atlantis.units.AUnitType;
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
 * Strong army never flees (owner 2026-10-29): at double the enemy
 * strength the cluster stands and fights - FLEE at 200 sat a finished
 * army in the base, oscillating with the retreat instead of marching
 * out. 6 Dragoons at 243% read FIGHT even though the headcount rule
 * alone would still defend and the evaluator alone would still flee.
 */
@Tag("combat")
public class StrongArmyNeverFleesTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @Override
    public bwapi.Race initEnemyRace() {
        return bwapi.Race.Zerg;
    }

    @AfterEach
    public void clearAlpha() {
        for (atlantis.units.AUnit u : Alpha.get().selection().list()) {
            Alpha.get().removeUnit(u);
        }
        ClusterTactic.reset();
    }

    @Test
    public void doubleStrengthAlwaysFights() {
        FakeUnit g1 = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit g2 = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit g3 = fake(AUnitType.Protoss_Dragoon, 20, 21);
        FakeUnit g4 = fake(AUnitType.Protoss_Dragoon, 21, 21);
        FakeUnit g5 = fake(AUnitType.Protoss_Dragoon, 22, 20);
        FakeUnit g6 = fake(AUnitType.Protoss_Dragoon, 22, 21);
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 30, 20);
        l1.setEnemy();

        world(3, fakeOurs(g1, g2, g3, g4, g5, g6), fakeEnemies(l1), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(l1);

            assertTrue(Army.strength() >= ClusterTactic.STRONG_ARMY_NEVER_FLEE,
                "fixture check: 6 dragoons vs 1 ling must read 200+, got " + Army.strength());
            assertTrue(ClusterTactic.strongArmyStands(), "6 dragoons at 200%+ stand");
            assertEquals(FightOrFlight.Tactic.FIGHT, ClusterTactic.decide(),
                "double strength never flees, whatever the evaluator says");
        });
    }
}
