package tests.acceptance;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.AtlantisGameCommander;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertTrue;

public class DragoonsVsDragoonsTest extends NonAbstractTestFakingGame {
    @Test
    public void goonsUseReasonableManagers() {
        FakeUnit[] our = defineOurs();
        FakeUnit[] enemies = defineEnemies();

        useEngine(createEngine()); // Uncomment to use StarEngine

        createWorld(30 * 30, () -> {
//            A.println("Frame: " + A.now());
//            Select.our().print();
//            Select.enemy().print();

//            FakeUnit leader = (FakeUnit) Select.our().first();

//            System.err.println("leader = " + leader);
//            System.err.println("unit = " + unit);
//            if (true) return;

            (new AtlantisGameCommander()).invokeCommander();

            for (AUnit unit : Select.ourCombatUnits().list()) {
//                (new CombatUnitManager(unit)).invoke(this);

                boolean printUnit = false;
//                boolean printUnit = true;

                if (printUnit) {
                    A.errPrintln("_____");
                    A.errPrintln(A.now()
                            + " -       " + unit.tooltip()
                            + "\n   Type    : " + unit
                            + "\n   Manager : " + unit.manager()
//                            + "\n   Eval    : " + unit.combatEvalRelativeDigit()
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
                }
            }
//            A.errPrintln("_______________________________________");
        }, () -> our, () -> enemies);

        A.errPrintln("Test finioshed");
    }

    // =========================================================

    protected static FakeUnit[] defineOurs() {
        Alpha alpha = Alpha.get();

        int ty = 10;

        return fakeOurs(
            (FakeUnit) fake(AUnitType.Protoss_Dragoon, 10, ty).setSquad(alpha),
            (FakeUnit) fake(AUnitType.Protoss_Dragoon, 13, ty + 1).setSquad(alpha),
            (FakeUnit) fake(AUnitType.Protoss_Dragoon, 19, ty).setSquad(alpha)
        );
    }

    protected FakeUnit[] defineEnemies() {
        int enemyTy = 16;
        return fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk, 10, enemyTy),
            fake(AUnitType.Protoss_Dragoon, 13, enemyTy + 1),
            fake(AUnitType.Protoss_Dragoon, 19, enemyTy)
        );
    }
}
