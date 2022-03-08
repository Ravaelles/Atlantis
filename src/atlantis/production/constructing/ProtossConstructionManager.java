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
                ConstructionRequests.notStartedOfType(newBuilding.type());
        
        // Find a Probe-builder closest to the constructions of given type and cancel this construction
        Units closestBuilders = new Units();
        for (ConstructionOrder order : notStartedConstructions) {
            if (order.builder() == null || order.buildPosition() == null) {
                continue;
            }
            int distBuilderToConstruction = (int) (order.builder().distTo(order.buildPosition())) * 10;
            closestBuilders.changeValueBy(order.builder(), distBuilderToConstruction);
        }
        
        // =========================================================
        
        if (!closestBuilders.isEmpty()) {
            AUnit closestBuilder = closestBuilders.unitWithLowestValue();
            
            // Assume that closest builder is the one that has just constructed a building.
            if (closestBuilder != null) {
                ConstructionOrder order = ConstructionRequests.constructionOrderFor(closestBuilder);
                if (order != null) {
                    order.cancel();
                }
            }
        }
    }
    
}
