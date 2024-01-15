package atlantis.production.orders.production.queue.add;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class PreventAddDuplicate {
    public static final int MAX_NONCOMPLETED_ORDERS_AT_ONCE = 20;

    protected static boolean preventExcessiveOrInvalidOrders(AUnitType type, HasPosition position) {
        assert type != null;

        if (tooManyOrdersOfThisType(type, position)) return true;
        if (tooManyOrdersInGeneral(type)) return true;

        if (forProtossEnforceHavingAPylonFirst(type)) return true;

        return false;
    }

    private static boolean forProtossEnforceHavingAPylonFirst(AUnitType type) {
        if (We.protoss() && type.isABuilding() && (!type.isPylon() && !type.isBase()) && Count.pylons() == 0) {
            if (A.seconds() < 200) {
                System.out.println("PREVENT " + type + " from being built. Enforce Pylon first.");
                A.printStackTrace("Duplicate first Pylon - something went very wrong");
            }
            return true;
        }

        return false;
    }

    private static boolean tooManyOrdersOfThisType(AUnitType type, HasPosition position) {
        int existingInQueue = Count.inQueue(type);

        int max = type.isABuilding() ? (type.isCombatBuilding() ? 5 : 2) : 4;
        if (type.isSupplyDepot() && A.supplyTotal() <= 32) max = 1;

        if (existingInQueue >= max) {
            if (type.isSupplyDepot()) ErrorLog.printMaxOncePerMinute("Exceeded DEPOTS allowed: " + existingInQueue);
//            if (type.isBunker()) ErrorLog.printMaxOncePerMinute("Exceeded BUNKERS allowed: " + existingInQueue);
            return true;
        }

        if (tooManyDepots(type, position)) return true;
        if (tooManyBunkers(type, position)) return true;

        return false;
    }

    private static boolean tooManyDepots(AUnitType type, HasPosition position) {
        if (!type.isSupplyDepot()) return false;

        if (CountInQueue.count(AUnitType.Terran_Supply_Depot) >= 2) {
            ErrorLog.printMaxOncePerMinute(
                "Exceeded DEPOTS allowed: " + CountInQueue.count(AUnitType.Terran_Supply_Depot)
            );
            return true;
        }

        return false;
    }

    private static boolean tooManyBunkers(AUnitType type, HasPosition position) {
        if (!type.isBunker()) return false;

        if (position == null) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Position for bunker is null, that's retarded");
            return false;
        }

        return Count.existingOrPlannedBuildingsNear(AUnitType.Terran_Bunker, 13, position) > 0
            || Count.withPlanned(AUnitType.Terran_Bunker) >= Count.basesWithPlanned();
    }

    private static boolean tooManyOrdersInGeneral(AUnitType type) {
        if (
            !Env.isTournament() &&
                Queue.get().nonCompleted().notInProgress().forCurrentSupply().size() >= MAX_NONCOMPLETED_ORDERS_AT_ONCE
        ) {
            ErrorLog.printMaxOncePerMinute("There are too many orders in queue, can't add more: " + type);
            if (A.everyNthGameFrame(79)) Queue.get().nonCompleted().forCurrentSupply().print();
            return true;
        }

        return false;
    }
}
