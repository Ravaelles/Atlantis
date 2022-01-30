package atlantis.production.orders.build;

import atlantis.config.AtlantisConfig;
import atlantis.game.AGame;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.TreeMap;

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
    private ArrayList<ProductionOrder> productionOrders = new ArrayList<>();

    /**
     * Special settings associated with this build order e.g. change mission to attack
     * or auto produce workers if have more than N workers.
     */
    protected TreeMap<String, BuildOrderSetting> settings = new TreeMap<>();

    // === Constructor =========================================

    public ABuildOrder(String name) {
        this.name = name;
    }

    // === Abstract methods ====================================
    
    /**
     * Request to produce worker (Zerg Drone, Terran SCV or Protoss Probe) that should be handled according to
     * the race played.
     *
     * See ADynamicWorkerProductionManager which is also used to produce workers.
     */
    public boolean produceWorker(AUnit base) {
        if (!AGame.canAfford(50, 0) || AGame.supplyFree() < 1) {
            return false;
        }

        if (base != null) {
            return base.train(AtlantisConfig.WORKER);
        }

        // If we're here it means all bases are busy. Try queue request
        for (AUnit anotherBase : Select.ourBases().reverse().list()) {
            if (
                    anotherBase.remainingTrainTime() <= 4
                            && anotherBase.hasNothingInQueue()
                            && AGame.supplyFree() >= 2
            ) {
                anotherBase.train(AtlantisConfig.WORKER);
                return true;
            }
        }

        return false;
    }

    /**
     * Request to produce non-building and non-worker unit. Should be handled according to the race played.
     */
    public abstract boolean produceUnit(AUnitType unitType);

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

    public void print() {
        System.out.println("--- Full production order list ---");
        for (ProductionOrder productionOrder : productionOrders()) {
            System.out.println("   - " + productionOrder.toString());
        }
        System.out.println("--- END OF production order list ---");
    }

    // === Getters =============================================

    /**
     * Returns relative file path as seen from project root.
     */
    public String getName() {
        return name;
    }

    public ArrayList<ProductionOrder> productionOrders() {
        return productionOrders;
    }

    public void useProductionOrdersLoadedFromFile(ArrayList<ProductionOrder> productionOrders) {
        this.productionOrders = productionOrders;
    }

    public void addSetting(String key, int value) {
        if (key.charAt(0) == '#') {
            key = key.substring(1);
        }

        BuildOrderSetting setting = new BuildOrderSetting(key, value);
        settings.put(key, setting);
    }

}
