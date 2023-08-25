package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TooCloseToMineralsOrGeyser {
    public static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {
        if (building.isCombatBuilding()) return false;

        if (Select.main() == null) return false;

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);

        if (nearestBase == null) return false;

        double distToBase = nearestBase.translateByTiles(2, 0).distTo(position);
        if (distToBase <= 10) {
            AUnit mineral = Select.minerals().nearestTo(position);
            if (mineral != null && mineral.distTo(position) <= 4 && distToBase <= 7.5) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to mineral";
                return true;
            }

            AUnit geyser = Select.geysers().nearestTo(position);

            if (geyser != null) {
                int minDistToGeyser = building.isPylon() ? 5 : (building.isSupplyUnit() ? 8 : 6);
                if (geyser.distTo(position) <= minDistToGeyser) {
                    AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to geyser";
                    return true;
                }
            }

            AUnit gasBuilding = Select.geyserBuildings().nearestTo(position);
            if (gasBuilding != null && gasBuilding.distTo(position) <= 4 && distToBase <= 5.5) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to gas building";
                return true;
            }
        }

        return false;
    }
}
