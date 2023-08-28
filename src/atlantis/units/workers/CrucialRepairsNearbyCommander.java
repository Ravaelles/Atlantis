package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class CrucialRepairsNearbyCommander extends Commander {
    @Override
    protected void handle() {
        if (A.everyFrameExceptNthFrame(17) || !A.hasMinerals(16)) return;
        if (tooManyRepairers()) return;

        for (AUnit worker : Select.ourWorkers().list()) {
            (new CrucialRepairsNearby(worker)).invoke();
        }
    }

    private boolean tooManyRepairers() {
        int totalRepairers = RepairAssignments.countTotalRepairers();
        return totalRepairers >= 8 || (totalRepairers >= 12 && totalRepairers >= Count.workers() / 3);
    }
}