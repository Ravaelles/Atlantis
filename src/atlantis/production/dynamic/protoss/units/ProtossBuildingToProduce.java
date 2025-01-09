package atlantis.production.dynamic.protoss.units;

import atlantis.information.enemy.EnemyUnits;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossBuildingToProduce {
    public static boolean produce(AUnitType unitToProduce) {
        AUnit building = defineBestBuilding(unitToProduce);

        if (building == null) {
            return false;
        }

        return building.train(unitToProduce, ForcedDirectProductionOrder.create(unitToProduce));
    }

    private static AUnit defineBestBuilding(AUnitType unitToProduce) {
        AUnitType buildingType = unitToProduce.whatBuildsIt();
        Selection buildings = Select.ourFree(buildingType);
        AUnit nearestEnemyBuilding = EnemyUnits.nearestEnemyBuilding();

        AUnit building;
        if (nearestEnemyBuilding != null) building = buildings.groundNearestTo(nearestEnemyBuilding);
        else building = buildings.mostDistantToBase();

        return building;
    }
}
