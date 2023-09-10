package atlantis.production.orders.build;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

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
