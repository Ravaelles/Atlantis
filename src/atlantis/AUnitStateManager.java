package atlantis;

import atlantis.units.AUnit;
import atlantis.units.Select;

public class AUnitStateManager {

    private static int now;

    public static void update() {
        now = AGame.getTimeFrames();

        for (AUnit unit : Select.our().listUnits()) {
            updateUnitInfo(unit);
        }
    }

    private static void updateUnitInfo(AUnit unit) {
        if (unit.isAttacking()) {
            unit._lastAttackOrder = now;
        }
        if (unit.isAttackFrame()) {
            unit._lastAttackFrame = now;
        }
        if (unit.isRunning()) {
            unit._lastRunning = now;
        }
        if (unit.isStartingAttack()) {
            unit._lastStartingAttack = now;
        }
        if (unit.isUnderAttack()) {
            unit._lastUnderAttack = now;
        }

//        if (unit.getID() == Select.ourCombatUnits().first().getID()) {
//            System.out.println(AGame.getTimeFrames() + " ### "
//                    + unit._lastAttackOrder + " // " + unit._lastAttackFrame + " // " + unit._lastStartingAttack);
//        }
    }

}
