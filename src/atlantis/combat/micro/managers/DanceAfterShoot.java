package atlantis.combat.micro.managers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.DontInterruptStartedAttacks;
import atlantis.units.interrupt.UnitAttackWaitFrames;

public class DanceAfterShoot extends Manager {
    public DanceAfterShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (!unit.isMarine()) return false;

        boolean applies = unit.isRanged()
//            && !unit.isAttacking()
            && unit.cooldownRemaining() >= cooldownRemainingThreshold()
//            && unit.cooldownRemaining() <= unit.cooldownAbsolute() - UnitAttackWaitFrames.effectiveStopFrames(unit.type())
            && unit.lastActionMoreThanAgo(6, Actions.ATTACK_UNIT)
            && UnitAttackWaitFrames.waitedLongEnoughForAttackFrameToFinish(unit)
//            && UnitAttackWaitFrames.waitedLongEnoughForStartedAttack(unit)
            && !unit.isHoldingPosition()
            && !unit.isStartingAttack()
            && !unit.isAttackFrame()
            && !isExcludedUnit()
            && (!unit.hasTarget() || !unit.target().isABuilding())
//            && unit.enemiesNear().ranged().havingGreaterRanged().inRadius(6, unit).empty()
            && !shouldSkip();

//        System.err.println("APPLIES = " + (applies));

        return applies;
    }

    private boolean isExcludedUnit() {
        return unit.isWraith()
            || unit.isTank();
    }

    private int cooldownRemainingThreshold() {
        return 7;
    }

    @Override
    protected Manager handle() {
        if (update()) return usedManager(this);

        return null;
    }

    /**
     * For ranged unit, once shoot is fired, move slightly away or move towards the target when still have cooldown.
     */
    private boolean update() {
        AUnit target = unit.target();
        double dist = target.distTo(unit);
        int weaponRange = unit.enemyWeaponRangeAgainstThisUnit(target);

        String danceAway = "DanceAway-" + unit.cooldownRemaining();
        String danceTo = "DanceTo";

        // === Ranged vs ranged case ===============================

//        if (unit.isRanged() && target.isRanged()) {
//            boolean lesserRange = weaponRange < target.weaponRangeAgainst();
//            if (lesserRange && dist >= 3.8) {
//                unit.addLog(danceTo);
//                return unit.move(
//                    unit.translateTilesTowards(0.4, target), Actions.MOVE_DANCE, danceTo, false
//                );
//            } else {
//                unit.addLog(danceAway);
//                return unit.moveAwayFrom(target, 1, danceAway, Actions.MOVE_DANCE);
//            }
//        }

        // =========================================================

        // Step FORWARD
//        if (shouldDanceTowards(target, dist)) {
//            System.err.println("______ DANCE TO (" + dist + ")");
//            unit.addLog(danceTo);
//            return unit.move(
//                unit.translateTilesTowards(0.2, target), Actions.MOVE_DANCE_TO, danceTo, false
//            );
//        }

//        // Big step BACK
//        else if (shouldDanceBigStepBackwards(dist, weaponRange, target)) {
//            unit.addLog(danceAway);
//            return unit.moveAwayFrom(target, 0.9, Actions.MOVE_DANCE_AWAY, danceAway);
//        }

        // Small step BACK
        if (shouldDanceAway(dist, weaponRange, target)) {
//            System.err.println("^^^^^^ DANCE AWAY (" + dist + ")");
            unit.addLog(danceAway);
            return unit.moveAwayFrom(target.position(), 0.5, Actions.MOVE_DANCE_AWAY, danceAway);
        }

        return false;
    }

    private boolean shouldDanceAway(double dist, int weaponRange, AUnit target) {
        return unit.cooldownRemaining() >= Math.max(9, cooldownRemainingThreshold())
            && dist <= weaponRange + 0.2
            && unit.lastAttackFrameLessThanAgo(30)
//            && !UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)
            && !target.hasBiggerWeaponRangeThan(unit);
    }

//    private static boolean shouldDanceBigStepBackwards(double dist, int weaponRange, AUnit target) {
//        return dist <= weaponRange - 1.2 && !target.isCombatBuilding();
//    }

    // =========================================================

    private boolean shouldDanceTowards(AUnit target, double dist) {
        return target.isVisibleUnitOnMap()
            && target.effVisible()
//            && unit.distToMoreThan(target, 3.7)
//            && dist >= (unit.enemyWeaponRangeAgainstThisUnit(target))
            && !unit.isTargetInWeaponRangeAccordingToGame(target)
            && (
            (!target.isABuilding() && dist >= 1.6)
                || target.hasNoWeaponAtAll()
        );
    }

    private boolean shouldSkip() {
//        if (true) return true;
//        if (true) return false;

        if (unit.isMelee()) return true;
        if (unit.target() == null) return true;

//        if (unit.target() == null || !unit.target().isRealUnit()) {
//            return true;
//        }

//        if (unit.isDragoon() && unit.isHealthy()) {
//            return true;
//        }

        if (unit.isMissionSparta()) return true;

//        int lastAttackFrameAgo = unit.lastAttackFrameAgo();
//        int cooldownAbsolute = unit.cooldownAbsolute();
//
//        int minStop = UnitAttackWaitFrames.effectiveStopFrames(unit.type());
//
//        // @CheckB
////        if (lastAttackFrameAgo <= minStop || lastAttackFrameAgo >= cooldownAbsolute) return true;

        // In process of shooting
        // @CheckA
//        if ((unit.cooldownRemaining() + minStop) >= cooldownAbsolute) return true;

        if (!unit.isAttacking() && unit.noCooldown()) return true;

        return false;
    }

}
