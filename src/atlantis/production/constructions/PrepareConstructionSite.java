package atlantis.production.constructions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.DefineExactPositionForNewConstruction;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.AConsole;

/**
 * Owner 2026-10-14: pick the site one supply before the order unlocks, so
 * the building is aimed before it is affordable. A PREPARING construction
 * holds the reserved site with no builder and no issue; ProduceBuilding
 * adopts it (builder + NOT_STARTED) once the order is ready instead of
 * searching anew. While poor or supply-gated the site sits marked without
 * costing a worker - and a placement that can never work shows up here,
 * at pick time, instead of deep in the builder loop.
 *
 * <p>v1 scope: ordinary buildings only. Bases (special natural logic) and
 * gas buildings (fixed geyser tile, nothing to pick) stay on the old path.
 * One site in flight per type; staleness is bounded because picking happens
 * at most ~1 supply ahead, and RefreshConstructionPosition keeps running.
 */
public class PrepareConstructionSite {

    public static void prepareIfDue(ProductionOrder order) {
        if (order == null || !order.isBuilding()) return;
        if (order.construction() != null) return;

        AUnitType type = order.unitType();
        if (type == null || type.isBase() || type.isGasBuilding()) return;

        // One supply earlier (owner spec): Pylon at 8 is picked at 7. Past
        // that the site may sit reserved while poor or supply-gated.
        if (A.supplyUsed() < order.minSupply() - 1) return;

        // One site in flight per type - the second Gateway row prepares once
        // the first leaves the unfinished list.
        if (!ConstructionRequests.notFinishedOfType(type).isEmpty()) return;

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
        ConstructionRequests.addConstruction(prepared);
        AConsole.println(
            A.minSec() + ": PREPARING " + type.name()
                + " at [" + site.tx() + "," + site.ty() + "] for supply " + order.minSupply());
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
            if (construction.status() != ConstructionOrderStatus.PREPARING) continue;
            ProductionOrder order = construction.productionOrder();
            if (order == null || order.isStatus(OrderStatus.FINISHED)) {
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
}
