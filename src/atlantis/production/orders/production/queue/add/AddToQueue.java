package atlantis.production.orders.production.queue.add;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.orders.production.queue.ClearCountCache;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.production.requests.RemoveExcessiveOrders;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;
import bwapi.TechType;
import bwapi.UpgradeType;

public class AddToQueue {
    public static final int MAX = 999;

    public static ProductionOrder withTopPriority(AUnitType type) {
        return withTopPriority(type, null);
    }

    public static ProductionOrder withTopPriority(AUnitType type, HasPosition position) {

//        if (type.is(AUnitType.Protoss_Robotics_Facility)) {
//            A.printStackTrace("Why top priority " + type + "???");
//        }

        return addToQueue(type, position, IndexForNewOrder.indexForPriority(ProductionOrderPriority.TOP));
    }

    public static ProductionOrder withPriority(AUnitType type, ProductionOrderPriority priority) {
        return addToQueue(type, null, IndexForNewOrder.indexForPriority(priority));
    }

    public static ProductionOrder withHighPriority(AUnitType type) {
        return withHighPriority(type, null);
    }

    public static ProductionOrder withHighPriority(AUnitType type, HasPosition position) {
        return addToQueue(type, position != null ? position.position() : null, IndexForNewOrder.indexForPriority(ProductionOrderPriority.HIGH));
    }

    public static ProductionOrder withStandardPriority(AUnitType type) {
        return withStandardPriority(type, null);
    }

    public static ProductionOrder withStandardPriority(AUnitType type, HasPosition position) {
        return addToQueue(type, position != null ? position.position() : null, IndexForNewOrder.indexForPriority(ProductionOrderPriority.STANDARD));
    }

    public static boolean tech(TechType tech) {
//        if (Count.inQueueOrUnfinished(tech, 1) > 0) return false;
        if (Count.inQueueOrUnfinished(tech, MAX) > 0) {
            ProductionOrder existingOrder = Queue.get().nextOrders(MAX).techType(tech).first();

            if (existingOrder != null) Queue.get().removeOrder(existingOrder);
            else ErrorLog.printMaxOncePerMinute("Could not find existing order for " + tech + " to remove");

            return false;
        }

        ProductionOrder productionOrder = new ProductionOrder(tech, 0);

        if (tech.equals(TechType.Tank_Siege_Mode)) productionOrder.setPriority(ProductionOrderPriority.TOP);

        Queue.get().addNew(0, productionOrder);
        return true;
    }

    public static boolean upgrade(UpgradeType upgrade) {
//        if (Count.inQueueOrUnfinished(upgrade, MAX) > 0) return false;
        if (Count.inQueueOrUnfinished(upgrade, MAX) > 0) {
            ProductionOrder existingOrder = Queue.get().nextOrders(MAX).upgradeType(upgrade).first();

            if (existingOrder != null && !existingOrder.isInProgress()) Queue.get().removeOrder(existingOrder);
            else ErrorLog.printMaxOncePerMinute("Could not find existing order for " + upgrade + " to remove");

            return false;
        }

        Queue.get().addNew(0, new ProductionOrder(upgrade, 0));
        return true;
    }

    // =========================================================

