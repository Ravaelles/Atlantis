package atlantis.init;

import atlantis.config.ActiveMap;
import atlantis.game.AGame;
import atlantis.units.select.Select;
import atlantis.units.workers.AMineralGathering;

public class AInitialActions {

    /**
     * This method is executed only once, at the game start. It's supposed to initialize game by doing some
     * one-time only actions like initial assignment of workers to minerals etc.
     */
    public static void executeInitialActions() {
        if (Select.main() == null) {
            AGame.setUmsMode();
        }

        if (Select.ourWorkers().count() != 4) {
            if (!ActiveMap.isMap("vsGosuRav")) {
                AGame.setUmsMode();
            }
        }

        AMineralGathering.initialAssignWorkersToMinerals();
    }

}
