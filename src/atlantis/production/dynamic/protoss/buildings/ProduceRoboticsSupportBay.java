package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Robotics_Facility;
import static atlantis.units.AUnitType.Protoss_Robotics_Support_Bay;

public class ProduceRoboticsSupportBay {
    public static boolean produce() {
        if (!A.supplyUsed(Enemy.protoss() ? 90 : 80)) return false;
//        if (Have.notEvenPlanned(Protoss_Robotics_Facility) || Have.a(Protoss_Robotics_Support_Bay)) return;

        if (Have.dontHaveEvenInPlans(Protoss_Robotics_Facility)) {
            return DynamicCommanderHelpers.buildNow(Protoss_Robotics_Facility);
        }

        if (Have.dontHaveEvenInPlans(Protoss_Robotics_Support_Bay)) {
            return DynamicCommanderHelpers.buildNow(Protoss_Robotics_Support_Bay);
        }

        return false;
    }
}
