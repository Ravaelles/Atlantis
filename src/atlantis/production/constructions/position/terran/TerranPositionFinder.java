package atlantis.production.constructions.position.terran;

import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.PositionFulfillsAllConditions;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.cache.CacheKey;
import atlantis.util.log.ErrorLog;

public class TerranPositionFinder extends AbstractPositionFinder {
    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     */
    public static APosition findStandardPositionFor(
        AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance
    ) {
        int cacheForFrames = 23;

        ValidateParams validator = validateParams(builder, building, nearTo, maxDistance);

        String cacheKey = CacheKey.create(
            "findStandardPositionFor",
            building,
            nearTo
//            A.digit(validator.maxDistance)
        );

        return cache.get(
            cacheKey,
            cacheForFrames,
            () -> findNewPosition(validator.finalBuilder, building, validator.finalNearTo, validator.maxDistance)
        );
    }

    private static ValidateParams validateParams(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        if (maxDistance < 0) maxDistance = 28;

        if (builder == null) {
//            A.errPrintln("builder is null for " + building + ", fallback to any builder");
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("builder is null for " + building + ", fallback to any");
            builder = FreeWorkers.get().first();
        }
//        if (nearTo == null) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Your nearTo is null for " + building + ", fallback to any building");
//        }

        HasPosition finalNearTo = nearTo != null ? nearTo : Select.mainOrAnyBuilding();
        AUnit finalBuilder = builder;
        return new ValidateParams(maxDistance, finalNearTo, finalBuilder);
    }

    private static class ValidateParams {
        public final double maxDistance;
        public final HasPosition finalNearTo;
        public final AUnit finalBuilder;

        public ValidateParams(double maxDistance, HasPosition finalNearTo, AUnit finalBuilder) {
            this.maxDistance = maxDistance;
            this.finalNearTo = finalNearTo;
            this.finalBuilder = finalBuilder;
        }
    }

    public static APosition findNewPosition(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        _STATUS = "None";

        // =========================================================

        if (builder == null) {
            AbstractPositionFinder._STATUS = "NO BUILDER ASSIGNED";
            return null;
        }

        // =========================================================

//        int searchRadius = (building.isBase() || building.isCombatBuilding()) ? 0 : 1;
        int searchRadius = 0;

//        boolean logToFile = building.isGateway();
//        if (logToFile) LogToFile.info("------------\n");

        int xMapMax = AMap.getMapWidthInTiles() - 1;
        int yMapMax = AMap.getMapHeightInTiles() - 1;

//        System.err.println("maxDistance = " + maxDistance);

        while (searchRadius < maxDistance) {
            int xMin = Math.max(0, nearTo.tx() - searchRadius);
            int xMax = Math.min(xMapMax, nearTo.tx() + searchRadius);
            int yMin = Math.max(0, nearTo.ty() - searchRadius);
            int yMax = Math.min(yMapMax, nearTo.ty() + searchRadius);

            for (int tileX = xMin; tileX <= xMax; tileX++) {
                for (int tileY = yMin; tileY <= yMax; tileY++) {
                    if (tileX == xMin || tileY == yMin || tileX == xMax || tileY == yMax) {
//                        if (logToFile) LogToFile.info("tx,ty: [" + tileX + "," + tileY + "]\n");

                        APosition constructionPosition = APosition.create(tileX, tileY);
//                        System.err.println("constructionPosition = " + constructionPosition + " / " + _CONDITION_THAT_FAILED);
                        if (PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                            builder, building, constructionPosition, nearTo
                        )) {
                            if (building.isCombatBuilding()) {
                                // Turret fix - make sure to build in the same region
                                if (constructionPosition.groundDistanceTo(nearTo) > 1.6 * searchRadius) {
                                    continue;
                                }
                            }

                            AbstractPositionFinder._STATUS = "OK";
                            return constructionPosition;
                        }

//                        if (A.supplyUsed() <= 28) {
//                            System.out.println("Fail: " + _STATUS);
//                        }
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
        if (!We.terran()) return false;

        if (
            building.canHaveAddon()
                && !building.isBase()
                && !CanPhysicallyBuildHere.check(builder, building, position.translateByTiles(2, 0))
        ) {
            _STATUS = "MY_ADDON_COULDNT_BE_BUILT_HERE";
            return true;
        }

        // === Compare against existing buildings ========================================

        // Check for other buildings' addons
        for (AUnit otherBuilding : Select.ourBuildingsWithUnfinished().inRadius(8, position).list()) {
            if (otherBuilding.canHaveAddon()) {
                if (!CanPhysicallyBuildHere.check(builder, building, position.translateByTiles(-2, 0))) {
                    _STATUS = "WOULD_COLLIDE_WITH_ANOTHER_BUILDING_ADDON";
                    return true;
                }
            }
        }

        return false;
    }
}
