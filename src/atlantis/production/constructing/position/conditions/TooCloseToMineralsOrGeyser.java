package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TooCloseToMineralsOrGeyser {
    public static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {
        if (building.isCombatBuilding() && !building.isBunker()) return false;
        if (Select.main() == null) return false;

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);

        if (nearestBase == null) return false;

        AUnit mineral = Select.minerals().nearestTo(position);
        int minDistToMineral = building.isBunker() ? 5 : 2;
        if (mineral != null && mineral.distTo(position) <= minDistToMineral) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to mineral";
            return true;
        }

        AUnit gasBuilding = Select.geysersAndGasBuildings().nearestTo(position);
        if (gasBuilding != null && gasBuilding.distTo(position) <= 3.2) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to geyser";
            return true;
        }

        return false;
    }
}
