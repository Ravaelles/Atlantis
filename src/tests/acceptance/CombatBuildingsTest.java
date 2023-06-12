package tests.acceptance;

import atlantis.combat.ACombatUnitManager;
import atlantis.game.A;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertTrue;

public class CombatBuildingsTest extends AbstractTestFakingGame {

    @Test
    public void neverRunsIntoCombatBuildings() {
        createWorld(100, () -> {
            FakeUnit unit = ourFirst;
            boolean result = ACombatUnitManager.update(unit);

            double distToSunken = distToNearestEnemy(unit);
            boolean isSafe = distToSunken > 7.05;
            boolean alwaysShow = false;
//            boolean alwaysShow = true;

            if (!isSafe || alwaysShow) {
                System.out.println(A.now() + " -       " + unit.tooltip()
                        + "\n " + unit.lastCommand()
                        + ",\n tx:" + unit.tx()
                        + ",\n dist_to_sunken:" + A.dist(distToSunken)
                        + (unit.target == null ? "" : ",\n dist_to_target:" + A.dist(unit, unit.target))
                        + (unit.targetPosition == null ? "" : ",\n target_position:" + unit.targetPosition)
                );
                System.out.println("_______________________________________");
            }

            assertTrue(isSafe);
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
                fake(AUnitType.Terran_Marine, 10),
                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
                fake(AUnitType.Terran_Medic, 11)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 23;
        return fakeEnemies(
                fake(AUnitType.Zerg_Sunken_Colony, enemyTy),
                fake(AUnitType.Zerg_Sunken_Colony, enemyTy + 10)
        );
    }

}
