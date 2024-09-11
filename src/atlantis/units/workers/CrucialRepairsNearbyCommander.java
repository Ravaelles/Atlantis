package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class CrucialRepairsNearbyCommander extends Commander {
    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected void handle() {
        if (A.everyFrameExceptNthFrame(17) || !A.hasMinerals(16)) return;
        if (tooManyRepairers()) return;

        for (AUnit worker : FreeWorkers.get().list()) {
            (new DoRepairsNearby(worker)).invokeFrom(this);
        }
    }

    private boolean tooManyRepairers() {
        int totalRepairers = RepairAssignments.countTotalRepairers();
        return totalRepairers >= 8 || (totalRepairers >= 12 && totalRepairers >= Count.workers() / 3);
    }
}
