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
            unit._lastX = unit.getX();
            unit._lastY = unit.getY();
        }

        if (unit.isAttacking()) {
            unit._lastAttackOrder = now;
        }
        if (unit.isAttackFrame()) {
            unit._lastAttackFrame = now;
            if (unit.isFirstCombatUnit()) {
                System.out.println("unit._lastAttackFrame = " + unit._lastAttackFrame);
            }
        }
        if (unit.isStartingAttack()) {
            unit._lastStartedAttack = now;
            if (unit.isFirstCombatUnit()) {
                System.out.println("unit._lastStartedAttack = " + unit._lastStartedAttack);
            }
        }
        if (unit.isStartingAttack() && unit.cooldownRemaining() > unit._lastCooldown) {
            unit._lastFrameOfStartingAttack = now;
            if (unit.isFirstCombatUnit()) {
                System.out.println("unit._lastFrameOfStartingAttack = " + unit._lastFrameOfStartingAttack);
            }
        }
        if (unit.isUnderAttack()) {
            APainter.paintCircleFilled(unit, 12, Color.Blue);
//            if (!unit.lastUnderAttackLessThanAgo(15)) {
//                AGameSpeed.pauseGame();
            unit._lastUnderAttack = now;
//            }
        }

        unit._lastHitPoints.add(unit.hp());
        unit._lastCooldown = unit.cooldownRemaining();

//        if (unit.getID() == Select.ourCombatUnits().first().getID()) {
//            System.out.println(AGame.getTimeFrames() + " ### "
//                    + unit._lastAttackOrder + " // " + unit._lastAttackFrame + " // " + unit._lastStartingAttack);
//        }
    }

}
