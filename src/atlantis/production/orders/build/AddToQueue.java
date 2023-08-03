package atlantis.production.orders.build;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.production.orders.production.ProductionOrderPriority;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import bwapi.TechType;
import bwapi.UpgradeType;

public class AddToQueue {

    public static ProductionOrder withTopPriority(AUnitType type) {
        return withTopPriority(type, null);
    }

    public static ProductionOrder withTopPriority(AUnitType type, HasPosition position) {
//        System.out.println("TOP type = " + type + " at " + A.seconds());
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
        if (Count.inQueueOrUnfinished(tech, 8) > 0) {
            return false;
        }

        ProductionOrder productionOrder = new ProductionOrder(tech, 0);

        if (tech.equals(TechType.Tank_Siege_Mode)) {
            productionOrder.setPriority(ProductionOrderPriority.TOP);
        }

        ProductionQueue.addToQueue(0, productionOrder);
        return true;
    }

    public static boolean upgrade(UpgradeType upgrade) {
        if (Count.inQueueOrUnfinished(upgrade, 3) > 0) {
            return false;
        }

        ProductionQueue.addToQueue(0, new ProductionOrder(upgrade, 0));
        return true;
    }

    // =========================================================

    private static ProductionOrder addToQueue(AUnitType type, HasPosition position, int index) {
        assert type != null;

        if (We.protoss() && type.isBuilding() && (!type.isPylon() && !type.isBase()) && Count.pylons() == 0) {
            if (A.seconds() < 200) {
                System.out.println("PREVENT " + type + " from being built. Enforce Pylon first.");
            }
            return null;
        }

//        if (type.isBuilding()) {
//            System.err.println("At " + A.seconds() + "s added to QUEUE > " + type + " <");
//            System.err.println("Reserved: minerals(" + A.reservedMinerals() + "), gas(" + A.reservedGas() + ")");
//            A.printStackTrace("At " + A.seconds() + "s added to QUEUE > " + type + " <");
//        }

//        if (!allowToQueueRequiredBuildings(type)) {
        int minSupply = 0;
        ProductionOrder productionOrder = new ProductionOrder(type, position, minSupply);
        ProductionQueue.addToQueue(index, productionOrder);
//        System.out.println("productionOrder = " + productionOrder);

        return productionOrder;
//        }
//        else {
//            if (
//                    type.getWhatIsRequired() != null
//                            && !type.getWhatIsRequired().isPylon()
//                            && !type.getWhatIsRequired().isPrimaryBase()
//                            && !Requirements.hasRequirements(type)
//            ) {
//                if (!ProductionQueue.isAtTheTopOfQueue(type, 6)) {
//                    System.out.println("FIRST ADD REQUIREMENT = " + type.getWhatIsRequired() + " // " + type.getWhatBuildsIt() + " (for " + type + ")");
//                    addToQueue(type.getWhatIsRequired(), null, 0);
//                    return true;
//                }
//            }
//        }
//        return false;
    }

    // =========================================================

//    private static boolean allowToQueueRequiredBuildings(AUnitType type) {
//        return type.isCombatBuilding();
//    }

    private static int indexForPriority(ProductionOrderPriority priority) {
        return ProductionQueue.countOrdersWithPriorityAtLeast(priority);
    }

    protected static boolean addToQueue(AUnitType type) {
//        if (AGame.supplyFree() == 0) {
//            return false;
//        }

//        if (!AGame.canAffordWithReserved(Math.max(80, type.getMineralPrice()), type.getGasPrice())) {
//            return false;
//        }

        withStandardPriority(type);
        return true;
    }

    public static boolean addToQueueIfNotAlreadyThere(AUnitType type) {
        if (ProductionQueue.countInQueue(type, 5) == 0) {
            return addToQueue(type);
        }

        return false;
    }

    public static boolean maxAtATime(AUnitType type, int maxAtATime) {
        if (ProductionQueue.countInQueue(type, 20) < maxAtATime) {
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
