package atlantis.production.constructions.position;

import atlantis.config.env.Env;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.production.constructions.position.modifier.ModifierAnchor;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.placement.PlacementPlanner;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;
import atlantis.map.ReachableTiles;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * Legacy site search answered from the catalogue planner: two entries
 * (queue rows with orders, sited requests without), one shared core.
 *
 * <p>
 * The legacy pipeline ({@code Construction} → builders → refresh) keeps
 * owning builders, statuses and retries; only the "which tile" question is
 * delegated, through the same {@code PlacementPlanner} seam the v2
 * scheduler consumes. Anything out of scope - or any catalogue failure -
 * declines with null and the caller falls back to {@code FindPosition}, so
 * a refused catalogue answer can never do worse than today. Selected by
 * {@code PLACEMENT=catalogue}, default off.
 * </p>
 *
 * <p>
 * Scope (one place: {@code SiteScope}): Protoss generic buildings.
 * Diagnostics (one place: {@code SiteTwin}): every answer, decline and
 * perf sample as {@code CATALOGUE_SITE} rows.
 * </p>
 */
public final class CatalogueSiteAdapter {

    /**
     * One planner per game frame, shared by every site search in that frame.
     * A fresh planner rebuilds the whole-map grid + catalogue at
     * construction (measured 2026-10-18: per-search rebuilds made capped
     * runs unfinishable, 35 s wall for 208 game-s vs 6 s legacy). The
     * frame's first search pays one rebuild; later searches reuse the pass -
     * which additionally keeps two constructions off one tile, the same pass
     * semantics the v2 scheduler relies on. Same static-state shape as the
     * {@code APositionFinder} cache next door; single-threaded game loop.
     */
    private static CataloguePlacementPlanner sharedPlanner;
    private static int sharedPlannerFrame = -1;
    private CatalogueSiteAdapter() {
    }

    private static synchronized PlacementPlanner plannerForFrame(int frame) {
        if (sharedPlanner == null || sharedPlannerFrame != frame) {
            // The constructor already builds the grid + catalogue, so no
            // startPass here (it would build everything twice per frame).
            sharedPlanner = new CataloguePlacementPlanner();
            sharedPlannerFrame = frame;
            SiteTwin.addRebuild();
        }
        return sharedPlanner;
    }

    /**
     * A catalogue tile for this construction, or null when the catalogue
     * declines and the legacy search must answer. Uses the frame's shared
     * planner. Flag-gated inside: with the flag off this answers null and
     * the legacy path runs exactly as before (one home for the flag, both
     * entries, so the callers never branch on it).
     */
    public static APosition siteFor(AUnit builder, AUnitType building, Construction construction) {
        if (!Env.placementCatalogue()) return null;

        return answerWithTwin(siteForInner(plannerForFrame(AGame.now()), builder, building, construction),
            building, "funnel");
    }

    /**
     * Same, with the planner injected: the seam the tests use to hand over a
     * synthetic grid instead of the live map.
     */
    public static APosition siteFor(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        planner.startPass();
        return answerWithTwin(siteForInner(planner, builder, building, construction),
            building, "funnel");
    }

    /**
     * Sited requests ({@code RequestBuildingNear}: cannons, forward pylons)
     * search with no order and no construction yet. Same propose + validate
     * + range discipline, same null-means-legacy contract, selected by the
     * same flag. The caller keeps its validations, queueing and
     * construction afterwards, unchanged.
     */
    public static APosition siteForSited(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        if (!Env.placementCatalogue()) return null;

        return siteForSited(plannerForFrame(AGame.now()), builder, building, nearTo, maxDistance);
    }

