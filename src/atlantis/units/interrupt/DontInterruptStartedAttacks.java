package atlantis.units.interrupt;

import atlantis.debug.painter.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Color;

public class DontInterruptStartedAttacks {

    //    private static boolean DEBUG = true;
    private static final boolean DEBUG = false;

    public static boolean shouldNotInterrupt(AUnit unit) {
//        if (true) return false;
//        if (unit.cooldownRemaining() == 0 || unit.lastActionMoreThanAgo(15)) {
        if (unit.lastActionMoreThanAgo(15)) {
            return false;
        }

        if (unit.isDragoon()) {
            if (unit.hpMoreThan(40)
                && unit.lastAttackOrderMoreThanAgo(40)
                && unit.lastAttackOrderLessThanAgo(40)) {
                return true;
            }

            if (unit.action().isAttacking()) {
                AUnit nearestEnemy = unit.nearestEnemy();
                if (nearestEnemy != null && nearestEnemy.distToMoreThan(unit, 2.8)) {
                    return true;
                }
            }
        }

        if (unit.hp() <= 20 || !unit.isAttackingOrMovingToAttack()) {
            return false;
        }

        if (unit.hp() <= 40 && unit.enemiesNear().ofType(AUnitType.Protoss_Zealot).inRadius(2.7, unit).atLeast(2)) {
            return false;
        }

        if (unit.isVulture() && unit.target() != null && unit.isUnitActionAttack()) {
//            if (unit.lastStartedAttackLessThanAgo(12 + unit.hpPercent() / 25)) {
            if (
                unit.target().hp() <= 18
                    || unit.lastStartedAttackLessThanAgo(10)
                    || unit.target().distToMoreThan(unit, 2.4)
//                    || (unit.lastStartedAttackLessThanAgo(13) && unit.isFacingItsTarget())
            ) {
                APainter.paintCircleFilled(unit, 10, Color.Brown);
                return true;
            }
        }

//        int lastAttackFrame = A.ago(unit._lastAttackFrame);
//        int lastStartingAttack = A.ago(unit._lastStartedAttack);
//        int cooldown = unit.cooldownRemaining();
//        int cooldownAbs = unit.cooldownAbsolute();
//        int friends = Select.ourCombatUnits().inRadius(2.5, unit).count();
//        boolean shouldAvoidAnyUnit = AAvoidUnits.shouldAvoidAnyUnit(unit);

        // === Target acquired recently, allow to attack ===========

        if (unit.recentlyAcquiredTargetToAttack()) {
            return true;
        }

        // === Unit already started attack animation ===============

        if (
            UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)
//                        && NearestEnemy.rangedHasSmallerRangeThan(unit)
        ) {
//            if (unit.isFirstCombatUnit()) {
//                System.out.println(A.now() + "  " + unit.idWithHash() + " STARTED ATTACK ANIMATION");
//            }
//            APainter.paintCircle(unit, 15, Color.Green);
//            APainter.paintCircle(unit, 13, Color.Green);
//            APainter.paintCircle(unit, 11, Color.Green);
//            unit.setTooltip("Shoot(" + unit.lastFrameOfStartingAttackAgo() + ")");
            return true;
        }

        return false;
    }

    private static boolean attackingCrucialUnit(AUnit unit) {
        AUnit target = unit.target();
        if (target == null) {
            return false;
        }

//        if (!unit.isRanged() && unit.lastStartedAttackLessThanAgo(9)) {
//            return true;
//        }

        return target.isTank() || target.is(AUnitType.Protoss_Reaver);
    }

}
