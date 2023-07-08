package atlantis.combat.micro.managers;

import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;

public class StopAndShoot extends Manager {

    private int unitWeaponRange;
    private AUnit target;
    private double distToEnemy;
    private boolean c1, c2, c3, c4;

    public StopAndShoot(AUnit unit) {
        super(unit);
    }

    /**
     * For ranged unit, once shoot is fired, move slightly away or move towards the target when still have cooldown.
     */
    @Override
    public Manager handle() {
        if (shouldSkip()) {
            return null;
        }

        target = unit.target();
        distToEnemy = unit.distTo(target);
        unitWeaponRange = unit.weaponRangeAgainst(target);

        // =========================================================

        // HOLD
//        System.err.println(". " + A.now());
//        CameraManager.centerCameraOn();
        if (shouldStop()) {
//            GameSpeed.changeSpeedTo(30);
//            System.err.println("@ STOP & SHOOT - " + distToEnemy + " / " + unit);
            if (
                unit.isMoving()
                    && !unit.isHoldingPosition()
                    && unit.lastActionMoreThanAgo(2, Actions.HOLD_POSITION)
            ) {
//                System.err.println("@ HOLD - " + distToEnemy + " / " + unit);
                unit.holdPosition("HoldToShoot");
                return usedManager(this);
            }

            String tooltip = "Stop&Shoot";
            unit.addLog(tooltip);
//            return unit.attackUnit(target);
//            return AttackNearbyEnemies.handleAttackNearEnemyUnits();
            return fallbackToUseManager(AttackNearbyEnemies.class);
        }
//        else {
//            GameSpeed.changeSpeedTo(1);
//            GameSpeed.changeFrameSkipTo(0);
//        }
//        System.err.println(c1 + " / " + c2 + " / " + c3 + " / " + c4);
//        System.err.println(c1 + " / " + c2 + " / " + c3);

        return null;
    }

    // =========================================================

    private boolean shouldStop() {
        return (c1 = unit.isMoving())
            && (c2 = unit.cooldown() <= 2)
//            && unit.combatEvalRelative() > 0.8
//            && AvoidEnemies.unitsToAvoid(unit, true).isEmpty()
            && unit.avoidEnemiesManager().unitsToAvoid(true).isEmpty()
            && (c3 = distToEnemy <= minDistToStop())
            && (c4 = unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION));
//            && (c4 = !unit.isStartingAttack());
//            && (c4 = unit.lastStartedAttackMoreThanAgo(10));
    }

    private double minDistToStop() {
        double bonus = (target.isMoving() ? (target.isFacing(unit) ? -1.4 : 1) : -0.5);
        double minDist = unitWeaponRange + bonus;
//        System.err.println("   minDist = " + minDist);
        return minDist;
    }

    private boolean shouldSkip() {
        if (unit.isMelee()) {
            return true;
        }

        if (unit.hpLessThan(19)) {
            return true;
        }

        if (unit.isTank() || unit.isVulture()) {
            return true;
        }

//        if (!unit.isAttacking()) {
//            return true;
//        }

        if (unit.cooldownRemaining() >= 4) {
            return true;
        }

        if (unit.isMissionSparta()) {
            return true;
        }

        AUnit target = unit.target();
        if (target == null) {
            return true;
        }

        // Allow to load into bunkers and transports
        if (target.isOur()) {
            return true;
        }

        if (unit.isRetreating()) {
            return true;
        }

        if (unit.combatEvalRelative() < 0.8) {
            return true;
        }

//        if (unit.isMissionDefendOrSparta() && unit.friendsNear().buildings().empty()) {
//            return true;
//        }

        if (unit.friendsInRadius(13).ofType(AUnitType.Terran_Bunker).notEmpty()) {
            return false;
        }

        if (unit.allUnitsNear().groundUnits().inRadius(1, unit).atLeast(3)) {
            return false;
        }

//        if (!unit.hasBiggerWeaponRangeThan(target)) {
//            return true;
//        }

        return false;
    }

}
