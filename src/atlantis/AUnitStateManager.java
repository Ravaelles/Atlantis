package atlantis;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

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
            unit._lastX = unit.x();
            unit._lastY = unit.y();
        }

        if (unit.isAttackFrame()) {
            unit._lastAttackFrame = now;
//            APainter.paintCircleFilled(unit, 8, Color.Yellow);
//            if (unit.isFirstCombatUnit()) {
//                System.out.println("unit._lastAttackFrame = " + unit._lastAttackFrame);
//            }
        }

        if (unit.isAttackingOrMovingToAttack()) {
            unit._lastAttackOrder = now;
        }

        unit._lastCooldown = unit.cooldownRemaining();

        if (unit.isStartingAttack() && unit.cooldownRemaining() > unit._lastCooldown) {
//            APainter.paintCircleFilled(unit, 8, Color.Orange);
            unit._lastFrameOfStartingAttack = now;
//            if (unit.isFirstCombatUnit()) {
//                System.out.println("unit._lastFrameOfStartingAttack = " + unit._lastFrameOfStartingAttack);
//            }
        }

        unit._lastHitPoints.add(unit.hp());

        if (unit.isStartingAttack()) {
            unit._lastStartedAttack = now;
//            if (unit.isFirstCombatUnit()) {
//                System.out.println("unit._lastStartedAttack = " + unit._lastStartedAttack);
//            }
        }

        AUnit _oldLastTargetToAttack = unit._lastTargetToAttack;
        unit._lastTargetToAttack = unit.isAttackingOrMovingToAttack() ? unit.getTarget() : null;
        if (unit.getTarget() != null && !unit.getTarget().equals(_oldLastTargetToAttack)) {
            unit._lastTargetToAttackAcquired = now;
        }

        if (unit.isUnderAttack(3)) {
            unit._lastUnderAttack = now;

            AUnit enemy = Select.enemy().nearestTo(unit);
//            System.out.println("-------------- (" + A.dist(enemy, unit));
//            System.out.println(unit.getPosition());
//            System.out.println(enemy.getPosition());
        }
//        if (unit.getID() == Select.ourCombatUnits().first().getID()) {
//            System.out.println(AGame.getTimeFrames() + " ### "
//                    + unit._lastAttackOrder + " // " + unit._lastAttackFrame + " // " + unit._lastStartingAttack);
//        }
    }

}
