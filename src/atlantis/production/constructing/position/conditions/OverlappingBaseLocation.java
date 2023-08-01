package atlantis.production.constructing.position.conditions;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.Bases;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class OverlappingBaseLocation {
    public static boolean isOverlappingBaseLocation(AUnitType building, APosition position) {
        if (building.isBase()) {
            if (Select.ourBuildingsWithUnfinished().bases().inRadius(10, position).isNotEmpty()) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Base already exists here";
                return true;
            }

            return false;
        }

        for (ABaseLocation base : Bases.baseLocations()) {
            APosition natural = Bases.natural();
            if (
                !base.isStartLocation()
                    && (natural != null && natural.translateByTiles(3, 1).distTo(position) <= 3)
                    && base.translateByTiles(We.terran() ? 3 : 0, 0).distTo(position) <= 3.5
            ) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Overlaps base location";
                return true;
            }
        }

        return false;
    }
}