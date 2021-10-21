package atlantis;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Color;

public class AUnitStateManager {

    private static int now;
    private static boolean updatePosition;

    public static void update() {
        now = AGame.getTimeFrames();
        updatePosition = AGame.everyNthGameFrame(AUnit.UPDATE_UNIT_POSITION_EVERY_FRAMES);

        for (AUnit unit : Select.our().listUnits()) {
            updateUnitInfo(unit);
        }
    }

    private static void updateUnitInfo(AUnit unit) {
        if (updatePosition) {
            unit.lastX = unit.getX();
            unit.lastY = unit.getY();
        }

        if (unit.isAttacking()) {
            unit._lastAttackOrder = now;
        }
        if (unit.isAttackFrame()) {
            unit._lastAttackFrame = now;
        }
        if (unit.isStartingAttack()) {
            unit._lastStartingAttack = now;
        }
        if (unit.isUnderAttack()) {
            APainter.paintCircleFilled(unit, 12, Color.Blue);
//            if (!unit.lastUnderAttackLessThanAgo(15)) {
//                AGameSpeed.pauseGame();
            unit._lastUnderAttack = now;
//            }
        }

        unit.lastHitPoints.add(unit.hp());

//        if (unit.getID() == Select.ourCombatUnits().first().getID()) {
//            System.out.println(AGame.getTimeFrames() + " ### "
//                    + unit._lastAttackOrder + " // " + unit._lastAttackFrame + " // " + unit._lastStartingAttack);
//        }
    }

}
