package atlantis.interrupt;

import atlantis.debug.APainter;
import atlantis.enemy.NearestEnemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.A;
import bwapi.Color;

public class DontInterruptStartedAttacks {

    //    private static boolean DEBUG = true;
    private static final boolean DEBUG = false;

    public static boolean shouldNotInterrupt(AUnit unit) {
        if (!unit.isAttackingOrMovingToAttack()) {
            return false;
        }

//        int lastAttackFrame = AGame.framesAgo(unit._lastAttackFrame);
//        int lastStartingAttack = AGame.framesAgo(unit._lastStartedAttack);
//        int cooldown = unit.cooldownRemaining();
//        int cooldownAbs = unit.cooldownAbsolute();
//        int friends = Select.ourCombatUnits().inRadius(2.5, unit).count();
//        boolean shouldAvoidAnyUnit = AAvoidUnits.shouldAvoidAnyUnit(unit);

        // === Target acquired recently, allow to attack ===========

        if (unit.recentlyAcquiredTargetToAttack()) {
//            if (unit.isFirstCombatUnit()) {
//                System.out.println(A.now() + "  " + unit.idWithHash() + " TARGET ACQUIRED");
//            }
//            APainter.paintCircle(unit, 14, Color.Teal);
//            APainter.paintCircle(unit, 12, Color.Teal);
//            APainter.paintCircle(unit, 10, Color.Teal);
            unit.setTooltip("Target(" + unit.lastTargetToAttackAcquiredAgo() + ")");
            return true;
        }

        // === Unit already started attack animation ===============

        if (
                UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)
//                        && NearestEnemy.rangedHasSmallerRangeThan(unit)
                        && !unit.isRunning()
        ) {
//            if (unit.isFirstCombatUnit()) {
//                System.out.println(A.now() + "  " + unit.idWithHash() + " STARTED ATTACK ANIMATION");
//            }
//            APainter.paintCircle(unit, 15, Color.Green);
//            APainter.paintCircle(unit, 13, Color.Green);
//            APainter.paintCircle(unit, 11, Color.Green);
            unit.setTooltip("Shoot(" + unit.lastFrameOfStartingAttackAgo() + ")");
            return true;
        }

        // =========================================================

        if (attackingCrucialUnit(unit)) {
            unit.setTooltip("Crucial");
            return true;
        }

//        if (unit.isMelee() || unit.type().isReaver()) {
//            return true;
//        }
//
//        if (attackingCrucialUnit(unit)) {
//            return true;
//        }
//
//        if ((shouldAvoidAnyUnit || unit.isUnderAttack(40)) && !unit.isMelee() && unit.woundPercent() > 65) {
//            return false;
//        }
//
//        if (unit.isAttackFrame() && unit.lastAttackOrderLessThanAgo(14)) {
//            unit.setTooltip("Attack frame(" + lastAttackFrame + "/" + lastStartingAttack + ")");
//            return true;
//        }
//
////        if (unit.lastAttackOrderMoreThanAgo(20) && shouldAvoidAnyUnit) {
////            return false;
////        }
//
//        if (cooldown <= 3 && unit.lastAttackOrderLessThanAgo(9)) {
//            return true;
//        }
//
//        if (unit.isStartingAttack() || unit.lastStartedAttackLessThanAgo(Math.min(9, cooldownAbs / 4))) {
//            unit.setTooltip("Starts attack(" + lastAttackFrame + "/" + lastStartingAttack + ")");
//            return true;
//        }
//
//        if (cooldown <= 3 && unit.lastAttackOrderLessThanAgo(Math.min(8, cooldownAbs / 3))) {
//            unit.setTooltip("Attack(" + lastAttackFrame + "/" + lastStartingAttack + " // " + cooldown + ")");
//            return true;
//        }

        return false;
    }

    private static boolean attackingCrucialUnit(AUnit unit) {
        AUnit target = unit.getTarget();
        if (target == null) {
            return false;
        }

//        if (!unit.isRanged() && unit.lastStartedAttackLessThanAgo(9)) {
//            return true;
//        }

        return target.isTank() || target.is(AUnitType.Protoss_Reaver);
    }

}
