package atlantis.production.constructions;

import atlantis.game.A;
import atlantis.information.enemy.UnitsArchive;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.DefineExactPositionForNewConstruction;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.AConsole;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Owner 2026-10-14, extended 2026-10-15: pick the site two supplies before
 * the order unlocks, so the building is aimed while still ~100 minerals
 * short of its price. A PREPARING construction holds the reserved site with
 * no builder and no issue; ProduceBuilding adopts it (builder + NOT_STARTED)
 * once the order is ready instead of searching anew. While poor or
 * supply-gated the site sits marked without costing a worker - and a
 * placement that can never work shows up here, at pick time, instead of
 * deep in the builder loop.
 *
 * <p>v1 scope: ordinary buildings only. Bases (special natural logic) and
 * gas buildings (fixed geyser tile, nothing to pick) stay on the old path.
 * One site in flight per type; staleness is bounded because picking happens
 * at most ~2 supplies ahead, and RefreshConstructionPosition keeps running.
 */
public class PrepareConstructionSite {

    public static void prepareIfDue(ProductionOrder order) {
        if (order == null || !order.isBuilding()) return;
        if (order.construction() != null) return;

        AUnitType type = order.unitType();
        if (type == null || type.isBase() || type.isGasBuilding()) return;

        // Two supplies earlier (owner spec): Pylon at 8 is picked at 6,
        // Gateway at 9 at 7. Past that the site may sit reserved while
        // poor or supply-gated.
        if (A.supplyUsed() < order.minSupply() - 2) return;

        // One site in flight per type - the second Gateway row prepares once
        // the first leaves the unfinished list.
        if (!ConstructionRequests.notFinishedOfType(type).isEmpty()) return;

        // Owner 2026-10-16: no PREPARING while one already warps. A site
        // picked minutes early for a building that is coming anyway only
        // churns (re-picks, refresh fights); the row waits as a plain
        // queued order and prepares once the warping one completes. MANDATORY.
        if (Count.ourUnfinishedOfType(type) > 0) return;

        Construction prepared = new Construction(type);
        prepared.setProductionOrder(order);
        prepared.setNearTo(order.aroundPosition());
        prepared.setMaxDistance(order.maximumDistance());

        APosition site = DefineExactPositionForNewConstruction.exactPositionForNewConstruction(
            type, order, prepared);
        if (site == null) {
            // Search failed: unlink (setProductionOrder linked both ways) so
            // the order stays a plain queued row; the orders path reports it
            // (Can't find place) and retries - same as today, no leak.
            order.setConstruction(null);

            return;
        }

        prepared.setPositionToBuild(site);
        prepared.setStatus(ConstructionOrderStatus.PREPARING);

        // Owner 2026-10-18: a picked site must never overlap a sibling's
        // site (measured: Gateway [95,111] on top of warping [92,111]).
        // Overlap unlinks and retries later - same as a failed search.
        if (overlapsUnfinishedSite(site, type)) {
            EventLog.key(LogKey.BUILD_REFUSED)
                .param("type", type.name())
                .param("guard", "prepare-site-overlap")
                .param("oid", order.id())
                .param("order", order.toString())
                .param("tile", "[" + site.tx() + "," + site.ty() + "]")
                .param("supplyUsed", A.supplyUsed())
                .param("minerals", A.minerals())
                .log(A.now(), A.minSec());
            order.setConstruction(null);

            return;
        }

        // Owner 2026-10-18: link any (nearest free, not random - the
        // search is free and determinism pins in tests) worker at pick
        // time. Inert: BuilderManager claims on builder(), never on this,
        // so the worker keeps mining until adoption prefers it.
        AUnit reserved = atlantis.units.workers.FreeWorkers.get().nearestTo(site);
        if (reserved != null) prepared.setReservedBuilder(reserved);

        ConstructionRequests.addConstruction(prepared);
//        AConsole.errPrintln(
//            A.minSec() + ": PREPARING " + type.name()
//                + " at [" + site.tx() + "," + site.ty() + "] for supply " + order.minSupply());
        EventLog.key(LogKey.CONSTRUCTION_PREPARING)
            .param("type", type.name())
            .param("cid", prepared.id())
            .param("oid", order.id())
            .param("tx", site.tx())
            .param("ty", site.ty())
            .param("supply", order.minSupply())
            .param("reserved", reserved == null ? "-" : "#" + reserved.id())
            .log(A.now(), A.minSec());
    }

    /**
     * Pure footprint intersection: two sites sharing a tile are the same
     * claim (touching edges are legal - packed bases rely on them). Pinned
     * - the [95,111]/[92,111] shared column must fail here instead of
     * reaching the engine.
     */
    public static boolean footprintsOverlap(
        int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh
    ) {
        return ax < bx + bw && bx < ax + aw
            && ay < by + bh && by < ay + ah;
    }

