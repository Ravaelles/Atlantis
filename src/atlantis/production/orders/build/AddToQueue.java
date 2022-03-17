package atlantis.production.orders.build;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.production.ProductionOrderPriority;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

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

}
