package atlantis.game;

import atlantis.application.FramePipeline;
import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
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
 * Top abstraction level entity that issues orders to all other modules (managers).
 * /*
 * Executes every time when game has new frame.
 * It represents minimal passage of game-time (one game frame).
 *
 * <p>The ordered list of top-level steps now lives in
 * {@link atlantis.application.FramePipeline} — one explicit source of truth
 * (Stage C, _AI/REVIEW.md §16).</p>
 */
public class AtlantisGameCommander extends Commander {
    public static Class<? extends Commander>[] topLevelSubcommanders() {
        return FramePipeline.steps();
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        // Must mirror FramePipeline.steps() in the same order.
        // steps() stays the Class[]-based pinned order (see FramePipelineTest);
        // this is the construction side of the same list, without reflection.
        return new CommanderFactory[]{
            BulletsCommander::new,
            UnitStateCommander::new,

            SpecialActionsCommander::new,
            ScoutCommander::new,
            WorkerCommander::new,
            CombatCommander::new,
            ProductionCommander::new,
            BuildingsCommander::new,
            ConstructionsCommander::new,

            SpecialCommander::new,

            StrategyCommander::new,
            EnemyUnitsCommander::new,
            CameraCommander::new,
            MapSpecificCommander::new,
            PainterCommander::new,

            DebugCommander::new,
        };
    }
}
