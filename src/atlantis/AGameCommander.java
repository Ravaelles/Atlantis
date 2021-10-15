package atlantis;

import atlantis.combat.ACombatCommander;
import atlantis.debug.AAdvancedPainter;
import atlantis.production.ABuildingsCommander;
import atlantis.scout.AScoutManager;
import atlantis.strategy.AStrategyCommander;
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

        AUnitStateManager.update();
        AStrategyCommander.update();
        ABuildingsCommander.update();
        AWorkerCommander.update();
        ACombatCommander.update();
        AScoutManager.update();

        // === Handle UMS ===================================================
        
        if (AGame.isUms()) {
            AUnit unit = Select.ourCombatUnits().first();
            if (unit != null) {
                AViewport.centerCameraOn(unit.getPosition());
            }
        }
    }

}
