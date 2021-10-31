package atlantis.production.orders;

import atlantis.position.APosition;
import atlantis.production.ProductionOrder;
import atlantis.production.Requirements;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Us;

public class AddToQueue {

    public static void addWithTopPriority(AUnitType type) {
        addWithTopPriority(type, null);
    }

    public static void addWithTopPriority(AUnitType type, APosition position) {
        addToQueue(type, position, indexForPriority(ProductionOrderPriority.TOP));
    }

    public static void addWithHighPriority(AUnitType type) {
        addWithHighPriority(type, null);
    }

    public static void addWithHighPriority(AUnitType type, APosition position) {
        addToQueue(type, position, indexForPriority(ProductionOrderPriority.HIGH));
    }

    // =========================================================

    private static void addToQueue(AUnitType type, APosition position, int index) {
        if (Us.isProtoss() && type.isBuilding() && !type.isPylon() && Count.pylons() == 0) {
            System.out.println("PREVENT " + type + " from being built. Enforce Pylon first.");
            return;
        }

        ProductionOrder productionOrder = new ProductionOrder(type, position);
        ProductionQueue.currentProductionQueue.add(index, productionOrder);

        if (type.getWhatIsRequired() != null && !Requirements.hasRequirements(type)) {
            if (!ProductionQueue.isAtTheTopOfQueue(type, 6)) {
                System.out.println("FIRST ADD REQUIREMENT = " + type.getWhatIsRequired() + " // " + type.getWhatBuildsIt() + " (for " + type + ")");
                addToQueue(type.getWhatIsRequired(), null, 0);
            }
        }
    }

    // =========================================================

    private static int indexForPriority(ProductionOrderPriority priority) {
        return ProductionQueue.countOrdersWithPriorityAtLeast(priority);
    }

}
