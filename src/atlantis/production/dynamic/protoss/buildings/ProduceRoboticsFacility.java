package atlantis.production.dynamic.protoss.buildings;

import atlantis.information.decisions.Decisions;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Robotics_Facility;

public class ProduceRoboticsFacility {
    public static void produce() {
        if (!Decisions.buildRoboticsFacility()) {
            return;
        }

        if (Have.notEvenPlanned(Protoss_Robotics_Facility)) {
            AddToQueue.withHighPriority(Protoss_Robotics_Facility);
            return;
        }

        if (Count.withPlanned(Protoss_Robotics_Facility) == 0) {
            DynamicCommanderHelpers.buildNow(Protoss_Robotics_Facility);
        }
    }
}
