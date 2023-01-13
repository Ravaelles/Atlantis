package atlantis.combat.micro.managers;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.UnitAttackWaitFrames;

public class DanceAfterShoot {

    /**
     * For ranged unit, once shoot is fired, move slightly away or move towards the target when still have cooldown.
     */
    public static boolean handle(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        AUnit target = unit.target();
        double dist = target.distTo(unit);
        int weaponRange = unit.enemyWeaponRange(target);

        String danceAway = "DanceAway-" + unit.cooldownRemaining();
        String danceTo = "DanceTo";

        // === Ranged vs ranged case ===============================

//        if (unit.isRanged() && target.isRanged()) {
//            boolean lesserRange = weaponRange < target.weaponRangeAgainst(unit);
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
        if (shouldDanceTo(unit, target, dist)) {
            unit.addLog(danceTo);
            return unit.move(
                unit.translateTilesTowards(0.2, target), Actions.MOVE_DANCE, danceTo, false
            );
        }
        // Big step BACK
        else if (dist <= weaponRange - 1.2) {
            unit.addLog(danceAway);
            return unit.moveAwayFrom(target, 0.9, danceAway, Actions.MOVE_DANCE);
        }
        // Small step BACK
        else if (dist <= weaponRange - 0.45) {
            unit.addLog(danceAway);
            return unit.moveAwayFrom(target, 0.35, danceAway, Actions.MOVE_DANCE);
        }

        return false;
    }

    // =========================================================

    private static boolean shouldDanceTo(AUnit unit, AUnit target, double dist) {
        return dist >= (unit.enemyWeaponRange(target))
            || (!target.isBuilding() && dist >= 1.6)
            || target.hasNoWeaponAtAll();
    }

    private static boolean shouldSkip(AUnit unit) {
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

        if (!unit.isAttacking()) {
            return true;
        }

//        System.out.println("unit.lastAttackFrameAgo = " + lastAttackFrameAgo + " // " + minStop);
        return false;
    }

}
