package atlantis.production.constructing.position;

import atlantis.config.AtlantisRaceConfig;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;

public class GasBuildingPositionFinder {
    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    protected static APosition findPositionForGasBuilding(AUnitType building, HasPosition nearTo) {
//        System.err.println(
//            "Count.inProductionOrInQueue(AtlantisRaceConfig.GAS_BUILDING) = "
//                + Count.inProductionOrInQueue(AtlantisRaceConfig.GAS_BUILDING)
//        );

        if (Count.inProductionOrInQueue(AtlantisRaceConfig.GAS_BUILDING) >= 2) {
//            A.printStackTrace("Too many refineries");
            ErrorLog.printMaxOncePerMinute("Too many refineries, don't build for now");
            return null;
        }

        for (AUnit base : Select.ourBases().list()) {
            Selection geysers = Select.geysers();

            if (nearTo != null) {
                geysers = geysers.inRadius(12, nearTo);
            }

            AUnit geyser = geysers.nearestTo(base);
            if (geyser != null && geyser.distTo(base) < 12) {
                return geyser.translateByPixels(-64, -32);
            }
        }

//        System.err.println("Couldn't find geyser for " + building);
        return null;
    }
}