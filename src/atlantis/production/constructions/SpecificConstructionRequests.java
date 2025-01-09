package atlantis.production.constructions;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.production.constructions.position.terran.TerranAddonBuilder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;


public class SpecificConstructionRequests {

    /**
     * Some buildings like Zerg SUnken Colony need special treatment.
     */
    protected static ProductionOrder handledAsSpecialBuilding(AUnitType building, ProductionOrder order) {
        if (We.protoss()) return null;

        if (handledTerranSpecialBuilding(building, order)) return order;
        if (handledZergSpecialBuilding(building, order)) return order;

        return null;
    }

    // === Terran ========================================

    private static boolean handledTerranSpecialBuilding(AUnitType building, ProductionOrder order) {
        if (!We.terran()) return false;

        if (building.isAddon()) {
            TerranAddonBuilder.buildNewAddon(building, order);
            return true;
        }

        return false;
    }

    // === Zerg ========================================

    private static boolean handledZergSpecialBuilding(AUnitType building, ProductionOrder order) {
        if (!We.zerg()) return false;

        if (building.equals(AUnitType.Zerg_Sunken_Colony)) {
            ZergCreepColony.creepOneIntoSunkenColony(order);
            return true;
        }

        else if (building.is(AUnitType.Zerg_Lair)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair, order);
            return true;
        }

        else if (building.is(AUnitType.Zerg_Hive)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Lair, AUnitType.Zerg_Hive, order);
            return true;
        }

        else if (building.is(AUnitType.Zerg_Greater_Spire)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Spire, AUnitType.Zerg_Greater_Spire, order);
            return true;
        }

        return false;
    }

    private static void morphFromZergBuildingInto(AUnitType from, AUnitType into, ProductionOrder order) {
        AUnit building = Select.ourBuildings().ofType(from).first();
        if (building == null) {
            System.err.println("No " + from + " found to morph into " + into);
        }
        else {
            building.morph(into, order);
        }
    }

}
