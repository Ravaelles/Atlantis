package atlantis.production.orders.production.queue.add;

import atlantis.config.env.Env;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.map.base.Bases;
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
import bwapi.Color;
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
        if (Count.inQueueOrUnfinished(tech, 1) > 0) return false;

        ProductionOrder productionOrder = new ProductionOrder(tech, 0);

        if (tech.equals(TechType.Tank_Siege_Mode)) {
            productionOrder.setPriority(ProductionOrderPriority.TOP);
        }

        Queue.get().addNew(0, productionOrder);
        return true;
    }

    public static boolean upgrade(UpgradeType upgrade) {
        if (Count.inQueueOrUnfinished(upgrade, 1) > 0) return false;

        Queue.get().addNew(0, new ProductionOrder(upgrade, 0));
        return true;
    }

    // =========================================================

    private static ProductionOrder addToQueue(AUnitType type, HasPosition position, int index) {
        if (preventExcessiveOrInvalidOrders(type)) return null;

//        if (type != null && type.isFactory()) {
//            A.printStackTrace("Factory, " + CountInQueue.count(AUnitType.Terran_Factory));
//        }

        ProductionOrder productionOrder = new ProductionOrder(type, position, defineMinSupplyForNewOrder());

        if (Queue.get().addNew(index, productionOrder)) {
            clearOtherExistingOfTheSameTypeIfNeeded(productionOrder);

//            if (type.isBase()) {
//                A.printStackTrace(A.now() + ": Queued BASE at " + position + " / natural: " + Bases.natural());
//            }

            if (type.isBunker()) {
                if (position != null) {
                    AAdvancedPainter.paintingMode = 3;
                    AAdvancedPainter.paintCircleFilled(position, 14, Color.Orange);
                    CameraCommander.centerCameraOn(position);
                }
                A.printStackTrace(A.now() + ": Adding bunker to queue at " + position + " / natural: " + Bases.natural());
                GameSpeed.pauseGame();
            }

//            A.errPrintln("Adding to queue: " + productionOrder + " / existingInQueue = " + Count.inQueue(type, 30));
        }

        return productionOrder;
    }

    private static void clearOtherExistingOfTheSameTypeIfNeeded(ProductionOrder productionOrder) {
        if (productionOrder.isUnitOrBuilding() && productionOrder.unitType().isBase()) {
            for (ProductionOrder order : Queue.get().readyToProduceOrders().ofType(productionOrder.unitType()).list()) {
                if (!order.equals(productionOrder)) order.cancel();
            }
        }
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

        int maxOrdersAtOnceWithoutWarning = 30;

        // Too many requests of this type
        int existingInQueue = Count.inQueue(type);
        if (existingInQueue >= 4) {
            return true;
        }

        if (!Env.isTournament()) {
            if (Queue.get().nonCompleted().notInProgress().forCurrentSupply().size() >= maxOrdersAtOnceWithoutWarning) {
                ErrorLog.printMaxOncePerMinute("There are too many orders in queue, can't add more: " + type);
                if (A.everyNthGameFrame(79)) Queue.get().nonCompleted().forCurrentSupply().print();
                return true;
            }
        }

        if (We.protoss() && type.isABuilding() && (!type.isPylon() && !type.isBase()) && Count.pylons() == 0) {
            if (A.seconds() < 200) {
                System.out.println("PREVENT " + type + " from being built. Enforce Pylon first.");
            }
            return true;
        }

        return false;
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

    public static boolean toHave(AUnitType type, int inTotal, ProductionOrderPriority priority) {
        if (Count.existingOrInProductionOrInQueue(type) < inTotal) {
            return withPriority(type, priority) != null;
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
