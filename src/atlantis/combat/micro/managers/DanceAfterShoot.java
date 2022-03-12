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
//        double range = unit.weaponRangeAgainst(target);

        String danceAway = "DanceAway-" + unit.cooldownRemaining();
        String danceTo = "DanceTo";

        // Big step BACK
        if (dist <= 2.8 && !target.isBuilding() && dist >= 1.6) {
            unit.addLog(danceAway);
            return unit.moveAwayFrom(target, 0.3, danceAway, Actions.MOVE_DANCE);
        }
        // Small step BACK
        else if (dist <= 3.3) {
            unit.addLog(danceAway);
            return unit.moveAwayFrom(target, 0.1, danceAway, Actions.MOVE_DANCE);
        }
        // Step FORWARD
        else if (dist >= 3.8) {
            unit.addLog(danceTo);
            return unit.move(
                unit.translateTilesTowards(0.2, target), Actions.MOVE_DANCE, danceTo, false
            );
        }

        return false;
    }

    // =========================================================

    private static boolean shouldSkip(AUnit unit) {
        if (true) return true;

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

        System.out.println("unit.lastAttackFrameAgo = " + lastAttackFrameAgo + " // " + minStop);
        return false;
    }

}
