package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.BuildingTileHelper;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class HasEnoughSidesFreeFromOtherBuildings {
    public static boolean check(AUnit builder, AUnitType building, APosition position) {
        if (!building.isBunker()) {
            if (
                !BuildingTileHelper.tileRightFrom(building, position).isWalkable()
                    && !BuildingTileHelper.tileLeftFrom(building, position).isWalkable()
            ) return false;

            if (
                !BuildingTileHelper.tileUpFrom(building, position).isWalkable()
                    && !BuildingTileHelper.tileDownFrom(building, position).isWalkable()
            ) return false;
        }

        if (building.isBunker()) {
            double margin = 2.5;

            if (
                !BuildingTileHelper.tileRightFrom(building, position.translateByTiles(margin, 0)).isWalkable()
                    && !BuildingTileHelper.tileLeftFrom(building, position.translateByTiles(-margin, 0)).isWalkable()
            ) return false;

            if (
                !BuildingTileHelper.tileUpFrom(building, position.translateByTiles(0, -margin)).isWalkable()
                    && !BuildingTileHelper.tileDownFrom(building, position.translateByTiles(0, margin)).isWalkable()
            ) return false;
        }

        return true;
    }
}
