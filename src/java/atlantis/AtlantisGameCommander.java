package atlantis;

import atlantis.buildings.AtlantisBuildingsCommander;
import atlantis.combat.AtlantisCombatCommander;
import atlantis.debug.AtlantisPainter;
import atlantis.production.AtlantisProductionCommander;
import atlantis.scout.AtlantisScoutManager;
import atlantis.workers.AtlantisWorkerCommander;

public class AtlantisGameCommander {

    /**
     * Executed every time when game has new frame. It represents minimal passage of game-time (one action
     * frame).
     */
    public void update() {

        // =========================================================
        // Execute code of every Commander
        AtlantisWorkerCommander.update();
        AtlantisCombatCommander.update();
        AtlantisScoutManager.update();
        AtlantisBuildingsCommander.update();
        AtlantisProductionCommander.update();

        // =========================================================
        // Execute extra paint methods at the end of all actions
        AtlantisPainter.paint();
    }

}
