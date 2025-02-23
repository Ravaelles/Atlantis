package atlantis.production.constructions.cancelling;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.units.AUnitType;
import atlantis.util.We;

import static atlantis.units.AUnitType.*;

/**
 * Cancel pending constructions to get minerals for critical constructions (like Cannons against DT).
 */
public class CriticalCancelPending {
    private static int lastCancelledMinerals = 0;

    public static void cancelToGetMinerals(int mineralsNeeded) {
        int cleared = 0;

        CancelNotStartedBases.cancelNotStartedOrEarlyBases(null, "Critical base cancel");

        while (cleared < mineralsNeeded) {
            cleared += cancelOne();
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
        if (cancel(Protoss_Pylon)) return lastCancelledMinerals;

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
        Construction construction = ConstructionRequests.getNotStartedOfType(type);
        if (construction != null) construction.cancel("Critical not started cancel");

        construction = ConstructionRequests.getNotFinishedOfType(type);
        if (construction != null) {
            construction.cancel("Critical in progress cancel");
            lastCancelledMinerals = type.mineralPrice();
            return true;
        }

        return false;
    }

    public static void cancelBases() {
        cancel(AtlantisRaceConfig.BASE);
    }
}
