package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AvoidCloseEnemiesAsTank extends Manager {
    public AvoidCloseEnemiesAsTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankUnsieged() && unit.hasCooldown() && enemiesTooClose().notEmpty();
    }

    public Manager handle() {
        if (separate()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean separate() {
        AUnit nearest = enemiesTooClose().nearestTo(unit);
        if (nearest != null) {
            unit.moveAwayFrom(nearest, 3, "Caution", Actions.MOVE_AVOID);
            return true;
        }

        return false;
    }

    private Selection enemiesTooClose() {
        return unit.enemiesNear().groundUnits().canAttack(unit, 2);
    }
}

