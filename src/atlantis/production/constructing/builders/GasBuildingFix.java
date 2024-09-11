package atlantis.production.constructing.builders;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.conditions.CanPhysicallyBuildHere;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class GasBuildingFix extends HasUnit {
    public GasBuildingFix(AUnit unit) {
        super(unit);
    }

    /**
     * From reasons impossible to explain sometimes it happens that we need to build Extractor
     * one tile left from the top left position. Sometimes not. This method takes care of
     * these cases and ensures the position is valid.
     */
    protected APosition applyGasBuildingFixIfNeeded(APosition position, AUnitType building) {
        if (position == null) {
            A.errPrintln("Gas building FIX got NULL");
            return null;
        }

        // Fix geyser position problem
        if (building.isGasBuilding() && Select.geysers().inRadius(2, position).isEmpty()) {
            AUnit geyser = Select.geysers().inRadius(12, position).nearestTo(position);
            if (geyser != null) {
                position = geyser.position();
            }
        }

        if (building.isGasBuilding() && Select.geysers().inRadius(3, position).isEmpty()) {
//            ErrorLog.printMaxOncePerMinute(
//                "There are no geysers in radius 3 of " + position + " for " + building
//                    + "\nThis indicates a problem with initial position of the gas building."
//            );
//
//            AUnit geyser = Select.geysers().nearestTo(position);
//            if (geyser != null) {
//                ErrorLog.printErrorOnce("Nearest geyser dist = " + geyser.distTo(position));
//            }
//            else {
//                ErrorLog.printErrorOnce("No geyser is known!");
//            }

            A.errPrintln("Gas building FIX got position too far from geyser");
            AUnit geyser = Select.geysers().inRadius(10, position).nearestTo(position);

            if (geyser == null) return null;

            position = geyser.position();
        }

        // =========================================================

        if (CanPhysicallyBuildHere.check(unit, building, position)) return position;

        // === Loop ================================================

        int radius = 1;
        while (radius <= 4) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    if (CanPhysicallyBuildHere.check(
                        unit, building, position.translateByTiles(dx, dy))
                    ) {
                        return position.translateByTiles(dx, dy);
                    }
                }
            }

            radius++;
        }

        return position;
    }

//    if (
//        building.isGasBuilding()
//        && !CanPhysicallyBuildHere.check(unit, building, position)
//        ) {
//        if (CanPhysicallyBuildHere.check(
//            unit, building, position.translateByTiles(-1, 0))
//        ) {
//            A.errPrintln("Gas building FIX A was applied");
//            return position.translateByTiles(-1, 0);
//        }
//        if (CanPhysicallyBuildHere.check(
//            unit, building, position.translateByTiles(1, 0))
//        ) {
//            A.errPrintln("Gas building FIX B was applied");
//            return position.translateByTiles(1, 0);
//        }
//        if (CanPhysicallyBuildHere.check(
//            unit, building, position.translateByTiles(-2, -1))
//        ) {
//            A.errPrintln("Gas building FIX C was applied");
//            return position.translateByTiles(-2, -1);
//        }
//        if (CanPhysicallyBuildHere.check(
//            unit, building, position.translateByTiles(2, 1))
//        ) {
//            A.errPrintln("Gas building FIX D was applied");
//            return position.translateByTiles(2, 1);
//        }
//
////            if (building.isGasBuilding()) {
////                GameSpeed.pauseGame();
////            }
//
//        ErrorLog.printMaxOncePerMinute("Gas building FIX was not applied. This can halt gas building");
//    }
}
