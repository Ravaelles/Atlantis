package atlantis.production.constructions.commanders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.cancelling.CancelNotStarted;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class CancelTooLongConstructions {
    public static void cancelCauseTakingTooLongIfNeeded(
        Construction constr, AUnitType type, AUnitType buildingType
    ) {
        if (
            tookTooLong(constr)
                && A.canAfford(type)
                && A.hasMinerals(type.mineralPrice() + 16)
                && (type.gasPrice() == 0 || A.hasGas(type.gasPrice() + 8))
        ) {
//            if (neverCancel(type)) {
//                constr.findPositionForNewBuilding();
//
//                ErrorLog.printMaxOncePerMinute("--- Took too long but don't cancel " + type);
//                return;
//            }

            APosition buildPosition = constr.buildPosition();
            constr.cancel(type + " took too long (" + tookSeconds(constr) + "s)");
//            System.err.println(" // " + AGame.now() + " // " + constr.timeOrdered() + " // > " + timeoutSeconds);
            ErrorLog.printMaxOncePerMinute(
                "Cancel constr of " + type
                    + " (Took too long)"
                    + " buildable:" + buildPosition.isBuildableIncludeBuildings()
                    + " (Supply " + A.supplyUsed() + "/" + A.supplyTotal() + ")"
            );

            constructionCancelledRequestAgainBecauseItsImportant(buildingType, buildPosition);
        }
    }

//    private static boolean neverCancel(AUnitType type) {
//        return type.isRoboticsFacility()
//            || type.isRoboticsSupportBay()
//            || type.isCyberneticsCore()
//            || type.isObservatory()
//            || type.isGasBuilding();
//    }

    private static boolean tookTooLong(Construction constr) {
        int timeoutSeconds = timeoutInSeconds(constr);

        return tookFrames(constr) / 30 > timeoutSeconds;
    }

    private static int timeoutInSeconds(Construction constr) {
        AUnitType type = constr.buildingType();
        AUnit main = Select.main();
        int bonus = We.protoss() ? 8 : 0;
        int timeoutSeconds = bonus + (
            14
                + (type.isBase() || type.isCombatBuilding() ? 20 : 0)
                + ((int) (2 * constr.buildPosition().groundDistanceTo(main != null ? main : constr.builder())))
        );

//        if (type.isGasBuilding()) timeoutSeconds += 6;

//        if (!type.isCombatBuilding() && !type.producesLandUnits()) {
//            timeoutSeconds += 18;
//        }

        return timeoutSeconds;
    }

    private static int tookFrames(Construction constr) {
        return AGame.now() - constr.timeOrdered();
    }

    private static int tookSeconds(Construction constr) {
        return tookFrames(constr) / 30;
    }

    private static boolean constructionCancelledRequestAgainBecauseItsImportant(AUnitType type, APosition oldPosition) {
        ProductionOrder newOrder = null;

        if (We.protoss()) {
            if (CountInQueue.countNotStarted(type) >= maxAllowedAtOnce(type)) {
                return false;
            }
//            if (type.isPylon()) return false;

//            if (type.isCyberneticsCore()) {
//                AUnit pylon = Select.ourBuildingsWithUnfinished()
//                        .ofType(AUnitType.Protoss_Pylon).nearestTo(oldPosition);
//
//                System.out.println("CC dist to pylon: " + (pylon != null ? pylon.distToDigit(oldPosition) : "no pylon"));
//            }

//            if (type.isProtossImportantBuilding() || type.isPylon()) {
            CancelNotStarted.cancel(type, "Taking too long, re-try");

            newOrder = AddToQueue.withHighPriority(type);

            ErrorLog.printMaxOncePerMinute(
                "### FAILED ### Requesting " + type + " as it got cancelled\n" +
                "New requested order: " + newOrder
            );
//            }

            if (newOrder == null && type.isProtossImportantBuilding()) {
                newOrder = AddToQueue.withTopPriority(type);

                ErrorLog.printMaxOncePerMinute(
                    "### IMPORTANT ### Re-request failed again " + type + "\n" +
                    "Re-requested: " + newOrder
                );
            }
        }

        return newOrder != null;
    }

    private static int maxAllowedAtOnce(AUnitType type) {
        return (type.isGateway() || type.isPylon())
            ? 2
            : 1;
    }
}
