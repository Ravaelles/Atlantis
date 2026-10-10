package atlantis.production.v2.engine;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.UnitProducible;

/**
 * The bridge from the pending legacy {@link Construction} requests back into a
 * v2
 * {@link ProductionPlan}.
 *
 * <p>
 * The plan is recomputed from scratch every frame, so a construction whose
 * builder died en route would otherwise be lost with the previous plan. This
 * adapter re-offers every construction that is already placed but not started,
 * as
 * a committed item - and it is idempotent on the tile: the same building type
 * at
 * a <b>different</b> tile is a second request, not a duplicate, so both are
 * kept
 * (STATE.md #48: de-duping on the type alone silently dropped a second Gateway
 * while the first was still a request).
 * </p>
 *
 * <p>
 * It owns no state and is a pure function of the construction queue, the plan
 * and
 * the frame, which is what keeps it unit-testable without a game.
 * </p>
 */
public final class CommittedConstructions {

    private CommittedConstructions() {
    }

    /** {@code plan} plus every pending construction it does not already carry. */
    public static ProductionPlan mergeInto(ProductionPlan plan, int frame) {
        ProductionPlan merged = new ProductionPlan();
        for (ProductionItem item : plan.items()) {
            merged.add(item);
        }

        for (Construction construction : ConstructionRequests.constructions) {
            if (construction.hasStarted() || construction.buildingType() == null)
                continue;
            if (construction.buildPosition() == null)
                continue;

            int tileX = construction.buildPosition().tx();
            int tileY = construction.buildPosition().ty();
            UnitProducible producible = UnitProducible.of(construction.buildingType());
            if (alreadyOffered(merged, producible, tileX, tileY))
                continue;

            PlacementReservation placement = PlacementReservation
                    .success(tileX, tileY, frame)
                    .committedAt(frame);
            merged.add(new ProductionItem(producible, construction.timeOrdered(), false, placement));
        }

        return merged;
    }

    /**
     * True when the plan already carries this building at this tile. Compared on
     * type AND tile: the reservation's tile is the identity of a construction
     * request, while the type alone is not - several requests of one type may be
     * pending at distinct tiles, and it is exactly the tile that
     * {@code GameOrderDirector.buildAt} de-dupes on.
     */
    private static boolean alreadyOffered(ProductionPlan plan, UnitProducible producible, int tileX, int tileY) {
        for (ProductionItem item : plan.items()) {
            if (!item.item().id().equals(producible.id()))
                continue;

            PlacementReservation placement = item.placement();
            if (placement != null && placement.tileX() == tileX && placement.tileY() == tileY) {
                return true;
            }
        }
        return false;
    }

}
