package atlantis.production.constructions.position;

import atlantis.game.A;
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
            sharedPlanner = new CataloguePlacementPlanner();
            sharedPlanner.startPass();
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
        return answerWithTwin(siteForInner(planner, building, construction), building);
    }

    /**
     * Same, with the planner injected: the seam the tests use to hand over a
     * synthetic grid instead of the live map.
     */
    public static APosition siteFor(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        planner.startPass();
        return answerWithTwin(siteForInner(planner, building, construction), building);
    }

    private static APosition answerWithTwin(APosition site, AUnitType building) {
        if (site != null) {
            EventLog.key(LogKey.CATALOGUE_SITE)
                .param("type", building.name())
                .param("result", "answered")
                .param("tx", site.tx())
                .param("ty", site.ty())
                .log(AGame.now(), A.minSec());
        }
        return site;
    }

    private static APosition siteForInner(PlacementPlanner planner, AUnitType building, Construction construction) {
        if (construction == null || building == null) return null;
        if (building.isGasBuilding() || building.isBase()) {
            decline(building, "dedicated-finder");
            return null;
        }

        ProductionOrder order = construction.productionOrder();
        if (order == null) {
            decline(building, "no-order");
            return null;
        }

        if (!We.protoss()) {
            decline(building, "non-protoss");
            return null;
        }

        long start = System.nanoTime();
        PlacementReservation reservation = planner.reservePlacement(
            UnitProducible.of(building), constraintFor(order, construction), AGame.now()
        );
        adapterNanosTotal += System.nanoTime() - start;
        adapterCalls++;
        samplePerf(AGame.now());
        if (reservation == null || !reservation.isSuccessful()) {
            decline(building, "no-tile");
            return null;
        }
        return APosition.create(reservation.tileX(), reservation.tileY());
    }

    private static void decline(AUnitType building, String reason) {
        EventLog.key(LogKey.CATALOGUE_SITE)
            .param("type", building == null ? "-" : building.name())
            .param("result", "declined")
            .param("reason", reason)
            .log(AGame.now(), A.minSec());
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
