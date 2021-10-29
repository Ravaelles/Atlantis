package atlantis.init;

import atlantis.AGame;
import atlantis.map.AMap;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.workers.AMineralGathering;

public class AInitialActions {

    /**
     * This method is executed only once, at the game start. It's supposed to initialize game by doing some
     * one-time only actions like initial assignment of workers to minerals etc.
     */
    public static void executeInitialActions() {
        if (Select.mainBase() == null) {
            AGame.setUmsMode(true);
        }

        if (!AMap.disableSomeOfTheChokes() || Select.ourWorkers().count() != 4) {
            AGame.setUmsMode(true);
        }

//        try {
        AMineralGathering.initialAssignWorkersToMinerals();
//        } catch (Exception ex) {
//            AGame.setUmsMode(true);
//        }
    }

}
