package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ScoutAvoidCombatBuildings extends Manager {
    private AUnit building;

    public ScoutAvoidCombatBuildings(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        building = unit.enemiesNear().combatBuildingsAntiLand().inRadius(10, unit).nearestTo(unit);
        return building != null;
    }

    public Manager handle() {
        if (unit.moveToMain(Actions.MOVE_AVOID)) {
            return usedManager(this, "ScoutAvoidsBuilding");
        }

        return null;
    }
}
