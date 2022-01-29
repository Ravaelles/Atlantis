package atlantis;

import atlantis.combat.ACombatCommander;
import atlantis.combat.missions.AMissionManager;
import atlantis.debug.AAdvancedPainter;
import atlantis.enemy.EnemyUnits;
import atlantis.production.ABuildingsCommander;
import atlantis.scout.AScoutManager;
import atlantis.strategy.AStrategyCommander;
import atlantis.ums.UmsSpecialActionsManager;
import atlantis.util.CodeProfiler;
import atlantis.workers.AWorkerCommander;

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

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_BUILDINGS);
        ABuildingsCommander.update();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_BUILDINGS);

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
        AMissionManager.updateGlobalMission();
        EnemyUnits.updateFoggedUnits();
        UmsSpecialActionsManager.update();
        AUnitStateManager.update();
        CameraManager.update();
        UseMap.updateMapSpecific();
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_OTHER);
    }

}
