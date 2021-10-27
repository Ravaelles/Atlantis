package atlantis.production.orders;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

import java.util.ArrayList;

/**
 * Represents sequence of commands to produce units/buildings.
 */
public abstract class ABuildOrder {

    /**
     * Relative path to build order file as seen from project root.
     */
    private final String name;

    /**
     * Sequence of units/buildings/techs to be produced in this build.
     */
    private final ArrayList<ProductionOrder> productionOrders;

    // === Constructor =========================================

    public ABuildOrder(String name, ArrayList<ProductionOrder> productionOrders) {
        this.name = name;
        this.productionOrders = productionOrders;
    }

    // === Abstract methods ====================================
    
    /**
     * Request to produce worker (Zerg Drone, Terran SCV or Protoss Probe) that should be handled according to
     * the race played.
     *
     * See ADynamicWorkerProductionManager which is also used to produce workers.
     */
    public boolean produceWorker() {
        if (!AGame.canAfford(50, 0) || AGame.getSupplyFree() < 1) {
            return false;
        }

        AUnit building = Select.ourOneIdle(AtlantisConfig.BASE);
        if (building != null) {
            return building.train(AtlantisConfig.WORKER);
        }

        // If we're here it means all bases are busy. Try queue request
        for (AUnit base : Select.ourBases().reverse().list()) {
            if (
                    base.getRemainingTrainTime() <= 4
                            && base.hasNothingInQueue()
                            && AGame.getSupplyFree() >= 2
            ) {
                return base.train(AtlantisConfig.WORKER);
            }
        }

        return false;
    }

    /**
     * Request to produce non-building and non-worker unit. Should be handled according to the race played.
     */
    public abstract boolean produceUnit(AUnitType unitType);

    /**
     * When production orders run out, we should always produce some units.
     */
    public abstract ArrayList<AUnitType> produceWhenNoProductionOrders();

    // === Getters =============================================

    /**
     * Returns relative file path as seen from project root.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns human readable name of the file.
     */
    @Override
    public String toString() {
        String name = this.name;
        
        name = name.substring(name.lastIndexOf("/") + 1);
        name = name.substring(0, name.lastIndexOf(".txt"));
        
        return name;
    }

    public ArrayList<ProductionOrder> productionOrders() {
        return productionOrders;
    }
}
