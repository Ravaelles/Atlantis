package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.*;

public class ProduceShuttle {
    public static final AUnitType type = Protoss_Shuttle;

    public static boolean shuttles() {
//        if (true) return false;

        if (!Have.roboticsFacility()) return false;
        if (!Have.observer() && EnemyInfo.hasHiddenUnits()) return false;

        int shuttles = Count.shuttles();
        if (shuttles == 0 && A.hasMinerals(650)) return produceShuttle();

        int reavers = Count.reavers();
        if (reavers == 0) return false;

        if (reavers < shuttles) return false;
        if (shuttles == 0 && A.hasMinerals(580)) return produceShuttle();

        if (A.supplyUsed() >= 100) {
            if (reavers > shuttles) {
                return produceShuttle();
            }
        }

        return false;
//        return buildToHave(AUnitType.Protoss_Shuttle, 1);
    }

    private static boolean produceShuttle() {
        AUnit building = Select.ourFree(Protoss_Robotics_Facility).mostDistantTo(Select.mainOrAnyBuilding());
        if (building == null) return false;

        return building.train(
            type, ForcedDirectProductionOrder.create(Protoss_Shuttle)
        );
    }

}