    private static boolean overlapsUnfinishedSite(APosition site, AUnitType type) {
        int w = type.getTilesWidth();
        int h = type.getTilesHeights();
        for (Construction other : ConstructionRequests.notFinished()) {
            APosition otherSite = other.buildPosition();
            if (otherSite == null) continue;
            AUnitType otherType = other.buildingType();
            if (otherType == null) continue;
            if (footprintsOverlap(
                site.tx(), site.ty(), w, h,
                otherSite.tx(), otherSite.ty(),
                otherType.getTilesWidth(), otherType.getTilesHeights()
            )) return true;
        }
        return false;
    }

    /**
     * Drop PREPARING sites whose order is gone or finished, so a cancelled
     * row cannot pin a tile forever. Runs with the refresh, same cadence.
     * Also clears dangling links: an order pointing at a construction that
     * is no longer listed (cancelled elsewhere) would otherwise skip both
     * the preparer (linked) and the handler (stale) forever.
     */
    public static void dropOrphanedPreparing() {
        for (Construction construction : ConstructionRequests.all()) {
            if (!construction.status().isWaiting()) {
                dropBuilderlessNotStarted(construction);
                continue;
            }
            ProductionOrder order = construction.productionOrder();
            if (order == null || order.isStatus(OrderStatus.FINISHED)) {
                // Named, not silent: a PREPARING site vanishing with no row
                // reads as "the pick never happened" (measured on OpenBW:
                // a dynamic Pylon prepared at [79,107] left one PREPARING
                // row and nothing else, while its tile got built by a
                // sibling order). The chain needs the drop to attribute it.
                EventLog.key(LogKey.CONSTRUCTION_CANCEL)
                    .param("type", construction.buildingType() == null ? "-"
                        : construction.buildingType().name())
                    .param("cid", construction.id())
                    .param("oid", order == null ? null : order.id())
                    .param("reason", order == null ? "orphan-drop: order gone" : "orphan-drop: order finished")
                    .param("tile", construction.buildPosition() == null ? "-"
                        : construction.buildPosition().toString())
                    .log(A.now(), A.minSec());
                ConstructionRequests.removeOrder(construction);
            }
        }

        atlantis.production.orders.production.queue.Queue queue =
            atlantis.production.orders.production.queue.Queue.get();
        if (queue == null) return;

        for (ProductionOrder order : queue.notFinished().list()) {
            Construction linked = order.construction();
            if (linked != null && !ConstructionRequests.constructions.contains(linked)) {
                order.setConstruction(null);
            }
        }
    }

    /**
     * The silent-death half of the fresh-request path (measured Wine
     * 7th_rav: Gateway At-10 requested a site at 1:08, the order was
     * consumed on the request, and the NOT_STARTED construction then
     * sat builderless with no advance path - the handler skips linked
     * non-PREPARING rows, refresh skips consumed ones, no twin fires).
     * A NOT_STARTED site with no live builder and a live order can never
     * advance on its own, so it re-enters the normal path visibly
     * (unlink + pristine row + named twin) instead of rotting: a
     * transient loss (dead builder) heals next refresh, a persistent one
     * reads as a retry loop, not silence. Orderless rows are not ours
     * to retry (V2 owns those); finished rows take the orphan-drop
     * above, never a retry.
     */
    private static void dropBuilderlessNotStarted(Construction construction) {
        if (construction.status() != ConstructionOrderStatus.NOT_STARTED) return;

        AUnit builder = construction.builder();
        // Archive knows dead even when the wrapper still reports hp > 0
        // (measured Wine: builder #126 dead 12:28, STARTED twin still
        // named it 12:35, warp froze with zero rows). A corpse reads
        // alive here without the archive check and the site rots silently.
        if (builder != null && builder.isAlive() && !UnitsArchive.isDestroyed(builder.id())) return;

        ProductionOrder order = construction.productionOrder();
        if (order == null || order.isStatus(OrderStatus.FINISHED)) return;

        EventLog.key(LogKey.CONSTRUCTION_CANCEL)
            .param("type", construction.buildingType() == null ? "-"
                : construction.buildingType().name())
            .param("cid", construction.id())
            .param("oid", order.id())
            .param("reason", builder == null ? "builderless-retry" : "builder-dead-retry")
            .param("tile", construction.buildPosition() == null ? "-"
                : construction.buildPosition().toString())
            .log(A.now(), A.minSec());
        ConstructionRequests.removeOrder(construction);
        order.setConstruction(null);
        order.resetRuntimeState();
    }
}
