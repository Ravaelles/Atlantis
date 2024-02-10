package atlantis.production.orders.zerg;

import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProduceZergUnit {
    /**
     * Produce zerg unit from free larva. Will do nothing if no free larva is available.
     */
    public static boolean produceZergUnit(AUnitType type, ProductionOrder productionOrder) {
//        for (AUnit base : Select.ourBases().list()) {

        for (AUnit larva : Select.ourOfType(AUnitType.Zerg_Larva).list()) {
            try {
                larva.train(type, productionOrder);
                return true;
            } catch (Exception e) {
//                ErrorLog.printMaxOncePerMinute(
//                    "Exception in produceZergUnit: " + type + " // " + larva
//                        + " / " + e.getMessage()
//                );
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
            return morphBuildingFromTo(AUnitType.Zerg_Creep_Colony, type, order);
        }
        else if (type.isSporeColony()) {
            return morphBuildingFromTo(AUnitType.Zerg_Creep_Colony, type, order);
        }
        else if (type.isLair()) {
            return morphBuildingFromTo(AUnitType.Zerg_Hatchery, type, order);
        }
        else if (type.isHive()) {
            return morphBuildingFromTo(AUnitType.Zerg_Lair, type, order);
        }
        else if (type.isGreaterSpire()) {
            return morphBuildingFromTo(AUnitType.Zerg_Spire, type, order);
        }

        return NewConstructionRequest.requestConstructionOf(order);
    }

    private static boolean morphBuildingFromTo(AUnitType from, AUnitType into, ProductionOrder order) {
        AUnit fromUnit = Select.ourOfType(from).last();
        if (fromUnit != null) {
            return fromUnit.morph(into, order);
        }

        return false;
    }
}
