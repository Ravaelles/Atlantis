package atlantis.production.constructions.cancelling;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;

public class CancelNotStarted {
    public static boolean cancel(AUnitType type) {
        return cancel(type, "No_cancel_reason");
    }

    public static boolean cancel(AUnitType type, String reason) {
        Construction construction = ConstructionRequests.getNotStartedOfType(type);
        if (construction != null) construction.cancel(reason);

        construction = ConstructionRequests.getNotFinishedOfType(type);
        if (construction != null) {
            construction.cancel(reason);
            CriticalCancelPending.lastCancelledMinerals = type.mineralPrice();
            return true;
        }

        return false;
    }
}
