package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ScoutSafetyFarFromEnemy extends Manager {
    public ScoutSafetyFarFromEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().buildings().inRadius(9, unit).empty()
            && unit.enemiesNear().combatUnits().canAttack(unit, safetyMargin()).notEmpty()
            && unit.friendsNear().specialAction().inRadius(11, unit).empty();
    }

    private double safetyMargin() {
        return 3.1 + unit.woundPercent() / 40.0;
    }

    @Override
    public Manager handle() {
        if (
            (unit.isHealthy() || unit.enemiesNearInRadius(2.1) == 0)
                && unit.distToBase() >= 40
                && unit.moveToMain(Actions.MOVE_AVOID)
        ) return usedManager(this);

        return null;
    }
}
