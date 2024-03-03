package atlantis.units.special;

import atlantis.architecture.Commander;
import atlantis.combat.CombatCommander;
import atlantis.config.MapSpecificCommander;
import atlantis.debug.DebugCommander;
import atlantis.debug.painter.PainterCommander;
import atlantis.game.CameraCommander;
import atlantis.information.enemy.EnemyUnitsCommander;
import atlantis.information.strategy.StrategyCommander;
import atlantis.map.scout.ScoutCommander;
import atlantis.production.BuildingsCommander;
import atlantis.production.ProductionCommander;
import atlantis.production.constructing.ConstructionsCommander;
import atlantis.protoss.ProtossObserver;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.terran.chokeblockers.ChokeBlockersCommander;
import atlantis.terran.repair.TerranRepairsCommander;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.UnitStateCommander;
import atlantis.units.select.Select;
import atlantis.units.workers.WorkerCommander;
import atlantis.units.workers.defence.proxy.TrackEnemyEarlyScoutCommander;

public class SpecialCommander extends Commander {
    public static Class<? extends Commander>[] topLevelSubcommanders() {
        return new Class[]{
            TrackEnemyEarlyScoutCommander.class,
            TerranRepairsCommander.class,
            ChokeBlockersCommander.class,
            SpecialUnitsCommander.class,
        };
    }
}
