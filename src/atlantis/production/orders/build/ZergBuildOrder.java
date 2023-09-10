package atlantis.production.orders.build;

import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class ZergBuildOrder extends ABuildOrder {

//    public static final ZergBuildOrder ZERG_13_POOL_MUTA = new ZergBuildOrder("13 Pool Muta");

    // =========================================================

    public ZergBuildOrder(String name) {
        super(name);
    }

    // =========================================================

    @Override
    public boolean produceUnit(AUnitType type) {
        return produceZergUnit(type);
    }

    // =========================================================

    /**
     * Produce zerg unit from free larva. Will do nothing if no free larva is available.
     */
    public static boolean produceZergUnit(AUnitType type) {
        for (AUnit base : Select.ourBases().list()) {
            for (AUnit larva : base.getLarva()) {
                try {
                    base.train(type);
                } catch (Exception e) {
                    ErrorLog.printMaxOncePerMinute("" +
                        "Exception in produceZergUnit: " + type + " // " + base
                        + " / " + e.getMessage()
                    );
                }
                return true;
            }
        }
        return false;
    }

    public static boolean produceZergBuilding(AUnitType type, ProductionOrder order) {
        if (type == null) {
            System.err.println("produceZergBuilding got type = null");
            return false;
        }
        else if (order.unitType() == null) {
            System.err.println("produceZergBuilding got order.unitType = null");
            return false;
        }

        if (type.isSunken()) {
            return morphBuildingFromTo(AUnitType.Zerg_Creep_Colony, type);
        }
        else if (type.isSporeColony()) {
            return morphBuildingFromTo(AUnitType.Zerg_Creep_Colony, type);
        }
        else if (type.isLair()) {
            return morphBuildingFromTo(AUnitType.Zerg_Hatchery, type);
        }
        else if (type.isHive()) {
            return morphBuildingFromTo(AUnitType.Zerg_Lair, type);
        }
        else if (type.isGreaterSpire()) {
            return morphBuildingFromTo(AUnitType.Zerg_Spire, type);
        }

        return NewConstructionRequest.requestConstructionOf(order);
    }

    private static boolean morphBuildingFromTo(AUnitType from, AUnitType into) {
        AUnit fromUnit = Select.ourOfType(from).last();
        if (fromUnit != null) {
            return fromUnit.morph(into);
        }

        return false;
    }

}
