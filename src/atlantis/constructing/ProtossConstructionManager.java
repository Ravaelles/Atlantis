package atlantis.constructing;

import atlantis.units.AUnit;
import atlantis.units.Units;
import java.util.ArrayList;


/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ProtossConstructionManager {

    /**
     * Handle construction fix: detect new Protoss buildings and remove them from construction queue.
     * It's because the construction of Protoss buildings is immediate and we have no way of telling
     * that the Probe has actually started a construction.
     */
    public static void handleWarpingNewBuilding(AUnit newBuilding) {
        ArrayList<ConstructionOrder> notStartedConstructions = 
                AConstructionManager.getNotStartedConstructionsOfType(newBuilding.getType());
        
        // Find a Probe-builder closest to the constructions of given type and cancel this construction
        Units closestBuilders = new Units();
        for (ConstructionOrder order : notStartedConstructions) {
            if (order.getBuilder() == null || order.getPositionToBuild() == null) {
                continue;
            }
            int distBuilderToConstruction = (int) (order.getBuilder().distanceTo(order.getPositionToBuild())) * 10;
            closestBuilders.changeValueBy(order.getBuilder(), distBuilderToConstruction);
        }
        
        // =========================================================
        
        if (!closestBuilders.isEmpty()) {
            AUnit closestBuilder = closestBuilders.getUnitWithLowestValue();
            
            // Assume that closest builder is the one that has just constructed a building.
            if (closestBuilder != null) {
                ConstructionOrder order = AConstructionManager.getConstructionOrderFor(closestBuilder);
                if (order != null) {
                    order.cancel();
                }
            }
        }
    }
    
}
