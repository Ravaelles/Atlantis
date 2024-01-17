package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

/**
 * We don't know any enemy building, unit nearest starting location.
 */
public class TryFindingEnemy extends Manager {
    public TryFindingEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit != null
            && !EnemyInfo.hasDiscoveredAnyBuilding()
            && A.notUms()
            && !unit.isSpecialMission()
            && Select.main() != null;
    }

    @Override
    public Manager handle() {
        unit.setTooltipTactical("Find enemy");

        // =========================================================
        // Get nearest unexplored starting location and go there

        HasPosition startingLocation;
        if (unit.is(AUnitType.Zerg_Overlord) || ScoutCommander.allScouts().size() > 1) {
            startingLocation = BaseLocations.startingLocationBasedOnIndex(
                unit.getUnitIndexInBwapi()// UnitUtil.getUnitIndex()
            );
        }
        else {
            startingLocation = BaseLocations.nearestUnexploredStartingLocation(unit.position());
        }

        // =========================================================

        if (
            startingLocation != null
                && unit.move(startingLocation, Actions.MOVE_EXPLORE, "Explore", true)
        ) return usedManager(this);

        return null;
    }
}
