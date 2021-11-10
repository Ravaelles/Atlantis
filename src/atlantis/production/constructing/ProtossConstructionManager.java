package atlantis.production.constructing;

import atlantis.units.AUnit;
import atlantis.units.Units;
import java.util.ArrayList;

public class ProtossConstructionManager {

    /**
     * Handle construction fix: detect new Protoss buildings and remove them from construction queue.
     * It's because the construction of Protoss buildings is immediate and we have no way of telling
     * that the Probe has actually started a construction.
     */
    public static void handleWarpingNewBuilding(AUnit newBuilding) {
        ArrayList<ConstructionOrder> notStartedConstructions = 
                AConstructionRequests.getNotStartedConstructionsOfType(newBuilding.type());
        
        // Find a Probe-builder closest to the constructions of given type and cancel this construction
        Units closestBuilders = new Units();
        for (ConstructionOrder order : notStartedConstructions) {
            if (order.getBuilder() == null || order.positionToBuild() == null) {
                continue;
            }
            int distBuilderToConstruction = (int) (order.getBuilder().distTo(order.positionToBuild())) * 10;
            closestBuilders.changeValueBy(order.getBuilder(), distBuilderToConstruction);
        }
        
        // =========================================================
        
        if (!closestBuilders.isEmpty()) {
            AUnit closestBuilder = closestBuilders.unitWithLowestValue();
            
            // Assume that closest builder is the one that has just constructed a building.
            if (closestBuilder != null) {
                ConstructionOrder order = AConstructionRequests.getConstructionOrderFor(closestBuilder);
                if (order != null) {
                    order.cancel();
                }
            }
        }
    }
    
}
