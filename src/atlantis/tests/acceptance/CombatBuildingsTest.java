package atlantis.tests.acceptance;

import atlantis.combat.ACombatUnitManager;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import org.junit.Test;

public class CombatBuildingsTest extends AbstractTestFakingGame {

    @Test
    public void neverRunsIntoCombatBuildings() {
        createWorld(100, () -> {
            FakeUnit unit = ourFirst;
            boolean result = ACombatUnitManager.update(unit);

//            System.out.println(A.now() + " -       " + unit.tooltip()
//                    + "\n " + unit.lastCommand()
//                    + ",\n tx:" + unit.tx()
//                    + ",\n dist_to_sunken:" + distToNearestEnemy(unit)
//                    + (unit.target == null ? "" : ",\n dist_to_target:" + A.dist(unit, unit.target))
//                    + (unit.targetPosition == null ? "" : ",\n target_position:" + unit.targetPosition)
//            );
//            System.out.println("_______________________________________");
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
                fake(AUnitType.Terran_Marine, 10),
                fake(AUnitType.Terran_Marine, 11),
                fake(AUnitType.Terran_Marine, 11),
                fake(AUnitType.Terran_Marine, 11),
                fake(AUnitType.Terran_Marine, 11),
                fake(AUnitType.Terran_Marine, 11),
                fake(AUnitType.Terran_Marine, 11),
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
