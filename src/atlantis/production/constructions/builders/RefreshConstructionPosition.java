package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.RefusedTiles;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import atlantis.units.BuildingTilesAreOccupied;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import atlantis.util.log.LogLevel;
import atlantis.util.log.PostGameDigest;

public class RefreshConstructionPosition {

    public static APosition refreshIfNeeded(Construction construction) {
        AUnitType buildingType = construction.buildingType();

        if (
            buildingType.isGasBuilding() || (buildingType.isBase() && !Enemy.terran() && !Enemy.zerg())
        ) return construction.buildPosition();

        // Owner 2026-10-18: a picked-early site stays put. Refresh exists
        // for a traveling builder whose target went bad - with no builder
        // dispatched yet there is nothing to save, only churn to make.
        // Adoption re-validates at issue time anyway.
        if (construction.status().isWaiting()) {
            return construction.buildPosition();
        }

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
                // Standing-covered sites never reach this cancel: the
                // refresh layer above resites them instead (#145.1). What
                // lands here failed for other reasons (power, unexplored,
                // transient traffic) with a stuck builder or none - cancel
                // and re-request keeps the demand alive.
                twinRefreshCancel(unit, construction, type, buildPosition, builder);
                construction.cancel(type + " Can't build here");

                AConsole.errPrintln(LogLevel.HIGH, "Can't build here " + type + ", so cancel + re-request");
                AddToQueue.withTopPriority(
                    type,
                    construction.positionToBuildCenter()
                );

                return true;
            }
        }
        return false;
    }

    /**
     * #237: names who the refresh cancel strands - the builderless
     * window vs a stuck builder vs a constructing unit read
     * identically in the ledger today ("Can't build here" + re-request
     * every ~100 frames, gas 0.0 all game). The twin carries the
     * discriminator: which builder (or none), whether it had moved
     * recently, and whether the engine thought it was constructing.
     * Extended with the builder's tile, its distance to the site, and
     * the nearest geyser tile: a builder parked ON the geyser reads
     * identically to a wrong site without them.
     */
    private static void twinRefreshCancel(
        AUnit unit, Construction construction, AUnitType type, APosition buildPosition, AUnit builder
    ) {
        try {
            boolean still;
            try {
                still = builder == null || builder.lastPositionChangedMoreThanAgo(30 * 8);
            } catch (Exception ignored) {
                still = true;
            }
            boolean constructing;
            try {
                constructing = builder != null && builder.isConstructing();
            } catch (Exception ignored) {
                constructing = false;
            }
            int btx = -1, bty = -1, bdist = -1, gtx = -1, gty = -1;
            boolean overlaps = false, earlyAllow = false, onGeyser = false;
            boolean engineOk = false;
            String blocker = "-";
            try {
                if (builder != null && builder.position() != null) {
                    btx = builder.tx();
                    bty = builder.ty();
                    if (buildPosition != null) bdist = (int) (builder.distTo(buildPosition) * 10);
                }
                if (type != null && type.isGasBuilding()) {
                    atlantis.units.AUnit geyser =
                        atlantis.units.select.Select.geysers().nearestTo(buildPosition);
                    if (geyser != null && geyser.tilePosition() != null) {
                        gtx = geyser.tilePosition().getX();
                        gty = geyser.tilePosition().getY();
                    }
                }
            } catch (Exception ignored) {
            }
            try {
                overlaps = atlantis.production.constructions.position.conditions
                    .OverlapsOurStandingBuilding.check(buildPosition, type);
                if (overlaps && buildPosition != null && type != null) {
                    int w = type.getTilesWidth(), h = type.getTilesHeights();
                    for (atlantis.units.AUnit u : atlantis.units.select.Select.all().list()) {
                        if (u == null || !u.isAlive() || !u.isABuilding() || !u.isCompleted()) continue;
                        if (u.tilePosition() == null || u.type() == null) continue;
                        int ux = u.tilePosition().getX(), uy = u.tilePosition().getY();
                        if (ux < buildPosition.tx() + w && ux + u.type().getTilesWidth() > buildPosition.tx()
                            && uy < buildPosition.ty() + h && uy + u.type().getTilesHeights() > buildPosition.ty()) {
                            blocker = u.type().name() + "@" + ux + "," + uy + "#" + u.id();
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            try {
                earlyAllow = atlantis.production.constructions.position.conditions
                    .can_build_here.ProtossAllowHereEarlyEvenWithoutRequirements
                    .allow(builder, type, buildPosition);
            } catch (Exception ignored) {
            }
            try {
                onGeyser = type != null && type.isGasBuilding() && buildPosition != null
                    && gtx == buildPosition.tx() && gty == buildPosition.ty();
            } catch (Exception ignored) {
            }
            try {
                engineOk = atlantis.map.MapTiles.canBuildHere(builder, type, buildPosition);
            } catch (Exception | Error ignored) {
            }
            EventLog.key(LogKey.REFRESH_CANCEL)
                .param("unit", unit == null ? -1 : unit.id())
                .param("type", type == null ? "-" : type.name())
                .param("tile", buildPosition == null ? "-" : buildPosition.toString())
                .param("builder", builder == null ? -1 : builder.id())
                .param("still", still)
                .param("constructing", constructing)
                .param("btx", btx)
                .param("bty", bty)
                .param("bdist", bdist)
                .param("gtx", gtx)
                .param("gty", gty)
                .param("overlaps", overlaps)
                .param("blocker", blocker)
                .param("earlyAllow", earlyAllow)
                .param("onGeyser", onGeyser)
                .param("engineOk", engineOk)
                .log(A.now(), A.minSec());
        } catch (Exception ignored) {
        }
    }

    protected static APosition refreshPosition(Construction construction) {
        if (doNotRefreshPosition(construction)) return construction.buildPosition();

        // A refused tile breaks the commitment below on purpose: a builder
        // holds a worthless commitment, and the travel chain never re-issues
        // orders on its own (NEXT #53) - without revoking them the builder
        // keeps walking to the old tile while the row waits at the new one.
        // Skipped when a warp is already up: a started warp is the engine
        // saying yes, and nothing here may move it.
        boolean refusedSite = construction.buildPosition() != null
            && !warpUp(construction)
            && RefusedTiles.isRefused(construction.buildPosition(), construction.buildingType());
        if (refusedSite) {
            AUnit committed = construction.builder();
            if (committed != null) committed.stop("StahpRefused");
        }
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
        // (A refused tile already revoked the commitment above and stopped the
        // builder, so this guard only protects healthy sites.)
        if (!refusedSite && builderHasCommittedToTheSite(construction)) {
            return construction.buildPosition();
        }

        Construction.clearCache();
        AbstractPositionFinder.clearCache();

        APosition positionForNewBuilding = findFreshSite(construction);
        if (positionForNewBuilding != null
            && !sameTile(positionForNewBuilding, construction.buildPosition())) {
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
     * Null-safe tile equality for the no-op guard above: a failed search
     * re-serves the identical tile (or null), and neither is a move.
     */
    private static boolean sameTile(APosition a, APosition b) {
        if (a == null || b == null) return a == null && b == null;

        return a.tx() == b.tx() && a.ty() == b.ty();
    }

    /**
     * The resite search, answering even when the assigned builder is gone:
     * the funnel needs a living builder for validation, and "no-builder"
     * declines forever on a row whose builder just died (measured live:
     * a refused Gateway kept its cursed tile through two re-arms because
     * every dead-builder window answered null). A free worker validates
     * as a proxy - it is never assigned here, the row stays builderless
     * until the re-arm paths run, and a live assigned builder is always
     * preferred (same answers as before for healthy rows). Orderless
     * rows and the testing seam stay in the shared overload, not
     * duplicated here.
     */
    private static APosition findFreshSite(Construction construction) {
        AUnit builder = construction.builder();
        if (builder != null && builder.isAlive()) {
            return construction.findPositionForNewBuilding();
        }

        AUnit proxy = null;
        try {
            APosition anchor = construction.buildPosition() != null
                ? construction.buildPosition()
                : (construction.nearTo() != null
                    ? APosition.create(construction.nearTo())
                    : null);
            if (anchor != null) {
                proxy = atlantis.units.workers.FreeWorkers.get().nearestTo(anchor);
            }
        } catch (Exception | Error ignored) {
        }
        if (proxy == null || !proxy.isAlive()) return construction.findPositionForNewBuilding();

        return construction.findPositionForNewBuilding(proxy);
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

    /**
     * A started warp is the engine saying yes: neither refresh, resite
     * nor stop may touch the row. Covers the adopted building (the
     * demote path names it) and a builder mid-construction/morph,
     * whichever the path observed first.
     */
    private static boolean warpUp(Construction construction) {
        if (construction.buildingUnit() != null) return true;

        AUnit builder = construction.builder();

        return builder != null && (builder.isConstructing() || builder.isMorphing());
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

            APosition before = construction.buildPosition();
            buildPosition = refreshIfNeeded(construction);

            // Twin + stderr only when the site actually moved. A failed
            // search re-serves the same tile every frame (measured live:
            // 81 no-op "refreshed" twins + log lines for one Gateway while
            // its builder walked to an engine-refused tile), and each one
            // read as a move in the log.
            if (!sameTile(before, construction.buildPosition())) {
                // Owner 2026-11-04: a moved site reads MEDIUM, not LOW -
                // a building that cannot sit disrupts the whole pipeline
                // (measured live: first Gateway unplaceable to 2:47 while
                // the log whispered LOW). Still capped at ten per type.
                AConsole.printlnCapped(LogLevel.MEDIUM, "refresh-move-" + buildingType,
                    PostGameDigest.MAX_SAMPLES_PER_KEY,
                    A.minSec() + " Refresh " + buildingType + " position");

                EventLog.key(LogKey.POSITION_REFRESH)
                    .param("type", buildingType.name())
                    .param("result", "refreshed")
                    .param("prevTile", before == null ? "-" : before.toString())
                    .param("tile", construction.buildPosition() == null ? "-"
                        : construction.buildPosition().toString())
                    .log(A.now(), A.minSec());
            }
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

            AConsole.errPrintln(LogLevel.HIGH, A.minSec() + " Cancel and request again: " + buildingType);
            APosition prevPosition = construction.buildPosition();
            AUnit blocker = blockingCompletedBuilding(prevPosition, buildingType);
            if (blocker != null) {
                AConsole.errPrintln(LogLevel.HIGH, A.minSec() + " site blocked by "
                    + blocker.type().name() + " at [" + blocker.tx() + "," + blocker.ty() + "]");
            }

            construction.cancel(buildingType + " position still not good after refresh");

            // A completed building on the site kills the tile forever -
            // re-requesting it spins (measured: 10x same-tile churn on a
            // Pylon-covered Core site). Re-pick elsewhere instead;
            // unfinished overlap keeps the tile (the warp settles it and
            // B-41 adopts on position).
            HasPosition nextPosition = completedBuildingOverlaps(prevPosition, buildingType)
                ? null : prevPosition;
            ProductionOrder replaced = construction.productionOrder();
            ProductionOrder newOrder;
            if (replaced != null && Queue.get() != null
                && Queue.get().allOrders().list().contains(replaced)
                && !replaced.isStatus(OrderStatus.FINISHED)) {
                // One demand, one row: the replaced row survives the cancel
                // above, and a fresh row next to it would mint a twin the
                // next time either path fires (measured live: duplicate
                // tech buildings). Reuse the live row instead - same supply
                // gate, fresh site, reset patience - and mint nothing.
                replaced.setAroundPosition(nextPosition);
                replaced.clearFailedSearchStreak();
                replaced.resetRuntimeState();
                newOrder = replaced;
            }
            else {
                newOrder = AddToQueue.withHighPriority(buildingType, nextPosition);
            }
            AConsole.errPrintln(LogLevel.HIGH, "This got re-requested: " + newOrder);

            if (newOrder == null) {
                newOrder = AddToQueue.withTopPriority(buildingType, nextPosition);
                AConsole.errPrintln(LogLevel.HIGH, "Re-re-requesting: " + newOrder);
            }

            // Same demand, same timing: a tail-derived minSupply parks the
            // replacement rows above the current supply (measured: "At 28"
            // re-requests maturing into duplicate Cores), while the pump
            // resets its own rows on the next pass anyway. Copied only onto
            // a genuinely new row - a reused row already carries it.
            if (newOrder != null && newOrder != replaced && replaced != null) {
                newOrder.setMinSupply(replaced.minSupply());
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

    /**
     * The completed building overlapping the site, or null: names the
     * blocker the pick-time guard must already have refused. Log-only;
     * the cancel decision below does not read it.
     */
    private static AUnit blockingCompletedBuilding(APosition site, AUnitType type) {
        if (site == null) return null;

        int left = site.tx();
        int top = site.ty();
        int right = left + type.getTilesWidth();
        int bottom = top + type.getTilesHeights();

        for (AUnit unit : Select.all().list()) {
            if (unit == null || !unit.isAlive()) continue;
            if (!unit.isABuilding() || !unit.isCompleted()) continue;

            int unitRight = unit.tx() + unit.type().getTilesWidth();
            int unitBottom = unit.ty() + unit.type().getTilesHeights();
            if (unit.tx() < right && left < unitRight && unit.ty() < bottom && top < unitBottom) {
                return unit;
            }
        }

        return null;
    }

    /**
     * Overlap twin of the status changer's exact-tiles finish: a completed
     * building anywhere on the reserved footprint (not just the same
     * origin) kills the tile, so the re-request must move on instead of
     * re-pinning it. Unfinished overlap returns false - the warp settles
     * it.
     */
    private static boolean completedBuildingOverlaps(APosition site, AUnitType type) {
        return blockingCompletedBuilding(site, type) != null;
    }

    private static boolean shouldRefreshConstructionPosition(Construction construction) {
        AUnitType buildingType = construction.buildingType();
        APosition buildPosition = construction.buildPosition();

        if (buildPosition == null) {
            return true;
        }

        // An engine-refused tile never heals by waiting: the builder walks,
        // the engine says no, the row demotes or ages out, and the re-arm
        // walks back to the same cursed tile (measured live 2026-11-03 vs
        // Steamhammer: Gateway at unpowered [123,26], 82 s of refused Builds,
        // then the cancel churn with the bank full). Moving now is strictly
        // better than standing: the catalogue re-gates the fresh tile
        // (power, geyser, refused, standing cover), and a null answer keeps
        // the old site. Decided gas sites never refresh (doNotRefreshPosition)
        // and neither does a row whose warp is already up (buildingUnit set
        // or builder constructing) - a started warp is the engine saying yes.
        if (!doNotRefreshPosition(construction)
            && !warpUp(construction)
            && RefusedTiles.isRefused(buildPosition, buildingType)) {
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
        // that is right in both cases, so that is what decides: refresh only
        // when a COMPLETED building stands on the tiles. A crossing worker
        // is traffic, not a verdict (owner log: a Pylon re-placed twice in
        // minutes, "Refresh Nexus position" spam for 4 minutes while miners
        // crossed the site) - the builder issues when the tile is free.
        // A warping building is the construction itself in progress, never
        // a reason to move. The builder exclusion the old question needed
        // dies with it: a unit never triggers a refresh anymore, so the
        // arriving builder cannot trip its own site.
        if (buildingType != null && !buildingType.isGasBuilding() && !buildPosition.isBuildableIncludeBuildings()) {
            return siteHopelesslyBlocked(buildPosition, buildingType);
        }

        // Same question on a timer, for the case where the tile was free when it was
        // chosen and something moved onto it afterwards. CanPhysicallyBuildHere alone
        // cannot see that here (its JBWEB fallback reads a usedGrid that is never
        // updated in-game), so occupancy is asked directly again.
        return A.everyNthGameFrame(23)
            && buildPosition.isPositionVisible()
            && siteHopelesslyBlocked(buildPosition, buildingType);
    }

    /**
     * The refresh discriminator, extracted as a seam so it pins without a
     * map or a finder: true only when a completed building stands on the
     * footprint. Units (crossing workers, the arriving builder) and
     * warping buildings (the construction itself) never move a site.
     */
    public static boolean siteHopelesslyBlocked(APosition site, AUnitType type) {
        return BuildingTilesAreOccupied.checkBuildingsOnly(site, type);
    }
}
