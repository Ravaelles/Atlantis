package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.BuildingTileHelper;
import atlantis.units.AUnitType;

public class TooCloseToUnwalkable {
    public static boolean isTooCloseToUnwalkable(AUnitType building, APosition position) {
//        if (true) return false;

        if (building.isSupplyDepot()) return false;
        if (building.isBase() || building.isCombatBuilding() || building.isGasBuilding()) return false;

        if (isTooClose(building, position)) return true;

        AbstractPositionFinder._CONDITION_THAT_FAILED = "Too close to unwalkable";
        return false;
    }

    protected static boolean isTooClose(AUnitType building, APosition position) {
//        APosturn true;

        APosition left = BuildingTileHelper.tileLeftFrom(building, position);
        APosition up = BuildingTileHelper.tileUpFrom(building, position);
        APosition down = BuildingTileHelper.tileDownFrom(building, position);

        if (!left.isWalkable() && !down.isWalkable()) return true;

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
