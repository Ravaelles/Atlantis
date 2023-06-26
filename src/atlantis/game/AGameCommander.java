package atlantis.game;

import atlantis.combat.ACombatCommander;
import atlantis.combat.missions.MissionChanger;
import atlantis.config.MapAndRace;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.strategy.AStrategyCommander;
import atlantis.map.scout.AScoutManager;
import atlantis.production.ABuildingManager;
import atlantis.production.AProductionCommander;
import atlantis.units.AUnitStateManager;
import atlantis.units.UmsSpecialActionsManager;
import atlantis.units.workers.AWorkerCommander;
import atlantis.util.CodeProfiler;

/**
 * Top abstraction level entity that issues orders to all other modules (managers).
 */
public class AGameCommander {

    /**
     * Executed every time when game has new frame.
     * It represents minimal passage of game-time (one game frame).
     */
    public void update() {
//        System.out.println("AGameCommander Frame = " + A.now());

        // === Execute paint methods ========================================
        
        AAdvancedPainter.paint();

        // === Strategy =====================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_STRATEGY);
        AStrategyCommander.update();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_STRATEGY);

        // === Production ===================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_PRODUCTION);
        AProductionCommander.update();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_PRODUCTION);

        // === Workers ======================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_WORKERS);
        AWorkerCommander.update();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_WORKERS);

        // === Combat =======================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_COMBAT);
        ACombatCommander.update();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_COMBAT);

        // === Scout ========================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_SCOUTING);
        AScoutManager.update();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_SCOUTING);

        // =========================================================

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_OTHER);
        ABuildingManager.update();
        MissionChanger.evaluateGlobalMission();
        EnemyUnitsUpdater.updateFoggedUnits();
        UmsSpecialActionsManager.update();
        AUnitStateManager.update();
        CameraManager.update();
        MapAndRace.updateMapSpecific();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_OTHER);
    }

}
