package atlantis.production.dynamic.protoss.buildings;

import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Observatory;
import static atlantis.units.AUnitType.Protoss_Robotics_Facility;

public class ProduceObservatory {
    public static void produce() {
        if (Have.a(Protoss_Observatory) || Have.notEvenPlanned(Protoss_Robotics_Facility)) {
            return;
        }

        if (Count.withPlanned(Protoss_Observatory) == 0) {
            DynamicCommanderHelpers.buildNow(Protoss_Observatory);
        }
    }
}
