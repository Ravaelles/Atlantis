package atlantis.production.constructions.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.BuildingTileHelper;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class TerranHasEnoughSidesFreeFromOtherBuildings {
    public static boolean isOkay(AUnit builder, AUnitType building, APosition position) {
        if (true) return true;
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
        AbstractPositionFinder._STATUS = reason;
        return false;
    }
}