    /**
     * Same, with the planner injected: the seam the tests use.
     */
    public static APosition siteForSited(PlacementPlanner planner, AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        String scope = SiteScope.declineReason(building);
        if (scope != null) {
            SiteTwin.declined(building, scope, "sited");

            return null;
        }
        if (building == null) return null;

        planner.startPass();
        TargetPlacement constraint = constraintForSited(nearTo);
        PlacementReservation reservation = propose(planner, builder, building, constraint, "sited");
        if (reservation == null) {
            return null;
        }
        APosition site = APosition.create(reservation.tileX(), reservation.tileY());
        if (!withinRange(site, nearTo, maxDistance)) {
            SiteTwin.declined(building, "too-far", "sited",
                constraint.nearTileX(), constraint.nearTileY());

            return null;
        }
        return answerWithTwin(site, building, "sited", reservation, constraint);
    }

    private static APosition siteForInner(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        if (construction == null || building == null) return null;

        String scope = SiteScope.declineReason(building);
        if (scope != null) {
            SiteTwin.declined(building, scope, "funnel");

            return null;
        }

        ProductionOrder order = construction.productionOrder();
        atlantis.production.demand.Demand demand = construction.demand();
        if (order == null && demand == null) {
            SiteTwin.declined(building, "no-order", "funnel");

            return null;
        }

        TargetPlacement constraint = demand != null
            ? constraintFor(demand, construction)
            : constraintFor(order, construction);
        PlacementReservation reservation = propose(planner, builder, building, constraint, "funnel");
        if (reservation == null) return null;

        return answerWithTwin(
            APosition.create(reservation.tileX(), reservation.tileY()), building, "funnel", reservation,
            constraint);
    }

    private static APosition answerWithTwin(APosition site, AUnitType building, String via) {
        return answerWithTwin(site, building, via, null, TargetPlacement.anywhere());
    }

    private static APosition answerWithTwin(APosition site, AUnitType building, String via,
                                            PlacementReservation reservation, TargetPlacement constraint) {
        SiteTwin.answered(building, site, via, reservation, constraint);

        return site;
    }

    /**
     * The shared core: builder check, catalogue proposal, legacy validation.
     * Returns the validated reservation or null (decline already twinned).
     */
    private static PlacementReservation propose(PlacementPlanner planner, AUnit builder, AUnitType building, TargetPlacement constraint, String via) {
        // The catalogue proposes, the legacy validator disposes: a tile the
        // builder-time checks refuse would start a builder, fail refresh and
        // re-request in a loop (measured 2026-10-18: 236 requests vs 15 on
        // the control for the same opening). Decline instead and let the
        // legacy search answer; subsuming these checks is a later slice with
        // its own A/B proof.
        if (builder == null || !builder.isAlive()) {
            SiteTwin.declined(building, "no-builder", via, constraint.nearTileX(), constraint.nearTileY());

            return null;
        }

        PlacementReservation reservation = SiteTwin.timed(() -> planner.reservePlacement(
            UnitProducible.of(building), constraint, AGame.now()
        ));
        for (atlantis.placement.core.SupportRequest support : planner.drainSupportRequests()) {
            AddToQueue.withHighPriority(
                support.unitType(),
                APosition.create(support.tileX(), support.tileY()));
        }
        if (reservation == null || !reservation.isSuccessful()) {
            SiteTwin.declined(building, "no-tile", via, constraint.nearTileX(), constraint.nearTileY());

            return null;
        }

        APosition site = APosition.create(reservation.tileX(), reservation.tileY());
        // V3 S1: one verdict owns refused / power-at-footprint-center /
        // geyser-minerals / worker-cover, so the funnel can never serve a
        // tile the legacy spiral would decline. Physics (occupancy, engine
        // canBuildHere, early allowances) stays with CanPhysicallyBuildHere
        // below - the verdict answers "should we", physics answers "could we".
        SitingVerdict verdict = SitingVerdict.check(building, site);
        if (!verdict.accepted()) {
            SiteTwin.declined(building, verdict.twin, via, constraint.nearTileX(), constraint.nearTileY());

            return null;
        }
        if (!CanPhysicallyBuildHere.check(builder, building, site)) {
            SiteTwin.declined(building, "legacy-refused", via, constraint.nearTileX(), constraint.nearTileY());

            return null;
        }
        // Owner 2026-11-01: the main base must reach the site by ground.
        // Last and once per proposal (reachability is the costly query):
        // a disconnected region (UMS/island pocket) declines to the legacy
        // spiral instead of stranding the builder. Skipped when no base
        // stands yet (first frame) - there is nothing to measure from.
        AUnit main = Select.mainOrAnyBuilding();
        if (main != null && main.position() != null) {
            if (!ReachableTiles.between(main.position(), site)) {
                SiteTwin.declined(building, "unreachable", via, constraint.nearTileX(), constraint.nearTileY());

                return null;
            }
        }
        return reservation;
    }

