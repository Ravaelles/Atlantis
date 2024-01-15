package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.BuildingTileHelper;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class HasEnoughSidesFreeFromOtherBuildings {
    public static boolean isOkay(AUnit builder, AUnitType building, APosition position) {
        if (!We.terran()) return false;
        if (building.isBase()) return true;

        boolean specialCombatBuildings = building.isBunker() || building.isMissileTurret();

        if (!specialCombatBuildings) {
            if (
                !BuildingTileHelper.tileLeftFrom(building, position).isWalkable()
                    && !BuildingTileHelper.tileLeftFrom(building, position).isWalkable()
            ) {
                return forbidden("Not enough side on left");
            }

            if (building.canHaveAddon()) {
                if (!BuildingTileHelper.tileRightFrom(
                    building, position.translateByTiles(2, 0)
                ).isWalkable()) {
                    return forbidden("Not enough side on right");
                }
            }

            if (
//                !BuildingTileHelper.tileUpFrom(building, position).isWalkable() &&
                !BuildingTileHelper.tileDownFrom(building, position).isWalkable()
            ) {
                return forbidden("Not enough side down");
            }
        }

        return true;
    }

    private static boolean forbidden(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return false;
    }
}
