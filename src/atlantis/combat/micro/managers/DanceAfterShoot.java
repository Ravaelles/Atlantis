package atlantis.combat.micro.managers;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DanceAfterShoot {

    /**
     * For ranged unit, once shoot is fired, move slightly away or move towards the target when still have cooldown.
     */
    public static boolean handle(AUnit unit) {
        if (unit.target() == null || !unit.target().isRealUnit()) {
            return false;
        }

        if (unit.isDragoon() && unit.isHealthy()) {
            return false;
        }

        if (!unit.isAttacking() || !unit.isRanged() || unit.cooldownRemaining() >= 6) {
            return false;
        }

        AUnit target = unit.target();
        if (target == null) {
            return false;
        }

        double dist = target.distTo(unit);

        if (dist <= 2.45) {
            return unit.moveAwayFrom(target, 0.4, "DanceAway", Actions.MOVE_DANCE);
        }
        else if (dist <= 3) {
            return unit.moveAwayFrom(target, 0.1, "DanceAway", Actions.MOVE_DANCE);
        }
        else if (dist >= 3.8) {
            return unit.move(
                unit.translateTilesTowards(0.2, target), Actions.MOVE_DANCE, "DanceTo", false
            );
        }

        return false;
    }

}
