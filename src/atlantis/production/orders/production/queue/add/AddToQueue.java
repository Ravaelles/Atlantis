package atlantis.production.orders.production.queue.add;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import bwapi.TechType;
import bwapi.UpgradeType;

public class AddToQueue {

    public static ProductionOrder withTopPriority(AUnitType type) {
        return withTopPriority(type, null);
    }

    public static ProductionOrder withTopPriority(AUnitType type, HasPosition position) {

//        if (type.is(AUnitType.Protoss_Robotics_Facility)) {
//            A.printStackTrace("Why top priority " + type + "???");
//        }

        return addToQueue(type, position, indexForPriority(ProductionOrderPriority.TOP));
    }

    public static ProductionOrder withHighPriority(AUnitType type) {
        return withHighPriority(type, null);
    }

    public static ProductionOrder withHighPriority(AUnitType type, HasPosition position) {
        return addToQueue(type, position != null ? position.position() : null, indexForPriority(ProductionOrderPriority.HIGH));
    }

    public static ProductionOrder withStandardPriority(AUnitType type) {
        return withStandardPriority(type, null);
    }

    public static ProductionOrder withStandardPriority(AUnitType type, HasPosition position) {
        return addToQueue(type, position != null ? position.position() : null, indexForPriority(ProductionOrderPriority.STANDARD));
    }

    public static boolean tech(TechType tech) {
        if (Count.inQueueOrUnfinished(tech, 8) > 0) return false;

        ProductionOrder productionOrder = new ProductionOrder(tech, 0);

        if (tech.equals(TechType.Tank_Siege_Mode)) {
            productionOrder.setPriority(ProductionOrderPriority.TOP);
        }

        Queue.get().addNew(0, productionOrder);
        return true;
    }

    public static boolean upgrade(UpgradeType upgrade) {
        if (Count.inQueueOrUnfinished(upgrade, 3) > 0) return false;

        Queue.get().addNew(0, new ProductionOrder(upgrade, 0));
        return true;
    }

    // =========================================================

    private static ProductionOrder addToQueue(AUnitType type, HasPosition position, int index) {
        if (preventExcessiveOrInvalidOrders(type)) return null;

        ProductionOrder productionOrder = new ProductionOrder(type, position, defineMinSupplyForNewOrder());

        if (Queue.get().addNew(index, productionOrder)) {
            Queue.get().clearCache();
//            A.errPrintln("Adding to queue: " + productionOrder + " / existingInQueue = " + Count.inQueue(type, 30));
        }

        return productionOrder;
    }

    private static int defineMinSupplyForNewOrder() {
        Orders nextOrders = Queue.get().nextOrders(2);

//        nextOrders.print("nextOrders");

        if (nextOrders.isEmpty()) {
//            return A.supplyUsed() - 1;
            return 0;
        }

//        System.err.println("A = " + (nextOrders.list().get(0).minSupply() + 1));

        return nextOrders.list().get(0).minSupply() + 1;
    }

    private static boolean preventExcessiveOrInvalidOrders(AUnitType type) {
        assert type != null;

        int maxOrdersAtOnceWithoutWarning = 20;

        // Too many requests of this type
        int existingInQueue = Count.inQueue(type, 30);
        if (existingInQueue >= 4) {
            return true;
        }

        if (!Env.isTournament()) {
            if (Queue.get().nextOrders(30).size() >= maxOrdersAtOnceWithoutWarning) {
                ErrorLog.printMaxOncePerMinute("There are too many orders in queue, can't add more: " + type);
                Queue.get().nextOrders(30).print();
                return true;
            }
        }

        if (We.protoss() && type.isBuilding() && (!type.isPylon() && !type.isBase()) && Count.pylons() == 0) {
            if (A.seconds() < 200) {
                System.out.println("PREVENT " + type + " from being built. Enforce Pylon first.");
            }
            return true;
        }

        return false;
    }

    private static int indexForPriority(ProductionOrderPriority priority) {
        if (priority.isStandard()) {
            return CountInQueue.countOrdersWithPriorityAtLeast(ProductionOrderPriority.HIGH);
        }

        return CountInQueue.countOrdersWithPriorityAtLeast(priority);
    }

    protected static boolean addToQueue(AUnitType type) {
        withStandardPriority(type);
        return true;
    }

    public static boolean addToQueueIfNotAlreadyThere(AUnitType type) {
        if (CountInQueue.count(type, 5) == 0) {
            return addToQueue(type);
        }

        return false;
    }

    public static boolean maxAtATime(AUnitType type, int maxAtATime) {
        if (CountInQueue.count(type, 30) < maxAtATime) {
            return addToQueue(type);
        }

        return false;
    }

    public static boolean addToQueueIfHaveFreeBuilding(AUnitType type) {
        AUnitType building = type.whatBuildsIt();
        for (AUnit buildingProducing : Select.ourOfType(building).list()) {
            if (!buildingProducing.isTrainingAnyUnit() && AGame.canAffordWithReserved(type)) {
                addToQueue(type);
                return true;
            }
        }
        return false;
    }
}
