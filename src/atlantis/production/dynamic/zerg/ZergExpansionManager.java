package atlantis.production.dynamic.zerg;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.select.Count;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class ZergExpansionManager {

//    private static Cache<Integer> cacheInt = new Cache<>();
    private static int _lastExpandedAt = -1;

    // =========================================================

    public static boolean handleNoZergLarvas() {
        if (!A.hasMinerals(300)) {
            if (!We.zerg() || Count.larvas() > 0) {
                return false;
            }
        }

        if (lastExpandedLessThanSecondsAgo(10)) {
            return false;
        }

//        A.seconds() >= 200 &&
        if (AGame.canAffordWithReserved(225, 0)) {
            return markExpandedNow();
        }

        return false;
    }

    // =========================================================

    private static boolean markExpandedNow() {
        _lastExpandedAt = A.now();
        return true;
    }

    private static boolean lastExpandedLessThanSecondsAgo(int seconds) {
        return (A.now() - _lastExpandedAt) <= 30 * seconds;
    }

}
