package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.BuildingTileHelper;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class HasEnoughSidesFreeFromOtherBuildings {
    public static boolean isOkay(AUnit builder, AUnitType building, APosition position) {
        if (building.isBase()) return true;

        if (!building.isBunker()) {
            if (
                !BuildingTileHelper.tileRightFrom(building, position).isWalkable()
                    && !BuildingTileHelper.tileLeftFrom(building, position).isWalkable()
            ) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Not enough side on left";
                return false;
            }

            if (building.canHaveAddon()) {
                if (!BuildingTileHelper.tileRightFrom(
                    building, position.translateByTiles(2, 0)
                ).isWalkable()) {
                    AbstractPositionFinder._CONDITION_THAT_FAILED = "Not enough side on right";
                    return false;
                }
            }

            if (
//                !BuildingTileHelper.tileUpFrom(building, position).isWalkable() &&
                !BuildingTileHelper.tileDownFrom(building, position).isWalkable()
            ) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Not enough side down";
                return false;
            }
        }

        if (building.isBunker()) {
            double margin = 2.5;

            if (
                !BuildingTileHelper.tileRightFrom(building, position.translateByTiles(margin, 0)).isWalkable()
                    && !BuildingTileHelper.tileLeftFrom(building, position.translateByTiles(-margin, 0)).isWalkable()
            ) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Bunker not enough side on left/right";
                return false;
            }

            if (
                !BuildingTileHelper.tileUpFrom(building, position.translateByTiles(0, -margin)).isWalkable()
                    && !BuildingTileHelper.tileDownFrom(building, position.translateByTiles(0, margin)).isWalkable()
            ) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Bunker not enough side on up/down";
                return false;
            }
        }

        return true;
    }
}
