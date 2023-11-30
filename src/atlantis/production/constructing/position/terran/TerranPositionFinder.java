package atlantis.production.constructing.position.terran;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.PositionFulfillsAllConditions;
import atlantis.production.constructing.position.conditions.CanPhysicallyBuildHere;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class TerranPositionFinder extends AbstractPositionFinder {
    private static Cache<APosition> cache = new Cache<>();

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     */
    public static APosition findStandardPositionFor(
        AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance
    ) {
//        int cacheForFrames = building.isCombatBuilding() ? 27 : 77;
        int cacheForFrames = 83;

        String cacheKey = "findStandardPositionFor:" + building.id()
            + "," + (nearTo != null ? nearTo.toStringPixels() : nearTo)
            + "," + (builder != null ? builder.id() : "-");

//        System.err.println("@ " + A.now() + " - " + building + " / near " + nearTo + " / maxDistance " + maxDistance);

        return cache.get(
            cacheKey,
            cacheForFrames,
            () -> findNewPosition(builder, building, nearTo, maxDistance)
        );
    }

    private static APosition findNewPosition(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        _CONDITION_THAT_FAILED = null;

//        System.err.println("building = " + building);
//        System.err.println("nearTo = " + nearTo);
//        System.err.println("maxDistance = " + maxDistance);

        int searchRadius = (building.isBase() || building.isCombatBuilding()) ? 0 : 1;

        while (searchRadius < maxDistance) {
            int xMin = nearTo.tx() - searchRadius;
            int xMax = nearTo.tx() + searchRadius;
            int yMin = nearTo.ty() - searchRadius;
            int yMax = nearTo.ty() + searchRadius;
            for (int tileX = xMin; tileX <= xMax; tileX++) {
                for (int tileY = yMin; tileY <= yMax; tileY++) {
                    if (tileX == xMin || tileY == yMin || tileX == xMax || tileY == yMax) {
                        APosition constructionPosition = APosition.create(tileX, tileY);
                        if (PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                            builder, building, constructionPosition, nearTo
                        )) {

                            if (building.isCombatBuilding()) {
                                // Turret fix - make sure to build in the same region
                                if (constructionPosition.groundDistanceTo(nearTo) > 1.6 * searchRadius) {
                                    continue;
                                }
                            }

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
    // Low-level

    public static boolean isNotEnoughPlaceLeftForAddons(AUnit builder, AUnitType building, APosition position) {
        if (
            building.canHaveAddon()
                && !building.isBase()
                && !CanPhysicallyBuildHere.check(builder, building, position.translateByTiles(2, 0))
        ) {
            _CONDITION_THAT_FAILED = "MY_ADDON_COULDNT_BE_BUILT_HERE";
            return true;
        }

        // === Compare against existing buildings ========================================

        // Check for other buildings' addons
        for (AUnit otherBuilding : Select.ourBuildingsWithUnfinished().inRadius(8, position).list()) {
            if (otherBuilding.canHaveAddon()) {
                if (!CanPhysicallyBuildHere.check(builder, building, position.translateByTiles(-2, 0))) {
                    _CONDITION_THAT_FAILED = "WOULD_COLLIDE_WITH_ANOTHER_BUILDING_ADDON";
                    return true;
                }
            }
        }

        return false;
    }
}
