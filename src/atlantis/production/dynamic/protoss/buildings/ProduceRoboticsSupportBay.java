package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Robotics_Facility;
import static atlantis.units.AUnitType.Protoss_Robotics_Support_Bay;

public class ProduceRoboticsSupportBay {
    public static boolean produce() {
//        if (true) return false;

        if (Enemy.zerg()) return false;
        if (!A.supplyUsed(atSupply())) return false;
//        if (Have.notEvenPlanned(Protoss_Robotics_Facility) || Have.a(Protoss_Robotics_Support_Bay)) return;

        if (Have.dontHaveEvenInPlans(Protoss_Robotics_Facility)) {
            return DynamicCommanderHelpers.buildNow(Protoss_Robotics_Facility);
        }

        if (Have.dontHaveEvenInPlans(Protoss_Robotics_Support_Bay)) {
            return DynamicCommanderHelpers.buildNow(Protoss_Robotics_Support_Bay);
        }

        return false;
    }

    private static int atSupply() {
        if (Enemy.zerg()) {
            if (
                A.supplyUsed() >= 70
                    && OurArmy.strength() >= 150
                    && !EnemyStrategy.get().isAirUnits()
            ) return A.supplyUsed();

            if (A.supplyUsed() >= 100 && A.hasMinerals(600)) return A.supplyUsed();

            return 130;
        }

        if (Enemy.protoss()) return 130;

        return 140;
    }
}
