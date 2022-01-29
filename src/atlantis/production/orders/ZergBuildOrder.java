package atlantis.production.orders;

import atlantis.config.AtlantisConfig;
import atlantis.production.ProductionOrder;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ZergBuildOrder extends ABuildOrder {
    
//    public static final ZergBuildOrder ZERG_13_POOL_MUTA = new ZergBuildOrder("13 Pool Muta");

    // =========================================================

    public ZergBuildOrder(String name) {
        super(name);
    }

    // =========================================================

    @Override
    public boolean produceWorker(AUnit base) {
        return produceZergUnit(AtlantisConfig.WORKER);
    }

    @Override
    public boolean produceUnit(AUnitType type) {
        return produceZergUnit(type);
    }

    // =========================================================

    /**
     * Produce zerg unit from free larva. Will do nothing if no free larva is available.
     */
    public boolean produceZergUnit(AUnitType type) {
        for (AUnit base : Select.ourBases().list()) {
            for (AUnit larva : base.getLarva()) {
                try {
                    base.train(type);
                } catch (Exception e) {
                    System.err.println("Exception in produceZergUnit: " + type + " // " + base);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean produceZergBuilding(AUnitType type, ProductionOrder order) {
        if (type.isSunken()) {
            return morphBuildingInto(AUnitType.Zerg_Creep_Colony, type);
        }
        else if (type.isSporeColony()) {
            return morphBuildingInto(AUnitType.Zerg_Creep_Colony, type);
        }
        else if (type.isLair()) {
            return morphBuildingInto(AUnitType.Zerg_Hatchery, type);
        }
        else if (type.isHive()) {
            return morphBuildingInto(AUnitType.Zerg_Lair, type);
        }
        else if (type.isGreaterSpire()) {
            return morphBuildingInto(AUnitType.Zerg_Spire, type);
        }

        return ConstructionRequests.requestConstructionOf(order);
    }

    private static boolean morphBuildingInto(AUnitType from, AUnitType into) {
        AUnit fromUnit = Select.ourOfType(from).last();
        if (fromUnit != null) {
            return fromUnit.morph(into);
        }
        
        return false;
    }

}
