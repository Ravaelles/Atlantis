package atlantis.units.interrupt;

import atlantis.units.AUnit;

public class DontInterruptStartedAttacks {

    //    private static boolean DEBUG = true;
    private static final boolean DEBUG = false;

    public static boolean shouldNotInterrupt(AUnit unit) {
//        if (true) return false;

        if (unit.lastActionMoreThanAgo(15)) return false;

        AUnit target = unit.target();
        if (target != null && target.isCombatBuilding() && unit.friendsInRadiusCount(4) <= 7) return false;

        if (unit.isVulture()) {
            if (unit.hp() >= 22 && unit.isBraking() && unit.isAttackingOrMovingToAttack() && unit.lastAttackFrameAgo() >= 40) {
                return true;
            }
        }

//        if (unit.isDragoon()) {
//            if (unit.action().isAttacking()) {
//                AUnit nearestEnemy = unit.nearestEnemy();
//                if (nearestEnemy != null && nearestEnemy.distToMoreThan(unit, 2.9)) {
//                    System.err.println("Drag A");
//                    return true;
//                }
//            }
//        }

//        if (unit.hp() <= 20 || !unit.isAttackingOrMovingToAttack()) {
        if (!unit.isAttackingOrMovingToAttack()) return false;

//        if (unit.hp() <= 40 && unit.enemiesNear().ofType(AUnitType.Protoss_Zealot).inRadius(2.8, unit).atLeast(2)) {
//            return false;
//        }

        if (unit.isVulture() && target != null && unit.isUnitActionAttack()) {
//            if (unit.lastStartedAttackLessThanAgo(12 + unit.hpPercent() / 25)) {
            if (
                target.hp() <= 18
                    || unit.lastStartedAttackLessThanAgo(10)
                    || target.distToMoreThan(unit, 2.4)
//                    || (unit.lastStartedAttackLessThanAgo(13) && unit.isFacingItsTarget())
            ) {
//                APainter.paintCircleFilled(unit, 10, Color.Brown);
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

//        if (unit.recentlyAcquiredTargetToAttack()) {
//            return true;
//        }

        // === Unit already started attack animation ===============

        if (
            UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)
//                        && NearestEnemy.rangedHasSmallerRangeThan(unit)
        ) {
//            if (unit.isFirstCombatUnit()) {

//            }
//            APainter.paintCircle(unit, 15, Color.Green);
//            APainter.paintCircle(unit, 13, Color.Green);
//            APainter.paintCircle(unit, 11, Color.Green);
//            unit.setTooltip("Shoot(" + unit.lastFrameOfStartingAttackAgo() + ")");
            return true;
        }

        return false;
    }

//    private static boolean attackingCrucialUnit(AUnit unit) {
//        AUnit target = unit.target();
//        if (target == null) {
//            return false;
//        }
//
////        if (!unit.isRanged() && unit.lastStartedAttackLessThanAgo(9)) {
////            return true;
////        }
//
//        return target.isTank() || target.is(AUnitType.Protoss_Reaver);
//    }

}
