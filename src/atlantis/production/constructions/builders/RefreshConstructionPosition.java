package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.BuildingTilesAreOccupied;
import atlantis.util.AConsole;

public class RefreshConstructionPosition {

    public static APosition refreshIfNeeded(Construction construction) {
        AUnitType buildingType = construction.buildingType();

        if (
            buildingType.isGasBuilding() || (buildingType.isBase() && !Enemy.terran() && !Enemy.zerg())
        ) return construction.buildPosition();

        if (shouldRefreshConstructionPosition(construction)) {
            RefreshConstructionPosition.refreshPosition(construction);
        }

//        return construction.positionToBuildCenter();
        return construction.buildPosition();
    }

    protected static boolean finalRefreshBeforeIssuingOrderFailed(
        AUnit unit, Construction construction, AUnitType type, APosition buildPosition, AUnit builder
    ) {
        if (A.canAfford(type)) {
            buildPosition = handleRefreshingPositionIfNeeded(construction, type, buildPosition);

            if (
                A.everyNthGameFrame(97)
                    && !CanPhysicallyBuildHere.check(unit, type, buildPosition)
                    && (builder == null || builder.lastPositionChangedMoreThanAgo(30 * 8))
            ) {
                construction.cancel(type + " Can't build here");

                AConsole.errPrintln("Can't build here " + type + ", so cancel + re-request");
                AddToQueue.withTopPriority(
                    type,
                    construction.positionToBuildCenter()
                );
                return true;
            }
        }
        return false;
    }

    protected static APosition refreshPosition(Construction construction) {
        if (doNotRefreshPosition(construction)) return construction.buildPosition();

        Construction.clearCache();
        AbstractPositionFinder.clearCache();

        APosition positionForNewBuilding = construction.findPositionForNewBuilding();
        if (positionForNewBuilding != null) {
            construction.setPositionToBuild(positionForNewBuilding);
            Construction.clearCache();

            // The order must move with the construction, or the refresh is undone.
            //
            // A position requested explicitly (RequestBuildingNear ->
            // markAsUsingExactPosition, which is how Protoss pylons, cannons and
            // the Cybernetics Core are placed) is stored on the ProductionOrder as
            // aroundPosition, and DefineExactPositionForNewConstruction hands that
            // value back verbatim the next time the construction is built from the
            // order - with no validation. Updating only the Construction therefore
            // relocated it for one frame and the occupied tile returned on the next
            // order pass: the builder walked to a tile it could never build on and
            // was cancelled after ~57s ("CyberneticsC took too long"), because the
            // exact position it was ordered to kept pointing at the occupied tile.
            ProductionOrder order = construction.productionOrder();
            if (order != null && order.isUsingExactPosition()) {
                order.setAroundPosition(positionForNewBuilding);
            }
        }

        return construction.buildPosition();
    }

    protected static boolean doNotRefreshPosition(Construction construction) {
        return construction.buildingType().isGasBuilding();
    }

    private static APosition handleRefreshingPositionIfNeeded(
        Construction construction, AUnitType buildingType, APosition buildPosition
    ) {
        if (shouldRefreshConstructionPosition(construction)) {
            AbstractPositionFinder.clearCache();

            System.err.println(A.minSec() + " Refresh " + buildingType + " position");
            AbstractPositionFinder.clearCache();

            buildPosition = refreshIfNeeded(construction);
        }

        if (shouldRefreshConstructionPosition(construction)) {
            System.err.println(A.minSec() + " WTF?!? Cancel and request again.");
            APosition prevPosition = construction.buildPosition();
            construction.cancel(buildingType + " position still not good after refresh");

            ProductionOrder newOrder = AddToQueue.withHighPriority(buildingType, prevPosition);
            System.err.println("This got re-requested: " + newOrder);

            if (newOrder == null) {
                newOrder = AddToQueue.withTopPriority(buildingType, prevPosition);
                System.err.println("Re-re-requesting: " + newOrder);
            }
        }

        return buildPosition;
    }

    private static boolean shouldRefreshConstructionPosition(Construction construction) {
        AUnitType buildingType = construction.buildingType();
        APosition buildPosition = construction.buildPosition();

        if (buildPosition == null) {
            System.err.println("buildPosition IS NULL - refresh " + buildingType);
            return true;
        }

        // The engine's raw tile query is the fast answer, but it is not the truth on
        // OpenBW: it reports "not buildable" both when something really stands on the
        // tile and when it merely refuses a valid one (see MapTiles.canBuildHere).
        //
        // The two cases need opposite handling, and asking the same query cannot tell
        // them apart - which is what produced this bug's two opposite symptoms:
        //   * trusting the query alone -> a valid empty tile is called "not good",
        //     the finder re-finds it and the construction is cancelled in a loop
        //     ("position still not good after refresh / buildable:false");
        //   * trusting CanPhysicallyBuildHere (whose JBWEB fallback reads a usedGrid
        //     that is only filled at game start - onUnitDiscover is never called) ->
        //     an occupied tile is called "good", so the builder travels there and
        //     never builds ("took too long (45s) / buildable:false").
        //
        // Occupancy is the discriminator, and the live unit list is the one source
        // that is right in both cases, so that is what decides: refresh only when a
        // unit is actually standing on the tiles.
        if (buildingType != null && !buildingType.isGasBuilding() && !buildPosition.isBuildableIncludeBuildings()) {
            return BuildingTilesAreOccupied.check(buildPosition, buildingType);
        }

        // Same question on a timer, for the case where the tile was free when it was
        // chosen and something moved onto it afterwards. CanPhysicallyBuildHere alone
        // cannot see that here (its JBWEB fallback reads a usedGrid that is never
        // updated in-game), so occupancy is asked directly again.
        return A.everyNthGameFrame(23)
            && buildPosition.isPositionVisible()
            && BuildingTilesAreOccupied.check(buildPosition, buildingType);
    }
}
