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
        addToQueue(type, position, topPriorityIndex(type));
    }

    public static void addWithHighPriority(AUnitType type) {
        addWithHighPriority(type, null);
    }

    public static void addWithHighPriority(AUnitType type, APosition position) {
        addToQueue(type, position, ProductionQueue.currentProductionQueue.isEmpty() ? 0 : 1);
    }

    // =========================================================

    private static void addToQueue(AUnitType type, APosition position, int index) {
        ProductionOrder productionOrder = new ProductionOrder(type, position);
        ProductionQueue.currentProductionQueue.add(index, productionOrder);

        if (!Requirements.hasRequirements(type.getWhatIsRequired())) {
            System.out.println("FIRST ADD EQUIREMENT = " + type.getWhatIsRequired() + " (for " + type + ")");
            addToQueue(type.getWhatIsRequired(), null, 0);
        }
    }

    // =========================================================

    private static int topPriorityIndex(AUnitType type) {
        if (Us.isProtoss()) {
            return Count.ofType(AUnitType.Protoss_Pylon) > 0 ? 0 : ProductionQueue.queueIndexOf(AUnitType.Protoss_Pylon);
        }
    }

}
