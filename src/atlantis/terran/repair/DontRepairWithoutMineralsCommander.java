package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.production.orders.production.ProductionQueue;

public class DontRepairWithoutMineralsCommander extends Commander {
    @Override
    protected void handle() {
        int totalRepairers = RepairAssignments.countTotalRepairers();
        int minMineralsForRepairers = totalRepairers * 20;
        if (totalRepairers >= 1 && ProductionQueue.mineralsReserved() <= minMineralsForRepairers) {
            ProductionQueue.setMineralsNeeded(
                Math.max(ProductionQueue.mineralsNeeded(), minMineralsForRepairers)
            );
        }
    }
}
