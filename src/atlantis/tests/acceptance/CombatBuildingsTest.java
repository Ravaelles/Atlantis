package atlantis.tests.acceptance;

import atlantis.combat.ACombatUnitManager;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.A;
import org.junit.Test;

public class CombatBuildingsTest extends AbstractTestFakingGame {

    @Test
    public void neverRunsIntoCombatBuildings() {
        createWorld(100, () -> {
            FakeUnit unit = ourFirst;
            boolean result = ACombatUnitManager.update(unit);

            System.out.println(A.now() + " -       \n"
                    + unit.lastCommand() + ", " + unit.tooltip()
                    + ",\n ty:" + unit.ty()
                    + ",\n dist_to_sunken:" + distToNearestCombatBuild(unit)
                    + ",\n dist_to_target:" + A.dist(unit, unit.target)
            );
            System.out.println("_______________________________________");
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeUnits(
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
        return fakeUnits(
                fake(AUnitType.Zerg_Sunken_Colony, enemyTy),
                fake(AUnitType.Zerg_Sunken_Colony, enemyTy + 10)
        );
    }

    private String distToNearestCombatBuild(FakeUnit our) {
        return A.dist(our, Select.enemyCombatUnits().nearestTo(our));
    }

}
