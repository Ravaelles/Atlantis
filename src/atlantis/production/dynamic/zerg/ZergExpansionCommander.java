package atlantis.production.dynamic.zerg;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class ZergExpansionCommander extends Commander {

    //    private static Cache<Integer> cacheInt = new Cache<>();
    private static int _lastExpandedAt = -1;

    // =========================================================

    public static boolean handleNoZergLarvas() {
        if (Count.larvas() >= 2 && !A.hasMinerals(250)) return false;
        if (!A.hasMinerals(200)) return false;

//        if (!A.hasMinerals(300)) {
//            if (Count.larvas() > 0 && !A.hasMinerals(600)) return false;
//        }

        if (lastExpandedLessThanSecondsAgo(6)) return false;

//        A.seconds() >= 200 &&
        if (AGame.canAfford(223, 0)) {
            return markExpandedNow();
        }

        return false;
    }

    // =========================================================

    private static boolean markExpandedNow() {
        AddToQueue.maxAtATime(AUnitType.Zerg_Hatchery, 3);

        _lastExpandedAt = A.now();
        return true;
    }

    private static boolean lastExpandedLessThanSecondsAgo(int seconds) {
        return (A.now() - _lastExpandedAt) <= 30 * seconds;
    }

}
