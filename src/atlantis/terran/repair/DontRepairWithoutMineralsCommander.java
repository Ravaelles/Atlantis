package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.production.orders.production.queue.ReservedResources;


public class DontRepairWithoutMineralsCommander extends Commander {
    @Override
    protected void handle() {
        int totalRepairers = RepairAssignments.countTotalRepairers();
        int minMineralsForRepairers = totalRepairers * 20;
        if (totalRepairers >= 1 && ReservedResources.minerals() <= minMineralsForRepairers) {
            ReservedResources.reserveMinerals(
                Math.max(ReservedResources.minerals(), minMineralsForRepairers),
                "repairs"
            );
        }
    }
}
