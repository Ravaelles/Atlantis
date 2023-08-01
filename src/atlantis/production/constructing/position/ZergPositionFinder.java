package atlantis.production.constructing.position;

import atlantis.Atlantis;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.conditions.CanPhysicallyBuildHere;
import atlantis.production.constructing.position.conditions.OtherConstructionTooClose;
import atlantis.production.constructing.position.conditions.TooCloseToMineralsOrGeyser;
import atlantis.production.constructing.position.conditions.TooCloseToMainBase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ZergPositionFinder extends AbstractPositionFinder {

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     */
    public static APosition findStandardPositionFor(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        _CONDITION_THAT_FAILED = null;

        if (nearTo == null) {
            System.err.println("@@@ NULL nearTo for findStandardPositionFor " + building);
            return null;
        }

//        int searchRadius = (building.isBase() || building.isCombatBuilding()) ? 0 : 10;
        int searchRadius = 0;
//        maxDistance = limitMaxDistanceForImportantBuildings(maxDistance, building);

        while (searchRadius < maxDistance) {
            int xMin = nearTo.tx() - searchRadius;
            int xMax = nearTo.tx() + searchRadius;
            int yMin = nearTo.ty() - searchRadius;
            int yMax = nearTo.ty() + searchRadius;
            for (int tileX = xMin; tileX <= xMax; tileX++) {
                for (int tileY = yMin; tileY <= yMax; tileY++) {
                    if (tileX == xMin || tileY == yMin || tileX == xMax || tileY == yMax) {
                        APosition constructionPosition = APosition.create(tileX, tileY);
                        if (doesPositionFulfillAllConditions(builder, building, constructionPosition)) {
                            return constructionPosition;
                        }
                    }
                }
            }

            searchRadius++;
        }

        return null;
    }

    // =========================================================
    // Hi-level

    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    public static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {
//        APainter.paintCircle(position, 10, Color.Red);

        if (building.isBunker()) {
            CameraCommander.centerCameraOn(position);
            GameSpeed.changeSpeedTo(100);
        }
//        CameraCommander.centerCameraOn(position);

        // Check for CREEP
        if (!isCreepConditionFulfilled(building, position)) {
            _CONDITION_THAT_FAILED = "No CREEP";
            return false;
        }

        // =========================================================
        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!CanPhysicallyBuildHere.canPhysicallyBuildHere(builder, building, position)) {
//            System.out.println(builder + " / " + ConstructionBuildPositionFinder.building + " / " + position);
            _CONDITION_THAT_FAILED = "CAN'T PHYSICALLY BUILD";
            return false;
        }

        if (TooCloseToMainBase.isTooCloseToMainBase(building, position)) {
            return false;
        }

        // If other buildings too close
        if (OtherConstructionTooClose.isOtherConstructionTooClose(builder, building, position)) {
            _CONDITION_THAT_FAILED = "BUILDINGS TOO CLOSE";
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(building, position)) {
            _CONDITION_THAT_FAILED = "TOO CLOSE TO MINERALS OR GEYSER";
            return false;
        }

        // All conditions are fullfilled, return this position
        _CONDITION_THAT_FAILED = null;
        return true;
    }

    // =========================================================
    // Lo-level

    private static boolean isCreepConditionFulfilled(AUnitType building, APosition position) {
        return Atlantis.game().hasCreep(position.toTilePosition())
            || building.equals(AUnitType.Zerg_Hatchery)
            || building.equals(AUnitType.Zerg_Extractor);
    }

}
