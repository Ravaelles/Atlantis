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
        return Enemy.terran() && unit.isGroundUnit() && !unit.isSieged() && unit.combatEvalRelative() <= 2;
    }

    @Override
    protected Manager handle() {
        AUnit tankSieged = unit.enemiesNear().tanksSieged().inRadius(13.1, unit).nearestTo(unit);
        if (tankSieged == null) {
            return null;
        }

        unit.setTooltip("TANK!");
        unit.runningManager().runFrom(tankSieged, 0.5, Actions.MOVE_AVOID, false);
        return usedManager(this);
    }
}
