package atlantis.game;

import atlantis.architecture.Commander;
import atlantis.combat.CombatCommander;
import atlantis.config.MapSpecificCommander;
import atlantis.debug.DebugCommander;
import atlantis.debug.painter.PainterCommander;
import atlantis.information.enemy.EnemyUnitsCommander;
import atlantis.information.strategy.StrategyCommander;
import atlantis.map.scout.ScoutCommander;
import atlantis.production.BuildingsCommander;
import atlantis.production.ProductionCommander;
import atlantis.production.constructing.ConstructionsCommander;
import atlantis.terran.chokeblockers.ChokeBlockersCommander;
import atlantis.terran.repair.TerranRepairsCommander;
import atlantis.units.special.SpecialCommander;
import atlantis.units.special.SpecialUnitsCommander;
import atlantis.units.special.SpecialActionsCommander;
import atlantis.units.UnitStateCommander;
import atlantis.units.workers.WorkerCommander;
import atlantis.units.workers.defence.proxy.TrackEnemyEarlyScoutCommander;

/**
 * Top abstraction level entity that issues orders to all other modules (managers).
 * /*
 * Executes every time when game has new frame.
 * It represents minimal passage of game-time (one game frame).
 */
public class AtlantisGameCommander extends Commander {
    public static Class<? extends Commander>[] topLevelSubcommanders() {
        return new Class[]{
            SpecialActionsCommander.class,
            WorkerCommander.class,
            CombatCommander.class,
            ProductionCommander.class,
            ScoutCommander.class,
            BuildingsCommander.class,
            ConstructionsCommander.class,

            SpecialCommander.class,
            UnitStateCommander.class,

            StrategyCommander.class,
            EnemyUnitsCommander.class,
            CameraCommander.class,
            MapSpecificCommander.class,
            PainterCommander.class,

            DebugCommander.class,
        };
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return topLevelSubcommanders();
    }
}