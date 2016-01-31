package atlantis.constructing;

import atlantis.wrappers.MappingCounter;
import atlantis.wrappers.Units;
import java.util.ArrayList;
import jnibwapi.Unit;

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
    public static void handleWarpingNewBuilding(Unit newBuilding) {
        ArrayList<ConstructionOrder> notStartedConstructions = 
                AtlantisConstructingManager.getNotStartedConstructionsOfType(newBuilding.getType());
        
        // Find a Probe-builder closest to the constructions of given type and cancel this construction
        Units closestBuilders = new Units();
        for (ConstructionOrder order : notStartedConstructions) {
            if (order.getBuilder() == null || order.getPositionToBuild() == null) {
                continue;
            }
            int distBuilderToConstruction = (int) (order.getPositionToBuild().distanceTo(order.getBuilder()) * 10);
            closestBuilders.changeValueBy(order.getBuilder(), distBuilderToConstruction);
        }
        
        // =========================================================
        
        if (!closestBuilders.isEmpty()) {
            Unit closestBuilder = closestBuilders.getUnitWithLowestValue();
            
            // Assume that closest builder is the one that has just constructed a building.
            if (closestBuilder != null) {
                ConstructionOrder order = AtlantisConstructingManager.getConstructionOrderFor(closestBuilder);
                if (order != null) {
                    order.cancel();
                }
            }
        }
    }
    
}
