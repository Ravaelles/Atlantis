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
            && unit.enemiesNear().combatUnits().canAttack(unit, safetyMargin()).notEmpty();
    }

    private double safetyMargin() {
        return 2.7 + unit.woundPercent() / 60.0;
    }

    @Override
    public Manager handle() {
        if (unit.distToBase() >= 15 && unit.moveToMain(Actions.MOVE_AVOID)) return usedManager(this);

        return null;
    }
}
