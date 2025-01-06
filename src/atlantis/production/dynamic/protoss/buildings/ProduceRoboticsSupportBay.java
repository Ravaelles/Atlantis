package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Robotics_Facility;
import static atlantis.units.AUnitType.Protoss_Robotics_Support_Bay;

public class ProduceRoboticsSupportBay {
    public static final AUnitType type = Protoss_Robotics_Support_Bay;

    public static boolean produce() {
//        if (true) return false;

        if (A.supplyUsed() <= 49) return false;

        if (Count.ourOfTypeUnfinished(type) >= 1) return false;

        if (A.supplyUsed() >= (120 - OurArmy.strength() >= 140 ? 40 : 0)) return produceSupportBay();
        if (A.supplyUsed() >= 50 && A.canAfford(500, 180)) return produceSupportBay();

//        if (Enemy.zerg()) return false;
        if (!A.supplyUsed(atSupply())) return false;
//        if (Have.notEvenPlanned(Protoss_Robotics_Facility) || Have.a(Protoss_Robotics_Support_Bay)) return;

        if (Have.notEvenPlanned(Protoss_Robotics_Facility)) {
            AddToQueue.withTopPriority(Protoss_Robotics_Facility);
            return produceSupportBay();
        }

        if (Have.notEvenPlanned(type)) {
            return produceSupportBay();
        }

        return false;
    }

    private static boolean produceSupportBay() {
        return AddToQueue.withHighPriority(type) != null;
    }

    private static int atSupply() {
        if (Enemy.zerg()) {
            if (
                A.supplyUsed() >= 70
                    && OurArmy.strength() >= 150
                    && !EnemyStrategy.get().isAirUnits()
            ) return A.supplyUsed();

            if (A.supplyUsed() >= 70 && A.hasMinerals(600)) return 70;

            return 110;
        }

        if (Enemy.protoss()) return 130;

        return 140;
    }
}
