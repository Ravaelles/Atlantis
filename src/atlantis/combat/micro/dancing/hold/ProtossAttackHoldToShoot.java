package atlantis.combat.micro.dancing.hold;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossAttackHoldToShoot {
    public static boolean holdInsteadAttack(AUnit unit, AUnit target) {
//        if (true) return false;

        if (!We.protoss()) return false;
        if (!unit.isRanged()) return false;
        if (unit.attackState().starting()) return false;

        if (target == null) target = unit.lastTarget();
        if (target == null || target.hp() <= 0) return false;

        if (unit.isHoldingToShoot() && unit.speed() >= 0.9) {
//            System.err.println(A.now() + " ## ContinueHolding, speed:" + unit.speed());
            return true;
        }

        if (Enemy.terran() && unit.enemiesNear().tanks().notEmpty()) return false;
        if (unit.lastPositionChangedAgo() >= 60) return false;
        if (unit.isAttacking() && unit.lastActionLessThanAgo(20, Actions.HOLD_TO_SHOOT)) return false;
        if (unit.isTargetInWeaponRangeAccordingToGame(target)) return false;
        if (target.hasBiggerWeaponRangeThan(unit)) return false;
        if (!unit.isHoldingPosition() && unit.lastActionLessThanAgo(10, Actions.HOLD_TO_SHOOT)) return false;
        if (unit.distTo(target) <= unit.weaponRangeAgainst(target) + 0.07) return false;
        if (!unit.isOtherUnitFacingThisUnit(target)) return false;

        // ===

        if (unit.isHoldingToShoot()) return false;

        double dist, ourRange, theirRange;
        if (
            //                && !unit.isTargetInWeaponRangeAccordingToGame(target)
//            (dist = distToTargetWithFactors(unit, target)) >= minDist(unit, target)
            (dist = distToTargetWithFactors(unit, target)) <= ((ourRange = unit.weaponRangeAgainst(target)))
//                && unit.hp() >= 23
//                && dist < (2 + (ourRange = unit.weaponRangeAgainst(target)))
//                && ourRange >= (theirRange = target.weaponRangeAgainst(unit))
                && (
                            (target.isMoving() || unit.isHoldingToShoot())
//                    !unit.isTargetInWeaponRangeAccordingToGame(target)
//                        || (
//                            (target.isMoving() || unit.isHoldingToShoot())
//                                && unit.distTo(target) > unit.weaponRangeAgainst(target)
//                    )
            )
//                    && (new HoldToShoot(unit)).forceHandle() != null
        ) {
//            System.out.println(unit.distToDigit(target) + " dist to target !!!!!");
//            if (!unit.isHoldingPosition()) {
            unit.holdPosition(Actions.HOLD_TO_SHOOT, "HoldToShoot");
//            } else {
//                unit.stop("StopToShoot");
//            }

            return true;
        }

        return false;
    }

    private static double distToTargetWithFactors(AUnit unit, AUnit target) {
        double unitBonus = unit.speed() / 10;
        double targetMovingBonus = targetMovingDistBonus(target);
        double lowHealthBonus = unit.hp() <= 31 ? 0.1 : 0;
//        System.err.println("unit bonus = " + unitBonus + " / " + unit.speed());
//        System.err.println("target bonus = " + targetMovingBonus + " / " + target.speed());

        return unit.distTo(target)
            - lowHealthBonus
            - unitBonus
            - targetMovingBonus;
    }

    private static double targetMovingDistBonus(AUnit target) {
        if (target.isZerg()) return target.speed() / 7;
        if (target.isProtoss()) return target.speed() / 10;

        return target.speed() / 8;
    }

//    private static double minDist(AUnit unit, AUnit target) {
////        return unit.shieldWound() <= 30 ? 3.4 : 3.8;
//        return OurDragoonRange.range() + 0.05;
//    }
}
