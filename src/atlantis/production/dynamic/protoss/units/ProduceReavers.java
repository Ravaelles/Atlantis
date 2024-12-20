package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.production.AbstractDynamicUnits.buildToHave;
import static atlantis.units.AUnitType.Protoss_Robotics_Facility;
import static atlantis.units.AUnitType.Protoss_Zealot;

public class ProduceReavers {
    private static int produced = 0;

    public static boolean reavers() {
//        if (true) return false;

        if (A.supplyUsed() <= 70) return false;

        if (
            Have.no(AUnitType.Protoss_Robotics_Support_Bay)
                || Have.no(AUnitType.Protoss_Robotics_Facility)
        ) return false;

        if (Count.observers() == 0 && ProduceObserver.needObservers()) return false;

        int maxReavers = haveThisManyReavers();
        if (Count.ourOfTypeUnfinished(type()) >= maxReavers) return false;

        return produceReaver();
    }

    private static int haveThisManyReavers() {
        int reavers = Count.reavers();

//        if (Count.ofType(AUnitType.Protoss_Shuttle) > 0 && reavers == 0) return 1;
        if (reavers == 0) return 1;

        int resourcesBonus = A.canAfford(200, 200) ? 2 : 0;

        return
            A.inRange(
                resourcesBonus,
                (Decisions.isEnemyGoingAirAndWeAreNotPreparedEnough() ? 0 : (1 + A.supplyUsed() / 45)),
                4 + resourcesBonus
            );
    }

    private static boolean produceReaver() {
        AUnit facility = Select.ourFree(Protoss_Robotics_Facility).mostDistantTo(Select.mainOrAnyBuilding());
        if (facility == null) return false;

//        System.err.println("YES< zealot");
        return facility.train(
            type(), ForcedDirectProductionOrder.create(type())
        ) && increaseProduced();
    }

    private static boolean increaseProduced() {
        produced++;
        return true;
    }

    private static AUnitType type() {
        return AUnitType.Protoss_Reaver;
    }
}
