package atlantis.production.constructions.position;

import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.PlacementPlanner;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

/**
 * Phase 4 slice 1: answers the legacy site search from the catalogue planner.
 *
 * <p>
 * The legacy pipeline ({@code Construction} → {@code APositionFinder}) keeps
 * owning builders, statuses and retries; only the "which tile" question is
 * delegated, through the same {@code PlacementPlanner} seam the v2 scheduler
 * already consumes. Strangler shape: the legacy side consumes the new
 * implementation, so the old finder tree can shrink without the pipeline
 * changing.
 * </p>
 *
 * <p>
 * Scope, deliberately narrow: Protoss generic buildings only. Gas buildings
 * and bases keep their dedicated finders (exact geyser tile, base
 * locations), Terran/Zerg stay on the legacy path (their catalogue
 * strategies are stubs, D8 keeps the bot Protoss-only). Anything outside
 * the scope - or any catalogue failure - is declined with null and the
 * caller falls back to {@code FindPosition}, so a refused catalogue answer
 * can never do worse than today.
 * </p>
 *
 * <p>
 * Selected by {@code PLACEMENT=catalogue}, default off. The branch itself is
 * four lines of plumbing with no test (the flag reads false under
 * {@code Env.isTesting()}, so no test can reach it); the game run in slice 2
 * is its proof.
 * </p>
 */
public final class CatalogueSiteAdapter {

    private CatalogueSiteAdapter() {
    }

    /**
     * A catalogue tile for this construction, or null when the catalogue
     * declines (out of scope, or no tile) and the legacy search must answer.
     */
    public static APosition siteFor(AUnit builder, AUnitType building, Construction construction) {
        return siteFor(new CataloguePlacementPlanner(), builder, building, construction);
    }

    /**
     * Same, with the planner injected: the seam the tests use to hand over a
     * synthetic grid instead of the live map.
     */
    public static APosition siteFor(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        if (construction == null || building == null) return null;
        if (building.isGasBuilding() || building.isBase()) return null;

        ProductionOrder order = construction.productionOrder();
        if (order == null) return null;

        if (!We.protoss()) return null;

        planner.startPass();
        PlacementReservation reservation = planner.reservePlacement(
            UnitProducible.of(building), constraintFor(order, construction), AGame.now()
        );
        if (reservation == null || !reservation.isSuccessful()) return null;
        return APosition.create(reservation.tileX(), reservation.tileY());
    }

    /**
     * The order's request as a catalogue constraint: an exact tile stays
     * exact (the planner re-validates it), a neighbourhood hint stays a
     * neighbourhood, and no hint means anywhere.
     */
    static TargetPlacement constraintFor(ProductionOrder order, Construction construction) {
        if (order.isUsingExactPosition() && order.aroundPosition() != null) {
            APosition exact = APosition.create(order.aroundPosition());
            return TargetPlacement.exactTile(exact.tx(), exact.ty());
        }
        HasPosition near = construction.nearTo();
        if (near != null) {
            APosition centre = APosition.create(near);
            return TargetPlacement.inNeighbourhood(centre.tx(), centre.ty());
        }
        return TargetPlacement.anywhere();
    }
}
