package atlantis;

//import atlantis.buildings.AtlantisBuildingsCommander;
import atlantis.buildings.managers.TerranFlyingBuildingManager;
import atlantis.combat.ACombatCommander;
import atlantis.debug.APainter;
import atlantis.production.AProductionCommander;
import atlantis.production.orders.ABuildOrderManager;
import atlantis.production.orders.TerranBuildOrder;
import atlantis.repair.ARepairCommander;
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
        
        APainter.paint();

        // === Execute code of every Commander and Manager ==================
        
        AStrategyCommander.update();
        AProductionCommander.update();
        AWorkerCommander.update();
        ACombatCommander.update();
        AScoutManager.update();

        // === Terran only ==================================================

        if (AGame.playsAsTerran()) {
            TerranFlyingBuildingManager.update();
            ARepairCommander.update();
        }

        // === Handle UMT ===================================================
        
        if (AGame.isUmtMode()) {
            AUnit unit = Select.ourCombatUnits().first();
            if (unit != null) {
                AViewport.centerScreenOn(unit);
            }
        }
    }

}
