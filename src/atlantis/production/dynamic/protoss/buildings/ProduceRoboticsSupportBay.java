package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Robotics_Facility;
import static atlantis.units.AUnitType.Protoss_Robotics_Support_Bay;

public class ProduceRoboticsSupportBay {
    public static void produce() {
        if (true) return;

        if (!A.supplyUsed(80)) return;
        if (Have.notEvenPlanned(Protoss_Robotics_Facility) || Have.a(Protoss_Robotics_Support_Bay)) return;

        if (Have.dontHaveEvenInPlans(Protoss_Robotics_Support_Bay)) {
            DynamicCommanderHelpers.buildNow(Protoss_Robotics_Support_Bay);
        }
    }
}
