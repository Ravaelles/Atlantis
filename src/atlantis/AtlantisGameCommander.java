package atlantis;

//import atlantis.buildings.AtlantisBuildingsCommander;
import atlantis.combat.AtlantisCombatCommander;
import atlantis.debug.AtlantisPainter;
import atlantis.production.AtlantisProductionCommander;
import atlantis.scout.AtlantisScoutManager;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.workers.AtlantisWorkerCommander;

/**
 * Top abstraction level entity that issues orders to all other modules (managers).
 */
public class AtlantisGameCommander {

    /**
     * Executed every time when game has new frame. It represents minimal passage of game-time (one action
     * frame).
     */
    public void update() {
        System.out.println(AtlantisGame.getTimeFrames());

        // === Execute paint methods ========================================
        
        AtlantisPainter.paint();

        // === Execute code of every Commander and Manager ==================
        
        AtlantisProductionCommander.update();
        AtlantisWorkerCommander.update();
        AtlantisCombatCommander.update();
        AtlantisScoutManager.update();
//        AtlantisBuildingsCommander.update(); // Currently unused

        // === Handle UMT ==========================================
        
        if (AtlantisGame.isUmtMode()) {
            AUnit unit = Select.ourCombatUnits().first();
            if (unit != null) {
                AtlantisViewport.centerScreenOn(unit);
            }
        }
    }

}
