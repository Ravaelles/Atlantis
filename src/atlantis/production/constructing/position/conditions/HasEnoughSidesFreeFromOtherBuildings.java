package atlantis.production.constructing.position.conditions;

import atlantis.debug.painter.APainter;
import atlantis.game.GameSpeed;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.BuildingTileHelper;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Color;

public class HasEnoughSidesFreeFromOtherBuildings {
    public static boolean check(AUnit builder, AUnitType building, APosition position) {
//        if (!BuildingTileHelper.tileRightFrom(building, position).isWalkable()) return false;
//        if (!BuildingTileHelper.tileLeftFrom(building, position).isWalkable()) return false;
//        if (!BuildingTileHelper.tileUpFrom(building, position).isWalkable()) return false;
//        if (!BuildingTileHelper.tileDownFrom(building, position).isWalkable()) return false;

//        building = AUnitType.Terran_Barracks;

//        System.out.println(building + " / " + building.dimensionLeftPx() + " / " + building.dimensionRightPx() + " / " + building.dimensionUpPx() + " / " + building.dimensionDownPx());
//        System.out.println(position + " / up:" + BuildingTileHelper.tileUpFrom(building, position));
//        System.out.println(position + " / down:" + BuildingTileHelper.tileDownFrom(building, position));
//        System.out.println(position + " / left:" + BuildingTileHelper.tileLeftFrom(building, position));
//        System.out.println(position + " / right:" + BuildingTileHelper.tileRightFrom(building, position));
//
//        APainter.paintCircle(position, 10, Color.Green);
//        APainter.paintCircle(BuildingTileHelper.tileUpFrom(building, position), 10, Color.Blue);
//        APainter.paintCircle(BuildingTileHelper.tileLeftFrom(building, position), 10, Color.Blue);
//        APainter.paintCircle(BuildingTileHelper.tileRightFrom(building, position), 10, Color.Yellow);
//        APainter.paintCircle(BuildingTileHelper.tileDownFrom(building, position), 10, Color.Yellow);
//        GameSpeed.pauseGame();
        if (
            !BuildingTileHelper.tileRightFrom(building, position).isWalkable()
                && !BuildingTileHelper.tileLeftFrom(building, position).isWalkable()
        ) return false;

        if (
            !BuildingTileHelper.tileUpFrom(building, position).isWalkable()
                && !BuildingTileHelper.tileDownFrom(building, position).isWalkable()
        ) return false;

        return true;
    }
}
