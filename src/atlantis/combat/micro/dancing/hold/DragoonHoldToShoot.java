package atlantis.combat.micro.dancing.hold;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

/**
 * The idea is to stop the unit using Hold Position command and then attack the enemy,
 * gaining a couple of frames of advantage.
 */
public class DragoonHoldToShoot extends Manager {
    private int unitWeaponRange;
    private AUnit target;
    private double distToTarget;
    private boolean c1, c2, c3, c4;

    public DragoonHoldToShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isDragoon()) return false;
        if (unit.cooldown() >= 8) return false;
        if (!unit.isMoving()) return false;
//        if (unit.lastPositionChangedAgo() >= 2) return false;
        if (unit.enemiesNear().countInRadius(8, unit) == 0) return false;
        if (unit.meleeEnemiesNearCount(3.3) > 0) return false;
        if (!unit.isMoving() && !unit.isHoldingPosition()) return false;
        if (unit.enemiesThatCanAttackMe(1.85).count() >= 2) return false;

        target = unit.target();
        if (target == null || !unit.hasValidTarget()) return false;
//        if (unit.isTargetInWeaponRangeAccordingToGame()) return false;

//        System.err.println("unit.cooldown() = " + unit.cooldown());

//        if (!target.isMoving() || unit.isOtherUnitShowingBackToUs(target)) return false;
        if (unit.isOtherUnitShowingBackToUs(target)) return false;
//        if (target.isMoving() && unit.isOtherUnitShowingBackToUs(target)) return false;
//        if (!unit.isFacing(target)) return false;

        return true;
    }

    private double minDistToHold() {
//        System.err.println("unit.speed() = " + unit.speed());
        double minDist = unitWeaponRange
            + (!unit.hasBiggerWeaponRangeThan(target) ? -1.2 : 0)
            + enemyMovementModifiers()
            + ourMovementModifiers();
//        System.err.println("   " + unit.idWithHash() + " minDist = " + minDist + " (" + distToTarget + ")");

        return minDist;
    }

    @Override
    protected Manager handle() {
//        System.err.println("@ " + A.now() + " HOLD?");
//        if (target == null) {
//            target = unit.lastTarget();
//        }
        if (target == null) {
            return null;
        }

//        System.err.println("@ " + A.now() + " ........ (cooldown: " + unit.cooldown() + ")");

        distToTarget = unit.distTo(target);
        unitWeaponRange = unit.weaponRangeAgainst(target);

        // =========================================================

//        System.out.println("distToEnemy: " + A.digit(distToEnemy) + " / range:" + shouldAttack());


        // ATTACK ENEMY
//        if (enemyIsWithinRealRange()) {
//            if (attackedEnemy()) return usedManager(this, "Hold&Attack");
//        }

        // HOLD
        if (shouldHold()) {
            if (!unit.isHoldingPosition()) {
//                System.err.println("@ " + A.now() + " HOLD!!!!!! DIST = " + unit.distToTargetDigit());

                unit.holdPosition(Actions.HOLD_TO_SHOOT, "HoldToShoot");
            }
//            else {
//                System.err.println("@ " + A.now() + " non-hold! ");
//            }
//            System.err.println("@ " + A.now() + " HOLD TO SHOOT - " + distToEnemy + " / " + unit);
            return usedManager(this);
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

    private boolean attackedEnemy() {
        System.out.println(A.now + " Within real range - ATTACK " + target);
        if (target == null) return false;

        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(target)) return true;

        System.out.println("Failed to attack " + target);
        return false;
    }

    // =========================================================

    private boolean shouldHold() {
//        System.out.println("M:" + unit.isMoving() + " / A:" + unit.isAttacking() + " / H:" + unit.isHoldingPosition());

//        if (unit.isAttacking()) {
//            if (unit.lastActionLessThanAgo(1, Actions.HOLD_POSITION)) {
//                System.out.println(A.now + " Still hold / target = " + unit.target());
//                return true;
//            }
//        }

//        if (unit.isAttacking() && !unit.isMoving()) return false;
//        if (!unit.isMoving() && unit.lastActionLessThanAgo(30, Actions.HOLD_POSITION)) return false;

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

        return //(c1 = unit.isMoving())
            (c2 = unit.cooldown() <= 15)
//            && unit.combatEvalRelative() > 0.8
//            && AvoidEnemies.unitsToAvoid(unit, true).isEmpty()
//            && unit.avoidEnemiesManager().enemiesDangerouslyClose().isEmpty()
                && (c3 = distToTarget <= minDistToHold());
//            && (c4 = (
//            unit.lastActionMoreThanAgo(51, Actions.HOLD_POSITION)
//                || unit.lastActionLessThanAgo(50, Actions.HOLD_POSITION
//            )));
//            && (c4 = !unit.isStartingAttack());
//            && (c4 = unit.lastStartedAttackMoreThanAgo(10));
    }

    private double ourMovementModifiers() {
//        if (Enemy.zerg()) return (unit.isMoving() ? unit.maxSpeed() / 4.8 : 0);

//        if (Enemy.protoss()) {
//            return (unit.isMoving() ? unit.maxSpeed() / 4.0 : 0);
//        }

//        return (unit.isMoving() ? unit.maxSpeed() / 4.8 : 0);
        return (unit.isMoving() ? unit.speed() / 5.5 : 0);
    }

    private double enemyMovementModifiers() {
        if (Enemy.zerg()) return (target.isMoving() ? unit.maxSpeed() / 4.0 : 0);

//        return (target.isMoving() ? unit.maxSpeed() / 4.8 : 0);
        return (target.isMoving() ? unit.maxSpeed() / 5.5 : 0);
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