    /**
     * The legacy ring search stays within maxDistance of near; a catalogue
     * answer beyond it would queue an exact order somewhere the caller never
     * asked for. Decline instead (negative means unbounded, as in legacy).
     * Public as a seam: pure range math, pinned directly.
     */
    public static boolean withinRange(APosition site, HasPosition nearTo, double maxDistance) {
        if (site == null || nearTo == null || maxDistance < 0) return true;

        return site.distTo(nearTo) <= maxDistance;
    }

    /**
     * The order's request as a catalogue constraint: an exact tile stays
     * exact (the planner re-validates it), a neighbourhood hint stays a
     * neighbourhood, and no hint means anywhere. Public as a seam: the
     * translation is pinned directly, without map physics.
     */
    public static TargetPlacement constraintFor(ProductionOrder order, Construction construction) {
        if (order.isUsingExactPosition() && order.aroundPosition() != null) {
            APosition exact = APosition.create(order.aroundPosition());

            return TargetPlacement.exactTile(exact.tx(), exact.ty());
        }
        // Migration A4b (owner #146): legacy order modifiers (@NATURAL,
        // @MAIN, choke names...) anchor the neighbourhood instead of
        // degrading to anywhere. The spiral used to re-derive these per
        // search; the catalogue ranks around the same anchor.
        // Legacy precedence (A4g): an explicitly set nearTo wins over the
        // generic modifier - the modifier names a neighbourhood class,
        // nearTo names the tile. Same order as the spiral used.
        HasPosition anchor = construction.nearTo() != null
            ? construction.nearTo()
            : ModifierAnchor.resolve(order.getModifier());
        if (anchor != null) {
            APosition centre = APosition.create(anchor);

            return TargetPlacement.inNeighbourhood(centre.tx(), centre.ty());
        }
        return TargetPlacement.anywhere();
    }

    /**
     * The demand's request as a catalogue constraint (NEXT #228, WO-147):
     * same translation as the order above, read from the v2 carrier.
     * The funnel prefers this overload; the order overload stays for the
     * legacy path and its pins.
     */
    public static TargetPlacement constraintFor(atlantis.production.demand.Demand demand,
                                                Construction construction) {
        if (demand.isExact() && demand.around() != null) {
            APosition exact = APosition.create(demand.around());

            return TargetPlacement.exactTile(exact.tx(), exact.ty());
        }
        // Same modifier anchoring as the order overload (A4b), same
        // legacy precedence (A4g): explicit nearTo wins.
        HasPosition anchor = construction.nearTo() != null
            ? construction.nearTo()
            : ModifierAnchor.resolve(demand.modifier());
        if (anchor != null) {
            APosition centre = APosition.create(anchor);

            return TargetPlacement.inNeighbourhood(centre.tx(), centre.ty());
        }
        return TargetPlacement.anywhere();
    }

    private static TargetPlacement constraintForSited(HasPosition nearTo) {
        if (nearTo == null) return TargetPlacement.anywhere();

        APosition centre = APosition.create(nearTo);

        return TargetPlacement.inNeighbourhood(centre.tx(), centre.ty());
    }
}
