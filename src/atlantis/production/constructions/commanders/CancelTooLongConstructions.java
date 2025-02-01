package atlantis.production.constructions.commanders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class CancelTooLongConstructions {
    public static void cancelCauseTakingTooLongIfNeeded(Construction constr, int timeout, AUnitType type, AUnitType buildingType) {
        if (tookTooLong(constr, timeout)) {
            if (neverCancel(type)) {
                constr.findPositionForNewBuilding();

                ErrorLog.printMaxOncePerMinute("--- Took too long but don't dancel " + type);
                return;
            }

//            System.err.println(" // " + AGame.now() + " // " + constr.timeOrdered() + " // > " + timeout);
            ErrorLog.printMaxOncePerMinute(
                "Cancel constr of " + type
                    + " (Took too long)"
                    + " buildable:" + constr.buildPosition().isBuildableIncludeBuildings()
                    + " (Supply " + A.supplyUsed() + "/" + A.supplyTotal() + ")"
            );
            constr.cancel();

            constructionCancelledRequestAgainBecauseItsImportant(buildingType);
        }
    }

    private static boolean neverCancel(AUnitType type) {
        return type.isRoboticsFacility()
            || type.isRoboticsSupportBay()
            || type.isCyberneticsCore()
            || type.isObservatory()
            || type.isGasBuilding();
    }

    private static boolean tookTooLong(Construction constr, int timeout) {
        return AGame.now() - constr.timeOrdered() > timeout;
    }

    private static void constructionCancelledRequestAgainBecauseItsImportant(AUnitType type) {
        if (We.protoss()) {
            if (type.isRoboticsFacility() || type.isObservatory() || type.isForge()) {
                ErrorLog.printMaxOncePerMinute("### IMPORTANT ### Requesting again " + type + " as it got cancelled");
                AddToQueue.withHighPriority(type);
            }
        }
    }
}
