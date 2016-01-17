package atlantis.constructing.position;

import atlantis.information.AtlantisMap;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.wrappers.SelectUnits;
import jnibwapi.BaseLocation;

public class ConstructionSpecialBuildPositionFinder {

    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    protected static Position findPositionForGasBuilding(UnitType building) {
        for (Unit base : SelectUnits.ourBases().list()) {
            Unit geyser = SelectUnits.neutral().ofType(UnitTypes.Resource_Vespene_Geyser).nearestTo(base);

            if (geyser != null && geyser.distanceTo(base) < 10) {
                return geyser.translated(-48, -32);
            }
        }

        return null;
    }

    /**
     * Returns build position for next base. It will usually be next free BaseLocation that doesn't have base
     * built.
     */
    public static Position findPositionForBase(UnitType building, Unit builder) {
//        String mode = "NEAREST_FREE";
        String mode = "NEAR_MAIN";
        if (mode.equals("NEAREST_FREE")) {
            return findPositionForBase_nearestFreeBase(building, builder);
        } else if (mode.equals("NEAR_MAIN")) {
            return findPositionForBase_nearestMainBase(building, builder);
        }

        System.err.println("Error, unknown base location mode");
        return null;
    }

    // =========================================================
    
    private static Position findPositionForBase_nearestFreeBase(UnitType building, Unit builder) {
        BaseLocation baseLocationToExpand = AtlantisMap.getNearestBaseLocationToExpand(SelectUnits.mainBase());
        if (baseLocationToExpand == null) {
            System.err.println("baseLocationToExpand is null");
            return null;
        }

        System.out.println("Main base = " + SelectUnits.mainBase());
        System.out.println("baseLocationToExpand = " + baseLocationToExpand);

        return ConstructionBuildPositionFinder.findStandardPosition(builder, building, baseLocationToExpand, 3);
    }

    private static Position findPositionForBase_nearestMainBase(UnitType building, Unit builder) {
        return ConstructionBuildPositionFinder.findStandardPosition(builder, building, SelectUnits.mainBase(), 20);
    }

}
