package atlantis.production.orders;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.production.ProductionOrder;
import atlantis.production.Requirements;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AddToQueue {

    public static void withTopPriority(AUnitType type) {
        withTopPriority(type, null);
    }

    public static void withTopPriority(AUnitType type, APosition position) {
        addToQueue(type, position, indexForPriority(ProductionOrderPriority.TOP));
    }

    public static void withHighPriority(AUnitType type) {
        withHighPriority(type, null);
    }

    public static void withHighPriority(AUnitType type, HasPosition position) {
        addToQueue(type, position != null ? position.position() : null, indexForPriority(ProductionOrderPriority.HIGH));
    }

    public static void withStandardPriority(AUnitType type) {
        withStandardPriority(type, null);
    }

    public static void withStandardPriority(AUnitType type, HasPosition position) {
        addToQueue(type, position != null ? position.position() : null, indexForPriority(ProductionOrderPriority.STANDARD));
    }

    // =========================================================

    private static void addToQueue(AUnitType type, APosition position, int index) {
        assert type != null;

        if (We.protoss() && type.isBuilding() && (!type.isPylon() && !type.isBase()) && Count.pylons() == 0) {
            System.out.println("PREVENT " + type + " from being built. Enforce Pylon first.");
            return;
        }

        if (!allowToQueueRequiredBuildings(type)) {
            int minSupply = 0;
            ProductionOrder productionOrder = new ProductionOrder(type, position, minSupply);
            ProductionQueue.currentProductionQueue.add(index, productionOrder);
        }
        else {
            if (
                    type.getWhatIsRequired() != null
                            && !type.getWhatIsRequired().isPylon()
                            && !type.getWhatIsRequired().isPrimaryBase()
                            && !Requirements.hasRequirements(type)
            ) {
                if (!ProductionQueue.isAtTheTopOfQueue(type, 6)) {
                    System.out.println("FIRST ADD REQUIREMENT = " + type.getWhatIsRequired() + " // " + type.getWhatBuildsIt() + " (for " + type + ")");
                    addToQueue(type.getWhatIsRequired(), null, 0);
                }
            }
        }
    }

    // =========================================================

    private static boolean allowToQueueRequiredBuildings(AUnitType type) {
        return type.isCombatBuilding();
    }

    private static int indexForPriority(ProductionOrderPriority priority) {
        return ProductionQueue.countOrdersWithPriorityAtLeast(priority);
    }

}
