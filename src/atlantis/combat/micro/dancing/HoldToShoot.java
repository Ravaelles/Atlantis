package atlantis.combat.micro.dancing;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

/**
 * The idea is to stop the unit using Hold Position command and then attack the enemy,
 * gaining a couple of frames of advantage.
 */
public class HoldToShoot extends Manager {
    private int unitWeaponRange;
    private AUnit target;
    private double distToTarget;
    private boolean c1, c2, c3, c4;

    public HoldToShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isDragoon()) return false;
//        if (!unit.isDragoon() && !unit.isMarine()) return false;
//        if (!unit.isRanged()) return false;
//        if (unit.isMarine()) return false;
//        if (unit.isTank()) return false;
        if (unit.cooldown() >= 8) return false;

        target = unit.target();

        if (target == null) return false;
        if (unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)) return false;
        if (unit.lastActionLessThanAgo(5, Actions.MOVE_ATTACK)) return false;

        if (
            unit.enemiesNear().canBeAttackedBy(unit, 2).inRadius(1.2, unit).empty()
        ) return false;

        if (unit.lastActionMoreThanAgo(5, Actions.HOLD_POSITION)) return true;

        if (shouldSkip()) return false;

//        if (isTargetMovingAwayFromUsAndWeArePursuingIt()) {
//            System.err.println("isTargetMovingAwayFromUs");
//            return false;
//        }

        return true;
    }

    /**
     * For ranged unit, once shoot is fired, move slightly away or move towards the target when still have cooldown.
     */
    @Override
    protected Manager handle() {
//        System.err.println("@ " + A.now() + " ........ (cooldown: " + unit.cooldown() + ")");

        distToTarget = unit.distTo(target);
        unitWeaponRange = unit.weaponRangeAgainst(target);

        // =========================================================

//        System.out.println("distToEnemy: " + A.digit(distToEnemy) + " / range:" + shouldAttack());

        // HOLD
        if (shouldHold()) {
//            GameSpeed.changeSpeedTo(30);
//            System.err.println("************************** HOLD & SHOOT - " + distToTarget + " ");
            unit.holdPosition("HoldToShoot");
//            System.err.println("@ " + A.now() + " HOLD TO SHOOT - " + distToEnemy + " / " + unit);
            return usedManager(this);
//            return fallbackToUseManager(AttackNearbyEnemies.class, this);
        }

        // START ATTACK
//        System.err.println("ATTACK! dist: " + distToEnemy);

//        if (shouldAttack()) {
//            unit.attackUnit(target);
//            return usedManager(this);
////            return fallbackToUseManager(AttackNearbyEnemies.class, this);
//        }

//        else {
//            GameSpeed.changeSpeedTo(1);
//            GameSpeed.changeFrameSkipTo(0);
//        }
//        System.err.println(c1 + " / " + c2 + " / " + c3 + " / " + c4);
//        System.err.println(c1 + " / " + c2 + " / " + c3);

        return null;
    }

    private boolean shouldAttack() {
//        if (!unit.isHoldingPosition() && !enemyIsWithinRealRange()) return false;
//        if (!unit.isHoldingPosition()) return false;

//        return unit.canAttackTarget(target, true, true, false, extraMargin());
        return unit.cooldown() <= 4 && enemyIsWithinRealRange();
    }

    private double extraMargin() {
        if (unit.isMarine()) {
            if (target.isMoving() && (unit.equals(target.target()) || target.isFacing(unit))) return 1.2;
            return 0;
        }

        return (target.isMoving() ? (target.isFacing(unit) ? -1.4 : 1.0) : -1.0);
    }

    private boolean enemyIsWithinRealRange() {
        return unit.isTargetInWeaponRangeAccordingToGame(target);
    }

    // =========================================================

    private boolean shouldHold() {
//        System.out.println("M:" + unit.isMoving() + " / A:" + unit.isAttacking() + " / H:" + unit.isHoldingPosition());

        boolean enemyIsWithinRealRange = enemyIsWithinRealRange();
        if (enemyIsWithinRealRange) return false;

        if (unit.lastActionLessThanAgo(150, Actions.HOLD_POSITION)) {
            return true;
        }

        if (unit.isAttacking() && !unit.isMoving()) return false;
        if (!unit.isMoving() && unit.lastActionLessThanAgo(30, Actions.HOLD_POSITION)) return false;

//        if (!unit.isMoving() && unit.lastActionMoreThanAgo(20, Actions.MOVE_ATTACK)) return false;
//        if (!unit.isMoving() && unit.lastActionMoreThanAgo(20, Actions.MOVE_ATTACK)) return false;

//        if (unit.lastActionLessThanAgo(40, Actions.HOLD_POSITION)) return true;

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
            && (c3 = distToTarget <= minDistToHold());
//            && (c4 = (
//            unit.lastActionMoreThanAgo(51, Actions.HOLD_POSITION)
//                || unit.lastActionLessThanAgo(50, Actions.HOLD_POSITION
//            )));
//            && (c4 = !unit.isStartingAttack());
//            && (c4 = unit.lastStartedAttackMoreThanAgo(10));
    }

    private double minDistToHold() {
        double bonus = extraMargin();
        double minDist = unitWeaponRange + bonus;
//        System.err.println("   minDist = " + minDist);
        return minDist;
    }

    private boolean shouldSkip() {
        AUnit target = unit.target();
        if (target == null) return true;

        if (unit.isMelee()) return true;
//        if (unit.hpLessThan(19)) return true;
        if (unit.isTank() || unit.isVulture()) return true;
//        if (unit.cooldownRemaining() <= 6) return true;
        if (unit.isMissionSparta()) return true;

        if (target.isOur()) return true; // Allow to load into bunkers and transports
//        if (unit.isRetreating()) return true;
//        if (unit.combatEvalRelative() < 0.8) return true;

//        if (unit.isMissionDefendOrSparta() && unit.friendsNear().buildings().empty()) {
//            return true;
//        }

        if (unit.friendsInRadius(13).ofType(AUnitType.Terran_Bunker).notEmpty()) return false;
        if (unit.allUnitsNear().groundUnits().inRadius(1, unit).atLeast(3)) return false;

        return false;
    }

    private boolean isTargetMovingAwayFromUsAndWeArePursuingIt() {
        return (target != null && target.isMoving() && !target.isFacing(unit))
            &&
            (unit.isFacing(target));
    }
}
