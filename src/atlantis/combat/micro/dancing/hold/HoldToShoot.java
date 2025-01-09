package atlantis.combat.micro.dancing.hold;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

/**
 * The idea is to stop the unit using Hold Position command and then attack the enemy,
 * gaining a couple of frames of advantage.
 */
public class HoldToShoot extends Manager {
    public HoldToShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isRanged()) return false;
        if (unit.cooldown() >= 15) return false;
//        if (unit.hasTarget() && unit.target().isRanged()) return false;
        if (!unit.isHoldingPosition() && !unit.hasValidTarget()) return false;
        if (unit.enemiesNear().inShootRangeOf(unit).count() > 0) return false;
        if (Enemy.zerg() && unit.friendsInRadiusCount(3) >= 3) return false;

        return true;
    }

    public static boolean isHoldingToShoot(AUnit unit) {
        if (unit.isHoldingPosition() && unit.lastActionLessThanAgo(5, Actions.HOLD_TO_SHOOT)) {
            return true;
        }

        return false;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DragoonHoldToShoot.class,
            ContinueHoldToShoot.class,
        };
    }
}
