package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import atlantis.util.cache.CacheKey;

public class GasBuildingPositionFinder {
    private static Cache<APosition> cache = new Cache<>();

    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    protected static APosition findPositionForGasBuilding(AUnitType building, HasPosition nearTo) {
        return cache.get(
            CacheKey.create("findPositionForGasBuilding", building, nearTo),
            157,
            () -> {
                for (AUnit base : Select.ourBases().list()) {
                    Selection geysers = Select.geysers();

                    int maxDistFromBase = We.zerg() ? 7 : 12;

//                    HasPosition innerNearTo = nearTo;
//                    if (innerNearTo == null) innerNearTo = Select.ourBases().last();
//                    if (innerNearTo != null) geysers = geysers.inRadius(maxDistFromBase, innerNearTo);

                    AUnit geyser = geysers.nearestTo(base);
                    if (geyser != null && geyser.distTo(base) < maxDistFromBase) {
                        return geyser.translateByPixels(-64, -32);
                    }
                }

                return null;
            }
        );
    }
}