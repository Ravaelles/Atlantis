package atlantis.production.constructions.position;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.PlacementPlanner;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

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
     * One planner per game frame, shared by every site search in that frame.
     * A fresh planner rebuilds the whole-map grid + catalogue at
     * construction, which made capped runs unfinishable (measured
     * 2026-10-18: 35 s wall for 208 game-s with catalogue vs 6 s for 306
     * game-s legacy on the same opening). The frame's first search pays the
     * rebuild via {@code startPass()}; later searches reuse the pass - which
     * additionally keeps two constructions off one tile, the same pass
     * semantics the v2 scheduler relies on. Same static-state shape as the
     * {@code APositionFinder} cache next door; single-threaded game loop.
     */
    private static CataloguePlacementPlanner sharedPlanner;
    private static int sharedPlannerFrame = -1;
    private static int sharedPlannerRebuilds = 0;
    private static long adapterNanosTotal = 0;
    private static int adapterCalls = 0;
    private static int lastPerfSample = -1;

    private static synchronized PlacementPlanner plannerForFrame(int frame) {
        if (sharedPlanner == null || sharedPlannerFrame != frame) {
            // The constructor already builds the grid + catalogue, so no
            // startPass here (it would build everything twice per frame).
            sharedPlanner = new CataloguePlacementPlanner();
            sharedPlannerFrame = frame;
            sharedPlannerRebuilds++;
        }
        return sharedPlanner;
    }

    private static void samplePerf(int frame) {
        int bucket = frame / 720;
        if (bucket == lastPerfSample) return;
        lastPerfSample = bucket;
        if (adapterCalls == 0) return;
        EventLog.key(LogKey.CATALOGUE_SITE)
            .param("type", "-")
            .param("result", "perf")
            .param("calls", adapterCalls)
            .param("avgMicros", (int) (adapterNanosTotal / 1000 / adapterCalls))
            .param("rebuilds", sharedPlannerRebuilds)
            .log(frame, A.minSec());
    }

    /**
     * A catalogue tile for this construction, or null when the catalogue
     * declines (out of scope, or no tile) and the legacy search must answer.
     * Uses the frame's shared planner (one grid+catalogue rebuild per frame).
     */
    public static APosition siteFor(AUnit builder, AUnitType building, Construction construction) {
        long start = System.nanoTime();
        PlacementPlanner planner = plannerForFrame(AGame.now());
        adapterNanosTotal += System.nanoTime() - start;
        return answerWithTwin(siteForInner(planner, builder, building, construction), building, "funnel");
    }

    /**
     * Same, with the planner injected: the seam the tests use to hand over a
     * synthetic grid instead of the live map.
     */
    public static APosition siteFor(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        planner.startPass();
        return answerWithTwin(siteForInner(planner, builder, building, construction), building, "funnel");
    }

    /**
     * Sited requests ({@code RequestBuildingNear}: cannons, forward pylons)
     * search with no order and no construction yet, straight at legacy
     * {@code FindPosition} - bypassing every catalogue consumer, including
     * the coverage ranker. This is their funnel entry: same propose +
     * validate + range discipline, same null-means-legacy contract, selected
     * by the same flag. The caller keeps its validations, queueing and
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
        if (constructionFreeDeclined(builder, building)) {
            return null;
        }
        planner.startPass();
        PlacementReservation reservation = propose(planner, builder, building, constraintForSited(nearTo), "sited");
        if (reservation == null) {
            return null;
        }
        APosition site = APosition.create(reservation.tileX(), reservation.tileY());
        if (!withinRange(site, nearTo, maxDistance)) {
            decline(building, "too-far", "sited");
            return null;
        }
        return answerWithTwin(site, building, "sited", reservation);
    }

    /** Declines needing no order: nulls, dedicated finders, wrong race. True means declined. */
    private static boolean constructionFreeDeclined(AUnit builder, AUnitType building) {
        if (building == null) return true;
        if (building.isGasBuilding() || building.isBase()) {
            decline(building, "dedicated-finder", "sited");
            return true;
        }
        if (!We.protoss()) {
            decline(building, "non-protoss", "sited");
            return true;
        }
        if (builder == null || !builder.isAlive()) {
            decline(building, "no-builder", "sited");
            return true;
        }
        return false;
    }

    private static TargetPlacement constraintForSited(HasPosition nearTo) {
        if (nearTo == null) return TargetPlacement.anywhere();
        APosition centre = APosition.create(nearTo);
        return TargetPlacement.inNeighbourhood(centre.tx(), centre.ty());
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

    private static APosition answerWithTwin(APosition site, AUnitType building, String via) {
        return answerWithTwin(site, building, via, null);
    }

    private static APosition answerWithTwin(APosition site, AUnitType building, String via, PlacementReservation reservation) {
        if (site != null) {
            EventLog.Builder row = EventLog.key(LogKey.CATALOGUE_SITE)
                .param("type", building.name())
                .param("result", "answered")
                .param("tx", site.tx())
                .param("ty", site.ty())
                .param("via", via);
            if (reservation != null && reservation.coversChoke() != null) {
                row.param("coversChoke", reservation.coversChoke());
                row.param("coversAnchor", reservation.coversAnchor());
            }
            row.log(AGame.now(), A.minSec());
        }
        return site;
    }

    private static APosition siteForInner(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        if (construction == null || building == null) return null;
        if (building.isGasBuilding() || building.isBase()) {
            decline(building, "dedicated-finder", "funnel");
            return null;
        }

        ProductionOrder order = construction.productionOrder();
        if (order == null) {
            decline(building, "no-order", "funnel");
            return null;
        }

        if (!We.protoss()) {
            decline(building, "non-protoss", "funnel");
            return null;
        }

        PlacementReservation reservation = propose(planner, builder, building, constraintFor(order, construction), "funnel");
        if (reservation == null) return null;
        return answerWithTwin(
            APosition.create(reservation.tileX(), reservation.tileY()), building, "funnel", reservation);
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
            decline(building, "no-builder", via);
            return null;
        }

        long start = System.nanoTime();
        PlacementReservation reservation = planner.reservePlacement(
            UnitProducible.of(building), constraint, AGame.now()
        );
        adapterNanosTotal += System.nanoTime() - start;
        adapterCalls++;
        samplePerf(AGame.now());
        if (reservation == null || !reservation.isSuccessful()) {
            decline(building, "no-tile", via);
            return null;
        }

        APosition site = APosition.create(reservation.tileX(), reservation.tileY());
        if (!CanPhysicallyBuildHere.check(builder, building, site)) {
            decline(building, "legacy-refused", via);
            return null;
        }
        return reservation;
    }

    private static void decline(AUnitType building, String reason, String via) {
        EventLog.key(LogKey.CATALOGUE_SITE)
            .param("type", building == null ? "-" : building.name())
            .param("result", "declined")
            .param("reason", reason)
            .param("via", via)
            .log(AGame.now(), A.minSec());
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
}
