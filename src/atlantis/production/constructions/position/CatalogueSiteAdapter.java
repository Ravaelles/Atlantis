package atlantis.production.constructions.position;

import atlantis.config.env.Env;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.PlacementPlanner;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

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

    private CatalogueSiteAdapter() {
    }

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
        PlacementReservation reservation = propose(planner, builder, building, constraintForSited(nearTo), "sited");
        if (reservation == null) {
            return null;
        }
        APosition site = APosition.create(reservation.tileX(), reservation.tileY());
        if (!withinRange(site, nearTo, maxDistance)) {
            SiteTwin.declined(building, "too-far", "sited");
            return null;
        }
        return answerWithTwin(site, building, "sited", reservation);
    }

    private static APosition siteForInner(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        if (construction == null || building == null) return null;
        String scope = SiteScope.declineReason(building);
        if (scope != null) {
            SiteTwin.declined(building, scope, "funnel");
            return null;
        }

        ProductionOrder order = construction.productionOrder();
        if (order == null) {
            SiteTwin.declined(building, "no-order", "funnel");
            return null;
        }

        PlacementReservation reservation = propose(planner, builder, building, constraintFor(order, construction), "funnel");
        if (reservation == null) return null;
        return answerWithTwin(
            APosition.create(reservation.tileX(), reservation.tileY()), building, "funnel", reservation);
    }

    private static APosition answerWithTwin(APosition site, AUnitType building, String via) {
        return answerWithTwin(site, building, via, null);
    }

    private static APosition answerWithTwin(APosition site, AUnitType building, String via, PlacementReservation reservation) {
        SiteTwin.answered(building, site, via, reservation);
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
            SiteTwin.declined(building, "no-builder", via);
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
            SiteTwin.declined(building, "no-tile", via);
            return null;
        }

        APosition site = APosition.create(reservation.tileX(), reservation.tileY());
        if (!CanPhysicallyBuildHere.check(builder, building, site)) {
            SiteTwin.declined(building, "legacy-refused", via);
            return null;
        }
        // Power is not part of the physical check above, and the catalogue's
        // availability answer can go stale (a tile "powered" by a pylon that
        // never warped): measured 2026-10-18, a covering cannon tile held its
        // builder 122 s on unpowered ground, then cancelled. A dark tile is
        // declined now and re-answered when a pylon lands - just in time,
        // like the legacy chain, which refuses unpowered tiles at search.
        if (building.needsPower() && !IsPoweredByAPylon.check(site, building)) {
            SiteTwin.declined(building, "unpowered", via);
            return null;
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
        HasPosition near = construction.nearTo();
        if (near != null) {
            APosition centre = APosition.create(near);
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
