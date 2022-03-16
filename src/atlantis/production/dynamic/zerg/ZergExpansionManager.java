package atlantis.production.dynamic.zerg;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ZergExpansionManager {

    public static boolean handleNoZergLarvas() {
        if (!We.zerg() || Count.larvas() > 0) {
            return false;
        }

//        A.seconds() >= 200 &&
        return AGame.canAffordWithReserved(270, 0);
    }

}
