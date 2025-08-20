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

                A.errPrintln("Can't build here " + type + ", so cancel + re-request");
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
        AUnit builder = construction.builder();
        AUnitType buildingType = construction.buildingType();
        APosition buildPosition = construction.buildPosition();

        if (buildPosition == null) {
            System.err.println("buildPosition IS NULL - refresh " + buildingType);
            return true;
        }
        if (!buildPosition.isBuildableIncludeBuildings() && !construction.buildingType().isGasBuilding()) {
//            System.err.println("buildPosition NOT BUILDABLE - refresh " + buildingType);
            return true;
        }

        return A.everyNthGameFrame(23)
            && buildPosition.isPositionVisible()
            && !CanPhysicallyBuildHere.check(builder, buildingType, buildPosition);
    }
}
