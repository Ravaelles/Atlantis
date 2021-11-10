package atlantis.production.orders;

import atlantis.position.HasPosition;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AddToQueue {

    public static boolean withTopPriority(AUnitType type) {
        return withTopPriority(type, null);
    }

    public static boolean withTopPriority(AUnitType type, HasPosition position) {
        return addToQueue(type, position, indexForPriority(ProductionOrderPriority.TOP));
    }

    public static boolean withHighPriority(AUnitType type) {
        return withHighPriority(type, null);
    }

    public static boolean withHighPriority(AUnitType type, HasPosition position) {
        return addToQueue(type, position != null ? position.position() : null, indexForPriority(ProductionOrderPriority.HIGH));
    }

    public static boolean withStandardPriority(AUnitType type) {
        return withStandardPriority(type, null);
    }

    public static boolean withStandardPriority(AUnitType type, HasPosition position) {
        return addToQueue(type, position != null ? position.position() : null, indexForPriority(ProductionOrderPriority.STANDARD));
    }

    // =========================================================

    private static boolean addToQueue(AUnitType type, HasPosition position, int index) {
        assert type != null;

        if (We.protoss() && type.isBuilding() && (!type.isPylon() && !type.isBase()) && Count.pylons() == 0) {
            System.out.println("PREVENT " + type + " from being built. Enforce Pylon first.");
            return false;
        }

//        if (!allowToQueueRequiredBuildings(type)) {
        int minSupply = 0;
        ProductionOrder productionOrder = new ProductionOrder(type, position, minSupply);
        ProductionQueue.nextInQueue.add(index, productionOrder);
        return true;
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
