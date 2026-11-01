package atlantis.production.constructions;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;


public class SpecificConstructionRequests {

    /**
     * Some buildings like Zerg Sunken Colony need special treatment.
     * Protoss-only (owner #146, migration A4f): the Terran/Zerg
     * special-building paths are deleted - this is the whole method now,
     * a null for every Protoss building.
     */
    protected static ProductionOrder handledAsSpecialBuilding(AUnitType building, ProductionOrder order) {
        return null;
    }

}
