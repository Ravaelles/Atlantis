package atlantis.combat.micro.managers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.UnitAttackWaitFrames;

public class DanceAfterShoot extends Manager {
    public DanceAfterShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;
        return unit.isRanged() && !unit.isWraith();
    }

    @Override
    public Manager handle() {
        if (update()) return usedManager(this);

        return null;
    }

    /**
     * For ranged unit, once shoot is fired, move slightly away or move towards the target when still have cooldown.
     */
    private boolean update() {
        if (shouldSkip()) {
            return false;
        }

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
        if (shouldDanceTo(target, dist)) {
            unit.addLog(danceTo);
            return unit.move(
                unit.translateTilesTowards(0.2, target), Actions.MOVE_DANCE_TO, danceTo, false
            );
        }
        // Big step BACK
        else if (dist <= weaponRange - 1.2 && !target.isCombatBuilding()) {
            unit.addLog(danceAway);
            return unit.moveAwayFrom(target, 0.9, danceAway, Actions.MOVE_DANCE_AWAY);
        }
        // Small step BACK
        else if (dist <= weaponRange - 0.45 && !target.isCombatBuilding()) {
            unit.addLog(danceAway);
            return unit.moveAwayFrom(target, 0.35, danceAway, Actions.MOVE_DANCE_AWAY);
        }

        return false;
    }

    // =========================================================

    private boolean shouldDanceTo(AUnit target, double dist) {
        return target.isVisibleUnitOnMap()
            && target.effVisible()
            && unit.distToMoreThan(target, 3)
            && dist >= (unit.enemyWeaponRangeAgainstThisUnit(target))
            && (
            (!target.isABuilding() && dist >= 1.6)
                || target.hasNoWeaponAtAll()
        );
    }

    private boolean shouldSkip() {
//        if (true) return true;

        if (unit.isMelee()) {
            return true;
        }

        if (unit.target() == null) {
            return true;
        }

//        if (unit.target() == null || !unit.target().isRealUnit()) {
//            return true;
//        }

//        if (unit.isDragoon() && unit.isHealthy()) {
//            return true;
//        }

        // Can start shooting
        if (unit.cooldownRemaining() <= 3) {
            return true;
        }

        if (unit.isMissionSparta()) {
            return true;
        }

        int lastAttackFrameAgo = unit.lastAttackFrameAgo();
        int cooldownAbsolute = unit.cooldownAbsolute();

        int minStop = UnitAttackWaitFrames.effectiveStopFrames(unit.type());

        if (lastAttackFrameAgo <= minStop || lastAttackFrameAgo >= cooldownAbsolute) {
            return true;
        }
//        if (unit.lastAttackFrameMoreThanAgo(unit.cooldownAbsolute() - 3)) {
//            return true;
//        }

        // In process of shooting
        if ((unit.cooldownRemaining() + minStop) >= cooldownAbsolute) {
            return true;
        }

        if (!unit.isAttacking() && unit.noCooldown()) {
            return true;
        }

//        System.out.println("unit.lastAttackFrameAgo = " + lastAttackFrameAgo + " // " + minStop);
        return false;
    }

}
