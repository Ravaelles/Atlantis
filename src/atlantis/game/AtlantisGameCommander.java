package atlantis.game;

import atlantis.architecture.Commander;
import atlantis.combat.CombatCommander;
import atlantis.config.MapSpecificCommander;
import atlantis.debug.painter.PainterCommander;
import atlantis.information.enemy.EnemyUnitsCommander;
import atlantis.information.strategy.StrategyCommander;
import atlantis.map.scout.ScoutCommander;
import atlantis.production.BuildingsCommander;
import atlantis.production.ProductionCommander;
import atlantis.production.constructing.ConstructionsCommander;
import atlantis.terran.repair.RepairsCommander;
import atlantis.units.SpecialUnitsCommander;
import atlantis.units.UmsSpecialActionsManager;
import atlantis.units.UnitStateCommander;
import atlantis.units.workers.WorkerCommander;

/**
 * Top abstraction level entity that issues orders to all other modules (managers).
/*
 * Executes every time when game has new frame.
 * It represents minimal passage of game-time (one game frame).
 */
public class AtlantisGameCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            WorkerCommander.class,
            CombatCommander.class,
            ProductionCommander.class,
            ScoutCommander.class,
            BuildingsCommander.class,
            ConstructionsCommander.class,
            RepairsCommander.class,
            SpecialUnitsCommander.class,
            UmsSpecialActionsManager.class,
            UnitStateCommander.class,

            StrategyCommander.class,
            EnemyUnitsCommander.class,
            CameraCommander.class,
            MapSpecificCommander.class,
            PainterCommander.class,
        };
    }
}