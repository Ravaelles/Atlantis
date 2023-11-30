package atlantis.production.orders.production.queue.add;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class PreventAddDuplicate {
    public static final int MAX_NONCOMPLETED_ORDERS_AT_ONCE = 30;

    protected static boolean preventExcessiveOrInvalidOrders(AUnitType type, HasPosition position) {
        assert type != null;

        if (tooManyOrdersOfThisType(type)) return true;
        if (tooManyOrdersInGeneral(type)) return true;

        if (forProtossEnforceHavingAPylonFirst(type)) return true;

        return false;
    }

    private static boolean forProtossEnforceHavingAPylonFirst(AUnitType type) {
        if (We.protoss() && type.isABuilding() && (!type.isPylon() && !type.isBase()) && Count.pylons() == 0) {
            if (A.seconds() < 200) {
                System.out.println("PREVENT " + type + " from being built. Enforce Pylon first.");
            }
            return true;
        }

        return false;
    }

    private static boolean tooManyOrdersOfThisType(AUnitType type) {
        int existingInQueue = Count.inQueue(type);

        if (existingInQueue >= (type.isABuilding() ? (type.isCombatBuilding() ? 5 : 2) : 4)) {
            if (type.isSupplyDepot()) ErrorLog.printMaxOncePerMinute("Exceeded DEPOTS allowed: " + existingInQueue);
            if (type.isBunker()) ErrorLog.printMaxOncePerMinute("Exceeded BUNKERS allowed: " + existingInQueue);
            return true;
        }

        if (invalidBunkers()) return true;

        return false;
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
