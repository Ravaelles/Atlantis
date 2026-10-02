package atlantis.application;

import atlantis.architecture.Commander;
import atlantis.combat.CombatCommander;
import atlantis.config.MapSpecificCommander;
import atlantis.debug.DebugCommander;
import atlantis.debug.painter.PainterCommander;
import atlantis.game.CameraCommander;
import atlantis.game.state.BulletsCommander;
import atlantis.information.enemy.EnemyUnitsCommander;
import atlantis.information.strategy.StrategyCommander;
import atlantis.map.scout.ScoutCommander;
import atlantis.production.BuildingsCommander;
import atlantis.production.ProductionCommander;
import atlantis.production.constructions.ConstructionsCommander;
import atlantis.units.UnitStateCommander;
import atlantis.units.special.SpecialActionsCommander;
import atlantis.units.special.SpecialCommander;
import atlantis.units.workers.WorkerCommander;

/**
 * Stage C (see _AI/REVIEW.md §16): the single, explicit, ordered source of truth
 * for the top-level per-frame commands.
 *
 * <p>Previously the order lived only inside
 * {@link atlantis.game.AtlantisGameCommander}, and the same list was re-read by
 * {@link atlantis.architecture.BaseCommander} for profiling. Centralizing it here
 * makes the execution order visible in one place and pins it with a test, so that
 * later Stage C work (removing reflection, one documented contract per step) can
 * proceed without silently reordering the bot's behaviour.</p>
 *
 * <p><b>Order matters.</b> This is a pipeline: every step runs, in this order,
 * once per game frame. Do not reorder without an intentional, reviewed change to
 * {@code FramePipelineTest}.</p>
 *
 * <p>This class lives in {@code atlantis.application} on purpose: the pipeline is
 * the one place that is allowed to know about every context, whereas
 * {@code atlantis.architecture} must stay a base package (enforced by
 * {@code ArchitectureBoundaryTest}).</p>
 */
public final class FramePipeline {

    private FramePipeline() {
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Commander>[] steps() {
        return new Class[]{
            BulletsCommander.class,
            UnitStateCommander.class,

            SpecialActionsCommander.class,
            ScoutCommander.class,
            WorkerCommander.class,
            CombatCommander.class,
            ProductionCommander.class,
            BuildingsCommander.class,
            ConstructionsCommander.class,

            SpecialCommander.class,

            StrategyCommander.class,
            EnemyUnitsCommander.class,
            CameraCommander.class,
            MapSpecificCommander.class,
            PainterCommander.class,

            DebugCommander.class,
        };
    }
}
