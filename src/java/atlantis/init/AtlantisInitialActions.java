package atlantis.init;

import atlantis.information.AtlantisMap;
import atlantis.workers.AtlantisMineralGathering;

public class AtlantisInitialActions {

    /**
     * This method is executed only once, at the game start. It's supposed to initialize game by doing some
     * one-time only actions like initial assignment of workers to minerals etc.
     */
    public static void executeInitialActions() {
        AtlantisMineralGathering.initialAssignWorkersToMinerals();
        AtlantisMap.disableSomeOfTheChokePoints();
    }

}
