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

        int base = 14;
        int bonus = 0;

        if (We.protoss()) {
            if (A.hasMinerals(250)) {
                bonus += 6;
            }
        }

        int timeoutSeconds = base + bonus + (
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
            // CANCEL FIRST, then decide whether to re-request.
            //
            // The guard below used to run BEFORE this cancel, and that inverted its meaning:
            // it asked "do we already have the maximum not-started ones?" while still counting
            // the very construction this method exists to replace. With
            // `maxAllowedAtOnce(Gateway) = 2`, a plan legitimately holding two not-started
            // Gateways read as "we have enough", the method returned early, the construction
            // stayed cancelled, and the requeue never ran at all (measured in a full game:
            // `### FAILED ### Requesting` = 0 and `Taking too long, re-try` = 0, ending with 25
            // Probes, 834 minerals and zero Gateways).
            //
            // Cancelling is exactly what frees the slot the guard is asking about, so the
            // order has to be: remove, then count, then decide. `CancelNotStarted.cancel`
            // cancels the not-started ones of this type (including a stale sibling), which is
            // what the earlier code was reaching for three lines too late.
            CancelNotStarted.cancel(type, "Taking too long, re-try");

            if (CountInQueue.countNotStarted(type) >= maxAllowedAtOnce(type)) {
                // Diagnostic, not a behavior change (owner's Wine game: a Nexus
                // cancelled after 130s was never re-requested, and the log could
                // not say whether the producer was gated out or the request was
                // refused). Names the veto with its counts.
                ErrorLog.printMaxOncePerMinute(
                    "REQUEUE_VETO " + type.name()
                        + " notStartedInQueue=" + CountInQueue.countNotStarted(type)
                        + " maxAllowedAtOnce=" + maxAllowedAtOnce(type));
                return false;
            }

            newOrder = AddToQueue.withHighPriority(type);

            ErrorLog.printMaxOncePerMinute(
                "### FAILED ### Requesting " + type + " as it got cancelled\n" +
                "New requested order: " + newOrder
            );
//            }

            if (newOrder == null && type.isProtossImportantTechBuilding()) {
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
