package atlantis.init;

import atlantis.AGame;
import atlantis.map.AMap;
import atlantis.units.Select;
import atlantis.workers.AMineralGathering;

public class AInitialActions {

    /**
     * This method is executed only once, at the game start. It's supposed to initialize game by doing some
     * one-time only actions like initial assignment of workers to minerals etc.
     */
    public static void executeInitialActions() {
        try {
            AMineralGathering.initialAssignWorkersToMinerals();
        }
        catch (IndexOutOfBoundsException ex) {
            AGame.setUmtMode(true);
        }
        catch (NullPointerException ex) {
            AGame.setUmtMode(true);
        }
        catch (Exception ex) {
            AGame.setUmtMode(true);
        }

        if (!AMap.disableSomeOfTheChokePoints() || Select.ourWorkers().count() != 4) {
            AGame.setUmtMode(true);
        }
    }

}
