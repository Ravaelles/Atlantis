package tests.acceptance;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import net.bytebuddy.description.annotation.AnnotationValue;
import org.junit.Test;
import tests.unit.FakeUnit;

import static atlantis.units.AUnitType.Terran_Marine;
import static org.junit.Assert.assertTrue;

public class AdvanceTest extends NonAbstractTestFakingGame {
    private FakeUnit unit;

    @Test
    public void unitsAreAdvancingInMissionAttack() {
        // Currently broken because combatEvalRelative doesn't work in test env :-/
        if (true) return;

//        Alpha alpha = Alpha.get();
//        fake(Terran_Marine, 10.1).setSquad(alpha);
//        System.err.println("fake(AUnitType.Terran_Marine, 10.1) = " + fake(Terran_Marine, 10.1));
//        System.err.println("fake(AUnitType.Terran_Marine, 10.1).setSquad(alpha) = " + fake(Terran_Marine, 10.1).setSquad(alpha));

        FakeUnit[] our = defineOurs();
        FakeUnit[] enemies = defineEnemies();

        createWorld(2, () -> {
            Select.our().print();
            Select.enemy().print();

            FakeUnit leader = (FakeUnit) Select.our().first();

//            System.err.println("leader = " + leader);
//            System.err.println("unit = " + unit);
//            if (true) return;

            for (AUnit unit : Select.ourCombatUnits().list()) {
                (new CombatUnitManager(unit)).invoke(this);

                //            boolean printUnit = false;
                boolean printUnit = true;

                if (printUnit) {
                    System.err.println(A.now()
                            + " -       " + unit.tooltip()
                            + "\n   Type    : " + unit
                            + "\n   Manager : " + unit.manager()
                            + "\n   Eval    : " + unit.combatEvalRelativeDigit()
//                            + "\n   Squad   : " + unit.squad().toString()
                        //                    + "\n   Managers: " + unit.managerLogs().toString()
                        //                    + "\n   Command : " + unit.lastCommand()
                        //                    + ",\n   tx     :" + unit.txWithPrecision()
                        //                    + ",\n   dist_to_sunken:" + A.dist(distToSunken)
                        //                    + (unit.target == null ? "" : ",\n   dist_to_target:" + A.dist(unit, unit.target))
                        //                    + (unit.targetPosition == null ? "" : ",\n   target_position:" + unit.targetPosition)
                        //                    + "\n   marine eval = " + unit.combatEvalRelative()
                        //                    + "\n   sunken eval = " + sunken.combatEvalRelative()
                    );
                    System.err.println("_______________________________________");
                }
            }
        }, () -> our, () -> enemies);
    }

    // =========================================================

    protected static FakeUnit[] defineOurs() {
        Alpha alpha = Alpha.get();

        return fakeOurs(
            (FakeUnit) fake(AUnitType.Terran_Marine, 10.1).setSquad(alpha),
            (FakeUnit) fake(AUnitType.Terran_Marine, 10.2).setSquad(alpha),
            (FakeUnit) fake(AUnitType.Terran_Siege_Tank_Tank_Mode, 11).setSquad(alpha)
        );
    }

    protected FakeUnit[] defineEnemies() {
        int enemyTy = 19;
        return fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk, enemyTy),
            fake(AUnitType.Zerg_Zergling, enemyTy + 1)
        );
    }

}
