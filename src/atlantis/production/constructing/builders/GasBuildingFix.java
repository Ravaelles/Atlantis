package atlantis.production.constructing.builders;

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

            return null;
        }

        if (
            building.isGasBuilding()
                && !CanPhysicallyBuildHere.check(unit, building, position)
        ) {
            if (CanPhysicallyBuildHere.check(
                unit, building, position.translateByTiles(-1, 0))
            ) {
//                System.out.println("Applied [-1,0] " + building + " position FIX");
                return position.translateByTiles(-1, 0);
            }
            if (CanPhysicallyBuildHere.check(
                unit, building, position.translateByTiles(1, 0))
            ) {
//                System.out.println("Applied [1,0] " + building + " position FIX");
                return position.translateByTiles(1, 0);
            }
            if (CanPhysicallyBuildHere.check(
                unit, building, position.translateByTiles(-2, -1))
            ) {
//                System.out.println("Applied [-2,-1] " + building + " position FIX");
                return position.translateByTiles(-2, -1);
            }
            if (CanPhysicallyBuildHere.check(
                unit, building, position.translateByTiles(2, 1))
            ) {
//                System.out.println("Applied [2,1] " + building + " position FIX");
                return position.translateByTiles(2, 1);
            }

//            if (building.isGasBuilding()) {
//                GameSpeed.pauseGame();
//            }

            ErrorLog.printMaxOncePerMinute("Gas building FIX was not applied. This can halt gas building");
        }

        return position;
    }
}