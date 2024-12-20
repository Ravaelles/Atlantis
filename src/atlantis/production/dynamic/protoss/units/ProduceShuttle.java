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

        if (Count.reavers() > 0 && Count.shuttles() == 0) return produceShuttle();

        if (A.supplyUsed() <= 100) {
            if (
                Have.notEvenPlanned(AUnitType.Protoss_Robotics_Facility)
                    || Count.ofType(AUnitType.Protoss_Reaver) >= Count.ofType(AUnitType.Protoss_Shuttle)
            ) {
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
