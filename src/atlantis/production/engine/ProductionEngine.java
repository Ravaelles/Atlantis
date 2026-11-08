package atlantis.production.engine;
import atlantis.production.execution.DispatchResult;
import atlantis.production.execution.GameOrderDirector;
import atlantis.production.placement.PlacementPlanner;
import atlantis.production.execution.ProductionDispatcher;
import atlantis.production.demand.ProductionGoal;
import atlantis.production.scheduling.ProductionItem;
import atlantis.production.scheduling.ProductionPlan;
import atlantis.production.policy.ProductionRacePolicy;
import atlantis.production.scheduling.ProductionScheduler;
import atlantis.production.scheduling.ResourceTimeline;
import atlantis.production.execution.V2DispatchTwin;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.placement.core.ReservedSlots;
import atlantis.placement.core.SupportRequest;
import atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding;
import atlantis.production.constructions.position.conditions.OverlapsOurUnfinishedBuilding;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.demand.*;
import atlantis.production.execution.*;
import atlantis.production.placement.*;
import atlantis.production.policy.*;
import atlantis.production.producibles.*;
import atlantis.production.scheduling.*;
import atlantis.production.demand.BuildOrderGoals;
import atlantis.production.demand.BuildOrderRow;
import atlantis.units.AUnitType;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The composition root of production-v2: builds this frame's inputs from the
 * game, runs the stateless pipeline, and hands the result to the dispatcher
 * (M4/M5 of _AI/redesign/01_PRODUCTION.md).
 *
 * <p>
 * One frame, in full:
 * <ol>
 * <li>snapshot the game (stocks, mining rate, facilities, supply in
 * production);</li>
 * <li>collect goals from the build order and the dynamic generators;</li>
 * <li>schedule them over a fresh resource timeline - the previous frame's plan
 * is gone and is not consulted;</li>
 * <li>issue whatever is due inside the latency window.</li>
 * </ol>
 * </p>
 *
 * <p>
 * The three modes come from {@code PRODUCTION_V2} in ENV. In
 * {@link atlantis.config.env.ProductionV2Mode#DRY_RUN} the pipeline runs on
 * real
 * game state and only records what it would order - the comparison step that
 * decides whether the cutover is safe. In
 * {@link atlantis.config.env.ProductionV2Mode#LIVE} it issues the commands.
 * Nothing else in the codebase asks which mode is on: the legacy pipeline does
 * not check it, and this class never edits a flag mid-game.
 * </p>
 *
 * <p>
 * Deliberately <b>not</b> a {@code Commander} in the legacy sense: it owns no
 * unit state and adds no per-unit managers. It is called once per frame from
 * the production commander, and it is the only class in v2 that knows the game
 * exists - apart from the two thin adapters it delegates to
 * ({@link GameStateSnapshot} and {@link GameOrderDirector}).
 * </p>
 */
public final class ProductionEngine {

    /**
     * Latency margin for issuing commands; BWAPI drops a command sent too late.
     */
    private static final int LATENCY_FRAMES = 3;

    /**
     * Which placement implementation answers the seam: the catalogue
     * planner, full stop (V3 single door - the legacy spiral was cut
     * slice by slice, last caller gone 2026-11-06).
     */
    private final PlacementPlanner placementPlanner = createPlacementPlanner();
    private final GameOrderDirector gameDirector = new GameOrderDirector();
    /**
     * Race identities for goal generation, injected once (NEXT #8).
     */
    private final ProductionRacePolicy racePolicy;
    private ProductionPlan lastPlan = new ProductionPlan();

    /**
     * Ambient race (tests, legacy callers).
     */
    public ProductionEngine() {
        this(ProductionRacePolicy.ambient());
    }

    public ProductionEngine(ProductionRacePolicy racePolicy) {
        this.racePolicy = racePolicy;
    }

    private static PlacementPlanner createPlacementPlanner() {
        return new atlantis.placement.engine.CataloguePlacementPlanner();
    }

    /**
     * Frame of the next update, for the comparison log.
     */
    public static int now() {
        return A.now();
    }

    /**
     * Runs one frame of the engine. Returns the plan it computed, which is also
     * what the dry-run log reports.
     */
    public ProductionPlan updateFrame() {
        GameStateSnapshot state = new GameStateSnapshot(racePolicy);

        ResourceTimeline timeline = state.buildTimeline();
        List<ProductionGoal> goals = collectGoals(state, timeline);

        // Goal bands in input order (NEXT #52 watch): the slide is back under
        // LIVE despite PRIORITY_OPENING_SUPPLY, so the next measurement needs
        // each goal's band next to its item - whether the Pylon is classified
        // opening-supply and whether the band decides allocation order.
        // BuilderLog-gated and throttled; the plan line itself stays as is.
        if (Env.builderLog() && A.everyNthGameFrame(30)) {
            StringBuilder bands = new StringBuilder("V2_GOALS @" + state.frame());
            for (ProductionGoal goal : goals) {
                bands.append(" [").append(goal.item().id())
                    .append(":").append(goal.priority()).append(']');
            }
            System.err.println(bands);
        }

        ProductionScheduler scheduler = new ProductionScheduler(
            state.facilityRegistry(), placementPlanner, state.existingItems());
        ProductionPlan plan = scheduler.schedule(goals, timeline);
        plan = CommittedConstructions.mergeInto(plan, state.frame());
        lastPlan = plan;

        forwardPlannedSupport();

        gameDirector.startFrame();
        List<DispatchResult> issued = new ProductionDispatcher(gameDirector).dispatch(plan, goals, state.frame(), LATENCY_FRAMES);

        logFrame(state.frame(), plan, issued);

        return plan;
    }

    /**
     * V3 single door, support half (owner order 2026-11-06): the planner
     * declares power support as data during reservePlacement, and this
     * composition root forwards each declaration to production - the V2
     * heartbeat (no-placement every 600f, zero Gateways live
     * GAME_120B0292) proved declarations without forwarding are a
     * silent stall. Each support Pylon goes to the covering free
     * Pylon slot (ReservedSlots), validated by exact reservation,
     * affordable only, pylon-cap respected; a refused repeat twins
     * nothing.
     */
    private void forwardPlannedSupport() {
        forwardDrainedSupport(placementPlanner, gameDirector, A.now());
    }

    /**
     * Testable seam: forwarding is a pure function of the buffered
     * support, the planned layout, the bank and the pylon cap - no
     * economy sim needed to pin it (CONVENTIONS §10: decisions pin
     * pure, the sim proves the wiring once per capability).
     */
    public static void forwardDrainedSupport(PlacementPlanner planner, GameOrderDirector director, int now) {
        java.util.List<SupportRequest> drained;
        try {
            drained = planner.drainSupportRequests();
        } catch (Exception | Error ignored) {
            return;
        }
        if (drained == null || drained.isEmpty()) return;

        for (SupportRequest support : drained) {
            try {
                forwardOneSupport(planner, director, support, now);
            } catch (Exception | Error ignored) {
            }
        }
    }

    /**
     * Throttled witness for silent support drops (one row per reason per
     * 600f, same heartbeat as the scheduler refusals): the support Pylon
     * unblocks the Gateway slot, and every drop path used to twin nothing -
     * the next drought would have needed a diagnostic build to name its
     * gate. Routine absorbs (cap, live row, short bank) show as rare
     * chatter; layout gaps (no-slot, exact-fail) shout by repetition.
     */
    private static final java.util.Map<String, Integer> supportDropLastTwin = new java.util.HashMap<>();

    private static void twinSupportDrop(String reason, SupportRequest support, APosition pylonTile,
                                        int now) {
        try {
            Integer last = supportDropLastTwin.get(reason);
            if (last != null && now - last < 600) return;
            supportDropLastTwin.put(reason, now);

            EventLog.key(LogKey.CATALOGUE_SITE)
                .param("type", demanderName(support))
                .param("result", "support-dropped")
                .param("reason", reason)
                .param("tx", support == null ? -1 : support.tileX())
                .param("ty", support == null ? -1 : support.tileY())
                .param("via", "slot-planner")
                .param("supportTx", pylonTile == null ? -1 : pylonTile.tx())
                .param("supportTy", pylonTile == null ? -1 : pylonTile.ty())
                .log(now, A.minSec());
        } catch (Exception | Error ignored) {
        }
    }

    /**
     * Who asked for the support Pylon (owner 2026-11-08: a Cannon -
     * Gateways never order Pylons). Short human name for the twin;
     * unknown or legacy requests without a demander read Gateway.
     */
    private static String demanderName(SupportRequest support) {
        try {
            String id = support == null ? null : support.demanderTypeId();
            if (id != null) {
                if (id.contains("Cannon")) return "Cannon";
                if (id.contains("Gateway")) return "Gateway";
                if (id.contains("Nexus")) return "Nexus";
                if (id.startsWith("Protoss_")) return id.substring("Protoss_".length());
                return id;
            }
        } catch (Exception | Error ignored) {
        }

        return "Gateway";
    }

    private static void forwardOneSupport(PlacementPlanner planner, GameOrderDirector director, SupportRequest support, int now) {
        if (support == null || support.unitType() == null) return;
        if (!support.unitType().isPylon()) return;

        APosition gatewayTile = APosition.create(support.tileX(), support.tileY());
        APosition pylonTile = ReservedSlots.supportPylonSlotFor(
            gatewayTile, support.unitType(),
            (tile, type) ->
                OverlapsOurStandingBuilding.check(tile, type)
                    || OverlapsOurUnfinishedBuilding.check(tile, type));
        if (pylonTile == null) {
            twinSupportDrop("no-slot", support, null, now);
            return;
        }

        // Same doors as a normal mint: no overdraft, no flood past the cap,
        // no twin for a re-offer the live row already absorbs.
        if (!A.canAfford(support.unitType())) {
            twinSupportDrop("unaffordable", support, pylonTile, now);
            return;
        }
        if (atlantis.production.requests.produce.ProduceBuilding.tooManyPylonsInFlight()) {
            twinSupportDrop("pylon-cap", support, pylonTile, now);
            return;
        }
        if (hasLiveConstruction(support.unitType(), pylonTile)) {
            twinSupportDrop("live-row", support, pylonTile, now);
            return;
        }

        PlacementReservation reservation = planner.reservePlacement(
            UnitProducible.of(support.unitType()),
            TargetPlacement.exactTile(pylonTile.tx(), pylonTile.ty()),
            now);
        if (reservation == null || !reservation.isSuccessful()) {
            twinSupportDrop("exact-fail:" + planner.lastRefusal(), support, pylonTile, now);
            return;
        }

        boolean ok = director.buildAt(
            UnitProducible.of(support.unitType()), reservation, A.supplyUsed());
        if (!ok) {
            twinSupportDrop("buildat-false", support, pylonTile, now);
            return;
        }

        EventLog.key(LogKey.CATALOGUE_SITE)
            .param("type", demanderName(support))
            .param("result", "supported")
            .param("tx", gatewayTile.tx())
            .param("ty", gatewayTile.ty())
            .param("via", "slot-planner")
            .param("support", support.unitType().name())
            .param("supportTx", pylonTile.tx())
            .param("supportTy", pylonTile.ty())
            .log(now, A.minSec());
    }

    /**
     * A live row on the same tile already owns this demand: the re-offer
     * is absorbed, never re-minted, never re-twinned (mirrors the
     * same-tile dedupe inside buildAt).
     */
    private static boolean hasLiveConstruction(AUnitType type, APosition tile) {
        try {
            for (atlantis.production.constructions.Construction construction :
                atlantis.production.constructions.ConstructionRequests.constructions) {
                if (construction == null || construction.buildingType() != type
                    || construction.buildPosition() == null) continue;
                if (construction.buildPosition().tx() == tile.tx()
                    && construction.buildPosition().ty() == tile.ty()) {
                    return true;
                }
            }
        } catch (Exception | Error ignored) {
        }

        return false;
    }

    /**
     * One line per frame with work in it: the plan, and what actually left.
     * Builder-log-gated like the goal bands above (the twin already carries
     * this structurally every frame) - an unconditional per-minute line is
     * noise in every game and every sim run.
     */
    private void logFrame(int frame, ProductionPlan plan, List<DispatchResult> issued) {
        if (issued.isEmpty()) return;

        V2DispatchTwin.emit(frame, issued);
        if (!Env.builderLog()) return;

        StringBuilder log = new StringBuilder("PRODUCTION_V2 @@" + frame + " plan=" + plan.size() + " [");
        for (ProductionItem item : plan.items()) log.append(item).append(' ');
        log.append("] issued:");
        for (DispatchResult result : issued) log.append(' ').append(result);

        ErrorLog.printMaxOncePerMinute(log.toString());
    }

    /**
     * Goals of this frame: the opening from the build order, plus the dynamic
     * generators. They are all declarations; the scheduler resolves the
     * contention between them by priority and by time.
     */
    private List<ProductionGoal> collectGoals(GameStateSnapshot state, ResourceTimeline timeline) {
        List<ProductionGoal> goals = new ArrayList<>();

        List<BuildOrderRow> buildOrderRows = buildOrderRows();
        if (!buildOrderRows.isEmpty()) {
            goals.addAll(state.buildOrderGoals(buildOrderRows));
        }

        goals.addAll(state.dynamicGoals(buildOrderOwnsProvider(buildOrderRows)));

        // Pull-forward rules (01_PRODUCTION.md step 3): supply and gas structures
        // are wanted a little before they are needed, not after the block.
        goals.addAll(state.pullForwardGoals(timeline));
        Collections.sort(goals);

        // WO-144 open question: a duplicate Pylon pair reads as BO-row
        // re-fire, pipeline overlap, or a door hole - the plan-time source
        // column decides. Providers only; everything else would be noise.
        atlantis.production.demand.V2GoalSourceTwin.emit(
            state.frame(),
            goals,
            atlantis.production.producibles.UnitProducible.of(racePolicy.supplyProviderType()).id());

        return goals;
    }

    static List<BuildOrderRow> buildOrderRows() {
        if (CurrentBuildOrder.get() == null)
            return Collections.emptyList();

        return BuildOrderRow.fromLegacy(CurrentBuildOrder.get().productionOrders());
    }

    /**
     * True while the build order still holds an unproduced supply-provider
     * row: then the row is the opening's single provider author and the
     * preventive supply fire stays off (the emergency fire never defers).
     */
    private boolean buildOrderOwnsProvider(List<BuildOrderRow> rows) {
        if (rows.isEmpty()) return false;

        AUnitType provider = racePolicy.supplyProviderType();
        if (provider == null) return false;

        return BuildOrderGoals.hasUnproducedRowOf(
            rows, new LiveBuildOrderProgress(), UnitProducible.of(provider));
    }

    /**
     * The plan of the last frame, for debug output and tests. Read-only.
     */
    public ProductionPlan lastPlan() {
        return lastPlan;
    }
}
