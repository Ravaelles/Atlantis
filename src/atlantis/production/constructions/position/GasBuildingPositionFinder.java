package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.CacheKey;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import bwapi.TilePosition;

public class GasBuildingPositionFinder {
    private static final Cache<APosition> cache = new Cache<>();

    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    public static APosition findPositionForGasBuilding(AUnitType building, HasPosition nearTo) {
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
                        // The geyser's OWN tile (Stardust: Block::Location(geyser->tile)):
                        // the engine builds a refinery only on exactly this tile, so no
                        // center-math belongs here (NEXT #62).
                        TilePosition geyserTile = geyser.tilePosition();
                        if (geyserTile != null) {
                            return APosition.create(geyserTile);
                        }
                    }
                }

                return null;
            }
        );
    }
}
