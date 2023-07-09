package atlantis.game;

import atlantis.architecture.Commander;
import atlantis.combat.CombatCommander;
import atlantis.config.MapSpecificConfig;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.debug.painter.PainterCommander;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.strategy.AStrategyCommander;
import atlantis.map.scout.AScoutManager;
import atlantis.production.BuildingCommander;
import atlantis.production.AProductionCommander;
import atlantis.units.AUnitStateManager;
import atlantis.units.UmsSpecialActionsManager;
import atlantis.units.workers.AWorkerCommander;
import atlantis.util.CodeProfiler;

/**
 * Top abstraction level entity that issues orders to all other modules (managers).
 */
public class AtlantisGameCommander extends Commander {

    private CombatCommander combatCommander = new CombatCommander();
    private BuildingCommander buildingManager = new BuildingCommander();

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            PainterCommander.class,
            AStrategyCommander.class,
            AProductionCommander.class,
        };
    }

    /**
     * Executed every time when game has new frame.
     * It represents minimal passage of game-time (one game frame).
     */
    public void update() {
//        System.out.println("AtlantisGameCommander Frame = " + A.now());

        // === Execute paint methods ========================================
        
//        AAdvancedPainter.paint();

        // === Strategy =====================================================

//        AStrategyCommander.update();

        // === Production ===================================================

//        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_PRODUCTION);
//        AProductionCommander.update();
//        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_PRODUCTION);

        // === Workers ======================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_WORKERS);
        AWorkerCommander.update();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_WORKERS);

        // === Combat =======================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_COMBAT);
        combatCommander.handle();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_COMBAT);

        // === Scout ========================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_SCOUTING);
        AScoutManager.update();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_SCOUTING);

        // =========================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_OTHER);
        buildingManager.handle();
        EnemyUnitsUpdater.updateFoggedUnits();
        UmsSpecialActionsManager.update();
        AUnitStateManager.update();
        CameraManager.update();
        MapSpecificConfig.updateMapSpecific();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_OTHER);
    }

}
