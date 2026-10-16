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
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

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

    /**
     * Public as a seam: tests pin the refresh-vs-cancel contract here
     * (a decided gas site must survive the pass) rather than through a game.
     */
    public static boolean finalRefreshBeforeIssuingOrderFailed(
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

        // NEVER move the site out from under a builder that is already on its way there.
        //
        // This is the remaining half of NEXT.md #53, and it is the same shape as #52: a
        // decision made twice, disagreeing. The construction's site can be re-found at any
        // time, but once a worker has been sent, moving the site leaves the worker walking
        // to the old tile and the construction waiting at the new one - forever, because
        // nothing reconciles the two. Measured before this guard:
        //
        //     REFRESH_MOVE Gateway from=[16,48] to=[17,41] builder=#122@9,45
        //                  builderIsMoving=false status=NOT_STARTED
        //
        // seven tiles of drift, with the builder twelve tiles away and no order that would
        // ever take it to either tile. The owner's log shows the same from the other side:
        // `FOUND PLACE FOR Gateway at 118,37` alternating with `Pylon at 101,30`, the same
        // two tiles over and over - the finder is honest, the construction keeps changing
        // its mind.
        //
        // A builder that is assigned but has NOT started moving is still fair game: nothing
        // has been committed yet, and that is the case this refresh exists for.
        if (builderHasCommittedToTheSite(construction)) {
            return construction.buildPosition();
        }

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

    /**
     * Has a worker been sent to this construction's tile already - or is it standing there?
     *
     * <p>
     * True when the builder is moving (it has a destination and a commitment to it) or when
     * it is already within the site's neighbourhood. A merely assigned builder that is idle
     * at the base does NOT count: no order has been issued, so relocating the site costs
     * nothing and is exactly what the refresh is for.
     * </p>
     *
     * <p>
     * Deliberately generous about "at the site" (3.5 tiles, the same radius the status
     * change uses): the engine shifts a worker onto the footprint as the building goes up, so
     * a tight comparison would declare the builder uncommitted at the very moment it is
     * building.
     * </p>
     */
    private static boolean builderHasCommittedToTheSite(Construction construction) {
        AUnit builder = construction.builder();
        if (builder == null) return false;
        if (construction.buildPosition() == null) return false;

        if (builder.isMoving()) return true;

        return builder.distTo(construction.buildPosition()) <= 3.5;
    }

    protected static boolean doNotRefreshPosition(Construction construction) {
        return construction.buildingType().isGasBuilding();
    }

    private static APosition handleRefreshingPositionIfNeeded(
        Construction construction, AUnitType buildingType, APosition buildPosition
    ) {
        // A decided gas site is exact (its geyser's tile): refresh is a no-op
        // for it by doNotRefreshPosition, so a cancel + same-tile re-request
        // after it can never heal - with the geyser present it rebuilds the
        // identical site (churn), with it consumed (warped) the re-request
        // finds no geyser and dies on null (measured 3:44 Assimilator loop).
        // Null sites keep the old path: fresh placement there can help.
        if (doNotRefreshPosition(construction) && construction.buildPosition() != null) {
            return buildPosition;
        }

        if (shouldRefreshConstructionPosition(construction)) {
            AbstractPositionFinder.clearCache();

            System.err.println(A.minSec() + " Refresh " + buildingType + " position");
            AbstractPositionFinder.clearCache();

            buildPosition = refreshIfNeeded(construction);

            EventLog.key(LogKey.POSITION_REFRESH)
                .param("type", buildingType.name())
                .param("result", "refreshed")
                .log(A.now(), A.minSec());
        }

        if (shouldRefreshConstructionPosition(construction)) {
            // Transient traffic is not a dead site (Wine: a miner walking
            // over the Core site cancelled it 7x in 10s - #126 to #132 -
            // minting a duplicate 200-mineral reservation per cancel until
            // the bank froze at 600 with the building affordable). Only a
            // standing building makes refresh hopeless: units move, so the
            // builder issues when the tile is free. Null sites keep the old
            // path (fresh placement heals); true stalls still die by overdue.
            if (construction.buildPosition() != null
                && !BuildingTilesAreOccupied.checkBuildingsOnly(
                    construction.buildPosition(), buildingType)) {
                return buildPosition;
            }

            System.err.println(A.minSec() + " WTF?!? Cancel and request again.");
            APosition prevPosition = construction.buildPosition();
            construction.cancel(buildingType + " position still not good after refresh");

            ProductionOrder newOrder = AddToQueue.withHighPriority(buildingType, prevPosition);
            System.err.println("This got re-requested: " + newOrder);

            if (newOrder == null) {
                newOrder = AddToQueue.withTopPriority(buildingType, prevPosition);
                System.err.println("Re-re-requesting: " + newOrder);
            }

            EventLog.key(LogKey.POSITION_REFRESH)
                .param("type", buildingType.name())
                .param("result", "cancelled-re-requested")
                .param("prevTile", prevPosition == null ? "-" : prevPosition.toString())
                .param("newOrder", newOrder == null ? "-" : newOrder.toString())
                .log(A.now(), A.minSec());
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
        //
        // The builder is passed in (NEXT.md #49) so the question asked here is the one
        // the game enforces: the footprint may contain the unit that will build - the
        // engine moves it onto the site - and nothing else. Without it, the builder
        // arriving at its own site looked like "something is standing there", which would
        // refresh the position out from under a construction that was about to start.
        AUnit builder = construction.builder();

        if (buildingType != null && !buildingType.isGasBuilding() && !buildPosition.isBuildableIncludeBuildings()) {
            return BuildingTilesAreOccupied.check(buildPosition, buildingType, builder);
        }

        // Same question on a timer, for the case where the tile was free when it was
        // chosen and something moved onto it afterwards. CanPhysicallyBuildHere alone
        // cannot see that here (its JBWEB fallback reads a usedGrid that is never
        // updated in-game), so occupancy is asked directly again.
        return A.everyNthGameFrame(23)
            && buildPosition.isPositionVisible()
            && BuildingTilesAreOccupied.check(buildPosition, buildingType, builder);
    }
}