    private static ProductionOrder addToQueue(AUnitType type, HasPosition position, int index) {
        if (PreventDuplicateOrders.preventExcessiveOrInvalidOrders(type, position)) return null;

        if (A.supplyTotal() >= 30 && type.isPylon()) {
            int inQueue = CountInQueue.count(AUnitType.Protoss_Pylon);

            if (inQueue >= 2) {
                A.println(A.now() + ": @@@@@@@@@@@@@ Add PYLON @@@@ " + position + " / " +
                    inQueue);
            }

            if (inQueue >= 2) {
                A.printStackTrace("Too many pylons in queue (" + inQueue + ")");
                return null;
            }
        }

        ProductionOrder productionOrder = new ProductionOrder(type, position, defineMinSupplyForNewOrder(type));

        if (Queue.get().addNew(index, productionOrder)) {
//            A.println(A.now() + ": Adding " + type + " to queue");

//            if (type.isSupplyDepot()) {
//                System.out.println("@ " + A.now() + " - ADDED DEPOT = "
//                    + CountInQueue.count(AUnitType.Terran_Supply_Depot)
//                    + " / " + Count.inQueueOrUnfinished(AUnitType.Terran_Supply_Depot, 50)
//                );
//            }

//            if (type.isBunker()) {
//                A.printStackTrace(A.now() + ": Adding bunker to queue at " + position);
//            }

//            if (type.is(AUnitType.Protoss_Robotics_Facility)) {
//                A.printStackTrace("Robotics Facility ADDED TO QUEUE");
//            }

//            if (A.supplyTotal() >= 16 && type.is(AUnitType.Protoss_Pylon) && A.supplyTotal() <= 26) {
//                A.printStackTrace("Pylon ADDED TO QUEUE (Supply:" + A.supplyTotal()
//                    + ", IN_PROD:" + Count.inProductionOrInQueue(AUnitType.Protoss_Pylon) + ")");
//            }

            if (type.is(AUnitType.Protoss_Nexus)) {
                A.println(A.now() + " Nexus ADDED TO QUEUE, min=" + A.minerals()
                    + "/ sup=" + A.supplyUsed() + " / " + ShouldExpand.reason);
            }

            RemoveExcessiveOrders.removeExcessive(type);

            ClearCountCache.clear();

//            if (type.isSupplyDepot()) {
//                System.out.println("@ " + A.now() + " - ADDED DEPOT - POST CLEAR CACHE = "
//                    + CountInQueue.count(AUnitType.Terran_Supply_Depot)
//                    + " / " + Count.inQueueOrUnfinished(AUnitType.Terran_Supply_Depot, 50)
//                );
//            }

//            clearOtherExistingOfTheSameTypeIfNeeded(productionOrder);

//            if (type.isBase()) {
//                A.printStackTrace(A.now() + ": Queued BASE at " + position + " / natural: " + Bases.natural());
//            }

//            if (type.isBunker()) {
//                if (position != null) {
//                    AAdvancedPainter.paintingMode = 3;
//                    AAdvancedPainter.paintCircleFilled(position, 14, Color.Orange);
//                    CameraCommander.centerCameraOn(position);
//                }
//                A.printStackTrace(A.now() + ": Adding bunker to queue at " + position + " / natural: " + Bases.natural());
//                GameSpeed.pauseGame();
//            }

//            A.errPrintln("Adding to queue: " + productionOrder + " / existingInQueue = " + Count.inQueue(type, 30));
        }
        else {
//            ErrorLog.printMaxOncePerMinute("Could not add " + type + " to queue");
        }

        return productionOrder;
    }

//    private static void clearOtherExistingOfTheSameTypeIfNeeded(ProductionOrder productionOrder) {
//        if (productionOrder.isUnitOrBuilding() && productionOrder.unitType().isBase()) {
//            for (ProductionOrder order : Queue.get().readyToProduceOrders().ofType(productionOrder.unitType()).list()) {
//                if (!order.equals(productionOrder)) order.cancel();
//            }
//        }
//    }

    private static int defineMinSupplyForNewOrder(AUnitType type) {
        Orders nextOrders = Queue.get().nextOrders(20).nonCompleted();

//        nextOrders.print("nextOrders");

        int currentMin1 = A.supplyUsed() - 1;

        if (nextOrders.isEmpty()) {
            return currentMin1;
//            return 0;
        }

//        System.err.println("A = " + (nextOrders.list().get(0).minSupply() + 1));

        ProductionOrder last = type == null ? null : nextOrders.nonCompleted().ofType(type).last();

        return 1 + Math.max(
            Math.max(0, last == null ? 0 : last.minSupply()),
            nextOrders.last().minSupply()
        );
    }

    protected static boolean addToQueue(AUnitType type) {
        withStandardPriority(type);
        return true;
    }

    public static boolean addToQueueIfNotAlreadyThere(AUnitType type) {
        if (CountInQueue.count(type) == 0) {
            return addToQueue(type);
        }

        return false;
    }

    public static ProductionOrder maxAtATime(AUnitType type, int maxAtATime) {
        return maxAtATime(type, maxAtATime, ProductionOrderPriority.STANDARD);
    }

    public static ProductionOrder maxAtATime(AUnitType type, int maxAtATime, ProductionOrderPriority priority) {
        if (CountInQueue.count(type, 50) < maxAtATime) {
            return withPriority(type, priority);
        }

        return null;
    }

    public static boolean toHave(AUnitType type) {
        return toHave(type, 1, ProductionOrderPriority.STANDARD);
    }

    public static boolean toHave(AUnitType type, int inTotal) {
        return toHave(type, inTotal, ProductionOrderPriority.STANDARD);
    }

    public static boolean toHave(AUnitType type, int expectedInTotal, ProductionOrderPriority priority) {
        int existing = Count.withPlanned(type);
        boolean result = false;

        if (existing < expectedInTotal) {
            PreventDuplicateOrders.tempDisabled = true;

            for (int i = 0; i < expectedInTotal - existing; i++) {
//                System.err.println("_Adding " + existing + " / " + expectedInTotal + " " + type + " to queue");
                result = withPriority(type, priority) != null || result;
            }

            PreventDuplicateOrders.tempDisabled = false;
        }

        return result;
    }

    public static boolean addToQueueIfHaveFreeBuilding(AUnitType type) {
        AUnitType building = type.whatBuildsIt();
        for (AUnit buildingProducing : Select.ourFree(building).list()) {
            if (!buildingProducing.isTrainingAnyUnit() && A.canAffordWithReserved(type)) {
                addToQueue(type);
                return true;
            }
        }
        return false;
    }
}
