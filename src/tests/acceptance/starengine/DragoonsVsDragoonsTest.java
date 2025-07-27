package tests.acceptance.starengine;

import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.AtlantisGameCommander;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DragoonsVsDragoonsTest extends WorldStubForTests {
    @Test
    public void goonsUseReasonableManagers() {
        FakeUnit[] our = defineOurs();
        FakeUnit[] enemies = defineEnemies();

        useStarEngine(); // Uncomment to use StarEngine

        int runForSeconds = !isUsingEngine() ? 1 : 10;
        createWorld(30 * runForSeconds, () -> {
            if (isUsingEngine() && A.now % 30 == 0) A.println("Frame: " + A.now);
//            Select.our().print();
//            Select.enemy().print();

//            FakeUnit leader = (FakeUnit) Select.our().first();

//            System.err.println("leader = " + leader);
//            System.err.println("unit = " + unit);
//            if (true) return;

            (new AtlantisGameCommander()).invokeCommander();

            for (AUnit unit : Select.ourCombatUnits().list()) {
//                (new CombatUnitManager(unit)).invokeFrom(null);
                (new AttackNearbyEnemies(unit)).forceHandle();

//                boolean printUnit = false;
//                boolean printUnit = true;
                boolean printUnit = unit == Select.ourCombatUnits().second();

                if (printUnit) {
                    System.out.println(A.now + " - " + unit.action() + " / " + unit.manager());
                }

//                if (printUnit) {
//                    A.errPrintln("_____");
//                    A.errPrintln(A.now()
//                            + " -       " + unit.tooltip()
//                            + "\n   Type    : " + unit
//                            + "\n   Manager : " + unit.manager()
////                            + "\n   Eval    : " + unit.combatEvalRelativeDigit()
////                            + "\n   Squad   : " + unit.squad().toString()
//                        //                    + "\n   Managers: " + unit.managerLogs().toString()
//                        //                    + "\n   Command : " + unit.lastCommand()
//                        //                    + ",\n   tx     :" + unit.txWithPrecision()
//                        //                    + ",\n   dist_to_sunken:" + A.dist(distToSunken)
//                        //                    + (unit.target == null ? "" : ",\n   dist_to_target:" + A.dist(unit, unit.target))
//                        //                    + (unit.targetPosition == null ? "" : ",\n   target_position:" + unit.targetPosition)
//                        //                    + "\n   marine eval = " + unit.combatEvalRelative()
//                        //                    + "\n   sunken eval = " + sunken.combatEvalRelative()
//                    );
//                }
            }
        }, () -> our, () -> enemies);
    }

    // =========================================================

    protected static FakeUnit[] defineOurs() {
//        Alpha alpha = Alpha.get();
        Alpha alpha = null;

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
            fake(AUnitType.Protoss_Dragoon, 10, enemyTy),
            fake(AUnitType.Protoss_Dragoon, 13, enemyTy + 1),
            fake(AUnitType.Protoss_Dragoon, 16, enemyTy + 4),
            fake(AUnitType.Protoss_Dragoon, 17, enemyTy + 1),
            fake(AUnitType.Protoss_Dragoon, 19, enemyTy)
        );
    }
}
