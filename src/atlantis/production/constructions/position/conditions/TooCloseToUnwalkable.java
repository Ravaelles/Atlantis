package atlantis.production.constructions.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.BuildingTileHelper;
import atlantis.units.AUnitType;

public class TooCloseToUnwalkable {
    public static boolean isTooCloseToUnwalkable(AUnitType building, APosition position) {
        if (building.isBase() || building.isPylon()) return false;

        if (isTooClose(building, position)) return failed("Too close to unwalkable (A)");
        if (building.producesLandUnits() && isTooClose(building, position.translateByTiles(4, 3))) {
            return failed("Too close to unwalkable (B)");
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }

    protected static boolean isTooClose(AUnitType building, APosition position) {
//        int delta = building.getTileWidth() >= 4 ? 5 : (!building.producesLandUnits() ? 3 : 2);
        int delta = building.getTilesWidth() >= 4 ? 3 : (!building.producesLandUnits() ? 1 : 2);

        APosition left = BuildingTileHelper.tilesLeftFrom(building, position, delta);
        APosition down = BuildingTileHelper.tilesDownFrom(building, position, delta);

        if (!left.isWalkable() && !down.isWalkable()) {
            return failed("Left and down from unwalkable");
        }

        APosition up = BuildingTileHelper.tilesUpFrom(building, position, delta);
        APosition right = BuildingTileHelper.tilesRightFrom(building, position, delta);

        if (!up.isWalkable() && !right.isWalkable()) {
            return failed("Up and right from unwalkable");
        }

//        if (
//            !left.isWalkable()
////                && !left.isBuildable()
//        ) return true;
//
//        if (
//            !up.isWalkable()
////                && up.isBuildable()
//        ) return true;
//
//        if (
//            !down.isWalkable()
////                && down.isBuildable()
//        ) return true;

        return false;
    }
}
