package atlantis.production.orders.production.queue.add;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.QueueLastStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import bwapi.TechType;
import bwapi.UpgradeType;

public class PreventDuplicateOrders {
    public static final int MAX_NONCOMPLETED_ORDERS_AT_ONCE = 20;
    public static boolean tempDisabled = false;
//    public static History history = new History();

    protected static boolean preventExcessiveOrInvalidOrders(AUnitType type, HasPosition position) {
        if (tempDisabled) return false;

        assert type != null;

        if (onlyOneAllowed(type, position)) {
            QueueLastStatus.updateStatusFailed("OnlyOneAllowed", type.toString());
            return true;
        }

        if (excessivePylon(type, position)) {
            QueueLastStatus.updateStatusFailed("ExcessivePylon", type.toString());
            return true;
        }

        if (justRequestedThisType(type)) {
            QueueLastStatus.updateStatusFailed("JustRequested", type.toString());
            return true;
        }

        if (tooManyOrdersOfThisType(type, position)) {
            QueueLastStatus.updateStatusFailed("TooManySuchOrders", type.toString());
            return true;
        }

        if (tooManyOrdersInGeneral(type)) {
            QueueLastStatus.updateStatusFailed("tooManyOrders", type.toString());
            return true;
        }

        if (forProtossEnforceHavingAPylonFirst(type)) {
            QueueLastStatus.updateStatusFailed("EnforcePylonFirst", type.toString());
            return true;
        }

        return false;
    }

    private static boolean onlyOneAllowed(AUnitType type, HasPosition position) {
        if (type.equals(AUnitType.Protoss_Citadel_of_Adun)) return Count.withPlanned(type) > 0;
        if (type.equals(AUnitType.Protoss_Cybernetics_Core)) return Count.withPlanned(type) > 0;
        if (type.equals(AUnitType.Protoss_Observatory)) return Count.withPlanned(type) > 0;
        if (type.equals(AUnitType.Protoss_Robotics_Support_Bay)) return Count.withPlanned(type) > 0;

        return false;
    }

    private static boolean justRequestedThisType(AUnitType type) {
        if (type == null) return false;
//        if (!type.isResource()) return false;
        if (!type.isABuilding()) return false;
        if (Count.withPlanned(type) == 0) return false;

        int lastRequestedAgo = Queue.get().history().lastHappenedAgo(type.name());
//        System.err.println(
//            A.now() + " - " + type + " lastRequestedAgo = " + lastRequestedAgo + " / CIQ="
//                + CountInQueue.count(type)
//        );

        if (type.is(AUnitType.Protoss_Observatory)) {
            A.printStackTrace("Excessive observatory // " + Count.withPlanned(type));
        }
        if (type.is(AUnitType.Protoss_Observer)) {
            A.printStackTrace("Excessive observER // " + Count.withPlanned(type));
        }

        if (lastRequestedAgo <= 30 * 2 && !type.isCombatBuilding()) {
//            ErrorLog.printMaxOncePerMinute("Canceling " + type + " as last requested " + lastRequestedAgo + " frames ago.");
            return true;
        }

        if (!type.isCombatBuilding() && Queue.get().history().countInLastSeconds(type.name(), 30) >= 2) {
            return true;
        }

        return false;
    }

    private static boolean excessivePylon(AUnitType type, HasPosition position) {
        if (!type.isPylon()) return false;

        if (A.supplyUsed() <= 27 && A.supplyFree() >= 4) return true;
        if (A.supplyUsed() <= 36 && A.supplyFree() >= 5) return true;
        if (A.supplyUsed() <= 50 && A.supplyFree() >= 6) return true;
        if (A.supplyUsed() <= 65 && A.supplyFree() >= 7) return true;

        return A.hasFreeSupply(10);
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
        if (type.isPylon() && A.supplyTotal() <= 32) max = 1;
        if (type.isGateway()) max = 1 + A.minerals() / 170;

        if (existingInQueue >= max) {
            if (type.isSupplyDepot()) ErrorLog.printMaxOncePerMinute("Exceeded DEPOTS allowed: " + existingInQueue);
//            if (type.isBunker()) ErrorLog.printMaxOncePerMinute("Exceeded BUNKERS allowed: " + existingInQueue);
            return true;
        }

        if (tooManyDepots(type, position)) return true;
        if (tooManyPylons(type, position)) return true;
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

    private static boolean tooManyPylons(AUnitType type, HasPosition position) {
        if (!type.isPylon()) return false;

        if (CountInQueue.count(AUnitType.Protoss_Pylon) >= (A.supplyTotal() >= 60 && A.supplyFree() <= 0 ? 4 : 2)) {
            ErrorLog.printMaxOncePerMinute(
                "Exceeded PYLON allowed: " + CountInQueue.count(AUnitType.Protoss_Pylon)
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

    public static boolean cancelPreviousNonStartedOrdersOf(AUnitType type) {
        boolean result = false;

        for (ProductionOrder order : Queue.get().nextOrders().ofType(type).list()) {
            if (order.isInProgress()) return true;
            order.setIgnore(true);

            if (order.construction() != null) {
                order.construction().setBuilder(null);
            }

            order.cancel();
            result = true;
        }

        return result;
    }

    public static boolean cancelPreviousNonStartedOrdersOf(UpgradeType upgrade) {
        boolean result = false;

        for (ProductionOrder order : Queue.get().nonCompleted().ofType(upgrade).list()) {
            if (order.isInProgress()) return true;
            order.setIgnore(true);

            order.cancel();
            result = true;
        }

        return result;
    }

    public static boolean cancelPreviousNonStartedOrdersOf(TechType tech) {
        boolean result = false;

        for (ProductionOrder order : Queue.get().nonCompleted().ofType(tech).list()) {
            if (order.isInProgress()) return true;
            order.setIgnore(true);

            order.cancel();
            result = true;
        }

        return result;
    }
}
