package atlantis.init;

import atlantis.AGame;
import atlantis.workers.AMineralGathering;

public class AInitialActions {

    /**
     * This method is executed only once, at the game start. It's supposed to initialize game by doing some
     * one-time only actions like initial assignment of workers to minerals etc.
     */
    public static void executeInitialActions() {
        try {
            AMineralGathering.initialAssignWorkersToMinerals();
        } catch (Exception ex) {
            AGame.setUmtMode(true);
        }

//        AMap.disableSomeOfTheChokePoints();
    }

}
