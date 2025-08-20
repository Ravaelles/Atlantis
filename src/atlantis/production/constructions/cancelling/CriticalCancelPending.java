package atlantis.production.constructions.cancelling;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.units.AUnitType;
import atlantis.util.We;

import static atlantis.units.AUnitType.*;

/**
 * Cancel pending constructions to get minerals for critical constructions (like Cannons against DT).
 */
public class CriticalCancelPending {
    protected static int lastCancelledMinerals = 0;

    public static void cancelToGetMinerals(int mineralsNeeded) {
        int cleared = 0;

        CancelNotStartedBases.cancelNotStartedOrEarlyBases(null, "Critical base cancel");

        int counter = 0;
        while (cleared < mineralsNeeded) {
            cleared += cancelOne();

            if (++counter >= 5) {
                break;
            }
        }
    }

    // =========================================================

    private static int cancelOne() {
        if (We.protoss()) {
            return cancelOneProtoss();
        }
        else if (We.terran()) {
            return cancelOneTerran();
        }
//        else if (We.zerg()) {
//            return cancelOneZerg();
//        }

        return 0;
    }

    private static int cancelOneProtoss() {
        if (cancel(Protoss_Gateway)) return lastCancelledMinerals;
        if (cancel(Protoss_Cybernetics_Core)) return lastCancelledMinerals;
        if (cancel(Protoss_Robotics_Support_Bay)) return lastCancelledMinerals;
//        if (cancel(Protoss_Pylon)) return lastCancelledMinerals;

        return 0;
    }

    private static int cancelOneTerran() {
        if (cancel(Terran_Factory)) return lastCancelledMinerals;
        if (cancel(Terran_Barracks)) return lastCancelledMinerals;
        if (cancel(Terran_Supply_Depot)) return lastCancelledMinerals;
        if (cancel(Terran_Machine_Shop)) return lastCancelledMinerals;

        return 0;
    }

    // =========================================================

    private static boolean cancel(AUnitType type) {
        return CancelNotStarted.cancel(type, "Critical");
    }

    public static void cancelBases() {
        cancel(AtlantisRaceConfig.BASE);
    }

    public static void cancelGatewats() {
        cancel(Protoss_Gateway);
    }
}
