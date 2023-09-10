package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.production.orders.production.queue.ResourcesReserved;


public class DontRepairWithoutMineralsCommander extends Commander {
    @Override
    protected void handle() {
        int totalRepairers = RepairAssignments.countTotalRepairers();
        int minMineralsForRepairers = totalRepairers * 20;
        if (totalRepairers >= 1 && ResourcesReserved.minerals() <= minMineralsForRepairers) {
            ResourcesReserved.reserveMinerals(
                Math.max(ResourcesReserved.minerals(), minMineralsForRepairers)
            );
        }
    }
}
