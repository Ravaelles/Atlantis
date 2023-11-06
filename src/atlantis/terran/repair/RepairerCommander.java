package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.terran.repair.repairer.RepairerManager;
import atlantis.units.AUnit;

import java.util.Iterator;

public class RepairerCommander extends Commander {
    @Override
    protected void handle() {
        boolean noMineralsToContinueRepairs = !A.hasMinerals(4);
        
        for (Iterator<AUnit> iterator = RepairAssignments.getRepairers().iterator(); iterator.hasNext(); ) {
            AUnit repairer = iterator.next();

            if (repairer.isProtector() && (repairer.isRepairing() || repairer.lastActionLessThanAgo(30 * 1))) {
                continue;
            }

            if (noMineralsToContinueRepairs || !repairer.isAlive()) {
                RepairAssignments.removeRepairer(repairer);
                iterator.remove();
                continue;
            }

            (new RepairerManager(repairer)).invoke();
        }
    }
}
