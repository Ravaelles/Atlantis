package tests.unit;

import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.map.position.APosition;
import atlantis.production.dynamic.expansion.decision.ExpansionUnderPressure;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An expansion dies only to real pressure at its site - never to a scout,
 * and never to our army being small (GAME_366E9D6C: natural cancelled five
 * times in a row with the nearest marines ~110 tiles away).
 *
 * <p>One world per test: the stub-world rules in _AI/NOTES.md forbid sharing
 * measured runs.</p>
 */
public class ExpansionUnderPressureTest extends AbstractTestWithWorld {

    @Test
    public void noEnemiesMeansNoPressure() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 10)), fakeEnemies(), () -> {
            assertFalse(ExpansionUnderPressure.check(APosition.create(20, 10)));
            assertFalse(ExpansionUnderPressure.check(null));
        });
    }

    @Test
    public void combatUnitAtTheSiteIsPressure() {
        FakeUnit ling = fakeEnemy(AUnitType.Zerg_Zergling, 20);
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 10)), fakeEnemies(), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(ling);
            assertTrue(ExpansionUnderPressure.check(ling.position()));
        });
    }

    @Test
    public void farAwayArmyIsNotPressure() {
        FakeUnit ling = fakeEnemy(AUnitType.Zerg_Zergling, 20);
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 10)), fakeEnemies(), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(ling);
            assertFalse(ExpansionUnderPressure.check(APosition.create(100, 100)));
        });
    }

    @Test
    public void loneScoutIsNotPressure() {
        FakeUnit scout1 = fakeEnemy(AUnitType.Terran_SCV, 20);
        FakeUnit scout2 = fakeEnemy(AUnitType.Terran_SCV, 21);
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 10)), fakeEnemies(), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(scout1);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(scout2);
            assertFalse(
                ExpansionUnderPressure.check(scout1.position()),
                "two workers are a scout, not pressure"
            );
        });
    }

    @Test
    public void threeWorkersArePressure() {
        FakeUnit scout1 = fakeEnemy(AUnitType.Terran_SCV, 20);
        FakeUnit scout2 = fakeEnemy(AUnitType.Terran_SCV, 21);
        FakeUnit scout3 = fakeEnemy(AUnitType.Terran_SCV, 22);
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 10)), fakeEnemies(), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(scout1);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(scout2);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(scout3);
            assertTrue(
                ExpansionUnderPressure.check(scout1.position()),
                "three workers can deny a warping nexus"
            );
        });
    }

    @Override
    protected FakeUnit[] generateOur() {
        return fakeOurs();
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return fakeEnemies();
    }
}
