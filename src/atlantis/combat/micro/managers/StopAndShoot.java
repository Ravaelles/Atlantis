package atlantis.combat.micro.managers;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

public class StopAndShoot extends Manager {
    private int unitWeaponRange;
    private AUnit target;
    private double distToEnemy;
    private boolean c1, c2, c3, c4;

    public StopAndShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isRanged()) return false;

        return true;
    }

    /**
     * For ranged unit, once shoot is fired, move slightly away or move towards the target when still have cooldown.
     */
    @Override
    protected Manager handle() {
        if (shouldSkip()) return null;

        target = unit.target();
        distToEnemy = unit.distTo(target);
        unitWeaponRange = unit.weaponRangeAgainst(target);

        // =========================================================

//        System.out.println("distToEnemy: " + A.digit(distToEnemy) + " / range:" + shouldAttack());

        // HOLD
        if (shouldStop()) {
//            GameSpeed.changeSpeedTo(30);
//            System.err.println("@ STOP & SHOOT - " + distToEnemy + " / " + unit);
            unit.holdPosition("HoldToShoot");
//            System.err.println("@ " + A.now() + " HOLD TO SHOOT - " + distToEnemy + " / " + unit);
            return usedManager(this);
//            return fallbackToUseManager(AttackNearbyEnemies.class, this);
        }

        // START ATTACK
//        System.err.println("ATTACK! dist: " + distToEnemy);
        if (shouldAttack()) {
            unit.attackUnit(target);
//            System.err.println("@ " + A.now() + " ### ATTACK - " + distToEnemy);
            return usedManager(this);
        }


//        else {
//            GameSpeed.changeSpeedTo(1);
//            GameSpeed.changeFrameSkipTo(0);
//        }
//        System.err.println(c1 + " / " + c2 + " / " + c3 + " / " + c4);
//        System.err.println(c1 + " / " + c2 + " / " + c3);

        return null;
    }

    private boolean shouldAttack() {
        return unit.canAttackTarget(target, true, true, false, extraMargin());
//        return enemyIsWithinRange();
    }

    private double extraMargin() {
        return (target.isMoving() ? (target.isFacing(unit) ? -1.9 : 1.0) : -1.0);
    }

    private boolean enemyIsWithinRange() {
        return unit.isTargetInWeaponRangeAccordingToGame(target);
    }

    // =========================================================

    private boolean shouldStop() {
//        System.out.println("M:" + unit.isMoving() + " / A:" + unit.isAttacking() + " / H:" + unit.isHoldingPosition());

        if (unit.isAttacking()) return false;
        if (enemyIsWithinRange()) return false;

        if (unit.lastActionLessThanAgo(40, Actions.HOLD_POSITION)) return true;

//        if (unit.isAttacking() && unit.lastActionLessThanAgo(4, Actions.ATTACK_UNIT)) return false;

//        if (!unit.isHoldingPosition()) {
//            if (unit.lastActionLessThanAgo(30, Actions.HOLD_POSITION)) return true;
//        }

//        !unit.isHoldingPosition()
////                unit.isMoving()
////                    && !unit.isHoldingPosition()
////                    && unit.lastActionMoreThanAgo(2, Actions.HOLD_POSITION)
//            && !unit.isStartingAttack()
//            && !unit.isAttackFrame()

        return (c1 = unit.isMoving())
            && (c2 = unit.cooldown() <= 4)
//            && unit.combatEvalRelative() > 0.8
//            && AvoidEnemies.unitsToAvoid(unit, true).isEmpty()
            && unit.avoidEnemiesManager().enemiesDangerouslyClose().isEmpty()
            && (c3 = distToEnemy <= minDistToStop());
//            && (c4 = (
//            unit.lastActionMoreThanAgo(51, Actions.HOLD_POSITION)
//                || unit.lastActionLessThanAgo(50, Actions.HOLD_POSITION
//            )));
//            && (c4 = !unit.isStartingAttack());
//            && (c4 = unit.lastStartedAttackMoreThanAgo(10));
    }

    private double minDistToStop() {
        double bonus = extraMargin();
        double minDist = unitWeaponRange + bonus;
//        System.err.println("   minDist = " + minDist);
        return minDist;
    }

    private boolean shouldSkip() {
        if (unit.isMelee()) return true;
//        if (unit.hpLessThan(19)) return true;
        if (unit.isTank() || unit.isVulture()) return true;
        if (unit.cooldownRemaining() >= 6) return true;
        if (unit.isMissionSparta()) return true;

        AUnit target = unit.target();
        if (target == null) return true;

        if (target.isOur()) return true; // Allow to load into bunkers and transports
        if (unit.isRetreating()) return true;
//        if (unit.combatEvalRelative() < 0.8) return true;

//        if (unit.isMissionDefendOrSparta() && unit.friendsNear().buildings().empty()) {
//            return true;
//        }

        if (unit.friendsInRadius(13).ofType(AUnitType.Terran_Bunker).notEmpty()) return false;
        if (unit.allUnitsNear().groundUnits().inRadius(1, unit).atLeast(3)) return false;

//        if (!unit.hasBiggerWeaponRangeThan(target)) {
//            return true;
//        }

        return false;
    }

}
