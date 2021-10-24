package atlantis;

import atlantis.combat.ACombatCommander;
import atlantis.ums.UmsSpecialActionsManager;
import atlantis.debug.AAdvancedPainter;
import atlantis.production.ABuildingsCommander;
import atlantis.protoss.ProtossSpecificBuildingsCommander;
import atlantis.scout.AScoutManager;
import atlantis.strategy.AStrategyCommander;
import atlantis.terran.TerranSpecificBuildingsCommander;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.workers.AWorkerCommander;

/**
 * Top abstraction level entity that issues orders to all other modules (managers).
 */
public class AGameCommander {

    /**
     * Executed every time when game has new frame. It represents minimal passage of game-time (one action
     * frame).
     */
    public void update() {
//        System.out.println("Frame number: " + AGame.getTimeFrames());

        // === Execute paint methods ========================================
        
        AAdvancedPainter.paint();

        // === Execute code of every Commander and Manager ==================

        if (AGame.isUms()) {
            UmsSpecialActionsManager.update();
        }

        AStrategyCommander.update();
        ABuildingsCommander.update();
        AWorkerCommander.update();
        ACombatCommander.update();
        AScoutManager.update();

        if (AGame.isPlayingAsProtoss()) {
            ProtossSpecificBuildingsCommander.update();
        }
        else if (AGame.isPlayingAsTerran()) {
            TerranSpecificBuildingsCommander.update();
        }

        UmsSpecialActionsManager.update();
        AUnitStateManager.update();

        // === Handle UMS ===================================================
        
        if (AGame.isUms()) {
            AUnit umsUnit = Select.ourCombatUnits().groundUnits().first();
//            AUnit umsUnit = Select.ourCombatUnits().transports(true).first();
            if (umsUnit != null) {
                if (AGame.timeSeconds() <= 3) {
                    ACamera.centerCameraOn(umsUnit.getPosition());
                }
            }
        }
    }

}
