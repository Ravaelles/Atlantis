package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.Enemy;

public class AvoidTanksSieged extends Manager {
    public AvoidTanksSieged(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMissionAttack()) return false;

        return Enemy.terran() && unit.isGroundUnit() && !unit.isSieged() && unit.combatEvalRelative() <= 2;
    }

    @Override
    protected Manager handle() {
        AUnit tankSieged = unit.enemiesNear().tanksSieged().inRadius(13.5, unit).nearestTo(unit);
        if (tankSieged == null) {
            return null;
        }

        if (!unit.isAttacking() && unit.distTo(tankSieged) <= 1.9) {
            unit.holdPosition("TANK!");
            return usedManager(this);
        }

        unit.setTooltip("TANK!");
        unit.runningManager().runFrom(tankSieged, 2, Actions.MOVE_AVOID, false);
        return usedManager(this);
    }
}
