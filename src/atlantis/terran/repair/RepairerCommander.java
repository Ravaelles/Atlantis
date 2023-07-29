package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.units.AUnit;

import java.util.Iterator;

public class RepairerCommander  extends Commander {
    @Override
    public void handle() {
        for (Iterator<AUnit> iterator = RepairAssignments.getRepairers().iterator(); iterator.hasNext();) {
            AUnit repairer = iterator.next();

            if (repairer.isProtector()) {
                continue;
            }

            if (!repairer.isAlive()) {
                RepairAssignments.removeRepairer(repairer);
                iterator.remove();
                continue;
            }

            (new RepairerManager(repairer)).handle();
        }
    }
}