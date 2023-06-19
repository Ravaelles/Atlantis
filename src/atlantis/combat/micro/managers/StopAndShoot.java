package atlantis.combat.micro.managers;

import atlantis.game.A;
import atlantis.game.CameraManager;
import atlantis.game.GameSpeed;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class StopAndShoot {

    private static AUnit unit;
    private static int unitWeaponRange;
    private static AUnit target;
    private static double distToEnemy;
    private static boolean c1, c2, c3, c4;

    /**
     * For ranged unit, once shoot is fired, move slightly away or move towards the target when still have cooldown.
     */
    public static boolean update(AUnit unit) {
        StopAndShoot.unit = unit;
        if (shouldSkip()) {
            return false;
        }

        target = unit.target();
        distToEnemy = unit.distTo(target);
        unitWeaponRange = unit.weaponRangeAgainst(target);

        // =========================================================

        // HOLD
//        System.err.println(". " + A.now());
//        CameraManager.centerCameraOn(unit);
        if (shouldStop()) {
//            GameSpeed.changeSpeedTo(30);
//            System.err.println("@ STOP - " + distToEnemy);
            String tooltip = "Stop&Shoot";
            unit.addLog(tooltip);
            return unit.attackUnit(target);
        }
//        else {
//            GameSpeed.changeSpeedTo(1);
//            GameSpeed.changeFrameSkipTo(0);
//        }
//        System.err.println(c1 + " / " + c2 + " / " + c3 + " / " + c4);
//        System.err.println(c1 + " / " + c2 + " / " + c3);

        return false;
    }

    // =========================================================

    private static boolean shouldStop() {
        return (c1 = unit.isMoving())
            && (c2 = unit.cooldown() <= 2)
            && (c3 = distToEnemy <= minDistToStop())
            && (c4 = unit.lastActionMoreThanAgo(15, Actions.HOLD_POSITION));
//            && (c4 = !unit.isStartingAttack());
//            && (c4 = unit.lastStartedAttackMoreThanAgo(10));
    }

    private static double minDistToStop() {
        double bonus = (target.isMoving() ? (target.isFacing(unit) ? -1.4 : 1) : -0.5);
        double minDist = unitWeaponRange + bonus;
//        System.err.println("   minDist = " + minDist);
        return minDist;
    }

    private static boolean shouldSkip() {
//        if (unit.isMelee()) {
//            return true;
//        }
//
//        if (!unit.isAttacking()) {
//            return true;
//        }
//
//        // Can start shooting
//        if (unit.cooldownRemaining() <= 3) {
//            return true;
//        }
//
//        if (unit.isMissionSparta()) {
//            return true;
//        }

        AUnit target = unit.target();
        if (target == null) {
            return true;
        }

//        if (!unit.hasBiggerWeaponRangeThan(target)) {
//            return true;
//        }

        return false;
    }

}
