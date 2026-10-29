package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterTactic;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * No early attacking vs Zerg without Dragoons while weak (owner
 * 2026-10-29): Zerg x early game x zero standing Dragoons x hedged
 * strength under 155 forces the retreat instead of letting the
 * evaluator trade an early army without ranged units.
 */
@Tag("combat")
public class WeakEarlyZergTest extends WorldStubForTests {

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

    private static FakeUnit zealot(double tx, double ty) {
        return fake(AUnitType.Protoss_Zealot, tx, ty);
    }

    private static FakeUnit ling(double tx, double ty) {
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, tx, ty);
        ling.setEnemy();

        return ling;
    }

    @Test
    public void weakZealotsRetreatEarlyVsZerg() {
        FakeUnit z1 = zealot(20, 20);
        FakeUnit l1 = ling(22, 20);
        FakeUnit l2 = ling(23, 20);
        FakeUnit l3 = ling(22, 21);
        FakeUnit l4 = ling(23, 21);

        world(3, fakeOurs(z1), fakeEnemies(l1, l2, l3, l4), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(l1);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(l2);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(l3);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(l4);

            assertTrue(ClusterTactic.weakEarlyVsZergWithoutDragoons(),
                "1 zealot vs 4 lings early is way under 155");
        });
    }

    @Test
    public void dragoonVetoesTheGate() {
        FakeUnit z1 = zealot(20, 20);
        FakeUnit goon = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit l1 = ling(22, 20);
        FakeUnit l2 = ling(23, 20);
        FakeUnit l3 = ling(22, 21);
        FakeUnit l4 = ling(23, 21);

        world(3, fakeOurs(z1, goon), fakeEnemies(l1, l2, l3, l4), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(l1);

            assertFalse(ClusterTactic.weakEarlyVsZergWithoutDragoons(),
                "a standing Dragoon answers the no-ranged-units worry");
        });
    }

    @Test
    public void strongArmyFightsOn() {
        FakeUnit[] zealots = new FakeUnit[6];
        for (int i = 0; i < zealots.length; i++) {
            zealots[i] = zealot(20 + i * 0.5, 20);
        }
        FakeUnit ling = ling(25, 20);

        world(3, fakeOurs(zealots), fakeEnemies(ling), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(ling);

            assertFalse(ClusterTactic.weakEarlyVsZergWithoutDragoons(),
                "6 zealots vs 1 ling is no weak army");
        });
    }

    @Test
    public void weakEarlyArmyFleesInsteadOfTrading() {
        FakeUnit z1 = zealot(20, 20);
        FakeUnit l1 = ling(22, 20);
        FakeUnit l2 = ling(23, 20);

        world(3, fakeOurs(z1), fakeEnemies(l1, l2), () -> {
            if (atlantis.game.A.now() != 2) return;

            EnemyUnitsUpdater.weDiscoveredEnemyUnit(l1);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(l2);
            Alpha.get().addUnit(z1);

            try {
                assertEquals(FightOrFlight.Tactic.FLEE, ClusterTactic.decide(),
                    "weak early zealots vs Zerg retreat instead of trading");
            } finally {
                Alpha.get().removeUnit(z1);
            }
        });
    }
}
