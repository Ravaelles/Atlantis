package atlantis.production.v2.engine;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.PlacementPlanner;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.ProductionScheduler;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.LegacyPlacementPlanner;
import atlantis.production.v2.execution.DryRunOrderDirector;
import atlantis.production.v2.execution.GameOrderDirector;
import atlantis.production.v2.execution.OrderDirector;
import atlantis.production.v2.execution.ProductionDispatcher;

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

    /** Latency margin for issuing commands; BWAPI drops a command sent too late. */
    private static final int LATENCY_FRAMES = 3;

    private final LegacyPlacementPlanner placementPlanner = new LegacyPlacementPlanner();
    private final DryRunOrderDirector dryRunDirector = new DryRunOrderDirector();

    private ProductionPlan lastPlan = new ProductionPlan();

    /**
     * Runs one frame of the engine. Returns the plan it computed, which is also
     * what the dry-run log reports.
     */
    public ProductionPlan updateFrame() {
        GameStateSnapshot state = new GameStateSnapshot();

        ResourceTimeline timeline = state.buildTimeline();
        List<ProductionGoal> goals = collectGoals(state);

        ProductionScheduler scheduler = new ProductionScheduler(state.facilityRegistry(), placementPlanner);
        ProductionPlan plan = scheduler.schedule(goals, timeline);
        lastPlan = plan;

        OrderDirector director = Env.productionV2().isDryRun() ? dryRunDirector : new GameOrderDirector();
        new ProductionDispatcher(director).dispatch(plan, state.frame(), LATENCY_FRAMES);

        if (director instanceof DryRunOrderDirector) {
            ((DryRunOrderDirector) director).logSummary(state.frame(), plan.size());
            ((DryRunOrderDirector) director).clear();
        }

        return plan;
    }

    /**
     * Goals of this frame: the opening from the build order, plus the dynamic
     * generators. They are all declarations; the scheduler resolves the
     * contention between them by priority and by time.
     */
    private List<ProductionGoal> collectGoals(GameStateSnapshot state) {
        List<ProductionGoal> goals = new ArrayList<>();

        List<ProductionOrder> buildOrderRows = buildOrderRows();
        if (!buildOrderRows.isEmpty()) {
            goals.addAll(state.buildOrderGoals(buildOrderRows));
        }

        goals.addAll(state.dynamicGoals());
        Collections.sort(goals);

        return goals;
    }

    private List<ProductionOrder> buildOrderRows() {
        if (CurrentBuildOrder.get() == null)
            return Collections.emptyList();
        return CurrentBuildOrder.get().productionOrders();
    }

    /** The plan of the last frame, for debug output and tests. Read-only. */
    public ProductionPlan lastPlan() {
        return lastPlan;
    }

    /**
     * True when the engine is allowed to play; the game caller uses it to skip
     * the work entirely rather than run a pipeline whose output it will throw
     * away. Kept static so a debug painter can read it without an instance.
     */
    public static boolean enabled() {
        return Env.productionV2().isEnabled();
    }

    /** Frame of the next update, for the comparison log. */
    public static int now() {
        return A.now();
    }
}
