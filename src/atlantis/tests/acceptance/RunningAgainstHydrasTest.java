package atlantis.tests.acceptance;

import atlantis.combat.ACombatUnitManager;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import org.junit.Test;

public class RunningAgainstHydrasTest extends AbstractTestFakingGame {

    @Test
    public void runsFromHydras() {
        createWorld(10, () -> {
            FakeUnit unit = ourFirst;
            boolean result = ACombatUnitManager.update(unit);

            FakeUnit enemy = nearestEnemy(unit);

//            System.out.println(A.now() + " -       " + unit.tooltip()
//                    + "\n " + unit.lastCommand()
//                    + ",\n tx:" + unit.tx() + ", x:" + unit.x()
//                    + ",\n combat_eval:" + unit.combatEvalRelative()
////                    + ",\n ENEMY_eval:" + enemy.combatEvalAbsolute()
////                    + ",\n combat_eval:" + unit.combatEvalAbsolute()
////                    + (i(nearestEnemy) ? "" : "a")
//                    + ",\n dist_to_enemy:" + distToNearestEnemy(unit)
////                    + (nearestEnemy == null ? "" : ",\n dist_to_target:" + A.dist(unit, unit.target))
//                    + (unit.target == null ? "" : ",\n dist_to_target:" + A.dist(unit, unit.target) + " (" + unit.target.hp() + "hp)")
//                    + (unit.targetPosition == null ? "" : ",\n target_position:" + unit.targetPosition)
//            );
//            System.out.println("_______________________________________");
        });
    }

//    private boolean i(Object nullForFalse) {
//        return nullForFalse != null;
//    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
                fake(AUnitType.Terran_Marine, 10)
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Medic, 11)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 16;
        return fakeEnemies(
                fake(AUnitType.Zerg_Hydralisk, enemyTy),
                fake(AUnitType.Zerg_Hydralisk, enemyTy + 1)
        );
    }

}
