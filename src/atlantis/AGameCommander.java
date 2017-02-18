package atlantis;

//import atlantis.buildings.AtlantisBuildingsCommander;
import atlantis.buildings.managers.FlyingBuildingManager;
import atlantis.combat.AtlantisCombatCommander;
import atlantis.debug.APainter;
import atlantis.production.AtlantisProductionCommander;
import atlantis.repair.ARepairCommander;
import atlantis.scout.AtlantisScoutManager;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.workers.AtlantisWorkerCommander;

/**
 * Top abstraction level entity that issues orders to all other modules (managers).
 */
public class AGameCommander {

    /**
     * Executed every time when game has new frame. It represents minimal passage of game-time (one action
     * frame).
     */
    public void update() {
//        System.out.println("Frame: " + AGame.getTimeFrames());

        // === Execute paint methods ========================================
        
        APainter.paint();

        // === Execute code of every Commander and Manager ==================
        
        AtlantisProductionCommander.update();
        AtlantisWorkerCommander.update();
        AtlantisCombatCommander.update();
        AtlantisScoutManager.update();
//        AtlantisBuildingsCommander.update(); // Currently unused

        // === Terran only ==================================================

        if (AGame.playsAsTerran()) {
            FlyingBuildingManager.update();
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
