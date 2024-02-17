package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.BuildingTileHelper;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossHasEnoughSidesFreeFromOtherBuildings {
    public static boolean isOkay(AUnit builder, AUnitType building, APosition position) {
        if (!We.protoss()) return true;
        if (building.isPylon()) return true;
        if (building.isBase()) return true;
        if (building.isCannon()) return true;

        if (
            !BuildingTileHelper.tileLeftFrom(building, position).isWalkable()
                && !BuildingTileHelper.tileRightFrom(building, position).isWalkable()
        ) {
            return forbidden("Not enough side on left and right");
        }

        if (
            !BuildingTileHelper.tileUpFrom(building, position).isWalkable() &&
                !BuildingTileHelper.tileDownFrom(building, position).isWalkable()
        ) {
            return forbidden("Not enough side on top and bottom");
        }

        return true;
    }

    private static boolean forbidden(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return false;
    }
}
