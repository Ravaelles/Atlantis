package tests.acceptance;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvoidCombatBuildingsTest extends AbstractTestWithWorld {
    private FakeUnit sunken;

    /**
     * With buildings there is a problem - JFAP returns "no danger" status
     * if the unit is 0.1 tiles outside of range of a Sunken Colony, but the
     * eval gets drastically worse once within range.
     */
    @Test
    public void neverRunsIntoCombatBuildings() {
        createWorld(50, () -> {
//            Select.our().print();
//            Select.enemy().print();

            FakeUnit unit = ourFirst;
            unit.setSquad(Alpha.get());
            (new CombatUnitManager(unit)).invokeFrom(this);

            double distToSunken = distToNearestEnemy(unit);
            boolean isSafe = distToSunken > 7.05;
            boolean alwaysShow = false;
//            boolean alwaysShow = true;

            if (!isSafe || alwaysShow) {
                System.err.println(A.now()
                    + " -       " + unit.tooltip()
                    + "\n   Manager : " + unit.manager()
                    + "\n   Managers: " + unit.managerLogs().toString()
                    + "\n   Command : " + unit.lastCommand()
                    + ",\n   tx     :" + unit.txWithPrecision()
                    + ",\n   dist_to_sunken:" + A.dist(distToSunken)
                    + (unit.target == null ? "" : ",\n   dist_to_target:" + A.dist(unit, unit.target))
                    + (unit.targetPosition == null ? "" : ",\n   target_position:" + unit.targetPosition)
                    + "\n   marine eval = " + unit.eval()
                    + "\n   sunken eval = " + sunken.eval()
                );
                System.err.println("_______________________________________");
            }

            assertTrue(isSafe);
        });
    }

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
        int enemyTy = 19;
        return fakeEnemies(
            sunken = fake(AUnitType.Zerg_Sunken_Colony, enemyTy),
            fake(AUnitType.Zerg_Sunken_Colony, enemyTy + 10)
        );
    }

}
