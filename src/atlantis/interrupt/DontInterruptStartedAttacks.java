package atlantis.interrupt;

import atlantis.AGame;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Color;

public class DontInterruptStartedAttacks {

    //    private static boolean DEBUG = true;
    private static final boolean DEBUG = false;

    public static boolean shouldNotBeInterruptedStartedAttack(AUnit unit) {
        if (!unit.isAttacking()) {
            return false;
        }

        int lastAttackFrame = AGame.framesAgo(unit._lastAttackFrame);
        int lastStartingAttack = AGame.framesAgo(unit._lastStartedAttack);
        int cooldown = unit.cooldownRemaining();
        int cooldownAbs = unit.getCooldownAbsolute();
//        int friends = Select.ourCombatUnits().inRadius(2.5, unit).count();
        boolean shouldAvoidAnyUnit = AAvoidUnits.shouldAvoidAnyUnit(unit);

        // =========================================================

        if (UnitAttackWaitFrames.isUnitInTheMiddleOfAttack(unit)) {
            APainter.paintCircle(unit, 15, Color.Cyan);
            return true;
        }

        if (attackingCrucialUnit(unit)) {
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
